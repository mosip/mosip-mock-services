package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;

/**
 * Test class for SegmentService.
 * Tests the segmentation functionality for biometric data.
 */
@ExtendWith(MockitoExtension.class)
class SegmentServiceTest {

    @Mock
    private Environment env;

    private SegmentService service;
    private BiometricRecord sample;
    private List<BiometricType> modalities;

    /**
     * Setup method executed before each test.
     * Initializes the sample, modalities, and service.
     */
    @BeforeEach
    void setUp() {
        sample = new BiometricRecord();
        modalities = Arrays.asList(BiometricType.FINGER, BiometricType.FACE, BiometricType.IRIS);
        service = new SegmentService(env, sample, modalities, new HashMap<>());
    }

    /**
     * Tests segmentation with valid input.
     * Verifies that a success response is returned.
     */
    @Test
    void getSegmentInfo_validInput_returnsSuccessResponse() {
        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse()),
                () -> assertNull(response.getResponse().getSegments())
        );
    }

    /**
     * Tests segmentation with null sample.
     * Verifies that a success response is returned.
     */
    @Test
    void getSegmentInfo_nullSample_returnsSuccessResponse() {
        service = new SegmentService(env, null, modalities, new HashMap<>());

        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }

    /**
     * Tests segmentation with null modalities.
     * Verifies that a success response is returned.
     */
    @Test
    void getSegmentInfo_nullModalities_returnsSuccessResponse() {
        service = new SegmentService(env, sample, null, new HashMap<>());

        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }

    /**
     * Tests segmentation with null flags.
     * Verifies that a success response is returned.
     */
    @Test
    void getSegmentInfo_nullFlags_returnsSuccessResponse() {
        service = new SegmentService(env, sample, modalities, null);

        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }

    /**
     * Tests segmentation with empty modalities list.
     * Verifies that a success response is returned.
     */
    @Test
    void getSegmentInfo_emptyModalities_returnsSuccessResponse() {
        service = new SegmentService(env, sample, Collections.emptyList(), new HashMap<>());

        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }

    /**
     * Tests segmentation with null environment.
     * Verifies that a success response is returned.
     */
    @Test
    void getSegmentInfo_nullEnvironment_returnsSuccessResponse() {
        service = new SegmentService(null, sample, modalities, new HashMap<>());

        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }
}
