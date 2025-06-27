package io.mosip.mock.sdk.service;

import java.util.List;
import java.util.Map;
import java.util.Random;

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

public class ExtractTemplateService extends SDKService {
	private Logger LOGGER = LoggerFactory.getLogger(ExtractTemplateService.class);

	private BiometricRecord sample;
	private List<BiometricType> modalitiesToExtract;

	private ProcessedLevelType[] types = new ProcessedLevelType[] { ProcessedLevelType.INTERMEDIATE,
			ProcessedLevelType.PROCESSED };

	public static final long FORMAT_TYPE_FINGER = 7;
	public static final long FORMAT_TYPE_FINGER_MINUTIAE = 2;

	public ExtractTemplateService(Environment env, BiometricRecord sample, List<BiometricType> modalitiesToExtract,
			Map<String, String> flags) {
		super(env, flags);
		this.sample = sample;
		this.modalitiesToExtract = modalitiesToExtract;
	}

	public Response<BiometricRecord> getExtractTemplateInfo() {
		ResponseStatus responseStatus = null;
		Response<BiometricRecord> response = new Response<>();
		try {
			if (sample == null || sample.getSegments() == null || sample.getSegments().isEmpty()) {
				responseStatus = ResponseStatus.MISSING_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
			}

			if (sample != null)
				LOGGER.info("extractTemplate segment size {}", sample.getSegments().size());
				
			for (BIR segment : sample.getSegments()) {
				if (isValidException(segment.getOthers()))
					break;

				if (!isValidBirData(segment))
					break;

				segment.getBirInfo().setPayload(segment.getBdb());
				BDBInfo bdbInfo = segment.getBdbInfo();
				if (bdbInfo != null) {
					// Update the level to processed
					bdbInfo.setLevel(getRandomLevelType());
					if (segment.getBdbInfo().getFormat() != null) {
						String type = segment.getBdbInfo().getFormat().getType();
						// Update the fingerprint image to fingerprint minutiae type
						if (type != null && type.equals(String.valueOf(FORMAT_TYPE_FINGER))) {
							segment.getBdbInfo().getFormat().setType(String.valueOf(FORMAT_TYPE_FINGER_MINUTIAE));
						}
					}
				}
				// do actual extraction
			}
		} catch (SDKException ex) {
			LOGGER.error("extractTemplate -- error", ex);
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
			LOGGER.error("extractTemplate -- error", ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setStatusMessage(String.format(ResponseStatus.UNKNOWN_ERROR.getStatusMessage(), ""));
			response.setResponse(null);
			return response;
		}
		response.setStatusCode(ResponseStatus.SUCCESS.getStatusCode());
		response.setResponse(sample);
		return response;
	}

	public ProcessedLevelType getRandomLevelType() {
		int rnd = new Random().nextInt(types.length);
		return types[rnd];
	}
}
