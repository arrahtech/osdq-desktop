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
 * This file is used for showing the interface 
 * for number date profiling. It fetches data from 
 * database and pass it on to ReportTable
 *
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class ProfilePane extends JPanel implements ActionListener {

	// These members are used by public methods :Global Variable

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CheckBoxGroup aggr, less, more, between1, between2;
	private JFormattedTextField text2, text3, text4, text5, text6, text7;
	private Hashtable<String, String> table_info;
	private String num_only = "For Number Type Only";
	private JPanel tp;

	public ReportTable _table; // For Reporting

	public ProfilePane(boolean isDate, Hashtable<String, String> map) {
		table_info = map;
		_table = new ReportTable("", "", "", "", "", "");
		SpringLayout layout = new SpringLayout();
		this.setLayout(layout);
		tp = new JPanel();
		tp.add(_table);
		this.add(tp);
		JPanel lp = createLeftPane();
		this.add(lp);

		JButton button7 = new JButton("Number Profile");
		button7.setMnemonic('N');
		Border raisedBevel = BorderFactory.createRaisedBevelBorder();
		button7.setBorder(raisedBevel);
		button7.setPreferredSize(new Dimension(100, 50));
		button7.addActionListener(this);
		button7.addKeyListener(new KeyBoardListener());
		this.add(button7);

		layout.putConstraint(SpringLayout.NORTH, lp, 0, SpringLayout.NORTH,
				this);
		layout.putConstraint(SpringLayout.WEST, lp, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, button7, -75,
				SpringLayout.SOUTH, lp);
		layout.putConstraint(SpringLayout.WEST, button7, 15, SpringLayout.EAST,
				lp);
		layout.putConstraint(SpringLayout.NORTH, tp, 5, SpringLayout.SOUTH, lp);
		layout.putConstraint(SpringLayout.WEST, tp, 0, SpringLayout.WEST, this);
		this.setPreferredSize(new Dimension(700, 460));
	}

	// Private Interface
	private JPanel createLeftPane() {
		JPanel leftPane = new JPanel();
		leftPane.setLayout(new GridLayout(5, 0, 5, 5));

		JPanel pane1 = new JPanel();
		pane1.setLayout(new GridLayout(0, 1, 1, 1));
		JLabel label1 = new JLabel("  On Complete Column");
		pane1.add(label1);
		aggr = new CheckBoxGroup();
		leftPane.add(getCheckBoxHeader(pane1, aggr));

		JPanel pane2 = new JPanel();
		pane2.setLayout(new GridLayout(0, 2, 1, 1));
		JLabel label2 = new JLabel("  Value less than");
		pane2.add(label2);
		text2 = new JFormattedTextField(new DecimalFormat());
		text2.setToolTipText(num_only);
		pane2.add(text2);
		less = new CheckBoxGroup();
		pane2.setToolTipText(num_only);
		leftPane.add(getCheckBoxHeader(pane2, less));

		JPanel pane3 = new JPanel();
		pane3.setLayout(new GridLayout(0, 2, 1, 1));
		JLabel label3 = new JLabel("  Value More than");
		pane3.add(label3);
		text3 = new JFormattedTextField(new DecimalFormat());
		text3.setToolTipText(num_only);
		pane3.add(text3);
		more = new CheckBoxGroup();
		pane3.setToolTipText(num_only);
		leftPane.add(getCheckBoxHeader(pane3, more));

		JPanel pane4 = new JPanel();
		pane4.setLayout(new GridLayout(0, 3, 10, 1));
		JLabel label4 = new JLabel("  Value in Between");
		pane4.add(label4);
		text4 = new JFormattedTextField(new DecimalFormat());
		text4.setToolTipText(num_only);
		pane4.add(text4);
		text5 = new JFormattedTextField(new DecimalFormat());
		text5.setToolTipText(num_only);
		pane4.add(text5);
		between1 = new CheckBoxGroup();
		pane4.setToolTipText(num_only);
		leftPane.add(getCheckBoxHeader(pane4, between1));

		JPanel pane5 = new JPanel();
		pane5.setLayout(new GridLayout(0, 3, 10, 1));
		JLabel label5 = new JLabel("  Value in Between");
		pane5.add(label5);
		text6 = new JFormattedTextField(new DecimalFormat());
		text6.setToolTipText(num_only);
		pane5.add(text6);
		text7 = new JFormattedTextField(new DecimalFormat());
		text7.setToolTipText(num_only);
		pane5.add(text7);
		between2 = new CheckBoxGroup();
		pane5.setToolTipText(num_only);
		leftPane.add(getCheckBoxHeader(pane5, between2));

		return leftPane;
	}

	private class CheckBoxGroup

	{
		JPanel parent;
		JCheckBox sum, count, min, max, avg;

		public CheckBoxGroup() {
			parent = new JPanel();
			parent.setLayout(new GridLayout(0, 5, 5, 5));

			sum = new JCheckBox("sum");
			sum.setToolTipText(num_only);
			parent.add(sum);
			count = new JCheckBox("count");
			parent.add(count);
			min = new JCheckBox("min");
			parent.add(min);
			max = new JCheckBox("max");
			parent.add(max);
			avg = new JCheckBox("avg");
			avg.setToolTipText(num_only);
			parent.add(avg);

		}

		public JPanel getPane() {
			return parent;
		}

		public String getStatus() {
			/*
			 * The protocol define here is Y for Selected, N for not Selected
			 * Order should be index,Count,Avg,Max,Min,Sum Example : 2YNNYN ,
			 * 5YYYYY, 0NNNNN
			 */
			int count_index = 0;
			String status = "";

			if (count.isSelected()) {
				count_index++;
				status += "Y";
			} else
				status += "N";
			if (avg.isSelected()) {
				count_index++;
				status += "Y";
			} else
				status += "N";
			if (max.isSelected()) {
				count_index++;
				status += "Y";
			} else
				status += "N";
			if (min.isSelected()) {
				count_index++;
				status += "Y";
			} else
				status += "N";
			if (sum.isSelected()) {
				count_index++;
				status += "Y";
			} else
				status += "N";

			String new_status = new String(count_index + status);
			return new_status;
		}
	}

	private JPanel getCheckBoxHeader(JPanel header, CheckBoxGroup chk) {
		JPanel chkHeader = new JPanel();
		chkHeader.setLayout(new GridLayout(2, 0));

		chkHeader.add(header);
		chkHeader.add(chk.getPane());
		Border lowerEtched = BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED);
		chkHeader.setBorder(lowerEtched);
		return chkHeader;

	}

	private String[] createColValue(ResultSet rs, String sel) {
		String values[] = { "" };
		String count_str = "", avg_str = "", max_str = "", min_str = "", sum_str = "";
		try {

			if (sel.charAt(1) == 'Y')
				count_str = rs.getString("row_count");
			if (sel.charAt(2) == 'Y')
				avg_str = rs.getString("avg_count");
			if (sel.charAt(3) == 'Y')
				max_str = rs.getString("max_count");
			if (sel.charAt(4) == 'Y')
				min_str = rs.getString("min_count");
			if (sel.charAt(5) == 'Y')
				sum_str = rs.getString("sum_count");

			values = new String[] { count_str, avg_str, max_str, min_str,
					sum_str };

		} catch (SQLException sqlexc) {
			ConsoleFrame.addText("\n ERROR: Createing Column Value Exception");
			JOptionPane.showMessageDialog(null, sqlexc.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
		return values;
	}

	private long getDuplicateCount(String dup_count, String dup_dist) {
		long i_dup_count = 0L;
		i_dup_count = Math.round(Double.parseDouble(dup_count)
				- Double.parseDouble(dup_dist));
		return i_dup_count;
	}

	/* Mouse Event Handler */
	public void actionPerformed(ActionEvent e) {

		// If nothing is selected
		if (table_info == null) {
			JOptionPane.showMessageDialog(null, "Select a Column to Profile",
					"Error Message", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String dsn_str = "" + table_info.get("Schema");
		String type_str = "" + table_info.get("Type");
		String tbl_str = "" + table_info.get("Table");
		String col_str = "" + table_info.get("Column");

		// Instantiate QueryBuilder
		QueryBuilder q_factory = new QueryBuilder(dsn_str, tbl_str, col_str,
				Rdbms_conn.getDBType());

		// Find out where the event occured
		String clicked_but = ((JButton) e.getSource()).getText();

		// Open the connection
		try {
			Rdbms_conn.openConn();
		} catch (SQLException sqlexc) {
			ConsoleFrame.addText("\n Open Connection Exception");
			JOptionPane.showMessageDialog(null, sqlexc.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
		/* Number Profile button is clicked */

		/* Declare the variables */

		String aggr_sel = "", less_sel = "", more_sel = "", bet1_sel = "", bet2_sel = "";
		String aggr_query = "", less_query = "", more_query = "", bet1_query = "", bet2_query = "", null_query = "", zero_query = "", neg_query = "";
		String less_val = "", more_val = "", b1_less_val = "", b1_more_val = "", b2_less_val = "", b2_more_val = "";
		String[] values = { "" };
		String aggr_count = "", less_count = "", more_count = "", bet1_count = "", bet2_count = "";
		String aggr_dist = "", less_dist = "", more_dist = "", bet1_dist = "", bet2_dist = "";
		boolean dup_chk = true;
		long aggr_dup_count = 0, less_dup_count = 0, more_dup_count = 0, bet1_dup_count = 0, bet2_dup_count = 0;

		// Get aggregate Value
		aggr_sel = aggr.getStatus();

		aggr_query = q_factory.aggr_query(aggr_sel, 0, "0", "0");
		if (dup_chk) {
			aggr_count = q_factory.aggr_query("1YNNNN", 0, "0", "0");
			aggr_dist = q_factory.dist_count_query(0, "0", "0");
		}

		// get the TextField Area Less Than
		if (text2.getValue() != null) {
			less_val = text2.getValue().toString();
			less_sel = less.getStatus();
			less_query = q_factory.aggr_query(less_sel, 1, less_val, "0");
			if (dup_chk) {
				less_count = q_factory.aggr_query("1YNNNN", 1, less_val, "0");
				less_dist = q_factory.dist_count_query(1, less_val, "0");
			}

		}

		// get the TextField Area more than
		if (text3.getValue() != null) {
			more_val = text3.getValue().toString();
			more_sel = more.getStatus();
			more_query = q_factory.aggr_query(more_sel, 2, "0", more_val);
			if (dup_chk) {
				more_count = q_factory.aggr_query("1YNNNN", 2, "0", more_val);
				more_dist = q_factory.dist_count_query(2, "0", more_val);
			}
		}

		// Get first in between value
		if (text4.getValue() != null && text5.getValue() != null) {
			b1_less_val = text4.getValue().toString();
			b1_more_val = text5.getValue().toString();
			if (new Double(b1_less_val).doubleValue() > new Double(b1_more_val)
					.doubleValue()) {
				ConsoleFrame
						.addText("\n WARNING:Less Val is more than More Value");
			} else {
				bet1_sel = between1.getStatus();
				bet1_query = q_factory.aggr_query(bet1_sel, 3, b1_less_val,
						b1_more_val);
				if (dup_chk) {
					bet1_count = q_factory.aggr_query("1YNNNN", 3, b1_less_val,
							b1_more_val);
					bet1_dist = q_factory.dist_count_query(3, b1_less_val,
							b1_more_val);
				}
			}
		}

		// Get second in between value
		if (text6.getValue() != null && text7.getValue() != null) {
			b2_less_val = text6.getValue().toString();
			b2_more_val = text7.getValue().toString();
			if (new Double(b2_less_val).doubleValue() > new Double(b2_more_val)
					.doubleValue()) {
				ConsoleFrame.addText("\n Less Val is more than More Value");
			} else {
				bet2_sel = between2.getStatus();
				bet2_query = q_factory.aggr_query(bet2_sel, 3, b2_less_val,
						b2_more_val);
				if (dup_chk) {
					bet2_count = q_factory.aggr_query("1YNNNN", 3, b2_less_val,
							b2_more_val);
					bet2_dist = q_factory.dist_count_query(3, b2_less_val,
							b2_more_val);
				}
			}
		}

		/* Put inside table */
		ReportTable table = new ReportTable(less_val, more_val, b1_less_val,
				b1_more_val, b2_less_val, b2_more_val);

		try {
			// Change the cursor
			this.getTopLevelAncestor().setCursor(
					java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

			// First Column population

			if (aggr_query.equals("") == false) {
				ResultSet rs = Rdbms_conn.runQuery(aggr_query);
				while (rs.next()) {
					values = createColValue(rs, aggr_sel);
				}

				rs.close();
				table.setColValue(1, values);
			}
			// Get the value for duplicate
			if (aggr_count.equals("") == false && aggr_dist.equals("") == false) {
				String ag = "", dg = "";
				ResultSet rs = Rdbms_conn.runQuery(aggr_count);
				while (rs.next())
					ag = rs.getString("row_count");
				rs.close();

				rs = Rdbms_conn.runQuery(aggr_dist);
				while (rs.next())
					dg = rs.getString("dist_count");
				rs.close();

				aggr_dup_count = getDuplicateCount(ag, dg);
				table.setTableValueAt(Long.toString(aggr_dup_count), 5, 1);
			}

			// Second Column population

			if (less_query.equals("") == false) {
				ResultSet rs = Rdbms_conn.runQuery(less_query);
				while (rs.next()) {
					values = createColValue(rs, less_sel);
				}

				rs.close();
				table.setColValue(2, values);
			}
			// Get the value for duplicate
			if (less_count.equals("") == false && less_dist.equals("") == false) {
				String ag = "", dg = "";
				ResultSet rs = Rdbms_conn.runQuery(less_count);
				while (rs.next())
					ag = rs.getString("row_count");
				rs.close();

				rs = Rdbms_conn.runQuery(less_dist);
				while (rs.next())
					dg = rs.getString("dist_count");
				rs.close();

				less_dup_count = getDuplicateCount(ag, dg);
				table.setTableValueAt(Long.toString(less_dup_count), 5, 2);
			}
			// Third Column population

			if (more_query.equals("") == false) {
				ResultSet rs = Rdbms_conn.runQuery(more_query);
				while (rs.next()) {
					values = createColValue(rs, more_sel);
				}

				rs.close();
				table.setColValue(3, values);
			}
			// Get the value for duplicate
			if (more_count.equals("") == false && more_dist.equals("") == false) {
				String ag = "", dg = "";
				ResultSet rs = Rdbms_conn.runQuery(more_count);
				while (rs.next())
					ag = rs.getString("row_count");
				rs.close();

				rs = Rdbms_conn.runQuery(more_dist);
				while (rs.next())
					dg = rs.getString("dist_count");
				rs.close();

				more_dup_count = getDuplicateCount(ag, dg);
				table.setTableValueAt(Long.toString(more_dup_count), 5, 3);
			}
			// Fourth Column population

			if (bet1_query.equals("") == false) {
				ResultSet rs = Rdbms_conn.runQuery(bet1_query);
				while (rs.next()) {
					values = createColValue(rs, bet1_sel);
				}

				rs.close();
				table.setColValue(4, values);
			}
			// Get the value for duplicate
			if (bet1_count.equals("") == false && bet1_dist.equals("") == false) {
				String ag = "", dg = "";
				ResultSet rs = Rdbms_conn.runQuery(bet1_count);
				while (rs.next())
					ag = rs.getString("row_count");
				rs.close();

				rs = Rdbms_conn.runQuery(bet1_dist);
				while (rs.next())
					dg = rs.getString("dist_count");
				rs.close();
				bet1_dup_count = getDuplicateCount(ag, dg);
				table.setTableValueAt(Long.toString(bet1_dup_count), 5, 4);
			}
			// Ffth Column population

			if (bet2_query.equals("") == false) {
				ResultSet rs = Rdbms_conn.runQuery(bet2_query);
				while (rs.next()) {
					values = createColValue(rs, bet2_sel);
				}

				rs.close();
				table.setColValue(5, values);
			}
			// Get the value for duplicate
			if (bet2_count.equals("") == false && bet2_dist.equals("") == false) {
				String ag = "", dg = "";
				ResultSet rs = Rdbms_conn.runQuery(bet2_count);
				while (rs.next())
					ag = rs.getString("row_count");
				rs.close();

				rs = Rdbms_conn.runQuery(bet2_dist);
				while (rs.next())
					dg = rs.getString("dist_count");
				rs.close();
				bet2_dup_count = getDuplicateCount(ag, dg);
				table.setTableValueAt(Long.toString(bet2_dup_count), 5, 5);
			}

			// Null Zero Negative Population
			if (null_query.equals("") == false) {
				ResultSet rs = Rdbms_conn.runQuery(null_query);
				while (rs.next()) {
					table_info.put("Null_Count", rs.getString("equal_count"));
				}
				rs.close();
			}

			if (zero_query.equals("") == false) {
				ResultSet rs = Rdbms_conn.runQuery(zero_query);
				while (rs.next()) {
					table_info.put("Zero_Count", rs.getString("equal_count"));
				}
				rs.close();
			}

			if (neg_query.equals("") == false) {
				ResultSet rs = Rdbms_conn.runQuery(neg_query);
				while (rs.next()) {
					table_info.put("Neg_Count", rs.getString("row_count"));
				}
				rs.close();
			}

			Rdbms_conn.closeConn();

		} catch (SQLException sqlexc) {
			ConsoleFrame.addText("\n Running Query Exception");
			JOptionPane.showMessageDialog(null, sqlexc.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} finally {
			// Reset the cursor
			this.getTopLevelAncestor()
					.setCursor(
							java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}
		addTable(table);

	}

	private void addTable(ReportTable rt) {
		_table = rt;
		tp.removeAll();
		tp.add(_table);
		revalidate();
		repaint();
	}
}
