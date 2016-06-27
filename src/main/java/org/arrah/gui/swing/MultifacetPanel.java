package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2015      * 
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
 * This file is used to create multifaceted 
 * search - based on value and range
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
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;

import org.arrah.framework.dataquality.SimilarityCheckLucene;
import org.arrah.framework.rdbms.JDBCRowset;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class MultifacetPanel implements ActionListener, ItemListener {
	private ReportTable _rt, outputRT;
	private String[] colName;
	private String[] colType;
	private JDialog d_m;
	private JFrame dg;
	private JComboBox<String>[] sType;
	private JTextField[] sSearch;
	private JTextField[] sLower,sHigher;
	boolean isRowSet = false;
	private JDBCRowset _rows;
	private SimilarityCheckLucene _simcheck;
	private boolean isCancel=false;

	
	// For independent table and selected Columns
	public MultifacetPanel( String tabName, Vector<String> colV) {
		
		_rt = createRT(tabName,colV);
		_simcheck = new SimilarityCheckLucene(_rt.getRTMModel());
		colName = getColName();
		_simcheck.makeIndex();
		mapDialog();
	}
	
	// For RTM  table and single Column Search
	public MultifacetPanel(ReportTable reportTable) {
		
		_rt = reportTable;
		_simcheck = new SimilarityCheckLucene(_rt.getRTMModel());
		colName = getColName();
		_simcheck.makeIndex();
		mapDialog();
		
	}

	// Private supporting functions
	
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

// UI for multi-facet search
	private JDialog mapDialog() {

		int colC = colName.length;
		sType = new JComboBox[colC];
		sSearch = new JTextField[colC];
		sLower = new JTextField[colC];
		sHigher = new JTextField[colC];
		JTextField[] sColName = new JTextField[colC];
		

		JPanel jp = new JPanel();
		SpringLayout layout = new SpringLayout();
		jp.setLayout(layout);

		JLabel l1 = new JLabel("Field Name");
		l1.setForeground(Color.BLUE);
		JLabel l2 = new JLabel("Search Criterion");
		l2.setForeground(Color.BLUE);
		JLabel l3 = new JLabel("Search String");
		l3.setForeground(Color.BLUE);
		JLabel l4 = new JLabel("Lower Range");
		l4.setForeground(Color.BLUE);
		JLabel l5 = new JLabel("Upper Range");
		l5.setForeground(Color.BLUE);

		jp.add(l1);
		jp.add(l2);
		jp.add(l3);
		jp.add(l4);
		jp.add(l5);
		

		for (int i = 0; i < colC; i++) {
			sColName[i] = new JTextField(8);
			sColName[i].setText(colName[i]);
			sColName[i].setEditable(false);
			sColName[i].setToolTipText(colType[i]);
			jp.add(sColName[i]);

			sType[i] = new JComboBox<String>(new String[] { "Not Applicable", "Exact Match",
					"Similar-Any Word", "Similar-All Words", "Range Bound" });
			sType[i].addItemListener(this);
			sType[i].setActionCommand(Integer.toString(i));
			jp.add(sType[i]);

			sSearch[i] = new JTextField(10);
			sSearch[i].setEnabled(false);
			jp.add(sSearch[i]);
			
			sLower[i] = new JTextField(10);
			sLower[i].setText(new String("Lower"));
			sLower[i].setEnabled(false);
			jp.add(sLower[i]);
			
			sHigher[i] = new JTextField(10);
			sHigher[i].setText(new String("Higher"));
			sHigher[i].setEnabled(false);
			jp.add(sHigher[i]);

		}
		SpringUtilities.makeCompactGrid(jp, colC + 1, 5, 3, 3, 3, 3); // +1 for
																		// header

		JScrollPane jscrollpane1 = new JScrollPane(jp);
		if (colC * 35 + 50 > 400)
			jscrollpane1.setPreferredSize(new Dimension(625, 400));
		else
			jscrollpane1.setPreferredSize(new Dimension(625, colC * 35 + 50));

		JPanel bp = new JPanel();
		
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
		d_m.setTitle("MultiFacet Map Dialog");
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
				searchTableIndex();
			} finally {
				d_m.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				d_m.dispose();
			}
		}
		if (command.equals("mcancel")) {
			if (isRowSet == true && _rows != null)
				_rows.close();
			setCancel(true);
			d_m.dispose();
		}
	}

	// Create Lucene query
	private String getQString() {
		String queryString = "";
		int colc = colName.length;

		for (int j = 0; j < colc; j++) {
			int type = sType[j].getSelectedIndex();
			float fuzzyval = 0.600f; // default fuzzy value
			String serterm= null;String lv = null; String hv = null;
			
			if (sSearch[j].isEnabled() == true)
				serterm = sSearch[j].getText();
			
			if (sLower[j].isEnabled() == true && sHigher[j].isEnabled() == true) {
			  lv = (String)sLower[j].getText();
			  hv = (String)sHigher[j].getText();
			}
			
			String multiWordQuery = "";

			if (serterm != null || lv != null || hv != null) {
				String term ="";
				
				switch (type) {
				case 0:
					continue; // do not use
				case 1: // Exact match
					term = serterm;
					term.trim();
					break;
				case 2:
					term = serterm;
					term.trim();
				case 3: // It may have multi-words
					term = term.replaceAll(",", " ");
					term = term.replaceAll("\\s+", " ");
					String[] token = term.split(" ");
					String newTerm = "";
					for (int i = 0; i < token.length; i++) {
						if (token[i] == null || "".equals(token[i]))
							continue;

						if (newTerm.equals("") == false && type == 3)
							newTerm += " AND ";
						if (newTerm.equals("") == false && type == 2)
							newTerm += " OR ";
						newTerm += colName[j] + ":"
								+ QueryParser.escape(token[i]) + "~"+fuzzyval+ " "; // For Fuzzy Logic
					}
					multiWordQuery = newTerm;
					break;
				case 4: // It may have range Bound query
					String ls = lv.toString();
					String hs = hv.toString();
					newTerm = colName[j] + ":[" + ls+ " TO " + hs+ "]";
					
					multiWordQuery = newTerm;
					break;
					
				default:
					break;

				}
				if (queryString.equals("") == false)
					queryString += " AND ";
				if (type == 2 || type == 3 || type == 4) // Single Word Match
					queryString += multiWordQuery;
				else if (type == 1) // Exact match
					queryString += colName[j] + ":\"" + term + "\"";
			}
		}
		ConsoleFrame.addText("\n Lucene Query is:"+queryString);
		return queryString;
	}

	
	private void searchTableIndex() { // Search the table
		outputRT = new ReportTable(colName, false, true);
		
		if (_simcheck.openIndex() == false)
			return;

		String queryString = getQString();
		if (queryString == null || queryString.equals("") == true) {
			isCancel = true;
			JOptionPane.showMessageDialog(null,"Empty Query");
			return;
		}
		
			Query qry = _simcheck.parseQuery(queryString);
			Hits hit = _simcheck.searchIndex(qry);
			if (hit == null || hit.length() <= 0) {
				JOptionPane.showMessageDialog(null,
						"No Matching Record Found", "No Record Found",
						JOptionPane.INFORMATION_MESSAGE);
				isCancel = true; // do not dispose 
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
		dg = new JFrame("Multi facet  Frame");
		dg.setLocation(250, 100);
		dg.getContentPane().add(jp_p);
		dg.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dg.pack();
		QualityListener.bringToFront(dg);
	}
	
	
	
	public void itemStateChanged(ItemEvent event) {
		
		if (event.getStateChange() == ItemEvent.SELECTED ) {
			JComboBox<String> selCombo = (JComboBox<String>) event.getSource();
			String s = event.getItem().toString();
			int index = Integer.parseInt(selCombo.getActionCommand());
			
			if("Not Applicable".equalsIgnoreCase(s) == true) {
				sSearch[index].setEnabled(false);
				sLower[index].setEnabled(false);
				sHigher[index].setEnabled(false);

			} else {
				
				if("Exact Match".equalsIgnoreCase(s) == true  ) {
					sSearch[index].setEnabled(true);
					sLower[index].setEnabled(false);
					sHigher[index].setEnabled(false);
				} 
				if("Similar-Any Word".equalsIgnoreCase(s)  == true || 
						"Similar-All Words".equalsIgnoreCase(s) == true	) {
					sSearch[index].setEnabled(true);
					sLower[index].setEnabled(false);
					sHigher[index].setEnabled(false);
					
				}
				if("Range Bound".equalsIgnoreCase(s)  == true ) {
					sSearch[index].setEnabled(false);
					sLower[index].setEnabled(true);
					sHigher[index].setEnabled(true);
					
				} 
			} // end of else
		}
	} // end of item state

	private boolean validateInput() {
		for (int j=0; j < colName.length; j++) {
			
			if (sSearch[j].isEnabled() == true ) {
				if (sSearch[j].getText() == null  || "".equals(sSearch[j].getText()) == true) {
					JOptionPane.showMessageDialog(null,
							"Search String can not be empty");
					return false;
				}
			}
			
			if (sLower[j].isEnabled() == true || sHigher[j].isEnabled() == true) {
				if (sLower[j].getText() == null  || sHigher[j].getText() == null) {
					
					JOptionPane.showMessageDialog(null,
							"Range Value can not be empty");
					return false;
				}
			}
		}
		return true;
	}

	public boolean isCancel() {
		return isCancel;
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
} // End of Multi Facet panel
