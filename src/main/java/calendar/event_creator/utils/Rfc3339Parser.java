package calendar.event_creator.utils;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rfc3339Parser {
	private static final Pattern PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})T(\\d{2}:\\d{2})");

	public static String format(String rfc3339) {
		Matcher matcher = PATTERN.matcher(rfc3339);
		if (matcher.find()) {
			return MessageFormat.format("{1}, {0}", matcher.group(1), matcher.group(2));
		}
		return "Could not parse date!";
	}
}
