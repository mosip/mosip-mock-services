package io.mosip.mock.mv.queue;

import jakarta.jms.Message;

public abstract class QueueListener {
	public abstract void setListener(Message message);
}