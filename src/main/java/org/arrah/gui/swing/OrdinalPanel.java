package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2016      *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is uses to create 
 * ordinal panel in data prepatation so that 
 * strings can be mapped to number for analysis
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.arrah.framework.util.DiscreetRange;



public class OrdinalPanel implements ActionListener, ItemListener {
	private ReportTable _rt;
	private int _rowC = 0;
	private int _colIndex = 0;
	private JDialog d_f;
	private JFormattedTextField jrn_low, jrn_high;
	private JComboBox<String> colSel;
	private Border line_b;
	private int beginIndex, endIndex;
	private JLabel colType;
	private JPanel cps;
	private Vector<JTextField> grpName_v;
	private ArrayList<Object> key;
	private boolean onehot = false;

	
	public OrdinalPanel(ReportTable rt, int colIndex,int ordinaltype) {
		if (ordinaltype == 1) onehot = true;
		_rt = rt;
		_colIndex = colIndex;
		_rowC = rt.table.getRowCount();
		createDialog();
	}; // Constructor
	

	private void createDialog() {
		JPanel jp = new JPanel(new BorderLayout());
		line_b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

		jp.add(createSelectionPanel(),BorderLayout.NORTH);
		
		
		if (cps == null) {
			cps = new JPanel();
			cps.setPreferredSize(new Dimension(475,325));
		}
		
		JScrollPane jscrollpane1 = new JScrollPane(cps);
		jscrollpane1.setPreferredSize(new Dimension(500, 300));
		
		jp.add(jscrollpane1,BorderLayout.CENTER);

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
		d_f.setTitle("Ordinal Panel Dialog");
		d_f.setLocation(300, 250);
		d_f.setPreferredSize(new Dimension(625,415));
		d_f.getContentPane().add(jp);
		d_f.pack();
		d_f.setVisible(true);

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
	

	private JPanel createSelectionPanel() {
		JPanel selectionjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel selL = new JLabel("Choose Column to Apply Function on:     ");
		int colC = _rt.getModel().getColumnCount();
		String[] colName = new String[colC+2]; // prompt to select column
		colName[0] = "Select Column";
		colName[1] = "--------------";
		
		for (int i = 0; i < colC; i++) 
			colName[i+2] = _rt.getModel().getColumnName(i);
		
		selectionjp.add(selL);
		colSel = new JComboBox<String>(colName);
		colSel.addItemListener(this);
		selectionjp.add(colSel);
		colType=new JLabel("  ");
		selectionjp.add(colType);
		return selectionjp;
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
			
				int selColIndex = colSel.getSelectedIndex(); // Take value from  col on which grouping will be done
				if (selColIndex < 2 ) {
					JOptionPane.showMessageDialog(null, "Select the column to make ordinal");
					return;
				} else selColIndex = selColIndex -2;
				
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					Object colObject = _rt.getModel().getValueAt(i, selColIndex);
					 if (colObject == null) continue;
					 int index = key.indexOf(colObject);
					 if (index < 0) continue;
					 String grpString= grpName_v.get(index).getText();
					_rt.getModel().setValueAt(grpString, i, _colIndex);
				}
				d_f.dispose(); // in case it is not disposed yet if all the filed null condition
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
			if (index < 2 ) { colType.setText("        "); return ;  }
			else index = index -2; // first two are for selection
			
			colType.setText("        "+_rt.getModel().getColumnClass(index).getName());
		
			try {
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				addOrdinalRow(index);
			} catch (Exception exp) {
				ConsoleFrame.addText("\n Exception:" + exp.getLocalizedMessage());
				return;
			} finally {
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
			}
		}
		
	} // End of ItemStateChange
	
	/* This function will be used to add row to ordinal grouping feature */
	private void addOrdinalRow(int colIndex) {
		if (cps == null)
			cps = new JPanel();
		cps.setPreferredSize(new Dimension(475,325));
		
		grpName_v = new Vector<JTextField> ();

		// remove all then rebuilt
		cps.removeAll();
		Vector<Object> vc = _rt.getRTMModel().getColDataV(colIndex);
		Hashtable<Object,Integer> ordinalData= DiscreetRange.getUnique(vc);
		int carSize = ordinalData.size();
		key = new ArrayList<Object>();
		
		if (carSize > 10000) 
			ConsoleFrame.addText("\n Warning: Cardinality very high, may not render:"+ carSize);
		
		for (Enumeration <Object> keyEnum = ordinalData.keys(); keyEnum.hasMoreElements();)
			key.add( keyEnum.nextElement().toString());
		
		key.sort(null);
		
		// for one hot key
		Hashtable<Object,String> ht = null;
		if (onehot == true)
			ht= DiscreetRange.getOneHotEncoding(null,ordinalData);
		
		for (int i=0; i < carSize; i++ ) {
			JLabel ordinalName = new JLabel("  Nominal String:"+key.get(i).toString());
			JLabel ordinalCount = new JLabel("   Repeat Count:"+ ordinalData.get(key.get(i)).toString());
			JTextField ordgrp = new JTextField(15);
			
			// for one hot key
			if (onehot == true)
				ordgrp.setText(""+ht.get(key.get(i)));
			else
				ordgrp.setText(""+i);
			
			ordgrp.setToolTipText("Enter Ordinal  value");
			grpName_v.add(i,ordgrp);
			
			/* Add value to Panel */
			cps.add(ordinalName);
			cps.add(ordinalCount);
			cps.add(ordgrp);
		}
		cps.setLayout(new SpringLayout());
		if (carSize > 10 ) // till ten it is default value
		cps.setPreferredSize(new Dimension(475,carSize*30));
		SpringUtilities.makeCompactGrid(cps, carSize, 3, 3, 3, 3, 3);
		cps.revalidate();
	}
}
