package calendar.event_creator.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import calendar.event_creator.utils.CalendarProperties;

public class FootballDataRestClient {
	private static final Log log = LogFactory.getLog(FootballDataRestClient.class);
	private static final String AUTH_TOKEN_VALUE = CalendarProperties.getProperty("football.data.api.token");
	private static final String AUTH_TOKEN_KEY = "X-Auth-Token";
	private static final String FOOTBALL_DATA_SITE = "api.football-data.org";
	private static final String FOOTBALL_DATA_MATCHES = "/v2/teams/%s/matches";
	private static final String FOOTBALL_DATA_TEAM = "/v2/teams/%s";
	private static final String UPCOMING_MATCHES_PARAMETER = "SCHEDULED";
	private static final String TEAM_LABEL = "tla";
	private static int matchesUpdateLimit = Integer.valueOf(CalendarProperties.getProperty("matches.update.limit"));
	public static int num_of_calls = 0;

	public static String getTeamMatchesAsString(String id) throws Exception {
		num_of_calls++;
		HttpGet httpGet = buildHttpGetMatches(id);
		return getSiteData(httpGet);
	}

	public static String getTeamLabel(String id) throws Exception {
		num_of_calls++;
		HttpGet httpGet = buildHttpGetTeam(id);
		String teamInfo = getSiteData(httpGet);
		return new ObjectMapper()
				.readTree(teamInfo)
				.get(TEAM_LABEL)
				.asText();
	}

	private static HttpGet buildHttpGetMatches(String id) throws URISyntaxException {
		matchesUpdateLimitModifier(matchesUpdateLimit);
		URI uri = new URIBuilder()
				.setScheme("https")
				.setHost(FOOTBALL_DATA_SITE)
				.setPath(String.format(FOOTBALL_DATA_MATCHES, id))
				.setParameter("status", UPCOMING_MATCHES_PARAMETER)
				//.setParameter("limit", String.valueOf(matchesUpdateLimit))
				.build();
		return buildHttpGet(uri);
	}

	private static HttpGet buildHttpGetTeam(String id) throws URISyntaxException {
		URI uri = new URIBuilder()
				.setScheme("https")
				.setHost(FOOTBALL_DATA_SITE)
				.setPath(String.format(FOOTBALL_DATA_TEAM, id))
				.build();
		return buildHttpGet(uri);
	}

	private static HttpGet buildHttpGet(URI uri) {
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader(AUTH_TOKEN_KEY, AUTH_TOKEN_VALUE);
		httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		return httpGet;
	}

	private static String getSiteData(HttpGet httpGet) throws ClientProtocolException, IOException {
		HttpResponse response = HttpClientBuilder
				.create()
				.build()
				.execute(httpGet);
		String stringResponse = EntityUtils.toString(response.getEntity());
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			log.error("Error with football data response!");
			log.error(stringResponse);
			System.exit(0);
		}
		return stringResponse;
	}

	private static void matchesUpdateLimitModifier(int limit) {
		if (limit < 1 || limit > 8) {
			matchesUpdateLimit = 5;
		}
	}
}
