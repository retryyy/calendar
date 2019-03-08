package calendar.event_creator.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateMatcher {
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm";

	public static boolean equals(String time1, String time2) throws ParseException {
		Date d1 = new SimpleDateFormat(DATE_FORMAT).parse(time1);
		Date d2 = new SimpleDateFormat(DATE_FORMAT).parse(time2);
		return d1.compareTo(d2) == 0;
	}
}
