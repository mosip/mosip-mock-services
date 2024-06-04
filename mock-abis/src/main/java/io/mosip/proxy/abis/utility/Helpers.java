package io.mosip.proxy.abis.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

public class Helpers {
	private Helpers() {
		throw new IllegalStateException("Helpers class");
	}

	public static String readFileFromResources(String filename) throws IOException {
		InputStream inputStream = Helpers.class.getClassLoader().getResourceAsStream(filename);
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
		return writer.toString();
	}

	public static InputStream readStreamFromResources(String filename) {
		return Helpers.class.getClassLoader().getResourceAsStream(filename);
	}
}
