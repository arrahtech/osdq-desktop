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

/* 
 * This files creates Interface for Bin data 
 * and also provides methods to populate the
 * bin.
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class PlotterDataPanel extends JPanel implements PropertyChangeListener,
		KeyListener, FocusListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFormattedTextField text[] = new JFormattedTextField[11]; // 11 for
																		// 10
																		// bin
																		// values
	private JTextField binName[] = new JTextField[10];
	private String _dsn = "", _table = "", _column = "", _type = "";
	private double[] r_values = new double[10];
	private JComboBox<String> c_combo;
	private int color_index = 0;
	private double[] values = new double[text.length];

	private NumberFormat numberFormat = null;

	public PlotterDataPanel() {
		EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);
		BevelBorder bevelBorder = new BevelBorder(BevelBorder.LOWERED);
		CompoundBorder compoundBorder = new CompoundBorder(emptyBorder,
				bevelBorder);
		this.setBorder(new CompoundBorder(compoundBorder, emptyBorder));

		this.setLayout(new GridLayout(0, 2));
		this.add(new JLabel(" Lower Value "));
		this.add(new JLabel(" Bin Names "));

		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumIntegerDigits(100);

		for (int i = 0; i < text.length; i++) {
			text[i] = new JFormattedTextField(numberFormat);
			text[i].setColumns(8);
			text[i].setValue(new Integer(0));
			text[i].addPropertyChangeListener("value", this);
			text[i].addKeyListener(this);
			text[i].addFocusListener(this);
			text[i].setToolTipText("<html> Bin or Container will show <br> <b> count </b> of values lying in bin range.<br> If 0,10,100,1000,.... are the values <br> in <b>Lower Value</b> column <br> Bin 1 will show count of values <b>&gt;=0 and &lt;10 </b><br> Bin 2 will show count of values <b>&gt;= 10 and &lt;100 </b><br> and so on </html> ");
		}

		for (int i = 0; i < binName.length; i++) {
			binName[i] = new JTextField("Bin " + (i + 1));
			this.add(text[i]);
			this.add(binName[i]);

		}
		this.add(text[10]);

		c_combo = new JComboBox<String>(
				new String[] { "Red", "Green", "Yellow", "Blue" });
		c_combo.setRenderer(new MyCellRenderer());

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				color_index = c_combo.getSelectedIndex();
			}
		};
		c_combo.addActionListener(actionListener);
		this.add(c_combo);

	}

	public void init(Hashtable<String, String> map) {
		_dsn = (String) map.get("Schema");
		_type = (String) map.get("Type");
		_table = (String) map.get("Table");
		_column = (String) map.get("Column");
	}

	public double[] fillXValues() throws SQLException {
		QueryBuilder q_factory = new QueryBuilder(_dsn, _table, _column,
				Rdbms_conn.getDBType());
		String q_str = q_factory.get_prep_query();
		try {

			Rdbms_conn.openConn();
			PreparedStatement stmt = Rdbms_conn.createQuery(q_str);
			if (stmt == null) {
				ConsoleFrame.addText("\n ERROR:Bin Query Null");
				return null;
			}

			for (int i = 0; i < text.length; i++) {
				values[i] = ((Number) text[i].getValue()).doubleValue();
				if (i == 0)
					continue;

				int ic = q_str.indexOf(" ?", 0);
				if (ic != -1)
					stmt.setDouble(1, values[i - 1]);

				ic = q_str.indexOf(" ?", ic + 1);
				if (ic != -1)
					stmt.setDouble(2, values[i]);

				ic = q_str.indexOf(" ?", ic + 1);
				if (ic != -1) {
					Vector[] dateVar = QueryBuilder.getDateCondition();
					for (int j = 0; j < dateVar[0].size(); j++) {
						String s1 = (String) dateVar[1].get(j);
						if (s1.compareToIgnoreCase("time") == 0)
							stmt.setTime(
									j + 3,
									new Time(((Date) dateVar[0].get(j))
											.getTime()));
						if (s1.compareToIgnoreCase("date") == 0)
							stmt.setDate(j + 3, new java.sql.Date(
									((Date) dateVar[0].get(j)).getTime()));
						if (s1.compareToIgnoreCase("timestamp") == 0)
							stmt.setTimestamp(j + 3, new Timestamp(
									((Date) dateVar[0].get(j)).getTime()));
					}
				} // End of date condition

				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					double val = rs.getDouble("row_count");
					r_values[i - 1] = val;
				}
				rs.close();
			}
			Rdbms_conn.closeConn();

		} catch (SQLException e) {
			ConsoleFrame.addText("\n SQL Exception in Bin Query");
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Message", JOptionPane.ERROR_MESSAGE);
			throw e;
		}
		return r_values;
	}

	public double[] getXValues() {
		return r_values;
	}

	public double[] getBboundary() {
		return values;
	}

	public String[] getNValues() {
		String[] values = new String[binName.length];
		for (int i = 0; i < binName.length; i++) {
			values[i] = binName[i].getText();
		}

		return values;
	}

	public void propertyChange(PropertyChangeEvent event) {
	}

	public void keyTyped(KeyEvent event) {
		if ((event.getKeyChar() < '0' || event.getKeyChar() > '9')
				&& event.getKeyChar() != '.' && event.getKeyChar() != '-'
				&& event.getKeyCode() != KeyEvent.VK_BACK_SPACE)
			event.consume();
	}

	public void keyPressed(KeyEvent event) {
		if ((event.getKeyChar() < '0' || event.getKeyChar() > '9')
				&& event.getKeyChar() != '.' && event.getKeyChar() != '-'
				&& event.getKeyCode() != KeyEvent.VK_BACK_SPACE)
			event.consume();
	}

	public void keyReleased(KeyEvent event) {
		if ((event.getKeyChar() < '0' || event.getKeyChar() > '9')
				&& event.getKeyChar() != '.' && event.getKeyChar() != '-'
				&& event.getKeyCode() != KeyEvent.VK_BACK_SPACE)
			event.consume();
	}

	public void focusGained(FocusEvent event) {
		if (event.getSource() instanceof JFormattedTextField) {
			final JFormattedTextField textField = (JFormattedTextField) event
					.getSource();
			textField.setSelectedTextColor(Color.blue);
			textField.setSelectionColor(Color.yellow);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					textField.selectAll();
				}
			});

		}
	}

	public void focusLost(FocusEvent event) {
	}

	public int getColorIndex() {
		return color_index;
	}

	private class MyCellRenderer extends JLabel implements ListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MyCellRenderer() {
			setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel l = new JLabel(value.toString());
			l.setOpaque(true);
			l.setBackground(Color.white);
			switch (index) {
			case 0:
				l.setForeground(Color.red);
				break;
			case 1:
				l.setForeground(Color.green);
				break;
			case 2:
				l.setForeground(Color.yellow);
				break;
			default:
				l.setForeground(Color.blue);
				break;
			}
			if (isSelected == true)
				l.setBorder(BorderFactory.createLineBorder(Color.black));

			return l;
		}
	}

}
