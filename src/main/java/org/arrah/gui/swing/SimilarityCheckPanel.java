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
 * This file is used to Similarity Check
 * for Lucene Index directory or RAM
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;

import org.arrah.framework.dataquality.SimilarityCheckLucene;
import org.arrah.framework.rdbms.JDBCRowset;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class SimilarityCheckPanel implements ActionListener, TableModelListener, ItemListener {
	private ReportTable _rt, outputRT;
	private String[] colName;
	private String[] colType;
	private int rowC;
	private Vector<Integer> skipVC, markDel;
	private JDialog d_m;
	private JFrame dg;
	private JCheckBox chk;
	private JComboBox<String>[] sType;
	private JFormattedTextField[] sImp,sFuzzy,sCharLen;
	private JTextField[] skiptf;
	private boolean isChanged = false, isDelete = true;
	private Hashtable<Integer, Integer> parentMap; // it will hold original position
	boolean isRowSet = false;
	private JDBCRowset _rows;
	private SimilarityCheckLucene _simcheck;
	private String queryForRowset="";

	// For ReportTable Input
	public SimilarityCheckPanel(ReportTable rt, boolean isdelete) {
		isDelete = isdelete; // check if delete or replace
		_rt = rt;
		_simcheck = new SimilarityCheckLucene(_rt.getRTMModel());
		colName = getColName();
		rowC = _rt.table.getRowCount();
		_rt.getModel().addTableModelListener(this);
		mapDialog();
	}
	
	/*** For Rowset Table Input for rdbms
	// Not in use for now
	public SimilarityCheckPanel(JDBCRowset rowSet) {
		isRowSet = true;
		_rows = rowSet;
		_simcheck = new SimilarityCheckLucene(_rows);
		colName = _rows.getColName();
		colType = _rows.getColType();
		rowC = _rows.getRowCount();
		mapDialog();
	} ****/
	
	// For Rowset Table Input for Hive
	// Hive rowset does not move forward and backward so create new rowset
	public SimilarityCheckPanel(JDBCRowset rowSet, String query, boolean isdelete) {
		isDelete = isdelete; // check if delete or replace
		queryForRowset = query;
		isRowSet = true;
		_rows = rowSet;
		_simcheck = new SimilarityCheckLucene(_rows);
		colName = _rows.getColName();
		colType = _rows.getColType();
		rowC = _rows.getRowCount();
		mapDialog();
	}
	
	// For independent table and selected Columns
	public SimilarityCheckPanel(String searchString, String tabName, Vector<String> colV) {
		
		_rt = createRT(tabName,colV);
		_simcheck = new SimilarityCheckLucene(_rt.getRTMModel());
		colName = getColName();
		rowC = _rt.table.getRowCount();
		_rt.getModel().addTableModelListener(this);
		_simcheck.makeIndex();
		searchTableIndex(searchString,-1); // search in colV
	}
	
	// For RTM  table and single Column Search
	public SimilarityCheckPanel(String searchString, ReportTable reportTable, int colIndex) {
		
		_rt = reportTable;
		_simcheck = new SimilarityCheckLucene(_rt.getRTMModel());
		colName = getColName();
		rowC = _rt.table.getRowCount();
		_rt.getModel().addTableModelListener(this);
		_simcheck.makeIndex();
		searchTableIndex(searchString,colIndex);
	}

	private void createNewRowset(String query) throws SQLException {
		
		isRowSet = true;
		_rows = new JDBCRowset(query, -1, false); 
		_simcheck.setRowset(_rows); // new class will have new index
		colName = _rows.getColName();
		colType = _rows.getColType();
		rowC = _rows.getRowCount();
	}
	
	private String[] getColName() {
		int colC = _rt.table.getColumnCount();
		colName = new String[colC];
		colType = new String[colC];

		for (int i = 0; i < colC; i++) {
			colName[i] = _rt.table.getColumnName(i);
			colType[i] = _rt.table.getColumnClass(i).getName();
		}
		return colName;
	}

	/* For Hive a new rowset need to be created 
	 * because it does not scroll forward and backward
	 */
	private void searchTableIndex() {
		if (_simcheck.openIndex() == false)
			return;
		// Add a boolean column for delete
		String[] newColN = new String[colName.length + 3];
		newColN[0] = "Delete Editable"; // CheckBox selection
		newColN[1] = "Group ID"; // Group ID 
		newColN[2] = "Row Number"; // Row number
		
		for (int i = 0; i < colName.length; i++)
			newColN[i + 3] = colName[i];

		if (isDelete == true)
			outputRT = new ReportTable(newColN, false, true);
		else
			outputRT = new ReportTable(newColN, true, false); // Editable for replace with class awareness
		
		skipVC = new Vector<Integer>();
		parentMap = new Hashtable<Integer, Integer>();
		
		if (isRowSet == true) { // Only if this is rowset
			if (Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") == 0 ) {
			 // for Hive
					if (_rows != null) _rows.close();
					try {
						createNewRowset(queryForRowset);
					} catch (SQLException e) {
						System.out.println("New Rowset Exception:"+e.getLocalizedMessage());
						return;
					}
			}
		}

		// Iterate over row
		int grpid = 1; // groupID for matching groups
		for (int i = 0; i < rowC; i++) {
			if (isRowSet == false && skipVC.contains(i) == true)
				continue;
			if (isRowSet == true && skipVC.contains(i + 1) == true)
				continue;

			String queryString = getQString(i);
			
			if (queryString == null || queryString.equals("") == true)
				continue;
			
			Query qry = _simcheck.parseQuery(queryString);
			Hits hit = _simcheck.searchIndex(qry);
			if (hit == null || hit.length() <= 1)
				continue; // It will match self
			// Iterate over the Documents in the Hits object

			for (int j = 0; j < hit.length(); j++) {
				try {
					Document doc = hit.doc(j);
					String rowid = doc.get("at__rowid__");
					parentMap.put(outputRT.table.getRowCount(),
							Integer.parseInt(rowid));
					Object[] row = null;
					if (isRowSet == false)
						row = _rt.getRow(Integer.parseInt(rowid));
					else {
						if (Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") != 0 ) {
							row = _rows.getRow(Integer.parseInt(rowid)); 
						} else {
							// will not work for Hive as rowset can not move bothways
							// Hive the info should be taken from document itself by colname
							row = new Object[colName.length];
							for (int k =0; k < colName.length; k++)
								row[k] = doc.get(colName[k]);
						}
					}
					
					Object[] newRow = new Object[row.length + 3];
					boolean del = false;
					newRow[0] = del;
					newRow[1] = "Group: "+grpid;
							
					if (isRowSet == false)
						newRow[2] = "Index: "+rowid;
					else
						newRow[2] = "Row: "+rowid;
					
					for (int k = 0; k < row.length; k++)
						newRow[k + 3] = row[k];
					outputRT.addFillRow(newRow);
					skipVC.add(Integer.parseInt(rowid));
				} catch (Exception e) {
					ConsoleFrame.addText("\n " + e.getMessage());
					ConsoleFrame.addText("\n Error: Can not open Document");
				}
			}
			outputRT.addNullRow();
			grpid++;
		}
		_simcheck.closeSeachIndex();
		
		// hide boolean column if replace
		if (isDelete == false)
		outputRT.hideColumn(0);
		
		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(outputRT, BorderLayout.CENTER);
		if (isDelete == true)
			jp_p.add(deletePanel(), BorderLayout.PAGE_END); // delete menu
		else
			jp_p.add(replacePanel(), BorderLayout.PAGE_END); // replacing menu

		// Show the table now
		dg = new JFrame("Similar Records Frame");
		dg.setLocation(250, 100);
		dg.getContentPane().add(jp_p);
		dg.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dg.pack();
		QualityListener.bringToFront(dg);
	}

	private JDialog mapDialog() {

		int colC = colName.length;
		sType = new JComboBox[colC];
		sImp = new JFormattedTextField[colC];
		sFuzzy = new JFormattedTextField[colC];
		JTextField[] tf1 = new JTextField[colC];
		skiptf = new JTextField[colC];
		sCharLen = new JFormattedTextField[colC];

		JPanel jp = new JPanel();
		SpringLayout layout = new SpringLayout();
		jp.setLayout(layout);

		JLabel l1 = new JLabel("Field Name");
		l1.setForeground(Color.BLUE);
		JLabel l2 = new JLabel("Search Criterion");
		l2.setForeground(Color.BLUE);
		JLabel l3 = new JLabel("Boosting Factor");
		l3.setForeground(Color.BLUE);
		JLabel l4 = new JLabel("Skip Words");
		l4.setForeground(Color.BLUE);
		JLabel l5 = new JLabel("Similarity Index");
		l5.setForeground(Color.BLUE);
		JLabel l6 = new JLabel("Char Length");
		l6.setForeground(Color.BLUE);
		jp.add(l1);
		jp.add(l2);
		jp.add(l3);
		jp.add(l5);
		jp.add(l4);
		jp.add(l6);
		

		for (int i = 0; i < colC; i++) {
			tf1[i] = new JTextField(8);
			tf1[i].setText(colName[i]);
			tf1[i].setEditable(false);
			tf1[i].setToolTipText(colType[i]);
			jp.add(tf1[i]);

			sType[i] = new JComboBox<String>(new String[] { "Not Applicable", "Exact Match",
					"Similar-Any Word", "Similar-All Words", "Begin Char Match", "End Char Match" });
			sType[i].addItemListener(this);
			sType[i].setActionCommand(Integer.toString(i));
			jp.add(sType[i]);

			sImp[i] = new JFormattedTextField();
			sImp[i].setEnabled(false);
			jp.add(sImp[i]);
			
			sFuzzy[i] = new JFormattedTextField();
			sFuzzy[i].setEnabled(false);
			jp.add(sFuzzy[i]);

			skiptf[i] = new JTextField(12);
			skiptf[i].setText("And,Or,Not"); // Lucene core words
			skiptf[i].setEnabled(false);
			jp.add(skiptf[i]);
			
			sCharLen[i] = new JFormattedTextField();
			sCharLen[i].setEnabled(false);
			jp.add(sCharLen[i]);

		}
		SpringUtilities.makeCompactGrid(jp, colC + 1, 6, 3, 3, 3, 3); // +1 for
																		// header

		JScrollPane jscrollpane1 = new JScrollPane(jp);
		if (colC * 35 + 50 > 400)
			jscrollpane1.setPreferredSize(new Dimension(625, 400));
		else
			jscrollpane1.setPreferredSize(new Dimension(625, colC * 35 + 50));

		JPanel bp = new JPanel();
		JButton help = new JButton("Help");
		help.setActionCommand("help");
		help.addActionListener(this);
		help.addKeyListener(new KeyBoardListener());
		bp.add(help);
		JButton ok = new JButton("Search");
		ok.setActionCommand("simcheck");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("mcancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(jscrollpane1, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		d_m = new JDialog();
		d_m.setTitle("Similarity Map Dialog");
		d_m.setLocation(150, 150);
		d_m.getContentPane().add(jp_p);
		d_m.setModal(true);
		d_m.pack();
		d_m.setVisible(true);

		return d_m;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("simcheck")) {
			try {
				d_m.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if (validateInput() == false ) return;
				_simcheck.makeIndex();
				searchTableIndex();
			} finally {
				d_m.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				d_m.dispose();
			}
		}
		if (command.equals("mcancel")) {
			if (isRowSet == true && _rows != null)
				_rows.close();
			d_m.dispose();
		}
		if (command.equals("cancel")) {
			if (isRowSet == true && _rows != null)
				_rows.close();
			dg.dispose();
		}
		if (command.equals("help")) {
			JOptionPane.showMessageDialog(null,
					"Boosting Factor  should be positive integer.\n" +
					"New Ranking = Boosting Factor X Old Ranking \n\n"+
					"Similarity Index should be between 0.0 and 1.0 \n " +
					"0.0 - No match                1.0 Exact match \n\n" +
					"Char Length should be positive integer.\n" +
					"It match number of characters from Beging or End \n\n" +
					" Open Similarity_check.[doc][pdf] to get more Infomation");
			return;
		}
		if (command.equals("replaces")) {
			String replace = JOptionPane.showInputDialog("Highlighted Values to replace with:");
			if (replace == null || "".equals(replace))
				return;
			
			int colI = outputRT.table.getSelectedColumn();
			int [] rowI = outputRT.table.getSelectedRows();
			if ( colI < 0 || rowI.length == 0)
				return;
			for (int i=0; i  < rowI.length; i++)
				outputRT.table.setValueAt(replace, rowI[i], colI);
			
			return;
		}
		
		if (command.equals("delete")) {
			if (outputRT.isSorting()) {
				JOptionPane.showMessageDialog(null,
						"Table is in Sorting State.");
				return;
			}
			if (isChanged == true && chk.isSelected() == true) {
				JOptionPane
						.showMessageDialog(null,
								"Parent table has changed.\n Run Similarity Check again to get updated value.");
				ConsoleFrame
						.addText("\n Parent table has changed.\n Run Similarity Check again get updated value.");
				return;
			}
			markDel = new Vector<Integer>();
			int rowC = outputRT.table.getRowCount();
			for (int i = 0; i < rowC; i++)
				if (outputRT.getValueAt(i, 0) != null
						&& ((Boolean) outputRT.getValueAt(i, 0)).booleanValue() == true)
					markDel.add(i);
			int size = markDel.size();
			if (size == 0) {
				JOptionPane.showMessageDialog(null, "Check Rows to Delete");
				return;
			}
			int n = JOptionPane.showConfirmDialog(null,
					"Do you want to delete " + size + " rows?",
					"Confirmation Type", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.NO_OPTION)
				return;

			int[] parentO = new int[size];
			for (int i = 0; i < size; i++) {
				if (chk.isSelected() == true) {
					if (parentMap.get(markDel.get(size - 1 - i)) == null)
						parentO[i] = -1;
					else
						parentO[i] = parentMap.get(markDel.get(size - 1 - i));
				}
				outputRT.removeRows(markDel.get(size - 1 - i), 1);
			}
			dg.repaint();
			if (chk.isSelected() == true) {
				if (isRowSet == false)
					_rt.cancelSorting();
				Arrays.sort(parentO);
				int indexL = parentO.length;
				for (int i = 0; i < indexL; i++) {
					if (parentO[indexL - 1 - i] < 0)
						continue;
					if (indexL - 1 - i > 0) {
						if (parentO[indexL - 1 - i] > parentO[indexL - 1
								- (i + 1)])
							if (isRowSet == false)
								_rt.removeRows(parentO[indexL - 1 - i], 1);
							else
								_rows.deleteRow(parentO[indexL - 1 - i]);
						else
							continue;
					} else {
						if (isRowSet == false)
							_rt.removeRows(parentO[indexL - 1 - i], 1);
						else
							_rows.deleteRow(parentO[indexL - 1 - i]);
					}
				}
				if (isRowSet == false)
					_rt.repaint();
				else if (_rows != null)
					_rows.close();
			}
			// Run delete with/without parent once. After that parent table and
			// mapping will change
			chk.setSelected(false);
			chk.setEnabled(false);
		}
	}

	/* This logic will not work with Hive. As rowset is not moving both ways
	 * A new rowset needs to be opened.
	 */
	private String getQString(int rowid) {
		String queryString = "";
		Object[] row = null;
		if (isRowSet == false)
			row = _rt.getRow(rowid);
		else {
			try {
				row = _rows.getRow(rowid + 1);
			} catch (Exception e) {
				ConsoleFrame.addText("\n Row Fetch Error:" + e.getMessage());
				e.printStackTrace();
			}
		}
		if (row == null)
			return "";

		for (int j = 0; j < row.length; j++) {
			int type = sType[j].getSelectedIndex();
			int imp = 5, charLen = 4; // default value
			float fuzzyval = 0.500f; // default value
			
			if (sImp[j].getValue() != null)
			 imp = (Integer)sImp[j].getValue();
			if (sFuzzy[j].getValue() != null)
			 fuzzyval = (Float)sFuzzy[j].getValue();
			if ( sCharLen[j].getValue() != null )
			 charLen = (Integer)sCharLen[j].getValue();
			
			String multiWordQuery = "";
			String skipText = skiptf[j].getText();
			boolean skip = true;
			String[] skiptoken = null;

			if (skipText != null && skipText.equals("") == false) {
				skip = false;
				skipText = skipText.trim().replaceAll(",", " ");
				skipText = skipText.trim().replaceAll("_", " "); // StandardAnalyse treats _ as new word
				skipText = skipText.trim().replaceAll("\\s+", " ");
				skiptoken = skipText.split(" ");
			}

			if (row[j] != null) {
				String term = row[j].toString();
				term.trim();
				switch (type) {
				case 0:
					continue; // do not use
				case 1: // Exact match
					boolean matchF = false;
					if (skip == false) {
						for (int k = 0; k < skiptoken.length; k++)
							if (skiptoken[k].compareToIgnoreCase(term) == 0) {
								matchF = true;
								break;
							}
					}
					if (matchF == true)
						continue;
					break;
				case 2:
				case 3: // It may have multi-words
					term = term.replaceAll(",", " ");
					term = term.replaceAll("\\s+", " ");
					String[] token = term.split(" ");
					String newTerm = "";
					for (int i = 0; i < token.length; i++) {
						if (token[i] == null || "".equals(token[i]))
							continue;
						matchF = false;
						if (skip == false) {
							for (int k = 0; k < skiptoken.length; k++)
								if (skiptoken[k].compareToIgnoreCase(token[i]) == 0) {
									matchF = true;
									break;
								}
						}
						if (matchF == true)
							continue;
						if (newTerm.equals("") == false && type == 3)
							newTerm += " AND ";
						if (newTerm.equals("") == false && type == 2)
							newTerm += " OR ";
						newTerm += colName[j] + ":"
								+ QueryParser.escape(token[i]) + "~"+fuzzyval+"^" + imp
								+ " "; // For Fuzzy Logic
					}
					multiWordQuery = newTerm;
					break;
				case 4: // Left Imp first n characters
					matchF = false;
					if (skip == false) {
						for (int k = 0; k < skiptoken.length; k++)
							if (skiptoken[k].compareToIgnoreCase(term) == 0) {
								matchF = true;
								break;
							}
					}
					if (matchF == true)
						continue;

					if (term.length() > charLen) {
						term = term.substring(0, charLen);
					}
					break;
				case 5: // Right Imp last n characters
					matchF = false;
					if (skip == false) {
						for (int k = 0; k < skiptoken.length; k++)
							if (skiptoken[k].compareToIgnoreCase(term) == 0) {
								matchF = true;
								break;
							}
					}
					if (matchF == true)
						continue;

					if (term.length() > charLen) {
						term = term.substring(term.length() - charLen, term.length());
					}
					break;

				default:
					break;

				}
				if (queryString.equals("") == false)
					queryString += " AND ";
				if (type == 2 || type == 3) // Single Word Match
					queryString += multiWordQuery;
				else if (type == 1) // Exact match
					queryString += colName[j] + ":\"" + term + "\"^" + imp;
				else if (type == 4) // Left Imp match
					queryString += colName[j] + ":"
							+ QueryParser.escape(term.trim()) + "*^" + imp;
				else if (type == 5) // Right Imp match
					queryString += colName[j] + ":*"
							+ QueryParser.escape(term.trim()) + "^" + imp;
			} else {
				if (type != 0 && imp > 1)
					return "";
			}
		}
		return queryString;
	}

	// Panel of delete is chosen
	private JComponent deletePanel() {
		JPanel delP = new JPanel();
		if (isRowSet == false)
			chk = new JCheckBox("Delete from Parent Table");
		else
			chk = new JCheckBox("Delete from Database");

		JButton delB = new JButton("Delete");
		delB.setActionCommand("delete");
		delB.addActionListener(this);
		delB.addKeyListener(new KeyBoardListener());

		JButton canB = new JButton("Cancel");
		canB.setActionCommand("cancel");
		canB.addActionListener(this);
		canB.addKeyListener(new KeyBoardListener());

		delP.add(chk);
		delP.add(delB);
		delP.add(canB);
		return delP;
	}
	// Panel of replace option is chosen
	private JComponent replacePanel() {
		JPanel delP = new JPanel();

		JButton delB = new JButton("Replace Selected");
		delB.setActionCommand("replaces");
		delB.addActionListener(this);
		delB.addKeyListener(new KeyBoardListener());

		JButton canB = new JButton("Cancel");
		canB.setActionCommand("cancel");
		canB.addActionListener(this);
		canB.addKeyListener(new KeyBoardListener());

		delP.add(delB);
		delP.add(canB);
		return delP;
	}

	public void tableChanged(TableModelEvent e) {
		isChanged = true;
	}
	
	private ReportTable createRT (String tabName, Vector<String> colName) {
		ReportTable newRT = null;
		QueryBuilder qb = new QueryBuilder(
				Rdbms_conn.getHValue("Database_DSN"), tabName,
				Rdbms_conn.getDBType());
		String query = qb.get_selCol_query(colName.toArray(),"");
		
		try {
			Rdbms_conn.openConn();
			ResultSet rs = Rdbms_conn.runQuery(query);
			newRT = SqlTablePanel.getSQLValue(rs, true);
			Rdbms_conn.closeConn();
		} catch (SQLException ee) {
			ConsoleFrame.addText("\n SQL Exception:" + ee.getMessage());
			return newRT; // newRT can not be populated
		}
		return newRT;
		
	}
	
	private void searchTableIndex(String searchStr, int colIndex) { // -1 data in all columns
		outputRT = new ReportTable(colName, false, true);
		
		if (_simcheck.openIndex() == false)
			return;

		String queryString = searchStr;
		if (queryString == null || queryString.equals("") == true)
			return;
		
		    queryString = prepareLQuery(queryString, colIndex);
			Query qry = _simcheck.parseQuery(queryString);
			Hits hit = _simcheck.searchIndex(qry);
			if (hit == null || hit.length() <= 0) {
				JOptionPane.showMessageDialog(null,
						"No Matching Record Found", "No Record Found",
						JOptionPane.INFORMATION_MESSAGE);
				return; 
			}
			// Iterate over the Documents in the Hits object

			for (int j = 0; j < hit.length(); j++) {
				try {
					Document doc = hit.doc(j);
					String rowid = doc.get("at__rowid__");
					Object[] row = null;
						row = _rt.getRow(Integer.parseInt(rowid));

					outputRT.addFillRow(row);

				} catch (Exception e) {
					ConsoleFrame.addText("\n " + e.getMessage());
					ConsoleFrame.addText("\n Error: Can not open Document");
				}
			}

			
		_simcheck.closeSeachIndex();

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(outputRT, BorderLayout.CENTER);
		// Show the table now
		dg = new JFrame("Similar Records Frame");
		dg.setLocation(250, 100);
		dg.getContentPane().add(jp_p);
		dg.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dg.pack();
		QualityListener.bringToFront(dg);
	}
	
	
	
	public String prepareLQuery(String rawquery, int colIndex) {
		rawquery = rawquery.replaceAll(",", " ");
		rawquery = rawquery.trim().replaceAll("_", " "); // StandardAnalyzer treats _ as new word
		rawquery = rawquery.replaceAll("\\s+", " ");
		String[] token = rawquery.split(" ");
		String newTerm = "";
		
	if (colIndex < 0 ) {
		for (int j=0; j<colName.length; j++ ) {
			for (int i = 0; i < token.length; i++) {
				if (token[i] == null || "".equals(token[i]))
				continue;

				if (newTerm.equals("") == false )
				newTerm += " OR ";
				
				newTerm += colName[j] + ":"
					+ QueryParser.escape(token[i]) + "~0.5 "; // For Fuzzy Logic
		
			}
		}
	} else { // prepare query for colName
		String colTitle = _rt.getModel().getColumnName(colIndex);
		for (int i = 0; i < token.length; i++) {
			if (token[i] == null || "".equals(token[i]))
			continue;
			
			if (newTerm.equals("") == false )
				newTerm += " AND ";
			
			newTerm += colTitle + ":"
					+ QueryParser.escape(token[i]) + "~0.5 "; // For Fuzzy Logic
		}
		
	}
		return newTerm;
	}

	public void itemStateChanged(ItemEvent event) {
		
		if (event.getStateChange() == ItemEvent.SELECTED ) {
			JComboBox<String> selCombo = (JComboBox<String>) event.getSource();
			String s = event.getItem().toString();
			int index = Integer.parseInt(selCombo.getActionCommand());
			
			if("Not Applicable".equalsIgnoreCase(s) == true) {
				sImp[index].setEnabled(false);
				sFuzzy[index].setEnabled(false);
				sCharLen[index].setEnabled(false);
				skiptf[index].setEnabled(false);
			} else {
				skiptf[index].setEnabled(true);
				sImp[index].setEnabled(true);
				
				if("Exact Match".equalsIgnoreCase(s) == true  ) {
					sImp[index].setEnabled(false);
					sFuzzy[index].setValue(new Float(1.000f));
					sFuzzy[index].setEnabled(false);
					sCharLen[index].setEnabled(false);
				} 
				if("Similar-Any Word".equalsIgnoreCase(s)  == true || 
						"Similar-All Words".equalsIgnoreCase(s) == true	) {
					sImp[index].setValue(new Integer(10));
					sFuzzy[index].setValue(new Float(0.500f));
					sFuzzy[index].setEnabled(true);
					sCharLen[index].setEnabled(false);
					
				}
				if("Begin Char Match".equalsIgnoreCase(s)  == true ||
						"End Char Match".equalsIgnoreCase(s)  == true ) {
					sImp[index].setValue(new Integer(5));
					sFuzzy[index].setValue(new Float(1.000f));
					sFuzzy[index].setEnabled(false);
					sCharLen[index].setEnabled(true);
					sCharLen[index].setValue(new Integer(4));
					
				} 
			}	
		}
	}

	private boolean validateInput() {
		for (int j=0; j < sImp.length; j++) {
			
			if (sImp[j].isEnabled() == true ) {
				if (sImp[j].getValue() != null ) {
					int imp = (Integer)sImp[j].getValue();
					if (imp < 0) {
						JOptionPane.showMessageDialog(null,
								"Boosting factor can not be Negative");
						return false;
					}	
				} else {
					JOptionPane.showMessageDialog(null,
							"Boosting factor can not be Null");
					return false;
				}
			}
			
			if (sFuzzy[j].isEnabled() == true ) {
				if (sFuzzy[j].getValue() != null ) {
					float fuzzy = (Float)sFuzzy[j].getValue();
					if (fuzzy < 0.0f || fuzzy > 1.0f) {
						JOptionPane.showMessageDialog(null,
								"Similarity Index has to be between 0.0f and 1.0f");
						return false;
					}	
				} else {
					JOptionPane.showMessageDialog(null,
							"Similarity Index can not be Null");
					return false;
				}
			}
			
			if (sCharLen[j].isEnabled() == true ) {
				if (sCharLen[j].getValue() != null ) {
					int charLen = (Integer)sCharLen[j].getValue();
					if (charLen <= 0 ) {
						JOptionPane.showMessageDialog(null,
								"Character Length can not be Negative");
						return false;
					}	
				} else {
					JOptionPane.showMessageDialog(null,
							"Character Length can not be Null");
					return false;
				}
			}
		}
		return true;
	}
} // End of Similarity Check panel
