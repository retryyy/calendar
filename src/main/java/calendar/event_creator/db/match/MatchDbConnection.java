package calendar.event_creator.db.match;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import calendar.event_creator.utils.CalendarProperties;
import calendar.event_creator.utils.MatchDbComparator;

public class MatchDbConnection {
	private static final String FILE_DB_PATH = CalendarProperties.getProperty("file.db.path");
	private ObjectMapper mapper;
	private ObjectWriter writer;
	private List<MatchDb> matches;

	public MatchDbConnection() throws Exception {
		mapper = new ObjectMapper();
		writer = mapper.writer(new DefaultPrettyPrinter());
		fileDbReader();
		cleanDb();
	}

	public MatchDb findMatchDbyId(String matchId) {
		for (MatchDb match : matches) {
			if (match.getMatchId().equals(matchId)) {
				return match;
			}
		}
		return null;
	}

	public void addMatchDb(String matchId, String eventId, String date) {
		matches.add(new MatchDb() {
			{
				setMatchId(matchId);
				setEventId(eventId);
				setDate(date);
			}
		});
	}

	public void updateMatchDb(MatchDb matchDb, String eventId, String date) {
		matchDb.setEventId(eventId);
		matchDb.setDate(date);
	}

	public void fileDbWriter() throws JsonGenerationException, JsonMappingException, IOException {
		matches.sort(new MatchDbComparator());
		writer.writeValue(new File(FILE_DB_PATH), matches);
	}

	private void fileDbReader() throws Exception {
		File file = new File(FILE_DB_PATH);
		if (!file.exists()) {
			writer.writeValue(file, new ArrayList<>());
		}
		byte[] data = Files.readAllBytes(Paths.get(FILE_DB_PATH));
		matches = mapper.readValue(data, new TypeReference<List<MatchDb>>() {
		});
	}

	private void cleanDb() {
		matches.removeIf(match -> {
			try {
				return new SimpleDateFormat("yyyy-MM-dd").parse(match.getDate()).before(new Date());
			} catch (ParseException e) {
				return false;
			}
		});
	}
}
