package calendar.event_creator.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import calendar.event_creator.match.Matches;
import calendar.event_creator.rest.FootballDataRestClient;
import calendar.event_creator.utils.CalendarProperties;
import calendar.event_creator.utils.MatchesComparator;

public class AppService {
	private static final Log log = LogFactory.getLog(AppService.class);
	private static final String TEAM_ID = CalendarProperties.getProperty("team.id");
	private GoogleCalendarService googleCalendarService;

	public void updateCalendar() throws Exception {
		String response = FootballDataRestClient.getTeamMatchesAsString(TEAM_ID);
		Matches matches = new ObjectMapper().readValue(response, Matches.class);
		matches.getMatches().sort(new MatchesComparator());

		googleCalendarService = new GoogleCalendarService()
				.setTeamId(TEAM_ID)
				.setEventList(getNearestMatchUtcDate(matches));

		matches.getMatches().forEach(match -> googleCalendarService.triggerEvent(match));
		log.info("football-data.org has been called " + FootballDataRestClient.num_of_calls + " times.");
	}

	private String getNearestMatchUtcDate(Matches matches) {
		if (matches.getMatches().size() > 0) {
			return matches.getMatches().get(0).getUtcDate();
		} else {
			log.info("There were no registered matches!");
			throw new RuntimeException();
		}
	}
}
