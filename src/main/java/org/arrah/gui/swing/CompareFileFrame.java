package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2019      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for getting info about 
 * comparing diffing files taken as RTM
 * to the cell level
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableCellRenderer;

import org.arrah.framework.ndtable.RTMDiffUtil;
import org.arrah.framework.ndtable.RTMDiffWithData;
import org.arrah.framework.ndtable.ReportTableModel;

public class CompareFileFrame implements ActionListener {

	private ReportTableModel rtmL = null, rtmR = null;
	private JDialog d_m= null, d_recHead = null, d_recColumn=null;
	private JComboBox<String>[] _rColC,_rColCC, _dataTYPEC ;
	private JCheckBox[] _checkB, _checkBC;
	private Vector<String> _lCols, _rCols;
	private Vector<Integer> _leftMap, _rightMap;
	private ReportTableModel rtmLnoMatch = null, rtmRnoMatch = null, rtmnonKeyUnMatchData = null;
	private ReportTable rtmLnoMatchTable = null, rtmRnoMatchTable = null, rtmnonKeyUnMatchDataTable = null;
	private boolean indexadded = false;
	
	private JRadioButton cellm,keym,keycellm;
	private String[] leftKey,rightKey; // added for data diff
	private ArrayList<String> leftCols,rightCols, datatypeCols; // added for data diff

	public CompareFileFrame() { // Default Constructor
		
	}
	
	public CompareFileFrame(ReportTableModel rtmL, ReportTableModel rtmR) { // Default left and right RTM
		this.rtmL = rtmL;
		this.rtmR = rtmR;
		createMapDialog();
	}	
	
	private void createGUI() {
		
	JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
	RTMDiffUtil rtmdiff = null;
	
	if (rtmL == null || rtmR == null ) {
		ConsoleFrame.addText("\n Comparing tables are Null");
		return;
	}
	
	rtmdiff = new RTMDiffUtil(rtmL,_leftMap, rtmR,_rightMap);
	boolean com = rtmdiff.compare(true,keym.isSelected());
	if ( com == false) {
		ConsoleFrame.addText("\n File Comparison Failed");
		return;
	}
	// Enter duplicate counts and right file matching count
	if (keym.isSelected() == true) {
		int duplicate = rtmdiff.getDuplicateCount();
		Map<Integer,ArrayList<Integer>> mapre = rtmdiff.reverseMap();
		if (mapre != null && mapre.size() > 0 ) {
		Set<Integer> keysre = mapre.keySet();
		for (Integer i:keysre ) {
			ArrayList<Integer> value = rtmdiff.reverseMap().get(i);
			if (value.size() > 1)
			ConsoleFrame.addText("\n Duplicate Primary Table Index: "+i+" - "+value);
		}
		
		ConsoleFrame.addText("\n Duplicated Primary key Count:"+ duplicate);
		if (duplicate > 15) // warn if more than 15 duplicates
			JOptionPane.showMessageDialog(null, "Many duplicates. Duplicate counts:"+duplicate,"Duplicate Warning",JOptionPane.WARNING_MESSAGE);
		}
	}
	
	if (keym.isSelected() == false) {
		tabPane.add("Matched Record",new ReportTable(rtmdiff.getMatchedRTM()));
		rtmLnoMatch = rtmdiff.leftNoMatchRTM();
		rtmLnoMatchTable = new ReportTable(rtmLnoMatch);
		tabPane.add("Primary No Match",rtmLnoMatchTable);
		rtmRnoMatch = rtmdiff.rightNoMatchRTM();
		rtmRnoMatchTable = new ReportTable(rtmRnoMatch);
		tabPane.add("Secondary No Match",rtmRnoMatchTable);
	} else {
		rtmLnoMatch = rtmdiff.leftNoMatchRTM();
		rtmLnoMatchTable = new ReportTable(rtmLnoMatch);
		tabPane.add("Primary Unmatched Key",rtmLnoMatchTable);
		rtmRnoMatch = rtmdiff.rightNoMatchRTM();
		rtmRnoMatchTable = new ReportTable(rtmRnoMatch);
		tabPane.add("Secondary Unmatched Key",rtmRnoMatchTable);
		rtmnonKeyUnMatchData = rtmdiff.getMatchFailedRTM();
		rtmnonKeyUnMatchDataTable = new ReportTable(rtmnonKeyUnMatchData);
		tabPane.add("Only Key matched records",rtmnonKeyUnMatchDataTable);
		tabPane.add("All Matched Records",new ReportTable(rtmdiff.getMatchedRTM()));
	}
	
	// Make existing report to go
	d_recHead.setVisible(false);
	d_m.setVisible(false); 
	
	JFrame db_d = new JFrame();
	db_d.setTitle("Difference Summary Dialog");
	
	// Add menu here
	JMenuBar menubar = new JMenuBar();
	JMenu menui = new JMenu("Show Cell Difference");
	
	JMenuItem menuitem = new JMenuItem("Primary Table");
    menuitem.addActionListener( this );
    menuitem.setActionCommand("showcell");
    menui.add(menuitem);
    
	JMenuItem menuitem1 = new JMenuItem("Secondary Table");
    menuitem1.addActionListener( this );
    menuitem1.setActionCommand("showcellr");
    menui.add(menuitem1);
    
    if (keym.isSelected() == true) { // add menu only for key
    	JMenuItem menuitem2 = new JMenuItem("Key Match Table");
    	menuitem2.addActionListener( this );
    	menuitem2.setActionCommand("showcellkey");
        menui.add(menuitem2);
    }
    
    menubar.add(menui);
    db_d.setJMenuBar(menubar);
    
	db_d.getContentPane().add(tabPane);
	db_d.setLocation(75, 75);
	db_d.pack();
	db_d.setVisible(true);
	
	QualityListener.bringToFront(db_d);

	}
	
	private void createDataDiffGUI() {
		RTMDiffWithData rtmdiff = null;
		
		if (rtmL == null || rtmR == null ) {
			ConsoleFrame.addText("\n Comparing tables are Null");
			return;
		}
		String[] leftColA = new String[leftCols.size()];
		String[] rightColA = new String[rightCols.size()];
		String[] dataTypeA = new String[datatypeCols.size()];
		
		rtmdiff = new RTMDiffWithData(rtmL,leftKey,datatypeCols.toArray(dataTypeA),leftCols.toArray(leftColA),
					rtmR,rightKey,datatypeCols.toArray(dataTypeA),rightCols.toArray(rightColA));
	
	
		// Create header Report Table Model
		ArrayList<String> colNames = new ArrayList<String>();
		for(int i=0; i<leftKey.length; i++)
			colNames.add(leftKey[i]+"_L");
		for(int i=0; i<rightKey.length; i++)
			colNames.add(rightKey[i]+"_R");
		for(int i=0; i<leftColA.length; i++) {
			colNames.add(leftColA[i]+"_L");
			colNames.add(rightColA[i]+"_R");
			colNames.add(leftColA[i]+"-"+rightColA[i]);
		}
		
		String[] colNamesA = new String[colNames.size()];
		colNamesA = colNames.toArray(colNamesA);
		ConsoleFrame.addText("\n Column Names:" + Arrays.toString(colNamesA));
		ReportTableModel rtm = new ReportTableModel(colNamesA,true,true);
		
		int nomatchIndex = 0;
		
		try {
			Object[][] result = rtmdiff.compareData();
			String[][] keydata = rtmdiff.getmatchedKeyData();
			
	//		for (int i = 0; i < result.length; i++) {
	//			System.out.print(Arrays.toString(keydata[i]));
	//			System.out.println(Arrays.toString(result[i]));
	//		}
			ArrayList<String[]> nomatchData = rtmdiff.getNomatchKeyData();
			
			for (int i=0; i < result.length ; i++ ) {
				
				// if there is no match keydata would be empty so better use keys itself
				// String[]  = new String[keydata[0].length * 2]; // for left key and right key
				String[]  doubleKey= new String[leftKey.length + rightKey.length];
				
				if (keydata[i]!= null) {
					for (int j=0; j < keydata[0].length; j++) 
						doubleKey[j] = keydata[i][j];
					for (int j=keydata[0].length; j < keydata[0].length * 2; j++)
						doubleKey[j] = keydata[i][j-keydata[0].length];
				}
				else { // null left outer join condition
					if (nomatchIndex < nomatchData.size()) {
						String[] nomatchKey = nomatchData.get(nomatchIndex++);
						for (int k=0; k < nomatchKey.length; k++)
						doubleKey[k] = nomatchKey[k];
					}
				}
				
	//			System.out.print(Arrays.toString(doubleKey));
	//			System.out.println(Arrays.toString(result[i]));
				rtm.addFillRow(doubleKey, result[i]);
			}
		} catch (Exception e) {
			ConsoleFrame.addText("Compare File error:" +  e.getLocalizedMessage());
		}
		
		// Make existing report to go
		d_recColumn.setVisible(false);
		d_m.setVisible(false); 
		
		JFrame db_d = new JFrame();
		db_d.setTitle("Difference Summary Dialog");
		
		db_d.getContentPane().add(new ReportTable(rtm));
		db_d.setLocation(75, 75);
		db_d.pack();
		db_d.setVisible(true);
		
		QualityListener.bringToFront(db_d);

	}
	
	// Create GUI and show both table
	private JDialog createMapDialog() {
		
		JPanel tp = new JPanel();
		tp.setPreferredSize(new Dimension (1100,500));
		BoxLayout boxl = new BoxLayout(tp,BoxLayout.X_AXIS);
		tp.setLayout(boxl);
		tp.add(new ReportTable(rtmL)); 
		tp.add(new ReportTable(rtmR)); 
		
		JScrollPane jscrollpane = new JScrollPane(tp);
		jscrollpane.setPreferredSize(new Dimension(1125, 525));
		
		JPanel bp = new JPanel();
		JButton ok = new JButton("Next");;
		ok.setActionCommand("next");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(jscrollpane, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		d_m = new JDialog();
		d_m.setModal(true);
		d_m.setTitle("File Display Dialog");
		d_m.setLocation(50, 50);
		d_m.getContentPane().add(jp_p);
		d_m.pack();
		d_m.setVisible(true);

		return d_m;
	}
	
	private JDialog showHeaderMap() {
		// Header Making
		
		JPanel bjp = new JPanel();
		cellm = new JRadioButton("Cell Match");
		keym = new JRadioButton("Key Match");
		keycellm = new JRadioButton("Data Diff - Select keys");
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(cellm);bg.add(keym);bg.add(keycellm);
		cellm.setSelected(true);
		bjp.add(cellm);bjp.add(keym);bjp.add(keycellm);
		
		keym.addActionListener(this);
		cellm.addActionListener(this);
		keycellm.addActionListener(this);
		//bjp.setPreferredSize(new Dimension(675,50));
		
		// Center
		JPanel jp = new JPanel(new SpringLayout());
		int colCount = rtmL.getModel().getColumnCount(); // left column is master column
		
		_lCols = new Vector<String> ();
		_rCols = new Vector<String> ();
		
		for (int i=0 ; i <colCount; i++ )
			_lCols.add(rtmL.getModel().getColumnName(i));
			
		for (int i=0 ; i <rtmR.getModel().getColumnCount(); i++ )
			_rCols.add(rtmR.getModel().getColumnName(i));
			
		_rColC = new JComboBox[colCount];
		_checkB = new JCheckBox[colCount];

		for (int i =0; i < colCount; i++ ){
			_rColC[i] = new JComboBox<String>();
			for ( int j=0; j < _rCols.size() ; j++) 
				_rColC[i].addItem(_rCols.get(j));
			if (i < _rCols.size())
				_rColC[i].setSelectedIndex(i) ;  // So that mapping is easy
			//TODO to match with column names so that mapping is even easier
		}
		
		for (int i=0; i < colCount; i++) {
			_checkB[i] = new JCheckBox();
			_checkB[i].setSelected(true); // Keep selected for less work
			jp.add(_checkB[i]);
			JLabel rColLabel = new JLabel(_lCols.get(i),JLabel.TRAILING);
			jp.add(rColLabel);
			JLabel mapA = new JLabel("   Matches:   ",JLabel.TRAILING);
			mapA.setForeground(Color.BLUE);
			jp.add(mapA);
			jp.add(_rColC[i]);
		}
		
		SpringUtilities.makeCompactGrid(jp, colCount, 4, 3, 3, 3, 3); // 4 cols
		JScrollPane jscrollpane1 = new JScrollPane(jp);
		if ((100 + (colCount * 35)) > 500)
			jscrollpane1.setPreferredSize(new Dimension(675, 400));
		else
			jscrollpane1.setPreferredSize(new Dimension(675, 75 + colCount * 35));
		
		JPanel bp = new JPanel();
		JButton ok = new JButton("Next");;
		ok.setActionCommand("compare");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancelHeader");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(bjp, BorderLayout.PAGE_START);
		jp_p.add(jscrollpane1, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		d_recHead = new JDialog();
		d_recHead.setModal(true);
		d_recHead.setTitle("File HeaderMap Dialog");
		d_recHead.setLocation(250, 100);
		d_recHead.getContentPane().add(jp_p);
		d_recHead.pack();
		d_recHead.setVisible(true);

		return d_recHead;

	}
	
	// Key already have been selected. Now select columns to diff
	private JDialog showColumnSelection() {
		
		// Header Making
		JLabel infoc= new JLabel("Please select the Columns(Cell) to diff");
		
		// Center
		JPanel jp = new JPanel(new SpringLayout());
		int colCount = rtmL.getModel().getColumnCount(); // left column is master column
		
		if (_lCols != null) _lCols.clear();
		if (_rCols != null) _rCols.clear();
		
		_lCols = new Vector<String> ();
		_rCols = new Vector<String> ();
		
		for (int i=0 ; i <colCount; i++ ) {
			if (_leftMap.indexOf(i) == -1)
			_lCols.add(rtmL.getModel().getColumnName(i));
		}
			
		for (int i=0 ; i <rtmR.getModel().getColumnCount(); i++ ) {
			if (_rightMap.indexOf(i) == -1)
			_rCols.add(rtmR.getModel().getColumnName(i));
		}
		
		// new column count
		colCount = _lCols.size();
		_rColCC = new JComboBox[colCount];
		_checkBC = new JCheckBox[colCount];
		_dataTYPEC = new JComboBox[colCount];

		for (int i =0; i < colCount; i++ ){
			_rColCC[i] = new JComboBox<String>();
			for ( int j=0; j < _rCols.size() ; j++) 
				_rColCC[i].addItem(_rCols.get(j));
			if (i < _rCols.size())
				_rColCC[i].setSelectedIndex(i) ;  // So that mapping is easy
			//TODO to match with column names so that mapping is even easier
		}
		
		for (int i=0; i < colCount; i++) {
			_checkBC[i] = new JCheckBox();
			_checkBC[i].setSelected(true); // Keep selected for less work
			jp.add(_checkBC[i]);
			JLabel rColLabel = new JLabel(_lCols.get(i),JLabel.TRAILING);
			jp.add(rColLabel);
			JLabel mapA = new JLabel("   Matches:   ",JLabel.TRAILING);
			mapA.setForeground(Color.BLUE);
			jp.add(mapA);
			jp.add(_rColCC[i]);
			_dataTYPEC[i] = new JComboBox<String>(new String[] {"Number","Date","String"});
			jp.add(_dataTYPEC[i]);
		}
		
		SpringUtilities.makeCompactGrid(jp, colCount, 5, 3, 3, 3, 3); // 5 cols
		JScrollPane jscrollpane1 = new JScrollPane(jp);
		if ((100 + (colCount * 35)) > 500)
			jscrollpane1.setPreferredSize(new Dimension(675, 400));
		else
			jscrollpane1.setPreferredSize(new Dimension(675, 75 + colCount * 35));
		
		JPanel bp = new JPanel();
		JButton ok = new JButton("Next");;
		ok.setActionCommand("compareDataDiff");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancelColumn");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(infoc, BorderLayout.PAGE_START);
		jp_p.add(jscrollpane1, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		if (d_recHead != null) d_recHead.dispose();
		
		d_recColumn = new JDialog();
		d_recColumn.setModal(true);
		d_recColumn.setTitle("ColumnMap Dialog");
		d_recColumn.setLocation(250, 100);
		d_recColumn.getContentPane().add(jp_p);
		d_recColumn.pack();
		d_recColumn.setVisible(true);

		return d_recColumn;

	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(  e.getSource() instanceof JRadioButton) {
			JRadioButton selRadio= (JRadioButton)e.getSource();
			String radText= selRadio.getText();
			if(radText.equals("PrimaryKey Based Match") ) {
				boolean selected = selRadio.isSelected();
				if (selected == true) { // Key has been selected to uncheck keys
					for (int i=0; i <_checkB.length; i++)
					_checkB[i].setSelected(false);
				}
			} else {
				boolean selected = selRadio.isSelected();
				if (selected == true) { // cell has been selected to uncheck keys
					for (int i=0; i <_checkB.length; i++)
					_checkB[i].setSelected(true);
				}
			}
		}
		if ("cancel".equals(e.getActionCommand())) 
			d_m.dispose();
		if ("cancelHeader".equals(e.getActionCommand())) 
			d_recHead.dispose();
		if ("cancelColumn".equals(e.getActionCommand())) {
			d_recColumn.dispose();
		}
		if ("next".equals(e.getActionCommand())) 
			showHeaderMap();
		if ("compare".equals(e.getActionCommand())) {
			_leftMap = new Vector<Integer>();
			_rightMap = new Vector<Integer>();
			
			for (int i =0; i < rtmL.getModel().getColumnCount(); i++) {
				if (_checkB[i].isSelected() == false ) continue;
				int rightIndex =  _rColC[i].getSelectedIndex();
				if (_rightMap.contains(rightIndex) == true) {
					JOptionPane.showMessageDialog(null, "Duplicate Mapping at Row:"+i,
							"Record HeaderMap Dialog",JOptionPane.INFORMATION_MESSAGE);
					return;
				} else { 
					_rightMap.add(rightIndex);
					_leftMap.add(i);
				}
			}
			if (_rightMap.size() == 0)  { // 
				JOptionPane.showMessageDialog(null, "Select atleast one Column Mapping" ,
						"Record HeaderMap Dialog",JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			// Now populate leftKey and rightKey
			leftKey = new String[_leftMap.size()];rightKey = new String[_leftMap.size()];
			for (int i=0; i < _leftMap.size(); i++) {
				leftKey[i] = rtmL.getModel().getColumnName(_leftMap.get(i));
				rightKey[i] = rtmR.getModel().getColumnName(_rightMap.get(i));
			}
			try {
				if (keycellm.isSelected() == true) {
					showColumnSelection();
					return;
				} else {
					d_recHead.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
					createGUI();
				}
			} catch (Exception ee) {
				d_m.setVisible(true);
				System.out.println("Exeption:"+ee.getMessage());
				ee.printStackTrace();
			} finally {
				d_recHead.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				d_recHead.dispose();
			}
			d_m.dispose(); 

		}
		if ("compareDataDiff".equals(e.getActionCommand())) {
			
			leftCols = new ArrayList<String>();
			rightCols = new ArrayList<String>();
			datatypeCols =  new ArrayList<String>();
			
			for (int i =0; i < _lCols.size(); i++) {
				if (_checkBC[i].isSelected() == false ) continue;
				rightCols.add(_rColCC[i].getSelectedItem().toString());
				leftCols.add(_lCols.get(i));
				datatypeCols.add(_dataTYPEC[i].getSelectedItem().toString());
			}
			if (leftCols.size() == 0)  { // 
				JOptionPane.showMessageDialog(null, "Select atleast one Column Mapping" ,
						"Record ColumnMap Dialog",JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			try {
					d_recColumn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
					createDataDiffGUI();
			} catch (Exception ee) {
				d_m.setVisible(true);
				System.out.println(" Columns Exeption:"+ee.getMessage());
				ee.printStackTrace();
			} finally {
				d_recColumn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				d_recColumn.dispose();
			}
			d_m.dispose(); 
		}
		
		if ("showcellkey".equals(e.getActionCommand())) {
			rtmnonKeyUnMatchDataTable.table.setDefaultRenderer(Object.class,  new HighlightCellRenderer());
			rtmnonKeyUnMatchDataTable.table.repaint();
			
		}
		
		if ("showcell".equals(e.getActionCommand()) || "showcellr".equals(e.getActionCommand())) {
			
			// Index has been shifted as new column has been added for once
			if (indexadded == false) {
				Integer[] holdl = new Integer[_leftMap.size()];
				Integer[] holdr = new Integer[_rightMap.size()];
				_leftMap.copyInto(holdl);_rightMap.copyInto(holdr);
				_leftMap.clear();_rightMap.clear();
				
				for (int i: holdl)
					_leftMap.add(++i);
				for (int i: holdr)
					_rightMap.add(++i);
				indexadded = true;
			}
			
			if ("showcell".equals(e.getActionCommand())) {
				RTMDiffUtil rtmdiff = new RTMDiffUtil(rtmLnoMatch,_leftMap, rtmRnoMatch,_rightMap);
				HashMap<Integer, Vector<Integer>> diffIndex = rtmdiff.compareDiff(true);
				HashMap<Integer,Integer> matcheddiffIndex = rtmdiff.getDiffMatchedIndex();
				if ( diffIndex == null || diffIndex.size() == 0) {
					ConsoleFrame.addText("\n File Comparison Failed"
							+ "\n Or Primary Table has all new rows");
					return;
				}
				
				Set<Integer> s =  diffIndex.keySet();
				for (int index : s) {
					Integer matchI = matcheddiffIndex.get(index);
					if (matchI == null || diffIndex.get(index).size() == _leftMap.size() ) { // all new fields
						ConsoleFrame.addText("\n New Row at Index:"+index);
					} else {
						ConsoleFrame.addText("\n Primary table Index:" +index +" --> " + matchI+ " of Secondary Table" );
					}
				}
				
				// set the renderer for highlighting
				for (int k=0; k < _leftMap.size(); k++) 
					rtmLnoMatchTable.table.getColumnModel().getColumn(_leftMap.get(k)).setCellRenderer
					( new HighlightCellRenderer(diffIndex));
				rtmLnoMatchTable.table.repaint();
				
			} else { // Secondary Table Information
				RTMDiffUtil rtmdiff = new RTMDiffUtil(rtmRnoMatch,_rightMap,rtmLnoMatch,_leftMap);
				HashMap<Integer, Vector<Integer>> diffIndex = rtmdiff.compareDiff(true);
				HashMap<Integer,Integer> matcheddiffIndex = rtmdiff.getDiffMatchedIndex();
				if ( diffIndex == null || diffIndex.size() == 0) {
					ConsoleFrame.addText("\n File Comparison Failed"
							+ "\n Or Secondary Table has all new rows");
					return;
				}
				
				Set<Integer> s =  diffIndex.keySet();
				for (int index : s) {
					Integer matchI = matcheddiffIndex.get(index);
					if (matchI == null || diffIndex.get(index).size() == _rightMap.size() ) {
						ConsoleFrame.addText("\nNew Row at Index:"+index);
					} else {
						ConsoleFrame.addText("\n Secondary table Index:" +index +" --> " + matchI+ " of Primary Table" );
					}
				}
				
				// set the renderer for highlighting
				for (int k=0; k < _rightMap.size(); k++) 
					rtmRnoMatchTable.table.getColumnModel().getColumn(_rightMap.get(k)).setCellRenderer
					( new HighlightCellRenderer(diffIndex));
				rtmRnoMatchTable.table.repaint();
			}
		}
	} // end of action
	
	private class HighlightCellRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private HashMap<Integer, Vector<Integer>> _diffIndex;
		private boolean _matchwithFirst = false;
		

		public HighlightCellRenderer(HashMap<Integer, Vector<Integer>> diffIndex) {
			_diffIndex = diffIndex;
		}
		
		public HighlightCellRenderer() { // default is match with first
			_matchwithFirst = true;
		}
		
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			if (value == null ) return c; // for null value
			
			if (_matchwithFirst == false) {
				// If it is sorting row will change so default
				if (rtmLnoMatchTable.isSorting() == true || rtmRnoMatchTable.isSorting() == true ) {
					((JLabel )c).setForeground(Color.BLACK); // default color
					return c;
				}
				
				Vector<Integer> vc = _diffIndex.get(row);
				if (vc != null && vc.size() > 0 && vc.indexOf(column) != -1) 
					((JLabel )c).setForeground(Color.RED.darker()); // now select it
				else
					((JLabel )c).setForeground(Color.BLACK); // default color
				return c;
			} else { // match with First
				if (rtmnonKeyUnMatchDataTable.isSorting() == true  ) {
					((JLabel )c).setForeground(Color.BLACK); // default color
					return c;
				}
				if (row >= 1 ) {
					Object fircurr = table.getValueAt(row, 0); // count start from 0
					Object firprev = table.getValueAt(row-1, 0);
					if (fircurr == null || firprev == null || "".equals(fircurr.toString()) || "".equals(firprev.toString())) {
						((JLabel )c).setForeground(Color.BLACK);
						//System.out.println("zero column came:"+fircurr);
						return c;
					}
					
					Object colcurr = table.getValueAt(row, column);
					Object colprev = table.getValueAt(row -1, column);
					
					if ((colcurr == null && colprev != null) || (colcurr != null && colprev == null) ) {
						((JLabel )c).setForeground(Color.RED.darker()); // now select it
						return c;
					}
					if (colcurr == null && colprev == null)  {
						((JLabel )c).setForeground(Color.BLACK); // default color
						return c;
					}
					if (colcurr.toString().compareTo(colprev.toString()) != 0) {
						((JLabel )c).setForeground(Color.RED.darker()); // now select it
						return c;
					} else {
						((JLabel )c).setForeground(Color.BLACK); // default color
					}
				} else
					((JLabel )c).setForeground(Color.BLACK); // default color
						
				return c;
			}
		}
	} // End of HighlightCellRenderer
	
	
} // end of class
