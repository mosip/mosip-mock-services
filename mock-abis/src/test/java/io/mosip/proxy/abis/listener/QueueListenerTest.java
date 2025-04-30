package io.mosip.proxy.abis.listener;

import jakarta.jms.Message;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

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
        // Create a mock Message object
        Message mockMessage = mock(Message.class);

        // Create a concrete subclass of QueueListener for testing
        QueueListener listener = new QueueListener() {
            @Override
            public void setListener(Message message) {
                // Custom logic or assertions can be added here
                System.out.println("Message received in setListener()");
            }
        };

        // Call the setListener method with the mock Message
        listener.setListener(mockMessage);

        // Verify that no interactions occur with the mock Message object
        verifyNoInteractions(mockMessage); // Since the mockMessage is not used in the method
    }
}