package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2020      *
 *     http://www.arrahtech.com                *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with copyright      *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This file is used for implementing User
 * Defined Function into ReportTableModel
 * User can pull UDF and populated the RTM
 * defined column with that
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.arrah.framework.udf.UDFEvaluator;
import org.arrah.framework.udf.UDFInterfaceToRTM;

public class UDFPanel implements ActionListener {
	private ReportTable _rt;
	private JTextArea inputTextArea;
	private int _selColIndex;
	private int beginIndex,endIndex;
	private JDialog _dg;
	private JFormattedTextField jrn_low, jrn_high;
	private JLabel udfparameterInfoLabel;
	private String selectedUDF=null;
	

	public UDFPanel(ReportTable rt, int selColIndex) {
		_rt = rt;
		
		_selColIndex = selColIndex;
		
		createGUI();
	}

	public UDFPanel(ReportTable rt) {
		_rt = rt;
		
		createGUI();
	}

	private void createGUI() {
		
		JLabel infolabel = new JLabel("Please Select ordered columns for UDF parameters", JLabel.TRAILING);
		
		JPanel rowPanel = createRowNumPanel();

		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("ok");

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		cancel.setActionCommand("cancel");

		JButton clear = new JButton("Clear");
		clear.addActionListener(this);
		clear.addKeyListener(new KeyBoardListener());
		clear.setActionCommand("clear");

		inputTextArea = new JTextArea(8, 30);
		inputTextArea.setWrapStyleWord(true);
		inputTextArea.setLineWrap(true);
		inputTextArea.setBorder(BorderFactory.createLineBorder(Color.black));

		int colC = _rt.table.getColumnCount();
		String[] colName = new String[colC];
		for (int i = 0; i < colC; i++) {
			colName[i] = _rt.table.getColumnName(i);
		}

		final JList<String> list = new JList<String>(colName);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setCellRenderer(new MyListRenderer());

		// Add listener to it
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String insS = list.getSelectedValue().toString() ;
					inputTextArea.insert(insS, inputTextArea.getCaretPosition());
				}
			}
		};
		list.addMouseListener(mouseListener);

		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 400));

		JLabel clickinfoLabel = new JLabel("Double Click to Insert",JLabel.TRAILING);
		
		ConcurrentHashMap<String, Method> funcName = UDFEvaluator.getMapUdf();
		Enumeration<String> funckey =  funcName.keys();
		
		JComboBox<String >functiondropList = new JComboBox<String>();
		functiondropList.addItem("Please select the UDF");
		
		while (funckey.hasMoreElements()) {
			functiondropList.addItem(funckey.nextElement());
		}
		
		functiondropList.addActionListener(this);
		
		udfparameterInfoLabel = new JLabel("UDF uses and parameter info");
		udfparameterInfoLabel.setPreferredSize(new Dimension(300,250));
		udfparameterInfoLabel.setVerticalAlignment(SwingConstants.TOP);

		JPanel panel = new JPanel();
		panel.add(ok);
		panel.add(cancel);
		panel.add(clear);
		panel.add(inputTextArea);
		panel.add(listScroller);
		panel.add(clickinfoLabel);
		panel.add(infolabel);
		panel.add(rowPanel);
		panel.add(udfparameterInfoLabel);
		panel.add(functiondropList);

		SpringLayout layout = new SpringLayout();
		panel.setLayout(layout);
		panel.setPreferredSize(new Dimension(700, 510));
		
		layout.putConstraint(SpringLayout.NORTH, clickinfoLabel, 5, SpringLayout.NORTH,panel);
		layout.putConstraint(SpringLayout.WEST, clickinfoLabel, 5, SpringLayout.WEST,panel);
		layout.putConstraint(SpringLayout.NORTH, listScroller, 5,SpringLayout.SOUTH, clickinfoLabel);
		layout.putConstraint(SpringLayout.WEST, listScroller, 5,SpringLayout.WEST, panel);
		
		
		layout.putConstraint(SpringLayout.NORTH, functiondropList, 5,SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, functiondropList, 5, SpringLayout.EAST,listScroller);
		
		layout.putConstraint(SpringLayout.NORTH, infolabel,10,SpringLayout.SOUTH, functiondropList);
		layout.putConstraint(SpringLayout.WEST, infolabel, 5, SpringLayout.EAST,listScroller);
		
		layout.putConstraint(SpringLayout.NORTH, inputTextArea, 5,SpringLayout.SOUTH, infolabel);
		layout.putConstraint(SpringLayout.WEST, inputTextArea, 5, SpringLayout.EAST,listScroller);
		
		layout.putConstraint(SpringLayout.NORTH, clear, 5, SpringLayout.SOUTH,inputTextArea);
		layout.putConstraint(SpringLayout.WEST, clear, 150, SpringLayout.WEST,inputTextArea);
		
		layout.putConstraint(SpringLayout.NORTH, udfparameterInfoLabel, 10, SpringLayout.SOUTH,clear);
		layout.putConstraint(SpringLayout.WEST, udfparameterInfoLabel, 0, SpringLayout.WEST,inputTextArea);
		
		layout.putConstraint(SpringLayout.SOUTH, rowPanel, -10, SpringLayout.NORTH,ok);
		layout.putConstraint(SpringLayout.WEST, rowPanel, 40, SpringLayout.WEST,panel);
		
		layout.putConstraint(SpringLayout.SOUTH, ok, -5, SpringLayout.SOUTH,panel);
		layout.putConstraint(SpringLayout.WEST, ok, -400, SpringLayout.EAST,panel);
		layout.putConstraint(SpringLayout.SOUTH, cancel, 0, SpringLayout.SOUTH,ok);
		layout.putConstraint(SpringLayout.WEST, cancel, 5, SpringLayout.EAST,ok);

		_dg = new JDialog();
		_dg.setTitle("Expression Builder Dialog");
		_dg.setLocation(175, 50);
		_dg.getContentPane().add(panel);

		_dg.pack();
		_dg.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object actionObject = e.getSource();
		if (actionObject instanceof javax.swing.JComboBox) {
			
			int selectedIndex = ( (javax.swing.JComboBox<?>)actionObject).getSelectedIndex();
			if (selectedIndex == 0) {
				
				udfparameterInfoLabel.setText("UDF uses and parameter info");
				selectedUDF=""; //reset
				
			} else {
				
				try {
					
					selectedUDF =( (javax.swing.JComboBox<?>)actionObject).getSelectedItem().toString();
					Class<?> obj = Class.forName(selectedUDF);
					Method mapUDF = obj.getMethod("describeFunction");
					udfparameterInfoLabel.setText(mapUDF.invoke(obj.newInstance()).toString());
					
					return;
				} catch (Exception classnotfoundexp) {
					System.out.println(classnotfoundexp.getLocalizedMessage());
				}
				
			}
			
		} // End of JComboBox Action
		
		String command = e.getActionCommand();
		if (command.equals("clear")) {
			
			inputTextArea.setText("");
			
			return;
		}
		
		if (command.equals("cancel")) {
			
			_dg.dispose();
			
			return;
		}
		
		if (command.equals("ok")) {
			
			String exp = inputTextArea.getText().trim();
			
			if (selectedUDF == null || selectedUDF.isEmpty() == true || "Please select the UDF".equalsIgnoreCase(selectedUDF)) {
				
				JOptionPane.showMessageDialog(null, "Please select the UDF to apply on Populated column");
				
				return;
				
			}
			
			try {
				
				_dg.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				
				int rowC = _rt.getModel().getRowCount();
				
				beginIndex = ((Long) jrn_low.getValue()).intValue();
				
				endIndex = ((Long) jrn_high.getValue()).intValue();
				
				if (beginIndex <= 0 || beginIndex > rowC)
					beginIndex = 1;
				if (endIndex <= 0 || endIndex > (rowC+1) || endIndex < beginIndex )
					endIndex = rowC+1;
				
				UDFInterfaceToRTM.evalUDF(selectedUDF, _rt.getRTMModel(), _selColIndex,beginIndex,endIndex,Arrays.asList(exp.split(",")));
				
				return;
		} finally {
			_dg.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			_dg.dispose();
		}}

	}
	
	public class MyListRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = -347683601167694906L;

		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			Component c = super.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);
			
			int index_i = _rt.getRTMModel().getColumnIndex( value.toString());
			//int index_i = getColumnIndex(_rt, value.toString());
			if (index_i < 0)
				((JLabel) c).setToolTipText(null);
			else
				((JLabel) c).setToolTipText(_rt.table.getColumnClass(index_i)
						.getName());
			
			return c;
		}
	}
	
	private JPanel createRowNumPanel() {
		
		JPanel rownnumjp = new JPanel(new FlowLayout(FlowLayout.LEADING));
		
		JLabel lrange = new JLabel("  Row Numbers to Populate : From(Inclusive)", JLabel.LEADING);
		
		jrn_low = new JFormattedTextField();
		jrn_low.setValue(new Long(1));
		jrn_low.setColumns(8);
		
		JLabel torange = new JLabel("  To(Exclusive):", JLabel.LEADING);
		
		jrn_high = new JFormattedTextField();
		jrn_high.setValue(new Long(_rt.getModel().getRowCount() +1 ));
		jrn_high.setColumns(8);
		
		rownnumjp.add(lrange);
		rownnumjp.add(jrn_low);
		rownnumjp.add(torange);
		rownnumjp.add(jrn_high);
		return rownnumjp;
	}
	
} // End of UDFPanel
