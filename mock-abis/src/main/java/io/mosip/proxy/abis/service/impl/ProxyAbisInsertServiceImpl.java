package io.mosip.proxy.abis.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.biometrics.commons.CbeffValidator;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.core.cbeffutil.exception.CbeffException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.proxy.abis.CryptoCoreUtil;
import io.mosip.proxy.abis.dao.ProxyAbisBioDataRepository;
import io.mosip.proxy.abis.dao.ProxyAbisInsertRepository;
import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.dto.IdentifyDelayResponse;
import io.mosip.proxy.abis.dto.IdentityRequest;
import io.mosip.proxy.abis.dto.IdentityResponse;
import io.mosip.proxy.abis.dto.IdentityResponse.Modalities;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.dto.RequestMO;
import io.mosip.proxy.abis.entity.BiometricData;
import io.mosip.proxy.abis.entity.InsertEntity;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.service.ExpectationCache;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;

@Service
@Configuration
@PropertySource("classpath:config.properties")
public class ProxyAbisInsertServiceImpl implements ProxyAbisInsertService {
	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisInsertServiceImpl.class);

	private static Path currentPath = Paths.get(System.getProperty("user.dir"));
	private static Path keystoreFilePath = Paths.get(currentPath.toString(), "keystore");

	private static String PROPERTIES_FILE_NAME = "partner.properties";

	@Autowired
	ProxyAbisInsertRepository proxyabis;

	@Autowired
	ProxyAbisBioDataRepository proxyAbisBioDataRepository;

	@Autowired
	ProxyAbisConfigService proxyAbisConfigService;

	@Autowired(required = true)
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;

	@Autowired
	CryptoCoreUtil cryptoUtil;

	@Autowired
	private Environment env;

	@Autowired
	private ExpectationCache expectationCache;

	private static String CBEFF_URL = null;

	/**
	 * This flag is added for fast-tracking core ABIS functionality testing without
	 * depending on working environment
	 */
	@Value("${abis.bio.encryption:true}")
	private boolean encryption;

	/**
	 * Mosip host
	 */
	@Value("${mosip_host:#{null}}")
	private String mosipHost;

	@Override
	public int insertData(InsertRequestMO ire) {
		int delayResponse = 0;
		try {
			java.util.Optional<InsertEntity> op = proxyabis.findById(ire.getReferenceId());
			if (!op.isEmpty()) {
				logger.error("Reference Id already exists " + ire.getReferenceId());
				RequestMO re = new RequestMO(ire.getId(), ire.getVersion(), ire.getRequestId(), ire.getRequesttime(),
						ire.getReferenceId());
				throw new RequestException(re, FailureReasonsConstants.REFERENCEID_ALREADY_EXISTS);
			}
			CBEFF_URL = ire.getReferenceURL();
			InsertEntity ie = new InsertEntity(ire.getId(), ire.getVersion(), ire.getRequestId(), ire.getRequesttime(),
					ire.getReferenceId());
			List<BiometricData> lst = fetchCBEFF(ie);
			if (null == lst || lst.size() == 0)
				throw new RequestException(FailureReasonsConstants.INVALID_CBEFF_FORMAT);

			for (BiometricData bdt : lst) {
				Expectation exp = expectationCache.get(bdt.getBioData());
				if (exp.getId() != null && !exp.getId().isEmpty() && exp.getActionToInterfere().equals("Insert")) {
					logger.info("Expectation found for " + exp.getId());
					if (exp.getDelayInExecution() != null && !exp.getDelayInExecution().isEmpty()) {
						delayResponse = Integer.parseInt(exp.getDelayInExecution());
					}
					if (exp.getForcedResponse().equals("Error")) {
						throw new RequestException(exp.getErrorCode(), delayResponse);
					}
				}
			}
			ie.setBiometricList(lst);
			proxyabis.save(ie);
			return delayResponse;
		} catch (CbeffException cbef) {
			logger.error("CBEFF error While inserting data " + cbef.getMessage());
			throw new RequestException(cbef.getMessage(), delayResponse);
		} catch (RequestException rex) {
			rex.setDelayResponse(delayResponse);
			throw rex;
		} catch (Exception exp) {
			logger.error("Error While inserting data " + exp.getMessage());
			throw new RequestException(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, delayResponse);
		}
	}

	private List<BiometricData> fetchCBEFF(InsertEntity ie) throws Exception {
		List<BiometricData> lst = new ArrayList();
		try {
			logger.info("Fetching CBEFF for reference URL-" + CBEFF_URL);
			ResponseEntity<String> cbeffResp = restTemplate.exchange(CBEFF_URL, HttpMethod.GET, null, String.class);
			logger.info("CBEFF response-" + cbeffResp);
			String cbeff = cbeffResp.getBody();
			logger.info("CBEFF Data-" + cbeff);

			try {
				/*
				 * Data Share response { "id": "mosip.data.share", "version": "1.0",
				 * "responsetime": "2023-05-23T12:02:53.601Z", "dataShare": null, "errors": [ {
				 * "errorCode": "DAT-SER-006", "message": "Data share usuage expired" } ] } And
				 * Errors DATA_ENCRYPTION_FAILURE_EXCEPTION("DAT-SER-001",
				 * "Data Encryption failed"), API_NOT_ACCESSIBLE_EXCEPTION("DAT-SER-002",
				 * "API not accessible"), FILE_EXCEPTION("DAT-SER-003",
				 * "File is not exists or File is empty"), URL_CREATION_EXCEPTION("DAT-SER-004",
				 * "URL creation exception"), SIGNATURE_EXCEPTION("DAT-SER-005",
				 * "Failed to generate digital signature"),
				 * DATA_SHARE_NOT_FOUND_EXCEPTION("DAT-SER-006", "Data share not found"),
				 * DATA_SHARE_EXPIRED_EXCEPTION("DAT-SER-006", "Data share usuage expired"),
				 * POLICY_EXCEPTION("DAT-SER-007", "Exception while fetching policy details");
				 * KER-ATH-401 - Authentication Failed KER-ATH-403 - Forbidden
				 */
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(cbeff);
				JSONArray errors = (JSONArray) json.get("errors");
				for (Iterator it = errors.iterator(); it.hasNext();) {
					JSONObject error = (JSONObject) it.next();
					String errorCode = ((String) error.get("errorCode")).trim();
					String message = ((String) error.get("message")).trim();
					logger.info(String.format("ErrorCode[%s], ErrorMessage[%s],", errorCode, message));
					throw new RequestException(errorCode);
				}
			} catch (RequestException ex) {
				if (ex.getReasonConstant().equalsIgnoreCase("DAT-SER-006"))
					throw new RequestException(FailureReasonsConstants.DATA_SHARE_URL_EXPIRED);
				else
					throw new RequestException(FailureReasonsConstants.UNEXPECTED_ERROR);
			} catch (Exception ex) {
				// ex.printStackTrace();
			}

			if (encryption) {
				cbeff = cryptoUtil.decryptCbeff(cbeff);
			}

			logger.info("CBEFF Data-" + cbeff);
			if (cbeff == null || cbeff.isBlank() || cbeff.isEmpty()) {
				logger.info("Error while validating CBEFF");
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
			}

			BIR birType = null;
			try {
				birType = CbeffValidator.getBIRFromXML(IOUtils.toByteArray(cbeff));
				birType.setBirs(
						birType.getBirs().stream().filter(b -> b.getBdb() != null).collect(Collectors.toList()));
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Error while validating CBEFF", ex);
				throw new RequestException(FailureReasonsConstants.INVALID_CBEFF_FORMAT);
			}

			logger.info("Validating CBEFF data");
			try {
				if (!CbeffValidator.validateXML(birType)) {
					logger.error("Error while validating CBEFF");
					throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
				}

				if (birType == null || birType.getBirs().size() == 0)
					throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Error while validating CBEFF Data", ex);
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
			}

			logger.info("Valid CBEFF data");
			logger.info("Inserting biometric details to concerned table" + birType.getBirs().size());

			for (BIR bir : birType.getBirs()) {
				if (bir.getBdb() != null && bir.getBdb().length > 0) {
					BiometricData bd = new BiometricData();
					bd.setType(bir.getBdbInfo().getType().iterator().next().value());
					if (bir.getBdbInfo() == null)
						throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);

					if (bir.getBdbInfo().getSubtype() != null && bir.getBdbInfo().getSubtype().size() > 0)
						bd.setSubtype(bir.getBdbInfo().getSubtype().toString());

					if ((bir.getBdb() == null || bir.getBdb().length <= 0))
						throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);

					String hash = getSHAFromBytes(bir.getBdb());
					bd.setBioData(hash);
					bd.setInsertEntity(ie);

					lst.add(bd);
				} else {
					throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
				}
			}
		} catch (HttpClientErrorException ex) {
			ex.printStackTrace();
			logger.error("issue with httpclient URL " + ex.getLocalizedMessage());
			throw new RequestException(FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
		} catch (URISyntaxException | IllegalArgumentException ex) {
			ex.printStackTrace();
			logger.error("issue with httpclient URL Syntax " + ex.getLocalizedMessage());
			throw new RequestException(FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
		} catch (CbeffException ex) {
			logger.error("issue with cbeff " + ex.getLocalizedMessage());
			throw new RequestException(FailureReasonsConstants.INVALID_CBEFF_FORMAT);
		} catch (Exception ex) {
			logger.error("Issue while getting ,validating and inserting Cbeff" + ex.getLocalizedMessage());
			ex.printStackTrace();
			throw ex;
		}
		return lst;
	}

	@Override
	public void deleteData(String referenceId) {
		logger.info("Deleting reference Id " + referenceId);
		try {
			proxyabis.deleteById(referenceId);
		} catch (Exception e) {
			logger.error("Error while deleting record with reference Id" + referenceId);
			logger.error(e.getMessage());
			throw new RequestException();
		}
	}

	private String getSHA(String cbeffStr) throws NoSuchAlgorithmException {
		logger.info("Getting hash of string");
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return bytesToHex(md.digest(cbeffStr.getBytes(StandardCharsets.UTF_8)));

	}

	private String getSHAFromBytes(byte[] b) throws NoSuchAlgorithmException {
		logger.info("Getting hash of string");
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return bytesToHex(md.digest(b));

	}

	private static String bytesToHex(byte[] hash) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	@Override
	public IdentifyDelayResponse findDuplication(IdentityRequest ir) {
		int delayResponse = 0;
		try {
			String refId = ir.getReferenceId();
			logger.info("Checking for duplication of reference ID " + refId);
			List<BiometricData> lst = null;
			logger.info("find duplicate property set to " + proxyAbisConfigService.getDuplicate());
			logger.info("force duplicate property set to " + proxyAbisConfigService.isForceDuplicate());
			if (ir.getGallery() != null && ir.getGallery().getReferenceIds().size() > 0
					&& ir.getGallery().getReferenceIds().get(0).getReferenceId() != null
					&& !ir.getGallery().getReferenceIds().get(0).getReferenceId().isEmpty()) {
				List<String> referenceIds = new ArrayList();
				ir.getGallery().getReferenceIds().stream().forEach(ref -> referenceIds.add(ref.getReferenceId()));

				logger.info("checking for duplication of reference Id against" + referenceIds.toString());

				int galleryRefIdCountInDB = proxyabis.fetchCountForReferenceIdPresentInGallery(referenceIds);
				if (galleryRefIdCountInDB != referenceIds.size()) {
					logger.info(String.format(
							"checking for reference Id Present in DB[%d], Gallery reference Id list size[%d] ",
							galleryRefIdCountInDB, referenceIds.size()));
					throw new RequestException(FailureReasonsConstants.REFERENCEID_NOT_FOUND);
				}
				List<String> bioValues = proxyAbisBioDataRepository.fetchBioDataByRefId(refId);
				if (!bioValues.isEmpty()) {
					for (String bioValue : bioValues) {
						Expectation exp = expectationCache.get(bioValue);
						if (exp.getId() != null && !exp.getId().isEmpty()
								&& exp.getActionToInterfere().equals("Identify")) {
							logger.info("Expectation found for " + exp.getId());
							if (exp.getDelayInExecution() != null && !exp.getDelayInExecution().isEmpty()) {
								delayResponse = Integer.parseInt(exp.getDelayInExecution());
							}
							return new IdentifyDelayResponse(processExpectation(ir, exp, referenceIds), delayResponse);
						}
					}
				}

				if (proxyAbisConfigService.isForceDuplicate() || proxyAbisConfigService.getDuplicate()) {
					lst = proxyAbisBioDataRepository.fetchDuplicatesForReferenceIdBasedOnGalleryIds(refId,
							referenceIds);
				}
			} else {
				logger.info("checking for duplication in entire DB of reference ID" + refId);
				List<String> bioValues = proxyAbisBioDataRepository.fetchBioDataByRefId(refId);
				if (!bioValues.isEmpty()) {
					for (String bioValue : bioValues) {
						Expectation exp = expectationCache.get(bioValue);
						if (exp.getId() != null && !exp.getId().isEmpty()
								&& exp.getActionToInterfere().equals("Identify")) {
							logger.info("Expectation found for " + exp.getId());
							if (exp.getDelayInExecution() != null && !exp.getDelayInExecution().isEmpty()) {
								delayResponse = Integer.parseInt(exp.getDelayInExecution());
							}
							return new IdentifyDelayResponse(processExpectation(ir, exp, null), delayResponse);
						}
					}
				}
				if (proxyAbisConfigService.isForceDuplicate() || proxyAbisConfigService.getDuplicate()) {
					lst = proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(refId);
				}
			}
			if (lst != null)
				logger.info("Number of duplicate candidates are " + lst.size());
			return new IdentifyDelayResponse(constructIdentityResponse(ir, lst), delayResponse);
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * Constructs a identity response based on the expectaions that are set.
	 * 
	 * @param ir
	 * @param expectation
	 * @return
	 */
	private IdentityResponse processExpectation(IdentityRequest ir, Expectation expectation,
			List<String> galleryReferenceIds) {
		logger.info("processExpectation" + ir.getReferenceId());
		IdentityResponse response = new IdentityResponse();
		response.setId(ir.getId());
		response.setRequestId(ir.getRequestId());
		response.setResponsetime(ir.getRequesttime());
		logger.info("expectation.getForcedResponse()==" + expectation.getForcedResponse());
		logger.info("expectation=>" + expectation);
		if (expectation.getForcedResponse().equals("Error")) {
			throw new RequestException(expectation.getErrorCode());
		} else if (expectation.getForcedResponse().equals("Duplicate")) {
			response.setReturnValue("1");
			IdentityResponse.CandidateList cdl = new IdentityResponse.CandidateList();
			cdl.setCandidates(new ArrayList<>());
			List<Modalities> modalitiesList = new ArrayList<>();
			modalitiesList.add(new Modalities("FACE", getAnalytics()));
			modalitiesList.add(new Modalities("FINGER", getAnalytics()));
			modalitiesList.add(new Modalities("IRIS", getAnalytics()));
			logger.info("expectation.getGallery()=>" + expectation.getGallery());
			if (expectation.getGallery() != null && expectation.getGallery().getReferenceIds().size() > 0) {
				for (Expectation.ReferenceIds rd : expectation.getGallery().getReferenceIds()) {
					System.out.println("rd.getReferenceId()" + rd.getReferenceId());
					List<String> refIds;
					if (galleryReferenceIds != null) {
						refIds = proxyAbisBioDataRepository.fetchByReferenceId(rd.getReferenceId(),
								galleryReferenceIds);
					} else {
						refIds = proxyAbisBioDataRepository.fetchReferenceId(rd.getReferenceId());
					}
					logger.info("expectation.refIds=>" + refIds);
					if (refIds.size() > 0) {
						for (String refId : refIds) {
							cdl.getCandidates()
									.add(new IdentityResponse.Candidates(refId, getAnalytics(), modalitiesList));
						}
					}
				}
				cdl.setCount(cdl.getCandidates().size() + "");
				response.setCandidateList(cdl);
				logger.info("response==" + response);
				return response;
			}
		}
		return constructIdentityResponse(ir, null);
	}

	private IdentityResponse constructIdentityResponse(IdentityRequest ir, List<BiometricData> lst) {
		IdentityResponse response = new IdentityResponse();
		response.setId(ir.getId());
		response.setRequestId(ir.getRequestId());
		response.setReturnValue(1 + "");
		response.setResponsetime(ir.getRequesttime());
		IdentityResponse.CandidateList cl = new IdentityResponse.CandidateList();
		if (null == lst || lst.size() == 0) {
			logger.info("No duplicates found for referenceID" + ir.getReferenceId());
			cl.setCount(0 + "");
			response.setCandidateList(cl);
			return response;
		}
		logger.info("Duplicates found for referenceID" + ir.getReferenceId());
		try {
			Map<String, IdentityResponse.Candidates> mp = new HashMap();
			lst.stream().forEach(bio -> {

				IdentityResponse.Candidates candi = null;
				List<Modalities> modlst = null;
				if (mp.containsKey(bio.getInsertEntity().getReferenceId())) {
					candi = mp.get(bio.getInsertEntity().getReferenceId());
					modlst = candi.getModalities();
				} else {
					candi = new IdentityResponse.Candidates();
					candi.setReferenceId(bio.getInsertEntity().getReferenceId());
					candi.setAnalytics(getAnalytics());
					modlst = new ArrayList();
				}

				IdentityResponse.Modalities md = new IdentityResponse.Modalities();
				md.setBiometricType(bio.getType());
				md.setAnalytics(getAnalytics());
				modlst.add(md);
				candi.setModalities(modlst);
				mp.put(bio.getInsertEntity().getReferenceId(), candi);

			});
			logger.info("Number of duplicates are" + mp.size());
			cl.setCount(mp.size() + "");
			List<IdentityResponse.Candidates> clst = new ArrayList<>();
			mp.entrySet().stream().forEach(e -> {
				clst.add(e.getValue());
			});
			cl.setCandidates(clst);
			response.setCandidateList(cl);
			return response;
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		cl.setCount(0 + "");
		response.setCandidateList(new IdentityResponse.CandidateList());
		return response;
	}

	private IdentityResponse.Analytics getAnalytics() {
		IdentityResponse.Analytics a = new IdentityResponse.Analytics();
		a.setConfidence(env.getProperty("analytics.confidence").toString());
		a.setInternalScore(env.getProperty("analytics.internalscore").toString());
		a.setKey1(env.getProperty("analytics.key1"));
		a.setKey2(env.getProperty("analytics.key2"));
		return a;
	}

	public String saveUploadedFileWithParameters(MultipartFile uploadedFile, String alias, String password,
			String keystore) {
		try {
			logger.info("Uploading certificate");
			byte[] bytes = uploadedFile.getBytes();
			Path path = Paths.get(keystoreFilePath.toString(), uploadedFile.getOriginalFilename());

			File keyFile = new File(path.toString());
			File parent = keyFile.getParentFile();
			if (parent != null && !parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}
			keyFile.createNewFile();
			Files.write(path, bytes);

			path = Paths.get(keystoreFilePath.toString(), PROPERTIES_FILE_NAME);
			FileWriter myWriter = new FileWriter(path.toString());
			myWriter.write("certificate.alias=" + alias + "\n" + "certificate.password=" + password + "\n");
			myWriter.write("certificate.keystore=" + keystore + "\n" + "certificate.filename="
					+ uploadedFile.getOriginalFilename());
			myWriter.close();
			CryptoCoreUtil.setCertificateValues(uploadedFile.getOriginalFilename(), keystore, password, alias);

			File dir = new File(keystoreFilePath.toString());
			File[] fileList = dir.listFiles();
			for (File file : fileList) {
				if (!file.getName().equalsIgnoreCase(uploadedFile.getOriginalFilename())
						&& file.getName().endsWith(".p12")) {
					logger.info("Deleting file" + file.getName());
					file.delete();
					break;
				}
			}
			logger.info("Successfully uploaded certificate");
			return "Successfully uploaded file";
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Could not upload file");
		}
		return "Could not upload file";
	}
}
