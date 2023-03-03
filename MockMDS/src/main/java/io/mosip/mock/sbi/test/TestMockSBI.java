package io.mosip.mock.sbi.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.service.SBIMockService;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;

public class TestMockSBI {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestMockSBI.class);

	public static void main(String[] args) {

		String purpose = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_REGISTRATION);
		String biometricType = SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE;
		String biometricImageType = SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_JP2000;

		if (args != null && args.length > 0) {
			// Argument 0 should contain "mosip.mock.sbi.device.purpose=Registration"
			String purposeInfo = args[0];
			LOGGER.info("main :: Purpose :: Argument [0] " + purposeInfo);
			if (purposeInfo.contains(SBIConstant.MOSIP_PURPOSE)) {
				purpose = purposeInfo.split("=")[1];
			}
			// Argument 1 should contain "mosip.mock.sbi.biometric.type=Biometric Device"
			String biometricTypeInfo = args[1];
			LOGGER.info("main :: BiometricType :: Argument [1] " + purposeInfo);
			if (biometricTypeInfo.contains(SBIConstant.MOSIP_BIOMETRIC_TYPE)) {
				biometricType = biometricTypeInfo.split("=")[1];
			}
			// Argument 1 should contain "mosip.mock.sbi.biometric.image.type=JP2000/WSQ"
			if (args.length > 2) {
				String biometricImageTypeInfo = args[2];
				LOGGER.info("main :: BiometricImageType :: Argument [2] " + biometricImageTypeInfo);
				if (biometricImageTypeInfo.contains(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE)) {
					biometricImageType = biometricImageTypeInfo.split("=")[1];
				}
			}

			LOGGER.info("main :: purpose :: " + purpose + " :: BiometricType :: " + biometricType
					+ " :: BiometricImageType :: " + biometricImageType.trim());
			// Check Purpose and BiometricType values
			if (isValidPurpose(purpose) && isValidBiometricType(biometricType)
					&& isValidBiometricImageType(purpose, biometricImageType)) {
				SBIMockService mockService = new SBIMockService(purpose.trim(), biometricType, null,
						biometricImageType.trim());
				mockService.run();
			} else {
				LOGGER.error(
						"Please check the Arguments Passed through command line for purpose and biometricType values...");
			}
		} else {
			LOGGER.error("Please check if Arguments are Passed through command line for purpose and biometricType...");
		}
	}

	private static boolean isValidPurpose(String purpose) {
		if (purpose == null || purpose.trim().length() == 0)
			return false;

		if (purpose != null && (purpose
				.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_REGISTRATION))
				|| purpose.equalsIgnoreCase(
						ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_AUTH)))) {
			return true;
		}
		return false;
	}

	private static boolean isValidBiometricType(String biometricType) {
		if (biometricType == null || biometricType.trim().length() == 0)
			return false;
		if (biometricType != null && (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
				|| biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)
				|| biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)
				|| biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS))) {
			return true;
		}
		return false;
	}

	private static boolean isValidBiometricImageType(String purpose, String biometricImageType) {
		if (purpose == null || purpose.trim().length() == 0)
			return false;

		if (biometricImageType == null || biometricImageType.trim().length() == 0)
			return false;

		if (biometricImageType != null
				&& (biometricImageType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
						|| biometricImageType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_JP2000))) {
			if (purpose.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_REGISTRATION)) &&
					biometricImageType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ))
			{
				return false;
			}
			return true;
		}
		return false;
	}
}
