package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2015      *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This class is used for displaying  
 * loaded table into Frame 
 *
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;

import org.arrah.framework.analytics.MetadataMatcher;
import org.arrah.framework.analytics.NormalizeCol;
import org.arrah.framework.datagen.AggrCumRTM;
import org.arrah.framework.datagen.RandomColGen;
import org.arrah.framework.dataquality.FillCheck;
import org.arrah.framework.dataquality.FormatCheck;
import org.arrah.framework.dataquality.SimilarityCheckLucene;
import org.arrah.framework.hadooputil.HiveQueryBuilder;
import org.arrah.framework.ndtable.RTMUtil;
import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.ndtable.ResultsetToRTM;
import org.arrah.framework.profile.FileProfile;
import org.arrah.framework.profile.InterTableInfo;
import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.rdbms.SqlType;
import org.arrah.framework.util.DiscreetRange;
import org.arrah.framework.util.KeyValueParser;
import org.arrah.framework.util.StringCaseFormatUtil;
import org.arrah.framework.xml.XmlReader;

public class DisplayFileTable extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ReportTable _rt;
	private JRadioButton rb1, rb2;
	private JFormattedTextField tf, rn;
	private int actionType;
	private JDialog d_f, d_m;
	private String _fileN = "";
	private Hashtable<String, Integer> _ht;

	private Vector[] vector1;
	private JComboBox[] table1, col1;
	private JLabel[] l1;
	private JRadioButton[] radio1;
	private JTextField[] tf1;
	private boolean init = false;

	private int ADDITION = 0;
	private int DELETION = 1;

	private String[] queryString;
	private Vector<String> table_s, column_s;
	private Vector<String> unique_table_s;
	private Vector<Object> copy_v;
	private Vector<Integer> delete_v = null;
	private Vector<Object[]> delrow_v = null;

	private JCheckBox local, overWrite; 
	private JTextField locationf;
	private JTextArea partArea;
	
	private JFrame frame;

	// Constructor
	public DisplayFileTable(ReportTable rt) {
		_rt = rt;
		
		// call show GUI explicitly
		// showGUI();
	}

	public DisplayFileTable(ReportTable rt, String fileN) {
		_rt = rt;
		_fileN = fileN;
		
		// call show GUI explicitly
		// showGUI();
	}

	public void showGUI() {
		if (_rt == null)
			return;
		frame = new JFrame("File Table Display:" + _fileN);
		frame.getContentPane().add(_rt);
		frame.setLocation(100, 100);
		// Add Menubar
		JMenuBar menubar = new JMenuBar();
		frame.setJMenuBar(menubar);

		JMenu option_m = new JMenu("Options");
		option_m.setMnemonic('O');
		menubar.add(option_m);

		JMenu column_m = new JMenu("Column");
		column_m.setMnemonic('C');
		menubar.add(column_m);
		
		// Data Preparation Menu
		JMenu preparation_m = new JMenu("Preparation");
		preparation_m.setMnemonic('P');
		menubar.add(preparation_m);
		
		JMenuItem testdata_m = new JMenuItem("Test Data Preparation");
		testdata_m.addActionListener(this);
		testdata_m.setActionCommand("testdata");
		preparation_m.add(testdata_m);
		
		JMenuItem ordinal_m = new JMenuItem("Ordinal Variables");
		ordinal_m.addActionListener(this);
		ordinal_m.setActionCommand("ordinal");
		preparation_m.add(ordinal_m);
		
		JMenuItem onehot_m = new JMenuItem("One Hot Encoding");
		onehot_m.addActionListener(this);
		onehot_m.setActionCommand("onehot");
		preparation_m.add(onehot_m);
		
		JMenuItem season_m = new JMenuItem("Seasonality");
		season_m.addActionListener(this);
		season_m.setActionCommand("seasonality");
		preparation_m.add(season_m);
		
		JMenu timegrp_m = new JMenu("Time Grouping");
		
		JMenuItem monthgrp_m = new JMenuItem("Month Grouping");
		monthgrp_m.addActionListener(this);
		monthgrp_m.setActionCommand("monthgrouping");
		timegrp_m.add(monthgrp_m);
		JMenuItem daygrp_m = new JMenuItem("Day Grouping");
		daygrp_m.addActionListener(this);
		daygrp_m.setActionCommand("daygrouping");
		timegrp_m.add(daygrp_m);
		JMenuItem dategrp_m = new JMenuItem("Date Grouping");
		dategrp_m.addActionListener(this);
		dategrp_m.setActionCommand("dategrouping");
		timegrp_m.add(dategrp_m);
		JMenuItem minutegrp_m = new JMenuItem("Minute Grouping");
		minutegrp_m.addActionListener(this);
		minutegrp_m.setActionCommand("minutegrouping");
		timegrp_m.add(minutegrp_m);
		JMenuItem secondgrp_m = new JMenuItem("Second Grouping");
		secondgrp_m.addActionListener(this);
		secondgrp_m.setActionCommand("secondgrouping");
		timegrp_m.add(secondgrp_m);
		
		preparation_m.add(timegrp_m);
		
		JMenu enrich_m = new JMenu("Enrichment");
		preparation_m.add(enrich_m);
		
		JMenuItem geoencode_m = new JMenuItem("Geo Encoding");
		geoencode_m.addActionListener(this);
		geoencode_m.setActionCommand("geocoding");
		enrich_m.add(geoencode_m);
		
		JMenu nullrep_m = new JMenu("Null Replace");
		enrich_m.add(nullrep_m);
		
		JMenuItem attrreg_m = new JMenuItem("Attributes Based");
		attrreg_m.addActionListener(this);
		attrreg_m.setActionCommand("attreplace");
		nullrep_m.add(attrreg_m);
		
		JMenuItem nullreg_m = new JMenuItem("Regression Based");
		nullreg_m.addActionListener(this);
		nullreg_m.setActionCommand("nullreplace");
		nullrep_m.add(nullreg_m);
		
		JMenuItem avgreg_m = new JMenuItem("Average Value");
		avgreg_m.addActionListener(this);
		avgreg_m.setActionCommand("avgreplace");
		nullrep_m.add(avgreg_m);

		JMenuItem prereg_m = new JMenuItem("Previous Value");
		prereg_m.addActionListener(this);
		prereg_m.setActionCommand("prereplace");
		nullrep_m.add(prereg_m);

		JMenuItem rand_m = new JMenuItem("Random Value");
		rand_m.addActionListener(this);
		rand_m.setActionCommand("randreplace");
		nullrep_m.add(rand_m);
		
		JMenu normal_m = new JMenu("Normalization");
		preparation_m.add(normal_m);
		JMenuItem zeroNormal_m = new JMenuItem("between 0-1");
		zeroNormal_m.addActionListener(this);
		zeroNormal_m.setActionCommand("zeronormal");
		normal_m.add(zeroNormal_m);
		JMenuItem zscore_m = new JMenuItem("Z-Score");
		zscore_m.addActionListener(this);
		zscore_m.setActionCommand("zscore");
		normal_m.add(zscore_m);
		normal_m.addSeparator();
		
		JMenuItem meanNormal_m = new JMenuItem("Mean Ratio");
		meanNormal_m.addActionListener(this);
		meanNormal_m.setActionCommand("meanratio");
		normal_m.add(meanNormal_m);
		JMenuItem stdNormal_m = new JMenuItem("Standard Deviation Ratio");
		stdNormal_m.addActionListener(this);
		stdNormal_m.setActionCommand("stdratio");
		normal_m.add(stdNormal_m);
		normal_m.addSeparator();
		
		JMenuItem meanSubs_m = new JMenuItem("Mean Substraction");
		meanSubs_m.addActionListener(this);
		meanSubs_m.setActionCommand("meansubstract");
		normal_m.add(meanSubs_m);
		JMenuItem meandistSubs_m = new JMenuItem("Mean Distance by Std. Deviation");
		meandistSubs_m.addActionListener(this);
		meandistSubs_m.setActionCommand("meandist");
		normal_m.add(meandistSubs_m);
		normal_m.addSeparator();
		
		// rounding, flooring, ceiling, neareast 0
		JMenuItem rounding_m = new JMenuItem("Rounding");
		rounding_m.addActionListener(this);
		rounding_m.setActionCommand("rounding");
		normal_m.add(rounding_m);
		
		JMenuItem flooring_m = new JMenuItem("Flooring");
		flooring_m.addActionListener(this);
		flooring_m.setActionCommand("flooring");
		normal_m.add(flooring_m);
		
		JMenuItem ceiling_m = new JMenuItem("Ceiling");
		ceiling_m.addActionListener(this);
		ceiling_m.setActionCommand("ceiling");
		normal_m.add(ceiling_m);
		
		JMenuItem nearzero_m = new JMenuItem("Nearest Zero");
		nearzero_m.addActionListener(this);
		nearzero_m.setActionCommand("nearestzero");
		normal_m.add(nearzero_m);
		
		// Analytics Menu
		JMenu analytics_m = new JMenu("Analytics");
		analytics_m.setMnemonic('A');
		menubar.add(analytics_m);
		
		JMenuItem reportC_m = new JMenuItem("Tabular Report");
		reportC_m.addActionListener(this);
		reportC_m.setActionCommand("report");
		analytics_m.add(reportC_m);
		
		JMenuItem crosstablC_m = new JMenuItem("Cross Tab Report");
		crosstablC_m.addActionListener(this);
		crosstablC_m.setActionCommand("crosstab");
		analytics_m.add(crosstablC_m);

		JMenu uniDim = new JMenu("Visual Analytics");
		JMenuItem lineC_m = new JMenuItem("Line Chart");
		lineC_m.addActionListener(this);
		lineC_m.setActionCommand("linechart");
		uniDim.add(lineC_m);

		JMenuItem barC_m = new JMenuItem("Bar Chart");
		barC_m.addActionListener(this);
		barC_m.setActionCommand("barchart");
		uniDim.add(barC_m);

		JMenuItem hbarC_m = new JMenuItem("Horizontal Bar Chart");
		hbarC_m.addActionListener(this);
		hbarC_m.setActionCommand("hbarchart");
		uniDim.add(hbarC_m);

		JMenuItem pieC_m = new JMenuItem("Pie Chart");
		pieC_m.addActionListener(this);
		pieC_m.setActionCommand("piechart");
		uniDim.add(pieC_m);
		
		analytics_m.add(uniDim);
		analytics_m.addSeparator();
		
		JMenuItem timeser_m = new JMenuItem("Time Series Analysis");
		timeser_m.addActionListener(this);
		timeser_m.setActionCommand("timeseries");
		analytics_m.add(timeser_m);
		
		JMenuItem timeser_for = new JMenuItem("Time Series Forecast");
		timeser_for.addActionListener(this);
		timeser_for.setActionCommand("timefore");
		analytics_m.add(timeser_for);
		
		JMenuItem timeliness = new JMenuItem("Timeliness");
		timeliness.addActionListener(this);
		timeliness.setActionCommand("timeliness");
		analytics_m.add(timeliness);
		
		analytics_m.addSeparator();
		
		/*** Outlier Analysis ***/
		JMenu outlier = new JMenu("Outlier");
		// Box Plot for now. Other algo will be added later
		JMenuItem out_box = new JMenuItem("Box Plot");
		out_box.addActionListener(this);
		out_box.setActionCommand("boxplot");
		outlier.add(out_box);
		analytics_m.add(outlier); // Outlier
		
		JMenuItem kmean = new JMenuItem("K-Mean Cluster");
		kmean.addActionListener(this);
		kmean.setActionCommand("kmean");
		analytics_m.add(kmean);
		
		JMenuItem regress = new JMenuItem("Number Regression");
		regress.addActionListener(this);
		regress.setActionCommand("regression");
		analytics_m.add(regress);
		
		JMenuItem tregress = new JMenuItem("Time Regression");
		tregress.addActionListener(this);
		tregress.setActionCommand("timeregression");
		analytics_m.add(tregress);
		
		analytics_m.addSeparator();
		
		JMenuItem loc_m = new JMenuItem("Location Analytics");
		loc_m.addActionListener(this);
		loc_m.setActionCommand("location");
		analytics_m.add(loc_m);
		analytics_m.addSeparator();
		
		JMenuItem strlength = new JMenuItem("String Length Analysis");
		strlength.addActionListener(this);
		strlength.setActionCommand("stringlen");
		analytics_m.add(strlength);
		analytics_m.addSeparator();
		
		JMenuItem mfsearch = new JMenuItem("Multi Facet Search");
		mfsearch.addActionListener(this);
		mfsearch.setActionCommand("mfsearch");
		analytics_m.add(mfsearch);
		analytics_m.addSeparator();
		
		// Pearson Correlation
		JMenuItem correlation = new JMenuItem("Pearson Correlation");
		correlation.addActionListener(this);
		correlation.setActionCommand("pcorrelation");
		analytics_m.add(correlation);

		// Column Menu
		JMenuItem addC_m = new JMenuItem("Add Column");
		addC_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				InputEvent.ALT_MASK));
		addC_m.addActionListener(this);
		addC_m.setActionCommand("addcolumn");
		column_m.add(addC_m);

		JMenuItem hideC_m = new JMenuItem("Hide Column");
		hideC_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				InputEvent.ALT_MASK));
		hideC_m.addActionListener(this);
		hideC_m.setActionCommand("hidecolumn");
		column_m.add(hideC_m);
		
		JMenuItem clearC_m = new JMenuItem("Clear Column");
		clearC_m.addActionListener(this);
		clearC_m.setActionCommand("clearcolumn");
		column_m.add(clearC_m);
		
		column_m.addSeparator();

		JMenuItem copyC_m = new JMenuItem("Copy Column");
		copyC_m.addActionListener(this);
		copyC_m.setActionCommand("copycolumn");
		column_m.add(copyC_m);

		JMenuItem pasteC_m = new JMenuItem("Paste Column");
		pasteC_m.addActionListener(this);
		pasteC_m.setActionCommand("pastecolumn");
		column_m.add(pasteC_m);

		JMenuItem renameC_m = new JMenuItem("Rename Column");
		renameC_m.addActionListener(this);
		renameC_m.setActionCommand("renamecolumn");
		column_m.add(renameC_m);

		JMenuItem populateC_m = new JMenuItem("Populate Column");
		populateC_m.addActionListener(this);
		populateC_m.setActionCommand("populatecolumn");
		column_m.add(populateC_m);
		
		JMenuItem maskC_m = new JMenuItem("Mask Column");
		maskC_m.addActionListener(this);
		maskC_m.setActionCommand("maskcolumn");
		column_m.add(maskC_m);
		
		JMenuItem splitC_m = new JMenuItem("Split Column into Rows");
		splitC_m.addActionListener(this);
		splitC_m.setActionCommand("splitcolumn");
		column_m.add(splitC_m);
		column_m.addSeparator();

		JMenuItem searC_m = new JMenuItem("Standardisation Regex");
		searC_m.addActionListener(this);
		searC_m.setActionCommand("seareplace");
		column_m.add(searC_m);
		
		JMenuItem stdfuzzy_m = new JMenuItem("Standardisation Fuzzy");
		stdfuzzy_m.addActionListener(this);
		stdfuzzy_m.setActionCommand("seareplacefuzzy");
		column_m.add(stdfuzzy_m);

		JMenuItem replaceC_m = new JMenuItem("Replace Null");
		replaceC_m.addActionListener(this);
		replaceC_m.setActionCommand("replace");
		column_m.add(replaceC_m);
		column_m.addSeparator();

		JMenu caseFormatC_m = new JMenu("Case Format");
		column_m.add(caseFormatC_m);

		JMenuItem upperC_m = new JMenuItem("UPPER CASE");
		upperC_m.addActionListener(this);
		upperC_m.setActionCommand("uppercase");
		caseFormatC_m.add(upperC_m);

		JMenuItem lowerC_m = new JMenuItem("lower case");
		lowerC_m.addActionListener(this);
		lowerC_m.setActionCommand("lowercase");
		caseFormatC_m.add(lowerC_m);

		JMenuItem titleC_m = new JMenuItem("Title Case");
		titleC_m.addActionListener(this);
		titleC_m.setActionCommand("titlecase");
		caseFormatC_m.add(titleC_m);

		JMenuItem sentenceC_m = new JMenuItem("Sentence case");
		sentenceC_m.addActionListener(this);
		sentenceC_m.setActionCommand("sentencecase");
		caseFormatC_m.add(sentenceC_m);
		column_m.addSeparator();
		
		JMenuItem patternC_m = new JMenuItem("Pattern Info");
		patternC_m.addActionListener(this);
		patternC_m.setActionCommand("patterninfo");
		column_m.add(patternC_m);
		column_m.addSeparator();
		
		JMenuItem piiC_m = new JMenuItem("Personally Identifiable Info");
		piiC_m.addActionListener(this);
		piiC_m.setActionCommand("piinfo");
		column_m.add(piiC_m);

		//Options menu
		JMenuItem addR_m = new JMenuItem("Insert Rows");
		addR_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				InputEvent.ALT_MASK));
		addR_m.addActionListener(this);
		addR_m.setActionCommand("addrow");
		option_m.add(addR_m);

		JMenuItem removeR_m = new JMenuItem("Delete Rows");
		removeR_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				InputEvent.ALT_MASK));
		removeR_m.addActionListener(this);
		removeR_m.setActionCommand("deleterow");
		option_m.add(removeR_m);
		option_m.addSeparator();

		JMenuItem subset_m = new JMenuItem("Subset Table");
		subset_m.addActionListener(this);
		subset_m.setActionCommand("subsettable");
		option_m.add(subset_m);
		option_m.addSeparator();
		
		JMenuItem splitt_m = new JMenuItem("Split Table");
		splitt_m.addActionListener(this);
		splitt_m.setActionCommand("splittable");
		option_m.add(splitt_m);
		option_m.addSeparator();
		
		JMenuItem sampleset_m = new JMenuItem("Random Sample Table");
		sampleset_m.addActionListener(this);
		sampleset_m.setActionCommand("randomtable");
		option_m.add(sampleset_m);
		option_m.addSeparator();
		

		JMenuItem transR_m = new JMenuItem("Transpose Rows");
		transR_m.addActionListener(this);
		transR_m.setActionCommand("transrow");
		option_m.add(transR_m);
		option_m.addSeparator();

		JMenu lookup_m = new JMenu("Join Lookup File");
		
		JMenuItem lookupr_m = new JMenuItem("Replace Value");
		lookupr_m.addActionListener(this);
		lookupr_m.setActionCommand("lookup");
		lookup_m.add(lookupr_m);
		
		JMenuItem lookupa_m = new JMenuItem("Add Column");
		lookupa_m.addActionListener(this);
		lookupa_m.setActionCommand("lookupadd");
		lookup_m.add(lookupa_m);
		
		option_m.add(lookup_m);
		option_m.addSeparator();
		
		JMenuItem diff_m = new JMenuItem("Diff Join");
		diff_m.addActionListener(this);
		diff_m.setActionCommand("diffjoin");
		option_m.add(diff_m);
		
		JMenuItem join_m = new JMenuItem("Left Outer Join");
		join_m.addActionListener(this);
		join_m.setActionCommand("joinfile");
		option_m.add(join_m);
		
		JMenuItem inner_m = new JMenuItem("Inner Join");
		inner_m.addActionListener(this);
		inner_m.setActionCommand("innerjoin");
		option_m.add(inner_m);
		
		JMenuItem cart_m = new JMenuItem("Cartesian Join");
		cart_m.addActionListener(this);
		cart_m.setActionCommand("cartjoin");
		option_m.add(cart_m);
		
		option_m.addSeparator();

		JMenuItem loadR_m = new JMenuItem("Load File into Rows");
		loadR_m.addActionListener(this);
		loadR_m.setActionCommand("filerow");
		option_m.add(loadR_m);

		JMenuItem loadC_m = new JMenuItem("Load File into Columns");
		loadC_m.addActionListener(this);
		loadC_m.setActionCommand("filecol");
		option_m.add(loadC_m);
		option_m.addSeparator();

		JMenuItem formatC_m = new JMenuItem("Format");
		formatC_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				InputEvent.ALT_MASK));
		formatC_m.addActionListener(this);
		formatC_m.setActionCommand("format");
		option_m.add(formatC_m);
		option_m.addSeparator();
		
		JMenuItem fillInfo_m = new JMenuItem("Table Completeness Info");
		fillInfo_m.addActionListener(this);
		fillInfo_m.setActionCommand("fillInfo");
		option_m.add(fillInfo_m);

		JMenuItem profile_m = new JMenuItem("Profile");
		profile_m.addActionListener(this);
		profile_m.setActionCommand("profile");
		option_m.add(profile_m);
		
		JMenuItem analyseC_m = new JMenuItem("Analyse");
		analyseC_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				InputEvent.ALT_MASK));
		analyseC_m.addActionListener(this);
		analyseC_m.setActionCommand("analyse");
		option_m.add(analyseC_m);

		JMenuItem analyseS_m = new JMenuItem("Analyse Selected");
		analyseS_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				InputEvent.ALT_MASK));
		analyseS_m.addActionListener(this);
		analyseS_m.setActionCommand("analyseselected");
		option_m.add(analyseS_m);
		option_m.addSeparator();

		JMenuItem createC_m = new JMenuItem("Create Condition");
		createC_m.addActionListener(this);
		createC_m.setActionCommand("createcond");
		option_m.add(createC_m);

		JMenuItem undoC_m = new JMenuItem("Undo Condition");
		undoC_m.addActionListener(this);
		undoC_m.setActionCommand("undocond");
		option_m.add(undoC_m);
		option_m.addSeparator();
		
		JMenuItem dedupC_m = new JMenuItem("DeDup");
		dedupC_m.addActionListener(this);
		dedupC_m.setActionCommand("dedup");
		option_m.add(dedupC_m);

		JMenu dedup = new JMenu("Fuzzy DeDup");
		JMenuItem similarC_m = new JMenuItem("Delete");
		similarC_m.addActionListener(this);
		similarC_m.setActionCommand("simcheck");
		dedup.add(similarC_m);
		
		JMenuItem replaceSimC_m = new JMenuItem("Replace");
		replaceSimC_m.addActionListener(this);
		replaceSimC_m.setActionCommand("simreplace");
		dedup.add(replaceSimC_m);
		option_m.add(dedup);
		option_m.addSeparator();
		
		JMenuItem crossCol_m = new JMenuItem("Cross Column Search");
		crossCol_m.addActionListener(this);
		crossCol_m.setActionCommand("crosscol");
		option_m.add(crossCol_m);
		option_m.addSeparator();

		JMenu discreetC_m = new JMenu("Discreet Range Check");
		option_m.add(discreetC_m);

		JMenuItem matchdiscreetC_m = new JMenuItem("Match");
		matchdiscreetC_m.addActionListener(this);
		matchdiscreetC_m.setActionCommand("matchdiscreetrange");
		discreetC_m.add(matchdiscreetC_m);
		JMenuItem nomatchdiscreetC_m = new JMenuItem("No Match");
		nomatchdiscreetC_m.addActionListener(this);
		nomatchdiscreetC_m.setActionCommand("nomatchdiscreetrange");
		discreetC_m.add(nomatchdiscreetC_m);
		option_m.addSeparator();

		JMenuItem loadDB_m = new JMenuItem("Load to DB");
		loadDB_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				InputEvent.ALT_MASK));
		loadDB_m.addActionListener(this);
		loadDB_m.setActionCommand("todb");
		option_m.add(loadDB_m);

		JMenuItem syncDB_m = new JMenuItem("Synch From DB");
		syncDB_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.ALT_MASK));
		syncDB_m.addActionListener(this);
		syncDB_m.setActionCommand("fromdb");
		option_m.add(syncDB_m);

		frame.pack();
		frame.setVisible(true);

	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (_rt.isSorting() || _rt.table.isEditing()) {
			JOptionPane.showMessageDialog(null, "Table is in Sorting or Editing State");
			return;
		}
		try {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			if (command.equals("barchart")) {
				 new FileAnalyticsListener(_rt, 1);
				return;
			}
			if (command.equals("piechart")) {
				new FileAnalyticsListener(_rt, 2);
				return;
			}
			if (command.equals("hbarchart")) {
				  new FileAnalyticsListener(_rt, 3);
				return;
			}
			if (command.equals("linechart")) {
				 new FileAnalyticsListener(_rt, 4);
				return;
			}
			if (command.equals("report")) {
				 new FileAnalyticsListener(_rt, 5);
				return;
			}
			if (command.equals("location")) {
				 new FileAnalyticsListener(_rt, 6);
				return;
			}
			if (command.equals("crosstab")) {
				 new FileAnalyticsListener(_rt, 7);
				return;
			}
			// Outlier
			if (command.equals("bynumber")) {
				 new FileAnalyticsListener(_rt, 8);
				return;
			}
			if (command.equals("bypercent")) {
				 new FileAnalyticsListener(_rt, 9);
				return;
			}
			if (command.equals("bystddev")) {
				 new FileAnalyticsListener(_rt, 10);
				return;
			}
			if (command.equals("boxplot")) {
				 new FileAnalyticsListener(_rt, 11);
				return;
			}
			// End outlier
			if (command.equals("kmean")) {
				 new FileAnalyticsListener(_rt, 12);
				return;
			}
			if (command.equals("timeseries")) {
				 new FileAnalyticsListener(_rt, 13);
				return;
			}
			if (command.equals("regression")) {
				 new FileAnalyticsListener(_rt, 14);
				return;
			}
			if (command.equals("timeliness")) {
				 new FileAnalyticsListener(_rt, 15);
				return;
			}
			if (command.equals("stringlen")) {
				 new FileAnalyticsListener(_rt, 16);
				return;
			}
			if (command.equals("timefore")) {
				 new FileAnalyticsListener(_rt, 17);
				return;
			}
			if (command.equals("timeregression")) {
				 new FileAnalyticsListener(_rt, 18);
				return;
			}
			if (command.equals("mfsearch")){
				new MultifacetPanel(_rt);
				return;
			}
			if (command.equals("pcorrelation")) {
				_rt.cancelSorting();
				int index_a = selectedColIndex(_rt,"Select First Column");
				if (index_a < 0)
					return;
				int index_b = selectedColIndex(_rt,"Select Second Column");
				if (index_b < 0)
					return;
				Vector<Double> inputData_a = _rt.getRTMModel().getColDataVD(index_a);
				Vector<Double> inputData_b = _rt.getRTMModel().getColDataVD(index_b);
				double corr = AggrCumRTM.getPCorrelation(inputData_a, inputData_b);
				if (Double.isNaN(corr) == true)
					JOptionPane.showMessageDialog(null, "Could not get Correlation for dataset");
				else
					JOptionPane.showMessageDialog(null, "Corrleation is:" + corr);
				
				return;
			}
			if (command.equals("undocond")) {
				if (delete_v == null || delrow_v == null) {
					JOptionPane.showMessageDialog(null, "Condition is not Set");
					return;
				}
				for (int i = (delete_v.size() - 1); i >= 0; i--) {
					_rt.addRows(delete_v.get(i), 1);
					_rt.pasteRow(delete_v.get(i), delrow_v.get(i));
				}
				delrow_v.clear();
				delrow_v = null;
				delete_v.clear();
				delete_v = null;
			}
			if (command.equals("createcond")) {
				Vector[] vector1 = new Vector[2];
				vector1[0] = new Vector<String>();
				vector1[1] = new Vector<String>();
				for (int i = 0; i < _rt.table.getColumnCount(); i++) {
					vector1[0].add(_rt.table.getColumnName(i));
					vector1[1].add(_rt.table.getColumnClass(i).getName());
				}
				FileQueryDialog fqd = new FileQueryDialog(2, _fileN, vector1);
				fqd.setReportTable(_rt);
				fqd.setLocation(175, 100);
				fqd.setTitle(_fileN + " Query Dialog");
				fqd.setModal(true);
				fqd.pack();
				fqd.setVisible(true);
				int j = fqd.response;
				if (j == 1) {
					_rt = fqd._rt;
					if (delete_v == null)
						delete_v = new Vector<Integer>();
					if (delrow_v == null)
						delrow_v = new Vector<Object[]>();
					for (int i = 0; i < fqd.delete_v.size()
							&& i < fqd.delrow_v.size(); i++) {
						delete_v.add(fqd.delete_v.get(i));
						delrow_v.add(fqd.delrow_v.get(i));
					}
					revalidate();
					repaint();
				}
				return;
			}
			if (command.compareTo("addcolumn") == 0) {
				String input = (String) JOptionPane.showInputDialog(null,
						"Column to Add", "Column Add Dialog",
						JOptionPane.PLAIN_MESSAGE, null, null, "Column_"
								+ (_rt.table.getColumnCount() + 1));
				if (input == null || input.equals(""))
					return;
				 _rt.getModel().addColumn(input);
				 _rt.getModel().fireTableStructureChanged();
				 _rt.table.setAutoCreateColumnsFromModel(true);
				 _rt.table.addNotify();
				return;
			}
			if (command.equals("hidecolumn")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				_rt.hideColumn(index);
				return;
			}
			if (command.equals("clearcolumn")) {
				int n = JOptionPane.showConfirmDialog(
								null,"It will clear Column Data.\n Do you wish to Continue ?",
								"Clear Column Dialog",JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION)
					return;
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				int row_c = _rt.table.getRowCount();
				for (int i = 0; i < row_c; i++) {
					_rt.table.setValueAt(null,i, index); // set to null
				}
				return;
			}
			if (command.equals("renamecolumn")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				String name = JOptionPane.showInputDialog("Column Name??",
						"New_Name");
				if (name == null || "".equals(name))
					return;
				
				//change table model name setColumIdentifier
				Object[] colN = _rt.getAllColName();
				colN[index] = name;
				_rt.getModel().setColumnIdentifiers(colN);
				return;
			}
			if (command.equals("copycolumn")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				int row_c = _rt.table.getRowCount();
				copy_v = new Vector<Object>();
				for (int i = 0; i < row_c; i++) {
					copy_v.addElement(_rt.table.getValueAt(i, index));
				}
				JOptionPane.showMessageDialog(null, copy_v.size()
						+ " objects copied", "Information Message",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (command.equals("pastecolumn")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				if (copy_v == null || copy_v.size() == 0) {
					JOptionPane.showMessageDialog(null,
							"Nothing to Paste \n Copy a Column First",
							"Information Message",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				int row_c = _rt.table.getRowCount();
				int vec_c = copy_v.size();
				for (int i = 0; (i < row_c) && (i < vec_c); i++) {
					_rt.table.setValueAt(copy_v.get(i), i, index);
				}

				return;
			}
			if (command.equals("maskcolumn")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				MaskingPanel msp = new MaskingPanel(_rt,index);
				msp.createDialog();
				
				return;
			}
			if (command.equals("splitcolumn")) {
				_rt.cancelSorting();
				_rt.table.clearSelection();
				
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				
				new SplitColumnPanel(_rt,index);
				return;
			}
			if (command.equals("populatecolumn")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				int row_c = _rt.table.getRowCount();

				String[] popOption = new String[] { "Auto Incremental",
						"Expression Builder", "Random Generation", "Data Explosion","Grouping - Number & Date",
						"Utility Functions"};
				String input = (String) JOptionPane.showInputDialog(null,
						"Choose population Type", "Select population Type",
						JOptionPane.INFORMATION_MESSAGE, null, popOption,
						popOption[0]);
				if (input == null || "".equals(input))
					return;
				if ("Auto Incremental".equals(input)) {
					String start = "";
					int i = 0;
					while ("".equals(start)) {
						start = JOptionPane.showInputDialog(
								"Choose Starting Number", 1);
						if (start == null)
							return;
						try {
							i = Integer.parseInt(start);
						} catch (NumberFormatException ne) {
							start = "";
							continue;
						}
					}
					for (int j = 0; j < row_c; j++) {
						_rt.table.setValueAt(i++, j, index);
					}
				} else if ("Expression Builder".equals(input)) {
					new ExpressionBuilderPanel(_rt,index);
					
				} else if ("Data Explosion".equals(input)) {
					 new DataExplosionPanel(_rt,index);
					
				} else if ("Grouping - Number & Date".equals(input)) {
					new GroupingPanel(_rt,index);
				} else if ("Utility Functions".equals(input) ) {
					new UtilFunctionPanel(_rt,index);
				}
				else {
					/* Random value generator */
					 new RandomColGenPanel(_rt, index);
				}
				return;
			}
			if (command.equals("replace")) {
				if (_rt.isSorting() || _rt.table.isEditing()) {
					JOptionPane.showMessageDialog(null, "Table is in Sorting or Editing State");
					return;
				}
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				String input = JOptionPane
						.showInputDialog("Replace Null with: \n For Date Object Format is dd-MM-yyyy");
				if (input == null || "".equals(input))
					return;

				Object replace = null;
				Class<?> cclass = _rt.table.getColumnClass(index);
				try {
					if (cclass.getName().toUpperCase().contains("DOUBLE")) {
						replace = Double.parseDouble(input);
					} else if (cclass.getName().toUpperCase().contains("DATE")) {
						replace = new SimpleDateFormat("dd-MM-yyyy")
								.parse(input);
					} else {
						replace = new String(input);
					}
				} catch (Exception exp) {
					ConsoleFrame
							.addText("\n WANING: Could not Parse Input String:"
									+ input);
				}
				int row_c = _rt.table.getRowCount();
				_rt.cancelSorting();
				_rt.table.clearSelection();
				_rt.table.setColumnSelectionInterval(index, index);

				for (int i = 0; i < row_c; i++) {
					Object obj = _rt.getModel().getValueAt(i, index);
					if (obj == null || "".equals(obj.toString())
							|| obj.toString().compareToIgnoreCase("null") == 0) {
						_rt.getModel().setValueAt(replace, i, index);
						_rt.table.addRowSelectionInterval(i, i);
					}
				}
				return;
			}

			if (command.equals("seareplace")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				Hashtable<String,String> filterHash = null;
				SearchOptionDialog sd = new SearchOptionDialog();
				File f = sd.getFile();
				if (f == null) return;
				
				ConsoleFrame.addText("\n Selected File:" + f.toString());
				filterHash = KeyValueParser.parseFile(f.toString());
				
				String options = sd.getSelectedOption();
		
				int row_c = _rt.table.getRowCount();
				_rt.cancelSorting();
				_rt.table.clearSelection();
				_rt.table.setColumnSelectionInterval(index, index);

				// Take care of class type
				Object replace = null;
				Class<?> cclass = _rt.table.getColumnClass(index);

				Enumeration<String> en = filterHash.keys();
				while (en.hasMoreElements()) {
					String key = en.nextElement().toString();
					Pattern p= null;
				/* We will compile the pattern here so that once compile it can match all rows.*/
				try {
					if (options.charAt(0) == '0' && options.charAt(2) == '1' )  // case insensitive and literal true
						p =Pattern.compile(key, Pattern.LITERAL|Pattern.CASE_INSENSITIVE);
					else if ( options.charAt(0) == '1' && options.charAt(2) == '1' ) // case sensitive and literal true
						p = Pattern.compile(key, Pattern.LITERAL);
					else if (options.charAt(0) == '0' && options.charAt(2) == '0') // case insensitive and literal false
						p = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
					else // case sensitive and literal false
						p = Pattern.compile(key); // no flag
					
				} catch (PatternSyntaxException pe) {
					System.out.println("Pattern Compile Exception:"
						+ pe.getMessage());
					continue;
				} catch (IllegalArgumentException ee) {
					System.out.println("Illegal Argument Exception:"
							+ ee.getMessage());
						continue;
				}
				if (p== null ) {
					System.out.println("Pattern is NULL");
						continue;
				}
				
				for (int i = 0; i < row_c; i++) {
					Object obj = _rt.table.getValueAt(i, index);
					if (obj == null)
						continue;
					
					String value = obj.toString().trim()
							.replaceAll("\\s+", " "); // Split for White Space
					
					String[] valueTok = new String[1];
					valueTok[0] = value;
					
					if (options.charAt(1) == '0' ) // multi-word not chosen
						 valueTok = value.split(" ");
					
						boolean matchFound = false;
						
						for (int j = 0; j < valueTok.length; j++) {
							try {
								// needs to add case sensitive and word search replace
							Matcher m = p.matcher(valueTok[j]);
							if (options.charAt(3) == '1' ){ // Full sequence match
								if (m.matches() == true ) {
									String newvalue = (String) filterHash
											.get(key);
									valueTok[j] = newvalue;
									matchFound = true;
									continue;
								}
							} else { // find
								if (m.find() == true) {
									String newvalue = (String) filterHash.get(key);
									newvalue = m.replaceAll(newvalue);
									valueTok[j] = newvalue;
									matchFound = true;
									continue;
								}
								
							}
							} catch (PatternSyntaxException pe) {
								ConsoleFrame.addText("\n Pattern Compile Exception:"+ pe.getMessage());
								break;
							}
						}
						if (matchFound == true) {
							String newValue = "";
							for (int j = 0; j < valueTok.length; j++) {
								if (newValue.equals("") == false)
									newValue += " "; // Put space
								newValue += valueTok[j];
							}
							try {
								if (cclass.getName().toUpperCase()
										.contains("DOUBLE")) {
									replace = Double.parseDouble(newValue);
								} else if (cclass.getName().toUpperCase()
										.contains("DATE")) {
									replace = new SimpleDateFormat("dd-MM-yyyy")
											.parse(newValue);
								} else {
									replace = new String(newValue);
								}
							} catch (Exception exp) {
								ConsoleFrame
										.addText("\n WANING: Could not Parse Input String:"
												+ newValue);
							}
							_rt.table.setValueAt(replace, i, index);
							_rt.table.addRowSelectionInterval(i, i);
						}
					}
				}
				return;
			}
			if (command.equals("seareplacefuzzy")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				Hashtable<String,String> filterHash = null;
				SearchOptionDialog sd = new SearchOptionDialog(true);
				File f = sd.getFile();
				if (f == null) return;
				
				ConsoleFrame.addText("\n Selected File:" + f.toString());
				filterHash = KeyValueParser.parseFile(f.toString());
				
				_rt.cancelSorting();
				_rt.table.clearSelection();
				_rt.table.setColumnSelectionInterval(index, index);

				// Take care of class type
				Object replace = null;
				Class<?> cclass = _rt.table.getColumnClass(index);
				String colTitle = _rt.table.getColumnName(index);
				
				/* Build the index here and search in that index */
				SimilarityCheckLucene _simcheck = new SimilarityCheckLucene(_rt.getRTMModel());
				_simcheck.makeIndex();

				Enumeration<String> en = filterHash.keys();
				while (en.hasMoreElements()) {
					String key = en.nextElement().toString();
					
					if (key == null || "".equals(key) ) {
						System.out.println("Key is NULL");
							continue;
					}
					String fuzzyquery =  _simcheck.prepareLQuery(key, colTitle);
					
					Object[][] matchedrow = _simcheck.searchTableObject(fuzzyquery);
					if (matchedrow == null) continue;
					
					Vector<Integer> matchedIndex = _simcheck.getMatchedRowIndex(); // matched Index for this set
					if (matchedIndex == null || matchedIndex.size() == 0) continue;
					
					for (int i=0; i < matchedrow.length ; i++) {
						int matchedIndexVal = matchedIndex.get(i);
						
						String newValue = filterHash.get(key);
						if (newValue == null) break; // No value to replace
							try {
								if (cclass.getName().toUpperCase()
										.contains("DOUBLE")) {
									replace = Double.parseDouble(newValue);
								} else if (cclass.getName().toUpperCase()
										.contains("DATE")) {
									replace = new SimpleDateFormat("dd-MM-yyyy")
											.parse(newValue);
								} else {
									replace = new String(newValue);
								}
							} catch (Exception exp) {
								ConsoleFrame
										.addText("\n WANING: Could not Parse Input String:"
												+ newValue);
							}
							_rt.table.setValueAt(replace, matchedIndexVal, index);
							_rt.table.addRowSelectionInterval(matchedIndexVal, matchedIndexVal);
						}
				}
				return;
			}
			if (command.equals("uppercase")) {
				caseFormat(1);
				return;
			}
			if (command.equals("lowercase")) {
				caseFormat(2);
				return;
			}
			if (command.equals("titlecase")) {
				caseFormat(3);
				return;
			}
			if (command.equals("sentencecase")) {
				caseFormat(4);
				return;
			}
			if (command.equals("piinfo")) {
				// Create the Column name into hashtable
				Hashtable<String,String>__h = KeyValueParser.parseFile("resource/piiSearch.txt");
				if ( __h == null) {
					JOptionPane.showMessageDialog(null, " No Table found");
					return;
				}
				// Create MetaData Matcher
				MetadataMatcher mm = new MetadataMatcher(__h);
				String[] colName = new String[] {"Column","PIIGroup","Confidence",
						"field1","field2","field3","field4","field5","field6","field7","field8","field9","field10"};
				ReportTableModel rtm__ = new ReportTableModel(colName,true,true);
				
					
				String[] colNames = _rt.getAllColNameAsString();
				if (colNames == null ) {
					JOptionPane.showMessageDialog(null,  "No Column found" );
					return;
				}
				Hashtable<String, Vector<String>> ht = mm.matchedKeys(colNames,0.8f);
				
				for (Enumeration<String> e1 = ht.keys(); e1.hasMoreElements();) {
					String key = e1.nextElement();
					Vector<String>piiv= ht.get(key);
					int size = piiv.size();
					// System.out.println("---  Column:" + key + " Value:" + ht.get(key) + "Size:" + size);
					for (int j = 0; j < size; j++) {
						Object[] row = new Object[10+3];
						Object[] colD = new Object[10];
						colD = mm.getPIIColData(_rt.getRTMModel(), key, piiv.get(j), colD);
						String confidence = mm.getConfidenceLevel();
						row[0]= key;row[1]= piiv.get(j);row[2]= confidence;
						for (int z=0; z < colD.length; z++) 
							row[z+3] = colD[z];
						rtm__.addFillRow(row);
					}
					
				} // end of column 
				
				ReportTable rt__ = new ReportTable(rtm__);
				JDialog jd = new JDialog();
				jd.setTitle("Personally Identifiable Info");
				jd.setLocation(150, 150);
				jd.getContentPane().add(rt__);
				jd.setModal(true);
				jd.pack();
				jd.setVisible(true);
				
				return;
			}
			
			if (command.equals("patterninfo")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				String[] colHeader = new String[]{"Value","Count"};
				ReportTable rt = new ReportTable(colHeader);
				Vector<Object> colV = _rt.getRTMModel().getColDataV(index);
				Hashtable<Object,Integer> htable = DiscreetRange.getUniqueInclusive(colV);
				Hashtable<Object,Integer> htableP = new FileProfile().showPattern(htable);
				int rowI = 0;
				
				for (Enumeration<Object> e1 = htableP.keys(); e1.hasMoreElements();) {
					rt.addRow();
					Object key = e1.nextElement();
					if (key.toString().equals("Null-Arrah") == true )
						rt.table.setValueAt("Null", rowI, 0);
					else
						rt.table.setValueAt(key.toString(), rowI, 0);
					Object val = htableP.get(key);
					rt.table.setValueAt(val.toString(), rowI, 1);
					rowI++;
				}
				
				JDialog jd = new JDialog();
				jd.setTitle("Pattern Info");
				jd.setLocation(150, 150);
				jd.getContentPane().add(rt);
				jd.setModal(true);
				jd.pack();
				jd.setVisible(true);
				return;
			}
			if (command.equals("fillInfo")) {
				double tabCount = _rt.getModel().getRowCount();
				int[] fillCount = FillCheck.getEmptyCount(_rt.getRTMModel());
				
				ReportTable newRTFill = new ReportTable(new String[]{"Empty Column #","Count","Percentage"}, false, false);
				
				if ( (tabCount > 0) && (fillCount.length > 0) )
				for (int i=0; i <fillCount.length; i++ ) {
					newRTFill.addFillRow(new String[] {""+i+" Empty Column",
							((Integer)(fillCount[i])).toString(),((Double)((fillCount[i]/tabCount)*100)).toString() });
				}
				JDialog d_fill = new JDialog();
				d_fill.setModal(true);
				d_fill.setTitle("Table Fill Dialog");
				d_fill.setLocation(250, 250);
				d_fill.getContentPane().add(newRTFill);
				d_fill.pack();
				d_fill.setVisible(true);
				
				return;
			}
			

			if (command.equals("format")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;

				int row_c = _rt.table.getRowCount();
				_rt.cancelSorting(); // make sure they are in original order

				FormatPatternPanel fp = new FormatPatternPanel(
						_rt.table.getColumnName(index));
				int response = fp.createDialog();
				if (response == 0) // cancel is clicked
					return;
				String type = fp.getType();
				Object[] pattern = fp.inputPatterns();
				if (pattern.length == 0)
					return; // Nothing to parse
				
				String tid = "";
				if (type.equals("Date")) { // get Time Zone Info
					String[] allid = TimeZone.getAvailableIDs();
					String deftz = TimeZone.getDefault().getID();
					
					String[] newV = new String[allid.length +1];
					newV[0] = deftz;
					for (int i=1; i < newV.length; i++)
						newV[i] = allid[i-1];
					
					 tid = JOptionPane.showInputDialog(null, "Choose Time Zone", "Time Zone Selection", 
							JOptionPane.INFORMATION_MESSAGE, null, newV, newV[0]).toString();
				}
				// Set Display on first pattern
				_rt.table.getColumnModel().getColumn(index).setCellRenderer(
								new MyCellRenderer(pattern[0].toString(),tid));
				_rt.setPrerenderCol(index);

				Object v = null;
				if (type.equals("Number")) {
					DefaultTableModel model = _rt.getRTMModel().getModel();
					for (int i = 0; i < row_c; i++) {
						Object o = model.getValueAt(i, index);
						if (o != null)
							v = FormatCheck.parseNumber(o, pattern);
						else
							v = (Double) null;
						//_rt.setTableValueAt(v, i, index);
						model.setValueAt(v, i, index);
					}
					return;
				} // end of Number
				else if (type.equals("Date")) {
					DefaultTableModel model = _rt.getRTMModel().getModel();
					for (int i = 0; i < row_c; i++) {
						Object o = model.getValueAt(i, index);
						if (o != null)
							v = FormatCheck.parseDate(o, pattern);
						else
							v = (Date) null;
						model.setValueAt(v, i, index);
					}
					return;
				} // end of Date
				else {
					try {
						_rt.table.getColumnModel().getColumn(index).setCellEditor(
							new DefaultCellEditor( new JFormattedTextField( new MaskFormatter(pattern[0].toString()))));
						
						for (int i = 0; i < row_c; i++) {
							String o = _rt.getTextValueAt(i, index);
							if (o != null)
								v = FormatCheck.parseString(o, pattern);
							else
								v = (String) null;
							if (v == null)
								_rt.getModel().setValueAt(v, i, index);
							else {
								StringBuffer t;
								if (type.equals("Phone"))
									t = FormatCheck.phoneFormat(v.toString(),
											pattern[0].toString());
								else
									t = FormatCheck.toFormat(v.toString(),
											pattern[0].toString());

								_rt.getModel().setValueAt(t.toString(), i, index);
							}
						}
					} catch (Exception e_parse) {
						ConsoleFrame
								.addText("\n ERROR:Parsing Exception happened");
						JOptionPane.showMessageDialog(null,
								"Parsing Exception:" + e_parse.getMessage(),
								"Error Message", JOptionPane.ERROR_MESSAGE);
					}
				} // end of else - string

				return;
			}
			if (command.equals("addrow")) {
				actionType = ADDITION;
				inputDialog();
				return;
			}
			if (command.equals("ok")) {
				d_f.dispose();
				ok_action(actionType);
				return;
			}
			if (command.equals("cancel")) {
				d_f.dispose();
				return;
			}
			if (command.equals("mcancel")) {
				init = false;
				d_m.dispose();
				return;
			}
			if (command.equals("deleterow")) {
				actionType = DELETION;
				inputDialog();
				return;
			}
			if (command.equals("profile")) {
				int colC = _rt.getModel().getColumnCount();
				String[] colHeader = new String[]{"Column","Total","Unique","Pattern","Null"};
				ReportTable rt = new ReportTable(colHeader);
				Object[] colName = _rt.getRTMModel().getAllColName();
				for (int i=0; i < colC; i++) {
					rt.addRow();
					Vector<Object> colV = _rt.getRTMModel().getColDataV(i);
					Hashtable<Object,Integer> htable = DiscreetRange.getUniqueInclusive(colV);
					Integer[] val = new FileProfile().getProfiledValue(htable);
					rt.table.setValueAt(colName[i].toString(), i, 0); // First Col Name
					for (int j=1; j < 5; j++)
						rt.table.setValueAt(val[j -1].toString(), i, j); 
				}
				JDialog jd = new JDialog();
				jd.setTitle("Profile Info");
				jd.setLocation(150, 150);
				jd.getContentPane().add(rt);
				jd.setModal(true);
				jd.pack();
				jd.setVisible(true);
				
				return;
			}
			if (command.equals("analyse")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				Object[] colObj = getColObject(index);
				StatisticalAnalysisPanel fp = new StatisticalAnalysisPanel(
						colObj);
				fp.createAndShowGUI();
				return;
			}
			if (command.equals("analyseselected")) {
				Object[] colObj = getSelectedColObject();
				if (colObj == null)
					return;
				StatisticalAnalysisPanel fp = new StatisticalAnalysisPanel(
						colObj);
				fp.createAndShowGUI();
				return;
			}
			if (command.equals("dedup")) {
				new NewTableDialog(_rt.getRTMModel(),true);
				 return;
			}
			if (command.equals("simcheck")) {
				_rt.cancelSorting();
				new SimilarityCheckPanel(_rt,true);
				return;
			}
			if (command.equals("simreplace")) {
				_rt.cancelSorting();
				new SimilarityCheckPanel(_rt,false);
				return;
			}
			if (command.equals("crosscol")) {
				_rt.cancelSorting();
				new CrossColumnPanel(_rt);
				return;
			}
			if (command.equals("todb")) {
				mapDialog(true);
				return;
			}
			if (command.equals("fromdb")) {
				mapDialog(false);
				return;
			}
			if (command.equals("load")) {
				String[] query = getQString(true);
				if (query == null || query.length == 0)
					return;
				loadQuery(query);
				return;
			}
			if (command.equals("synch")) {
				String[] query = getQString(false);
				if (query == null || query.length == 0)
					return;
				synchQuery(query);
				return;
			}
			if (command.equals("hiveload")) {
				if ( runHiveLoad() == true )
					if (d_m != null)
						d_m.dispose();
				return;
			}
			if (command.equals("tableinfo")) {
				 showHiveTableInfo();
				return;
			}
			if (command.equals("filerow") || command.equals("filecol")) {
				ImportFilePanel impF = new ImportFilePanel(false);
				ReportTable rtable = impF.getTable();
				if (rtable == null)
					return;

				int colC = rtable.table.getColumnCount();
				int colCE = _rt.table.getColumnCount();
				if (command.equals("filerow")) {
					if (colC != colCE) {
						JOptionPane
								.showMessageDialog(
										null,
										"Column Count not Matching "
												+ colC
												+ " Columns and "
												+ colCE
												+ " Columns \n Will adjust Accordingly",
										"Error Message",
										JOptionPane.INFORMATION_MESSAGE);
					}
				}
				int rowC = rtable.table.getRowCount();
				int rowCE = _rt.table.getRowCount();
				if (command.equals("filerow")) {
					_rt.addRows(rowCE, rowC);
					for (int i = 0; i < colC - colCE; i++)
						_rt.getModel().addColumn(rtable.table.getColumnName(colCE + i));

					for (int i = 0; i < rowC; i++) {
						Object[] obj = rtable.copyRow(i);
						for (int j = 0; j < colC; j++) {
							_rt.table.setValueAt(obj[j], rowCE + i, j);
						}
					}
				} else {
					if (rowC > rowCE)
						_rt.addRows(rowCE, rowC - rowCE);
					for (int i = 0; i < colC; i++) {
						_rt.getModel().addColumn(rtable.table.getColumnName(i));
					}
					for (int i = 0; i < rowC; i++) {
						Object[] obj = rtable.copyRow(i);
						for (int j = 0; j < obj.length; j++) {
							_rt.table.setValueAt(obj[j], i, colCE + j);
						}
					}
				}
				return;
			}
			if (command.equals("joinfile") || command.equals("innerjoin") || command.equals("diffjoin")
					|| command.equals("cartjoin")) {
				int lindex = selectedColIndex(_rt,"Select column to Join To:");
				if (lindex < 0)
					return;
				
				JOptionPane.showMessageDialog (null, "Choose the joining file");
				
				ImportFilePanel impF = new ImportFilePanel(false);
				ReportTable rtable = impF.getTable();
				if (rtable == null)
					return;

				int rindex = selectedColIndex(rtable,"Select column to Join From:");
				if (rindex < 0)
					return;
				if( command.equals("joinfile") )
					_rt = joinTables(_rt, lindex, rtable, rindex, 0); // Left Outer Join
				else if( command.equals("innerjoin") )
					_rt = joinTables(_rt, lindex, rtable, rindex, 1); // Inner Join
				else if( command.equals("diffjoin") )
					_rt = joinTables(_rt, lindex, rtable, rindex, 2); // Diff join
				else 
					_rt = joinTables(_rt, lindex, rtable, rindex, 3); // Cartesian join
				return;
			}
			if (command.equals("lookup") || command.equals("lookupadd")) {
				_rt.cancelSorting(); // Make sure it is not in sorting order
				
				int lindex = selectedColIndex(_rt,"Select column for Lookup:");
				if (lindex < 0)
					return;
				
				JOptionPane.showMessageDialog (null, "Choose the Lookup file:");
				
				ImportFilePanel impF = new ImportFilePanel(false);
				ReportTable rtable = impF.getTable();
				if (rtable == null)
					return;

				int rindex = selectedColIndex(rtable,"Select column for Lookup Join:");
				if (rindex < 0)
					return;
				
				int rinfo = selectedColIndex(rtable,"Select column for Lookup Description:");
				if (rinfo < 0)
					return;
				
				int rowc = _rt.getModel().getRowCount();
				int colc = _rt.getModel().getColumnCount();
				
				if ( command.equals("lookupadd") ) {
					_rt.getRTMModel().addColumn("LookupValue");
				}
				
				Hashtable<Object, Object> hlookup = RTMUtil.lookupInfo(rtable.getRTMModel(), rindex, rinfo);
				
				for (int i=0; i < rowc; i++ ) {
					Object key = _rt.getModel().getValueAt(i, lindex);
					Object value = hlookup.get(key);
					if (value == null) continue;
					if ( command.equals("lookupadd") )
						_rt.getModel().setValueAt(value,i,colc);
					else
						_rt.getModel().setValueAt(value,i,lindex);
				}
				
				return;
			}
			if (command.equals("geocoding")) {
				_rt.cancelSorting(); // Make sure it is not in sorting order
				
				int lindex = selectedColIndex(_rt,"Please Select Zip/Pincode column:");
				if (lindex < 0)
					return;
				
				JOptionPane.showMessageDialog (null, "Choose the Latitude/Longitude and Zip/Pin Mapping file \n"
						+ "You can look at http://download.geonames.org/ if you don't have \n"
						+ "mapping file:");
				
				ImportFilePanel impF = new ImportFilePanel(false);
				ReportTable rtable = impF.getTable();
				if (rtable == null)
					return;

				int rindex = selectedColIndex(rtable,"Select Zip/Pin column in mapping file:");
				if (rindex < 0)
					return;
				
				int rlat = selectedColIndex(rtable,"Select Latitude Column in mapping file:");
				if (rlat < 0)
					return;
				
				int rlon = selectedColIndex(rtable,"Select Longitude Column in mapping file:");
				if (rlon < 0)
					return;
				
				int rowc = _rt.getModel().getRowCount();
				int colc = _rt.getModel().getColumnCount();
				_rt.getRTMModel().addColumn("MappedLatitude");
				_rt.getRTMModel().addColumn("MappedLongitude");
				
				Hashtable<Object, Object> hlookup = RTMUtil.lookupIndex(rtable.getRTMModel(),rindex);
				for (int i=0; i < rowc; i++ ) {
					Object key = _rt.getModel().getValueAt(i, lindex);
					Object value = hlookup.get(key);
					if (value == null) continue;
					Object lat = rtable.getModel().getValueAt((Integer)value, rlat);
					Object lon = rtable.getModel().getValueAt((Integer)value, rlon);
					_rt.getModel().setValueAt(lat,i,colc);
					_rt.getModel().setValueAt(lon,i,colc+1);
				}
				
				return;
			}
			if (command.equals("subsettable")) {
				NewTableDialog ntd=  new NewTableDialog(_rt.getRTMModel());
				ntd.displayGUI();
				 return;
			}
			if (command.equals("splittable")) {
				String splitC = JOptionPane.showInputDialog("How many tables to split into?", new Integer(10));
				if (splitC == null) return;
				try {
					int count = Integer.parseInt(splitC);
					ReportTableModel[] rtm = RTMUtil.splitRTM(_rt.getRTMModel(),count);
					
					JPanel splitPanel = new JPanel();
					//splitPanel.setPreferredSize(new Dimension(600, 400*count));
					BoxLayout boxl = new BoxLayout(splitPanel,BoxLayout.Y_AXIS);
					splitPanel.setLayout(boxl);
					
					for (int i=0; i <rtm.length; i++ ) {
						JLabel l = new JLabel("Split Table:" + (i+1));
						ReportTable newRT = new ReportTable(rtm[i]);
						splitPanel.add(l);
						splitPanel.add(newRT);
					}
					
					JScrollPane splitP = new JScrollPane(splitPanel);
					splitPanel.setPreferredSize(new Dimension(800,600*count));
					
					JDialog jd1 = new JDialog ();
					jd1.setTitle("Table Split Dialog");
					jd1.setModal(true);
					jd1.setLocation(250,100);
					jd1.setPreferredSize(new Dimension(950,900));
					jd1.getContentPane().add(splitP);
					jd1.pack();
					jd1.setVisible(true);
					
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Not Valid Input");
					return;
				} finally {
					_rt.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				}
				 return;
			}
			if (command.equals("randomtable")) {
				String randomC = JOptionPane.showInputDialog("How may random Rows??", new Integer(100));
				if (randomC == null) return;
				try {
					int count = Integer.parseInt(randomC);
					ReportTableModel rtm = RTMUtil.sampleRTM(_rt.getRTMModel(),count);
					ReportTable newRT = new ReportTable(rtm);
					JDialog jd1 = new JDialog ();
					jd1.setTitle("Table Sample Dialog");
					jd1.setModal(true);
					jd1.setLocation(250,250);
					jd1.getContentPane().add(newRT);
					jd1.pack();
					jd1.setVisible(true);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Not Valid Input");
					return;
				} finally {
					_rt.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				}
				
				 return;
			}
			if (command.equals("transrow")) {
				_rt.transposeTable();
				return;
			}
			if (command.equals("nomatchdiscreetrange")
					|| command.equals("matchdiscreetrange")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				DiscreetInputGUI dig = new DiscreetInputGUI();
				dig.createDialog();
				Vector<String> token = StringCaseFormatUtil.tokenizeText(
						dig.getRawText(), dig.getDelimiter());
				if (token == null || token.size() == 0) {
					ConsoleFrame.addText("\n No Token Processed");
					return;
				}
				int row_c = _rt.table.getRowCount();
				_rt.cancelSorting();
				_rt.table.clearSelection();
				_rt.table.setColumnSelectionInterval(index, index);

				for (int i = 0; i < row_c; i++) {
					Object obj = _rt.table.getValueAt(i, index);
					if (obj == null)
						continue;
					String value = obj.toString();
					ConsoleFrame
							.addText("\n Matched Display Value for Row index "
									+ i + " is:" + value);
					int tokenI = 0;
					boolean matchFound = false;

					while (tokenI < token.size()) {
						String key = token.elementAt(tokenI++);
						try {
							if (Pattern.matches(key, value) == true) {
								matchFound = true;
								break;
							}
						} catch (PatternSyntaxException pe) {
							ConsoleFrame
									.addText("\n Pattern Compile Exception:"
											+ pe.getMessage());
							continue;
						}
					}
					if (matchFound == true
							&& command.equals("matchdiscreetrange"))
						_rt.table.addRowSelectionInterval(i, i);
					if (matchFound == false
							&& command.equals("nomatchdiscreetrange"))
						_rt.table.addRowSelectionInterval(i, i);
				}
				return;
			}
			// Data Preparation 
			if (command.equals("testdata")) {
				new TestdataDialog(_rt.getRTMModel());
				return;
			}
			if (command.equals("ordinal")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				new OrdinalPanel(_rt,index,0);
				return;
			}
			if (command.equals("onehot")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				new OrdinalPanel(_rt,index,1); // Hot one
				return;
			}
			if (command.equals("seasonality")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				new GroupingPanel(_rt,index);
				return;
			}
			if (command.equals("monthgrouping")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				new TimeGroupingPanel(_rt,index,1); // month
				return;
			}
			if (command.equals("daygrouping")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				new TimeGroupingPanel(_rt,index,3); //day
				return;
			}
			if (command.equals("dategrouping")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				new TimeGroupingPanel(_rt,index,2); // date
				return;
			}
			if (command.equals("hourgrouping")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				new TimeGroupingPanel(_rt,index,4); // hour
				return;
			}
			if (command.equals("minutegrouping")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				new TimeGroupingPanel(_rt,index,5); // minute
				return;
			}
			if (command.equals("secondgrouping")) {
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				new TimeGroupingPanel(_rt,index,6); // second
				return;
			}
			if (command.equals("attreplace")) {
				int index = selectedColIndex(_rt,"Select Column to Replace Null Value:");
				if (index < 0)
					return;
				
				JList<String> colL = new JList<String>(_rt.getRTMModel().getAllColNameStr());
				int option = JOptionPane.showConfirmDialog(null,colL,"Choose Attribue Columns",JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.CANCEL_OPTION )
					return;
				
				_rt.table.clearSelection(); // clear selection for new selection
				_rt.table.setColumnSelectionInterval(index, index);
				
				String[] otherC = new String[colL.getSelectedValuesList().size()];
				otherC = colL.getSelectedValuesList().toArray(otherC);
				Hashtable<String, Vector<Integer>> hashT = RTMUtil.getLuceneQueryForNull(_rt.getRTMModel(), index, otherC);
				Vector<Integer> matchedI = RTMUtil.replaceNullbyAttr(_rt.getRTMModel(), index, hashT);
				
				// now highlight the selected value
				for (int i=0; i <matchedI.size(); i++)
				_rt.table.addRowSelectionInterval(matchedI.get(i), matchedI.get(i));
				
				return;
			}
			if (command.equals("nullreplace")) {
				new FileAnalyticsListener(_rt, 19);
				revalidate();
				repaint();
				return;
			}
			if (command.equals("avgreplace")) {
				_rt.cancelSorting(); // No sorting 
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				Vector<Double> colD = _rt.getRTMModel().getColDataVD(index);
				Double avg = AggrCumRTM.getAverage(colD);
				int rowc = _rt.getModel().getRowCount();
				
				for (int i=0; i < rowc; i++) {
					Object o = _rt.getModel().getValueAt(i, index);
					if (o == null || "".equals(o.toString())) {
						try {
							_rt.getModel().setValueAt(avg,i, index);
						} catch(Exception e1) {	
							_rt.getModel().setValueAt(avg.toString(),i, index);
						}
					}
				}
				
				return;
			}
			if (command.equals("zeronormal")) {
				_rt.cancelSorting(); // No sorting 
				int popindex = selectedColIndex(_rt, "Select Column to Populate:");
				if (popindex < 0)
					return;
				int outputindex = selectedColIndex(_rt, "Select Column to get Normalized values");
				if (outputindex < 0)
					return;
				
				NormalizeCol.zeroNormal(_rt.getRTMModel(), outputindex, popindex);
				return;
			}
			if (command.equals("zscore")) {
				_rt.cancelSorting(); // No sorting 
				int popindex = selectedColIndex(_rt, "Select Column to Populate:");
				if (popindex < 0)
					return;
				int outputindex = selectedColIndex(_rt, "Select Column to get Normalized values");
				if (outputindex < 0)
					return;
				
				NormalizeCol.zscoreNormal(_rt.getRTMModel(), outputindex, popindex);
				return;
			}
			if (command.equals("meandist")) {
				_rt.cancelSorting(); // No sorting 
				int popindex = selectedColIndex(_rt, "Select Column to Populate:");
				if (popindex < 0)
					return;
				int outputindex = selectedColIndex(_rt, "Select Column to get Normalized values");
				if (outputindex < 0)
					return;
				
				NormalizeCol.distStdNormal(_rt.getRTMModel(), outputindex, popindex);
				return;
			}
			if (command.equals("meanratio") || command.equals("stdratio") || command.equals("meansubstract")  ) {
				_rt.cancelSorting(); // No sorting 
				int popindex = selectedColIndex(_rt, "Select Column to Populate:");
				if (popindex < 0)
					return;
				int outputindex = selectedColIndex(_rt, "Select Column to get Normalized values");
				if (outputindex < 0)
					return;
				if (command.equals("stdratio"))
					NormalizeCol.meanStdNormal(_rt.getRTMModel(), outputindex, popindex,0);
				else if (command.equals("meanratio"))
					NormalizeCol.meanStdNormal(_rt.getRTMModel(), outputindex, popindex,1);
				else
					NormalizeCol.meanStdNormal(_rt.getRTMModel(), outputindex, popindex,2);
				return;
			}
			if (command.equals("rounding") || command.equals("flooring") || command.equals("ceiling")
					|| command.equals("nearestzero")) {
				_rt.cancelSorting(); // No sorting 
				int popindex = selectedColIndex(_rt, "Select Column to Populate:");
				if (popindex < 0)
					return;
				int outputindex = selectedColIndex(_rt, "Select Column to get Rounding values");
				if (outputindex < 0)
					return;
				if (command.equals("rounding"))
					NormalizeCol.roundingIndex(_rt.getRTMModel(), outputindex, popindex,1);
				else if (command.equals("ceiling"))
					NormalizeCol.roundingIndex(_rt.getRTMModel(), outputindex, popindex,2);
				else if (command.equals("flooring"))
					NormalizeCol.roundingIndex(_rt.getRTMModel(), outputindex, popindex,3);
				else // nearest 0
					NormalizeCol.roundingIndex(_rt.getRTMModel(), outputindex, popindex,4);
				return;
			}
			
			if (command.equals("randreplace")) {
				_rt.cancelSorting(); // No sorting 
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				Vector<Double> colD = _rt.getRTMModel().getColDataVD(index);
				double[] minMax = AggrCumRTM.getMinMax(colD);
				int rowc = _rt.getModel().getRowCount();
				
				for (int i=0; i < rowc; i++) {
					Object o = _rt.getModel().getValueAt(i, index);
					Double randval = RandomColGen.randomDouble(minMax[1], minMax[0]);
					if (o == null || "".equals(o.toString())) {
						try {
							_rt.getModel().setValueAt(randval,i, index);
						} catch(Exception e1) {	
							_rt.getModel().setValueAt(randval.toString(),i, index);
						}
					}
				}
				
				return;
			}
			if (command.equals("prereplace")) {
				_rt.cancelSorting(); // No sorting 
				int index = selectedColIndex(_rt);
				if (index < 0)
					return;
				
				int rowc = _rt.getModel().getRowCount();
				for (int i=0; i < rowc; i++) {
					Object o = _rt.getModel().getValueAt(i, index);
					if (o == null || "".equals(o.toString())) {
						Object prev = null; int pindex=i-1;
						
						while (pindex >=0 && ( prev == null || "".equals(prev.toString()))) { // see if previous values are null
							prev = _rt.getModel().getValueAt(pindex, index);
							pindex--;
						}
						
						//If all the previous values are null
						if (prev == null || "".equals(prev.toString())) {
							 pindex=i+1;
							 while (pindex < rowc && prev == null || "".equals(prev.toString())) { // see if previous values are null
									prev = _rt.getModel().getValueAt(pindex, index);
									pindex++;
								}
						}
						
						try {
							_rt.getModel().setValueAt(prev,i, index);
						} catch(Exception e1) {	
							_rt.getModel().setValueAt(prev.toString(),i, index);
						}
					}
				}
				
				return;
			}
			
		} catch (Exception e1) {
      ConsoleFrame.addText("\n Exception:" + e1.getLocalizedMessage());
      e1.printStackTrace();
    } finally {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}// End of Action Performed

	private String[] getQString(boolean isLoad) {
		String[] query = null;
		table_s = new Vector<String>();
		column_s = new Vector<String>();
		Hashtable<String, Vector<String>> ht = new Hashtable<String, Vector<String>>();
		_ht = new Hashtable<String, Integer>();
		Vector<String> vc = null;
		String table, column;

		for (int i = 0; i < tf1.length; i++) {
			if (radio1[i].isSelected() == true) {
				table = table1[i].getSelectedItem().toString();
				column = col1[i].getSelectedItem().toString();

				_ht.put(table + column, i);
				table_s.add(table);
				column_s.add(column);
				vc = ht.get(table);
				if (vc == null) {
					vc = new Vector<String>();
					vc.add(0, column);
					ht.put(table, vc);
				} else if (vc.indexOf(column) == -1) {
					vc.add(vc.size(), column);
					ht.put(table, vc);
				} else {
					JOptionPane.showMessageDialog(null, "Duplicate Mapping: "
							+ (i + 1) + " Row", "Error Message",
							JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
		}

		init = false;
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), Rdbms_conn.getDBType());
		if (isLoad) {
			unique_table_s = new Vector<String>();
			query = qb.get_mapping_query(ht, unique_table_s);
		} else {

			Vector<String> s_vc = qb.get_synch_mapping_query(table_s, column_s);
			query = new String[s_vc.size()];
			for (int a = 0; a < s_vc.size(); a++)
				query[a] = s_vc.get(a);
		}
		return query;
	}

	public Object[] getColObject(int colIndex) {
		int row_c = _rt.table.getRowCount();
		Object[] colObj = new Object[row_c];
		for (int i = 0; i < row_c; i++)
			colObj[i] = _rt.getModel().getValueAt(i, colIndex);
		return colObj;
	}

	public Object[] getSelectedColObject() {
		int c_s = _rt.table.getSelectedColumn();
		if (c_s < 0)
			return null;
		int r_c = _rt.table.getSelectedRowCount();
		if (r_c <= 0)
			return null;

		Object[] colObj = new Object[r_c];
		int[] rowS = _rt.table.getSelectedRows();
		for (int i = 0; i < rowS.length; i++)
			colObj[i] = _rt.getModel().getValueAt(rowS[i], c_s);
		return colObj;
	}

	private JDialog inputDialog() {
		ButtonGroup bg = new ButtonGroup();
		rb1 = new JRadioButton("Last Row");
		rb2 = new JRadioButton("From/At Row Index");
		bg.add(rb1);
		bg.add(rb2);
		rb1.setSelected(true);

		tf = new JFormattedTextField(NumberFormat.getIntegerInstance());
		tf.setValue(new Integer(0));
		JLabel row_n = new JLabel("# of Rows", JLabel.TRAILING);
		rn = new JFormattedTextField(NumberFormat.getIntegerInstance());
		rn.setValue(new Integer(1));

		JButton ok = new JButton("Ok");
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());

		JPanel dp = new JPanel();
		dp.setLayout(new GridLayout(4, 2));
		dp.add(rb1);
		dp.add(new JLabel());
		dp.add(rb2);
		dp.add(tf);
		dp.add(row_n);
		dp.add(rn);
		dp.add(ok);
		dp.add(cancel);

		d_f = new JDialog();
		d_f.setModal(true);
		d_f.setTitle("Row Edit Option Dialog");
		d_f.setLocation(250, 250);
		d_f.getContentPane().add(dp);
		d_f.pack();
		d_f.setVisible(true);
		return d_f;
	}

	private JDialog mapDialog(boolean toDb) throws IOException {
		init = false;
		JPanel jp_p = null;
		if (Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") == 0 && toDb == true ) {
			jp_p = hiveLoadPanel();
			jp_p.setPreferredSize(new Dimension(500,300));
		} else {
		TableItemListener tl = new TableItemListener();
		ColumnItemListener cl = new ColumnItemListener();

		int colC = _rt.table.getColumnCount();
		Vector<String> vector = Rdbms_conn.getTable();
		vector1 = new Vector[2];
		table1 = new JComboBox[colC];
		col1 = new JComboBox[colC];
		l1 = new JLabel[colC];
		radio1 = new JRadioButton[colC];
		tf1 = new JTextField[colC];
		JLabel[] condF = new JLabel[colC];
		JLabel[] showL = new JLabel[colC];
		queryString = new String[colC];

		vector1 = TableMetaInfo.populateTable(5, 0, 1, vector1);

		JPanel jp = new JPanel();
		SpringLayout layout = new SpringLayout();
		jp.setLayout(layout);

      ImageIcon imageicon = new ImageIcon(DisplayFileTable.class
          .getClassLoader().getResource("image/Filter.gif"), "Query");
  		int imageLS = imageicon.getImageLoadStatus();

		for (int i = 0; i < colC; i++) {
			tf1[i] = new JTextField(8);
			tf1[i].setText(_rt.table.getColumnName(i));
			tf1[i].setEditable(false);
			tf1[i].setToolTipText(_rt.table.getColumnClass(i).getName());
			jp.add(tf1[i]);

			if (toDb)
				radio1[i] = new JRadioButton("Map to");
			else
				radio1[i] = new JRadioButton("Synch From");

			if (toDb)
				radio1[i].setSelected(true);
			else
				radio1[i].setSelected(false);
			jp.add(radio1[i]);

			table1[i] = new JComboBox<String>();
			table1[i].addItemListener(tl);
			for (int j = 0; j < vector.size(); j++) {
				String item = (String) vector.get(j);
				table1[i].addItem(item);
			}
			jp.add(table1[i]);

			col1[i] = new JComboBox<String>();
			col1[i].addItemListener(cl);
			for (int j = 0; j < vector1[0].size(); j++) {
				String item = (String) vector1[0].get(j);
				col1[i].addItem(item);
			}
			jp.add(col1[i]);

			int va = ((Integer) (vector1[1].get(0))).intValue();
			l1[i] = new JLabel(SqlType.getTypeName(va));
			l1[i].setToolTipText("Data Type");
			jp.add(l1[i]);

			if (imageLS == MediaTracker.ABORTED
					|| imageLS == MediaTracker.ERRORED)
				condF[i] = new JLabel(
						"<html><body><a href=\"\">Query</A></body></html>", 0);
			else
				condF[i] = new JLabel(imageicon, JLabel.CENTER);
			condF[i].setToolTipText("Click to Add Conditions");
			if (toDb)
				condF[i].setVisible(false);
			condF[i].addMouseListener(new LinkMouseListener(i));
			jp.add(condF[i]);

			showL[i] = new JLabel(
					"<html><body><a href=\"\">Show Condition</A></body></html>",
					0);
			showL[i].setToolTipText("Click to show condition");
			if (toDb)
				showL[i].setVisible(false);
			showL[i].addMouseListener(new LinkMouseListener(i));
			jp.add(showL[i]);
		}
		SpringUtilities.makeCompactGrid(jp, colC, 7, 3, 3, 3, 3);

		JScrollPane jscrollpane1 = new JScrollPane(jp);
		if (colC * 35 > 400)
			jscrollpane1.setPreferredSize(new Dimension(575, 400));
		else
			jscrollpane1.setPreferredSize(new Dimension(575, colC * 35));

		JPanel bp = new JPanel();
		JButton ok;
		JLabel smap = new JLabel(
				"<html><body><a href=\"\">Save Mapping</A></body></html>", 0);
		smap.setToolTipText("Click to save Mapping");
		smap.addMouseListener(new LinkMouseListener(0));

		JLabel lmap = new JLabel(
				"<html><body><a href=\"\">Load Mapping</A></body></html>", 0);
		lmap.setToolTipText("Click to load from Mapping File");
		lmap.addMouseListener(new LinkMouseListener(0));

		bp.add(smap);
		bp.add(lmap);
		if (toDb) {
			ok = new JButton("Load");
			ok.setActionCommand("load");
		} else {
			ok = new JButton("Synch");
			ok.setActionCommand("synch");
		}

		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("mcancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		jp_p = new JPanel(new BorderLayout());
		jp_p.add(jscrollpane1, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);
		}

		d_m = new JDialog();
		d_m.setModal(true);
		d_m.setTitle("Map Dialog");
		d_m.setLocation(250, 250);
		d_m.getContentPane().add(jp_p);
		init = true;
		d_m.pack();
		d_m.setVisible(true);

		return d_m;
	}

	private void ok_action(int actionType) {
		int actionR = ((Number) rn.getValue()).intValue();
		if (actionR < 1)
			actionR = 1;
		if (rb1.isSelected() == true) {
			if (actionType == ADDITION)
				_rt.addRows(_rt.table.getRowCount(), actionR);
			else {
				if (actionR >= _rt.table.getRowCount())
					_rt.removeRows(0, _rt.table.getRowCount());
				else
					_rt.removeRows(_rt.table.getRowCount() - actionR, actionR);
			}
		} // End of Last Row
		else {
			int rowC = _rt.table.getRowCount();
			int insertR = ((Number) tf.getValue()).intValue();
			if (insertR >= rowC)
				insertR = rowC - 1;
			if (insertR < 0)
				insertR = 0;

			if (actionType == ADDITION)
				_rt.addRows(insertR, actionR);
			else {
				if ((insertR + actionR) >= rowC)
					_rt.removeRows(insertR, _rt.table.getRowCount() - insertR);
				else
					_rt.removeRows(insertR, actionR);
			}
		} // End of Row At
	}

	private class MyCellRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String _format;
		private String _tid;

		public MyCellRenderer(String format, String tid) {
			_format = format;
			_tid = tid;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			StringBuffer text = null;
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);

			if (value instanceof Number) {
				DecimalFormat f = new DecimalFormat(_format);
				text = f.format(value, new StringBuffer(), new FieldPosition(0));
				((JLabel) c).setHorizontalAlignment(JLabel.TRAILING);
				c.setForeground(Color.RED.darker());
			}
			if (value instanceof Date) {
				SimpleDateFormat f = new SimpleDateFormat(_format);
				f.setTimeZone(TimeZone.getTimeZone(_tid));
				text = f.format(value, new StringBuffer(), new FieldPosition(0));
				((JLabel) c).setHorizontalAlignment(JLabel.LEADING);
				c.setForeground(Color.MAGENTA.darker());
			}
			if (value instanceof String) {
				text = new StringBuffer(value.toString());
				((JLabel) c).setHorizontalAlignment(JLabel.LEADING);
				c.setForeground(Color.BLUE.darker());
			}
			if (text != null)
				((JLabel) c).setText(text.toString());
			return c;
		}
	} // End of MyCellRenderer

	// Default column selected
	private int selectedColIndex(ReportTable rt) {
		int colC = rt.table.getColumnCount();
		Object[] colN = new Object[colC];
		for (int i = 0; i < colC; i++)
		 colN[i] = (i + 1) + "," + rt.table.getColumnName(i);	
		String input = (String) JOptionPane.showInputDialog(null,
				"Select the Column ", "Column Selection Dialog",
				JOptionPane.PLAIN_MESSAGE, null, colN, colN[0]);
		if (input == null || input.equals(""))
			return -1;

		String col[] = input.split(",", 2);
		int index = Integer.valueOf(col[0]).intValue();
		return index - 1;
	}
	
	private int selectedColIndex(ReportTable rt, String msg) {
		if (msg == null || "".equals(msg))
			return selectedColIndex(rt);
		
		int colC = rt.table.getColumnCount();
		Object[] colN = new Object[colC];
		for (int i = 0; i < colC; i++)
		 colN[i] = (i + 1) + "," + rt.table.getColumnName(i);	
		String input = (String) JOptionPane.showInputDialog(null,
				msg, "Column Selection Dialog",
				JOptionPane.PLAIN_MESSAGE, null, colN, colN[0]);
		if (input == null || input.equals(""))
			return -1;

		String col[] = input.split(",", 2);
		int index = Integer.valueOf(col[0]).intValue();
		return index - 1;
	}

	private class TableItemListener implements ItemListener {
		private int index = 0;

		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED && init == true) {
				for (index = 0; index < table1.length; index++) {
					if (e.getSource().equals(table1[index])) {
						int s_index = table1[index].getSelectedIndex();
						vector1 = TableMetaInfo.populateTable(5, s_index,
								s_index + 1, vector1);
						int va = ((Integer) (vector1[1].get(0))).intValue();
						col1[index].removeAllItems();
						for (int i = 0; i < vector1[0].size(); i++) {
							String item = (String) vector1[0].get(i);
							col1[index].addItem(item);
						}
						l1[index].setText(SqlType.getTypeName(va));
						queryString[index] = "";
					}
				}
			}
		}
	}

	private class ColumnItemListener implements ItemListener {
		private int index = 0;

		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED && init == true) {
				for (index = 0; index < col1.length; index++) {
					if (e.getSource().equals(col1[index])) {
						int s_index = table1[index].getSelectedIndex();
						vector1 = TableMetaInfo.populateTable(5, s_index,
								s_index + 1, vector1);
						int va = ((Integer) (vector1[1].get(col1[index]
								.getSelectedIndex()))).intValue();
						l1[index].setText(SqlType.getTypeName(va));
					}
				}
			}
		}
	}

	private void loadQuery(final String[] query) throws Exception {
		_rt.cancelSorting();
		if (d_m != null)
			d_m.dispose();
		InterTableInfo.loadQuery(query, _rt.getRTMModel(), unique_table_s, _ht);

	}

	private void synchQuery(final String[] query) {
		_rt.cancelSorting();
		if (d_m != null)
			d_m.dispose();

		InterTableInfo.synchQuery(query, _rt.getRTMModel(), table_s, column_s,
				_ht, queryString);
	}

	/* Link Mouse Adapter */
	private class LinkMouseListener extends MouseAdapter {
		int _index;

		public void mouseClicked(MouseEvent mouseevent) {
			try {
				mouseevent.getComponent().setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				String s1 = ((JLabel) mouseevent.getSource()).getText();
				if (s1 != null
						&& s1.equals("<html><body><a href=\"\">Show Condition</A></body></html>")) {
					String qry_msg = queryString[_index];
					if (qry_msg == null || "".equals(qry_msg))
						qry_msg = "Condition Not Set";
					JOptionPane.showMessageDialog(null, qry_msg,
							"Query Information",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				} else if (s1 != null
						&& s1.equals("<html><body><a href=\"\">Save Mapping</A></body></html>")) {
					String[] query = getQString(true);
					if (query == null)
						return;
					// Create the Report Table here and Save
					ReportTable smTable = new ReportTable(new String[] {
							"File", "Field", "Table", "Column", "Data_Type" });
					int colC = _rt.table.getColumnCount();
					for (int i = 0; i < colC; i++) {
						if (radio1[i].isSelected() == true) {
							String[] row = new String[] { _fileN,
									tf1[i].getText(),
									table1[i].getSelectedItem().toString(),
									col1[i].getSelectedItem().toString(),
									l1[i].getText() };
							smTable.addFillRow(row);
						}
					}
					smTable.saveAsXml();
				} else if (s1 != null
						&& s1.equals("<html><body><a href=\"\">Load Mapping</A></body></html>")) {
					try {
						File f = FileSelectionUtil
								.chooseFile("Select Mapping File");
						if (f == null
								|| f.getName().toLowerCase().endsWith(".xml") == false)
							return;
						final XmlReader xmlReader = new XmlReader();
						ReportTable lmTable = new ReportTable(xmlReader.read(f));
						if (lmTable == null)
							return;
						if (_fileN.equals(lmTable.getValueAt(0, 0)) == false) {
							int n = JOptionPane
									.showConfirmDialog(
											null,
											"File Name not Matching. \n Do you wish to Continue ?",
											"File Not Matching",
											JOptionPane.YES_NO_OPTION);
							if (n == JOptionPane.NO_OPTION)
								return;
						}
						int colC = _rt.table.getColumnCount();
						Vector<String> colName = new Vector<String>();
						for (int i = 0; i < colC; i++) {
							radio1[i].setSelected(false);
							colName.add(_rt.table.getColumnName(i));
						}
						int lrowC = lmTable.table.getRowCount();
						for (int i = 0; i < lrowC; i++) {
							int index = colName.indexOf(lmTable
									.getValueAt(i, 1));
							if (index < 0 || index > colC) {
								ConsoleFrame.addText("\n Index out of range:"
										+ index);
								return;
							}
							radio1[index].setSelected(true);
							String tableN = lmTable.getValueAt(i, 2).toString();
							String colN = lmTable.getValueAt(i, 3).toString();
							int tableI = table1[index].getItemCount();
							for (int j = 0; j < tableI; j++) {
								if (tableN.equals(table1[index].getItemAt(j)))
									table1[index].setSelectedIndex(j);
							}
							int colI = col1[index].getItemCount();
							for (int j = 0; j < colI; j++) {
								if (colN.equals(col1[index].getItemAt(j)))
									col1[index].setSelectedIndex(j);
							}
						}
					} catch (Exception e) {
						ConsoleFrame
								.addText("\n Exception: Load Mapping File Exception");
						ConsoleFrame.addText("\n " + e.getMessage());
					}
				} else {
					Vector<String> vector = Rdbms_conn.getTable();
					int i = vector.indexOf(table1[_index].getSelectedItem()
							.toString());

					Vector avector[] = null;
					avector = TableMetaInfo.populateTable(5, i, i + 1, avector);

					QueryDialog querydialog = new QueryDialog(2, table1[_index]
							.getSelectedItem().toString(), avector);
					querydialog.setColumn(col1[_index].getSelectedItem()
							.toString());
					querydialog.setLocation(175, 100);
					querydialog.setTitle(" DataQuality Query Setup ");
					querydialog.setModal(true);
					querydialog.pack();
					querydialog.setVisible(true);
					int j = querydialog.response;
					if (j == 1) {
						queryString[_index] = querydialog.cond;
					}
					return;
				}
			} finally {
				mouseevent
						.getComponent()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
		}

		public void mouseEntered(MouseEvent mouseevent) {
			mouseevent.getComponent().setCursor(
					Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		private LinkMouseListener(int index) {
			_index = index;
		}
	}

	public static ReportTable joinTables(ReportTable leftT, int indexL,
			ReportTable rightT, int indexR, int joinType) {
		// Left table is table displayed
		leftT.cancelSorting();
		leftT = new ReportTable(RTMUtil.joinTables(leftT.getRTMModel(), indexL,
				rightT.getRTMModel(), indexR, joinType));
		return leftT;
	}

	private void caseFormat(int caseType) {
		int index = selectedColIndex(_rt);
		if (index < 0)
			return;
		String name = _rt.table.getColumnClass(index).getName();
		if (name.toUpperCase().contains("STRING") == false)
			return;
		int rowC = _rt.table.getRowCount();
		char defChar = '.';

		if (caseType == 4) {
			Locale defLoc = Locale.getDefault();
			ConsoleFrame.addText("\n Default Locale is :" + defLoc);
			if (defLoc.equals(Locale.US) || defLoc.equals(Locale.UK)
					|| defLoc.equals(Locale.CANADA)) {
				// Do nothing
			} else {
				String response = JOptionPane
						.showInputDialog(null,
								"Please enter the end of Line Character ?",
								"Language End Line Input",
								JOptionPane.QUESTION_MESSAGE);
				if (response == null || "".equals(response)) {
					// Do nothing
				} else
					defChar = response.charAt(0);
			}
		}
		for (int i = 0; i < rowC; i++) {
			if (_rt.table.getValueAt(i, index) == null)
				continue;
			String prevC = _rt.table.getValueAt(i, index).toString();

			switch (caseType) {
			case 1:
				prevC = StringCaseFormatUtil.toUpperCase(prevC);
				break;
			case 2:
				prevC = StringCaseFormatUtil.toLowerCase(prevC);
				break;
			case 3:
				prevC = StringCaseFormatUtil.toTitleCase(prevC);
				break;
			case 4:
				prevC = StringCaseFormatUtil.toSentenceCase(prevC, defChar);
				break;
			default:
			}
			_rt.table.setValueAt(prevC, i, index);
		}

	}
	/* This function will create a panel to load data into hive */
	private JPanel hiveLoadPanel() {
		
		Vector<String> vector = Rdbms_conn.getTable();
		vector1 = new Vector[2];
		table1 = new JComboBox[1]; // Only one is needed to hold tablenames

		vector1 = TableMetaInfo.populateTable(5, 0, 1, vector1);
		
		table1[0] = new JComboBox<String>();
		for (int j = 0; j < vector.size(); j++) {
			String item = (String) vector.get(j);
			table1[0].addItem(item);
		}
		
		JLabel gInfo = new JLabel("This option will load existing HDFS/Hive file into Hive table");
		JLabel fileInfo = new JLabel("Full Path:");
		local = new JCheckBox("Local File");
		local.setToolTipText("Local File on which Hadoop File System is running");
		locationf= new JTextField (35);
		locationf.setText("\"FILE_PATH\"");
		JLabel tableInfo = new JLabel("Select Table to load Into");
		JLabel partInfo = new JLabel("Enter Partition Value - (partcol1='val1', partcol2=\"val2\" ...)");
		partArea= new JTextArea (5,30);
		overWrite = new JCheckBox("Overwrite data ( It will overwrite existing data)");

		JPanel jp = new JPanel();
		SpringLayout layout = new SpringLayout();
		jp.setLayout(layout);
		jp.add(gInfo);jp.add(fileInfo);jp.add(local);jp.add(locationf);jp.add(tableInfo);
		jp.add(table1[0]);jp.add(partInfo);jp.add(partArea);jp.add(overWrite);

		layout.putConstraint(SpringLayout.WEST, gInfo, 5, SpringLayout.WEST, jp);
		layout.putConstraint(SpringLayout.NORTH, gInfo, 8, SpringLayout.NORTH,jp);
		layout.putConstraint(SpringLayout.WEST, fileInfo, 5, SpringLayout.WEST, jp);
		layout.putConstraint(SpringLayout.NORTH, fileInfo, 5, SpringLayout.SOUTH,gInfo);
		layout.putConstraint(SpringLayout.WEST, locationf, 5, SpringLayout.EAST, fileInfo);
		layout.putConstraint(SpringLayout.NORTH, locationf, 2, SpringLayout.NORTH,fileInfo);
		
		layout.putConstraint(SpringLayout.WEST, local, 5, SpringLayout.WEST, jp);
		layout.putConstraint(SpringLayout.NORTH, local, 5, SpringLayout.SOUTH,fileInfo);
		layout.putConstraint(SpringLayout.WEST, tableInfo, 5, SpringLayout.WEST, jp);
		layout.putConstraint(SpringLayout.NORTH, tableInfo, 8, SpringLayout.SOUTH,local);
		layout.putConstraint(SpringLayout.WEST, table1[0], 15, SpringLayout.EAST, tableInfo);
		layout.putConstraint(SpringLayout.NORTH, table1[0], -3, SpringLayout.NORTH,tableInfo);
		
		layout.putConstraint(SpringLayout.WEST, partInfo, 5, SpringLayout.WEST, jp);
		layout.putConstraint(SpringLayout.NORTH, partInfo, 10, SpringLayout.SOUTH,table1[0]);
		layout.putConstraint(SpringLayout.WEST, partArea, 5, SpringLayout.WEST, jp);
		layout.putConstraint(SpringLayout.NORTH, partArea, 8, SpringLayout.SOUTH,partInfo);
		layout.putConstraint(SpringLayout.WEST, overWrite, 5, SpringLayout.WEST, jp);
		layout.putConstraint(SpringLayout.NORTH, overWrite, 10, SpringLayout.SOUTH,partArea);
		

		JPanel bp = new JPanel();
		
		JButton info = new JButton("Table Info");
		info.setActionCommand("tableinfo");
		info.addActionListener(this);
		info.addKeyListener(new KeyBoardListener());
		bp.add(info);
		
		JButton ok = new JButton("Load");
		ok.setActionCommand("hiveload");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("mcancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(jp, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		return jp_p;
	}
	
	 /* This function is used to Hive Table Information in dialog box */
	 private void showHiveTableInfo() {
		 String table  = table1[0].getSelectedItem().toString();
		 HiveQueryBuilder qb = new HiveQueryBuilder(
					Rdbms_conn.getHValue("Database_DSN"),table,Rdbms_conn.getDBType());
			String query = qb.descHiveTable();
			
			try {
				d_m.setCursor(java.awt.Cursor
						.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				Rdbms_conn.openConn();
				ResultSet rs = Rdbms_conn.runQuery(query); 
				ReportTableModel rtm = ResultsetToRTM.getSQLValue(rs, true);
				rs.close();
				Rdbms_conn.closeConn();
				ReportTable rt = new ReportTable(rtm);
				
				/* Now Open Dialog to show */
				JDialog showDia = new JDialog();
				showDia.setModal(true);
				showDia.setTitle("Map Dialog");
				showDia.setLocation(250, 100);
				showDia.getContentPane().add(rt);
				showDia.pack();
				showDia.setVisible(true);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null,
						e.getLocalizedMessage(), "Hive SQL Error",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				d_m.setCursor(java.awt.Cursor
						.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
		 
	 }
	
	/* This function will load file into table */
	private boolean runHiveLoad() {
		String table  = table1[0].getSelectedItem().toString();
		
		String path = locationf.getText();
		
		if (path == null || "".equals(path) || "\"\"".equals(path)) {
			JOptionPane.showMessageDialog(null,
					"File Location can not be empty", "File Path Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// in case user is not putting under double quote
		if (path.startsWith("\"") == false) 
			path = "\""+path+"\"";
		
		String partition = partArea.getText();
		if (!( partition == null || "".equals(partition))) {
			partition.trim();
			if (partition.startsWith("(") == false )
				partition = "("+partition+")";
		}
		
		HiveQueryBuilder qb = new HiveQueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"),table,Rdbms_conn.getDBType());
		String query = qb.appendHiveTable(path, table, local.isSelected(), overWrite.isSelected(), partition);
		
		try {
			d_m.setCursor(java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(query); 
			rs.close();
			Rdbms_conn.closeConn();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null,
					e.getLocalizedMessage(), "Hive SQL Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		} finally {
			d_m.setCursor(java.awt.Cursor
					.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}
		ConsoleFrame.addText("\n Load successful to Table:"+table);
		return true;
	}
}
