package io.mosip.mock.mv.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import io.mosip.mock.mv.constant.MVErrorCode;
import io.mosip.mock.mv.dto.ConfigureDto;
import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.exception.MVException;
import io.mosip.mock.mv.queue.Listener;
import io.mosip.mock.mv.service.MockMvDecisionService;
import org.mockito.MockedStatic;

class MockMvConfigControllerTest {

    @Mock
    private MockMvDecisionService mockMvDecisionService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private MockMvConfigController controller;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes Mockito mocks and injects them into the controller instance.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new MockMvConfigController(mockMvDecisionService);
    }

    /**
     * Tests the configure method for a successful configuration update scenario.
     * Verifies that when a valid ConfigureDto is provided without binding errors,
     * the method returns HTTP 200 status with success message and properly
     * interacts with the mock service.
     */
    @Test
    void configureMethod_SuccessfulConfigurationUpdate_ValidatesResponse() {
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("ACCEPT");
        when(bindingResult.hasErrors()).thenReturn(false);
        doNothing().when(mockMvDecisionService).setMockMvDecision(anyString());
        when(mockMvDecisionService.getMockMvDecision()).thenReturn("ACCEPT");

        ResponseEntity<String> response = controller.configure(configureDto, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully updated the configuration", response.getBody());
        verify(mockMvDecisionService).setMockMvDecision("ACCEPT");
        verify(mockMvDecisionService).getMockMvDecision();
    }

    /**
     * Tests the configure method when the service throws an exception during configuration.
     * Verifies that when the mock service throws a RuntimeException, the method
     * properly handles it by throwing an MVException with the appropriate error code
     * and message.
     */
    @Test
    void configureMethod_ServiceThrowsException_ThrowsMVException() {
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("ACCEPT");
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new RuntimeException("Test Exception")).when(mockMvDecisionService).setMockMvDecision(anyString());

        MVException exception = assertThrows(MVException.class, () -> controller.configure(configureDto, bindingResult));

        assertEquals(MVErrorCode.CONFIGURE_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.CONFIGURE_EXCEPTION.getErrorMessage()));
    }

    /**
     * Tests the checkConfiguration method for successfully retrieving the current configuration.
     * Verifies that when the service returns a valid configuration, the method returns
     * HTTP 200 status with the correct ConfigureDto containing the mock decision value.
     */
    @Test
    void checkConfigurationMethod_SuccessfulRetrieval_ReturnsValidConfiguration() {
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("ACCEPT");
        when(mockMvDecisionService.getMockMvDecision()).thenReturn("ACCEPT");

        ResponseEntity<ConfigureDto> response = controller.checkConfiguration();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCEPT", response.getBody().getMockMvDescision());
        verify(mockMvDecisionService).getMockMvDecision();
    }

    /**
     * Tests the checkConfiguration method when the service throws an exception during retrieval.
     * Verifies that when the mock service throws a RuntimeException while getting the configuration,
     * the method properly handles it by throwing an MVException with the appropriate error code
     * and message.
     */
    @Test
    void checkConfigurationMethod_ServiceThrowsException_ThrowsMVException() {
        when(mockMvDecisionService.getMockMvDecision()).thenThrow(new RuntimeException("Test Exception"));

        MVException exception = assertThrows(MVException.class, () -> controller.checkConfiguration());

        assertEquals(MVErrorCode.CONFIGURE_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.CONFIGURE_EXCEPTION.getErrorMessage()));
    }

    /**
     * Tests the setExpectation method for successfully setting an expectation.
     * Verifies that when a valid Expectation object is provided, the method returns
     * HTTP 200 status with success message and properly interacts with the service
     * and static Listener utility for JSON conversion.
     */
    @Test
    void setExpectationMethod_SuccessfulExpectationSetting_ReturnsSuccessResponse() {
        try (MockedStatic<Listener> mockedStatic = mockStatic(Listener.class)) {
            Expectation expectation = new Expectation();
            expectation.setRId("123");
            mockedStatic.when(() -> Listener.javaObjectToJsonString(any(Expectation.class)))
                    .thenReturn("{\"rId\":\"123\"}");
            doNothing().when(mockMvDecisionService).setExpectation(any(Expectation.class));

            ResponseEntity<String> response = controller.setExpectation(expectation);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Successfully inserted expectation 123", response.getBody());
            verify(mockMvDecisionService).setExpectation(expectation);
            mockedStatic.verify(() -> Listener.javaObjectToJsonString(expectation));
        }
    }

    /**
     * Tests the setExpectation method when an exception is thrown during JSON conversion.
     * Verifies that when the Listener utility throws a RuntimeException during JSON conversion,
     * the method properly handles it by throwing an MVException with the appropriate error code
     * and message.
     */
    @Test
    void setExpectationMethod_JsonConversionThrowsException_ThrowsMVException() {
        try (MockedStatic<Listener> mockedStatic = mockStatic(Listener.class)) {
            Expectation expectation = new Expectation();
            expectation.setRId("123");
            mockedStatic.when(() -> Listener.javaObjectToJsonString(any(Expectation.class)))
                    .thenThrow(new RuntimeException("Test Exception"));

            MVException exception = assertThrows(MVException.class, () -> controller.setExpectation(expectation));

            assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage()));
        }
    }

    /**
     * Tests the getExpectation method for successfully retrieving all expectations.
     * Verifies that when the service returns a valid map of expectations, the method
     * returns HTTP 200 status with the complete expectations map.
     */
    @Test
    void getExpectationMethod_AllExpectations_ReturnsAllExpectations() {
        Map<String, Expectation> expectations = new HashMap<>();
        Expectation expectation = new Expectation();
        expectation.setRId("123");
        expectations.put("123", expectation);
        when(mockMvDecisionService.getExpectations()).thenReturn(expectations);

        ResponseEntity<Map<String, Expectation>> response = controller.getExpectation();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectations, response.getBody());
        verify(mockMvDecisionService).getExpectations();
    }

    /**
     * Tests the getExpectation method when an exception is thrown while retrieving all expectations.
     * Verifies that when the service throws a RuntimeException during expectations retrieval,
     * the method properly handles it by throwing an MVException with the appropriate error code
     * and message.
     */
    @Test
    void getExpectationMethod_AllExpectations_ServiceThrowsException_ThrowsMVException() {
        when(mockMvDecisionService.getExpectations()).thenThrow(new RuntimeException("Test Exception"));

        MVException exception = assertThrows(MVException.class, () -> controller.getExpectation());

        assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage()));
    }

    /**
     * Tests the getExpectation method for successfully retrieving an expectation by rid.
     * Verifies that when the service returns a valid expectation for a given rid,
     * the method returns HTTP 200 status with the expectation's string representation.
     */
    @Test
    void getExpectationMethod_ByRid_SuccessfulRetrieval_ReturnsExpectation() {
        Expectation expectation = new Expectation();
        expectation.setRId("123");
        when(mockMvDecisionService.getExpectation("123")).thenReturn(expectation);

        ResponseEntity<String> response = controller.getExpectation("123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectation.toString(), response.getBody());
        verify(mockMvDecisionService, times(2)).getExpectation("123");
    }

    /**
     * Tests the getExpectation method when no expectation is found for the given rid.
     * Verifies that when the service returns an empty expectation for a given rid,
     * the method returns HTTP 200 status with a "no expectation found" message.
     */
    @Test
    void getExpectationMethod_ByRid_ExpectationNotFound_ReturnsNotFoundMessage() {
        Expectation expectation = new Expectation();
        when(mockMvDecisionService.getExpectation("123")).thenReturn(expectation);

        ResponseEntity<String> response = controller.getExpectation("123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("No expectation set for given rid:123", response.getBody());
        verify(mockMvDecisionService).getExpectation("123");
    }

    /**
     * Tests the getExpectation method when an exception is thrown while retrieving by rid.
     * Verifies that when the service throws a RuntimeException during expectation retrieval
     * for a specific rid, the method properly handles it by throwing an MVException with
     * the appropriate error code and message.
     */
    @Test
    void getExpectationMethod_ByRid_ServiceThrowsException_ThrowsMVException() {
        when(mockMvDecisionService.getExpectation("123")).thenThrow(new RuntimeException("Test Exception"));

        MVException exception = assertThrows(MVException.class, () -> controller.getExpectation("123"));

        assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage()));
        verify(mockMvDecisionService).getExpectation("123");
    }

    /**
     * Tests the deleteExpectation method for successfully deleting an expectation by rid.
     * Verifies that when the service successfully deletes an expectation for a given rid,
     * the method returns HTTP 200 status with a success message.
     */
    @Test
    void deleteExpectationMethod_SuccessfulDeletion_ReturnsSuccessMessage() {
        doNothing().when(mockMvDecisionService).deleteExpectation("123");

        ResponseEntity<String> response = controller.deleteExpectation("123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted expectation 123", response.getBody());
        verify(mockMvDecisionService).deleteExpectation("123");
    }

    /**
     * Tests the deleteExpectation method when an exception is thrown by the service.
     * Verifies that when the service throws a RuntimeException during expectation deletion,
     * the method properly handles it by throwing an MVException with the appropriate
     * error code and message.
     */
    @Test
    void deleteExpectationMethod_ServiceThrowsException_ThrowsMVException() {
        doThrow(new RuntimeException("Test Exception")).when(mockMvDecisionService).deleteExpectation("123");

        MVException exception = assertThrows(MVException.class, () -> controller.deleteExpectation("123"));

        assertEquals(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorMessage()));
    }

    /**
     * Tests the deleteAllExpectations method for successfully deleting all expectations.
     * Verifies that when the service successfully deletes all expectations,
     * the method returns HTTP 200 status with a success message.
     */
    @Test
    void deleteAllExpectationsMethod_SuccessfulDeletion_ReturnsSuccessMessage() {
        doNothing().when(mockMvDecisionService).deleteExpectations();

        ResponseEntity<String> response = controller.deleteAllExpectations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted expectations ", response.getBody());
        verify(mockMvDecisionService).deleteExpectations();
    }

    /**
     * Tests the deleteAllExpectations method when an exception is thrown by the service.
     * Verifies that when the service throws a RuntimeException during deletion of all expectations,
     * the method properly handles it by throwing an MVException with the appropriate
     * error code and message.
     */
    @Test
    void deleteAllExpectationsMethod_ServiceThrowsException_ThrowsMVException() {
        doThrow(new RuntimeException("Test Exception")).when(mockMvDecisionService).deleteExpectations();

        MVException exception = assertThrows(MVException.class, () -> controller.deleteAllExpectations());

        assertEquals(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorMessage()));
    }
}
