package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with copyright      *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This file is used for creating Expression   
 * Builder on the Table columns.
 * that needs to be run to get next/previous value 
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

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.arrah.framework.datagen.ExpressionBuilder;

public class ExpressionBuilderPanel implements ActionListener {
	private ReportTable _rt;
	private JTextArea _expane;
	private int _selColIndex;
	private int beginIndex,endIndex;
	private JDialog _dg;
	private JLabel statusL;
	private JFormattedTextField jrn_low, jrn_high;
	
	final static private String START_TOKEN = "#{";
	final static private String END_TOKEN = "}";
	final static private String STATUS_STR = "Preview Value:";

	public ExpressionBuilderPanel(ReportTable rt, int selColIndex) {
		_rt = rt;
		_selColIndex = selColIndex;
		createGUI();
	}

	public ExpressionBuilderPanel(ReportTable rt) {
		_rt = rt;
		createGUI();
	}

	private void createGUI() {
		
		JLabel infolabel = new JLabel("Enter Expression in the Panel below and click Preview", JLabel.TRAILING);
		JPanel rowPanel = createRowNumPanel();

		statusL = new JLabel("Preview Value:", JLabel.TRAILING);

		JButton preview = new JButton("Preview");
		preview.addActionListener(this);
		preview.addKeyListener(new KeyBoardListener());
		preview.setActionCommand("preview");

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

		_expane = new JTextArea(8, 30);
		_expane.setWrapStyleWord(true);
		_expane.setLineWrap(true);
		_expane.setBorder(BorderFactory.createLineBorder(Color.black));

		JLabel exml = new JLabel();
		Border line_b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
				Color.YELLOW, Color.RED);
		exml.setBorder(BorderFactory.createTitledBorder(line_b, "Information"));
		String exp_text = "<html><body>";
		exp_text +=	"To Define String Variable<br><pre>put \"#{ then insert COLUMN_NAME close with }\" </pre>";
		exp_text +=	"To Define Number Variable<br><pre>put #{ then insert COLUMN_NAME close with } </pre>";
		exp_text +=	"At runtime the Column variable <Pre>#{COLUMN_NAME1} will be replaced by column value</pre>";
		exp_text +=	"At Preview <Pre>Output will be populated for first row</pre>";
		
		exp_text +=	"<br>Expression for String Type<br><pre>\"#{COLUMN_NAME1}\"+\"#{COLUMN_NAME2}\"</pre> ";
		exp_text += "Expression for Number Type<br><pre>#{COLUMN_NAME1}+#{COLUMN_NAME2}</pre>";
		exp_text += "<br>IF (condition) THEN expression <pre>IF (#{COLUMN_NAME1} > #{COLUMN_NAME2})<br>THEN #{SUM_COLUMN_NAME1}</pre>";
		exp_text += "<br>For Using Functions<br><pre>indexOf(\"#{COLUMN_NAME1}\",\"abc\",#{COLUMN_NAME2})</pre>";
		exp_text +=	"</body><html>";
		exml.setText(exp_text);

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
					String insS = START_TOKEN
							+ list.getSelectedValue().toString() + END_TOKEN;
					_expane.insert(insS, _expane.getCaretPosition());
				}
			}
		};
		list.addMouseListener(mouseListener);

		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 400));

		JLabel ll = new JLabel("Double Click to Insert",JLabel.TRAILING);
		
		JTabbedPane tabPane = createFuntionTab();

		JPanel panel = new JPanel();
		panel.add(preview);
		panel.add(ok);
		panel.add(cancel);
		panel.add(clear);
		panel.add(_expane);
		panel.add(listScroller);
		panel.add(statusL);
		panel.add(ll);
		panel.add(exml);
		panel.add(infolabel);
		panel.add(rowPanel);
		panel.add(tabPane);

		SpringLayout layout = new SpringLayout();
		panel.setLayout(layout);
		panel.setPreferredSize(new Dimension(1100, 510));
		
		layout.putConstraint(SpringLayout.NORTH, ll, 5, SpringLayout.NORTH,panel);
		layout.putConstraint(SpringLayout.WEST, ll, 0, SpringLayout.WEST,listScroller);
		layout.putConstraint(SpringLayout.NORTH, listScroller, 2,SpringLayout.SOUTH, ll);
		layout.putConstraint(SpringLayout.WEST, listScroller, 2,SpringLayout.WEST, panel);

		layout.putConstraint(SpringLayout.NORTH, infolabel, 2,SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, infolabel, 0, SpringLayout.WEST,_expane);
		layout.putConstraint(SpringLayout.NORTH, _expane, 2,SpringLayout.SOUTH, infolabel);
		layout.putConstraint(SpringLayout.WEST, _expane, 5, SpringLayout.EAST,listScroller);
		layout.putConstraint(SpringLayout.NORTH, exml, 2, SpringLayout.NORTH,panel);
		//layout.putConstraint(SpringLayout.WEST, exml, 5, SpringLayout.EAST,_expane);
		layout.putConstraint(SpringLayout.WEST, exml, 5, SpringLayout.EAST,tabPane);
		
		layout.putConstraint(SpringLayout.NORTH, statusL, 2,SpringLayout.SOUTH, _expane);
		layout.putConstraint(SpringLayout.WEST, statusL, 0, SpringLayout.WEST,_expane);
		layout.putConstraint(SpringLayout.NORTH, clear, 5, SpringLayout.SOUTH,statusL);
		layout.putConstraint(SpringLayout.WEST, clear, 20, SpringLayout.WEST,_expane);
		layout.putConstraint(SpringLayout.NORTH, preview, 5,SpringLayout.SOUTH, statusL);
		layout.putConstraint(SpringLayout.WEST, preview, 20, SpringLayout.EAST,clear);
		
		layout.putConstraint(SpringLayout.NORTH, tabPane, 10, SpringLayout.SOUTH,clear);
		layout.putConstraint(SpringLayout.WEST, tabPane, 0, SpringLayout.WEST,_expane);
		
		layout.putConstraint(SpringLayout.SOUTH, rowPanel, -10, SpringLayout.NORTH,ok);
		layout.putConstraint(SpringLayout.WEST, rowPanel, 40, SpringLayout.WEST,panel);
		layout.putConstraint(SpringLayout.SOUTH, ok, -5, SpringLayout.SOUTH,panel);
		layout.putConstraint(SpringLayout.WEST, ok, -575, SpringLayout.EAST,panel);
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
		String command = e.getActionCommand();
		if (command.equals("clear")) {
			_expane.setText("");
			return;
		}
		if (command.equals("cancel")) {
			_dg.dispose();
			return;
		}
		if (command.equals("preview")) {
			String exp = _expane.getText();
			String res = preparseJeval(exp, _rt, -1, beginIndex, endIndex); // -1 for preview do not
														// update column
			if (res != null) {
				JOptionPane.showMessageDialog(null, "Parsing OK");
				statusL.setText(STATUS_STR + " " + res);
			} else {
				JOptionPane.showMessageDialog(null, "Parsing Failed");
				statusL.setText(STATUS_STR + " Parsing Failed");
			}
			return;
		}
		if (command.equals("ok")) {
			String exp = _expane.getText();
			try {
				_dg.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				
				beginIndex = ((Long) jrn_low.getValue()).intValue();
				endIndex = ((Long) jrn_high.getValue()).intValue();
				int rowC = _rt.getModel().getRowCount();
				if (beginIndex <= 0 || beginIndex > rowC)
					beginIndex = 1;
				if (endIndex <= 0 || endIndex > (rowC+1) || endIndex < beginIndex )
					endIndex = rowC+1;
				preparseJeval(exp, _rt, _selColIndex,beginIndex,endIndex);
				return;
		} finally {
			_dg.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			_dg.dispose();
		}}

	}

	public static String preparseJeval(String expression, ReportTable rpt,
			int selIndex, int beginIndex, int endIndex) {
		
		String jevalString = ExpressionBuilder.preparseJeval(expression,
				rpt.getRTMModel(), selIndex,beginIndex,endIndex);

		return jevalString;
	}

	public static int getColumnIndex(ReportTable rpt, String colName) {
		int row_c = rpt.table.getColumnCount();
		for (int i = 0; i < row_c; i++) {
			if (colName.equals(rpt.table.getColumnName(i)))
				return i;
		}
		return -1;
	}

	public class MyListRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = -347683601167694906L;

		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);
			int index_i = getColumnIndex(_rt, value.toString());
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
	
/* This function will create Tabbed component where functions be selected */
	
	private JTabbedPane createFuntionTab() {
		JTabbedPane tabPanel = new JTabbedPane(JTabbedPane.TOP);
		
		JPanel operFuncP  = new JPanel();
		JLabel operfuncL = new JLabel();
		String operfuncText = "<html><body><pre>";
		operfuncText += "Binary Operators  <br>  +   -   *   /   %    <br>";
		operfuncText += "Boolean Operators <br>  &#60;  &lt;=   &&   ||  !  != ==  >  >= <br>";
		operfuncText += "</pre></html></body>";
		
		operfuncL.setText(operfuncText);
		operFuncP.add(operfuncL);
		tabPanel.addTab("Operator",operFuncP);
		
		
		JPanel numFuncP = new JPanel();
		JLabel numfuncL = new JLabel();
		String numfuncText = "<html><body> <pre>";
		numfuncText += " abs(number) acos(number) asin(number)<br>" ;
		numfuncText += " atan(number) atan2(number,number) ceil(number)<br>" ;
		numfuncText += " cos(number) exp(number) floor(number)<br>";
		numfuncText += " IEEEremainder(number,number) log(number)<br>";
		numfuncText += " max(number,number) min(number,number)<br>";
		numfuncText += " pow(number,number) random() rint(number)<br>";
		numfuncText += " round(number) sin(number) sqrt(number) <br>";
		numfuncText += " tan(number) toDegrees(number) toRadians(number) </pre>";
		numfuncText += " For more information about above <br> functions look into Java Math Class";
		numfuncText += "</html></body>";
		
		numfuncL.setText(numfuncText);
		numFuncP.add(numfuncL);
		JScrollPane js1 = new JScrollPane();
		js1.setHorizontalScrollBarPolicy(
				   JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		js1.setVerticalScrollBarPolicy(
				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		js1.setViewportView(numFuncP);
		js1.setPreferredSize(new Dimension(450,250));
		tabPanel.addTab("Numeric",js1);
		
		JPanel strfuncP = new JPanel();
		JLabel strfuncL = new JLabel();
		String strfuncText = "<html><body> <pre>";
		strfuncText += "charAt(\"string\",integer) compareTo(\"string\",\"string\") compareToIgnoreCase(\"string\",\"string\") concat(\"string\",\"string\")<br>" ;
		strfuncText += "endsWith((\"string\",\"string\") equals((\"string\",\"string\") equalsIgnoreCase((\"string\",\"string\") <br>";
		strfuncText += "indexOf(\"string\",\"string\",integer) lastIndexOf(\"string\",\"string\",integer) length(\"string\") replace(\"string\",\"char\",\"char\")<br>";
		strfuncText += "startsWith(\"string\",\"string\",integer) substring(\"string\",integer,integer) toLowerCase(\"string\")<br>";
		strfuncText += "toUpperCase(\"string\") trim(\"string\") </pre>";
		strfuncText += "<br> For more information about above <br> functions look into Java String Class";
		strfuncText += "</html></body>";
		
		strfuncL.setText(strfuncText);
		strfuncP.add(strfuncL);		
		JScrollPane js2 = new JScrollPane();
		js2.setHorizontalScrollBarPolicy(
				   JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		js2.setVerticalScrollBarPolicy(
				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		js2.setViewportView(strfuncP);
		js2.setPreferredSize(new Dimension(450,250));
		tabPanel.addTab("String",js2);
		
		JPanel cumFuncP = new JPanel();
		JLabel cumFuncL = new JLabel();
		String cumFuncText = "<html><body><pre>";
		cumFuncText += "Functions:SUM_ AVG_ MIN_ MAX_ CUMSUM_ CUMAVG_ <br>PREV_ NEXT_<br>";
		cumFuncText += "Examples:#{SUM_COLNAME} will be replaced by <br> Total SUM of the column COLNAME<br>";
		cumFuncText += "Examples:#{CUMAVG_COLNAME} will be replaced<br> by Cumulative Average of that row <br> for the column COLNAME<br>";
		cumFuncText += "</pre></html></body>";
		
		cumFuncL.setText(cumFuncText);
		cumFuncP.add(cumFuncL);
		tabPanel.addTab("Utility",cumFuncP);
		
		
		tabPanel.setPreferredSize(new Dimension (425,235));
		
		return tabPanel;
	}
	
} // End of Expression Builder
