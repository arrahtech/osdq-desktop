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

/* 
 * This is standardization panel which will
 * be used to create key- value pairs which
 * can be used in standardization process.
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Hashtable;


import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.util.KeyValueParser;


public class StandardizePanel extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private Hashtable <String,String> __h;
	private ReportTable rt;
	
	private File f=null;

	
	public StandardizePanel () {
		// Constructor
		JPanel cen =createPanel();
		if (cen == null ) return;
		
		this.setLocation(250,50);
		this.getContentPane().add(cen);
		this.setPreferredSize(new Dimension(720, 620));
		this.setTitle("Standardisation Dialog");
		this.setModal(true);
		this.pack();
		this.setVisible(true);
	}
	private JPanel createPanel() {
		
		try {
			 f = FileSelectionUtil.chooseFile("Select Standardisation File");
			 if ( f== null) return null;
			 ConsoleFrame.addText("\n Selected File is:"+f.toString());
		} catch (FileNotFoundException fe ) {
			JOptionPane.showMessageDialog(null, fe.getMessage(),"File not Found Dialog",
					JOptionPane.ERROR_MESSAGE);
			ConsoleFrame.addText("\n ERROR: Selected File Not Found");
			return new JPanel(); // to avoid null pointer exception
		} 
			
		__h = KeyValueParser.parseFile(f.getAbsolutePath());
		if (__h == null)  {
			JOptionPane.showMessageDialog(null, "Select File has no value");
			ConsoleFrame.addText("\n ERROR: Selected File Not Found");
			return new JPanel(); // to avoid null pointer exception
			
		}
		ReportTableModel rtm = new ReportTableModel(new String[]{"DataValue","StandardName"},true,true);
		Enumeration<String> enum1 = __h.keys();
		while (enum1.hasMoreElements()) {
			String key_n = (String) enum1.nextElement();
			String val_n = __h.get(key_n);
			rtm.addFillRow(new String[]{key_n,val_n});
		}
		rt = new ReportTable(rtm);
		
		JPanel fd_panel = new JPanel(new BorderLayout());
		
		JButton newrow = new JButton("New Row");
		newrow.setActionCommand("newrow");
		newrow.addActionListener(this);
		newrow.addKeyListener(new KeyBoardListener());
	
		JButton validate = new JButton("Save");
		validate.setActionCommand("validate");
		validate.addActionListener(this);
		validate.addKeyListener(new KeyBoardListener());
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		
		JScrollPane regexScroll = new JScrollPane(rt);
		regexScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		regexScroll.setPreferredSize(new Dimension(700, 250));
		regexScroll.setMinimumSize(new Dimension(10, 10));

		JPanel labP = new JPanel(new BorderLayout());
		JLabel tit = new JLabel ("DataValue can be Regex or Comma Separated",JLabel.CENTER); 
		labP.add(tit,BorderLayout.CENTER);
		
		fd_panel.add(labP, BorderLayout.PAGE_START);
		fd_panel.add(regexScroll, BorderLayout.CENTER);
		
		JPanel bp = new JPanel ();
		bp.add(newrow);
		bp.add(validate);
		bp.add(cancel);
		
		fd_panel.add(bp,BorderLayout.PAGE_END);

		return fd_panel;
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if ("newrow".equals(command)) {
			rt.addRow();
		}
		if ("validate".equals(command)) {
			Hashtable<String,String> h = new Hashtable<String,String>();
			int rowc = rt.getModel().getRowCount();
			
			for (int i=0; i < rowc; i++) {
				Object k = rt.getValueAt(i, 0); // key
				if (k == null || "".equals(k.toString()) ) continue;
				
				Object v = rt.getValueAt(i, 1); // Value
				String[] keya= k.toString().split(",");
				
				for (String key:keya)
					h.put(key, v==null?"":v.toString());
			}
			if (h == null || h.isEmpty() == true) {
				JOptionPane.showMessageDialog(null, "Nothing to Save");
				return;
			}
			
			int seloption = JOptionPane.showConfirmDialog(null, "Do you want to overwrite file:"+f.getAbsolutePath(),
					"File Save Option", JOptionPane.YES_NO_OPTION);
			if (seloption == JOptionPane.NO_OPTION)
				return;
			
			boolean savef = KeyValueParser.saveTextFile(f.getAbsolutePath(),h);
			if (savef==true)
				ConsoleFrame.addText("\n File Saved Successfully :"+f.getAbsolutePath());
			this.dispose();
		}
		if ("cancel".equals(command)) {
			this.dispose();
		}
		
	}
}
