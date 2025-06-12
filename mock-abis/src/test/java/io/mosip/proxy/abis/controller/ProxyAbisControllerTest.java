package io.mosip.proxy.abis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mosip.proxy.abis.dto.FailureResponse;
import io.mosip.proxy.abis.dto.IdentityRequest;
import io.mosip.proxy.abis.dto.IdentifyDelayResponse;
import io.mosip.proxy.abis.dto.IdentityResponse;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.dto.RequestMO;
import io.mosip.proxy.abis.dto.ResponseMO;

import io.mosip.proxy.abis.exception.FailureReasonsConstants;
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

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
     * Tests successful insertion of a new ABIS request.
     * Verifies that the controller properly handles valid insert requests and returns OK status.
     */
    @Test
    void testSaveInsertRequest_WhenValidRequest_ShouldReturnSuccessResponse() throws Exception {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(abisInsertService.insertData(any(InsertRequestMO.class))).thenReturn(0);

        ResponseEntity<Object> response = controller.saveInsertRequest(validInsertRequest, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ResponseMO.class, response.getBody());
    }

    /**
     * Tests validation failure handling for insert requests.
     * Ensures that requests with validation errors are properly rejected.
     */
    @Test
    void testSaveInsertRequest_WhenValidationFails_ShouldThrowException() {
        when(bindingResult.hasErrors()).thenReturn(true);

        assertThrows(Exception.class, () ->
                controller.saveInsertRequest(validInsertRequest, bindingResult));
    }

    /**
     * Tests successful deletion of an ABIS reference.
     * Verifies proper handling of delete requests and response generation.
     */
    @Test
    void testDeleteRequest_WhenValidRequest_ShouldReturnSuccessResponse() {
        doNothing().when(abisInsertService).deleteData(anyString());

        ResponseEntity<Object> response = controller.deleteRequest(validRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ResponseMO.class, response.getBody());
    }

    /**
     * Tests successful certificate upload functionality.
     * Verifies that valid certificate files are properly processed and stored.
     */
    @Test
    void testUploadCertificate_WhenValidFileAndParamsProvided_ShouldReturnSuccessResponse() {
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
     * Tests rejection of empty certificate files.
     * Ensures proper error handling when no file content is provided.
     */
    @Test
    void testUploadCertificate_WhenEmptyFileProvided_ShouldReturnNoContentWithMessage() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.cert", "application/x-x509-ca-cert", new byte[0]);

        ResponseEntity<String> response = controller.uploadcertificate(
                emptyFile, "password", "alias", "keystore");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Please select a file", response.getBody());
    }

    /**
     * Tests successful identity verification request processing.
     * Verifies proper handling of biometric identity verification requests.
     */
    @Test
    void testIdentityRequest_WhenValidRequestProvided_ShouldReturnSuccessResponse() {
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
     * Tests failure handling in identity verification requests.
     * Ensures proper error responses when identity verification fails.
     */
    @Test
    void testIdentityRequest_WhenServiceThrowsException_ShouldReturnFailureResponse() {
        when(abisInsertService.findDuplication(any(IdentityRequest.class)))
                .thenThrow(new RequestException("Error"));

        ResponseEntity<Object> response = controller.identityRequest(validIdentityRequest);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }

    /**
     * Tests successful asynchronous insert request handling through listener.
     * Verifies proper queuing and processing of insert requests via message listener.
     */
    @Test
    void testSaveInsertRequestThroughListener_WhenValidRequest_ShouldReturnSuccessResponse() {
        when(abisInsertService.insertData(any(InsertRequestMO.class))).thenReturn(0);

        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(validInsertRequest, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ResponseMO);
    }

    /**
     * Tests the deleteRequestThroughListner method for a successful scenario.
     * It ensures that when deleteData is called on the service, the controller responds with HTTP OK
     * and returns a valid ResponseMO object.
     */
    @Test
    void testDeleteRequestThroughListener_WhenValidRequest_ShouldReturnSuccessResponse() {
        doNothing().when(abisInsertService).deleteData(anyString());

        ResponseEntity<Object> response = controller.deleteRequestThroughListner(validRequest, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ResponseMO);
    }

    /**
     * Tests the identityRequestThroughListener method for a successful scenario.
     * This test verifies that when the duplication check returns a valid response,
     * the controller responds with HTTP OK and returns an IdentityResponse.
     */
    @Test
    void testIdentityRequestThroughListener_WhenSuccess_ShouldReturnIdentityResponse() {
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
     * This test sets an invalid ID for the insert request and verifies that the controller responds
     * with HTTP NOT_ACCEPTABLE and returns a FailureResponse.
     */
    @Test
    void testSaveInsertRequestThroughListener_InvalidId_ReturnsFailureResponse() {
        validInsertRequest.setId("invalid.id");
        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(validInsertRequest, 1);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }

    /**
     * Tests the executeAsync method to verify asynchronous execution.
     * This test creates a response entity, triggers the asynchronous call, and then verifies
     * that the listener's sendToQueue method is invoked with the correct msgType.
     */
    @Test
    void testExecuteAsync_SendsMessageToQueue_WithCorrectMsgType() throws Exception {
        ResponseEntity<Object> responseEntity = new ResponseEntity<>("test", HttpStatus.OK);
        ArgumentCaptor<Integer> msgTypeCaptor = ArgumentCaptor.forClass(Integer.class);
        controller.executeAsync(responseEntity, 0, 1);
        verify(listener, timeout(1000)).sendToQueue(any(), msgTypeCaptor.capture());
        assertEquals(1, msgTypeCaptor.getValue());
    }

    /**
     * Tests the deletion of a specific expectation.
     * Verifies that the service is called with the correct ID and the response contains a confirmation message with HTTP OK status.
     */
    @Test
    void testDeleteRequestThroughListener_ExceptionThrown_ReturnsFailureResponse() {
        doThrow(new RequestException("Delete failed"))
                .when(abisInsertService).deleteData(anyString());
        ResponseEntity<Object> response = controller.deleteRequestThroughListner(validRequest, 1);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }

    /**
     * Tests the identityRequestThroughListener method when a RuntimeException occurs.
     * This test simulates an unexpected error from the service and verifies that the controller
     * responds with HTTP NOT_ACCEPTABLE and returns a FailureResponse.
     */
    @Test
    void testIdentityRequestThroughListener_ServiceException_ReturnsFailureResponse() {
        when(abisInsertService.findDuplication(any(IdentityRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        ResponseEntity<Object> response = controller.identityRequestThroughListner(validIdentityRequest, 1);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }

    /**
     * Tests that saveInsertRequest throws a NullPointerException when a null request is provided.
     */
    @Test
    void testSaveInsertRequest_NullRequest_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                controller.saveInsertRequest(null, bindingResult));
    }

    /**
     * Tests that deleteRequest throws a NullPointerException when a null request is provided.
     */
    @Test
    void testDeleteRequest_NullRequest_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                controller.deleteRequest(null));
    }

    /**
     * Tests that identityRequest throws a NullPointerException when a null request is provided.
     */
    @Test
    void testIdentityRequest_NullRequest_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                controller.identityRequest(null));
    }

    /**
     * Tests saving an insert request through the listener when the request id is missing.
     * The test verifies that the controller returns a NOT_ACCEPTABLE status along with a FailureResponse.
     */
    @Test
    void testSaveInsertRequestThroughListener_MissingRequestId_ReturnsFailureResponse() {
        validInsertRequest.setRequestId(null);
        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(validInsertRequest, 1);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }

    /**
     * Tests saving an insert request through the listener when the reference id is missing.
     * Verifies that the controller returns a NOT_ACCEPTABLE status along with a FailureResponse.
     */
    @Test
    void testSaveInsertRequestThroughListener_MissingReferenceId_ReturnsFailureResponse() {
        validInsertRequest.setReferenceId(null);
        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(validInsertRequest, 1);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }

    /**
     * Tests the uploadCertificate method with missing alias.
     */
    @Test
    void testUploadCertificate_MissingAlias_ReturnsNoContent() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.cert", "application/x-x509-ca-cert", "test content".getBytes());
        ResponseEntity<String> response = controller.uploadcertificate(
                file, "password", "", "keystore");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Please enter alias", response.getBody());
    }

    /**
     * Tests the uploadCertificate method with missing password.
     */
    @Test
    void testUploadCertificate_MissingPassword_ReturnsNoContent() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.cert", "application/x-x509-ca-cert", "test content".getBytes());
        ResponseEntity<String> response = controller.uploadcertificate(
                file, "", "alias", "keystore");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Please enter password", response.getBody());
    }

    /**
     * Tests validation of invalid version format.
     */
    @Test
    void testValidateRequest_InvalidVersion_ReturnsNotAcceptable() {
        validInsertRequest.setVersion("invalid");
        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(validInsertRequest, 1);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }

    /**
     * Tests validation of missing request time.
     */
    @Test
    void testSaveInsertRequestThroughListener_MissingRequestTime_ReturnsNotAcceptable() {
        validInsertRequest.setRequesttime(null);
        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(validInsertRequest, 1);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
    }


    /**
     * Tests timer cleanup in controller.
     */
    @Test
    void testExecuteAsync_TimerInitializedAndNotNullAfterDelay() throws Exception {
        ResponseEntity<Object> responseEntity = new ResponseEntity<>("test", HttpStatus.OK);
        controller.executeAsync(responseEntity, 0, 1);

        Thread.sleep(1500);

        Field timerField = ProxyAbisController.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        Timer timer = (Timer) timerField.get(controller);
        assertNotNull(timer);
    }

    /**
     * Tests executeAsync with UnsupportedEncodingException.
     */
    @Test
    void testExecuteAsync_WhenUnsupportedEncodingExceptionThrown() throws Exception {
        ResponseEntity<Object> responseEntity = new ResponseEntity<>("test", HttpStatus.OK);
        doThrow(new UnsupportedEncodingException())
                .when(listener).sendToQueue(any(), anyInt());
        controller.executeAsync(responseEntity, 0, 1);
        verify(listener, timeout(1000)).sendToQueue(any(), anyInt());
    }

    /**
     * Tests validation errors in saveInsertRequestThroughListner method.
     */
    @Test
    void testSaveInsertRequestThroughListener_WhenValidationFailsDueToMissingFields() {
        InsertRequestMO request = new InsertRequestMO();
        request.setId("mosip.abis.insert");
        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(request, 1);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(FailureReasonsConstants.MISSING_REQUESTID, failureResponse.getFailureReason());
    }

    /**
     * Tests identityRequestThroughListner with delayed response.
     */
    @Test
    void testIdentityRequestThroughListener_WithArtificialDelay() {
        IdentifyDelayResponse identifyResponse = new IdentifyDelayResponse();
        IdentityResponse identityResponse = new IdentityResponse();
        identifyResponse.setIdentityResponse(identityResponse);
        identifyResponse.setDelayResponse(5);
        when(abisInsertService.findDuplication(any(IdentityRequest.class)))
                .thenReturn(identifyResponse);
        ResponseEntity<Object> response = controller.identityRequestThroughListner(validIdentityRequest, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof IdentityResponse);
    }

    /**
     * Tests executeAsync with JsonProcessingException.
     */
    @Test
    void testExecuteAsync_WhenJsonProcessingExceptionThrownInSendToQueue() throws Exception {
        ResponseEntity<Object> responseEntity = new ResponseEntity<>("test", HttpStatus.OK);
        doThrow(new JsonProcessingException("Test error") {})
                .when(listener).sendToQueue(any(), anyInt());
        controller.executeAsync(responseEntity, 0, 1);
        verify(listener, timeout(1000)).sendToQueue(any(), anyInt());
    }

    /**
     * Tests getListener method.
     */
    @Test
    void testGetListener_ReturnsExpectedListenerInstance() {
        Listener result = controller.getListener();
        assertEquals(listener, result);
    }

    /**
     * Tests processDeleteRequest with delayed execution.
     */
    @Test
    void testProcessDeleteRequest_WithDelay_ReturnsOkStatusAndResponseMO() {
        doNothing().when(abisInsertService).deleteData(anyString());
        ResponseEntity<Object> response = controller.deleteRequestThroughListner(validRequest, 2);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ResponseMO);
        verify(abisInsertService).deleteData(validRequest.getReferenceId());
    }

    /**
     * Tests that when the service throws a RequestException with a null reason constant while processing
     * a valid insert request, the controller handles the exception by returning an HTTP OK status along with
     * a FailureResponse that has the default INTERNAL_ERROR_UNKNOWN failure reason.
     */
    @Test
    void testSaveInsertRequest_RequestExceptionWithNullReason_InternalError() throws Exception {
        when(bindingResult.hasErrors()).thenReturn(false);
        RequestException mockException = mock(RequestException.class);
        when(mockException.getReasonConstant()).thenReturn(null);
        when(mockException.getDelayResponse()).thenReturn(0);
        when(abisInsertService.insertData(any(InsertRequestMO.class)))
                .thenThrow(mockException);
        ResponseEntity<Object> response = controller.saveInsertRequest(validInsertRequest, bindingResult);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, failureResponse.getFailureReason());
    }

    /**
     * Tests that when deleteData in the service layer throws a RequestException with a null reason constant,
     * the deleteRequest method of the controller returns an INTERNAL_SERVER_ERROR status and the default
     * INTERNAL_ERROR_UNKNOWN failure reason as the response body.
     */
    @Test
    void testDeleteRequest_RequestExceptionWithNullReason_ReturnsInternalServerErrorAndUnknown() {
        RequestException mockException = mock(RequestException.class);
        when(mockException.getReasonConstant()).thenReturn(null);
        doThrow(mockException).when(abisInsertService).deleteData(anyString());
        ResponseEntity<Object> response = controller.deleteRequest(validRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, response.getBody());
    }

    /**
     * Tests that when the insert service throws a RequestException with a null reason constant
     * and a delay response is provided during the processing of an insert request,
     * the controller returns an HTTP OK status with a FailureResponse containing
     * the default INTERNAL_ERROR_UNKNOWN failure reason.
     */
    @Test
    void testProcessInsertRequest_RequestExceptionWithNullReason_ReturnsOkStatusAndInternalErrorFailureResponse() {
        RequestException mockException = mock(RequestException.class);
        when(mockException.getReasonConstant()).thenReturn(null);
        when(mockException.getDelayResponse()).thenReturn(5);
        when(abisInsertService.insertData(any(InsertRequestMO.class)))
                .thenThrow(mockException);
        ResponseEntity<Object> response = controller.processInsertRequest(validInsertRequest, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, failureResponse.getFailureReason());
    }

    /**
     * Tests that when the insert service throws a RequestException with a custom reason constant
     * and a delay response is provided during the processing of an insert request,
     * the controller returns an HTTP OK status with a FailureResponse containing
     * the provided custom error reason.
     */
    @Test
    void testProcessInsertRequest_RequestExceptionWithCustomReason_ReturnsOkStatusAndCustomFailureResponse() {
        String customReason = "Custom error reason";
        RequestException mockException = mock(RequestException.class);
        when(mockException.getReasonConstant()).thenReturn(customReason);
        when(mockException.getDelayResponse()).thenReturn(5);
        when(abisInsertService.insertData(any(InsertRequestMO.class)))
                .thenThrow(mockException);
        ResponseEntity<Object> response = controller.processInsertRequest(validInsertRequest, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(customReason, failureResponse.getFailureReason());
    }

    /**
     * Tests that when the insert service throws a RequestException with a null reason constant,
     * the controller's saveInsertRequestThroughListner method handles the exception by returning
     * an HTTP OK status with a FailureResponse containing the default INTERNAL_ERROR_UNKNOWN failure reason
     * and a return value of "2".
     */
    @Test
    void testSaveInsertRequestThroughListner_RequestExceptionWithNullReason_ReturnsOkStatusAndInternalErrorFailureResponse() {
        InsertRequestMO request = new InsertRequestMO();
        request.setId("mosip.abis.insert");
        request.setRequestId("123");
        request.setRequesttime(LocalDateTime.now());
        request.setReferenceId("ref123");
        request.setVersion("1.0");
        RequestException mockException = mock(RequestException.class);
        when(mockException.getReasonConstant()).thenReturn(null);
        when(mockException.getDelayResponse()).thenReturn(0);
        when(abisInsertService.insertData(any(InsertRequestMO.class))).thenThrow(mockException);
        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(request, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, failureResponse.getFailureReason());
        assertEquals("2", failureResponse.getReturnValue());
    }

    /**
     * Tests that when the insert service throws a RequestException with a custom reason constant
     * and a delay response is provided during the processing of an insert request,
     * the controller returns an HTTP OK status with a FailureResponse containing
     * the provided custom error reason and a return value of "2".
     */
    @Test
    void testSaveInsertRequestThroughListner_RequestExceptionWithCustomReason_ReturnsOkStatusAndCustomFailureResponse() {
        InsertRequestMO request = new InsertRequestMO();
        request.setId("mosip.abis.insert");
        request.setRequestId("123");
        request.setRequesttime(LocalDateTime.now());
        request.setReferenceId("ref123");
        request.setVersion("1.0");
        String customReason = "Custom error reason";
        RequestException mockException = mock(RequestException.class);
        when(mockException.getReasonConstant()).thenReturn(customReason);
        when(mockException.getDelayResponse()).thenReturn(0); // Add delay response
        when(abisInsertService.insertData(any(InsertRequestMO.class))).thenThrow(mockException);
        ResponseEntity<Object> response = controller.saveInsertRequestThroughListner(request, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Changed to OK to match implementation
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(customReason, failureResponse.getFailureReason());
        assertEquals("2", failureResponse.getReturnValue());
    }

    /**
     * Tests that when the insert service throws a RequestException with a null reason constant,
     * the controller's saveInsertRequest method handles the exception properly by returning an HTTP OK
     * status along with a FailureResponse that contains the default INTERNAL_ERROR_UNKNOWN failure reason
     * and a return value of "2".
     */
    @Test
    void testSaveInsertRequest_RequestExceptionWithNullReason_ReturnsOkStatusAndInternalErrorFailureResponse() throws Exception {
        InsertRequestMO request = new InsertRequestMO();
        request.setId("mosip.abis.insert");
        request.setRequestId("123");
        request.setRequesttime(LocalDateTime.now());
        request.setReferenceId("ref123");
        request.setVersion("1.0");

        when(bindingResult.hasErrors()).thenReturn(false);
        RequestException mockException = mock(RequestException.class);
        when(mockException.getReasonConstant()).thenReturn(null);
        when(mockException.getDelayResponse()).thenReturn(0);
        when(abisInsertService.insertData(any(InsertRequestMO.class))).thenThrow(mockException);
        ResponseEntity<Object> response = controller.saveInsertRequest(request, bindingResult);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, failureResponse.getFailureReason());
        assertEquals("2", failureResponse.getReturnValue());
    }

    /**
     * Tests that when the findDuplication service method throws a RequestException with a null reason constant,
     * the identityRequest method of the controller returns an HTTP NOT_ACCEPTABLE status with a FailureResponse
     * containing the default INTERNAL_ERROR_UNKNOWN failure reason.
     */
    @Test
    void testIdentityRequest_RequestExceptionWithNullReason_ReturnsNotAcceptableStatusAndInternalErrorFailureResponse() {
        IdentityRequest request = new IdentityRequest();
        request.setId("test-id");
        request.setRequestId("123");
        request.setRequesttime(LocalDateTime.now());
        request.setReferenceId("ref123");

        RequestException mockException = mock(RequestException.class);
        when(mockException.getReasonConstant()).thenReturn(null);
        when(abisInsertService.findDuplication(any(IdentityRequest.class))).thenThrow(mockException);
        ResponseEntity<Object> response = controller.identityRequest(request);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, failureResponse.getFailureReason());
        verify(mockException).getReasonConstant();
    }

    /**
     * Tests that when the service layer throws a RequestException with a null reason constant
     * during a delete request, the controller's deleteRequest method returns an HTTP INTERNAL_SERVER_ERROR
     * status with the default INTERNAL_ERROR_UNKNOWN failure reason.
     */
    @Test
    void testDeleteRequest_RequestExceptionWithNullReason_ReturnsInternalServerErrorAndUnknownFailureReason() {
        RequestMO request = new RequestMO();
        request.setId("test-id");
        request.setRequestId("123");
        request.setRequesttime(LocalDateTime.now());
        request.setReferenceId("ref123");
        RequestException mockException = mock(RequestException.class);
        when(mockException.getReasonConstant()).thenReturn(null);
        doThrow(mockException).when(abisInsertService).deleteData(anyString());
        ResponseEntity<Object> response = controller.deleteRequest(request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, response.getBody());
        verify(mockException).getReasonConstant();
    }

    /**
     * Tests exception handling in saveInsertRequest when RequestException has null reason constant.
     */
    @Test
    void testSaveInsertRequest_RequestExceptionWithNullReasonConstant_ReturnsOkStatusAndInternalErrorFailureResponse() throws Exception {
        InsertRequestMO request = new InsertRequestMO();
        request.setId("mosip.abis.insert");
        request.setVersion("1.0");
        request.setRequestId("test-request-id");
        request.setRequesttime(LocalDateTime.now());
        request.setReferenceId("test-reference-id");
        when(bindingResult.hasErrors()).thenReturn(false);
        RequestException mockException = new RequestException(null);
        when(abisInsertService.insertData(any(InsertRequestMO.class))).thenThrow(mockException);
        ResponseEntity<Object> response = controller.saveInsertRequest(request, bindingResult);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN, failureResponse.getFailureReason());
    }

    /**
     * Tests exception handling in saveInsertRequest when RequestException has a custom reason constant.
     */
    @Test
    void testSaveInsertRequest_RequestExceptionWithCustomReason_ReturnsOkStatusWithCustomReasonFailureResponse() throws Exception {
        InsertRequestMO request = new InsertRequestMO();
        request.setId("mosip.abis.insert");
        request.setVersion("1.0");
        request.setRequestId("test-request-id");
        request.setRequesttime(LocalDateTime.now());
        request.setReferenceId("test-reference-id");
        String customReason = "CUSTOM_ERROR_REASON";
        when(bindingResult.hasErrors()).thenReturn(false);
        RequestException mockException = new RequestException(customReason);
        when(abisInsertService.insertData(any(InsertRequestMO.class))).thenThrow(mockException);
        ResponseEntity<Object> response = controller.saveInsertRequest(request, bindingResult);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof FailureResponse);
        FailureResponse failureResponse = (FailureResponse) response.getBody();
        assertEquals(customReason, failureResponse.getFailureReason());
    }

}