package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2013      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for taking search options 
 * liek case sensitiveness etc which
 * will be passed to Pattern compile time to
 * java classes.
 * 
 */

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;


public class SearchOptionDialog implements ActionListener {
	private JTextArea infoBox = new JTextArea(10,30);
	private JDialog jd = new JDialog();
	private File f = null;
	
	private JCheckBox isCaseinSen,isMultiword,literals;
	private JRadioButton fullmatch,find;
	private JLabel fileLabel;
	private String selectString=""; // 1 for select and 0 for not select like 0001
	private boolean optionDisable = false;
	
	
	public SearchOptionDialog() {
		
		createDialog();
		
	}; // Constructor
	
	public SearchOptionDialog(boolean optionDisable) {
		this.optionDisable = optionDisable;
		createDialog();
		
	}; // Constructor

	private void createDialog() {
		
		JPanel topPanel = new JPanel();
		SpringLayout layout = new SpringLayout();
		topPanel.setLayout(layout);
		
		JLabel infoLabel = new JLabel("Information Panel:");
		topPanel.add(infoLabel);
		
		infoBox.setEditable(false);
		JScrollPane js = new JScrollPane(infoBox);
		js.setPreferredSize(new Dimension(350,200));
		js.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		js.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
		
		topPanel.add(js);
		
		isCaseinSen = new JCheckBox("Case Sensitive");
		isCaseinSen.setSelected(true);
		JButton caseB = new JButton("Info");
		caseB.setActionCommand("caseb");
		caseB.addActionListener(this);
		topPanel.add(isCaseinSen);
		topPanel.add(caseB);
		
		isMultiword = new JCheckBox("Multi Word");
		JButton multiB = new JButton("Info");
		multiB.setActionCommand("multib");
		multiB.addActionListener(this);
		topPanel.add(isMultiword);
		topPanel.add(multiB);
		
		literals = new JCheckBox("Exact Match");
		JButton literalB = new JButton("Info");
		literalB.setActionCommand("literalb");
		literalB.addActionListener(this);
		topPanel.add(literals);
		topPanel.add(literalB);
		
		fullmatch = new JRadioButton("Match full Sequence");
		fullmatch.setSelected(true);
		JButton matchB = new JButton("Info");
		matchB.setActionCommand("matchb");
		matchB.addActionListener(this);
		topPanel.add(fullmatch);
		topPanel.add(matchB);
		
		
		find = new JRadioButton("Find in Sequence");
		JButton findB = new JButton("Info");
		findB.setActionCommand("findb");
		findB.addActionListener(this);
		topPanel.add(find);
		topPanel.add(findB);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(find); bg.add(fullmatch);
		
		JLabel fileSelection = new JLabel("Choose a file which has Key Value format like \n \"resource/searchReplace.txt\" ");
		JButton fileSB = new JButton("Select File");
		fileSB.setActionCommand("selectfile");
		fileSB.addActionListener(this);
		topPanel.add(fileSelection);
		topPanel.add(fileSB);
		
		fileLabel = new JLabel("Selected File:");
		topPanel.add(fileLabel);
		
		JButton ok = new JButton("OK");
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		topPanel.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		topPanel.add(cancel);
		
		// Set the layout
		layout.putConstraint(SpringLayout.WEST, infoLabel, 15, SpringLayout.EAST, caseB);
		layout.putConstraint(SpringLayout.NORTH, infoLabel, 8, SpringLayout.NORTH,topPanel);
		layout.putConstraint(SpringLayout.WEST, js, 15, SpringLayout.EAST, caseB);
		layout.putConstraint(SpringLayout.NORTH, js, 5, SpringLayout.SOUTH,infoLabel);
		
		layout.putConstraint(SpringLayout.WEST, isCaseinSen, 5, SpringLayout.WEST, topPanel);
		layout.putConstraint(SpringLayout.NORTH, isCaseinSen, 8, SpringLayout.NORTH,topPanel);
		layout.putConstraint(SpringLayout.WEST, caseB, 5, SpringLayout.EAST, fullmatch);
		layout.putConstraint(SpringLayout.SOUTH, caseB, 0, SpringLayout.SOUTH,isCaseinSen);
		
		layout.putConstraint(SpringLayout.WEST, isMultiword, 5, SpringLayout.WEST, topPanel);
		layout.putConstraint(SpringLayout.NORTH, isMultiword, 8, SpringLayout.SOUTH,caseB);
		layout.putConstraint(SpringLayout.WEST, multiB, 5, SpringLayout.EAST, fullmatch);
		layout.putConstraint(SpringLayout.SOUTH, multiB, 0, SpringLayout.SOUTH,isMultiword);
		
		layout.putConstraint(SpringLayout.WEST, literals, 5, SpringLayout.WEST, topPanel);
		layout.putConstraint(SpringLayout.NORTH, literals, 8, SpringLayout.SOUTH,multiB);
		layout.putConstraint(SpringLayout.WEST, literalB, 5, SpringLayout.EAST, fullmatch);
		layout.putConstraint(SpringLayout.SOUTH, literalB, 0, SpringLayout.SOUTH,literals);
		
		layout.putConstraint(SpringLayout.WEST, fullmatch, 5, SpringLayout.WEST, topPanel);
		layout.putConstraint(SpringLayout.NORTH, fullmatch, 8, SpringLayout.SOUTH,literalB);
		layout.putConstraint(SpringLayout.WEST, matchB, 5, SpringLayout.EAST, fullmatch);
		layout.putConstraint(SpringLayout.SOUTH, matchB, 0, SpringLayout.SOUTH,fullmatch);
		
		layout.putConstraint(SpringLayout.WEST, find, 5, SpringLayout.WEST, topPanel);
		layout.putConstraint(SpringLayout.NORTH, find, 8, SpringLayout.SOUTH,matchB);
		layout.putConstraint(SpringLayout.WEST, findB, 5, SpringLayout.EAST, fullmatch);
		layout.putConstraint(SpringLayout.SOUTH, findB, 0, SpringLayout.SOUTH,find);
		
		layout.putConstraint(SpringLayout.WEST, fileSelection, 5, SpringLayout.WEST, topPanel);
		layout.putConstraint(SpringLayout.NORTH, fileSelection, 18, SpringLayout.SOUTH,js);
		layout.putConstraint(SpringLayout.WEST, fileSB, 5, SpringLayout.EAST, fileSelection);
		layout.putConstraint(SpringLayout.SOUTH, fileSB, 0, SpringLayout.SOUTH,fileSelection);
		layout.putConstraint(SpringLayout.WEST, fileLabel, 0, SpringLayout.WEST, fileSelection);
		layout.putConstraint(SpringLayout.NORTH, fileLabel, 5, SpringLayout.SOUTH,fileSelection);
		
		layout.putConstraint(SpringLayout.WEST, ok, 35, SpringLayout.WEST, topPanel);
		layout.putConstraint(SpringLayout.NORTH, ok, 35, SpringLayout.SOUTH,fileSB);
		layout.putConstraint(SpringLayout.WEST, cancel, 10, SpringLayout.EAST, ok);
		layout.putConstraint(SpringLayout.SOUTH, cancel, 0, SpringLayout.SOUTH,ok);
		
		if (optionDisable == true )
			disableOption();
		
		jd.setPreferredSize(new Dimension(600,400));
		jd.setTitle("Search Option Dialog");
		jd.getContentPane().add(topPanel);
		jd.setModal(true);
		jd.setLocation(150,100);
		jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		jd.pack();
		jd.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("caseb")) {
			infoBox.setText(" Option for Case InSensitiveness \n" +
					" Examples: \n"+
					"  \"bangalore\" matches value \"BANGALORE\" if UnSelected \n" +
					"  \"bangalore\" does not match value \"Bangalore\" if Selected \n"+
					" Information: \n"+"Key is Case Sensitive if Selected");
			return;
		}
		if (command.equals("multib")) {
			infoBox.setText(" Option for Multi-Word search \n" +
					" Examples: \n"+
					"  \"bangalore is\" matches value \"bangalore is\" if Selected. \n" +
					" If UnSelected key \"bangalore is\" will be matched against \n" +
					" each word of the input string like \"bangalore\" and \"is\" and will fail \n"+
					" Information: \n"+"Key is never split \n so multi-word key will never match\n"+
					" multi-word value if multi-word not selected");
			return;
		}
		if (command.equals("matchb")) {
			infoBox.setText(" Option for Full Sequence Match \n" +
					" Examples: \n"+
					" \"bangalore is\" does not match \"bangalore is nice\" if Selected. \n" +
					" \"bangalore\" does not match \"bangaloreisnice\" if Selected. \n"+
					" \"bangalore is.*\" matches \"bangalore is nice\" if Selected and Exact Match not Selected \n" +
					" Information: \n"+" Full Sequence will match the full string value and not substring");
			return;
		}
		if (command.equals("findb")) {
			infoBox.setText(" Option for Find \n" +
					" Examples: \n"+
					" \"bangalore is\" matches \"bangalore is nice\" if Selected. \n" +
					" \"bangalore\" matches \"bangaloreisnice\" if Selected. \n"+
					" Information: \n"+" Find will match the substring inside the value string also");
			return;
		}
		if (command.equals("literalb")) {
			infoBox.setText(" Option for Exact Match \n" +
					" Examples: \n"+
					" \"bangalore is.*\" matches \"bangalore is nice\" if UnSelected and Mutli-Word Selected. \n" +
					" \"bangalore.*\" does not match \"bangaloreisnice\" if Selected. \n"+
					" Information: \n"+" Exact  Match will treat Key as literal if Selected\n " +
					" Otherwise it treats key as regex");
			return;
		}
		if (command.equals("selectfile")) {
			try {
				 f = FileSelectionUtil.chooseFile("Select Standardisation File");
				 infoBox.setText("Selected File is:"+f.toString());
				 fileLabel.setText("Selected File:"+f.toString());
			} catch (FileNotFoundException fe ) {
				JOptionPane.showMessageDialog(null, fe.getMessage(),
						"File not Found Dialog",
						JOptionPane.ERROR_MESSAGE);
				ConsoleFrame.addText("\n ERROR: Selected File Not Found");
			} catch (Exception fe ) {
				JOptionPane.showMessageDialog(null, "Exception Occured:"+fe.getLocalizedMessage(),
						"Exception Dialog",
						JOptionPane.ERROR_MESSAGE);
				ConsoleFrame.addText("\n ERROR: Selected File Not Found");
			}
			return;
		}
		if (command.equals("ok")) {
			if (f == null) {
				JOptionPane.showMessageDialog(null, "Please select File for Search Replace",
						"File not Found Dialog",
						JOptionPane.ERROR_MESSAGE);
				
				return;
			}
			
			if(isCaseinSen.isSelected() == true)
				selectString="1";
			else 
				selectString="0";
			
			if(isMultiword.isSelected() == true)
				selectString += "1";
			else 
				selectString += "0";
			
			if(literals.isSelected() == true)
				selectString += "1";
			else 
				selectString += "0";
			
			if(fullmatch.isSelected() == true)
				selectString += "1";
			else  // find is selected
				selectString += "0";
			jd.dispose();
			return;
		}
		if (command.equals("cancel")) {
			f = null;
			jd.dispose();
			return;
		}
		
	}
	public File getFile() {
		return f;
	}
	public String getSelectedOption() {
		return selectString;
	}
	
	//This function will disable check box and radio button
	private void disableOption () {
		isCaseinSen.setEnabled(false);isMultiword.setEnabled(false);literals.setEnabled(false);
		fullmatch.setEnabled(false);find.setEnabled(false);
	}
		
}
