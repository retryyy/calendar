package calendar.event_creator.rest;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import calendar.event_creator.utils.CalendarProperties;

public class GoogleCalendarRestClient {
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String CREDENTIALS_FILE_PATH = CalendarProperties.getProperty("credentials.path");
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	public static Calendar buildCalendar() throws Exception {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		return new Calendar.Builder(
				HTTP_TRANSPORT,
				JSON_FACTORY,
				getCredentials(HTTP_TRANSPORT))
						.build();
	}

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
		InputStream in = GoogleCalendarRestClient.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT,
				JSON_FACTORY,
				clientSecrets,
				Collections.singletonList(CalendarScopes.CALENDAR))
						.setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline")
						.build();
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}
}
