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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import io.mosip.proxy.abis.entity.IdentityRequest;
import io.mosip.proxy.abis.entity.IdentityResponse;
import io.mosip.proxy.abis.entity.InsertEntity;
import io.mosip.proxy.abis.entity.InsertRequestMO;
import io.mosip.proxy.abis.entity.RequestMO;
import io.mosip.proxy.abis.entity.IdentityResponse.Modalities;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;

@Service
@Configuration
@PropertySource("classpath:config.properties")
public class ProxyAbisInsertServiceImpl implements ProxyAbisInsertService {

	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisInsertServiceImpl.class);
	private static String UPLOAD_FOLDER = "src/main/resources/";
	private static String UPLOAD_FOLDER_PROPERTIES = "src/main/resources/parter.properties";

	
	
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

	private static String CBEFF_URL = null;
	
	@Value("${secret_url}")
	private String SECRET_URL ;

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
		} catch (Exception exp) {
			logger.error("Error While inserting data " + exp.getMessage());
			throw new RequestException(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
		}

	}

	private List<BiometricData> fetchCBEFF(InsertEntity ie) throws Exception {
		List<BiometricData> lst = new ArrayList();
		try {
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

			logger.info("Fetching CBEFF for reference URL-" + CBEFF_URL);
			HttpHeaders headers1 = new HttpHeaders();

			headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers1.set("Cookie", "AUTHORIZATION" + responseHeader.get("Set-Cookie").get(0).toString().substring(0,
					responseHeader.get("Set-Cookie").get(0).toString().indexOf(";")));

			HttpEntity<String> entity1 = new HttpEntity<String>(headers1);
			String cbeff = restTemplate.exchange(CBEFF_URL, HttpMethod.GET, entity1, String.class).getBody();

			String cbf=cryptoUtil.decrypt(cbeff);
			
			//BIRType birType = CbeffValidator.getBIRFromXML(IOUtils.toByteArray(cbeff));
			BIRType birType = CbeffValidator.getBIRFromXML(IOUtils.toByteArray(cbf));
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(cbeff)));
			logger.info("Validating CBEFF data");
			if (CbeffValidator.validateXML(birType)) {
				logger.info("Error while validating CBEFF");
				throw new CbeffException("Invalid CBEFF");
			}

			logger.info("Valid CBEFF data");
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			NodeList birs = doc.getElementsByTagName("BIR");
			logger.info("Inserting biometric details to concerned table");

			for (int i = 1; i < birs.getLength(); i++) {
				Element birEle = (Element) birs.item(i);
				Element bdbInfo = ((Element) birEle.getElementsByTagName("BDBInfo").item(0));

				BiometricData bd = new BiometricData();
				bd.setType(bdbInfo.getElementsByTagName("Type").item(1).getTextContent());
				bd.setSubtype(bdbInfo.getElementsByTagName("Subtype").item(0).getTextContent());

				Element bdb = ((Element) birEle.getElementsByTagName("BDB").item(0));
				StringWriter sw = new StringWriter();
				trans.transform(new DOMSource(bdb), new StreamResult(sw));

				bd.setBioData(getSHA(sw.toString()));

				bd.setInsertEntity(ie);

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
	public IdentityResponse findDupication(IdentityRequest ir) {
		try {
			String refId = ir.getReferenceId();
			logger.info("Checking for dulication of reference ID " + refId);
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
				lst = proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(refId);
			}
			logger.info("Number of dulplicate candidates are " + lst.size());
			return constructIdentityResponse(ir, lst);
		} catch (Exception ex) {
			throw ex;

		}

	}

	private IdentityResponse constructIdentityResponse(IdentityRequest ir, List<BiometricData> lst) {
		IdentityResponse response = new IdentityResponse();
		response.setId(ir.getId());
		response.setRequestId(ir.getRequestId());
		response.setReturnValue(1);
		response.setResponsetime(ir.getRequesttime());
		IdentityResponse.CandidateList cl = new IdentityResponse.CandidateList();
		if (null == lst || lst.size() == 0) {
			logger.info("No duplicates found for referenceID" + ir.getId());
			cl.setCount(0);
			response.setCandidateList(cl);
			return response;
		}
		logger.info("Duplicates found for referenceID" + ir.getId());
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

}
