package com.mosaicatm.projectplanner;

import java.util.Comparator;

public class EarliestStartWeekSorter implements
		Comparator<TaskAssignment> {

	@Override
	public int compare(TaskAssignment o1, TaskAssignment o2) {
		
		return Integer.compare(startWeekFor(o1), startWeekFor(o2));
	}

	private int startWeekFor(TaskAssignment task) {
		
		int startWeek = 0;
		for (int hours: task.getHoursByWeek()) {
			if (hours > 0)
				return startWeek;
			startWeek++;
		}
		return 9999999;
	}

}
