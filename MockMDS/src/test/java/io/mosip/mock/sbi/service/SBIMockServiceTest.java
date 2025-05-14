package io.mosip.mock.sbi.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;

import io.mosip.mock.sbi.exception.SBIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.mosip.mock.sbi.SBIConstant;

/**
 * Test class for SBIMockService
 * Tests the core functionality of the SBI Mock Service including initialization,
 * lifecycle management, and device helper operations
 */
class SBIMockServiceTest {
    private TestSBIMockService service;

    /**
     * Test subclass of SBIMockService that mocks network operations
     * Provides controlled environment for testing server socket operations
     */
    static class TestSBIMockService extends SBIMockService {
        private boolean socketCreated = false;

        public TestSBIMockService(String purpose, String biometricType, String keystoreFilePath, String biometricImageType) {
            super(purpose, biometricType, keystoreFilePath, biometricImageType);
        }

        /**
         * Overridden method to avoid actual socket creation
         * Sets a flag and mock port instead of creating real socket
         */
        @Override
        public void createServerSocket() throws SBIException {
            socketCreated = true;
            this.serverPort = 9999;
        }

        public boolean wasSocketCreated() {
            return socketCreated;
        }

        /**
         * Overridden method to safely simulate service stoppage
         * Sets the stopped flag without actual network cleanup
         */
        @Override
        public void stop() throws IOException {
            setStopped(true);
        }
    }

    /**
     * Sets up the test environment before each test
     * Initializes a test service instance with standard test parameters
     */
    @BeforeEach
    void setUp() {
        service = new TestSBIMockService(
                SBIConstant.PURPOSE_REGISTRATION,
                SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER,
                "test-keystore.p12",
                "jpeg"
        );
    }

    /**
     * Tests proper initialization of service constructor
     * Verifies that all parameters are correctly set during instantiation
     */
    @Test
    void testConstructorInitialization() {
        assertEquals(SBIConstant.PURPOSE_REGISTRATION, service.getPurpose());
        assertEquals(SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER, service.getBiometricType());
        assertEquals("test-keystore.p12", service.getKeystoreFilePath());
        assertEquals("jpeg", service.getBiometricImageType());
        assertEquals(SBIConstant.PROFILE_DEFAULT, service.getProfileId());
    }

    /**
     * Tests device helper initialization
     * Verifies that device helpers are properly created and accessible
     * for the specified biometric type and subtype
     */
    @Test
    void testInitDeviceHelpers() {
        service.initDeviceHelpers();
        assertNotNull(service.getDeviceHelper(
                SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_" +
                        SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP
        ));
    }

    /**
     * Tests the complete lifecycle of the service
     * Verifies:
     * 1. Service starts correctly
     * 2. Socket is created
     * 3. Service can be stopped
     * 4. Stopped state is properly set
     */
    @Test
    void testServiceLifecycle() throws IOException {
        Thread serviceThread = new Thread(service);
        serviceThread.start();

        try {
            Thread.sleep(100);
            assertTrue(service.wasSocketCreated());
            service.stop();
            assertTrue(service.isStopped());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }
    }

    /**
     * Tests device helper retrieval functionality
     * Verifies:
     * 1. Null is returned for non-existent devices
     * 2. Valid helper is returned after initialization
     */
    @Test
    void testGetDeviceHelper() {
        assertNull(service.getDeviceHelper("NON_EXISTENT_DEVICE"));
        service.initDeviceHelpers();
        assertNotNull(service.getDeviceHelper(
                SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_" +
                        SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SLAP
        ));
    }

    /**
     * Tests profile ID management
     * Verifies that profile ID can be set and retrieved correctly
     */
    @Test
    void testProfileIdManagement() {
        String newProfileId = "TEST_PROFILE";
        service.setProfileId(newProfileId);
        assertEquals(newProfileId, service.getProfileId());
    }

    /**
     * Tests server port management
     * Verifies that port number can be set and retrieved correctly
     */
    @Test
    void testPortManagement() {
        assertEquals(0, service.getServerPort());
        service.setServerPort(8080);
        assertEquals(8080, service.getServerPort());
    }
}