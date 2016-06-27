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

/* This file is used for masking
 * and shuffling the records.
 * 
 */

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.arrah.framework.datagen.EncryptRTM;
import org.arrah.framework.datagen.ShuffleRTM;


public class MaskingPanel implements ActionListener {
	private ReportTable _rt;
	private int _rowC = 0;
	private int _colIndex = 0;
	private JDialog d_f;
	private JFormattedTextField jfs_low, jfs_high;
	private JTextField key_enter, key_reenter;
	private JTextField decrypt_key;
	private JFormattedTextField jrn_low, jrn_high;
	private JRadioButton rd1, rd2, rd3,rd4,rd5;
	private Border line_b;
	private JList<String> collist;
	private int beginIndex, endIndex;

	public MaskingPanel(ReportTable rt, int colIndex) {
		_rt = rt;
		_colIndex = colIndex;
		_rowC = rt.table.getRowCount();
	}; // Constructor

	public void createDialog() throws IOException {
		JPanel jp = new JPanel(new GridLayout(5, 1));
		line_b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

		rd1 = new JRadioButton("Shuffle Record");
		rd2 = new JRadioButton("Shuffle Record across Table");
		rd3 = new JRadioButton("Mask Record");
		rd4 = new JRadioButton("Encrypt Record");
		rd5 = new JRadioButton("Decrypt Record");
		ButtonGroup bg = new ButtonGroup();
		bg.add(rd1);
		bg.add(rd2);
		bg.add(rd3);bg.add(rd4);bg.add(rd5);
		rd1.setSelected(true);

		JPanel shP = createShufflePanel();
		jp.add(shP);
		JPanel shTableP = createShuffleTablePanel();
		jp.add(shTableP);
		JPanel masT = createMaskPanel();
		jp.add(masT);
		JPanel encrpt = createEncryptPanel();
		jp.add(encrpt);
		JPanel decrypt = createDecryptPanel();
		jp.add(decrypt);
				
				
		JPanel rowNP = createRowNumPanel();
		jp.add(rowNP);

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

		jp.add(bp);

		SpringLayout layout = new SpringLayout();
		jp.setLayout(layout);
		jp.setPreferredSize(new Dimension(815, 550));

		layout.putConstraint(SpringLayout.NORTH, shP, 4, SpringLayout.NORTH, jp);
		layout.putConstraint(SpringLayout.WEST, shP, 4, SpringLayout.WEST,jp);
		layout.putConstraint(SpringLayout.NORTH, shTableP, 4, SpringLayout.SOUTH, shP);
		layout.putConstraint(SpringLayout.WEST, shTableP, 4, SpringLayout.WEST,jp);
		layout.putConstraint(SpringLayout.NORTH, masT, 4, SpringLayout.SOUTH, shTableP);
		layout.putConstraint(SpringLayout.WEST, masT, 4, SpringLayout.WEST,jp);
		layout.putConstraint(SpringLayout.NORTH, encrpt, 4, SpringLayout.SOUTH, masT);
		layout.putConstraint(SpringLayout.WEST, encrpt, 4, SpringLayout.WEST,jp);
		layout.putConstraint(SpringLayout.NORTH, decrypt, 4, SpringLayout.SOUTH, encrpt);
		layout.putConstraint(SpringLayout.WEST, decrypt, 4, SpringLayout.WEST,jp);
		layout.putConstraint(SpringLayout.NORTH, rowNP, 8, SpringLayout.SOUTH, decrypt);
		layout.putConstraint(SpringLayout.WEST, rowNP, 4, SpringLayout.WEST,jp);
		layout.putConstraint(SpringLayout.NORTH, bp, 15, SpringLayout.SOUTH, rowNP);
		layout.putConstraint(SpringLayout.WEST, bp, 300, SpringLayout.WEST,jp);
		
		
		d_f = new JDialog();
		d_f.setModal(true);
		d_f.setTitle("Data Shuffle Dialog");
		d_f.setLocation(150, 100);
		d_f.getContentPane().add(jp);
		d_f.pack();
		d_f.setVisible(true);

	}

	private JPanel createShufflePanel() {
		JPanel shuffp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		
		JLabel lrange = new JLabel("  Example- \"String\":\"irSntg\" ", JLabel.LEADING);
		shuffp.add(rd1);
		shuffp.add(lrange);
		shuffp.setBorder(line_b);
		return shuffp;
	}
	private JPanel createRowNumPanel() {
		JPanel rownnumjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel lrange = new JLabel("  Row Numbers to Populate : From (Inclusive)", JLabel.LEADING);
		jrn_low = new JFormattedTextField();
		jrn_low.setValue(new Long(1));
		jrn_low.setColumns(8);
		JLabel torange = new JLabel("  To(Exclusive):", JLabel.LEADING);
		jrn_high = new JFormattedTextField();
		jrn_high.setValue(new Long(_rowC+1));
		jrn_high.setColumns(8);
		
		rownnumjp.add(lrange);
		rownnumjp.add(jrn_low);
		rownnumjp.add(torange);
		rownnumjp.add(jrn_high);
		// rownnumjp.setBorder(line_b);
		return rownnumjp;
	}
	private JPanel createShuffleTablePanel() throws IOException {
		
		JPanel shuffTablejp = new JPanel();
		SpringLayout layout = new SpringLayout();
		shuffTablejp.setLayout(layout);
		shuffTablejp.setPreferredSize(new Dimension(600, 250));

		JLabel lrange = new JLabel("  Example:", JLabel.LEADING);
		JLabel showImg;
		
    ImageIcon imageicon = new ImageIcon(MaskingPanel.class
        .getClassLoader().getResource("image/ShuffleTable.jpg"),
        "Example Image");
		int imageLS = imageicon.getImageLoadStatus();

		if (imageLS == MediaTracker.ABORTED
					|| imageLS == MediaTracker.ERRORED)
			showImg = new JLabel(
						"Image not Shown", 0);
		else
			showImg = new JLabel(imageicon, JLabel.CENTER);
		
		JLabel integrityL = new JLabel("<html>(Keep the chosen column Selected)<br>" +
				"  Referential Integrity with : </html>", JLabel.LEADING);
		
		int colC = _rt.getModel().getColumnCount();
		String[] colName = new String[colC]; // Show All Columns
		for (int i = 0; i < colC; i++) {
			 colName[i] = _rt.getModel().getColumnName(i);
		}
		
		collist = new JList<String>(colName);
		collist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		collist.setSelectedIndex(_colIndex); // Chosen column is selected

		JScrollPane listScroller = new JScrollPane(collist);
		listScroller.setPreferredSize(new Dimension(200, 200));
		
		shuffTablejp.add(rd2);
		shuffTablejp.add(lrange);
		shuffTablejp.add(showImg);
		shuffTablejp.add(integrityL);
		shuffTablejp.add(listScroller);
		shuffTablejp.setBorder(line_b);
		
		
		layout.putConstraint(SpringLayout.NORTH, rd2, 2, SpringLayout.NORTH, shuffTablejp);
		layout.putConstraint(SpringLayout.WEST, rd2, 2, SpringLayout.WEST,shuffTablejp);
		layout.putConstraint(SpringLayout.NORTH, lrange, 2, SpringLayout.SOUTH, rd2);
		layout.putConstraint(SpringLayout.WEST, lrange, 4, SpringLayout.WEST,shuffTablejp);
		layout.putConstraint(SpringLayout.NORTH, showImg, 2, SpringLayout.SOUTH, lrange);
		layout.putConstraint(SpringLayout.WEST, showImg, 4, SpringLayout.WEST,shuffTablejp);
		
		layout.putConstraint(SpringLayout.NORTH, integrityL, 2, SpringLayout.NORTH, shuffTablejp);
		layout.putConstraint(SpringLayout.WEST, integrityL, 15, SpringLayout.EAST,showImg);
		layout.putConstraint(SpringLayout.NORTH, listScroller, 2, SpringLayout.SOUTH, integrityL);
		layout.putConstraint(SpringLayout.WEST, listScroller, 0, SpringLayout.WEST,integrityL);
		
		
		return shuffTablejp;
	}

	private JPanel createMaskPanel() {
		JPanel maskjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		jfs_low = new JFormattedTextField();
		jfs_low.setValue(new String("****"));
		jfs_low.setColumns(8);
		
		jfs_high = new JFormattedTextField();
		jfs_high.setValue(new Integer(0));
		jfs_high.setColumns(4);
		
		JLabel strLen = new JLabel("  Example- \"111-222\":\"111-***\"", JLabel.LEADING);
		JLabel andLabel = new JLabel(" Mask String:", JLabel.LEADING);
		JLabel maskIndexL = new JLabel(" Mask Index(-1 to mask from End):", JLabel.LEADING);
		maskjp.add(rd3);
		maskjp.add(strLen);
		maskjp.add(andLabel);
		maskjp.add(jfs_low);
		maskjp.add(maskIndexL);
		maskjp.add(jfs_high);
		maskjp.setBorder(line_b);

		return maskjp;
	}
	private JPanel createEncryptPanel() {
		JPanel encrptjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		key_enter = new JTextField();
		key_enter.setText(new String("key1234"));
		key_enter.setColumns(8);
		
		
		key_reenter = new JTextField();
		key_reenter.setText(new String("key1234"));;
		key_reenter.setColumns(8);
		
		
		JLabel strLen = new JLabel("  Enter Key(Recommended 16 byte):", JLabel.LEADING);
		JLabel andLabel = new JLabel(" Re Enter Key:", JLabel.LEADING);
		
		encrptjp.add(rd4);
		encrptjp.add(strLen);
		encrptjp.add(key_enter);
		encrptjp.add(andLabel);
		encrptjp.add(key_reenter);
		
		encrptjp.setBorder(line_b);
		return encrptjp;
	}
	
	private JPanel createDecryptPanel() {
		JPanel decrptjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		decrypt_key = new JTextField();
		decrypt_key.setText(new String("Enter key"));
		decrypt_key.setColumns(8);

		JLabel strLen = new JLabel("  Decrypt Key:", JLabel.LEADING);
		decrptjp.add(rd5);
		
		decrptjp.add(strLen);
		decrptjp.add(decrypt_key);

		decrptjp.setBorder(line_b);
		return decrptjp;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("cancel")) {
			d_f.dispose();
			return;
		}
		if (action.equals("ok")) {
			d_f.dispose();
			beginIndex = ((Long) jrn_low.getValue()).intValue();
			endIndex = ((Long) jrn_high.getValue()).intValue();
			if (beginIndex <= 0 || beginIndex > _rowC)
				beginIndex = 1;
			if (endIndex <= 0 || endIndex > (_rowC + 1))
				endIndex = _rowC +1;
			
			int numGenerate = endIndex - beginIndex;
			if ( numGenerate <= 0 || numGenerate > _rowC) {
				numGenerate = _rowC; // default behavior for Invalid number
				beginIndex = 1;endIndex = _rowC+1;
			}
			try {
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				if (rd1.isSelected() == true) {
					ShuffleRTM.shuffleRecord(_rt.getRTMModel(), _colIndex, beginIndex -1 , endIndex -1);
				}
				if (rd2.isSelected() == true) {
					int[] selCols = collist.getSelectedIndices();
					if (selCols == null || selCols.length == 0) {
						JOptionPane.showMessageDialog(null,
								"No Column Selected", "Input Validation Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					ShuffleRTM.shuffleColumns(_rt.getRTMModel(), selCols, beginIndex -1 , endIndex -1);
				}
				if (rd3.isSelected() == true) {
					String val = jfs_low.getText();
					int index = (Integer) jfs_high.getValue();
					if (index < 0) index = -1; //start from End
					
					if(val == null || "".equals(val)) {
						JOptionPane.showMessageDialog(null,
								"Mask String not Chosen", "Input Validation Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					ShuffleRTM.maskColumn(_rt.getRTMModel(), _colIndex, beginIndex -1, endIndex -1,
							val, index);
				}
				if (rd4.isSelected() == true) {
					String key = key_enter.getText();
					String keyre = key_reenter.getText();
					
					if ("".equals(key) == true || key.equals(keyre) == false) {
						JOptionPane.showMessageDialog(null,
								"Re Entered Key not maching", "Key Validation Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					} else {
						JOptionPane.showMessageDialog(null,
								"Remember key for decryption ", "Key Information",
								JOptionPane.INFORMATION_MESSAGE);
						
					}
					EncryptRTM encrpt = new EncryptRTM(); // can't be static as security
					encrpt.encryptColumn(_rt.getRTMModel(), _colIndex, beginIndex -1, endIndex -1,key);
					return;
			}
			if (rd5.isSelected() == true) {
					String key = decrypt_key.getText();
					
					if (key == null || "".equals(key) == true ) {
						JOptionPane.showMessageDialog(null,
								"Re Entered Key not maching", "Key Validation Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					} 
					EncryptRTM encrpt = new EncryptRTM(); // can't be static as security
					encrpt.decryptColumn(_rt.getRTMModel(), _colIndex, beginIndex -1, endIndex -1,key);
					return;
			}
			} finally {
				d_f.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}
	}
	}

} // End of class
