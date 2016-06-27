package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2015      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for creating 
 * random numbers, string and date
 * from populate column.
 * 
 */

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.arrah.framework.datagen.RandomColGen;

public class RandomColGenPanel implements ActionListener {
	private ReportTable _rt;
	private int _rowC = 0;
	private int _colIndex = 0;
	private JDialog d_f;
	private JFormattedTextField jft_low, jft_high;
	private JFormattedTextField jfs_low, jfs_high;
	private JFormattedTextField jrn_low, jrn_high;
	private JRadioButton rd1, rd2, rd3, rd4;
	private JSpinner jsp_low, jsp_high, jsp_low_time, jsp_high_time;
	private JComboBox<String> typeCombo, lanCombo, strType;
	private Border line_b;
	private int beginIndex, endIndex;
	private JCheckBox sortCheck;

	public RandomColGenPanel(ReportTable rt, int colIndex) {
		_rt = rt;
		_colIndex = colIndex;
		_rowC = rt.table.getRowCount();
		createDialog();
	}; // Constructor

	private void createDialog() {
		JPanel jp = new JPanel(new GridLayout(6, 1));
		line_b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

		rd1 = new JRadioButton("Number");
		rd2 = new JRadioButton("String");
		rd3 = new JRadioButton("Date");
		rd4 = new JRadioButton("Time");
		ButtonGroup bg = new ButtonGroup();
		bg.add(rd1);
		bg.add(rd2);
		bg.add(rd3);
		bg.add(rd4);
		rd1.setSelected(true);

		jp.add(createNumPanel());
		jp.add(createStringPanel());
		jp.add(createDatePanel());
		jp.add(createTimePanel());
		jp.add(createRowNumPanel());

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

		d_f = new JDialog();
		d_f.setModal(true);
		d_f.setTitle("Random Generation Dialog");
		d_f.setLocation(250, 250);
		d_f.getContentPane().add(jp);
		d_f.pack();
		d_f.setVisible(true);

	}

	private JPanel createNumPanel() {
		JPanel numjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		typeCombo = new JComboBox<String>(
				new String[] { "Integer Type", "Decimal Type" });
		JLabel lrange = new JLabel("  Range:", JLabel.LEADING);
		jft_low = new JFormattedTextField();
		jft_low.setValue(new Long(0));
		jft_low.setColumns(8);
		JLabel torange = new JLabel("  to:", JLabel.LEADING);
		jft_high = new JFormattedTextField();
		jft_high.setValue(new Long(100));
		jft_high.setColumns(8);
		numjp.add(rd1);
		numjp.add(typeCombo);
		numjp.add(lrange);
		numjp.add(jft_low);
		numjp.add(torange);
		numjp.add(jft_high);
		numjp.setBorder(line_b);
		return numjp;
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
		
		//CheckBox for sorted value
		sortCheck = new JCheckBox("Incremental");
		
		rownnumjp.add(lrange);
		rownnumjp.add(jrn_low);
		rownnumjp.add(torange);
		rownnumjp.add(jrn_high);
		rownnumjp.add(sortCheck);
		rownnumjp.setBorder(line_b);
		return rownnumjp;
	}
	private JPanel createDatePanel() {
		JPanel datejp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		jsp_low = new JSpinner(new SpinnerDateModel());
		jsp_high = new JSpinner(new SpinnerDateModel());
		jsp_low.setEditor(new JSpinner.DateEditor(jsp_low, "dd/MM/yyyy HH:mm:ss"));
		jsp_high.setEditor(new JSpinner.DateEditor(jsp_high, "dd/MM/yyyy HH:mm:ss"));
		JLabel lrange = new JLabel("  Range:", JLabel.LEADING);
		JLabel toRange = new JLabel("  to:", JLabel.LEADING);
		datejp.add(rd3);
		datejp.add(lrange);
		datejp.add(jsp_low);
		datejp.add(toRange);
		datejp.add(jsp_high);
		datejp.setBorder(line_b);
		return datejp;
	}
	private JPanel createTimePanel() {
		JPanel datejp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		jsp_low_time = new JSpinner(new SpinnerDateModel());
		jsp_high_time = new JSpinner(new SpinnerDateModel());
		jsp_low_time.setEditor(new JSpinner.DateEditor(jsp_low_time, "HH:mm:ss"));
		jsp_high_time.setEditor(new JSpinner.DateEditor(jsp_high_time, "HH:mm:ss"));
		JLabel lrange = new JLabel("  Range:", JLabel.LEADING);
		JLabel toRange = new JLabel("  to:", JLabel.LEADING);
		datejp.add(rd4);
		datejp.add(lrange);
		datejp.add(jsp_low_time);
		datejp.add(toRange);
		datejp.add(jsp_high_time);
		datejp.setBorder(line_b);
		return datejp;
	}

	private JPanel createStringPanel() {
		JPanel stringjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		jfs_low = new JFormattedTextField();
		jfs_low.setValue(new Long(6));
		jfs_low.setColumns(8);
		jfs_high = new JFormattedTextField();
		jfs_high.setValue(new Long(10));
		jfs_high.setColumns(8);
		lanCombo = new JComboBox<String>(new String[] { "Basic Latin", "Greek",
				"Hebrew", "Arabic", "Devanagiri", "Tamil", "Kannada", "Thai",
				"Hangul", "Hiragana", "Katakana", "Bopomofo", "Kanbun" });
		strType = new JComboBox<String>(new String[] { "Any Character",
				"LetterOrDigit", "Letter", "Digit", });
		JLabel strLen = new JLabel("  Length between:", JLabel.LEADING);
		JLabel andLabel = new JLabel("  and:", JLabel.LEADING);
		stringjp.add(rd2);
		stringjp.add(lanCombo);
		stringjp.add(strType);
		stringjp.add(strLen);
		stringjp.add(jfs_low);
		stringjp.add(andLabel);
		stringjp.add(jfs_high);
		stringjp.setBorder(line_b);

		return stringjp;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("cancel")) {
			d_f.dispose();
			return;
		}
		if (action.equals("ok")) {
			if (_rt.isSorting() || _rt.table.isEditing()) {
				JOptionPane.showMessageDialog(null, "Table is in Sorting or Editing State");
				return;
			}
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
			
			if (rd1.isSelected() == true)
				if (typeCombo.getSelectedIndex() == 0) {
					Vector<Long> vc = new RandomColGen(numGenerate)
							.updateColumnRandomInt(
									((Long) jft_high.getValue()).longValue(),
									((Long) jft_low.getValue()).longValue());
					if (sortCheck.isSelected() == true)
						vc.sort(null);
					for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
						_rt.getModel().setValueAt(vc.get(i - (beginIndex -1)).toString(), i, _colIndex);
					}
				} else {
					Vector<Double> vc = new RandomColGen(numGenerate)
							.updateColumnRandomDouble(
									((Long) jft_high.getValue()).longValue(),
									((Long) jft_low.getValue()).longValue());
					if (sortCheck.isSelected() == true)
						vc.sort(null);
					for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
						_rt.getModel().setValueAt(vc.get(i- (beginIndex -1)).toString(), i, _colIndex);
					}
				}
			if (rd2.isSelected() == true) {
				Vector<String> vc = new RandomColGen(numGenerate)
						.updateColumnRandomString(((Long) jfs_high.getValue()),
								((Long) jfs_low.getValue()),
								lanCombo.getSelectedIndex(),
								strType.getSelectedIndex());
				if (sortCheck.isSelected() == true)
					vc.sort(null);
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					_rt.getModel().setValueAt(vc.get(i - (beginIndex -1)).toString(), i, _colIndex);
				}
			}
			if (rd3.isSelected() == true || rd4.isSelected() == true) {
				long min=0,max=0;
				if (rd3.isSelected() == true) {
				 min = ((Date) jsp_low.getValue()).getTime();
				 max = ((Date) jsp_high.getValue()).getTime();
				}
				else {
				 min = ((Date) jsp_low_time.getValue()).getTime();
				 max = ((Date) jsp_high_time.getValue()).getTime();
				}
				
				Vector<Date> vc = new RandomColGen(numGenerate)
						.updateColumnRandomDate(max, min);
				if (sortCheck.isSelected() == true)
					vc.sort(null);
				for (int i = (beginIndex -1) ; i < ( endIndex -1 ); i++) {
					_rt.getModel().setValueAt(vc.get(i - (beginIndex -1)).toString(), i, _colIndex);
				}
			}
			return;
		}
	}

}
