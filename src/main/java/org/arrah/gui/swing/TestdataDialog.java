package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2016      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for creating test data 
 * which will be used for model testing and 
 * validation
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Vector;

import javax.swing.BoxLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.arrah.framework.datagen.SplitRTM;
import org.arrah.framework.dataquality.DeDuplicateRTM;
import org.arrah.framework.ndtable.ReportTableModel;

public class TestdataDialog implements ActionListener, ChangeListener {
	
	private JDialog jd, jd1, jd2;
	private JCheckBox selBox[];
	private JLabel lvalue[];
	private ReportTableModel _rtm, _rtmsubset, _rtmtestdata = null, _rtmtraindata = null, _rtmvaliddata[];
	private String[]  newCol = null; // selected Value
	private int[] newColI = null; // selected Index
	
	// For Input Validation
	private JFormattedTextField valdtf,testdtf,traindtf,foldtf;
	private JCheckBox trainWoVal, ignoredata, testdata;
	private JRadioButton rd1,rd2;
	private JSpinner igsp,testsp;
	private JComboBox<String> cb1;
	
	public TestdataDialog (ReportTableModel rtm) {
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
		String buttonL = "Next";
		JButton tstc = new JButton(buttonL);
		tstc.setActionCommand("next");
		tstc.addKeyListener(new KeyBoardListener());
		tstc.addActionListener(this);
		bp.add(tstc);
		
		JButton cn_b = new JButton("Cancel");
		cn_b.setActionCommand("exit");
		cn_b.addKeyListener(new KeyBoardListener());
		cn_b.addActionListener(this);
		bp.add(cn_b);
		
		dp.add(p, BorderLayout.CENTER);
		dp.add(bp, BorderLayout.PAGE_END);
		
		jd = new JDialog ();
		jd.setTitle("Test Creation Dialog");
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
				jd.dispose();
				return;
			}
			if (action_c.compareToIgnoreCase("cancel") == 0) {
				jd1.dispose();
				return;
			}
			if (action_c.compareToIgnoreCase("next") == 0) {
				int colC = lvalue.length;
				Vector<String>  colName = new Vector<String>();
				for (int i = 0; i < colC; i++) {
					if (selBox[i].isSelected() == true) //only selected one
					colName.add(lvalue[i].getText());
				}
				// Common Index
				newCol = new String[colName.size()];
				newCol = colName.toArray(newCol);
				newColI = new int[colName.size()];
				for (int i=0; i < newColI.length; i++) 
					newColI[i] = _rtm.getColumnIndex(newCol[i]);
				
				if (colName.size() == colC) { // all selected
					_rtmsubset = _rtm;
					// newCol = _rtm.getAllColNameStr();
				} else { // newModel
					
					_rtmsubset = new ReportTableModel(newCol,true,true);
					int rowC = _rtm.getModel().getRowCount();
					
					for (int i=0; i < rowC; i++) {
						Object []  rowv = _rtm.getSelectedColRow(i, newColI);
						_rtmsubset.addFillRow(rowv);
					}
				}
				
				int prevC = _rtmsubset.getModel().getRowCount();
				// Remove Duplicates for testdata preparation
				_rtmsubset = new DeDuplicateRTM().removeDuplicate(_rtmsubset, null); // all columns
				int newC = _rtmsubset.getModel().getRowCount();
				ConsoleFrame.addText("\n Rows deleted for Unique:"+ (prevC - newC));
				
				jd.dispose(); // remove the selection dialog
				JPanel jp = displayGUI();
				
				jd1 = new JDialog ();
				jd1.setTitle("Test Data Creation Dialog");
				jd1.setModal(true);
				jd1.setLocation(250,250);
				jd1.getContentPane().add(jp);
				jd1.pack();
				jd1.setVisible(true);

				return;
			}
			if (action_c.compareToIgnoreCase("create") == 0) {
				
				jd1.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				if ( validateInput() == false )  {
					jd1.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
					return;
				}
				
				JPanel jp =new JPanel();
				BoxLayout boxl = new BoxLayout(jp,BoxLayout.Y_AXIS);
				jp.setLayout(boxl);

				
				if (_rtmtraindata != null)  { // Training
					JLabel ltrain = new JLabel("Training data");
					jp.add(ltrain);
					jp.add(new ReportTable(_rtmtraindata));
				}
					
				for (int i=0; i < _rtmvaliddata.length; i++) { // show fold
					JLabel lfold = new JLabel("Fold data:" + i);
					jp.add(lfold);
					jp.add(new ReportTable(_rtmvaliddata[i]));
				}
				
				if (_rtmtestdata != null)  { // show test
					JLabel ltrain = new JLabel("Test data");
					jp.add(ltrain);
					jp.add(new ReportTable(_rtmtestdata));
				}
				
				JScrollPane splitP = new JScrollPane(jp);
				jp.setPreferredSize(new Dimension(825,600*(_rtmvaliddata.length +2)));
				splitP.setPreferredSize(new Dimension(900,900));
				
				jd1.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				jd1.dispose(); // dispose
				
				jd2 = new JDialog ();
				jd2.setTitle("Test Data Sample Creation Dialog");
				jd2.setModal(true);
				jd2.setLocation(250,100);
				jd2.getContentPane().add(splitP);
				jd2.pack();
				jd2.setVisible(true);

				return;
			}
		} catch (Exception e1) {
			ConsoleFrame.addText("\n Exeption:"+e1.getLocalizedMessage());
		} finally {
			jd.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			jd.dispose();
		}
		
	}
	
	// This function will create GUI
	private JPanel displayGUI() {
		
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(800,450));
		
		JPanel gridpan = new JPanel(new GridLayout(5,3,20,20));
		rd1 = new JRadioButton("Time Series Based Sampling");
		rd1.addChangeListener(this);
		JLabel l1 = new JLabel("Select Time Column:");
		cb1 = new JComboBox<String>(newCol);
		
		ignoredata = new JCheckBox("Ignore date before:");
		JLabel lig = new JLabel("          ");
		testdata = new JCheckBox("Test data date after:");
		testdata.addChangeListener(this);
		JLabel ltest = new JLabel("         ");
		igsp = new JSpinner(new SpinnerDateModel());
		testsp = new JSpinner(new SpinnerDateModel());
		
		rd2 = new JRadioButton("Random Sampling");
		rd2.setSelected(true);
		JLabel l2 = new JLabel("Random seed");
		JFormattedTextField tf2 = new JFormattedTextField(new Integer(20)); // Random seed
		
		ButtonGroup jb = new ButtonGroup();
		jb.add(rd1); jb.add(rd2);
		
		JCheckBox trainVal = new JCheckBox("Validation Data part of Training Data");
		trainVal.setSelected(true);
		trainWoVal = new JCheckBox("Validation Data not part of Training Data");
		trainVal.addChangeListener(this);trainWoVal.addChangeListener(this);
		ButtonGroup jbval = new ButtonGroup();
		jbval.add(trainVal); jbval.add(trainWoVal);
		
		gridpan.add(rd1);gridpan.add(l1); gridpan.add(cb1);
		gridpan.add(lig);gridpan.add(ignoredata);gridpan.add(igsp); 
		gridpan.add(ltest);gridpan.add(testdata);gridpan.add(testsp);
		gridpan.add(rd2);gridpan.add(l2); gridpan.add(tf2);
		gridpan.add(trainVal);gridpan.add(trainWoVal);
		jp.add(gridpan);
		
		JPanel inputpan = new JPanel(new GridLayout(5,2,20,20));
		JLabel traind = new JLabel("Training Data:");
		traindtf = new JFormattedTextField(new Integer(50));
		
		JLabel vald = new JLabel("Validation Data:");
		valdtf = new JFormattedTextField(new Integer(20));
		valdtf.setEnabled(false); // default behavior
		
		JLabel testd = new JLabel("Test Data:");
		testdtf = new JFormattedTextField(new Integer(30));
		JLabel fold = new JLabel("Num of Folds:");
		foldtf = new JFormattedTextField(new Integer(4));
		
		JButton create = new JButton("Create");
		create.setActionCommand("create");
		create.addKeyListener(new KeyBoardListener());
		create.addActionListener(this);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addKeyListener(new KeyBoardListener());
		cancel.addActionListener(this);
		

		inputpan.add(traind);inputpan.add(traindtf);
		inputpan.add(vald);inputpan.add(valdtf);
		inputpan.add(testd);inputpan.add(testdtf);
		inputpan.add(fold);inputpan.add(foldtf);
		inputpan.add(create);inputpan.add(cancel);
		
		jp.add(inputpan);
		
		return jp;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == trainWoVal) {
			if (trainWoVal.isSelected() == true)
				valdtf.setEnabled(true);
			else
				valdtf.setEnabled(false);
			return;
		}
		
		if (e.getSource() == rd1 ) {
			 if ( rd1.isSelected() == false )
				 testdtf.setEnabled(true);
			 if ( rd1.isSelected() == true ) {
				 ignoredata.setSelected(false);
				 testdata.setSelected(false);
			 }
			 return;
		}
		
		if (e.getSource() == testdata ) {
			if (testdata.isSelected() == true && rd1.isSelected() == true)
				testdtf.setEnabled(false);
			else
				testdtf.setEnabled(true);
			return;
		} 
	}
	
	// This function will return boolean yes no if input is valid
	private boolean  validateInput() {
		
		// sum should be 100 ( with or without validation)
		int validv = 0,testv=0,trainv=0, foldv=0;
		if (valdtf.isEnabled() == true) {
			validv = (int) valdtf.getValue();
		}
		if (testdtf.isEnabled() == true) {
			testv = (int) testdtf.getValue();
		}
		trainv =	(int) traindtf.getValue();
		foldv = (int) foldtf.getValue();
		
		if (validv + testv + trainv != 100)  { // 100 % should match
			JOptionPane.showMessageDialog(null, "Sum of Training, Validation (if enabled) and Test percentage \n"
					+ "should be 100");
			
			return false;
		}
				
		
		// Select if timeseries is chosen or random
		if (rd1.isSelected() == true) {
			Vector<Date> vcdate = new Vector<Date>();
			if (ignoredata.isSelected() == true) {
				vcdate.add( (Date) igsp.getValue());
			}
			if (testdata.isSelected() == true) {
				vcdate.add((Date) testsp.getValue());
			}
			
			if (vcdate.size() != 0)  { // time series used
				if (vcdate.size() == 2) // both before after is there
					if (vcdate.get(0).after(vcdate.get(1))) {
						JOptionPane.showMessageDialog(null, "Ignore date is after Test data date");
						return false;
					}
				
				// Report table should have split according to time
				Date[] datea = new Date[vcdate.size()];
				ReportTableModel[] rm = new SplitRTM().splitByDate(_rtmsubset, cb1.getSelectedIndex(),vcdate.toArray(datea));
				
				if (rm.length == 3) { // both ignore and testdata 
					_rtmtestdata = rm[2];
					_rtmsubset = rm[1];
				}
				
				if (rm.length == 2 && (ignoredata.isSelected() == true))  // only ignore selected
					_rtmsubset = rm[1];
				
				if (rm.length == 2 && (testdata.isSelected() == true)) { // only testdata selected
					_rtmtestdata = rm[1];
					_rtmsubset = rm[0];
				}
				
			} // time series used
		}
		
		// Now create fold
		int [] splitnum = new int[3];
		splitnum[0] = trainv ;splitnum[1] = validv; splitnum[2] = testv;
		ReportTableModel[] rm = new SplitRTM().splitRandom(_rtmsubset, splitnum );
		
		if (testv != 0)
			_rtmtestdata = rm[2];

		int[] foldarr = new int[foldv];
		int buckno= (int) Math.floor( 100 /foldv);
		for (int i=0; i < foldv-1; i++) 
			foldarr[i] = buckno;
		
		foldarr[foldv-1] = 100 - (buckno * (foldv -1));
		
		if (validv == 0) {
			 _rtmvaliddata = new SplitRTM().splitRandom(rm[0], foldarr );
		} else {
			_rtmvaliddata = new SplitRTM().splitRandom(rm[1], foldarr );
			_rtmtraindata = rm[0];
		}
		
		
		return true;
		
	}
	
}
