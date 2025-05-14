package io.mosip.mock.sbi.devicehelper.face;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SBIFaceCaptureInfoTest {

    private SBIFaceCaptureInfo captureInfo;

    @BeforeEach
    void setUp() {
        captureInfo = new SBIFaceCaptureInfo();
    }

    /**
     * Test initialization of capture info
     * Verifies that all fields are properly reset to their default values
     */
    @Test
    void testInitCaptureInfo() {
        // When
        captureInfo.initCaptureInfo();

        // Then
        assertNull(captureInfo.getImage());
        assertFalse(captureInfo.isLiveStreamStarted());
        assertFalse(captureInfo.isLiveStreamCompleted());
        assertFalse(captureInfo.isCaptureStarted());
        assertFalse(captureInfo.isCaptureCompleted());
        assertNull(captureInfo.getBioExceptionInfo());
    }

    /**
     * Test de-initialization of capture info
     * Verifies that all fields are properly cleared
     */
    @Test
    void testDeInitCaptureInfo() {
        // Given
        captureInfo.setLiveStreamStarted(true);
        captureInfo.setCaptureStarted(true);

        // When
        captureInfo.deInitCaptureInfo();

        // Then
        assertNull(captureInfo.getImage());
        assertFalse(captureInfo.isLiveStreamStarted());
        assertFalse(captureInfo.isLiveStreamCompleted());
        assertFalse(captureInfo.isCaptureStarted());
        assertFalse(captureInfo.isCaptureCompleted());
        assertNull(captureInfo.getBioExceptionInfo());
    }

    /**
     * Test face biometric value getters and setters
     */
    @Test
    void testFaceBioValueProperties() {
        // Given
        String bioValue = "test-bio-value";
        String bioSubType = "test-sub-type";
        float requestScore = 90.5f;
        float captureScore = 85.0f;

        // When
        captureInfo.setBioValueFace(bioValue);
        captureInfo.setBioSubTypeFace(bioSubType);
        captureInfo.setRequestScoreFace(requestScore);
        captureInfo.setCaptureScoreFace(captureScore);
        captureInfo.setCaptureFace(true);

        // Then
        assertEquals(bioValue, captureInfo.getBioValueFace());
        assertEquals(bioSubType, captureInfo.getBioSubTypeFace());
        assertEquals(requestScore, captureInfo.getRequestScoreFace());
        assertEquals(captureScore, captureInfo.getCaptureScoreFace());
        assertTrue(captureInfo.isCaptureFace());
    }

    /**
     * Test exception photo properties getters and setters
     */
    @Test
    void testExceptionPhotoProperties() {
        // Given
        String bioValue = "test-exception-photo";
        String bioSubType = "exception-type";
        int requestScore = 90;
        int captureScore = 85;

        // When
        captureInfo.setBioValueExceptionPhoto(bioValue);
        captureInfo.setBioSubTypeExceptionPhoto(bioSubType);
        captureInfo.setRequestScoreExceptionPhoto(requestScore);
        captureInfo.setCaptureScoreExceptionPhoto(captureScore);
        captureInfo.setCaptureExceptionPhoto(true);

        // Then
        assertEquals(bioValue, captureInfo.getBioValueExceptionPhoto());
        assertEquals(bioSubType, captureInfo.getBioSubTypeExceptionPhoto());
        assertEquals(requestScore, captureInfo.getRequestScoreExceptionPhoto());
        assertEquals(captureScore, captureInfo.getCaptureScoreExceptionPhoto());
        assertTrue(captureInfo.isCaptureExceptionPhoto());
    }

    /**
     * Test default values after constructor
     */
    @Test
    void testDefaultValues() {
        assertNull(captureInfo.getBioValueFace());
        assertNull(captureInfo.getBioSubTypeFace());
        assertEquals(0.0f, captureInfo.getRequestScoreFace());
        assertEquals(0.0f, captureInfo.getCaptureScoreFace());
        assertFalse(captureInfo.isCaptureFace());

        assertNull(captureInfo.getBioValueExceptionPhoto());
        assertNull(captureInfo.getBioSubTypeExceptionPhoto());
        assertEquals(0, captureInfo.getRequestScoreExceptionPhoto());
        assertEquals(0, captureInfo.getCaptureScoreExceptionPhoto());
        assertFalse(captureInfo.isCaptureExceptionPhoto());
    }
}