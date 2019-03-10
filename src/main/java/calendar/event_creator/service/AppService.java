package calendar.event_creator.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import calendar.event_creator.football_data.match.Matches;
import calendar.event_creator.rest.FootballDataRestClient;
import calendar.event_creator.utils.CalendarProperties;
import calendar.event_creator.utils.MatchesComparator;

public class AppService {
	private static final Log log = LogFactory.getLog(AppService.class);
	private static final String TEAM_ID = CalendarProperties.getProperty("team.id");
	private GoogleCalendarService googleCalendarService;
	private String currentTime;

	public AppService() {
		try {
			currentTime = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
			googleCalendarService = new GoogleCalendarService().setTeamId(TEAM_ID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public void updateCalendar() throws Exception {
		String response = FootballDataRestClient.getTeamMatchesAsString(TEAM_ID, currentTime);
		Matches matches = new ObjectMapper().readValue(response, Matches.class);
		matches.getMatches().sort(new MatchesComparator());
		matches.getMatches().forEach(match -> googleCalendarService.triggerEvent(match));
		log.info("football-data.org has been called " + FootballDataRestClient.num_of_calls + " times.");
	}
}
