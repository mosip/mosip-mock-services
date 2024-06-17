package io.mosip.proxy.abis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MockAbisQueueDetails represents the details of a mock ABIS (Automated
 * Biometric Identification System) queue configuration.
 * <p>
 * This class encapsulates information about the name, host, port, broker URL,
 * inbound and outbound queue names, ping inbound and outbound queue names,
 * username, password, and the type of queue.
 * </p>
 * <p>
 * Instances of this class are typically used to configure mock ABIS queue
 * settings, facilitating integration testing and development activities.
 * </p>
 * 
 * @author
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MockAbisQueueDetails {
	/**
	 * The name of the mock ABIS queue.
	 */
	private String name;

	/**
	 * The host name or IP address of the queue server.
	 */
	private String host;

	/**
	 * The port number used to connect to the queue server.
	 */
	private String port;

	/**
	 * The broker URL used to establish the connection.
	 */
	private String brokerUrl;

	/**
	 * The name of the inbound queue for receiving messages.
	 */
	private String inboundQueueName;

	/**
	 * The name of the outbound queue for sending messages.
	 */
	private String outboundQueueName;

	/**
	 * The name of the ping inbound queue for receiving ping messages.
	 */
	private String pingInboundQueueName;

	/**
	 * The name of the ping outbound queue for sending ping messages.
	 */
	private String pingOutboundQueueName;

	/**
	 * The username required to authenticate with the queue server.
	 */
	private String userName;

	/**
	 * The password required to authenticate with the queue server.
	 */
	private String password;

	/**
	 * The type or category of the queue (e.g., mock, test, production).
	 */
	private String typeOfQueue;
}