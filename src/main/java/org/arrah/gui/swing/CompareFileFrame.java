package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2016      *
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
 * comparing files taken as RTM
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

import org.arrah.framework.analytics.RTMDiffUtil;
import org.arrah.framework.ndtable.ReportTableModel;

public class CompareFileFrame implements ActionListener {

	private ReportTableModel rtmL = null, rtmR = null;
	private JDialog d_m= null, d_recHead = null;
	private JComboBox<String>[] _rColC;
	private JCheckBox[] _checkB;
	private Vector<String> _lCols, _rCols;
	private Vector<Integer> _leftMap, _rightMap;
	private ReportTableModel rtmLnoMatch = null, rtmRnoMatch = null, rtmnonKeyUnMatchData = null;
	private ReportTable rtmLnoMatchTable = null, rtmRnoMatchTable = null, rtmnonKeyUnMatchDataTable = null;
	private boolean indexadded = false;
	
	private JRadioButton cellm,keym;

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
		cellm = new JRadioButton("Cell Based Matching");
		keym = new JRadioButton("PrimaryKey Based Match");
		ButtonGroup bg = new ButtonGroup();
		bg.add(cellm);bg.add(keym);
		cellm.setSelected(true);
		bjp.add(cellm);bjp.add(keym);
		keym.addActionListener(this);
		cellm.addActionListener(this);
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
			try {
				d_recHead.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				createGUI();
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
