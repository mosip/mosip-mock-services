package io.mosip.mock.sbi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);	

	public static boolean exists (String filename)
	{
		boolean valid = true;
		File file = new File (filename);
		if (file.exists () == false)
		{
			valid = false;
		}
		return valid;
	}
	
	public static boolean directoryExists (String directoryName)
	{
		return exists (directoryName);
	}
	
	public static void createDirectory (String strDirectoryName, boolean bIsCaseSensitive)
	{
		File oFile = new File (strDirectoryName);
		if (!oFile.exists ())
		{
			oFile.mkdir ();
		}
	}

	public static File createFile (String filename)
	{
		File file = null;
		if (exists (filename) == false)
		{
			try
			{
				file = new File (filename);
				file.createNewFile ();
			}
			catch (IOException ex)
			{
				LOGGER.error ("createFile :: Error in creating file :: ", ex);
			}
		}
		else
		{
			file = new File (filename);
		}
		return file;
	}
	
	public static byte[] readAllBytes (String fileName)
	{
		File file = new File (fileName);
		return loadFile (file);
	}
	
	public static byte[] loadFile (File file)
	{
		FileInputStream fin = null;

		byte[] fileContent = null;
		try
		{
			fin = new FileInputStream (file);
			fileContent = new byte[(int) file.length ()];
			fin.read (fileContent);
		}
		catch (IOException ex)
		{
			LOGGER.error ("loadFile :: Error in getting file :: ", ex);
		}
		finally
		{
			try
			{
				if (fin != null)
					fin.close ();
			}
			catch (Exception ex)
			{
				LOGGER.error ("loadFile :: Error in closing file :: ", ex);
			}
		}
		return fileContent;
	}
	
	public static String getCanonicalPath () throws IOException
	{
		return new File (".").getCanonicalPath ();
	}

	public static String getUserTempDirectory () throws IOException
	{
		return System.getProperty ("java.io.tmpdir");
	}

	public static String getOS ()
	{
		return System.getProperty ("os.arch");
	}
}
