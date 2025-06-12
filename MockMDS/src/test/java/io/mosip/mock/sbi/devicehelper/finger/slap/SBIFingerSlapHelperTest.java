package io.mosip.mock.sbi.devicehelper.finger.slap;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.StringHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SBIFingerSlapHelperTest {

    private SBIFingerSlapHelper helper;
    private static final int PORT = 4501;
    private static final String PURPOSE = "Registration";
    private static final String KEYSTORE_PATH = "keystore.p12";
    private static final String IMAGE_TYPE = "WSQ";

    @BeforeEach
    void setUp() {
        helper = SBIFingerSlapHelper.getInstance(PORT, PURPOSE, KEYSTORE_PATH, IMAGE_TYPE);
        helper.setProfileId(SBIConstant.PROFILE_AUTOMATIC);
        helper.setDeviceSubId(SBIConstant.DEVICE_FINGER_SLAP_SUB_TYPE_ID_LEFT);
    }

    /**
     * Tests device initialization.
     * Should create new capture info and return 0 on success.
     */
    @Test
    void initDevice_DeviceInitialization_ReturnsZeroAndInitializesCaptureInfo() {
        long result = helper.initDevice();
        assertEquals(0, result, "Device initialization should return 0");
        assertNotNull(helper.getCaptureInfo(), "CaptureInfo should be initialized");
        assertTrue(helper.getCaptureInfo() instanceof SBIFingerSlapCaptureInfo,
                "CaptureInfo should be instance of SBIFingerSlapCaptureInfo");
    }

    /**
     * Tests device deinitialization.
     * Should clear capture info and return 0 on success.
     */
    @Test
    void deInitDevice_DeviceDeinitialization_ReturnsZeroAndClearsCaptureInfo() {
        helper.initDevice();
        int result = helper.deInitDevice();
        assertEquals(0, result, "Device deinitialization should return 0");
        assertNull(helper.getCaptureInfo(), "CaptureInfo should be null after deinitialization");
    }

    /**
     * Tests biometric capture for authentication.
     * Should process fingerprint slap images and update capture info accordingly.
     */
    @Test
    void getBioCaptureForAuthentication_InvokesBioCapture_ReturnsZeroAndEncodesBioData() throws Exception {
        try (MockedStatic<ApplicationPropertyHelper> propertyHelperMock = mockStatic(ApplicationPropertyHelper.class);
             MockedStatic<StringHelper> stringHelperMock = mockStatic(StringHelper.class)) {

            propertyHelperMock.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(anyString()))
                    .thenReturn("1");

            stringHelperMock.when(() -> StringHelper.base64UrlEncode(any(byte[].class)))
                    .thenReturn("base64EncodedString");

            helper.initDevice();
            int result = helper.getBioCapture(true);
            assertEquals(0, result, "Bio capture should return 0 on success");
        }
    }

    /**
     * Tests instance creation with valid parameters.
     * Should return new instance with correct initialization parameters.
     */
    @Test
    void getInstance_CreationWithParameters_ReturnsInitializedInstance() {
        SBIFingerSlapHelper instance = SBIFingerSlapHelper.getInstance(PORT, PURPOSE, KEYSTORE_PATH, IMAGE_TYPE);
        assertNotNull(instance, "getInstance should return a non-null instance");
        assertEquals(PORT, instance.getPort(), "Port should match constructor parameter");
        assertEquals(PURPOSE, instance.getPurpose(), "Purpose should match constructor parameter");
    }

    /**
     * Tests multiple instance creation.
     * Should return different instances due to non-singleton implementation.
     */
    @Test
    void getInstance_MultipleCalls_ReturnsDistinctInstances() {
        SBIFingerSlapHelper instance1 = SBIFingerSlapHelper.getInstance(PORT, PURPOSE, KEYSTORE_PATH, IMAGE_TYPE);
        SBIFingerSlapHelper instance2 = SBIFingerSlapHelper.getInstance(PORT, PURPOSE, KEYSTORE_PATH, IMAGE_TYPE);
        assertNotSame(instance1, instance2, "getInstance should return different instances");
    }

    /**
     * Tests capture info initialization.
     * Should properly initialize slap capture info with bio exception info.
     */
    @Test
    void getCaptureInfo_AfterInitDevice_InitializesBioExceptionInfoInstance() {
        helper.initDevice();
        SBIFingerSlapCaptureInfo captureInfo = (SBIFingerSlapCaptureInfo) helper.getCaptureInfo();
        assertNotNull(captureInfo.getBioExceptionInfo(), "Bio exception info should be initialized");
        assertTrue(captureInfo.getBioExceptionInfo() instanceof SBIFingerSlapBioExceptionInfo,
                "Bio exception info should be instance of SBIFingerSlapBioExceptionInfo");
    }
}