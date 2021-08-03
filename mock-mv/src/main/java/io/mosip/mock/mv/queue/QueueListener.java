package io.mosip.mock.mv.queue;

import javax.jms.Message;

public abstract class QueueListener {
	
	public abstract void setListener(Message message);

}
