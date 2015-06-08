package com.mosaicatm.projectplanner;

import java.util.ArrayList;
import java.util.List;

public class TaskAssignment {

	private List<Integer> hoursPerWeek = new ArrayList<>();
	private String taskName;
	private String resourceName;
	
	public TaskAssignment(String taskName, String resourceName) {
		this.taskName = taskName;
		this.resourceName = resourceName;
	}

	public void addHoursForWeek(int hoursInWeek) {
		hoursPerWeek.add(hoursInWeek);
	}
	
	public int totalHours() {
		int total = 0;
		for (Integer hrs: hoursPerWeek) {
			total += hrs;
		}
		return total;
	}

	public String getTaskName() {
		return taskName;
	}

	public boolean isForResource(String name) {
		return resourceName != null && resourceName.equals(name);
	}

	public int projectSpanInWeeks() {
		return hoursPerWeek.size();
	}

	public boolean isActiveInWeek(int weekIndex) {
		return hoursPerWeek.get(weekIndex) > 0;
	}

	public List<Integer> getHoursByWeek() {
		return hoursPerWeek;
	}

	public String getResourceName() {
		return resourceName;
	}

}
