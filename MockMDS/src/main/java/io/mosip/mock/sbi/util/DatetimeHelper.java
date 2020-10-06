package io.mosip.mock.sbi.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatetimeHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatetimeHelper.class);	
    public static String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSzzz";

    public static String getISO8601CurrentDate ()
    {
    	SimpleDateFormat sdf = new SimpleDateFormat(DatetimeHelper.ISO8601_DATE_FORMAT);    	
    	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    	
        return sdf.format(timestamp);
    }
}
