package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2017      *
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.util.DiscreetRange;

public class FilterDialog implements ActionListener {
	
	private JDialog jd;
	private JCheckBox selBox[];
	private JLabel lvalue[];
	private ReportTableModel _rtm;
	private int _colIndex;
	
	Vector<Integer> delete_v;
	Vector<Object[]> delrow_v;
	int response=0;
	
	public FilterDialog (ReportTableModel rtm, int colIndex) {
		_rtm = rtm;
		_colIndex = colIndex;
		Vector<Object> vc = _rtm.getColDataV(_colIndex);
		Hashtable<Object,Integer> ordinalData= DiscreetRange.getUnique(vc);
		tableRenameDialog(ordinalData);
	}
	
	private void tableRenameDialog(Hashtable<Object,Integer> ordinalData) {
		JPanel dp = new JPanel();
		dp.setLayout(new BorderLayout());
		
		
		//Create and populate the panel for table rename       
		JPanel p = new JPanel(new SpringLayout());
		int numPairs = ordinalData.size();
		selBox = new JCheckBox[numPairs];
		lvalue = new JLabel[numPairs];
		
		Object[] key_s = ordinalData.keySet().toArray();
		Arrays.sort(key_s);
		// Set<Object> keys = ordinalData.keySet();
		int i=0;
        for(Object key: key_s){
		
			lvalue[i] = new JLabel(key +"("+ordinalData.get(key)+")",JLabel.TRAILING);
			selBox[i] = new JCheckBox();
			p.add(selBox[i]);
			p.add(lvalue[i]);
			i++;
		}
		
		//Lay out the panel.        
		SpringUtilities.makeCompactGrid(p,                                        
				numPairs, 2, //rows, cols                                        
				6, 6,        //initX, initY                                        
				6, 6);       //xPad, yPad          
		
		JScrollPane pane = new JScrollPane(p);
		pane.setPreferredSize(new Dimension(650, 400));
		
		JPanel bp = new JPanel();
			
		JButton tstc = new JButton("OK");
		tstc.setActionCommand("ok");
		tstc.addKeyListener(new KeyBoardListener());
		tstc.addActionListener(this);
		bp.add(tstc);
		
		JButton cn_b = new JButton("Cancel");
		cn_b.setActionCommand("exit");
		cn_b.addKeyListener(new KeyBoardListener());
		cn_b.addActionListener(this);
		bp.add(cn_b);
		
		dp.add(pane, BorderLayout.CENTER);
		dp.add(bp, BorderLayout.PAGE_END);
		
		jd = new JDialog ();
		jd.setTitle("Filter Creation Dialog");
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
				return;
			}
			if (action_c.compareToIgnoreCase("ok") == 0) {
				int strC = lvalue.length;
				Vector<String>  filtercond = new Vector<String>();
				
				for (int i = 0; i < strC; i++) {
					if (selBox[i].isSelected() == true) // only selected one
						filtercond.add(lvalue[i].getText().substring(0, lvalue[i].getText().indexOf("(")));
				}
				
				int rowC = _rtm.getModel().getRowCount();
				int incrementC = -1; // increment at first line
				delete_v = new Vector<Integer>();
				delrow_v = new Vector<Object[]>();
				
				for (int i=0; i < rowC; i++) {
					incrementC++; // now index is 0
					Object rowobj = _rtm.getModel().getValueAt(i, _colIndex);
					if (rowobj == null) continue; // not removing null
					if (filtercond.indexOf(rowobj.toString()) == -1) { // could not find so delete
						// Before delete add into delete vector
						delete_v.add(incrementC);
						delrow_v.add(_rtm.getRow(i));
						_rtm.removeRows(i, 1);
						// now decrease count and index
						i--;rowC--;
					}
				}
				response = 1;
				return;
			}
		} catch (Exception e1) {
			ConsoleFrame.addText("\n Exeption:"+e1.getLocalizedMessage());
		} finally {
			jd.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			jd.dispose();
		}
		
	}
}
