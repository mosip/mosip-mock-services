package io.mosip.mock.sdk.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.mock.sdk.exceptions.SDKException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Test class for CheckQualityService
 * Tests the quality checking functionality for biometric data
 */
@ExtendWith(MockitoExtension.class)
class CheckQualityServiceTest {

    @Mock
    private Environment env;

    @Mock
    private BiometricRecord sample;

    private CheckQualityService service;

    /**
     * Setup method executed before each test
     * Initializes the service with face biometric type
     */
    @BeforeEach
    void setUp() {
        service = new CheckQualityService(env, sample,
                Arrays.asList(BiometricType.FACE), new HashMap<>());
    }

    /**
     * Tests quality check with null BDBInfo
     * Verifies that appropriate error response is returned
     */
    @Test
    void getCheckQualityInfo_nullBDBInfo_returns500Error() {
        BIR bir = new BIR();
        List<BIR> segments = Collections.singletonList(bir);
        when(sample.getSegments()).thenReturn(segments);

        Response<QualityCheck> response = service.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(500, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }

    /**
     * Tests quality check with invalid quality score
     * Verifies handling of negative quality scores
     */
    @Test
    void getCheckQualityInfo_invalidQualityScore_returns404Error() {
        List<BIR> segments = createValidBiometricSegments(BiometricType.FACE, -1L);
        when(sample.getSegments()).thenReturn(segments);

        Response<QualityCheck> response = service.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(404, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }

    /**
     * Tests quality check with mixed valid and invalid segments
     * Verifies handling of partially valid biometric data
     */
    @Test
    void getCheckQualityInfo_mixedValidInvalidSegments_returns404Error() {
        List<BIR> segments = new ArrayList<>();
        segments.addAll(createValidBiometricSegments(BiometricType.FACE, 90L));
        segments.add(new BIR());
        when(sample.getSegments()).thenReturn(segments);

        Response<QualityCheck> response = service.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(404, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }

    /**
     * Helper method to create valid biometric segments for testing
     *
     * @param type BiometricType to set in the segment
     * @param qualityScore Quality score to set
     * @return List containing a single valid BIR segment
     */
    private List<BIR> createValidBiometricSegments(BiometricType type, Long qualityScore) {
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        bdbInfo.setType(Collections.singletonList(type));
        QualityType quality = new QualityType();
        quality.setScore(qualityScore);
        bdbInfo.setQuality(quality);
        bir.setBdbInfo(bdbInfo);
        return Collections.singletonList(bir);
    }

    /**
     * Tests quality check when the biometric type is null
     * This test verifies that the service returns a 500 error response
     * when a biometric segment has a null biometric type
     */
    @Test
    void getCheckQualityInfo_nullBiometricType_returns500Error() {
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        bdbInfo.setType(null);
        QualityType quality = new QualityType();
        quality.setScore(90L);
        bdbInfo.setQuality(quality);
        bir.setBdbInfo(bdbInfo);

        when(sample.getSegments()).thenReturn(Collections.singletonList(bir));

        Response<QualityCheck> response = service.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(500, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }

    /**
     * Tests quality check when the biometric type list is empty
     * This test verifies that the service returns a 500 error response
     * when a biometric segment has an empty biometric type list
     */
    @Test
    void getCheckQualityInfo_emptyBiometricType_returns500Error() {
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        bdbInfo.setType(Collections.emptyList());
        QualityType quality = new QualityType();
        quality.setScore(90L);
        bdbInfo.setQuality(quality);
        bir.setBdbInfo(bdbInfo);

        when(sample.getSegments()).thenReturn(Collections.singletonList(bir));

        Response<QualityCheck> response = service.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(500, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }

    /**
     * Tests quality check with null sample
     * Verifies that missing input error is returned
     */
    @Test
    void getCheckQualityInfo_nullSample_returnsMissingInputError() {
        service = new CheckQualityService(env, null, Arrays.asList(BiometricType.FACE), new HashMap<>());

        Response<QualityCheck> response = service.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(402, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }

    /**
     * Tests quality check with valid face data
     * Verifies successful quality score calculation
     */
    @Test
    void getCheckQualityInfo_validFaceData_returnsSuccessWithScore() {
        BiometricRecord realSample = new BiometricRecord();
        List<BIR> segments = createValidBiometricSegments(BiometricType.FACE, 80L);
        realSample.setSegments(segments);

        service = new CheckQualityService(env, realSample, Arrays.asList(BiometricType.FACE), new HashMap<>());
        CheckQualityService spyService = spy(service);
        doReturn(true).when(spyService).isValidBirData(any());

        Response<QualityCheck> response = spyService.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse()),
                () -> assertEquals(80.0f, response.getResponse().getScores().get(BiometricType.FACE).getScore())
        );
    }

    /**
     * Tests quality check with valid finger data
     * Verifies successful quality score calculation for finger modality
     */
    @Test
    void getCheckQualityInfo_validFingerData_returnsSuccessWithScore() {
        BiometricRecord realSample = new BiometricRecord();
        List<BIR> segments = createValidBiometricSegments(BiometricType.FINGER, 75L);
        realSample.setSegments(segments);

        service = new CheckQualityService(env, realSample, Arrays.asList(BiometricType.FINGER), new HashMap<>());
        CheckQualityService spyService = spy(service);
        doReturn(true).when(spyService).isValidBirData(any());

        Response<QualityCheck> response = spyService.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse()),
                () -> assertEquals(75.0f, response.getResponse().getScores().get(BiometricType.FINGER).getScore())
        );
    }

    /**
     * Tests quality check with valid iris data
     * Verifies successful quality score calculation for iris modality
     */
    @Test
    void getCheckQualityInfo_validIrisData_returnsSuccessWithScore() {
        BiometricRecord realSample = new BiometricRecord();
        List<BIR> segments = createValidBiometricSegments(BiometricType.IRIS, 90L);
        realSample.setSegments(segments);

        service = new CheckQualityService(env, realSample, Arrays.asList(BiometricType.IRIS), new HashMap<>());
        CheckQualityService spyService = spy(service);
        doReturn(true).when(spyService).isValidBirData(any());

        Response<QualityCheck> response = spyService.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse()),
                () -> assertEquals(90.0f, response.getResponse().getScores().get(BiometricType.IRIS).getScore())
        );
    }

    /**
     * Tests quality check with unsupported modality
     * Verifies error handling for unsupported biometric types
     */
    @Test
    void getCheckQualityInfo_unsupportedModality_returnsErrorInScore() {
        BiometricRecord realSample = new BiometricRecord();
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        bdbInfo.setType(Collections.singletonList(BiometricType.VOICE));
        QualityType quality = new QualityType();
        quality.setScore(85L);
        bdbInfo.setQuality(quality);
        bir.setBdbInfo(bdbInfo);
        realSample.setSegments(Collections.singletonList(bir));

        service = new CheckQualityService(env, realSample, Arrays.asList(BiometricType.VOICE), new HashMap<>());
        CheckQualityService spyService = spy(service);
        doReturn(true).when(spyService).isValidBirData(any());

        Response<QualityCheck> response = spyService.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse()),
                () -> assertEquals(0.0f, response.getResponse().getScores().get(BiometricType.VOICE).getScore()),
                () -> assertTrue(response.getResponse().getScores().get(BiometricType.VOICE).getErrors().contains("Modality VOICE is not supported"))
        );
    }

    /**
     * Tests quality check with multiple segments of same type
     * Verifies average score calculation
     */
    @Test
    void getCheckQualityInfo_multipleSegmentsSameType_returnsAverageScore() {
        BiometricRecord realSample = new BiometricRecord();

        BIR bir1 = new BIR();
        BDBInfo bdbInfo1 = new BDBInfo();
        bdbInfo1.setType(Collections.singletonList(BiometricType.FINGER));
        QualityType quality1 = new QualityType();
        quality1.setScore(70L);
        bdbInfo1.setQuality(quality1);
        bir1.setBdbInfo(bdbInfo1);

        BIR bir2 = new BIR();
        BDBInfo bdbInfo2 = new BDBInfo();
        bdbInfo2.setType(Collections.singletonList(BiometricType.FINGER));
        QualityType quality2 = new QualityType();
        quality2.setScore(80L);
        bdbInfo2.setQuality(quality2);
        bir2.setBdbInfo(bdbInfo2);

        realSample.setSegments(Arrays.asList(bir1, bir2));

        service = new CheckQualityService(env, realSample, Arrays.asList(BiometricType.FINGER), new HashMap<>());
        CheckQualityService spyService = spy(service);
        doReturn(true).when(spyService).isValidBirData(any());

        Response<QualityCheck> response = spyService.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(200, response.getStatusCode()),
                () -> assertNotNull(response.getResponse()),
                () -> assertEquals(75.0f, response.getResponse().getScores().get(BiometricType.FINGER).getScore())
        );
    }

    /**
     * Tests SDK exception handling for invalid input
     */
    @Test
    void getCheckQualityInfo_sdkExceptionInvalidInput_returnsInvalidInputError() {
        BiometricRecord realSample = new BiometricRecord();
        List<BIR> segments = createValidBiometricSegments(BiometricType.FACE, 80L);
        realSample.setSegments(segments);

        service = new CheckQualityService(env, realSample, Arrays.asList(BiometricType.FACE), new HashMap<>());
        CheckQualityService spyService = spy(service);
        doThrow(new SDKException("401", "Invalid input")).when(spyService).isValidBirData(any());

        Response<QualityCheck> response = spyService.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(401, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }
}
