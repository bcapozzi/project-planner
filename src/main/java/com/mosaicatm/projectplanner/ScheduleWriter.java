package com.mosaicatm.projectplanner;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ScheduleWriter {

	public static String toCSV(Schedule schedule) throws Exception {
		
		// TODO:  persist the data to a file
		//FileWriter writer = new FileWriter(absolutePath);
		
		// write the header
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("Task Name");
		buffer.append(",");
		buffer.append("Resource");
		buffer.append(",");
		int numWeeks = schedule.numWeeks();
		int currWeek = 1;
		for (int i=1; i<=numWeeks; i++) {
			buffer.append("W" + currWeek);
			buffer.append(",");
			if (i % 4 == 0) {
				currWeek = 1;
				buffer.append("Total");
				if (i < numWeeks)
					buffer.append(",");
			}
			else
			{
				currWeek++;
			}
		}
		buffer.append("\r\n");
		
		// now each task assignment
		for (TaskAssignment t: schedule.getTaskAssignments()) {
			buffer.append(t.getTaskName());
			buffer.append(",");
			buffer.append(t.getResourceName());
			buffer.append(",");
			List<Integer> hours = t.getHoursByWeek();
			int totalForMonth = 0;
			for (int i=1; i<=hours.size(); i++) {
				buffer.append(hours.get(i-1));
				buffer.append(",");
				totalForMonth += hours.get(i-1);
				if (i % 4 == 0) {
					buffer.append(totalForMonth);
					totalForMonth = 0;
					if (i < hours.size())
						buffer.append(",");
				}
			}
			buffer.append("\r\n");
		}
		
		return buffer.toString();
	}

	public static void toCSVFile(Schedule schedule, String filePath) throws Exception {
		String csv = toCSV(schedule);
		FileWriter writer = new FileWriter(filePath);
		writer.write(csv);
		writer.close();
	}
}
