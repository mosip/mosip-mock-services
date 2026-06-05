package io.mosip.mock.sdk.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;

/**
 * Service class for extracting biometric templates from a
 * {@link BiometricRecord}. Supports various biometric types and processes them
 * to generate templates based on specified formats.
 */
public class ExtractTemplateService extends SDKService {
	private Logger logger = LoggerFactory.getLogger(ExtractTemplateService.class);

	private SecureRandom random = new SecureRandom();
	private BiometricRecord sample;
	@SuppressWarnings("unused")
	private List<BiometricType> modalitiesToExtract;

	private ProcessedLevelType[] types = new ProcessedLevelType[] { ProcessedLevelType.INTERMEDIATE,
			ProcessedLevelType.PROCESSED };

	public static final long FORMAT_TYPE_FINGER = 7;
	public static final long FORMAT_TYPE_FINGER_MINUTIAE = 2;

	/**
	 * Constructs an instance of ExtractTemplateService.
	 *
	 * @param env                 The Spring environment configuration.
	 * @param sample              The biometric record from which templates are to
	 *                            be extracted.
	 * @param modalitiesToExtract The list of biometric types to extract templates
	 *                            for.
	 * @param flags               Additional flags or parameters for customization.
	 */
	public ExtractTemplateService(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToExtract,
								  Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.modalitiesToExtract = modalitiesToExtract;
	}

	/**
	 * Retrieves the extracted biometric templates information as a response.
	 *
	 * @return A {@link Response} object containing the status code, status message,
	 *         and extracted templates.
	 */
	public Response<BiometricRecord> getExtractTemplateInfo() {
		ResponseStatus responseStatus = null;
		Response<BiometricRecord> response = new Response<>();
		try {
			if (Objects.isNull(sample) || Objects.isNull(sample.getSegments()) || sample.getSegments().isEmpty()) {
				responseStatus = ResponseStatus.MISSING_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
			}

			doExtractTemplateInfo(sample);
		} catch (SDKException ex) {
			logger.error("extractTemplate -- error", ex);
			switch (ResponseStatus.fromStatusCode(Integer.parseInt(ex.getErrorCode()))) {
				case INVALID_INPUT:
					response.setStatusCode(ResponseStatus.INVALID_INPUT.getStatusCode());
					response.setStatusMessage(String.format(ResponseStatus.INVALID_INPUT.getStatusMessage(), "sample"));
					response.setResponse(null);
					return response;
				case MISSING_INPUT:
					response.setStatusCode(ResponseStatus.MISSING_INPUT.getStatusCode());
					response.setStatusMessage(String.format(ResponseStatus.MISSING_INPUT.getStatusMessage(), "sample"));
					response.setResponse(null);
					return response;
				case QUALITY_CHECK_FAILED:
					response.setStatusCode(ResponseStatus.QUALITY_CHECK_FAILED.getStatusCode());
					response.setStatusMessage(String.format(ResponseStatus.QUALITY_CHECK_FAILED.getStatusMessage(), ""));
					response.setResponse(null);
					return response;
				case BIOMETRIC_NOT_FOUND_IN_CBEFF:
					response.setStatusCode(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode());
					response.setStatusMessage(
							String.format(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusMessage(), ""));
					response.setResponse(null);
					return response;
				case MATCHING_OF_BIOMETRIC_DATA_FAILED:
					response.setStatusCode(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusCode());
					response.setStatusMessage(
							String.format(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusMessage(), ""));
					response.setResponse(null);
					return response;
				case POOR_DATA_QUALITY:
					response.setStatusCode(ResponseStatus.POOR_DATA_QUALITY.getStatusCode());
					response.setStatusMessage(String.format(ResponseStatus.POOR_DATA_QUALITY.getStatusMessage(), ""));
					response.setResponse(null);
					return response;
				default:
					response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
					response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(), ""));
					response.setResponse(null);
					return response;
			}
		} catch (Exception ex) {
			logger.error("extractTemplate -- error", ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(), ""));
			response.setResponse(null);
			return response;
		}
		response.setStatusCode(ResponseStatus.SUCCESS.getStatusCode());
		response.setResponse(sample);
		return response;
	}

	/**
	 * Performs the extraction of biometric templates from the provided biometric
	 * record.
	 *
	 * @param bioRecord The biometric record containing segments from which
	 *                  templates are to be extracted.
	 */
	private void doExtractTemplateInfo(BiometricRecord bioRecord) {
		for (BIR segment : bioRecord.getSegments()) {
			if (isValidException(segment))
				break;

			if (!isValidBirData(segment))
				break;

			/**
			 * use it if required .. removed to get better performance
			 * MOSIP-35428
			 * segment.getBirInfo().setPayload(segment.getBdb());
			 */
			BDBInfo bdbInfo = segment.getBdbInfo();
			if (bdbInfo != null) {
				// Update the level to processed
				bdbInfo.setLevel(getRandomLevelType());

				// Check the flag from your config only used for performance testing
				boolean isExtractionEnabled = Boolean.parseBoolean(
						getEnv().getProperty("mosip.mocksdk.extraction", "false")
				);
				if (isExtractionEnabled) {
					// Determine modality and pick the configured mock template size
					long templateSize = getMockTemplateSize(bdbInfo);

					// Read current BDB (raw image/data) - we don't use its content, just simulate extraction
					byte[] originalBdb = segment.getBdb();  // This is the raw biometric data (ISO image etc.)

					// Create a mock template of the desired size (filled with random bytes to simulate real template)
					byte[] mockTemplate = new byte[(int) templateSize];
					random.nextBytes(mockTemplate);   // SecureRandom already present in the class

					// Replace the original BDB with the mock template
					segment.setBdb(mockTemplate);
				}
				// or do actual extraction
				if (segment.getBdbInfo().getFormat() != null) {
					String type = segment.getBdbInfo().getFormat().getType();
					// Update the fingerprint image to fingerprint minutiae type
					if (type != null && type.equals(String.valueOf(FORMAT_TYPE_FINGER))) {
						segment.getBdbInfo().getFormat().setType(String.valueOf(FORMAT_TYPE_FINGER_MINUTIAE));
					}
				}
			}
		}
	}

	/**
	 * Generates a random processed level type.
	 *
	 * @return A randomly selected {@link ProcessedLevelType} from the available
	 *         types.
	 */
	public ProcessedLevelType getRandomLevelType() {
		int rnd = this.random.nextInt(types.length);
		return types[rnd];
	}

	/**
	 * Returns the configured mock template size based on biometric type(s) present in BDBInfo.
	 * Takes the first type from the list (most common case). Falls back to 2000 bytes if unknown or empty.
	 */
	private long getMockTemplateSize(BDBInfo bdbInfo) {
		if (bdbInfo == null || bdbInfo.getType() == null || bdbInfo.getType().isEmpty()) {
			logger.warn("BDBInfo type is null or empty. Using default template size: 2000");
			return 2000;
		}

		// Get the first biometric type (standard practice - usually only one type per segment)
		BiometricType bioType = bdbInfo.getType().get(0);

		String key;
		switch (bioType) {
			case FINGER:
				key = "mosip.mocksdk.extraction.finger.size";
				break;
			case IRIS:
				key = "mosip.mocksdk.extraction.iris.size";
				break;
			case FACE:
				key = "mosip.mocksdk.extraction.face.size";
				break;
			default:
				logger.warn("Unknown biometric type: {}. Using default size 2000", bioType);
				return 2000;
		}

		// Read from Spring Environment (your config properties)
		String sizeStr = getEnv() != null ? getEnv().getProperty(key) : null;

		if (sizeStr != null && !sizeStr.trim().isEmpty()) {
			try {
				return Long.parseLong(sizeStr.trim());
			} catch (NumberFormatException e) {
				logger.warn("Invalid size value for {} = '{}'. Using default 2000", key, sizeStr);
			}
		}

		// Hard-coded fallbacks matching your config
		return switch (bioType) {
			case FINGER -> 2000;
			case IRIS -> 5000;
			case FACE -> 20000;
			default -> 2000;
		};
	}
}