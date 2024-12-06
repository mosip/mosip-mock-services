package io.mosip.mock.sbi.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.registration.mdm.dto.BioMetricsDto;
import io.mosip.registration.mdm.dto.ErrorInfo;
import io.mosip.registration.mdm.dto.RCaptureResponse;
import lombok.Getter;
import lombok.Setter;

public class SBIJsonInfo {
	private static final Logger logger = LoggerFactory.getLogger(SBIJsonInfo.class);

	private SBIJsonInfo() {
		throw new IllegalStateException("SBIJsonInfo class");
	}

	public static String getErrorJson(String lang, String errorCode, String exceptionMessage) {
		List<ErrorDto> errorList = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();

		ErrorDto errorDto = new ErrorDto();
		ErrorInfo errorInfo = new ErrorInfo(errorCode,
				(getErrorDescription(lang, errorCode) + " " + exceptionMessage).trim());
		errorDto.error = errorInfo;
		errorList.add(errorDto);
		try {
			return mapper.writeValueAsString(errorList);
		} catch (IOException e) {
			logger.error("getErrorJson", e);
		}
		return null;
	}

	public static String getAdminApiErrorJson(String lang, String errorCode, String exceptionMessage) {
		ObjectMapper mapper = new ObjectMapper();
		ErrorInfo errorInfo = new ErrorInfo(errorCode,
				(getErrorDescription(lang, errorCode) + " " + exceptionMessage).trim());
		try {
			return mapper.writeValueAsString(errorInfo);
		} catch (IOException e) {
			logger.error("getAdminApiErrorJson", e);
		}
		return null;
	}

	public static String getStreamErrorJson(String lang, String errorCode, String exceptionMessage) {
		ObjectMapper mapper = new ObjectMapper();
		ErrorInfo errorInfo = new ErrorInfo(errorCode,
				(getErrorDescription(lang, errorCode) + " " + exceptionMessage).trim());
		try {
			return mapper.writeValueAsString(errorInfo);
		} catch (IOException e) {
			logger.error("getStreamErrorJson", e);
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static String getCaptureErrorJson(String specVersion, String lang, String errorCode, String exceptionMessage,
			boolean isRCapture) {
		List<BioMetricsDto> biometrics = new ArrayList<>();

		BioMetricsDto biometric = new BioMetricsDto();
		biometric.setSpecVersion(specVersion);
		biometric.setData("");
		biometric.setHash("");
		if (!isRCapture) {
			biometric.setSessionKey("");
			biometric.setThumbprint("");
		}

		biometric.setError(
				new ErrorInfo(errorCode, (getErrorDescription(lang, errorCode) + " " + exceptionMessage).trim()));

		RCaptureResponse captureResponse = new RCaptureResponse();
		biometrics.add(biometric);
		captureResponse.setBiometrics(biometrics);

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		try {
			return mapper.writeValueAsString(captureResponse);
		} catch (IOException e) {
			logger.error("getCaptureErrorJson", e);
		}
		return null;
	}

	public static String getErrorDescription(String lang, String errorCode) {
		if (lang == null || lang.trim().equals(""))
			lang = "en";
		String message = "mds_ERROR_" + errorCode + "_msg_" + lang;
		String errorDescription = ApplicationPropertyHelper.getPropertyKeyValue(message);

		if (errorDescription == null || errorDescription.trim().equals("")) {
			errorDescription = "No Description available.";
		}
		return errorDescription;
	}
}

@Getter
@Setter
@SuppressWarnings({ "java:S1104" })
class ErrorDto {
	public ErrorInfo error;
}
