package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
 *                                             *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for creating Tools menu
 * callbacks 
 *
 */

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.arrah.framework.dataquality.FormatCheck;

public class ToolListener implements ActionListener {
	private JTextField f_t, e_t;
	private int classIndex = -1; // 0 for Number, 1 for Date so on
	private JDialog d_f;
	private JComponent _parentC;

	public ToolListener(JComponent parentC) {
		_parentC = parentC; // for Cursor Change
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JMenuItem) {
			try {
				if (_parentC != null)
					_parentC.getTopLevelAncestor()
							.setCursor(
									java.awt.Cursor
											.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

				String source = ((JMenuItem) (e.getSource())).getText();
				if (source.equals("SQL Interface")) {
					new SqlTablePanel();
					return;
				}
				if (source.equals("Create Table")) {
					new CreateTableDialog();
					return;
				}
				if (source.equals("Text Format") || source.equals("XML Format") || source.equals("XLS Format")) {
					new ImportFilePanel(true);
					return;
				}
				if (source.equals("OpenCSV Format")) {
					new ImportFilePanel(true,1 ); // 1 for OpenCSV
					return;
				}
				
				if (source.equals("Single File Match") || source.equals("Multiple File Match") ||
					source.equals("1:1 Record Linkage") ||	source.equals("1:N Record Linkage") ||
					source.equals("Single File Merge") || source.equals("Multiple File Merge") ||
					source.equals("Diff File") ) {
					
					ReportTable firstRT = null, secondRT= null;
					JOptionPane.showMessageDialog(null, "Select the First File");
					ImportFilePanel firstFile = new ImportFilePanel(false);
					if (firstFile != null ) 
						firstRT = firstFile.getTable();
					if (firstRT == null) {
						JOptionPane.showMessageDialog(null, "Selected File has no data", "Invalid Input", JOptionPane.ERROR_MESSAGE);
						ConsoleFrame.addText("\n Invalid File Format");
						return;
					}
	
					if ( source.equals("1:1 Record Linkage") || source.equals("1:N Record Linkage") || 
							source.equals("Multiple File Match") || source.equals("Multiple File Merge")
							|| source.equals("Diff File") ) {
						JOptionPane.showMessageDialog(null, "Select the Second File");
						ImportFilePanel secondFile = new ImportFilePanel(false);
						if (secondFile != null ) 
							secondRT = secondFile.getTable();
						if (secondRT == null) {
							JOptionPane.showMessageDialog(null, "File has no data", "Invalid Input", JOptionPane.ERROR_MESSAGE);
							ConsoleFrame.addText("\n Invalid File Format");
							return;
						}
					}
					if (source.equals("Diff File")) {
						new CompareFileDialog(firstRT.getRTMModel(), secondRT.getRTMModel());
						return;
					}
					CompareRecordDialog crd=null;
					if (source.equals("Single File Match") || source.equals("Multiple File Match") == true)
						crd = new CompareRecordDialog(firstRT, secondRT, 0); // 0 for Match // 1 for linkage
					else if ( source.equals("1:N Record Linkage") )
						crd = new CompareRecordDialog(firstRT, secondRT, 1);
					else if ( source.equals("1:1 Record Linkage") )
						crd = new CompareRecordDialog(firstRT, secondRT, 4); // 4 for Inner Join
					else if ( source.equals("Single File Merge") || source.equals("Multiple File Merge") )
						crd = new CompareRecordDialog(firstRT, secondRT, 2); // 2 for Merge
					
					crd.createMapDialog();
						
					return;
				}
				if (source.equals("Multi-Line Format")) {
					new MultiLineInputPanel();
					return;
				}
				if ( source.equals("To HDFS")) {
					new HDFSFileTransferFrame(true); // copy to HDFS
					return;
				}
				if (source.equals("From HDFS") ) {
					new HDFSFileTransferFrame(false); // copy from HDFS
					return;
				}
				if (source.equals("Create Regex")) {
					 new RegexPanel();
					return;
				}
				if (source.equals("Create Standardization Value")) {
					JOptionPane.showMessageDialog(null, "Choose a file which has \n key val pair of standardization values");
					 new StandardizePanel();
					return;
				}
				if (source.equals("Search DB")) {
					final String input = JOptionPane
							.showInputDialog("Enter String to Search Database","Search String");
					if (input == null || "".equals(input))
						return;
					new SearchDBPanel(input);
					return;
				}
				if (source.equals("Search Table")) {
					final String input = JOptionPane
							.showInputDialog("Enter String to Search Table","Search String");
					if (input == null || "".equals(input))
						return;
					TableSearchDialog tsd = new TableSearchDialog(input);
					tsd.createMapDialog();
					return;
				}
				if (source.equals("Fuzzy Search")) {
					final String input = JOptionPane
							.showInputDialog("Enter String to Search Table","Similar Search String");
					if (input == null || "".equals(input))
						return;
					TableSearchDialog tsd = new TableSearchDialog(input,true);
					tsd.createMapDialog();
					return;
				}
				if (source.equals("Multi Facet Search")) {
					
					TableSearchDialog tsd = new TableSearchDialog(true);
					tsd.createMapDialog();
					return;
				}
				if (source.equals("Create Format")) {
					String[] type = { "Number", "Date", "Phone",
							"Formatted String" };
					String s_type = (String) JOptionPane.showInputDialog(null,
							"Choose Format Class", "Format Class Dialog",
							JOptionPane.QUESTION_MESSAGE, null, type, type[0]);
					if (s_type == null || "".equals(s_type))
						return;

					for (int i = 0; i < type.length; i++)
						if (type[i].equals(s_type)) {
							classIndex = i;
							break;
						}
					createFD(classIndex);
					return;
				}
			} finally {
				if (_parentC != null)
					_parentC.getTopLevelAncestor()
							.setCursor(
									java.awt.Cursor
											.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
		}// End of Menu Item
		else {
			String command = e.getActionCommand();
			if (command.equals("validate")) {

				if (classIndex == 0) { // Start of Number
					validateNumber(f_t.getText(), e_t.getText());
					return;
				} // End of Number

				if (classIndex == 1) { // Start of Date
					validateDate(f_t.getText(), e_t.getText());
					return;
				} // End of Date
				if (classIndex == 2) { // Start of Phone
					validatePhone(f_t.getText(), e_t.getText());
					return;
				} // End of Phone
				if (classIndex == 3) { // Start of Format String
					validateString(f_t.getText(), e_t.getText());
					return;
				} // End of Format String
			}
			if (command.equals("cancel"))
				d_f.dispose();

		}
	}// End of Action Performed

	private void createFD(int classID) {
		JPanel fd_panel = new JPanel();
		SpringLayout layout = new SpringLayout();
		fd_panel.setLayout(layout);

		JEditorPane editorPane = createEditorPane(classID);
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(600, 400));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));

		JLabel f_l = new JLabel("Format:", JLabel.TRAILING);
		JLabel e_l = new JLabel("Example:", JLabel.TRAILING);
		f_t = new JTextField("Format String", 30);
		e_t = new JTextField("Example", 30);
		JButton validate = new JButton("Validate");
		validate.setActionCommand("validate");
		validate.addActionListener(this);
		validate.addKeyListener(new KeyBoardListener());
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());

		fd_panel.add(editorScrollPane);
		fd_panel.add(f_l);
		fd_panel.add(e_l);
		fd_panel.add(f_t);
		fd_panel.add(e_t);
		fd_panel.add(validate);
		fd_panel.add(cancel);

		layout.putConstraint(SpringLayout.WEST, editorScrollPane, 3,
				SpringLayout.WEST, fd_panel);
		layout.putConstraint(SpringLayout.NORTH, editorScrollPane, 3,
				SpringLayout.NORTH, fd_panel);
		layout.putConstraint(SpringLayout.WEST, f_l, 6, SpringLayout.WEST,
				fd_panel);
		layout.putConstraint(SpringLayout.NORTH, f_l, 6, SpringLayout.SOUTH,
				editorScrollPane);
		layout.putConstraint(SpringLayout.WEST, f_t, 3, SpringLayout.EAST, f_l);
		layout.putConstraint(SpringLayout.NORTH, f_t, 0, SpringLayout.NORTH,
				f_l);
		layout.putConstraint(SpringLayout.WEST, e_l, 6, SpringLayout.WEST,
				fd_panel);
		layout.putConstraint(SpringLayout.NORTH, e_l, 6, SpringLayout.SOUTH,
				f_l);
		layout.putConstraint(SpringLayout.WEST, e_t, 3, SpringLayout.EAST, e_l);
		layout.putConstraint(SpringLayout.NORTH, e_t, 0, SpringLayout.NORTH,
				e_l);

		layout.putConstraint(SpringLayout.WEST, validate, 16,
				SpringLayout.WEST, editorScrollPane);
		layout.putConstraint(SpringLayout.NORTH, validate, 10,
				SpringLayout.SOUTH, e_t);
		layout.putConstraint(SpringLayout.WEST, cancel, 5, SpringLayout.EAST,
				validate);
		layout.putConstraint(SpringLayout.NORTH, cancel, 10,
				SpringLayout.SOUTH, e_t);
		layout.putConstraint(SpringLayout.SOUTH, fd_panel, 150,
				SpringLayout.SOUTH, editorScrollPane);
		layout.putConstraint(SpringLayout.EAST, fd_panel, 10,
				SpringLayout.EAST, editorScrollPane);

		d_f = new JDialog();
		d_f.setModal(true);
		switch (classID) {
		case 0:
			d_f.setTitle("Format Dialog: Number Format");
			break;
		case 1:
			d_f.setTitle("Format Dialog: Date Format");
			break;
		case 2:
			d_f.setTitle("Format Dialog: Phone Format");
			break;
		case 3:
			d_f.setTitle("Format Dialog: String Mask Format");
			break;
		default:
			d_f.setTitle("Format Dialog");
		}
		d_f.setLocation(250, 50);
		d_f.getContentPane().add(fd_panel);
		d_f.pack();
		d_f.setVisible(true);
	}

	private JEditorPane createEditorPane(int classID) {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		String fileN = null;

		switch (classID) {
		case 0:
			fileN = "htmls/DecimalFormatHelp.html";
			break;
		case 1:
			fileN = "htmls/DateFormatHelp.html";
			break;
		case 2:
			fileN = "htmls/PhoneFormatHelp.html";
			break;
		case 3:
			fileN = "htmls/MaskFormatHelp.html";
			break;
		default:
			break;
		}
		
		java.net.URL helpURL = ToolListener.class.getClassLoader().getResource(fileN);
		if (helpURL != null) {
			try {
				editorPane.setPage(helpURL);
			} catch (IOException e) {
				ConsoleFrame.addText("\n Attempted to read a bad URL: "
						+ helpURL);
			}
		} else {
			ConsoleFrame.addText("\n Couldn't find file:" + fileN);
		}
		return editorPane;
	}

	private void validateNumber(String format, String number) {

		Number n = FormatCheck.validateNumber(format, number);

		if (n != null) {
			DecimalFormat form = new DecimalFormat(format.trim());
			StringBuffer output = form.format(n, new StringBuffer(),
					new FieldPosition(0));
			if (output == null || "".equals(output))
				return;

			int n1 = JOptionPane.showConfirmDialog(null, "Valid Format:"
					+ output + "\n Do you want to save this format ?",
					"Format Save Dialog", JOptionPane.YES_NO_OPTION);
			if (n1 == JOptionPane.YES_OPTION) {
				FormatPatternPanel fp = new FormatPatternPanel();
				Hashtable<String, StringBuffer> ht = fp.getNumberTable();
				if (ht == null)
					ht = new Hashtable<String, StringBuffer>();
				ht.put(f_t.getText().trim(), output);
				fp.setNumberTable(ht);
				JOptionPane.showMessageDialog(null, "Number Format Saved");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Invalid Format",
					"Format Validation Dialog", JOptionPane.INFORMATION_MESSAGE);
			
			return;
		}

	}

	private void validateDate(String format, String date) {
		Date d = FormatCheck.validateDate(format, date);

		if (d != null) {
			SimpleDateFormat form = new SimpleDateFormat(format.trim());
			StringBuffer output = form.format(d, new StringBuffer(),
					new FieldPosition(0));
			if (output == null || "".equals(output))
				return;
			int n = JOptionPane.showConfirmDialog(null, "Valid Format:"
					+ output + "\n Do you want to save this format ?",
					"Format Save Dialog", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				FormatPatternPanel fp = new FormatPatternPanel();
				Hashtable<String, StringBuffer> ht = fp.getDateTable();
				if (ht == null)
					ht = new Hashtable<String, StringBuffer>();
				ht.put(f_t.getText().trim(), output);
				fp.setDateTable(ht);
				JOptionPane.showMessageDialog(null, "Date Format Saved");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Invalid Format",
					"Format Validation Dialog", JOptionPane.INFORMATION_MESSAGE);
			
			return;
		}
	}

	private void validateString(String format, String str) {

		Object v = FormatCheck.validateString(format, str);
		if (v != null) {
			int n = JOptionPane.showConfirmDialog(null, "Valid Format:" + v
					+ "\n Do you want to save this format ?",
					"Format Save Dialog", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				FormatPatternPanel fp = new FormatPatternPanel();
				Hashtable<String, Object> ht = fp.getStringTable();
				if (ht == null)
					ht = new Hashtable<String, Object>();
				ht.put(f_t.getText().trim(), v);
				fp.setStringTable(ht);
				JOptionPane.showMessageDialog(null, "String Format Saved");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Invalid Format",
					"Format Validation Dialog", JOptionPane.INFORMATION_MESSAGE);
			
			return;
		}
	}

	private void validatePhone(String format, String phone) {
		Object v = FormatCheck.validatePhone(format, phone);
		if (v != null) {
			int n = JOptionPane.showConfirmDialog(null, "Valid Format:" + v
					+ "\n Do you want to save this format ?",
					"Format Save Dialog", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				FormatPatternPanel fp = new FormatPatternPanel();
				Hashtable<String, Object> ht = fp.getPhoneTable();
				if (ht == null)
					ht = new Hashtable<String, Object>();
				ht.put(f_t.getText().trim(), v);
				fp.setPhoneTable(ht);
				JOptionPane.showMessageDialog(null, "Phone Format Saved");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Invalid Format",
					"Format Validation Dialog", JOptionPane.INFORMATION_MESSAGE);
			
			return;
		}
	}
}
