package io.mosip.mock.sbi.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for ApplicationPropertyHelper
 * Tests the functionality of reading properties from application.properties file
 */
class ApplicationPropertyHelperTest {

    private File tempPropFile;

    /**
     * Tests reading a non-existent property key.
     * Expected: Returns null when the key doesn't exist in the properties file.
     */
    @Test
    void getPropertyKeyValue_missingKey_returnsNull() {
        String value = ApplicationPropertyHelper.getPropertyKeyValue("non.existent.key");
        assertNull(value);
    }

    /**
     * Tests reading a property key that exists but has an empty value.
     * Expected: Returns null for a key with no value.
     */
    @Test
    void getPropertyKeyValue_emptyValue_returnsNull() {
        String value = ApplicationPropertyHelper.getPropertyKeyValue("empty.key");
        assertNull(value);
    }

    /**
     * Cleans up the test environment by deleting the temporary properties file.
     * Ensures no test files are left behind after test execution.
     */
    @AfterEach
    void cleanup() {
        if (tempPropFile != null && tempPropFile.exists()) {
            tempPropFile.delete();
        }
    }
}
