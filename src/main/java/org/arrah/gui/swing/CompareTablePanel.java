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

/* This file is used for comparing tables. 
 * and display this value in rowset
 *
 */

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.arrah.framework.profile.InterTableInfo;
import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.rdbms.SqlType;

public class CompareTablePanel implements ItemListener, ActionListener {
	private JComboBox<String> table1, table2;
	private Vector<?> vector1[], vector2[];
	private JComboBox<String> col1, col2;
	private boolean init, ainit;
	private JRadioButton rb1, rb2, rb3, rb4;
	private UniversePanel up1, up2;
	private JLabel l1, l2;
	private JFormattedTextField ft;
	private JPanel tagP, rowsetP, tp;
	private long[] lg;
	private JLabel ll1, ll2, ll3, ll4, ll5, ll6;
	private String str1, str2, str3, str4;
	private byte multiple;
	private int mX;
	private boolean isEditable = false; // default value
	private JDBCRowsetPanel rowsetPanel = null;
	private JFrame frame;
	
	public CompareTablePanel() {
		init = false;
		ainit = false;
		createFrame();

	}
	public CompareTablePanel(boolean isEditable) {
		init = false;
		ainit = false;
		this.isEditable = isEditable;
		createFrame();

	}

	private void createFrame() {
		JLabel ta = new JLabel("Select Table A");
		JLabel tb = new JLabel("Select Table B");

		Vector<String> vector = Rdbms_conn.getTable();
		table1 = new JComboBox<String>();
		table1.addItemListener(this);
		table2 = new JComboBox<String>();
		table2.addItemListener(this);
		for (int i = 0; i < vector.size(); i++) {
			String item = (String) vector.get(i);
			table1.addItem(item);
			table2.addItem(item);

		}
		col1 = new JComboBox<String>();
		col1.addItemListener(this);
		col2 = new JComboBox<String>();
		col2.addItemListener(this);

		vector1 = new Vector[2];
		vector2 = new Vector[2];
		vector1 = TableMetaInfo.populateTable(5, 0, 1, vector1);
		vector2 = vector1;

		for (int i = 0; i < vector1[0].size(); i++) {
			String item = (String) vector1[0].get(i);
			col1.addItem(item);
			col2.addItem(item);
		}
		int va = ((Integer) (vector1[1].get(0))).intValue();
		l1 = new JLabel(SqlType.getTypeName(va));
		l2 = new JLabel(SqlType.getTypeName(va));

		ButtonGroup bg = new ButtonGroup();
		rb1 = new JRadioButton("1:1", true);
		rb2 = new JRadioButton("1:M (including one)", false);
		rb3 = new JRadioButton("1:M (excluding one)", false);
		rb4 = new JRadioButton("1:", false);
		bg.add(rb1);
		bg.add(rb2);
		bg.add(rb3);
		bg.add(rb4);

		ft = new JFormattedTextField();
		ft.setColumns(3);
		ft.setValue(new Integer(5));
		JButton bt1 = new JButton("Analyse");
		bt1.addKeyListener(new KeyBoardListener());
		bt1.addActionListener(this);

		// Add Close button also which is same as close frame
		JButton closebt1 = new JButton("Cancel");
		closebt1.addKeyListener(new KeyBoardListener());
		closebt1.setActionCommand("cancel");
		closebt1.addActionListener(this);
		
		ll1 = new JLabel("Table A");
		ll2 = new JLabel("Table B");
		ll3 = new JLabel(
				"<html><body><a href=\"\">Table A No Match</A><body></html>");
		ll3.addMouseListener(new LinkMouseListener());
		ll4 = new JLabel(
				"<html><body><a href=\"\">Table A Match</A><body></html>");
		ll4.addMouseListener(new LinkMouseListener());
		ll5 = new JLabel(
				"<html><body><a href=\"\">Table B Match</A><body></html>");
		ll5.addMouseListener(new LinkMouseListener());
		ll6 = new JLabel(
				"<html><body><a href=\"\">Table B No Match</A><body></html>");
		ll6.addMouseListener(new LinkMouseListener());
		ll1.setVisible(false);
		ll2.setVisible(false);
		ll3.setVisible(false);
		ll4.setVisible(false);
		ll5.setVisible(false);
		ll6.setVisible(false);

		up1 = new UniversePanel();
		up1.setPreferredSize(new Dimension(50, 275));
		up2 = new UniversePanel();
		up2.setPreferredSize(new Dimension(50, 275));

		tagP = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void paintComponent(Graphics g) {
				if (ainit == false || lg == null)
					return;
				byte by0 = 18;
				byte by1 = 20;
				byte fs = 14;
				byte gp = 10;

				int i = getHeight();
				int j = getWidth();
				g.setColor(getBackground());
				g.fillRect(0, 0, j, i);

				Font font = new Font("Helvetika", 0, fs);
				g.setFont(font);
				// Create Color tag
				g.setColor(new Color(0, 255, 0));
				g.fillRect(0, gp, by0, by1);
				g.setColor(Color.BLACK);
				g.drawRect(0, gp, by0, by1);
				g.drawString(
						"Match(" + String.valueOf(lg[0]) + ","
								+ String.valueOf(lg[3]) + ")", by0 + 5, fs + gp
								+ 2);

				g.setColor(new Color(255, 0, 0));
				g.fillRect(0, fs + 2 * gp, by0, by1);
				g.setColor(Color.BLACK);
				g.drawRect(0, fs + 2 * gp, by0, by1);
				g.drawString("No Match(" + String.valueOf(lg[2] - lg[0]) + ","
						+ String.valueOf(lg[5] - lg[3]) + ")", by0 + 5, 2 * fs
						+ 2 * gp + 2);

				g.setColor(new Color(231, 139, 75));
				g.fillRect(0, 2 * fs + 3 * gp, by0, by1);
				g.setColor(Color.BLACK);
				g.drawRect(0, 2 * fs + 3 * gp, by0, by1);
				g.drawString(
						"Null(" + String.valueOf(lg[1]) + ","
								+ String.valueOf(lg[4]) + ")", by0 + 5, 3 * fs
								+ 3 * gp + 2);
			}
		};
		tagP.setPreferredSize(new Dimension(150, 100));
		rowsetP = new JPanel();

		tp = new JPanel();
		tp.setPreferredSize(new Dimension(850, 700));
		SpringLayout layout = new SpringLayout();
		tp.setLayout(layout);
		tp.add(ta);
		tp.add(tb);
		tp.add(table1);
		tp.add(table2);
		tp.add(col1);
		tp.add(col2);
		tp.add(l1);
		tp.add(l2);
		tp.add(rb1);
		tp.add(rb2);
		tp.add(rb3);
		tp.add(rb4);
		tp.add(ft);
		tp.add(bt1); tp.add(closebt1);
		tp.add(up1);
		tp.add(up2);
		tp.add(tagP);
		tp.add(ll1);
		tp.add(ll2);
		tp.add(ll3);
		tp.add(ll4);
		tp.add(ll5);
		tp.add(ll6);
		tp.add(rowsetP);
		JScrollPane tp_s = new JScrollPane(tp);
		tp_s.setPreferredSize(new Dimension(825, 650));

		layout.putConstraint(SpringLayout.WEST, ta, 15, SpringLayout.WEST, tp_s);
		layout.putConstraint(SpringLayout.NORTH, ta, 5, SpringLayout.NORTH,
				tp_s);
		layout.putConstraint(SpringLayout.EAST, tb, -25, SpringLayout.EAST,
				tp_s);
		layout.putConstraint(SpringLayout.NORTH, tb, 5, SpringLayout.NORTH,
				tp_s);
		layout.putConstraint(SpringLayout.WEST, table1, 15, SpringLayout.WEST,
				tp_s);
		layout.putConstraint(SpringLayout.NORTH, table1, 5, SpringLayout.SOUTH,
				ta);
		layout.putConstraint(SpringLayout.EAST, table2, -25, SpringLayout.EAST,
				tp_s);
		layout.putConstraint(SpringLayout.NORTH, table2, 5, SpringLayout.SOUTH,
				tb);
		layout.putConstraint(SpringLayout.WEST, col1, 0, SpringLayout.WEST,
				table1);
		layout.putConstraint(SpringLayout.NORTH, col1, 5, SpringLayout.SOUTH,
				table1);
		layout.putConstraint(SpringLayout.EAST, col2, 0, SpringLayout.EAST,
				table2);
		layout.putConstraint(SpringLayout.NORTH, col2, 5, SpringLayout.SOUTH,
				table2);
		layout.putConstraint(SpringLayout.WEST, l1, 0, SpringLayout.WEST,
				table1);
		layout.putConstraint(SpringLayout.NORTH, l1, 5, SpringLayout.SOUTH,
				col1);
		layout.putConstraint(SpringLayout.EAST, l2, 0, SpringLayout.EAST,
				table2);
		layout.putConstraint(SpringLayout.NORTH, l2, 5, SpringLayout.SOUTH,
				col2);

		layout.putConstraint(SpringLayout.WEST, rb1, 75, SpringLayout.EAST, l1);
		layout.putConstraint(SpringLayout.NORTH, rb1, 0, SpringLayout.NORTH, l1);
		layout.putConstraint(SpringLayout.WEST, rb2, 5, SpringLayout.EAST, rb1);
		layout.putConstraint(SpringLayout.NORTH, rb2, 0, SpringLayout.NORTH,
				rb1);
		layout.putConstraint(SpringLayout.WEST, rb3, 5, SpringLayout.EAST, rb2);
		layout.putConstraint(SpringLayout.NORTH, rb3, 0, SpringLayout.NORTH,
				rb2);
		layout.putConstraint(SpringLayout.WEST, rb4, 5, SpringLayout.EAST, rb3);
		layout.putConstraint(SpringLayout.NORTH, rb4, 0, SpringLayout.NORTH,
				rb3);
		layout.putConstraint(SpringLayout.WEST, ft, 0, SpringLayout.EAST, rb4);
		layout.putConstraint(SpringLayout.NORTH, ft, 2, SpringLayout.NORTH, rb4);

		layout.putConstraint(SpringLayout.WEST, bt1, 0, SpringLayout.WEST, rb3);
		layout.putConstraint(SpringLayout.NORTH, bt1, 15, SpringLayout.SOUTH,
				rb3);
		
		layout.putConstraint(SpringLayout.WEST, closebt1, 20, SpringLayout.EAST, bt1);
		layout.putConstraint(SpringLayout.NORTH, closebt1, 0, SpringLayout.NORTH,bt1);
		
		layout.putConstraint(SpringLayout.WEST, ll1, 10, SpringLayout.WEST,
				tp_s);
		layout.putConstraint(SpringLayout.NORTH, ll1, 5, SpringLayout.SOUTH,
				bt1);
		layout.putConstraint(SpringLayout.WEST, ll2, 40, SpringLayout.EAST, ll1);
		layout.putConstraint(SpringLayout.NORTH, ll2, 5, SpringLayout.SOUTH,
				bt1);
		layout.putConstraint(SpringLayout.WEST, ll3, 35, SpringLayout.EAST, ll2);
		layout.putConstraint(SpringLayout.NORTH, ll3, 5, SpringLayout.SOUTH,
				bt1);
		layout.putConstraint(SpringLayout.WEST, ll4, 15, SpringLayout.EAST, ll3);
		layout.putConstraint(SpringLayout.NORTH, ll4, 5, SpringLayout.SOUTH,
				bt1);
		layout.putConstraint(SpringLayout.WEST, ll5, 15, SpringLayout.EAST, ll4);
		layout.putConstraint(SpringLayout.NORTH, ll5, 5, SpringLayout.SOUTH,
				bt1);
		layout.putConstraint(SpringLayout.WEST, ll6, 15, SpringLayout.EAST, ll5);
		layout.putConstraint(SpringLayout.NORTH, ll6, 5, SpringLayout.SOUTH,
				bt1);

		layout.putConstraint(SpringLayout.WEST, up1, 10, SpringLayout.WEST,
				tp_s);
		layout.putConstraint(SpringLayout.SOUTH, up1, 0, SpringLayout.SOUTH,
				up2);
		layout.putConstraint(SpringLayout.WEST, up2, 30, SpringLayout.EAST, up1);
		layout.putConstraint(SpringLayout.SOUTH, up2, -125, SpringLayout.SOUTH,
				tp_s);
		layout.putConstraint(SpringLayout.WEST, tagP, 15, SpringLayout.WEST,
				tp_s);
		layout.putConstraint(SpringLayout.NORTH, tagP, 5, SpringLayout.SOUTH,
				up2);
		layout.putConstraint(SpringLayout.WEST, rowsetP, 25, SpringLayout.EAST,
				up2);
		layout.putConstraint(SpringLayout.NORTH, rowsetP, 15,
				SpringLayout.SOUTH, ll5);

		frame = new JFrame("Compare Table Frame");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				if (rowsetPanel != null)
					rowsetPanel.rowset.close(); // Close the open rowset
					frame.dispose();
				}
			});

		frame.getContentPane().add(tp_s);

		frame.setLocation(75, 25);
		frame.pack();
		frame.setVisible(true);
		init = true;

	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && init == true) {
			if (e.getSource().equals(table1)) {
				int index = table1.getSelectedIndex();
				vector1 = TableMetaInfo.populateTable(5, index, index + 1,
						vector1);
				int va = ((Integer) (vector1[1].get(0))).intValue();
				col1.removeAllItems();
				for (int i = 0; i < vector1[0].size(); i++) {
					String item = (String) vector1[0].get(i);
					col1.addItem(item);
				}
				l1.setText(SqlType.getTypeName(va));
			} else if (e.getSource().equals(table2)) {
				int index = table2.getSelectedIndex();
				vector2 = TableMetaInfo.populateTable(5, index, index + 1,
						vector2);
				int va = ((Integer) (vector2[1].get(0))).intValue();
				col2.removeAllItems();
				for (int i = 0; i < vector2[0].size(); i++) {
					String item = (String) vector2[0].get(i);
					col2.addItem(item);
				}
				l2.setText(SqlType.getTypeName(va));
			} else if (e.getSource().equals(col1)) {
				int va = ((Integer) (vector1[1].get(col1.getSelectedIndex())))
						.intValue();
				l1.setText(SqlType.getTypeName(va));
			} else if (e.getSource().equals(col2)) {
				int va = ((Integer) (vector2[1].get(col2.getSelectedIndex())))
						.intValue();
				l2.setText(SqlType.getTypeName(va));
			}

		}
	}

	public void actionPerformed(ActionEvent e) {
		
		if (rowsetPanel != null) 
			rowsetPanel.rowset.close();
		
		String command = e.getActionCommand();
		if ("cancel".equals(command) == true ) {
			frame.dispose();
			return;
		}
		
		str1 = (String) table1.getSelectedItem();
		str2 = (String) table2.getSelectedItem();
		str3 = (String) col1.getSelectedItem();
		str4 = (String) col2.getSelectedItem();
		multiple = 0;
		mX = 0;
		if (rb1.isSelected())
			multiple = 0;
		else if (rb2.isSelected())
			multiple = 1;
		else if (rb3.isSelected())
			multiple = 2;
		else if (rb4.isSelected()) {
			multiple = 3;
			mX = ((Integer) ft.getValue()).intValue();
			if (mX < 2) {
				JOptionPane.showMessageDialog(null,
						"Match Value can not be < 2", "Warning Message",
						JOptionPane.INFORMATION_MESSAGE);
			}

		}
		if (str1.equals(str2) && str3.equals(str4)) {
			JOptionPane.showMessageDialog(null, "Identical Fields to Compare",
					"Error Message", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String[] st = InterTableInfo.getMatchCount(str1, str3, str2, str4,
				multiple, mX);
		lg = UniversePanel.convertLong(st);
		up1.setPreferredSize(new Dimension(50, 275));
		up2.setPreferredSize(new Dimension(50, 275));

		if (lg[1] + lg[2] <= 0 && lg[4] + lg[5] <= 0) {
			JOptionPane.showMessageDialog(null, "Both Tables have 0 records. ",
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} else if (lg[1] + lg[2] > lg[4] + lg[5]) {
			up2.setPreferredSize(new Dimension(50, (int) (100 + 175
					* (lg[4] + lg[5]) / (lg[1] + lg[2]))));
		} else {
			up1.setPreferredSize(new Dimension(50, (int) (100 + 175
					* (lg[1] + lg[2]) / (lg[4] + lg[5]))));
		}

		up2.repaint();
		up2.revalidate();
		up1.repaint();
		up1.revalidate();
		up1.setTitle("");
		up1.setValues(new String[] { st[0], st[1], st[2] });
		up1.setLeft(true);
		up1.drawUniverseChart();
		up2.setTitle("");
		up2.setValues(new String[] { st[3], st[4], st[5] });
		up2.drawUniverseChart();

		ainit = true;
		tagP.repaint();
		ll1.setVisible(true);
		ll2.setVisible(true);
		ll3.setVisible(true);
		ll4.setVisible(true);
		ll5.setVisible(true);
		ll6.setVisible(true);

		rowsetP.removeAll();
		tp.revalidate();
		tp.repaint();
	}

	/* Link Mouse Adapter */
	private class LinkMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent mouseevent) {
			QueryBuilder qb = new QueryBuilder(
					Rdbms_conn.getHValue("Database_DSN"), str1, str3,
					Rdbms_conn.getDBType());
			qb.setCTableCol(str2, str4);
			try {
				mouseevent
						.getComponent()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				String s1 = ((JLabel) mouseevent.getSource()).getText();

				String query = "";
				
				boolean isLeft = true;
				
                if (s1 != null && s1.equals("<html><body><a href=\"\">Table A Match</A><body></html>")) {
                    
					if(Rdbms_conn.getDBType().compareToIgnoreCase("oracle_native") == 0
					|| (Rdbms_conn.getDBType().compareToIgnoreCase("oracle_odbc") == 0)){
						query=qb.get_col_match_value(multiple, mX, true, true);
					}
					else {
						query = qb.get_match_value(multiple, mX, true, true);
					}
					
				} else if (s1 != null
					&& s1.equals("<html><body><a href=\"\">Table A No Match</A><body></html>")) {
					if(Rdbms_conn.getDBType().compareToIgnoreCase("oracle_native") == 0
					|| (Rdbms_conn.getDBType().compareToIgnoreCase("oracle_odbc") == 0)){
						query=qb.get_col_match_value(multiple, mX, false, true);
					}
					else {
						query = qb.get_match_value(multiple, mX, false, true);
					}
					
				} else if (s1 != null
					&& s1.equals("<html><body><a href=\"\">Table B No Match</A><body></html>")) {
					isLeft = false;
					if(Rdbms_conn.getDBType().compareToIgnoreCase("oracle_native") == 0
					|| (Rdbms_conn.getDBType().compareToIgnoreCase("oracle_odbc") == 0)){
						query=qb.get_col_match_value(multiple, mX, false, false);
					}
					else {
						query = qb.get_match_value(multiple, mX, false, false);
					}
				} else {
					isLeft = false;
					if(Rdbms_conn.getDBType().compareToIgnoreCase("oracle_native") == 0
					|| (Rdbms_conn.getDBType().compareToIgnoreCase("oracle_odbc") == 0)){
						query=qb.get_col_match_value(multiple, mX, true, false);
					}
					else {
						query = qb.get_match_value(multiple, mX, true, false);
					}
					
				}
                
				String pc=""; // Primary column to bring as first column
				if ( isEditable == false ) {
				Rdbms_conn.openConn();
				ResultSet resultset = Rdbms_conn.runQuery(query);
				ReportTable _rt = SqlTablePanel.getSQLValue(resultset, true);
				resultset.close();
				Rdbms_conn.closeConn();
				
				// Put Primary column to First
				int cc = _rt.table.getColumnCount();
				if (str1.equals(str2) || isLeft) {
					for (int i = 0; i < cc; i++) {
						String cn = _rt.table.getColumnName(i);
						if (cn.equals(str3)) {
							pc = str3;
							_rt.table.moveColumn(i, 0);
							break;
						}
					}
				}
				if (str1.equals(str2) || !isLeft) {
					for (int i = 0; i < cc; i++) {
						String cn = _rt.table.getColumnName(i);
						if (cn.equals(str4)) {
							pc = str4;
							if (str1.equals(str2)) {
								_rt.table.moveColumn(i, 1);
							}
							else {
								_rt.table.moveColumn(i, 0);
							}
							break;
						}
					}
				}

				rowsetP.removeAll();
				rowsetP.add(_rt);
				tp.revalidate();
				tp.repaint();
				} else { // Editable row 
					if (rowsetPanel != null)
						rowsetPanel.rowset.close();
					rowsetPanel = new JDBCRowsetPanel(query,true,pc);
					rowsetP.removeAll();
					rowsetP.add(rowsetPanel);
					tp.revalidate();
					tp.repaint();
				}

			} catch (Exception sql_e) {
				ConsoleFrame.addText("\n Exception " + sql_e.getMessage());

			} finally {
				mouseevent
						.getComponent()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}

		}

		public void mouseEntered(MouseEvent mouseevent) {
			mouseevent.getComponent().setCursor(Cursor.getPredefinedCursor(12));
		}

		private LinkMouseListener() {
		}

	}

} // End of class
