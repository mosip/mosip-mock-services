package org.biometric.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegerDeserializerTest {

    private final ObjectMapper mapper;

    /**
     * Initializes the ObjectMapper and registers the custom IntegerDeserializer
     * for String.class.
     */
    public IntegerDeserializerTest() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new IntegerDeserializer());
        mapper.registerModule(module);
    }

    /**
     * Tests that a JSON string value is correctly deserialized to a Java String.
     */
    @Test
    void deserializeString_success() throws IOException {
        String json = "\"12345\"";
        String result = mapper.readValue(json, String.class);
        assertEquals("12345", result);
    }

    /**
     * Tests that a numeric JSON value throws an IOException during deserialization,
     * as only string values are allowed.
     */
    @Test
    void deserializeNumber_throwsIOException() {
        String json = "12345";
        Exception exception = assertThrows(IOException.class, () -> {
            mapper.readValue(json, String.class);
        });
        assertTrue(exception.getMessage().contains("must be a string"));
    }
}
