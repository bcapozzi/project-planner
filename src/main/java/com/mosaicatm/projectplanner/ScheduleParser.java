package com.mosaicatm.projectplanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ScheduleParser {

	public static Schedule parse(String filePath) throws Exception {
		
		Schedule schedule = new Schedule();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String nextLine = reader.readLine(); // skip header 
		List<Integer> totalColumnIndices = findTotalColumns(nextLine);
		while ((nextLine = reader.readLine()) != null) {
			String [] tokens = nextLine.split(",");
			// expect data in form taskName, resourceName, W1, W2, W3, W4, total ... for each month
			String taskName = tokens[0].trim();
			String resourceName = tokens[1].trim();
			TaskAssignment assignment = new TaskAssignment(taskName, resourceName);
			schedule.addTaskAssignment(assignment);
			for (int i=2; i<tokens.length; i++) {
				
				if (totalColumnIndices.contains(i))
					continue;
				
				int hoursInWeek = parseHoursInWeekToken(tokens[i].trim());
				assignment.addHoursForWeek(hoursInWeek);
			}
		}
		
		reader.close();
		
		return schedule;
	}

	private static List<Integer> findTotalColumns(String nextLine) {
		String [] tokens = nextLine.split(",");
		List<Integer> indices = new ArrayList<Integer>();
		for (int i=0; i<tokens.length; i++) {
			String token = tokens[i];
			if (token.trim().equalsIgnoreCase("Total")) {
				indices.add(i);
			}
		}
		return indices;
	}

	private static int parseHoursInWeekToken(String token) {
		if (token.length() == 0)
			return 0;
		return Integer.parseInt(token);
	}

}
