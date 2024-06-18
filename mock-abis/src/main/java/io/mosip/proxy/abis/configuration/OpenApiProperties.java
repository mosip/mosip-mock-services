package io.mosip.proxy.abis.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties class for OpenAPI specification customization.
 * <p>
 * This class defines properties to configure various aspects of the OpenAPI
 * specification such as information (title, description, version, license),
 * service details (servers), and API grouping (group name and paths).
 */
@Configuration
@ConfigurationProperties(prefix = "openapi")
@Data
public class OpenApiProperties {
	/**
	 * Information properties for OpenAPI specification.
	 */
	private InfoProperty info;

	/**
	 * Service properties for OpenAPI specification.
	 */
	private Service service;

	/**
	 * Group properties for OpenAPI specification.
	 */
	private Group group;
}

/**
 * Information properties class for OpenAPI specification.
 */
@Data
class InfoProperty {

	/**
	 * Title of the API.
	 */
	private String title;

	/**
	 * Description of the API.
	 */
	private String description;

	/**
	 * Version of the API.
	 */
	private String version;

	/**
	 * License information for the API.
	 */
	private LicenseProperty license;
}

/**
 * License properties class for OpenAPI specification.
 */
@Data
class LicenseProperty {

	/**
	 * Name of the license.
	 */
	private String name;

	/**
	 * URL for the license.
	 */
	private String url;
}

/**
 * Service properties class for OpenAPI specification.
 */
@Data
class Service {

	/**
	 * List of server configurations.
	 */
	private List<Server> servers;
}

/**
 * Server configuration class for OpenAPI specification.
 */
@Data
class Server {

	/**
	 * Description of the server.
	 */
	private String description;

	/**
	 * URL of the server.
	 */
	private String url;
}

/**
 * Group properties class for OpenAPI specification.
 */
@Data
class Group {

	/**
	 * Name of the API group.
	 */
	private String name;

	/**
	 * List of paths to match for the API group.
	 */
	private List<String> paths;
}