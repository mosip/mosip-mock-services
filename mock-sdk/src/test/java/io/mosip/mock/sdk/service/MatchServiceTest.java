package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doThrow;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIRInfo;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Decision;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.mock.sdk.exceptions.SDKException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

/**
 * Test class for MatchService with 100% line coverage.
 */
@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private Environment env;

    private MatchService service;
    private BiometricRecord sample;
    private BiometricRecord[] gallery;
    private List<BiometricType> modalities;
    private Map<String, String> flags;

    /**
     * Sets up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        modalities = Arrays.asList(BiometricType.FINGER, BiometricType.FACE, BiometricType.IRIS);
        flags = new HashMap<>();
        sample = createBiometricRecord(BiometricType.FINGER, "Left IndexFinger", "sample_data");
        gallery = new BiometricRecord[]{
                createBiometricRecord(BiometricType.FINGER, "Left IndexFinger", "sample_data")
        };
        service = new MatchService(env, sample, gallery, modalities, flags);
    }

    /**
     * Tests matching with invalid biometric data.
     */
    @Test
    void getMatchDecisionInfo_invalidBiometricData_returnsInvalidInputError() {
        Response<MatchDecision[]> response = service.getMatchDecisionInfo();

        assertEquals(401, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests null pointer exception handling during getBioSegmentMap.
     */
    @Test
    void getMatchDecisionInfo_nullPointerException_returnsUnknownError() {
        sample = createBiometricRecordWithNullBdbInfo();
        service = new MatchService(env, sample, gallery, modalities, flags);

        Response<MatchDecision[]> response = service.getMatchDecisionInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with invalid input error code.
     */
    @Test
    void getMatchDecisionInfo_sdkExceptionInvalidInput_returnsInvalidInputError() {
        MatchService spyService = spy(service);
        doThrow(new SDKException("401", "Invalid input")).when(spyService).getBioSegmentMap(any(), any());

        Response<MatchDecision[]> response = spyService.getMatchDecisionInfo();

        assertEquals(401, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with missing input error code.
     */
    @Test
    void getMatchDecisionInfo_sdkExceptionMissingInput_returnsMissingInputError() {
        MatchService spyService = spy(service);
        doThrow(new SDKException("402", "Missing input")).when(spyService).getBioSegmentMap(any(), any());

        Response<MatchDecision[]> response = spyService.getMatchDecisionInfo();

        assertEquals(402, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with quality check failed error code.
     */
    @Test
    void getMatchDecisionInfo_sdkExceptionQualityCheckFailed_returnsQualityCheckFailedError() {
        MatchService spyService = spy(service);
        doThrow(new SDKException("403", "Quality check failed")).when(spyService).getBioSegmentMap(any(), any());

        Response<MatchDecision[]> response = spyService.getMatchDecisionInfo();

        assertEquals(403, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with biometric not found error code.
     */
    @Test
    void getMatchDecisionInfo_sdkExceptionBiometricNotFound_returnsBiometricNotFoundError() {
        MatchService spyService = spy(service);
        doThrow(new SDKException("404", "Biometric not found")).when(spyService).getBioSegmentMap(any(), any());

        Response<MatchDecision[]> response = spyService.getMatchDecisionInfo();

        assertEquals(404, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with matching failed error code.
     */
    @Test
    void getMatchDecisionInfo_sdkExceptionMatchingFailed_returnsMatchingFailedError() {
        MatchService spyService = spy(service);
        doThrow(new SDKException("405", "Matching failed")).when(spyService).getBioSegmentMap(any(), any());

        Response<MatchDecision[]> response = spyService.getMatchDecisionInfo();

        assertEquals(405, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with poor data quality error code.
     */
    @Test
    void getMatchDecisionInfo_sdkExceptionPoorDataQuality_returnsPoorDataQualityError() {
        MatchService spyService = spy(service);
        doThrow(new SDKException("406", "Poor data quality")).when(spyService).getBioSegmentMap(any(), any());

        Response<MatchDecision[]> response = spyService.getMatchDecisionInfo();

        assertEquals(406, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with unknown error code.
     */
    @Test
    void getMatchDecisionInfo_sdkExceptionUnknownError_returnsUnknownError() {
        MatchService spyService = spy(service);
        doThrow(new SDKException("999", "Unknown error")).when(spyService).getBioSegmentMap(any(), any());

        Response<MatchDecision[]> response = spyService.getMatchDecisionInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests general exception handling.
     */
    @Test
    void getMatchDecisionInfo_generalException_returnsUnknownError() {
        MatchService spyService = spy(service);
        doThrow(new RuntimeException("General error")).when(spyService).getBioSegmentMap(any(), any());

        Response<MatchDecision[]> response = spyService.getMatchDecisionInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests fingerprint comparison with empty subtype.
     */
    @Test
    void compareFingerprints_emptySubtype_returnsMissingInputError() {
        sample = createBiometricRecord(BiometricType.FINGER, "", "sample_data");
        gallery[0] = createBiometricRecord(BiometricType.FINGER, "", "sample_data");
        service = new MatchService(env, sample, gallery, modalities, flags);

        Response<MatchDecision[]> response = service.getMatchDecisionInfo();

        assertEquals(402, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests validation when gallery segments are empty.
     */
    @Test
    void validateSegments_galleryEmpty_returnsInvalidInputError() {
        sample = createBiometricRecord(BiometricType.FINGER, "Left IndexFinger", "data");
        gallery[0] = createBiometricRecordWithEmptySegments();
        service = new MatchService(env, sample, gallery, modalities, flags);

        Response<MatchDecision[]> response = service.getMatchDecisionInfo();

        assertEquals(401, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests setMatchDecisions method with all matched results.
     */
    @Test
    void setMatchDecisions_allMatched_returnsMatched() {
        Decision decision = new Decision();
        List<Boolean> matched = Arrays.asList(true, true, true);

        ReflectionTestUtils.invokeMethod(service, "setMatchDecisions", decision, matched);

        assertEquals(Match.MATCHED, decision.getMatch());
    }

    /**
     * Tests setMatchDecisions method with some not matched results.
     */
    @Test
    void setMatchDecisions_someNotMatched_returnsNotMatched() {
        Decision decision = new Decision();
        List<Boolean> matched = Arrays.asList(true, false, true);

        ReflectionTestUtils.invokeMethod(service, "setMatchDecisions", decision, matched);

        assertEquals(Match.NOT_MATCHED, decision.getMatch());
    }

    /**
     * Tests setMatchDecisions method with empty match list.
     */
    @Test
    void setMatchDecisions_emptyList_returnsError() {
        Decision decision = new Decision();
        List<Boolean> matched = new ArrayList<>();

        ReflectionTestUtils.invokeMethod(service, "setMatchDecisions", decision, matched);

        assertEquals(Match.ERROR, decision.getMatch());
    }

    /**
     * Tests validation when both sample and gallery segments are null.
     */
    @Test
    void validateSegments_bothNull_returnsMatched() {
        Decision decision = ReflectionTestUtils.invokeMethod(service, "vaildateSegments", null, null, "FINGER");
        assertEquals(Match.MATCHED, decision.getMatch());
    }

    /**
     * Tests validation when sample segments are null but gallery has data.
     */
    @Test
    void validateSegments_sampleNull_returnsNotMatched() {
        List<BIR> gallerySegments = Collections.singletonList(new BIR());
        Decision decision = ReflectionTestUtils.invokeMethod(service, "vaildateSegments", null, gallerySegments, "FINGER");
        assertEquals(Match.NOT_MATCHED, decision.getMatch());
    }

    /**
     * Tests validation when sample segments are empty.
     */
    @Test
    void validateSegments_sampleEmpty_returnsNotMatched() {
        List<BIR> sampleSegments = Collections.emptyList();
        List<BIR> gallerySegments = Collections.singletonList(new BIR());
        Decision decision = ReflectionTestUtils.invokeMethod(service, "vaildateSegments", sampleSegments, gallerySegments, "FINGER");
        assertEquals(Match.NOT_MATCHED, decision.getMatch());
    }

    /**
     * Tests compareModality method with unsupported modality.
     */
    @Test
    void compareModality_unsupportedModality_returnsError() {
        List<BIR> sampleSegments = Collections.singletonList(new BIR());
        List<BIR> gallerySegments = Collections.singletonList(new BIR());

        Decision decision = ReflectionTestUtils.invokeMethod(service, "compareModality", BiometricType.VOICE, sampleSegments, gallerySegments);

        assertEquals(Match.ERROR, decision.getMatch());
        assertNotNull(decision.getAnalyticsInfo());
        assertTrue(decision.getAnalyticsInfo().containsKey("errors"));
    }

    /**
     * Creates a biometric record with specified type, subtype, and data.
     */
    private BiometricRecord createBiometricRecord(BiometricType type, String subtype, String data) {
        BiometricRecord record = new BiometricRecord();
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        BIRInfo birInfo = new BIRInfo();

        ReflectionTestUtils.setField(bdbInfo, "type", Collections.singletonList(type));
        ReflectionTestUtils.setField(bdbInfo, "subtype", subtype != null ? Collections.singletonList(subtype) : null);
        ReflectionTestUtils.setField(bir, "bdbInfo", bdbInfo);
        ReflectionTestUtils.setField(bir, "birInfo", birInfo);
        ReflectionTestUtils.setField(bir, "bdb", data.getBytes());
        ReflectionTestUtils.setField(record, "segments", Collections.singletonList(bir));

        return record;
    }

    /**
     * Creates a biometric record with null BDBInfo to trigger null pointer exception.
     */
    private BiometricRecord createBiometricRecordWithNullBdbInfo() {
        BiometricRecord record = new BiometricRecord();
        BIR bir = new BIR();
        ReflectionTestUtils.setField(bir, "bdbInfo", null);
        ReflectionTestUtils.setField(record, "segments", Collections.singletonList(bir));
        return record;
    }

    /**
     * Creates a biometric record with empty segments list.
     */
    private BiometricRecord createBiometricRecordWithEmptySegments() {
        BiometricRecord record = new BiometricRecord();
        ReflectionTestUtils.setField(record, "segments", Collections.emptyList());
        return record;
    }
}
