package org.biometric.provider;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Initializes the web application by configuring the servlet context
 * programmatically. This class is an implementation of
 * {@link WebApplicationInitializer} and serves as an alternative to the
 * traditional {@code web.xml} configuration file.
 * 
 * <p>
 * This configuration class sets up the {@link DispatcherServlet} and maps it to
 * the root URL ("/"). It also registers the {@link WebMvcConfigure} class with
 * the application context.
 * </p>
 */
@Configuration
public class WebAppInitializer implements WebApplicationInitializer {

	/**
	 * Configures the {@link ServletContext} with a {@link DispatcherServlet}. This
	 * method is invoked automatically by the Spring framework during the
	 * application startup.
	 * 
	 * @param container the {@link ServletContext} to be configured
	 * @throws ServletException if any error occurs during servlet registration
	 */
	public void onStartup(ServletContext container) throws ServletException {
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(WebMvcConfigure.class);
		ctx.setServletContext(container);

		ServletRegistration.Dynamic servlet = container.addServlet("dispatcherExample", new DispatcherServlet(ctx));
		servlet.setLoadOnStartup(1);
		servlet.addMapping("/");
	}
}