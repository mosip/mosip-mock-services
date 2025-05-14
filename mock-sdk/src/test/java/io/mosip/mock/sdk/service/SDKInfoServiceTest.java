package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.model.SDKInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test class for SDKInfoService
 * Verifies the SDK information retrieval functionality including:
 * - Basic SDK information
 * - Supported biometric modalities
 * - Supported biometric functions
 */
@ExtendWith(MockitoExtension.class)
class SDKInfoServiceTest {

    // Mock Spring environment
    @Mock
    private Environment env;

    // Service instance to be tested
    private SDKInfoService service;

    // Constants for SDK information
    private static final String SPEC_VERSION = "0.9.0";
    private static final String ORGANIZATION = "MOSIP";
    private static final String TYPE = "SDK";
    private static final String VERSION = "0.9";

    /**
     * Setup method executed before each test
     * Initializes the SDKInfoService with mock environment and test constants
     */
    @BeforeEach
    void setUp() {
        service = new SDKInfoService(env, SPEC_VERSION, ORGANIZATION, TYPE, VERSION);
    }

    /**
     * Tests basic SDK information retrieval
     * Verifies that API version is properly set and not empty
     */
    @Test
    void testGetSDKInfo_BasicInfo() {
        SDKInfo info = service.getSDKInfo();

        assertAll(
                () -> assertNotNull(info),
                () -> assertNotNull(info.getApiVersion()),
                () -> assertTrue(info.getApiVersion().length() > 0)
        );
    }

    /**
     * Tests supported biometric modalities
     * Verifies that all expected modalities (FINGER, FACE, IRIS) are supported
     */
    @Test
    void testGetSDKInfo_SupportedModalities() {
        SDKInfo info = service.getSDKInfo();
        List<BiometricType> expectedModalities = Arrays.asList(
                BiometricType.FINGER,
                BiometricType.FACE,
                BiometricType.IRIS
        );

        assertAll(
                () -> assertNotNull(info.getSupportedModalities()),
                () -> assertEquals(expectedModalities.size(), info.getSupportedModalities().size()),
                () -> assertTrue(info.getSupportedModalities().containsAll(expectedModalities))
        );
    }

    /**
     * Tests supported biometric methods
     * Verifies that all expected functions (MATCH, QUALITY_CHECK, EXTRACT, CONVERT_FORMAT)
     * are supported
     */
    @Test
    void testGetSDKInfo_SupportedMethods() {
        SDKInfo info = service.getSDKInfo();
        Map<BiometricFunction, List<BiometricType>> methods = info.getSupportedMethods();

        assertAll(
                () -> assertNotNull(methods),
                () -> assertEquals(4, methods.size()),
                () -> assertTrue(methods.containsKey(BiometricFunction.MATCH)),
                () -> assertTrue(methods.containsKey(BiometricFunction.QUALITY_CHECK)),
                () -> assertTrue(methods.containsKey(BiometricFunction.EXTRACT)),
                () -> assertTrue(methods.containsKey(BiometricFunction.CONVERT_FORMAT))
        );
    }

    /**
     * Tests modalities supported for each biometric method
     * Verifies that each function supports all three modalities (FINGER, FACE, IRIS)
     */
    @Test
    void testGetSDKInfo_MethodModalities() {
        SDKInfo info = service.getSDKInfo();
        Map<BiometricFunction, List<BiometricType>> methods = info.getSupportedMethods();

        for (BiometricFunction function : methods.keySet()) {
            List<BiometricType> modalities = methods.get(function);
            assertAll(
                    () -> assertNotNull(modalities),
                    () -> assertEquals(3, modalities.size()),
                    () -> assertTrue(modalities.contains(BiometricType.FINGER)),
                    () -> assertTrue(modalities.contains(BiometricType.FACE)),
                    () -> assertTrue(modalities.contains(BiometricType.IRIS))
            );
        }
    }

    /**
     * Tests SDK information retrieval with null environment
     * Verifies that service functions correctly even with null environment
     */
    @Test
    void testGetSDKInfo_WithNullEnvironment() {
        service = new SDKInfoService(null, SPEC_VERSION, ORGANIZATION, TYPE, VERSION);
        SDKInfo info = service.getSDKInfo();

        assertAll(
                () -> assertNotNull(info),
                () -> assertNotNull(info.getApiVersion()),
                () -> assertNotNull(info.getSupportedModalities()),
                () -> assertNotNull(info.getSupportedMethods())
        );
    }
}