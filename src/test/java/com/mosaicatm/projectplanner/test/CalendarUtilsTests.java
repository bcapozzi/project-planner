package com.mosaicatm.projectplanner.test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import com.mosaicatm.projectplanner.CalendarUtils;

public class CalendarUtilsTests {

	private SimpleDateFormat dateFormatter;
	
	@Test public void canDetermineNumberOfWeeksLeftInMonth() throws Exception {
		
		assertEquals(2, CalendarUtils.getNumberOfWeeksLeftInMonth(constructDateForString("06/15/2015")));
		assertEquals(1, CalendarUtils.getNumberOfWeeksLeftInMonth(constructDateForString("06/20/2015")));
		assertEquals(1, CalendarUtils.getNumberOfWeeksLeftInMonth(constructDateForString("06/25/2015")));
		
	}

	private Date constructDateForString(String string) throws Exception {
		initializeFormatterIfNecessary();
		Date date = dateFormatter.parse(string);
		return date;
	}

	private void initializeFormatterIfNecessary() {
		if (dateFormatter == null)
		{
			dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
			dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		}
	}
}
