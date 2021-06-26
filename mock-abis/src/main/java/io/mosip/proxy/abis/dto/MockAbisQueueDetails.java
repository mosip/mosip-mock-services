package io.mosip.proxy.abis.dto;

import lombok.AllArgsConstructor;
//import io.mosip.registration.processor.core.queue.factory.MosipQueue;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MockAbisQueueDetails {
	private String name;
	private String host;
	private String port;
	private String brokerUrl;
	private String inboundQueueName;
	private String outboundQueueName;
	private String pingInboundQueueName;
	private String pingOutboundQueueName;
	private String userName;
	private String password;
	private String typeOfQueue;
	
}