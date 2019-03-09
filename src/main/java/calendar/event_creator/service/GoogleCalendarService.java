package calendar.event_creator.service;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import calendar.event_creator.db.match.MatchDb;
import calendar.event_creator.db.match.MatchDbConnection;
import calendar.event_creator.football_data.match.Match;
import calendar.event_creator.rest.FootballDataRestClient;
import calendar.event_creator.rest.GoogleCalendarRestClient;
import calendar.event_creator.utils.CalendarProperties;
import calendar.event_creator.utils.DateMatcher;

public class GoogleCalendarService {
	private static final Log log = LogFactory.getLog(GoogleCalendarService.class);
	private static final String CALENDAR_ID = CalendarProperties.getProperty("calendar.id");
	private static final String CHAMPIONSHIP = "REGULAR_SEASON";
	private static final String TIMEZONE = "UTC";
	private static final String EVENT_CREATE_TIMEZONE = "Europe/Budapest";
	private static final String EVENT_LENGTH = "-02:00";
	private static final String CANCELLED_STATUS = "cancelled";
	private String teamId;
	private String teamName;
	private List<Event> events;

	private Calendar calendar;
	private MatchDbConnection matchDbConnection;

	public GoogleCalendarService() throws Exception {
		calendar = GoogleCalendarRestClient.buildCalendar();
		matchDbConnection = new MatchDbConnection();
	}

	public void triggerEvent(Match match) {
		try {
			if (events == null) {
				setEventList(match);
			}
			match.setSummary(getSummary(match));
			String matchId = createMatchId(match);
			MatchDb matchDb = matchDbConnection.findMatchDbyId(matchId);
			log.info("Checking " + match.getSummary());

			if (matchDb == null) {
				Event event = searchEventByMatchId(matchId);
				event = event == null ? createEvent(match) : event;
				matchDbConnection.addMatchDb(matchId, event.getId(), match.getUtcDate());
				log.info(match.getSummary() + " is updated in the DB.");
			} else {
				Event calendarEvent = getEvent(matchDb.getEventId());
				String calendarEventTime = calendarEvent.getStart().getDateTime().toString();
				String matchDate = match.getUtcDate();

				if (DateMatcher.equals(matchDb.getDate(), matchDate)) {
					if (calendarEvent.getStatus().equals(CANCELLED_STATUS)) {
						Event event = createEvent(match);
						matchDbConnection.updateMatchDb(matchDb, event.getId(), matchDate);
					} else if (!DateMatcher.equals(calendarEventTime, matchDate)) {
						Event event = updateEvent(match, matchDb.getEventId());
						matchDbConnection.updateMatchDb(matchDb, event.getId(), matchDate);
					}
				} else {
					Event event = calendarEvent.getStatus().equals(CANCELLED_STATUS)
							? createEvent(match)
							: updateEvent(match, matchDb.getEventId());
					matchDbConnection.updateMatchDb(matchDb, event.getId(), matchDate);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GoogleCalendarService setTeamId(String teamId) {
		this.teamId = teamId;
		return this;
	}

	public void setEventList(Match match) throws IOException {
		this.events = calendar.events().list(CALENDAR_ID)
				.setTimeZone(TIMEZONE)
				.setTimeMin(new DateTime(match.getUtcDate()))
				.setSingleEvents(true)
				.setOrderBy("startTime")
				.execute()
				.getItems();
	}

	public void flushDb() throws Exception {
		matchDbConnection.fileDbWriter();
	}

	private Event searchEventByMatchId(String matchId) {
		for (Event event : events) {
			String googleEventId = createMatchId(event);
			if (matchId.equals(googleEventId)) {
				return event;
			}
		}
		return null;
	}

	private Event createEvent(Match match) throws IOException {
		Event event = new Event()
				.setSummary(match.getSummary())
				.setDescription(getDescription(match));
		addDateTimesToEvent(event, match.getUtcDate());
		event.setColorId("9");
		Event updated = calendar.events().insert(CALENDAR_ID, event).execute();
		log.info(match.getSummary() + " is created.");
		return updated;
	}

	private Event updateEvent(Match match, String eventId) throws IOException {
		deleteEvent(eventId);
		Event event = createEvent(match);
		log.info(match.getSummary() + " is updated.");
		return event;
	}

	private void deleteEvent(String eventId) throws IOException {
		calendar.events().delete(CALENDAR_ID, eventId).execute();
	}

	private Event getEvent(String eventId) throws IOException {
		return calendar.events().get(CALENDAR_ID, eventId).setTimeZone(TIMEZONE).execute();
	}

	private void addDateTimesToEvent(Event event, String date) {
		event.setStart(createTimeZonedEventDateTime(date))
				.setEnd(createTimeZonedEventDateTime(date.replace("Z", "") + EVENT_LENGTH));
	}

	private EventDateTime createTimeZonedEventDateTime(String date) {
		return new EventDateTime()
				.setDateTime(new DateTime(date))
				.setTimeZone(EVENT_CREATE_TIMEZONE);
	}

	private String createMatchId(Match match) {
		String matchId = match.getSummary() + getDescription(match);
		return matchId.replaceAll("[\\s-:.]", "");
	}

	private String createMatchId(Event event) {
		String matchId = event.getSummary() + event.getDescription();
		return matchId.replaceAll("[\\s-:.]", "");
	}

	private String getDescription(Match match) {
		return match.getCompetition().getName() + getCompetitionDescription(match) +
				System.lineSeparator() +
				String.format("%s - %s", match.getHomeTeam().getName(), match.getAwayTeam().getName());
	}

	private String getCompetitionDescription(Match match) {
		return match.getStage().equals(CHAMPIONSHIP)
				? ": " + match.getMatchday() + ". matchday"
				: ": " + match.getStage().toLowerCase().replace("_", " ");
	}

	private String getSummary(Match match) throws Exception {
		String homeTeamLabel = null;
		String awayTeamLabel = null;
		String homeTeamId = match.getHomeTeam().getId();
		String awayTeamId = match.getAwayTeam().getId();

		if (homeTeamId.equals(teamId)) {
			setTeamName(homeTeamId);
			homeTeamLabel = teamName;
			awayTeamLabel = FootballDataRestClient.getTeamLabel(awayTeamId);
		} else {
			setTeamName(awayTeamId);
			homeTeamLabel = FootballDataRestClient.getTeamLabel(homeTeamId);
			awayTeamLabel = teamName;
		}
		return String.format("%s-%s", homeTeamLabel, awayTeamLabel);
	}

	private void setTeamName(String id) throws Exception {
		if (teamName == null) {
			teamName = FootballDataRestClient.getTeamLabel(id);
		}
	}
}
