package org.biometric.provider;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utility class for managing Cross-Origin Resource Sharing (CORS) in a Spring
 * Boot application.
 *
 * This class provides methods for configuring and applying CORS headers to HTTP
 * responses. It utilizes Spring's `@Value` annotation to inject configuration
 * properties for allowed origins and methods.
 *
 * **Important Security Note:** Allowing all origins with "*" is generally not
 * recommended for production environments due to potential security risks.
 * Consider restricting allowed origins to specific trusted domains for enhanced
 * security.
 */
public class CORSManager {
	/**
	 * Comma-separated list of allowed HTTP methods for CORS requests. This value is
	 * injected from a configuration property (e.g., application.properties).
	 */
	@Value("${cors.headers.allowed.methods: OPTIONS, RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM, GET, POST}")
	@SuppressWarnings({ "java:S1444" })
	public static String allowedMethods;

	/**
	 * Allowed origin(s) for CORS requests. This value is injected from a
	 * configuration property (e.g., application.properties).
	 *
	 * **Security Note:** Allowing "*" for allowedOrigin is generally not
	 * recommended for production.
	 */
	@Value("${cors.headers.allowed.origin: *}")
	@SuppressWarnings({ "java:S1444" })
	public static String allowedOrigin;

	private CORSManager() {
		throw new IllegalStateException("CORSManager class");
	}

	/**
	 * Handles OPTIONS requests for CORS preflight checks.
	 *
	 * This method sets the following CORS headers in the response: -
	 * Access-Control-Allow-Origin: "*" (configurable) -
	 * Access-Control-Allow-Headers: "Content-Type" - Access-Control-Allow-Methods:
	 * The value of `allowedMethods` (configurable)
	 *
	 * It also writes an empty response body.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object to set CORS headers on.
	 * @throws IOException If an error occurs while writing to the response.
	 */
	@SuppressWarnings({ "java:S1172" })
	public static void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		response.setHeader("Access-Control-Allow-Methods",
				"OPTIONS, RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM, GET, POST");
		PrintWriter out = response.getWriter();
		out.println("");
	}

	/**
	 * Sets common CORS headers to the provided HTTP response.
	 *
	 * This method sets the same CORS headers as `doOptions` but doesn't write an
	 * empty response body.
	 *
	 * @param response The HttpServletResponse object to set CORS headers on.
	 * @return The modified HttpServletResponse object with CORS headers set.
	 */
	public static HttpServletResponse setCors(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		response.setHeader("Access-Control-Allow-Methods",
				"OPTIONS, RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM, GET, POST");
		return response;
	}
}