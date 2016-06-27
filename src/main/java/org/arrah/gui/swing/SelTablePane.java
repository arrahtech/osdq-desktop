package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2007      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for displaying Panel where
 * user can select the table and colums required.
 *
 */

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.Rdbms_conn;

public class SelTablePane extends JPanel implements ItemListener,
		ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox <String> table1;
	private Vector vector1[];
	private DefaultListModel<String> all_l_model, input_l_model;
	private JList<String> all_l = null, input_l = null;
	private boolean init, conditionable = true;
	private int sel_mode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
	private String queryString = "";

	public SelTablePane() {
		createPane();
	}

	public SelTablePane(int selectionMode) {
		sel_mode = selectionMode;
		createPane();
	}

	public SelTablePane(int selectionMode, boolean condition) {
		sel_mode = selectionMode;
		conditionable = condition;
		createPane();
	}

	private void createPane() {
		// TODO - add information panel to give more information
		JLabel ta = new JLabel("Select Table");

		Vector<String> vector = Rdbms_conn.getTable();
		table1 = new JComboBox<String>();
		table1.addItemListener(this);
		for (int i = 0; i < vector.size(); i++) {
			String item = (String) vector.get(i);
			table1.addItem(item);
		}

		JLabel tb = new JLabel("Select Column(s) for Quality Rule");
		all_l_model = new DefaultListModel<String>();
		vector1 = TableMetaInfo.populateTable(5, 0, 1, vector1);
		if (vector1 == null)
			return; // no columns to populate
		for (int i = 0; i < vector1[0].size(); i++) {
			String item = (String) vector1[0].get(i);
			all_l_model.add(i, item);
		}
		all_l = new JList<String>(all_l_model);
		all_l.setSelectionMode(sel_mode);
		JScrollPane all_sp = new JScrollPane(all_l);

		JLabel tc = new JLabel("Selected Column(s)");
		input_l_model = new DefaultListModel<String>();
		input_l = new JList<String>(input_l_model);
		JScrollPane input_sp = new JScrollPane(input_l);

		all_sp.setPreferredSize(new Dimension(200, 240));
		input_sp.setPreferredSize(new Dimension(200, 240));

		JButton toInput = new JButton(">>");
		toInput.setActionCommand("toinput");
		toInput.addActionListener(this);
		JButton fromInput = new JButton("<<");
		fromInput.setActionCommand("frominput");
		fromInput.addActionListener(this);

    ImageIcon imageicon;
    imageicon = new ImageIcon(SelTablePane.class
        .getClassLoader().getResource("image/Filter.gif"), "Query");
		int imageLS = imageicon.getImageLoadStatus();
		JLabel jlabel;
		if (imageLS == MediaTracker.ABORTED || imageLS == MediaTracker.ERRORED)
			jlabel = new JLabel(
					"<html><body><a href=\"\">Query</A><body></html>", 0);
		else
			jlabel = new JLabel(imageicon, JLabel.CENTER);
		jlabel.addMouseListener(new LinkMouseListener());
		jlabel.setToolTipText("Click to Add Conditions");
		if (conditionable == false)
			jlabel.setVisible(false);

		JLabel dummy = new JLabel(
				"<html><body><a href=\"\">Show Condition</A><body></html>", 0);
		dummy.addMouseListener(new LinkMouseListener());

		add(ta);
		add(tb);
		add(tc);
		add(table1);
		add(all_sp);
		add(input_sp);
		add(toInput);
		add(fromInput);
		add(jlabel);
		add(dummy);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		layout.putConstraint(SpringLayout.WEST, ta, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, ta, 5, SpringLayout.NORTH,
				this);
		layout.putConstraint(SpringLayout.WEST, table1, 5, SpringLayout.WEST,
				this);
		layout.putConstraint(SpringLayout.NORTH, table1, 5, SpringLayout.SOUTH,
				ta);
		layout.putConstraint(SpringLayout.WEST, jlabel, 15, SpringLayout.EAST,
				table1);
		layout.putConstraint(SpringLayout.NORTH, jlabel, 0, SpringLayout.NORTH,
				table1);
		layout.putConstraint(SpringLayout.WEST, dummy, 15, SpringLayout.EAST,
				jlabel);
		layout.putConstraint(SpringLayout.NORTH, dummy, 0, SpringLayout.NORTH,
				jlabel);
		layout.putConstraint(SpringLayout.WEST, tb, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, tb, 5, SpringLayout.SOUTH,
				table1);
		layout.putConstraint(SpringLayout.WEST, all_sp, 5, SpringLayout.WEST,
				this);
		layout.putConstraint(SpringLayout.NORTH, all_sp, 5, SpringLayout.SOUTH,
				tb);
		layout.putConstraint(SpringLayout.WEST, toInput, 15, SpringLayout.EAST,
				all_sp);
		layout.putConstraint(SpringLayout.NORTH, toInput, 15,
				SpringLayout.NORTH, all_sp);
		layout.putConstraint(SpringLayout.WEST, fromInput, 15,
				SpringLayout.EAST, all_sp);
		layout.putConstraint(SpringLayout.NORTH, fromInput, -50,
				SpringLayout.SOUTH, all_sp);
		layout.putConstraint(SpringLayout.WEST, input_sp, 15,
				SpringLayout.EAST, toInput);
		layout.putConstraint(SpringLayout.NORTH, input_sp, 0,
				SpringLayout.NORTH, all_sp);
		layout.putConstraint(SpringLayout.WEST, tc, 0, SpringLayout.WEST,
				input_sp);
		layout.putConstraint(SpringLayout.SOUTH, tc, -5, SpringLayout.NORTH,
				input_sp);
		layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST,
				input_sp);
		layout.putConstraint(SpringLayout.SOUTH, this, 15, SpringLayout.SOUTH,
				input_sp);
		init = true;
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && init == true) {
			if (e.getSource().equals(table1)) {
				queryString = "";
				int index = table1.getSelectedIndex();
				vector1 = TableMetaInfo.populateTable(5, index, index + 1,
						vector1);
				int va = ((Integer) (vector1[1].get(0))).intValue();
				all_l_model.clear();
				input_l_model.clear();
				for (int i = 0; i < vector1[0].size(); i++) {
					String item = (String) vector1[0].get(i);
					all_l_model.add(i, item);
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("toinput")) {
			moveItem(all_l, input_l);
			return;
		}
		if (command.equals("frominput")) {
			moveItem(input_l, all_l);
			return;
		}
	}

	private void moveItem(JList<String> source, JList<String> destination) {
		int[] indices = source.getSelectedIndices();
		DefaultListModel<String> source_m = (DefaultListModel<String>) source.getModel();
		DefaultListModel<String> dest_m = (DefaultListModel<String>) destination.getModel();

		/* delete/add from bottom so that index does not change */
		for (int i = indices.length - 1; i >= 0; i--) {
			dest_m.addElement(source_m.elementAt(indices[i]));
			source_m.removeElementAt(indices[i]);
		}
	}

	public String getTable() {
		return table1.getSelectedItem().toString();
	}

	public Vector<String> getColumns() {
		Vector<String> vc = new Vector<String>();
		int i = 0;
		Enumeration<String> e = ((DefaultListModel<String>) input_l.getModel()).elements();
		while (e.hasMoreElements())
			vc.add(i++, e.nextElement().toString());
		return vc;
	}

	public String getQueryString() {
		if (conditionable == false)
			return "";
		return queryString;
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
					String qry_msg = queryString;
					if ("".equals(qry_msg))
						qry_msg = "Condition Not Set";
					JOptionPane.showMessageDialog(null, qry_msg,
							"Query Information",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				} else {
					Vector<String> vector = Rdbms_conn.getTable();
					int i = vector.indexOf(table1.getSelectedItem().toString());

					Vector avector[] = null;
					avector = TableMetaInfo.populateTable(5, i, i + 1, avector);

					QueryDialog querydialog = new QueryDialog(2, table1
							.getSelectedItem().toString(), avector);
					querydialog.setColumn("Dummy");
					querydialog.setLocation(175, 100);
					querydialog.setTitle(" DataQuality Query Setup ");
					querydialog.setModal(true);
					querydialog.pack();
					querydialog.setVisible(true);
					int j = querydialog.response;
					if (j == 1) {
						queryString = querydialog.cond;
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

} // End of class
