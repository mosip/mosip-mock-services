package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.*;

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

@ExtendWith(MockitoExtension.class)
class SDKInfoServiceTest {

    @Mock
    private Environment env;

    private SDKInfoService service;
    private static final String SPEC_VERSION = "0.9.0";
    private static final String ORGANIZATION = "MOSIP";
    private static final String TYPE = "SDK";
    private static final String VERSION = "0.9";

    @BeforeEach
    void setUp() {
        service = new SDKInfoService(env, SPEC_VERSION, ORGANIZATION, TYPE, VERSION);
    }

    @Test
    void testGetSDKInfo_BasicInfo() {
        SDKInfo info = service.getSDKInfo();

        assertAll(
                () -> assertNotNull(info),
                () -> assertNotNull(info.getApiVersion()),
                () -> assertTrue(info.getApiVersion().length() > 0)
        );
    }

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