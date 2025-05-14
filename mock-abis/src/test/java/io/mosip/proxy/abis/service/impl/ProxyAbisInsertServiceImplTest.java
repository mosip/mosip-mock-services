package io.mosip.proxy.abis.service.impl;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.mosip.proxy.abis.dto.IdentityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.proxy.abis.constant.FailureReasonsConstants;
import io.mosip.proxy.abis.dao.ProxyAbisBioDataRepository;
import io.mosip.proxy.abis.dao.ProxyAbisInsertRepository;
import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.dto.IdentifyDelayResponse;
import io.mosip.proxy.abis.dto.IdentityResponse;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.entity.BiometricData;
import io.mosip.proxy.abis.entity.InsertEntity;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.service.ExpectationCache;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
import io.mosip.proxy.abis.utility.CryptoCoreUtil;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.constant.BiometricType;
import org.apache.commons.io.FileUtils;

@ExtendWith(MockitoExtension.class)
class ProxyAbisInsertServiceImplTest {

    @Mock
    private ProxyAbisInsertRepository proxyabis;

    @Mock
    private ProxyAbisBioDataRepository proxyAbisBioDataRepository;

    @Mock
    private ProxyAbisConfigService proxyAbisConfigService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CryptoCoreUtil cryptoUtil;

    @Mock
    private Environment env;

    @Mock
    private ExpectationCache expectationCache;

    @InjectMocks
    private ProxyAbisInsertServiceImpl proxyAbisInsertService;

    private InsertRequestMO insertRequest;
    private InsertEntity insertEntity;
    private IdentityRequest identityRequest;
    private List<BiometricData> biometricDataList;
    private ResponseEntity<String> cbeffResponse;
    private String cbeffData;

    /**
     * Setup method executed before each test.
     * Initializes all the mock objects and test data required for the tests.
     */
    @BeforeEach
    void setUp() {
        // Initialize test data
        insertRequest = new InsertRequestMO();
        insertRequest.setId("test-id");
        insertRequest.setVersion("1.0");
        insertRequest.setRequestId("test-request-id");
        OffsetDateTime.parse("2023-01-01T12:00:00.000Z").toLocalDateTime();
        insertRequest.setReferenceId("test-reference-id");
        insertRequest.setReferenceURL("http://test-url.com/cbeff");

        insertEntity = new InsertEntity(
                insertRequest.getId(),
                insertRequest.getVersion(),
                insertRequest.getRequestId(),
                insertRequest.getRequesttime(),
                insertRequest.getReferenceId()
        );

        identityRequest = new IdentityRequest();
        identityRequest.setId("test-id");
        identityRequest.setVersion("1.0");
        identityRequest.setRequestId("test-request-id");
        identityRequest.setRequesttime(OffsetDateTime.parse("2023-01-01T12:00:00.000Z").toLocalDateTime());
        identityRequest.setReferenceId("test-reference-id");

        biometricDataList = new ArrayList<>();
        BiometricData biometricData = new BiometricData();
        biometricData.setId(123L);
        biometricData.setSubtype("[LEFT]");
        biometricData.setBioData("hash-value");
        biometricData.setInsertEntity(insertEntity);
        biometricDataList.add(biometricData);

        insertEntity = new InsertEntity();
        insertEntity.setId("test-id");
        insertEntity.setReferenceId("test-reference-id");

        cbeffData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><BIR>...</BIR>";
        cbeffResponse = ResponseEntity.ok(cbeffData);

        // Inject the restTemplate mock into proxyAbisInsertService
        ReflectionTestUtils.setField(proxyAbisInsertService, "restTemplate", restTemplate);
    }

    /**
     * Tests the insertion of biometric data when reference ID already exists.
     * Verifies that the method throws RequestException with the correct failure reason.
     */
    @Test
    void testInsertData_ReferenceIdAlreadyExists() {
        // Arrange
        when(proxyabis.findById(insertRequest.getReferenceId())).thenReturn(Optional.of(insertEntity));

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(insertRequest);
        });

        assertEquals(FailureReasonsConstants.REFERENCEID_ALREADY_EXISTS, exception.getReasonConstant());
    }

    /**
     * Tests the deletion of biometric data with valid input.
     * Verifies that the delete method is called with the correct reference ID.
     */
    @Test
    void testDeleteData_Success() {
        // Arrange
        String referenceId = "test-reference-id";
        doNothing().when(proxyabis).deleteById(referenceId);

        // Act
        proxyAbisInsertService.deleteData(referenceId);

        // Assert
        verify(proxyabis).deleteById(referenceId);
    }

    /**
     * Tests the deletion of biometric data when an exception occurs.
     * Verifies that the method throws RequestException when the deletion fails.
     */
    @Test
    void testDeleteData_Exception() {
        // Arrange
        String referenceId = "test-reference-id";
        doThrow(new RuntimeException("Database error")).when(proxyabis).deleteById(referenceId);

        // Act & Assert
        assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.deleteData(referenceId);
        });
    }

    /**
     * Tests the duplication check with no gallery reference IDs.
     * Verifies that the method correctly searches for duplicates in the entire database.
     */
    @Test
    void testFindDuplication_NoGallery() {
        // Arrange
        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(new ArrayList<>());
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);
        when(proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(anyString())).thenReturn(biometricDataList);

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getDelayResponse());
        assertNotNull(response.getIdentityResponse());
        assertEquals("1", response.getIdentityResponse().getReturnValue());
        assertEquals("1", response.getIdentityResponse().getCandidateList().getCount());
    }

    /**
     * Tests the duplication check with gallery reference IDs.
     * Verifies that the method correctly searches for duplicates only within the gallery.
     */
    @Test
    void testFindDuplication_WithGallery() {
        // Arrange
        IdentityRequest.Gallery gallery = new IdentityRequest.Gallery();
        List<IdentityRequest.ReferenceIds> referenceIds = new ArrayList<>();
        IdentityRequest.ReferenceIds refId = new IdentityRequest.ReferenceIds();
        refId.setReferenceId("gallery-ref-id");
        referenceIds.add(refId);
        gallery.setReferenceIds(referenceIds);
        identityRequest.setGallery(gallery);

        List<String> galleryReferenceIds = new ArrayList<>();
        galleryReferenceIds.add("gallery-ref-id");

        when(proxyabis.fetchCountForReferenceIdPresentInGallery(anyList())).thenReturn(1);
        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(new ArrayList<>());
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);
        when(proxyAbisBioDataRepository.fetchDuplicatesForReferenceIdBasedOnGalleryIds(anyString(), anyList()))
                .thenReturn(biometricDataList);

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getDelayResponse());
        assertNotNull(response.getIdentityResponse());
        assertEquals("1", response.getIdentityResponse().getReturnValue());
        assertEquals("1", response.getIdentityResponse().getCandidateList().getCount());
    }

    /**
     * Tests the duplication check with expectation cache hit.
     * Verifies that the method correctly processes expectations and returns appropriate response.
     */
    @Test
    void testFindDuplication_WithExpectation() {
        // Arrange
        List<String> bioValues = new ArrayList<>();
        bioValues.add("hash-value");

        Expectation expectation = new Expectation();
        expectation.setId("test-expectation");
        expectation.setActionToInterfere("Identify");
        expectation.setDelayInExecution("5000");
        expectation.setForcedResponse("Duplicate");

        Expectation.Gallery expGallery = new Expectation.Gallery();
        List<Expectation.ReferenceIds> expRefIds = new ArrayList<>();
        Expectation.ReferenceIds expRefId = new Expectation.ReferenceIds();
        expRefId.setReferenceId("exp-ref-id");
        expRefIds.add(expRefId);
        expGallery.setReferenceIds(expRefIds);
        expectation.setGallery(expGallery);

        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(bioValues);
        when(expectationCache.get(anyString())).thenReturn(expectation);
        when(proxyAbisBioDataRepository.fetchReferenceId(anyString())).thenReturn(List.of("exp-ref-id"));

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        assertEquals(5000, response.getDelayResponse());
        assertNotNull(response.getIdentityResponse());
        assertEquals("1", response.getIdentityResponse().getReturnValue());
    }

    /**
     * Tests the duplication check when no duplicates are found.
     * Verifies that the method returns an empty candidate list.
     */
    @Test
    void testFindDuplication_NoDuplicates() {
        // Arrange
        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(new ArrayList<>());
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);
        when(proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(anyString())).thenReturn(new ArrayList<>());

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getDelayResponse());
        assertNotNull(response.getIdentityResponse());
        assertEquals("1", response.getIdentityResponse().getReturnValue());
        assertEquals("0", response.getIdentityResponse().getCandidateList().getCount());
    }

    /**
     * Tests the retrieval of analytics data from environment properties.
     * Verifies that the analytics properties are correctly retrieved and set.
     */
    @Test
    void testGetAnalytics() {
        // This is a private method, but we can test it through its usage in findDuplication

        // Arrange
        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(new ArrayList<>());
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);
        when(proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(anyString())).thenReturn(biometricDataList);

        when(env.getProperty("analytics.confidence")).thenReturn("90");
        when(env.getProperty("analytics.internalscore")).thenReturn("85");
        when(env.getProperty("analytics.key1")).thenReturn("value1");
        when(env.getProperty("analytics.key2")).thenReturn("value2");

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        IdentityResponse.Analytics analytics = response.getIdentityResponse().getCandidateList().getCandidates().getFirst().getAnalytics();
        assertNotNull(analytics);
        assertEquals("90", analytics.getConfidence());
        assertEquals("85", analytics.getInternalScore());
        assertEquals("value1", analytics.getKey1());
        assertEquals("value2", analytics.getKey2());
    }

    /**
     * Tests that the insertData method throws a RequestException when a null request is provided.
     */
    @Test
    void testInsertData_NullRequest() {
        // Act & Assert
        assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(null);
        });
    }

    /**
     * Tests that when an empty reference ID is provided in the insert request,
     * the insertData method throws a RequestException with the expected failure code.
     */
    @Test
    void testInsertData_EmptyReferenceId() {
        // Arrange
        insertRequest.setReferenceId("");

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(insertRequest);
        });
        assertEquals("1", exception.getReasonConstant());
    }

    /**
     * Tests that the insertData method throws a RequestException when a network error occurs
     * while attempting to fetch CB\-eff XML data.
     */
    @Test
    void testInsertData_CbeffFetchError() {
        // Arrange
        when(proxyabis.findById(insertRequest.getReferenceId())).thenReturn(Optional.empty());
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(insertRequest);
        });
    }

    /**
     * Tests the findDuplication method when duplicate check is disabled.
     * Verifies that no duplicate search is performed and the candidate list count is zero.
     */
    @Test
    void testFindDuplication_DuplicateCheckDisabled() {
        // Arrange
        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(new ArrayList<>());
        when(proxyAbisConfigService.getDuplicate()).thenReturn(false);

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        assertEquals("0", response.getIdentityResponse().getCandidateList().getCount());
        verify(proxyAbisBioDataRepository, never()).fetchDuplicatesForReferenceId(anyString());
    }

    /**
     * Tests the findDuplication method when the gallery reference count is invalid.
     * Verifies that a RequestException with the failure constant REFERENCEID_NOT_FOUND is thrown.
     */
    @Test
    void testFindDuplication_WithInvalidGalleryCount() {
        // Arrange
        IdentityRequest.Gallery gallery = new IdentityRequest.Gallery();
        List<IdentityRequest.ReferenceIds> referenceIds = new ArrayList<>();
        IdentityRequest.ReferenceIds refId = new IdentityRequest.ReferenceIds();
        refId.setReferenceId("gallery-ref-id");
        referenceIds.add(refId);
        gallery.setReferenceIds(referenceIds);
        identityRequest.setGallery(gallery);

        when(proxyabis.fetchCountForReferenceIdPresentInGallery(anyList())).thenReturn(0);

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.findDuplication(identityRequest);
        });
        assertEquals(FailureReasonsConstants.REFERENCEID_NOT_FOUND, exception.getReasonConstant());
    }

    /**
     * Tests that insertData throws a RequestException with DATA_SHARE_URL_EXPIRED
     * when a data share usage expired error is received from the external service.
     */
    @Test
    void testInsertData_DataShareExpired() {
        // Arrange
        String errorResponse = "{\"errors\":[{\"errorCode\":\"DAT-SER-006\",\"message\":\"Data share usuage expired\"}]}";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(errorResponse));

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(insertRequest);
        });
        assertEquals(FailureReasonsConstants.DATA_SHARE_URL_EXPIRED, exception.getReasonConstant());
    }

    /**
     * Tests that insertData throws a RequestException with INVALID_CBEFF_FORMAT
     * when an invalid CBEFF format is returned.
     */
    @Test
    void testInsertData_WithInvalidCBEFF() throws Exception {
        // Arrange
        String invalidCbeff = "invalid-cbeff-data";
        ReflectionTestUtils.setField(proxyAbisInsertService, "cbeffURL", "http://test-url.com/cbeff");
        ReflectionTestUtils.setField(proxyAbisInsertService, "encryption", false);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(invalidCbeff));
        when(proxyabis.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(insertRequest);
        });
        assertEquals(FailureReasonsConstants.INVALID_CBEFF_FORMAT, exception.getReasonConstant());
    }

    /**
     * Tests that insertData throws a RequestException with CBEFF_HAS_NO_DATA
     * when an empty CBEFF is returned.
     */
    @Test
    void testInsertData_WithEmptyCBEFF() throws Exception {
        // Arrange
        String emptyCbeff = "";
        ReflectionTestUtils.setField(proxyAbisInsertService, "cbeffURL", "http://test-url.com/cbeff");
        ReflectionTestUtils.setField(proxyAbisInsertService, "encryption", false);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(emptyCbeff));
        when(proxyabis.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(insertRequest);
        });
        assertEquals(FailureReasonsConstants.CBEFF_HAS_NO_DATA, exception.getReasonConstant());
    }

    /**
     * Tests that insertData throws a RequestException with UNEXPECTED_ERROR
     * when a data share error is returned from the external service.
     */
    @Test
    void testInsertData_WithDataShareError() throws Exception {
        // Arrange
        String errorResponse = "{\"errors\":[{\"errorCode\":\"DAT-SER-001\",\"message\":\"Data Encryption failed\"}]}";
        ReflectionTestUtils.setField(proxyAbisInsertService, "cbeffURL", "http://test-url.com/cbeff");
        ReflectionTestUtils.setField(proxyAbisInsertService, "encryption", false);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(errorResponse));
        when(proxyabis.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(insertRequest);
        });
        assertEquals(FailureReasonsConstants.UNEXPECTED_ERROR, exception.getReasonConstant());
    }

    /**
     * Tests that findDuplication retrieves analytics correctly and sets the appropriate
     * analytics values from the environment properties.
     */
    @Test
    void testFindDuplication_WithAnalytics() {
        // Arrange
        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(new ArrayList<>());
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);
        when(proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(anyString())).thenReturn(biometricDataList);

        // Mock analytics properties
        when(env.getProperty("analytics.confidence")).thenReturn("90");
        when(env.getProperty("analytics.internalscore")).thenReturn("85");
        when(env.getProperty("analytics.key1")).thenReturn("value1");
        when(env.getProperty("analytics.key2")).thenReturn("value2");

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getIdentityResponse());
        assertNotNull(response.getIdentityResponse().getCandidateList());
        assertNotNull(response.getIdentityResponse().getCandidateList().getCandidates());
        assertFalse(response.getIdentityResponse().getCandidateList().getCandidates().isEmpty());
        assertNotNull(response.getIdentityResponse().getCandidateList().getCandidates().get(0).getAnalytics());
        assertEquals("90", response.getIdentityResponse().getCandidateList().getCandidates().get(0).getAnalytics().getConfidence());
    }

    /**
     * Tests that insertData throws a RequestException with UNABLE_TO_FETCH_BIOMETRIC_DETAILS
     * when HttpClientErrorException occurs.
     */
    @Test
    void testInsertData_HttpClientError() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(insertRequest);
        });
        assertEquals(FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS, exception.getReasonConstant());
    }

    /**
     * Tests that findDuplication throws a RequestException with INTERNAL_ERROR_UNKNOWN
     * when the expectation returns an error forced response.
     */
    @Test
    void testFindDuplication_WithExpectationError() throws Exception {
        // Arrange
        List<String> bioValues = new ArrayList<>();
        bioValues.add("hash-value");

        Expectation expectation = new Expectation();
        expectation.setId("test-expectation");
        expectation.setActionToInterfere("Identify");
        expectation.setDelayInExecution("5000");
        expectation.setForcedResponse("Error");
        expectation.setErrorCode(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);

        when(expectationCache.get(anyString())).thenReturn(expectation);
        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(bioValues);

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.findDuplication(identityRequest);
        });
        assertEquals(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, exception.getReasonConstant());
    }

    /**
     * Tests that findDuplication returns a valid response with 0 delay when an empty gallery is provided.
     */
    @Test
    void testFindDuplication_WithEmptyGallery() throws Exception {
        // Arrange
        IdentityRequest.Gallery gallery = new IdentityRequest.Gallery();
        gallery.setReferenceIds(new ArrayList<>());
        identityRequest.setGallery(gallery);

        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(new ArrayList<>());
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);
        when(proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(anyString())).thenReturn(biometricDataList);

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getDelayResponse());
        assertNotNull(response.getIdentityResponse());
        assertEquals("1", response.getIdentityResponse().getReturnValue());
    }

    /**
     * Tests that insertData throws a RequestException with INVALID_CBEFF_FORMAT
     * when the decrypted CBEFF is not valid.
     */
    @Test
    void testInsertData_WithEmptyBiometricList() throws Exception {
        // Arrange
        String encryptedCbeff = "encrypted-cbeff-data";
        String decryptedCbeff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><BIR><Version>1.0</Version><CBEFFVersion>1.0</CBEFFVersion>"
                + "<BIRInfo><Integrity>false</Integrity></BIRInfo><BDBInfo><Format>ISO_19794_4_2011</Format>"
                + "<Quality><Algorithm><Organization>HMAC</Organization><Type>SHA-256</Type></Algorithm><Score>100</Score>"
                + "</Quality><Type>Finger</Type><Subtype>Left IndexFinger</Subtype><Level>Raw</Level><Purpose>Enroll</Purpose>"
                + "<CreationDate>2023-01-01T12:00:00.000Z</CreationDate></BDBInfo><BDB>base64-encoded-data</BDB></BIR>";

        ReflectionTestUtils.setField(proxyAbisInsertService, "cbeffURL", "http://test-url.com/cbeff");
        ReflectionTestUtils.setField(proxyAbisInsertService, "encryption", true);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(encryptedCbeff));
        when(cryptoUtil.decryptCbeff(encryptedCbeff)).thenReturn(decryptedCbeff);
        when(proxyabis.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            proxyAbisInsertService.insertData(insertRequest);
        });
        assertEquals(FailureReasonsConstants.INVALID_CBEFF_FORMAT, exception.getReasonConstant());
    }

    /**
     * Tests that findDuplication returns a valid response when force duplicate is enabled,
     * even if duplicate check is disabled.
     */
    @Test
    void testFindDuplication_WithForceDuplicate() throws Exception {
        // Arrange
        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(new ArrayList<>());
        when(proxyAbisConfigService.getDuplicate()).thenReturn(false);
        when(proxyAbisConfigService.isForceDuplicate()).thenReturn(true);
        when(proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(anyString())).thenReturn(biometricDataList);

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getDelayResponse());
        assertNotNull(response.getIdentityResponse());
        assertEquals("1", response.getIdentityResponse().getReturnValue());
        assertEquals("1", response.getIdentityResponse().getCandidateList().getCount());
    }

    /**
     * Tests that findDuplication returns a valid response with 0 candidates
     * when no biometric data is found.
     */
    @Test
    void testFindDuplication_WithEmptyBiometricData() throws Exception {
        // Arrange
        when(proxyAbisBioDataRepository.fetchBioDataByRefId(anyString())).thenReturn(new ArrayList<>());
        when(proxyAbisConfigService.getDuplicate()).thenReturn(true);
        when(proxyAbisBioDataRepository.fetchDuplicatesForReferenceId(anyString())).thenReturn(new ArrayList<>());

        // Act
        IdentifyDelayResponse response = proxyAbisInsertService.findDuplication(identityRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getDelayResponse());
        assertNotNull(response.getIdentityResponse());
        assertEquals("1", response.getIdentityResponse().getReturnValue());
        assertEquals("0", response.getIdentityResponse().getCandidateList().getCount());
    }

    /**
     * Tests the SHA generation from string data
     */
    @Test
    void testGetSHA() throws Exception {
        // Arrange
        String testData = "test-data";

        // Act
        String result = ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "getSHA", testData);

        // Assert
        assertNotNull(result);
        assertEquals(64, result.length()); // SHA-256 produces 64 character hex string
    }

    /**
     * Tests the SHA generation from byte array
     */
    @Test
    void testGetSHAFromBytes() throws Exception {
        // Arrange
        byte[] testData = "test-data".getBytes();

        // Act
        String result = ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "getSHAFromBytes", testData);

        // Assert
        assertNotNull(result);
        assertEquals(64, result.length()); // SHA-256 produces 64 character hex string
    }

    /**
     * Tests bytes to hex conversion
     */
    @Test
    void testBytesToHex() throws Exception {
        // Arrange
        byte[] testData = new byte[]{(byte)0xFF, (byte)0x00, (byte)0xAB};

        // Act
        String result = ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "bytesToHex", testData);

        // Assert
        assertNotNull(result);
        assertEquals("ff00ab", result.toLowerCase());
    }

    /**
     * Tests adding BIRs with empty BDB
     */
    @Test
    void testAddBirs_EmptyBDB() throws Exception {
        // Arrange
        InsertEntity ie = new InsertEntity();
        List<BiometricData> lst = new ArrayList<>();
        BIR birInfo = new BIR();
        List<BIR> birs = new ArrayList<>();
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        List<BiometricType> types = new ArrayList<>();
        types.add(BiometricType.FINGER);
        bdbInfo.setType(types);
        bir.setBdbInfo(bdbInfo);
        bir.setBdb(new byte[0]);
        birs.add(bir);
        birInfo.setBirs(birs);

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "addBirs", ie, lst, birInfo);
        });

        assertEquals(FailureReasonsConstants.CBEFF_HAS_NO_DATA, exception.getReasonConstant());
    }

    /**
     * Tests error handling in getBirs method
     */
    @Test
    void testGetBirs_InvalidCBEFF() throws Exception {
        // Arrange
        String invalidCbeff = "invalid-cbeff-data";

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "getBirs", invalidCbeff);
        });

        assertEquals(FailureReasonsConstants.INVALID_CBEFF_FORMAT, exception.getReasonConstant());
    }

    /**
     * Tests the saveUploadedFileWithParameters method for successful certificate upload.
     * Verifies that the certificate file is saved, properties are written, and old certificates are cleaned up.
     */
    @Test
    void testSaveUploadedFileWithParameters_Success() throws Exception {
        // Arrange
        String fileName = "test-cert.p12";
        String alias = "test-alias";
        String password = "test-password";
        String keystore = "test-keystore";
        byte[] fileContent = "test certificate content".getBytes();

        MultipartFile uploadedFile = new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "application/x-pkcs12";
            }

            @Override
            public boolean isEmpty() {
                return fileContent.length == 0;
            }

            @Override
            public long getSize() {
                return fileContent.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return fileContent;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(fileContent);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), fileContent);
            }
        };

        // Create a temporary directory structure for testing
        Path tempKeystorePath = Files.createTempDirectory("keystore");
        ReflectionTestUtils.setField(proxyAbisInsertService, "keystoreFilePath", tempKeystorePath);

        try {
            // Act
            String result = proxyAbisInsertService.saveUploadedFileWithParameters(uploadedFile, alias, password, keystore);

            // Assert
            assertEquals("Successfully uploaded file", result);

            // Verify certificate file was created
            Path certPath = tempKeystorePath.resolve(fileName);
            assertTrue(Files.exists(certPath));
            assertArrayEquals(fileContent, Files.readAllBytes(certPath));

            // Verify properties file was created with correct content
            Path propsPath = tempKeystorePath.resolve("partner.properties");
            assertTrue(Files.exists(propsPath));
            List<String> propsContent = Files.readAllLines(propsPath);
            assertTrue(propsContent.contains("certificate.alias=" + alias));
            assertTrue(propsContent.contains("certificate.password=" + password));
            assertTrue(propsContent.contains("certificate.keystore=" + keystore));
            assertTrue(propsContent.contains("certificate.filename=" + fileName));

        } finally {
            // Cleanup
            FileUtils.deleteDirectory(tempKeystorePath.toFile());
        }
    }

    /**
     * Tests the saveUploadedFileWithParameters method when an exception occurs during file operations.
     * Verifies that the method handles errors gracefully and returns appropriate error message.
     */
    @Test
    void testSaveUploadedFileWithParameters_Error() throws Exception {
        // Arrange
        String fileName = "test-cert.p12";
        byte[] fileContent = "test content".getBytes();

        MultipartFile uploadedFile = new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "application/x-pkcs12";
            }

            @Override
            public boolean isEmpty() {
                return fileContent.length == 0;
            }

            @Override
            public long getSize() {
                return fileContent.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("Simulated file read error");
            }

            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("Simulated file read error");
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                throw new IOException("Simulated file transfer error");
            }
        };

        // Act
        String result = proxyAbisInsertService.saveUploadedFileWithParameters(uploadedFile, "alias", "password", "keystore");

        // Assert
        assertEquals("Could not upload file", result);
    }

    /**
     * Tests validateCBEFFData method with data share URL expired error.
     * Verifies that the method throws RequestException with DATA_SHARE_URL_EXPIRED constant.
     */
    @Test
    void testValidateCBEFFData_DataShareUrlExpired() throws Exception {
        // Arrange
        String cbeffWithExpiredError = "{\"errors\":[{\"errorCode\":\"DAT-SER-006\",\"message\":\"Data share URL expired\"}]}";
        
        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "validateCBEFFData", cbeffWithExpiredError);
        });
        
        assertEquals(FailureReasonsConstants.DATA_SHARE_URL_EXPIRED, exception.getReasonConstant());
    }

    /**
     * Tests validateCBEFFData method with unexpected error.
     * Verifies that the method throws RequestException with UNEXPECTED_ERROR constant.
     */
    @Test
    void testValidateCBEFFData_UnexpectedError() throws Exception {
        // Arrange
        String cbeffWithError = "{\"errors\":[{\"errorCode\":\"DAT-SER-001\",\"message\":\"Some unexpected error\"}]}";
        
        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "validateCBEFFData", cbeffWithError);
        });
        
        assertEquals(FailureReasonsConstants.UNEXPECTED_ERROR, exception.getReasonConstant());
    }

    /**
     * Tests validateCBEFFData method with invalid JSON.
     * Verifies that the method handles invalid JSON gracefully.
     */
    @Test
    void testValidateCBEFFData_InvalidJson() throws Exception {
        // Arrange
        String invalidJson = "invalid json content";
        
        // Act & Assert
        // Should not throw any exception for invalid JSON
        ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "validateCBEFFData", invalidJson);
    }

    /**
     * Tests validateCBEFFData method with valid JSON without errors.
     * Verifies that the method handles valid JSON without throwing exceptions.
     */
    @Test
    void testValidateCBEFFData_ValidJson() throws Exception {
        // Arrange
        String validJson = "{\"data\":\"some valid data\"}";
        
        // Act & Assert
        // Should not throw any exception for valid JSON without errors
        ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "validateCBEFFData", validJson);
    }

    /**
     * Tests validateCBEFFData method with null errors array.
     * Verifies that the method handles null errors gracefully.
     */
    @Test
    void testValidateCBEFFData_NullErrors() throws Exception {
        // Arrange
        String jsonWithNullErrors = "{\"errors\":null}";
        
        // Act & Assert
        // Should not throw any exception for null errors
        ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "validateCBEFFData", jsonWithNullErrors);
    }

    /**
     * Tests addBirs method with valid BIR data.
     * Verifies that BiometricData entries are correctly created and added to the list.
     */
    @Test
    void testAddBirs_ValidData() throws Exception {
        // Arrange
        InsertEntity ie = new InsertEntity();
        List<BiometricData> lst = new ArrayList<>();
        BIR birInfo = new BIR();
        List<BIR> birs = new ArrayList<>();
        
        // Create a valid BIR
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        List<BiometricType> types = new ArrayList<>();
        types.add(BiometricType.FINGER);
        bdbInfo.setType(types);
        List<String> subtypes = new ArrayList<>();
        subtypes.add("Left IndexFinger");
        bdbInfo.setSubtype(subtypes);
        bir.setBdbInfo(bdbInfo);
        bir.setBdb("test biometric data".getBytes());
        birs.add(bir);
        birInfo.setBirs(birs);

        // Act
        ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "addBirs", ie, lst, birInfo);

        // Assert
        assertEquals(1, lst.size());
        BiometricData bd = lst.get(0);
        assertEquals(BiometricType.FINGER.value(), bd.getType());
        assertEquals("[Left IndexFinger]", bd.getSubtype());
        assertNotNull(bd.getBioData());
        assertEquals(ie, bd.getInsertEntity());
    }

    /**
     * Tests addBirs method with multiple valid BIRs.
     * Verifies that multiple BiometricData entries are correctly created and added.
     */
    @Test
    void testAddBirs_MultipleValidBirs() throws Exception {
        // Arrange
        InsertEntity ie = new InsertEntity();
        List<BiometricData> lst = new ArrayList<>();
        BIR birInfo = new BIR();
        List<BIR> birs = new ArrayList<>();
        
        // Create first BIR (Finger)
        BIR bir1 = new BIR();
        BDBInfo bdbInfo1 = new BDBInfo();
        List<BiometricType> types1 = new ArrayList<>();
        types1.add(BiometricType.FINGER);
        bdbInfo1.setType(types1);
        List<String> subtypes1 = new ArrayList<>();
        subtypes1.add("Left IndexFinger");
        bdbInfo1.setSubtype(subtypes1);
        bir1.setBdbInfo(bdbInfo1);
        bir1.setBdb("finger data".getBytes());
        
        // Create second BIR (Face)
        BIR bir2 = new BIR();
        BDBInfo bdbInfo2 = new BDBInfo();
        List<BiometricType> types2 = new ArrayList<>();
        types2.add(BiometricType.FACE);
        bdbInfo2.setType(types2);
        bir2.setBdbInfo(bdbInfo2);
        bir2.setBdb("face data".getBytes());
        
        birs.add(bir1);
        birs.add(bir2);
        birInfo.setBirs(birs);

        // Act
        ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "addBirs", ie, lst, birInfo);

        // Assert
        assertEquals(2, lst.size());
        assertEquals(BiometricType.FINGER.value(), lst.get(0).getType());
        assertEquals("[Left IndexFinger]", lst.get(0).getSubtype());
        assertEquals(BiometricType.FACE.value(), lst.get(1).getType());
        assertNull(lst.get(1).getSubtype());
    }

    /**
     * Tests addBirs method with null BDBInfo.
     * Verifies that the method throws RequestException with CBEFF_HAS_NO_DATA.
     */
    @Test
    void testAddBirs_NullBdbInfo() throws Exception {
        // Arrange
        InsertEntity ie = new InsertEntity();
        List<BiometricData> lst = new ArrayList<>();
        BIR birInfo = new BIR();
        List<BIR> birs = new ArrayList<>();
        
        BIR bir = new BIR();
        bir.setBdbInfo(null);
        bir.setBdb(new byte[0]); // Empty BDB to trigger the first check
        birs.add(bir);
        birInfo.setBirs(birs);

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "addBirs", ie, lst, birInfo);
        });
        
        assertEquals(FailureReasonsConstants.CBEFF_HAS_NO_DATA, exception.getReasonConstant());
    }

    /**
     * Tests addBirs method with empty BDB (biometric data).
     * Verifies that the method throws RequestException with CBEFF_HAS_NO_DATA.
     */
    @Test
    void testAddBirs_EmptyBdb() throws Exception {
        // Arrange
        InsertEntity ie = new InsertEntity();
        List<BiometricData> lst = new ArrayList<>();
        BIR birInfo = new BIR();
        List<BIR> birs = new ArrayList<>();
        
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        List<BiometricType> types = new ArrayList<>();
        types.add(BiometricType.FINGER);
        bdbInfo.setType(types);
        bir.setBdbInfo(bdbInfo);
        bir.setBdb(new byte[0]);
        birs.add(bir);
        birInfo.setBirs(birs);

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "addBirs", ie, lst, birInfo);
        });
        
        assertEquals(FailureReasonsConstants.CBEFF_HAS_NO_DATA, exception.getReasonConstant());
    }

    /**
     * Tests addBirs method with null BDB (biometric data).
     * Verifies that the method throws RequestException with CBEFF_HAS_NO_DATA.
     */
    @Test
    void testAddBirs_NullBdb() throws Exception {
        // Arrange
        InsertEntity ie = new InsertEntity();
        List<BiometricData> lst = new ArrayList<>();
        BIR birInfo = new BIR();
        List<BIR> birs = new ArrayList<>();
        
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();
        List<BiometricType> types = new ArrayList<>();
        types.add(BiometricType.FINGER);
        bdbInfo.setType(types);
        bir.setBdbInfo(bdbInfo);
        bir.setBdb(null);
        birs.add(bir);
        birInfo.setBirs(birs);

        // Act & Assert
        RequestException exception = assertThrows(RequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "addBirs", ie, lst, birInfo);
        });
        
        assertEquals(FailureReasonsConstants.CBEFF_HAS_NO_DATA, exception.getReasonConstant());
    }

    /**
     * Tests addBirs method with empty BIRs list.
     * Verifies that no BiometricData entries are added to the list.
     */
    @Test
    void testAddBirs_EmptyBirsList() throws Exception {
        // Arrange
        InsertEntity ie = new InsertEntity();
        List<BiometricData> lst = new ArrayList<>();
        BIR birInfo = new BIR();
        birInfo.setBirs(new ArrayList<>());

        // Act
        ReflectionTestUtils.invokeMethod(proxyAbisInsertService, "addBirs", ie, lst, birInfo);

        // Assert
        assertTrue(lst.isEmpty());
    }
}