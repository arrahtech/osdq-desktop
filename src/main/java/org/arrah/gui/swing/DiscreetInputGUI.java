package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2012      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This class is used to implement
 * GUI for inputing discreet range analysis
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class DiscreetInputGUI implements ActionListener {
	private JTextArea inputText;
	private JDialog d_f;
	private int index;
	private JTextField jtf;

	public DiscreetInputGUI() {
		// Constructor
	}

	/* Create dialog for taking input for Discreet Range */
	public void createDialog() {
		JPanel tp = new JPanel();
		JLabel sep = new JLabel("Delimiter:");
		JComboBox<String> type_c = new JComboBox <String>(new String[] { "Comma", "New Line",
				"White Space", "Others" });
		JLabel others = new JLabel("Others:");
		jtf = new JTextField(";");
		jtf.setEnabled(false);
		jtf.setColumns(5);
		type_c.addActionListener(this);
		JLabel lab = new JLabel("Date Format dd-MM-yyyy");
		tp.add(sep);
		tp.add(type_c);
		tp.add(others);
		tp.add(jtf);
		tp.add(lab);

		JPanel cp = new JPanel();
		inputText = new JTextArea();
		inputText.setLineWrap(true);
		inputText.setWrapStyleWord(true);
		JScrollPane js = new JScrollPane(inputText);
		js.setPreferredSize(new Dimension(400, 400));
		cp.add(js);

		JPanel bp = new JPanel();
		JButton ok = new JButton("Ok");
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());

		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		bp.add(cancel);

		JPanel dp = new JPanel(new BorderLayout());
		dp.add(tp, BorderLayout.PAGE_START);
		dp.add(cp, BorderLayout.CENTER);
		dp.add(bp, BorderLayout.PAGE_END);

		d_f = new JDialog();
		d_f.setModal(true);
		d_f.setTitle("Discreet Input Format Dialog");
		d_f.setLocation(250, 50);
		d_f.getContentPane().add(dp);
		d_f.pack();
		d_f.setVisible(true);

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox<?>) {
			index = ((JComboBox<?>) e.getSource()).getSelectedIndex();
			if (index == 3) // Date selected
				jtf.setEnabled(true);
			else
				jtf.setEnabled(false);
		}
		String command = e.getActionCommand();
		if (command.equals("cancel")) {
			inputText.setText(null);
			d_f.dispose();
		}
		if (command.equals("ok")) {
			String val = inputText.getText();
			if (val == null || "".equals(val)) {
				JOptionPane.showMessageDialog(null, "Please input values");
				return;
			}
			d_f.dispose();
		}
	}

	public String getRawText() {
		return inputText.getText();
	}

	public String getDelimiter() {
		switch (index) {
		case 0:
			return ",";
		case 1:
			return "\\n";
		case 2:
			return "\\s";
		case 3:
			if (jtf.getText() ==null || "".equals(jtf.getText())) {
				ConsoleFrame.addText("Default Delimiter is White Space");
				return "\\s";
			}
			return jtf.getText();
		default:
			return "\\s";
		}

	}

}
