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
 * This is Table Panel which shows interactive SQL data
 * in ReportTable structure 
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.arrah.framework.ndtable.ResultsetToRTM;
import org.arrah.framework.rdbms.Rdbms_conn;

public class SqlTablePanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea sql_t;
	private JScrollPane bot_scroll;
	private JMenu query_m;
	private JMenuItem new_i;
	private Stack<String> newMenuStack = new Stack<String>();
	private static Hashtable<String, String> stored_query = new Hashtable<String, String>();
	private JRadioButtonMenuItem ch_s, run_s, no_s;
	private JCheckBoxMenuItem fm_c;
	private JLabel q_time;

	public SqlTablePanel() {
		JFrame frame = new JFrame("SQL Interface");
		bot_scroll = new JScrollPane();

		// Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(createTopPane());
		splitPane.setBottomComponent(bot_scroll);

		splitPane.setDividerLocation(230);
		splitPane.setPreferredSize(new Dimension(600, 600));

		splitPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(splitPane);

		// Add Menu bar
		JMenuBar menubar = new JMenuBar();
		frame.setJMenuBar(menubar);

		query_m = new JMenu("Query");
		query_m.setMnemonic('Q');
		menubar.add(query_m);

		JMenuItem open_m = new JMenuItem("Open");
		open_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK));
		open_m.addActionListener(this);
		query_m.add(open_m);

		JMenuItem save_m = new JMenuItem("Save as..");
		save_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
		save_m.addActionListener(this);
		query_m.add(save_m);
		JMenuItem delete_m = new JMenuItem("Delete");
		delete_m.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				InputEvent.CTRL_MASK));
		delete_m.addActionListener(this);
		query_m.add(delete_m);
		JSeparator sep = new JSeparator();
		query_m.add(sep);

		JMenu option_m = new JMenu("Options");
		option_m.setMnemonic('O');
		menubar.add(option_m);

		JMenu enter_m = new JMenu("Enter as Button Click");
		option_m.add(enter_m);

		ButtonGroup c_b = new ButtonGroup();
		ch_s = new JRadioButtonMenuItem("Check SQL");
		run_s = new JRadioButtonMenuItem("Run SQL");
		run_s.setSelected(true);
		no_s = new JRadioButtonMenuItem("None");
		c_b.add(ch_s);
		c_b.add(run_s);
		c_b.add(no_s);
		enter_m.add(ch_s);
		enter_m.add(run_s);
		enter_m.add(no_s);
		option_m.addSeparator();
		fm_c = new JCheckBoxMenuItem("Default Format");
		fm_c.setSelected(true);
		option_m.add(fm_c);

		// Display the window.
		frame.setLocation(125, 50);
		frame.pack();
		frame.setVisible(true);
	}

	private JPanel createTopPane() {
		JPanel t_p = new JPanel();
		t_p.setLayout(new BorderLayout());

		final JButton q_b = new JButton("Check SQL");
		final JButton r_b = new JButton("Run SQL");

		sql_t = new JTextArea(6, 60);
		sql_t.setLineWrap(true);
		sql_t.setWrapStyleWord(true);
		sql_t.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (ch_s.isSelected() == true)
						q_b.doClick();
					else if (run_s.isSelected() == true)
						r_b.doClick();
					else
						return;
				}
			}
		});
		Font f = new Font("Helvetika", Font.PLAIN, 16);
		sql_t.setFont(f);
		JScrollPane t_scroll = new JScrollPane(sql_t);
		t_p.add(t_scroll, BorderLayout.CENTER);

		JPanel sql_value = new JPanel();
		sql_value.setLayout(new GridLayout(0, 5));

		JLabel dummy1 = new JLabel("Time Taken:", JLabel.TRAILING);
		sql_value.add(dummy1);
		q_time = new JLabel();
		sql_value.add(q_time);

		q_b.setMnemonic('C');
		q_b.addActionListener(new buttonListener());
		q_b.addKeyListener(new KeyBoardListener());
		sql_value.add(q_b);
		JLabel dummy2 = new JLabel();
		sql_value.add(dummy2);
		r_b.setMnemonic('R');
		r_b.addActionListener(new buttonListener());
		r_b.addKeyListener(new KeyBoardListener());
		sql_value.add(r_b);

		t_p.add(sql_value, BorderLayout.PAGE_END);
		return t_p;
	}

	private class buttonListener implements ActionListener {
		public buttonListener() {
		};

		public void actionPerformed(ActionEvent e) {
			String sql_s ="";
			sql_s = sql_t.getSelectedText(); // get only selected text
			if (sql_s == null || "".equals(sql_s))
			sql_s = sql_t.getText();
			if (sql_s == null || "".equals(sql_s)) {
				bot_scroll.setViewportView(new JLabel(
						"Error: Empty Query String."));
				return;
			}
			sql_s = sql_s.trim().replaceAll("\\s+", " ");

			String[] sp_v = sql_s.split(" "); // Split to get words
			if (sp_v[0] == null || sp_v[0].compareTo("") == 0) {
				bot_scroll.setViewportView(new JLabel(
						"Error: Empty Query String."));
				return;

			}
			long q_s_t = 0;
			long q_e_t = 0;
			long diff = 0;

			// Add previous 5 non matching sqls to menu
			int temp_size = newMenuStack.size();

			if (temp_size < 5) { // 5 Previous command to display
				if (temp_size == 0
						|| ((String) newMenuStack.peek()).compareTo(sql_s) != 0) {
					newMenuStack.add(temp_size, sql_s);
					new_i = new JMenuItem(sql_s);
					new_i.addActionListener(SqlTablePanel.this);
					query_m.insert(new_i, 4);
				}
			} else {
				if (((String) newMenuStack.peek()).compareTo(sql_s) != 0) {
					newMenuStack.remove(0);
					newMenuStack.push(sql_s);
					query_m.getItem(8).setText(query_m.getItem(7).getText());
					query_m.getItem(7).setText(query_m.getItem(6).getText());
					query_m.getItem(6).setText(query_m.getItem(5).getText());
					query_m.getItem(5).setText(query_m.getItem(4).getText());
					query_m.getItem(4).setText(sql_s);
				}
			}
			String clicked_but = ((JButton) e.getSource()).getText();
			if (clicked_but.equals("Run SQL")) {
				try {
					((JButton) e.getSource()).setCursor(java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
					Rdbms_conn.openConn();
					// Set the cursor
					// Get time for query
					if ((sp_v[0].compareToIgnoreCase("INSERT") == 0)
							|| (sp_v[0].compareToIgnoreCase("UPDATE") == 0)
							|| (sp_v[0].compareToIgnoreCase("DELETE") == 0)) {
						q_s_t = System.currentTimeMillis();
						int row_affected = Rdbms_conn.executeUpdate(sql_s);
						q_e_t = System.currentTimeMillis();

						if (row_affected > 0)
							bot_scroll.setViewportView(new JLabel(row_affected
									+ " Row Affected.."));
						else
							bot_scroll.setViewportView(new JLabel(
									"Query Successfull.."));

						Rdbms_conn.closeConn();
						diff = q_e_t - q_s_t;
						q_time.setText(Long.toString(diff) + " ms");
						return;
					}
					q_s_t = System.currentTimeMillis();
					ResultSet rs = Rdbms_conn.execute(sql_s);
					q_e_t = System.currentTimeMillis();
					if (rs == null) {
						bot_scroll.setViewportView(new JLabel(
								"Query Successfull.."));
						Rdbms_conn.closeConn();
						diff = q_e_t - q_s_t;
						q_time.setText(Long.toString(diff) + " ms");
						return;
					} else {
						ReportTable rt = getSQLValue(rs, fm_c.isSelected());
						bot_scroll.setViewportView(rt);
					} // End of else
					rs.close();
					Rdbms_conn.closeConn();
					diff = q_e_t - q_s_t;
					q_time.setText(Long.toString(diff) + " ms");
					return;
				} catch (SQLException sqle) {
					q_time.setText("Exception !!");
					bot_scroll.setViewportView(new JLabel(sqle.getMessage()));
					return;
				} finally {
					((JButton) e.getSource())
							.setCursor(java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				}
			} // Run SQL
			else {
				try {
					((JButton) e.getSource()).setCursor(java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
					Rdbms_conn.openConn();
					// Set the cursor
					// Get time for query
					q_s_t = System.currentTimeMillis();
					String native_s = Rdbms_conn.checkAndReturnSql(sql_s);
					q_e_t = System.currentTimeMillis();
					bot_scroll.setViewportView(new JLabel(
							"<html>Query OK.<br>Native Query will look like:<br>"
									+ native_s + "</html>"));
					Rdbms_conn.closeConn();
					diff = q_e_t - q_s_t;
					q_time.setText(Long.toString(diff) + " ms");
				} catch (SQLException sqle) {
					q_time.setText("Exception !!");
					bot_scroll.setViewportView(new JLabel(sqle.getMessage()));
				} finally {
					((JButton) e.getSource())
							.setCursor(java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				}
			} // End of else
		}
	} // End of class buttonListener

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		loadSQLFile("resource/storedSQL");
		if (command.compareTo("Save as..") == 0) {
			String q_name = (String) JOptionPane.showInputDialog(null,
					"Enter Query Name:", "Query Input Dialog",
					JOptionPane.PLAIN_MESSAGE);
			if (q_name == null || q_name.compareTo("") == 0
					|| sql_t.getText() == null
					|| sql_t.getText().compareTo("") == 0)
				return;
			stored_query.put(q_name, sql_t.getText());
			saveSQLFile("resource/storedSQL");
			return;
		}
		if (command.compareTo("Open") == 0) {
			int size = stored_query.size();
			if (size == 0)
				return; // Nothing to open
			String[] query_key = new String[size];
			int index = 0;

			Enumeration<String> key_e = stored_query.keys();
			while (key_e.hasMoreElements()) {
				query_key[index++] = (String) key_e.nextElement();
			}
			String q_name = (String) JOptionPane.showInputDialog(null,
					"Choose Query Name:", "Query Show Dialog",
					JOptionPane.QUESTION_MESSAGE, null, query_key, null);
			if (q_name == null || q_name.compareTo("") == 0)
				return;
			sql_t.setText((String) stored_query.get(q_name));
			return;
		}
		if (command.compareTo("Delete") == 0) {
			int size = stored_query.size();
			if (size == 0)
				return; // Nothing to open
			String[] query_key = new String[size];
			int index = 0;

			Enumeration<String> key_e = stored_query.keys();
			while (key_e.hasMoreElements()) {
				query_key[index++] = (String) key_e.nextElement();
			}
			String q_name = (String) JOptionPane.showInputDialog(null,
					"Choose Query Name to DELETE:", "Query Delete Dialog",
					JOptionPane.QUESTION_MESSAGE, null, query_key, null);
			if (q_name == null || q_name.compareTo("") == 0)
				return;
			stored_query.remove(q_name);
			saveSQLFile("resource/storedSQL");
			return;
		}
		sql_t.setText(command);

	}

	private static void saveSQLFile(String fileName) {
		try {
		  
		  URL url = SqlTablePanel.class.getClassLoader().getResource(fileName);
		  File file = new File(url.toURI().getPath());
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(stored_query);
			out.close();
			fileOut.close();
		} catch (IOException | URISyntaxException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} 
	}

	private static void loadSQLFile(String fileName) {
		try {
			// Open the file and load Hashtable
			InputStream fileIn = SqlTablePanel.class.getClassLoader().getResourceAsStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			stored_query = (Hashtable<String, String>) in.readObject();
			in.close();
			fileIn.close();
		} catch (FileNotFoundException file_exp) {
			JOptionPane.showMessageDialog(null, file_exp.getMessage()
					+ " :Will Create if Required", "Error Message",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException exp) {
			JOptionPane.showMessageDialog(null, exp.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
			exp.printStackTrace();
		} catch (ClassNotFoundException cl_exp) {
			JOptionPane.showMessageDialog(null, cl_exp.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
			cl_exp.printStackTrace();
		}
	}

	public static ReportTable getSQLValue(ResultSet rs, boolean format)
			throws SQLException {

		ReportTable rt = new ReportTable(ResultsetToRTM.getSQLValue(rs, format));
		return rt;
	}

}
