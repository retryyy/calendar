package calendar.event_creator.service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import calendar.event_creator.match.Match;
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
	private String teamId;
	private String teamName;
	private List<Event> events;
	private Calendar calendar;

	public GoogleCalendarService() throws Exception {
		calendar = GoogleCalendarRestClient.buildCalendar();
	}

	public void triggerEvent(Match match) {
		try {
			match.setSummary(createMatchSummary(match));
			String matchId = createMatchId(match);
			Event event = searchEventByMatchId(matchId);

			if (event == null) {
				createEvent(match);
				log.info("CREATED: " + match.getSummary());
			} else {
				String eventTime = event.getStart().getDateTime().toString();
				if (!DateMatcher.equals(eventTime, match.getUtcDate())) {
					updateEvent(match, event.getId());
					log.info("UPDATED: " + match.getSummary());
				} else {
					log.info("UNTOUCHED: " + match.getSummary());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GoogleCalendarService setTeam(String teamId) throws Exception {
		this.teamId = teamId;
		this.teamName = FootballDataRestClient.getTeamLabel(teamId);
		return this;
	}

	public GoogleCalendarService setEventList(String date) throws IOException {
		this.events = calendar.events().list(CALENDAR_ID)
				.setTimeZone(TIMEZONE)
				.setTimeMin(new DateTime(date))
				.setSingleEvents(true)
				.setOrderBy("startTime")
				.execute()
				.getItems();
		cleanOutNonMatchEvents();
		return this;
	}

	private void cleanOutNonMatchEvents() {
		String matchSummaryRegex = MessageFormat.format("{0}-{1}|{1}-{0}", teamName, "[A-Z]{3}");
		Iterator<Event> eventIterator = events.iterator();
		while (eventIterator.hasNext()) {
			Event event = eventIterator.next();
			if (!event.getSummary().matches(matchSummaryRegex)) {
				eventIterator.remove();
			}
		}
	}

	public void deletePostponedMatchEvent() {
		for (Event event : events) {
			try {
				if (event.getDescription() != null && !event.getDescription().equals("")) {
					deleteEvent(event.getId());
					log.info("DELETED: " + event.getSummary());
				}
			} catch (IOException e) {
				log.warn("Could not delete event: " + event.getSummary());
			}
		}
	}

	private Event searchEventByMatchId(String matchId) {
		for (int i = 0; i < events.size(); i++) {
			Event event = events.get(i);
			String googleEventId = createMatchId(event);
			if (matchId.equals(googleEventId)) {
				return events.remove(i);
			}
		}
		return null;
	}

	private Event createEvent(Match match) throws IOException {
		Event event = new Event()
				.setSummary(match.getSummary())
				.setDescription(getDescription(match))
				.setColorId("9");
		addDateTimesToEvent(event, match.getUtcDate());
		return calendar.events()
				.insert(CALENDAR_ID, event)
				.execute();
	}

	private Event updateEvent(Match match, String eventId) throws IOException {
		deleteEvent(eventId);
		return createEvent(match);
	}

	private void deleteEvent(String eventId) throws IOException {
		calendar.events().delete(CALENDAR_ID, eventId).execute();
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
		return match.getSummary() + getDescription(match);
	}

	private String createMatchId(Event event) {
		return event.getSummary() + event.getDescription();
	}

	private String getDescription(Match match) {
		return match.getCompetition().getName() + getCompetitionDescription(match) +
				System.lineSeparator() +
				MessageFormat.format("{0} - {1}", match.getHomeTeam().getName(), match.getAwayTeam().getName());
	}

	private String getCompetitionDescription(Match match) {
		return match.getStage().equals(CHAMPIONSHIP)
				? ": " + match.getMatchday() + ". matchday"
				: ": " + match.getStage().toLowerCase().replace("_", " ");
	}

	private String createMatchSummary(Match match) throws Exception {
		String homeTeamId = match.getHomeTeam().getId();
		String awayTeamId = match.getAwayTeam().getId();

		if (homeTeamId.equals(teamId)) {
			return MessageFormat.format("{0}-{1}", teamName, FootballDataRestClient.getTeamLabel(awayTeamId));
		} else {
			return MessageFormat.format("{0}-{1}", FootballDataRestClient.getTeamLabel(homeTeamId), teamName);
		}
	}
}
