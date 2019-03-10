package calendar.event_creator.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import calendar.event_creator.football_data.match.Match;

public class MatchesComparator implements Comparator<Match> {

	@Override
	public int compare(Match m1, Match m2) {
		try {
			Date date1 = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(m1.getUtcDate());
			Date date2 = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(m2.getUtcDate());
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