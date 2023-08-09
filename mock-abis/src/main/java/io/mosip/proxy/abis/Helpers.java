package io.mosip.proxy.abis;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;

public class Helpers {
	public static String readFileFromResources(String filename) throws URISyntaxException, IOException {
		InputStream inputStream = Helpers.class.getClassLoader().getResourceAsStream(filename);
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "UTF-8");
		return writer.toString();
	}

	public static InputStream readStreamFromResources(String filename) {
		InputStream inputStream = Helpers.class.getClassLoader().getResourceAsStream(filename);
		return inputStream;
	}
}
