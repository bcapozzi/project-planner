package com.mosaicatm.projectplanner;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

public class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor 
{
    private JComboBox editor;
    //private String [] values = {"Analyst", "Senior Analyst", "Senior Dev", "Dev"};

    public MyTableCellEditor(JComboBox resourceOptions)
    {
    	// Create a new Combobox with the array of values.
    	this.editor = resourceOptions;
    }
    
    @Override
    public Object getCellEditorValue() 
    {
    	return editor.getSelectedItem();
    }

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		
		// Set the model data of the table
	    if(isSelected)
	    {
		    editor.setSelectedItem(value);
		    TableModel model = table.getModel();
		    model.setValueAt(value, row, column);
	    }

	    return editor;
	}
}
