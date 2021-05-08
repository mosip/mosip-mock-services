package io.mosip.proxy.abis.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import io.mosip.kernel.core.exception.ExceptionUtils;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import io.mosip.kernel.core.cbeffutil.common.CbeffValidator;
import io.mosip.kernel.core.cbeffutil.exception.CbeffException;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.proxy.abis.CryptoCoreUtil;
import io.mosip.proxy.abis.dao.ProxyAbisBioDataRepository;
import io.mosip.proxy.abis.dao.ProxyAbisInsertRepository;
import io.mosip.proxy.abis.entity.BiometricData;
import io.mosip.proxy.abis.entity.Expectation;
import io.mosip.proxy.abis.entity.FailureResponse;
import io.mosip.proxy.abis.entity.IdentityRequest;
import io.mosip.proxy.abis.entity.IdentityResponse;
import io.mosip.proxy.abis.entity.InsertEntity;
import io.mosip.proxy.abis.entity.InsertRequestMO;
import io.mosip.proxy.abis.entity.RequestMO;
import io.mosip.proxy.abis.entity.IdentityResponse.Modalities;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.service.ExpectationCache;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;

@Service
@Configuration
@PropertySource("classpath:config.properties")
public class ProxyAbisInsertServiceImpl implements ProxyAbisInsertService {

	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisInsertServiceImpl.class);
	private static String UPLOAD_FOLDER = "src/main/resources/";
	private static String UPLOAD_FOLDER_PROPERTIES = "src/main/resources/partner.properties";


	
	@Autowired
	ProxyAbisInsertRepository proxyabis;

	@Autowired
	ProxyAbisBioDataRepository proxyAbisBioDataRepository;

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
	 * set this flag to false then we will not check for duplicate we will always return unique biometric
	 */
	@Value("${abis.return.duplicate:true}")
	private boolean findDuplicate;

    /**
     * This flag is added for fast-tracking core ABIS functionality testing without depending on working environment
     */
	@Value("${abis.bio.encryption:true}")
	private boolean encryption;

	/**
	 * This flag is added for development & debugging locally registration-processor-abis-sample.json
	 * If true then registration-processor-abis-sample.json will be picked from resources
	 */
	@Value("${local.development:false}")
	private boolean localDevelopment;

	/**
	 * Mosip host
	 */
	@Value("${mosip_host}")
	private String mosipHost;

	@Override
	public void insertData(InsertRequestMO ire) {
		System.out.println(SECRET_URL);
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
			ie.setBiometricList(lst);
			proxyabis.save(ie);

		} catch (CbeffException cbef) {
			logger.error("CBEF error While inserting data " + cbef.getMessage());
			throw new RequestException(cbef.getMessage());
		} catch(RequestException rex) {
			throw rex;
		} catch (Exception exp) {
			logger.error("Error While inserting data " + exp.getMessage());
			throw new RequestException(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
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
			if (localDevelopment){
				/* It will replace the host in referenceUrl with the mosip host */
				CBEFF_URL = CBEFF_URL.replace("http://datashare-service", mosipHost);
			}
			logger.info("Fetching CBEFF for reference URL-" + CBEFF_URL);
			HttpEntity<String> entity1 = new HttpEntity<String>(headers1);
			String cbeff = restTemplate.exchange(CBEFF_URL, HttpMethod.GET, entity1, String.class).getBody();

			if(encryption) {
				cbeff = cryptoUtil.decryptCbeff(cbeff);
			}

			BIRType birType = CbeffValidator.getBIRFromXML(IOUtils.toByteArray(cbeff));
			logger.info("Validating CBEFF data");
			if (CbeffValidator.validateXML(birType)) {
				logger.info("Error while validating CBEFF");
				throw new CbeffException("Invalid CBEFF");
			}

			logger.info("Valid CBEFF data");
			logger.info("Inserting biometric details to concerned table");

			for (BIRType type : birType.getBIR()) {

				BiometricData bd = new BiometricData();
				bd.setType(type.getBDBInfo().getType().iterator().next().value());
				if (type.getBDBInfo().getSubtype() != null && type.getBDBInfo().getSubtype().size() >0)
					bd.setSubtype(type.getBDBInfo().getSubtype().toString());
				bd.setBioData(getSHA(new String(type.getBDB())));
				bd.setInsertEntity(ie);
				Expectation exp = expectationCache.get(bd.getBioData());
				if(exp.getId() != null && !exp.getId().isEmpty() && exp.getActionToInterfere().equals("Insert")){
					int delayResponse = 0;
					if(exp.getDelayInExecution() != null && !exp.getDelayInExecution().isEmpty()){
						delayResponse = Integer.parseInt(exp.getDelayInExecution());
					}
					TimeUnit.SECONDS.sleep(delayResponse);

					if(exp.getForcedResponse().equals("Error")){
						throw new RequestException(exp.getErrorCode());
					}
				}
				lst.add(bd);
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
    public IdentityResponse findDuplication(IdentityRequest ir) {
		try {
			String refId = ir.getReferenceId();
            logger.info("Checking for duplication of reference ID " + refId);
			List<BiometricData> lst = null;
			if (null != ir.getGallery() && ir.getGallery().getReferenceIds().size() > 0
					&& null != ir.getGallery().getReferenceIds().get(0).getReferenceId()
					&& !ir.getGallery().getReferenceIds().get(0).getReferenceId().isEmpty()) {
				List<String> referenceIds = new ArrayList();
				ir.getGallery().getReferenceIds().stream().forEach(ref -> referenceIds.add(ref.getReferenceId()));
				logger.info("checking for duplication of reference Id against" + referenceIds.toString());
				lst = proxyAbisBioDataRepository.fetchDuplicatesForReferenceIdBasedOnGalleryIds(refId, referenceIds);
			} else {
				logger.info("checking for duplication in entire DB of reference ID" + refId);
                List<String> bioValue = proxyAbisBioDataRepository.fetchBiodata(refId);
                if(!bioValue.isEmpty()){
                    Expectation exp = expectationCache.get(bioValue.get(0));
					if(exp.getId() != null && !exp.getId().isEmpty() && exp.getActionToInterfere().equals("Identify")){
						int delayResponse = 0;
						if(exp.getDelayInExecution() != null && !exp.getDelayInExecution().isEmpty()){
							delayResponse = Integer.parseInt(exp.getDelayInExecution());
						}
						try {
							TimeUnit.SECONDS.sleep(delayResponse);
						} catch (InterruptedException e) {
							logger.info("findDuplication -> InterruptedException: " + e.getMessage());
						}
						return processExpectation(ir, exp);
					}
                }
				if (findDuplicate) {
					lst = proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(refId);
				}
				
			}
			if(lst != null)
				logger.info("Number of dulplicate candidates are " + lst.size());
			return constructIdentityResponse(ir, lst);
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
					cdl.getCandidates().add(new IdentityResponse.Candidates(rd.getReferenceId(), getAnalytics(), modalitiesList));
				}
			} else {
				cdl.getCandidates().add(new IdentityResponse.Candidates(UUID.randomUUID().toString(), getAnalytics(), modalitiesList));
			}
			response.setCandidateList(new IdentityResponse.CandidateList(cdl.getCount(), cdl.getCandidates()));
		} else {
			return constructIdentityResponse(ir, null);
		}
		return response;
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
	

	public  String saveUploadedFileWithParameters(MultipartFile upoadedFile, String alias,
			String password, String keystore) {
		try {
			byte[] bytes = upoadedFile.getBytes();
			Path path = Paths.get(UPLOAD_FOLDER + upoadedFile.getOriginalFilename());
			Files.write(path, bytes);

			FileWriter myWriter = new FileWriter(UPLOAD_FOLDER_PROPERTIES);
			myWriter.write("cerificate.alias=" + alias + "\n" + "cerificate.password=" + password + "\n");
			myWriter.write("certificate.keystore=" + keystore + "\n" + "certificate.filename="
					+ upoadedFile.getOriginalFilename());
			myWriter.close();
			CryptoCoreUtil.setCertificateValues(upoadedFile.getOriginalFilename(), keystore, password, alias);
			
			File dir = new File("src/main/resources");
			File[] fileList = dir.listFiles();
			for (File file : fileList) {
				if (!file.getName().equalsIgnoreCase(upoadedFile.getOriginalFilename())
						&& file.getName().endsWith(".p12")) {
					logger.info("Deleting file" + file.getName());
					file.delete();
					break;
				}
			}
			return "Successfully uploaded file";
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Could not upload file");
		}
		return "Could not upload file";

	}

	public Boolean getDuplicate(){
		return findDuplicate;
	}
	public void setDuplicate(Boolean d){
		findDuplicate = d;
	}

	public Map<String, Expectation> getExpectations(){
    	return expectationCache.get();
	}

	public void setExpectation(Expectation exp){
		expectationCache.insert(exp);
	}

	public void deleteExpectation(String id){
    	expectationCache.delete(id);
	}


}
