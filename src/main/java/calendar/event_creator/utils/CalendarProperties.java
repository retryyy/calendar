package calendar.event_creator.utils;

import java.io.IOException;
import java.util.Properties;

public class CalendarProperties {
	private static Properties props;

	public static String getProperty(String property) {
		readProperties();
		return props.getProperty(property);
	}

	private static void readProperties() {
		if (props == null) {
			props = new Properties();
			try {
				props.load(CalendarProperties.class.getResourceAsStream("/calendar.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}