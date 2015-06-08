package com.mosaicatm.projectplanner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class Schedule {

	public static final int FIRST_WEEK_IN_MONTH = 0;
	public static final int SECOND_WEEK_IN_MONTH = 1;
	public static final int THIRD_WEEK_IN_MONTH = 2;
	public static final int FOURTH_WEEK_IN_MONTH = 3;
	
	private List<TaskAssignment> tasks = new ArrayList<>();
	public int totalHours() {
		int total = 0;
		for (TaskAssignment task: tasks) {
			total += task.totalHours();
		}
		return total;
	}

	public void addTaskAssignment(TaskAssignment assignment) {
		tasks.add(assignment);
	}

	public int numTasks() {
		
		Set<String> unique = new HashSet<>();
		for (TaskAssignment t: tasks) {
			unique.add(t.getTaskName());
		}
		return unique.size();
	}

	public Schedule forResource(String resourceName) {
		// go thru all tasks
		// find all that match resource name
		// add work allocation
		Schedule schedule = new Schedule();
		for (TaskAssignment t: tasks) {
			if (t.isForResource(resourceName)) {
				schedule.addTaskAssignment(t);
			}
		}
		return schedule;
	}

	public String displayByMonthStartingAtWeek(int monthStartsAtWeek) {
		
		// first (monthStartsAtWeek - 1) weeks are included in the "first" month totals
		// next 4-week periods are included in each successive "month"
		// remaining <4 week periods are included in the final "month"
		
		// find unique task names
		List<String> uniqueTaskNames = findUniqueTaskNames();

		
		Map<String, List<Integer>> taskHoursPerWeek = new HashMap<>();
		for (String name: uniqueTaskNames) {
			List<Integer> hoursByWeek = getHoursByWeekForTask(name);
			taskHoursPerWeek.put(name, hoursByWeek);
		}

		// now group by "month"
		Map<String,List<Integer>> taskHoursByMonth = new HashMap<>();
		for (String name: uniqueTaskNames)
			taskHoursByMonth.put(name, new ArrayList<Integer>());
		
		// first N-1 go in first month
		Map<Integer, Integer> numWeeksInMonth = new HashMap<>();
		for (String name: uniqueTaskNames) {

			List<Integer> hoursPerWeek = taskHoursPerWeek.get(name);
			List<Integer> hoursPerMonth = taskHoursByMonth.get(name);
			
			int totalInMonth = 0;
			int numWeeks = 0;
			int currentMonth = 0;
			if (monthStartsAtWeek > 0) {
				for (int i=0; i<monthStartsAtWeek; i++) {
					totalInMonth += hoursPerWeek.get(i);
					numWeeks += 1;
				}
				numWeeksInMonth.put(currentMonth, numWeeks);
				hoursPerMonth.add(totalInMonth);
			}
			
			// now go 4 weeks at a time
			int currentWeek = monthStartsAtWeek;
			while (hoursPerWeek.size()-currentWeek >= 4) {
				
				totalInMonth = 0;
				numWeeks = 0;
				for (int i=currentWeek; i<currentWeek+4; i++) {
					totalInMonth += hoursPerWeek.get(i);
					numWeeks++;
				}
				hoursPerMonth.add(totalInMonth);
				currentWeek += 4;
				currentMonth = numWeeksInMonth.size();
				numWeeksInMonth.put(currentMonth, numWeeks);
			}
			
			// now get number of weeks "left"
			totalInMonth = 0;
			numWeeks = 0;
			while (currentWeek < hoursPerWeek.size()) {
				totalInMonth += hoursPerWeek.get(currentWeek);
				currentWeek += 1;
				numWeeks++;
			}
			currentMonth = numWeeksInMonth.size();
			hoursPerMonth.add(totalInMonth);
			numWeeksInMonth.put(currentMonth, numWeeks);
		}
		
		// now display the schedule
		StringBuffer buffer = new StringBuffer();
		
		// put number of weeks in each modeled month
		buffer.append(String.format("%50s", ""));
		buffer.append("\t");
		int numMonthsToShow = getNumberOfMonthsToShow(taskHoursByMonth);
		for (int i=0; i<numMonthsToShow; i++) {
			int numWeeksModeled = numWeeksInMonth.get(i);
			buffer.append(String.format("%3s ", "(" + numWeeksModeled + ")"));
		}
		buffer.append("\r\n");
		
		buffer.append(String.format("%50s", ""));
		buffer.append("\t");
		for (int i=0; i<getNumberOfMonthsToShow(taskHoursByMonth); i++) {
			buffer.append(String.format("%3s ", "M" + (i+1)));
		}
		buffer.append("\r\n");
		
		for (String name: uniqueTaskNames) {
			buffer.append(String.format("%50s", name));
			buffer.append("\t");
			List<Integer> hours = taskHoursByMonth.get(name);
			for (Integer h: hours) {
				buffer.append(String.format("%3d ", h));
			}
			buffer.append(String.format("%4s\r\n", sum(hours)));
		}
		
		// write last row corresponding to sum for month
		List<Integer> byMonthTotals = computeTotalByMonth(taskHoursByMonth);
		buffer.append(String.format("%50s", "Totals"));
		buffer.append("\t");
		for (Integer h: byMonthTotals) {
			buffer.append(String.format("%3s ", h.intValue()));
		}
		buffer.append(String.format("%4s\r\n", sum(byMonthTotals)));
		
		return buffer.toString();
	}


	private List<Integer> computeTotalByMonth(
			Map<String, List<Integer>> taskHoursByMonth) {
		
		List<Integer> totals = new ArrayList<>();
		for (String key: taskHoursByMonth.keySet()) {
			updateTotals(totals, taskHoursByMonth.get(key));
		}
		return totals;
	}

	private void updateTotals(List<Integer> totals, List<Integer> taskHours) {
		
		if (totals.isEmpty()) {
			for (Integer h: taskHours)
				totals.add(h);
		}
		else {
			assert totals.size() == taskHours.size();
			for (int i=0; i< totals.size(); i++) {
				int previous = totals.get(i);
				int updated = previous + taskHours.get(i);
				totals.set(i, updated);
			}
		}
		
	}

	private int getNumberOfMonthsToShow(
			Map<String, List<Integer>> taskHoursByMonth) {
		
		for (String key: taskHoursByMonth.keySet()) {
			return taskHoursByMonth.get(key).size();
		}
		return -1;
	}

	public String displayByWeek() {
		
		// find unique task names
		List<String> uniqueTaskNames = findUniqueTaskNames();
		
		StringBuffer buffer = new StringBuffer();
		List<Integer> totalByWeek = null;
		
		for (String name: uniqueTaskNames) {
			buffer.append(String.format("%50s", name));
			buffer.append("\t");
			List<Integer> hoursByWeek = getHoursByWeekForTask(name);
			if (totalByWeek == null) {
				totalByWeek = new ArrayList<>();
				for (Integer h: hoursByWeek)
					totalByWeek.add(0);
			}
			for (Integer h: hoursByWeek) {
				buffer.append(String.format("%2s ", h.intValue()));
			}
			buffer.append(String.format("%4s\r\n", sum(hoursByWeek)));
			updateTotalPerWeek(totalByWeek, hoursByWeek);
		}
		
		if (totalByWeek == null)
			return "EMPTY SCHEDULE";
		
		// write one last row corresponding to hour total per week
		buffer.append(String.format("%50s", "Totals"));
		buffer.append("\t");
		for (Integer h: totalByWeek) {
			buffer.append(String.format("%2s ", h.intValue()));
		}
		buffer.append(String.format("%4s\r\n", sum(totalByWeek)));
		
		return buffer.toString();
	}

	private void updateTotalPerWeek(List<Integer> totalByWeek,
			List<Integer> hoursByWeek) {
		
		assert totalByWeek.size() == hoursByWeek.size();
		for (int i=0; i<hoursByWeek.size(); i++) {
			int previous = totalByWeek.get(i);
			int updated = previous + hoursByWeek.get(i);
			totalByWeek.set(i, updated);
		}
	}

	private int sum(List<Integer> values) {
		int sum = 0;
		for (Integer v: values)
			sum += v.intValue();
		return sum;
	}

	private List<Integer> getHoursByWeekForTask(String name) {
		for (TaskAssignment t: tasks) {
			if (t.getTaskName().equals(name))
				return t.getHoursByWeek();
		}
		return Collections.emptyList();
	}

	private List<String> findUniqueTaskNames() {
		List<String> unique = new ArrayList<>();
		for (TaskAssignment t: tasks) {
			if (!unique.contains(t.getTaskName()))
				unique.add(t.getTaskName());
		}
		return new ArrayList<>(unique);
	}

	private List<TaskAssignment> findTasksInWeek(int weekIndex) {
		List<TaskAssignment> tasksInWeek = new ArrayList<>();
		for (TaskAssignment t: tasks) {
			if (t.isActiveInWeek(weekIndex)) {
				tasksInWeek.add(t);
			}
		}
		return tasksInWeek;
	}

	
	public int getHoursBetweenWeeks(int startWeek, int endWeek) {
		List<Integer> hoursByWeek = new ArrayList<>();
		for (int i=startWeek; i<endWeek; i++)
			hoursByWeek.add(0);
		
		for (TaskAssignment t: tasks) {
			List<Integer> taskHoursByWeek = t.getHoursByWeek();
			updateTotalPerWeek(hoursByWeek, taskHoursByWeek.subList(startWeek, endWeek));
		}
		
		int sum = 0;
		for (Integer h: hoursByWeek)
			sum += h;
		return sum;
	}

	public List<String> getUniqueResourceNames() {
		
		Set<String> unique = new HashSet<>();
		for (TaskAssignment t: tasks) {
			unique.add(t.getResourceName());
		}
		return new ArrayList<String>(unique);
	}

	public int estimateCostGivenRates(Map<String, Integer> rateMapInCents) {
		
		int totalCents = 0;
		for (TaskAssignment t: tasks) {
			
			totalCents += t.totalHours()*rateMapInCents.get(t.getResourceName());
		}
		return totalCents;
	}

	public int numResourceAssignments() {
		return tasks.size();
	}

	public int numWeeks() {
		return tasks.get(0).getHoursByWeek().size();
	}

	public List<TaskAssignment> getTaskAssignments() {
		return tasks;
	}

	public boolean isEmpty() {
		return tasks.isEmpty();
	}



}
