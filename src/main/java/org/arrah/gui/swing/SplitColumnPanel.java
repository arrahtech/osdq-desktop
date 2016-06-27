package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2015      *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is uses to add split column features
 * 
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;


public class SplitColumnPanel implements ActionListener {
	private ReportTable _rt;
	private int _rowC = 0;
	private int _colIndex = 0;
	private JDialog d_f;
	private JCheckBox ch1;
	private Border line_b;
	private JTextField splitString,splitString_subF;
	private JRadioButton rd1,rd2;

	
	public SplitColumnPanel(ReportTable rt, int colIndex) {
		_rt = rt;
		_colIndex = colIndex;
		_rowC = rt.table.getRowCount();
		try {
      createDialog();
    } catch (IOException e) {
      e.printStackTrace();
    }
	}; // Constructor
	

	private void createDialog() throws IOException {
		JPanel jp = new JPanel(new BorderLayout());
		line_b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

		ch1 = new JCheckBox("Split First Column");
		
		JPanel header = new JPanel(new GridLayout(3,1));
		header.add(createSplitPanel());
		header.add(createSubSplitPanel1());
		header.add(createSubSplitPanel2());
		jp.add(header,BorderLayout.CENTER);

		JPanel bp = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		bp.add(ok);
		JButton can = new JButton("Cancel");
		can.addKeyListener(new KeyBoardListener());
		can.setActionCommand("cancel");
		can.addActionListener(this);
		bp.add(can);

		JPanel bottom = new JPanel(new GridLayout(1,1));
		bottom.add(bp);
		jp.add(bottom,BorderLayout.SOUTH);

		d_f = new JDialog();
		d_f.setModal(true);
		d_f.setTitle("Split Column Function Dialog");
		d_f.setLocation(300, 200);
		d_f.setPreferredSize(new Dimension(780,550));
		d_f.getContentPane().add(jp);
		d_f.pack();
		d_f.setVisible(true);

	}

	/* default Split input that will apply to all rows */
	private JPanel createSplitPanel() throws IOException {
		
		JPanel splitjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		splitjp.setBorder(line_b);
		JLabel l = new JLabel("Column Separator");
		splitjp.add(l);
		splitString = new JTextField();
		splitString.setText("|");
		splitString.setColumns(10);
		splitjp.add(splitString);
		JLabel dummy = new JLabel("                                      Example: ",JLabel.TRAILING);
		splitjp.add(dummy);
		
		JLabel imageIfo;
    ImageIcon imageicon = new ImageIcon(SplitColumnPanel.class.getClassLoader()
            .getResource("image/SplitColumn_nohd.png"),
        "Image with no first column split");

		int imageLS = imageicon.getImageLoadStatus();
		if (imageLS == MediaTracker.ABORTED
				|| imageLS == MediaTracker.ERRORED)
			imageIfo = new JLabel("Image with no first column split");
		else
			imageIfo = new JLabel(imageicon, JLabel.CENTER);
		splitjp.add(imageIfo);
		
		return splitjp;
	}
	
	private JPanel createSubSplitPanel1() throws IOException {
		
		JPanel splitjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		splitjp.add(ch1);
		splitString_subF = new JTextField();
		splitString_subF.setText(":");
		splitString_subF.setColumns(10);
		splitjp.add(splitString_subF);
		
		ButtonGroup jbg = new ButtonGroup();
		rd1 = new JRadioButton("Ignore First subString");
		splitjp.add(rd1);rd1.setSelected(true);
		rd2 = new JRadioButton("Append First subString to all rows");
		
		JLabel imageIfo;
    ImageIcon imageicon = new ImageIcon(SplitColumnPanel.class.getClassLoader()
            .getResource("image/SplitColumn_ighd.png"),
        "Image with ignore first column split");

		int imageLS = imageicon.getImageLoadStatus();
		if (imageLS == MediaTracker.ABORTED
				|| imageLS == MediaTracker.ERRORED)
			imageIfo = new JLabel("Image with ignore first column split");
		else
			imageIfo = new JLabel(imageicon, JLabel.CENTER);
		splitjp.add(imageIfo);
		
		jbg.add(rd1);jbg.add(rd2);
		
		
		return splitjp;
	}
	private JPanel createSubSplitPanel2() throws IOException {
		
		JPanel splitjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel dummy = new JLabel("                                                           ");
		splitjp.add(dummy);
		splitjp.add(rd2);
		
		JLabel imageIfo2;
    ImageIcon imageicon2 = new ImageIcon(SplitColumnPanel.class.getClassLoader()
            .getResource("image/SplitColumn_apphd.png"),
        "Image with append first column split");
		int imageLS2 = imageicon2.getImageLoadStatus();
		if (imageLS2 == MediaTracker.ABORTED
				|| imageLS2 == MediaTracker.ERRORED)
			imageIfo2 = new JLabel("Image with appended first column split");
		else
			imageIfo2 = new JLabel(imageicon2, JLabel.CENTER);
		splitjp.add(imageIfo2);
		
		return splitjp;
	}


	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("cancel")) {
			d_f.dispose();
			return;
		}
		if (action.equals("ok")) {
			try {
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				int col_c= _rt.table.getColumnCount();
				String splitv = splitString.getText();
				String subsplitv = splitString_subF.getText();
				boolean substrS = ch1.isSelected();
				
				if (splitv == null || "".equals(splitv) == true) {
					JOptionPane.showMessageDialog(null, "Column separator is not valid ", 
						"Split Type Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (substrS == true && (subsplitv == null || "".equals(subsplitv) == true)) {
					JOptionPane.showMessageDialog(null, "First Column separator is not valid. \n Uncheck the option", 
						"Split Type Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
					
				for (int i=0; i < _rowC; i++) { //Scan table
					Object colv = _rt.getValueAt(i, _colIndex);
					if (colv == null) continue;
					String[] newv = colv.toString().split(Pattern.quote(splitv));
					int noSplit=newv.length;
					String[] firstCV = new String[] {"",""};
					String appStr=":";
					
					if (noSplit <= 1) // only one value no need to split
						continue;
					
					if (substrS == true) {
						 firstCV = newv[0].split(Pattern.quote(subsplitv),2);
						 if (firstCV[1] == null) firstCV[1]=""; // if no split empty string
						 
						 if (rd1.isSelected() == true) //ignore first substring
							 _rt.table.setValueAt(firstCV[1],i, _colIndex);
						 else // append both
							 _rt.table.setValueAt(firstCV[0]+appStr+firstCV[1],i, _colIndex);
					}
					else
						_rt.table.setValueAt(newv[0],i, _colIndex);
					
					
					for (int j=1; j < noSplit; j++) { // add rows for split
						_rt.addRows(i+1, 1);
						for (int k=0; k < col_c; k++) {
							if (k != _colIndex)
								_rt.table.setValueAt(_rt.table.getValueAt(i,k),i+1, k);
							else
								if(rd2.isSelected() == true)
									_rt.table.setValueAt(firstCV[0]+appStr+newv[j],i+1, k);
								else
									_rt.table.setValueAt(newv[j],i+1, k);
						}
						i++; _rowC++; // increase the table Index
					}
				} // End of table scan
			d_f.dispose(); // dispose it now
			return;
			} finally {
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
		} // Ok action
	} // End of actionPerformed
	
}
