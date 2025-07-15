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
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
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
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.biometrics.commons.CbeffValidator;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.core.cbeffutil.exception.CbeffException;
import io.mosip.proxy.abis.constant.FailureReasonsConstants;
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
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.service.ExpectationCache;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;
import io.mosip.proxy.abis.utility.CryptoCoreUtil;

/**
 * Service implementation for handling biometric data insertion and duplication
 * checking.
 */
@Service
@Configuration
@PropertySource("classpath:config.properties")
public class ProxyAbisInsertServiceImpl implements ProxyAbisInsertService {
	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisInsertServiceImpl.class);

	//Changed a current directory path to a home directory path
	private static Path currentPath = Paths.get(System.getProperty("user.home"), "files");
	private static Path keystoreFilePath = Paths.get(currentPath.toString(), "keystore");

	private static final String PROPERTIES_FILE_NAME = "partner.properties";

	private ProxyAbisInsertRepository proxyabis;
	private ProxyAbisBioDataRepository proxyAbisBioDataRepository;
	private ProxyAbisConfigService proxyAbisConfigService;

	@Autowired(required = true)
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;

	private CryptoCoreUtil cryptoUtil;
	private Environment env;
	private ExpectationCache expectationCache;

	private String cbeffURL = null;

	/**
	 * Flag for enabling or disabling biometric data encryption.
	 */
	@Value("${abis.bio.encryption:true}")
	private boolean encryption;

	/**
	 * MOSIP host URL.
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

	/**
	 * Inserts biometric data into the database.
	 *
	 * @param ire the insertion request object containing reference ID and other
	 *            details
	 * @return the delay response time
	 */
	@Override
	@SuppressWarnings({ "java:S1192", "java:S2139", "java:S3776" })
	public int insertData(InsertRequestMO ire) {
		int delayResponse = 0;
		try {
			java.util.Optional<InsertEntity> op = proxyabis.findById(ire.getReferenceId());
			if (!op.isEmpty()) {
				// Logs a masked version of the reference ID to avoid exposing sensitive information.
				// Only the last 4 characters are shown, and the rest are replaced with asterisks.
				logger.error("Reference Id already exists ending with ****{}", ire.getReferenceId().substring(Math.max(ire.getReferenceId().length() - 4, 0)));
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

	/**
	 * Fetches CBEFF data from the given URL and parses it.
	 *
	 * @param ie the insertion entity
	 * @return a list of biometric data
	 * @throws Exception if an error occurs while fetching or parsing CBEFF data
	 */
	@SuppressWarnings({ "java:S1141" })
	private List<BiometricData> fetchCBEFF(InsertEntity ie) throws Exception {
		List<BiometricData> lst = new ArrayList();
		try {
			logger.info("Fetching CBEFF for reference URL-" + cbeffURL);
			ResponseEntity<String> cbeffResp = restTemplate.exchange(cbeffURL, HttpMethod.GET, null, String.class);
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
					logger.info("ErrorCode {}, ErrorMessage {}", errorCode, message);
					throw new RequestException(errorCode);
				}
			} catch (RequestException ex) {
				if (ex.getReasonConstant().equalsIgnoreCase("DAT-SER-006"))
					throw new RequestException(FailureReasonsConstants.DATA_SHARE_URL_EXPIRED);
				else
					throw new RequestException(FailureReasonsConstants.UNEXPECTED_ERROR);
			} catch (Exception ex) {
			}

			if (encryption) {
				cbeff = cryptoUtil.decryptCbeff(cbeff);
			}

			logger.info("CBEFF Data- {}", cbeff);
			if (cbeff == null || cbeff.isBlank() || cbeff.isEmpty()) {
				logger.error("Error while validating CBEFF null of blank");
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
			}

			BIR birType = null;
			try {
				birType = CbeffValidator.getBIRFromXML(IOUtils.toByteArray(cbeff));
				birType.setBirs(
						birType.getBirs().stream().filter(b -> b.getBdb() != null).collect(Collectors.toList()));
			} catch (Exception ex) {
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
				logger.error("Error while validating CBEFF Data", ex);
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
			}

			logger.info("Valid CBEFF data");
			logger.info("Inserting biometric details to concerned table {} ", birType.getBirs().size());

			for (BIR bir : birType.getBirs()) {
				if (bir.getBdb() != null && bir.getBdb().length > 0) {
					BiometricData bd = new BiometricData();
					bd.setType(bir.getBdbInfo().getType().iterator().next().value());
					if (bir.getBdbInfo() == null)
						throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);

					if (bir.getBdbInfo().getSubtype() != null && !bir.getBdbInfo().getSubtype().isEmpty())
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
			logger.error("issue with httpclient URL ", ex);
			throw new RequestException(FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
		} catch (URISyntaxException | IllegalArgumentException ex) {
			logger.error("issue with httpclient URL Syntax ", ex);
			throw new RequestException(FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
		} catch (CbeffException ex) {
			logger.error("issue with cbeff ", ex);
			throw new RequestException(FailureReasonsConstants.INVALID_CBEFF_FORMAT);
		} catch (Exception ex) {
			logger.error("Issue while getting ,validating and inserting Cbeff", ex);
			throw ex;
		}
		return lst;
	}

	/**
	 * Processes BIR information and adds corresponding BiometricData entries to the
	 * provided list.
	 *
	 * This method iterates through each BIR within the provided BIR information
	 * object, extracts relevant details, and creates corresponding BiometricData
	 * entries. It performs the following checks:
	 *
	 * - Validates the presence and length of biometric data (BDB) within each BIR.
	 * - Extracts BIR type and subtype for each valid BIR. - Calculates a SHA hash
	 * (consider using a more secure algorithm like SHA-256) from the biometric
	 * data. - Associates the BiometricData entry with the provided InsertEntity.
	 *
	 * @param insertEntity      The InsertEntity object associated with the
	 *                          biometric data.
	 * @param biometricDataList The list to which extracted BiometricData entries
	 *                          will be added.
	 * @param birInfo           The BIR information object containing BIR data.
	 * @throws NoSuchAlgorithmException If the SHA hashing algorithm is not
	 *                                  available.
	 */
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

	/**
	 * Validates the provided BIR information object.
	 *
	 * This method performs the following validations on the BIR information:
	 *
	 * - Checks if the BIR information can be successfully parsed as valid CBEFF
	 * XML. - Verifies that the BIR information object is not null and contains BIR
	 * data.
	 *
	 * @param birInfo The BIR information object to be validated.
	 * @throws CbeffException If an error occurs during CBEFF validation.
	 */
	private void validateBirs(BIR birInfo) throws CbeffException {
		try {
			if (!CbeffValidator.validateXML(birInfo)) {
				logger.error("Error while validating CBEFF");
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
			}

			if (Objects.isNull(birInfo) || birInfo.getBirs().isEmpty())
				throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
		} catch (Exception ex) {
			throw new RequestException(FailureReasonsConstants.CBEFF_HAS_NO_DATA);
		}
	}

	/**
	 * Parses the provided CBEFF data (assumed to be a UTF-8 encoded String) and
	 * extracts BIR information.
	 *
	 * This method attempts to convert the CBEFF string into a BIR object using a
	 * CBEFF validator. It then filters the extracted BIRs to keep only those
	 * containing valid biometric data (BDB is not null).
	 *
	 * @param cbeff The CBEFF data in String format (assumed UTF-8 encoding).
	 * @return A BIR object containing parsed BIR information, or throws an
	 *         exception if parsing fails.
	 * @throws CbeffException   If an error occurs during CBEFF parsing.
	 * @throws RequestException If the CBEFF format is invalid.
	 */
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

	/**
	 * Validates CBEFF data assumed to be in JSON format.
	 *
	 * This method parses the provided CBEFF data as JSON and extracts error
	 * information if present. It iterates through any "errors" found in the JSON
	 * structure and logs them along with error codes and messages. It then throws a
	 * RequestException with the extracted error code.
	 *
	 * @param cbeff The CBEFF data in String format (assumed JSON).
	 * @throws RequestException If the CBEFF data contains errors.
	 */
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
			if (e.getReasonConstant().equalsIgnoreCase("DAT-SER-006"))
				throw new RequestException(FailureReasonsConstants.DATA_SHARE_URL_EXPIRED);
			else
				throw new RequestException(FailureReasonsConstants.UNEXPECTED_ERROR);
		} catch (Exception ex) {
		}
	}

	/**
	 * Deletes an InsertEntity record identified by the provided reference ID.
	 *
	 * This method attempts to delete the InsertEntity record from the underlying
	 * data store using the `proxyabis.deleteById` method. It logs informative
	 * messages about the deletion attempt and throws a relevant exception if an
	 * error occurs.
	 *
	 * @param referenceId The unique identifier of the InsertEntity record to be
	 *                    deleted.
	 * @throws DataAccessException If an error occurs during data access operations.
	 * @throws RequestException    If the deletion fails for a non-data access
	 *                             related reason.
	 */
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

	/**
	 * Calculates the SHA-256 hash of a provided String.
	 *
	 * This method uses the SHA-256 algorithm to generate a hash from the input
	 * string. It converts the String to a byte array using UTF-8 encoding before
	 * calculating the hash.
	 *
	 * @param data The String for which to calculate the SHA-256 hash.
	 * @return The SHA-256 hash of the input string in hexadecimal format.
	 * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available on
	 *                                  the system.
	 */
	@SuppressWarnings("unused")
	private String getSHA(String data) throws NoSuchAlgorithmException {
		logger.info("Getting hash of string");
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return bytesToHex(md.digest(data.getBytes(StandardCharsets.UTF_8)));

	}

	/**
	 * Calculates the SHA-256 hash of a provided byte array.
	 *
	 * This method uses the SHA-256 algorithm to generate a hash from the input byte
	 * array.
	 *
	 * @param data The byte array for which to calculate the SHA-256 hash.
	 * @return The SHA-256 hash of the input byte array in hexadecimal format.
	 * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available on
	 *                                  the system.
	 */
	private String getSHAFromBytes(byte[] data) throws NoSuchAlgorithmException {
		logger.info("Getting hash of string");
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return bytesToHex(md.digest(data));
	}

	/**
	 * Converts a byte array to a hexadecimal string representation.
	 *
	 * This method iterates through each byte in the provided byte array and
	 * converts it to its corresponding two-digit hexadecimal representation with
	 * leading zeros for single-digit values. The individual hexadecimal strings are
	 * then appended to a StringBuilder and returned as a String.
	 *
	 * @param data The byte array to be converted to a hexadecimal string.
	 * @return The hexadecimal string representation of the input byte array.
	 */
	private static String bytesToHex(byte[] data) {
		StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			String hex = Integer.toHexString(0xff & data[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	/**
	 * Finds potential duplicate biometric data based on the provided
	 * IdentityRequest.
	 *
	 * This method checks for duplicate biometric data associated with the reference
	 * ID in the IdentityRequest. It considers both gallery reference IDs and the
	 * entire database depending on configuration and the presence of a gallery. It
	 * also checks for expectations associated with the found biometric data and
	 * applies any delay configured in the expectation.
	 *
	 * @param ir The IdentityRequest object containing reference ID and other
	 *           details.
	 * @return An IdentifyDelayResponse object containing potential matches and any
	 *         delay from expectations.
	 * @throws AbisException If an error occurs during data access or processing.
	 */
	@Override
	@SuppressWarnings({ "java:S2139", "java:S3776", "java:S6541","java:S5145" })
	public IdentifyDelayResponse findDuplication(IdentityRequest ir) {
		int delayResponse = 0;
		try {
			String refId = ir.getReferenceId();
			logger.info("Checking for duplication of reference ID");
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
				logger.info("checking for duplication in entire DB");
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
	 * Processes expectations associated with an IdentityRequest.
	 *
	 * This method checks the provided expectation and constructs an
	 * IdentityResponse based on the expectation type ("Error", "Duplicate"). If the
	 * expectation requires fetching gallery data, it retrieves the relevant
	 * reference IDs and constructs candidate information for the response.
	 *
	 * @param ir                  The IdentityRequest object.
	 * @param expectation         The expectation associated with the biometric
	 *                            data.
	 * @param galleryReferenceIds Optional list of gallery reference IDs (if
	 *                            applicable).
	 * @return An IdentityResponse object containing the constructed response data.
	 * @throws RequestException If the expectation forces an "Error" response.
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

	/**
	 * Constructs an IdentityResponse object containing information about potential
	 * duplicates.
	 *
	 * This method iterates through a list of BiometricData objects and builds a
	 * CandidateList for the IdentityResponse. It groups candidates by reference ID
	 * and creates Candidate objects with modality information for each unique
	 * reference ID found.
	 *
	 * @param identityRequest The IdentityRequest object containing basic
	 *                        information.
	 * @param duplicates      A list of BiometricData objects representing potential
	 *                        duplicates.
	 * @return An IdentityResponse object containing the constructed candidate
	 *         information.
	 * @throws AbisException If an error occurs during data processing.
	 */
	private IdentityResponse constructIdentityResponse(IdentityRequest ir, List<BiometricData> lst) {
		IdentityResponse response = new IdentityResponse();
		response.setId(ir.getId());
		response.setRequestId(ir.getRequestId());
		response.setReturnValue(1 + "");
		response.setResponsetime(ir.getRequesttime());
		IdentityResponse.CandidateList cl = new IdentityResponse.CandidateList();
		if (null == lst || lst.isEmpty()) {
			logger.info("No duplicates found for referenceID ");
			cl.setCount(0 + "");
			response.setCandidateList(cl);
			return response;
		}
		logger.info("Duplicates found for referenceID");
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

	/**
	 * Retrieves analytics data from the environment for inclusion in the
	 * IdentityResponse.
	 *
	 * This method attempts to retrieve analytics-related properties from the
	 * environment using the specified keys ("analytics.confidence",
	 * "analytics.internalscore", etc.). It populates the corresponding fields in an
	 * IdentityResponse.Analytics object if the properties are found.
	 *
	 * @return An IdentityResponse.Analytics object containing retrieved analytics
	 *         data or an empty object if no properties are found in the
	 *         environment.
	 */
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

	/**
	 * Saves a uploaded certificate file along with its metadata.
	 *
	 * This method securely stores a certificate (.p12) file uploaded through the
	 * `MultipartFile` object. It also stores the certificate alias, password, and
	 * keystore path using a secure storage mechanism (implementation details
	 * omitted for security reasons). The method calls an external component
	 * (`CryptoCoreUtil`) to configure the certificate using the provided
	 * parameters. Finally, it deletes any existing certificate file with the same
	 * extension in the specified keystore directory.
	 *
	 * @param uploadedFile The MultipartFile object containing the uploaded
	 *                     certificate data.
	 * @param alias        The alias associated with the certificate within the
	 *                     keystore.
	 * @param keystorePath The path to the keystore where the certificate will be
	 *                     stored.
	 * @return A success message upon successful upload, or an error message
	 *         otherwise.
	 * @throws CertificateUploadException If an error occurs during file upload,
	 *                                    storage, or deletion.
	 */
	@SuppressWarnings({ "java:S899", "java:S4042", "unused" })
	public String saveUploadedFileWithParameters(MultipartFile uploadedFile, String alias, String password,
			String keystore) {
		try {
			logger.info("Uploading certificate");
			byte[] bytes = uploadedFile.getBytes();
			String fileExtension = uploadedFile.getOriginalFilename().substring(uploadedFile.getOriginalFilename().lastIndexOf('.'));
			String randomFilename = UUID.randomUUID().toString().replace("-", "").substring(0, 8) + fileExtension;
			Path path = Paths.get(keystoreFilePath.toString(), randomFilename);

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
						+ randomFilename);
			}
			CryptoCoreUtil.setCertificateValues(uploadedFile.getOriginalFilename(), keystore, password, alias);

			File dir = new File(keystoreFilePath.toString());
			File[] fileList = dir.listFiles();
			for (File file : fileList) {
				if (!file.getName().equalsIgnoreCase(randomFilename)
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
