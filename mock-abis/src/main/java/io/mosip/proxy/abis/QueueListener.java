package io.mosip.proxy.abis;

import jakarta.jms.Message;

public abstract class QueueListener {	
	public abstract void setListener(Message message);
}