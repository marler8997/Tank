package jmar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTime {
	private static SimpleDateFormat standardDateTimeDashes  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat standardDateTimeSlashes = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static Date parseDashes(String dateTime) throws ParseException {
		return standardDateTimeDashes.parse(dateTime);
	}
	
	public static String nowStringSlashes() {
		return standardDateTimeSlashes.format(new Date());
	}
}
