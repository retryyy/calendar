package calendar.event_creator;

import calendar.event_creator.service.AppService;

public class CalendarApp {

	public static void main(String[] args) throws Exception {
		new AppService().updateCalendar();
	}
}