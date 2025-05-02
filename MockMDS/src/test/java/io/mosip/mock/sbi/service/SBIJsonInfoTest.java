package io.mosip.mock.sbi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;

/**
 * Test class for SBIJsonInfo
 * Tests the JSON generation and error handling functionality
 */
class SBIJsonInfoTest {

    private static final String ERROR_CODE = "101";
    private static final String LANG = "en";
    private static final String EXCEPTION_MESSAGE = "Test exception";
    private static final String ERROR_DESCRIPTION = "Error description";

    /**
     * Tests the getErrorJson method
     * Verifies that the generated error JSON contains:
     * - The error code
     * - The exception message
     */
    @Test
    void testGetErrorJson() {
        String errorJson = SBIJsonInfo.getErrorJson(LANG, ERROR_CODE, EXCEPTION_MESSAGE);
        assertNotNull(errorJson);
        assertTrue(errorJson.contains(ERROR_CODE));
        assertTrue(errorJson.contains(EXCEPTION_MESSAGE));
    }

    /**
     * Tests the getAdminApiErrorJson method
     * Verifies that the generated admin API error JSON contains:
     * - The error code
     * - The exception message
     */
    @Test
    void testGetAdminApiErrorJson() {
        String errorJson = SBIJsonInfo.getAdminApiErrorJson(LANG, ERROR_CODE, EXCEPTION_MESSAGE);
        assertNotNull(errorJson);
        assertTrue(errorJson.contains(ERROR_CODE));
        assertTrue(errorJson.contains(EXCEPTION_MESSAGE));
    }

    /**
     * Tests the getStreamErrorJson method
     * Verifies that the generated stream error JSON contains:
     * - The error code
     * - The exception message
     */
    @Test
    void testGetStreamErrorJson() {
        String errorJson = SBIJsonInfo.getStreamErrorJson(LANG, ERROR_CODE, EXCEPTION_MESSAGE);
        assertNotNull(errorJson);
        assertTrue(errorJson.contains(ERROR_CODE));
        assertTrue(errorJson.contains(EXCEPTION_MESSAGE));
    }

    /**
     * Tests the getCaptureErrorJson method
     * Verifies that the generated capture error JSON contains:
     * - The error code
     * - The exception message
     * - The spec version
     */
    @Test
    void testGetCaptureErrorJson() {
        String specVersion = "1.0";
        String errorJson = SBIJsonInfo.getCaptureErrorJson(specVersion, LANG, ERROR_CODE, EXCEPTION_MESSAGE, true);
        assertNotNull(errorJson);
        assertTrue(errorJson.contains(ERROR_CODE));
        assertTrue(errorJson.contains(EXCEPTION_MESSAGE));
        assertTrue(errorJson.contains(specVersion));
    }

    /**
     * Tests getErrorDescription with valid language parameter
     * Verifies that the correct error description is returned when:
     * - A valid language code is provided
     * - The property helper returns a valid description
     */
    @Test
    void testGetErrorDescription_WithValidLang() {
        try (MockedStatic<ApplicationPropertyHelper> mockedStatic = mockStatic(ApplicationPropertyHelper.class)) {
            mockedStatic.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(anyString()))
                    .thenReturn(ERROR_DESCRIPTION);

            String description = SBIJsonInfo.getErrorDescription(LANG, ERROR_CODE);
            assertEquals(ERROR_DESCRIPTION, description);
        }
    }

    /**
     * Tests getErrorDescription with null language parameter
     * Verifies that:
     * - The method handles null language gracefully
     * - Default language (en) is used
     * - Correct error description is returned
     */
    @Test
    void testGetErrorDescription_WithNullLang() {
        try (MockedStatic<ApplicationPropertyHelper> mockedStatic = mockStatic(ApplicationPropertyHelper.class)) {
            mockedStatic.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(anyString()))
                    .thenReturn(ERROR_DESCRIPTION);

            String description = SBIJsonInfo.getErrorDescription(null, ERROR_CODE);
            assertEquals(ERROR_DESCRIPTION, description);
        }
    }

    /**
     * Tests getErrorDescription when no description is available
     * Verifies that:
     * - When property helper returns empty string
     * - Default "No Description available." message is returned
     */
    @Test
    void testGetErrorDescription_WithEmptyDescription() {
        try (MockedStatic<ApplicationPropertyHelper> mockedStatic = mockStatic(ApplicationPropertyHelper.class)) {
            mockedStatic.when(() -> ApplicationPropertyHelper.getPropertyKeyValue(anyString()))
                    .thenReturn("");

            String description = SBIJsonInfo.getErrorDescription(LANG, ERROR_CODE);
            assertEquals("No Description available.", description);
        }
    }
}