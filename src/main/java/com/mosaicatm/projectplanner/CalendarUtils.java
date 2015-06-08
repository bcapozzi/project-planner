package com.mosaicatm.projectplanner;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CalendarUtils {

	public static int getNumberOfWeeksLeftInMonth(Date startDate) {
		// determine number of "weeks" left in first month based on start date
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		calendar.setTime(startDate);
		int startDay = calendar.get(Calendar.DAY_OF_MONTH);
		int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		int daysLeftInMonth = lastDayOfMonth - startDay;

		int numWeeksLeft = daysLeftInMonth/7;
		if (numWeeksLeft == 0 && daysLeftInMonth > 0)
			return 1;
		
		return numWeeksLeft;
	}
}
