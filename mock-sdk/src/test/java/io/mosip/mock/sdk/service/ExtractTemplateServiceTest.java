package io.mosip.mock.sdk.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
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
 * Test class for ExtractTemplateService with 100% line coverage.
 */
@ExtendWith(MockitoExtension.class)
class ExtractTemplateServiceTest {

    @Mock
    private Environment env;

    private ExtractTemplateService service;
    private BiometricRecord sample;
    private List<BiometricType> modalities;
    private Map<String, String> flags;

    /**
     * Sets up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        modalities = Arrays.asList(BiometricType.FINGER, BiometricType.FACE, BiometricType.IRIS);
        flags = new HashMap<>();
        sample = createValidBiometricRecord();
        service = new ExtractTemplateService(env, sample, modalities, flags);
    }

    /**
     * Tests template extraction with null sample.
     */
    @Test
    void getExtractTemplateInfo_nullSample_returnsMissingInputError() {
        service = new ExtractTemplateService(env, null, modalities, flags);

        Response<BiometricRecord> response = service.getExtractTemplateInfo();

        assertEquals(402, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests template extraction with null segments.
     */
    @Test
    void getExtractTemplateInfo_nullSegments_returnsMissingInputError() {
        sample.setSegments(null);
        service = new ExtractTemplateService(env, sample, modalities, flags);

        Response<BiometricRecord> response = service.getExtractTemplateInfo();

        assertEquals(402, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests template extraction with empty segments.
     */
    @Test
    void getExtractTemplateInfo_emptySegments_returnsMissingInputError() {
        sample.setSegments(new ArrayList<>());
        service = new ExtractTemplateService(env, sample, modalities, flags);

        Response<BiometricRecord> response = service.getExtractTemplateInfo();

        assertEquals(402, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with invalid input error code.
     */
    @Test
    void getExtractTemplateInfo_sdkExceptionInvalidInput_returnsInvalidInputError() {
        ExtractTemplateService spyService = spy(service);
        doThrow(new SDKException("401", "Invalid input")).when(spyService).isValidBirData(any());

        Response<BiometricRecord> response = spyService.getExtractTemplateInfo();

        assertEquals(401, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with quality check failed error code.
     */
    @Test
    void getExtractTemplateInfo_sdkExceptionQualityCheckFailed_returnsQualityCheckFailedError() {
        ExtractTemplateService spyService = spy(service);
        doThrow(new SDKException("403", "Quality check failed")).when(spyService).isValidBirData(any());

        Response<BiometricRecord> response = spyService.getExtractTemplateInfo();

        assertEquals(403, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with biometric not found error code.
     */
    @Test
    void getExtractTemplateInfo_sdkExceptionBiometricNotFound_returnsBiometricNotFoundError() {
        ExtractTemplateService spyService = spy(service);
        doThrow(new SDKException("404", "Biometric not found")).when(spyService).isValidBirData(any());

        Response<BiometricRecord> response = spyService.getExtractTemplateInfo();

        assertEquals(404, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with matching failed error code.
     */
    @Test
    void getExtractTemplateInfo_sdkExceptionMatchingFailed_returnsMatchingFailedError() {
        ExtractTemplateService spyService = spy(service);
        doThrow(new SDKException("405", "Matching failed")).when(spyService).isValidBirData(any());

        Response<BiometricRecord> response = spyService.getExtractTemplateInfo();

        assertEquals(405, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with poor data quality error code.
     */
    @Test
    void getExtractTemplateInfo_sdkExceptionPoorDataQuality_returnsPoorDataQualityError() {
        ExtractTemplateService spyService = spy(service);
        doThrow(new SDKException("406", "Poor data quality")).when(spyService).isValidBirData(any());

        Response<BiometricRecord> response = spyService.getExtractTemplateInfo();

        assertEquals(406, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests SDK exception with unknown error code.
     */
    @Test
    void getExtractTemplateInfo_sdkExceptionUnknownError_returnsUnknownError() {
        ExtractTemplateService spyService = spy(service);
        doThrow(new SDKException("999", "Unknown error")).when(spyService).isValidBirData(any());

        Response<BiometricRecord> response = spyService.getExtractTemplateInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests general exception handling.
     */
    @Test
    void getExtractTemplateInfo_generalException_returnsUnknownError() {
        ExtractTemplateService spyService = spy(service);
        doThrow(new RuntimeException("General error")).when(spyService).isValidBirData(any());

        Response<BiometricRecord> response = spyService.getExtractTemplateInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests handling of null format.
     */
    @Test
    void getExtractTemplateInfo_nullFormat_returnsUnknownError() {
        BIR bir = createBIRWithNullFormat();
        sample.setSegments(Collections.singletonList(bir));
        service = new ExtractTemplateService(env, sample, modalities, flags);

        Response<BiometricRecord> response = service.getExtractTemplateInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests handling of null BDBInfo.
     */
    @Test
    void getExtractTemplateInfo_nullBDBInfo_returnsUnknownError() {
        BIR bir = createBIRWithNullBDBInfo();
        sample.setSegments(Collections.singletonList(bir));
        service = new ExtractTemplateService(env, sample, modalities, flags);

        Response<BiometricRecord> response = service.getExtractTemplateInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests handling of null format type.
     */
    @Test
    void getExtractTemplateInfo_nullFormatType_returnsUnknownError() {
        BIR bir = createBIRWithNullFormatType();
        sample.setSegments(Collections.singletonList(bir));
        service = new ExtractTemplateService(env, sample, modalities, flags);

        Response<BiometricRecord> response = service.getExtractTemplateInfo();

        assertEquals(500, response.getStatusCode());
        assertNull(response.getResponse());
    }

    /**
     * Tests invalid BIR data breaks the loop.
     */
    @Test
    void getExtractTemplateInfo_invalidBirData_breaksLoop() {
        ExtractTemplateService spyService = spy(service);
        doReturn(false).when(spyService).isValidBirData(any());

        Response<BiometricRecord> response = spyService.getExtractTemplateInfo();

        assertEquals(200, response.getStatusCode());
    }

    /**
     * Tests random level type generation.
     */
    @Test
    void getRandomLevelType_returnsValidProcessedLevelType() {
        ProcessedLevelType levelType = service.getRandomLevelType();

        assertTrue(levelType == ProcessedLevelType.INTERMEDIATE || levelType == ProcessedLevelType.PROCESSED);
    }

    /**
     * Creates a valid biometric record for testing.
     */
    private BiometricRecord createValidBiometricRecord() {
        BiometricRecord record = new BiometricRecord();
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();

        ReflectionTestUtils.setField(bdbInfo, "type", Collections.singletonList(BiometricType.FINGER));
        ReflectionTestUtils.setField(bdbInfo, "subtype", Collections.singletonList("Left IndexFinger"));
        ReflectionTestUtils.setField(bir, "bdbInfo", bdbInfo);
        ReflectionTestUtils.setField(bir, "bdb", "test_data".getBytes());
        ReflectionTestUtils.setField(record, "segments", Collections.singletonList(bir));

        return record;
    }

    /**
     * Creates a BIR with specific format type.
     */
    private BIR createBIRWithFormat(String formatType) {
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        RegistryIDType format = new RegistryIDType();

        ReflectionTestUtils.setField(format, "type", formatType);
        ReflectionTestUtils.setField(bdbInfo, "format", format);
        ReflectionTestUtils.setField(bdbInfo, "type", Collections.singletonList(BiometricType.FINGER));
        ReflectionTestUtils.setField(bdbInfo, "subtype", Collections.singletonList("Left IndexFinger"));
        ReflectionTestUtils.setField(bir, "bdbInfo", bdbInfo);
        ReflectionTestUtils.setField(bir, "bdb", "test_data".getBytes());

        return bir;
    }

    /**
     * Creates a BIR with null format.
     */
    private BIR createBIRWithNullFormat() {
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();

        ReflectionTestUtils.setField(bdbInfo, "format", null);
        ReflectionTestUtils.setField(bdbInfo, "type", Collections.singletonList(BiometricType.FINGER));
        ReflectionTestUtils.setField(bir, "bdbInfo", bdbInfo);
        ReflectionTestUtils.setField(bir, "bdb", "test_data".getBytes());

        return bir;
    }

    /**
     * Creates a BIR with null BDBInfo.
     */
    private BIR createBIRWithNullBDBInfo() {
        BIR bir = new BIR();
        ReflectionTestUtils.setField(bir, "bdbInfo", null);
        ReflectionTestUtils.setField(bir, "bdb", "test_data".getBytes());
        return bir;
    }

    /**
     * Creates a BIR with null format type.
     */
    private BIR createBIRWithNullFormatType() {
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        RegistryIDType format = new RegistryIDType();

        ReflectionTestUtils.setField(format, "type", null);
        ReflectionTestUtils.setField(bdbInfo, "format", format);
        ReflectionTestUtils.setField(bdbInfo, "type", Collections.singletonList(BiometricType.FINGER));
        ReflectionTestUtils.setField(bir, "bdbInfo", bdbInfo);
        ReflectionTestUtils.setField(bir, "bdb", "test_data".getBytes());

        return bir;
    }
}
