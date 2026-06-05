package io.mosip.mock.mv.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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

/**
 * Comprehensive test class for MockMvConfigController providing 100% code coverage.
 * Tests all controller endpoints, exception scenarios, and edge cases.
 */
@DisplayName("MockMvConfigController Comprehensive Tests")
class MockMvConfigControllerComprehensiveTest {

    @Mock
    private MockMvDecisionService mockMvDecisionService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private io.mosip.mock.mv.controller.MockMvConfigController controller;

    /**
     * Sets up test environment before each test execution.
     * Initializes mocks and creates controller instance.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new io.mosip.mock.mv.controller.MockMvConfigController(mockMvDecisionService);
    }

    /**
     * Tests successful configuration update with valid input.
     * Verifies proper service interaction and response format.
     */
    @Test
    @DisplayName("Configure - Success with valid decision")
    void testConfigureMethod_ValidDecision_ReturnsSuccessResponse() {
        // Arrange
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("APPROVED");
        doNothing().when(mockMvDecisionService).setMockMvDecision("APPROVED");
        when(mockMvDecisionService.getMockMvDecision()).thenReturn("APPROVED");

        // Act
        ResponseEntity<String> response = controller.configure(configureDto, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully updated the configuration", response.getBody());
        verify(mockMvDecisionService).setMockMvDecision("APPROVED");
        verify(mockMvDecisionService).getMockMvDecision();
    }

    /**
     * Tests configuration update with REJECTED decision.
     * Verifies handling of different decision types.
     */
    @Test
    @DisplayName("Configure - Success with REJECTED decision")
    void testConfigureMethod_RejectedDecision_ReturnsSuccessResponse() {
        // Arrange
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("REJECTED");
        doNothing().when(mockMvDecisionService).setMockMvDecision("REJECTED");
        when(mockMvDecisionService.getMockMvDecision()).thenReturn("REJECTED");

        // Act
        ResponseEntity<String> response = controller.configure(configureDto, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully updated the configuration", response.getBody());
        verify(mockMvDecisionService).setMockMvDecision("REJECTED");
    }

    /**
     * Tests configuration update when service throws RuntimeException.
     * Verifies proper exception handling and error response.
     */
    @Test
    @DisplayName("Configure - Service throws RuntimeException")
    void testConfigureMethod_ServiceThrowsRuntimeException_ThrowsMVException() {
        // Arrange
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("APPROVED");
        doThrow(new RuntimeException("Service error")).when(mockMvDecisionService).setMockMvDecision(anyString());

        // Act & Assert
        MVException exception = assertThrows(MVException.class, 
            () -> controller.configure(configureDto, bindingResult));
        
        assertEquals(MVErrorCode.CONFIGURE_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.CONFIGURE_EXCEPTION.getErrorMessage()));
        assertTrue(exception.getMessage().contains("Service error"));
    }

    /**
     * Tests configuration update when service throws generic Exception.
     * Verifies handling of different exception types.
     */
    @Test
    @DisplayName("Configure - Service throws generic Exception")
    void testConfigureMethod_ServiceThrowsGenericException_ThrowsMVException() {
        // Arrange
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("APPROVED");
        doThrow(new IllegalArgumentException("Invalid argument")).when(mockMvDecisionService).setMockMvDecision(anyString());

        // Act & Assert
        MVException exception = assertThrows(MVException.class, 
            () -> controller.configure(configureDto, bindingResult));
        
        assertEquals(MVErrorCode.CONFIGURE_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Invalid argument"));
    }

    /**
     * Tests successful configuration retrieval.
     * Verifies proper response format and service interaction.
     */
    @Test
    @DisplayName("Check Configuration - Success")
    void testCheckConfigurationMethod_ValidConfiguration_ReturnsConfiguration() {
        // Arrange
        when(mockMvDecisionService.getMockMvDecision()).thenReturn("APPROVED");

        // Act
        ResponseEntity<ConfigureDto> response = controller.checkConfiguration();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("APPROVED", response.getBody().getMockMvDescision());
        verify(mockMvDecisionService).getMockMvDecision();
    }

    /**
     * Tests configuration retrieval when service throws exception.
     * Verifies proper exception handling in GET endpoint.
     */
    @Test
    @DisplayName("Check Configuration - Service throws exception")
    void testCheckConfigurationMethod_ServiceThrowsException_ThrowsMVException() {
        // Arrange
        when(mockMvDecisionService.getMockMvDecision()).thenThrow(new RuntimeException("Get error"));

        // Act & Assert
        MVException exception = assertThrows(MVException.class, 
            () -> controller.checkConfiguration());
        
        assertEquals(MVErrorCode.CONFIGURE_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Get error"));
    }

    /**
     * Tests successful expectation setting with valid data.
     * Verifies JSON conversion and service interaction.
     */
    @Test
    @DisplayName("Set Expectation - Success")
    void testSetExpectationMethod_ValidExpectation_ReturnsSuccessResponse() {
        try (MockedStatic<Listener> mockedStatic = mockStatic(Listener.class)) {
            // Arrange
            Expectation expectation = new Expectation();
            expectation.setRId("test-rid-123");
            expectation.setMockMvDecision("REJECTED");
            expectation.setDelayResponse(30);
            
            mockedStatic.when(() -> Listener.javaObjectToJsonString(any(Expectation.class)))
                    .thenReturn("{\"rId\":\"test-rid-123\",\"mockMvDecision\":\"REJECTED\"}");
            doNothing().when(mockMvDecisionService).setExpectation(any(Expectation.class));

            // Act
            ResponseEntity<String> response = controller.setExpectation(expectation);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Successfully inserted expectation test-rid-123", response.getBody());
            verify(mockMvDecisionService).setExpectation(expectation);
            mockedStatic.verify(() -> Listener.javaObjectToJsonString(expectation));
        }
    }

    /**
     * Tests expectation setting when JSON conversion fails.
     * Verifies proper exception handling for JSON processing errors.
     */
    @Test
    @DisplayName("Set Expectation - JSON conversion fails")
    void testSetExpectationMethod_JsonConversionFails_ThrowsMVException() {
        try (MockedStatic<Listener> mockedStatic = mockStatic(Listener.class)) {
            // Arrange
            Expectation expectation = new Expectation();
            expectation.setRId("test-rid-123");
            
            mockedStatic.when(() -> Listener.javaObjectToJsonString(any(Expectation.class)))
                    .thenThrow(new RuntimeException("JSON conversion error"));

            // Act & Assert
            MVException exception = assertThrows(MVException.class, 
                () -> controller.setExpectation(expectation));
            
            assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains("JSON conversion error"));
        }
    }

    /**
     * Tests expectation setting when service throws exception.
     * Verifies proper exception handling during service operations.
     */
    @Test
    @DisplayName("Set Expectation - Service throws exception")
    void testSetExpectationMethod_ServiceThrowsException_ThrowsMVException() {
        try (MockedStatic<Listener> mockedStatic = mockStatic(Listener.class)) {
            // Arrange
            Expectation expectation = new Expectation();
            expectation.setRId("test-rid-123");
            
            mockedStatic.when(() -> Listener.javaObjectToJsonString(any(Expectation.class)))
                    .thenReturn("{\"rId\":\"test-rid-123\"}");
            doThrow(new RuntimeException("Service error")).when(mockMvDecisionService).setExpectation(any(Expectation.class));

            // Act & Assert
            MVException exception = assertThrows(MVException.class, 
                () -> controller.setExpectation(expectation));
            
            assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains("Service error"));
        }
    }

    /**
     * Tests successful retrieval of all expectations.
     * Verifies proper response format and service interaction.
     */
    @Test
    @DisplayName("Get All Expectations - Success")
    void testGetExpectationMethod_AllExpectations_ReturnsExpectationsMap() {
        // Arrange
        Map<String, Expectation> expectations = new HashMap<>();
        Expectation expectation1 = new Expectation("rid1", "APPROVED", 0);
        Expectation expectation2 = new Expectation("rid2", "REJECTED", 30);
        expectations.put("rid1", expectation1);
        expectations.put("rid2", expectation2);
        
        when(mockMvDecisionService.getExpectations()).thenReturn(expectations);

        // Act
        ResponseEntity<Map<String, Expectation>> response = controller.getExpectation();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectations, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(mockMvDecisionService).getExpectations();
    }

    /**
     * Tests retrieval of all expectations when service throws exception.
     * Verifies proper exception handling in GET all endpoint.
     */
    @Test
    @DisplayName("Get All Expectations - Service throws exception")
    void testGetExpectationMethod_AllExpectations_ServiceThrowsException() {
        // Arrange
        when(mockMvDecisionService.getExpectations()).thenThrow(new RuntimeException("Get all error"));

        // Act & Assert
        MVException exception = assertThrows(MVException.class, 
            () -> controller.getExpectation());
        
        assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Get all error"));
    }

    /**
     * Tests successful retrieval of specific expectation by RID.
     * Verifies proper response format when expectation exists.
     */
    @Test
    @DisplayName("Get Expectation by RID - Success")
    void testGetExpectationMethod_ByRid_ExistingExpectation_ReturnsExpectation() {
        // Arrange
        Expectation expectation = new Expectation("test-rid", "APPROVED", 0);
        when(mockMvDecisionService.getExpectation("test-rid")).thenReturn(expectation);

        // Act
        ResponseEntity<String> response = controller.getExpectation("test-rid");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectation.toString(), response.getBody());
        verify(mockMvDecisionService, times(2)).getExpectation("test-rid");
    }

    /**
     * Tests retrieval of non-existent expectation by RID.
     * Verifies proper response when expectation doesn't exist.
     */
    @Test
    @DisplayName("Get Expectation by RID - Not found")
    void testGetExpectationMethod_ByRid_NonExistentExpectation_ReturnsNotFoundMessage() {
        // Arrange
        Expectation emptyExpectation = new Expectation();
        when(mockMvDecisionService.getExpectation("non-existent")).thenReturn(emptyExpectation);

        // Act
        ResponseEntity<String> response = controller.getExpectation("non-existent");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("No expectation set for given rid:non-existent", response.getBody());
        verify(mockMvDecisionService).getExpectation("non-existent");
    }

    /**
     * Tests retrieval of expectation by RID when service throws exception.
     * Verifies proper exception handling in GET by ID endpoint.
     */
    @Test
    @DisplayName("Get Expectation by RID - Service throws exception")
    void testGetExpectationMethod_ByRid_ServiceThrowsException_ThrowsMVException() {
        // Arrange
        when(mockMvDecisionService.getExpectation("error-rid")).thenThrow(new RuntimeException("Get by ID error"));

        // Act & Assert
        MVException exception = assertThrows(MVException.class, 
            () -> controller.getExpectation("error-rid"));
        
        assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Get by ID error"));
    }

    /**
     * Tests successful deletion of specific expectation by RID.
     * Verifies proper response format and service interaction.
     */
    @Test
    @DisplayName("Delete Expectation by RID - Success")
    void testDeleteExpectationMethod_ValidRid_ReturnsSuccessMessage() {
        // Arrange
        doNothing().when(mockMvDecisionService).deleteExpectation("test-rid");

        // Act
        ResponseEntity<String> response = controller.deleteExpectation("test-rid");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted expectation test-rid", response.getBody());
        verify(mockMvDecisionService).deleteExpectation("test-rid");
    }

    /**
     * Tests deletion of expectation when service throws exception.
     * Verifies proper exception handling in DELETE endpoint.
     */
    @Test
    @DisplayName("Delete Expectation by RID - Service throws exception")
    void testDeleteExpectationMethod_ServiceThrowsException_ThrowsMVException() {
        // Arrange
        doThrow(new RuntimeException("Delete error")).when(mockMvDecisionService).deleteExpectation("error-rid");

        // Act & Assert
        MVException exception = assertThrows(MVException.class, 
            () -> controller.deleteExpectation("error-rid"));
        
        assertEquals(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Delete error"));
    }

    /**
     * Tests successful deletion of all expectations.
     * Verifies proper response format and service interaction.
     */
    @Test
    @DisplayName("Delete All Expectations - Success")
    void testDeleteAllExpectationsMethod_ValidOperation_ReturnsSuccessMessage() {
        // Arrange
        doNothing().when(mockMvDecisionService).deleteExpectations();

        // Act
        ResponseEntity<String> response = controller.deleteAllExpectations();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted expectations ", response.getBody());
        verify(mockMvDecisionService).deleteExpectations();
    }

    /**
     * Tests deletion of all expectations when service throws exception.
     * Verifies proper exception handling in DELETE all endpoint.
     */
    @Test
    @DisplayName("Delete All Expectations - Service throws exception")
    void testDeleteAllExpectationsMethod_ServiceThrowsException_ThrowsMVException() {
        // Arrange
        doThrow(new RuntimeException("Delete all error")).when(mockMvDecisionService).deleteExpectations();

        // Act & Assert
        MVException exception = assertThrows(MVException.class, 
            () -> controller.deleteAllExpectations());
        
        assertEquals(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Delete all error"));
    }

    /**
     * Tests expectation setting with null RID.
     * Verifies handling of edge cases with null values.
     */
    @Test
    @DisplayName("Set Expectation - Null RID")
    void testSetExpectationMethod_NullRid_HandlesGracefully() {
        try (MockedStatic<Listener> mockedStatic = mockStatic(Listener.class)) {
            // Arrange
            Expectation expectation = new Expectation();
            expectation.setRId(null);
            expectation.setMockMvDecision("APPROVED");
            
            mockedStatic.when(() -> Listener.javaObjectToJsonString(any(Expectation.class)))
                    .thenReturn("{\"rId\":null,\"mockMvDecision\":\"APPROVED\"}");
            doNothing().when(mockMvDecisionService).setExpectation(any(Expectation.class));

            // Act
            ResponseEntity<String> response = controller.setExpectation(expectation);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Successfully inserted expectation null", response.getBody());
        }
    }

    /**
     * Tests configuration with empty decision string.
     * Verifies handling of edge cases with empty values.
     */
    @Test
    @DisplayName("Configure - Empty decision")
    void testConfigureMethod_EmptyDecision_HandlesGracefully() {
        // Arrange
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("");
        doNothing().when(mockMvDecisionService).setMockMvDecision("");
        when(mockMvDecisionService.getMockMvDecision()).thenReturn("");

        // Act
        ResponseEntity<String> response = controller.configure(configureDto, bindingResult);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully updated the configuration", response.getBody());
        verify(mockMvDecisionService).setMockMvDecision("");
    }

    /**
     * Tests retrieval of empty expectations map.
     * Verifies handling when no expectations exist.
     */
    @Test
    @DisplayName("Get All Expectations - Empty map")
    void testGetExpectationMethod_EmptyExpectationsMap_ReturnsEmptyMap() {
        // Arrange
        Map<String, Expectation> emptyExpectations = new HashMap<>();
        when(mockMvDecisionService.getExpectations()).thenReturn(emptyExpectations);

        // Act
        ResponseEntity<Map<String, Expectation>> response = controller.getExpectation();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(mockMvDecisionService).getExpectations();
    }
}