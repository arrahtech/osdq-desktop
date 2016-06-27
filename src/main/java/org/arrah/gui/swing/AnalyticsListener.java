package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2012      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for calling flows 
 * from Analytics  menuItems  from
 * file displayed.
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;

import org.arrah.framework.util.ValueSorter;

public class AnalyticsListener implements ActionListener, ItemListener {
	private ReportTable _rt;
	private int _rowC = 0;
	private int _colC = 0;
	private String[] col_n;
	private Hashtable<String, Double> _map;
	private Vector<Integer> _reportColV;
	private Vector<Integer> _reportFieldV;
	private JComboBox<String> comboX, comboY, comboT;
	private JComboBox<String> comboAggr;
	private JComboBox<String>[] comboCol, comboField;
	private JCheckBox[] selOption;
	private JRadioButton asc, desc, dsort;
	private JDialog jd;
	private String _title = "Chart";
	private boolean cancel_clicked = true;
	private int _chartType = -1; // No chart
	private JTextField tf;
	private boolean isDate = false;

	private static Calendar cal = Calendar.getInstance();

	static private int BARCHART = 1;
	static private int PIECHART = 2;
	static private int HBARCHART = 3;
	static private int LINECHART = 4;
	static private int REPORT = 5;

	private static String OTHER = "UNDEFINED"; // Null or Undefined Key

	public AnalyticsListener(ReportTable rt) {
		_rt = rt;
		_rowC = rt.table.getRowCount();
		_colC = rt.table.getColumnCount();
		col_n = new String[_colC];
	}; // Constructor

	public AnalyticsListener(ReportTable rt, int chartType) {
		_chartType = chartType;
		_rt = rt;
		_rowC = rt.table.getRowCount();
		_colC = rt.table.getColumnCount();
		col_n = new String[_colC];
		createAnalytics(_chartType);
	}; // Constructor

	private void createDialog(int chartType) {
		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		JPanel jp = new JPanel();
		SpringLayout layout = new SpringLayout();
		jp.setLayout(layout);

		JLabel la = new JLabel("Aggregator for Measure");
		comboAggr = new JComboBox<String>(new String[] {"Sum","Absolute Sum","Avg","Count","Min","Max"});
		jp.add(la);
		jp.add(comboAggr);
		
		JLabel lm = new JLabel("Choose Measure (Y-Axis)");
		comboY = new JComboBox<String>(col_n);
		jp.add(lm);
		jp.add(comboY);
		JLabel ld = new JLabel("Choose Dimension (X-Axis)");
		comboX = new JComboBox<String>(col_n);
		comboX.addItemListener(this);
		jp.add(ld);
		jp.add(comboX);

		JLabel lt = new JLabel("Title of Chart:");
		tf = new JTextField("Chart Title here");
		tf.setColumns(20);
		tf.selectAll();
		jp.add(lt);
		jp.add(tf);

		JLabel aL = new JLabel("Time Dimension");
		jp.add(aL);
		comboT = new JComboBox<String>(new String[] { "Year", "Quarter-Year",
				"Quarter", "Month-Year", "Month", "Day of Week", "None" });
		Class<?> cclass = _rt.table.getColumnClass(0);
		if (cclass.getName().toUpperCase().contains("DATE"))
			comboT.setEnabled(true);
		else
			comboT.setEnabled(false);
		jp.add(comboT);

		JLabel aS = new JLabel("Sort By");
		jp.add(aS);
		ButtonGroup bg = new ButtonGroup();
		asc = new JRadioButton("Ascending Values");
		desc = new JRadioButton("Descending Values");
		dsort = new JRadioButton("Dimension Values");
		dsort.setSelected(true);
		bg.add(asc);bg.add(desc);bg.add(dsort);
		jp.add(asc);jp.add(desc);jp.add(dsort);
		
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		jp.add(ok);

		JButton cancel = new JButton("Cancel");
		cancel.addKeyListener(new KeyBoardListener());
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		jp.add(cancel);

		layout.putConstraint(SpringLayout.WEST, lt, 5, SpringLayout.WEST, jp);
		layout.putConstraint(SpringLayout.NORTH, lt, 5, SpringLayout.NORTH, jp);
		layout.putConstraint(SpringLayout.WEST, tf, 0, SpringLayout.WEST, lt);
		layout.putConstraint(SpringLayout.NORTH, tf, 2, SpringLayout.SOUTH, lt);
		layout.putConstraint(SpringLayout.WEST, lm, 0, SpringLayout.WEST, lt);
		layout.putConstraint(SpringLayout.NORTH, lm, 10, SpringLayout.SOUTH, tf);
		layout.putConstraint(SpringLayout.WEST, comboY, 0, SpringLayout.WEST,
				lm);
		layout.putConstraint(SpringLayout.NORTH, comboY, 2, SpringLayout.SOUTH,
				lm);
		layout.putConstraint(SpringLayout.WEST, ld, 50, SpringLayout.EAST,
				comboY);
		layout.putConstraint(SpringLayout.NORTH, ld, 0, SpringLayout.NORTH, lm);
		layout.putConstraint(SpringLayout.WEST, comboX, 0, SpringLayout.WEST,ld);
		layout.putConstraint(SpringLayout.NORTH, comboX, 2, SpringLayout.SOUTH,ld);
		layout.putConstraint(SpringLayout.WEST, aL, 50, SpringLayout.EAST, comboX);
		layout.putConstraint(SpringLayout.NORTH, aL, 0, SpringLayout.NORTH,lm);
		
		layout.putConstraint(SpringLayout.WEST, comboT, 0, SpringLayout.WEST,aL);
		layout.putConstraint(SpringLayout.NORTH, comboT, 0,SpringLayout.NORTH, comboX);
		
		layout.putConstraint(SpringLayout.WEST, la, 10, SpringLayout.WEST,jp);
		layout.putConstraint(SpringLayout.NORTH, la, 25,SpringLayout.SOUTH, comboT);
		layout.putConstraint(SpringLayout.WEST, comboAggr, 10, SpringLayout.WEST,jp);
		layout.putConstraint(SpringLayout.NORTH, comboAggr, 10,SpringLayout.SOUTH, la);
		layout.putConstraint(SpringLayout.WEST, aS, 20, SpringLayout.EAST,la);
		layout.putConstraint(SpringLayout.NORTH, aS, 0,SpringLayout.NORTH, la);
		
		layout.putConstraint(SpringLayout.WEST, asc, 0, SpringLayout.WEST,aS);
		layout.putConstraint(SpringLayout.NORTH, asc, 2,SpringLayout.SOUTH, aS);
		layout.putConstraint(SpringLayout.WEST, desc, 0, SpringLayout.WEST,asc);
		layout.putConstraint(SpringLayout.NORTH, desc, 0,SpringLayout.SOUTH, asc);
		layout.putConstraint(SpringLayout.WEST, dsort, 0, SpringLayout.WEST,asc);
		layout.putConstraint(SpringLayout.NORTH, dsort, 0,SpringLayout.SOUTH, desc);


		layout.putConstraint(SpringLayout.WEST, ok, 200, SpringLayout.WEST, jp);
		layout.putConstraint(SpringLayout.SOUTH, ok, -50, SpringLayout.SOUTH,jp);
		layout.putConstraint(SpringLayout.WEST, cancel, 5, SpringLayout.EAST,ok);
		layout.putConstraint(SpringLayout.NORTH, cancel, 0, SpringLayout.NORTH,ok);

		jp.setPreferredSize(new Dimension(500, 400));
		jd = new JDialog();
		jd.setTitle("Chart Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}

	private void createAnalytics(int analType) {
		if (_chartType == BARCHART) {
			createDialog(BARCHART);
			if (cancel_clicked)
				return;
			showBarChart();
			return;
		} else if (_chartType == PIECHART) {
			createDialog(PIECHART);
			if (cancel_clicked)
				return;
			showPieChart();
			return;
		} else if (_chartType == HBARCHART) {
			createDialog(HBARCHART);
			if (cancel_clicked)
				return;
			showHBarChart();
			return;
		} else if (_chartType == LINECHART) {
			createDialog(LINECHART);
			if (cancel_clicked)
				return;
			showLineChart();
			return;
		} else if (_chartType == REPORT) {
			createReportDialog(REPORT);
			if (cancel_clicked)
				return;
			showReport();
			return;
		}

	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("cancel")) {
			jd.dispose();
			cancel_clicked = true;
			return;
		} else if (action.equals("ok")) {
			_title = tf.getText();
			jd.dispose();
			cancel_clicked = false;
			_map = new Hashtable<String, Double>();

			int xs = comboX.getSelectedIndex();
			int ys = comboY.getSelectedIndex();
			if (_title == null)
				_title = comboY.getSelectedItem().toString() + " By "
						+ comboX.getSelectedItem().toString();
			Class<?> cclass = _rt.table.getColumnClass(ys);
			int aindex = comboAggr.getSelectedIndex();
			for (int i = 0; i < _rowC; i++) {
				String key;
				Object obj = _rt.table.getValueAt(i, xs);
				if (obj == null)
					key = OTHER;
				else
					key = obj.toString();

				if (obj instanceof Date) {
					isDate = true;
					key = timeKey((Date) obj, comboT.getSelectedIndex());
				}

				
				Object value = _rt.table.getValueAt(i, ys);
				Double oldV = _map.get(key);
				if (oldV == null) {
					if (cclass.getName().toUpperCase().contains("DOUBLE"))
						_map.put(key, (Double) value);
					else
						_map.put(key, 1.0D); // if not number take count
				} else {
					if (cclass.getName().toUpperCase().contains("DOUBLE")) {
						switch (aindex) {
						case 0: //Sum
							_map.put(key, (Double) value + oldV);
							break;
						case 1: // Absolute Sum
							_map.put(key, Math.abs((Double) value + oldV));
							break;
						case 2: // Weighted Avg
							Double newV = (((Double)value) + _map.size()*oldV)/(_map.size()+1);
							_map.put(key,newV);
							break;
						case 3: // count
							_map.put(key, (oldV + 1)); 
							break;
						case 4: //Min
							if ((Double) value < oldV )
							_map.put(key, (Double)value); 
							break;
						case 5: //Max
							if ((Double) value >  oldV )
							_map.put(key, (Double)value); 
							break;
						default:
						}
					}
					
					else
						_map.put(key, (oldV + 1)); // if not number take count
				}
			} // End of Row iteration
		}  else if (action.equals("reportok")) {

			// Loop and check input in hash function
			_reportColV = new Vector<Integer>();
			_reportFieldV = new Vector<Integer>();
			for (int i=0; i < _colC; i++) {
				if (selOption[i].isSelected() == true ) {
					_reportColV.add(comboCol[i].getSelectedIndex());
					_reportFieldV.add(comboField[i].getSelectedIndex());
				}
			}
			if (_reportColV.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"Empty Selection", "Selection Error",
					JOptionPane.ERROR_MESSAGE);
			
				return;
			}
			
			jd.dispose();
			cancel_clicked = false;
			
		}
	} // End of Action Performed

	private void showBarChart() {
		PlotterPanel barChart = new PlotterPanel();
		barChart.setZoomFactor(1.0D);
		barChart.setInit();
		Object[] key_s = sortKey(_map);
		int s = _map.size();
		Object[] val = new Object[s];
		Object[] lab = new Object[s];
		for (int i = 0; i < s; i++) {
			lab[i] = mapKey(key_s[i]);
			val[i] = _map.get(key_s[i]);
		}

		barChart.setXLabel(lab);
		barChart.setxValues(val);
		barChart.setColorIndex(3);
		barChart.setTitle(_title);
		barChart.drawBarChart();
		barChart.setPreferredSize(new Dimension(s * 50 + 50, 600));

		JScrollPane pane = new JScrollPane(barChart);
		pane.setPreferredSize(new Dimension(600, 620));

		jd = new JDialog();
		jd.setTitle("Bar Chart");
		jd.setLocation(150, 100);
		jd.getContentPane().add(pane);
		jd.pack();
		jd.setVisible(true);
	}

	private void showLineChart() {
		LinelPlotter lineChart = new LinelPlotter();
		lineChart.setZoomFactor(1.0D);
		lineChart.setInit();
		Object[] key_s = sortKey(_map);
		int s = _map.size();
		Object[] val = new Object[s];
		Object[] lab = new Object[s];
		for (int i = 0; i < s; i++) {
			lab[i] = mapKey(key_s[i]);
			val[i] = _map.get(key_s[i]);
		}

		lineChart.setXLabel(lab);
		lineChart.setxValues(val);
		lineChart.setColorIndex(3);
		lineChart.setTitle(_title);
		lineChart.drawLineChart();
		lineChart.setPreferredSize(new Dimension(s * 50 + 50, 600));

		JScrollPane pane = new JScrollPane(lineChart);
		pane.setPreferredSize(new Dimension(600, 620));

		jd = new JDialog();
		jd.setTitle("Line Chart");
		jd.setLocation(150, 100);
		jd.getContentPane().add(pane);
		jd.pack();
		jd.setVisible(true);
	}

	private void showHBarChart() {
		HorizontalPlotter hbarChart = new HorizontalPlotter();
		hbarChart.setZoomFactor(1.0D);
		hbarChart.setInit();
		Object[] key_s = sortKey(_map);
		int s = _map.size();
		Object[] val = new Object[s];
		Object[] lab = new Object[s];
		for (int i = 0; i < s; i++) {
			lab[i] = mapKey(key_s[i]);
			val[i] = _map.get(key_s[i]);
		}
		hbarChart.setXLabel(lab);
		hbarChart.setxValues(val);
		hbarChart.setColorIndex(3);
		hbarChart.setTitle(_title);
		hbarChart.drawBarChart();
		hbarChart.setPreferredSize(new Dimension(600, s * 20 + 50));

		JScrollPane pane = new JScrollPane(hbarChart);
		pane.setPreferredSize(new Dimension(600, 620));

		jd = new JDialog();
		jd.setTitle("Horizontal Bar Chart");
		jd.setLocation(150, 100);
		jd.getContentPane().add(pane);
		jd.pack();
		jd.setVisible(true);
	}

	private void showPieChart() {
		PiePanel pieChart = new PiePanel();
		Object[] key_s = sortKey(_map);
		int s = _map.size();
		Object[] val = new Object[s];
		Object[] lab = new Object[s];
		for (int i = 0; i < s; i++) {
			lab[i] = mapKey(key_s[i]);
			val[i] = _map.get(key_s[i]);
		}
		pieChart.setLabel(lab);
		pieChart.setValue(val);
		pieChart.setTitle(_title);
		pieChart.drawPieChart();

		JScrollPane pane = new JScrollPane(pieChart);
		pane.setPreferredSize(new Dimension(620, 600));

		jd = new JDialog();
		jd.setTitle("Pie Chart");
		jd.setLocation(150, 100);
		jd.getContentPane().add(pane);
		jd.pack();
		jd.setVisible(true);
	}

	public static String timeKey(Date date, int timeH) {
		if (date == null || (date instanceof Date) == false)
			return null;

		cal.setTime(date);
		cal.setLenient(true);
		int yr = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_WEEK);

		switch (timeH) {
		case 0:
			return Integer.toString(yr);
		case 1:
			return Integer.toString(yr) + getQuarter(month);
		case 2:
			return getQuarter(month);
		case 3:
			return Integer.toString(yr) + getMonthCode(month);
		case 4:
			return getMonthCode(month);
		case 5:
			return Integer.toString(day);
		default:
			return date.toString();
		}
	}

	public static String getMonthCode(int month) { // Good for Sorting
		switch (month) {
		case Calendar.JANUARY:
			return "A";
		case Calendar.FEBRUARY:
			return "B";
		case Calendar.MARCH:
			return "C";
		case Calendar.APRIL:
			return "D";
		case Calendar.MAY:
			return "E";
		case Calendar.JUNE:
			return "F";
		case Calendar.JULY:
			return "G";
		case Calendar.AUGUST:
			return "H";
		case Calendar.SEPTEMBER:
			return "I";
		case Calendar.OCTOBER:
			return "J";
		case Calendar.NOVEMBER:
			return "K";
		case Calendar.DECEMBER:
			return "L";
		case Calendar.UNDECIMBER:
		default:
			return OTHER;
		}
	}

	public static String getMonthName(String code) { // Good for Sorting
		if (code.equals("A"))
			return "Jan";
		if (code.equals("B"))
			return "Feb";
		if (code.equals("C"))
			return "Mar";
		if (code.equals("D"))
			return "Apr";
		if (code.equals("E"))
			return "May";
		if (code.equals("F"))
			return "Jun";
		if (code.equals("G"))
			return "Jul";
		if (code.equals("H"))
			return "Aug";
		if (code.equals("I"))
			return "Sep";
		if (code.equals("J"))
			return "Oct";
		if (code.equals("K"))
			return "Nov";
		if (code.equals("L"))
			return "Dec";
		return OTHER;
	}

	public static String getDayName(String code) { // Good for Sorting
		int day = Integer.parseInt(code);
		switch (day) {
		case Calendar.SUNDAY:
			return "Sun";
		case Calendar.MONDAY:
			return "Mon";
		case Calendar.TUESDAY:
			return "Tue";
		case Calendar.WEDNESDAY:
			return "Wed";
		case Calendar.THURSDAY:
			return "Thu";
		case Calendar.FRIDAY:
			return "Fri";
		case Calendar.SATURDAY:
			return "Sat";
		default:
			return OTHER;
		}
	}

	public static String getQuarter(int month) {
		switch (month) {
		case Calendar.JANUARY:
		case Calendar.FEBRUARY:
		case Calendar.MARCH:
			return "Q1";
		case Calendar.APRIL:
		case Calendar.MAY:
		case Calendar.JUNE:
			return "Q2";
		case Calendar.JULY:
		case Calendar.AUGUST:
		case Calendar.SEPTEMBER:
			return "Q3";
		case Calendar.OCTOBER:
		case Calendar.NOVEMBER:
		case Calendar.DECEMBER:
			return "Q4";
		case Calendar.UNDECIMBER:
		default:
			return OTHER;
		}

	}

	private Object[] sortKey(Hashtable<String, Double> map) {
		if (asc.isSelected() == true || desc.isSelected() == true ) {
			return sortOnValue(map);
		}
		Object[] obj = null;
		if (map.containsKey(OTHER) == false) {
			obj = map.keySet().toArray();
			Arrays.sort(obj);
			return obj;
		} else {
			Object[] key_s = map.keySet().toArray();
			Arrays.sort(key_s);
			obj = new Object[key_s.length];
			int j = 0; // New Object Index
			for (int i = 0; i < key_s.length; i++) {
				if (key_s[i].toString().compareToIgnoreCase(OTHER) == 0) {
					obj[key_s.length - 1] = OTHER;
					j--;
				} else
					obj[j] = key_s[i];
				j++;
			}
			return obj;
		}
	}
	
	private Object[] sortOnValue(Hashtable<String, Double> map) {
		Enumeration <String> key = map.keys();
		String keyE = null;
		Vector <ValueSorter> vsv = new Vector <ValueSorter> ();
		while (key.hasMoreElements() == true ){
			keyE = key.nextElement();
			ValueSorter vs = new ValueSorter(keyE, map.get(keyE));
			vsv.add(vs);
		}
			Collections.sort(vsv); //ascending order
			
		if (desc.isSelected() == true) {
			Collections.reverse(vsv);
		}
		
		Object[] obj = new Object[vsv.size()];
		for ( int i=0; i <vsv.size(); i++ ) {
			ValueSorter vs = vsv.get(i);
			String keyv = vs.get_key();
			obj[i] = keyv;
		}
		return obj;
		
	}

	private String mapKey(Object key) {
		if (isDate == false
				|| comboT.getSelectedItem().toString()
						.compareToIgnoreCase("None") == 0
				|| key.toString().compareToIgnoreCase(OTHER) == 0)
			return key.toString();
		else {
			switch (comboT.getSelectedIndex()) {
			case 0:
			case 2:
				return key.toString();
			case 1:
				String yr = key.toString().substring(0, 4);
				return key.toString().substring(4, 6) + yr.substring(2, 4);
			case 3:
				yr = key.toString().substring(0, 4);
				return getMonthName(key.toString().substring(4, 5))
						+ yr.substring(2, 4);
			case 4:
				return getMonthName(key.toString());
			case 5:
				return getDayName(key.toString());
			default:
				return key.toString();
			}
		}

	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			int index = comboX.getSelectedIndex();
			Class<?> cclass = _rt.table.getColumnClass(index);
			if (cclass.getName().toUpperCase().contains("DATE"))
				comboT.setEnabled(true);
			else
				comboT.setEnabled(false);
		}

	}
	
	@SuppressWarnings("unchecked")
	private void createReportDialog(int chartType) {
		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		
		JPanel jp = new JPanel(new BorderLayout());
		
		JPanel topP = new JPanel();
		JLabel lm = new JLabel("Choose Dimensions (Group by) and Measures");
		topP.add(lm);
		jp.add(topP,BorderLayout.PAGE_START);
		
		JPanel cenP = new JPanel(new SpringLayout());
		String [] field = new String[]{"Group By","Sum","Absolute Sum","Count","Average"}; // add Avg, count
		comboCol = new JComboBox[_colC];
		comboField = new JComboBox[_colC];
		selOption = new JCheckBox[_colC];
		
		for (int i=0; i<_colC; i++) {
			comboCol[i] = new JComboBox<String>(col_n);
			comboField[i] = new JComboBox<String>(field);
			selOption[i] = new JCheckBox();
			cenP.add(selOption[i]);
			cenP.add(comboCol[i]);
			cenP.add(comboField[i]);
		}
		SpringUtilities.makeCompactGrid(cenP, _colC, 3, 3, 3, 3, 3);
		
		 int size = 50;
		 size += (400 > _colC*35 )?_colC*35 : 400;

		cenP.setPreferredSize(new Dimension(400, size));
		JScrollPane pane = new JScrollPane(cenP);
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		jp.add(pane,BorderLayout.CENTER);
		
		JPanel botP = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("reportok");
		ok.addActionListener(this);
		botP.add(ok);

		JButton cancel = new JButton("Cancel");
		cancel.addKeyListener(new KeyBoardListener());
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		botP.add(cancel);

		jp.add(botP,BorderLayout.PAGE_END);
		jp.setPreferredSize(new Dimension(420, size+40));
				
		jd = new JDialog();
		jd.setTitle("Report Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}
	private void showReport() {
		int newColC = _reportColV.size();
		String[] newColN = new String[newColC];
		int[] newColT = new int[newColC];
				
		for (int i = 0; i < newColC; i++) {
			newColN[i] = _rt.table.getColumnName(_reportColV.get(i));
			newColT[i] = _reportFieldV.get(i);
		}
		Vector<Integer> dimV = new Vector<Integer>();
		Vector<Integer> measureV = new Vector<Integer>();
		
		for (int i = 0; i < newColC; i++) {
			int fieldVal = _reportFieldV.get(i);
			if ( fieldVal == 0 ) { // Dimension
				dimV.add(_reportColV.get(i));
			} else {
				measureV.add(_reportColV.get(i));
			}
		}
		
		ReportTable newRT = new ReportTable(newColN,false,true);
		int dimSize = dimV.size();
		Object[] dimObj = new Object[dimSize];
		int measureSize = measureV.size();
		Object[] measureObj = new Object[measureSize];
		
		// A Hashtable to contain group by value and rowid
		Hashtable<String,Integer> rptContent = new Hashtable<String, Integer>();
		int newRowIndex = 0;
		
		// A Hashtable to contain group by value and count
		Hashtable<String,Integer> dimCount = new Hashtable<String, Integer>();
		Integer existingCount = null;
		
		for (int i=0; i< _rowC; i++) { //scan the table and create new tables
			
			boolean newrecord = true;
			Object[] row = new Object[newColC];
			String dimensionId ="";
			
			for ( int j=0; j<dimSize ; j++) {
				dimObj[j] = _rt.getValueAt(i,dimV.get(j));
				dimensionId += dimObj[j].toString();
			}
			Integer existingrowid = rptContent.get(dimensionId);
			existingCount = dimCount.get(dimensionId);
			
			if (existingrowid == null) {
				rptContent.put(dimensionId,newRowIndex++);
				existingCount = 1;
				dimCount.put(dimensionId, existingCount); // First Instance
			} else {
				newrecord = false;
				dimCount.put(dimensionId,++existingCount);
			}
			
			
			for ( int j=0; j<measureSize ; j++) {
				measureObj[j] = _rt.getValueAt(i,measureV.get(j));
			}
			// prepare new Record
			for ( int j=0; j<newColC ; j++) {
				int fieldVal = newColT[j];
				if ( fieldVal == 2) {  // Absolute Sum
					try {
					row[j] = Math.abs((Double)(_rt.getValueAt(i,_reportColV.get(j))));
					} catch (Exception e) {
						ConsoleFrame.addText("\n Can not cast table value as Number");
					}
				} else if ( fieldVal == 3) { // Count
					row[j] = (Double)existingCount.doubleValue();
				} else {
					row[j] = _rt.getValueAt(i,_reportColV.get(j));
				}
			}
			
			if (newrecord == true) {
				newRT.addFillRow(row);
				
			} else { // add sum, abs sum, count, avg
				Object[] existMeasureObj = new Object[measureSize];
				int k=0;
				for (int j = 0; j < newColC; j++) {
					int fieldVal = newColT[j];
					if (fieldVal == 1 || fieldVal == 2 // Sum // Absolute Sum
							||  fieldVal == 3 ||  fieldVal == 4 ) {  // Count // Avg
						existMeasureObj[k] = newRT.getValueAt(existingrowid.intValue(),j);
						Double newVal =0D;
						if (newRT.getModel().getColumnClass(j).getName().toString().toUpperCase().contains("DOUBLE")) {
							if (fieldVal == 1) {
								 newVal = (Double)existMeasureObj[k] + (Double)measureObj[k];
							} else if (fieldVal == 2) {
								 newVal = Math.abs((Double)existMeasureObj[k]) + Math.abs((Double)measureObj[k]);
							} else if (fieldVal == 3) {
								newVal = existingCount.doubleValue();
							} else if (fieldVal == 4) {
								newVal = (((Double)(existMeasureObj[k])*(existingCount-1)) + (Double)measureObj[k])/existingCount;
							}
							
							newRT.table.setValueAt(newVal, existingrowid.intValue(), j);
							k++;
						} else {
							ConsoleFrame.addText("\n Value is not Number");
						}
					}		
						
				}
				
				
			} // End of updating existing row

		} // End of For loop
		
		jd = new JDialog();
		jd.setTitle("Report Table");
		jd.setLocation(150, 100);
		jd.getContentPane().add(newRT);
		jd.pack();
		jd.setVisible(true);
		
	}

}
