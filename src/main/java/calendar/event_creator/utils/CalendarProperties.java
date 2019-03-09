package calendar.event_creator.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CalendarProperties {
	private static final String PROPERTY_FILE = "calendar.properties";
	private static Properties props;

	public static String getProperty(String property) {
		if (props == null) {
			readProperties();
		}
		return props.getProperty(property);
	}

	private static void readProperties() {
		props = new Properties();
		try (InputStream in = new FileInputStream(PROPERTY_FILE)) {
			props.load(in);
		} catch (IOException e) {
			InputStream in = CalendarProperties.class.getResourceAsStream("/" + PROPERTY_FILE);
			try {
				props.load(in);
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException();
			}
		}
	}
}