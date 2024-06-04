package io.mosip.mock.sbi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHelper {
	private static final Logger logger = LoggerFactory.getLogger(FileHelper.class);

	private FileHelper() {
		throw new IllegalStateException("FileHelper class");
	}

	public static boolean exists(String filename) {
		boolean valid = true;
		File file = new File(filename);
		if (!file.exists()) {
			valid = false;
		}
		return valid;
	}

	public static boolean directoryExists(String directoryName) {
		return exists(directoryName);
	}

	@SuppressWarnings({ "java:S1172" })
	public static void createDirectory(String strDirectoryName, boolean bIsCaseSensitive) {
		File oFile = new File(strDirectoryName);
		if (!oFile.exists()) {
			oFile.mkdir();
		}
	}

	@SuppressWarnings({ "java:S899", "unused" })
	public static File createFile(String filename) {
		File file = null;
		if (!exists(filename)) {
			try {
				file = new File(filename);
				boolean isCreated = file.createNewFile();
			} catch (IOException ex) {
				logger.error("createFile :: Error in creating file :: ", ex);
			}
		} else {
			file = new File(filename);
		}
		return file;
	}

	public static byte[] readAllBytes(String fileName) throws IOException {
		File file = new File(fileName);
		return loadFile(file);
	}

	@SuppressWarnings("unused")
	public static byte[] loadFile(File file) throws IOException {
		byte[] fileContent = null;
		try (FileInputStream fin = new FileInputStream(file)){
			fileContent = new byte[(int) file.length()];
			int bytesRead = fin.read(fileContent);
		} 
		return fileContent;
	}

	public static String getCanonicalPath() throws IOException {
		return new File(".").getCanonicalPath();
	}

	public static String getUserTempDirectory() {
		return System.getProperty("java.io.tmpdir");
	}

	public static String getOS() {
		return System.getProperty("os.arch");
	}
}