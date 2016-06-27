package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2006      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with copyright      *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This file is used for creating variable Query  
 * This class will have reference to query object
 * that needs to be run to get next/previous value 
 */

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.Rdbms_conn;

public class TableMetaInfoPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private int currentP = 1; // Current Page in the GUI
	private int totalP = 0; // Current Page in the GUI
	private int tablePPage = 1; // Number of tables in a page
	private ReportTable __rt = null; // ReportTable for holding the record
	private ReportTableModel rtm = null;
	private JFormattedTextField ft, ft_p;
	private int queryID = 0;
	private int tableC = Rdbms_conn.getTableCount(); // Table Count
	private JLabel l_e, l_s;
	private JButton go;
	private static int DEFAULT_TABLE = 10; // Default Table number

	public TableMetaInfoPanel(int QueryId) throws NullPointerException {
		queryID = QueryId;
		tablePPage = (tableC > DEFAULT_TABLE) ? DEFAULT_TABLE : tableC;
		populateTable(QueryId, 0, tablePPage);
		if (__rt == null) {
			ConsoleFrame.addText("\n ERROR: Record Table Null");
			throw new NullPointerException();
		}
		createGUI();
	}

	private void createGUI() {
		setLayout(new BorderLayout());
		JPanel tp = new JPanel();
		JLabel l = new JLabel("Tables per page:", JLabel.TRAILING);
		ft = new JFormattedTextField(NumberFormat.getIntegerInstance());
		ft.setColumns(4);
		ft.setValue(new Integer(tablePPage));
		JButton jb = new JButton("Update");
		jb.addKeyListener(new KeyBoardListener());
		jb.setActionCommand("update");
		jb.addActionListener(this);

		JLabel p_l = new JLabel(
				"<html><body><a href=\"\">&lt;&lt;Prev</A><body></html>");
		p_l.addMouseListener(new LinkMouseListener());
		JLabel n_l = new JLabel(
				"<html><body><a href=\"\">Next&gt;&gt;</A><body></html>");
		n_l.addMouseListener(new LinkMouseListener());

		JLabel l_p = new JLabel("Page:", JLabel.TRAILING);
		ft_p = new JFormattedTextField(NumberFormat.getIntegerInstance());
		ft_p.setColumns(5);
		ft_p.setValue(new Integer(1));
		if (tablePPage >= tableC)
			ft_p.setEditable(false);
		totalP = pageCount();
		l_e = new JLabel(" of " + totalP, JLabel.TRAILING);
		go = new JButton("Go");
		go.addKeyListener(new KeyBoardListener());
		go.setActionCommand("goto");
		go.addActionListener(this);
		l_s = new JLabel(" Showing 1 - " + tablePPage + " of total " + tableC
				+ " tables in Page " + currentP);

		tp.add(p_l);
		tp.add(n_l);
		tp.add(l);
		tp.add(ft);
		tp.add(jb);
		tp.add(l_p);
		tp.add(ft_p);
		tp.add(l_e);
		tp.add(go);
		add(tp, BorderLayout.PAGE_START);
		add(__rt, BorderLayout.CENTER);
		add(l_s, BorderLayout.PAGE_END);

	}

	private void populateTable(int QueryID, int fromIndex, int toIndex) {
		rtm = TableMetaInfo.populateTable(QueryID, fromIndex,toIndex, rtm);
		__rt = new ReportTable(rtm);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("update")) {
			int toIndex = ((Number) ft.getValue()).intValue();
			if (toIndex <= 0) {
				ft.setValue(new Integer(tablePPage));
				return;
			}
			if (toIndex > tableC) {
				if (tablePPage == tableC) {
					ft.setValue(new Integer(tablePPage));
					return;
				}
				tablePPage = tableC;
				ft.setValue(new Integer(tablePPage));
				ConsoleFrame
						.addText("\n Information: Display Table Count is more than Total Table Count");
				ConsoleFrame
						.addText("\n Information: ReSetting to Total Table Count");
				JOptionPane.showMessageDialog(null,
						"Display Table Count is more than Total Table Count",
						"Error Message", JOptionPane.INFORMATION_MESSAGE);
			} else
				tablePPage = toIndex;

			totalP = pageCount();
			l_e.setText(" of " + totalP);
			if (totalP > 1)
				ft_p.setEditable(true);
			else
				ft_p.setEditable(false);
			currentP = 1;
			ft_p.setValue(new Integer(currentP));
			
			// Now change the cursor
			try {
				this.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				l_s.setText(" Showing 1 - " + tablePPage + " of total " + tableC
						+ " tables in Page " + currentP);
				populateTable(queryID, 0, tablePPage);
				revalidate();
				repaint();
			} finally {
				this.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
		}
		if (command.equals("goto")) {

			int fromPage = ((Number) ft_p.getValue()).intValue();
			if (fromPage <= 0 || fromPage > totalP) {
				ft_p.setValue(new Integer(currentP));
				return;
			}
			currentP = fromPage;
			ft.setValue(new Integer(tablePPage));
			int startIndex = (currentP - 1) * tablePPage;
			int endIndex = currentP * tablePPage;

			// Now change the cursor
			try {
				this.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
					if (endIndex > tableC)
						endIndex = tableC;
					populateTable(queryID, startIndex, endIndex);

			l_s.setText(" Showing " + (startIndex + 1) + " - " + endIndex
					+ " of total " + tableC + " tables in Page" + currentP);
			revalidate();
			repaint();
			} finally {
				this.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
		}
	}

	private int pageCount() {
		if (tableC == 0)
			return 1;
		if (tableC % tablePPage == 0)
			return tableC / tablePPage;
		else
			return (tableC / tablePPage) + 1;
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
						&& s1.equals("<html><body><a href=\"\">&lt;&lt;Prev</A><body></html>")) {
					int p = ((Number) ft_p.getValue()).intValue();
					ft_p.setValue(p - 1);
					go.doClick();
				} else if (s1 != null
						&& s1.equals("<html><body><a href=\"\">Next&gt;&gt;</A><body></html>")) {
					int p = ((Number) ft_p.getValue()).intValue();
					ft_p.setValue(p + 1);
					go.doClick();

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

}
