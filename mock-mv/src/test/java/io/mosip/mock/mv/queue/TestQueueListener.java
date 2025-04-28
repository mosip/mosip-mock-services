package io.mosip.mock.mv.queue;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;

public class TestQueueListener extends QueueListener {

    @Override
    public void setListener(Message message) {
        try {
            if (message instanceof TextMessage) {
                String text = ((TextMessage) message).getText();
                System.out.println("Received message: " + text);
            } else {
                System.out.println("Received non-text message");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing message", e);
        }
    }
}
