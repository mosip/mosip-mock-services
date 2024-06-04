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
import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import io.mosip.proxy.abis.exception.AbisException;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.service.ExpectationCache;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;
import io.mosip.proxy.abis.utility.CryptoCoreUtil;

@Service
@Configuration
@PropertySource("classpath:config.properties")
public class ProxyAbisInsertServiceImpl implements ProxyAbisInsertService {
	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisInsertServiceImpl.class);

	private static Path currentPath = Paths.get(System.getProperty("user.dir"));
	private static Path keystoreFilePath = Paths.get(currentPath.toString(), "keystore");

	private static final String PROPERTIES_FILE_NAME = "partner.properties";

	private ProxyAbisInsertRepository proxyabis;
	private ProxyAbisBioDataRepository proxyAbisBioDataRepository;
	private ProxyAbisConfigService proxyAbisConfigService;

	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;

	private CryptoCoreUtil cryptoUtil;
	private Environment env;
	private ExpectationCache expectationCache;

	private String cbeffURL = null;

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

	@Autowired(required = true)
	public ProxyAbisInsertServiceImpl(ProxyAbisInsertRepository proxyabis,
			ProxyAbisBioDataRepository proxyAbisBioDataRepository, ProxyAbisConfigService proxyAbisConfigService,
			CryptoCoreUtil cryptoUtil, Environment env, ExpectationCache expectationCache) {
		this.proxyabis = proxyabis;
		this.proxyAbisBioDataRepository = proxyAbisBioDataRepository;
		this.proxyAbisConfigService = proxyAbisConfigService;
		this.cryptoUtil = cryptoUtil;
		this.env = env;
		this.expectationCache = expectationCache;
	}

	@Override
	@SuppressWarnings({ "java:S1192", "java:S2139", "java:S3776" })
	public int insertData(InsertRequestMO ire) {
		int delayResponse = 0;
		try {
			java.util.Optional<InsertEntity> op = proxyabis.findById(ire.getReferenceId());
			if (!op.isEmpty()) {
				logger.error("Reference Id already exists {}", ire.getReferenceId());
				RequestMO re = new RequestMO(ire.getId(), ire.getVersion(), ire.getRequestId(), ire.getRequesttime(),
						ire.getReferenceId());
				throw new RequestException(re, FailureReasonsConstants.REFERENCEID_ALREADY_EXISTS);
			}
			cbeffURL = ire.getReferenceURL();
			InsertEntity ie = new InsertEntity(ire.getId(), ire.getVersion(), ire.getRequestId(), ire.getRequesttime(),
					ire.getReferenceId());
			List<BiometricData> lst = fetchCBEFF(ie);
			if (null == lst || lst.isEmpty())
				throw new RequestException(FailureReasonsConstants.INVALID_CBEFF_FORMAT);

			for (BiometricData bdt : lst) {
				Expectation exp = expectationCache.get(bdt.getBioData());
				if (exp.getId() != null && !exp.getId().isEmpty() && exp.getActionToInterfere().equals("Insert")) {
					logger.info("Expectation found for {}", exp.getId());
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
			logger.error("CBEFF error While inserting data ", cbef);
			throw new RequestException(cbef.getMessage(), delayResponse);
		} catch (RequestException rex) {
			logger.error("Error While inserting data ", rex);
			throw new RequestException(rex.getEntity(), rex.getReasonConstant(), rex.getDelayResponse());
		} catch (Exception exp) {
			logger.error("Error While inserting data ", exp);
			throw new RequestException(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, delayResponse);
		}
	}

	@SuppressWarnings({ "java:S1141" })
	private List<BiometricData> fetchCBEFF(InsertEntity ie) throws Exception {
		List<BiometricData> lst = new ArrayList<>();
		try {
			logger.info("Fetching CBEFF for reference URL- {}", cbeffURL);
			ResponseEntity<String> cbeffResp = restTemplate.exchange(cbeffURL, HttpMethod.GET, null, String.class);
			logger.info("CBEFF response-{}", cbeffResp);
			String cbeff = cbeffResp.getBody();
			logger.info("CBEFF Data-{}", cbeff);

			try {
				validateCBEFFData(cbeff);
			} catch (RequestException ex) {
				if (ex.getReasonConstant().equalsIgnoreCase("DAT-SER-006"))
					throw new RequestException(FailureReasonsConstants.DATA_SHARE_URL_EXPIRED);
				else
					throw new RequestException(FailureReasonsConstants.UNEXPECTED_ERROR);
			} catch (Exception ex) {
				logger.error("fetchCBEFF", ex);
			}

			if (encryption) {
				cbeff = cryptoUtil.decryptCbeff(cbeff);
			}

			logger.info("CBEFF Data-{}", cbeff);
			if (Objects.isNull(cbeff) || cbeff.isBlank() || cbeff.isEmpty()) {
				logger.info("Error while validating CBEFF at fetchCBEFF");
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
			}

			BIR birInfo = getBirs(cbeff);

			logger.info("Validating CBEFF data");
			validateBirs(birInfo);

			logger.info("Valid CBEFF data");
			logger.info("Inserting biometric details to concerned table {}", birInfo.getBirs().size());

			addBirs(ie, lst, birInfo);
		} catch (HttpClientErrorException ex) {
			ex.printStackTrace();
			logger.error("issue with httpclient URL ", ex);
			throw new RequestException(FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
		} catch (URISyntaxException | IllegalArgumentException ex) {
			ex.printStackTrace();
			logger.error("issue with httpclient URL Syntax ", ex);
			throw new RequestException(FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
		} catch (CbeffException ex) {
			logger.error("issue with cbeff ", ex);
			throw new RequestException(FailureReasonsConstants.INVALID_CBEFF_FORMAT);
		} catch (Exception ex) {
			logger.error("Issue while getting ,validating and inserting Cbeff", ex);
			throw new RequestException(FailureReasonsConstants.INVALID_CBEFF_FORMAT);
		}
		return lst;
	}

	@SuppressWarnings({ "java:S3776" })
	private void addBirs(InsertEntity ie, List<BiometricData> lst, BIR birInfo) throws NoSuchAlgorithmException {
		for (BIR bir : birInfo.getBirs()) {
			if (!Objects.isNull(bir.getBdb()) && bir.getBdb().length > 0) {
				BiometricData bd = new BiometricData();
				bd.setType(bir.getBdbInfo().getType().iterator().next().value());
				if (Objects.isNull(bir.getBdbInfo()))
					throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);

				if (!Objects.isNull(bir.getBdbInfo().getSubtype()) && !bir.getBdbInfo().getSubtype().isEmpty())
					bd.setSubtype(bir.getBdbInfo().getSubtype().toString());

				if ((Objects.isNull(bir.getBdb()) || bir.getBdb().length <= 0))
					throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);

				String hash = getSHAFromBytes(bir.getBdb());
				bd.setBioData(hash);
				bd.setInsertEntity(ie);

				lst.add(bd);
			} else {
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
			}
		}
	}

	private void validateBirs(BIR birInfo) throws CbeffException {
		try {
			if (!CbeffValidator.validateXML(birInfo)) {
				logger.error("Error while validating CBEFF");
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
			}

			if (Objects.isNull(birInfo) || birInfo.getBirs().isEmpty())
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
		} catch (Exception ex) {
			logger.error("Error while validating CBEFF Data", ex);
			throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
		}
	}

	private BIR getBirs(String cbeff) throws Exception {
		try {
			BIR birInfo = CbeffValidator.getBIRFromXML(cbeff.getBytes(StandardCharsets.UTF_8));
			birInfo.setBirs(birInfo.getBirs().stream().filter(b -> b.getBdb() != null).toList());
			return birInfo;
		} catch (Exception ex) {
			logger.error("Error while validating CBEFF", ex);
			throw new RequestException(FailureReasonsConstants.INVALID_CBEFF_FORMAT);
		}
	}

	@SuppressWarnings({ "java:S2139", "rawtypes" })
	private void validateCBEFFData(String cbeff) throws ParseException {
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(cbeff);
			JSONArray errors = (JSONArray) json.get("errors");
			for (Iterator it = errors.iterator(); it.hasNext();) {
				JSONObject error = (JSONObject) it.next();
				String errorCode = ((String) error.get("errorCode")).trim();
				String message = ((String) error.get("message")).trim();
				logger.info("ErrorCode {}, ErrorMessage {}", errorCode, message);
				throw new RequestException(errorCode);
			}
		} catch (RequestException e) {
			logger.error("validateCBEFFData", e);
			throw new RequestException(e.getReasonConstant());
		}
	}

	@Override
	public void deleteData(String referenceId) {
		logger.info("Deleting reference Id {}", referenceId);
		try {
			proxyabis.deleteById(referenceId);
		} catch (Exception e) {
			logger.error("Error while deleting record with reference Id {}", referenceId);
			logger.error("deleteData", e);
			throw new RequestException();
		}
	}

	@SuppressWarnings("unused")
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
		StringBuilder hexString = new StringBuilder();
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
	@SuppressWarnings({ "java:S2139", "java:S3776", "java:S6541" })
	public IdentifyDelayResponse findDuplication(IdentityRequest ir) {
		int delayResponse = 0;
		try {
			String refId = ir.getReferenceId();
			logger.info("Checking for duplication of reference ID {}", refId);
			List<BiometricData> lst = null;
			logger.info("find duplicate property set to {}", proxyAbisConfigService.getDuplicate());
			logger.info("force duplicate property set to {}", proxyAbisConfigService.isForceDuplicate());
			if (ir.getGallery() != null && !ir.getGallery().getReferenceIds().isEmpty()
					&& ir.getGallery().getReferenceIds().get(0).getReferenceId() != null
					&& !ir.getGallery().getReferenceIds().get(0).getReferenceId().isEmpty()) {
				List<String> referenceIds = new ArrayList<>();
				ir.getGallery().getReferenceIds().stream().forEach(ref -> referenceIds.add(ref.getReferenceId()));

				logger.info("checking for duplication of reference Id against {}", referenceIds);

				int galleryRefIdCountInDB = proxyabis.fetchCountForReferenceIdPresentInGallery(referenceIds);
				if (galleryRefIdCountInDB != referenceIds.size()) {
					logger.info("checking for reference Id Present in DB {}, Gallery reference Id list size {} ",
							galleryRefIdCountInDB, referenceIds.size());
					throw new RequestException(FailureReasonsConstants.REFERENCEID_NOT_FOUND);
				}
				List<String> bioValues = proxyAbisBioDataRepository.fetchBioDataByRefId(refId);
				if (!bioValues.isEmpty()) {
					for (String bioValue : bioValues) {
						Expectation exp = expectationCache.get(bioValue);
						if (exp.getId() != null && !exp.getId().isEmpty()
								&& exp.getActionToInterfere().equals("Identify")) {
							logger.info("Expectation found for {}", exp.getId());
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
				logger.info("checking for duplication in entire DB of reference ID {}", refId);
				List<String> bioValues = proxyAbisBioDataRepository.fetchBioDataByRefId(refId);
				if (!bioValues.isEmpty()) {
					for (String bioValue : bioValues) {
						Expectation exp = expectationCache.get(bioValue);
						if (exp.getId() != null && !exp.getId().isEmpty()
								&& exp.getActionToInterfere().equals("Identify")) {
							logger.info("Expectation found for {}", exp.getId());
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
				logger.info("Number of duplicate candidates are {}", lst.size());
			return new IdentifyDelayResponse(constructIdentityResponse(ir, lst), delayResponse);
		} catch (AbisException ex) {
			logger.error("Error while findDuplication", ex);
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
	@SuppressWarnings({ "java:S3776" })
	private IdentityResponse processExpectation(IdentityRequest ir, Expectation expectation,
			List<String> galleryReferenceIds) {
		logger.info("processExpectation {}", ir.getReferenceId());
		IdentityResponse response = new IdentityResponse();
		response.setId(ir.getId());
		response.setRequestId(ir.getRequestId());
		response.setResponsetime(ir.getRequesttime());
		logger.info("expectation.getForcedResponse {}", expectation.getForcedResponse());
		logger.info("expectation {}", expectation);
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
			logger.info("expectation.getGallery {}", expectation.getGallery());
			if (expectation.getGallery() != null && !expectation.getGallery().getReferenceIds().isEmpty()) {
				for (Expectation.ReferenceIds rd : expectation.getGallery().getReferenceIds()) {
					logger.info("rd.getReferenceId {}", rd.getReferenceId());
					List<String> refIds;
					if (galleryReferenceIds != null) {
						refIds = proxyAbisBioDataRepository.fetchByReferenceId(rd.getReferenceId(),
								galleryReferenceIds);
					} else {
						refIds = proxyAbisBioDataRepository.fetchReferenceId(rd.getReferenceId());
					}
					logger.info("expectation.refIds {}", refIds);
					if (!refIds.isEmpty()) {
						for (String refId : refIds) {
							cdl.getCandidates()
									.add(new IdentityResponse.Candidates(refId, getAnalytics(), modalitiesList));
						}
					}
				}
				cdl.setCount(cdl.getCandidates().size() + "");
				response.setCandidateList(cdl);
				logger.info("response {}", response);
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
		if (null == lst || lst.isEmpty()) {
			logger.info("No duplicates found for referenceID {}", ir.getReferenceId());
			cl.setCount(0 + "");
			response.setCandidateList(cl);
			return response;
		}
		logger.info("Duplicates found for referenceID {}", ir.getReferenceId());
		try {
			Map<String, IdentityResponse.Candidates> mp = new HashMap<>();
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
					modlst = new ArrayList<>();
				}

				IdentityResponse.Modalities md = new IdentityResponse.Modalities();
				md.setBiometricType(bio.getType());
				md.setAnalytics(getAnalytics());
				modlst.add(md);
				candi.setModalities(modlst);
				mp.put(bio.getInsertEntity().getReferenceId(), candi);

			});
			logger.info("Number of duplicates are {}", mp.size());
			cl.setCount(mp.size() + "");
			List<IdentityResponse.Candidates> clst = new ArrayList<>();
			mp.entrySet().stream().forEach(e -> clst.add(e.getValue()));
			cl.setCandidates(clst);
			response.setCandidateList(cl);
			return response;
		} catch (Exception e) {
			logger.error("constructIdentityResponse", e);
		}
		cl.setCount(0 + "");
		response.setCandidateList(new IdentityResponse.CandidateList());
		return response;
	}

	private IdentityResponse.Analytics getAnalytics() {
		IdentityResponse.Analytics a = new IdentityResponse.Analytics();
		if (!Objects.isNull(env)) {
			if (!Objects.isNull(env.getProperty("analytics.confidence")))
				a.setConfidence(env.getProperty("analytics.confidence"));
			if (!Objects.isNull(env.getProperty("analytics.internalscore")))
				a.setInternalScore(env.getProperty("analytics.internalscore"));
			if (!Objects.isNull(env.getProperty("analytics.key1")))
				a.setKey1(env.getProperty("analytics.key1"));
			if (!Objects.isNull(env.getProperty("analytics.key2")))
				a.setKey2(env.getProperty("analytics.key2"));
		}
		return a;
	}

	@SuppressWarnings({ "java:S899", "java:S4042", "unused" })
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
			boolean fileCreated = keyFile.createNewFile();
			Files.write(path, bytes);

			path = Paths.get(keystoreFilePath.toString(), PROPERTIES_FILE_NAME);
			try (FileWriter myWriter = new FileWriter(path.toString())) {
				myWriter.write("certificate.alias=" + alias + "\n" + "certificate.password=" + password + "\n");
				myWriter.write("certificate.keystore=" + keystore + "\n" + "certificate.filename="
						+ uploadedFile.getOriginalFilename());
			}
			CryptoCoreUtil.setCertificateValues(uploadedFile.getOriginalFilename(), keystore, password, alias);

			File dir = new File(keystoreFilePath.toString());
			File[] fileList = dir.listFiles();
			for (File file : fileList) {
				if (!file.getName().equalsIgnoreCase(uploadedFile.getOriginalFilename())
						&& file.getName().endsWith(".p12")) {
					logger.info("Deleting file {}", file.getName());
					file.delete();
					break;
				}
			}
			logger.info("Successfully uploaded certificate");
			return "Successfully uploaded file";
		} catch (Exception ex) {
			logger.error("Could not upload file", ex);
		}
		return "Could not upload file";
	}
}