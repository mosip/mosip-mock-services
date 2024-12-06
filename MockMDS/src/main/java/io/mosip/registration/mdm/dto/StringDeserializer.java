package io.mosip.registration.mdm.dto;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class StringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        // Get the JSON node for the timeout field
        JsonNode node = jp.getCodec().readTree(jp);

        // Retrieve the field name for better error context
        String fieldName = jp.getCurrentName();
        
        // Check if the node is null
        if (node == null || node.isNull()) {
            throw new IOException(fieldName + " must not be null.");
        }
        
        // Check if the node is a number (not a string)
        if (node.isNumber()) {
            throw new IOException(fieldName + " must be a string, not a number.");
        }

        // Check if the node is an empty string
        String value = node.asText();
        if (value == null || value.trim().isEmpty()) {
            throw new IOException(fieldName + " must not be null or empty.");
        }
        
        // Return the validated string value
        return value;
    }
}