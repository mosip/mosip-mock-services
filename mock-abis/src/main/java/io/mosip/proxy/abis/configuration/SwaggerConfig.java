package io.mosip.proxy.abis.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger/OpenAPI documentation generation.
 * <p>
 * This class defines beans to configure the OpenAPI specification based on the
 * provided {@link OpenApiProperties}. It initializes an {@link OpenAPI} bean
 * and a {@link GroupedOpenApi} bean to customize and group API documentation
 * according to specified properties.
 */
@Configuration
public class SwaggerConfig {
	private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

	private OpenApiProperties openApiProperties;

	/**
	 * Constructs a {@code SwaggerConfig} instance with the provided
	 * {@link OpenApiProperties}.
	 *
	 * @param openApiProperties The properties containing OpenAPI configuration
	 *                          details
	 */
	@Autowired
	public SwaggerConfig(OpenApiProperties openApiProperties) {
		this.openApiProperties = openApiProperties;
	}

	/**
	 * Creates an {@link OpenAPI} bean configured with title, version, description,
	 * and license information.
	 *
	 * @return Configured {@link OpenAPI} instance representing the OpenAPI
	 *         specification
	 */
	@Bean
	public OpenAPI openApi() {
		OpenAPI api = new OpenAPI().components(new Components())
				.info(new Info().title(openApiProperties.getInfo().getTitle())
						.version(openApiProperties.getInfo().getVersion())
						.description(openApiProperties.getInfo().getDescription())
						.license(new License().name(openApiProperties.getInfo().getLicense().getName())
								.url(openApiProperties.getInfo().getLicense().getUrl())));

		openApiProperties.getService().getServers().forEach(
				server -> api.addServersItem(new Server().description(server.getDescription()).url(server.getUrl())));
		logger.info("swagger open api bean is ready");
		return api;
	}

	/**
	 * Creates a {@link GroupedOpenApi} bean to group API paths based on configured
	 * properties.
	 *
	 * @return {@link GroupedOpenApi} instance representing grouped API paths
	 */
	@Bean
	public GroupedOpenApi groupedOpenApi() {
		return GroupedOpenApi.builder().group(openApiProperties.getGroup().getName())
				.pathsToMatch(openApiProperties.getGroup().getPaths().stream().toArray(String[]::new)).build();
	}
}