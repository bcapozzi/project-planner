package com.mosaicatm.projectplanner;

import java.util.Comparator;

public class TaskNameSorter implements Comparator<TaskAssignment> {

	@Override
	public int compare(TaskAssignment o1, TaskAssignment o2) {
		return o1.getTaskName().compareTo(o2.getTaskName());
	}

}
