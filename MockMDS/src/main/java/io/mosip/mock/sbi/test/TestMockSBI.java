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
		String biometricType = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE);

		if (args != null && args.length > 0)
		{
			// Argument 0 should contain "mosip.mock.sbi.device.purpose=Registration"
			String purposeInfo = args[0];
			LOGGER.info("main :: Purpose :: Argument [0] " + purposeInfo);
			if (purposeInfo.contains(SBIConstant.MOSIP_PURPOSE))
			{
				purpose = purposeInfo.split("=") [1];
			}
			// Argument 1 should contain "mosip.mock.sbi.biometric.type=Biometric Device"
			String biometricTypeInfo = args[1];
			LOGGER.info("main :: BiometricType :: Argument [1] " + purposeInfo);
			if (biometricTypeInfo.contains(SBIConstant.MOSIP_BIOMETRIC_TYPE))
			{
				biometricType = biometricTypeInfo.split("=")[1];
			}

			LOGGER.info("main :: purpose :: " + purpose + " :: BiometricType :: " + biometricType);
			//Check Purpose and BiometricType values
			if (isValidPurpose (purpose) && isValidBiometricType(biometricType))
			{
				SBIMockService mockService = new SBIMockService (purpose, biometricType, null);
				mockService.run();
			}
			else
			{
				LOGGER.error("Please check the Arguments Passed through command line for purpose and biometricType values...");
			}
		}
		else
		{
			LOGGER.error("Please check if Arguments are Passed through command line for purpose and biometricType...");
		}
	}
	
	private static boolean isValidPurpose(String purpose) {
		if (purpose == null || purpose.trim().length() == 0)
			return false;

		if (purpose != null && (
				purpose.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_REGISTRATION)) ||
				purpose.equalsIgnoreCase(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.MOSIP_PURPOSE_AUTH))
						))
		{
			return true;
		}
		return false;
	}

	private static boolean isValidBiometricType(String biometricType) {
		if (biometricType == null || biometricType.trim().length() == 0)
			return false;
		if (biometricType != null && (
				biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMTRIC_DEVICE) ||
				biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER) ||
				biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE) ||
				biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS)
						))
		{
			return true;
		}
		return false;
	}
}
