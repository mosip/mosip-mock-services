package io.mosip.proxy.abis;

import javax.jms.Message;

public abstract class QueueListener {
	
	public abstract void setListener(Message message);

}
