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

/* 
 * This file is used to create Time 
 * Profiling Panel. It runs the query and shows it
 * statistical value in ReportTable Panel object .
 *
 */


import java.awt.BorderLayout;
import java.awt.Graphics;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.arrah.framework.profile.TimeStatisticalAnalysis;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class TimeProfilerPanel extends JPanel  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _dsn, _table, _col;
	private ArrayList<Long> _colObj;
	private ReportTable freq_t,range_t,perc_t;
	private TimeStatisticalAnalysis sa;

	public TimeProfilerPanel(Hashtable<String, String> map) {

		_dsn = (String) map.get("Schema");
		_table = (String) map.get("Table");
		_col = (String) map.get("Column");
		if ( getDataforAnalysis() == true) 
				createAndShowGUI();
	};
	
	public TimeProfilerPanel(Long[] timearray) {
		sa = new TimeStatisticalAnalysis(timearray);
		_colObj = sa.getColObjectArray();
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
		
		_ta_p.addTab("Frequency Analysis", null, freq_t, "Frequency Analyis");
		_ta_p.addTab("Variation Analysis", null, range_t, "Variation Analysis");

		perc_t =  new ReportTable(sa.getPercTable());
		_ta_p.addTab("Percentile Analysis", null, perc_t,"Percentile Analysis");
		
		if (_colObj == null) return;
		
		Long[] temparray = new Long[_colObj.size()];
		temparray = _colObj.toArray(temparray);
		Arrays.sort(temparray);
		final Long[] newarray = temparray;
		
		ScatterPlotterPanel sc = new ScatterPlotterPanel(true) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			public Vector<Double> fillValues() {
				
				final int gc = getGC();
				if (gc == 0)
					return null;

				int counter = 0;
				double d = 0;
				double sum = 0;
				Vector<Double> vc = new Vector<Double>(20, 5);
				int i = 0;
				
				int colC = newarray.length;
				try {
					for (int c = 0; c < colC; c++) {
						
						d = newarray[c].doubleValue();
						counter++;
						if (counter <= gc) {
							sum += d;
							if (counter != gc)
								continue;
						}
						double avg = sum / counter;
						sum = 0;
						counter = 0;
						vc.add(i++, new Double(avg));
					}
				} catch (NumberFormatException e) {
					counter = 0;
					ConsoleFrame
							.addText("\n ERROR: Could not fill data into Cluster Chart");
					JOptionPane.showMessageDialog(null, e.getMessage(),
							"Error Message", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				// After loop breaks
				// Rounding off the values
				if (counter != 0 && Math.round((float) counter / gc) > 0) {
					double avg = sum / counter;
					vc.add(i, new Double(avg));
				}

				return vc;
			}
			
		};
		_ta_p.addTab("Cluster Analysis", null, sc,"Cluster Analysis");
		
		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);

		this.add(_ta_p,BorderLayout.CENTER);
	}
		

	private boolean getDataforAnalysis() {

		QueryBuilder s_prof = new QueryBuilder(_dsn, _table, _col,
				Rdbms_conn.getDBType());
		String query = s_prof.get_all_worder_query();
		
		try {
			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(query);
			
			/* Get Metadata and validate time field
			 */
			ResultSetMetaData rsmd = rs.getMetaData();
			String colLabel =  rsmd.getColumnLabel(1); // First Col should be like_wise
			int colType = rsmd.getColumnType(1);
			
			if (colLabel.equalsIgnoreCase("like_wise") == false ) {
				JOptionPane.showMessageDialog(null, "Could NOT fetch the column:"+_col,
						"Error Message", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if (!(colType == java.sql.Types.DATE || colType == java.sql.Types.TIME ||
					colType == java.sql.Types.TIMESTAMP)) {
				JOptionPane.showMessageDialog(null, _col+" Data type is not DATE/TIME/TIMESTAMP",
						"Error Message", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			_colObj = new ArrayList<Long>();
			
			while (rs.next()) {
				Date q_value = null;
				switch (colType) {
				case java.sql.Types.DATE:
					q_value = rs.getDate("like_wise"); break;
				case java.sql.Types.TIME:
					q_value = rs.getTime("like_wise"); break;
				case java.sql.Types.TIMESTAMP:
					q_value = rs.getTimestamp("like_wise"); break;
				}
				
				if (q_value == null)
					continue;
				_colObj.add(q_value.getTime());

			} // End of while loop

			rs.close();
			Rdbms_conn.closeConn();
		} catch (SQLException e) {
			ConsoleFrame.addText("\n ERROR:Regex Query execution failed");
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
			return false;
		} finally {
			// do something
			
		}
		Long[] temparray = new Long[_colObj.size()];
		temparray = _colObj.toArray(temparray);
		sa = new TimeStatisticalAnalysis(temparray);
		return true;
	} // end of getDataforAnalysis

	
} // end of class
