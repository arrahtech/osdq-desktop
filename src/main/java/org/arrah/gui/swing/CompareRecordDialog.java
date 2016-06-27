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

/* This file is used for getting info about 
 * comparing record tables for diff, merge
 * and linkage
 * 
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.arrah.framework.dataquality.RecordMatch;
import org.arrah.framework.dataquality.RecordMatch.MultiColData;
import org.arrah.framework.dataquality.StringMergeUtil;

public class CompareRecordDialog implements ActionListener {
	private ReportTable _lTab, _rTab, _rt;
	private Vector<String> _lCols, _rCols;
	private JComboBox<String>[] _rColC, _algoC , _dataActionC = null;
	private JCheckBox[] _checkB;
	private JFormattedTextField[] _simIndex;
	private JDialog d_m, d_recHead, d_nonMap ,d_r;
	private int _type;
	private Vector<Integer> _leftMap, _rightMap, _dataActionNonMap = null;
	private boolean _singleFile = false, _mapCancel = false;
	private Integer[] _actionType;
	private JButton refreshB,pushgoldenB;
	
	/* leftTable is must . rightTable only in multiple file or linkage option */
	public CompareRecordDialog (ReportTable leftTable, ReportTable rightTable, int type) {
		_lTab = leftTable;
		_rTab = rightTable;
		_type = type;
		if (_lTab == null  ){
			ConsoleFrame.addText("\n Input data is empty");
			return;
		}
		else {	
			_lCols = new Vector<String>();
			_rCols = new Vector<String>();
		}
		if (_rTab == null ) {
			_singleFile = true;
			_rTab = _lTab; // dummy assignment
		}
		
		for (int i=0 ; i <_lTab.getModel().getColumnCount(); i++ )
		_lCols.add(_lTab.getModel().getColumnName(i));
		
		for (int i=0 ; i <_rTab.getModel().getColumnCount(); i++ )
			_rCols.add(_rTab.getModel().getColumnName(i));
	}

	// Create GUI and show both table
	public JDialog createMapDialog() {
		
		JPanel tp = new JPanel();
		tp.setPreferredSize(new Dimension (1100,500));
		BoxLayout boxl = new BoxLayout(tp,BoxLayout.X_AXIS);
		tp.setLayout(boxl);
		tp.add(_lTab); 
		if (_singleFile == false)
			tp.add(_rTab);
		
		JScrollPane jscrollpane = new JScrollPane(tp);
		jscrollpane.setPreferredSize(new Dimension(1125, 525));
		
		JPanel bp = new JPanel();
		JButton ok = new JButton("Next");;
		ok.setActionCommand("next");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(jscrollpane, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		d_m = new JDialog();
		d_m.setModal(true);
		d_m.setTitle("Record Display Dialog");
		d_m.setLocation(50, 50);
		d_m.getContentPane().add(jp_p);
		d_m.pack();
		d_m.setVisible(true);

		return d_m;
	}
	
	private JDialog showHeaderMap() {
		// Header Making
		JPanel jp = new JPanel(new SpringLayout());
		int colCount = _lTab.getModel().getColumnCount(); // left column is master column
		_rColC = new JComboBox[colCount];
		_algoC = new JComboBox[colCount];
		_checkB = new JCheckBox[colCount];
		_simIndex = new JFormattedTextField[colCount];
		
		String[] algoList = new String[]{"Levenshtein","JaroWinkler","Jaro","CosineSimilarity","BlockDistance","ChapmanLengthDeviation",
				"ChapmanMeanLength","ChapmanOrderedNameCompoundSimilarity","DiceSimilarity","EuclideanDistance",
				"JaccardSimilarity","MatchingCoefficient","MongeElkan","NeedlemanWunch","OverlapCoefficient",
				"QGramsDistance","SmithWaterman","SmithWatermanGotohWindowedAffine","Soundex","TagLink","TagLinkToken"};
		
		for (int i =0; i < colCount; i++ ){
			_rColC[i] = new JComboBox<String>();
			for ( int j=0; j < _rCols.size() ; j++)
				_rColC[i].addItem(_rCols.get(j));
		}
		
		for (int i=0; i < colCount; i++) {
			_checkB[i] = new JCheckBox();
			jp.add(_checkB[i]);
			JLabel rColLabel = new JLabel(_lCols.get(i),JLabel.TRAILING);
			jp.add(rColLabel);
			JLabel mapA = new JLabel("   Matches:   ",JLabel.TRAILING);
			mapA.setForeground(Color.BLUE);
			jp.add(mapA);
			jp.add(_rColC[i]);
			_algoC[i] = new JComboBox<String>(algoList);
			jp.add(_algoC[i]);
			_simIndex[i] = new JFormattedTextField();
			_simIndex[i].setValue(new Float(1.000f));
			_simIndex[i].setColumns(10);
			_simIndex[i].setToolTipText("<html> <body> Enter a value between 0.00 and 1.00 <BR>" +
					"0.00 for No Match 1.00 for Exact match </body></html>");
			jp.add(_simIndex[i]);
		}
		
		SpringUtilities.makeCompactGrid(jp, colCount, 6, 3, 3, 3, 3);
		JScrollPane jscrollpane1 = new JScrollPane(jp);
		if ((100 + (colCount * 35)) > 500)
			jscrollpane1.setPreferredSize(new Dimension(675, 400));
		else
			jscrollpane1.setPreferredSize(new Dimension(675, 75 + colCount * 35));
		
		JPanel bp = new JPanel();
		JButton ok = new JButton("Next");;
		ok.setActionCommand("compare");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancelHeader");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(jscrollpane1, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		d_recHead = new JDialog();
		d_recHead.setModal(true);
		d_recHead.setTitle("Record HeaderMap Dialog");
		d_recHead.setLocation(250, 100);
		d_recHead.getContentPane().add(jp_p);
		d_recHead.pack();
		d_recHead.setVisible(true);

		return d_recHead;

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("cancel".equals(e.getActionCommand())) 
			d_m.dispose();
		if ("cancelHeader".equals(e.getActionCommand())) 
			d_recHead.dispose();
		if ("cancelNonMap".equals(e.getActionCommand())) {
			_mapCancel = true;
			d_nonMap.dispose();
		}
		if ("refresh".equals(e.getActionCommand())) {
			ReportTable newRT = null;
			d_r.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			newRT = recalMergeTable (newRT);
			d_r.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			
			if (newRT == null || newRT.table == null) {
				JOptionPane.showMessageDialog(null, "Refreshed Table is Null");
				ConsoleFrame.addText("\n Refreshed Table is null");
				return;
			}
			_rt = newRT;
			d_r.getContentPane().removeAll();
			d_r.getContentPane().add(mergePanel());
			d_r.revalidate(); d_r.repaint();
		}
		if ("golden".equals(e.getActionCommand())) {
			
			int option = JOptionPane.showConfirmDialog(null, "Only Golden Values will be kept. \n" +
					"Others will be discarded. \n \n Continue ?", "Confirmation",JOptionPane.YES_NO_OPTION);
			if (option != JOptionPane.OK_OPTION) return;
			
			if (_rt.isSorting() || _rt.table.isEditing()) {
				JOptionPane.showMessageDialog(null, "Table is in Sorting or Editing State");
				return;
			}
			int rowcount = _rt.table.getRowCount();
			d_r.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			for ( int i = rowcount-1 ; i >= 0; i--) {
				String val = _rt.getTextValueAt(i, 0);
				if (val.equalsIgnoreCase("Golden Merge") == false) {
					_rt.removeRows(i, 1);
				}
			}
			d_r.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			d_r.getContentPane().removeAll();
			d_r.getContentPane().add(mergePanel());
			refreshB.setEnabled(false); pushgoldenB.setEnabled(false); // disable refresh
			d_r.revalidate(); d_r.repaint();
		}
		if ("pushgolden".equals(e.getActionCommand())) {
			
			String option = JOptionPane.showInputDialog(null, "Golden Values will be pushed to Matched Records. \n" +
					"Enter the Column Name to push values:", "Column Information",JOptionPane.OK_CANCEL_OPTION);
			
			if (option == null || "".equals(option)) 
				return;
			
			Object[] colName = _rt.getAllColName();
			int colMatI= -1;
			
			for (int i =0 ; i < colName.length; i++) {
				if (option.equals(colName[i].toString())== true) {
					colMatI = i;
					break;
				}
			}
			if (colMatI < 0) { // colName did not Match
				JOptionPane.showMessageDialog(null, "Column Name did not Match");
				return;
			}
			if (_rt.isSorting() || _rt.table.isEditing()) {
				JOptionPane.showMessageDialog(null, "Table is in Sorting or Editing State");
				return;
			}
			int rowcount = _rt.table.getRowCount();
			Object obj="";
			d_r.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			for ( int i = 0; i < rowcount; i++) {
				String val = _rt.getTextValueAt(i, 0);
				if (val.equalsIgnoreCase("Golden Merge") == true) {
					obj = _rt.getModel().getValueAt(i, colMatI);
				} else if (val.equalsIgnoreCase("") == true) {
					continue;
				} else {
					//_rt.setTableValueAt(obj, i, colMatI);
					_rt.getModel().setValueAt(obj, i, colMatI);
				}
			}
			d_r.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			d_r.getContentPane().removeAll();
			d_r.getContentPane().add(mergePanel());
			d_r.revalidate(); d_r.repaint();
		}
		if ("next".equals(e.getActionCommand())) 
			showHeaderMap();
		if ("Continue".equals(e.getActionCommand())) {
			_dataActionNonMap = new Vector<Integer>();
			for (int i=0; i< _dataActionC.length; i++)
				_dataActionNonMap.add(i,_dataActionC[i].getSelectedIndex());
			d_nonMap.dispose();
		}
		
		if ("compare".equals(e.getActionCommand())) {
			_leftMap = new Vector<Integer>();
			_rightMap = new Vector<Integer>();
			RecordMatch diff = new RecordMatch();
			List<RecordMatch.ColData> diffCols = new ArrayList<RecordMatch.ColData>();
			
			for (int i =0; i < _lTab.getModel().getColumnCount(); i++) {
				if (_checkB[i].isSelected() == false ) continue;
				int rightIndex =  _rColC[i].getSelectedIndex();
				if (_rightMap.contains(rightIndex) == true) {
					JOptionPane.showMessageDialog(null, "Duplicate Mapping at Row:"+i,
							"Record HeaderMap Dialog",JOptionPane.INFORMATION_MESSAGE);
					return;
				} else { // Create ColData and MultiCol data here to feed to RecordMatch class
					float simVal = (Float)_simIndex[i].getValue();
					if ( simVal < 0.00f || simVal > 1.00f ){
						JOptionPane.showMessageDialog(null, "Similarity Index must be between 0.00 and 1.00 at Row:"+i,
								"Record HeaderMap Dialog",JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					_rightMap.add(rightIndex);
					_leftMap.add(i);
					String algoName = _algoC[i].getSelectedItem().toString().toUpperCase();
					RecordMatch.ColData col1 = diff.new ColData(i,_rColC[i].getSelectedIndex(),simVal,algoName);
					diffCols.add(col1);
				}
			}
			if (_rightMap.size() == 0)  { // 
				JOptionPane.showMessageDialog(null, "Select atleast one Column Mapping" ,
						"Record HeaderMap Dialog",JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			// Send Information to record  comparison
			try {
			d_m.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			d_recHead.setVisible(false);
			MultiColData m1 = diff.new MultiColData();
			m1.setA(diffCols);
			// m1.setAlgoName("LEVENSHTEIN"); not rquired at MultiCol level
			RecordMatch.operator doDiff = diff.new operator();
			
			// Now I have to load tables into memory and do Match/Linkage
			
			List<List<String>> lRecordList = new ArrayList<List<String>>();
			List<List<String>> rRecordList = new ArrayList<List<String>>();
			for (int i=0; i < _lTab.getModel().getRowCount(); i++) {
				Object[] rowObject = _lTab.getRow(i);
				List<String> row = new ArrayList<String>();
				for(Object a: rowObject) {
					if (a !=null) {
						String cell = a.toString();
						row.add(cell);
					} else {
						row.add(""); // for null objects
					}
					
				}
				lRecordList.add(row);
			}
			
			for (int i=0; i < _rTab.getModel().getRowCount(); i++) {
				Object[] rowObject = _rTab.getRow(i);
				List<String> row = new ArrayList<String>();
				for(Object a: rowObject) {
					if (a !=null) {
						String cell = a.toString();
						row.add(cell);
					} else {
						row.add(""); // for null objects
					}
					
				}
				rRecordList.add(row);
			}
			
			List<RecordMatch.Result> resultSet= doDiff.compare(lRecordList, rRecordList, m1, m1);
			 _rt = displayRecord(resultSet , _type); // 0 for match
			
			} catch (Exception ee) {
				System.out.println("Exeption:"+ee.getMessage());
				ee.printStackTrace();
			} finally {
				d_m.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				 if (_mapCancel == true) 
				 		return; // cancel from Merge Map
				d_recHead.dispose();
			}
			d_m.dispose();
			
			// Now pass the results for display
				d_r = new JDialog();
				d_r.setTitle("Record Match Dialog");
				d_r.setLocation(250, 100);
				if (_type == 2 ) // Merge Type
					d_r.getContentPane().add(mergePanel());
				else
					d_r.getContentPane().add(_rt);
				d_r.pack();
				d_r.setVisible(true);
		} // end of compare
		
	} // End of action performed
	
	// This function will be used to display matched records in ReportTable
	private ReportTable displayRecord(List<RecordMatch.Result> resultSet , int type) {
		
		// Match create ReportTable with Master ( left table columns )
		if (type == 0) {
			String[] newColName;
			if (_singleFile == true)
			 newColName = new String[_lCols.size() + 1]; // 1 for Matched Indexed
			else
			newColName = new String[_lCols.size() + _rCols.size()+ 1]; // left and right column
			
			newColName[0] = "Matched Index";
			
			for (int i=0; i <_lCols.size(); i++ )
				newColName[i+1] =_lCols.get(i); 
			
			if (_singleFile == false) 
				for (int i=0; i <_rCols.size(); i++ )
					newColName[_lCols.size()+1+i] =_rCols.get(i); 
			
			_rt = new ReportTable(newColName, true, true);
			
			int rowCount = resultSet.size();
			int prev_leftI = -1; // keep track of last index
			
			for (int i=0; i < rowCount ; i++) {
				RecordMatch.Result  res = resultSet.get(i);
				if (res.isMatch() == false)
					continue; // Only displaying matched one
				
				int leftI = res.getLeftMatchIndex();
				int rightI = res.getRightMatchIndex();
				if (_singleFile == true && rightI == leftI)
					 continue; // single file same index means same record
				
				if ( leftI != prev_leftI) {
					if (_rt.getModel().getRowCount() > 1) // add empty row after each left index match
					_rt.addRow();
					
					List<String> row = res.getLeftMatchedRow();
					String[] newRow = new String[newColName.length];
					for (int j=0; j < newColName.length; j++) // Initialize 
						newRow[j] = "";
					
					if (_singleFile == false)
						newRow[0] = "Left File Index:" + leftI;
					else
						newRow[0] = "File Index:" + leftI; // Only One file
					
					for (int j =0; j < row.size(); j++)
						newRow[j+1] = row.get(j);
					_rt.addFillRow(newRow);
					prev_leftI = leftI;
					
				}
				
				List<String> row = res.getRightMatchedRow();
				
				String[] newRow = new String[newColName.length];
				for (int j=0; j < newColName.length; j++) // Initialize 
					newRow[j] = "";
				if (_singleFile == false)
					newRow[0] = "Right File Index:" + rightI;
				else 
					newRow[0] = "File Index:" + rightI;
				
				for (int j =0; j < row.size(); j++)
					newRow[newColName.length - _rCols.size() + j] = row.get(j);
				
				_rt.addFillRow(newRow);
			
		} } // type 0 - match
		
		// Linkage create ReportTable with both left and right tables.
		// Linkage will have union of columns for left table and right table
		else if (type == 4) { // Linkage type == 4 INNER JOIN 1:1
			String[] newColName = mergeColName();
			_rt = new ReportTable(newColName, true, true);
			HashMap<List<String>, List<String>> mappedRow = new HashMap<List<String>, List<String>>();
			mappedRow = StringMergeUtil.innerJoinResult(resultSet);
			if (mappedRow.isEmpty() == true) {
				System.out.println("1:1 Record Linkage set is empty");
			} else {
				for (Iterator<List<String>> a = mappedRow.keySet().iterator() ; a.hasNext(); ) {
					String[] newRow = new String[newColName.length];
					
					// Fill left row values
					int linkedColInd =0; int unlinkedColInd =0;
					List<String> leftrow = a.next();
					for (int j =0; j < leftrow.size(); j++) {
						if (_leftMap.contains(j) == true)  // it is linked column
							newRow[_lCols.size() -_leftMap.size() + (linkedColInd++) ] = leftrow.get(j);
						else // unlinked
							newRow[unlinkedColInd++] = leftrow.get(j);
					}

					// Fill right values
					linkedColInd =0; unlinkedColInd =0;
					List<String> rightrow = mappedRow.get(leftrow);
					
					for (int j =0; j < rightrow.size(); j++) {
						if (_rightMap.contains(j) == true)  { // it is linked column [leftValue-RightValue]
							newRow[_lCols.size() -_leftMap.size() + linkedColInd ] = 
									newRow[_lCols.size() -_leftMap.size() + linkedColInd ]+"-"+rightrow.get(j);;
							linkedColInd++;
						}
						else // unlinked
							newRow[_lCols.size() + (unlinkedColInd++)] = rightrow.get(j);
					}
					
					_rt.addFillRow(newRow);
					
				}
			}
			
			
		}
		else if (type == 1) { // Linkage type == Cartesian 1:N
		
			String[] newColName = mergeColName();
			_rt = new ReportTable(newColName, true, true);
			
			// Now fill the data
			int rowCount = resultSet.size();
			for (int i=0; i < rowCount ; i++) {
				RecordMatch.Result  res = resultSet.get(i);
				if (res.isMatch() == false)
					continue; // Only displaying matched one
				
				String[] newRow = new String[newColName.length];
				
				// Fill left row values
				int linkedColInd =0; int unlinkedColInd =0;
				List<String> row = res.getLeftMatchedRow();
				for (int j =0; j < row.size(); j++) {
					if (_leftMap.contains(j) == true)  // it is linked column
						newRow[_lCols.size() -_leftMap.size() + (linkedColInd++) ] = row.get(j);
					else // unlinked
						newRow[unlinkedColInd++] = row.get(j);
				}

				// Fill right values
				linkedColInd =0; unlinkedColInd =0;
				row = res.getRightMatchedRow();
				
				for (int j =0; j < row.size(); j++) {
					if (_rightMap.contains(j) == true)  { // it is linked column [leftValue-RightValue]
						newRow[_lCols.size() -_leftMap.size() + linkedColInd ] = 
								newRow[_lCols.size() -_leftMap.size() + linkedColInd ]+"-"+row.get(j);;
						linkedColInd++;
					}
					else // unlinked
						newRow[_lCols.size() + (unlinkedColInd++)] = row.get(j);
				}
				
				_rt.addFillRow(newRow);
			}
		} // End of linkage
		else if (type == 2) { // Merge type == 2
			int nonMapCount = 0;
			if (_singleFile == true)
				nonMapCount = _lCols.size() - _leftMap.size(); // diff will be not mapped for merge
			else
				nonMapCount = _lCols.size() - _leftMap.size() + _rCols.size() - _rightMap.size();
			if (nonMapCount > 0) {
				showNonMappedDialog();
				
				if ( _mapCancel == true ) 
					return null;
			}
			
			// get the mapping and non mapping details and pass it on the mergeColumns
			String[] colName = mergeColName();
			String[] newColName = new String[colName.length + 1]; // one column for golden copy info
			newColName[0] = "MergeType";
			for (int i=0; i < colName.length; i++)
				newColName[i+1] = colName[i];
			_rt = new ReportTable(newColName, true, true);
			
			// Now merge the records with old column 
			mergeResultSet(resultSet, colName.length);
		}
		return _rt;
	} // End of Display Record
	
	/* This function will take resultset and will use different 
	 * algorithm to find the golden copy of the result.
	 * Newer data will have more weight
	 * Repetitive data will have more weight.
	 * Larger data will have more weight.
	 * 
	 */
	private void mergeResultSet(List<RecordMatch.Result> resultSet, int newColLen) {
		int rowCount = resultSet.size();
		int prev_index = -1;
		List<String> leftrow = null;
		ArrayList<List<String>> oneMatchSet = new ArrayList<List<String>>();
		ArrayList<Integer> oneRecord = new ArrayList<Integer>();
		
		// Show optionDialog and take input
		int option = JOptionPane.showOptionDialog(null,
                "\"One Record in One Cluster\" will map a matched record to one and only one cluster \n" +
                "even it matches to many clusters.\n\n" +
                "Once merged results are displayed User can edit, add, remove records in cluster \n"+
                "and Refresh the table. Golden values will be recalculated. \n\n",
                "Record Mapping Input",JOptionPane.OK_OPTION,JOptionPane.INFORMATION_MESSAGE,null,
                new String[] {"One Record in One Cluster","One Record in Many Cluster"},
                new String("One Record in One Cluster")) ;
		
		for (int i=0; i < rowCount ; i++) {
			RecordMatch.Result  res = resultSet.get(i);
			if (res.isMatch() == false)
				continue; // Only displaying matched one
			
			int leftI = res.getLeftMatchIndex();
			int rightI = res.getRightMatchIndex();
			if (_singleFile == true && rightI == leftI)
				continue; // single file same index means same record

			// Create a record as per header and give for merge
			List<String> rightrow = res.getRightMatchedRow();
			
			if (prev_index == leftI && i != rowCount) { // last set should show mergeData
				if (option != 0 ) // One record One cluster
					oneMatchSet.add(rightrow);
				else {
					if (oneRecord.contains(rightI) == true ) // already there
						continue;
					oneMatchSet.add(rightrow);
					oneRecord.add(rightI);
				}
			} else { // send to merge function
				if (oneMatchSet.size() > 0 && leftrow != null) {
					ArrayList<String[]> newOrder = rearrange(leftrow,oneMatchSet,newColLen);
					String[] goldcopy = StringMergeUtil.getGoldenValue(newOrder,_actionType);
					// Add value of mergerType
					String[] newgoldCopy = new String[goldcopy.length +1];
					newgoldCopy[0] = "Golden Merge";
					for (int newval=0; newval <goldcopy.length ; newval++ )
						newgoldCopy[newval+1] = goldcopy[newval];
					_rt.addFillRow(newgoldCopy);
					
					for (int listsize = 0; listsize < newOrder.size();  listsize++ ) {
						String[] a = newOrder.get(listsize);
						String[] newa = new String[a.length +1];
						newa[0] = "Matched Record";
						for (int newval=0; newval <a.length ; newval++ )
							newa[newval+1] = a[newval];
						_rt.addFillRow(newa);
					}
					_rt.addRow(); // add empty row	
				}
				prev_index = leftI;
				oneMatchSet.removeAll(oneMatchSet);
				leftrow = res.getLeftMatchedRow();
				
			}
		}
	} // end of MergeResult
	
	// This function will return a set of matched record in the same
	// order of header - liked linked record
	
	private ArrayList<String[]> rearrange(List<String> leftrow, ArrayList<List<String>> rightRows, int newColLen) {
		ArrayList<String[]> matchedSingleSet = new ArrayList<String[]>();
		// Fill left row values
		int linkedColInd =0; int unlinkedColInd =0;
		
		String[] newRow = new String[newColLen];
		for (int j =0; j < leftrow.size(); j++) {
			if (_leftMap.contains(j) == true)  // it is linked column
				newRow[_lCols.size() -_leftMap.size() + (linkedColInd++) ] = leftrow.get(j);
			else // unlinked
				newRow[unlinkedColInd++] = leftrow.get(j);
		}
		matchedSingleSet.add(newRow);
		
		// Fill right values
		for (int i=0; i <rightRows.size(); i++  ) {
			newRow = new String[newColLen];
			List<String> rightrow = rightRows.get(i);
			linkedColInd =0; unlinkedColInd =0;
			for (int j =0; j < rightrow.size(); j++) {
				if (_rightMap.contains(j) == true)  { // it is linked column [leftValue-RightValue]
					newRow[_lCols.size() -_leftMap.size() + linkedColInd ] = rightrow.get(j);;
					linkedColInd++;
				}
				else  { // unlinked
					if (_singleFile == true)
						newRow[unlinkedColInd++] = rightrow.get(j);
					else
						newRow[_lCols.size() + (unlinkedColInd++)] = rightrow.get(j);
				}
			}
			matchedSingleSet.add(newRow);
		}
		return matchedSingleSet;
	}
	
	/* this function will create colheader of linked recordset
	 * 
	 */
	private String[] mergeColName() {
		
		if (_leftMap.size() != _rightMap.size() ) {
			ConsoleFrame.addText("\n WARNING:Mapping for Record Merge is not correct.");
		}
		int linkedColCount = _leftMap.size(); // linked column count
		int linkedColInd =0,unlinkedColInd =0; // keep index of columns
		int colLen = 0;
		if (_singleFile == true)
			colLen = _lCols.size();
		else
			colLen = _lCols.size() + _rCols.size() - linkedColCount ;
		
		String[] newColName = new String [colLen];
		_actionType = new Integer[colLen]; // it will hold action type
		
		// Create col for left table
		for (int i=0 ; i < _lCols.size(); i++ )  { // fill left column fist
			if (_leftMap.contains(i) == true)  { // it is linked column
				newColName[_lCols.size() -_leftMap.size() + linkedColInd ] = _lCols.get(i);
				_actionType[_lCols.size() -_leftMap.size() + linkedColInd ] = 100; //100 for merged type
				linkedColInd++;
			}
			else { // unlinked
				newColName[unlinkedColInd] = _lCols.get(i);
				if (_dataActionNonMap != null)
				_actionType[unlinkedColInd] = _dataActionNonMap.get(unlinkedColInd);
				unlinkedColInd++;	
			}
		}
		
		if (_singleFile == false) {
		// Create column for right table
		int totalIndex = unlinkedColInd;
		linkedColInd =0; unlinkedColInd =0;
		
		
		for (int i=0 ; i < _rCols.size(); i++ )  { // fill right  column name
			if (_rightMap.contains(i) == true)  { // it is linked column name should [lefttable-righttable]
				newColName[_lCols.size() -_leftMap.size() + linkedColInd ] = 
						newColName[_lCols.size() -_leftMap.size() + linkedColInd ]+"-"+_rCols.get(i);
				linkedColInd++;
			}
			else { // unlinked
				newColName[_lCols.size() + unlinkedColInd] = _rCols.get(i);
				if (_dataActionNonMap != null)
				_actionType[_lCols.size() + unlinkedColInd] = _dataActionNonMap.get(totalIndex++);
				unlinkedColInd++;	
			}
		}
		} // need right Column only for multiple File
		
		return newColName;
	}
	
	/* This function will pop up non mapped columns and will take input
	 * of what to do with those columns
	 */
	private JDialog showNonMappedDialog() {
		JPanel jp = new JPanel(new SpringLayout());
		int nonMapCount = 0;
		if (_singleFile == true)
			nonMapCount = _lCols.size() - _leftMap.size(); // diff will be not mapped for merge
		else
			nonMapCount = _lCols.size() - _leftMap.size() + _rCols.size() - _rightMap.size();

		String[] dataAction= new String[]{"Ignore","Take Any","Most Common","Sum","Count","Min","Max","Average"};
		_dataActionC = new JComboBox[nonMapCount];
		int index = 0; // keep the counter
		
		for (int i=0; i < _lCols.size(); i++) {
			if (_leftMap.contains(i) == true)
					continue; // It is mapped
			JLabel colLabel = new JLabel(_lCols.get(i),JLabel.TRAILING);
			jp.add(colLabel);
			JLabel mapA = new JLabel("   Merge Action:   ",JLabel.TRAILING);
			mapA.setForeground(Color.BLUE);
			jp.add(mapA);
			_dataActionC[index] = new JComboBox<String>(dataAction);
			if (_singleFile == false)
				_dataActionC[index].setSelectedItem("Take Any");
			else
				_dataActionC[index].setSelectedItem("Most Common");
			jp.add(_dataActionC[index]);
			index++;
		}
		
		if (_singleFile == false)
			for (int i=0; i < _rCols.size(); i++) {
				if (_rightMap.contains(i) == true)
						continue; // It is mapped
				JLabel colLabel = new JLabel("Second File:"+_rCols.get(i),JLabel.TRAILING);
				jp.add(colLabel);
				JLabel mapA = new JLabel("   Action:   ",JLabel.TRAILING);
				mapA.setForeground(Color.BLUE);
				jp.add(mapA);
				_dataActionC[index] = new JComboBox<String>(dataAction);
				_dataActionC[index].setSelectedItem("Most Common");
				jp.add(_dataActionC[index]);
				index++;
			}
			
		
		SpringUtilities.makeCompactGrid(jp, nonMapCount, 3, 3, 3, 3, 3);
		JScrollPane jscrollpane1 = new JScrollPane(jp);
		if ((100 + (nonMapCount * 35)) > 500)
			jscrollpane1.setPreferredSize(new Dimension(675, 400));
		else
			jscrollpane1.setPreferredSize(new Dimension(675, 75 + nonMapCount * 35));
		
		JPanel bp = new JPanel();
		JButton ok = new JButton("Continue");;
		ok.setActionCommand("Continue");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancelNonMap");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		JPanel jp_p = new JPanel(new BorderLayout());
		jp_p.add(jscrollpane1, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		d_nonMap = new JDialog();
		d_nonMap.setModal(true);
		d_nonMap.setTitle("NonMerged Column Action Dialog");
		_mapCancel = false; // reset to false
		d_nonMap.setLocation(250, 100);
		d_nonMap.getContentPane().add(jp_p);
		d_nonMap.pack();
		d_nonMap.setVisible(true);

		return d_nonMap;
	}
	
	// This function will recalculate the merged table 
	// and give new golden values
	private ReportTable recalMergeTable(ReportTable newRT) {
		
		if (_rt.isSorting() || _rt.table.isEditing()) {
			JOptionPane.showMessageDialog(null, "Table  is in Sorting or Editing State \\n Can not Refresh");
			return null;
		}
		newRT = new ReportTable(_rt.getAllColName(), true, true);
		int rowC = _rt.table.getRowCount();
		int colC = _rt.table.getColumnCount();
		boolean isClusterStart = false;
		
		ArrayList<String[]> oneMatchSet = new ArrayList<String[]>();
		
		for (int i=0; i < rowC; i++) {
			if (_rt.getTextValueAt(i, 0).equals("Golden Merge") == true) {
				isClusterStart = true;
				continue;
			}
			
			if (_rt.getTextValueAt(i, 0) == null ||
				_rt.getTextValueAt(i, 0).equals("") == true || i == (rowC -1)) {  // empty row or last row
				isClusterStart = false;
			}
			
			String[] row = new String[colC -1];  // Merge Type not required
			for (int j=1; j< colC; j++)
				row[j-1] = _rt.getTextValueAt(i, j);
			
		if (isClusterStart) { 
				oneMatchSet.add(row);
		} else { // send to merge function
			if (i == (rowC -1))
				oneMatchSet.add(row); // Boundary condition
			
			if (oneMatchSet.size() > 0 ) {
				String[] goldcopy = StringMergeUtil.getGoldenValue(oneMatchSet,_actionType);
				// Add value of mergerType
				String[] newgoldCopy = new String[goldcopy.length +1];
				newgoldCopy[0] = "Golden Merge";
				for (int newval=0; newval <goldcopy.length ; newval++ )
					newgoldCopy[newval+1] = goldcopy[newval];
				newRT.addFillRow(newgoldCopy);
				
				for (int listsize = 0; listsize < oneMatchSet.size();  listsize++ ) {
					String[] a = oneMatchSet.get(listsize);
					String[] newa = new String[a.length +1];
					newa[0] = "Matched Record";
					for (int newval=0; newval <a.length ; newval++ )
						newa[newval+1] = a[newval];
					newRT.addFillRow(newa);
				}
				newRT.addRow(); // add empty row	
			}
			
			// new Cluester Starting so clear it
			oneMatchSet.removeAll(oneMatchSet);
		}
		
		} // End of for loop
		
		return newRT;
	}
	
	private JPanel mergePanel() {
		JPanel jp = new JPanel ( new BorderLayout() );
		jp.add(_rt,BorderLayout.CENTER);
		
		JPanel bp = new JPanel();
		
		refreshB = new JButton("Refresh"); // will refresh the merge table
		refreshB.setActionCommand("refresh");
		refreshB.addActionListener(this);
		refreshB.addKeyListener(new KeyBoardListener());
		bp.add(refreshB);
		
		pushgoldenB = new JButton("Push Golden Value"); // will push golden Value to all relevant records
		pushgoldenB.setActionCommand("pushgolden");
		pushgoldenB.addActionListener(this);
		pushgoldenB.addKeyListener(new KeyBoardListener());
		bp.add(pushgoldenB);
		
		JButton golden = new JButton("Golden Value"); // will save golden Value
		golden.setActionCommand("golden");
		golden.addActionListener(this);
		golden.addKeyListener(new KeyBoardListener());
		bp.add(golden);
		
		
		jp.add(bp,BorderLayout.PAGE_END);
		
		return jp;
	}
}
