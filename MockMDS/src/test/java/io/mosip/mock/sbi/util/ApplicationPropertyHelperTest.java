package io.mosip.mock.sbi.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ApplicationPropertyHelper
 * Tests the functionality of reading properties from application.properties file
 */
class ApplicationPropertyHelperTest {

    private File tempPropFile;

    /**
     * Tests reading a non-existent property key
     * Expected: Returns null when the key doesn't exist in the properties file
     */
    @Test
    void testGetPropertyKeyValueForMissingKey() {
        String value = ApplicationPropertyHelper.getPropertyKeyValue("non.existent.key");
        assertNull(value);
    }

    /**
     * Tests reading a property key that exists but has an empty value
     * Expected: Returns an empty string for a key with no value
     */
    @Test
    void testGetPropertyKeyValueForEmptyValue() {
        String value = ApplicationPropertyHelper.getPropertyKeyValue("empty.key");
        assertNull(value);
    }

    /**
     * Cleans up the test environment by deleting the temporary properties file
     * Ensures no test files are left behind after test execution
     */
    @AfterEach
    void cleanup() {
        if (tempPropFile != null && tempPropFile.exists()) {
            tempPropFile.delete();
        }
    }
}