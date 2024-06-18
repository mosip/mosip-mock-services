package io.mosip.mock.mv.queue;

import jakarta.jms.Message;

/**
 * Abstract base class for implementing a message listener for JMS queues.
 * <p>
 * This class defines the contract for processing messages received from a JMS
 * queue. Subclasses must implement the {@code setListener} method to handle
 * incoming messages according to application-specific logic.
 * <p>
 * Implementations of this class enable integration with JMS-based messaging
 * systems, allowing asynchronous message consumption and processing.
 * 
 */
public abstract class QueueListener {

	/**
	 * Abstract method to be implemented by subclasses to process incoming JMS
	 * messages.
	 * 
	 * @param message The JMS message received from the queue.
	 */
	public abstract void setListener(Message message);
}