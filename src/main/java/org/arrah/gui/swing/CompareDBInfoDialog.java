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

/* This file is used for getting info about 
 * comparing tables across DB
 * 
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
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
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.ndtable.ResultsetToRTM;
import org.arrah.framework.rdbms.Rdbms_NewConn;
import org.arrah.framework.rdbms.Rdbms_conn;


public class CompareDBInfoDialog implements ActionListener {
	private Vector<String> _lTab, _rTab;
	private Hashtable<String, String> _dbParam;
	private int tabCount = 0;
	
	private JCheckBox [] _uqiC;
	private JComboBox<String>[] _rTableC;
	private JDialog d_m;
	private JRadioButton mr, nomr;
	
	public boolean showDialog = true;
	
	public CompareDBInfoDialog (Vector<String>lTab, Vector<String>rTab, Hashtable<String, String> dbParam) {
		_lTab = lTab;
		_rTab = rTab;
		_dbParam = dbParam;
		tabCount = lTab.size();
	}
	public CompareDBInfoDialog() { // Default
		inputTable();
	}
	
	private void setFields(Vector<String>lTab, Vector<String>rTab, Hashtable<String, String> dbParam) {
		_lTab = lTab;
		_rTab = rTab;
		_dbParam = dbParam;
		tabCount = lTab.size();
	}

	public JDialog createMapDialog() {

		_rTableC = new JComboBox[tabCount];
		_uqiC = new JCheckBox[tabCount];
		
		for (int i =0; i < tabCount; i++ ){
			_rTableC[i] = new JComboBox<String>();
			_uqiC[i] = new JCheckBox();
			for ( int j=0; j < _rTab.size() ; j++)
				_rTableC[i].addItem(_rTab.get(j));
		}
		// Header Making
		JPanel jp = new JPanel(new SpringLayout());
		
		for (int i=0; i < tabCount; i++) {
			JLabel tabPartition = new JLabel("Table: ",JLabel.TRAILING);
			tabPartition.setForeground(Color.BLUE);
			jp.add(tabPartition);
			JLabel lTabLabel = new JLabel(_lTab.get(i),JLabel.TRAILING);
			jp.add(lTabLabel);
			JLabel mapA = new JLabel("   Maps   ",JLabel.TRAILING);
			mapA.setForeground(Color.BLUE);
			jp.add(mapA);
			jp.add(_rTableC[i]);
			_uqiC[i].setHorizontalAlignment(JCheckBox.CENTER);
			jp.add(_uqiC[i]);

		}
		SpringUtilities.makeCompactGrid(jp, tabCount, 5, 3, 3, 3, 3);
		JScrollPane jscrollpane1 = new JScrollPane(jp);
		if (tabCount * 35 > 400)
			jscrollpane1.setPreferredSize(new Dimension(575, 400));
		else
			jscrollpane1.setPreferredSize(new Dimension(575, tabCount * 35));

		JPanel bp = new JPanel();
		ButtonGroup gp1 = new ButtonGroup();
		mr = new JRadioButton("Show Matched Record");
		nomr = new JRadioButton("Show No-Matched Record");
		nomr.setSelected(true);
		gp1.add(mr); gp1.add(nomr);
		bp.add(mr); bp.add(nomr);
		
		JButton ok = new JButton("Compare");;
		ok.setActionCommand("compare");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(jscrollpane1, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		d_m = new JDialog();
		d_m.setModal(true);
		d_m.setTitle("Map Dialog");
		d_m.setLocation(250, 100);
		d_m.getContentPane().add(jp_p);
		d_m.pack();
		d_m.setVisible(true);

		return d_m;
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("cancel".equals(e.getActionCommand())) 
			d_m.dispose();
		if ("compare".equals(e.getActionCommand())) {
			Vector<String> tabName_v = new Vector<String>();
			Vector<String> ltabName_v = new Vector<String>();
			
			for (int i =0; i < tabCount; i++) {
				if (_uqiC[i].isSelected() == false) continue; // If not selected no need to compare
				
				String tabName = (String) _rTableC[i].getSelectedItem();
				if (tabName_v.contains(tabName) == true) {
					JOptionPane.showMessageDialog(null, "Duplicate Mapping at Row:"+i,
							"DB Comparison Info Dialog",
							JOptionPane.INFORMATION_MESSAGE);
					
					return;
				} else {
					tabName_v.add(tabName);
					ltabName_v.add(_lTab.get(i));
				}
				
			}
			if (tabName_v.size() == 0 ) {
				JOptionPane.showMessageDialog(null, "Please select the tables to compare",
						"DB Comparison Info Dialog",
						JOptionPane.INFORMATION_MESSAGE);
				return;
				
			}
			// Send Information to table comparison in loop
			boolean match = false;
			if (mr.isSelected() == true) match = true;
			JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
			
			
			try {
				d_m.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				
				for (int i=0 ; i <tabName_v.size(); i++ ) {
				ReportTableModel rtm = ResultsetToRTM.compareTable(ltabName_v.get(i), _dbParam, tabName_v.get(i), match);
				ReportTable rt = new ReportTable(rtm);
				
				tabPane.addTab(ltabName_v.get(i)+"--"+tabName_v.get(i), rt);
				} // end of For Loop
				
			} catch (SQLException ee) {
				System.out.println("SQL Exeption in MD5:"+ee.getMessage());
			} catch (Exception ee) {
				System.out.println("Exeption in MD5:"+ee.getMessage());
			} finally {
				d_m.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				d_m.dispose();
			}
			
			JDialog db_d = new JDialog();
			db_d.setTitle("DB Comparison Tab");
			db_d.getContentPane().add(tabPane);
			db_d.setModal(true);
			db_d.setLocation(200, 100);
			db_d.pack();
			db_d.setVisible(true);
			
			d_m.dispose();
		}
	}
	
	private void inputTable() {
	TestConnectionDialog tcd = new TestConnectionDialog(1); // new Connection
	JOptionPane.showMessageDialog(null, "Choose Table to Compare from another Data Source",
			"Table Comparison Dialog",
			JOptionPane.INFORMATION_MESSAGE);
	tcd.createGUI();
	Hashtable <String,String> _fileParse = tcd.getDBParam();
	if (_fileParse == null ) { showDialog = false; return; } // do not show dialog
	
	try {
		Rdbms_NewConn newConn = new Rdbms_NewConn(_fileParse);
		if ( newConn.openConn() == false) return;
		newConn.populateTable();
		Vector<String> table_v = newConn.getTable();
		newConn.closeConn();
		setFields(Rdbms_conn.getTable(),table_v,_fileParse);
		
	} catch (Exception e1) {
		System.out.println(e1.getMessage());
		e1.printStackTrace();
	} finally {
		
	}
	}
}
