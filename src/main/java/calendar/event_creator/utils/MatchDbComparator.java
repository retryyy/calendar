package calendar.event_creator.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import calendar.event_creator.db.match.MatchDb;

public class MatchDbComparator implements Comparator<MatchDb> {

	@Override
	public int compare(MatchDb m1, MatchDb m2) {
		try {
			Date date1 = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(m1.getDate());
			Date date2 = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(m2.getDate());
			return date1.equals(date2)
					? 0
					: date1.before(date2)
							? -1
							: 1;
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
