package io.mosip.mock.sdk.service;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SegmentServiceTest {

    @Mock
    private Environment env;

    @Mock
    private BiometricRecord sample;

    private SegmentService service;

    @BeforeEach
    void setUp() {
        List<BiometricType> modalitiesToSegment = Arrays.asList(BiometricType.FACE, BiometricType.FINGER);
        service = new SegmentService(env, sample, modalitiesToSegment, new HashMap<>());
    }

    /**
     * Tests that getSegmentInfo returns a valid response with status code 200
     * and a non-null biometric record when provided with valid inputs.
     */
    @Test
    void testGetSegmentInfo_Success() {
        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }

    /**
     * Tests getSegmentInfo when the sample biometric record is null.
     * Verifies that the service still returns a valid response with status code 200
     * and a non-null biometric record.
     */
    @Test
    void testGetSegmentInfo_WithNullSample() {
        service = new SegmentService(env, null, Arrays.asList(BiometricType.FACE), new HashMap<>());

        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }

    /**
     * Tests getSegmentInfo when the list of modalities is empty.
     * Ensures that the response is valid with status code 200 and a non-null biometric record.
     */
    @Test
    void testGetSegmentInfo_WithEmptyModalities() {
        service = new SegmentService(env, sample, Collections.emptyList(), new HashMap<>());

        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }

    /**
     * Tests getSegmentInfo when the modalities list is null.
     * Verifies that the service returns a valid response with status code 200
     * and a non-null biometric record.
     */
    @Test
    void testGetSegmentInfo_WithNullModalities() {
        service = new SegmentService(env, sample, null, new HashMap<>());

        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }

    /**
     * Tests getSegmentInfo when the flags parameter is null.
     * Ensures that the response is valid with status code 200 and a non-null biometric record.
     */
    @Test
    void testGetSegmentInfo_WithNullFlags() {
        service = new SegmentService(env, sample, Arrays.asList(BiometricType.FACE), null);

        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse())
        );
    }

    /**
     * Tests that the structure of the response is as expected.
     * Specifically, verifies that the biometric record's segments are null.
     */
    @Test
    void testGetSegmentInfo_VerifyResponseStructure() {
        Response<BiometricRecord> response = service.getSegmentInfo();

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse()),
                () -> assertNull(response.getResponse().getSegments())
        );
    }
}