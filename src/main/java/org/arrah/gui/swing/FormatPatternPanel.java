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

/* This file is used for inputing multiple 
 * Formats and creating formats.
 * Number, Date, Phone, String
 *
 */
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class FormatPatternPanel implements ActionListener {
	private JDialog d_f = null;
	private Hashtable<String, StringBuffer> _nf, _df;
	private Hashtable<String, Object>   _sf, _pf;
	private JComboBox<String> type_c = null;
	private DefaultListModel<String> all_l_model, input_l_model;
	private JList<String> all_l = null, input_l = null;
	private MyCellRenderer cellR = null;
	private int responseType = 0; // 0 for cancel 1 for Ok
	private String dialogN = "";

	public FormatPatternPanel() {
	};

	public FormatPatternPanel(String dialogName) {
		dialogN = dialogName;
	};

	/* Create dialog for taking input for pattern */
	public int createDialog() {
		type_c = new JComboBox<String>(new String[] { "Number", "Date", "Phone",
				"Formatted String" });
		type_c.addActionListener(this);

		cellR = new MyCellRenderer();

		all_l_model = new DefaultListModel<String>();
		loadFormat(0);
		all_l = new JList<String>(all_l_model);
		JScrollPane all_sp = new JScrollPane(all_l);
		all_l.setCellRenderer(cellR);

		input_l_model = new DefaultListModel<String>();
		input_l = new JList<String>(input_l_model);
		JScrollPane input_sp = new JScrollPane(input_l);
		input_l.setCellRenderer(cellR);
		cellR.setType(0);

		all_sp.setPreferredSize(new Dimension(200, 240));
		input_sp.setPreferredSize(new Dimension(200, 240));

		JButton toTop = new JButton("^");
		toTop.setActionCommand("totop");
		toTop.addActionListener(this);
		JButton toInput = new JButton(">>");
		toInput.setActionCommand("toinput");
		toInput.addActionListener(this);
		JButton fromInput = new JButton("<<");
		fromInput.setActionCommand("frominput");
		fromInput.addActionListener(this);

		JButton ok = new JButton("Ok");
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);

		JLabel rl = new JLabel("(Select Relevant Formats)");
		JLabel rsl = new JLabel("(Selected Formats)");
		JLabel rtl = new JLabel("(Top Selected is Output Format)");

		JPanel dp = new JPanel();
		dp.add(type_c);
		dp.add(toTop);
		dp.add(all_sp);
		dp.add(input_sp);
		dp.add(toInput);
		dp.add(fromInput);
		dp.add(ok);
		dp.add(cancel);
		dp.add(rl);
		dp.add(rsl);
		dp.add(rtl);
		SpringLayout layout = new SpringLayout();
		dp.setLayout(layout);

		layout.putConstraint(SpringLayout.NORTH, type_c, 10,
				SpringLayout.NORTH, dp);
		layout.putConstraint(SpringLayout.WEST, type_c, 0, SpringLayout.WEST,
				all_sp);
		layout.putConstraint(SpringLayout.WEST, rl, 5, SpringLayout.WEST,
				all_sp);
		layout.putConstraint(SpringLayout.NORTH, rl, 5, SpringLayout.SOUTH,
				type_c);
		layout.putConstraint(SpringLayout.NORTH, all_sp, 0, SpringLayout.SOUTH,
				rl);
		layout.putConstraint(SpringLayout.WEST, all_sp, 10, SpringLayout.WEST,
				dp);

		layout.putConstraint(SpringLayout.WEST, toInput, 10, SpringLayout.EAST,
				all_sp);
		layout.putConstraint(SpringLayout.WEST, fromInput, 10,
				SpringLayout.EAST, all_sp);
		layout.putConstraint(SpringLayout.NORTH, toInput, 10,
				SpringLayout.NORTH, all_sp);
		layout.putConstraint(SpringLayout.SOUTH, fromInput, -10,
				SpringLayout.SOUTH, all_sp);
		layout.putConstraint(SpringLayout.NORTH, input_sp, 0,
				SpringLayout.SOUTH, rsl);
		layout.putConstraint(SpringLayout.WEST, input_sp, 10,
				SpringLayout.EAST, toInput);
		layout.putConstraint(SpringLayout.WEST, rsl, 5, SpringLayout.WEST,
				input_sp);
		layout.putConstraint(SpringLayout.NORTH, rsl, 5, SpringLayout.SOUTH,
				type_c);
		layout.putConstraint(SpringLayout.WEST, rtl, 5, SpringLayout.WEST,
				input_sp);
		layout.putConstraint(SpringLayout.NORTH, rtl, 0, SpringLayout.SOUTH,
				input_sp);

		layout.putConstraint(SpringLayout.NORTH, toTop, 20, SpringLayout.NORTH,
				all_sp);
		layout.putConstraint(SpringLayout.WEST, toTop, 10, SpringLayout.EAST,
				input_sp);

		layout.putConstraint(SpringLayout.WEST, ok, 0, SpringLayout.WEST,
				all_sp);
		layout.putConstraint(SpringLayout.WEST, cancel, 10, SpringLayout.EAST,
				ok);
		layout.putConstraint(SpringLayout.NORTH, cancel, 10,
				SpringLayout.SOUTH, all_sp);
		layout.putConstraint(SpringLayout.NORTH, ok, 10, SpringLayout.SOUTH,
				all_sp);
		layout.putConstraint(SpringLayout.EAST, dp, 10, SpringLayout.EAST,
				toTop);
		layout.putConstraint(SpringLayout.SOUTH, dp, 20, SpringLayout.SOUTH,
				cancel);

		d_f = new JDialog();
		d_f.setModal(true);
		d_f.setTitle("Format Dialog:" + dialogN);
		d_f.setLocation(250, 50);
		d_f.getContentPane().add(dp);
		d_f.pack();
		d_f.setVisible(true);

		return responseType;
	}

	// Load Format from tables and load in Hashtable
	private void loadFormat(int classId) {
		getNumberTable();
		if (_nf == null)
			return;
		Enumeration<String> e = _nf.keys();
		while (e.hasMoreElements()) {
			all_l_model.addElement(e.nextElement());
		}
	}

	private void refreshFormat(int classId) {
		Enumeration<String> e = null;
		input_l_model.clear();
		all_l_model.clear();

		switch (classId) {
		case 0:
			if (_nf != null)
				e = _nf.keys();
			break;
		case 1:
			if (_df != null)
				e = _df.keys();
			break;
		case 2:
			if (_pf != null)
				e = _pf.keys();
			break;
		case 3:
			if (_sf != null)
				e = _sf.keys();
			break;
		default:
			break;
		}
		if (e == null)
			return;
		cellR.setType(classId);
		while (e.hasMoreElements()) {
			all_l_model.addElement(e.nextElement());
		}
	}

	// Return the input format to calling class
	public Object[] inputPatterns() {
		return input_l_model.toArray();
	}

	private void saveFormatFile(String fileName) {
		try {
			FileOutputStream fileOut = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(_nf);
			out.writeObject(_df);
			out.writeObject(_pf);
			out.writeObject(_sf);
			out.close();
			fileOut.close();
		} catch (FileNotFoundException file_exp) {
			JOptionPane.showMessageDialog(null, file_exp.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} catch (IOException exp) {
			JOptionPane.showMessageDialog(null, exp.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} catch (Exception cl_exp) {
			JOptionPane.showMessageDialog(null, cl_exp.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void loadFormatFile(String fileName) {
		try {
			// Open the file and load Hashtable
			Path path = FileSystems.getDefault().getPath(fileName);
			File formatFile = path.toFile();
			InputStream fileIn = new FileInputStream(formatFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			_nf = (Hashtable<String, StringBuffer>) in.readObject();
			_df = (Hashtable<String, StringBuffer>) in.readObject();
			_pf = (Hashtable<String, Object>) in.readObject();
			_sf = (Hashtable<String, Object>) in.readObject();
			in.close();
			fileIn.close();
		} catch (FileNotFoundException file_exp) {
			JOptionPane.showMessageDialog(null, file_exp.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} catch (IOException exp) {
			JOptionPane.showMessageDialog(null, exp.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} catch (ClassNotFoundException cl_exp) {
			JOptionPane.showMessageDialog(null, cl_exp.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		} catch (Exception cl_exp) {
			JOptionPane.showMessageDialog(null, cl_exp.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
		}
	}

	public Hashtable<String, StringBuffer> getNumberTable() {
		loadFormatFile("resource/formatFile.atc");
		return _nf;
	}

	public void setNumberTable(Hashtable<String, StringBuffer> nt) {
		_nf = nt;
		saveFormatFile("resource/formatFile.atc");

	}

	public Hashtable<String, StringBuffer> getDateTable() {
		loadFormatFile("resource/formatFile.atc");
		return _df;
	}

	public void setDateTable(Hashtable<String, StringBuffer> dt) {
		_df = dt;
		saveFormatFile("resource/formatFile.atc");
	}

	public Hashtable<String, Object> getStringTable() {
		loadFormatFile("resource/formatFile.atc");
		return _sf;
	}

	public void setStringTable(Hashtable<String, Object> st) {
		_sf = st;
		saveFormatFile("resource/formatFile.atc");
	}

	public Hashtable<String, Object> getPhoneTable() {
		loadFormatFile("resource/formatFile.atc");
		return _pf;
	}

	public void setPhoneTable(Hashtable<String, Object> pt) {
		_pf = pt;
		saveFormatFile("resource/formatFile.atc");
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox<?>) {
			int index = ((JComboBox<?>) e.getSource()).getSelectedIndex();
			refreshFormat(index);
		}
		String command = e.getActionCommand();
		if (command.equals("toinput")) {
			moveItem(all_l, input_l);
			return;
		}
		if (command.equals("frominput")) {
			moveItem(input_l, all_l);
			return;
		}
		if (command.equals("totop")) {
			Object obj = input_l.getSelectedValue();
			if (obj == null)
				return;
			if (input_l_model.removeElement(obj) == true)
				input_l_model.insertElementAt(obj.toString(), 0);
			return;
		}
		if (command.equals("cancel")) {
			responseType = 0;
			d_f.dispose();
			return;
		}
		if (command.equals("ok")) {
			responseType = 1;
			d_f.dispose();
			return;
		}
	}

	private void moveItem(JList<String> source, JList<String> destination) {
		int[] indices = source.getSelectedIndices();
		DefaultListModel<String> source_m = (DefaultListModel<String>) source.getModel();
		DefaultListModel <String> dest_m = (DefaultListModel<String>) destination.getModel();

		/* delete/add from bottom so that index does not change */
		for (int i = indices.length - 1; i >= 0; i--) {
			dest_m.addElement(source_m.elementAt(indices[i]));
			source_m.removeElementAt(indices[i]);
		}
	}

	public String getType() {
		return (String) type_c.getSelectedItem();
	}

	/* For Showing the tooltip value */
	private class MyCellRenderer extends DefaultListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int listType = 0; // Different label for different type
		private Hashtable<?,?> tipValue = null;

		public void setType(int type) {
			listType = type;
			switch (listType) {
			case 0:
				tipValue = _nf;
				break;
			case 1:
				tipValue = _df;
				break;
			case 2:
				tipValue = _pf;
				break;
			case 3:
				tipValue = _sf;
				break;
			default:
				break;
			}
		}

		public Component getListCellRendererComponent(JList<?> list, Object value, // value
																				// to
																				// display
				int index, // cell index
				boolean isSelected, // is the cell selected
				boolean cellHasFocus) // the list and the cell have the focus
		{
			Component c = super.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);
			if (tipValue != null && c != null)
				((JComponent) c).setToolTipText(tipValue.get(value).toString());
			return c;
		}
	}

}
