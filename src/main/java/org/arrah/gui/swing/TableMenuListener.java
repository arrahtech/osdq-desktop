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

/* This file is used for  creating listener for
 * ReportTable menu items like search, count,
 * analyse selected or conditional formatting
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.arrah.framework.dataquality.FillCheck;
import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.ndtable.ReportTableSorter;
import org.arrah.framework.util.KeyValueParser;



public class TableMenuListener implements ActionListener, ItemListener {
	private JTable table;
	private JTextField f, colName_t[];
	private JFormattedTextField gt, lt, et;
	private String prev = ""; // Empty string not null
	private String reg_prev = ""; // Empty string not null
	private JDialog d, jd, jdc;
	private int rowI = 0;
	private int colI = 0, columnSearchIndex= -1; // selected Column for search
	private int rowcount = 0;
	private int colcount = 0;
	private int i,j;
	private JLabel sn;
	private int match_c = 0;
	private JCheckBox cs, em;
	private Pattern pat = null;
	private boolean stateChanged = true, columnSearch = false;
	private String replace = null; // replace string
	private JComboBox<String> c_combo, c_combo1, c_combo2;
	private ReportTable _rt;
	

	public TableMenuListener(JTable rt) {
		table = rt;
	}; // /Constructor
	
	// Need for incomplete test
	public TableMenuListener(ReportTable reportTable) {
		_rt = reportTable;
		table = reportTable.table;
	}

	public void createDialogPanel() {
		JPanel dia_p = new JPanel();
		SpringLayout layout = new SpringLayout();
		dia_p.setLayout(layout);

		JLabel l = new JLabel("Regex :", JLabel.TRAILING);
		JLabel pr = new JLabel("<html><body><a href=\"\">Saved Regex</A><body></html>");
		pr.addMouseListener(new LinkMouseListener());

		final JButton s = new JButton("Search"); // Used by JLabel

		f = new JTextField(15);
		f.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					s.doClick();
			}

		});
		s.setMnemonic(KeyEvent.VK_S);
		if (columnSearch == true)
			s.setActionCommand("searchCol");
		else
			s.setActionCommand("search");
		s.addActionListener(this);
		s.addKeyListener(new KeyBoardListener());
		
		JButton sr = new JButton("Search & Replace");
		sr.setMnemonic(KeyEvent.VK_R);
		sr.setActionCommand("replace");
		sr.addActionListener(this);
		sr.addKeyListener(new KeyBoardListener());

		JButton c = new JButton("Cancel");
		c.setMnemonic(KeyEvent.VK_C);
		c.setActionCommand("cancel");
		c.addActionListener(this);
		c.addKeyListener(new KeyBoardListener());

		cs = new JCheckBox("Case Sensitive");
		cs.addItemListener(this);
		em = new JCheckBox("Exact Match");
		em.setSelected(true);
		em.addItemListener(this);

		sn = new JLabel(" Match Index: ");

		dia_p.add(l);dia_p.add(pr);dia_p.add(f);dia_p.add(s);
		dia_p.add(sr);dia_p.add(c);dia_p.add(cs);dia_p.add(em);dia_p.add(sn);
		dia_p.setOpaque(true);

		layout.putConstraint(SpringLayout.WEST, dia_p, 2, SpringLayout.WEST, l);
		layout.putConstraint(SpringLayout.WEST, f, 2, SpringLayout.EAST, l);
		layout.putConstraint(SpringLayout.NORTH, l, 2, SpringLayout.NORTH,dia_p);
		layout.putConstraint(SpringLayout.NORTH, f, 0, SpringLayout.NORTH, l);
		layout.putConstraint(SpringLayout.WEST, pr, 10, SpringLayout.EAST, f);
		layout.putConstraint(SpringLayout.NORTH, pr, 0, SpringLayout.NORTH, f);

		layout.putConstraint(SpringLayout.NORTH, cs, 4, SpringLayout.SOUTH, l);
		layout.putConstraint(SpringLayout.NORTH, em, 2, SpringLayout.SOUTH, cs);

		layout.putConstraint(SpringLayout.WEST, s, 15, SpringLayout.EAST, em);
		layout.putConstraint(SpringLayout.SOUTH, s, -5, SpringLayout.SOUTH, em);
		layout.putConstraint(SpringLayout.WEST, sr, 5, SpringLayout.EAST, s);
		layout.putConstraint(SpringLayout.SOUTH, sr, -5, SpringLayout.SOUTH, em);
		layout.putConstraint(SpringLayout.WEST, c, 5, SpringLayout.EAST, sr);
		layout.putConstraint(SpringLayout.SOUTH, c, -5, SpringLayout.SOUTH, em);
		layout.putConstraint(SpringLayout.EAST, dia_p, 5, SpringLayout.EAST, c);
		layout.putConstraint(SpringLayout.NORTH, sn, 2, SpringLayout.SOUTH, em);
		layout.putConstraint(SpringLayout.SOUTH, dia_p, 2, SpringLayout.SOUTH, sn);

		Container container = table.getTopLevelAncestor();
		if (container != null && container instanceof JDialog)
			d = new JDialog((JDialog) table.getTopLevelAncestor());
		else if (container != null && container instanceof JFrame)
			d = new JDialog((JFrame) table.getTopLevelAncestor());
		else
			d = new JDialog();

		d.setTitle("Regex Search Dialog");
		d.addWindowListener(new WindowAdapter() {
			public void windowDeactivated(WindowEvent evt) {
				d.toFront();
			}
		});
		d.setLocation(200, 200);
		d.getContentPane().add(dia_p);
		d.setMinimumSize(new Dimension (350,100));
		d.pack();
		d.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String action_c = e.getActionCommand();
		if (action_c.compareToIgnoreCase("cancel") == 0) {
			replace = null;
			d.dispose();
			return;
		}
		if (action_c.compareToIgnoreCase("exit") == 0) {
			replace = null;
			jd.dispose();
			return;
		}
		if (action_c.compareToIgnoreCase("cancelcond") == 0) {
			replace = null;
			columnSearchIndex= -1;
			jdc.dispose();
			return;
		}
		if (action_c.compareToIgnoreCase("save") == 0) {
			int colC = colName_t.length;
			String  colName[] = new String[colC];
			for (int i = 0; i < colC; i++) {
				colName[i] = colName_t[i].getText();
			}
			((DefaultTableModel)((ReportTableSorter)(table.getModel())).getTableModel()).setColumnIdentifiers(colName);
			jd.dispose();
			return;
		}
		if (action_c.compareToIgnoreCase("search") == 0 || 
				action_c.compareToIgnoreCase("searchCol") == 0 ||
				action_c.compareToIgnoreCase("replace") == 0) {
			boolean match_found = false;
			String s_s = f.getText();
			if (s_s == null || s_s.compareTo("") == 0) {
				JOptionPane.showMessageDialog(d, "Nothing to Search",
						"Error Message", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (action_c.compareToIgnoreCase("replace") == 0 && replace == null) {
				replace = JOptionPane.showInputDialog("Please enter replace String");
				if (replace == null) return;
			}
			
			if (prev.compareTo(s_s) != 0) {
				rowI = 0;
				
				if (columnSearch == true) {
					colI = columnSearchIndex; // start Search from selected Column
					colcount = colI +1; // only search in selected column
				}
					
				else {
					colI = 0;
					colcount = table.getColumnCount();
				}
				match_c = 0;
				prev = s_s;
				rowcount = table.getRowCount();
				
				sn.setText(" Match Index: ");
			}
			if (em.isSelected() == false
					&& ((stateChanged == true) || reg_prev.compareTo(s_s) != 0)) {
				try {
					if (cs.isSelected() == false)
						pat = Pattern.compile(s_s, Pattern.CASE_INSENSITIVE);
					else
						pat = Pattern.compile(s_s);
					reg_prev = s_s;
					stateChanged = false;
				} catch (PatternSyntaxException pat_e) {
					d.setVisible(false);
					JOptionPane.showMessageDialog(d, pat_e.getMessage(),
							"Error Message", JOptionPane.ERROR_MESSAGE);
					d.setVisible(true);
					return;
				}
			}
			for (i = rowI; i < rowcount; i++) {
				for (j = colI; j < colcount; j++) {
					String cell = formatString(i, j);
					if (cell == null)
						continue;

					if (em.isSelected() == false) {
						Matcher m = pat.matcher(cell);
						match_found = m.find();
					} else {
						if (cs.isSelected() == true) {
							if (cell.compareTo(s_s) == 0)
								match_found = true;
						} else {
							if (cell.compareToIgnoreCase(s_s) == 0)
								match_found = true;
						}
					}

					if (match_found == true) {
						table.clearSelection();
						table.setRowSelectionInterval(i, i);
						table.setColumnSelectionInterval(j, j);
						table.scrollRectToVisible(table.getCellRect(i, j, true));
						
						if (action_c.compareToIgnoreCase("replace") == 0)
							if ("null".equals(replace)) // replace with null value
								table.getModel().setValueAt(null, i, j);
							else
								table.getModel().setValueAt(replace, i, j);
						
						sn.setText(" Match Index: " + ++match_c);
						
						rowI = (j < colcount - 1) ? i : i + 1;
						if (columnSearch == true)
							colI = (j < colcount - 1) ? j + 1 : columnSearchIndex ;
						else
							colI = (j < colcount - 1) ? j + 1 : 0;
						break;
					}

				}
				if (match_found == true)
					break;
				if (columnSearch == true)
					colI = columnSearchIndex;
				else
					colI = 0;
			}
			if (i == rowcount) {
				d.setVisible(false);
				int n = JOptionPane.showConfirmDialog(d,
						"Reached Bottom ....\n .. Starting from Top",
						"Regex Selection Dialog", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION) {
					d.setVisible(true);
					rowI = rowcount;
					colI = colcount; // it will not go into loop because of rowcount
					replace = null;
					return;
				}
				colI = 0;
				rowI = 0;
				match_c = 0;
				sn.setText(" Match Index: ");
				d.setVisible(true);
			}
			return;
		}
		
		// For conditioning coloring
		if (action_c.compareToIgnoreCase("render") == 0) {
			double dgt = (double) gt.getValue();
			double lgt = (double) lt.getValue();
			double egt = (double) et.getValue();
			int index = columnSearchIndex;
			
			int colgt = c_combo.getSelectedIndex();
			int collt = c_combo1.getSelectedIndex();
			int colet = c_combo2.getSelectedIndex();
			
			// Set Display on first pattern
			table.getColumnModel().getColumn(index).setCellRenderer(new MyCellRenderer(dgt,colgt,lgt,collt,egt,colet));
			jdc.dispose();
			table.repaint();
			return;
		}

		JMenuItem source = (JMenuItem) (e.getSource());
		
		if (source.getText().compareTo("Rename Columns") == 0) {
			int colcount = table.getColumnCount();
			String[] colName = new String[colcount];
			for (int i=0; i < colcount; i++)
				colName[i] = table.getColumnName(i);
			tableRenameDialog(colName);
			
			return;
		}
		
		if (source.getText().compareTo("Record Count") == 0) {
			JOptionPane.showMessageDialog(null,
					"Row Count=" + table.getRowCount() + "\nColumn Count="
							+ table.getColumnCount(), "Row Count Dialog",
					JOptionPane.PLAIN_MESSAGE);
			return;
		}
		if (source.getText().compareTo("Selected Count") == 0) {
			int r_s = table.getSelectedRowCount();
			int c_s = table.getSelectedColumnCount();

			JOptionPane.showMessageDialog(null, "Selected Row Count=" + r_s
					+ "\nSelected Column Count=" + c_s + "\nSelected Cells="
					+ r_s * c_s, "Selected Count Dialog",
					JOptionPane.PLAIN_MESSAGE);
			return;
		}
		if (source.getText().compareTo("Select All") == 0) {
			table.selectAll();
			table.requestFocusInWindow();
			return;
		}
		if (source.getText().compareTo("DeSelect All") == 0) {
			table.clearSelection();
			return;
		}
		if (source.getText().compareTo("Across Table") == 0) {
			createDialogPanel();
			return;
		}
		if (source.getText().compareTo("Across Column") == 0) {
			int i = selectedColIndex(table);
			if (i >= 0 ) {
				columnSearch = true;
				columnSearchIndex = i;
			}
			createDialogPanel();
			return;
		}
		if (source.getText().compareTo("Analyse Selected") == 0) {
			Object[] colObj = getSelectedColObject();
			if (colObj == null)
				return;
			StatisticalAnalysisPanel fp = new StatisticalAnalysisPanel(colObj);
			fp.createAndShowGUI();
			return;
		}
		if (source.getText().compareTo("Number Rendering") == 0) {
			int i = selectedColIndex(table);
			if (i < 0 ) return;
			columnSearchIndex = i;
			conditonalDialog();
			return;
		}
		if (source.getText().compareTo("WhiteSpace Rendering") == 0) {
			int i = selectedColIndex(table);
			if (i < 0 ) return;
			table.getColumnModel().getColumn(i).setCellRenderer(new MyCellRenderer(true));
			table.repaint();
			return;
		}
		if (source.getText().compareTo("BlankSpace Rendering") == 0) {
			int i = selectedColIndex(table);
			if (i < 0 ) return;
			table.getColumnModel().getColumn(i).setCellRenderer(new MyCellRenderer(false));
			table.repaint(); // to make rendering work first time
			return;
		}
		if (source.getText().compareTo("InComplete Records") == 0) {
			String count = JOptionPane.showInputDialog("Number of Columns Null or Empty?");
			if (count == null) return;
			int colcount = -1;
			try {
				colcount = Integer.parseInt(count);
			} catch (Exception exp ) {
				// do nothing
			}
			if (colcount <=0 || colcount >= table.getModel().getColumnCount()) {
				JOptionPane.showMessageDialog(null, "Invalid input: Input may ne negative or more than column count");
				return;
			}
				
			ReportTableModel rtm = FillCheck.getEmptyCount(_rt.getRTMModel(), colcount);
			if (rtm == null || rtm.getModel().getRowCount() <= 0) {
				JOptionPane.showMessageDialog(null, "Empty Data Returned");
				return;
			}
			ReportTable newRT  = new ReportTable(rtm);
			JDialog jdn = new JDialog ();
			jdn.setTitle("Table Incomplete Dialog");
			jdn.setModal(true);
			jdn.setLocation(200, 200);
			jdn.getContentPane().add(newRT);
			jdn.pack();
			jdn.setVisible(true);
			return;
		}
		

	}// End of Action Performed

	public void itemStateChanged(ItemEvent e) {
		stateChanged = true;

	}

	private String formatString(int row, int col) {
		TableCellRenderer cr = table.getCellRenderer(row, col);
		if (cr == null)
			return null;
		Component c = cr.getTableCellRendererComponent(table,
				table.getValueAt(row, col), false, false, row, col);
		if (c instanceof JLabel) {
			return ((JLabel) c).getText();
		} else {
			if (table.getValueAt(row, col) == null)
				return null;
			return table.getValueAt(row, col).toString();
		}

	}

	private Object[] getSelectedColObject() {
		int c_s = table.getSelectedColumn();
		if (c_s < 0)
			return null;
		int r_c = table.getSelectedRowCount();
		if (r_c <= 0)
			return null;

		Object[] colObj = new Object[r_c];
		int[] rowS = table.getSelectedRows();
		for (int i = 0; i < rowS.length; i++)
			colObj[i] = table.getValueAt(rowS[i], c_s);
		return colObj;
	}
	
	private void tableRenameDialog(String[] colName) {
		JPanel dp = new JPanel();
		dp.setLayout(new BorderLayout());
		
		//Create and populate the panel for table rename       
		JPanel p = new JPanel(new SpringLayout());
		int numPairs = colName.length;
		colName_t = new JTextField[numPairs];
		for (int i = 0; i < numPairs; i++) {
			JLabel l = new JLabel(colName[i],JLabel.TRAILING);
			colName_t[i] = new JTextField(colName[i]);
			p.add(l);
			l.setLabelFor(colName_t[i]);
			p.add(colName_t[i]);
		}
		
		//Lay out the panel.        
		SpringUtilities.makeCompactGrid(p,                                        
				numPairs, 2, //rows, cols                                        
				6, 6,        //initX, initY                                        
				6, 6);       //xPad, yPad          
		

		JPanel bp = new JPanel();

		JButton tstc = new JButton("Save");
		tstc.setActionCommand("save");
		tstc.addKeyListener(new KeyBoardListener());
		tstc.addActionListener(this);
		bp.add(tstc);
		
		JButton cn_b = new JButton("Exit");
		cn_b.setActionCommand("exit");
		cn_b.addKeyListener(new KeyBoardListener());
		cn_b.addActionListener(this);
		bp.add(cn_b);
		
		dp.add(p, BorderLayout.CENTER);
		dp.add(bp, BorderLayout.PAGE_END);
		
		jd = new JDialog ();
		jd.setTitle("Table Rename Dialog");
		jd.setModal(true);
		jd.setLocation(200, 200);
		jd.getContentPane().add(dp);
		jd.pack();
		jd.setVisible(true);

	}
	
	private void conditonalDialog() {
		JPanel dp = new JPanel();
		dp.setLayout(new BorderLayout());
		
		//Create and populate the panel for table rename       
		JPanel p = new JPanel(new SpringLayout());
		
		
			JLabel gl = new JLabel("Greater Than",JLabel.TRAILING);
			p.add(gl);
			gt = new JFormattedTextField(new Double(100.00D));
			p.add(gt);
		
			c_combo = new JComboBox<String>(new String[] { "Red", "Green",
					"Yellow", "Blue" });
			c_combo.setBorder(new EmptyBorder(0, 4, 0, 0));
			p.add(c_combo);
			

			JLabel el = new JLabel("Equal To",JLabel.TRAILING);
			p.add(el);
			et = new JFormattedTextField(new Double(100.00D));
			p.add(et);
		
			c_combo2 = new JComboBox<String>(new String[] { "Red", "Green",
					"Yellow", "Blue" });
			c_combo2.setBorder(new EmptyBorder(0, 4, 0, 0));
			p.add(c_combo2);
			
			
			JLabel ll = new JLabel("Less Than",JLabel.TRAILING);
			p.add(ll);
			lt = new JFormattedTextField(new Double(100.00D));
			p.add(lt);
		
			c_combo1 = new JComboBox<String>(new String[] { "Red", "Green",
					"Yellow", "Blue" });
			c_combo1.setBorder(new EmptyBorder(0, 4, 0, 0));
			c_combo1.setSelectedIndex(3); // Blue selected
			p.add(c_combo1);
			
		
		//Lay out the panel.        
		SpringUtilities.makeCompactGrid(p,                                        
				3, 3,  //rows, cols                                        
				6, 6,        //initX, initY                                        
				6, 6);       //xPad, yPad          
		

		JPanel bp = new JPanel();

		JButton tstc = new JButton("Render");
		tstc.setActionCommand("render");
		tstc.addKeyListener(new KeyBoardListener());
		tstc.addActionListener(this);
		bp.add(tstc);
		
		JButton cn_b = new JButton("Cancel");
		cn_b.setActionCommand("cancelcond");
		cn_b.addKeyListener(new KeyBoardListener());
		cn_b.addActionListener(this);
		bp.add(cn_b);
		
		dp.add(p, BorderLayout.CENTER);
		dp.add(bp, BorderLayout.PAGE_END);
		
		jdc = new JDialog ();
		jdc.setTitle("Conditioning Rendering Dialog");
		jdc.setModal(true);
		jdc.setLocation(200, 200);
		jdc.setPreferredSize(new Dimension(400, 200));
		jdc.getContentPane().add(dp);
		jdc.pack();
		jdc.setVisible(true);

	}
	
	/* Link Mouse Adapter */
	private class LinkMouseListener extends MouseAdapter implements ActionListener {
		private Hashtable<String,String>__h ;
		
		public void mouseClicked(MouseEvent mouseevent) {
			
			try {
				String s1 = ((JLabel) mouseevent.getSource()).getText();
				if (s1 != null && s1.equals("<html><body><a href=\"\">Saved Regex</A><body></html>")) {
					//popup will come here
					JMenuItem menuItem;
					JPopupMenu popup = new JPopupMenu();

					// Create the popup menu From regexString.txt
					__h = KeyValueParser.parseFile("resource/popupmenu.txt");
					if (__h == null)
						return;
					Enumeration<String> enum1 = __h.keys();
					while (enum1.hasMoreElements()) {
						String key_n = (String) enum1.nextElement();

						menuItem = new JMenuItem(key_n);
						menuItem.addActionListener(this);
						popup.add(menuItem);
					}
					popup.show(mouseevent.getComponent(), mouseevent.getX(), mouseevent.getY());
				} 
			} catch (Exception exp) {
				ConsoleFrame.addText("\n Exception " + exp.getMessage());

			}
		}

		public void mouseEntered(MouseEvent mouseevent) {
			mouseevent.getComponent().setCursor(Cursor.getPredefinedCursor(12));
		}

		private LinkMouseListener() {
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			f.setText(__h.get(e.getActionCommand()));
			em.setSelected(false);
			
		}

	}
	
	private int selectedColIndex(JTable  table) {
		int colC = table.getColumnCount();
		Object[] colN = new Object[colC];
		for (int i = 0; i < colC; i++)
		 colN[i] = (i + 1) + "," + table.getColumnName(i);	
		String input = (String) JOptionPane.showInputDialog(null,
				"Select the Column ", "Column Selection Dialog",
				JOptionPane.PLAIN_MESSAGE, null, colN, colN[0]);
		if (input == null || input.equals(""))
			return -1;

		String col[] = input.split(",", 2);
		int index = Integer.valueOf(col[0]).intValue();
		return index - 1;
	}
	
	private class MyCellRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private double _gt, _lt, _et;
		private int _colorgt,_colorlt,_coloret;
		private boolean whitespacerender = false;
		private Pattern p;
		

		public MyCellRenderer(double gt, int colorgt, double lt, int colorlt,double et, int coloret ) {
			_gt = gt;
			_lt = lt;
			_et = et;
			_colorgt = colorgt;
			_colorlt = colorlt;
			_coloret = coloret;
		}
		public MyCellRenderer(boolean whitespace ) {
			whitespacerender = true;
			if ( whitespace == true )
				p = Pattern.compile("\\s",Pattern.MULTILINE);
			else
				p = Pattern.compile(" ",Pattern.MULTILINE);

		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			if (value == null) return c; // for null value
			
			if (whitespacerender == true) { // whitespace matcher
				Matcher m = p.matcher(value.toString());
				
				if ( m.find() == true ) 
						c.setForeground(Color.RED.darker());
				 else 
					c.setForeground(Color.BLACK); // Default Color
				
				return c;
			}
			
			try{
				double din = Double.parseDouble(value.toString());
			
				if (din > _gt) {
					if (_colorgt == 0 )
						c.setForeground(Color.RED.darker());
					if (_colorgt == 1 )
						c.setForeground(Color.GREEN.darker().darker().darker()); // background is also green
					if (_colorgt == 2 )
						c.setForeground(Color.YELLOW.darker());
					if (_colorgt == 3 )
						c.setForeground(Color.BLUE.darker());
				} else if (din < _lt) {
					if (_colorlt == 0 )
						c.setForeground(Color.RED.darker());
					if (_colorlt == 1 )
						c.setForeground(Color.GREEN.darker().darker().darker());
					if (_colorlt == 2 )
						c.setForeground(Color.YELLOW.darker());
					if (_colorlt == 3 )
						c.setForeground(Color.BLUE.darker());
				} else if (din == _et) {
					if (_coloret == 0 )
						c.setForeground(Color.RED.darker());
					if (_coloret == 1 )
						c.setForeground(Color.GREEN.darker().darker().darker());
					if (_coloret == 2 )
						c.setForeground(Color.YELLOW.darker());
					if (_coloret == 3 )
						c.setForeground(Color.BLUE.darker());
				} else {
					c.setForeground(Color.BLACK); //Default color
				}
			} catch (Exception e) {
					return c;
			}
			((JLabel) c).setHorizontalAlignment(JLabel.TRAILING);	
			return c;
		}
	} // End of MyCellRenderer

}
