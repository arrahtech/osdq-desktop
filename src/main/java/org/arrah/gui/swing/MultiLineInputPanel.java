package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
 *                                             *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with copyright      *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This file is used for taking input for
 * multiLine feed.
 */


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.util.AsciiParser;



public class MultiLineInputPanel implements ActionListener {
	private ReportTable _rt;
	private File f;
    private JDialog _dg;
    private JScrollPane table_s;
	private JFormattedTextField recSep, fldSep , colCount;
	
	public MultiLineInputPanel() {
		try {
			f = FileSelectionUtil.chooseFile("ATD Open File");
			if (f == null)
				return;
			 else 
				takeOptions(f);
		} catch (FileNotFoundException file_e) {

		} catch (IOException io_e) {
		}
		
		createGUI();
	}
	// Preview
	private void takeOptions(File f) throws IOException {
		if (f == null)
			return;
		int count = 0;
		String line = null;
		_rt = new ReportTable(new String[] { "Column Name" });
		BufferedReader br = new BufferedReader(new FileReader(f));
		while ((line = br.readLine()) != null && (count < 100)) { // show hundred lines
			count++;
			_rt.addFillRow(new String[] { line });
		}
		br.close();
		return;

	}

	private void createGUI() {
		
		JLabel reclabel = new JLabel("Record Separator: ", JLabel.TRAILING);
		JLabel fldlabel = new JLabel("Field Separator: ", JLabel.TRAILING);
		JLabel noCollabel = new JLabel("Number of Fields: ", JLabel.TRAILING);
		
		recSep = new JFormattedTextField();
		recSep.setValue(new String("#"));
		recSep.setColumns(6);

		fldSep = new JFormattedTextField();
		fldSep.setValue(new String(","));
		fldSep.setColumns(6);
		
		colCount = new JFormattedTextField();
		colCount.setValue(2);
		colCount.setColumns(6);
		
		JButton orig_b = new JButton("Raw Value");
		orig_b.addKeyListener(new KeyBoardListener());
		orig_b.setActionCommand("original");
		orig_b.addActionListener(this);
		
		JButton preview = new JButton("Preview");
		preview.addActionListener(this);
		preview.addKeyListener(new KeyBoardListener());
		preview.setActionCommand("preview");
		
		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("ok");

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		cancel.setActionCommand("cancel");

		table_s = new JScrollPane();
		table_s.setPreferredSize(new Dimension(670, 350));
		table_s.setViewportView(_rt);
		
		JPanel panel = new JPanel();

		panel.add(orig_b);panel.add(preview);panel.add(ok);panel.add(cancel);
		panel.add(reclabel);panel.add(fldlabel);panel.add(noCollabel);
		panel.add(recSep);panel.add(fldSep);panel.add(colCount);
		panel.add(table_s);

		SpringLayout layout = new SpringLayout();
		panel.setLayout(layout);
		panel.setPreferredSize(new Dimension(700, 450));

		layout.putConstraint(SpringLayout.NORTH, reclabel, 10, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, reclabel, 4, SpringLayout.WEST,panel);
		layout.putConstraint(SpringLayout.NORTH, recSep, 0, SpringLayout.NORTH, reclabel);
		layout.putConstraint(SpringLayout.WEST, recSep, 4, SpringLayout.EAST,reclabel);
		
		layout.putConstraint(SpringLayout.NORTH, fldlabel, 10, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, fldlabel, 4, SpringLayout.EAST,recSep);
		layout.putConstraint(SpringLayout.NORTH, fldSep, 0, SpringLayout.NORTH, fldlabel);
		layout.putConstraint(SpringLayout.WEST, fldSep, 4, SpringLayout.EAST,fldlabel);
		
		layout.putConstraint(SpringLayout.NORTH, noCollabel, 10, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, noCollabel, 4, SpringLayout.EAST,fldSep);
		layout.putConstraint(SpringLayout.NORTH, colCount, 0,SpringLayout.NORTH, noCollabel);
		layout.putConstraint(SpringLayout.WEST, colCount, 4, SpringLayout.EAST,noCollabel);
		
		layout.putConstraint(SpringLayout.NORTH, table_s, 5,SpringLayout.SOUTH, noCollabel);
		layout.putConstraint(SpringLayout.WEST, table_s, 4, SpringLayout.WEST,panel);

		layout.putConstraint(SpringLayout.SOUTH, orig_b, -15, SpringLayout.SOUTH,panel);
		layout.putConstraint(SpringLayout.WEST, orig_b, 200, SpringLayout.WEST,panel);
		layout.putConstraint(SpringLayout.SOUTH, preview, -15, SpringLayout.SOUTH,panel);
		layout.putConstraint(SpringLayout.WEST, preview, 5, SpringLayout.EAST,orig_b);
		layout.putConstraint(SpringLayout.SOUTH, ok, -15, SpringLayout.SOUTH,panel);
		layout.putConstraint(SpringLayout.WEST, ok, 5, SpringLayout.EAST,preview);
		layout.putConstraint(SpringLayout.SOUTH, cancel, 0, SpringLayout.SOUTH,ok);
		layout.putConstraint(SpringLayout.WEST, cancel, 5, SpringLayout.EAST,ok);

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
		if (command.equals("original")) {
			try {
				takeOptions(f);
				table_s.setViewportView(_rt);
				return;
			} catch (Exception exp) {
				ConsoleFrame.addText("\n File IO Exception Happened:" + f);
				JOptionPane.showMessageDialog(null, exp.getMessage(),
						"IO Error", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}
		String rcdInput = recSep.getText();
		String fldInput = fldSep.getText();
		String  colInput = colCount.getText();
		
		if ( fldInput == null || fldInput.equals("") 
			|| colInput == null || colInput.equals("")){
		
			JOptionPane.showMessageDialog(null, "Please Choose Valid Input",
				"Invalid Input",JOptionPane.ERROR_MESSAGE);
			return;
		}
		int colCountInput = 0;
		try {
		 colCountInput = Integer.parseInt(colInput);
		if (colCountInput <= 0) {
			JOptionPane.showMessageDialog(null, "Please Choose Valid Field Numbers",
					"Invalid Field Count",JOptionPane.ERROR_MESSAGE);
				return;
		}
		} catch (NumberFormatException ne) {
			JOptionPane.showMessageDialog(null, "Please Choose Valid Field Numbers",
					"Invalid Field Count",JOptionPane.ERROR_MESSAGE);
				return;
		}
		
				
		if (command.equals("preview")) {
			ReportTableModel rtm = AsciiParser.loadRecord(f,rcdInput,fldInput, colCountInput,false);
			_rt = new ReportTable(rtm);
			table_s.setViewportView(_rt);
			return;
		}

		if (command.equals("ok")) {
			ReportTableModel rtm = AsciiParser.loadRecord(f,rcdInput,fldInput, colCountInput,true);
			_rt = new ReportTable(rtm);
			_dg.dispose();
			DisplayFileTable dft = new DisplayFileTable(_rt, f.toString());
			dft.showGUI();
			return;
		}

	}

} // End of Expression Builder
