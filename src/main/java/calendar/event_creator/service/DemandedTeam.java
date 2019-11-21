package calendar.event_creator.service;

public class DemandedTeam {
	private String teamId;
	private String teamName;

	public DemandedTeam(String teamId, String teamName) {
		this.teamId = teamId;
		this.teamName = teamName;
	}

	public String getTeamId() {
		return teamId;
	}

	public String getTeamName() {
		return teamName;
	}
}
