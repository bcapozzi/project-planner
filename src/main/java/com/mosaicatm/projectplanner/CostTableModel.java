package com.mosaicatm.projectplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class CostTableModel extends AbstractTableModel {

	private List<String> resources = new ArrayList<>();
	private Map<String,Integer> resourceCosts = new HashMap<String, Integer>();
	
	public void addResourceCost(String resource, int dollarsPerHour) {
		resourceCosts.put(resource, dollarsPerHour);
		resources.add(resource);
		fireTableStructureChanged();
		fireTableDataChanged();
	}
	
	public String getColumnName(int column)
	{
		if (column == 0)
			return "Resource";
		else
			return "$/hr";
	}
	public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
        	return String.class;
        else if (columnIndex == 1)
        	return Integer.class;
        
        return Object.class;
    }
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1)
        	return true;
        return false;
    }

	@Override
	public int getRowCount() {
		return resourceCosts.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0)
			return resources.get(rowIndex);
		else
			return resourceCosts.get(resources.get(rowIndex));
	}
	
	public void setValueAt(Object o, int rowIndex, int colIndex) {
		if (colIndex == 1) {
			String resource = resources.get(rowIndex);
			resourceCosts.put(resource, (Integer)o);
		}
	}

	public Map<String, Integer> getRateMapInCents() {
		Map<String, Integer> result = new HashMap<>();
		for (String key: resourceCosts.keySet()) {
			result.put(key, resourceCosts.get(key)*100);
		}
		return result;
	}

	public boolean includesResource(String resourceName) {
		return resourceCosts.containsKey(resourceName);
	}

}
