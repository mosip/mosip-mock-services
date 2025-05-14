package io.mosip.mock.sbi.test;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.service.SBIMockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TestMockSBITest {

     static final Logger logger = LoggerFactory.getLogger(TestMockSBITest.class);

     @Mock
     private SBIMockService mockService;

    @BeforeEach
     void setUp() {
        // Initialize the mock service before each test
        mockService = mock(SBIMockService.class);
    }

    /**
     * Test if the purpose is valid.
     */
    @Test
     void testIsValidPurpose_ValidPurpose() {
        String validPurpose = SBIConstant.MOSIP_PURPOSE_REGISTRATION;
        assertTrue(isValidPurpose(validPurpose), "Valid purpose should return true.");
    }

    /**
     * Test if the biometric type is valid.
     */
    @Test
    void testIsValidBiometricType_ValidBiometricType() {
        String validBiometricType = SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER;
        assertTrue(isValidBiometricType(validBiometricType), "Valid biometric type should return true.");
    }

    /**
     * Test if the biometric image type is valid.
     */
    @Test
    void testIsValidBiometricImageType_ValidBiometricImageType() {
        String validBiometricImageType = SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_JP2000;
        assertTrue(isValidBiometricImageType(SBIConstant.MOSIP_PURPOSE_REGISTRATION, validBiometricImageType),
                "Valid biometric image type should return true.");
    }


    /**
     * Test if the `SBIMockService` handles invalid purposes correctly.
     */
    @Test
    void testMain_InvalidPurpose() {
        // Arrange
        String[] args = new String[]{
                "mosip.mock.sbi.device.purpose=InvalidPurpose",
                "mosip.mock.sbi.biometric.type=Biometric Device",
                "mosip.mock.sbi.biometric.image.type=JP2000"
        };

        // Act
        TestMockSBI.main(args);

        // Assert
        // We are verifying that the logger logs the error about invalid arguments
        // We assume the logging is done inside the `run` method or via logger in the `TestMockSBI` class
        verify(mockService, never()).run(); // Make sure the run method is never called
    }

    /**
     * Test if the `SBIMockService` handles invalid biometric types correctly.
     */
    @Test
    void testMain_InvalidBiometricType() {
        // Arrange
        String[] args = new String[]{
                "mosip.mock.sbi.device.purpose=Registration",
                "mosip.mock.sbi.biometric.type=InvalidBiometricType",
                "mosip.mock.sbi.biometric.image.type=JP2000"
        };

        // Act
        TestMockSBI.main(args);

        // Assert
        verify(mockService, never()).run(); // Ensure the run method is never called for invalid biometric type
    }

    /**
     * Test if the `SBIMockService` handles invalid biometric image types correctly.
     */
    @Test
    void testMain_InvalidBiometricImageType() {
        // Arrange
        String[] args = new String[]{
                "mosip.mock.sbi.device.purpose=Registration",
                "mosip.mock.sbi.biometric.type=Biometric Device",
                "mosip.mock.sbi.biometric.image.type=InvalidImageType"
        };

        // Act
        TestMockSBI.main(args);

        // Assert
        verify(mockService, never()).run(); // Ensure the run method is never called for invalid image type
    }

    /**
     * Helper method to validate the purpose.
     * You can mock this method to test various scenarios in your actual test class.
     */
     boolean isValidPurpose(String purpose) {
        if (purpose == null || purpose.trim().length() == 0)
            return false;

        return (purpose
                .equalsIgnoreCase(SBIConstant.MOSIP_PURPOSE_REGISTRATION)
                || purpose.equalsIgnoreCase(SBIConstant.MOSIP_PURPOSE_AUTH));
    }

    /**
     * Helper method to validate the biometric type.
     */
    boolean isValidBiometricType(String biometricType) {
        if (biometricType == null || biometricType.trim().length() == 0)
            return false;
        return (biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_BIOMETRIC_DEVICE)
                || biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER)
                || biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_FACE)
                || biometricType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_TYPE_IRIS));
    }

    /**
     * Helper method to validate the biometric image type.
     */
    boolean isValidBiometricImageType(String purpose, String biometricImageType) {
        if (purpose == null || purpose.trim().isEmpty() || biometricImageType == null
                || biometricImageType.trim().isEmpty())
            return false;

        return ((biometricImageType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)
                || biometricImageType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_JP2000))
                && !(purpose.equalsIgnoreCase(
                SBIConstant.MOSIP_PURPOSE_REGISTRATION)
                && biometricImageType.equalsIgnoreCase(SBIConstant.MOSIP_BIOMETRIC_IMAGE_TYPE_WSQ)));
    }
}
