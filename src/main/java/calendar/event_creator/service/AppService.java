package calendar.event_creator.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import calendar.event_creator.football_data.match.Matches;
import calendar.event_creator.rest.FootballDataRestClient;
import calendar.event_creator.utils.CalendarProperties;

public class AppService {
	private static final Log log = LogFactory.getLog(AppService.class);
	private static final String TEAM_ID = CalendarProperties.getProperty("team.id");
	private static final String QUERY_DATE_FORMAT = "yyyy-MM-dd";
	private GoogleCalendarService googleCalendarService;
	private String currentTime;
	private String monthLaterTime;

	public AppService() {
		try {
			setDates();
			googleCalendarService = new GoogleCalendarService().setTeamId(TEAM_ID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public void updateCalendar() throws Exception {
		String response = FootballDataRestClient.getTeamMatchesAsString(TEAM_ID, currentTime, monthLaterTime);
		Matches matches = new ObjectMapper().readValue(response, Matches.class);
		matches.getMatches().forEach(match -> googleCalendarService.triggerEvent(match));
		googleCalendarService.flushDb();
		log.info("football-data.org has been called " + FootballDataRestClient.num_of_calls + " times.");
	}

	private void setDates() {
		currentTime = new SimpleDateFormat(QUERY_DATE_FORMAT).format(Calendar.getInstance().getTime());

		Calendar oneMonthLater = Calendar.getInstance();
		oneMonthLater.add(Calendar.MONTH, 1);
		monthLaterTime = new SimpleDateFormat(QUERY_DATE_FORMAT).format(oneMonthLater.getTime());
	}
}
