package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2013      *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for grouping date and number
 * fields. This file will also provide
 * option to select the grouping type
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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.arrah.framework.datagen.TimeUtil;


public class GroupingPanel implements ActionListener, ItemListener {
	private ReportTable _rt;
	private int _rowC = 0;
	private int _colIndex = 0;
	private JDialog d_f;
	private JFormattedTextField jrn_low, jrn_high;
	private JRadioButton rd1, rd3, rd2;
	private JComboBox<String> colSel, timeGroup, timeFormat;
	private Border line_b;
	private int beginIndex, endIndex;
	private JLabel colType, dateInfo;
	private Vector<JTextField> grpName_v,otherValue_v,grpName_vS;
	private Vector<JFormattedTextField>jft_low_v, jft_high_v;
	private Vector<JSpinner>jft_low_vS, jft_high_vS;
	private JPanel cp,cps; // for adding more row for number grouping
	private Vector<Boolean> validNum_bv,validDate_bvS;
	private Vector<Vector<Double>> otherNum_v;
	
	private int capacity, capacityS;
	
	public GroupingPanel(ReportTable rt, int colIndex) {
		_rt = rt;
		_colIndex = colIndex;
		_rowC = rt.table.getRowCount();
		createDialog();
	}; // Constructor
	

	private void createDialog() {
		JPanel jp = new JPanel(new BorderLayout());
		line_b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

		rd1 = new JRadioButton("Number");
		rd2 = new JRadioButton("Seasonality");
		rd3 = new JRadioButton("Date");
		ButtonGroup bg = new ButtonGroup();
		bg.add(rd1);bg.add(rd2);bg.add(rd3);
		
		rd3.setSelected(true);

		JPanel header = new JPanel(new GridLayout(2,1));
		header.add(createSelectionPanel());header.add(createDatePanel());
		jp.add(header,BorderLayout.NORTH);

		JPanel body = new JPanel(new GridLayout(2,1));
		body.add(createSeasonPanel());body.add(createNumPanel());
		jp.add(body,BorderLayout.CENTER);

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
		d_f.setTitle("Grouping  Dialog");
		d_f.setLocation(100, 100);
		d_f.setPreferredSize(new Dimension(950,850));
		d_f.getContentPane().add(jp);
		d_f.pack();
		d_f.setVisible(true);

	}

	/* User can choose multiple options to group */
	private JPanel createNumPanel() {
		
		grpName_v = new Vector<JTextField>();
		jft_low_v = new Vector<JFormattedTextField>();
		jft_high_v = new Vector<JFormattedTextField>();
		otherValue_v = new Vector<JTextField>();
		
		JPanel numjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		numjp.add(rd1);
		numjp.setBorder(line_b);
		
		JButton addcol = new JButton("Add Row");
		addcol.setActionCommand("addcol");
		addcol.addActionListener(this);
		addcol.addKeyListener(new KeyBoardListener());
		cp = new JPanel(new SpringLayout());
		for (int i = 0; i < 8; i++) // create default 8 col Name
			addRow();

		SpringUtilities.makeCompactGrid(cp, capacity, 8, 3, 3, 3, 3);
		JScrollPane jscrollpane1 = new JScrollPane(cp);
		jscrollpane1.setPreferredSize(new Dimension(850, 300));
		
		numjp.add(addcol);
		numjp.add(jscrollpane1);
		
		return numjp;
	}
	
	/* User can choose multiple options to group date so that */
	private JPanel createSeasonPanel() {
		
		grpName_vS = new Vector<JTextField>();
		jft_low_vS = new Vector<JSpinner>();
		jft_high_vS = new Vector<JSpinner>();
				
		JPanel numjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		numjp.add(rd2);
		numjp.setBorder(line_b);
		
		JButton addcol = new JButton("Add Row");
		addcol.setActionCommand("addcolseason");
		addcol.addActionListener(this);
		addcol.addKeyListener(new KeyBoardListener());
		cps = new JPanel(new SpringLayout());
		for (int i = 0; i < 8; i++) // create default 8 col Name
			addSeasonRow();

		SpringUtilities.makeCompactGrid(cps, capacityS, 6, 3, 3, 3, 3);
		JScrollPane jscrollpane1 = new JScrollPane(cps);
		jscrollpane1.setPreferredSize(new Dimension(850, 250));
		
		numjp.add(addcol);
		numjp.add(jscrollpane1);
		
		return numjp;
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
	private JPanel createDatePanel() {
		JPanel datejp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		datejp.add(rd3);
		timeGroup = new JComboBox<String>(new String[] {"Year","Year - Qrt","Qrt","Year - Month","Month",
				"Day","Hour","Half Hour","15 Min","10 Min","5 Min","Min","Week of Year","Weekend"});
		timeGroup.addItemListener(this);
		datejp.add(timeGroup);
		dateInfo = new JLabel();
		datejp.add(dateInfo);
		timeFormat = new JComboBox<String>(new String[] {"Choose"});
		timeFormat.setEnabled(false);
		datejp.add(timeFormat);
		datejp.setBorder(line_b);
		
		return datejp;
	}


	private JPanel createSelectionPanel() {
		JPanel selectionjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel selL = new JLabel("Choose Column to Group From:     ");
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

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("cancel")) {
			d_f.dispose();
			return;
		}
		if (action.equals("addcol")) {
			addRow();
			cp.setLayout(new SpringLayout());
			SpringUtilities.makeCompactGrid(cp, capacity, 8, 3, 3, 3, 3);
			cp.revalidate();
			return;
		}
		if (action.equals("addcolseason")) {
			addSeasonRow();
			cps.setLayout(new SpringLayout());
			SpringUtilities.makeCompactGrid(cps, capacityS, 6, 3, 3, 3, 3);
			cps.revalidate();
			return;
		}
		if (action.equals("ok")) {
			// d_f.dispose(); do not dispose now
			if (_rt.isSorting() || _rt.table.isEditing()) {
				JOptionPane.showMessageDialog(null, "Please Cancel Editing or Sorting. Then proceed");
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
			
			if (rd1.isSelected() == true) {
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				// Check if it date type
				boolean numValidated = false;
				
				validateNumberInput();
				
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 if (numValidated == false) {
						 if (colObject instanceof Number) {
								d_f.dispose(); // now dispose
								numValidated = true;
						} else {
							ConsoleFrame.addText("\n Input String is not in Number format");
								JOptionPane.showMessageDialog(null, "Input is not of Number Type\n Please format to Number type ", 
										"Number Type Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					 }
					 String colVal = "";
					 for (int j=0; j < capacity; j++) {
							if (validNum_bv.get(j) == true) {
								Double minval = (Double) jft_low_v.get(j).getValue();
								Double maxval = (Double) jft_high_v.get(j).getValue();
								if ((Double)colObject >= minval && (Double) colObject <maxval) {
									colVal = grpName_v.get(j).getText();
									break; // break the loop and setValue then get next value
								}
								Vector<Double> list_v= otherNum_v.get(j);
								if (list_v.contains((Double)colObject) == true) {
									colVal = grpName_v.get(j).getText();
									break; // break the loop and setValue then get next value
								}
								
							} else continue; // not valid value	
					}
					_rt.getModel().setValueAt(colVal, i, _colIndex);
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
			} // end of Number
			if (rd2.isSelected() == true) {
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				// Check if it date type
				boolean dateValidated = false;
				
				validateDateInput();
				
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 if (dateValidated == false) {
						 if (colObject instanceof java.util.Date) {
								d_f.dispose(); // now dispose
								dateValidated = true;
						} else {
							ConsoleFrame.addText("\n Input String is not in Date format");
								JOptionPane.showMessageDialog(null, "Input is not of Date Type\n Please format to Date type ", 
										"Date Type Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					 }
					 String colVal = "";
					 for (int j=0; j < capacityS; j++) { // First match will break
						if (validDate_bvS.get(j) == true) {
							Date mindate = (Date) jft_low_vS.get(j).getValue();
							Date maxdate = (Date) jft_high_vS.get(j).getValue();
							
							if ( ((Date)colObject).compareTo(mindate) >= 0 && ((Date)colObject).before(maxdate)) {
								colVal = grpName_vS.get(j).getText();
								break; // break the loop and setValue then get next value
							}
							
						} else continue; // not valid value	
					}
					_rt.getModel().setValueAt(colVal, i, _colIndex);
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
			} // end of Seasonality
			if (rd3.isSelected() == true) { // Date Type
				
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				int grpType = timeGroup.getSelectedIndex();
				int format=0; // How to format show data
					// need to take input and pass right value
					switch (grpType) {
					case 1: case 2:
						format = TimeUtil.monthCalValue(timeFormat.getSelectedItem().toString());
						break;
					case 12:
						format = TimeUtil.weekCalValue(timeFormat.getSelectedItem().toString());
						break;
					case 6 :case 7 :case 8 :case 9 :case 10 :case 11 :
						format = timeFormat.getSelectedIndex();
						break;
					default:
						break;	
					}
				
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
					 
					String colVal = TimeUtil.timeValue((Date)colObject, grpType, format); 
					_rt.getModel().setValueAt(colVal, i, _colIndex);
					
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
			} // end of date option
			
			return;
		} // Ok action
	} // End of actionPerformed
	
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED ) {
			if (e.getSource() == colSel) {
			int index = colSel.getSelectedIndex();
			colType.setText("        "+_rt.getModel().getColumnClass(index).getName());
			} else if (e.getSource() == timeGroup) {
				// Show options for date
				int index = timeGroup.getSelectedIndex();
				switch (index) {
					case 0: case 3: case 4: case 5: case 13: default:
						dateInfo.setText("");
						timeFormat.setEnabled(false);
						break;
					case 1: case 2:
						dateInfo.setText("   Starting Month:");
						timeFormat.removeAllItems();
						timeFormat.addItem("Jan");timeFormat.addItem("Feb");timeFormat.addItem("Mar");
						timeFormat.addItem("Apr");timeFormat.addItem("May");timeFormat.addItem("Jun");
						timeFormat.addItem("Jul");timeFormat.addItem("Aug");timeFormat.addItem("Sep");
						timeFormat.addItem("Oct");timeFormat.addItem("Nov");timeFormat.addItem("Dec");
						timeFormat.setEnabled(true);
						break;
					case 6:case 7:case 8:case 9:case 10:case 11:
						dateInfo.setText("   Hour Format:");
						timeFormat.removeAllItems();
						timeFormat.addItem("AM PM");
						timeFormat.addItem("24 Hrs");
						timeFormat.setEnabled(true);
						break;
						
					case 12:
						dateInfo.setText("   Starting Day:");
						timeFormat.removeAllItems();
						timeFormat.addItem("Sun");timeFormat.addItem("Mon");timeFormat.addItem("Tue");
						timeFormat.addItem("Wed");timeFormat.addItem("Thu");timeFormat.addItem("Fri");
						timeFormat.addItem("Sat");
						timeFormat.setEnabled(true);
						break;
				}
			}
		}
	} // End of ItemStateChange
	
	
	/* This function will be used to add row to number grouping feature */
	private void addRow() {
		
		JLabel grpNameL = new JLabel("Group ID:");
		JTextField grpName = new JTextField(10);
		grpName.setText("Group_"+ (capacity+1));
		grpName.setToolTipText("Enter Group Name");
		
		JLabel lrange = new JLabel("  Min:", JLabel.LEADING);
		JFormattedTextField jft_low = new JFormattedTextField();
		jft_low.setValue(new Double(capacity * 100.00D));
		jft_low.setColumns(10);
		JLabel hrange = new JLabel("  Max:", JLabel.LEADING);
		JFormattedTextField jft_high = new JFormattedTextField();
		jft_high.setValue(new Double((capacity+1) * 100.00D)); // default Min
		jft_high.setColumns(10);
		
		JLabel otherValueL = new JLabel("Others:", JLabel.LEADING);
		otherValueL.setToolTipText("Comma Separated List of Numbers");
		JTextField otherValue = new JTextField(20);
		otherValue.setToolTipText("Comma Separated List of Numbers");
		
		grpName_v.add(capacity,grpName);
		jft_low_v.add(capacity,jft_low);
		jft_high_v.add(capacity,jft_high);
		otherValue_v.add(capacity,otherValue);
		
		/* Add value to Panel */
		cp.add(grpNameL);
		cp.add(grpName);
		cp.add(lrange);
		cp.add(jft_low);
		cp.add(hrange);
		cp.add(jft_high);
		cp.add(otherValueL);
		cp.add(otherValue);
		capacity++;
	}

	private void validateNumberInput() {
		validNum_bv = new Vector<Boolean>();
		otherNum_v = new Vector<Vector<Double>>();
		
		for (int i=0; i < capacity; i++) {
			Vector<Double> newVec = new Vector<Double>();
			otherNum_v.add(i,newVec);
			String grpName = grpName_v.get(i).getText();
			if (grpName == null || "".equals(grpName) ){
				validNum_bv.add(i, false);
				continue;
			}
			Double minval = (Double) jft_low_v.get(i).getValue();
			Double maxval = (Double) jft_high_v.get(i).getValue();
			if ( minval == null || maxval == null || maxval <= minval ) {
				validNum_bv.add(i, false);
				continue;
			}
			String otherVal = otherValue_v.get(i).getText();
			if (otherVal != null &&  "".equals(otherVal) == false) {
				String[] numVal = otherVal.split(","); // comma separated
				for (int j=0; j< numVal.length; j++) {
					try {
						double othVal = Double.parseDouble(numVal[j]);
						otherNum_v.get(i).add(othVal);
						
					} catch (NumberFormatException e) {
						
					} catch (NullPointerException e) {
						
					}
				}
			}
			validNum_bv.add(i, true); // All valid
		}
	}
	
	/* This function will be used to add row to season grouping feature */
	private void addSeasonRow() {
		
		JLabel grpNameL = new JLabel("Season ID:");
		JTextField grpName = new JTextField(10);
		grpName.setText("Season_"+ (capacityS+1));
		grpName.setToolTipText("Enter Season Name");
		
		JSpinner jsp_low = new JSpinner(new SpinnerDateModel());
		JSpinner jsp_high = new JSpinner(new SpinnerDateModel());
		jsp_low.setEditor(new JSpinner.DateEditor(jsp_low, "dd/MM/yyyy HH:mm:ss"));
		jsp_low.setToolTipText("dd/MM/yyyy HH:mm:ss format");
		jsp_high.setEditor(new JSpinner.DateEditor(jsp_high, "dd/MM/yyyy HH:mm:ss"));
		jsp_high.setToolTipText("dd/MM/yyyy HH:mm:ss format");
		
		JLabel lrange = new JLabel("Start Date:", JLabel.LEADING);
		JLabel hrange = new JLabel("End Date:", JLabel.LEADING);
		
		grpName_vS.add(capacityS,grpName);
		jft_low_vS.add(capacityS,jsp_low);
		jft_high_vS.add(capacityS,jsp_high);

		
		/* Add value to Panel */
		cps.add(grpNameL);
		cps.add(grpName);
		cps.add(lrange);
		cps.add(jsp_low);
		cps.add(hrange);
		cps.add(jsp_high);
		capacityS++;
	}
	
	// Validate the date
	private void validateDateInput() {
		validDate_bvS = new Vector<Boolean>();
		
		for (int i=0; i < capacityS; i++) {
			String grpName = grpName_vS.get(i).getText();
			if (grpName == null || "".equals(grpName) ){
				validDate_bvS.add(i, false);
				continue;
			}
			Date mindate = (Date) jft_low_vS.get(i).getValue();
			Date maxdate = (Date) jft_high_vS.get(i).getValue();
			if ( mindate == null || maxdate == null || maxdate.before(mindate) == true ) {
				validDate_bvS.add(i, false);
				continue;
			}
			validDate_bvS.add(i, true); // All valid
		}
	}

}
