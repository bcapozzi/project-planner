package com.mosaicatm.projectplanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ProjectPlanner extends JFrame implements TableModelListener {

	private static final int DEFAULT_RESOURCE_DOLLARS_PER_HOUR = 100;
	//private JPanel projectPanel;
	private JTable projectTable;
	private JScrollPane scrollPane;
	private JButton addTaskButton;
	private ProjectTableModel projectTableModel;
	private JTextArea scheduleArea;
	private JComboBox<String> aggregateByOptions;
	private JComboBox<String> firstMonthStartsInWeek;
	private List<String> recentFiles = new ArrayList<>();
	private JMenu fileMenu;
	
	//private Map<Integer,String> resourceNames = new HashMap<>();
	//private Map<Integer,String> taskNames = new HashMap<>();
	//private Map<Integer, List<Integer>> hoursByWeek = new HashMap<>();
	private JComboBox<String> resourcesAvailable;
	private List<String> uniqueResourceNames = new ArrayList<>();
	private CostTableModel costTableModel;
	private JLabel totalCostLabel;
	
	private ProjectPlanner(Map<String,Integer> resourceRates) {
		super("Project Planner");

		uniqueResourceNames.add("Select...");

		for (String key: resourceRates.keySet()) {
			uniqueResourceNames.add(key);
		}	

		//projectPanel = new JPanel();
		recentFiles = loadRecentFiles();
		
		JMenuBar menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		JMenuItem loadMenu = new JMenuItem("Open...");
		
		loadMenu.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				
				JFileChooser chooser = new JFileChooser();
				
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					String filePath = chooser.getSelectedFile().getAbsolutePath();
					System.out.format("Selected to OPEN file: %s\n", filePath);
					try {
						Schedule schedule = ScheduleParser.parse(filePath);
						viewSchedule(schedule);
						addFileToRecentFileHistory(filePath);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		});
		
		
		JMenuItem saveMenu = new JMenuItem("Save As...");
		saveMenu.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				
				try {
				int result = chooser.showSaveDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					Schedule schedule = projectTableModel.toSchedule();
					writeScheduleToFile(schedule,chooser.getSelectedFile().getAbsolutePath());
				}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		fileMenu.add(loadMenu);
		fileMenu.add(saveMenu);
		fileMenu.addSeparator();
		
		for (String f: recentFiles) {
			addToRecentFileMenu(f);
		}
		
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.addSeparator();
		fileMenu.add(exit);
		
		menuBar.add(fileMenu);
	
		setJMenuBar(menuBar);
		
		aggregateByOptions = new JComboBox<String>(new String[]{"Week", "Month"});
		aggregateByOptions.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof JComboBox<?>) {
					
					if (resourcesAvailable.getSelectedIndex() == 0)
						return;
					
					String selectedResource = resourcesAvailable.getItemAt(resourcesAvailable.getSelectedIndex());
					String aggregateBy = getAggregateBy();
					int firstMonthStartsAtWeek = getFirstMonthStartsInWeek();
					generateScheduleForResource(selectedResource, aggregateBy, firstMonthStartsAtWeek);
				}
			}
		});
		firstMonthStartsInWeek = new JComboBox<String>(new String[]{"1","2","3","4"});
		firstMonthStartsInWeek.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
		
				if (e.getSource() instanceof JComboBox<?>) {
					
					if (resourcesAvailable.getSelectedIndex() == 0)
						return;
					
					String selectedResource = resourcesAvailable.getItemAt(resourcesAvailable.getSelectedIndex());
					String aggregateBy = getAggregateBy();
					int firstMonthStartsAtWeek = getFirstMonthStartsInWeek();
					generateScheduleForResource(selectedResource, aggregateBy, firstMonthStartsAtWeek);
				}
			}
		});
		
		addTaskButton = new JButton("Add Row");
		addTaskButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//projectTable.invalidate();
				projectTableModel.addRow();
				
			}
		});
		
		costTableModel = new CostTableModel();
		for (String resource: resourceRates.keySet()) {
//			if (resource.startsWith("Select"))
//				continue;
			Integer ratePerHr = resourceRates.get(resource);
			if (ratePerHr != null)
				costTableModel.addResourceCost(resource, ratePerHr.intValue());
			else
				costTableModel.addResourceCost(resource, DEFAULT_RESOURCE_DOLLARS_PER_HOUR);
		}
		
		JTable costTable = new JTable(costTableModel);
		costTable.setRowHeight(30);
		
		final JPopupMenu popupMenu = new JPopupMenu();
        final JMenuItem moveUpItem = new JMenuItem("Move Up");
        final JMenuItem moveDownItem = new JMenuItem("Move Down");
        final JMenuItem deleteItem = new JMenuItem("Delete");
        
        
		projectTableModel = new ProjectTableModel(1,24);
		projectTable = new JTable(projectTableModel);
		projectTable.setRowHeight(30);
		projectTableModel.addTableModelListener(this);
		projectTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				System.out.println("Selected row " + projectTable.getSelectedRow());
				if (projectTable.getSelectedRow() == 0)
				{
					moveUpItem.setEnabled(false);
					moveDownItem.setEnabled(true);
				}
				else if (projectTable.getSelectedRow() == projectTableModel.getRowCount()-1) {
					moveUpItem.setEnabled(true);
					moveDownItem.setEnabled(false);;
				}
				else {
					moveUpItem.setEnabled(true);
					moveDownItem.setEnabled(true);
				}
			}
		});
		
		moveUpItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Right-click performed on table and chose Move Up");
                int selectedRow = projectTable.getSelectedRow();
                projectTableModel.moveRowUp(selectedRow);
                //projectTableModel
            }
        });
        
        moveDownItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Right-click performed on table and chose Move Down");
                int selectedRow = projectTable.getSelectedRow();
                projectTableModel.moveRowDown(selectedRow);
            }
        });
        
        deleteItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = projectTable.getSelectedRow();
				projectTableModel.removeTaskAssignmentInRow(selectedRow);
				
			}
		});
        popupMenu.add(moveUpItem);
        popupMenu.add(moveDownItem);
        popupMenu.add(deleteItem);
        projectTable.setComponentPopupMenu(popupMenu);
		
		TableColumnModel columnModel = projectTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(400);
		columnModel.getColumn(1).setPreferredWidth(300);
		
		JComboBox<String> resourceOptions = new JComboBox<>(uniqueResourceNames.toArray(new String[0]));
		
		//
		//MyTableCellEditor resourceSelector = new MyTableCellEditor(resourceOptions);
		
		TableColumn resourceAssignedColumn = projectTable.getColumnModel().getColumn(1);
		resourceAssignedColumn.setCellEditor(new DefaultCellEditor(resourceOptions));
		
		resourceOptions.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Detected action.");
				// given selected row
				int row = projectTable.getSelectedRow();
				if (e.getSource() instanceof JComboBox<?>) {
					JComboBox<String> cb = (JComboBox<String>)e.getSource();
					String resource = (String)cb.getSelectedItem();
					System.out.println("updated resource selection for row: " + row + " to resource: " + resource);
					// given seleted item
					projectTableModel.updateResourceForTaskInRow(row, resource);
				}
			}
		});
//		columnModel.getColumn(1).setCellEditor(resourceSelector);
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		
		DefaultTableCellRenderer totalRenderer = new DefaultTableCellRenderer();
		totalRenderer.setHorizontalAlignment( JLabel.CENTER );
		totalRenderer.setBackground(Color.gray);
		
		for (int i=0; i<columnModel.getColumnCount()-1; i++) {
			projectTable.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
		}
		projectTable.getColumnModel().getColumn(columnModel.getColumnCount()-1).setCellRenderer( totalRenderer );
		
		add(addTaskButton);
		
		scrollPane = new JScrollPane(projectTable);
		
		scheduleArea = new JTextArea();
		scheduleArea.setBorder(new LineBorder(Color.BLACK));
		scheduleArea.setMinimumSize(new Dimension(1000, 100));
		scheduleArea.setPreferredSize(new Dimension(1000, 200));
		scheduleArea.setEditable(false);
		Font font = new Font("Courier New",Font.BOLD,12);
		scheduleArea.setFont(font);
		
		resourcesAvailable = new JComboBox<>(uniqueResourceNames.toArray(new String[0]));
		resourcesAvailable.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof JComboBox<?>) {
					JComboBox<String> source = (JComboBox<String>)e.getSource();
					String selectedResource = source.getItemAt(source.getSelectedIndex());
					System.out.println("Selected: " + selectedResource);
					
					String aggregateBy = getAggregateBy();
					int firstMonthStartsAtWeek = getFirstMonthStartsInWeek();
					generateScheduleForResource(selectedResource, aggregateBy, firstMonthStartsAtWeek);
				}
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addTaskButton);
		buttonPanel.add(new JLabel("Generate schedule for: "));
		buttonPanel.add(resourcesAvailable);
		buttonPanel.add(new JLabel("grouped by: " ));
		buttonPanel.add(aggregateByOptions);
		buttonPanel.add(new JLabel("with first month starting in week"));
		buttonPanel.add(firstMonthStartsInWeek);
		
		setLayout(new BorderLayout());
		
		add(buttonPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(scheduleArea, BorderLayout.SOUTH);
		
		JPanel costPanel = new JPanel();
		costPanel.setLayout(new BorderLayout());
		costPanel.add(new JLabel("Resource Costs"), BorderLayout.NORTH);
		costPanel.add(costTable, BorderLayout.CENTER);
		
		JPanel totalPanel = new JPanel();
		JButton updateTotals = new JButton("Update total");
		updateTotals.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				int costInCents = 
						projectTableModel.toSchedule().estimateCostGivenRates(costTableModel.getRateMapInCents());
				
				totalCostLabel.setText("$" + costInCents/100);
			}
		});
		totalCostLabel = new JLabel("$0");
		totalPanel.add(updateTotals);
		totalPanel.add(totalCostLabel);
		
		costPanel.add(totalPanel, BorderLayout.SOUTH);
		
		add(costPanel, BorderLayout.EAST);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	protected void writeScheduleToFile(Schedule schedule, String absolutePath) throws Exception {
		ScheduleWriter.toCSVFile(schedule, absolutePath);
		System.out.println("Wrote schedule to file: " + absolutePath);
	}

	private List<String> loadRecentFiles() {

		List<String> files = new ArrayList<>();

		try {
			File file = new File("recents.txt");
			if (!file.exists())
				file.createNewFile();
			
			BufferedReader reader = new BufferedReader(new FileReader("recents.txt"));
			String nextLine = null;
			while ((nextLine = reader.readLine()) != null) {
				files.add(nextLine);
			}
			reader.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return files;
	}

	protected void addFileToRecentFileHistory(String filePath) throws Exception {
		
		addToRecentFileMenu(filePath);
		recentFiles.add(0, filePath);
		if (recentFiles.size() >= 5) {
			recentFiles.remove(recentFiles.size()-1);
		}
		FileWriter writer = new FileWriter("recents.txt", false);
		for (String f: recentFiles) {
			writer.write(f + "\n");
		}
		writer.close();
	}

	private void addToRecentFileMenu(String filePath) {
		JMenuItem m = new JMenuItem(filePath);
		fileMenu.add(m);
		m.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				
				JMenuItem item = (JMenuItem)event.getSource();
				try {
					Schedule schedule = ScheduleParser.parse(item.getText());
					viewSchedule(schedule);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
	}

	protected int getFirstMonthStartsInWeek() {
		String value = (String)this.firstMonthStartsInWeek.getItemAt(firstMonthStartsInWeek.getSelectedIndex());
		int startWeek = Integer.parseInt(value);
		if (startWeek == 1)
			return Schedule.FIRST_WEEK_IN_MONTH;
		else if (startWeek == 2)
			return Schedule.SECOND_WEEK_IN_MONTH;
		else if (startWeek == 3) 
			return Schedule.THIRD_WEEK_IN_MONTH;
		else if (startWeek == 4)
			return Schedule.FOURTH_WEEK_IN_MONTH;
		
		throw new RuntimeException("Tilt!");
	}

	protected String getAggregateBy() {
		return (String)this.aggregateByOptions.getItemAt(aggregateByOptions.getSelectedIndex());
	}

	protected void viewSchedule(Schedule schedule) {
		projectTableModel.fromSchedule(schedule);
		scheduleArea.setText("");
	}

	protected void generateScheduleForResource(String selectedResource, String aggregateBy, int offsetWeeks) {
		
		Schedule schedule = projectTableModel.toSchedule();
		if (schedule.isEmpty())
			return;
		
		schedule = schedule.forResource(selectedResource);
		if (schedule.isEmpty())
			return;
		
		if (aggregateBy.equalsIgnoreCase("Month")) {
			scheduleArea.setText(schedule.displayByMonthStartingAtWeek(offsetWeeks));
		}
		else {
			scheduleArea.setText(schedule.displayByWeek());			
		}
		
	}
	public static ProjectPlanner createAndShowGui(Map<String, Integer> resourceRates) {
		ProjectPlanner planner = new ProjectPlanner(resourceRates);
		planner.setMinimumSize(new Dimension(1280,600));
		planner.setPreferredSize(new Dimension(1280,600));
		planner.pack();
		planner.setVisible(true);
		return planner;
	}

	public class ProjectTableModel extends AbstractTableModel implements TableModelListener {

		private int numWeeks;
		private int numRows;
		private int numCols;
		private List<TaskAssignment> tasks = new ArrayList<>();
		private Map<Integer,List<Integer>> hoursByWeek = new HashMap<>();

		public ProjectTableModel(int rows, int weeks) {
			this.numWeeks = weeks;
			this.numRows = rows;
			this.numCols = weeks + 2 + 1;
			tasks.add(createEmptyTask(weeks));
			
			List<Integer> hoursPerWeek = new ArrayList<>();
			for (int i=0; i<weeks; i++)
				hoursPerWeek.add(0);
			hoursByWeek.put(0, hoursPerWeek);
			//addTableModelListener(this);

		}
		
		private TaskAssignment createEmptyTask(int numWeeks) {
			TaskAssignment task = new TaskAssignment("Enter task name", "Select resource...");
			for (int i=0; i<numWeeks; i++)
				task.addHoursForWeek(0);
			return task;
		}

		public void removeTaskAssignmentInRow(int selectedRow) {
			
			//String taskToDelete = taskNames.get(selectedRow);
			System.out.println("Received message to delete task in row: " + selectedRow);
		
			tasks.remove(selectedRow);
			updateHoursByWeek();
			
			numRows = tasks.size();
			fireTableStructureChanged();
			fireTableDataChanged();
		}


		public void moveRowUp(int selectedRow) {
			
			// what this really means is that this row's data will swap with the one above it
			System.out.println("Received message to move row " + selectedRow + " up...");
			
			// basically this is a "swap" of 2 elements in the array
			TaskAssignment taskToMoveUp = tasks.get(selectedRow);
			TaskAssignment taskAbovePriorToMove = tasks.get(selectedRow-1);
			
			tasks.set(selectedRow-1, taskToMoveUp);
			tasks.set(selectedRow, taskAbovePriorToMove);
			
			updateHoursByWeek();
			
/*			// get current elements
			String taskPriorToSwap = taskNames.get(selectedRow);
			String taskAbovePriorToSwap = taskNames.get(selectedRow-1);
			taskNames.put(selectedRow, taskAbovePriorToSwap);
			taskNames.put(selectedRow-1, taskPriorToSwap);
			
			String resourcePriorToSwap = resourceNames.get(selectedRow);
			String resourceAbovePriorToSwap = resourceNames.get(selectedRow-1);
			resourceNames.put(selectedRow, resourceAbovePriorToSwap);
			resourceNames.put(selectedRow-1, resourcePriorToSwap);
			
			List<Integer> hoursPriorToSwap = hoursByWeek.get(selectedRow);
			List<Integer> hoursAbovePriorToSwap = hoursByWeek.get(selectedRow-1);
			hoursByWeek.put(selectedRow, hoursAbovePriorToSwap);
			hoursByWeek.put(selectedRow-1, hoursPriorToSwap);
	
			*/
			/*Map<Integer,String> previousTaskNames = new HashMap<>();
			for (Integer n: taskNames.keySet()) {
				
			}*/
			fireTableDataChanged();
		}
		
		private void updateHoursByWeek() {
			
			hoursByWeek.clear();
			for (int i=0; i<tasks.size(); i++) {
				hoursByWeek.put(i, tasks.get(i).getHoursByWeek());
			}
			
		}

		public void moveRowDown(int selectedRow) {
			
			System.out.println("Received message to move row " + selectedRow + " down...");
			// get current elements
			TaskAssignment taskToMoveDown = tasks.get(selectedRow);
			TaskAssignment taskBelowPriorToSwap = tasks.get(selectedRow+1);
			tasks.set(selectedRow, taskBelowPriorToSwap);
			tasks.set(selectedRow+1, taskToMoveDown);

			updateHoursByWeek();
			fireTableDataChanged();
		}
		public void fromSchedule(Schedule schedule) {
			
			this.numRows = schedule.numResourceAssignments();
			this.numCols = schedule.numWeeks() + 2 + 1;
			List<TaskAssignment> tasks = schedule.getTaskAssignments();
			
			Collections.sort(tasks, new TaskNameSorter());
			Collections.sort(tasks, new EarliestStartWeekSorter());
			
			this.tasks.clear();
			this.tasks.addAll(tasks);

			updateHoursByWeek();
			for (int i=0; i<tasks.size(); i++) {
				addResource(tasks.get(i).getResourceName());
			}
			
			fireTableStructureChanged();
			fireTableDataChanged();
		}
		public Schedule toSchedule() {
			Schedule schedule = new Schedule();
			for (int i=0; i<numRows; i++) {
				schedule.addTaskAssignment(tasks.get(i));
			}
			return schedule;
		}
		public void addRow() {
			//this.numRows++;
			tasks.add(createEmptyTask(numWeeks));
			numRows = tasks.size();
			
//			List<Integer> hoursPerWeek = new ArrayList<>();
//			for (int i=0; i<numCols-2; i++)
//				hoursPerWeek.add(0);
//			hoursByWeek.put(numRows-1, hoursPerWeek);
			System.out.println("Number of rows: " + numRows);
			fireTableStructureChanged();
			fireTableDataChanged();
			//revalidate();
		}
		@Override
		public int getRowCount() {
			return numRows;
		}

		@Override
		public int getColumnCount() {
			return numCols;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "Task Name";
			else if (columnIndex == 1)
				return "Resource Name";
			else if (columnIndex < numCols-1)
				return "W" + (columnIndex-2+1);
			else
				return "Total";
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0 || columnIndex == 1)
				return String.class;
			else
				return Integer.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == numCols-1)
				return false;
			return true;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			
			TaskAssignment task = tasks.get(rowIndex);
			
			if (columnIndex == 0) {
				String name = task.getTaskName();
				if (name == null)
					return "Add Task Name";
				else
					return name;
			}
			else if (columnIndex == 1) {
				return task.getResourceName();
			}
			else if (columnIndex == numCols-1) {
				return computeTotalForRow(rowIndex);
			}
			else {
				int hoursInWeek = task.getHoursPlannedForWeek(columnIndex-2);
				if (hoursInWeek > 0)
					return hoursInWeek;
					
				return null;
				
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			
			TaskAssignment task = tasks.get(rowIndex);
			
			//System.out.format("Calling setValueAt(%d, %d)\n", rowIndex, columnIndex);
			if (columnIndex == 0) {
				task.updateTaskName((String)aValue);
				System.out.println("Setting task name " + (String)aValue + " for row " + rowIndex);
				fireTableDataChanged();
			}
			else if (columnIndex == 1) {
				System.out.println("Updating resource assigned for row: " + rowIndex + " to: " + (String)aValue);
				task.updateResourceAssigned((String)aValue);
				fireTableDataChanged();
			}
			else {
				//List<Integer> hoursPerWeek = task.get
				/*if (hoursPerWeek == null) {
					hoursPerWeek = new ArrayList<>();
					for (int i=0; i<numCols-2; i++)
						hoursPerWeek.add(0);
					hoursByWeek.put(rowIndex, hoursPerWeek);
				}*/
				task.updateHoursForWeek(columnIndex-1, (Integer)aValue);
				System.out.println("Updating value for week: " + (columnIndex-2));
//				hoursPerWeek.set(columnIndex-2, (Integer) aValue);
				fireTableCellUpdated(rowIndex, columnIndex);
				fireTableCellUpdated(rowIndex, numCols-1);
			}
			
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			System.out.println("Table changed event");
		}
		
		private Integer computeTotalForRow(int rowIndex) {
			TaskAssignment task = tasks.get(rowIndex);
			List<Integer> hours = task.getHoursByWeek();
			int sum = 0;
			for (Integer h: hours)
				sum += h;
			
			System.out.println("Computing total for row: " + rowIndex + " = " + sum);

			return sum;
		}

		public void updateResourceForTaskInRow(int row, String resource) {
			TaskAssignment task = tasks.get(row);
			task.updateResourceAssigned(resource);
		}
		
	}

	
	public void addResource(String resourceName) {
		Set<String> resources = new HashSet<>();
		for (int i=0; i<resourcesAvailable.getItemCount(); i++) {
			String item = (String)resourcesAvailable.getItemAt(i);
			resources.add(item);
		}
		if (!resources.contains(resourceName))
			resourcesAvailable.addItem(resourceName);
		
		resources.clear();
		for (int i=0; i<uniqueResourceNames.size(); i++) {
			String next = uniqueResourceNames.get(i);
			resources.add(next);
		}
		if (!resources.contains(resourceName)) {
			uniqueResourceNames.add(resourceName);
		}

		if (!costTableModel.includesResource(resourceName)) {
			costTableModel.addResourceCost(resourceName, DEFAULT_RESOURCE_DOLLARS_PER_HOUR);
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		System.out.println("Received table changed event");
		TableColumnModel columnModel = projectTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(400);
		columnModel.getColumn(1).setPreferredWidth(300);
		
		JComboBox<String> resourceOptions = new JComboBox<String>(uniqueResourceNames.toArray(new String[0]));
		TableColumn resourceAssignedColumn = projectTable.getColumnModel().getColumn(1);
		resourceAssignedColumn.setCellEditor(new DefaultCellEditor(resourceOptions));
		
		resourceOptions.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Detected action.");
				// given selected row
				int row = projectTable.getSelectedRow();
				if (row < 0)
				{
					System.out.println("No row selected - not updating resource");
					return;
				}
				
				JComboBox cb = (JComboBox)e.getSource();
				int index = cb.getSelectedIndex();
				String resource = (String)cb.getSelectedItem();
				System.out.println("updated resource selection for row: " + row + " to resource: " + resource);
				// given seleted item
				projectTableModel.updateResourceForTaskInRow(row, resource);
				
				projectTableModel.fireTableDataChanged();
			}
		});
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		
		DefaultTableCellRenderer totalRenderer = new DefaultTableCellRenderer();
		totalRenderer.setHorizontalAlignment( JLabel.CENTER );
		totalRenderer.setBackground(Color.gray);
		
		for (int i=0; i<columnModel.getColumnCount()-1; i++) {
			projectTable.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
		}
		projectTable.getColumnModel().getColumn(columnModel.getColumnCount()-1).setCellRenderer( totalRenderer );
		
		
		int costInCents = 
				projectTableModel.toSchedule().estimateCostGivenRates(costTableModel.getRateMapInCents());
		
		totalCostLabel.setText("$" + costInCents/100);
	}
	
	public static void main(String[] args) throws Exception {
		
		Map<String,Integer> resourceRates = ResourceRateParser.parseResourceRates("src/main/resources/resource_costs.csv");
		ProjectPlanner planner = ProjectPlanner.createAndShowGui(resourceRates);
		
	}
}
