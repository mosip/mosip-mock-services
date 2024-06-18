package io.mosip.proxy.abis.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

/**
 * Utility class providing helper methods for file and stream operations.
 */
public class Helpers {
	// Private constructor to prevent instantiation of the class
	private Helpers() {
		throw new IllegalStateException("Helpers class");
	}

	/**
	 * Reads the contents of a file from the resources folder as a string.
	 *
	 * @param filename The name of the file in the resources folder.
	 * @return The content of the file as a string.
	 * @throws IOException If an I/O error occurs while reading the file.
	 */
	public static String readFileFromResources(String filename) throws IOException {
		InputStream inputStream = Helpers.class.getClassLoader().getResourceAsStream(filename);
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
		return writer.toString();
	}

	/**
	 * Retrieves an input stream for a file located in the resources folder.
	 *
	 * @param filename The name of the file in the resources folder.
	 * @return An InputStream for the specified file.
	 */
	public static InputStream readStreamFromResources(String filename) {
		return Helpers.class.getClassLoader().getResourceAsStream(filename);
	}
}