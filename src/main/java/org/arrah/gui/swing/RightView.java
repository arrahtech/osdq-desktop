package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
 *                                             *
 *                                             *
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
 * for analysis. It also hold different analytics 
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class RightView implements ItemListener,ActionListener {

	private JScrollPane htmlView;
	private JLabel schema_name, table_name, col_name;
	private Hashtable<String, String> table_info;
	private JPanel parent, body, body_p;
	private JCheckBox edit_c;
	private JComboBox<String> ana_combo;

	public RightView() {

		
		parent = new JPanel();
		parent.setLayout(new BorderLayout());

		// Create top Panel
    parent.add(createTopPane(), BorderLayout.PAGE_START);

		// Create the body Panel
		body = new JPanel(new GridLayout(0,1));
		parent.add(body, "Center");
		
		// Create the HTML viewing pane.
		htmlView = new JScrollPane();
		htmlView.getViewport().add(parent);
	}

	// Public interface for Profiler

	public JScrollPane getRScrollPane() {
		return htmlView;

	}

	public void setLabel(Hashtable<String, String> map) {
		table_info = map;

		schema_name.setText("" + table_info.get("Schema"));
		table_name.setText("" + table_info.get("Table"));
		col_name.setText("" + table_info.get("Column"));
		edit_c.setSelected(false);
		ana_combo.setSelectedIndex(0); // Set it to first
		if (body_p != null && body_p instanceof JDBCRowsetPanel)
			((JDBCRowsetPanel) body_p).rowset.close();
		body.removeAll();
		body_p = null;
		parent.revalidate();
		parent.repaint();
		
	}

	private JPanel createTopPane() {
		JPanel topPane = new JPanel();
		SpringLayout layout = new SpringLayout();
		topPane.setLayout(layout);

		schema_name = new JLabel("DSN", JLabel.CENTER);
		schema_name.setToolTipText("Schema");
		topPane.add(schema_name);
		table_name = new JLabel("Table");
		table_name.setToolTipText("Table");
		topPane.add(table_name);
		col_name = new JLabel("Column");
		col_name.setToolTipText("Column");
		topPane.add(col_name);

		JPanel ep = new JPanel();
		edit_c = new JCheckBox("Edit Mode");
		ep.add(edit_c);
		JButton anal = new JButton("Show Record");
		anal.addKeyListener(new KeyBoardListener());
		anal.setActionCommand("analyse");
		anal.addActionListener(this);
		ep.add(anal);
		topPane.add(ep);
		Border line_b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		ep.setBorder(line_b);

    ImageIcon imageicon = new ImageIcon(RightView.class
        .getClassLoader().getResource("image/Filter.gif"), "Query");

		int imageLS = imageicon.getImageLoadStatus();
		JLabel jlabel;
		if (imageLS == MediaTracker.ABORTED || imageLS == MediaTracker.ERRORED)
			jlabel = new JLabel(
					"<html><body><a href=\"\">Query</A><body></html>", 0);
		else
			jlabel = new JLabel(imageicon, JLabel.CENTER);
		jlabel.addMouseListener(new LinkMouseListener());
		jlabel.setToolTipText("Add Conditions");
		topPane.add(jlabel);

		JLabel show_l = new JLabel(
				"<html><body><a href=\"\">Show Condition</A><body></html>", 0);
		show_l.addMouseListener(new LinkMouseListener());
		topPane.add(show_l);
		
		/* All analysis should be selectable by comboBox */
		
		ana_combo = new JComboBox<String> (new String[] {"Analysis Method","-----------------------",
				"Number Analysis","String Analysis","String Length Analysis","Timeliness Analysis"} );
		ana_combo.addItemListener(this);
		topPane.add(ana_combo);

		// Set the Border for topPane
		topPane.setBorder(BorderFactory.createLineBorder(Color.black));
		layout.putConstraint(SpringLayout.WEST, schema_name, 15,
				SpringLayout.WEST, topPane);
		layout.putConstraint(SpringLayout.NORTH, schema_name, 5,
				SpringLayout.NORTH, topPane);
		layout.putConstraint(SpringLayout.WEST, table_name, 65,
				SpringLayout.EAST, schema_name);
		layout.putConstraint(SpringLayout.NORTH, table_name, 5,
				SpringLayout.NORTH, topPane);
		layout.putConstraint(SpringLayout.WEST, col_name, 65,
				SpringLayout.EAST, table_name);
		layout.putConstraint(SpringLayout.NORTH, col_name, 5,
				SpringLayout.NORTH, topPane);
		layout.putConstraint(SpringLayout.WEST, ep, 425, SpringLayout.WEST,
				topPane);
		layout.putConstraint(SpringLayout.NORTH, ep, -5, SpringLayout.NORTH,
				jlabel);

		layout.putConstraint(SpringLayout.WEST, jlabel, 0, SpringLayout.WEST,
				schema_name);
		layout.putConstraint(SpringLayout.NORTH, jlabel, 4, SpringLayout.SOUTH,
				schema_name);
		layout.putConstraint(SpringLayout.WEST, show_l, 15, SpringLayout.EAST,
				jlabel);
		layout.putConstraint(SpringLayout.NORTH, show_l, 4, SpringLayout.SOUTH,
				schema_name);
		layout.putConstraint(SpringLayout.WEST, ana_combo, 25, SpringLayout.EAST,
				show_l);
		layout.putConstraint(SpringLayout.NORTH, ana_combo, 4, SpringLayout.SOUTH,
				schema_name);
		
		layout.putConstraint(SpringLayout.SOUTH, topPane, 2,
				SpringLayout.SOUTH, ep);

		return topPane;
	}

	public void actionPerformed(ActionEvent e) {

		String dsn_str = "" + table_info.get("Schema");
		// String type_str = "" + table_info.get("Type");
		String tbl_str = "" + table_info.get("Table");
		String col_str = "" + table_info.get("Column");
		QueryBuilder q_factory = new QueryBuilder(dsn_str, tbl_str, col_str,
				Rdbms_conn.getDBType());

		if (body_p != null && body_p instanceof JDBCRowsetPanel)
			((JDBCRowsetPanel) body_p).rowset.close();

		boolean emode = false;
		if (edit_c.isSelected()) {
			int n = JOptionPane
					.showConfirmDialog(
							null,
							"Edit Mode will update underlying Database \n Do you want Edit Mode ?",
							"Edit Option", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION)
				emode = true;
		}
		if (emode == false)
			edit_c.setSelected(false);

		String q1 = q_factory.get_tb_value(true);

		try {
			htmlView.getTopLevelAncestor().setCursor(
					java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			body_p = new JDBCRowsetPanel(q1, emode, col_str);
		} catch (SQLException exp) {
			try {
				q1 = q_factory.get_tb_value(false);
				body_p = new JDBCRowsetPanel(q1, edit_c.isSelected(), col_str);
			} catch (SQLException exp1) {
				JOptionPane.showMessageDialog(null, exp1.getMessage(),
						"Error Message", JOptionPane.ERROR_MESSAGE);
				ConsoleFrame.addText("\n " + exp1.getMessage());
				body.removeAll();
				return;
			}
		} finally {
			htmlView.getTopLevelAncestor()
					.setCursor(
							java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}
		body.removeAll();
		body.add(body_p);
		parent.revalidate();
		parent.repaint();		
		return;
	}

	/* Link Mouse Adapter */
	private class LinkMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent mouseevent) {
			try {
				mouseevent
						.getComponent()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				String s1 = ((JLabel) mouseevent.getSource()).getText();
				if (s1 != null
						&& s1.equals("<html><body><a href=\"\">Show Condition</A><body></html>")) {
					String qry_msg = QueryBuilder.getCond();
					if ("".equals(qry_msg))
						qry_msg = "Condition Not Set";
					JOptionPane.showMessageDialog(null, qry_msg,
							"Query Information",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}  else {
					Vector<String> vector = Rdbms_conn.getTable();
					int i = vector.indexOf(table_name.getText());

					Vector<?> avector[] = null;
					avector = TableMetaInfo.populateTable(5, i, i + 1, avector);

					QueryDialog querydialog = new QueryDialog(2,
							table_name.getText(), avector);
					querydialog.setColumn(col_name.getText());
					querydialog.setLocation(175, 100);
					querydialog.setTitle(col_name.getText() + " Query Setup ");
					querydialog.setModal(true);
					querydialog.pack();
					querydialog.setVisible(true);
					int j = querydialog.response;
					if (j == 1) {
						String c_query = querydialog.cond;
						if ("".equals(c_query))
							QueryBuilder.unsetCond();
						else
							QueryBuilder.setCond(c_query);
						table_info.put("Condition", c_query); // Put into
																// HashTable
						body.removeAll();
						parent.revalidate();
						parent.repaint();
					}
					return;
				}
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

	public void itemStateChanged(ItemEvent e) {
		
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getItem().toString().equalsIgnoreCase("Number Analysis") == true){
				if (body_p != null && body_p instanceof JDBCRowsetPanel)
					((JDBCRowsetPanel) body_p).rowset.close();
				body_p = new ReportViewer(table_info);
				body.removeAll();
				body.add(body_p);
				parent.revalidate();
				parent.repaint();
				return;
			}
			if (e.getItem().toString().equalsIgnoreCase("String Analysis") == true){
				if (body_p != null && body_p instanceof JDBCRowsetPanel)
					((JDBCRowsetPanel) body_p).rowset.close();
				body_p = new StringProfilerPanel(table_info);
				body.removeAll();
				body.add(body_p);
				parent.revalidate();
				parent.repaint();
				return;
			}
			if (e.getItem().toString().equalsIgnoreCase("String Length Analysis") == true){
				if (body_p != null && body_p instanceof JDBCRowsetPanel)
					((JDBCRowsetPanel) body_p).rowset.close();
				try {
					htmlView.getTopLevelAncestor().setCursor(
						java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
					body_p = new StringLenProfilerPanel(table_info);
					body.removeAll();
					body.add(body_p);
					parent.revalidate();
					parent.repaint();
				} finally {
					htmlView.getTopLevelAncestor().setCursor(
							java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				}
				return;
			}
			if (e.getItem().toString().equalsIgnoreCase("Timeliness Analysis") == true){
				if (body_p != null && body_p instanceof JDBCRowsetPanel)
					((JDBCRowsetPanel) body_p).rowset.close();
				try {
					htmlView.getTopLevelAncestor().setCursor(
							java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
					body_p = new TimeProfilerPanel(table_info);
					body.removeAll();
					body.add(body_p);
					parent.revalidate();
					parent.repaint();
				} finally {
					htmlView.getTopLevelAncestor().setCursor(
							java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				}
				return;

			}
			if (body_p != null && body_p instanceof JDBCRowsetPanel)
				((JDBCRowsetPanel) body_p).rowset.close();
			body_p = new JPanel();
			body.removeAll();
			body.add(body_p);
			parent.revalidate();
			parent.repaint();
			return;
			
		} // End of ItemEvent.SELECTED
		
	}

}
