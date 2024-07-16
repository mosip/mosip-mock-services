package io.mosip.mock.sbi.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.biometric.provider.CryptoUtility;
import org.biometric.provider.JwtUtility;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.devicehelper.SBIBioSubTypeInfo;
import io.mosip.mock.sbi.devicehelper.SBICheckState;
import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.mock.sbi.devicehelper.face.SBIFaceCaptureInfo;
import io.mosip.mock.sbi.devicehelper.finger.single.SBIFingerSingleCaptureInfo;
import io.mosip.mock.sbi.devicehelper.finger.slap.SBIFingerSlapBioExceptionInfo;
import io.mosip.mock.sbi.devicehelper.finger.slap.SBIFingerSlapCaptureInfo;
import io.mosip.mock.sbi.devicehelper.iris.binacular.SBIIrisDoubleBioExceptionInfo;
import io.mosip.mock.sbi.devicehelper.iris.binacular.SBIIrisDoubleCaptureInfo;
import io.mosip.mock.sbi.devicehelper.iris.monocular.SBIIrisSingleBioExceptionInfo;
import io.mosip.mock.sbi.devicehelper.iris.monocular.SBIIrisSingleCaptureInfo;
import io.mosip.mock.sbi.exception.SBIException;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.StringHelper;
import io.mosip.registration.mdm.dto.BioMetricsDataDto;
import io.mosip.registration.mdm.dto.BioMetricsDto;
import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailDto;
import io.mosip.registration.mdm.dto.CaptureRequestDto;
import io.mosip.registration.mdm.dto.DelayRequest;
import io.mosip.registration.mdm.dto.DeviceDiscoveryRequestDetail;
import io.mosip.registration.mdm.dto.DeviceInfo;
import io.mosip.registration.mdm.dto.DeviceInfoDto;
import io.mosip.registration.mdm.dto.DiscoverDto;
import io.mosip.registration.mdm.dto.ErrorInfo;
import io.mosip.registration.mdm.dto.ProfileRequest;
import io.mosip.registration.mdm.dto.RCaptureResponse;
import io.mosip.registration.mdm.dto.ScoreRequest;
import io.mosip.registration.mdm.dto.StatusRequest;
import io.mosip.registration.mdm.dto.StreamingRequestDetail;

public class SBIServiceResponse {
	private static final Logger logger = LoggerFactory.getLogger(SBIServiceResponse.class);
	private static final String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

	protected int port = 0;
	protected String request = "";
	private static Semaphore semaphore = new Semaphore(1);

	private static final String[] bioExceptionsArrayFinger = { "Left IndexFinger", "Left MiddleFinger",
			"Left RingFinger", "Left LittleFinger", "Left Thumb", "Right IndexFinger", "Right MiddleFinger",
			"Right RingFinger", "Right LittleFinger", "Right Thumb" };
	private static final String[] bioExceptionsArrayIris = { "Left", "Right" };
	private static final List<String> bioExceptionsListFinger = Arrays.asList(bioExceptionsArrayFinger);
	private static final List<String> bioExceptionsListIris = Arrays.asList(bioExceptionsArrayIris);

	private static final String[] bioSubtypesArrayFinger = { "Left IndexFinger", "Left MiddleFinger", "Left RingFinger",
			"Left LittleFinger", "Left Thumb", "Right IndexFinger", "Right MiddleFinger", "Right RingFinger",
			"Right LittleFinger", "Right Thumb", "UNKNOWN" };
	private static final String[] bioSubtypesArrayIris = { "Left", "Right", "UNKNOWN" };
	private static final List<String> bioSubtypesListFinger = Arrays.asList(bioSubtypesArrayFinger);
	private static final List<String> bioSubtypesListIris = Arrays.asList(bioSubtypesArrayIris);

	public SBIServiceResponse(int port) {
		setPort(port);
	}

	public String getServiceresponse(SBIMockService mockService, Socket socket, String strJsonRequest) {
		String responseJson = "";
		setRequest(strJsonRequest);

		if (strJsonRequest.contains(SBIConstant.MOSIP_POST_VERB) || strJsonRequest.contains(SBIConstant.MOSIP_GET_VERB)
				|| strJsonRequest.contains(SBIConstant.MOSIP_DISC_VERB)) {
			responseJson = processDeviceDicoveryInfo(mockService);
		} else if (strJsonRequest.contains(SBIConstant.MOSIP_INFO_VERB)) {
			responseJson = processDeviceInfo(mockService);
		} else if (strJsonRequest.contains(SBIConstant.MOSIP_STREAM_VERB)) {
			responseJson = processLiveStreamInfo(mockService, socket);
		} else if (strJsonRequest.contains(SBIConstant.MOSIP_RCAPTURE_VERB)) {
			responseJson = processRCaptureInfo(mockService);
		} else if (strJsonRequest.contains(SBIConstant.MOSIP_CAPTURE_VERB)) {
			responseJson = processCaptureInfo(mockService);
		} else if (strJsonRequest.contains(SBIConstant.MOSIP_ADMIN_API_STATUS)) {
			responseJson = processSetStatus(mockService);
		} else if (strJsonRequest.contains(SBIConstant.MOSIP_ADMIN_API_SCORE)) {
			responseJson = processSetQualityScore(mockService);
		} else if (strJsonRequest.contains(SBIConstant.MOSIP_ADMIN_API_DELAY)) {
			responseJson = processSetDelay(mockService);
		} else if (strJsonRequest.contains(SBIConstant.MOSIP_ADMIN_API_PROFILE)) {
			responseJson = processSetProfileInfo(mockService);
		} else {
			responseJson = SBIResponseInfo.generateErrorResponse("en", getPort(), "500", "");
		}
		return responseJson;
	}

	@SuppressWarnings({ "java:S3776", "java:S6541" })
	public String processDeviceDicoveryInfo(SBIMockService mockService) {
		String response = null;
		String lang = "en";
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			DeviceDiscoveryRequestDetail requestObject = (DeviceDiscoveryRequestDetail) getRequestJson(
					SBIConstant.MOSIP_DISC_VERB);
			String type = null;
			if (requestObject != null && requestObject.getType() != null && requestObject.getType().length() > 0)
				type = requestObject.getType();

			logger.info("processDeviceDicoveryInfo :: type :: {}", type);

			List<DiscoverDto> infoList = new ArrayList<>();
			if (type == null || type.trim().length() == 0) {
				return SBIJsonInfo.getErrorJson(lang, "502", "");
			} else if (!type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) {
				return SBIJsonInfo.getErrorJson(lang, "502", "");
			} else {
				long delay = 0;
				SBIDeviceHelper deviceHelper = null;
				if (type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
						|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) {
					if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
						deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
								+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP);
					} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
						deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
								+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE);
					}
					if (deviceHelper != null) {
						delay = deviceHelper.getDelayForMethod(SBIConstant.MOSIP_METHOD_MOSIPDISC);
						delay(delay);

						deviceHelper.initDeviceDetails();
						DiscoverDto discoverInfo = deviceHelper.getDiscoverDto();
						if (discoverInfo != null)
							infoList.add(discoverInfo);
					}
				}

				if (type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
						|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) {
					deviceHelper = mockService.getDeviceHelper(
							SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE);
					if (deviceHelper != null) {
						delay = deviceHelper.getDelayForMethod(SBIConstant.MOSIP_METHOD_MOSIPDISC);
						delay(delay);
						deviceHelper.initDeviceDetails();
						DiscoverDto discoverInfo = deviceHelper.getDiscoverDto();
						if (discoverInfo != null)
							infoList.add(discoverInfo);
					}
				}

				if (type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
						|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) {
					if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
						deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_"
								+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE);
					} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
						deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_"
								+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE);
					}
					if (deviceHelper != null) {
						delay = deviceHelper.getDelayForMethod(SBIConstant.MOSIP_METHOD_MOSIPDISC);
						delay(delay);

						deviceHelper.initDeviceDetails();
						DiscoverDto discoverInfo = deviceHelper.getDiscoverDto();
						if (discoverInfo != null)
							infoList.add(discoverInfo);
					}
				}

				if (infoList != null && !infoList.isEmpty()) {
					return objectMapper.writeValueAsString(infoList);
				} else {
					return SBIJsonInfo.getErrorJson(lang, "503", "");
				}
			}
		} catch (Exception ex) {
			response = SBIResponseInfo.generateErrorResponse(lang, getPort(), SBIConstant.ERROR_CODE_999 + "", "");
			logger.error("processDeviceDicoveryInfo", ex);
		}
		return response;
	}

	@SuppressWarnings({ "java:S3776" })
	public String processDeviceInfo(SBIMockService mockService) {
		String response = null;
		String lang = "en";
		try {
			long delay = 0;
			ObjectMapper objectMapper = new ObjectMapper();

			List<DeviceInfoDto> infoList = new ArrayList<>();
			DeviceInfoDto deviceInfoDto = null;
			SBIDeviceHelper deviceHelper = null;
			if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
				deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
						+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP);
			} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
				deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
						+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE);
			}

			if (deviceHelper != null) {
				delay = deviceHelper.getDelayForMethod(SBIConstant.MOSIP_METHOD_MOSIPDINFO);
				delay(delay);

				deviceHelper.initDeviceDetails();
				deviceInfoDto = deviceHelper.getDeviceInfoDto();
				if (deviceInfoDto != null)
					infoList.add(deviceInfoDto);
			}

			deviceHelper = mockService.getDeviceHelper(
					SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE);
			if (deviceHelper != null) {
				delay = deviceHelper.getDelayForMethod(SBIConstant.MOSIP_METHOD_MOSIPDINFO);
				delay(delay);

				deviceHelper.initDeviceDetails();
				deviceInfoDto = deviceHelper.getDeviceInfoDto();
				if (deviceInfoDto != null)
					infoList.add(deviceInfoDto);
			}

			if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
				deviceHelper = mockService.getDeviceHelper(
						SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE);
			} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
				deviceHelper = mockService.getDeviceHelper(
						SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE);
			}
			if (deviceHelper != null) {
				delay = deviceHelper.getDelayForMethod(SBIConstant.MOSIP_METHOD_MOSIPDINFO);
				delay(delay);

				deviceHelper.initDeviceDetails();
				deviceInfoDto = deviceHelper.getDeviceInfoDto();
				if (deviceInfoDto != null)
					infoList.add(deviceInfoDto);
			}

			if (infoList != null && !infoList.isEmpty()) {
				return objectMapper.writeValueAsString(infoList);
			} else {
				return SBIJsonInfo.getErrorJson(lang, "106", "");
			}
		} catch (Exception ex) {
			response = SBIResponseInfo.generateErrorResponse(lang, getPort(), SBIConstant.ERROR_CODE_999 + "", "");
			logger.error("processDeviceDicoveryInfo", ex);
		}
		return response;
	}

	@SuppressWarnings({ "java:S3776", "java:S6541" })
	public String processSetStatus(SBIMockService mockService) {
		String response = null;
		String lang = "en";
		try {
			StatusRequest requestObject = (StatusRequest) getRequestJson(SBIConstant.MOSIP_ADMIN_API_STATUS);
			String type = null;
			String status = null;
			if (requestObject != null && requestObject.getType() != null && requestObject.getType().length() > 0)
				type = requestObject.getType();

			if (requestObject != null && requestObject.getDeviceStatus() != null
					&& requestObject.getDeviceStatus().length() > 0)
				status = requestObject.getDeviceStatus();

			logger.info("processSetStatus :: Type :: {}  :: Status :: {}", type, status);

			if (type == null || type.trim().length() == 0) {
				return SBIJsonInfo.getAdminApiErrorJson(lang, "502", "");
			} else if (!type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) {
				return SBIJsonInfo.getAdminApiErrorJson(lang, "502", "");
			} else if (!status.equals(SBIConstant.DEVICE_STATUS_ISREADY)
					&& !status.equals(SBIConstant.DEVICE_STATUS_ISBUSY)
					&& !status.equals(SBIConstant.DEVICE_STATUS_NOTREADY)
					&& !status.equals(SBIConstant.DEVICE_STATUS_NOTREGISTERED)) {
				return SBIJsonInfo.getAdminApiErrorJson(lang, "504", "");
			} else {
				SBIDeviceHelper deviceHelper = null;
				if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
					deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
							+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP);
				} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
					deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
							+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE);
				}
				if (deviceHelper != null) {
					deviceHelper.setDeviceStatus((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) ? status
									: SBIConstant.DEVICE_STATUS_ISREADY));
				}

				deviceHelper = mockService.getDeviceHelper(
						SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE);
				if (deviceHelper != null) {
					deviceHelper.setDeviceStatus((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) ? status
									: SBIConstant.DEVICE_STATUS_ISREADY));
				}

				if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
					deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_"
							+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE);
				} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
					deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_"
							+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE);
				}
				if (deviceHelper != null) {
					deviceHelper.setDeviceStatus((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) ? status
									: SBIConstant.DEVICE_STATUS_ISREADY));
				}
			}

			response = SBIJsonInfo.getAdminApiErrorJson(lang, "0", "");
		} catch (Exception ex) {
			response = SBIJsonInfo.getAdminApiErrorJson(lang, "999", ex.getLocalizedMessage() + "");
			logger.error("processSetStatus", ex);
		}
		return response;
	}

	@SuppressWarnings({ "java:S1125", "java:S3776", "java:S6541" })
	public String processSetQualityScore(SBIMockService mockService) {
		String response = null;
		String lang = "en";
		try {
			ScoreRequest requestObject = (ScoreRequest) getRequestJson(SBIConstant.MOSIP_ADMIN_API_SCORE);
			String type = null;
			String qualityScore = null;
			boolean scoreFromIso = false;
			if (requestObject != null && requestObject.getType() != null && requestObject.getType().length() > 0)
				type = requestObject.getType();

			if (requestObject != null && requestObject.getQualityScore() != null
					&& requestObject.getQualityScore().length() > 0)
				qualityScore = requestObject.getQualityScore();

			if (requestObject != null)
				scoreFromIso = requestObject.isFromIso();

			logger.info("processSetQualityScore :: Type :: {} :: qualityScore :: {} :: fromIso :: {} ", type,
					qualityScore, scoreFromIso);

			if (type == null || type.trim().length() == 0) {
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "502", "");
			} else if (!type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) {
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "502", "");
			} else if (qualityScore == null || qualityScore.trim().length() == 0) {
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "505", "");
			} else if (qualityScore != null
					&& (Float.parseFloat(qualityScore) < 0.0f || Float.parseFloat(qualityScore) > 100.0f)) {
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "505", "");
			} else {
				float defaultQualityScore = Float.parseFloat(
						ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_MOCK_SBI_QUALITY_SCORE));

				SBIDeviceHelper deviceHelper = null;
				if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
					deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
							+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP);
				} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
					deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
							+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE);
				}
				if (deviceHelper != null) {
					deviceHelper.setScoreFromIso((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) ? scoreFromIso : false);
					deviceHelper.setQualityScore((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) ? Float.parseFloat(qualityScore)
									: defaultQualityScore);
					deviceHelper.setQualityScoreSet((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)));
				}

				deviceHelper = mockService.getDeviceHelper(
						SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE);
				if (deviceHelper != null) {
					deviceHelper.setScoreFromIso((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) ? scoreFromIso : false);
					deviceHelper.setQualityScore((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) ? Float.parseFloat(qualityScore)
									: defaultQualityScore);
					deviceHelper.setQualityScoreSet((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)));
				}

				if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
					deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_"
							+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE);
				} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
					deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_"
							+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE);
				}
				if (deviceHelper != null) {
					deviceHelper.setScoreFromIso((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) ? scoreFromIso : false);
					deviceHelper.setQualityScore((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) ? Float.parseFloat(qualityScore)
									: defaultQualityScore);
					deviceHelper.setQualityScoreSet((type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
							|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)));
				}

				response = SBIJsonInfo.getAdminApiErrorJson(lang, "0", "");
			}
		} catch (Exception ex) {
			response = SBIJsonInfo.getAdminApiErrorJson(lang, "999", ex.getLocalizedMessage() + "");
			logger.error("processSetQualityScore", ex);
		}
		return response;
	}

	@SuppressWarnings({ "java:S3776", "java:S6541" })
	public String processSetDelay(SBIMockService mockService) {
		String response = null;
		String lang = "en";
		try {
			DelayRequest requestObject = (DelayRequest) getRequestJson(SBIConstant.MOSIP_ADMIN_API_DELAY);
			String type = null;
			String delay = null;
			String[] method = null;
			if (requestObject != null && requestObject.getType() != null && requestObject.getType().length() > 0)
				type = requestObject.getType();

			if (requestObject != null && requestObject.getDelay() != null && requestObject.getDelay().length() > 0)
				delay = requestObject.getDelay();

			if (requestObject != null && requestObject.getMethod() != null && requestObject.getMethod().length > 0)
				method = requestObject.getMethod();

			logger.info("processSetDelay :: Type :: {} :: Delay :: {}", type, delay);

			if (type == null || type.trim().length() == 0) {
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "502", "");
			} else if (!type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)
					&& !type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) {
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "502", "");
			} else if (delay == null || delay.trim().length() == 0) {
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "506", "");
			} else if (delay != null && Long.parseLong(delay) < 0) {
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "506", "");
			} else {
				boolean isValidMethod = true;
				String corsHeaderMethodsFor = ApplicationPropertyHelper
						.getPropertyKeyValue(SBIConstant.CORS_HEADER_METHODS);
				if (method == null || method.length == 0) {
					method = corsHeaderMethodsFor.split(",");
				}
				if (method != null && method.length > 0) {
					for (int index = 0; index < method.length; index++) {
						if (!corsHeaderMethodsFor.contains(method[index].trim())) {
							response = SBIJsonInfo.getAdminApiErrorJson(lang, "507", "");
							isValidMethod = false;
							break;
						}
					}
				}
				if (isValidMethod) {
					SBIDeviceHelper deviceHelper = null;
					if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
						deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
								+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP);
					} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
						deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_"
								+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE);
					}

					if (deviceHelper != null) {
						deviceHelper.resetDelayForMethod();
						deviceHelper.setDelayForMethod(
								(type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
										|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)) ? method : null,
								Long.parseLong(delay));
					}

					deviceHelper = mockService.getDeviceHelper(
							SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE);
					if (deviceHelper != null) {
						deviceHelper.resetDelayForMethod();
						deviceHelper.setDelayForMethod(
								(type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
										|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)) ? method : null,
								Long.parseLong(delay));
					}

					if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
						deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_"
								+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE);
					} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
						deviceHelper = mockService.getDeviceHelper(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_"
								+ SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE);
					}
					if (deviceHelper != null) {
						deviceHelper.resetDelayForMethod();
						deviceHelper.setDelayForMethod(
								(type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
										|| type.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)) ? method : null,
								Long.parseLong(delay));
					}

					response = SBIJsonInfo.getAdminApiErrorJson(lang, "0", "");
				}
			}
		} catch (Exception ex) {
			response = SBIJsonInfo.getAdminApiErrorJson(lang, "999", ex.getLocalizedMessage() + "");
			logger.error("processSetDelay", ex);
		}
		return response;
	}

	public String processSetProfileInfo(SBIMockService mockService) {
		String response = null;
		String lang = "en";
		try {
			ProfileRequest requestObject = (ProfileRequest) getRequestJson(SBIConstant.MOSIP_ADMIN_API_PROFILE);
			if (requestObject != null && requestObject.getProfileId() != null
					&& requestObject.getProfileId().length() > 0) {
				mockService.setProfileId(requestObject.getProfileId());
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "0", "");
			} else {
				logger.info("processSetProfileInfo :: ProfileId :: Was  Not SET Check the JSON request :: ");
				mockService.setProfileId(SBIConstant.PROFILE_DEFAULT);
				response = SBIJsonInfo.getAdminApiErrorJson(lang, "0", "");
			}

			logger.info("processSetProfileInfo :: ProfileId :: {}", mockService.getProfileId());
		} catch (Exception ex) {
			response = SBIJsonInfo.getAdminApiErrorJson(lang, "999", ex.getLocalizedMessage() + "");
			logger.error("processSetProfileInfo", ex);
		}
		return response;
	}

	@SuppressWarnings({ "java:S135", "java:S1141", "java:S2142", "java:S3626", "java:S3776", "java:S6541" })
	private String processLiveStreamInfo(SBIMockService mockService, Socket socket) {
		String response = null;
		String lang = "en";
		SBIDeviceHelper deviceHelper = null;
		try {
			if (mockService.getPurpose()
					.equals(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_AUTH))) {
				return SBIJsonInfo.getStreamErrorJson(lang, "601", "");
			}

			StreamingRequestDetail requestObject = (StreamingRequestDetail) getRequestJson(
					SBIConstant.MOSIP_STREAM_VERB);
			String deviceId = requestObject.getDeviceId();
			int deviceSubId = Integer.parseInt(requestObject.getDeviceSubId());
			boolean isStreamTimeoutSet = false;
			long timeout = 0;
			if (requestObject.getTimeout() != null && requestObject.getTimeout().trim().length() != 0
					&& Long.parseLong(requestObject.getTimeout().trim()) > 0) {
				timeout = Long.parseLong(requestObject.getTimeout().trim());
				isStreamTimeoutSet = true;
			}

			logger.info("processLiveStreamInfo :: deviceId :: {}  :: deviceSubId ::{}", deviceId, deviceSubId);

			if (deviceId != null && deviceId.trim().length() == 0) {
				return SBIJsonInfo.getStreamErrorJson(lang, "604", "");
			}

			deviceHelper = getDeviceHelperForDeviceId(mockService, deviceId);
			if (deviceHelper == null || deviceHelper.getDeviceInfo() == null) {
				return SBIJsonInfo.getStreamErrorJson(lang, "605", "");
			}
			if (deviceHelper.getDeviceInfo() != null
					&& !deviceHelper.getDeviceInfo().getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
				return SBIJsonInfo.getStreamErrorJson(lang, "606", "");
			}
			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_NOTREGISTERED)) {
				return SBIJsonInfo.getStreamErrorJson(lang, "100", "");
			}
			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_NOTREADY)) {
				return SBIJsonInfo.getStreamErrorJson(lang, "110", "");
			}
			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_ISBUSY)) {
				return SBIJsonInfo.getStreamErrorJson(lang, "111", "");
			}
			if (deviceHelper.getDeviceInfo() != null
					&& !deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_ISREADY)) {
				return SBIJsonInfo.getStreamErrorJson(lang, "607", "");
			}

			deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISBUSY);
			deviceHelper.initDevice();
			deviceHelper.setDeviceId(deviceId);
			deviceHelper.setDeviceSubId(deviceSubId);
			deviceHelper.getCaptureInfo().setLiveStreamStarted(true);
			renderMainHeaderData(socket);
			int returnCode = -1;

			long startTime = System.currentTimeMillis();
			long endTime = startTime + timeout;
			boolean streamTimeOut = false;

			long delay = deviceHelper.getDelayForMethod(SBIConstant.MOSIP_METHOD_STREAM);
			while (true) {
				delay(delay);
				if (isStreamTimeoutSet && System.currentTimeMillis() > endTime) {
					streamTimeOut = true;
					break;
				}

				if (deviceHelper.getCaptureInfo() == null) {
					response = "ok";
					break;
				}

				try {
					// acquiring the lock
					if (semaphore != null)
						semaphore.acquire();

					returnCode = deviceHelper.getLiveStream();

					if (returnCode < 0)
						break;
					if (returnCode != 0)
						continue;
				} catch (Exception ex) {
					logger.error("processLiveStreamInfo :: error ::", ex);
				} finally {
					try {
						if (semaphore != null)
							semaphore.release();
					} catch (Exception ex) {
						logger.error("processLiveStreamInfo :: semaphore error ::", ex);
					}
				}

				if (deviceHelper.getCaptureInfo() != null && deviceHelper.getCaptureInfo().getImage() != null) {
					try {
						renderJPGImageData(socket, deviceHelper.getCaptureInfo().getImage());
					} catch (Exception ex) {
						logger.error("processLiveStreamInfo :: Exception ::", ex);
						break;
					}
				}

				Thread.sleep(30);
			}
			if (deviceHelper.getCaptureInfo() != null) {
				deviceHelper.deInitDevice();
				deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
			}
			if (streamTimeOut)
				response = SBIJsonInfo.getStreamErrorJson(lang, "609", "");
			else
				response = SBIJsonInfo.getStreamErrorJson(lang, "0", "");
		} catch (Exception ex) {
			response = SBIJsonInfo.getStreamErrorJson(lang, "610", ex.getLocalizedMessage());
			logger.error("processLiveStreamInfo", ex);
		} finally {
			try {
				if (semaphore != null)
					semaphore.release();
			} catch (Exception ex) {
				logger.error("processLiveStreamInfo :: semaphore error ::", ex);
			}
		}
		return response;
	}

	@SuppressWarnings({ "java:S135", "java:S1141", "java:S2142", "java:S3776", "java:S6541", "deprecation", "unused" })
	private String processRCaptureInfo(SBIMockService mockService) {
		String response = null;
		String lang = "en";
		String specVersion = "";
		SBIDeviceHelper deviceHelper = null;
		try {
			if (!mockService.getPurpose().equals(SBIConstant.MOSIP_PURPOSE_REGISTRATION)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "709", "", true);
			}

			String deviceId = "";
			String deviceType = "";
			String env = "";
			String purpose = "";
			int deviceSubId = 0;
			CaptureRequestDto requestObject = (CaptureRequestDto) getRequestJson(SBIConstant.MOSIP_RCAPTURE_VERB);
			List<CaptureRequestDeviceDetailDto> mosipBioRequest = null;
			// if Null Throw Errors here
			if (requestObject != null) {
				mosipBioRequest = requestObject.getBio();
				if (mosipBioRequest != null && !mosipBioRequest.isEmpty()) {
					deviceId = requestObject.getBio().get(0).getDeviceId();
					deviceSubId = Integer.parseInt(requestObject.getBio().get(0).getDeviceSubId());
					deviceType = requestObject.getBio().get(0).getType();
					env = requestObject.getEnv();
					purpose = requestObject.getPurpose();
				}
			}

			logger.info("processRCaptureInfo :: deviceId :: {}  :: deviceSubId :: {}", deviceId, deviceSubId);
			if (env == null || env.trim().length() == 0) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "115", "", true);
			} else if (env != null && env.trim().length() > 0
					&& !(env.equals(SBIConstant.ENVIRONMENT_STAGING) || env.equals(SBIConstant.ENVIRONMENT_DEVELOPER)
							|| env.equals(SBIConstant.ENVIRONMENT_PRE_PRODUCTION)
							|| env.equals(SBIConstant.ENVIRONMENT_PRODUCTION))) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "115", "", true);
			} else if (purpose != null && purpose.trim().length() > 0
					&& !(purpose.equals(SBIConstant.PURPOSE_REGISTRATION))) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "121", "", true);
			}

			if (deviceId != null && deviceId.trim().length() == 0) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "704", "", true);
			}
			if (deviceType == null || deviceType.trim().length() == 0) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "114", "", true);
			}

			if (deviceType != null && deviceType.trim().length() > 0
					&& !(deviceType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)
							|| deviceType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)
							|| deviceType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "114", "", true);
			}

			deviceHelper = getDeviceHelperForDeviceId(mockService, deviceId);
			if (deviceHelper == null || deviceHelper.getDeviceInfo() == null) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "705", "", true);
			}
			if (deviceType != null && !deviceHelper.getDeviceType().equals(deviceType)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "120", "", true);
			}

			if (deviceHelper.getDeviceInfo() != null
					&& !deviceHelper.getDeviceInfo().getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "706", "", true);
			}
			if (deviceHelper.getCaptureInfo() != null && (!deviceHelper.getDeviceId().equalsIgnoreCase(deviceId)
					&& deviceHelper.getDeviceSubId() != deviceSubId)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "702", "", true);
			}

			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_NOTREGISTERED)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "100", "", true);
			}
			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_NOTREADY)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "110", "", true);
			}
			if (!StringHelper.isAlphaNumericHyphenWithMinMaxLength(requestObject.getTransactionId())) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "112", "", true);
			}

			String bioType = mosipBioRequest.get(0).getType();
			String[] bioException = mosipBioRequest.get(0).getException();// Bio exceptions
			String[] bioSubtype = mosipBioRequest.get(0).getBioSubType();// Bio subtype
			int count = Integer.parseInt(mosipBioRequest.get(0).getCount());
			int exceptionCount = (bioException != null ? bioException.length : 0);
			int bioSubtypeCount = (bioSubtype != null ? bioSubtype.length : 0);
			int finalCount = count + exceptionCount;

			if (bioSubtypeCount != 0 && !isValidBioSubtypeValues(bioType, bioSubtype, false)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "122", "", true);
			}
			if (exceptionCount != 0 && !isValidBioExceptionValues(bioType, bioException)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "122", "", true);
			}

			switch (bioType) {
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER:
				switch (deviceSubId) {
				case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT:
					// Max Count = 4 exception allowed
					if (finalCount != 4)
						return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", true);
					break;
				case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT:
					// Max Count = 4 exception allowed
					if (finalCount != 4)
						return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", true);
					break;
				case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB:
					// Max Count = 2 exception allowed
					if (finalCount != 2)
						return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", true);
					break;
				default:
					break;
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS:
				switch (deviceSubId) {
				case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_LEFT:
					// Max Count = 1 no exception allowed
					if (count != 1 || exceptionCount != 0)
						return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", true);
					break;
				case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_RIGHT:
					// Max Count = 1 no exception allowed
					if (count != 1 || exceptionCount != 0)
						return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", true);
					break;
				case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_BOTH:
					// Max Count = 2 exception allowed
					finalCount = count + exceptionCount;
					if (finalCount != 2)
						return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", true);
					break;
				default:
					break;
				}
				break;
			case SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE:
				// Max Face Count = 1 with or without exception
				if (count != 1)
					return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", true);
				break;
			default:
				break;
			}

			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_ISREADY)) {
				deviceHelper.initDevice();
				deviceHelper.setDeviceId(deviceId);
				deviceHelper.setDeviceSubId(deviceSubId);
				deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISBUSY);
			} else if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_ISBUSY)
					&& deviceHelper.getCaptureInfo() == null || deviceHelper.getCaptureInfo().isCaptureStarted()) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "703", "", true);
			}

			int timeout = Integer.parseInt(requestObject.getTimeout() + "");
			float requestScore = Float.parseFloat(mosipBioRequest.get(0).getRequestedScore() + "");

			specVersion = requestObject.getSpecVersion();
			int returnCode = -1;
			long startTime = System.currentTimeMillis();
			long endTime = startTime + timeout;
			boolean captureStarted = false;
			boolean captureTimeOut = false;
			boolean captureLiveStreamEnded = false;
			long delay = deviceHelper.getDelayForMethod(SBIConstant.MOSIP_METHOD_RCAPTURE);
			while (true) {
				if (!captureStarted) {
					deviceHelper.setProfileId(mockService.getProfileId());

					if (bioException != null && !bioType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))
						deviceHelper.getCaptureInfo().getBioExceptionInfo().initBioException(bioException);

					deviceHelper.getCaptureInfo().setRequestScore(requestScore);
					deviceHelper.getCaptureInfo().setCaptureStarted(true);
					captureStarted = true;
				}
				delay(delay);
				try {
					if (System.currentTimeMillis() > endTime) {
						captureTimeOut = true;
						break;
					}
					// acquiring the lock
					if (semaphore != null)
						semaphore.acquire();

					if (deviceHelper.getCaptureInfo() == null) {
						captureLiveStreamEnded = true;
						break;
					}

					returnCode = deviceHelper.getBioCapture(false);

					if (deviceHelper.getCaptureInfo() != null && deviceHelper.getCaptureInfo().isCaptureCompleted()) {
						break;
					}
				} catch (Exception ex) {
					logger.error("processRCaptureInfo :: error ::", ex);
				} finally {
					try {
						if (semaphore != null)
							semaphore.release();
					} catch (Exception ex) {
						logger.error("processRCaptureInfo :: semaphore error ::", ex);
					}
				}

				Thread.sleep(30);
			}

			if (captureLiveStreamEnded) {
				response = SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "700", "", true);
			} else if (captureTimeOut) {
				response = SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "701", "", true);
				if (deviceHelper.getCaptureInfo() == null)
					deviceHelper.getCaptureInfo().setCaptureCompleted(true);
			} else {
				List<BioMetricsDto> biometrics = getBioMetricsDtoList(lang, requestObject, deviceHelper, deviceSubId,
						false);
				if (biometrics != null && !biometrics.isEmpty()) {
					RCaptureResponse captureResponse = new RCaptureResponse();
					captureResponse.setBiometrics(biometrics);

					ObjectMapper mapper = new ObjectMapper();
					SerializationConfig config = mapper.getSerializationConfig();
					config.setSerializationInclusion(Inclusion.NON_NULL);
					mapper.setSerializationConfig(config);

					response = mapper.writeValueAsString(captureResponse);
				} else {
					response = SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "708", "", true);
				}

				deviceHelper.deInitDevice();
				deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
			}

			if (deviceHelper.getCaptureInfo() != null) {
				deviceHelper.getCaptureInfo().getBioExceptionInfo().deInitBioException();
				// When Capture is called After LiveStreaming is called
				// DeInit is called in Livestream method
				if (deviceHelper.getCaptureInfo().isLiveStreamStarted()) {
					deviceHelper.getCaptureInfo().setCaptureStarted(false);
					deviceHelper.getCaptureInfo().setCaptureCompleted(true);
				}
				// DeInit When Capture is called Directly
				else {
					deviceHelper.deInitDevice();
					deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
				}
			}
		} catch (

		Exception ex) {
			response = SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "710", "", true);
			logger.error("processRCaptureInfo", ex);
		} finally {
			try {
				if (semaphore != null)
					semaphore.release();
			} catch (Exception ex) {
				logger.error("processRCaptureInfo :: semaphore error ::", ex);
			}
		}
		return response;
	}

	@SuppressWarnings({ "java:S135", "java:S1141", "java:S2142", "java:S3776", "java:S6541", "deprecation", "unused" })
	private String processCaptureInfo(SBIMockService mockService) {
		String response = null;
		String lang = "en";
		String specVersion = "";
		SBIDeviceHelper deviceHelper = null;
		try {
			if (!mockService.getPurpose()
					.equalsIgnoreCase(SBIConstant.MOSIP_PURPOSE_AUTH)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "809", "", false);
			}

			String deviceId = "";
			String deviceType = "";
			String env = "";
			String purpose = "";
			int deviceSubId = 0;
			CaptureRequestDto requestObject = (CaptureRequestDto) getRequestJson(SBIConstant.MOSIP_RCAPTURE_VERB);
			List<CaptureRequestDeviceDetailDto> mosipBioRequest = null;
			// if Null Throw Errors here
			if (requestObject != null) {
				mosipBioRequest = requestObject.getBio();
				if (mosipBioRequest != null && !mosipBioRequest.isEmpty()) {
					deviceId = requestObject.getBio().get(0).getDeviceId();
					deviceSubId = Integer.parseInt(requestObject.getBio().get(0).getDeviceSubId());
					deviceType = requestObject.getBio().get(0).getType();
					env = requestObject.getEnv();
					purpose = requestObject.getPurpose();
				}
			}

			logger.info("processCaptureInfo :: deviceId :: {}  :: deviceSubId ::{}", deviceId, deviceSubId);

			if (env == null || env.trim().length() == 0) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "115", "", false);
			} else if (env != null && env.trim().length() > 0
					&& !(env.equals(SBIConstant.ENVIRONMENT_STAGING) || env.equals(SBIConstant.ENVIRONMENT_DEVELOPER)
							|| env.equals(SBIConstant.ENVIRONMENT_PRE_PRODUCTION)
							|| env.equals(SBIConstant.ENVIRONMENT_PRODUCTION))) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "115", "", false);
			} else if (purpose != null && purpose.trim().length() > 0 && !(purpose.equals(SBIConstant.PURPOSE_AUTH))) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "121", "", false);
			}

			if (deviceId == null || deviceId.trim().length() == 0) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "804", "", false);
			}
			if (deviceType == null || deviceType.trim().length() == 0) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "114", "", false);
			}

			if (deviceType != null && deviceType.trim().length() > 0
					&& !(deviceType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)
							|| deviceType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)
							|| deviceType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "114", "", false);
			}

			deviceHelper = getDeviceHelperForDeviceId(mockService, deviceId);
			if (deviceHelper == null || deviceHelper.getDeviceInfo() == null) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "805", "", false);
			}
			if (deviceType != null && !deviceHelper.getDeviceType().equals(deviceType)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "120", "", false);
			}

			if (deviceHelper.getDeviceInfo() != null
					&& !deviceHelper.getDeviceInfo().getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "806", "", false);
			}
			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_NOTREGISTERED)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "100", "", false);
			}
			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_NOTREADY)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "110", "", false);
			}
			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_ISBUSY)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "111", "", false);
			}
			if (!StringHelper.isAlphaNumericHyphenWithMinMaxLength(requestObject.getTransactionId())) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "112", "", false);
			}

			String bioType = mosipBioRequest.get(0).getType();
			String[] bioSubType = mosipBioRequest.get(0).getBioSubType();// Bio Subtype
			int timeout = Integer.parseInt(requestObject.getTimeout() + "");

			float requestScore = Float.parseFloat(mosipBioRequest.get(0).getRequestedScore() + "");
			int bioCount = Integer.parseInt(mosipBioRequest.get(0).getCount() + "");

			if (!isValidBioSubtypeValues(bioType, bioSubType, true)) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "122", "", true);
			}
			if (deviceType != null) {
				if ((bioCount < 0 || bioCount > 10) && deviceType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER))
					return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", false);
				if ((bioCount < 0 || bioCount > 2) && deviceType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS))
					return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", false);
				if ((bioCount < 0 || bioCount > 1) && deviceType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))
					return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "109", "", false);
			}

			if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_ISREADY)) {
				deviceHelper.initDevice();
				deviceHelper.setDeviceId(deviceId);
				deviceHelper.setDeviceSubId(deviceSubId);
				deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISBUSY);
			} else if (deviceHelper.getDeviceInfo() != null
					&& deviceHelper.getDeviceInfo().getDeviceStatus().equals(SBIConstant.DEVICE_STATUS_ISBUSY)
					&& deviceHelper.getCaptureInfo().isCaptureStarted()) {
				return SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "803", "", false);
			}

			specVersion = requestObject.getSpecVersion();
			int returnCode = -1;
			long startTime = System.currentTimeMillis();
			long endTime = startTime + timeout;
			boolean captureStarted = false;
			boolean captureTimeOut = false;
			boolean captureLiveStreamEnded = false;
			long delay = deviceHelper.getDelayForMethod(SBIConstant.MOSIP_METHOD_CAPTURE);

			while (true) {
				if (!captureStarted) {
					deviceHelper.setProfileId(mockService.getProfileId());

					deviceHelper.getCaptureInfo().setBioCount(bioCount);
					deviceHelper.getCaptureInfo().setBioSubType(bioSubType);
					deviceHelper.getCaptureInfo().setRequestScore(requestScore);
					deviceHelper.getCaptureInfo().setCaptureStarted(true);
					captureStarted = true;
				}
				delay(delay);
				try {
					if (System.currentTimeMillis() > endTime) {
						captureTimeOut = true;
						break;
					}
					// acquiring the lock
					if (semaphore != null)
						semaphore.acquire();

					if (deviceHelper.getCaptureInfo() == null) {
						captureLiveStreamEnded = true;
						break;
					}

					returnCode = deviceHelper.getBioCapture(true);

					if (deviceHelper.getCaptureInfo() != null && deviceHelper.getCaptureInfo().isCaptureCompleted()) {
						break;
					}
				} catch (Exception ex) {
					logger.error("processCaptureInfo :: error ::", ex);
				} finally {
					try {
						if (semaphore != null)
							semaphore.release();
					} catch (Exception ex) {
						logger.error("processCaptureInfo :: semaphore error ::", ex);
					}
				}

				Thread.sleep(30);
			}

			if (captureLiveStreamEnded) {
				response = SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "800", "", false);
			} else if (captureTimeOut) {
				response = SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "801", "", false);
				if (deviceHelper.getCaptureInfo() == null)
					deviceHelper.getCaptureInfo().setCaptureCompleted(true);
			} else {
				List<BioMetricsDto> biometrics = null;
				try {
					biometrics = getBioMetricsDtoList(lang, requestObject, deviceHelper, deviceSubId, true);
					if (biometrics != null && !biometrics.isEmpty()) {
						RCaptureResponse captureResponse = new RCaptureResponse();
						captureResponse.setBiometrics(biometrics);

						ObjectMapper mapper = new ObjectMapper();
						SerializationConfig config = mapper.getSerializationConfig();
						config.setSerializationInclusion(Inclusion.NON_NULL);
						mapper.setSerializationConfig(config);

						response = mapper.writeValueAsString(captureResponse);
					} else {
						response = SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "708", "", false);
					}
				} catch (Exception ex) {
					response = SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "999", ex.getLocalizedMessage(),
							false);
				}

				deviceHelper.deInitDevice();
				deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
			}

			if (deviceHelper.getCaptureInfo() != null) {
				deviceHelper.getCaptureInfo().getBioExceptionInfo().deInitBioException();
				// When Capture is called After LiveStreaming is called
				// DeInit is called in Livestream method
				if (deviceHelper.getCaptureInfo().isLiveStreamStarted()) {
					deviceHelper.getCaptureInfo().setCaptureStarted(false);
					deviceHelper.getCaptureInfo().setCaptureCompleted(true);
				}
				// DeInit When Capture is called Directly
				else {
					deviceHelper.deInitDevice();
					deviceHelper.setDeviceStatus(SBIConstant.DEVICE_STATUS_ISREADY);
				}
			}
		} catch (Exception ex) {
			response = SBIJsonInfo.getCaptureErrorJson(specVersion, lang, "810", "", false);
			logger.error("processCaptureInfo", ex);
		} finally {
			try {
				if (semaphore != null)
					semaphore.release();
			} catch (Exception ex) {
				logger.error("processCaptureInfo :: semaphore error ::", ex);
			}
		}
		return response;
	}

	@SuppressWarnings({ "java:S3776", "java:S6541", "unused" })
	private List<BioMetricsDto> getBioMetricsDtoList(String lang, CaptureRequestDto requestObject,
			SBIDeviceHelper deviceHelper, int deviceSubId, boolean isForAuthenication)
			throws SBIException, NoSuchAlgorithmException, IOException, DecoderException {
		List<BioMetricsDto> biometrics = new ArrayList<>();
		String specVersion = requestObject.getSpecVersion();
		String transactionId = requestObject.getTransactionId();
		float captureScore = deviceHelper.getQualityScore(); // SET MANUALLY
		int requestScore = requestObject.getBio().get(0).getRequestedScore();
		int bioCount = Integer.parseInt(requestObject.getBio().get(0).getCount());
		String bioType = requestObject.getBio().get(0).getType();
		String[] bioExceptions = requestObject.getBio().get(0).getException();// Bio exceptions
		String[] bioSubType = requestObject.getBio().get(0).getBioSubType();// Bio SubTypes

		String previousHash = requestObject.getBio().get(0).getPreviousHash();
		if (!isForAuthenication) {
			// For Finger Slap
			if (deviceHelper.getDigitalId().getType().equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) && deviceHelper
					.getDigitalId().getDeviceSubType().equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP)) {
				SBIFingerSlapCaptureInfo captureInfo = (SBIFingerSlapCaptureInfo) deviceHelper.getCaptureInfo();
				SBIFingerSlapBioExceptionInfo bioExceptionInfo = (SBIFingerSlapBioExceptionInfo) deviceHelper
						.getCaptureInfo().getBioExceptionInfo();
				switch (deviceSubId) {
				case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT:
					if (bioExceptionInfo.getChkMissingLeftIndex() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueLI() != null && captureInfo.getBioValueLI().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_LEFT_INDEX, captureInfo.getBioValueLI(),
									captureInfo.getCaptureScoreLI(), requestScore, "", "0", isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					if (bioExceptionInfo.getChkMissingLeftMiddle() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueLM() != null && captureInfo.getBioValueLM().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_LEFT_MIDDLE,
									captureInfo.getBioValueLM(), captureInfo.getCaptureScoreLM(), requestScore, "", "0",
									isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					if (bioExceptionInfo.getChkMissingLeftRing() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueLR() != null && captureInfo.getBioValueLR().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_LEFT_RING, captureInfo.getBioValueLR(),
									captureInfo.getCaptureScoreLR(), requestScore, "", "0", isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					if (bioExceptionInfo.getChkMissingLeftLittle() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueLL() != null && captureInfo.getBioValueLL().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_LEFT_LITTLE,
									captureInfo.getBioValueLL(), captureInfo.getCaptureScoreLL(), requestScore, "", "0",
									isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					break;
				case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_RIGHT:
					if (bioExceptionInfo.getChkMissingRightIndex() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueRI() != null && captureInfo.getBioValueRI().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_INDEX,
									captureInfo.getBioValueRI(), captureInfo.getCaptureScoreRI(), requestScore, "", "0",
									isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					if (bioExceptionInfo.getChkMissingRightMiddle() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueRM() != null && captureInfo.getBioValueRM().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_MIDDLE,
									captureInfo.getBioValueRM(), captureInfo.getCaptureScoreRM(), requestScore, "", "0",
									isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					if (bioExceptionInfo.getChkMissingRightRing() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueRR() != null && captureInfo.getBioValueRR().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_RING, captureInfo.getBioValueRR(),
									captureInfo.getCaptureScoreRR(), requestScore, "", "0", isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					if (bioExceptionInfo.getChkMissingRightLittle() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueRL() != null && captureInfo.getBioValueRL().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_LITTLE,
									captureInfo.getBioValueRL(), captureInfo.getCaptureScoreRL(), requestScore, "", "0",
									isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					break;
				case SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_THUMB:
					if (bioExceptionInfo.getChkMissingLeftThumb() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueLT() != null && captureInfo.getBioValueLT().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_LEFT_THUMB, captureInfo.getBioValueLT(),
									captureInfo.getCaptureScoreLT(), requestScore, "", "0", isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					if (bioExceptionInfo.getChkMissingRightThumb() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueRT() != null && captureInfo.getBioValueRT().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_THUMB,
									captureInfo.getBioValueRT(), captureInfo.getCaptureScoreRT(), requestScore, "", "0",
									isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					break;
				default:
					break;
				}
			}
			// For IRIS DOUBLE
			else if (deviceHelper.getDigitalId().getType().equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) && deviceHelper
					.getDigitalId().getDeviceSubType().equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE)) {
				SBIIrisDoubleCaptureInfo captureInfo = (SBIIrisDoubleCaptureInfo) deviceHelper.getCaptureInfo();
				SBIIrisDoubleBioExceptionInfo bioExceptionInfo = (SBIIrisDoubleBioExceptionInfo) deviceHelper
						.getCaptureInfo().getBioExceptionInfo();

				switch (deviceSubId) {
				case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_LEFT:
					if (bioExceptionInfo.getChkMissingLeftIris() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueLI() != null && captureInfo.getBioValueLI().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_LEFT_IRIS, captureInfo.getBioValueLI(),
									captureInfo.getCaptureScoreLI(), requestScore, "", "0", isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					break;
				case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_RIGHT:
					if (bioExceptionInfo.getChkMissingRightIris() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueRI() != null && captureInfo.getBioValueRI().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_IRIS, captureInfo.getBioValueRI(),
									captureInfo.getCaptureScoreRI(), requestScore, "", "0", isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					break;
				case SBIConstant.DEVICE_IRIS_DOUBLE_SUB_TYPE_ID_BOTH:

					if (bioExceptionInfo.getChkMissingLeftIris() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueLI() != null && captureInfo.getBioValueLI().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_LEFT_IRIS, captureInfo.getBioValueLI(),
									captureInfo.getCaptureScoreLI(), requestScore, "", "0", isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					if (bioExceptionInfo.getChkMissingRightIris() == SBICheckState.UNCHECKED) {
						if (captureInfo.getBioValueRI() != null && captureInfo.getBioValueRI().length() > 0) {
							BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
									previousHash, bioType, SBIConstant.BIO_NAME_RIGHT_IRIS, captureInfo.getBioValueRI(),
									captureInfo.getCaptureScoreRI(), requestScore, "", "0", isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						} else {
							BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
							if (bioDto != null) {
								biometrics.add(bioDto);
								previousHash = bioDto.getHash();
							}
						}
					}
					break;
				default:
					break;
				}
			}
			// For Face
			else if (deviceHelper.getDigitalId().getType().equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) && deviceHelper
					.getDigitalId().getDeviceSubType().equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)) {
				SBIFaceCaptureInfo captureInfo = (SBIFaceCaptureInfo) deviceHelper.getCaptureInfo();

				boolean isExceptionPhoto = false;
				if (bioExceptions != null && bioExceptions.length > 0)
					isExceptionPhoto = true;

				if (!isExceptionPhoto) {
					if (captureInfo.getBioValueFace() != null && captureInfo.getBioValueFace().length() > 0) {
						BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
								previousHash, bioType, null, captureInfo.getBioValueFace(),
								captureInfo.getCaptureScoreFace(), requestScore, "", "0", isForAuthenication);
						if (bioDto != null) {
							biometrics.add(bioDto);
							previousHash = bioDto.getHash();
						}
					} else {
						BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
						if (bioDto != null) {
							biometrics.add(bioDto);
							previousHash = bioDto.getHash();
						}
					}
				} else {
					if (captureInfo.getBioValueExceptionPhoto() != null
							&& captureInfo.getBioValueExceptionPhoto().length() > 0) {
						BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
								previousHash, bioType, null, captureInfo.getBioValueExceptionPhoto(),
								captureInfo.getCaptureScoreFace(), requestScore, "", "0", isForAuthenication);
						if (bioDto != null) {
							biometrics.add(bioDto);
							previousHash = bioDto.getHash();
						}
					} else {
						BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
						if (bioDto != null) {
							biometrics.add(bioDto);
							previousHash = bioDto.getHash();
						}
					}
				}
			}
		} else if (isForAuthenication) {
			SBIBioSubTypeInfo bioSubTypeInfo = new SBIBioSubTypeInfo();
			bioSubTypeInfo.initBioSubType(bioSubType);

			// For Finger Single
			if (deviceHelper.getDigitalId().getType().equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) && deviceHelper
					.getDigitalId().getDeviceSubType().equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE)) {
				SBIFingerSingleCaptureInfo captureInfo = (SBIFingerSingleCaptureInfo) deviceHelper.getCaptureInfo();
				if (deviceSubId == SBIConstant.DEVICE_FINGER_SINGLE_SUB_TYPE_ID) {
					HashMap<String, String> biometricData = (HashMap<String, String>) captureInfo.getBiometricData();
					if (biometricData != null && biometricData.size() > 0) {
						int bioCounter = 0;
						for (Map.Entry<String, String> pair : biometricData.entrySet()) {
							if (bioCounter > bioCount)
								break;

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkLeftIndex() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_LEFT_INDEX)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkLeftIndex() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_LEFT_INDEX
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreLI(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkLeftMiddle() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_LEFT_MIDDLE)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkLeftIndex() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_LEFT_MIDDLE
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreLM(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkLeftRing() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_LEFT_RING)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkLeftRing() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_LEFT_RING
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreLR(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkLeftLittle() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_LEFT_LITTLE)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkLeftLittle() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_LEFT_LITTLE
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreLL(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
										bioCounter++;
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkRightIndex() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_RIGHT_INDEX)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkRightIndex() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_RIGHT_INDEX
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreRI(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkRightMiddle() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_RIGHT_MIDDLE)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkRightIndex() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_RIGHT_MIDDLE
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreRM(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkRightRing() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_RIGHT_RING)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkRightRing() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_RIGHT_RING
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreRR(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkRightLittle() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_RIGHT_LITTLE)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkRightLittle() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_RIGHT_LITTLE
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreRL(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkLeftThumb() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_LEFT_THUMB)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkLeftThumb() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_LEFT_THUMB
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreLT(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkRightThumb() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_RIGHT_THUMB)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkRightThumb() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_RIGHT_THUMB
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreRT(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}
						}
					} else {
						BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
						if (bioDto != null) {
							biometrics.add(bioDto);
							previousHash = bioDto.getHash();
						}
					}
				}
			}
			// For IRIS SINGLE
			else if (deviceHelper.getDigitalId().getType().equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS) && deviceHelper
					.getDigitalId().getDeviceSubType().equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE)) {
				SBIIrisSingleCaptureInfo captureInfo = (SBIIrisSingleCaptureInfo) deviceHelper.getCaptureInfo();
				SBIIrisSingleBioExceptionInfo bioExceptionInfo = (SBIIrisSingleBioExceptionInfo) deviceHelper
						.getCaptureInfo().getBioExceptionInfo();

				if (deviceSubId == SBIConstant.DEVICE_IRIS_SINGLE_SUB_TYPE_ID) {
					HashMap<String, String> biometricData = (HashMap<String, String>) captureInfo.getBiometricData();
					if (biometricData != null && biometricData.size() > 0) {
						int bioCounter = 0;
						for (Map.Entry<String, String> pair : biometricData.entrySet()) {
							if (bioCounter > bioCount)
								break;

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkLeftIris() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_LEFT_IRIS)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkLeftIris() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_LEFT_IRIS
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreLI(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}

							if ((bioCounter < bioCount)
									&& (bioSubTypeInfo.getChkUnknown() == SBICheckState.CHECKED
											|| bioSubTypeInfo.getChkRightIris() == SBICheckState.CHECKED)
									&& pair.getKey().equals(SBIConstant.BIO_NAME_RIGHT_IRIS)) {
								String bioData = pair.getValue();
								if (bioData != null && bioData.length() > 0) {
									BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper,
											previousHash, bioType,
											(bioSubTypeInfo.getChkRightIris() == SBICheckState.CHECKED
													? SBIConstant.BIO_NAME_RIGHT_IRIS
													: SBIConstant.BIO_NAME_UNKNOWN),
											bioData, captureInfo.getCaptureScoreRI(), requestScore, "", "0",
											isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								} else {
									BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
									if (bioDto != null) {
										biometrics.add(bioDto);
										previousHash = bioDto.getHash();
									}
								}
								bioCounter++;
							}
						}
					} else {
						BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
						if (bioDto != null) {
							biometrics.add(bioDto);
							previousHash = bioDto.getHash();
						}
					}
				}
			}
			// For Face
			else if (deviceHelper.getDigitalId().getType().equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) && deviceHelper
					.getDigitalId().getDeviceSubType().equals(SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE)) {
				SBIFaceCaptureInfo captureInfo = (SBIFaceCaptureInfo) deviceHelper.getCaptureInfo();
				String bioData = captureInfo.getBiometricForBioSubType(SBIConstant.BIO_NAME_UNKNOWN);
				if (bioData != null && bioData.length() > 0) {
					BioMetricsDto bioDto = getBiometricData(transactionId, requestObject, deviceHelper, previousHash,
							bioType, null, bioData, captureInfo.getCaptureScoreFace(), requestScore, "", "0",
							isForAuthenication);
					if (bioDto != null) {
						biometrics.add(bioDto);
						previousHash = bioDto.getHash();
					}
				} else {
					BioMetricsDto bioDto = getBiometricErrorData(lang, specVersion, isForAuthenication);
					if (bioDto != null) {
						biometrics.add(bioDto);
						previousHash = bioDto.getHash();
					}
				}
			}
		}
		return biometrics;
	}

	@SuppressWarnings({ "java:S107", "java:S2139", "deprecation" })
	private BioMetricsDto getBiometricData(String transactionId, CaptureRequestDto requestObject,
			SBIDeviceHelper deviceHelper, String previousHash, String bioType, String bioSubType, String bioValue,
			float qualityScore, float qualityRequestScore, String lang, String errorCode,
			boolean isUsedForAuthenication)
			throws SBIException, IOException, NoSuchAlgorithmException, DecoderException {
		DeviceInfo deviceInfo = deviceHelper.getDeviceInfo();

		BioMetricsDto biometric = new BioMetricsDto();
		biometric.setSpecVersion(requestObject.getSpecVersion());

		biometric.setError(new ErrorInfo(errorCode, SBIJsonInfo.getErrorDescription(lang, errorCode)));

		BioMetricsDataDto biometricData = new BioMetricsDataDto();
		biometricData.setDeviceCode(deviceInfo.getDeviceCode());
		biometricData.setDigitalId(deviceInfo.getDigitalId());
		biometricData.setDeviceServiceVersion(deviceInfo.getServiceVersion());
		biometricData.setBioType(bioType);
		biometricData.setBioSubType(bioSubType);

		biometricData.setPurpose(requestObject.getPurpose());
		biometricData.setEnv(requestObject.getEnv());

		if (isUsedForAuthenication)
			biometricData.setDomainUri(requestObject.getDomainUri() + "");

		if (!isUsedForAuthenication) {
			biometricData.setBioValue(bioValue);
			biometricData.setTimestamp(CryptoUtility.getTimestamp());
		} else {
			try {
				X509Certificate certificate = new JwtUtility().getCertificateToEncryptCaptureBioValue();
				PublicKey publicKey = certificate.getPublicKey();
				Map<String, String> cryptoResult = CryptoUtility.encrypt(publicKey,
						io.mosip.mock.sbi.util.StringHelper.base64UrlDecode(bioValue), transactionId);

				biometricData.setTimestamp(cryptoResult.get("TIMESTAMP"));
				biometricData.setBioValue(cryptoResult.containsKey("ENC_DATA") ? cryptoResult.get("ENC_DATA") : null);
				biometric.setSessionKey(cryptoResult.get("ENC_SESSION_KEY"));
				String thumbPrint = toHex(JwtUtility.getCertificateThumbprint(certificate)).replace("-", "")
						.toUpperCase();
				biometric.setThumbprint(thumbPrint);
			} catch (Exception ex) {
				logger.error("getBiometricData :: encrypt :: ", ex);
				throw new SBIException("IDA Biometric encryption Certificate not found",
						"IDA Biometric encryption Certificate not found", ex);
			}
		}

		biometricData.setRequestedScore(qualityRequestScore + "");
		biometricData.setQualityScore(qualityScore + "");
		biometricData.setTransactionId(transactionId);

		ObjectMapper mapper = new ObjectMapper();
		SerializationConfig config = mapper.getSerializationConfig();
		config.setSerializationInclusion(Inclusion.NON_NULL);
		mapper.setSerializationConfig(config);

		String currentBioData = mapper.writeValueAsString(biometricData);

		// base64 signature of the data block. base64 signature of the hash element
		String dataBlockSignBase64 = deviceHelper.getSignBioMetricsDataDto(deviceHelper.getDeviceType(),
				deviceHelper.getDeviceSubType(), currentBioData);
		biometric.setData(dataBlockSignBase64);

		byte[] previousBioDataHash = null;
		if (previousHash == null || previousHash.trim().length() == 0) {
			byte[] previousDataByteArr = StringHelper.toUtf8ByteArray("");
			previousBioDataHash = generateHash(previousDataByteArr);
		} else {
			previousBioDataHash = decodeHex(previousHash);
		}
		// instead of BioData, bioValue (before encrytion in case of Capture response)
		// is used for computing the hash.
		byte[] currentDataByteArr = io.mosip.mock.sbi.util.StringHelper.base64UrlDecode(bioValue);
		// Here Byte Array
		byte[] currentBioDataHash = generateHash(currentDataByteArr);
		byte[] finalBioDataHash = new byte[currentBioDataHash.length + previousBioDataHash.length];
		System.arraycopy(previousBioDataHash, 0, finalBioDataHash, 0, previousBioDataHash.length);
		System.arraycopy(currentBioDataHash, 0, finalBioDataHash, previousBioDataHash.length,
				currentBioDataHash.length);

		biometric.setHash(toHex(generateHash(finalBioDataHash)));

		return biometric;
	}

	public String toHex(byte[] bytes) {
		return Hex.encodeHexString(bytes).toUpperCase();
	}

	private static final String HASH_ALGORITHM_NAME = "SHA-256";

	public byte[] generateHash(final byte[] bytes) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM_NAME);
		return messageDigest.digest(bytes);
	}

	public byte[] decodeHex(String hexData) throws DecoderException {
		return Hex.decodeHex(hexData);
	}

	private BioMetricsDto getBiometricErrorData(String lang, String specVersion, boolean isForAuthenication) {
		String errorCode = "701";
		BioMetricsDto biometric = new BioMetricsDto();
		biometric.setSpecVersion(specVersion);
		biometric.setData("");
		biometric.setHash("");
		if (isForAuthenication) {
			errorCode = "801";
			biometric.setSessionKey("");
			biometric.setThumbprint("");
		}

		biometric.setError(new ErrorInfo(errorCode, (SBIJsonInfo.getErrorDescription(lang, errorCode)).trim()));

		return biometric;
	}

	private void renderMainHeaderData(Socket socket) throws IOException {
		writeMainHeader(socket);
	}

	private void writeMainHeader(Socket socket) throws IOException {
		// prepare main header
		byte[] mainHeader = createMainHeader();

		BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
		outputStream.write(mainHeader, 0, mainHeader.length);
		outputStream.flush();
		outputStream.flush();
	}

	private byte[] createMainHeader() {
		String header = "HTTP/1.0 200 OK\r\n" + "Server: http://"
				+ ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + getPort() + "\r\n"
				+ "Access-Control-Allow-Origin:*\r\n" + "Connection: close\r\n" + "Max-Age: 0\r\n" + "Expires: 0\r\n"
				+ "Cache-Control: no-cache, private\r\n" + "Pragma: no-cache\r\n"
				+ "Content-Type: multipart/x-mixed-replace; " + "boundary=--BoundaryString\r\n\r\n";

		// using ascii encoder is fine since there is no international character used in
		// this string.
		return header.getBytes(StandardCharsets.US_ASCII);
	}

	private void renderJPGImageData(Socket socket, byte[] image) throws IOException {
		if (image != null && socket != null && !socket.isClosed())
			writeFrame(socket, image);
	}

	private void writeFrame(Socket socket, byte[] image) throws IOException {
		// prepare image data
		byte[] imageInByte = image;

		// prepare header
		byte[] header = createHeader(imageInByte.length);
		// prepare footer
		byte[] footer = createFooter();

		BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
		// Start writing data
		outputStream.write(header, 0, header.length);
		outputStream.write(imageInByte, 0, imageInByte.length);
		outputStream.write(footer, 0, footer.length);
		outputStream.flush();
		outputStream.flush();
	}

	private byte[] createHeader(int length) {
		String header = "--BoundaryString\r\n" + "Access-Control-Allow-Origin:*\r\n" + "Content-Type:image/jpeg\r\n"
				+ "Content-Length:" + length + "\r\n\r\n"; // there are always 2 new line character before the actual
															// data

		// using ascii encoder is fine since there is no international character used in
		// this string.
		return header.getBytes(StandardCharsets.US_ASCII);
	}

	public byte[] createFooter() {
		return "\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
	}

	@SuppressWarnings({ "java:S3776" })
	private SBIDeviceHelper getDeviceHelperForDeviceId(SBIMockService mockService, String deviceId) {
		SBIDeviceHelper deviceHelper = null;

		deviceHelper = mockService.getDeviceHelper(
				SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FACE);
		deviceHelper.initDeviceDetails();
		if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceId().equals(deviceId)) {
			return deviceHelper;
		}

		if (mockService.getPurpose().equals(SBIConstant.PURPOSE_REGISTRATION)) {
			deviceHelper = mockService.getDeviceHelper(
					SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP);
			deviceHelper.initDeviceDetails();
			if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceId().equals(deviceId)) {
				return deviceHelper;
			}

			deviceHelper = mockService.getDeviceHelper(
					SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_DOUBLE);
			deviceHelper.initDeviceDetails();
			if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceId().equals(deviceId)) {
				return deviceHelper;
			}
		} else if (mockService.getPurpose().equals(SBIConstant.PURPOSE_AUTH)) {
			deviceHelper = mockService.getDeviceHelper(
					SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE);
			deviceHelper.initDeviceDetails();
			if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceId().equals(deviceId)) {
				return deviceHelper;
			}

			deviceHelper = mockService.getDeviceHelper(
					SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS + "_" + SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_IRIS_SINGLE);
			deviceHelper.initDeviceDetails();
			if (deviceHelper.getDeviceInfo() != null && deviceHelper.getDeviceInfo().getDeviceId().equals(deviceId)) {
				return deviceHelper;
			}
		}

		return null;
	}

	@SuppressWarnings({ "java:S3776" })
	public Object getRequestJson(String methodVerb) {
		if (getRequest() != null && getRequest().indexOf("{") >= 0) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_DISC_VERB))
					return mapper.readValue(getRequest().substring(getRequest().indexOf("{")),
							DeviceDiscoveryRequestDetail.class);
				if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_STREAM_VERB))
					return mapper.readValue(getRequest().substring(getRequest().indexOf("{")),
							StreamingRequestDetail.class);
				if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_RCAPTURE_VERB))
					return mapper.readValue(getRequest().substring(getRequest().indexOf("{")), CaptureRequestDto.class);

				if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_ADMIN_API_STATUS))
					return mapper.readValue(getRequest().substring(getRequest().indexOf("{")), StatusRequest.class);
				if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_ADMIN_API_SCORE))
					return mapper.readValue(getRequest().substring(getRequest().indexOf("{")), ScoreRequest.class);
				if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_ADMIN_API_DELAY))
					return mapper.readValue(getRequest().substring(getRequest().indexOf("{")), DelayRequest.class);
				if (methodVerb.equalsIgnoreCase(SBIConstant.MOSIP_ADMIN_API_PROFILE))
					return mapper.readValue(getRequest().substring(getRequest().indexOf("{")), ProfileRequest.class);

				return null;
			} catch (Exception ex) {
				logger.error("getRequestJson", ex);
				return null;
			}
		} else {
			return null;
		}
	}

	@SuppressWarnings({ "java:S112" })
	public PublicKey getPublicKeyToEncryptCaptureBioValue() throws Exception {
		String certificate = getPublicKeyFromIDA();
		certificate = trimBeginEnd(certificate);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate x509Certificate = (X509Certificate) cf
				.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(certificate)));

		return x509Certificate.getPublicKey();
	}

	@SuppressWarnings({ "java:S112" })
	public String getThumbprint() throws Exception {
		String certificate = getPublicKeyFromIDA();
		certificate = trimBeginEnd(certificate);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate x509Certificate = (X509Certificate) cf
				.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(certificate)));

		// thumbprint =
		return CryptoUtil.computeFingerPrint(x509Certificate.getEncoded(), null);
	}

	public String getPublicKeyFromIDA() {
		OkHttpClient client = new OkHttpClient();
		String requestBody = String.format(AUTH_REQ_TEMPLATE,
				ApplicationPropertyHelper.getPropertyKeyValue("mosip.auth.appid"),
				ApplicationPropertyHelper.getPropertyKeyValue("mosip.auth.clientid"),
				ApplicationPropertyHelper.getPropertyKeyValue("mosip.auth.secretkey"),
				DateUtils.getUTCCurrentDateTime());

		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, requestBody);
		Request requestInfo = new Request.Builder()
				.url(ApplicationPropertyHelper.getPropertyKeyValue("mosip.auth.server.url")).post(body).build();
		try {
			Response response = client.newCall(requestInfo).execute();
			if (response.isSuccessful()) {
				String authToken = response.header("authorization");

				Request idarequest = new Request.Builder().header("cookie", "Authorization=" + authToken)
						.url(ApplicationPropertyHelper.getPropertyKeyValue("mosip.ida.server.url")).get().build();

				Response idaResponse = new OkHttpClient().newCall(idarequest).execute();
				if (idaResponse.isSuccessful()) {
					JSONObject jsonObject = new JSONObject(idaResponse.body().string());
					jsonObject = jsonObject.getJSONObject("response");
					return jsonObject.getString("certificate");
				}
			}

		} catch (IOException | JSONException e) {
			logger.error("getPublicKeyFromIDA", e);
		}
		return null;
	}

	private String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public static Semaphore getSemaphore() {
		return semaphore;
	}

	public static void setSemaphore(Semaphore semaphore) {
		SBIServiceResponse.semaphore = semaphore;
	}

	private void delay(long millseconds) {
		try {
			Thread.sleep(millseconds);
		} catch (InterruptedException e) {
			logger.error("delay", e);
			Thread.currentThread().interrupt();
		}
	}

	private boolean isValidBioExceptionValues(String bioType, String[] bioExceptions) {
		if (bioExceptions != null) {
			if (bioType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER))
				return bioExceptionsListFinger.containsAll(Arrays.asList(bioExceptions));
			else if (bioType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS))
				return bioExceptionsListIris.containsAll(Arrays.asList(bioExceptions));
			else if (bioType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))
				return true;
		}
		return true;
	}

	@SuppressWarnings({ "java:S1126" })
	private boolean isValidBioSubtypeValues(String bioType, String[] bioSubtypes, boolean isCapture) {
		if (bioSubtypes != null) {
			if (bioType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER))
				return bioSubtypesListFinger.containsAll(Arrays.asList(bioSubtypes));
			else if (bioType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS))
				return bioSubtypesListIris.containsAll(Arrays.asList(bioSubtypes));
			else if (bioType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))
				return true;
		}
		if (isCapture) {
			if (bioType.equals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE))
				return true;
			else
				return false;
		} else
			return true;
	}
}
