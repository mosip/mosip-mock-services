package io.mosip.mock.sbi.devicehelper.iris.binacular;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.mosip.mock.sbi.SBIConstant;

/**
 * Test class for SBIIrisDoubleHelper that verifies the functionality
 * of biometric iris capture operations for single and double iris scans.
 */
class SBIIrisDoubleHelperTest {

    private SBIIrisDoubleHelper irisHelper;
    // Constants for device configuration
    private static final int PORT = 4501;
    private static final String PURPOSE = SBIConstant.PURPOSE_AUTH;
    private static final String KEYSTORE_PATH = "keystore.p12";
    private static final String BIOMETRIC_IMAGE_TYPE = "ISO";
    private static final String PROFILE_ID = "default";

    // Device sub IDs for different iris capture modes
    private static final int LEFT_IRIS = 1;    // Left iris capture
    private static final int RIGHT_IRIS = 2;   // Right iris capture
    private static final int BOTH_IRIS = 3;    // Both irises capture

    /**
     * Sets up the test environment before each test method.
     * Initializes the iris helper with default configuration and sets required parameters.
     */
    @BeforeEach
    void setUp() {
        irisHelper = SBIIrisDoubleHelper.getInstance(PORT, PURPOSE, KEYSTORE_PATH, BIOMETRIC_IMAGE_TYPE);
        irisHelper.setProfileId(PROFILE_ID);
        irisHelper.setQualityScore(90);
    }

    /**
     * Tests the singleton pattern implementation of getInstance() method.
     * Verifies that the helper is properly initialized with correct configuration values.
     */
    @Test
    void testGetInstance() {
        assertNotNull(irisHelper);
        assertEquals(PORT, irisHelper.getPort());
        assertEquals(PURPOSE, irisHelper.getPurpose());
        assertEquals(PROFILE_ID, irisHelper.getProfileId());
    }

    /**
     * Tests the device initialization process.
     * Verifies that the device is properly initialized and capture info is created.
     */
    @Test
    void testInitDevice() {
        long result = irisHelper.initDevice();
        assertEquals(0, result);
        assertNotNull(irisHelper.getCaptureInfo());
        assertTrue(irisHelper.getCaptureInfo() instanceof SBIIrisDoubleCaptureInfo);
    }

    /**
     * Tests the device de-initialization process.
     * Ensures that the device is properly shut down and capture info is cleared.
     */
    @Test
    void testDeInitDevice() {
        irisHelper.initDevice();
        int result = irisHelper.deInitDevice();
        assertEquals(0, result);
        assertNull(irisHelper.getCaptureInfo());
    }

    /**
     * Tests the live stream functionality when no image is available.
     * Expects a -1 return value indicating no stream is available.
     */
    @Test
    void testGetLiveStream() {
        irisHelper.initDevice();
        int result = irisHelper.getLiveStream();
        assertEquals(-1, result);
    }

    /**
     * Tests the bio capture process for left iris only.
     * Verifies successful capture with proper initialization and flags set.
     */
    @Test
    void testGetBioCapture_LeftIris() throws Exception {
        irisHelper.initDevice();
        irisHelper.setDeviceSubId(LEFT_IRIS);
        SBIIrisDoubleCaptureInfo captureInfo = (SBIIrisDoubleCaptureInfo) irisHelper.getCaptureInfo();
        captureInfo.setCaptureStarted(true);
        int result = irisHelper.getBioCapture(true);
        assertEquals(0, result);
    }

    /**
     * Tests the bio capture process for right iris only.
     * Verifies successful capture with proper initialization and flags set.
     */
    @Test
    void testGetBioCapture_RightIris() throws Exception {
        irisHelper.initDevice();
        irisHelper.setDeviceSubId(RIGHT_IRIS);
        SBIIrisDoubleCaptureInfo captureInfo = (SBIIrisDoubleCaptureInfo) irisHelper.getCaptureInfo();
        captureInfo.setCaptureStarted(true);
        int result = irisHelper.getBioCapture(true);
        assertEquals(0, result);
    }

    /**
     * Tests the bio capture process for both irises simultaneously.
     * Verifies successful capture with proper initialization and flags set.
     */
    @Test
    void testGetBioCapture_BothIris() throws Exception {
        irisHelper.initDevice();
        irisHelper.setDeviceSubId(BOTH_IRIS);
        SBIIrisDoubleCaptureInfo captureInfo = (SBIIrisDoubleCaptureInfo) irisHelper.getCaptureInfo();
        captureInfo.setCaptureStarted(true);
        int result = irisHelper.getBioCapture(true);
        assertEquals(0, result);
    }
}