package io.mosip.mock.mv.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
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

    @BeforeEach
    void setUp() {
        // Initialize mocks and inject them into the controller before each test
        MockitoAnnotations.openMocks(this);
        controller = new MockMvConfigController(mockMvDecisionService);
    }

    // Test the configure method for a successful configuration update
    @Test
    void configure_Success() {
        // Arrange: Set up a valid ConfigureDto and mock the service behavior
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("ACCEPT");
        when(bindingResult.hasErrors()).thenReturn(false);
        doNothing().when(mockMvDecisionService).setMockMvDecision(anyString());
        when(mockMvDecisionService.getMockMvDecision()).thenReturn("ACCEPT");

        // Act: Call the configure method
        ResponseEntity<String> response = controller.configure(configureDto, bindingResult);

        // Assert: Verify the response status, body, and that the service methods were called
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully updated the configuration", response.getBody());
        verify(mockMvDecisionService).setMockMvDecision("ACCEPT");
        verify(mockMvDecisionService).getMockMvDecision();
    }

    // Test the configure method when an exception is thrown by the service
    @Test
    void configure_ExceptionThrown() {
        // Arrange: Set up a ConfigureDto and simulate an exception in the service
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("ACCEPT");
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new RuntimeException("Test Exception")).when(mockMvDecisionService).setMockMvDecision(anyString());

        // Act & Assert: Verify that an MVException is thrown with the correct error code and message
        MVException exception = assertThrows(MVException.class, () -> controller.configure(configureDto, bindingResult));

        assertEquals(MVErrorCode.CONFIGURE_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.CONFIGURE_EXCEPTION.getErrorMessage()));
    }

    // Test the checkConfiguration method for successfully retrieving the current configuration
    @Test
    void checkConfiguration_Success() {
        // Arrange: Mock the service to return a valid configuration
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("ACCEPT");
        when(mockMvDecisionService.getMockMvDecision()).thenReturn("ACCEPT");

        // Act: Call the checkConfiguration method
        ResponseEntity<ConfigureDto> response = controller.checkConfiguration();

        // Assert: Verify the response status, body, and service interaction
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCEPT", response.getBody().getMockMvDescision());
        verify(mockMvDecisionService).getMockMvDecision();
    }

    // Test the checkConfiguration method when an exception is thrown by the service
    @Test
    void checkConfiguration_ExceptionThrown() {
        // Arrange: Simulate an exception in the service
        when(mockMvDecisionService.getMockMvDecision()).thenThrow(new RuntimeException("Test Exception"));

        // Act & Assert: Verify that an MVException is thrown with the correct error code and message
        MVException exception = assertThrows(MVException.class, () -> controller.checkConfiguration());

        assertEquals(MVErrorCode.CONFIGURE_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.CONFIGURE_EXCEPTION.getErrorMessage()));
    }

    // Test the setExpectation method for successfully setting an expectation
    @Test
    void setExpectation_Success() {
        // Arrange: Mock the Listener class and service behavior for a valid expectation
        try (MockedStatic<Listener> mockedStatic = mockStatic(Listener.class)) {
            Expectation expectation = new Expectation();
            expectation.setRId("123");
            mockedStatic.when(() -> Listener.javaObjectToJsonString(any(Expectation.class)))
                    .thenReturn("{\"rId\":\"123\"}");
            doNothing().when(mockMvDecisionService).setExpectation(any(Expectation.class));

            // Act: Call the setExpectation method
            ResponseEntity<String> response = controller.setExpectation(expectation);

            // Assert: Verify the response status, body, and interactions with mocks
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Successfully inserted expectation 123", response.getBody());
            verify(mockMvDecisionService).setExpectation(expectation);
            mockedStatic.verify(() -> Listener.javaObjectToJsonString(expectation));
        }
    }

    // Test the setExpectation method when an exception is thrown during JSON conversion
    @Test
    void setExpectation_ExceptionThrown() {
        // Arrange: Mock the Listener class to throw an exception
        try (MockedStatic<Listener> mockedStatic = mockStatic(Listener.class)) {
            Expectation expectation = new Expectation();
            expectation.setRId("123");
            mockedStatic.when(() -> Listener.javaObjectToJsonString(any(Expectation.class)))
                    .thenThrow(new RuntimeException("Test Exception"));

            // Act & Assert: Verify that an MVException is thrown with the correct error code and message
            MVException exception = assertThrows(MVException.class, () -> controller.setExpectation(expectation));

            assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
            assertTrue(exception.getMessage().contains(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage()));
        }
    }

    // Test the getExpectation method for successfully retrieving all expectations
    @Test
    void getExpectation_AllExpectations_Success() {
        // Arrange: Mock the service to return a map of expectations
        Map<String, Expectation> expectations = new HashMap<>();
        Expectation expectation = new Expectation();
        expectation.setRId("123");
        expectations.put("123", expectation);
        when(mockMvDecisionService.getExpectations()).thenReturn(expectations);

        // Act: Call the getExpectation method without an rid
        ResponseEntity<Map<String, Expectation>> response = controller.getExpectation();

        // Assert: Verify the response status, body, and service interaction
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectations, response.getBody());
        verify(mockMvDecisionService).getExpectations();
    }

    // Test the getExpectation method when an exception is thrown while retrieving all expectations
    @Test
    void getExpectation_AllExpectations_ExceptionThrown() {
        // Arrange: Simulate an exception in the service
        when(mockMvDecisionService.getExpectations()).thenThrow(new RuntimeException("Test Exception"));

        // Act & Assert: Verify that an MVException is thrown with the correct error code and message
        MVException exception = assertThrows(MVException.class, () -> controller.getExpectation());

        assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage()));
    }

    // Test the getExpectation method for successfully retrieving an expectation by rid
    @Test
    void getExpectation_ByRid_Success() {
        // Arrange: Mock the service to return a valid expectation for a given rid
        Expectation expectation = new Expectation();
        expectation.setRId("123");
        when(mockMvDecisionService.getExpectation("123")).thenReturn(expectation);

        // Act: Call the getExpectation method with an rid
        ResponseEntity<String> response = controller.getExpectation("123");

        // Assert: Verify the response status, body, and service interaction
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectation.toString(), response.getBody());
        verify(mockMvDecisionService, times(2)).getExpectation("123"); // Expect two calls due to controller logic
    }

    // Test the getExpectation method when no expectation is found for the given rid
    @Test
    void getExpectation_ByRid_NotFound() {
        // Arrange: Mock the service to return an empty expectation
        Expectation expectation = new Expectation();
        when(mockMvDecisionService.getExpectation("123")).thenReturn(expectation);

        // Act: Call the getExpectation method with an rid
        ResponseEntity<String> response = controller.getExpectation("123");

        // Assert: Verify the response status, body, and service interaction
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("No expectation set for given rid:123", response.getBody());
        verify(mockMvDecisionService).getExpectation("123"); // Expect one call
    }

    // Test the getExpectation method when an exception is thrown while retrieving by rid
    @Test
    void getExpectation_ByRid_ExceptionThrown() {
        // Arrange: Simulate an exception in the service for a specific rid
        when(mockMvDecisionService.getExpectation("123")).thenThrow(new RuntimeException("Test Exception"));

        // Act & Assert: Verify that an MVException is thrown with the correct error code and message
        MVException exception = assertThrows(MVException.class, () -> controller.getExpectation("123"));

        assertEquals(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage()));
        verify(mockMvDecisionService).getExpectation("123"); // Expect one call
    }

    // Test the deleteExpectation method for successfully deleting an expectation by rid
    @Test
    void deleteExpectation_Success() {
        // Arrange: Mock the service to handle deletion without errors
        doNothing().when(mockMvDecisionService).deleteExpectation("123");

        // Act: Call the deleteExpectation method
        ResponseEntity<String> response = controller.deleteExpectation("123");

        // Assert: Verify the response status, body, and service interaction
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted expectation 123", response.getBody());
        verify(mockMvDecisionService).deleteExpectation("123");
    }

    // Test the deleteExpectation method when an exception is thrown by the service
    @Test
    void deleteExpectation_ExceptionThrown() {
        // Arrange: Simulate an exception in the service during deletion
        doThrow(new RuntimeException("Test Exception")).when(mockMvDecisionService).deleteExpectation("123");

        // Act & Assert: Verify that an MVException is thrown with the correct error code and message
        MVException exception = assertThrows(MVException.class, () -> controller.deleteExpectation("123"));

        assertEquals(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorMessage()));
    }

    // Test the deleteAllExpectations method for successfully deleting all expectations
    @Test
    void deleteAllExpectations_Success() {
        // Arrange: Mock the service to handle deletion of all expectations without errors
        doNothing().when(mockMvDecisionService).deleteExpectations();

        // Act: Call the deleteAllExpectations method
        ResponseEntity<String> response = controller.deleteAllExpectations();

        // Assert: Verify the response status, body, and service interaction
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully deleted expectations ", response.getBody());
        verify(mockMvDecisionService).deleteExpectations();
    }

    // Test the deleteAllExpectations method when an exception is thrown by the service
    @Test
    void deleteAllExpectations_ExceptionThrown() {
        // Arrange: Simulate an exception in the service during deletion of all expectations
        doThrow(new RuntimeException("Test Exception")).when(mockMvDecisionService).deleteExpectations();

        // Act & Assert: Verify that an MVException is thrown with the correct error code and message
        MVException exception = assertThrows(MVException.class, () -> controller.deleteAllExpectations());

        assertEquals(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(), exception.getErrorCode());
        assertTrue(exception.getMessage().contains(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorMessage()));
    }
}