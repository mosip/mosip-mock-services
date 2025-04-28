package io.mosip.mock.sbi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.mosip.mock.sbi.exception.SBIException;
import io.mosip.mock.sbi.SBIConstant;

class SBIMockServiceTest {
    private SBIMockService mockService;

    @BeforeEach
    void setup() {
        mockService = new SBIMockService(
                SBIConstant.PURPOSE_AUTH,
                SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER,
                "test-keystore.p12",
                "JPEG"
        );
    }

    @Test
    void testServerStartAndStop() throws IOException {
        // Start server
        Thread serverThread = new Thread(mockService);
        serverThread.start();

        // Verify server is running
        assertFalse(mockService.isStopped());
        assertTrue(mockService.getServerPort() > 0);

        // Stop server
        mockService.stop();

        // Verify server stopped
        assertTrue(mockService.isStopped());
    }

    @Test
    void testDeviceHelperInitialization() {
        mockService.initDeviceHelpers();

        String deviceType = SBIConstant.MOSIP_BIOMETRIC_TYPE_FINGER + "_" +
                SBIConstant.MOSIP_BIOMETRIC_SUBTYPE_FINGER_SINGLE;

        assertNotNull(mockService.getDeviceHelper(deviceType));
    }
}