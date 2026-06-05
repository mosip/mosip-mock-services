package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIRInfo;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Response;

/**
 * Test class for ConvertFormatService.
 * Tests the format conversion functionality for biometric data.
 */
@ExtendWith(MockitoExtension.class)
class ConvertFormatServiceTest {

    @Mock
    private Environment env;

    private ConvertFormatService service;
    private BiometricRecord sample;
    private List<BiometricType> modalities;
    private Map<String, String> sourceParams;
    private Map<String, String> targetParams;

    /**
     * Setup method executed before each test.
     * Initializes the modalities and parameters.
     */
    @BeforeEach
    void setUp() {
        modalities = Arrays.asList(BiometricType.FINGER, BiometricType.FACE, BiometricType.IRIS);
        sourceParams = new HashMap<>();
        targetParams = new HashMap<>();
    }

    /**
     * Tests format conversion with invalid finger data.
     * Verifies that an invalid input error is returned.
     */
    @Test
    void getConvertFormatInfo_invalidFingerData_returnsInvalidInputError() {
        sample = createBiometricRecord(BiometricType.FINGER, Collections.singletonList("Left IndexFinger"));
        service = new ConvertFormatService(env, sample, "ISO19794_4_2011", "ISO19794_4_2011",
                sourceParams, targetParams, modalities);

        Response<BiometricRecord> response = service.getConvertFormatInfo();

        assertEquals(401, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests format conversion with invalid face data.
     * Verifies that an invalid input error is returned.
     */
    @Test
    void getConvertFormatInfo_invalidFaceData_returnsInvalidInputError() {
        sample = createBiometricRecord(BiometricType.FACE, Collections.singletonList("Left IndexFinger"));
        service = new ConvertFormatService(env, sample, "ISO19794_5_2011", "ISO19794_5_2011",
                sourceParams, targetParams, modalities);

        Response<BiometricRecord> response = service.getConvertFormatInfo();

        assertEquals(401, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests format conversion with invalid iris subtype.
     * Verifies that a missing input error is returned.
     */
    @Test
    void getConvertFormatInfo_invalidIrisSubtype_returnsMissingInputError() {
        sample = createBiometricRecord(BiometricType.IRIS, Collections.singletonList("Left IndexFinger"));
        service = new ConvertFormatService(env, sample, "ISO19794_6_2011", "ISO19794_6_2011",
                sourceParams, targetParams, modalities);

        Response<BiometricRecord> response = service.getConvertFormatInfo();

        assertEquals(402, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests format conversion with valid iris subtype but invalid data.
     * Verifies that an invalid input error is returned.
     */
    @Test
    void getConvertFormatInfo_validIrisSubtype_returnsInvalidInputError() {
        sample = createBiometricRecord(BiometricType.IRIS, Collections.singletonList("Left"));
        service = new ConvertFormatService(env, sample, "ISO19794_6_2011", "ISO19794_6_2011",
                sourceParams, targetParams, modalities);

        Response<BiometricRecord> response = service.getConvertFormatInfo();

        assertEquals(401, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests format conversion with null subtype.
     * Verifies that an unknown error is returned.
     */
    @Test
    void getConvertFormatInfo_nullSubtype_returnsUnknownError() {
        sample = createBiometricRecord(BiometricType.FINGER, null);
        service = new ConvertFormatService(env, sample, "ISO19794_4_2011", "ISO19794_4_2011",
                sourceParams, targetParams, modalities);

        Response<BiometricRecord> response = service.getConvertFormatInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests format conversion with empty subtype.
     * Verifies that an unknown error is returned.
     */
    @Test
    void getConvertFormatInfo_emptySubtype_returnsUnknownError() {
        sample = createBiometricRecord(BiometricType.FINGER, Collections.emptyList());
        service = new ConvertFormatService(env, sample, "ISO19794_4_2011", "ISO19794_4_2011",
                sourceParams, targetParams, modalities);

        Response<BiometricRecord> response = service.getConvertFormatInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests format conversion with invalid BIR data.
     * Verifies that an unknown error is returned.
     */
    @Test
    void getConvertFormatInfo_invalidBirData_returnsUnknownError() {
        BIR invalidBir = new BIR();
        sample = new BiometricRecord();
        sample.setSegments(Collections.singletonList(invalidBir));
        service = new ConvertFormatService(env, sample, "ISO19794_4_2011", "ISO19794_4_2011",
                sourceParams, targetParams, modalities);

        Response<BiometricRecord> response = service.getConvertFormatInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests validation of finger biometric type with finger format.
     * Verifies that the validation returns true.
     */
    @Test
    void isValidBioTypeForSourceFormat_fingerWithFingerFormat_returnsTrue() {
        sample = createBiometricRecord(BiometricType.FINGER, Collections.singletonList("Left IndexFinger"));
        service = new ConvertFormatService(env, sample, "ISO19794_4_2011", "ISO19794_4_2011",
                sourceParams, targetParams, modalities);

        boolean result = ReflectionTestUtils.invokeMethod(service, "isValidBioTypeForSourceFormat",
                BiometricType.FINGER, "ISO19794_4_2011");

        assertTrue(result);
    }

    /**
     * Tests validation of face biometric type with face format.
     * Verifies that the validation returns true.
     */
    @Test
    void isValidBioTypeForSourceFormat_faceWithFaceFormat_returnsTrue() {
        sample = createBiometricRecord(BiometricType.FACE, Collections.singletonList("Left IndexFinger"));
        service = new ConvertFormatService(env, sample, "ISO19794_5_2011", "ISO19794_5_2011",
                sourceParams, targetParams, modalities);

        boolean result = ReflectionTestUtils.invokeMethod(service, "isValidBioTypeForSourceFormat",
                BiometricType.FACE, "ISO19794_5_2011");

        assertTrue(result);
    }

    /**
     * Tests validation of iris biometric type with iris format.
     * Verifies that the validation returns true.
     */
    @Test
    void isValidBioTypeForSourceFormat_irisWithIrisFormat_returnsTrue() {
        sample = createBiometricRecord(BiometricType.IRIS, Collections.singletonList("Left"));
        service = new ConvertFormatService(env, sample, "ISO19794_6_2011", "ISO19794_6_2011",
                sourceParams, targetParams, modalities);

        boolean result = ReflectionTestUtils.invokeMethod(service, "isValidBioTypeForSourceFormat",
                BiometricType.IRIS, "ISO19794_6_2011");

        assertTrue(result);
    }

    /**
     * Tests validation of finger biometric type with face format.
     * Verifies that the validation returns false.
     */
    @Test
    void isValidBioTypeForSourceFormat_fingerWithFaceFormat_returnsFalse() {
        sample = createBiometricRecord(BiometricType.FINGER, Collections.singletonList("Left IndexFinger"));
        service = new ConvertFormatService(env, sample, "ISO19794_5_2011", "ISO19794_5_2011",
                sourceParams, targetParams, modalities);

        boolean result = ReflectionTestUtils.invokeMethod(service, "isValidBioTypeForSourceFormat",
                BiometricType.FINGER, "ISO19794_5_2011");

        assertFalse(result);
    }

    /**
     * Tests validation with unknown format.
     * Verifies that the validation returns false.
     */
    @Test
    void isValidBioTypeForSourceFormat_unknownFormat_returnsFalse() {
        sample = createBiometricRecord(BiometricType.FINGER, Collections.singletonList("Left IndexFinger"));
        service = new ConvertFormatService(env, sample, "UNKNOWN_FORMAT", "ISO19794_4_2011",
                sourceParams, targetParams, modalities);

        boolean result = ReflectionTestUtils.invokeMethod(service, "isValidBioTypeForSourceFormat",
                BiometricType.FINGER, "UNKNOWN_FORMAT");

        assertFalse(result);
    }

    /**
     * Helper method to create a biometric record with specified type and subtypes.
     *
     * @param type The biometric type
     * @param subtypes The list of subtypes
     * @return A BiometricRecord instance
     */
    private BiometricRecord createBiometricRecord(BiometricType type, List<String> subtypes) {
        BiometricRecord record = new BiometricRecord();
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        BIRInfo birInfo = new BIRInfo();

        ReflectionTestUtils.setField(bdbInfo, "type", Collections.singletonList(type));
        ReflectionTestUtils.setField(bdbInfo, "subtype", subtypes);
        ReflectionTestUtils.setField(bir, "bdbInfo", bdbInfo);
        ReflectionTestUtils.setField(bir, "birInfo", birInfo);
        ReflectionTestUtils.setField(bir, "bdb", "test_data".getBytes());
        ReflectionTestUtils.setField(record, "segments", Collections.singletonList(bir));

        return record;
    }
}