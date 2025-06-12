package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.mockito.Mockito.when;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
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

}