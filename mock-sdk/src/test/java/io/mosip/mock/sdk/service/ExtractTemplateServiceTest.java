package io.mosip.mock.sdk.service;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for ExtractTemplateService.
 * Verifies the behavior of the service when handling various cases
 * such as null sample, empty or null segments, and random level type generation.
 */
@ExtendWith(MockitoExtension.class)
class ExtractTemplateServiceTest {

    @Mock
    private Environment env;

    private BiometricRecord sample;

    private ExtractTemplateService service;
    private List<BiometricType> modalities;

    @BeforeEach
    void setUp() {
        modalities = Arrays.asList(BiometricType.FACE, BiometricType.FINGER);
        sample = new BiometricRecord();
        service = new ExtractTemplateService(env, sample, modalities, new HashMap<>());
    }

    /**
     * Tests getExtractTemplateInfo when the biometric sample is null.
     * Verifies that the service returns a response with status code 402,
     * a null biometric record, and a non-null status message.
     */
    @Test
    void getExtractTemplateInfo_nullSample_returns402WithNullResponse() {
        service = new ExtractTemplateService(env, null, modalities, new HashMap<>());

        Response<BiometricRecord> response = service.getExtractTemplateInfo();

        assertAll(
                () -> assertEquals(402, response.getStatusCode()),
                () -> assertNull(response.getResponse()),
                () -> assertNotNull(response.getStatusMessage())
        );
    }

    /**
     * Tests getExtractTemplateInfo when the biometric record has empty segments.
     * Verifies that the service returns a response with status code 402,
     * a null biometric record, and a non-null status message.
     */
    @Test
    void getExtractTemplateInfo_emptySegments_returns402WithNullResponse() {
        sample.setSegments(Collections.emptyList());

        Response<BiometricRecord> response = service.getExtractTemplateInfo();

        assertAll(
                () -> assertEquals(402, response.getStatusCode()),
                () -> assertNull(response.getResponse()),
                () -> assertNotNull(response.getStatusMessage())
        );
    }

    /**
     * Tests getExtractTemplateInfo when the biometric record has null segments.
     * Verifies that the service returns a response with status code 402,
     * a null biometric record, and a non-null status message.
     */
    @Test
    void getExtractTemplateInfo_nullSegments_returns402WithNullResponse() {
        sample.setSegments(null);

        Response<BiometricRecord> response = service.getExtractTemplateInfo();

        assertAll(
                () -> assertEquals(402, response.getStatusCode()),
                () -> assertNull(response.getResponse()),
                () -> assertNotNull(response.getStatusMessage())
        );
    }

    /**
     * Tests the getRandomLevelType method.
     * Verifies that the generated ProcessedLevelType is one of the expected values:
     * INTERMEDIATE or PROCESSED.
     */
    @Test
    void getRandomLevelType_anyCall_returnsIntermediateOrProcessed() {
        ProcessedLevelType level = service.getRandomLevelType();
        assertTrue(Arrays.asList(ProcessedLevelType.INTERMEDIATE, ProcessedLevelType.PROCESSED).contains(level));
    }
}