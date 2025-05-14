package io.mosip.mock.mv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.mock.mv.dto.ConfigureDto;
import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.service.MockMvDecisionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MockMvConfigController.class)
@TestPropertySource(properties = {
        "registration.processor.manual.adjudication.queue.url=tcp://localhost:61616"
})
class MockMvConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MockMvDecisionService mockMvDecisionService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests successful configuration of MockMv decision.
     * Verifies that when a valid configuration is posted,
     * the service updates it and returns success message.
     */
    @Test
    void testConfigure_Success() throws Exception {
        ConfigureDto configureDto = new ConfigureDto();
        configureDto.setMockMvDescision("Decision1");

        Mockito.doNothing().when(mockMvDecisionService).setMockMvDecision(any(String.class));
        Mockito.when(mockMvDecisionService.getMockMvDecision()).thenReturn("Decision1");

        mockMvc.perform(post("/config/configureMockMv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configureDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Successfully updated the configuration")));
    }


    /**
     * Tests successful retrieval of MockMv configuration.
     * Verifies that the current configuration can be retrieved
     * and matches the expected decision value.
     */
    @Test
    void testCheckConfiguration_Success() throws Exception {
        Mockito.when(mockMvDecisionService.getMockMvDecision()).thenReturn("Decision1");

        mockMvc.perform(get("/config/configureMockMv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mockMvDescision").value("Decision1"));
    }

    /**
     * Tests successful setting of MockMv expectation.
     * Verifies that when a valid expectation is posted,
     * it is stored and returns success message with the RID.
     */
    @Test
    void testSetExpectation_Success() throws Exception {
        Expectation expectation = new Expectation();
        expectation.setRId("rid-123");

        Mockito.doNothing().when(mockMvDecisionService).setExpectation(any(Expectation.class));

        mockMvc.perform(post("/config/expectationMockMv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectation)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Successfully inserted expectation rid-123")));
    }

    /**
     * Tests successful retrieval of MockMv expectations.
     * Verifies that stored expectations can be retrieved
     * and contain the expected RID and content.
     */
    @Test
    void testGetExpectation_Success() throws Exception {
        Expectation expectation = new Expectation();
        expectation.setRId("rid-123");
        Map<String, Expectation> expectationMap = Collections.singletonMap("rid-123", expectation);

        Mockito.when(mockMvDecisionService.getExpectations()).thenReturn(expectationMap);

        mockMvc.perform(get("/config/expectationMockMv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['rid-123']").exists())
                .andExpect(content().string(containsString("rid-123")));
    }

    /**
     * Tests successful deletion of all MockMv expectations.
     * Verifies that all expectations can be deleted
     * and returns success message.
     */
    @Test
    void testDeleteAllExpectations_Success() throws Exception {
        Mockito.doNothing().when(mockMvDecisionService).deleteExpectations();

        mockMvc.perform(delete("/config/expectationMockMv"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Successfully deleted expectations")));
    }

}