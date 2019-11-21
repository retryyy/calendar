package calendar.event_creator.match;

import java.text.MessageFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import calendar.event_creator.rest.FootballDataRestClient;
import calendar.event_creator.service.DemandedTeam;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {
	private static final String CHAMPIONSHIP = "REGULAR_SEASON";

	private String utcDate;
	private String matchday;
	private String stage;
	private MatchObject competition;
	private MatchObject homeTeam;
	private MatchObject awayTeam;
	private String summary;

	public String getUtcDate() {
		return utcDate;
	}

	public String getMatchday() {
		return matchday;
	}

	public String getStage() {
		return stage;
	}

	public MatchObject getCompetition() {
		return competition;
	}

	public MatchObject getHomeTeam() {
		return homeTeam;
	}

	public MatchObject getAwayTeam() {
		return awayTeam;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(DemandedTeam team) throws Exception {
		String homeTeamId = getHomeTeam().getId();
		String awayTeamId = getAwayTeam().getId();

		this.summary = homeTeamId.equals(team.getTeamId())
				? MessageFormat.format("{0}-{1}", team.getTeamName(), FootballDataRestClient.getTeamLabel(awayTeamId))
				: MessageFormat.format("{0}-{1}", FootballDataRestClient.getTeamLabel(homeTeamId), team.getTeamName());
	}

	public String getDescription() {
		return getCompetition().getName() + getCompetitionDescription() +
				System.lineSeparator() +
				MessageFormat.format("{0} - {1}", getHomeTeam().getName(), getAwayTeam().getName());
	}

	private String getCompetitionDescription() {
		return getStage().equals(CHAMPIONSHIP)
				? ": " + getMatchday() + ". matchday"
				: ": " + getStage().toLowerCase().replace("_", " ");
	}
}
