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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.ndtable.ResultsetToRTM;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_NewConn;
import org.arrah.framework.rdbms.Rdbms_conn;


public class CompareTableInfoDialog implements ActionListener {
	private String _lTab;
	private Vector<String> _lCols, _rCols, _rTab;
	private Hashtable<String, String> _dbParam;
	private int colCount = 0;
	
	private JComboBox<String> _rTableC;
	private JComboBox<String>[] _rColC;
	private JDialog d_m;
	private JRadioButton mr, nomr;
	
	public CompareTableInfoDialog (String lTab, Vector<String> lCols, Vector<String>rTab, Vector<String>rCols,
			Hashtable<String, String> dbParam) {
		_lTab = lTab;
		_rTab = rTab;
		_lCols = lCols;
		_rCols = rCols;
		_dbParam = dbParam;
		colCount = lCols.size();
	}

	public JDialog createMapDialog() {

		TableItemListener tl = new TableItemListener();
		
		_rTableC = new JComboBox<String>();
		for (int i=0; i < _rTab.size(); i++ )
			_rTableC.addItem(_rTab.get(i));
		_rTableC.addItemListener(tl);
	
		_rColC = new JComboBox[colCount];
		
		for (int i =0; i < colCount; i++ ){
			_rColC[i] = new JComboBox<String>();
			for ( int j=0; j < _rCols.size() ; j++)
				_rColC[i].addItem(_rCols.get(j));
		}
		// Header Making
		JPanel jp = new JPanel(new SpringLayout());
		JLabel partition = new JLabel("Table:    ",JLabel.TRAILING);
		partition.setForeground(Color.BLUE);
		jp.add(partition);
		JLabel ltabL = new JLabel(_lTab,JLabel.TRAILING);
		jp.add(ltabL);
		JLabel partition1 = new JLabel("  Table:  ",JLabel.TRAILING);
		partition1.setForeground(Color.BLUE);
		jp.add(partition1);
		jp.add(_rTableC);
		
		
		JLabel dummy = new JLabel("",JLabel.TRAILING);
		jp.add(dummy);
		JLabel dummy1 = new JLabel("",JLabel.TRAILING);
		jp.add(dummy1);
		JLabel dummy3 = new JLabel("",JLabel.TRAILING);
		jp.add(dummy3);
		JLabel dummy4 = new JLabel("",JLabel.TRAILING);
		jp.add(dummy4);
		
		for (int i=0; i < colCount; i++) {
			JLabel colPartition = new JLabel("Column: ",JLabel.TRAILING);
			colPartition.setForeground(Color.BLUE);
			jp.add(colPartition);
			JLabel rColLabel = new JLabel(_lCols.get(i),JLabel.TRAILING);
			jp.add(rColLabel);
			JLabel mapA = new JLabel("   Columns:   ",JLabel.TRAILING);
			mapA.setForeground(Color.BLUE);
			jp.add(mapA);
			jp.add(_rColC[i]);

		}
		
		SpringUtilities.makeCompactGrid(jp, colCount+2, 4, 3, 3, 3, 3);
		JScrollPane jscrollpane1 = new JScrollPane(jp);
		if ((100 + (colCount * 35)) > 500)
			jscrollpane1.setPreferredSize(new Dimension(575, 400));
		else
			jscrollpane1.setPreferredSize(new Dimension(575, 75 + colCount * 35));

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
		d_m.setTitle("Table Comparison Dialog");
		d_m.setLocation(250, 100);
		d_m.getContentPane().add(jp_p);
		d_m.pack();
		d_m.setVisible(true);

		return d_m;
	}
	
	private class TableItemListener implements ItemListener {
		private int index = 0;
		private Vector avector[];
		

		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED ) {
				index = _rTableC.getSelectedIndex();
				String tableName = _rTab.get(index);
				try {
				Rdbms_NewConn newConn = new Rdbms_NewConn(_dbParam);
				 avector = newConn.populateColumn(tableName,null);	
				} catch ( SQLException ee) {
					ConsoleFrame.addText("\n Error in getting column Names for table:"+tableName+"\n"+ee.getMessage());
					System.out.println("SQL Error:" + ee.getMessage());
					return;
				}
				
				for (int i =0; i < colCount; i++ ){
					_rColC[i].removeAllItems();
					for ( int j=0; j < avector[0].size() ; j++)
						_rColC[i].addItem((String)(avector[0].get(j)));
				}

			}
		}
	} // End of TableListener

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("cancel".equals(e.getActionCommand())) 
			d_m.dispose();
		if ("compare".equals(e.getActionCommand())) {
			Vector<String> colName_v = new Vector<String>();
			for (int i =0; i < colCount; i++) {
				String colName = (String) _rColC[i].getSelectedItem();
				if (colName_v.contains(colName) == true) {
					JOptionPane.showMessageDialog(null, "Duplicate Mapping at Row:"+i,
							"Table Comparison Info Dialog",
							JOptionPane.INFORMATION_MESSAGE);
					
					return;
				} else 
					colName_v.add(colName);
				
			}
			// Send Information to table comparison
			QueryBuilder qb = new QueryBuilder(
					Rdbms_conn.getHValue("Database_DSN"), _lTab,
					Rdbms_conn.getDBType());
			String s1 = qb.get_selCol_query(_lCols.toArray(),"");
			
			try {
			d_m.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			Rdbms_conn.openConn();
			ResultSet resultset = Rdbms_conn.runQuery(s1); 
			Vector<BigInteger> hashNumber = ResultsetToRTM.getMD5Value(resultset);
			resultset.close();
			Rdbms_conn.closeConn();
			
			// Query to another table
			int index = _rTableC.getSelectedIndex();
			String tableName = _rTab.get(index);
			Rdbms_NewConn newConn = new Rdbms_NewConn(_dbParam);
			qb = new QueryBuilder(
					newConn.getHValue("Database_DSN"), tableName,
					newConn.getDBType());
			s1 = qb.get_selCol_query(colName_v.toArray(),"");
			
			if ( newConn.openConn() == true ) {
				resultset = newConn.runQuery(s1);
				boolean matval = false;
				if (mr.isSelected() == true) matval = true;
				ReportTableModel rtm = ResultsetToRTM.matchMD5Value(resultset,hashNumber,matval);
				resultset.close();
				newConn.closeConn();
				
				d_m.setVisible(false);
				
				ReportTable rt = new ReportTable(rtm);
				JDialog td_m = new JDialog();
				td_m.setTitle("Table Comparison Dialog");
				td_m.getContentPane().add(rt);
				td_m.setModal(true);
				td_m.setLocation(200, 100);
				td_m.pack();
				td_m.setVisible(true);
			}
			
			} catch (SQLException ee) {
				System.out.println("SQL Exeption in MD5:"+ee.getMessage());
			} catch (Exception ee) {
				System.out.println("Exeption in MD5:"+ee.getMessage());
			} finally {
				d_m.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				d_m.dispose();
			}
			
			d_m.dispose();
		}
			
		
	}
}
