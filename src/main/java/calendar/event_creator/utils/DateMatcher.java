package calendar.event_creator.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateMatcher {
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm";

	public static boolean equals(String date1, String date2) throws ParseException {
		Date d1 = new SimpleDateFormat(DATE_FORMAT).parse(date1);
		Date d2 = new SimpleDateFormat(DATE_FORMAT).parse(date2);
		return d1.compareTo(d2) == 0;
	}
}
