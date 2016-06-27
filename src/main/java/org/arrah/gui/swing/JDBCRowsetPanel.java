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
 * This class is used for displaying  
 *  reporttable integrated with JDBC Connected Rowset 
 *
 */
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import org.arrah.framework.rdbms.JDBCRowset;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.rdbms.UpdatableJdbcRowsetImpl;


public class JDBCRowsetPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private ReportTable _rt, _newRT;
	private UpdatableJdbcRowsetImpl rows;
	public JDBCRowset rowset; // a hack into JDBCRowset
	private String[] col_name;
	private int[] col_type;
	
	private JFormattedTextField ft, ft_p;
	private JLabel l_e;
	private int totalP = 1;
	private int currentP = 1;
	private int rowC = 0;
	private int rowPPage = 100; // Default Row number
	private Object prev_o = null;
	private int f_r = -1, c_e = -1;
	private int modelR = -1;
	private JButton go, jb;
	private Hashtable<Integer, Integer> modelToRow = null;
	private JDialog jd;
	private int numberOfColumns = 0;
	
	/* Need to keep constructor parameters for calling populate function again*/
	private boolean isEdit;
	private String _query = null;
	private Vector<Integer> _vc_t = null;
	private Vector<Object> _vc_v = null;
	private boolean isConstCall = false;

	/* IF YOU WANT GUI */
	public JDBCRowsetPanel(String query, boolean editable, String pc)
			throws SQLException {
		_query = query;
		isConstCall = true;
		try {
			rowset = new JDBCRowset(query, -1, editable);
			rows = rowset.getRowset();
			rowC = rowset.getRowCount();

		} catch (SQLException e) {
			System.err.println("Error in JDBCRowsetPanel Constructor:" +e.getLocalizedMessage());
			throw e;
		}
		isEdit = editable;
		createTH();
		populateTable(1, 100);
		createPanel(pc);
	}

	/* IF YOU WANT GUI IN Prepared query */
	// Hive will not support Prepared query
	public JDBCRowsetPanel(String query, boolean editable, String pc,
			Vector<Integer> vc_t, Vector<Object> vc_v) throws SQLException {
		_query = query;
		isConstCall = true;
		_vc_t = vc_t; _vc_v = vc_v;
		try {
			rowset = new JDBCRowset(query, editable, vc_t, vc_v);
			rows = rowset.getRowset();
			rowC = rowset.getRowCount();
		} catch (SQLException e) {
			System.err.println("Error in JDBCRowsetPanel Constructor:" +e.getLocalizedMessage());
			throw e;
		}
		isEdit = editable;
		createTH();
		populateTable(1, 100);
		createPanel(pc);
	}

	private void createPanel(String pc) {
		setLayout(new BorderLayout());
		// Put Primary column to First
		int cc = _rt.table.getColumnCount();
		for (int i = 0; i < cc; i++) {
			String cn = _rt.table.getColumnName(i);
			if (pc.equals(cn)) {
				_rt.table.moveColumn(i, 0);
				break;
			}
		}
		add(_rt, BorderLayout.CENTER);

		JLabel p_l = new JLabel(
				"<html><body><a href=\"\">&lt;&lt;Prev</A><body></html>");
		p_l.addMouseListener(new LinkMouseListener());
		JLabel n_l = new JLabel(
				"<html><body><a href=\"\">Next&gt;&gt;</A><body></html>");
		n_l.addMouseListener(new LinkMouseListener());

		JPanel rp = new JPanel();
		JLabel l = new JLabel("Rows in a page:", JLabel.TRAILING);
		ft = new JFormattedTextField(NumberFormat.getIntegerInstance());
		ft.setColumns(8);
		if (rowC <= rowPPage)
			rowPPage = rowC;
		ft.setValue(new Integer(rowPPage));

		jb = new JButton("Update");
		jb.addKeyListener(new KeyBoardListener());
		jb.setActionCommand("update");
		jb.addActionListener(this);

		JLabel l_p = new JLabel("Go to Page:", JLabel.TRAILING);
		ft_p = new JFormattedTextField(NumberFormat.getIntegerInstance());
		ft_p.setColumns(6);
		ft_p.setValue(new Integer(1));

		if (rowPPage >= rowC)
			ft_p.setEditable(false);
		totalP = pageCount();
		l_e = new JLabel(" of " + totalP, JLabel.TRAILING);
		go = new JButton("Go");
		go.addKeyListener(new KeyBoardListener());
		go.setActionCommand("goto");
		go.addActionListener(this);
		rp.add(p_l);
		rp.add(n_l);
		rp.add(l);
		rp.add(ft);
		rp.add(jb);
		rp.add(l_p);
		rp.add(ft_p);
		rp.add(l_e);
		rp.add(go);
		add(rp, BorderLayout.PAGE_START);

		JPanel bp = new JPanel();
		if (isEdit) {
			JButton add_b = new JButton("New Row");
			add_b.addKeyListener(new KeyBoardListener());
			add_b.setActionCommand("newrow");
			add_b.addActionListener(this);
			bp.add(add_b);
		}
		add(bp, BorderLayout.PAGE_END);
	}

	private void populateTable(int fromIndex, int toIndex) throws SQLException {
		if (Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") != 0 ) {
		rows.absolute(fromIndex);
		rows.previous();
		} else {
			/* Hive does not guarantee same order of row. Its cursor is not movable*/
			/* We need to call again and re-populate the rowset */
			
			JOptionPane.showMessageDialog(null,
					"Hive does not guarantee same sequence of rows for same operation",
					"Hive Information Message", JOptionPane.INFORMATION_MESSAGE);

			if (isConstCall != true) {
				if(_vc_t != null && _vc_v != null ) {
					rowset = new JDBCRowset(_query, isEdit, _vc_t, _vc_v);
				} else {
					rowset = new JDBCRowset(_query, -1, isEdit);
				}
				rows = rowset.getRowset();
				rowC = rowset.getRowCount();
			}
			
			isConstCall = false;
		}
		int counter = 0;
		if (_rt != null)
			_rt.cleanallRow();
		modelToRow = new Hashtable<Integer, Integer>();

		while (rows.next() && (toIndex >= fromIndex + counter)) {
			Vector<Object> row_v = new Vector<Object>();
			for (int i = 1; i < col_name.length + 1; i++) {
				switch (col_type[i - 1]) {
				case java.sql.Types.INTEGER:
				case java.sql.Types.TINYINT:
				case java.sql.Types.SMALLINT:
					row_v.add(i - 1, new Integer(rows.getInt(i)));
					break;
				case java.sql.Types.DOUBLE:
				case java.sql.Types.REAL:
				case java.sql.Types.DECIMAL:
				case java.sql.Types.NUMERIC:
				case java.sql.Types.BIGINT:
					row_v.add(i - 1, new Double(rows.getDouble(i)));
					break;
				case java.sql.Types.FLOAT:
					row_v.add(i - 1, new Float(rows.getFloat(i)));
					break;
				case java.sql.Types.CLOB:
					row_v.add(i - 1, rows.getClob(i));
					break;
				case java.sql.Types.BLOB:
					row_v.add(i - 1, rows.getBlob(i));
					break;
				case java.sql.Types.BOOLEAN:
				case java.sql.Types.BIT:
					row_v.add(i - 1, new Boolean(rows.getBoolean(i)));
					break;
				case java.sql.Types.DATE:
					row_v.add(i - 1, rows.getDate(i));
					break;
				case java.sql.Types.TIME:
					row_v.add(i - 1, rows.getTime(i));
					break;
				case java.sql.Types.TIMESTAMP:
					row_v.add(i - 1, rows.getTimestamp(i));
					break;
				case java.sql.Types.ARRAY:
					row_v.add(i - 1, rows.getArray(i));
					break;
				case java.sql.Types.REF:
					row_v.add(i - 1, rows.getRef(i));
					break;
				case java.sql.Types.BINARY:
					row_v.add(i - 1, rows.getByte(i));
					break;
				case java.sql.Types.LONGVARBINARY:
				case java.sql.Types.VARBINARY:
					row_v.add(i - 1, rows.getBytes(i));
					break;
				case java.sql.Types.DATALINK:
				case java.sql.Types.DISTINCT:
				case java.sql.Types.JAVA_OBJECT:
				case java.sql.Types.NULL:
				case java.sql.Types.OTHER:
				case java.sql.Types.STRUCT:
					row_v.add(i - 1, rows.getObject(i));
					break;
				default:
					row_v.add(i - 1, rows.getString(i));
				}
			}
			_rt.addFillRow(row_v);
			modelToRow.put(new Integer(counter), new Integer(counter
					+ fromIndex));
			counter++;
		}
		_rt.revalidate();
		_rt.repaint();
	}

	private void updateColumn(int rowIndex, int colIndex) throws SQLException {
		rows.absolute(f_r);
		int i = colIndex;
		Object o = _rt.getModel().getValueAt(rowIndex, colIndex);
		if (rowset.updateCell(i, o))
			_rt.getModel().setValueAt(null, rowIndex, i);
		rows.updateRow();
		rows.refreshRow();
	}

	private void createTH() throws SQLException {
		ResultSetMetaData rsmd = rows.getMetaData();
		numberOfColumns = rsmd.getColumnCount();
		col_name = new String[numberOfColumns];
		col_type = new int[numberOfColumns];

		for (int i = 1; i < numberOfColumns + 1; i++) {
			col_name[i - 1] = rsmd.getColumnName(i);
			col_type[i - 1] = rsmd.getColumnType(i);
		}
		_rt = new ReportTable(col_name, isEdit, true);
		if (isEdit) {
			MouseListener[] listen = ((TableRowHeader) _rt.getRowHeader())
					.getMouseListeners();
			for (int i = 0; i < listen.length; i++) {
				if (listen[i] instanceof TableRowHeader.PopupListener)
					((TableRowHeader) _rt.getRowHeader())
							.removeMouseListener(listen[i]);
			}
			((TableRowHeader) _rt.getRowHeader())
					.addMouseListener(new PopupListener());
			_rt.table.addPropertyChangeListener("tableCellEditor",
					new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							TableCellEditor newEditor = (TableCellEditor) evt
									.getNewValue();
							if (newEditor == null) {
								try {
									updateColumn(modelR, c_e);
								} catch (SQLException sql_e) {
									ConsoleFrame.addText("\n "
											+ sql_e.getMessage());
									if (prev_o != null)
										ConsoleFrame
												.addText("\n Resetting Previous Value:"
														+ prev_o.toString());
									else
										ConsoleFrame
												.addText("\n Resetting Previous Value:null");
									_rt.getModel().setValueAt(prev_o, modelR,
											c_e);
									prev_o = null;
									f_r = -1;
									c_e = -1;
									modelR = -1;
									try {
										rows.cancelRowUpdates();
										rows.refreshRow();
									} catch (SQLException sql_e1) {
										ConsoleFrame.addText("\n "
												+ sql_e1.getMessage());
										ConsoleFrame
												.addText("\n WARNING: Could not Reset Value");
									}
								}
							} else {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										c_e = _rt.table
												.convertColumnIndexToModel(_rt.table
														.getEditingColumn());
										int viewR = _rt.table.getEditingRow();
										modelR = _rt.modelIndex(viewR);
										prev_o = _rt.getModel().getValueAt(
												modelR, c_e);
										f_r = modelToRow.get(modelR);
										if (prev_o != null)
											ConsoleFrame
													.addText("\n Getting Current Editing Value:"
															+ prev_o.toString());
										else
											ConsoleFrame
													.addText("\n Getting Current Editing Value:null");
									}
								});

							}
						}
					});
		} // If editable

	}

	private int pageCount() {
		if (rowC == 0)
			return 1;
		if (rowC % rowPPage == 0)
			return rowC / rowPPage;
		else
			return (rowC / rowPPage) + 1;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		try {
			if (_rt.table.isEditing()) {
				JOptionPane.showMessageDialog(null, "Table is Being Edited",
						"ErrorMessage", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (_rt.isSorting())
				_rt.cancelSorting();

			if (command.equals("update")) {
				int toIndex = ((Number) ft.getValue()).intValue();
				if (toIndex <= 0) {
					ft.setValue(new Integer(rowPPage));
					return;
				}
				if (toIndex > rowC) {
					if (rowPPage == rowC) {
						ft.setValue(new Integer(rowPPage));
						return;
					}
					rowPPage = rowC;
					ft.setValue(new Integer(rowPPage));
					ConsoleFrame
							.addText("\n Information: Display Row Count is more than Total Table Count");
					ConsoleFrame
							.addText("\n Information: ReSetting to Total Row Count");
					JOptionPane.showMessageDialog(null,
							"Display Row Count is more than Total Row Count",
							"Error Message", JOptionPane.INFORMATION_MESSAGE);
				} else
					rowPPage = toIndex;

				totalP = pageCount();
				l_e.setText(" of " + totalP);
				if (totalP > 1)
					ft_p.setEditable(true);
				else
					ft_p.setEditable(false);
				currentP = 1;
				ft_p.setValue(new Integer(currentP));

				populateTable(1, rowPPage);
				revalidate();
				repaint();
				return;
			}
			if (command.equals("goto")) {
				int fromPage = ((Number) ft_p.getValue()).intValue();
				if (fromPage <= 0 || fromPage > totalP) {
					ft_p.setValue(new Integer(currentP));
					return;
				}
				currentP = fromPage;
				ft.setValue(new Integer(rowPPage));
				int startIndex = (currentP - 1) * rowPPage;
				int endIndex = currentP * rowPPage;
				if (endIndex > rowC)
					endIndex = rowC;

				populateTable(startIndex + 1, endIndex);
				revalidate();
				repaint();
				return;
			}
			if (command.equals("newrow")) {
				jd = new JDialog();
				jd.setModal(true);
				jd.setLocation(200, 150);
				jd.setTitle("Add Rows Dialog");
				_newRT = getAddTablePanel();

				JPanel jp = new JPanel(new BorderLayout());
				jp.add(_newRT, BorderLayout.CENTER);
				JPanel bp = new JPanel();
				JButton ar = new JButton("Add");
				ar.addKeyListener(new KeyBoardListener());
				ar.setActionCommand("addrow");
				ar.addActionListener(this);
				bp.add(ar);
				JButton ca = new JButton("Cancel");
				ca.addKeyListener(new KeyBoardListener());
				ca.setActionCommand("cancel");
				ca.addActionListener(this);
				bp.add(ca);
				jp.add(bp, BorderLayout.PAGE_END);
				jd.getContentPane().add(jp);
				jd.pack();
				jd.setVisible(true);
				return;
			}
			if (command.equals("cancel")) {
				jd.dispose();
				return;
			}
			if (command.equals("addrow")) {
				if (_newRT.table.isEditing()) {
					JOptionPane.showMessageDialog(null,
							"Table is Being Edited", "Error Message",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				int count = _newRT.table.getRowCount();
				int fcount = 0;
				rows.first();
				for (int i = 0; i < count; i++) {
					try {
						rows.moveToInsertRow();
						for (int j = 0; j < _newRT.table.getColumnCount(); j++)
							rowset.updateCell(j,
									_newRT.getModel().getValueAt(i, j));
						rows.insertRow();
						rows.moveToCurrentRow();
						rows.refreshRow();
					} catch (SQLException sql_e) {
						ConsoleFrame.addText("\n Row Id:" + (i + 1) + " Error-"
								+ sql_e.getMessage());
						fcount++;
						continue;
					}

				}
				JOptionPane.showMessageDialog(null, (count - fcount)
						+ " of Total " + count + " Rows Inserted Successfully",
						"Information Message", JOptionPane.INFORMATION_MESSAGE);
				ConsoleFrame.addText("\n " + (count - fcount) + " of Total "
						+ count + " Rows Inserted Successfully");

				jd.dispose();
				if (rows.last() == true)
					rowC = rows.getRow();
				jb.doClick();
			}

		} catch (SQLException sql_e) {
			JOptionPane.showMessageDialog(null, sql_e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
			ConsoleFrame.addText("\n Error:" + sql_e.getMessage());
		}
	}

	private class PopupListener extends MouseAdapter {
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

				JMenuItem menuItem1 = new JMenuItem("Delete");
				menuItem1.setActionCommand("delete");
				menuItem1.addActionListener(popAc);
				popup.add(menuItem1);
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
			if (_rt.table.isEditing()) {
				JOptionPane.showMessageDialog(null, "Table is Being Edited",
						"Error Message", JOptionPane.ERROR_MESSAGE);
				return;
			}
			int[] rowA = _rt.table.getSelectedRows();
			int sc = rowA.length;
			int[] modelA = new int[sc];
			for (int i = 0; i < sc; i++)
				modelA[i] = _rt.modelIndex(rowA[i]);
			Arrays.sort(modelA);

			String command = e.getActionCommand();

			if ("delete".equals(command)) {
				int n = JOptionPane.showConfirmDialog(null,
						"Do you want Delete " + sc + " Rows ?",
						"Delete Option", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION)
					return;
				int delRow = 0; // Rows deleted Successfully

				for (int i = sc - 1; i >= 0; i--) {
					try {
						int index = modelToRow.get(modelA[i]);
						rows.absolute(index);
						rows.deleteRow();
						delRow++;
						_rt.getModel().removeRow(modelA[i]);
						modelToRow.remove(modelA[i]);
					} catch (Exception sqle) {
						JOptionPane
								.showMessageDialog(
										null,
										"Couldn't Delete all selected Rows.\n Check Console for more Information",
										"Delete Information",
										JOptionPane.ERROR_MESSAGE);
						ConsoleFrame.addText("\n " + delRow + " of Total " + sc
								+ " Rows Deleted.");
						ConsoleFrame.addText("\n WARNING MESSAGE:"
								+ sqle.getMessage());
						ConsoleFrame.addText("\n WARNING: Row Id:" + (i + 1)
								+ " can not be deleted");
						ConsoleFrame
								.addText("\n WARNING: Coming out of Delete function");
						return;
					}
				}
				JOptionPane.showMessageDialog(null, sc
						+ " Rows Deleted Successfully", "Information Message",
						JOptionPane.INFORMATION_MESSAGE);
				ConsoleFrame.addText("\n " + sc + " Rows Deleted Successfully");
				try {
					if (rows.last() == true)
						rowC = rows.getRow();
					jb.doClick();
				} catch (Exception sqle) {
					ConsoleFrame.addText("\n Table Could not be refreshed");
				}
				return;
			}
		}
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

	public ReportTable getAddTablePanel() {
		ReportTable newRT = new ReportTable(col_name, col_type, true, true);
		newRT.addRows(0, 1);
		return newRT;
	}

}
