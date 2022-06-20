package io.mosip.proxy.abis.service.impl;

import io.mosip.kernel.core.cbeffutil.common.CbeffValidator;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
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
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.dto.RequestMO;
import io.mosip.proxy.abis.entity.BiometricData;
import io.mosip.proxy.abis.entity.InsertEntity;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.service.ExpectationCache;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import io.mosip.proxy.abis.dto.IdentityResponse.Modalities;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Configuration
@PropertySource("classpath:config.properties")
public class ProxyAbisInsertServiceImpl implements ProxyAbisInsertService {

	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisInsertServiceImpl.class);

	private static String UPLOAD_FOLDER = System.getProperty("user.dir")+"\\keystore";
	private static String PROPERTIES_FILE = UPLOAD_FOLDER+ "\\partner.properties";

	@Autowired
	ProxyAbisInsertRepository proxyabis;

	@Autowired
	ProxyAbisBioDataRepository proxyAbisBioDataRepository;

	@Autowired
	ProxyAbisConfigService proxyAbisConfigService;

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	CryptoCoreUtil cryptoUtil;
	
	@Autowired
    private Environment env;

    @Autowired
    private ExpectationCache expectationCache;

	private static String CBEFF_URL = null;
	
	@Value("${secret_url}")
	private String SECRET_URL ;

    /**
     * This flag is added for fast-tracking core ABIS functionality testing without depending on working environment
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
		System.out.println(SECRET_URL);
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
				if(exp.getId() != null && !exp.getId().isEmpty() && exp.getActionToInterfere().equals("Insert")){
					logger.info("Expectation found for " + exp.getId());
					if(exp.getDelayInExecution() != null && !exp.getDelayInExecution().isEmpty()){
						delayResponse = Integer.parseInt(exp.getDelayInExecution());
					}
					if(exp.getForcedResponse().equals("Error")){
						throw new RequestException(exp.getErrorCode(), delayResponse);
					}
				}
			}
			ie.setBiometricList(lst);
			proxyabis.save(ie);
			return delayResponse;
		} catch (CbeffException cbef) {
			logger.error("CBEF error While inserting data " + cbef.getMessage());
			throw new RequestException(cbef.getMessage(), delayResponse);
		} catch(RequestException rex) {
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
			HttpHeaders headers1 = new HttpHeaders();
			headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

			if(encryption) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", env.getProperty("secret_url.id"));
				jsonObject.put("metadata", new JSONObject());
				JSONObject jsonObject1 = new JSONObject();
				jsonObject1.put("clientId", env.getProperty("secret_url.clientnId"));
				jsonObject1.put("secretKey", env.getProperty("secret_url.secretKey"));
				jsonObject1.put("appId", env.getProperty("secret_url.appId"));
				jsonObject.put("requesttime", env.getProperty("secret_url.requesttime"));
				jsonObject.put("version", env.getProperty("secret_url.version"));
				jsonObject.put("request", jsonObject1);

				HttpEntity<String> entity = new HttpEntity<String>(jsonObject.toString(), headers);
				HttpEntity<String> response = restTemplate.exchange(SECRET_URL, HttpMethod.POST, entity, String.class);

				Object obj = JSONValue.parse(response.getBody());

				JSONObject jo1 = (JSONObject) ((JSONObject) obj).get("response");
				HttpHeaders responseHeader = response.getHeaders();
				if (!(jo1.get("status").toString().equalsIgnoreCase("Success"))) {

					throw new Exception();
				}
				headers1.set("Cookie", "AUTHORIZATION" + responseHeader.get("Set-Cookie").get(0).toString().substring(0,
						responseHeader.get("Set-Cookie").get(0).toString().indexOf(";")));
			}
			logger.info("Fetching CBEFF for reference URL-" + CBEFF_URL);
			HttpEntity<String> entity1 = new HttpEntity<String>(headers1);
			String cbeff = restTemplate.exchange(CBEFF_URL, HttpMethod.GET, entity1, String.class).getBody();

			if(encryption) {
				cbeff = cryptoUtil.decryptCbeff(cbeff);
			}
			
			BIRType birType = CbeffValidator.getBIRFromXML(IOUtils.toByteArray(cbeff));
			birType.setBir(birType.getBIR().stream().filter(b -> b.getBDB() != null).collect(Collectors.toList()));
			logger.info("Validating CBEFF data");
			if (CbeffValidator.validateXML(birType)) {
				logger.info("Error while validating CBEFF");
				throw new CbeffException("Invalid CBEFF");
			}

			logger.info("Valid CBEFF data");
			logger.info("Inserting biometric details to concerned table");

			for (BIRType bir : birType.getBIR()) {
				if (bir.getBDB() != null && bir.getBDB().length > 0) {
					BiometricData bd = new BiometricData();
					bd.setType(bir.getBDBInfo().getType().iterator().next().value());
					if (bir.getBDBInfo().getSubtype() != null && bir.getBDBInfo().getSubtype().size() >0)
					bd.setSubtype(bir.getBDBInfo().getSubtype().toString());
					String hash = getSHAFromBytes(bir.getBDB());
					bd.setBioData(hash);
					bd.setInsertEntity(ie);

					lst.add(bd);
				}
			}

		} catch (CbeffException ex) {
			logger.error("issue with cbeff " + ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			logger.error("Issue while getting ,validating and inserting Cbeff" + ex.getMessage());
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
			if (null != ir.getGallery() && ir.getGallery().getReferenceIds().size() > 0
					&& null != ir.getGallery().getReferenceIds().get(0).getReferenceId()
					&& !ir.getGallery().getReferenceIds().get(0).getReferenceId().isEmpty()) {
				List<String> referenceIds = new ArrayList();
				ir.getGallery().getReferenceIds().stream().forEach(ref -> referenceIds.add(ref.getReferenceId()));
				logger.info("checking for duplication of reference Id against" + referenceIds.toString());
				lst = proxyAbisBioDataRepository.fetchDuplicatesForReferenceIdBasedOnGalleryIds(refId, referenceIds);
			} else {
				logger.info("checking for duplication in entire DB of reference ID" + refId);
                List<String> bioValues = proxyAbisBioDataRepository.fetchBioDataByRefId(refId);
                if(!bioValues.isEmpty()){
                	for(String bioValue: bioValues){
						Expectation exp = expectationCache.get(bioValue);
						if(exp.getId() != null && !exp.getId().isEmpty() && exp.getActionToInterfere().equals("Identify")){
							logger.info("Expectation found for " + exp.getId());
							if(exp.getDelayInExecution() != null && !exp.getDelayInExecution().isEmpty()){
								delayResponse = Integer.parseInt(exp.getDelayInExecution());
							}
							return new IdentifyDelayResponse(processExpectation(ir, exp), delayResponse);
						}
					}
                }
				if (proxyAbisConfigService.isForceDuplicate() || proxyAbisConfigService.getDuplicate()) {
					lst = proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(refId);
				}
			}
			if(lst != null)
				logger.info("Number of duplicate candidates are " + lst.size());
			return new IdentifyDelayResponse(constructIdentityResponse(ir, lst), delayResponse);
		} catch (Exception ex) {
			throw ex;
		}

	}

	/**
	 * Constructs a identity response based on the expectaions that are set.
	 * @param ir
	 * @param expectation
	 * @return
	 */
    private IdentityResponse processExpectation(IdentityRequest ir, Expectation expectation){
		logger.info("processExpectation" + ir.getReferenceId());
		IdentityResponse response = new IdentityResponse();
		response.setId(ir.getId());
		response.setRequestId(ir.getRequestId());
		response.setResponsetime(ir.getRequesttime());

		if(expectation.getForcedResponse().equals("Error")){
			throw new RequestException(expectation.getErrorCode());
		} else if(expectation.getForcedResponse().equals("Duplicate")) {
			response.setReturnValue(1);
			IdentityResponse.CandidateList cdl = new IdentityResponse.CandidateList();
			cdl.setCandidates(new ArrayList<>());
			List<Modalities> modalitiesList = new ArrayList<>();
			modalitiesList.add(new Modalities("FACE", getAnalytics()));
			modalitiesList.add(new Modalities("FINGER", getAnalytics()));
			modalitiesList.add(new Modalities("IRIS", getAnalytics()));

			if(expectation.getGallery() != null && expectation.getGallery().getReferenceIds().size() > 0){
				for(Expectation.ReferenceIds rd: expectation.getGallery().getReferenceIds()){
					List<String> refIds = proxyAbisBioDataRepository.fetchReferenceId(rd.getReferenceId());
					if(refIds.size() > 0){
						for(String refId: refIds){
							cdl.getCandidates().add(new IdentityResponse.Candidates(refId, getAnalytics(), modalitiesList));
						}
					}
				}
				cdl.setCount(cdl.getCandidates().size());
				response.setCandidateList(cdl);
				return response;
			}
		}
		return constructIdentityResponse(ir, null);
    }

	private IdentityResponse constructIdentityResponse(IdentityRequest ir, List<BiometricData> lst) {
		IdentityResponse response = new IdentityResponse();
		response.setId(ir.getId());
		response.setRequestId(ir.getRequestId());
		response.setReturnValue(1);
		response.setResponsetime(ir.getRequesttime());
		IdentityResponse.CandidateList cl = new IdentityResponse.CandidateList();
		if (null == lst || lst.size() == 0) {
			logger.info("No duplicates found for referenceID" + ir.getReferenceId());
			cl.setCount(0);
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
			cl.setCount(mp.size());
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
		cl.setCount(0);
		response.setCandidateList(new IdentityResponse.CandidateList());
		return response;
	}

	private IdentityResponse.Analytics getAnalytics() {
		IdentityResponse.Analytics a = new IdentityResponse.Analytics();
		a.setConfidence(Integer.parseInt(env.getProperty("analytics.confidence")));
		a.setInternalScore(Integer.parseInt(env.getProperty("analytics.internalscore")));
		a.setKey1(env.getProperty("analytics.key1"));
		a.setKey2(env.getProperty("analytics.key2"));
		return a;
	}
	

	public  String saveUploadedFileWithParameters(MultipartFile uploadedFile, String alias,
			String password, String keystore) {
		try {
			logger.info("Uploading certificate");
			byte[] bytes = uploadedFile.getBytes();
			Path path = Paths.get(UPLOAD_FOLDER + "/"+ uploadedFile.getOriginalFilename());
			File keyFile = new File(path.toString());
			File parent = keyFile.getParentFile();
			if (parent != null && !parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}
			keyFile.createNewFile();
			Files.write(path, bytes);

			FileWriter myWriter = new FileWriter(PROPERTIES_FILE);
			myWriter.write("certificate.alias=" + alias + "\n" + "certificate.password=" + password + "\n");
			myWriter.write("certificate.keystore=" + keystore + "\n" + "certificate.filename="
					+ uploadedFile.getOriginalFilename());
			myWriter.close();
			CryptoCoreUtil.setCertificateValues(uploadedFile.getOriginalFilename(), keystore, password, alias);
			
			File dir = new File(UPLOAD_FOLDER);
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
