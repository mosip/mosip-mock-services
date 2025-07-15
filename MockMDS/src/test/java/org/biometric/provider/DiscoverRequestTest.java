package org.biometric.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscoverRequestTest {

    private DiscoverRequest servlet;
    private ObjectMapper objMapper = new ObjectMapper();

    @BeforeEach
    void setup() throws Exception {
        Path baseDir = Files.createTempDirectory("testBaseDir");
        System.setProperty("user.dir", baseDir.toString());

        Path mockMdsDir = baseDir.resolve("files").resolve("MockMDS");
        Files.createDirectories(mockMdsDir);

        Files.writeString(mockMdsDir.resolve("DiscoverFIR.txt"), "{\"callbackId\":\"1\"}");
        Files.writeString(mockMdsDir.resolve("DiscoverIIR.txt"), "{\"callbackId\":\"2\"}");
        Files.writeString(mockMdsDir.resolve("DiscoverFACE.txt"), "{\"callbackId\":\"3\"}");

        Files.writeString(mockMdsDir.resolve("DigitalFingerId.txt"),
                "{\"deviceProvider\":\"dp\",\"deviceProviderId\":\"dev123\",\"make\":\"m\",\"serialNo\":\"s\",\"model\":\"md\",\"deviceSubType\":\"sub\",\"type\":\"FIR\"}");
        Files.writeString(mockMdsDir.resolve("DigitalIrisId.txt"),
                "{\"deviceProvider\":\"dp\",\"deviceProviderId\":\"iris123\",\"make\":\"m\",\"serialNo\":\"s\",\"model\":\"md\",\"deviceSubType\":\"sub\",\"type\":\"IIR\"}");
        Files.writeString(mockMdsDir.resolve("DigitalFaceId.txt"),
                "{\"deviceProvider\":\"dp\",\"deviceProviderId\":\"face123\",\"make\":\"m\",\"serialNo\":\"s\",\"model\":\"md\",\"deviceSubType\":\"sub\",\"type\":\"FACE\"}");

        servlet = new DiscoverRequest();
    }

    /**
     * Tests doPost with a Fingerprint request.
     * Since the code does not have an explicit Fingerprint branch,
     * it is assumed the request falls in the default (Biometric Device) branch,
     * which adds responses for all modalities.
     */
    @Test
    void doPost_fingerprintRequest_success() throws Exception {
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
    void doPost_faceRequest_success() throws Exception {
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
     */
    @Test
    void doPost_irisRequest_success() throws Exception {
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
