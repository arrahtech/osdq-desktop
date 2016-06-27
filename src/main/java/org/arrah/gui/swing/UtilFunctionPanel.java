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

/* This file is uses to integrated utility functions
 * of expression builder
 * 
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.arrah.framework.datagen.TimeUtil;
import org.arrah.framework.util.StringCaseFormatUtil;


public class UtilFunctionPanel implements ActionListener, ItemListener {
	private ReportTable _rt;
	private int _rowC = 0;
	private int _colIndex = 0;
	private JDialog d_f;
	private JFormattedTextField jrn_low, jrn_high,split_low,split_high,splitStringw;
	private JRadioButton rd1, rd2, rd3, rd4, leftrd, rightrd, rd5, rd6, rd7, rd8, rd9, leftrdw,rightrdw;
	private JComboBox<String> colSel;
	private Border line_b;
	private int beginIndex, endIndex;
	private JLabel colType;
	private JTextField splitString,splitString_sub,splitString_meta,splitString_char,searchString,replaceString;
	private JCheckBox startM, endM, inBetweenM, startC, endC, inBetweenC;
	private JRadioButton firstrd, allrd;

	
	public UtilFunctionPanel(ReportTable rt, int colIndex) {
		_rt = rt;
		_colIndex = colIndex;
		_rowC = rt.table.getRowCount();
		createDialog();
	}; // Constructor
	

	private void createDialog() {
		JPanel jp = new JPanel(new BorderLayout());
		line_b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

		rd1 = new JRadioButton("Split String");
		rd5 = new JRadioButton("Split Into SubString");
		rd4 = new JRadioButton("Reversse String");
		rd6 = new JRadioButton("Remove Special Character");
		rd7 = new JRadioButton("Remove Charater(s)");
		rd8 = new JRadioButton("Replace String");
		rd2 = new JRadioButton("Epoch MilliSecond to Date ");
		rd3 = new JRadioButton("Date to Epoch MilliSecond");
		rd9 = new JRadioButton("Split String by Length");
		ButtonGroup bg = new ButtonGroup();
		bg.add(rd1);bg.add(rd2);bg.add(rd3);bg.add(rd4);bg.add(rd5);bg.add(rd6);bg.add(rd7);bg.add(rd8);
		bg.add(rd9);
		rd9.setSelected(true);

		jp.add(createSelectionPanel(),BorderLayout.NORTH);
		
		JPanel header = new JPanel(new GridLayout(9,1));
		header.add(createSplitPanelWidth());
		header.add(createSplitPanel());
		header.add(createSubSplitPanel());
		header.add(createReversePanel());
		header.add(createMetadataPanel());
		header.add(createCharacterPanel());
		header.add(createStringPanel());
		header.add(createDateToSecondPanel() );
		header.add(createSecondToDatePanel());
		jp.add(header,BorderLayout.CENTER);

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		JPanel bottom = new JPanel(new GridLayout(2,1));
		bottom.add(createRowNumPanel());bottom.add(bp);
		
		jp.add(bottom,BorderLayout.SOUTH);

		d_f = new JDialog();
		d_f.setModal(true);
		d_f.setTitle("Utility Function Dialog");
		d_f.setLocation(300, 250);
		d_f.setPreferredSize(new Dimension(820,450)); // some column names may be long
		d_f.getContentPane().add(jp);
		d_f.pack();
		d_f.setVisible(true);

	}

	/* User can choose multiple options / utilities to split and t
	 * transform the string  */
	private JPanel createSplitPanel() {
		
		JPanel splitjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		splitjp.add(rd1);
		
		splitjp.setBorder(line_b);
		splitString = new JTextField();
		splitString.setText("separator");
		splitString.setColumns(10);
		splitjp.add(splitString);
		
		leftrd = new JRadioButton("Left Value");
		rightrd = new JRadioButton("Right Value");
		leftrd.setSelected(true);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(leftrd); bg.add(rightrd);
		splitjp.add(leftrd) ;splitjp.add(rightrd) ;
		return splitjp;
	}
	
	private JPanel createSubSplitPanel() {
		
		JPanel splitjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		splitjp.add(rd5);
		
		splitjp.setBorder(line_b);
		splitString_sub = new JTextField();
		splitString_sub.setText("separator");
		splitString_sub.setColumns(10);
		splitjp.add(splitString_sub);
		split_low = new JFormattedTextField();
		split_low.setValue(new Long(1));
		split_low.setColumns(8);
		split_low.setToolTipText("From Split Column ID:");
		split_high = new JFormattedTextField();
		split_high.setValue(new Long(2));
		split_high.setColumns(8);
		split_high.setToolTipText("To Split Column ID:");
		
		splitjp.add(split_low) ;splitjp.add(split_high) ;
		return splitjp;
	}
	private JPanel createMetadataPanel() {
		
		JPanel splitjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		splitjp.add(rd6);
		splitjp.setBorder(line_b);
		JLabel jl = new JLabel(" Except:");
		splitjp.add(jl);
		splitString_meta = new JTextField();
		splitString_meta.setText("_");
		splitString_meta.setColumns(10);
		splitjp.add(splitString_meta);
		
		startM = new JCheckBox("From Start");
		startM.setSelected(true);
		endM = new JCheckBox("From End");
		inBetweenM = new JCheckBox("In Between");
		splitjp.add(startM);splitjp.add(endM);splitjp.add(inBetweenM);
		
		return splitjp;
	}
	
	private JPanel createCharacterPanel() {
		
		JPanel splitjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		splitjp.add(rd7);
		splitjp.setBorder(line_b);
		JLabel jl = new JLabel(" Character(s):");
		splitjp.add(jl);
		splitString_char = new JTextField();
		splitString_char.setText("#$");
		splitString_char.setColumns(10);
		splitjp.add(splitString_char);
		
		startC = new JCheckBox("From Start");
		startC.setSelected(true);
		endC = new JCheckBox("From End");
		inBetweenC = new JCheckBox("In Between");
		splitjp.add(startC);splitjp.add(endC);splitjp.add(inBetweenC);
		
		return splitjp;
	}
	
	private JPanel createStringPanel() {
		
		JPanel splitjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		splitjp.add(rd8);
		splitjp.setBorder(line_b);
		JLabel jl = new JLabel("Search");
		splitjp.add(jl);
		searchString = new JTextField();
		searchString.setText("search");
		searchString.setColumns(10);
		splitjp.add(searchString);
		
		JLabel j2 = new JLabel("Replace");
		splitjp.add(j2);
		replaceString = new JTextField();
		replaceString.setText("replace");
		replaceString.setColumns(10);
		splitjp.add(replaceString);
		
		firstrd = new JRadioButton("Only First");
		firstrd.setSelected(true);
		allrd = new JRadioButton("All");
		ButtonGroup bg = new ButtonGroup();
		bg.add(firstrd);bg.add(allrd);
		
		splitjp.add(firstrd);splitjp.add(allrd);
		
		return splitjp;
	}
	private JPanel createRowNumPanel() {
		JPanel rownnumjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel lrange = new JLabel("  Row Numbers to Populate : From (Inclusive)", JLabel.LEADING);
		jrn_low = new JFormattedTextField();
		jrn_low.setValue(new Long(1));
		jrn_low.setColumns(8);
		JLabel torange = new JLabel("  To(Exclusive):", JLabel.LEADING);
		jrn_high = new JFormattedTextField();
		jrn_high.setValue(new Long(_rowC+1));
		jrn_high.setColumns(8);
		
		rownnumjp.add(lrange);
		rownnumjp.add(jrn_low);
		rownnumjp.add(torange);
		rownnumjp.add(jrn_high);
		rownnumjp.setBorder(line_b);
		return rownnumjp;
	}
	
	private JPanel createReversePanel() {
		JPanel reversejp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		reversejp.add(rd4);
		reversejp.setBorder(line_b);
		return reversejp;
	}
	
	private JPanel createSecondToDatePanel() {
		JPanel datejp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		datejp.add(rd2);
		datejp.setBorder(line_b);
		return datejp;
	}

	private JPanel createDateToSecondPanel() {
		JPanel datejp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		datejp.add(rd3);
		datejp.setBorder(line_b);
		return datejp;
	}

	private JPanel createSelectionPanel() {
		JPanel selectionjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel selL = new JLabel("Choose Column to Apply Function on:     ");
		int colC = _rt.getModel().getColumnCount();
		String[] colName = new String[colC];
		
		for (int i = 0; i < colC; i++) 
			colName[i] = _rt.getModel().getColumnName(i);
		
		selectionjp.add(selL);
		colSel = new JComboBox<String>(colName);
		colSel.addItemListener(this);
		selectionjp.add(colSel);
		colType = new JLabel("        "+_rt.getModel().getColumnClass(0).getName());
		selectionjp.add(colType);
		return selectionjp;
	}
	
	private JPanel createSplitPanelWidth() {
		
		JPanel splitjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		splitjp.add(rd9);
		
		splitjp.setBorder(line_b);
		splitStringw = new JFormattedTextField();
		splitStringw.setValue(new Integer(5));
		splitStringw.setColumns(10);
		splitjp.add(splitStringw);
		
		leftrdw = new JRadioButton("Left Value");
		rightrdw = new JRadioButton("Right Value");
		leftrdw.setSelected(true);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(leftrdw); bg.add(rightrdw);
		splitjp.add(leftrdw) ;splitjp.add(rightrdw) ;
		return splitjp;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("cancel")) {
			d_f.dispose();
			return;
		}
		if (action.equals("ok")) {
			// d_f.dispose(); do not dispose now
			if (_rt.isSorting() || _rt.table.isEditing()) {
				JOptionPane.showMessageDialog(null, "Table is in Sorting or Editing State");
				return;
			}
			beginIndex = ((Long) jrn_low.getValue()).intValue();
			endIndex = ((Long) jrn_high.getValue()).intValue();
			if (beginIndex <= 0 || beginIndex > _rowC)
				beginIndex = 1;
			if (endIndex <= 0 || endIndex > (_rowC + 1))
				endIndex = _rowC +1;
			
			int numGenerate = endIndex - beginIndex;
			if ( numGenerate <= 0 || numGenerate > _rowC) {
				numGenerate = _rowC; // default behavior for Invalid number
				beginIndex = 1;endIndex = _rowC+1;
			}
			try {
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			
			
			if (rd4.isSelected() == true) {
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 String revString="";
					 for (int curIndex = colObject.toString().length()-1; curIndex >= 0; curIndex-- ) 
						 revString += colObject.toString().charAt(curIndex);
					 _rt.getModel().setValueAt(revString, i, _colIndex);
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
				return;
			} // end of Reverse
			if (rd1.isSelected() == true) {
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				String regexStr = splitString.getText();
				if (regexStr == null || "".equals(regexStr) == true) {
					JOptionPane.showMessageDialog(null, "Split value is not valid ", 
						"Split Type Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 String[] colVal = StringCaseFormatUtil.splitColString (colObject.toString(),regexStr) ;
					 if (leftrd.isSelected() == true)
						 _rt.getModel().setValueAt(colVal[0], i, _colIndex);
					 else if ( colVal.length > 1 && colVal[1] != null )
						 _rt.getModel().setValueAt(colVal[1], i, _colIndex);
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
				return;
			} // end of Split
			if (rd9.isSelected() == true) {
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				Integer strlen = (Integer)splitStringw.getValue();
				if (strlen == null || strlen < 0) {
					JOptionPane.showMessageDialog(null, "Split Width value is not valid ", 
						"Split Type Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 String[] colVal = StringCaseFormatUtil.splitColStringWidth (colObject.toString(),strlen) ;
					 if (leftrdw.isSelected() == true)
						 _rt.getModel().setValueAt(colVal[0], i, _colIndex);
					 else if ( colVal[1] != null && colVal.length > 1 )
						 _rt.getModel().setValueAt(colVal[1], i, _colIndex);
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
				return;
			} // end of Width Split
			if (rd5.isSelected() == true) {
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				String regexStr = splitString_sub.getText();
				if (regexStr == null || "".equals(regexStr) == true) {
					JOptionPane.showMessageDialog(null, "Split value is not valid ", 
						"Split Type Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// Validate Index
				int startI = ((Long) split_low.getValue()).intValue();
				int endI = ((Long) split_high.getValue()).intValue();
				
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 String[] colVal = StringCaseFormatUtil.splitColSubString (colObject.toString(),regexStr) ;
					 if ( (startI > endI )  || ( startI > colVal.length) ) // need more than string
						 _rt.getModel().setValueAt(colVal.toString(), i, _colIndex);
					 else {
						 if (endI > colVal.length )
							 endI = colVal.length; // -1 for index
						 String newString="";
						 for (int newI=startI; newI < endI; newI++)
							 newString += colVal[newI]+splitString.getText();
						 _rt.getModel().setValueAt(newString, i, _colIndex);
					 }
						 
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
				return;
			} // end of Split SubString
			if (rd2.isSelected() == true) { //Epoch Millisecond to Date
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				
				
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
						 if (colObject instanceof Long) {
								d_f.dispose(); // now dispose
						} else {
							try {
								colObject = new Double(Double.parseDouble(colObject.toString())).longValue();
							} catch(Exception forexp) {
								ConsoleFrame.addText("\n Input Value is not in Number format :" + colObject.toString());
								continue;
							}
						}
					Date colVal =  TimeUtil.secondIntoDate((Long)colObject);
					_rt.getModel().setValueAt(colVal, i, _colIndex);
					
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
				return;
			} // end of date to Epoch
			
			if (rd3.isSelected() == true) { // Date to Epoch Millisecond
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				
				// Check if it date type
				boolean dateValidated = false;
				
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 if (dateValidated == false) {
						 if (colObject instanceof java.util.Date) {
								d_f.dispose(); // now dispose
								dateValidated = true;
						} else {
							ConsoleFrame.addText("\n Input String is not in Date format");
								JOptionPane.showMessageDialog(null, "Input is not of Date Type\n Please format to date type ", 
										"Date Type Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					 }
					 
					long colVal = TimeUtil.dateIntoSecond((Date)colObject); 
					_rt.getModel().setValueAt(colVal, i, _colIndex);
					
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
				return;
			} // end of date to Epoch
			if (rd6.isSelected() == true) {
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				boolean start = startM.isSelected();
				boolean end = endM.isSelected();
				boolean inBet = inBetweenM.isSelected();
				
				if ( start == false &&  end == false && inBet == false) {
					JOptionPane.showMessageDialog(null, "Select atleast one chechBox option");
					return;
				}
				 String skipString = splitString_meta.getText();
				 if (skipString == null) skipString = ""; // To avoid null pointer exception
				 
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 String oldString = colObject.toString();
					 String newString="";
					 newString = StringCaseFormatUtil.removeMetaCharString(oldString,skipString,start,inBet,end);
					 _rt.getModel().setValueAt(newString, i, _colIndex);
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
				return;
			} // end of Meta Character
			if (rd7.isSelected() == true) {
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				boolean start = startC.isSelected();
				boolean end = endC.isSelected();
				boolean inBet = inBetweenC.isSelected();
				
				if ( start == false &&  end == false && inBet == false) {
					JOptionPane.showMessageDialog(null, "Select atleat one chechBox option");
					return;
				}
				 String skipString = splitString_char.getText();
				 if (skipString == null || "".equals(skipString)) { 
					 JOptionPane.showMessageDialog(null, "Select Character to remove");
					 return; // To avoid null pointer exception
				 }
				 
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 String oldString = colObject.toString();
					 String newString="";
					 newString = StringCaseFormatUtil.removeCharacterString(oldString,skipString,start,inBet,end);
					 _rt.getModel().setValueAt(newString, i, _colIndex);
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
				return;
			} // end of Character removal
			
			if (rd8.isSelected() == true) {
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				boolean start = firstrd.isSelected();
				
				 String serString = searchString.getText();
				 String repString = replaceString.getText();
				 
				 if (serString == null || "".equals(serString)) { 
					 JOptionPane.showMessageDialog(null, "Select String to be Matched");
					 return; // To avoid null pointer exception
				 }
				 
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 String oldString = colObject.toString();
					 String newString="";
					 newString = StringCaseFormatUtil.replaceString(oldString, serString, repString,start);
					 _rt.getModel().setValueAt(newString, i, _colIndex);
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
				return;
			} // end of String removal
			
			return;
			} finally {
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
		} // Ok action
	} // End of actionPerformed
	
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED ) {
			if (e.getSource() == colSel) {
			int index = colSel.getSelectedIndex();
			colType.setText("        "+_rt.getModel().getColumnClass(index).getName());
			} 
		}
	} // End of ItemStateChange
}
