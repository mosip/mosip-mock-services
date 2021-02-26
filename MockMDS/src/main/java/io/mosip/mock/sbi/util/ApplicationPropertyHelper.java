package io.mosip.mock.sbi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationPropertyHelper {
	private static Properties properties;

	public static String getPropertyKeyValue (String key)
	{
		try
		{
			createPropertyInfo ();

			if (properties != null)
			{
				return properties.getProperty (key);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
		}

		return null;
	}

	private static void createPropertyInfo ()
	{
		try
		{
			if (properties == null)
			{
				properties = new Properties ();
				try
				{
					//InputStream stream = ApplicationPropertyHelper.class.getClass().getResourceAsStream("application.properties");
					InputStream stream = new FileInputStream (new File (".").getCanonicalPath () + "/application.properties");
					properties.load (stream);
				}
				catch (Exception ex)
				{
					ex.printStackTrace ();
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
		}
	}
}
