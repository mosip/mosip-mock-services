package io.mosip.proxy.abis.listener;

import jakarta.jms.Message;

public abstract class QueueListener {	
	public abstract void setListener(Message message);
}