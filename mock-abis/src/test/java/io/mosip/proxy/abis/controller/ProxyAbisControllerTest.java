package io.mosip.proxy.abis.controller;

import io.mosip.proxy.abis.dto.*;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.listener.Listener;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test class for ProxyAbisController.
 * This class verifies the behavior of the controller methods
 * and their interactions with the service layer.
 */
@ExtendWith(MockitoExtension.class)
class ProxyAbisControllerTest {

    @Mock
    private ProxyAbisInsertService abisInsertService;

    @Mock
    private Listener listener;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ProxyAbisController controller;

    private InsertRequestMO validInsertRequest;
    private RequestMO validRequest;
    private IdentityRequest validIdentityRequest;

    /**
     * Sets up the test data and initializes the controller with a mock listener.
     */
    @BeforeEach
    void setUp() {
        controller.setListener(listener);

        validInsertRequest = new InsertRequestMO();
        validInsertRequest.setId("mosip.abis.insert");
        validInsertRequest.setRequestId("test-request-id");
        validInsertRequest.setRequesttime(LocalDateTime.now());
        validInsertRequest.setReferenceId("test-reference-id");
        validInsertRequest.setVersion("1.0");

        validRequest = new RequestMO();
        validRequest.setId("test-id");
        validRequest.setRequestId("test-request-id");
        validRequest.setRequesttime(LocalDateTime.now());
        validRequest.setReferenceId("test-reference-id");

        validIdentityRequest = new IdentityRequest();
        validIdentityRequest.setId("test-id");
        validIdentityRequest.setRequestId("test-request-id");
        validIdentityRequest.setRequesttime(LocalDateTime.now());
        validIdentityRequest.setReferenceId("test-reference-id");
    }

    /**
     * Tests the saveInsertRequest method for a successful scenario.
     */
    @Test
    void testSaveInsertRequest_Success() throws Exception {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(abisInsertService.insertData(any(InsertRequestMO.class))).thenReturn(0);

        ResponseEntity<Object> response = controller.saveInsertRequest(validInsertRequest, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ResponseMO.class, response.getBody());
    }

    /**
     * Tests the saveInsertRequest method for validation failure.
     */
    @Test
    void testSaveInsertRequest_ValidationFailure() {
        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(Exception.class, () ->
                controller.saveInsertRequest(validInsertRequest, bindingResult));
    }

    /**
     * Tests the deleteRequest method for a successful scenario.
     */
    @Test
    void testDeleteRequest_Success() {
        doNothing().when(abisInsertService).deleteData(anyString());

        ResponseEntity<Object> response = controller.deleteRequest(validRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ResponseMO.class, response.getBody());
    }

    /**
     * Tests the uploadCertificate method for a successful scenario.
     */
    @Test
    void testUploadCertificate_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.cert", "application/x-x509-ca-cert", "test content".getBytes());
        when(abisInsertService.saveUploadedFileWithParameters(any(), anyString(), anyString(), anyString()))
                .thenReturn("Success");

        ResponseEntity<String> response = controller.uploadcertificate(
                file, "password", "alias", "keystore");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody());
    }

    /**
     * Tests the uploadCertificate method for an empty file scenario.
     */
    @Test
    void testUploadCertificate_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.cert", "application/x-x509-ca-cert", new byte[0]);

        ResponseEntity<String> response = controller.uploadcertificate(
                emptyFile, "password", "alias", "keystore");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Please select a file", response.getBody());
    }

    /**
     * Tests the identityRequest method for a successful scenario.
     */
    @Test
    void testIdentityRequest_Success() {
        IdentifyDelayResponse identifyResponse = new IdentifyDelayResponse();
        IdentityResponse identityResponseInstance = new IdentityResponse();
        identifyResponse.setIdentityResponse(identityResponseInstance);
        identifyResponse.setDelayResponse(0);

        when(abisInsertService.findDuplication(any(IdentityRequest.class)))
                .thenReturn(identifyResponse);

        ResponseEntity<Object> response = controller.identityRequest(validIdentityRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Tests the identityRequest method for a failure scenario.
     */
    @Test
    void testIdentityRequest_Failure() {
        when(abisInsertService.findDuplication(any(IdentityRequest.class)))
                .thenThrow(new RequestException("Error"));

        ResponseEntity<Object> response = controller.identityRequest(validIdentityRequest);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }

    /**
     * Tests the saveInsertRequestThroughListener method for a successful scenario.
     */
    @Test
    void testSaveInsertRequestThroughListener_Success() {
        when(abisInsertService.insertData(any(InsertRequestMO.class))).thenReturn(0);

        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(validInsertRequest, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ResponseMO);
    }

    /**
     * Tests the deleteRequestThroughListener method for a successful scenario.
     */
    @Test
    void testDeleteRequestThroughListener_Success() {
        doNothing().when(abisInsertService).deleteData(anyString());

        ResponseEntity<Object> response = controller.deleteRequestThroughListner(validRequest, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ResponseMO);
    }

    /**
     * Tests the identityRequestThroughListener method for a successful scenario.
     */
    @Test
    void testIdentityRequestThroughListener_Success() {
        IdentifyDelayResponse identifyResponse = new IdentifyDelayResponse();
        IdentityResponse identityResponse = new IdentityResponse();
        identifyResponse.setIdentityResponse(identityResponse);
        identifyResponse.setDelayResponse(0);

        when(abisInsertService.findDuplication(any(IdentityRequest.class)))
                .thenReturn(identifyResponse);

        ResponseEntity<Object> response = controller.identityRequestThroughListner(validIdentityRequest, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof IdentityResponse);
    }

    /**
     * Tests the saveInsertRequestThroughListener method for an invalid ID scenario.
     */
    @Test
    void testValidateRequest_InvalidId() {
        validInsertRequest.setId("invalid.id");

        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(validInsertRequest, 1);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }

    /**
     * Tests the executeAsync method to verify asynchronous execution.
     */
    @Test
    void testAsyncExecution() throws Exception {
        ResponseEntity<Object> responseEntity = new ResponseEntity<>("test", HttpStatus.OK);
        ArgumentCaptor<Integer> msgTypeCaptor = ArgumentCaptor.forClass(Integer.class);

        controller.executeAsync(responseEntity, 0, 1);

        verify(listener, timeout(1000)).sendToQueue(any(), msgTypeCaptor.capture());
        assertEquals(1, msgTypeCaptor.getValue());
    }
}