package calendar.event_creator.service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import calendar.event_creator.match.Match;
import calendar.event_creator.rest.GoogleCalendarRestClient;
import calendar.event_creator.utils.CalendarProperties;
import calendar.event_creator.utils.DateMatcher;
import calendar.event_creator.utils.Rfc3339Parser;

public class GoogleCalendarService {
	private static final Log log = LogFactory.getLog(GoogleCalendarService.class);
	private static final String CALENDAR_ID = CalendarProperties.getProperty("calendar.id");
	private static final String TIMEZONE = "UTC";
	private static final String EVENT_CREATE_TIMEZONE = "Europe/Budapest";
	private static final String EVENT_LENGTH = "-02:00";
	private DemandedTeam team;
	private List<Event> events;
	private Calendar calendar;

	public GoogleCalendarService() throws Exception {
		calendar = GoogleCalendarRestClient.buildCalendar();
	}

	public void triggerEvent(Match match) {
		try {
			match.setSummary(team);
			Event event = searchEventByMatchId(createMatchId(match));

			String summary = match.getSummary();
			if (event == null) {
				event = createEvent(match);
				log.info("CREATED: " + summary + " [" + getFormattedStartDate(event) + "]");

			} else {
				String eventTime = event.getStart().getDateTime().toString();
				if (!DateMatcher.equals(eventTime, match.getUtcDate())) {
					event = updateEvent(match, event.getId());
					log.info("UPDATED: " + summary + " (" + getFormattedStartDate(event) + ")");

				} else {
					log.info("UNTOUCHED: " + summary);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GoogleCalendarService setTeam(DemandedTeam team) throws Exception {
		this.team = team;
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
		String matchSummaryRegex = MessageFormat.format("{0}-{1}|{1}-{0}", team.getTeamName(), "[A-Z]{3}");
		events.removeIf(event -> event.getSummary() == null || !event.getSummary().matches(matchSummaryRegex));
	}

	public void deletePostponedMatchEvents() {
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
			if (matchId.equals(googleEventId))
				return events.remove(i);
		}
		return null;
	}

	private Event createEvent(Match match) throws IOException {
		Event event = new Event()
				.setSummary(match.getSummary())
				.setDescription(match.getDescription())
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
		return match.getSummary() + match.getDescription();
	}

	private String createMatchId(Event event) {
		return event.getSummary() + event.getDescription();
	}

	private String getFormattedStartDate(Event event) {
		String date = event.getStart().getDateTime().toStringRfc3339();
		return Rfc3339Parser.format(date);
	}
}
