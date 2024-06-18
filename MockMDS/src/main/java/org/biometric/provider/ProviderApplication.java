package org.biometric.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * The main entry point for the Biometric Provider Spring Boot application. This
 * class is responsible for bootstrapping the application.
 * <p>
 * The {@code @SpringBootApplication} annotation marks this class as a Spring
 * Boot application. The {@code @ComponentScan} annotation is used to specify
 * the base packages to scan for Spring components.
 * </p>
 */
@SpringBootApplication
@ComponentScan(basePackages = { "io.mosip.kernel.crypto.jce.core", "org.biometric.provider" })
public class ProviderApplication {

	/**
	 * The main method which serves as the entry point for the Spring Boot
	 * application.
	 * <p>
	 * This method delegates to Spring Boot's
	 * {@link SpringApplication#run(Class, String...)} method to launch the
	 * application.
	 * </p>
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(ProviderApplication.class, args);
	}
}