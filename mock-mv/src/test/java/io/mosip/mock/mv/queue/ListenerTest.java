// java
package io.mosip.mock.mv.queue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.mosip.mock.mv.dto.Expectation;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

public class ListenerTest {

    private Listener listener;
    private Environment env;
    private io.mosip.mock.mv.service.ExpectationCache expectationCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new Listener();

        // Inject mocks for Environment and ExpectationCache
        env = mock(Environment.class);
        expectationCache = mock(io.mosip.mock.mv.service.ExpectationCache.class);
        ReflectionTestUtils.setField(listener, "env", env);
        ReflectionTestUtils.setField(listener, "expectationCache", expectationCache);
        ReflectionTestUtils.setField(listener, "mockDecision", "APPROVED");

        // Stub executeAsync so that asynchronous scheduling is bypassed
        Listener spyListener = org.mockito.Mockito.spy(listener);
        doReturn(true).when(spyListener).executeAsync(anyString(), anyInt(), anyInt(), anyString());
        listener = spyListener;

        // Stub property for decision service id
        when(env.getProperty(Listener.DECISION_SERVICE_ID)).thenReturn("decisionServiceId");
    }

    @Test
    void testConsumeLogic_withTextMessage() throws Exception {
        // Create a dummy JSON for ManualAdjudicationRequestDTO including minimal fields
        String dummyJson = "{ \"requestId\": \"req1\", \"referenceId\": \"ref1\", \"gallery\": { \"referenceIds\": [] } }";
        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(dummyJson);

        // For the given reference id, return an empty Expectation so that execution follows the default branch
        Expectation dummyExpectation = new Expectation();
        when(expectationCache.get("ref1")).thenReturn(dummyExpectation);

        // Call consumeLogic with the mocked text message and a dummy address
        boolean result = listener.consumeLogic(textMessage, "dummyAddress");

        // Verify that getText() is called and the consumeLogic() returns true.
        verify(textMessage).getText();
        assertTrue(result);
    }

    @Test
    void testConsumeLogic_withInvalidMessage() throws Exception {
        // Create a dummy JMS Message that is not an instance of TextMessage
        Message dummyMessage = mock(Message.class);

        // Since the message is not a TextMessage or ActiveMQTextMessage, consumeLogic should return false.
        boolean result = listener.consumeLogic(dummyMessage, "dummyAddress");
        assertFalse(result);
    }
}