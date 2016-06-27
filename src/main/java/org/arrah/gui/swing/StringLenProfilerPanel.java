package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2013      * 
 *     http://www.arrahtec.org                 * 
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          * 
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* 
 * This file is used to create String Length 
 * Profiling Panel. It runs the query and shows it in
 * ReportTable Panel object.
 *
 */


import java.awt.BorderLayout;
import java.awt.Graphics;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;


import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.arrah.framework.profile.StatisticalAnalysis;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class StringLenProfilerPanel extends JPanel  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _dsn, _table, _col;
	private ArrayList<Integer> _colObj;
	private StatisticalAnalysis sa ;
	private ReportTable freq_t,range_t,perc_t;
	private boolean isNumber = false;


	public StringLenProfilerPanel(Hashtable<String, String> map) {

		_dsn = (String) map.get("Schema");
		_table = (String) map.get("Table");
		_col = (String) map.get("Column");
		getDataforAnalysis();
		createAndShowGUI();
	};

	public StringLenProfilerPanel(Object[] data) {
		sa = new StatisticalAnalysis(data);
		createAndShowGUI();
	};
	
	private void createAndShowGUI() {
		
		final JTabbedPane _ta_p = new JTabbedPane() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				super.paint(g);
				if (this.getSelectedIndex() != 3)
					return;
			}
		};
		freq_t = new ReportTable(sa.getFrequencyTable());
		range_t = new ReportTable(sa.getRangeTable());
		
		/* Change Name of Reporttable column for strlen*/
		Object[] colN = freq_t.getAllColName();
		colN[0] = "String Length";
		freq_t.getModel().setColumnIdentifiers(colN);
		
		colN = range_t.getAllColName();
		colN[0] = "String Length Range";
		colN[1] = "String Length Metric Value";
		range_t.getModel().setColumnIdentifiers(colN);
		
		// Change value of report Table provided it is filled up
		if (range_t.getModel().getRowCount() > 5)
		range_t.getRTMModel().setValueAt("Total String Length(in Bytes)", 4, 0);
	
		
		
		_ta_p.addTab("Frequency Analysis", null, freq_t, "Frequency Analyis");
		_ta_p.addTab("Variation Analysis", null, range_t, "Variation Analysis");

		isNumber = sa.isObjNumber();
		if (isNumber == true) {
			perc_t =  new ReportTable(sa.getPercTable());
			colN = perc_t.getAllColName();
			colN[1] = "String Length Upper Value";
			perc_t.getModel().setColumnIdentifiers(colN);
			
			_ta_p.addTab("Percentile Analysis", null, perc_t,"Percentile Analysis");
		}
		
		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);

		
		JLabel comment = new JLabel("<html><b><br>String Length (in Bytes) analysis of column data. \n </br></html>");
		this.add(comment,BorderLayout.NORTH);
		

		this.add(_ta_p,BorderLayout.CENTER);
	}
		

	private void getDataforAnalysis() {

		QueryBuilder s_prof = new QueryBuilder(_dsn, _table, _col,
				Rdbms_conn.getDBType());
		String query = s_prof.get_all_worder_query();
		
		try {
			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(query);
			_colObj = new ArrayList<Integer>();
			
			while (rs.next()) {

				String q_value = rs.getString("like_wise");
				if (q_value == null)
					continue;
				int strlen = q_value.length();
				_colObj.add(strlen);

			} // End of while loop

			rs.close();
			Rdbms_conn.closeConn();
		} catch (SQLException e) {
			ConsoleFrame.addText("\n ERROR:String Length execution failed");
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} finally {
			// do something
		}
		sa = new StatisticalAnalysis(_colObj.toArray());
	} // end of getDataforAnalysis

	
} // end of class
