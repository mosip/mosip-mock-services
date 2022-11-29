package io.mosip.mock.sdk.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64.Encoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.finger.FingerBDIR;
import io.mosip.biometrics.util.finger.FingerDecoder;
import io.mosip.biometrics.util.finger.FingerPosition;
import io.mosip.biometrics.util.iris.EyeLabel;
import io.mosip.biometrics.util.iris.IrisBDIR;
import io.mosip.biometrics.util.iris.IrisDecoder;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.mock.sdk.constant.ResponseStatus;
import io.mosip.mock.sdk.exceptions.SDKException;

public abstract class SDKService {
	Logger LOGGER = LoggerFactory.getLogger(SDKService.class);

	protected Map<BiometricType, List<BIR>> getBioSegmentMap(BiometricRecord record,
			List<BiometricType> modalitiesToMatch) {
		Boolean noFilter = false;
		// if the modalities to match is not passed, assume that all modalities have to
		// be matched.
		if (modalitiesToMatch == null || modalitiesToMatch.isEmpty())
			noFilter = true;

		Map<BiometricType, List<BIR>> bioSegmentMap = new HashMap<>();
		for (BIR segment : record.getSegments()) {
			BiometricType bioType = segment.getBdbInfo().getType().get(0);

			// ignore modalities that are not to be matched
			if (noFilter == false && !modalitiesToMatch.contains(bioType))
				continue;

			if (!bioSegmentMap.containsKey(bioType)) {
				bioSegmentMap.put(bioType, new ArrayList<BIR>());
			}
			bioSegmentMap.get(bioType).add(segment);
		}

		return bioSegmentMap;
	}
	
	protected boolean isValidBIRParams(BIR segment, BiometricType bioType, String bioSubType) {
		ResponseStatus responseStatus = null;
		switch (bioType)
		{
			case FACE:
				break;
			case FINGER:
				if (!(bioSubType.equals("Unknown") || 
					bioSubType.equals("Left IndexFinger") || bioSubType.equals("Left RingFinger") || bioSubType.equals("Left MiddleFinger") || 
					bioSubType.equals("Left LittleFinger") || bioSubType.equals("Left Thumb") || 
					bioSubType.equals("Right IndexFinger") || bioSubType.equals("Right RingFinger") || bioSubType.equals("Right MiddleFinger") || 
					bioSubType.equals("Right LittleFinger") || bioSubType.equals("Right Thumb")))
				{
					LOGGER.error("isValidBIRParams>>BiometricType#" + bioType + ">>BioSubType#" + bioSubType);
					responseStatus = ResponseStatus.MISSING_INPUT;
					throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
				}
				break;
			case IRIS:
				if (!(bioSubType.equals("Unknown") || bioSubType.equals("Left") || bioSubType.equals("Right")))
				{
					LOGGER.error("isValidBIRParams>>BiometricType#" + bioType + ">>BioSubType#" + bioSubType);
					responseStatus = ResponseStatus.MISSING_INPUT;
					throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
				}
				break;
			default:
				LOGGER.error("isValidBIRParams>>BiometricType#" + bioType + ">>BioSubType#" + bioSubType);
				responseStatus = ResponseStatus.MISSING_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
		}
		return true;
	}
	
	protected boolean isValidBDBData(PurposeType purposeType, BiometricType bioType, String bioSubType, byte[] bdbData) {
		ResponseStatus responseStatus = null;
		if (bdbData != null && bdbData.length != 0)
		{
			return isValidBiometericData(purposeType, bioType, bioSubType, encodeToURLSafeBase64 (bdbData));
		}
		LOGGER.error("isValidBDBData>>bdbData==null#");
		responseStatus = ResponseStatus.BIOMETRIC_NOT_FOUND_IN_CBEFF;
		throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());		
	}

	protected boolean isValidBiometericData(PurposeType purposeType, BiometricType bioType, String bioSubType, String bdbData) {
		ResponseStatus responseStatus = null;
		switch (bioType)
		{
			case FACE:
				return isValidFaceBdb(purposeType, bioSubType, bdbData);
			case FINGER:
				return isValidFingerBdb(purposeType, bioSubType, bdbData);
			case IRIS:
				return isValidIrisBdb(purposeType, bioSubType, bdbData);
		}
		LOGGER.error("isValidBiometericData>>BiometricType==#" + bioType + ">>biometricSubType#" + bioSubType);
		responseStatus = ResponseStatus.INVALID_INPUT;
		throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());
	}

	protected boolean isValidFingerBdb(PurposeType purposeType, String biometricSubType, String bdbData)
	{
		ResponseStatus responseStatus = null;
		try {
			ConvertRequestDto requestDto = new ConvertRequestDto();
			requestDto.setModality("Finger");
			requestDto.setVersion("ISO19794_4_2011");
			requestDto.setInputBytes(decodeURLSafeBase64 (bdbData));

			FingerBDIR bdir;
			bdir = FingerDecoder.getFingerBDIR(requestDto);
			
			FingerPosition fingerPosition = bdir.getRepresentation().getRepresentationHeader().getFingerPosition();				
			//Check the ISO FingerPosition with BDB biometricSubType
			if (!isValidFingerPosition(fingerPosition, biometricSubType))
			{
				LOGGER.info("fingerPosition InValid>>" + fingerPosition + "   <<biometricSubType>>" + biometricSubType);

				responseStatus = ResponseStatus.INVALID_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());					
			}					
			//can check imagettype for auth and reg
		}
		catch (Exception ex)
		{
			responseStatus = ResponseStatus.INVALID_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage() + " "+ ex.getLocalizedMessage());
		}
		return true;
	}
	
	protected boolean isValidFingerPosition(FingerPosition fingerPosition, String biometricSubType) {
		boolean isValid = false;
		switch (biometricSubType)
		{
			case "Unknown":
				isValid = true;
				break;
			case "Left IndexFinger":
				if (fingerPosition == FingerPosition.LEFT_INDEX_FINGER)
					isValid = true;
				break;
			case "Left MiddleFinger":
				if (fingerPosition == FingerPosition.LEFT_MIDDLE_FINGER)
					isValid = true;
				break;
			case "Left RingFinger":
				if (fingerPosition == FingerPosition.LEFT_RING_FINGER)
					isValid = true;
				break;
			case "Left LittleFinger":
				if (fingerPosition == FingerPosition.LEFT_LITTLE_FINGER)
					isValid = true;
				break;
			case "Left Thumb":
				if (fingerPosition == FingerPosition.LEFT_THUMB)
					isValid = true;
				break;
			case "Right IndexFinger":
				if (fingerPosition == FingerPosition.RIGHT_INDEX_FINGER)
					isValid = true;
				break;
			case "Right MiddleFinger":
				if (fingerPosition == FingerPosition.RIGHT_MIDDLE_FINGER)
					isValid = true;
				break;
			case "Right RingFinger":
				if (fingerPosition == FingerPosition.RIGHT_RING_FINGER)
					isValid = true;
				break;
			case "Right LittleFinger":
				if (fingerPosition == FingerPosition.RIGHT_LITTLE_FINGER)
					isValid = true;
				break;
			case "Right Thumb":
				if (fingerPosition == FingerPosition.RIGHT_THUMB)
					isValid = true;
				break;
			default:
				isValid = false;
				break;
		}
		return isValid;
	}

	protected boolean isValidIrisBdb(PurposeType purposeType, String biometricSubType, String bdbData)
	{
		ResponseStatus responseStatus = null;
		try {
			ConvertRequestDto requestDto = new ConvertRequestDto();
			requestDto.setModality("Iris");
			requestDto.setVersion("ISO19794_6_2011");
			requestDto.setInputBytes(decodeURLSafeBase64 (bdbData));

			IrisBDIR bdir = IrisDecoder.getIrisBDIR(requestDto);

			EyeLabel eyeLabel = bdir.getRepresentation().getRepresentationHeader().getImageInformation().getEyeLabel();
			if (!isValidEyeLabel(eyeLabel, biometricSubType))
			{
				LOGGER.info("eyeLabel InValid>>" + eyeLabel + "<<biometricSubType>>" + biometricSubType);

				responseStatus = ResponseStatus.INVALID_INPUT;
				throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage());					
			}					
			//can check imagettype for auth and reg
		}
		catch (Exception ex)
		{
			responseStatus = ResponseStatus.INVALID_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage() + " "+ ex.getLocalizedMessage());
		}
		return true;
	}
	
	protected boolean isValidEyeLabel(EyeLabel eyeLabel, String biometricSubType) {
		boolean isValid = false;		
		switch (biometricSubType)
		{
			case "Unknown":
				isValid = true;
				break;
			case "Left":
				if (eyeLabel == EyeLabel.LEFT)
					isValid = true;
				break;
			case "Right":
				if (eyeLabel == EyeLabel.RIGHT)
					isValid = true;
				break;
			default:
				isValid = false;
			break;
		}				
		return isValid;
	}

	protected boolean isValidFaceBdb(PurposeType purposeType, String biometricSubType, String bdbData)
	{
		ResponseStatus responseStatus = null;
		try {
			ConvertRequestDto requestDto = new ConvertRequestDto();
			requestDto.setModality("Face");
			requestDto.setVersion("ISO19794_5_2011");
			requestDto.setInputBytes(decodeURLSafeBase64 (bdbData));
			FaceBDIR bdir = FaceDecoder.getFaceBDIR(requestDto);
			//can check imagettype for auth and reg
		}
		catch (Exception ex)
		{
			responseStatus = ResponseStatus.INVALID_INPUT;
			throw new SDKException(responseStatus.getStatusCode() + "", responseStatus.getStatusMessage() + " "+ ex.getLocalizedMessage());
		}
		return true;
	}

	private static Encoder urlSafeEncoder;
	static {
		urlSafeEncoder = Base64.getUrlEncoder().withoutPadding();
	}

	public static String encodeToURLSafeBase64(byte[] data) {
		if (isNullEmpty(data)) {
			return null;
		}
		return urlSafeEncoder.encodeToString(data);
	}

	public static String encodeToURLSafeBase64(String data) {
		if (isNullEmpty(data)) {
			return null;
		}
		return urlSafeEncoder.encodeToString(data.getBytes(StandardCharsets.UTF_8));
	}

	public static byte[] decodeURLSafeBase64(String data) {
		if (isNullEmpty(data)) {
			return null;
		}
		return Base64.getUrlDecoder().decode(data);
	}

	public static boolean isNullEmpty(byte[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isNullEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
}