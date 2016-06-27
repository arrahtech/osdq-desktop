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
 * This class defines create table Dialog
 * and all the parameter related to that
 *
 */

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.rdbms.SqlType;

public class QueryDialog extends JDialog implements ActionListener,
		ItemListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JList<String> list;
	private DefaultListModel<String> lm;
	protected Vector<JComboBox<String>> cb_v;
	protected Vector<JFormattedTextField> wt_v;
	protected Vector<JTextField> tt_v;
	protected Vector<JComboBox <String>> aoc_v;
	protected Vector<JTextField> t_type_v;
	protected int index;
	private int capacity;
	private JPanel cp;
	private Vector<?> __type_v;
	private Vector dateVar[];
	private String t_cond;
	private String a_cond;
	private JFormattedTextField tf;
	protected JButton apply_b;
	private int LEVEL;
	private String _table;
	private String _column;

	public String cond;
	public int response;
	public String tC;
	public ReportTable _rt;

	public QueryDialog(int i, String s, Vector<?> avector[]) {
		cb_v = new Vector<JComboBox<String>>();
		wt_v = new Vector<JFormattedTextField>();
		tt_v = new Vector<JTextField>();
		aoc_v = new Vector<JComboBox<String>>();
		t_type_v = new Vector<JTextField>();

		index = 0;
		capacity = 0;
		dateVar = new Vector[2];
		response = 0;
		tC = "0";
		_rt = null;
		LEVEL = i;
		_table = s;

		lm = new DefaultListModel<String>();
		Enumeration<String> enumeration = (Enumeration<String>) avector[0].elements();
		int j = 0;
		for (; enumeration.hasMoreElements(); lm.add(j++,
				enumeration.nextElement()))
			;
		__type_v = avector[1];

		createPanel();
	}

	public void actionPerformed(ActionEvent actionevent) {
		String s = actionevent.getActionCommand();
		if (s.equals("move")) {
			apply_b.setEnabled(false);
			Object aobj[] = list.getSelectedValuesList().toArray();
			int ai[] = list.getSelectedIndices();
			for (int i = aobj.length; index + i > capacity;)
				addRow();

			cp.setLayout(new SpringLayout());
			SpringUtilities.makeCompactGrid(cp, capacity, 5, 3, 3, 3, 3);
			cp.revalidate();
			for (int j = 0; j < aobj.length; j++) {
				JTextField jtextfield = (JTextField) tt_v.get(index);
				jtextfield.setText((String) aobj[j]);
				JTextField jtextfield1 = (JTextField) t_type_v.get(index);
				Object obj = __type_v.get(ai[j]);
				if (obj instanceof Number)
					jtextfield1.setText(SqlType.getTypeName(((Integer) __type_v
							.get(ai[j])).intValue()));
				else
					jtextfield1.setText(obj.toString());
				JComboBox<String> jcombobox = (JComboBox<String>) cb_v.get(index);
				jcombobox.setEnabled(true);
				index++;
			}

		} else if (s.equals("validate")) {

			if (LEVEL == 1) { // Table
				QueryBuilder querybuilder = new QueryBuilder(
						Rdbms_conn.getHValue("Database_DSN"), _table,
						Rdbms_conn.getDBType());
				t_cond = querybuilder.get_tableCount_query();
				a_cond = querybuilder.get_tableAll_query();
			}
			if (LEVEL == 2) { // Column
				QueryBuilder querybuilder = new QueryBuilder(
						Rdbms_conn.getHValue("Database_DSN"), _table, _column,
						Rdbms_conn.getDBType());
				t_cond = querybuilder.get_tableCount_query();
				a_cond = "";
			}
			String s1 = t_cond;
			dateVar[0] = new Vector<java.util.Date>();
			dateVar[1] = new Vector<String>();
			cond = condString();
			if (cond == null)
				return;
			s1 = cond.equals("") ? s1 : s1 + " WHERE " + cond;
			runValidation(s1);
		} else if (s.equals("cancel")) {
			dispose();
			response = 0;
		} else if (s.equals("apply")) {
			String s2 = a_cond;
			if (!("".equals(s2))) {
				s2 = cond.equals("") ? s2 : s2 + " WHERE " + cond;
				runApply(s2);
			}
			dispose();
			response = 1;
			if (LEVEL == 2) { // Column
				QueryBuilder.setDateCondition(dateVar);
			}
		}
	}

	private void createPanel() {
		JPanel jpanel = new JPanel();
		SpringLayout springlayout = new SpringLayout();
		jpanel.setLayout(springlayout);
		JLabel jlabel = new JLabel("Select Columns");
		JLabel jlabel1 = new JLabel("Show");
		tf = new JFormattedTextField();
		tf.setColumns(6);
		if (LEVEL == 1)
			tf.setValue(new Integer(100));
		else
			tf.setEnabled(false);
		JLabel jlabel2 = new JLabel("Rows");
		list = new JList<String>(lm);
		JScrollPane jscrollpane = new JScrollPane(list);
		jscrollpane.setPreferredSize(new Dimension(175, 350));
		JButton jbutton = new JButton(">>");
		jbutton.setActionCommand("move");
		jbutton.addActionListener(this);
		jbutton.addKeyListener(new KeyBoardListener());
		cp = new JPanel(new SpringLayout());
		for (int i = 0; i < 10; i++)
			addRow();

		SpringUtilities.makeCompactGrid(cp, capacity, 5, 3, 3, 3, 3);
		JScrollPane jscrollpane1 = new JScrollPane(cp);
		jscrollpane1.setPreferredSize(new Dimension(475, 350));
		JButton jbutton1 = new JButton("Validate");
		jbutton1.setActionCommand("validate");
		jbutton1.addActionListener(this);
		jbutton1.addKeyListener(new KeyBoardListener());
		apply_b = new JButton("Apply");
		apply_b.setActionCommand("apply");
		apply_b.addActionListener(this);
		apply_b.addKeyListener(new KeyBoardListener());
		apply_b.setEnabled(false);
		JButton jbutton2 = new JButton("Cancel");
		jbutton2.setActionCommand("cancel");
		jbutton2.addActionListener(this);
		jbutton2.addKeyListener(new KeyBoardListener());
		jpanel.add(jlabel);
		jpanel.add(jlabel1);
		jpanel.add(tf);
		jpanel.add(jlabel2);
		jpanel.add(jscrollpane);
		jpanel.add(jbutton);
		jpanel.add(jscrollpane1);
		jpanel.add(jbutton1);
		jpanel.add(apply_b);
		jpanel.add(jbutton2);
		springlayout.putConstraint("West", jlabel, 5, "West", jpanel);
		springlayout.putConstraint("North", jlabel, 5, "North", jpanel);
		springlayout.putConstraint("East", jlabel1, -5, "West", tf);
		springlayout.putConstraint("North", jlabel1, 5, "North", jpanel);
		springlayout.putConstraint("East", tf, -5, "West", jlabel2);
		springlayout.putConstraint("North", tf, 5, "North", jpanel);
		springlayout.putConstraint("East", jlabel2, -5, "East", jpanel);
		springlayout.putConstraint("North", jlabel2, 5, "North", jpanel);
		springlayout.putConstraint("West", jscrollpane, 5, "West", jpanel);
		springlayout.putConstraint("North", jscrollpane, 5, "South", jlabel);
		springlayout.putConstraint("West", jbutton, 5, "East", jscrollpane);
		springlayout.putConstraint("North", jbutton, 50, "North", jscrollpane);
		springlayout.putConstraint("West", jscrollpane1, 5, "East", jbutton);
		springlayout.putConstraint("North", jscrollpane1, 0, "North",
				jscrollpane);
		springlayout.putConstraint("West", jbutton1, 5, "West", jpanel);
		springlayout.putConstraint("North", jbutton1, 5, "South", jscrollpane1);
		springlayout.putConstraint("West", apply_b, 5, "East", jbutton1);
		springlayout.putConstraint("North", apply_b, 5, "South", jscrollpane1);
		springlayout.putConstraint("West", jbutton2, 5, "East", apply_b);
		springlayout.putConstraint("North", jbutton2, 5, "South", jscrollpane1);
		springlayout.putConstraint("East", jpanel, 5, "East", jscrollpane1);
		springlayout.putConstraint("South", jpanel, 5, "South", jbutton1);
		getContentPane().add(jpanel);
	}

	private void addRow() {
		JComboBox<String> jcombobox = null;
		JComboBox<String> jcombobox1 = null;
		JTextField jtextfield = null;
		JTextField jtextfield1 = null;
		JFormattedTextField jformattedtextfield = null;
		jtextfield = new JTextField(8);
		jtextfield.setEditable(false);
		tt_v.add(capacity, jtextfield);
		jtextfield1 = new JTextField(8);
		jtextfield1.setEditable(false);
		t_type_v.add(capacity, jtextfield1);
		jcombobox = new JComboBox<String>(new String[] { "Condition", "--------------",
				" IS NULL ", " IS NOT NULL ", " LIKE ", " NOT LIKE ", " = ",
				" <> ", " < ", " <= ", " > ", " >= " });
		jcombobox.setEnabled(false);
		jcombobox.addItemListener(this);
		cb_v.add(capacity, jcombobox);
		jformattedtextfield = new JFormattedTextField();
		jformattedtextfield.setEnabled(false);
		jformattedtextfield.setColumns(8);
		jformattedtextfield.addKeyListener(this);
		wt_v.add(capacity, jformattedtextfield);
		jcombobox1 = new JComboBox<String>(new String[] { " OR ", " AND " });
		aoc_v.add(capacity, jcombobox1);
		cp.add(jtextfield);
		cp.add(jtextfield1);
		cp.add(jcombobox);
		cp.add(jformattedtextfield);
		cp.add(jcombobox1);
		capacity++;
	}

	private String condString() {
		String s2 = "";
		int i = 0;
		String s3 = "";
		for (int j = 0; j < index; j++) {
			String s4 = ((JTextField) tt_v.get(j)).getText();
			
			// mysql and hive does not allow quotes for table and columns
			
			if ((Rdbms_conn.getDBType().compareToIgnoreCase("mysql") != 0) &&
			(Rdbms_conn.getDBType().compareToIgnoreCase("hive") != 0) &&
			(Rdbms_conn.getDBType().compareToIgnoreCase("informix") != 0))
				s4 = "\"" + s4 + "\"";

			JComboBox<String> jcombobox = (JComboBox<String>) cb_v.get(j);
			int k = jcombobox.getSelectedIndex();
			if (k <= 1)
				continue;
			String s5 = ((JFormattedTextField) wt_v.get(j)).getText().trim();
			s5 = s5.replace('"', '\'');
			String s6 = jcombobox.getSelectedItem().toString();
			String s7 = ((JTextField) t_type_v.get(j)).getText();
			String s1;
			switch (k) {
			case 2: // '\002'
			case 3: // '\003'
				s1 = s4 + s6;
				break;

			case 4: // '\004'
			case 5: // '\005'
				if (s5 == null || s5.equals("")) {
					JOptionPane
							.showMessageDialog(
									null,
									"Varibale can not be null or Empty \n  Enter %Str% or %str or Str% ",
									"Variable Format Error",
									JOptionPane.ERROR_MESSAGE);
					return null;
				}
				if (!s5.startsWith("%") && !s5.endsWith("%"))
					s5 = s5 + "%";
				if (!s5.startsWith("'"))
					s5 = "'" + s5 + "'";
				s1 = s4 + s6 + s5;
				break;

			default:
				if (s5 == null || s5.equals("")) {
					JOptionPane.showMessageDialog(null,
							"Varibale can not be null or Empty ",
							"Variable Format Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				if (s7.compareToIgnoreCase("time") == 0
						|| s7.compareToIgnoreCase("date") == 0
						|| s7.compareToIgnoreCase("timestamp") == 0
						|| s7.toUpperCase().contains("DATE") == true) {
					SimpleDateFormat simpledateformat = new SimpleDateFormat(
							"dd/MM/yyyy hh:mm:ss");
					simpledateformat.setLenient(true);
					Date date = simpledateformat
							.parse(s5, new ParsePosition(0));
					if (date == null) {
						ConsoleFrame.addText("\n ERROR:Could not Parse " + s5);
						JOptionPane.showMessageDialog(null, " Could not Parse"
								+ s5
								+ "\nEnter date in dd/MM/yyyy hh:mm:ss Format",
								"Date Format error", JOptionPane.ERROR_MESSAGE);
						return null;
					}
					
					// This will not work with Hive as it does not support setDate option
					// query need to build here
					if (Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") == 0 ) {
						SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Default format for Hive UnixTimeCall
						s5 = "unix_timestamp(\'"+sd.format(date) +"\')";
						
					} else { // for RDBMS
						dateVar[0].add(i, date);
						dateVar[1].add(i, s7);
						i++;
						s5 = "?"; // Parameterized query
					}
				} else if (s7.indexOf("Char") != -1 && !s5.startsWith("'"))
					s5 = "'" + s5 + "'";
				s1 = s4 + s6 + s5;
				break;
			}
			if (!s3.equals(""))
				s1 = s3 + s1;
			s3 = ((JComboBox<String>) aoc_v.get(j)).getSelectedItem().toString();
			s2 = s2 + s1;
		}

		return s2;
	}

	private void runValidation(String s) {
		PreparedStatement preparedstatement;
		try {
			Rdbms_conn.openConn();
			preparedstatement = Rdbms_conn.createQuery(s);
			if (preparedstatement == null) {
				ConsoleFrame.addText("\n ERROR:Validation Query Null");
				return;
			}
			for (int i = 0; i < dateVar[0].size(); i++) {
				String s1 = (String) dateVar[1].get(i);
				if (s1.compareToIgnoreCase("time") == 0)
					preparedstatement.setTime(i + 1, new Time(
							((Date) dateVar[0].get(i)).getTime()));
				if (s1.compareToIgnoreCase("date") == 0)
					preparedstatement.setDate(i + 1, new java.sql.Date(
							((Date) dateVar[0].get(i)).getTime()));
				if (s1.compareToIgnoreCase("timestamp") == 0)
					preparedstatement.setTimestamp(i + 1, new Timestamp(
							((Date) dateVar[0].get(i)).getTime()));
			}

			ResultSet resultset;
			for (resultset = preparedstatement.executeQuery(); resultset.next(); JOptionPane
					.showMessageDialog(null, " Query Success\n" + tC
							+ " Rows Found", "Information", 1))
				tC = resultset.getString("row_count");

			resultset.close();
			Rdbms_conn.closeConn();
			apply_b.setEnabled(true);
		} catch (SQLException sqlexception) {
			ConsoleFrame.addText("\n SQL Exception in Adhoc Query");
			JOptionPane.showMessageDialog(null, sqlexception.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
		return;
	}

	private void runApply(String s) {
		PreparedStatement preparedstatement;
		try {
			Rdbms_conn.openConn();
			preparedstatement = Rdbms_conn.createQuery(s);
			if (preparedstatement == null) {
				ConsoleFrame.addText("\n ERROR:Bin Query Null");
				return;
			}
			for (int i = 0; i < dateVar[0].size(); i++) {
				String s1 = (String) dateVar[1].get(i);
				if (s1.compareToIgnoreCase("time") == 0)
					preparedstatement.setTime(i + 1, new Time(
							((Date) dateVar[0].get(i)).getTime()));
				if (s1.compareToIgnoreCase("date") == 0)
					preparedstatement.setDate(i + 1, new java.sql.Date(
							((Date) dateVar[0].get(i)).getTime()));
				if (s1.compareToIgnoreCase("timestamp") == 0)
					preparedstatement.setTimestamp(i + 1, new Timestamp(
							((Date) dateVar[0].get(i)).getTime()));
			}

			preparedstatement.setMaxRows(((Number) tf.getValue()).intValue());
			ResultSet resultset = preparedstatement.executeQuery();
			_rt = SqlTablePanel.getSQLValue(resultset, true);
			resultset.close();
			Rdbms_conn.closeConn();
		} catch (SQLException sqlexception) {
			ConsoleFrame.addText("\n SQL Exception in Applying Adhoc Query");
			JOptionPane.showMessageDialog(null, sqlexception.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
		return;
	}

	public void itemStateChanged(ItemEvent itemevent) {
		apply_b.setEnabled(false);
		int i = cb_v.indexOf(itemevent.getSource());
		if (itemevent.getStateChange() == 2)
			return;
		JComboBox<String> jcombobox = (JComboBox<String>) cb_v.get(i);
		JFormattedTextField jformattedtextfield = (JFormattedTextField) wt_v
				.get(i);
		int j = jcombobox.getSelectedIndex();
		if (j < 4) {
			jformattedtextfield.setValue(new String(""));
			jformattedtextfield.setEnabled(false);
			return;
		}
		jformattedtextfield.setEnabled(true);
		JTextField jtextfield = (JTextField) t_type_v.get(i);
		String s = jtextfield.getText();
		if (j < 6)
			jformattedtextfield.setValue(new String("%"));
		else if (s.compareToIgnoreCase("time") == 0
				|| s.compareToIgnoreCase("date") == 0
				|| s.compareToIgnoreCase("timestamp") == 0
				|| s.toUpperCase().contains("DATE") == true)
			jformattedtextfield.setValue(new String("dd/MM/yyyy hh:mm:ss"));
		else
			jformattedtextfield.setValue(new String(""));
	}

	public void keyPressed(KeyEvent keyevent) {
		int i = keyevent.getKeyCode();
		if (i == 10)
			apply_b.doClick();
	}

	public void keyReleased(KeyEvent keyevent) {
	}

	public void keyTyped(KeyEvent keyevent) {
		apply_b.setEnabled(false);
	}

	public void setColumn(String col) {
		_column = col;

	}
}
