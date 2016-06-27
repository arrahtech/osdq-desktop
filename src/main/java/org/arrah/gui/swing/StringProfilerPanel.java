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

/* 
 * This file is used to create String Profiling *
 * Panel. It runs the query and shows it in     *
 * ReportTable object.
 *
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.util.KeyValueParser;

public class StringProfilerPanel extends JPanel implements ActionListener,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel _t_pane = new JPanel();
	private JPanel _b_pane = new JPanel();
	private boolean _distinct = false;
	private JComboBox<String> q_s;
	private JCheckBox d_c = null;
	private JButton sp_but;
	private String _dsn, _table, _col, _type;

	public JLabel q1_c, q2_c, q3_c; // need for reporting
	public Hashtable <String,String> _h_info = null; // need for reporting
	public QPanel qp_1, qp_2, qp_3; // need for reporting
	public JTextArea r_c; // Need for Reporting
	public JLabel r_t; // Need for reporting
	public ReportTable qtable;

	public StringProfilerPanel(Hashtable<String, String> map) {
		qtable = new ReportTable(new String[] { "Pattern_1", "Pattern_2",
				"Pattern_3" });
		_dsn = (String) map.get("Schema");
		_type = (String) map.get("Type");
		_table = (String) map.get("Table");
		_col = (String) map.get("Column");
		_h_info = map;
		r_c = new JTextArea("No Comments. ");
		createAndShowGUI();
	};

	public void createAndShowGUI() {

		// Create Top & Bottom Pane
		createTopPane();
		createBotPane();
		
		// Create and set up the content pane.
		 _t_pane.setPreferredSize(new Dimension(600, 160));
		JScrollPane t_view = new JScrollPane(_t_pane);

		// Table has ScrollPane
		_b_pane.setPreferredSize(new Dimension(640, 420));
		JScrollPane b_view = new JScrollPane(_b_pane);

		// Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(t_view);
		splitPane.setBottomComponent(b_view);

		Dimension minimumSize = new Dimension(100, 50);
		t_view.setMinimumSize(minimumSize);
		b_view.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(175); // XXX: ignored in some releases
											// of Swing. bug 4101306

		splitPane.setPreferredSize(new Dimension(700, 675));

		splitPane.setOpaque(true); // content panes must be opaque
		this.add(splitPane);
	}

	private void createTopPane() {

		SpringLayout layout = new SpringLayout();
		_t_pane.setLayout(layout);

		r_t = new JLabel("Profile Time: "
				+ new Date(System.currentTimeMillis()).toString());
		_t_pane.add(r_t);

		JButton ac = new JButton("Comment");
		ac.setActionCommand("comment");
		ac.addKeyListener(new KeyBoardListener());
		ac.addActionListener(this);
		_t_pane.add(ac);

		JButton sr = new JButton("Save Report");
		sr.setActionCommand("Save");
		sr.addKeyListener(new KeyBoardListener());
		sr.addActionListener(new FileActionListener(this, 2));
		_t_pane.add(sr);

		JPanel centerPane = new JPanel();
		centerPane.setLayout(new GridLayout(3, 0));
		qp_1 = new QPanel();
		qp_2 = new QPanel();
		qp_3 = new QPanel();

		centerPane.add(qp_1.createQPanel("Pattern_1:"));
		centerPane.add(qp_2.createQPanel("Pattern_2:"));
		centerPane.add(qp_3.createQPanel("Pattern_3:"));

		// Create Border for center pane
		EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
		BevelBorder bevelBorder = new BevelBorder(BevelBorder.LOWERED);
		CompoundBorder compoundBorder = new CompoundBorder(emptyBorder,
				bevelBorder);
		centerPane.setBorder(new CompoundBorder(compoundBorder, emptyBorder));

		_t_pane.add(centerPane);
		JPanel query_panel = query_sel_pane();
		_t_pane.add(query_panel);

		sp_but = new JButton("Search");
		sp_but.setMnemonic('e');
		sp_but.addKeyListener(new KeyBoardListener());
		sp_but.addActionListener(this);
		_t_pane.add(sp_but);

		// Setting the layout
		layout.putConstraint(SpringLayout.NORTH, r_t, 5, SpringLayout.NORTH,
				_t_pane);
		layout.putConstraint(SpringLayout.WEST, r_t, 5, SpringLayout.WEST,
				_t_pane);
		layout.putConstraint(SpringLayout.NORTH, ac, 5, SpringLayout.NORTH,
				_t_pane);
		layout.putConstraint(SpringLayout.WEST, ac, 5, SpringLayout.EAST, r_t);
		layout.putConstraint(SpringLayout.NORTH, sr, 5, SpringLayout.NORTH,
				_t_pane);
		layout.putConstraint(SpringLayout.WEST, sr, 5, SpringLayout.EAST, ac);
		layout.putConstraint(SpringLayout.NORTH, query_panel, 5,
				SpringLayout.SOUTH, r_t);
		layout.putConstraint(SpringLayout.WEST, query_panel, 5,
				SpringLayout.WEST, _t_pane);
		layout.putConstraint(SpringLayout.NORTH, centerPane, 5,
				SpringLayout.SOUTH, r_t);
		layout.putConstraint(SpringLayout.WEST, centerPane, 5,
				SpringLayout.EAST, query_panel);
		layout.putConstraint(SpringLayout.NORTH, sp_but, 50,
				SpringLayout.NORTH, centerPane);
		layout.putConstraint(SpringLayout.WEST, sp_but, 5, SpringLayout.EAST,
				centerPane);

	}

	public class QPanel implements ActionListener, Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JComboBox<String> s_field;
		private JCheckBox ig_case;
		private JLabel s_l;
		private Hashtable<String,String> __h;

		public JPanel createQPanel(String panelN) {

			JPanel q_pane = new JPanel();
			SpringLayout layout = new SpringLayout();
			q_pane.setLayout(layout);
			s_l = new JLabel(panelN, JLabel.TRAILING);
			s_l.setToolTipText("Right-Click for REGEX Menu");

			s_field = new JComboBox<String>(new String[] { "%" });
			s_field.setEditable(true);
			s_field.setToolTipText("<html> <B> For 'String Like' Pattern can be </B> <br> %str, str% or %str% <br> <B> For 'Regex' </B> <br>look into Regex_help.[doc/pdf] file in installation directory </html> ");

			ig_case = new JCheckBox("Ignore Case");
			ig_case.setToolTipText("Valid for Regex Seach only");
			ig_case.setSelected(true);
			createPopupMenu(); // Select Regex from here

			q_pane.add(s_l);
			q_pane.add(s_field);
			q_pane.add(ig_case);
			SpringUtilities.makeCompactGrid(q_pane, 1, 3, 3, 3, 3, 3);
			return q_pane;
		}

		public String getStatus() {
			String sel = null;
			if (s_field.getSelectedItem() != null) {
				sel = s_field.getSelectedItem().toString();
				s_field.insertItemAt(sel, 0);
			}
			return sel;

		}

		public boolean isCase() {
			return ig_case.isSelected();
		}

		public void createPopupMenu() {
			JMenuItem menuItem;
			JPopupMenu popup = new JPopupMenu();

			// Create the popup menu From regexString.txt
			__h = KeyValueParser.parseFile("resource/popupmenu.txt");
			if (__h == null)
				return;
			Enumeration<String> enum1 = __h.keys();
			while (enum1.hasMoreElements()) {
				String key_n = (String) enum1.nextElement();

				menuItem = new JMenuItem(key_n);
				menuItem.addActionListener(this);
				popup.add(menuItem);
			}
			// Add listener to the text area so the popup menu can come up.
			MouseListener popupListener = new PopupListener(popup);
			s_l.addMouseListener(popupListener);
		}

		public void actionPerformed(ActionEvent e) {
			s_field.setSelectedItem(__h.get(e.getActionCommand()));
			q_s.setSelectedIndex(2);
		}

		private class PopupListener extends MouseAdapter {
			JPopupMenu popup;

			PopupListener(JPopupMenu popupMenu) {
				popup = popupMenu;
			}

			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}

	}

	private JPanel query_sel_pane() {
		JPanel q_sel = new JPanel();
		q_sel.setLayout(new GridLayout(5, 0));

		d_c = new JCheckBox("Distinct");
		q_s = new JComboBox<String>(new String[] { "String Like", "String NOT Like",
				"Regex" });
		q_s.setBorder(new EmptyBorder(0, 4, 0, 0));
		q1_c = new JLabel(" Pattern_1 Count: N/P");
		q2_c = new JLabel(" Pattern_2 Count: N/P");
		q3_c = new JLabel(" Pattern_3 Count: N/P");

		q1_c.setToolTipText("<html>Count of Pattern_1 <BR>N/P: NOT PROFILED</html>");
		q2_c.setToolTipText("<html>Count of Pattern_2 <BR>N/P: NOT PROFILED</html>");
		q3_c.setToolTipText("<html>Count of Pattern_3 <BR>N/P: NOT PROFILED</html>");

		q_sel.add(d_c);
		q_sel.add(q_s);
		q_sel.add(q1_c);
		q_sel.add(q2_c);
		q_sel.add(q3_c);
		return q_sel;

	}

	private void reCreateBotPane_like() {

		int i = 0; // row Index max row count
		int col_c_1 = 0, col_c_2 = 0, col_c_3 = 0; // Start from negative index

		String q1_cont = qp_1.getStatus();
		String q2_cont = qp_2.getStatus();
		String q3_cont = qp_3.getStatus();

		if ((q1_cont == null || q1_cont.compareTo("") == 0)
				&& (q2_cont == null || q2_cont.compareTo("") == 0)
				&& (q3_cont == null || q3_cont.compareTo("") == 0)) {
			JOptionPane.showMessageDialog(null, "No Search Pattern",
					"Error Message", JOptionPane.ERROR_MESSAGE);
			return; // Nothing to profile
		}
		/* Add % if the person is not already have added it */
		if (!(q1_cont.startsWith("%") == true || q1_cont.endsWith("%") == true)
				&& q1_cont.compareTo("") != 0)
			q1_cont += "%"; // Adding in end so string with starting detected
		if (!(q2_cont.startsWith("%") == true || q2_cont.endsWith("%") == true)
				&& q2_cont.compareTo("") != 0)
			q2_cont += "%"; // Adding in end so string with starting detected
		if (!(q3_cont.startsWith("%") == true || q3_cont.endsWith("%") == true)
				&& q3_cont.compareTo("") != 0)
			q3_cont += "%"; // Adding in end so string with starting detected

		QueryBuilder s_prof = new QueryBuilder(_dsn, _table, _col,
				Rdbms_conn.getDBType());
		String like_query_1, like_query_2, like_query_3;
		boolean like = q_s.getSelectedIndex() == 0 ? true : false;

		if (_distinct) {
			like_query_1 = s_prof.get_freq_like_query(q1_cont, like);
			like_query_2 = s_prof.get_freq_like_query(q2_cont, like);
			like_query_3 = s_prof.get_freq_like_query(q3_cont, like);
		} else {
			like_query_1 = s_prof.get_like_query(q1_cont, like);
			like_query_2 = s_prof.get_like_query(q2_cont, like);
			like_query_3 = s_prof.get_like_query(q3_cont, like);
		}

		try {
			Rdbms_conn.openConn();
			ResultSet rs_1 = Rdbms_conn.runQuery(like_query_1);

			// SQL Bug with Invalid cursor state so take one result set at point
			// of time
			while (rs_1.next()) {
				qtable.addRow();
				String q_value_1 = rs_1.getString("like_wise");
				if (_distinct) {
					String dup_row_count = rs_1.getString("row_count");
					qtable.setTableValueAt("(" + dup_row_count + ") "
							+ q_value_1, col_c_1, 0);
				} else {
					qtable.setTableValueAt(q_value_1, col_c_1, 0);
				}
				++i;
				col_c_1++;
			}
			rs_1.close();

			ResultSet rs_2 = Rdbms_conn.runQuery(like_query_2);
			while (rs_2.next()) {
				if (col_c_2 == i) {
					qtable.addRow();
					++i;
				}
				String q_value_2 = rs_2.getString("like_wise");
				if (_distinct) {
					String dup_row_count = rs_2.getString("row_count");
					qtable.setTableValueAt("(" + dup_row_count + ") "
							+ q_value_2, col_c_2++, 1);
				} else
					qtable.setTableValueAt(q_value_2, col_c_2++, 1);
			}
			rs_2.close();

			ResultSet rs_3 = Rdbms_conn.runQuery(like_query_3);

			while (rs_3.next()) {
				if (col_c_3 == i) {
					qtable.addRow();
					++i;
				}
				String q_value_3 = rs_3.getString("like_wise");
				if (_distinct) {
					String dup_row_count = rs_3.getString("row_count");
					qtable.setTableValueAt("(" + dup_row_count + ") "
							+ q_value_3, col_c_3++, 2);
				} else
					qtable.setTableValueAt(q_value_3, col_c_3++, 2);
			}
			rs_3.close();

			Rdbms_conn.closeConn();
		} catch (SQLException e) {
			ConsoleFrame.addText("\n Like Query execution failed");
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}

		q1_c.setText(" Pattern_1 Count: " + col_c_1 + "  ");
		q2_c.setText(" Pattern_2 Count: " + col_c_2 + "  ");
		q3_c.setText(" Pattern_3 Count: " + col_c_3 + "  ");

	}

	private void reCreateBotPane_regex() {

		int col_c_1 = 0, col_c_2 = 0, col_c_3 = 0; // Count for Individual
													// column
		String regex_query = "";

		QueryBuilder s_prof = new QueryBuilder(_dsn, _table, _col,
				Rdbms_conn.getDBType());
		if (_distinct)
			regex_query = s_prof.get_freq_all_query();
		else
			regex_query = s_prof.get_all_worder_query();

		try {
			Pattern q_pattern_1 = null, q_pattern_2 = null, q_pattern_3 = null;
			Matcher q_match_1 = null, q_match_2 = null, q_match_3 = null;
			boolean q1_b = false, q2_b = false, q3_b = false;
			String query1_str = "", query2_str = "", query3_str = "";
			int count = 0;

			query1_str = qp_1.getStatus();
			query2_str = qp_2.getStatus();
			query3_str = qp_3.getStatus();

			// 2 for Pattern.CASE_INSENSITIVE

			if (!(query1_str == null || query1_str.compareTo("") == 0))
				if (qp_1.isCase())
					q_pattern_1 = Pattern.compile(query1_str,
							Pattern.CASE_INSENSITIVE);
				else
					q_pattern_1 = Pattern.compile(query1_str);

			if (!(query2_str == null || query2_str.compareTo("") == 0))
				if (qp_2.isCase())
					q_pattern_2 = Pattern.compile(query2_str,
							Pattern.CASE_INSENSITIVE);
				else
					q_pattern_2 = Pattern.compile(query2_str);

			if (!(query3_str == null || query3_str.compareTo("") == 0))
				if (qp_3.isCase())
					q_pattern_3 = Pattern.compile(query3_str,
							Pattern.CASE_INSENSITIVE);
				else
					q_pattern_3 = Pattern.compile(query3_str);

			if (q_pattern_1 == null && q_pattern_2 == null
					&& q_pattern_3 == null) {
				JOptionPane.showMessageDialog(null, "No Search Pattern",
						"Error Message", JOptionPane.ERROR_MESSAGE);
				return;
			}

			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(regex_query);

			while (rs.next()) {

				String q_value = rs.getString("like_wise");
				if (q_value == null)
					continue;
				count++;

				if (q_pattern_1 != null)
					q_match_1 = q_pattern_1.matcher(q_value);
				if (q_pattern_2 != null)
					q_match_2 = q_pattern_2.matcher(q_value);
				if (q_pattern_3 != null)
					q_match_3 = q_pattern_3.matcher(q_value);

				if (q_match_1 != null)
					q1_b = q_match_1.find();
				if (q_match_2 != null)
					q2_b = q_match_2.find();
				if (q_match_3 != null)
					q3_b = q_match_3.find();

				if (q1_b || q2_b || q3_b)
					qtable.addRow();

				if (_distinct) {
					String dup_row_count = rs.getString("row_count");
					if (q1_b)
						qtable.setTableValueAt("(" + dup_row_count + ") "
								+ q_value, col_c_1++, 0);
					if (q2_b)
						qtable.setTableValueAt("(" + dup_row_count + ") "
								+ q_value, col_c_2++, 1);
					if (q3_b)
						qtable.setTableValueAt("(" + dup_row_count + ") "
								+ q_value, col_c_3++, 2);
				} else {
					if (q1_b)
						qtable.setTableValueAt(q_value, col_c_1++, 0);
					if (q2_b)
						qtable.setTableValueAt(q_value, col_c_2++, 1);
					if (q3_b)
						qtable.setTableValueAt(q_value, col_c_3++, 2);
				}
			} // End of while loop

			rs.close();
			Rdbms_conn.closeConn();
		} catch (SQLException e) {
			ConsoleFrame.addText("\n ERROR:Regex Query execution failed");
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} catch (PatternSyntaxException e) {
			ConsoleFrame.addText("\n ERROR:Regex compilation error");
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
		q1_c.setText(" Pattern_1 Count: " + col_c_1 + "  ");
		q2_c.setText(" Pattern_2 Count: " + col_c_2 + "  ");
		q3_c.setText(" Pattern_3 Count: " + col_c_3 + "  ");

	}

	public void createBotPane() {
		SpringLayout layout = new SpringLayout();
		_b_pane.setLayout(layout);
		layout.putConstraint(SpringLayout.WEST, qtable, 0, SpringLayout.WEST,
				_b_pane);
		layout.putConstraint(SpringLayout.NORTH, qtable, 5, SpringLayout.NORTH,
				_b_pane);
		_b_pane.add(qtable);

	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("comment".equals(command)) {
			r_c = new InputDialog(1).createTextPanel();
			return;
		}

		// Clearup any previous table
		qtable.cleanallRow();
		if (d_c.isSelected())
			_distinct = true;
		else
			_distinct = false;

		// Change the cursor
		try {
			_b_pane.getTopLevelAncestor().setCursor(
					java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

			if (q_s.getSelectedItem().toString().equals("Regex"))
				reCreateBotPane_regex();
			else
				// Like query
				reCreateBotPane_like();
		} finally {
			_b_pane.getTopLevelAncestor()
					.setCursor(
							java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));

		}
	}

}
