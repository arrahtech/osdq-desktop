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
 * This file is used to Search DB
 * and the output is displayed in ReportTable
 *
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.arrah.framework.profile.DBMetaInfo;
import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class SearchDBPanel {
	private String _searchS = null;
	private ReportTable _rt = null;
	private ReportTable newRT = null;
	private JLabel n_l;
	private JPanel jp;
	private JFrame fm;
	private boolean _tableSearch = false;
	private String _tabName;
	private Vector<String> _colName;

	public SearchDBPanel(String searchString) {
		_searchS = searchString;
		queryDB(_searchS);
		createGUI();
	};
	public SearchDBPanel(String searchString, String tabName, Vector<String> colName) {
		_tableSearch = true;
		_tabName = tabName;
		_colName = colName;
		_searchS = searchString;
		queryDB(_searchS);
		createGUI(); 
	};

	private ReportTable queryDB(final String query) {
		if (_tableSearch == false)
		_rt = new ReportTable(DBMetaInfo.queryDB(query));
		else
		_rt = new ReportTable(TableMetaInfo.queryTable(query,_tabName,_colName));
		
		_rt.table.getColumnModel().getColumn(0)
				.setCellRenderer(new MyCellRenderer());
		_rt.table.addMouseListener(new MyCellRenderer());
		_rt.table.addMouseMotionListener(new MyCellRenderer());
		return _rt;
	}

	public void createGUI() {
		jp = new JPanel();
		jp.setLayout(new BorderLayout());

		JPanel tp = new JPanel();
		n_l = new JLabel(
				"<html><body><a href=\"\">Aggregate View  </A><body></html>");
		n_l.setVisible(false);
		n_l.addMouseListener(new LinkMouseListener());

		JLabel c_l = new JLabel(
				"<html><body><a href=\"\">  Close</A><body></html>",
				JLabel.TRAILING);
		c_l.addMouseListener(new LinkMouseListener());

		tp.add(n_l);
		tp.add(c_l);

		jp.add(tp, BorderLayout.PAGE_START);
		jp.add(_rt, BorderLayout.CENTER);

		fm = new JFrame("\""+ _searchS+ "\"" + " String DB Search");
		fm.getContentPane().add(jp);
		fm.setLocation(150, 150);
		fm.pack();
		fm.setVisible(true);
		
		QualityListener.bringToFront(fm);

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
						&& s1.equals("<html><body><a href=\"\">Aggregate View  </A><body></html>")) {

					jp.remove(newRT);
					jp.add(_rt, BorderLayout.CENTER);
					jp.revalidate();
					jp.repaint();
					n_l.setVisible(false);
				} else if (s1 != null
						&& s1.equals("<html><body><a href=\"\">  Close</A><body></html>")) {
					fm.dispose();
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

	private class MyCellRenderer extends DefaultTableCellRenderer implements
			MouseListener, MouseMotionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MyCellRenderer() {
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			String text = ((JLabel) c).getText();
			((JLabel) c).setHorizontalAlignment(JLabel.TRAILING);
			((JLabel) c).setText("<html><body><a href=\"\">" + text
					+ "</A><body></html>");
			return c;
		}

		public void mousePressed(MouseEvent e) {
			// Do Nothing
		}

		public void mouseReleased(MouseEvent e) {
			// Do Nothing
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
			// Do Nothing
		}

		public void mouseDragged(MouseEvent e) {
			// Do Nothing
		}

		public void mouseClicked(MouseEvent e) {
			Point p = e.getPoint();
			int col_i = _rt.table.columnAtPoint(p);
			int row_i = _rt.table.rowAtPoint(p);
			if (row_i >= _rt.table.getRowCount() || row_i < 0)
				return;

			if (_rt.isSorting()) {
				JOptionPane.showMessageDialog(null, "Table in Sorting state",
						"Sorting Error", JOptionPane.ERROR_MESSAGE);
				_rt.cancelSorting();
				return;
			}
			_rt.table.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			String table_s = _rt.getValueAt(row_i, 1).toString();
			QueryBuilder qb = new QueryBuilder(
					Rdbms_conn.getHValue("Database_DSN"), table_s,
					Rdbms_conn.getDBType());
			String toQuery ="";
			if (_tableSearch == false)
				toQuery = qb.get_like_table(_searchS, row_i, false);
			else
				toQuery = qb.get_like_table_cols(_searchS, _colName, false);	
			try {
				Rdbms_conn.openConn();
				ResultSet rs = Rdbms_conn.runQuery(toQuery);
				newRT = SqlTablePanel.getSQLValue(rs, true);
				Rdbms_conn.closeConn();
			} catch (SQLException ee) {
				ConsoleFrame.addText("\n SQL Exception:" + ee.getMessage());
				return; // newRT can not be populated
			}
			jp.remove(_rt);
			jp.add(newRT, BorderLayout.CENTER);
			jp.revalidate();
			jp.repaint();

			n_l.setVisible(true);
		}

		public void mouseMoved(MouseEvent e) {
			Point p = e.getPoint();
			int col_i = _rt.table.columnAtPoint(p);
			int row_i = _rt.table.rowAtPoint(p);
			String colName = _rt.table.getColumnName(col_i);

			if (colName.equalsIgnoreCase("COUNT") == true) {
				_rt.table.setCursor(Cursor
						.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				_rt.table.setCursor(Cursor.getDefaultCursor());
			}
		}
	} // End of MyCellRenderer
} // End of SearchDB
