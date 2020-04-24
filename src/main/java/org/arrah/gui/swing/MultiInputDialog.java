package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2019      *
 *     http://www.arrahtech.com                *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for taking mutilple input
 * Check Box bases. Than pass on the input
 * to calling classes
 * 
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.arrah.framework.rdbms.SqlType;

import java.util.ArrayList;
import java.util.List;

public class MultiInputDialog implements ActionListener {
	
	private JDialog jd;
	private JCheckBox selBox[];
	private JLabel lvalue[];
	private List<String> selectedValue= null;
	private List<String> selectedtype= null;
	private JComboBox<String> tpyeSelection [];
	private String[] typeStr = new String[] {"String","Number","Aadhar","PAN","GST","Creditcard","Mobile","Email"};
	private String[] columnClass;
	
	public MultiInputDialog (String[] selectOption, boolean isSelect) {
		multiselectDialog(selectOption, isSelect, false);
	}
	
	public MultiInputDialog (List<String> selectOption, boolean isSelect) {
		String[] stra = new String[selectOption.size()];
		multiselectDialog(selectOption.toArray(stra), isSelect, false);
	}
	
	public MultiInputDialog (String[] selectOption, String[] selectOptionClass,boolean isSelect, boolean showType) {
		
		columnClass = selectOptionClass;
		
		multiselectDialog(selectOption, isSelect, showType);
		
	}
	

	
	private void multiselectDialog(String[] selectOption, boolean isSelect, boolean showType) {
		JPanel dp = new JPanel();
		dp.setLayout(new BorderLayout());
		
		
		//Create and populate the panel for table rename       
		JPanel p = new JPanel(new SpringLayout());
		int numPairs = selectOption.length;
		selBox = new JCheckBox[numPairs];
		lvalue = new JLabel[numPairs];
		
		if (showType == true)
			tpyeSelection = new JComboBox[numPairs];
		
		boolean useColumClass = true;
		if (columnClass == null || columnClass.length == 0 || columnClass.length != numPairs )
			useColumClass = false;
			
		for (int i = 0; i < numPairs; i++) {
			lvalue[i] = new JLabel(selectOption[i],JLabel.LEADING);
			selBox[i] = new JCheckBox();
			selBox[i].setSelected(isSelect);
			p.add(selBox[i]);
			p.add(lvalue[i]);
			
			if (showType == true) {
				
				tpyeSelection[i] = new JComboBox<String>(typeStr);
				
				// System.out.println(columnClass[i]);
				
				if ( useColumClass && SqlType.getMetaClassType(columnClass[i]).compareToIgnoreCase("Number") ==0 ) 
					tpyeSelection[i].setSelectedIndex(1); // Number
				
				p.add(tpyeSelection[i]);
 			}
		}
		
		
		//Lay out the panel.  
		if (showType == false) {
			SpringUtilities.makeCompactGrid(p,                                        
					numPairs, 2, //rows, cols                                        
					6, 6,        //initX, initY                                        
					6, 6);       //xPad, yPad    
		} else {
			SpringUtilities.makeCompactGrid(p,                                        
					numPairs, 3, //rows, cols                                        
					6, 6,        //initX, initY                                        
					6, 6);       //xPad, yPad   
		}
		
		JScrollPane pane = new JScrollPane(p);
		if (showType == false) 
			pane.setPreferredSize(new Dimension(650, 400));
		else
			pane.setPreferredSize(new Dimension(700, 400));
		
		JPanel bp = new JPanel();
		JButton tstc = new JButton("Select");
		tstc.setActionCommand("select");
		tstc.addKeyListener(new KeyBoardListener());
		tstc.addActionListener(this);
		bp.add(tstc);
		
		JButton cn_b = new JButton("Cancel");
		cn_b.setActionCommand("cancel");
		cn_b.addKeyListener(new KeyBoardListener());
		cn_b.addActionListener(this);
		bp.add(cn_b);
		
		dp.add(pane, BorderLayout.CENTER);
		dp.add(bp, BorderLayout.PAGE_END);
		
		jd = new JDialog ();
		jd.setTitle("Multi Select Dialog");
		jd.setModal(true);
		jd.setLocation(200, 200);
		jd.getContentPane().add(dp);
		jd.pack();
		jd.setVisible(true);

	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String action_c = e.getActionCommand();
		try {
			jd.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			if (action_c.compareToIgnoreCase("cancel") == 0) {
				return;
			}
			if (action_c.compareToIgnoreCase("select") == 0) {
				int colC = lvalue.length;
				selectedValue = new ArrayList<String>();
				selectedtype = new ArrayList<String>();
				for (int i = 0; i < colC; i++) {
					if (selBox[i].isSelected() == true)  {// only selected one
						selectedValue.add(lvalue[i].getText());
						selectedtype.add(tpyeSelection[i].getSelectedItem().toString());
					}
				}
				return;
			}
		} catch (Exception e1) {
			ConsoleFrame.addText("\n Exeption:"+e1.getLocalizedMessage());
		} finally {
			jd.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			jd.dispose();
		}
	}
	
	public List<String> getSelected() {
		return selectedValue;
	}
	public List<String> getSelectedType() {
		return selectedtype;
	}
}
