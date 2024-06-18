package org.biometric.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for setting up web MVC configurations. This class
 * customizes the default configuration of Spring MVC and registers servlets for
 * handling specific requests.
 */
@Configuration
@PropertySource(value = { "application.properties" })
public class WebMvcConfigure implements WebMvcConfigurer {
	private Environment env;

	/**
	 * Constructor to inject the {@link Environment} object.
	 * 
	 * @param env the environment object used to access environment properties
	 */
	@Autowired
	public WebMvcConfigure(Environment env) {
		this.env = env;
	}

	/**
	 * Configures the default servlet handling by enabling the default servlet. This
	 * allows serving static resources through the default servlet.
	 * 
	 * @param configurer the {@link DefaultServletHandlerConfigurer} to configure
	 */
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	/**
	 * Registers the {@link InfoRequest} servlet for handling "/info" requests.
	 * 
	 * @return the {@link ServletRegistrationBean} for the InfoRequest servlet
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public ServletRegistrationBean infoBean() {
		ServletRegistrationBean bean = new ServletRegistrationBean(new InfoRequest(env.getProperty("server.port")),
				"/info");
		bean.setLoadOnStartup(1);
		return bean;
	}

	/**
	 * Registers the {@link DiscoverRequest} servlet for handling "/device"
	 * requests.
	 * 
	 * @return the {@link ServletRegistrationBean} for the DiscoverRequest servlet
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public ServletRegistrationBean discoverBean() {
		ServletRegistrationBean bean = new ServletRegistrationBean(new DiscoverRequest(), "/device");
		bean.setLoadOnStartup(1);
		return bean;
	}

	/**
	 * Registers the {@link CaptureRequest} servlet for handling "/capture"
	 * requests.
	 * 
	 * @return the {@link ServletRegistrationBean} for the CaptureRequest servlet
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public ServletRegistrationBean captureBean() {
		ServletRegistrationBean bean = new ServletRegistrationBean(new CaptureRequest(), "/capture");
		bean.setLoadOnStartup(1);
		return bean;
	}

	/**
	 * Registers the {@link StreamRequest} servlet for handling "/stream" requests.
	 * 
	 * @return the {@link ServletRegistrationBean} for the StreamRequest servlet
	 */
	@SuppressWarnings("rawtypes")
	@Bean
	public ServletRegistrationBean streamBean() {
		@SuppressWarnings("unchecked")
		ServletRegistrationBean bean = new ServletRegistrationBean(new StreamRequest(), "/stream");
		bean.setLoadOnStartup(1);
		return bean;
	}

	/**
	 * Configures a {@link PropertySourcesPlaceholderConfigurer} to resolve property
	 * placeholders. This bean is responsible for loading properties from the
	 * application.properties file.
	 * 
	 * @return the configured {@link PropertySourcesPlaceholderConfigurer} bean
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new ClassPathResource[] { new ClassPathResource("application.properties") };
		ppc.setLocations(resources);
		ppc.setTrimValues(true);
		return ppc;
	}
}