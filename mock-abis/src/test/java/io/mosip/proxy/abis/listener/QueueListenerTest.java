package io.mosip.proxy.abis.listener;

import jakarta.jms.Message;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

public class QueueListenerTest {

    @Test
    public void testSetListenerIsCalled() {
        // Create a mock Message
        Message mockMessage = mock(Message.class);

        // Create a concrete subclass of QueueListener for testing
        QueueListener listener = new QueueListener() {
            @Override
            public void setListener(Message message) {
                // Assert or perform custom logic
                System.out.println("Message received in setListener()");
            }
        };

        // Call the method
        listener.setListener(mockMessage);

        // You can optionally verify interactions with the mockMessage
        verifyNoInteractions(mockMessage);  // since we're not using it here
    }
}
