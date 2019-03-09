package calendar.event_creator.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import calendar.event_creator.utils.CalendarProperties;

public class FootballDataRestClient {
	private static final String AUTH_TOKEN_VALUE = CalendarProperties.getProperty("football.data.api.token");
	private static String matches_update_limit = CalendarProperties.getProperty("matches.update.limit");
	private static final String AUTH_TOKEN_KEY = "X-Auth-Token";
	private static final String FOOTBALL_DATA_SITE = "api.football-data.org";
	private static final String FOOTBALL_DATA_MATCHES = "/v2/teams/%s/matches";
	private static final String FOOTBALL_DATA_TEAM = "/v2/teams/%s";
	private static final String UPCOMING_MATCHES_PARAMETER = "SCHEDULED";
	private static final String TEAM_LABEL = "tla";
	public static int num_of_calls = 0;

	public static String getTeamMatchesAsString(String id, String dateFrom, String dateTo) throws Exception {
		num_of_calls++;
		HttpGet httpGet = buildHttpGetMatches(id, dateFrom, dateTo);
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

	private static HttpGet buildHttpGetMatches(String id, String dateFrom, String dateTo) throws URISyntaxException {
		matchesUpdateLimitModifier(matches_update_limit);
		URI uri = new URIBuilder()
				.setScheme("https")
				.setHost(FOOTBALL_DATA_SITE)
				.setPath(String.format(FOOTBALL_DATA_MATCHES, id))
				.addParameter("dateFrom", dateFrom)
				.setParameter("dateTo", dateTo)
				.setParameter("status", UPCOMING_MATCHES_PARAMETER)
				.setParameter("limit", matches_update_limit)
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
		return EntityUtils.toString(HttpClientBuilder
				.create()
				.build()
				.execute(httpGet)
				.getEntity());
	}

	private static void matchesUpdateLimitModifier(String limit) {
		int updateLimit = Integer.valueOf(limit);
		if (updateLimit < 1 || updateLimit > 8) {
			matches_update_limit = String.valueOf(5);
		}
	}
}
