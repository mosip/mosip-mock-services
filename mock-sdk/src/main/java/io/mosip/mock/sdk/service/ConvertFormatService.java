package io.mosip.mock.sdk.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import io.mosip.kernel.bio.converter.constant.ConverterErrorCode;
import io.mosip.kernel.bio.converter.exception.ConversionException;
import io.mosip.kernel.bio.converter.service.impl.ConverterServiceImpl;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;
import io.mosip.mock.sdk.utils.Util;

public class ConvertFormatService extends SDKService {
	private Logger logger = LoggerFactory.getLogger(ConvertFormatService.class);

	private BiometricRecord sample;
	@SuppressWarnings("unused")
	private List<BiometricType> modalitiesToConvert;
	private String sourceFormat;
	private String targetFormat;
	private Map<String, String> sourceParams;
	private Map<String, String> targetParams;

	private static final String TAG_ERROR_INFO = "convertFormat -- error";
	
	public ConvertFormatService(Environment env, BiometricRecord sample, String sourceFormat, String targetFormat,
			Map<String, String> sourceParams, Map<String, String> targetParams,
			List<BiometricType> modalitiesToConvert) {
		super(env, null);
		this.sample = sample;
		this.sourceParams = sourceParams;
		this.sourceFormat = sourceFormat;
		this.targetFormat = targetFormat;
		this.targetParams = targetParams;
		this.modalitiesToConvert = modalitiesToConvert;
	}

	@SuppressWarnings({ "java:S112", "java:S135", "java:S3776", "java:S6208", "java:S6541" })
	public Response<BiometricRecord> getConvertFormatInfo() {
		Response<BiometricRecord> response = new Response<>();

		Map<String, String> responseValues = null;
		try {
			Map<String, String> values = new HashMap<>();
			for (BIR segment : sample.getSegments()) {
				if (!isValidBirData(segment))
					break;

				BiometricType bioType = segment.getBdbInfo().getType().get(0);
				List<String> bioSubTypeList = segment.getBdbInfo().getSubtype();

				String bioSubType = null;
				if (bioSubTypeList != null && !bioSubTypeList.isEmpty()) {
					bioSubType = bioSubTypeList.get(0).trim();
					if (bioSubTypeList.size() >= 2)
						bioSubType += " " + bioSubTypeList.get(1).trim();
				}

				String key = bioType + "_" + bioSubType;
				// ignore modalities that are not to be matched
				if (!isValidBioTypeForSourceFormat(bioType, sourceFormat))
					continue;

				if (!values.containsKey(key)) {
					values.put(key, Util.encodeToURLSafeBase64(segment.getBdb()));
				}
			}

			responseValues = new ConverterServiceImpl().convert(values, sourceFormat, targetFormat, sourceParams,
					targetParams);
			List<BIR> birList = sample.getSegments();
			for (int index = 0; index < birList.size(); index++) {
				BIR segment = birList.get(index);
				BiometricType bioType = segment.getBdbInfo().getType().get(0);
				List<String> bioSubTypeList = segment.getBdbInfo().getSubtype();
				String bioSubType = null;
				if (bioSubTypeList != null && !bioSubTypeList.isEmpty()) {
					bioSubType = bioSubTypeList.get(0).trim();
					if (bioSubTypeList.size() >= 2)
						bioSubType += " " + bioSubTypeList.get(1).trim();
				}

				String key = bioType + "_" + bioSubType;
				// ignore modalities that are not to be matched
				if (!isValidBioTypeForSourceFormat(bioType, sourceFormat))
					continue;

				if (responseValues != null && responseValues.containsKey(key)) {
					segment.getBirInfo().setPayload(segment.getBdb());
					segment.setBdb(Util.decodeURLSafeBase64(responseValues.get(key)));
				}
				birList.set(index, segment);
			}
			sample.setSegments(birList);
			response.setStatusCode(ResponseStatus.SUCCESS.getStatusCode());
			response.setResponse(sample);
		} catch (SDKException ex) {
			logger.error(TAG_ERROR_INFO, ex);
			switch (ResponseStatus.fromStatusCode(Integer.parseInt(ex.getErrorCode()))) {
			case INVALID_INPUT:
				response.setStatusCode(ResponseStatus.INVALID_INPUT.getStatusCode());
				response.setStatusMessage(ResponseStatus.INVALID_INPUT.getStatusMessage() + " sample");
				response.setResponse(null);
				return response;
			case MISSING_INPUT:
				response.setStatusCode(ResponseStatus.MISSING_INPUT.getStatusCode());
				response.setStatusMessage(ResponseStatus.MISSING_INPUT.getStatusMessage() + " sample");
				response.setResponse(null);
				return response;
			case QUALITY_CHECK_FAILED:
				response.setStatusCode(ResponseStatus.QUALITY_CHECK_FAILED.getStatusCode());
				response.setStatusMessage(ResponseStatus.QUALITY_CHECK_FAILED.getStatusMessage());
				response.setResponse(null);
				return response;
			case BIOMETRIC_NOT_FOUND_IN_CBEFF:
				response.setStatusCode(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode());
				response.setStatusMessage(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusMessage());
				response.setResponse(null);
				return response;
			case MATCHING_OF_BIOMETRIC_DATA_FAILED:
				response.setStatusCode(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusCode());
				response.setStatusMessage(ResponseStatus.MATCHING_OF_BIOMETRIC_DATA_FAILED.getStatusMessage());
				response.setResponse(null);
				return response;
			case POOR_DATA_QUALITY:
				response.setStatusCode(ResponseStatus.POOR_DATA_QUALITY.getStatusCode());
				response.setStatusMessage(ResponseStatus.POOR_DATA_QUALITY.getStatusMessage());
				response.setResponse(null);
				return response;
			default:
				response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
				response.setStatusMessage(ResponseStatus.UNKNOWN_ERROR.getStatusMessage());
				response.setResponse(null);
				return response;
			}
		} catch (ConversionException ex) {
			logger.error(TAG_ERROR_INFO, ex);
			switch (ConverterErrorCode.fromErrorCode(ex.getErrorCode())) {
			case INPUT_SOURCE_EXCEPTION:
			case INVALID_REQUEST_EXCEPTION:
			case INVALID_SOURCE_EXCEPTION:
			case INVALID_TARGET_EXCEPTION:
			case SOURCE_NOT_VALID_FINGER_ISO_FORMAT_EXCEPTION:
			case SOURCE_NOT_VALID_FACE_ISO_FORMAT_EXCEPTION:
			case SOURCE_NOT_VALID_IRIS_ISO_FORMAT_EXCEPTION:
			case SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION:
			case COULD_NOT_READ_ISO_IMAGE_DATA_EXCEPTION:
			case TARGET_FORMAT_EXCEPTION:
			case NOT_SUPPORTED_COMPRESSION_TYPE:
				response.setStatusCode(ResponseStatus.INVALID_INPUT.getStatusCode());
				response.setResponse(null);
				break;

			case SOURCE_CAN_NOT_BE_EMPTY_OR_NULL_EXCEPTION:
				response.setStatusCode(ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF.getStatusCode());
				response.setResponse(null);
				break;

			default:
				response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
				response.setResponse(null);
				break;
			}
		} catch (Exception ex) {
			logger.error(TAG_ERROR_INFO, ex);
			response.setStatusCode(ResponseStatus.UNKNOWN_ERROR.getStatusCode());
			response.setResponse(null);
		}

		return response;
	}

	private boolean isValidBioTypeForSourceFormat(BiometricType bioType, String sourceFormat) {
		boolean isValid = false;
		switch (sourceFormat) {
		case "ISO19794_4_2011":
			if (bioType == BiometricType.FINGER)
				isValid = true;
			break;
		case "ISO19794_5_2011":
			if (bioType == BiometricType.FACE)
				isValid = true;
			break;
		case "ISO19794_6_2011":
			if (bioType == BiometricType.IRIS)
				isValid = true;
			break;
		default:
			break;
		}
		return isValid;
	}
}