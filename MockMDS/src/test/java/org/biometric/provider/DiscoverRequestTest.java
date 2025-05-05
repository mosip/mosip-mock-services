// Java
package org.biometric.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscoverRequestTest {

    private DiscoverRequest servlet;
    // Create one ObjectMapper instance to serialize digital ID maps identical to production
    private ObjectMapper objMapper = new ObjectMapper();

    @BeforeEach
    void setup() throws Exception {
        // Create a temporary base directory and set system property user.dir to its path
        Path baseDir = Files.createTempDirectory("testBaseDir");
        System.setProperty("user.dir", baseDir.toString());

        // Create the expected subdirectory structure: /files/MockMDS
        Path mockMdsDir = baseDir.resolve("files").resolve("MockMDS");
        Files.createDirectories(mockMdsDir);

        // Create Discover files (dummy contents; they are parsed into DiscoverDto)
        Files.writeString(mockMdsDir.resolve("DiscoverFIR.txt"), "{\"callbackId\":\"1\"}");
        Files.writeString(mockMdsDir.resolve("DiscoverIIR.txt"), "{\"callbackId\":\"2\"}");
        Files.writeString(mockMdsDir.resolve("DiscoverFACE.txt"), "{\"callbackId\":\"3\"}");

        // Create Digital ID files with expected deviceProviderId values
        Files.writeString(mockMdsDir.resolve("DigitalFingerId.txt"),
                "{\"deviceProvider\":\"dp\",\"deviceProviderId\":\"dev123\",\"make\":\"m\",\"serialNo\":\"s\",\"model\":\"md\",\"deviceSubType\":\"sub\",\"type\":\"FIR\"}");
        Files.writeString(mockMdsDir.resolve("DigitalIrisId.txt"),
                "{\"deviceProvider\":\"dp\",\"deviceProviderId\":\"iris123\",\"make\":\"m\",\"serialNo\":\"s\",\"model\":\"md\",\"deviceSubType\":\"sub\",\"type\":\"IIR\"}");
        Files.writeString(mockMdsDir.resolve("DigitalFaceId.txt"),
                "{\"deviceProvider\":\"dp\",\"deviceProviderId\":\"face123\",\"make\":\"m\",\"serialNo\":\"s\",\"model\":\"md\",\"deviceSubType\":\"sub\",\"type\":\"FACE\"}");

        // Instantiate the servlet
        servlet = new DiscoverRequest();
    }

    /**
     * Tests doPost with a Fingerprint request.
     * Since the code does not have an explicit Fingerprint branch,
     * it is assumed the request falls in the default (Biometric Device) branch,
     * which adds responses for all modalities.
     */
    @Test
    void testDoPost_FingerprintRequest() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        String jsonBody = "{\"type\":\"Fingerprint\"}";
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(mockRequest.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter, true);
        when(mockResponse.getWriter()).thenReturn(printWriter);

        servlet.doPost(mockRequest, mockResponse);
        printWriter.flush();

        String output = responseWriter.toString();
        System.out.println("Response JSON (Fingerprint): " + output);

        // Parse the JSON response array and decode each digitalId field from base64 URL encoding
        List<Map<String, Object>> list = objMapper.readValue(output,
                new TypeReference<List<Map<String, Object>>>() {});
        boolean found = false;
        for (Map<String, Object> responseObj : list) {
            String digitalIdEncoded = (String) responseObj.get("digitalId");
            byte[] decodedBytes = Base64.getUrlDecoder().decode(digitalIdEncoded);
            String decodedJson = new String(decodedBytes);
            Map<String, String> digitalMap = objMapper.readValue(decodedJson,
                    new TypeReference<Map<String, String>>() {});
            if ("dev123".equals(digitalMap.get("deviceProviderId"))) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Expected deviceProviderId 'dev123' not found in digitalId");
    }
    /**
     * Tests doPost with a Face request.
     * Expects that the response contains the digital ID value from DigitalFaceId.txt.
     */
    @Test
    void testDoPost_FaceRequest() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        String jsonBody = "{\"type\":\"Face\"}";
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(mockRequest.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter, true);
        when(mockResponse.getWriter()).thenReturn(printWriter);

        servlet.doPost(mockRequest, mockResponse);
        printWriter.flush();

        String output = responseWriter.toString();
        System.out.println("Response JSON (Face): " + output);

        // Parse the JSON response array and decode each digitalId field from base64 URL encoding
        List<Map<String, Object>> list = objMapper.readValue(output,
                new TypeReference<List<Map<String, Object>>>() {});
        boolean found = false;
        for (Map<String, Object> responseObj : list) {
            String digitalIdEncoded = (String) responseObj.get("digitalId");
            byte[] decodedBytes = Base64.getUrlDecoder().decode(digitalIdEncoded);
            String decodedJson = new String(decodedBytes);
            Map<String, String> digitalMap = objMapper.readValue(decodedJson,
                    new TypeReference<Map<String, String>>() {});
            if ("face123".equals(digitalMap.get("deviceProviderId"))) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Expected deviceProviderId 'face123' not found in digitalId");
    }

    /**
     * Tests doPost with an Iris request.
     * Expects that the response contains the digital ID value from DigitalIrisId.txt.
     * Since the digital ID is base64 URL\-encoded, this test decodes the value before asserting.
     */
    @Test
    void testDoPost_IrisRequest() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        String jsonBody = "{\"type\":\"Iris\"}";
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(mockRequest.getReader()).thenReturn(reader);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter, true);
        when(mockResponse.getWriter()).thenReturn(printWriter);

        servlet.doPost(mockRequest, mockResponse);
        printWriter.flush();

        String output = responseWriter.toString();
        System.out.println("Response JSON (Iris): " + output);

        // Parse the JSON response array and decode each digitalId field from base64 URL encoding
        List<Map<String, Object>> list = objMapper.readValue(output,
                new TypeReference<List<Map<String, Object>>>() {});
        boolean found = false;
        for (Map<String, Object> responseObj : list) {
            String digitalIdEncoded = (String) responseObj.get("digitalId");
            byte[] decodedBytes = Base64.getUrlDecoder().decode(digitalIdEncoded);
            String decodedJson = new String(decodedBytes);
            Map<String, String> digitalMap = objMapper.readValue(decodedJson,
                    new TypeReference<Map<String, String>>() {});
            if ("iris123".equals(digitalMap.get("deviceProviderId"))) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Expected deviceProviderId 'iris123' not found in digitalId");
    }
}