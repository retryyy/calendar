package calendar.event_creator.football_data.match;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {
	private String utcDate;
	private String matchday;
	private String stage;
	private MatchObject competition;
	private MatchObject homeTeam;
	private MatchObject awayTeam;
	@JsonIgnore
	private String summary;

	public String getUtcDate() {
		return utcDate;
	}

	public void setUtcDate(String utcDate) {
		this.utcDate = utcDate;
	}

	public String getMatchday() {
		return matchday;
	}

	public void setMatchday(String matchday) {
		this.matchday = matchday;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public MatchObject getCompetition() {
		return competition;
	}

	public void setCompetition(MatchObject competition) {
		this.competition = competition;
	}

	public MatchObject getHomeTeam() {
		return homeTeam;
	}

	public void setHomeTeam(MatchObject homeTeam) {
		this.homeTeam = homeTeam;
	}

	public MatchObject getAwayTeam() {
		return awayTeam;
	}

	public void setAwayTeam(MatchObject awayTeam) {
		this.awayTeam = awayTeam;
	}

	@JsonIgnore
	public String getSummary() {
		return summary;
	}

	@JsonIgnore
	public void setSummary(String summary) {
		this.summary = summary;
	}
}
