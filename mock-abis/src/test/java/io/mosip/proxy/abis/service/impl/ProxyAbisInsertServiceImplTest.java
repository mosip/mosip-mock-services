package io.mosip.proxy.abis.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.mosip.proxy.abis.dto.IdentityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

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
    public void testInsertData_ReferenceIdAlreadyExists() {
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
    public void testDeleteData_Success() {
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
    public void testDeleteData_Exception() {
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
    public void testFindDuplication_NoGallery() {
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
    public void testFindDuplication_WithGallery() {
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
    public void testFindDuplication_WithExpectation() {
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
    public void testFindDuplication_NoDuplicates() {
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
    public void testGetAnalytics() throws Exception {
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
        IdentityResponse.Analytics analytics = response.getIdentityResponse().getCandidateList().getCandidates().get(0).getAnalytics();
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
    public void testInsertData_NullRequest() {
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
    public void testInsertData_CbeffFetchError() {
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
    public void testFindDuplication_DuplicateCheckDisabled() {
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
    public void testFindDuplication_WithInvalidGalleryCount() {
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

}