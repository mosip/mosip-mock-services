package io.mosip.mock.sbi.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.SBIConstant;
import io.mosip.mock.sbi.util.ApplicationPropertyHelper;

/**
 * Serves static admin OpenAPI + Swagger UI files from {@code swagger-ui/} under
 * the process working directory ({@code target/} when using run-local helpers).
 */
public final class SBIStaticResources {
	private static final Logger LOGGER = LoggerFactory.getLogger(SBIStaticResources.class);

	private static final String DIR_NAME = "swagger-ui";
	private static final Set<String> ALLOWED_FILES = Set.of("index.html", "openapi.yaml");

	private SBIStaticResources() {
		throw new IllegalStateException("SBIStaticResources class");
	}

	public static boolean isStaticPath(String path) {
		if (path == null || path.isEmpty()) {
			return false;
		}
		String normalized = stripQuery(path);
		return normalized.equals(SBIConstant.SWAGGER_UI_PATH)
				|| normalized.equals(SBIConstant.SWAGGER_UI_PATH + "/")
				|| normalized.equals(SBIConstant.SWAGGER_UI_INDEX)
				|| normalized.equals(SBIConstant.SWAGGER_UI_OPENAPI)
				|| normalized.equals(SBIConstant.SWAGGER_UI_HTML)
				|| normalized.equals(SBIConstant.OPENAPI_DOCS_PATH);
	}

	public static byte[] handleGet(String path, int port) {
		String normalized = stripQuery(path);
		if (normalized.equals(SBIConstant.SWAGGER_UI_PATH) || normalized.equals(SBIConstant.SWAGGER_UI_PATH + "/")
				|| normalized.equals(SBIConstant.SWAGGER_UI_HTML)) {
			return redirect(SBIConstant.SWAGGER_UI_INDEX, port);
		}
		if (normalized.equals(SBIConstant.OPENAPI_DOCS_PATH) || normalized.equals(SBIConstant.SWAGGER_UI_OPENAPI)) {
			return openApiResponse(port);
		}
		if (normalized.equals(SBIConstant.SWAGGER_UI_INDEX)) {
			return fileResponse("index.html", "text/html; charset=UTF-8", port);
		}
		return notFound(port);
	}

	static String stripQuery(String path) {
		int q = path.indexOf('?');
		return q >= 0 ? path.substring(0, q) : path;
	}

	static Path resolveAllowedFile(String fileName) {
		if (fileName == null || !ALLOWED_FILES.contains(fileName)) {
			return null;
		}
		Path root = Paths.get(System.getProperty("user.dir"), DIR_NAME).toAbsolutePath().normalize();
		Path file = root.resolve(fileName).normalize();
		if (!file.startsWith(root)) {
			return null;
		}
		return Files.isRegularFile(file) ? file : null;
	}

	private static byte[] openApiResponse(int port) {
		Path file = resolveAllowedFile("openapi.yaml");
		if (file == null) {
			LOGGER.warn("Static resource missing: {}/openapi.yaml", DIR_NAME);
			return notFound(port);
		}
		try {
			String yaml = Files.readString(file, StandardCharsets.UTF_8);
			byte[] body = injectBoundServer(yaml, port).getBytes(StandardCharsets.UTF_8);
			return buildResponse(200, "OK", "application/yaml", body, port, false);
		} catch (IOException ex) {
			LOGGER.error("Failed reading static resource {}", file, ex);
			return notFound(port);
		}
	}

	/**
	 * Rewrites OpenAPI {@code servers} to the actual bound host:port so Swagger
	 * "Try it out" tracks first-free selection in 4501–4600 when another process
	 * already holds a lower port.
	 */
	static String injectBoundServer(String openapiYaml, int port) {
		String host = serverHost();
		String serversBlock = "servers:\n" + "  - url: http://" + host + ":" + port + "/\n"
				+ "    description: Bound MockMDS port " + port + " (first free in configured range)\n";
		String replaced = openapiYaml.replaceFirst("(?m)^servers:\\r?\\n(?:[ \\t][^\\r\\n]*\\r?\\n)*", serversBlock);
		if (replaced.equals(openapiYaml)) {
			// No servers block — prepend after info or at top
			int insertAt = openapiYaml.indexOf("\ntags:");
			if (insertAt < 0) {
				insertAt = openapiYaml.indexOf("\npaths:");
			}
			if (insertAt >= 0) {
				return openapiYaml.substring(0, insertAt + 1) + serversBlock + openapiYaml.substring(insertAt + 1);
			}
			return serversBlock + openapiYaml;
		}
		return replaced;
	}

	static String serverHost() {
		String host = ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS);
		if (host == null || host.isBlank()) {
			return "127.0.0.1";
		}
		return host.replace("\"", "").trim();
	}

	private static byte[] fileResponse(String fileName, String contentType, int port) {
		Path file = resolveAllowedFile(fileName);
		if (file == null) {
			LOGGER.warn("Static resource missing: {}/{}", DIR_NAME, fileName);
			return notFound(port);
		}
		try {
			byte[] body = Files.readAllBytes(file);
			return buildResponse(200, "OK", contentType, body, port, false);
		} catch (IOException ex) {
			LOGGER.error("Failed reading static resource {}", file, ex);
			return notFound(port);
		}
	}

	private static byte[] redirect(String location, int port) {
		String headers = "HTTP/1.1 302 Found\r\n"
				+ corsHeaders()
				+ "Location: " + location + "\r\n"
				+ "CACHE-CONTROL:no-cache\r\n"
				+ "Content-Length: 0\r\n"
				+ "LOCATION: HTTP://"
				+ ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS) + ":" + port + "/\r\n"
				+ "Connection: close\r\n\r\n";
		return headers.getBytes(StandardCharsets.UTF_8);
	}

	private static byte[] notFound(int port) {
		byte[] body = "{\"errorCode\":\"404\",\"errorInfo\":\"Not Found\"}".getBytes(StandardCharsets.UTF_8);
		return buildResponse(404, "Not Found", "application/json", body, port, false);
	}

	private static byte[] buildResponse(int status, String reason, String contentType, byte[] body, int port,
			boolean keepAlive) {
		StringBuilder http = new StringBuilder();
		http.append("HTTP/1.1 ").append(status).append(' ').append(reason).append("\r\n");
		http.append(corsHeaders());
		http.append("CACHE-CONTROL:no-cache\r\n");
		http.append("Content-Length: ").append(body.length).append("\r\n");
		http.append("Content-Type: ").append(contentType).append("\r\n");
		http.append("LOCATION: HTTP://")
				.append(ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.SERVER_ADDRESS)).append(':')
				.append(port).append("/\r\n");
		http.append("Connection: ").append(keepAlive ? "Keep-Alive" : "close").append("\r\n\r\n");
		byte[] headerBytes = http.toString().getBytes(StandardCharsets.UTF_8);
		byte[] response = new byte[headerBytes.length + body.length];
		System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
		System.arraycopy(body, 0, response, headerBytes.length, body.length);
		return response;
	}

	private static String corsHeaders() {
		return "Access-Control-Allow-Headers:DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,X-PINGOTHER,Authorization\r\n"
				+ "Access-Control-Allow-Origin: *\r\n"
				+ "Access-Control-Allow-Methods: "
				+ ApplicationPropertyHelper.getPropertyKeyValue(SBIConstant.CORS_HEADER_METHODS) + "\r\n"
				+ "Access-Control-Allow-Credentials: true\r\n";
	}

	/** Visible for tests — content type by file extension. */
	static String contentTypeFor(String fileName) {
		String lower = fileName.toLowerCase(Locale.ROOT);
		if (lower.endsWith(".html")) {
			return "text/html; charset=UTF-8";
		}
		if (lower.endsWith(".yaml") || lower.endsWith(".yml")) {
			return "application/yaml";
		}
		return "application/octet-stream";
	}
}
