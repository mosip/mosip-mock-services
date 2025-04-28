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

@ExtendWith(MockitoExtension.class)
class CheckQualityServiceTest {

    @Mock
    private Environment env;

    @Mock
    private BiometricRecord sample;

    private CheckQualityService service;

    @BeforeEach
    void setUp() {
        service = new CheckQualityService(env, sample,
                Arrays.asList(BiometricType.FACE), new HashMap<>());
    }

    @Test
    void testGetCheckQualityInfo_NullBDBInfo() {
        BIR bir = new BIR();  // BDBInfo is null
        List<BIR> segments = Collections.singletonList(bir);
        when(sample.getSegments()).thenReturn(segments);

        Response<QualityCheck> response = service.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(500, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }

    @Test
    void testGetCheckQualityInfo_InvalidQualityScore() {
        List<BIR> segments = createValidBiometricSegments(BiometricType.FACE, -1L);
        when(sample.getSegments()).thenReturn(segments);

        Response<QualityCheck> response = service.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(404, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }

    @Test
    void testGetCheckQualityInfo_MixedValidInvalidSegments() {
        List<BIR> segments = new ArrayList<>();
        segments.addAll(createValidBiometricSegments(BiometricType.FACE, 90L));
        segments.add(new BIR()); // Invalid segment
        when(sample.getSegments()).thenReturn(segments);

        Response<QualityCheck> response = service.getCheckQualityInfo();

        assertAll(
                () -> assertEquals(404, response.getStatusCode()),
                () -> assertNull(response.getResponse())
        );
    }

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
}