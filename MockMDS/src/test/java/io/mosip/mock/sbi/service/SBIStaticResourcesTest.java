package io.mosip.mock.sbi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.mosip.mock.sbi.SBIConstant;

class SBIStaticResourcesTest {

	@Test
	void isStaticPath_recognizesSwaggerRoutes() {
		assertTrue(SBIStaticResources.isStaticPath("/swagger-ui"));
		assertTrue(SBIStaticResources.isStaticPath("/swagger-ui/"));
		assertTrue(SBIStaticResources.isStaticPath("/swagger-ui/index.html"));
		assertTrue(SBIStaticResources.isStaticPath("/swagger-ui/openapi.yaml"));
		assertTrue(SBIStaticResources.isStaticPath("/swagger-ui.html"));
		assertTrue(SBIStaticResources.isStaticPath("/v3/api-docs"));
		assertTrue(SBIStaticResources.isStaticPath("/v3/api-docs?format=yaml"));
		assertFalse(SBIStaticResources.isStaticPath("/admin/status"));
		assertFalse(SBIStaticResources.isStaticPath("/info"));
	}

	@Test
	void stripQuery_removesQueryString() {
		assertEquals("/v3/api-docs", SBIStaticResources.stripQuery("/v3/api-docs?x=1"));
		assertEquals("/swagger-ui/index.html", SBIStaticResources.stripQuery("/swagger-ui/index.html"));
	}

	@Test
	void contentTypeFor_mapsKnownExtensions() {
		assertEquals("text/html; charset=UTF-8", SBIStaticResources.contentTypeFor("index.html"));
		assertEquals("application/yaml", SBIStaticResources.contentTypeFor("openapi.yaml"));
		assertEquals("application/octet-stream", SBIStaticResources.contentTypeFor("other.bin"));
	}

	@Test
	void injectBoundServer_rewritesServersBlockToActualPort() {
		String yaml = "openapi: 3.0.3\nservers:\n  - url: /\n    description: placeholder\ntags: []\n";
		String out = SBIStaticResources.injectBoundServer(yaml, 4507);
		assertTrue(out.contains("url: http://127.0.0.1:4507/") || out.contains("url: http://"));
		assertTrue(out.contains("Bound MockMDS port 4507"));
		assertTrue(out.contains("openapi: 3.0.3"));
		assertTrue(out.contains("tags: []"));
	}

	@Test
	void handleGet_servesOpenApiWhenPresent(@TempDir Path tempDir) throws Exception {
		Path swaggerDir = tempDir.resolve("swagger-ui");
		Files.createDirectories(swaggerDir);
		Files.writeString(swaggerDir.resolve("openapi.yaml"),
				"openapi: 3.0.3\nservers:\n  - url: /\n    description: placeholder\n");
		Files.writeString(swaggerDir.resolve("index.html"), "<html></html>");

		String previous = System.getProperty("user.dir");
		try {
			System.setProperty("user.dir", tempDir.toString());
			byte[] response = SBIStaticResources.handleGet(SBIConstant.OPENAPI_DOCS_PATH, 4503);
			String text = new String(response, StandardCharsets.UTF_8);
			assertTrue(text.startsWith("HTTP/1.1 200 OK"));
			assertTrue(text.contains("Content-Type: application/yaml"));
			assertTrue(text.contains("openapi: 3.0.3"));
			assertTrue(text.contains(":4503/"));
			assertTrue(text.contains("Bound MockMDS port 4503"));

			byte[] redirect = SBIStaticResources.handleGet(SBIConstant.SWAGGER_UI_PATH, 4503);
			String redirectText = new String(redirect, StandardCharsets.UTF_8);
			assertTrue(redirectText.startsWith("HTTP/1.1 302 Found"));
			assertTrue(redirectText.contains("Location: /swagger-ui/index.html"));

			byte[] index = SBIStaticResources.handleGet(SBIConstant.SWAGGER_UI_INDEX, 4503);
			assertNotNull(index);
			assertTrue(new String(index, StandardCharsets.UTF_8).contains("<html></html>"));
		} finally {
			System.setProperty("user.dir", previous);
		}
	}

	@Test
	void handleGet_returns404WhenMissing(@TempDir Path tempDir) {
		String previous = System.getProperty("user.dir");
		try {
			System.setProperty("user.dir", tempDir.toString());
			byte[] response = SBIStaticResources.handleGet(SBIConstant.SWAGGER_UI_INDEX, 4501);
			String text = new String(response, StandardCharsets.UTF_8);
			assertTrue(text.startsWith("HTTP/1.1 404 Not Found"));
		} finally {
			System.setProperty("user.dir", previous);
		}
	}
}
