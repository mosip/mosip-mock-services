package org.biometric.provider;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CORSManager {
	@Value("${cors.headers.allowed.methods: OPTIONS, RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM, GET, POST}")
	@SuppressWarnings({ "java:S1444" })
	public static String allowedMethods;

	@Value("${cors.headers.allowed.origin: *}")
	@SuppressWarnings({ "java:S1444" })
	public static String allowedOrigin;

	private CORSManager() {
		throw new IllegalStateException("CORSManager class");
	}

	@SuppressWarnings({ "java:S1172" })
	public static void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		response.setHeader("Access-Control-Allow-Methods",
				"OPTIONS, RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM, GET, POST");
		PrintWriter out = response.getWriter();
		out.println("");
	}

	public static HttpServletResponse setCors(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		response.setHeader("Access-Control-Allow-Methods",
				"OPTIONS, RCAPTURE, CAPTURE, MOSIPDINFO, MOSIPDISC, STREAM, GET, POST");
		return response;
	}
}