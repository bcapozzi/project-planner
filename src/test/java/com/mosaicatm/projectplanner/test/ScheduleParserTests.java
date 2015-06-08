package com.mosaicatm.projectplanner.test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;

import com.mosaicatm.projectplanner.Schedule;
import com.mosaicatm.projectplanner.ScheduleParser;
import com.mosaicatm.projectplanner.ScheduleWriter;

public class ScheduleParserTests {

	@Test public void canParseSchedule() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule.csv");
		assertNotNull(schedule);
		assertEquals(786, schedule.totalHours());
		assertEquals(12, schedule.numTasks());
	}
	
	@Test public void canParseScheduleV4() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule_v4.csv");
		assertNotNull(schedule);
		assertEquals(890, schedule.totalHours());
		assertEquals(13, schedule.numTasks());
	}
	
	@Test public void canGetScheduleForAGivenResource() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule.csv");
		Schedule scheduleForSeniorAnalyst = schedule.forResource("Senior Analyst");
		assertEquals(334, scheduleForSeniorAnalyst.totalHours());
	}
	
	@Test public void canGetScheduleForAGivenResourceWithinV4Schedule() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule_v4.csv");
		List<String> uniqueResources = schedule.getUniqueResourceNames();
		for (String resourceName: uniqueResources){
			Schedule scheduleForResource = schedule.forResource(resourceName);
			System.out.println("Schedule for resource: " + resourceName);
			System.out.println(scheduleForResource.displayByWeek());
		}
	}
	
	@Test public void canGetScheduleForAGivenResourceWithinV5Schedule() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule_v5.csv");
		List<String> uniqueResources = schedule.getUniqueResourceNames();
		for (String resourceName: uniqueResources){
			Schedule scheduleForResource = schedule.forResource(resourceName);
			System.out.println("Schedule for resource: " + resourceName);
			System.out.println(scheduleForResource.displayByWeek());
		}
		
		Map<String,Integer> rateMapInCents = new HashMap<>();
		rateMapInCents.put("Dev", 100*100);
		rateMapInCents.put("Senior Dev", 160*100);
		rateMapInCents.put("Senior Analyst", 170*100);
		rateMapInCents.put("Analyst", 93*100);
		rateMapInCents.put("Wargo", 192*100);
		rateMapInCents.put("Subcontractor", 144*100);
		
		int costInCents = schedule.estimateCostGivenRates(rateMapInCents);
		int costInDollars = costInCents/100;
		System.out.println("Estimated project cost: $" + costInDollars);
		assertEquals(125960, costInDollars);
		
	}
	
	@Test public void canGetHoursForGivenResourceOverPartOfProject() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule.csv");
		Schedule scheduleForSeniorAnalyst = schedule.forResource("Senior Analyst");
		assertEquals(334, scheduleForSeniorAnalyst.totalHours());
		assertEquals(30, scheduleForSeniorAnalyst.getHoursBetweenWeeks(0,2));
		assertEquals(76, scheduleForSeniorAnalyst.getHoursBetweenWeeks(2,6));
		int [] hours = {0,2,6,10,14,16,20,24};
		int total = 0;
		for (int i=0; i<hours.length-1; i++) {
			int h = scheduleForSeniorAnalyst.getHoursBetweenWeeks(hours[i],hours[i+1]);
			System.out.format("%2d,%2d\t%2d\n",hours[i],hours[i+1],h);
			total+=h;
		}
		System.out.println("-----------");
		System.out.format("    \t%d\n", total);
	}
	
	@Test public void canGetHoursForSeniorDevOverPartOfProject() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule.csv");
		Schedule scheduleForSeniorAnalyst = schedule.forResource("Senior Dev");
		int [] hours = {0,2,6,10,14,16,20,24};
		int total = 0;
		for (int i=0; i<hours.length-1; i++) {
			int h = scheduleForSeniorAnalyst.getHoursBetweenWeeks(hours[i],hours[i+1]);
			System.out.format("%2d,%2d\t%2d\n",hours[i],hours[i+1],h);
			total+=h;
		}
		System.out.println("-----------");
		System.out.format("    \t%d\n", total);
	}
	
	@Test public void canGetHoursForSeniorDevOverPartOfProject_v3() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule_v3.csv");
		Schedule scheduleForSeniorAnalyst = schedule.forResource("Senior Dev");
		int [] hours = {0,2,6,10,14,16,20,24};
		int total = 0;
		for (int i=0; i<hours.length-1; i++) {
			int h = scheduleForSeniorAnalyst.getHoursBetweenWeeks(hours[i],hours[i+1]);
			System.out.format("%2d,%2d\t%2d\n",hours[i],hours[i+1],h);
			total+=h;
		}
		System.out.println("-----------");
		System.out.format("    \t%d\n", total);
	}
	
	@Test public void canGetHoursForSeniorAnalystOverPartOfProject_v3() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule_v3.csv");
		Schedule scheduleForSeniorAnalyst = schedule.forResource("Senior Analyst");
		int [] hours = {0,2,6,10,14,16,20,24};
		int total = 0;
		for (int i=0; i<hours.length-1; i++) {
			int h = scheduleForSeniorAnalyst.getHoursBetweenWeeks(hours[i],hours[i+1]);
			System.out.format("%2d,%2d\t%2d\n",hours[i],hours[i+1],h);
			total+=h;
		}
		System.out.println("-----------");
		System.out.format("    \t%d\n", total);
	}
	
	@Test public void canGetHoursForSubContractorOverPartOfProject_v3() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule_v3.csv");
		Schedule scheduleForSeniorAnalyst = schedule.forResource("Subcontractor");
		int [] hours = {0,2,6,10,14,16,20,24};
		int total = 0;
		for (int i=0; i<hours.length-1; i++) {
			int h = scheduleForSeniorAnalyst.getHoursBetweenWeeks(hours[i],hours[i+1]);
			System.out.format("%2d,%2d\t%2d\n",hours[i],hours[i+1],h);
			total+=h;
		}
		System.out.println("-----------");
		System.out.format("    \t%d\n", total);
	}
	
	@Test public void canGetHoursForWargoOverPartOfProject_v3() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule_v3.csv");
		Schedule scheduleForSeniorAnalyst = schedule.forResource("Wargo");
		int [] hours = {0,2,6,10,14,16,20,24};
		int total = 0;
		for (int i=0; i<hours.length-1; i++) {
			int h = scheduleForSeniorAnalyst.getHoursBetweenWeeks(hours[i],hours[i+1]);
			System.out.format("%2d,%2d\t%2d\n",hours[i],hours[i+1],h);
			total+=h;
		}
		System.out.println("-----------");
		System.out.format("    \t%d\n", total);
	}
	
	@Test public void canDisplayScheduleForAGivenResource() throws Exception {
		
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule.csv");
		Schedule scheduleForSeniorAnalyst = schedule.forResource("Senior Analyst");
		System.out.println(scheduleForSeniorAnalyst.displayByWeek());
	}
	
	@Test public void canDisplayScheduleByMonthWithStartDateOffset() throws Exception {
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule_v3.csv");
		Schedule scheduleForSeniorAnalyst = schedule.forResource("Senior Analyst");
		System.out.println(scheduleForSeniorAnalyst.displayByMonthStartingAtWeek(Schedule.THIRD_WEEK_IN_MONTH));
	}
	
	@Test public void canWriteToFile() throws Exception {
		Schedule schedule = ScheduleParser.parse("src/test/resources/ua_teamer_schedule_v3.csv");
		System.out.println(ScheduleWriter.toCSV(schedule));
		
		ScheduleWriter.toCSVFile(schedule, "target/test_ua_schedule.csv");
		
		Schedule schedule2 = ScheduleParser.parse("target/test_ua_schedule.csv");
		assertEquals(schedule.totalHours(),schedule2.totalHours());
	}
}
