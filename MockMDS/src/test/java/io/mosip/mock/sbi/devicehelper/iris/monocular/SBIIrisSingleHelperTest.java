package io.mosip.mock.sbi.devicehelper.iris.monocular;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;
import io.mosip.mock.sbi.util.BioUtilHelper;

/**
 * Test class for SBIIrisSingleHelper that verifies iris capture functionality
 */
public class SBIIrisSingleHelperTest {

    private SBIIrisSingleHelper irisHelper;
    private static final int PORT = 8080;
    private static final String PURPOSE = SBIConstant.PURPOSE_AUTH;
    private static final String KEYSTORE_PATH = "keystore.p12";
    private static final String BIOMETRIC_IMAGE_TYPE = "image/jpeg";

    /**
     * Setup method to initialize the iris helper before each test
     */
    @Before
    public void setUp() {
        irisHelper = SBIIrisSingleHelper.getInstance(PORT, PURPOSE, KEYSTORE_PATH, BIOMETRIC_IMAGE_TYPE);
        irisHelper.setQualityScore(90);
        irisHelper.setProfileId("default");
    }

    /**
     * Cleanup method to deinitialize the iris helper after each test
     */
    @After
    public void tearDown() {
        if (irisHelper != null) {
            irisHelper.deInitDevice();
        }
    }

    /**
     * Tests getInstance method to verify proper initialization
     */
    @Test
    public void testGetInstance() {
        assertNotNull("getInstance should return non-null instance", irisHelper);
        assertEquals("Port should be set correctly", PORT, irisHelper.getPort());
        assertEquals("Purpose should be set correctly", PURPOSE, irisHelper.getPurpose());
    }

    /**
     * Tests device initialization process
     */
    @Test
    public void testInitDevice() {
        long result = irisHelper.initDevice();
        assertEquals("initDevice should return 0 on success", 0, result);
        assertNotNull("Capture info should be initialized", irisHelper.getCaptureInfo());
        assertTrue("Capture info should be correct type",
                irisHelper.getCaptureInfo() instanceof SBIIrisSingleCaptureInfo);
    }

    /**
     * Tests device deinitialization process
     */
    @Test
    public void testDeInitDevice() {
        irisHelper.initDevice();
        int result = irisHelper.deInitDevice();
        assertEquals("deInitDevice should return 0", 0, result);
        assertNull("Capture info should be null after deInit", irisHelper.getCaptureInfo());
    }

    /**
     * Tests getting live stream with no image available
     */
    @Test
    public void testGetLiveStream() {
        irisHelper.initDevice();
        int result = irisHelper.getLiveStream();
        assertEquals("getLiveStream should return -1 when no image", -1, result);
    }

    /**
     * Tests bio capture for authentication purpose
     */
    @Test
    public void testGetBioCapture_Auth() throws Exception {
        irisHelper.initDevice();
        SBIIrisSingleCaptureInfo captureInfo = (SBIIrisSingleCaptureInfo) irisHelper.getCaptureInfo();
        captureInfo.setCaptureStarted(true);

        // Set device sub ID for single iris
        irisHelper.setDeviceSubId(SBIConstant.DEVICE_IRIS_SINGLE_SUB_TYPE_ID);

        int result = irisHelper.getBioCapture(true);
        assertEquals("getBioCapture should return 0", 0, result);
    }

    /**
     * Tests bio capture for registration purpose
     */
    @Test
    public void testGetBioCapture_Registration() throws Exception {
        // Create registration purpose helper
        SBIIrisSingleHelper registrationHelper = SBIIrisSingleHelper.getInstance(
                PORT, SBIConstant.PURPOSE_REGISTRATION, KEYSTORE_PATH, BIOMETRIC_IMAGE_TYPE);

        registrationHelper.initDevice();
        registrationHelper.setQualityScore(90);
        registrationHelper.setProfileId(SBIConstant.PROFILE_AUTOMATIC);
        registrationHelper.setDeviceSubId(SBIConstant.DEVICE_IRIS_SINGLE_SUB_TYPE_ID);

        SBIIrisSingleCaptureInfo captureInfo =
                (SBIIrisSingleCaptureInfo) registrationHelper.getCaptureInfo();
        captureInfo.setCaptureStarted(true);

        try (MockedStatic<ApplicationPropertyHelper> mockedHelper =
                     mockStatic(ApplicationPropertyHelper.class)) {
            mockedHelper.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(
                            SBIConstant.MOSIP_BIOMETRIC_REGISTRATION_SEED_IRIS))
                    .thenReturn("5000");

            int result = registrationHelper.getBioCapture(true);
            assertEquals("getBioCapture should return 0", 0, result);
        }

        registrationHelper.deInitDevice();
    }
}