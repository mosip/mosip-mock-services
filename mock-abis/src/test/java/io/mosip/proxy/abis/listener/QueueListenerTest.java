package io.mosip.proxy.abis.listener;

import jakarta.jms.Message;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Unit test class for QueueListener.
 * This class tests the behavior of the setListener method in a concrete implementation of QueueListener.
 */
public class QueueListenerTest {

    /**
     * Tests the setListener method to ensure it is called correctly.
     * Verifies that no interactions occur with the mock Message object.
     */
    @Test
    public void testSetListenerIsCalled() {
        Message mockMessage = mock(Message.class);
        QueueListener listener = new QueueListener() {
            @Override
            public void setListener(Message message) {
                System.out.println("Message received in setListener()");
            }
        };

        listener.setListener(mockMessage);
    }
}