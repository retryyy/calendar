package calendar.event_creator.utils;

import java.io.FileReader;
import java.util.Properties;

public class CalendarProperties {
	private static Properties props;

	public static String getProperty(String property) {
		if (props == null) {
			try (FileReader reader = new FileReader("calendar.properties")) {
				props = new Properties();
				props.load(reader);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
		return props.getProperty(property);
	}
}