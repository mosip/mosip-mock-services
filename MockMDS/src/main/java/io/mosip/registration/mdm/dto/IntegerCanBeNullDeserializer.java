package io.mosip.registration.mdm.dto;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class IntegerCanBeNullDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        // Get the JSON node for the timeout field
        JsonNode node = jp.getCodec().readTree(jp);

        String fieldName = jp.getCurrentName();

        if (node == null || node.isNull()) {
            return null;
        }
        
        // Check if the node is a number (not a string)
        if (node.isNumber()) {
            throw new IOException(fieldName + " must be a string, not a number.");
        }

        String value = node.asText();
        if (value.trim().isEmpty()) {
            throw new IOException(fieldName + " must not be empty.");
        }

        return value;
    }
}