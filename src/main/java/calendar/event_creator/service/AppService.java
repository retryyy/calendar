package calendar.event_creator.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import calendar.event_creator.match.Match;
import calendar.event_creator.match.Matches;
import calendar.event_creator.rest.FootballDataRestClient;
import calendar.event_creator.utils.CalendarProperties;
import calendar.event_creator.utils.MatchesComparator;

public class AppService {
	private static final Log log = LogFactory.getLog(AppService.class);
	private static final String TEAM_ID = CalendarProperties.getProperty("team.id");
	private GoogleCalendarService googleCalendarService;
	private static int matchesUpdateLimit = Integer.valueOf(CalendarProperties.getProperty("matches.update.limit"));

	public void updateCalendar() throws Exception {
		String response = FootballDataRestClient.getTeamMatchesAsString(TEAM_ID);
		Matches matches = new ObjectMapper().readValue(response, Matches.class);
		matches.getMatches().sort(new MatchesComparator());
		limitMatches(matches);

		String teamName = FootballDataRestClient.getTeamLabel(TEAM_ID);
		DemandedTeam team = new DemandedTeam(TEAM_ID, teamName);

		googleCalendarService = new GoogleCalendarService()
				.setTeam(team)
				.setEventList(getNearestMatchUtcDate(matches));

		matches.getMatches().forEach(match -> googleCalendarService.triggerEvent(match));
		googleCalendarService.deletePostponedMatchEvents();
		log.info("football-data.org has been called " + FootballDataRestClient.num_of_calls + " times");
	}

	private String getNearestMatchUtcDate(Matches matches) {
		if (matches.getMatches().size() > 0) {
			return matches.getMatches().get(0).getUtcDate();
		} else {
			log.warn("There is no registered match!");
			throw new RuntimeException();
		}
	}

	private void limitMatches(Matches matches) {
		List<Match> limitedMatches = matches.getMatches()
				.stream()
				.limit(matchesUpdateLimit)
				.collect(Collectors.toList());
		matches.setMatches(limitedMatches);
	}
}
