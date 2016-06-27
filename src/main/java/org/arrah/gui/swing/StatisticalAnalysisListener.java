package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2006      *
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
 * This file is used for statistical analysis
 * getting data from DB  
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class StatisticalAnalysisListener extends JPanel implements
		ActionListener, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Hashtable<String,String> map = null;
	public ReportTable _table_1 = null;
	public ReportTable _table_2 = null;
	public ReportTable _table_3 = null;
	public JPanel top;

	private String _dsn, _type, _condition, _table, _col;

	public StatisticalAnalysisListener(Hashtable<String,String> _map) {
		map = _map;
	}

	public void actionPerformed(ActionEvent e) {
		_dsn = (String) map.get("Schema");
		_type = (String) map.get("Type");
		_condition = (String) map.get("Condition");
		_table = (String) map.get("Table");
		_col = (String) map.get("Column");

		_table_1 = new ReportTable(new String[] { "Record Value", "Frequency",
				"% Freq." });
		_table_2 = new ReportTable(new String[] { "Range Metric",
				"Metric Value" });
		_table_3 = new ReportTable(new String[] { "Percentile %",
				"Record Upper Value", "Samples Below" });

		QueryBuilder a_q = new QueryBuilder(_dsn, _table, _col,
				Rdbms_conn.getDBType());

		String count_str, avg_str, max_str, min_str, sum_str;
		double count_d = 0, avg_d = 0, max_d = 0, min_d = 0, sum_d = 0;

		String aggr_query = a_q.aggr_query("5YYYYY", 0, "0", "0");
		try {

			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(aggr_query);

			while (rs.next()) {

				count_str = rs.getString("row_count");
				avg_str = rs.getString("avg_count");
				max_str = rs.getString("max_count");
				min_str = rs.getString("min_count");
				sum_str = rs.getString("sum_count");

				count_d = Double.valueOf(count_str).doubleValue();
				if (count_d <= 0) {
					JOptionPane.showMessageDialog(null, "No Data to Analyse",
							"Error Message", JOptionPane.ERROR_MESSAGE);
					rs.close();
					return;
				}

				avg_d = Double.valueOf(avg_str).doubleValue();
				max_d = Double.valueOf(max_str).doubleValue();
				min_d = Double.valueOf(min_str).doubleValue();
				sum_d = Double.valueOf(sum_str).doubleValue();

				_table_2.addFillRow(new String[] { "Total Record Count", count_str });
				_table_2.addFillRow(new String[] { "Maximum Value ", max_str });
				_table_2.addFillRow(new String[] { "Minimum Value ", min_str });
				_table_2.addFillRow(new String[] { "Range(Max-Min)",
						Double.toString(max_d - min_d) });
				_table_2.addFillRow(new String[] { "Total Record Sum", sum_str });
				_table_2.addRow();
				_table_2.addFillRow(new String[] { "Mean Value ", avg_str });

			}
			rs.close();

			String all_q = a_q.get_all_query();
			double variance = 0, aad = 0, skew = 0, kurt = 0;

			long[] perc_a = new long[21];
			int arr_i = 0;
			double[] perv_a = new double[21]; // To store value
			int dataset_c = 1;

			perc_a[0] = Math.round(count_d / 100);
			if (perc_a[0] == 0) {
				arr_i = 1;
				perv_a[0] = 0;
			}

			for (int i = 1; i < 20; i++) {
				perc_a[i] = Math.round(5 * i * count_d / 100);
				if (perc_a[i] == 0) {
					arr_i++;
					perv_a[i] = 0;
				}
			}
			perc_a[20] = Math.round(99 * count_d / 100);
			if (perc_a[20] == 0) {
				arr_i = 21;
				perv_a[20] = 0;
			}

			rs = Rdbms_conn.runQuery(all_q);
			while (rs.next()) {

				String q_value = rs.getString("like_wise");
				if (q_value == null || q_value.equals("")) {
					ConsoleFrame
							.addText("\n WARNING: Null or Empty value ignored - might affect result");
					continue;
				}
				double d = Double.valueOf(q_value).doubleValue();

				if ((arr_i < 21) == true && dataset_c == perc_a[arr_i]) {

					while (arr_i < 20 && perc_a[arr_i + 1] == perc_a[arr_i]) {
						perv_a[arr_i] = d;
						arr_i++;
					}
					perv_a[arr_i] = d;
					arr_i++;

				}

				aad += Math.abs(d - avg_d) / count_d;
				variance += Math.pow(d - avg_d, 2) / (count_d - 1);
				skew += Math.pow(d - avg_d, 3);
				kurt += Math.pow(d - avg_d, 4);

				dataset_c++;

			}
			rs.close();
			Rdbms_conn.closeConn();

			_table_2.addFillRow(new String[] { "Avg. Absolute Dev.(AAD)",
					Double.toString(aad) });
			_table_2.addFillRow(new String[] { "Variance",
					Double.toString(variance) });
			_table_2.addFillRow(new String[] { "Std. Dev.(SD)",
					Double.toString(Math.sqrt(variance)) });
			_table_2.addFillRow(new String[] { "Std. Error of Mean(SE)",
					Double.toString(Math.sqrt(variance) / Math.sqrt(count_d)) });
			_table_2.addFillRow(new String[] {
					"Skewness",
					Double.toString(skew
							/ ((count_d - 1) * Math.pow(variance, 1.5))) });
			_table_2.addFillRow(new String[] {
					"Kurtosis",
					Double.toString(kurt
							/ ((count_d - 1) * Math.pow(variance, 2))) });

			_table_3.addFillRow(new String[] { "1", Double.toString(perv_a[0]),
					Long.toString(perc_a[0]) });
			for (int i = 1; i < 20; i++) {
				_table_3.addFillRow(new String[] { Integer.toString(i * 5),
						Double.toString(perv_a[i]), Long.toString(perc_a[i]) });
			}
			_table_3.addFillRow(new String[] { "99",
					Double.toString(perv_a[20]), Long.toString(perc_a[20]) });

			_table_2.addRow();
			_table_2.addFillRow(new String[] { "Mid Range Value",
					Double.toString((max_d + min_d) / 2) });
			
			if (perc_a[0] > 0)
			_table_2.addFillRow(new String[] { "Mid Range(1%-99%)",
					Double.toString((perv_a[0] + perv_a[20]) / 2) });
			if (perc_a[1] > 0)
			_table_2.addFillRow(new String[] { "Mid Range(5%-95%)",
					Double.toString((perv_a[1] + perv_a[19]) / 2) });
			if (perc_a[2] > 0)
			_table_2.addFillRow(new String[] { "Mid Range(10%-90%)",
					Double.toString((perv_a[2] + perv_a[18]) / 2) });
			if (perc_a[3] > 0)
			_table_2.addFillRow(new String[] { "Mid Range(15%-85%)",
					Double.toString((perv_a[3] + perv_a[17]) / 2) });
			if (perc_a[4] > 0)
			_table_2.addFillRow(new String[] { "Mid Range(20%-80%)",
					Double.toString((perv_a[4] + perv_a[16]) / 2) });
			if (perc_a[5] > 0)
			_table_2.addFillRow(new String[] { "Mid Range(25%-75%)",
					Double.toString((perv_a[5] + perv_a[15]) / 2) });
			if (perc_a[6] > 0)
			_table_2.addFillRow(new String[] { "Mid Range(30%-70%)",
					Double.toString((perv_a[6] + perv_a[14]) / 2) });
			if (perc_a[7] > 0)
			_table_2.addFillRow(new String[] { "Mid Range(35%-65%)",
					Double.toString((perv_a[7] + perv_a[13]) / 2) });
			if (perc_a[8] > 0)
			_table_2.addFillRow(new String[] { "Mid Range(40%-60%)",
					Double.toString((perv_a[8] + perv_a[12]) / 2) });

		} catch (Exception sql_e) {
			ConsoleFrame.addText("\n ERROR: Advance Query execution failed");
			JOptionPane.showMessageDialog(null, sql_e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String query = a_q.get_freq_all_query();

		try {

			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(query);
			while (rs.next()) {
				String col_name = rs.getString("like_wise");
				String col_count = rs.getString("row_count");

				double col_count_d = Double.valueOf(col_count).doubleValue();

				_table_1.addFillRow(new String[] { col_name, col_count,
						Double.toString((col_count_d / count_d) * 100), });

			}
			rs.close();
			Rdbms_conn.closeConn();

		} catch (Exception sql_e) {
			ConsoleFrame.addText("\n ERROR: Frequency Query execution failed");
			JOptionPane.showMessageDialog(null, sql_e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
			return;
		}

		createAndShowGUI();
	}

	private void createAndShowGUI() {

		JLabel cond_l = new JLabel("Condition: " + _condition);
		JLabel time_l = new JLabel("Profile Time: "
				+ new Date(System.currentTimeMillis()).toString());
		JLabel col_type_l = new JLabel("  Column Type: " + _type);

		JLabel sch_l = new JLabel("  DSN: " + _dsn);
		JLabel table_l = new JLabel("Table: " + _table);
		JLabel col_l = new JLabel("Column: " + _col);
		top = new JPanel();
		top.setLayout(new GridLayout(2, 3));
		top.add(sch_l);
		top.add(table_l);
		top.add(col_l);
		top.add(col_type_l);
		top.add(cond_l);
		top.add(time_l);

		JTabbedPane _ta_p = new JTabbedPane();
		_ta_p.addTab("Frequency Analysis", null, _table_1, "Frequency Analyis");
		_ta_p.addTab("Variation Analysis", null, _table_2, "Variation Analysis");
		_ta_p.addTab("Percentile Analysis", null, _table_3,"Percentile Analysis");

		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		JFrame frame = new JFrame("Advance Number Analyis");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create and set up the content pane.
		frame.getContentPane().add(top, BorderLayout.PAGE_START);
		frame.getContentPane().add(_ta_p, BorderLayout.CENTER);

		// Create Menubar
		JMenuBar menubar = new JMenuBar();
		frame.setJMenuBar(menubar);

		JMenu file_m = new JMenu("File");
		file_m.setMnemonic('F');
		menubar.add(file_m);

		JMenuItem open_m = new JMenuItem("Save");
		open_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
		file_m.add(open_m);
		open_m.addActionListener(new FileActionListener(this, 3));

		// Display the window.
		frame.setLocation(125, 75);
		frame.pack();
		frame.setVisible(true);

	}

}
