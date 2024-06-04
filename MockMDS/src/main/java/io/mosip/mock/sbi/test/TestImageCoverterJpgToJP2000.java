package io.mosip.mock.sbi.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.mock.sbi.util.ImageHelper;

public class TestImageCoverterJpgToJP2000 {
	private static final Logger logger = LoggerFactory.getLogger(TestImageCoverterJpgToJP2000.class);
	private static String fingerName = "Left_Ring";

	@SuppressWarnings({ "java:S899", "java:S1075", "java:S4042", "java:S5899" })
	public static void main(String[] args) {
		// Finger Conversion
		String path = "F:\\Home Projects\\Essl\\Mosip\\mosip-mock-services\\MockMDS\\Profile\\Automatic\\Finger\\img";
		try {
			File root = new File(path);
			File[] listJP2 = root.listFiles(new MyFileNameFilter(fingerName + ".jp2"));

			int count = 1;
			if (listJP2 != null)
				count = listJP2.length == 0 ? 1 : listJP2.length + 1;

			Collection<File> list = listFileTree(root);
			logger.info("Input File: {}", list);
			if (list.isEmpty())
				return;

			@SuppressWarnings("unused")
			boolean isLeftOrRight = false; // left = false ... right = true
			for (File f : list) {
				logger.info("Input File: {}", f.getAbsoluteFile());

				ImageHelper.toJ2000(f.getAbsoluteFile() + "",
						path + File.separator + changeOutputFileExtension(f, count));
				count++;
				f.delete();
			}
			logger.info("Image converted successfully.");

		} catch (IOException ex) {
			logger.error("Error during converting image.", ex);
		}
	}

	public static Collection<File> listFileTree(File dir) {
		Set<File> fileTree = new HashSet<>();
		if (dir == null || dir.listFiles() == null) {
			return fileTree;
		}
		for (File entry : dir.listFiles()) {
			if (entry.isFile())
				fileTree.add(entry);
			else
				fileTree.addAll(listFileTree(entry));
		}
		return fileTree;
	}

	private static String changeOutputFileExtension(File file, int fileNumber) {
		return String.format("%04d", fileNumber) + getFileNameChangeIris(getFileNameWithoutExtension(file)) + ".jp2";
	}

	@SuppressWarnings({ "java:S1172" })
	private static String getFileNameChangeIris(String fileName) {
		return fingerName;
	}

	private static String getFileNameWithoutExtension(File file) {
		String fileName = "";

		try {
			if (file != null && file.exists()) {
				String name = file.getName();
				fileName = name.replaceFirst("[.][^.]+$", "");
			}
		} catch (Exception e) {
			logger.error("getFileNameWithoutExtension.", e);
			fileName = "";
		}

		return fileName;
	}

	// FileNameFilter implementation
	public static class MyFileNameFilter implements FilenameFilter {
		private String extension;

		public MyFileNameFilter(String extension) {
			this.extension = extension.toLowerCase();
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(extension);
		}
	}
}