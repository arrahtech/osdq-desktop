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

/* This file is used for calling flows 
 * from Quality menuItems. 
 *
 */

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.arrah.framework.dataquality.QualityCheck;
import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.ndtable.ResultsetToRTM;
import org.arrah.framework.rdbms.JDBCRowset;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_NewConn;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.util.KeyValueParser;
import org.arrah.framework.util.StringCaseFormatUtil;


public class QualityListener implements ActionListener {
	private JDialog d_f;
	private SelTablePane selTP;
	private int sel_mode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
	private int menuSel = -1;
	private JFrame frame;
	private JDBCRowset rows;
	private JDBCRowsetPanel rowPanel;
	private Vector<Integer> mrowI;
	private ReportTable rt;
	private int matchI = -1;

	public QualityListener() {
	}; // Constructor

	private void createDialog() {

		JPanel bp = new JPanel();
		JButton next = new JButton("Next");
		next.setActionCommand("next");
		next.addActionListener(this);
		next.addKeyListener(new KeyBoardListener());
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(next);
		bp.add(cancel);

		JPanel mp = new JPanel(new BorderLayout());
		selTP = new SelTablePane(sel_mode);
		mp.add(selTP, BorderLayout.CENTER);
		mp.add(bp, BorderLayout.PAGE_END);
		// Take Input table, columns here
		d_f = new JDialog();
		d_f.setModal(true);
		d_f.setTitle("Select Table and Columns");
		d_f.setLocation(250, 50);
		d_f.getContentPane().add(mp);
		d_f.pack();
		d_f.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JMenuItem) {
			String source = ((JMenuItem) (e.getSource())).getText();

			if (source.equals("Duplicate")) {
				menuSel = 1;
				createDialog();
				return;
			}
			if (source.equals("Standardisation Regex")) {
				menuSel = 2;
				createDialog();
				return;
			}
			if (source.equals("AND (Inclusive)")) {
				menuSel = 3;
				createDialog();
				return;
			}
			if (source.equals("OR (Exclusive)")) {
				menuSel = 4;
				createDialog();
				return;
			}
			if (source.equals("Match")) {
				menuSel = 5;
				createDialog();
				return;
			}
			if (source.equals("No Match")) {
				menuSel = 6;
				createDialog();
				return;
			}
			if (source.equals("Cardinality")) {
				menuSel = 7;
				new CompareTablePanel();
				return;
			}
			if (source.equals("DeDup-Delete")) {
				menuSel = 8;
				createDialog();
				return;
			}
			if (source.equals("UPPER CASE")) {
				menuSel = 9;
				createDialog();
				return;
			}
			if (source.equals("lower case")) {
				menuSel = 10;
				createDialog();
				return;
			}
			if (source.equals("Title Case")) {
				menuSel = 11;
				createDialog();
				return;
			}
			if (source.equals("Sentence case")) {
				menuSel = 12;
				createDialog();
				return;
			}
			if (source.equals("Replace Null")) {
				menuSel = 13;
				createDialog();
				return;
			}
			if (source.equals("Discreet Match")) {
				menuSel = 14;
				createDialog();
				return;
			}
			if (source.equals("Discreet No Match")) {
				menuSel = 15;
				createDialog();
				return;
			}
			if (source.equals("Cardinality Editable")) {
				boolean emode = false;
				int n = JOptionPane
						.showConfirmDialog(
								null,
								"Edit Mode will update underlying Database \n Do you want Edit Mode ?",
								"Edit Option", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION)
					emode = true;
				menuSel = 16;
				new CompareTablePanel(emode);
				return;
			}
			if (source.equals("Table Comparison")) {
				menuSel = 17;
				createDialog();
				return;
			}
			if (source.equals("DB Comparison")) {
				menuSel = 18;
				CompareDBInfoDialog newDialog = new CompareDBInfoDialog ();
				if (newDialog.showDialog == true)
					newDialog.createMapDialog();
				return;
			}
			if (source.equals("Schema Comparison")) {
				menuSel = 19;
				CompareSchemaDialog newDialog = new CompareSchemaDialog ();
					newDialog.createGUI();
				return;
			}
			if (source.equals("Cross Column Search")) { // like fuzzy
				menuSel = 20;
				createDialog();
				return;
			}
			if (source.equals("Box Plot")) { // BoX Plot
				menuSel = 21;
				createDialog();
				return;
			}
			if (source.equals("K Mean Cluster")) { // K Mean Cluster
				menuSel = 22;
				createDialog();
				return;
			}
			if (source.equals("DeDup-Replace")) {
				menuSel = 23;
				createDialog();
				return;
			}
			if (source.equals("Standardisation Fuzzy")) {
				menuSel = 24;
				createDialog();
				return;
			}
		}// End of Menu Item
		else {
			String command = e.getActionCommand();
			if (command.equals("cancel")) {
				d_f.dispose();
				return;
			}
			if (command.equals("cancel_frame")) {
				rows.close();
				frame.dispose();
				return;
			}
			if (command.equals("commit")) {
				if (rt.isSorting() || rt.table.isEditing()) {
					JOptionPane.showMessageDialog(null,
							"Table is either in Sorting or in Editing state",
							"Information Message",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				int n = JOptionPane
						.showConfirmDialog(
								null,
								"Database will be updated.\n Do you want to continue ?",
								"Confirm Dialog", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION)
					return;
				if (mrowI == null) {
					ConsoleFrame.addText("\n Nothing to Commit");
					return;
				}
				for (int i = 0; i < mrowI.size(); i++) {
					try {
						rows.updateCellVal(mrowI.get(i), matchI, rt.getModel()
								.getValueAt(i, 0));
					} catch (Exception exp) {
						ConsoleFrame.addText("\n Error: Update Cell Error:"
								+ exp.getMessage());
					}
				}
				rows.close();
				frame.dispose();
				return;
			}
			if (command.equals("next")) {
				Vector<String> vc = selTP.getColumns();

				if (vc.isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"Select Columns for Operation",
							"No Selection Error Dialog",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				d_f.setCursor(java.awt.Cursor
						.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

				switch (menuSel) {
				case 1:
					if (dupAction(selTP.getTable(), vc))
						d_f.dispose();
					break;
				case 2:
				{
					SearchOptionDialog sd = new SearchOptionDialog();
					File f = sd.getFile();
					try {
						if (f == null) {
							d_f.setCursor(java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
							return;
						}
						ConsoleFrame.addText("\n Selected File:" + f.toString());
						Hashtable<String, String> filterHash = KeyValueParser.parseFile(f
								.toString());
						String options = sd.getSelectedOption();
						searchAction(selTP.getTable(), vc, filterHash,options); // Pass options

					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					} finally  {
						d_f.setCursor(java.awt.Cursor
								.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
						
					}
					d_f.dispose();
					break;
				}
				case 3:
					incAction(selTP.getTable(), vc, true);
					d_f.dispose();
					break;
				case 4:
					incAction(selTP.getTable(), vc, false);
					d_f.dispose();
					break;
				case 5:
					try {
						matchAction(selTP.getTable(), vc, true);
					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					}
					d_f.dispose();
					break;
				case 6:
					try {
						matchAction(selTP.getTable(), vc, false);
					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					}
					d_f.dispose();
					break;
				case 8: // Delete
					try {
						similarAction(selTP.getTable(), vc,true);
					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					}
					break;
				case 9:
				case 10:
				case 11:
				case 12:
					try {
						caseFormatAction(selTP.getTable(), vc, menuSel - 8); // to
																				// start
																				// with
																				// type
																				// 1
					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					}
					d_f.dispose();
					break;
				case 13:
					String input = JOptionPane
							.showInputDialog("Replace Null with: \n For Date Object Format is dd-MM-yyyy");
					if (input == null || "".equals(input))
						return;
					try {
						repalceNullAction(selTP.getTable(), vc, input);
					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					}
					d_f.dispose();
					break;
				case 14:
				case 15:
					DiscreetInputGUI dig = new DiscreetInputGUI();
					dig.createDialog();
					String dtext = dig.getRawText();
					String ddelim = dig.getDelimiter();
					Vector<String> token = StringCaseFormatUtil.tokenizeText(dtext,ddelim);
					if (token == null || token.size()  == 0 ) {
						ConsoleFrame.addText("\n No Token Processed");
						return;
					}
					try {
						if (menuSel == 14)
							disceetSearchAction(selTP.getTable(), vc, token,
									true);
						else
							disceetSearchAction(selTP.getTable(), vc, token,
									false);
					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					}
					break;
				case 17:
					d_f.dispose();
					TestConnectionDialog tcd = new TestConnectionDialog(1); // new Connection
					JOptionPane.showMessageDialog(null, "Choose Table to Compare from another Data Source",
							"Table Comparison Dialog",
							JOptionPane.INFORMATION_MESSAGE);
					tcd.createGUI();
					Hashtable <String,String> _fileParse = tcd.getDBParam();
					if (_fileParse == null ) return;
					
					try {
						Rdbms_NewConn newConn = new Rdbms_NewConn(_fileParse);
						if ( newConn.openConn() == false) return;
						newConn.populateTable();
						Vector<String> table_v = newConn.getTable();
						String tableName = table_v.get(0);
						Vector avector[] = newConn.populateColumn(tableName,null);
						newConn.closeConn();
						CompareTableInfoDialog newDialog = new CompareTableInfoDialog (selTP.getTable(), vc,
								table_v,avector[0],_fileParse );
						newDialog.createMapDialog();
						
					} catch (Exception e1) {
						System.out.println(e1.getMessage());
						e1.printStackTrace();
					} finally {
						
					}
					
					break;
				case 20:
					try {
						crossColumnAction(selTP.getTable(), vc);
					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					}
					break;
				case 21:
					boxPlotAction(selTP.getTable(), vc, true);
					d_f.dispose();
					break;
				case 22:
					kMeanAction(selTP.getTable(), vc, true);
					d_f.dispose();
					break;
					
				case 23:
					try {
						similarAction(selTP.getTable(), vc,false);
					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					}
					break;
				case 24: // Standardizaton fuzzy
					SearchOptionDialog sd = new SearchOptionDialog(true); // disable options
					File f = sd.getFile();
					try {
						if (f == null) {
							d_f.setCursor(java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
							return;
						}
						ConsoleFrame.addText("\n Selected File:" + f.toString());
						Hashtable<String, String> filterHash = KeyValueParser.parseFile(f
								.toString());
						searchAction(selTP.getTable(), vc, filterHash,null); // No options

					} catch (SQLException sqle) {
						JOptionPane.showMessageDialog(null, sqle.getMessage(),
								"SQL Exception Dialog",
								JOptionPane.ERROR_MESSAGE);
					} finally  {
						d_f.setCursor(java.awt.Cursor
								.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
						
					}
					d_f.dispose();
					break;
					
				default:
					d_f.dispose();
				}
				return;
			}
		} // End of Else
	}// End of Action Performed

	/* Duplicate Quality Check */
	private boolean dupAction(String table, Vector<?> col) {
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_table_duprow_query(col, selTP.getQueryString());

		JFrame frame = new JFrame("Duplicate Row Show Frame");
		DuplicatePane dp = new DuplicatePane(query, selTP.getQueryString(),
				table, frame);
		frame.setLocation(250, 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(dp);
		frame.pack();
		bringToFront(frame);
		
		return true;
	}
	/* Create Data for Box Plot */
	private void boxPlotAction(String table, Vector<?> col, boolean isInclusive) {
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_tableAll_query();
		if (!"".equals(selTP.getQueryString()))
			query += " AND (" + selTP.getQueryString() + ")";
		ReportTableModel rtm = null;

		try {
			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(query);
			rtm = ResultsetToRTM.getSQLValue(rs, true);
			rs.close();
			Rdbms_conn.closeConn();
		} catch (SQLException sql_e) {
			JOptionPane.showMessageDialog(null, sql_e.getMessage(),
					"BoxPlot Error Dialog", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		BoxPlotPanel bp = new BoxPlotPanel("Box Plot Number Profiling", "Columns", "Value");
		try {
			if (col.size() > 1)
				bp.addRTMDataSet(rtm, col.get(0).toString(), col.get(1).toString());
			else
				bp.addRTMDataSet(rtm, col.get(0).toString(), "");
			bp.drawBoxPlot();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Exception:"+e.getMessage());
			return;
		}
		
		frame = new JFrame("Box Plot Frame");
		frame.setLocation(250, 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(bp);
		frame.pack();
		bringToFront(frame);
	}
	/* Create Data for KMean Plot */
	private void kMeanAction(String table, Vector<?> col, boolean isInclusive) {
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_tableAll_query();
		if (!"".equals(selTP.getQueryString()))
			query += " AND (" + selTP.getQueryString() + ")";
		ReportTableModel rtm = null;

		try {
			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(query);
			rtm = ResultsetToRTM.getSQLValue(rs, true);
			rs.close();
			Rdbms_conn.closeConn();
		} catch (SQLException sql_e) {
			JOptionPane.showMessageDialog(null, sql_e.getMessage(),
					"K Mean Error Dialog", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		KMeanPanel kp = new KMeanPanel("K Mean Cluster", "Columns", "Value");
		try {
			kp.addRTMDataSet(rtm, col);
			kp.drawKMeanPlot(5,col); // hard coded to 5
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Exception:"+e.getMessage());
			return;
		}
		
		frame = new JFrame("K Mean Frame");
		frame.setLocation(250, 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(kp);
		frame.pack();
		bringToFront(frame);
	}


	/* Checks Null and empty string - Completeness Analysis */
	private void incAction(String table, Vector<?> col, boolean isInclusive) {
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_inclusive_query(col, isInclusive);
		if (!"".equals(selTP.getQueryString()))
			query += " AND (" + selTP.getQueryString() + ")";

		int n = JOptionPane.showConfirmDialog(null,
				"Records will be Updated.\n Do you want to continue ?",
				"Confirm Dialog", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.NO_OPTION)
			return;

		if (rows != null)
			rows.close();
		try {
			rowPanel = new JDBCRowsetPanel(query, true, col.get(0).toString());
			rows = rowPanel.rowset;
		} catch (SQLException sql_e) {
			JOptionPane.showMessageDialog(null, sql_e.getMessage(),
					"Completeness Error Dialog", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JPanel jp = new JPanel(new BorderLayout());

		JPanel tp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));

		JLabel n_l = new JLabel(
				"<html><body><a href=\"\">Close</A><body></html>");
		n_l.addMouseListener(new LinkMouseListener());
		tp.add(n_l);
		jp.add(tp, BorderLayout.PAGE_START);
		jp.add(rowPanel, BorderLayout.CENTER);

		if (isInclusive == true)
			frame = new JFrame("Completeness Inclusive Frame");
		else
			frame = new JFrame("Completeness Exclusive Frame");
		frame.setLocation(250, 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(jp);
		frame.pack();
		bringToFront(frame);
	}

	/* Search and Replaces the value from key-val pair  - Standardization */
	private void searchAction(String table, Vector<?> col,
			Hashtable<String, String> filter, String options) throws SQLException {
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_tableAll_query();
		if (!"".equals(selTP.getQueryString()))
			query += " WHERE " + selTP.getQueryString();

		rows = new JDBCRowset(query, -1, false);

		QualityCheck qc = new QualityCheck();
		if (options == null)
			rt = new ReportTable(qc.searchReplaceFuzzy(rows, col.get(0).toString(),filter));
		else
			rt = new ReportTable(qc.searchReplace(rows, col.get(0).toString(),filter,options));
		
		mrowI = qc.getrowIndex();
		matchI = qc.getColMatchIndex();

		rt.table.moveColumn(matchI + 1, 1);

		JPanel sP = new JPanel(new BorderLayout());
		sP.add(rt, BorderLayout.CENTER);

		JPanel bP = new JPanel();
		JButton commit = new JButton("Commit All");
		commit.setActionCommand("commit");
		commit.addActionListener(this);
		commit.addKeyListener(new KeyBoardListener());
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel_frame");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bP.add(commit);
		bP.add(cancel);
		sP.add(bP, BorderLayout.PAGE_END);

		frame = new JFrame("Standardisation Frame");
		frame.setLocation(250, 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(sP);
		frame.pack();
		bringToFront(frame);

	}

	/* Replaces Null and empty string */
	private void repalceNullAction(String table, Vector<?> col, String replaceWith)
			throws SQLException {
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_tableAll_query();
		if (!"".equals(selTP.getQueryString()))
			query += " WHERE " + selTP.getQueryString();

		rows = new JDBCRowset(query, -1, false);
		QualityCheck qc = new QualityCheck();
		rt = new ReportTable(qc.nullReplace(rows, col.get(0).toString(),
				replaceWith));
		mrowI = qc.getrowIndex();
		matchI = qc.getColMatchIndex();

		rt.table.moveColumn(matchI + 1, 1);

		JPanel sP = new JPanel(new BorderLayout());
		sP.add(rt, BorderLayout.CENTER);

		JPanel bP = new JPanel();
		JButton commit = new JButton("Commit All");
		commit.setActionCommand("commit");
		commit.addActionListener(this);
		commit.addKeyListener(new KeyBoardListener());
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel_frame");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bP.add(commit);
		bP.add(cancel);
		sP.add(bP, BorderLayout.PAGE_END);

		frame = new JFrame("Replace Null");
		frame.setLocation(250, 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(sP);
		frame.pack();
		bringToFront(frame);
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
						&& s1.equals("<html><body><a href=\"\">Close</A><body></html>")) {
					rows.close();
					frame.dispose();
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

	/*  Format Matching quality */
	private void matchAction(String table, Vector<?> col, boolean isMatch)
			throws SQLException {

		FormatPatternPanel fp = new FormatPatternPanel(col.get(0).toString());
		int response = fp.createDialog();
		if (response == 0) // cancel is clicked
			return;
		String type = fp.getType();
		Object[] pattern = fp.inputPatterns();
		if (pattern.length == 0)
			return; // Nothing to parse

		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_tableAll_query();
		if (!"".equals(selTP.getQueryString()))
			query += " WHERE " + selTP.getQueryString();

		rows = new JDBCRowset(query, -1, false);
		QualityCheck qc = new QualityCheck();
		rt = new ReportTable(qc.patternMatch(rows, col.get(0).toString(), type,
				pattern, isMatch));
		mrowI = qc.getrowIndex();
		matchI = qc.getColMatchIndex();

		rt.table.moveColumn(matchI + 1, 1);

		JPanel sP = new JPanel(new BorderLayout());
		sP.add(rt, BorderLayout.CENTER);

		JPanel bP = new JPanel();
		JButton commit = new JButton("Commit All");
		commit.setActionCommand("commit");
		commit.addActionListener(this);
		commit.addKeyListener(new KeyBoardListener());
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel_frame");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bP.add(commit);
		bP.add(cancel);
		sP.add(bP, BorderLayout.PAGE_END);

		frame = new JFrame("Format Match Frame");
		frame.setLocation(250, 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(sP);
		frame.pack();
		bringToFront(frame);

	}

	/* Fuzzy Search */
	private void similarAction(String table, Vector<?> col, boolean isDelete) throws SQLException {
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_selCol_query(col.toArray(),
				selTP.getQueryString());
		rows = new JDBCRowset(query, -1, false);
		d_f.dispose(); // now dispose the dialog
		 new SimilarityCheckPanel(rows,query,isDelete);
	}
	
	/* Cross Column Search */
	private void crossColumnAction(String table, Vector<?> col) throws SQLException {
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_selCol_query(col.toArray(),
				selTP.getQueryString());
		rows = new JDBCRowset(query, -1, false);
		d_f.dispose(); // now dispose the dialog
		 new CrossColumnPanel(rows,query);
	}

	/* String case formating quality check */
	private void caseFormatAction(String table, Vector<?> col, int formatType)
			throws SQLException {

		char defChar = '.';

		if (formatType == 4) { // Sentence case
			Locale defLoc = Locale.getDefault();
			ConsoleFrame.addText("\n Default Locale is :" + defLoc);
			if (defLoc.equals(Locale.US) || defLoc.equals(Locale.UK)
					|| defLoc.equals(Locale.CANADA)) {
				// Do nothing
			} else {
				String response = JOptionPane
						.showInputDialog(null,
								"Please enter the end of Line Character ?",
								"Language End Line Input",
								JOptionPane.QUESTION_MESSAGE);
				if (response == null || "".equals(response)) {
					// Do nothing
				} else
					defChar = response.charAt(0);
			}
		}

		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query ="";
		
		// Oracle RowsetImpl needs Column name
		if(Rdbms_conn.getDBType().compareToIgnoreCase("oracle_native") == 0
                || (Rdbms_conn.getDBType().compareToIgnoreCase("oracle_odbc") == 0))               
				query = qb.get_tb_value(false);
		else
			query = qb.get_tableAll_query();
		
		if (!"".equals(selTP.getQueryString()))
			query += " WHERE " + selTP.getQueryString();

		rows = new JDBCRowset(query, -1, false);
		QualityCheck qc = new QualityCheck();
		rt = new ReportTable(qc.caseFormat(rows, col.get(0).toString(),
				formatType, defChar));
		mrowI = qc.getrowIndex();
		matchI = qc.getColMatchIndex();

		rt.table.moveColumn(matchI + 1, 1);

		JPanel sP = new JPanel(new BorderLayout());
		sP.add(rt, BorderLayout.CENTER);

		JPanel bP = new JPanel();
		JButton commit = new JButton("Commit All");
		commit.setActionCommand("commit");
		commit.addActionListener(this);
		commit.addKeyListener(new KeyBoardListener());
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel_frame");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bP.add(commit);
		bP.add(cancel);
		sP.add(bP, BorderLayout.PAGE_END);

		frame = new JFrame("Case Format Frame");
		frame.setLocation(250, 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(sP);
		frame.pack();
		bringToFront(frame);

	}

	/* Discreet Search quality */
	private void disceetSearchAction(String table, Vector<?> col,
			Vector<String> token, boolean match) throws SQLException {
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), table,
				Rdbms_conn.getDBType());
		String query = qb.get_tableAll_query();
		if (!"".equals(selTP.getQueryString()))
			query += " WHERE " + selTP.getQueryString();

		rows = new JDBCRowset(query, -1, false);
		QualityCheck qc = new QualityCheck();
		rt = new ReportTable(qc.discreetSearch(rows, col.get(0).toString(),
				token, match));
		mrowI = qc.getrowIndex();
		matchI = qc.getColMatchIndex();

		rt.table.moveColumn(matchI + 1, 1);

		JPanel sP = new JPanel(new BorderLayout());
		sP.add(rt, BorderLayout.CENTER);

		JPanel bP = new JPanel();
		JButton commit = new JButton("Commit All");
		commit.setActionCommand("commit");
		commit.addActionListener(this);
		commit.addKeyListener(new KeyBoardListener());
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel_frame");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bP.add(commit);
		bP.add(cancel);
		sP.add(bP, BorderLayout.PAGE_END);

		// Now dispose old dialog and create new frame
		d_f.dispose();
		
		frame = new JFrame("Discreet Search Frame");
		frame.setLocation(250, 50);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(sP);
		frame.pack();
		frame.setVisible(true);
	}
	
	static public void bringToFront( final JFrame frame) {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		      frame.setVisible(true);
		      frame.toFront();
		      frame.repaint();
		      
		    }
		  });

	}

}
