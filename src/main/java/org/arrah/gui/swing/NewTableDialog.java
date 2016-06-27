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

/* This file is used for taking input and create
 * a new reportTable with less columns.
 * 
 * ReportTable does not allow deleting the column
 * so this is required.
 *
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.arrah.framework.dataquality.DeDuplicateRTM;
import org.arrah.framework.ndtable.ReportTableModel;

public class NewTableDialog implements ActionListener {
	
	private JDialog jd, jd1;
	private JCheckBox selBox[];
	private JLabel lvalue[];
	private ReportTableModel _rtm;
	private ReportTable _rt = null;
	private boolean isExit = true;
	private boolean isDeDup = false;
	
	public NewTableDialog (ReportTableModel rtm) {
		_rtm = rtm;
		int colcount = rtm.getModel().getColumnCount();
		String[] colName = new String[colcount];
		for (int i=0; i < colcount; i++)
			colName[i] = rtm.getModel().getColumnName(i);
		tableRenameDialog(colName);
	}
	
	// New Constructor for dedup
	public NewTableDialog (ReportTableModel rtm, boolean isDedup) {
		isDeDup = isDedup;
		_rtm = rtm;
		int colcount = rtm.getModel().getColumnCount();
		String[] colName = new String[colcount];
		for (int i=0; i < colcount; i++)
			colName[i] = rtm.getModel().getColumnName(i);
		tableRenameDialog(colName);
		
	}
	
	private void tableRenameDialog(String[] colName) {
		JPanel dp = new JPanel();
		dp.setLayout(new BorderLayout());
		
		
		//Create and populate the panel for table rename       
		JPanel p = new JPanel(new SpringLayout());
		int numPairs = colName.length;
		selBox = new JCheckBox[numPairs];
		lvalue = new JLabel[numPairs];
		
		for (int i = 0; i < numPairs; i++) {
			lvalue[i] = new JLabel(colName[i],JLabel.TRAILING);
			selBox[i] = new JCheckBox();
			if (isDeDup == true)
				selBox[i].setSelected(true);
			p.add(selBox[i]);
			p.add(lvalue[i]);
		}
		
		//Lay out the panel.        
		SpringUtilities.makeCompactGrid(p,                                        
				numPairs, 2, //rows, cols                                        
				6, 6,        //initX, initY                                        
				6, 6);       //xPad, yPad          
		

		JPanel bp = new JPanel();
		String buttonL = "Create";
		
		if (isDeDup == true)
			buttonL="DeDup";
			
		JButton tstc = new JButton(buttonL);
		if (isDeDup == true)
			tstc.setActionCommand("dedup");
		else
			tstc.setActionCommand("save");
		tstc.addKeyListener(new KeyBoardListener());
		tstc.addActionListener(this);
		bp.add(tstc);
		
		JButton cn_b = new JButton("Exit");
		cn_b.setActionCommand("exit");
		cn_b.addKeyListener(new KeyBoardListener());
		cn_b.addActionListener(this);
		bp.add(cn_b);
		
		dp.add(p, BorderLayout.CENTER);
		dp.add(bp, BorderLayout.PAGE_END);
		
		jd = new JDialog ();
		if (isDeDup == true)
			jd.setTitle("Duplicate Table Data");
		else
			jd.setTitle("Table Creation Dialog");
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
			if (action_c.compareToIgnoreCase("exit") == 0) {
				isExit = true;
				return;
			}
			if (action_c.compareToIgnoreCase("save") == 0) {
				isExit = false;
				int colC = lvalue.length;
				Vector<String>  colName = new Vector<String>();
				for (int i = 0; i < colC; i++) {
					if (selBox[i].isSelected() == true) // only selected one
					colName.add(lvalue[i].getText());
				}
				String[]  newCol = new String[colName.size()];
				int[] newColI = new int[colName.size()];
				ReportTableModel newRTM = new ReportTableModel(colName.toArray(newCol));
				
				for (int i=0; i < newColI.length; i++)
					newColI[i] = _rtm.getColumnIndex(newCol[i]);
				
				int rowC = _rtm.getModel().getRowCount();
						
				for (int i=0; i < rowC; i++) 
					newRTM.addFillRow(_rtm.getSelectedColRow(i, newColI));
				
				_rt = new ReportTable(newRTM); // assign new value
				return;
			}
			if (action_c.compareToIgnoreCase("dedup") == 0) {
				isExit = false;
				int colC = lvalue.length;
				Vector<String>  colName = new Vector<String>();
				for (int i = 0; i < colC; i++) {
					if (selBox[i].isSelected() == true) // only selected one
					colName.add(lvalue[i].getText());
				}
				String[]  newCol = new String[colName.size()];
				newCol = colName.toArray(newCol);
				int[] newColI = new int[colName.size()];
				for (int i=0; i < newColI.length; i++)
					newColI[i] = _rtm.getColumnIndex(newCol[i]);
				
				int rowC = _rtm.getModel().getRowCount();
				DeDuplicateRTM dupicateM = new DeDuplicateRTM();
				ReportTableModel newRTM = dupicateM.removeDuplicate(_rtm,newColI);
				int newRowC= newRTM.getModel().getRowCount();
				JOptionPane.showMessageDialog(null, rowC - newRowC + " Rows Deleted");
				
				jd.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				jd.dispose();
				int sel =JOptionPane.showConfirmDialog(null, "Do you want to see deleted duplicate rows?",
						"Duplicate Values",JOptionPane.YES_NO_OPTION);
				
				if (sel == JOptionPane.NO_OPTION) return;
				
				ReportTableModel dupRTM = dupicateM.showDuplicateModel();
				_rt = new ReportTable(dupRTM);
				displayGUI();
				
				return;
			}
		} catch (Exception e1) {
			ConsoleFrame.addText("\n Exeption:"+e1.getLocalizedMessage());
		} finally {
			jd.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			jd.dispose();
		}
		
	}
	public void displayGUI () {
		if (isExit == true ) return;
		jd1 = new JDialog ();
		if (isDeDup == true)
			jd1.setTitle("Duplicate Table Data");
		else
			jd1.setTitle("Table Creation Dialog");
		jd1.setModal(true);
		jd1.setLocation(250,250);
		jd1.getContentPane().add(_rt);
		jd1.pack();
		jd1.setVisible(true);
	}
}
