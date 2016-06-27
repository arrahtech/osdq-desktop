package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          * 
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* 
 * This file is used to validating
 * cross column data. String will be 
 * fuzzy search in all selected column
 *  line ( col A AND col B) OR 
 *  (col A AND col C ) OR 
 *  (col B AND col C)
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import org.arrah.framework.rdbms.Rdbms_conn;

public class CrossColumnPanel implements ActionListener, TableModelListener, ItemListener {
	private ReportTable _rt, outputRT;
	private String[] colName;
	private String[] colType;
	private int rowC;
	private Vector<Integer> skipVC, markDel;
	private JDialog d_m;
	private JFrame dg;
	private JCheckBox chk;
	private JComboBox<String>[] sType;
	private JFormattedTextField[] sImp,sFuzzy;
	private JCheckBox[] sCheck; // this is to hold the option for selection
	private JTextField[] skiptf;
	private boolean isChanged = false;
	private Hashtable<Integer, Integer> parentMap; // it will hold original position
	boolean isRowSet = false;
	private JDBCRowset _rows;
	private SimilarityCheckLucene _simcheck;
	private String queryForRowset="";

	// For ReportTable Input
	public CrossColumnPanel(ReportTable rt) {
		_rt = rt;
		_simcheck = new SimilarityCheckLucene(_rt.getRTMModel());
		colName = getColName();
		rowC = _rt.table.getRowCount();
		_rt.getModel().addTableModelListener(this);
		mapDialog();
	}

	// For Rowset Table Input for rdbms
	public CrossColumnPanel(JDBCRowset rowSet) {
		isRowSet = true;
		_rows = rowSet;
		_simcheck = new SimilarityCheckLucene(_rows);
		colName = _rows.getColName();
		colType = _rows.getColType();
		rowC = _rows.getRowCount();
		mapDialog();
	}
	
	// For Rowset Table Input for Hive
	// Hive rowset does not move forward and backward so create new rowset
		public CrossColumnPanel(JDBCRowset rowSet, String query) {
			queryForRowset = query;
			isRowSet = true;
			_rows = rowSet;
			_simcheck = new SimilarityCheckLucene(_rows);
			colName = _rows.getColName();
			colType = _rows.getColType();
			rowC = _rows.getRowCount();
			mapDialog();
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
		String[] newColN = new String[colName.length + 2];
		newColN[0] = "Delete Editable"; // CheckBox selection
		newColN[1] = "Group ID"; // Group ID and Row number
		for (int i = 0; i < colName.length; i++)
			newColN[i + 2] = colName[i];

		outputRT = new ReportTable(newColN, false, true);
		skipVC = new Vector<Integer>();
		parentMap = new Hashtable<Integer, Integer>();
		
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
			
			if (hit == null || hit.length() < 1) // Match one is valid as it is in other columns
				continue; 
			
			// We need to show the row details that matched as first row
			String rowString="";
			if (isRowSet == false)
				rowString = "at__rowid__:"+"\""+i+"\"";
			else 
				 rowString = "at__rowid__:"+"\""+(i+1)+"\""; // rowset rows start from 1
			
			Query rowqry = _simcheck.parseQuery(rowString);
			Hits rowhit = _simcheck.searchIndex(rowqry);
			
			if (rowhit == null || rowhit.length() < 1) { // It is not able to find the row
				ConsoleFrame.addText("\n Query could not find any match");
				continue;
			}
			try {
				Document rowdoc = rowhit.doc(0);
				Object[] row = null;
				row = new Object[colName.length + 2];
				row[0] = false;
				if (isRowSet == false)
					row[1] = "Group: "+grpid+ " Index:"+i; // index start from 0
				else
					row[1] = "Group: "+grpid+ " Row:"+(i+1);
				
				for (int k =0; k < colName.length ; k++)
					row[k+2] = rowdoc.get(colName[k]);
				outputRT.addFillRow(row);
			} catch (Exception e) {
				ConsoleFrame.addText("\n Exception in fetching Row Id:"+e.getLocalizedMessage() );
				continue;
			} 
			
			
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
					Object[] newRow = new Object[row.length + 2];
					boolean del = false;
					newRow[0] = del;
					if (isRowSet == false)
						newRow[1] = "Group: "+grpid+ " Index:"+rowid;
					else
						newRow[1] = "Group: "+grpid+ " Row:"+ rowid ;
					
					for (int k = 0; k < row.length; k++)
						newRow[k + 2] = row[k];
					
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

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(outputRT, BorderLayout.CENTER);
		jp_p.add(deletePanel(), BorderLayout.PAGE_END);
		// Show the table now
		dg = new JFrame("Cross Column Search Frame");
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
		sCheck = new JCheckBox[colC];

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
		JLabel l6 = new JLabel("Select Column");
		l6.setForeground(Color.BLUE);
		
		jp.add(l6);jp.add(l1);jp.add(l2);jp.add(l3);jp.add(l5);jp.add(l4);
		

		for (int i = 0; i < colC; i++) { // only first or primary column will be enabled
			
			sCheck[i] = new JCheckBox();
			if ( i == 0) {
				// First column is primary that you can not change
				sCheck[i].setSelected(true);
				sCheck[i].setEnabled(false);
				
			}
			jp.add(sCheck[i]);
			
			tf1[i] = new JTextField(8);
			tf1[i].setText(colName[i]);
			tf1[i].setEditable(false);
			tf1[i].setToolTipText(colType[i]);
			jp.add(tf1[i]);

			sType[i] = new JComboBox<String>(new String[] { "Exact Match",
					"Similar-Any Word", "Similar-All Words"});
			sType[i].addItemListener(this);
			sType[i].setActionCommand(Integer.toString(i));
			if (i > 0)
				sType[i].setEnabled(false);
			jp.add(sType[i]);

			sImp[i] = new JFormattedTextField();
			sImp[i].setEnabled(false);
			jp.add(sImp[i]);
			
			sFuzzy[i] = new JFormattedTextField();
			if ( i == 0 )
			sFuzzy[i].setValue(new Float(1.000f));
			sFuzzy[i].setEnabled(false);
			jp.add(sFuzzy[i]);

			skiptf[i] = new JTextField(12);
			skiptf[i].setText("And,Or,Not"); // Lucene core words
			skiptf[i].setEnabled(false);
			jp.add(skiptf[i]);

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
		d_m.setTitle("Cross Column Map Dialog");
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
					"0.0 - No match                1.0 Exact match \n\n\n" +
					"String value of first column will be matched.\n" +
					"Exact or Fuzzy ( multi-word) in all selected columns.");
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
								"Parent table has changed.\n Run Cross Column Check again to get updated value.");
				ConsoleFrame
						.addText("\n Parent table has changed.\n Run Cross Column Check again get updated value.");
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

		for (int j = 0; j < 1; j++) { // no need to all column. Only take first column
			int type = sType[j].getSelectedIndex();
			int imp = 5;// default value
			float fuzzyval = 0.500f; // default value
			
			if (sImp[j].getValue() != null)
			 imp = (Integer)sImp[j].getValue();
			if (sFuzzy[j].getValue() != null)
			 fuzzyval = (Float)sFuzzy[j].getValue();

			
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

			if (row[j] != null) { // cross column search we have to check term in other columns
				/* ( col A AND col B) OR 
				 *  (col A AND col C ) OR 
				 *  (col B AND col C)
				 */  
				 
				String term = row[j].toString();
				term.trim();
				switch (type) {
				
				case 0: // Exact match
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
				case 1:
				case 2: // It may have multi-words
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
					
						if (newTerm.equals("") == false && type == 2)
							newTerm += " AND ( ";
						if (newTerm.equals("") == false && type == 1)
							newTerm += " OR (";
						
						if (newTerm.equals("") == true )
							newTerm = "(";
						// loop all other column
						int skipCount = 0;
						for ( int nextcol=j+1; nextcol < row.length; nextcol++) {
							if (sCheck[nextcol].isSelected() == false) {
								skipCount++;
								continue;
							}
							if (nextcol > (j+1+skipCount) )
								newTerm += " OR ";
							newTerm += colName[nextcol] + ":"
								+ QueryParser.escape(token[i]) + "~"+fuzzyval+"^" + imp
								+ " "; // For Fuzzy Logic
						}
						// Grouping of newTerm
						newTerm =  newTerm+")";
					}
					multiWordQuery = newTerm;
					break;
					
				default:
					break;

				}
				if (type == 1 || type == 2)  {// Single Word Match
					queryString += multiWordQuery;
				} else if (type == 0)  { // Exact match
					// loop all other column
					int skipCount = 0;
					for ( int nextcol=j+1; nextcol < row.length; nextcol++) {
						if (sCheck[nextcol].isSelected() == false) {
							skipCount++;
							continue;
						}
						if (nextcol > (j+1+skipCount) )
							queryString += " OR ";
					queryString += colName[nextcol] + ":\"" + term + "\"^" + imp;
					}
				}
			} else {
				if (type != 0 && imp > 1)
					return "";
			}
		}
		return queryString;
	}

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

	public void tableChanged(TableModelEvent e) {
		isChanged = true;
	}
	
	public void itemStateChanged(ItemEvent event) {
		
		if (event.getStateChange() == ItemEvent.SELECTED ) {
			JComboBox<String> selCombo = (JComboBox<String>) event.getSource();
			String s = event.getItem().toString();
			int index = Integer.parseInt(selCombo.getActionCommand());

				skiptf[index].setEnabled(true);
				sImp[index].setEnabled(true);
				
				if("Exact Match".equalsIgnoreCase(s) == true  ) {
					sImp[index].setEnabled(false);
					sFuzzy[index].setValue(new Float(1.000f));
					sFuzzy[index].setEnabled(false);
				} 
				if("Similar-Any Word".equalsIgnoreCase(s)  == true || 
						"Similar-All Words".equalsIgnoreCase(s) == true	) {
					sImp[index].setValue(new Integer(10));
					sFuzzy[index].setValue(new Float(0.500f));
					sFuzzy[index].setEnabled(true);
					
				}	
		}
	}

	private boolean validateInput() {
		int checkCount=0;
		
		for (int j=0; j < sImp.length; j++) {
			if (sCheck[j].isSelected() == true)
				checkCount++;
			
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
		}
		if (checkCount < 2)  { // need minimum 2 columns 
			JOptionPane.showMessageDialog(null,
					"Minimum Two columns should be selected \n\n" +
					"One Primary (already selected) \n and " +
					"Other Secondaries to compare with");
			return false;
		}
		return true;
	}
} // End of Cross Column Check
