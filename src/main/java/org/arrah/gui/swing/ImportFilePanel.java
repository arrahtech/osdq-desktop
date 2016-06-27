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

/* This file is used for importing user defined  
 * textfile into ReportTable and analysing it.
 *
 */

// For Reading File
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

import org.arrah.framework.ndtable.CSVtoReportTableModel;
import org.arrah.framework.ndtable.ColumnAttr;
import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.xls.XlsReader;
import org.arrah.framework.xml.XmlReader;

public class ImportFilePanel implements ItemListener, ActionListener {
	private String FIELD_SEP = ",";
	private String COMMENT_STR = "#";
	private File f = null;
	private ReportTable showT;
	private JCheckBox f_s, w_s, s_c, skip_c, preview_c, hd, comment, rowCount_c;
	private BufferedReader br;
	private JScrollPane table_s;
	private JButton vw_b, previous;
	private JLabel l, status_b, a_status;
	private JTextField n, s, cv, of;
	private Vector<ColumnAttr> vc;
	private int vcIndex;
	private JFormattedTextField w, skip_r, fw_c, preview_r, rowCount_r;
	private JDialog d, d_f;
	private JRadioButton fw, vw, ws, cm, ot, ao;
	private boolean _showGUI;

	public ImportFilePanel(boolean isGUI) {
		_showGUI = isGUI;

		try {
			f = FileSelectionUtil.chooseFile("ATD Open File");
			if (f == null)
				return;
			if (f.getName().toLowerCase().endsWith(".xml")) {
				final XmlReader xmlReader = new XmlReader();
				showT = new ReportTable(xmlReader.read(f));
				if (_showGUI == true) {
					DisplayFileTable dft = new DisplayFileTable(showT,
							f.toString());
					dft.showGUI();
				}
			} else if (f.getName().toLowerCase().endsWith(".xls")) {
				final XlsReader xlsReader = new XlsReader();
				showT = new ReportTable(xlsReader.read(f));
				if (_showGUI == true) {
					DisplayFileTable dft = new DisplayFileTable(showT,
							f.toString());
					dft.showGUI();
				}
			} else if (f.getName().toLowerCase().endsWith(".csv")){
				CSVtoReportTableModel csvReader = new CSVtoReportTableModel(f);
				showT = new ReportTable(csvReader.loadOpenCSVIntoTable());
				if (_showGUI == true) {
					DisplayFileTable dft = new DisplayFileTable(showT,
							f.toString());
					dft.showGUI();
				}
			}
			else {
				takeOptions(f);
				createIDialog();
			}
		} catch (FileNotFoundException file_e) {

		} catch (IOException io_e) {
		}

	};
	// This constructor is for openCSV
	// ideally it should be merged with above 
	// if someone has csv format it is expected it will use openCSV format
	public ImportFilePanel(boolean isGUI, int fileType) {
		_showGUI = isGUI;

		try {
			f = FileSelectionUtil.chooseFile("ATD Open File");
			if (f == null)
				return;
			
			if (f.getName().toLowerCase().endsWith(".csv")) {
				CSVtoReportTableModel csvReader = new CSVtoReportTableModel(f);
				showT = new ReportTable(csvReader.loadOpenCSVIntoTable());
				if (_showGUI == true) {
					DisplayFileTable dft = new DisplayFileTable(showT,
							f.toString());
					dft.showGUI();
				}
			}
			
		} catch (Exception file_e) {

		} 
	}

	private void takeOptions(File f) throws IOException {
		if (f == null)
			return;
		int count = 0;
		String line = null;
		showT = new ReportTable(new String[] { "Column Name" });
		br = new BufferedReader(new FileReader(f));
		while ((line = br.readLine()) != null && (count < 15)) {
			count++;
			showT.addFillRow(new String[] { line });

		}
		br.close();
		return;

	}

	private void createIDialog() {
		/*
		 * Create a dialog box to take input A ScrollPane to hold ReportTable
		 * Option to choose Field Separator Or Option to choose Field width
		 */

		f_s = new JCheckBox("Field Separator");
		f_s.setSelected(true);
		f_s.addItemListener(this);
		ButtonGroup gp1 = new ButtonGroup();
		ws = new JRadioButton("White Space");
		cm = new JRadioButton("Comma");
		cm.setSelected(true);
		ot = new JRadioButton("Others");
		ao = new JRadioButton("Advance Options");
		gp1.add(ws);
		gp1.add(cm);
		gp1.add(ot);
		gp1.add(ao);
		of = new JTextField(";");
		JPanel p1 = new JPanel(new GridLayout(4, 2));
		p1.add(f_s);
		p1.add(new JLabel());
		p1.add(ws);
		p1.add(cm);
		p1.add(ot);
		p1.add(of);
		p1.add(ao);
		Border line_b = BorderFactory.createLineBorder(Color.BLACK);
		p1.setBorder(BorderFactory
				.createTitledBorder(line_b, "Field Separator"));

		w_s = new JCheckBox("Width Separator");
		w_s.setEnabled(false);
		w_s.addItemListener(this);
		ButtonGroup gp2 = new ButtonGroup();
		fw = new JRadioButton("Fixed Width");
		fw.setSelected(true);
		fw.setEnabled(false);
		vw = new JRadioButton("Advance Options");
		vw.setEnabled(false);
		gp2.add(fw);
		gp2.add(vw);
		fw_c = new JFormattedTextField(NumberFormat.getIntegerInstance());
		fw_c.setValue(new Integer(100));
		fw_c.setEnabled(false);
		JPanel p2 = new JPanel(new GridLayout(3, 2));
		p2.add(w_s);
		p2.add(new JLabel());
		p2.add(fw);
		p2.add(fw_c);
		p2.add(vw);
		p2.setBorder(BorderFactory
				.createTitledBorder(line_b, "Width Separator"));

		JPanel p3 = new JPanel(new GridLayout(4, 1));
		s_c = new JCheckBox("Lenient Parsing");
		s_c.setSelected(true);
		hd = new JCheckBox("First Row Column Name");
		hd.setSelected(true);
		JLabel i_info = new JLabel("    If above CheckBox is Unselected");
		i_info.setEnabled(false);
		JLabel i_info_2 = new JLabel("  Click Advance Options to input values");
		i_info_2.setEnabled(false);

		p3.add(s_c);
		p3.add(hd);
		p3.add(i_info);
		p3.add(i_info_2);
		p3.setBorder(BorderFactory.createTitledBorder(line_b,
				"File Information"));

		comment = new JCheckBox("Comment String");
		comment.addItemListener(this);
		cv = new JTextField("#");
		cv.setEnabled(false);
		skip_c = new JCheckBox("Skip Rows");
		skip_c.addItemListener(this);
		skip_r = new JFormattedTextField(NumberFormat.getIntegerInstance());
		skip_r.setValue(new Integer(0));
		skip_r.setEnabled(false);
		preview_c = new JCheckBox("Preview Rows");
		preview_c.addItemListener(this);
		preview_r = new JFormattedTextField(NumberFormat.getIntegerInstance());
		preview_r.setValue(new Integer(15));
		preview_r.setEnabled(false);
		
		rowCount_c = new JCheckBox("Display Rows");
		rowCount_c.addItemListener(this);
		rowCount_r = new JFormattedTextField(NumberFormat.getIntegerInstance());
		rowCount_r.setValue(new Integer(100));
		rowCount_r.setEnabled(false);
		JPanel p4 = new JPanel(new GridLayout(4, 2));
		p4.setBorder(BorderFactory.createTitledBorder(line_b,
				"File Optional Information"));
		p4.add(comment);
		p4.add(cv);
		p4.add(skip_c);
		p4.add(skip_r);
		p4.add(preview_c);
		p4.add(preview_r);
		p4.add(rowCount_c);
		p4.add(rowCount_r);

		vw_b = new JButton("Advance Options");
		vw_b.setActionCommand("datainput");
		vw_b.addActionListener(this);
		a_status = new JLabel("Set for 0 Column");

		JPanel content = new JPanel();
		SpringLayout layout = new SpringLayout();
		content.setLayout(layout);
		table_s = new JScrollPane();
		table_s.setPreferredSize(new Dimension(600, 280));
		table_s.setViewportView(showT);
		content.add(table_s);
		content.add(p1);
		content.add(p2);
		content.add(p3);
		content.add(p4);
		content.add(vw_b);
		content.add(a_status);

		layout.putConstraint(SpringLayout.WEST, table_s, 2, SpringLayout.WEST,content);
		layout.putConstraint(SpringLayout.EAST, content, 2, SpringLayout.EAST,table_s);
		layout.putConstraint(SpringLayout.NORTH, p1, 2, SpringLayout.SOUTH,table_s);
		layout.putConstraint(SpringLayout.NORTH, p2, 2, SpringLayout.SOUTH,table_s);
		layout.putConstraint(SpringLayout.WEST, p2, 2, SpringLayout.EAST, p1);
		
		layout.putConstraint(SpringLayout.NORTH, vw_b, 5, SpringLayout.SOUTH,p2);
		layout.putConstraint(SpringLayout.WEST, vw_b, 20, SpringLayout.EAST, p1);
		layout.putConstraint(SpringLayout.NORTH, a_status, 3,SpringLayout.NORTH, vw_b);
		layout.putConstraint(SpringLayout.WEST, a_status, 3,SpringLayout.EAST, vw_b);

		layout.putConstraint(SpringLayout.NORTH, p3, 2, SpringLayout.SOUTH, p1);
		layout.putConstraint(SpringLayout.EAST, p3, 0, SpringLayout.EAST, p1);
		layout.putConstraint(SpringLayout.WEST, p3, 0, SpringLayout.WEST, p1);

		layout.putConstraint(SpringLayout.NORTH, p4, 2, SpringLayout.SOUTH, vw_b);
		layout.putConstraint(SpringLayout.WEST, p4, 0, SpringLayout.WEST, p2);
		layout.putConstraint(SpringLayout.EAST, p4, 0, SpringLayout.EAST, p2);
		

		JButton orig_b = new JButton("Raw Value");
		orig_b.setActionCommand("original");
		orig_b.addActionListener(this);
		JButton pre_b = new JButton("Preview");
		pre_b.setActionCommand("preview");
		pre_b.addActionListener(this);
		JButton ok_b = new JButton("OK");
		ok_b.setActionCommand("ok");
		ok_b.addActionListener(this);
		JButton cancel_b = new JButton("Cancel");
		cancel_b.setActionCommand("cancel");
		cancel_b.addActionListener(this);
		JPanel b_p = new JPanel();
		b_p.add(orig_b);
		b_p.add(pre_b);
		b_p.add(ok_b);
		b_p.add(cancel_b);
		content.add(b_p);

		layout.putConstraint(SpringLayout.NORTH, b_p, 10, SpringLayout.SOUTH, p3);
		layout.putConstraint(SpringLayout.SOUTH, content, 2,SpringLayout.SOUTH, b_p);

		d_f = new JDialog();
		d_f.setTitle("Import File Option Dialog:" + f);
		d_f.setLocation(250, 50);
		d_f.getContentPane().add(content);
		d_f.setModal(true);
		d_f.pack();
		d_f.setVisible(true);
	}

	public void itemStateChanged(ItemEvent e) {
		String ch_name = ((AbstractButton) e.getSource()).getText();
		if (ch_name.compareTo("Field Separator") == 0) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				w_s.setSelected(false);
				w_s.setEnabled(false);
				vw.setEnabled(false);
				fw.setEnabled(false);
				fw_c.setEnabled(false);
				return;
			}
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				w_s.setEnabled(true);
				vw.setEnabled(true);
				fw.setEnabled(true);
				w_s.setSelected(true);
				fw_c.setEnabled(true);
				return;
			}
		}
		if (ch_name.compareTo("Width Separator") == 0) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				f_s.setEnabled(false);
				f_s.setSelected(false);
				ws.setEnabled(false);
				cm.setEnabled(false);
				ot.setEnabled(false);
				ao.setEnabled(false);
				of.setEnabled(false);
				return;
			}
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				f_s.setEnabled(true);
				f_s.setSelected(true);
				ws.setEnabled(true);
				cm.setEnabled(true);
				ot.setEnabled(true);
				ao.setEnabled(true);
				of.setEnabled(true);
				return;
			}
		}
		if (ch_name.compareTo("Skip Rows") == 0) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				skip_r.setEnabled(true);
			if (e.getStateChange() == ItemEvent.DESELECTED)
				skip_r.setEnabled(false);
			return;
		}
		if (ch_name.compareTo("Comment String") == 0) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				cv.setEnabled(true);
			if (e.getStateChange() == ItemEvent.DESELECTED)
				cv.setEnabled(false);
			return;
		}
		if (ch_name.compareTo("Preview Rows") == 0) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				preview_r.setEnabled(true);
			if (e.getStateChange() == ItemEvent.DESELECTED)
				preview_r.setEnabled(false);
			return;
		}
		if (ch_name.compareTo("Display Rows") == 0) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				rowCount_r.setEnabled(true);
			if (e.getStateChange() == ItemEvent.DESELECTED)
				rowCount_r.setEnabled(false);
			return;
		}

	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("original")) {
			try {
				takeOptions(f);
				table_s.setViewportView(showT);
				return;
			} catch (Exception exp) {
				ConsoleFrame.addText("\n File IO Exception Happened:" + f);
				JOptionPane.showMessageDialog(null, exp.getMessage(),
						"IO Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (command.equals("preview")) {
			loadFileIntoTable(0);
			table_s.setViewportView(showT);
			return;
		}
		if (command.equals("ok")) {
			try{
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				loadFileIntoTable(1);
				
			} finally {
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				d_f.dispose();
			}
			if (_showGUI == true) {
				DisplayFileTable dft = new DisplayFileTable(showT, f.toString());
				dft.showGUI();
			}
			return;
		}
		if (command.equals("cancel")) {
			d_f.dispose();
			showT = null; // make table null
			return;
		}
		if (command.equals("datainput")) {
			d = new JDialog(d_f);
			d.setModal(true);
			d.setTitle("Column Input Dialog");
			d.setLocation(300, 150);
			vc = new Vector<ColumnAttr>();
			vcIndex = 0; // First Column
			d.getContentPane().add(createDPanel());
			d.pack();
			d.setVisible(true);
			return;
		}
		if (command.equals("next")) {
			ColumnAttr att = new ColumnAttr(vcIndex);
			if (n.isEnabled() == true
					&& (n.getText() == null || n.getText().compareTo("") == 0)) {
				status_b.setText("Please select Column Name");
				return;
			} else
				att.setName(n.getText());
			if (s.isEnabled() == true
					&& (s.getText() == null || s.getText().compareTo("") == 0)) {
				status_b.setText("Please select Separator (Regex Allowed)");
				return;
			} else
				att.setSep(s.getText());
			if (w.isEnabled() == true
					&& ((Number) w.getValue()).intValue() <= 0) {
				status_b.setText("Please Enter Positive Number");
				return;
			} else
				att.setWidth(((Number) w.getValue()).intValue());

			status_b.setText("");
			if (previous.isEnabled() == false)
				previous.setEnabled(true);

			if (vcIndex == vc.size())
				vc.add(vcIndex, att);
			else
				vc.set(vcIndex, att);
			vcIndex++;

			if (vcIndex == vc.size()) {
				l.setText("" + (vcIndex + 1));
				if (n.isEnabled() == true) {
					n.setText("Column_" + (vcIndex + 1));
				}
				s.setText(null);
				w.setValue(new Integer(0));
			} else {
				att = (ColumnAttr) vc.get(vcIndex);
				l.setText("" + (vcIndex + 1));
				n.setText(att.getName());
				s.setText(att.getSep());
				w.setValue(new Integer(att.getWidth()));
			}

			return;
		}
		if (command.compareToIgnoreCase("previous") == 0) {
			vcIndex--;
			if (vcIndex == 0)
				previous.setEnabled(false);
			ColumnAttr att = (ColumnAttr) vc.get(vcIndex);
			l.setText("" + (vcIndex + 1));
			n.setText(att.getName());
			s.setText(att.getSep());
			w.setValue(new Integer(att.getWidth()));
			return;

		}
		if (command.compareToIgnoreCase("exit_d") == 0) {
			vc.clear();
			vc = null;
			d.dispose();
			return;
		}
		if (command.compareToIgnoreCase("cancel_input") == 0) {
			ColumnAttr att = new ColumnAttr(vcIndex);
			att.setName(n.getText());
			att.setSep(s.getText());
			att.setWidth(((Number) w.getValue()).intValue());

			if (vcIndex == vc.size())
				vc.add(vcIndex, att);
			else
				vc.set(vcIndex, att);

			d.dispose();
			a_status.setText("Set for " + vc.size() + " Column.");

			return;
		}

	}

	private JPanel createDPanel() {

		JLabel id = new JLabel("ID", JLabel.CENTER);
		JLabel na = new JLabel("Name", JLabel.CENTER);
		JLabel fs = new JLabel("Field Sep.", JLabel.CENTER);
		JLabel cw = new JLabel("Width", JLabel.CENTER);

		l = new JLabel("" + (vcIndex + 1), JLabel.CENTER);
		n = new JTextField();
		if (hd.isSelected() == true)
			n.setEnabled(false);
		else
			n.setText("Column_" + (vcIndex + 1));
		s = new JTextField();
		if (f_s.isSelected() == true && ao.isSelected() == true)
			s.setEnabled(true);
		else
			s.setEnabled(false);
		w = new JFormattedTextField(NumberFormat.getIntegerInstance());
		if (w_s.isSelected() == true && vw.isSelected() == true)
			w.setEnabled(true);
		else
			w.setEnabled(false);
		w.setValue(new Integer(0));

		JPanel p = new JPanel(new GridLayout(2, 4));
		p.add(id);
		p.add(na);
		p.add(fs);
		p.add(cw);
		p.add(l);
		p.add(n);
		p.add(s);
		p.add(w);

		previous = new JButton("Previous");
		previous.setActionCommand("previous");
		previous.addActionListener(this);
		previous.setEnabled(false);
		JButton next = new JButton("Save & Next");
		next.setActionCommand("next");
		next.addActionListener(this);
		JButton cancel = new JButton("Save & Exit");
		cancel.setActionCommand("cancel_input");
		cancel.addActionListener(this);
		JButton exit = new JButton("Cancel");
		exit.setActionCommand("exit_d");
		exit.addActionListener(this);
		JPanel b_p = new JPanel();
		b_p.add(previous);
		b_p.add(next);
		b_p.add(cancel);
		b_p.add(exit);

		JPanel d_p = new JPanel(new GridLayout(3, 0));
		d_p.add(p);
		d_p.add(b_p);
		d_p.add(status_b = new JLabel());
		return d_p;

	}

	private ReportTable loadFileIntoTable(int clickButtonType) {
		CSVtoReportTableModel csv_tableModel = new CSVtoReportTableModel(f);

		boolean fieldSelection = f_s.isSelected();
		boolean commentSelection = comment.isSelected();
		boolean skipRowSelection = skip_c.isSelected();
		boolean previewRowSelection = preview_c.isSelected();
		boolean rowCountSelection = rowCount_c.isSelected();

		int skipRowNumber = 0;
		int previewRowNumber = 15; // will show 15 lines in preview
		int rowCountNumber = 100;

		if (rowCountSelection == true) {
			rowCountNumber = ((Number) rowCount_r.getValue()).intValue();
			if (rowCountNumber <= 0) // Nothing to preview
				rowCountNumber = 100; // Default Value
			csv_tableModel.setDisplayRowSelection(true, rowCountNumber);
		}
		
		if (previewRowSelection == true) {
			previewRowNumber = ((Number) preview_r.getValue()).intValue();
			if (previewRowNumber <= 0) // Nothing to preview
				previewRowNumber = 15; // Default Value
			csv_tableModel.setPreviewRowSelection(true, previewRowNumber);
		}
		if (skipRowSelection == true) {
			skipRowNumber = ((Number) skip_r.getValue()).intValue();
			if (skipRowNumber <= 0) // Nothing to skip
				skipRowSelection = false;
			csv_tableModel.setSkipRowSelection(skipRowSelection, skipRowNumber);
		}
		if (commentSelection == true) {
			COMMENT_STR = cv.getText();
			if (COMMENT_STR == null || COMMENT_STR.equals(""))
				commentSelection = false;
			csv_tableModel.setCommentSelection(commentSelection, COMMENT_STR);
		}
		if (fieldSelection == true) {
			if (ws.isSelected() == true)
				FIELD_SEP = " ";
			if (cm.isSelected() == true)
				FIELD_SEP = ",";
			if (ot.isSelected() == true) {
				FIELD_SEP = of.getText();
				if (FIELD_SEP == null || FIELD_SEP.equals(""))
					FIELD_SEP = ";";
			}
			csv_tableModel.setFieldSelection(true, FIELD_SEP);
			if (ao.isSelected() == true) {
				csv_tableModel.setAdv_fieldSelection(true, vc);
			}
		}
		if (fw.isSelected() == true) {
			int colWidth = ((Number) fw_c.getValue()).intValue();
			csv_tableModel.setfixedWidthSelection(true, colWidth);
		}
		if (vw.isSelected() == true) {
			csv_tableModel.setAdv_widthSelection(true, vc);
		}
		csv_tableModel.setFirstRowColumnName(hd.isSelected(), vc);
		ReportTableModel rpt_model = csv_tableModel
				.loadFileIntoTable(clickButtonType);
		return showT = new ReportTable(rpt_model);
	}

	public ReportTable getTable() {
		return showT;
	}

}
