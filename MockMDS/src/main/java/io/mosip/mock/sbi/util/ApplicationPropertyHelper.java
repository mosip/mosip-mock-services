package io.mosip.mock.sbi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationPropertyHelper {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationPropertyHelper.class);
	private static Properties properties;

	private ApplicationPropertyHelper() {
		throw new IllegalStateException("ApplicationPropertyHelper class");
	}

	public static String getPropertyKeyValue(String key) {
		try {
			createPropertyInfo();

			if (properties != null) {
				return properties.getProperty(key);
			}
		} catch (Exception ex) {
			logger.error("getPropertyKeyValue", ex);
		}

		return null;
	}

	@SuppressWarnings({ "java:S2139" })
	private static void createPropertyInfo() throws IOException {
		if (Objects.isNull(properties)) {
			properties = new Properties();
			try (InputStream stream = new FileInputStream(
					new File(".").getCanonicalPath() + "/application.properties")) {
				properties.load(stream);
			}
		}
	}
}