package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.*;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 * Test class for CheckQualityService
 * Tests the quality checking functionality for biometric data
 */
@ExtendWith(MockitoExtension.class)
class CheckQualityServiceTest {

    // Mock dependencies
    @Mock
    private Environment env;

    @Mock
    private BiometricRecord sample;

    // Service under test
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
    void testGetCheckQualityInfo_NullBDBInfo() {
        // Create BIR with null BDBInfo
        BIR bir = new BIR();  // BDBInfo is null
        List<BIR> segments = Collections.singletonList(bir);
        when(sample.getSegments()).thenReturn(segments);

        // Execute quality check
        Response<QualityCheck> response = service.getCheckQualityInfo();

        // Verify error response
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
    void testGetCheckQualityInfo_InvalidQualityScore() {
        // Create segments with invalid quality score (-1)
        List<BIR> segments = createValidBiometricSegments(BiometricType.FACE, -1L);
        when(sample.getSegments()).thenReturn(segments);

        // Execute quality check
        Response<QualityCheck> response = service.getCheckQualityInfo();

        // Verify error response for invalid score
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
    void testGetCheckQualityInfo_MixedValidInvalidSegments() {
        // Create mix of valid and invalid segments
        List<BIR> segments = new ArrayList<>();
        segments.addAll(createValidBiometricSegments(BiometricType.FACE, 90L));
        segments.add(new BIR()); // Add invalid segment
        when(sample.getSegments()).thenReturn(segments);

        // Execute quality check
        Response<QualityCheck> response = service.getCheckQualityInfo();

        // Verify error response for mixed segments
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
     * Tests quality check when the biometric type is null.
     * This test verifies that the service returns a 500 error response
     * when a biometric segment has a null biometric type.
     */
    @Test
    void testGetCheckQualityInfo_NullBiometricType() {
        // Create segment with null biometric type
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
     * Tests quality check when the biometric type list is empty.
     * This test verifies that the service returns a 500 error response
     * when a biometric segment has an empty biometric type list.
     */
    @Test
    void testGetCheckQualityInfo_EmptyBiometricType() {
        // Create segment with empty biometric type list
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