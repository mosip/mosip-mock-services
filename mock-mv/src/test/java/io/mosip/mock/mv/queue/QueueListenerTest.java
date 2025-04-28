package io.mosip.mock.mv.queue;

import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class QueueListenerTest {

    private TestQueueListener listener;
    private TextMessage mockMessage;

    @BeforeEach
    void setUp() {
        listener = new TestQueueListener();
        mockMessage = mock(TextMessage.class);
    }

    @Test
    void testSetListenerWithTextMessage() throws Exception {
        when(mockMessage.getText()).thenReturn("Test Message");

        listener.setListener(mockMessage);

        verify(mockMessage, times(1)).getText();
    }

    @Test
    void testSetListenerWithException() throws Exception {
        when(mockMessage.getText()).thenThrow(new RuntimeException("Mock JMS error"));

        try {
            listener.setListener(mockMessage);
        } catch (RuntimeException ex) {
            assert ex.getMessage().contains("Error processing message");
        }
    }
}
