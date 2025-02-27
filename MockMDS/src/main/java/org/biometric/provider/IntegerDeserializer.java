package org.biometric.provider;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class IntegerDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        // Get the JSON node for the timeout field
        JsonNode node = jp.getCodec().readTree(jp);

        // Check if the node is a number (not a string)
        if (node.isNumber()) {
            throw new IOException(jp.getCurrentName() + " must be a string, not a number.");
        }

        // Return the string value if it is already a string
        return node.asText();
    }
}