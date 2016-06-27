package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;

import org.arrah.framework.analytics.LocationAnalysis;
import org.arrah.framework.analytics.TabularReport;
import org.arrah.framework.datagen.TimeUtil;
import org.arrah.framework.ndtable.RTMUtil;
import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.util.ValueSorter;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class FileAnalyticsListener implements ActionListener, ItemListener {
	private ReportTable _rt;
	private int _rowC = 0;
	private int _colC = 0;
	private String[] col_n;
	private Hashtable<String, Double> _map;
	private Vector<Integer> _reportColV, _reportFieldV;
	private JComboBox<String> comboX, comboY, comboT, comboLat, comboLon;
	private JComboBox<String> comboAggr;
	private JComboBox<String>[] comboCol, comboField;
	private JCheckBox[] selOption;
	private JRadioButton asc, desc, dsort, radLW, radLO;
	private JDialog jd;
	private String _title = "Chart";
	private boolean cancel_clicked = true;
	private int _chartType = -1; // No chart
	private JTextField tf, titleJT;
	private boolean isDate = false, isWithin = true;
	private JFormattedTextField latF, lonF, radF, radFO; // for location info
	private JFormattedTextField clusterF; // for Cluster

	static private int BARCHART = 1;
	static private int PIECHART = 2;
	static private int HBARCHART = 3;
	static private int LINECHART = 4;
	static private int REPORT = 5;
	static private int LOCATION = 6;
	static private int CROSSTAB = 7;
	static private int OUTNUM = 8;
	static private int OUTPERC = 9;
	static private int OUTSTDDEV = 10;
	static private int OUTBOXPLOT = 11;
	static private int KMEANCLUSTER = 12;
	static private int TIMESERIES = 13;
	static private int REGRESSION = 14;
	static private int TIMELINESS = 15;
	static private int STRINGLENGTH = 16;
	static private int TIMEFORECAST = 17;
	static private int TIMEREGRESSION = 18;
	static private int DATAENRICHMENT = 19;

	private static String OTHER = "UNDEFINED"; // Null or Undefined Key

	public FileAnalyticsListener(ReportTable rt) {
		_rt = rt;
		_rowC = rt.table.getRowCount();
		_colC = rt.table.getColumnCount();
		col_n = new String[_colC];
	}; // Constructor

	public FileAnalyticsListener(ReportTable rt, int chartType) {
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
	private void createLocationDialog() {
		JPanel jp = new JPanel(new BorderLayout());

		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		
		JPanel lat = new JPanel(new GridLayout(4,2));
		JLabel info = new JLabel("  Select Latitude and Longitude :");
		JLabel infof = new JLabel("Decimal Format DDD.dddd");
		lat.add(info);lat.add(infof);
		JLabel lm = new JLabel("  Latitude Column :");
		comboLat = new JComboBox<String>(col_n);
		lat.add(lm);
		lat.add(comboLat);
		JLabel ld = new JLabel("  Longitude Column :");
		comboLon = new JComboBox<String>(col_n);
		lat.add(ld);
		lat.add(comboLon);
		JLabel infoi = new JLabel("  Select Center and Radius");
		lat.add(infoi);
		jp.add(lat,BorderLayout.NORTH);

		JPanel radius = new JPanel(new GridLayout(4,2));
		JLabel cenLat = new JLabel("Center Latitude");
		latF = new JFormattedTextField(0000.000000);
		latF.setColumns(10);
		JLabel cenLon = new JLabel("Center Longitude");
		lonF = new JFormattedTextField(0000.000000);
		lonF.setColumns(10);
		
		radLW = new JRadioButton("Within Radius(Km)");
		radLW.setSelected(true);
		radF = new JFormattedTextField(00010.000);
		radF.setColumns(10);
		
		radLO = new JRadioButton("Outside Radius(Km)");
		radFO = new JFormattedTextField(00010.000);
		radFO.setColumns(10);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(radLW);bg.add(radLO);
		
		radius.add(cenLat);radius.add(latF);radius.add(cenLon);radius.add(lonF);
		radius.add(radLW);radius.add(radF);radius.add(radLO);radius.add(radFO);
		
		jp.add(radius,BorderLayout.CENTER);

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("locationok");
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		jp.add(bp,BorderLayout.SOUTH);
		
		jp.setPreferredSize(new Dimension(375, 250));
		jd = new JDialog();
		jd.setTitle("Location Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}
	
	private void createOutlierDialog() {
		JPanel jp = new JPanel(new BorderLayout());

		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		
		JPanel lat = new JPanel(new GridLayout(2,2));
		JLabel info = new JLabel("  Select Column  :");
		lat.add(info);
		comboLat = new JComboBox<String>(col_n);
		lat.add(comboLat);
		
		JLabel cenLat = new JLabel(" Outlier Input :");
		lat.add(cenLat);
		latF = new JFormattedTextField(10.00);
		latF.setColumns(10);
		lat.add(latF);
		jp.add(lat,BorderLayout.CENTER);

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("outlierok");
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		jp.add(bp,BorderLayout.SOUTH);
		
		jp.setPreferredSize(new Dimension(300, 100));
		jd = new JDialog();
		jd.setTitle("Outlier Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}

	private void createTimeSeriesDialog() {
		JPanel jp = new JPanel(new BorderLayout());

		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		
		JPanel timeserP = new JPanel(new GridLayout(3,2));
		JLabel info = new JLabel("  Select Date Column  :");
		timeserP.add(info);
		comboLat = new JComboBox<String>(col_n);
		timeserP.add(comboLat);
		
		JLabel cenLat = new JLabel(" Select Number Column :");
		timeserP.add(cenLat);
		comboLon = new JComboBox<String>(col_n);
		timeserP.add(comboLon);
		jp.add(timeserP,BorderLayout.CENTER);
		
		/***
		JLabel la = new JLabel("Aggregator for Number");
		timeserP.add(la);
		comboAggr = new JComboBox<String>(new String[] {"Sum","Avg","Count","Time Stamp"});
		timeserP.add(comboAggr);
		***/
		
		JLabel dim = new JLabel("Time Dimension");
		timeserP.add(dim);
		comboT = new JComboBox<String>(new String[] {"Year","Quarter","Month","Week", "Day",
				"Hour","Minute","Second","Milli Second"});
		timeserP.add(comboT);
		

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		if (_chartType == TIMEFORECAST )
			ok.setActionCommand("timeforecastok");
		else 
			ok.setActionCommand("timeseriesok");
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		jp.add(bp,BorderLayout.SOUTH);
		
		jp.setPreferredSize(new Dimension(350, 175));
		jd = new JDialog();
		if (_chartType == TIMEFORECAST )
			jd.setTitle("TimeSeries Forecast Input");
		else
			jd.setTitle("TimeSeries Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}
	
	private void createBoxPlotDialog() {
		JPanel jp = new JPanel(new BorderLayout());

		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		
		JPanel lat = new JPanel(new GridLayout(3,2));
		
		JLabel info = new JLabel("  Select Column  :");
		lat.add(info);
		comboLat = new JComboBox<String>(col_n);
		lat.add(comboLat);
		
		JLabel info1 = new JLabel("  Select Column  :");
		lat.add(info1);
		comboLon = new JComboBox<String>(col_n);
		lat.add(comboLon);
		
		JLabel cenLat = new JLabel(" Title :");
		lat.add(cenLat);
		titleJT = new JTextField("Box Plot");
		lat.add(titleJT);
		jp.add(lat,BorderLayout.CENTER);

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("boxplotok");
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		jp.add(bp,BorderLayout.SOUTH);
		
		jp.setPreferredSize(new Dimension(300, 100));
		jd = new JDialog();
		jd.setTitle("Outlier Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}
	private void createKMeanDialog() {
		JPanel jp = new JPanel(new BorderLayout());
		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		JPanel lat = new JPanel(new GridLayout(3,2));
		
		JLabel info = new JLabel("  Select Column  :");
		lat.add(info);
		comboLat = new JComboBox<String>(col_n);
		lat.add(comboLat);
		
		JLabel info1 = new JLabel("  Select Column  :");
		lat.add(info1);
		comboLon = new JComboBox<String>(col_n);
		lat.add(comboLon);
		
		JLabel cenLat = new JLabel(" No. of Cluster :");
		lat.add(cenLat);
		clusterF = new JFormattedTextField(5);
		lat.add(clusterF);
		jp.add(lat,BorderLayout.CENTER);

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("kmeanok");
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		jp.add(bp,BorderLayout.SOUTH);
		
		jp.setPreferredSize(new Dimension(300, 100));
		jd = new JDialog();
		jd.setTitle("KMean Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);
	}

	private void createRegressionDialog() {
		JPanel jp = new JPanel(new BorderLayout());

		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		
		JPanel regressionP = new JPanel(new GridLayout(3,2));
		JLabel info = new JLabel("  Select X Column  :");
		regressionP.add(info);
		comboLat = new JComboBox<String>(col_n);
		regressionP.add(comboLat);
		
		JLabel cenLat = new JLabel(" Select Y Column :");
		regressionP.add(cenLat);
		comboLon = new JComboBox<String>(col_n);
		regressionP.add(comboLon);
		jp.add(regressionP,BorderLayout.CENTER);
		
		
		JLabel dim = new JLabel("Regression Type");
		regressionP.add(dim);
		comboT = new JComboBox<String>(new String[] {"Linear","Polynomial","Power"});
		regressionP.add(comboT);
		

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("regressionok");
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		jp.add(bp,BorderLayout.SOUTH);
		
		jp.setPreferredSize(new Dimension(350, 175));
		jd = new JDialog();
		jd.setTitle("Regression Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}
	
	private void createTimeRegressionDialog() {
		JPanel jp = new JPanel(new BorderLayout());

		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		
		JPanel timeserP = new JPanel(new GridLayout(4,2));
		JLabel info = new JLabel("  Select Date Column  :");
		timeserP.add(info);
		comboLat = new JComboBox<String>(col_n);
		timeserP.add(comboLat);
		
		JLabel cenLat = new JLabel(" Select Number Column :");
		timeserP.add(cenLat);
		comboLon = new JComboBox<String>(col_n);
		timeserP.add(comboLon);
		
		JLabel dim = new JLabel("Time Dimension");
		timeserP.add(dim);
		comboT = new JComboBox<String>(new String[] {"Year","Quarter","Month","Week", "Day",
				"Hour","Minute","Second","Milli Second"});
		timeserP.add(comboT);
		
		JLabel rtype = new JLabel("Regression Type");
		timeserP.add(rtype);
		comboAggr = new JComboBox<String>(new String[] {"Linear","Polynomial","Power"});
		timeserP.add(comboAggr);
		
		jp.add(timeserP,BorderLayout.CENTER);
		

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("timeregressionok");
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		jp.add(bp,BorderLayout.SOUTH);
		
		jp.setPreferredSize(new Dimension(350, 175));
		jd = new JDialog();
		jd.setTitle("Time Regression Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}
	
	
	private void createTimelinessDialog() {
		JPanel jp = new JPanel(new BorderLayout());

		for (int i = 0; i < _colC; i++) {
			col_n[i] = _rt.table.getColumnName(i);
		}
		
		JPanel timeserP = new JPanel(new GridLayout(1,2));
		JLabel info = new JLabel("  Select Date Column  :");
		if (_chartType == STRINGLENGTH)
			info.setText(" Select Column :");
		timeserP.add(info);
		comboLat = new JComboBox<String>(col_n);
		timeserP.add(comboLat);
		jp.add(timeserP,BorderLayout.CENTER);
		

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		if (_chartType == STRINGLENGTH)
			ok.setActionCommand("strlenok");
		else
			ok.setActionCommand("timelinessok");
		
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		jp.add(bp,BorderLayout.SOUTH);
		
		jp.setPreferredSize(new Dimension(350, 100));
		jd = new JDialog();
		if (_chartType == STRINGLENGTH)
			jd.setTitle("String Length Parameters Input");
		else
			jd.setTitle("Timeliness Parameters Input");
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
			showReport(REPORT);
			return;
		} else if (_chartType == CROSSTAB) {
			createReportDialog(CROSSTAB);
			if (cancel_clicked)
				return;
			showReport(CROSSTAB);
			return;
		}  else if (_chartType == LOCATION) {
			createLocationDialog();
			if (cancel_clicked)
				return;
			double lat = (Double)latF.getValue();
			double lon = (Double)lonF.getValue();
			double radius = (Double)radF.getValue();
			int latIndex = comboLat.getSelectedIndex();
			int lonIndex = comboLon.getSelectedIndex();

			showLocationRadiusReport(lat,lon,radius,latIndex,lonIndex,isWithin);
			return;
		}  else if (_chartType == OUTNUM || _chartType == OUTPERC
				|| _chartType == OUTSTDDEV ) {
			createOutlierDialog();
			if (cancel_clicked)
				return;
			// Will be added in future
			 // int colndex = comboLat.getSelectedIndex();
			// showBoxPlot(colndex);
		} else if ( _chartType == OUTBOXPLOT) {
			createBoxPlotDialog();
			if (cancel_clicked)
				return;
			String title = titleJT.getText();
			showBoxPlot(comboLat.getSelectedItem().toString(),comboLon.getSelectedItem().toString(),title);
		} else if ( _chartType == KMEANCLUSTER) {
			createKMeanDialog();
			if (cancel_clicked)
				return;
			int noClus = (Integer) clusterF.getValue();
			showKMeanPlot(comboLat.getSelectedItem().toString(),comboLon.getSelectedItem().toString(),noClus);
		} else if ( _chartType == TIMESERIES) {
			createTimeSeriesDialog();
			if (cancel_clicked)
				return;
			// int aggrIndex = comboAggr.getSelectedIndex();
			int aggrIndex = 0; // dummy
			int dimIndex = comboT.getSelectedIndex();
			showTSPlot(comboLat.getSelectedItem().toString(),comboLon.getSelectedItem().toString(),aggrIndex,dimIndex);
		} else if ( _chartType == REGRESSION ) {
			createRegressionDialog();
			if (cancel_clicked)
				return;
			int dimIndex = comboT.getSelectedIndex();
			showRegressionPlot(comboLat.getSelectedItem().toString(),comboLon.getSelectedItem().toString(),dimIndex);
		} else if ( _chartType == TIMELINESS) {
			createTimelinessDialog();
			if (cancel_clicked)
				return;
			showTimelinessPlot(comboLat.getSelectedItem().toString());
		} else if ( _chartType == STRINGLENGTH) {
			createTimelinessDialog();
			if (cancel_clicked)
				return;
			showStringLenPlot(comboLat.getSelectedItem().toString());
		} else if ( _chartType == TIMEFORECAST) {
			createTimeSeriesDialog();
			if (cancel_clicked)
				return;
			// int aggrIndex = comboAggr.getSelectedIndex();
			int aggrIndex = 0; // dummy
			int dimIndex = comboT.getSelectedIndex();
			showTSForecastPlot(comboLat.getSelectedItem().toString(),comboLon.getSelectedItem().toString(),aggrIndex,dimIndex);
		} else if (_chartType == TIMEREGRESSION ) {
			createTimeRegressionDialog();
			if (cancel_clicked)
				return;
			int dimIndex = comboT.getSelectedIndex();
			int rtype = comboAggr.getSelectedIndex();
			showTimeRegressionPlot(comboLat.getSelectedItem().toString(),comboLon.getSelectedItem().toString(),rtype,dimIndex);
		}  else if (_chartType == DATAENRICHMENT ) {
			createRegressionDialog();
			if (cancel_clicked)
				return;
			int dimIndex = comboT.getSelectedIndex();
			showDataEnrichmentTable(comboLat.getSelectedItem().toString(),comboLon.getSelectedItem().toString(),dimIndex);
		}

	} // End of create Analytics

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
					key = TimeUtil.timeKey((Date) obj, comboT.getSelectedIndex());
				}

				Object value = _rt.table.getValueAt(i, ys);
				if (value == null) continue;
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
			int colC = _rt.getModel().getColumnCount();
			for (int i=0; i < colC; i++) {
				if (selOption[i].isSelected() == true ) {
					_reportColV.add(_rt.getRTMModel().getColumnIndex(
							_rt.table.getColumnName(comboCol[i].getSelectedIndex())));
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
			
		} else if (action.equals("locationok")) {
			if (radLW.isSelected() == true)
				isWithin = true;
			else
				isWithin = false;
			
			jd.dispose();
			cancel_clicked = false;
			
		} else if (action.equals("outlierok")) {
			jd.dispose();
			cancel_clicked = false;
			
		} else if (action.equals("boxplotok")) {
			jd.dispose();
			cancel_clicked = false;
			
		} else if (action.equals("kmeanok")) {
			jd.dispose();
			cancel_clicked = false;
			
		} else if (action.equals("timeseriesok")) {
			jd.dispose();
			cancel_clicked = false;
			
		} else if (action.equals("regressionok")) {
			jd.dispose();
			cancel_clicked = false;
			
		} else if (action.equals("timelinessok")) {
			jd.dispose();
			cancel_clicked = false;
			
		} else if (action.equals("strlenok")) {
			jd.dispose();
			cancel_clicked = false;
			
		} else if (action.equals("timeforecastok")) {
			jd.dispose();
			cancel_clicked = false;
			
		} else if (action.equals("timeregressionok")) {
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


	private Object[] sortKey(Hashtable<String, Double> map) {
		if (asc.isSelected() == true ) {
			return ValueSorter.sortOnValue(map,false); // desc is false
		} else if ( desc.isSelected() == true ) {
			return ValueSorter.sortOnValue(map,true); // desc is true
		}
		return ValueSorter.sortKey(map, OTHER);
		
	}
	
	
	private String mapKey(Object key) {
		if (isDate == false
				|| comboT.getSelectedItem().toString().compareToIgnoreCase("None") == 0
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
				return TimeUtil.getMonthName(key.toString().substring(4, 5))
						+ yr.substring(2, 4);
			case 4:
				return TimeUtil.getMonthName(key.toString());
			case 5:
				return TimeUtil.getDayName(key.toString());
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
		BoxLayout bxl = new BoxLayout(topP, BoxLayout.Y_AXIS);
		topP.setLayout(bxl);

		JLabel lm = new JLabel("  Tip 1: Columns should be Number Formatted for  Measures", JLabel.LEFT);
		JLabel lm1 = new JLabel("  Tip 2: For Cross Tab atleast one Column Dimension must be there",JLabel.LEFT);
		JLabel lm2 = new JLabel("  Tip 3: Same Order of \"Group By\" Columns will be carried to Report",JLabel.LEFT);
		JLabel lm3 = new JLabel("  Tip 4: Create Column Aliases (Copy) to use same column in multiple Measures",JLabel.LEFT);
		JLabel dummy1  = new JLabel(" ");
		
		JLabel lm4 = new JLabel(" Choose Dimensions (Group by) and Measures", JLabel.CENTER);

		topP.add(lm);topP.add(lm1);topP.add(lm2);topP.add(lm3);topP.add(dummy1);topP.add(lm4);
		topP.setPreferredSize(new Dimension(450,125));
		jp.add(topP,BorderLayout.PAGE_START);
		
		JPanel cenP = new JPanel(new SpringLayout());
		String [] field = null;
		if (chartType == REPORT)
			field = new String[]{"Group By","Sum","Absolute Sum","Count","Average"}; // add Avg, count
		if (chartType == CROSSTAB) 		// Cross Tab Row and Column dimension
			field = new String[]{"Row Dimension","Column Dimension","Sum","Absolute Sum","Count","Average"};

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

		cenP.setPreferredSize(new Dimension(450, size)); 
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
		jp.setPreferredSize(new Dimension(470, size+150)); // 200 for tips
				
		jd = new JDialog();
		jd.setTitle("Report Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}
	private void showReport(int reportType) {
		
		// Columns might be at different location for ReportTable and ReportTableModel
		ReportTableModel newRTM = null;
		
		Vector<Integer> orgreportColV, orgreportFieldV;
		orgreportColV = new Vector<Integer>(); orgreportFieldV = new Vector<Integer>();
		
		if (reportType == CROSSTAB) {
			// Column Dimension is also Group By but display will on cross tab
			// so reset the value get tabular report then convert tabular report to crossTab
			 Vector<Integer> newreportColV, newreportFieldV;
			 newreportColV = new Vector<Integer>(); newreportFieldV = new Vector<Integer>();
			 
			 for (int i=0; i < _reportFieldV.size(); i++ ) { // First Fill Row Dimension as high on priority
				 if( _reportFieldV.get(i) == 0 ) {
					 newreportFieldV.add(_reportFieldV.get(i));
					 newreportColV.add(_reportColV.get(i));
					 orgreportFieldV.add(_reportFieldV.get(i));
					 orgreportColV.add(_reportColV.get(i));
				 }
			 }
			 for (int i=0; i < _reportFieldV.size(); i++ ) { // Then Others and reduce by one zero for Row Dimension
				 if( _reportFieldV.get(i) != 0 ) {
					 newreportFieldV.add(_reportFieldV.get(i) -1 );
					 newreportColV.add(_reportColV.get(i));
					 orgreportFieldV.add(_reportFieldV.get(i));
					 orgreportColV.add(_reportColV.get(i));
				 }
			 }
			 
			 newRTM = TabularReport.showReport(_rt.getRTMModel(), newreportColV, newreportFieldV);
		} else { // Tabular Report
			newRTM = TabularReport.showReport(_rt.getRTMModel(), _reportColV, _reportFieldV);
		}
		// try to Sort the table
		ReportTableModel newRTM1 = RTMUtil.sortRTM(newRTM, true);
		ReportTable newRT = null;
		if (reportType == CROSSTAB) {
			ReportTableModel crossRTM = TabularReport.tabToCrossTab(newRTM1, orgreportColV, orgreportFieldV);
			 newRT = new ReportTable(crossRTM);
		} else
			newRT = new ReportTable(newRTM1);
		
		jd = new JDialog();
		jd.setTitle("Report Table");
		jd.setLocation(150, 100);
		jd.getContentPane().add(newRT);
		jd.pack();
		jd.setVisible(true);
		
	}
	private void showLocationRadiusReport(double clat, double clong, double cRad, int latIndex, int longIndex, boolean iswithin) {
		// Columns might be at different location for ReportTable and ReportTableModel
		
		LocationAnalysis.LocationComparator lcomp = new LocationAnalysis.LocationComparator();
		ReportTable newRT = new ReportTable(_rt.getAllColName(), false, true);
		newRT.getRTMModel().addColumn("distanceFromCenter");
		
		int rowC= _rt.getModel().getRowCount();
		
		for (int i=0; i < rowC; i++) {
			try {
				Object[] row = _rt.getRow(i);
				Object[] distance = new Object[1];
				double distance_d = lcomp.compare(clat, clong, cRad, row , latIndex, longIndex);
				distance[0] = Math.abs(distance_d);
			
				if (iswithin == true && distance_d <= 0) // within radius
						newRT.addFillRow(row,distance);
				else if (iswithin == false  && distance_d > 0) // outside radius
					newRT.addFillRow(row,distance);
			} catch (Exception e) {
				ConsoleFrame.addText("\n Exception for row :" +i + "  Execption:"+e.getLocalizedMessage());
			}
		}
		
		jd = new JDialog();
		jd.setTitle("Location Radius Table");
		jd.setLocation(150, 100);
		jd.getContentPane().add(newRT);
		jd.pack();
		jd.setVisible(true);
		
	}
	private void showBoxPlot(String col1, String comCol1, String title) {

		BoxPlotPanel bp = new BoxPlotPanel(title,"Field","Value");
		try {
			if (col1.equals(comCol1) == false)
				bp.addRTMDataSet(_rt.getRTMModel(), col1, comCol1);
			else
				bp.addRTMDataSet(_rt.getRTMModel(), col1, "");

			bp.drawBoxPlot();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Exception:"+e.getMessage());
			return;	
		}
		
		jd = new JDialog();
		jd.setTitle("Box Plot Dialog");
		jd.setLocation(150, 100);
		jd.getContentPane().add(bp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);
		
	}
	private void showKMeanPlot(String col1, String comCol1, int noClus) {

		KMeanPanel km = new KMeanPanel("KMean Plot","Field","Value");
		Vector<String> colname = new Vector<String>();
		colname.add(col1);
		if (col1.equals(comCol1) == false) 
			colname.add(comCol1);
		try {
			km.addRTMDataSet(_rt.getRTMModel(), colname);
			km.drawKMeanPlot(noClus,colname);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Exception:"+e.getMessage());
			return;
		}
		
		jd = new JDialog();
		jd.setTitle("KMean Dialog");
		jd.setLocation(150, 100);
		jd.getContentPane().add(km);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);
		
	}
	
	private void showTSPlot(String dateCol, String  numCol, int aggrIndex, int dimIndex) {

		TSPlotPanel ts = new TSPlotPanel("Time Series",dateCol,numCol);
		try {
			RTMUtil.addRTMDataSet(ts.getTimeSeries(),_rt.getRTMModel(), dateCol,numCol,dimIndex);
			ts.drawTSPlot();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Exception:"+e.getMessage());
			return;
		}
		
		jd = new JDialog();
		jd.setTitle("Time Series Dialog");
		jd.setLocation(150, 100);
		jd.getContentPane().add(ts);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);
		
	}
	
	private void showTSForecastPlot(String dateCol, String  numCol, int aggrIndex, int dimIndex) {

		TSPlotPanel ts = new TSPlotPanel("Time Series Forecast",dateCol,numCol);
		
		try {
			RTMUtil.addRTMDataSet(ts.getTimeSeries(),_rt.getRTMModel(), dateCol,numCol,dimIndex);
			ts.drawTSForecastPlot();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Exception:"+e.getMessage());
			return;
		}
		
		jd = new JDialog();
		jd.setTitle("Time Series Forecast Dialog");
		jd.setLocation(150, 100);
		jd.getContentPane().add(ts);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);
		
	}
	private void showTimeRegressionPlot(String dateCol, String  numCol, int rtype, int dimIndex) {

		RegressionPlotPanel rs = new RegressionPlotPanel("Time Regression ",dateCol,numCol);
		try {
			RTMUtil.addRTMDataSet(rs.getTimeSeries(),_rt.getRTMModel(), dateCol,numCol,dimIndex);
			rs.drawTimeRegressionPlot(rtype);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Exception:"+e.getMessage());
			return;
		}
		
		jd = new JDialog();
		jd.setTitle("Time Regression Dialog");
		jd.setLocation(150, 100);
		jd.getContentPane().add(rs);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);
		
	}
	
	private void showRegressionPlot(String dateCol, String  numCol,  int dimIndex) {

		RegressionPlotPanel rs = new RegressionPlotPanel("Regression ",dateCol,numCol);
		try {
			RTMUtil.addRTMDataSet(rs.getXYSeries(),_rt.getRTMModel(), dateCol,numCol);
			rs.drawRegressionPlot(dimIndex);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Exception:"+e.getMessage());
			return;
		}
		
		jd = new JDialog();
		jd.setTitle("Regression Dialog");
		jd.setLocation(150, 100);
		jd.getContentPane().add(rs);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);
		
	}
	
	private void showDataEnrichmentTable(String dateCol, String  numCol,  int dimIndex) {
		
		XYSeries xyseries = new XYSeries("Regression Data Enrichment");
		XYSeriesCollection xyseriescollection = new XYSeriesCollection(xyseries);
		double[] ad = new double[5];
		try {
			xyseries = RTMUtil.addRTMDataSet(xyseries,_rt.getRTMModel(), dateCol,numCol);
			if (dimIndex == 0) { // Add a+bx = a and b to tile
	        	ad = Regression.getOLSRegression(xyseriescollection, 0);
	        } else if (dimIndex == 1) { // Polynomial default order 4 --  a +bx+ cx^2+dx^3 +ex^4
	        	ad = Regression.getPolynomialRegression(xyseriescollection, 0,4);
	        } else if (dimIndex == 2) { // Power ax^b
	        	ad = Regression.getPowerRegression(xyseriescollection, 0);
	        }
			RTMUtil.addEnrichment(_rt.getRTMModel(), dateCol,numCol,ad,dimIndex);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Exception:"+e.getMessage());
			return;
		}
	}
	
	private void showTimelinessPlot(String dateCol) {

		int rowC= _rt.getModel().getRowCount();
		int index = _rt.getRTMModel().getColumnIndex(dateCol);
		if (index < 0) {
			JOptionPane.showMessageDialog(null,"Could not find Column Name");
			return;
		}
		Vector<Long> timeArray = new Vector<Long>();
		for (int i=0; i < rowC; i++) {
			try {
				Object cell = _rt.getModel().getValueAt(i, index);
				if (cell instanceof java.util.Date)
					timeArray.add(((java.util.Date)cell).getTime());
				else 
					ConsoleFrame.addText("\n Row Id:"+i+ " :Could not parse as Date" );
					
			} catch (Exception e) {
				ConsoleFrame.addText("\n Row Id:"+i+ " Exception:"+e.getLocalizedMessage() );
			}
		}
		Long[] temparray = new Long[timeArray.size()];
		temparray = timeArray.toArray(temparray);
		JPanel rs= new 	TimeProfilerPanel(temparray);
		jd = new JDialog();
		jd.setTitle("Timeliness Dialog");
		jd.setLocation(150, 100);
		jd.getContentPane().add(rs);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);
		
	}
	
	private void showStringLenPlot(String dateCol) {

		int rowC= _rt.getModel().getRowCount();
		int index = _rt.getRTMModel().getColumnIndex(dateCol);
		if (index < 0) {
			JOptionPane.showMessageDialog(null,"Could not find Column Name");
			return;
		}
		Vector<Object> strArray = new Vector<Object>();
		for (int i=0; i < rowC; i++) {
			try {
				Object cell = _rt.getModel().getValueAt(i, index);
				if (cell !=  null)
					strArray.add(cell.toString().length());
				else 
					ConsoleFrame.addText("\n Row Id:"+i+ " :Null Value" );
					
			} catch (Exception e) {
				ConsoleFrame.addText("\n Row Id:"+i+ " Exception:"+e.getLocalizedMessage() );
			}
		}

		JPanel rs= new 	StringLenProfilerPanel(strArray.toArray());
		jd = new JDialog();
		jd.setTitle("String Length Dialog");
		jd.setLocation(150, 100);
		jd.getContentPane().add(rs);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);
		
	}

}
