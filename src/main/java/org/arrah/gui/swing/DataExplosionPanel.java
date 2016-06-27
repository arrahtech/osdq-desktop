package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2013      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with copyright      *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This file is used for taking input data and
 * then expand data randomly
 */


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import org.arrah.framework.datagen.RandomColGen;
import org.arrah.framework.util.DiscreetRange;
import org.arrah.framework.util.StringCaseFormatUtil;


public class DataExplosionPanel implements ActionListener {
	private ReportTable _rt;
	private JTextArea _expane;
	private int _selColIndex;
	private int beginIndex,endIndex;
	private JDialog _dg;
	private JFormattedTextField jrn_low, jrn_high , jrn_delim;
	
	public DataExplosionPanel(ReportTable rt, int selColIndex) {
		_rt = rt;
		_selColIndex = selColIndex;
		createGUI();
	}

	public DataExplosionPanel(ReportTable rt) {
		_rt = rt;
		createGUI();
	}

	private void createGUI() {
		
		JLabel infolabel = new JLabel("Enter Data Values in the Panel below (Choose Delimiter): ", JLabel.TRAILING);
		JPanel rowPanel = createRowNumPanel();
		
		jrn_delim = new JFormattedTextField();
		jrn_delim.setValue(new String(","));
		jrn_delim.setColumns(3);

		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("ok");

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		cancel.setActionCommand("cancel");


		_expane = new JTextArea(15, 50);
		_expane.setWrapStyleWord(true);
		_expane.setLineWrap(true);
		
		JPanel panel = new JPanel();

		panel.add(ok);
		panel.add(cancel);
		panel.add(_expane);
		panel.add(infolabel);
		panel.add(rowPanel);
		panel.add(jrn_delim);

		SpringLayout layout = new SpringLayout();
		panel.setLayout(layout);
		panel.setPreferredSize(new Dimension(600, 400));

		layout.putConstraint(SpringLayout.NORTH, infolabel, 4,
				SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, infolabel, 4, SpringLayout.WEST,
				panel);
		layout.putConstraint(SpringLayout.NORTH, jrn_delim, 3,
				SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, jrn_delim, 4, SpringLayout.EAST,
				infolabel);
		layout.putConstraint(SpringLayout.NORTH, _expane, 3,
				SpringLayout.SOUTH, jrn_delim);
		layout.putConstraint(SpringLayout.WEST, _expane, 4, SpringLayout.WEST,
				panel);
		
		layout.putConstraint(SpringLayout.NORTH, rowPanel, 5, SpringLayout.SOUTH,
				_expane);
		layout.putConstraint(SpringLayout.WEST, rowPanel, 4, SpringLayout.WEST,
				panel);
		
		layout.putConstraint(SpringLayout.SOUTH, ok, -5, SpringLayout.SOUTH,
				panel);
		layout.putConstraint(SpringLayout.WEST, ok, -350, SpringLayout.EAST,
				panel);
		layout.putConstraint(SpringLayout.SOUTH, cancel, 0, SpringLayout.SOUTH,
				ok);
		layout.putConstraint(SpringLayout.WEST, cancel, 5, SpringLayout.EAST,
				ok);

		_dg = new JDialog();
		_dg.setTitle("Data Expansion Dialog");
		_dg.setLocation(200, 100);
		_dg.getContentPane().add(panel);

		_dg.pack();
		_dg.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("cancel")) {
			_dg.dispose();
			return;
		}
		if (command.equals("ok")) {
			String exp = _expane.getText();
			String delim = (String) jrn_delim.getValue();
			if (delim == null || "".equals(delim)) {
				JOptionPane.showMessageDialog(null,
						"Invalid  Delimiter", "Invalid  Input Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Vector<String> value = StringCaseFormatUtil.tokenizeText(exp,delim);
			if (value == null || value.size() == 0 ){
				JOptionPane.showMessageDialog(null,
						"Invalid Input Data", "Invalid  Input Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			try {	
			_dg.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			
			beginIndex = ((Long) jrn_low.getValue()).intValue();
			endIndex = ((Long) jrn_high.getValue()).intValue();
			int rowC = _rt.getModel().getRowCount();
			if (beginIndex <= 0 || beginIndex > rowC)
				beginIndex = 1;
			if (endIndex <= 0 || endIndex > (rowC+1) || endIndex < beginIndex )
				endIndex = rowC+1;
			Object[] burstVal= RandomColGen.burstData(value.toArray(), endIndex - beginIndex );
			
			for ( int i = (beginIndex -1 ) ; i < (endIndex -1); i++ )
			_rt.getModel().setValueAt(burstVal[i - (beginIndex -1)], i, _selColIndex);

			return;
		} finally {
			_dg.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			_dg.dispose();
		} }

	}

	private JPanel createRowNumPanel() {
		JPanel rownnumjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel lrange = new JLabel("  Row Numbers to Populate : From(Inclusive)", JLabel.LEADING);
		jrn_low = new JFormattedTextField();
		jrn_low.setValue(new Long(1));
		jrn_low.setColumns(8);
		JLabel torange = new JLabel("  To(Exclusive):", JLabel.LEADING);
		jrn_high = new JFormattedTextField();
		jrn_high.setValue(new Long(_rt.getModel().getRowCount() +1 ));
		jrn_high.setColumns(8);
		
		rownnumjp.add(lrange);
		rownnumjp.add(jrn_low);
		rownnumjp.add(torange);
		rownnumjp.add(jrn_high);
		return rownnumjp;
	}
} // End of Expression Builder
