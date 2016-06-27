package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This files is used for creating Header
 * for editable reportTable 
 *
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.arrah.framework.ndtable.TableSorter;

/* TableRowHeader.java is used by ReportTable */

public class TableRowHeader extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TableCellRenderer render = new RowHeaderRenderer();
	private JTable _table;
	private ReportTable _rt;
	private RowHeaderModel rm;
	private boolean isHeaderChanging = false;
	private Vector<Object[]> clippedRow = null;

	public TableRowHeader(ReportTable rt) {
		_rt = rt;
		_table = rt.table;
		TableModelListener tmListener = new TableModelListener() {

			public void tableChanged(TableModelEvent e) {
				if (e.getType() != TableModelEvent.UPDATE) {
					resizeAndRepaint();
				}
			}
		};
		_table.getModel().addTableModelListener(tmListener);
		rm = new RowHeaderModel(_table);
		rm.addTableModelListener(tmListener);
		super.setModel(rm);
		configure(_table);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(true);
	}

	protected void configure(JTable table) {
		setRowHeight(table.getRowHeight());
		setIntercellSpacing(new Dimension(0, 0));
		setShowHorizontalLines(false);
		setShowVerticalLines(false);
		addMouseListener(new PopupListener());
	}

	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(16, super.getPreferredSize().height);
	}

	public TableCellRenderer getDefaultRenderer(Class<?> c) {
		return render;
	}

	static class RowHeaderModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JTable table;

		protected RowHeaderModel(JTable tableToMirror) {
			table = tableToMirror;
		}

		public int getRowCount() {
			return table.getModel().getRowCount();
		}

		public int getColumnCount() {
			return 1;
		}

		public Object getValueAt(int row, int column) {
			return String.valueOf(row + 1);
		}
	}

	private class RowHeaderRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelect, boolean hasFocus, int row,
				int column) {
			setBackground(UIManager.getColor("TableHeader.background"));
			setForeground(UIManager.getColor("TableHeader.foreground"));
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setIcon(TableSorter.getDownArrow());
			if (isSelect) {
				setBackground(Color.BLUE);
				setToolTipText("Right Click for Menu");
			} else
				setToolTipText(null);
			return this;
		}
	}

	public boolean isHeaderChanging() {
		return isHeaderChanging;
	}

	public void valueChanged(ListSelectionEvent e) {
		super.valueChanged(e);
		if (!e.getValueIsAdjusting()) {
			int[] rc = getSelectedRows();
			isHeaderChanging = true;
			_table.setColumnSelectionInterval(0, _table.getColumnCount() - 1);
			for (int i = 0; i < rc.length; i++)
				if (i == 0)
					_table.setRowSelectionInterval(rc[i], rc[i]);
				else
					_table.addRowSelectionInterval(rc[i], rc[i]);
			isHeaderChanging = false;
		}

	}

	public class PopupListener extends MouseAdapter {
		PopupListener() {
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu popup = new JPopupMenu();
				PopupAction popAc = new PopupAction();

				JMenuItem menuItem = new JMenuItem("Insert Row");
				menuItem.setActionCommand("insert");
				menuItem.addActionListener(popAc);
				popup.add(menuItem);
				popup.addSeparator();

				JMenuItem menuItem0 = new JMenuItem("Insert Clip");
				menuItem0.setActionCommand("insertclip");
				menuItem0.addActionListener(popAc);
				popup.add(menuItem0);
				popup.addSeparator();

				JMenuItem menuItem1 = new JMenuItem("Delete");
				menuItem1.setActionCommand("delete");
				menuItem1.addActionListener(popAc);
				popup.add(menuItem1);
				popup.addSeparator();

				JMenuItem menuItem2 = new JMenuItem("Copy");
				menuItem2.setActionCommand("copy");
				menuItem2.addActionListener(popAc);
				popup.add(menuItem2);
				popup.addSeparator();

				JMenuItem menuItem3 = new JMenuItem("Paste");
				menuItem3.setActionCommand("paste");
				menuItem3.addActionListener(popAc);
				if (clippedRow == null || clippedRow.size() == 0)
					menuItem3.setEnabled(false);
				popup.add(menuItem3);
				popup.addSeparator();

				Point pt = e.getPoint();
				TableRowHeader trd = ((TableRowHeader) e.getSource());
				int rowid = trd.rowAtPoint(pt);
				if (trd.isRowSelected(rowid))
					popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	private class PopupAction implements ActionListener {
		public PopupAction() {
		};

		public void actionPerformed(ActionEvent e) {
			int[] rowA = _table.getSelectedRows();
			Arrays.sort(rowA);
			int sc = rowA.length;
			if (_table.isEditing()) {
				JOptionPane.showMessageDialog(null, "Table is Being Edited",
						"Error Message", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String command = e.getActionCommand();

			if ("delete".equals(command)) {
				int n = JOptionPane.showConfirmDialog(null,
						"Do you want to Delete " + sc + " Rows?",
						"Confirmation Dialog", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION)
					return;

				clippedRow = new Vector<Object[]>();
				int vci = 0;
				for (int i = sc - 1; i >= 0; i--) {
					Object[] a = _rt.copyRow(rowA[i]);
					clippedRow.add(vci, a);
					_rt.removeRows(rowA[i], 1);
					vci++;
				}
				return;
			}
			if ("insert".equals(command)) {
				if (_rt.isSorting()) {
					_rt.addRows(rowA[0], 1);
					_table.editCellAt(_table.getRowCount() - 1, 0);
				} else {
					_rt.addRows(rowA[0], 1);
					_table.editCellAt(rowA[0], 0);
				}
				_table.clearSelection();
				return;
			}
			if ("copy".equals(command)) {
				clippedRow = new Vector<Object[]>();
				int vci = 0;
				for (int i = sc - 1; i >= 0; i--) {
					Object[] a = _rt.copyRow(rowA[i]);
					clippedRow.add(vci, a);
					vci++;
				}
				_table.clearSelection();
				return;
			}
			if ("paste".equals(command)) {
				_rt.pasteRow(rowA[0], clippedRow);
				return;
			}
			if ("insertclip".equals(command)) {
				if (clippedRow == null || clippedRow.size() == 0) {
					if (_rt.isSorting()) {
						_rt.addRows(rowA[0], 1);
						_table.editCellAt(_table.getRowCount() - 1, 0);
					} else {
						_rt.addRows(rowA[0], 1);
						_table.editCellAt(rowA[0], 0);
					}
				} else {
					if (_rt.isSorting()) {
						int rowc = _rt.table.getRowCount();
						_rt.addRows(rowA[0], clippedRow.size());
						_rt.pasteRow(rowc, clippedRow);
						_table.editCellAt(rowc, 0);
					} else {
						_rt.addRows(rowA[0], clippedRow.size());
						_rt.pasteRow(rowA[0], clippedRow);
						_table.editCellAt(rowA[0], 0);
					}
				}
				_table.clearSelection();
			}

		}
	}
}
