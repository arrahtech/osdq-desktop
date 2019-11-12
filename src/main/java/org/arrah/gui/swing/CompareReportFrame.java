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

/* This file is used for comparing two reports
 * ( dimension and metric (aggregate ) and see 
 * what is different
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;




import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.arrah.framework.analytics.ReportDiffWrapper;
import org.arrah.framework.ndtable.ReportTableModel;

public class CompareReportFrame implements ActionListener {

	private ReportTableModel rtmL = null, rtmR = null;
	private JDialog d_m= null, d_recHead = null, d_recColumn=null;
	private JComboBox<String>[] _rColC,_rColCC, _dataTYPEC ;
	private JCheckBox[] _checkB, _checkBC;
	private Vector<String> _lCols, _rCols;
	private Vector<Integer> _leftMap, _rightMap;
	
	private JRadioButton keycellm;
	private String[] leftKey,rightKey; // added for report diff
	private ArrayList<String> leftCols,rightCols, datatypeCols; // added for report diff

	public CompareReportFrame() { // Default Constructor
		
	}
	
	public CompareReportFrame(ReportTableModel rtmL, ReportTableModel rtmR) { // Default left and right RTM
		this.rtmL = rtmL;
		this.rtmR = rtmR;
		createMapDialog();
	}	
	
	private void createReportDiffGUI() {
		ReportDiffWrapper reportrtmdiff = null;
		
		if (rtmL == null || rtmR == null ) {
			ConsoleFrame.addText("\n Comparing tables are Null");
			return;
		}
		String[] leftColA = new String[leftCols.size()];
		String[] rightColA = new String[rightCols.size()];
		String[] dataTypeA = new String[datatypeCols.size()];
		
		reportrtmdiff = new ReportDiffWrapper(rtmL,leftKey,datatypeCols.toArray(dataTypeA),leftCols.toArray(leftColA),
					rtmR,rightKey,rightCols.toArray(rightColA));
	
//		System.out.println("Left Dimenson:" + Arrays.toString(leftKey));
//		System.out.println("Left Metric:" + Arrays.toString(leftColA));
//		System.out.println("Aggr Type:" + Arrays.toString(dataTypeA));
//		
//		System.out.println("Right Dimenson:" + Arrays.toString(rightKey));
//		System.out.println("Right Metric:" + Arrays.toString(rightColA));
	
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
			
			Object[][] result = reportrtmdiff.showMetricDiff();
			String[][] keydata = reportrtmdiff.getmatchedKeyData();
			
	//		for (int i = 0; i < result.length; i++) {
	//			System.out.print(Arrays.toString(keydata[i]));
	//			System.out.println(Arrays.toString(result[i]));
	//		}
			
			ArrayList<String[]> nomatchData = reportrtmdiff.getNomatchKeyData();
			
			for (int i=0; i < result.length ; i++ ) {
				
				// if there is no match keydata would be empty so better use keys itself
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
				rtm.addFillRow(doubleKey, result[i]);
			}
		} catch (Exception e) {
			ConsoleFrame.addText("Compare Report error:" +  e.getLocalizedMessage());
			e.printStackTrace();
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
		d_m.setTitle("Report Display Dialog");
		d_m.setLocation(50, 50);
		d_m.getContentPane().add(jp_p);
		d_m.pack();
		d_m.setVisible(true);

		return d_m;
	}
	
	private JDialog showHeaderMap() {
		// Header Making
		
		JPanel bjp = new JPanel();
		keycellm = new JRadioButton("Select Dimensions to Compare");
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(keycellm);
		keycellm.setSelected(true);
		bjp.add(keycellm);
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
			JLabel mapA = new JLabel("   Compares:   ",JLabel.TRAILING);
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
		d_recHead.setTitle("Report HeaderMap Dialog");
		d_recHead.setLocation(250, 100);
		d_recHead.getContentPane().add(jp_p);
		d_recHead.pack();
		d_recHead.setVisible(true);

		return d_recHead;

	}
	
	// Key already have been selected. Now select columns to diff
	private JDialog showColumnSelection() {
		
		// Header Making
		JLabel infoc= new JLabel("Please select the Metrics");
		
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
			JLabel mapA = new JLabel("   Compares:   ",JLabel.TRAILING);
			mapA.setForeground(Color.BLUE);
			jp.add(mapA);
			jp.add(_rColCC[i]);
			_dataTYPEC[i] = new JComboBox<String>(new String[] {"Sum","Absolute Sum","Average","Count","Unique Count"});
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

			boolean selected = selRadio.isSelected();
			if (selected == true) { // cell has been selected to uncheck keys
				for (int i=0; i <_checkB.length; i++)
				_checkB[i].setSelected(true);
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
					createReportDiffGUI();
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
	} // end of action
	
} // end of class
