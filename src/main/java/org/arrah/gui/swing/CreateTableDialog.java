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
 * This class defines popup Query Dialog
 * from the link of table to take condition
 *
 */

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;

import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.arrah.framework.hadooputil.HiveQueryBuilder;
import org.arrah.framework.profile.DBMetaInfo;
import org.arrah.framework.rdbms.Rdbms_conn;

public class CreateTableDialog extends JDialog implements ActionListener,
		ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<JComboBox<String>> datatype_cb_v;
	private Vector<JTextField> colName_v,defaultval_v,othertype_v;
	private Vector<JCheckBox> nullable_v,primarykey_v,partitionkey_v;
	private JTextField _tableName_tf,fielddelim_tf;
	private JPanel cp;
	
	private int capacity;
	
	private String coldesc="";
	private String consdesc="";
	private String partcoldesc="";

	
	
	public CreateTableDialog () {
		
		// Initialize vector
		datatype_cb_v = new Vector<JComboBox<String>>();
		colName_v = new Vector<JTextField>();
		othertype_v = new Vector<JTextField>();
		defaultval_v = new Vector<JTextField>();
		nullable_v = new Vector<JCheckBox>();
		primarykey_v = new Vector<JCheckBox>();
		partitionkey_v = new Vector<JCheckBox>();
		
		// foreigntable_v = new Vector<JComboBox<String>>();
		capacity = 0;
		
		// Create Panel now
		createPanel();
	}

	
	public void actionPerformed(ActionEvent actionevent) {
		String s = actionevent.getActionCommand();
		if (s.equals("addcol")) {
				addRow();
			cp.setLayout(new SpringLayout());
			SpringUtilities.makeCompactGrid(cp, capacity, 7, 3, 3, 3, 3);
			cp.revalidate();

		}  else if (s.equals("cancel")) {
			dispose();
		} else if (s.equals("createtable")) {
			
				String query=parseTableInput();
				if (query== null || "".equals(query))
					return;
			
				try {
					Rdbms_conn.openConn();
					HiveQueryBuilder queryDB = new HiveQueryBuilder(
							Rdbms_conn.getHValue("Database_DSN"),Rdbms_conn.getDBType());
					
					if (Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") == 0 ) {
						 String fielddelim = fielddelim_tf.getText();
						String createQuery=queryDB.createHiveTable(_tableName_tf.getText(),coldesc,partcoldesc,fielddelim);
						Rdbms_conn.execute(createQuery);
					} else {
						String createQuery=queryDB.createRDBMSTable(coldesc,_tableName_tf.getText(),consdesc);
						Rdbms_conn.executeUpdate(createQuery);
					}
					Rdbms_conn.closeConn();

					ConsoleFrame.addText("\n The table " + _tableName_tf.getText() + " was created successfully");
					JOptionPane.showMessageDialog(null,
							"Table "+_tableName_tf.getText()+ " Created. \n Refresh to get updated list.", "Table Creation Success",
							JOptionPane.INFORMATION_MESSAGE);
					this.dispose();

				} catch (Exception e) {
					ConsoleFrame.addText("Error in creating table " + e.getLocalizedMessage());
					JOptionPane.showMessageDialog(null,
							e.getLocalizedMessage(), "Table Creation Error",
							JOptionPane.ERROR_MESSAGE);
				}
		}
	}

	private void createPanel() {
		JPanel jpanel = new JPanel();
		SpringLayout springlayout = new SpringLayout();
		jpanel.setLayout(springlayout);
		
		JLabel tableName = new JLabel("Table Name");
		_tableName_tf = new JTextField();
		_tableName_tf.setColumns(10);
		JButton addcol = new JButton("Add Column");
		addcol.setActionCommand("addcol");
		addcol.addActionListener(this);
		addcol.addKeyListener(new KeyBoardListener());
		cp = new JPanel(new SpringLayout());
		for (int i = 0; i < 5; i++) // create default 5 col Name
			addRow();

		SpringUtilities.makeCompactGrid(cp, capacity, 7, 3, 3, 3, 3);
		JScrollPane jscrollpane1 = new JScrollPane(cp);
		jscrollpane1.setPreferredSize(new Dimension(775, 300));
		
		JButton jbutton1 = new JButton("Create");
		jbutton1.setActionCommand("createtable");
		jbutton1.addActionListener(this);
		jbutton1.addKeyListener(new KeyBoardListener());
		
		JButton jbutton2 = new JButton("Cancel");
		jbutton2.setActionCommand("cancel");
		jbutton2.addActionListener(this);
		jbutton2.addKeyListener(new KeyBoardListener());

		jpanel.add(tableName);jpanel.add(_tableName_tf);
		jpanel.add(addcol);jpanel.add(jscrollpane1);
		jpanel.add(jbutton1);jpanel.add(jbutton2);
		
		// Hive will extra options of choosing row and field delimiter
		JLabel fieldLabel= new JLabel("Field Delimiter");
		if (Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") == 0 ) {
			 fielddelim_tf = new JTextField(5);
			fielddelim_tf.setText("\\t");
			jpanel.add(fieldLabel);jpanel.add(fielddelim_tf);
		}
		
		springlayout.putConstraint("West", tableName, 10, "West", jpanel);
		springlayout.putConstraint("North", tableName, 5, "North", jpanel);
		springlayout.putConstraint("West", _tableName_tf, 5, "East", tableName);
		springlayout.putConstraint("North", _tableName_tf, 5, "North", jpanel);
		springlayout.putConstraint("West", addcol, 15, "West", jpanel);
		springlayout.putConstraint("North", addcol, 5, "South", tableName);

		springlayout.putConstraint("West", jscrollpane1, 5, "West", jpanel);
		springlayout.putConstraint("North", jscrollpane1, 10, "South",addcol);
		
		// Hive will extra options of choosing row and field delimiter
		if (Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") == 0 ) {
			springlayout.putConstraint("West", fieldLabel, 15, "West", jpanel);
			springlayout.putConstraint("West",fielddelim_tf , 15, "East", fieldLabel);
			springlayout.putConstraint("North", fielddelim_tf, 10, "South", jscrollpane1);
			springlayout.putConstraint("North", fieldLabel, 0, "North", fielddelim_tf);

		}
		springlayout.putConstraint("West", jbutton1, 250, "West", jpanel);
		springlayout.putConstraint("South", jbutton1, -5, "South", jpanel);
		springlayout.putConstraint("West", jbutton2, 10, "East", jbutton1);
		springlayout.putConstraint("South", jbutton2, -5, "South", jpanel);
	
		getContentPane().add(jpanel);
		
		this.setLocation(175, 100);
		this.setPreferredSize(new Dimension(800, 450));
		this.setTitle(" Create Table Dialog ");
		this.setModal(true);
		this.pack();
		this.setVisible(true);
	}

	private void addRow() {
		
		JTextField colName = new JTextField(10);
		colName.setToolTipText("Enter Column Name");
		JTextField othertype = new JTextField(10);
		othertype.setToolTipText("Enter Data Type");
		
		JComboBox<String> dataType = null;
		// JComboBox<String> foreignTable = null, foreignCol = null;
		
		JCheckBox isNull = new JCheckBox("Not Null"),isPrimaryKey = new JCheckBox("Primary Key");
		
		JCheckBox isPartitionKey = new JCheckBox("Partition");
		isPartitionKey.setEnabled(false); // It is for HIVE 
		isPartitionKey.setToolTipText("Partitioned Column in Hive");
		
		JTextField defaultValue = new JTextField();
		defaultValue.setToolTipText("Enter Default Value of Column");
		
		boolean isDataTypeValid = false;
		
		colName.setText("ColName"+(capacity+1));
		
		/* Initializing and populating the value */
		colName_v.add(capacity, colName);
		Vector<String> datatype_s = null;
		
		try { // see if JDBC can fetch data type info
			datatype_s = DBMetaInfo.getDataTypeInfo();
			if (datatype_s != null && datatype_s.size() > 0) {
				isDataTypeValid = true;
			}
		} catch (SQLException sqlexp) {
			isDataTypeValid = false;
			// This method is not supported in Hive yet
		}
		
		if (isDataTypeValid == true) {
			dataType = new JComboBox<String>(datatype_s);
		} else if(Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") == 0 ) {
			dataType = new JComboBox<String>(new String[] {"TINYINT", "SMALLINT", "INT", "BIGINT",
					"BOOLEAN", "FLOAT", "DOUBLE", "DECIMAL", "STRING", "BINARY", "TIMESTAMP","Others"});
		} else {
			dataType = new JComboBox<String>(new String[] {"Others"});
			dataType.setEnabled(false);
		}
		dataType.addItemListener(this);
		dataType.setToolTipText("Select Data Type");
		othertype.setText(dataType.getItemAt(0));
		datatype_cb_v.add(capacity, dataType);
		othertype_v.add(capacity, othertype);
		
		/* Hive does not support following feature for create Table */
		if (Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("hive") == 0 ) {
			defaultValue.setEnabled(false);
			isNull.setEnabled(false);
			isPrimaryKey.setEnabled(false);
			isPartitionKey.setEnabled(true);
		}
		
		defaultValue.setColumns(8);
		defaultval_v.add(capacity, defaultValue);
		nullable_v.add(capacity,isNull);
		primarykey_v.add(capacity,isPrimaryKey);
		partitionkey_v.add(capacity,isPartitionKey);
		
		
		/* Add value to Panel */
		cp.add(colName);
		cp.add(dataType);
		cp.add(othertype);
		cp.add(isPartitionKey);
		cp.add(isNull);
		cp.add(isPrimaryKey);
		cp.add(defaultValue);
		capacity++;
	}

	/* Needed for Foreign key selection only */
	public void itemStateChanged(ItemEvent itemevent) {
		int i = datatype_cb_v.indexOf(itemevent.getSource());
		if (itemevent.getStateChange() == ItemEvent.SELECTED) {
			JComboBox<String> jcombobox = (JComboBox<String>) datatype_cb_v.get(i);
			String sel_data = jcombobox.getSelectedItem().toString();
			if (sel_data.compareToIgnoreCase("VARCHAR") == 0 ||
					sel_data.compareToIgnoreCase("VARBINARY") == 0) // add varchar value
				othertype_v.get(i).setText(sel_data+"(255)");
			else
				othertype_v.get(i).setText(sel_data);
		}
	}
	
	private String parseTableInput() {
		
		String tabName = _tableName_tf.getText();
		if ( tabName == null || "".equals(tabName)) {
			JOptionPane.showMessageDialog(null,
					"Table Name can not be empty", "Table Error",
					JOptionPane.ERROR_MESSAGE);
			return "";
		}
		
		// Create the Column description and Constraint description
		coldesc=""; consdesc=""; partcoldesc="";
		String primaryCol="";
		for (int i=0; i < capacity; i++ ) {
			
			String colNameVal="";
			String colName = colName_v.get(i).getText();
			if ( colName == null || "".equals(colName)) // ignore invalid colName
				continue;
			else 
				colNameVal = colName;
			
			String datatypeval = "";
			String datatype = othertype_v.get(i).getText();
			
			if ( datatype == null || "".equals(datatype)) // ignore invalid data type
				continue;
			else if (datatype.compareToIgnoreCase("VARCHAR") == 0 || 
					datatype.compareToIgnoreCase("VARBINARY") == 0 ) {// Varchar should be Varchar(num)
				datatypeval = datatype+"(255)"; // default value
			} else 
				datatypeval = datatype;
			
			String nullable="";
			boolean isnullable = nullable_v.get(i).isSelected();
			if (isnullable == true)
				nullable = " NOT NULL ";
			
			String defaultval="";
			String defaultv = defaultval_v.get(i).getText();
			if (! ( defaultv == null || "".equals(defaultv)) ) // Default value set
				defaultval = " DEFAULT "+defaultv;
			
			boolean ispartition = partitionkey_v.get(i).isSelected();
			if (ispartition == false ) {
			if ("".equals(coldesc) == true) 
				coldesc = colNameVal+","+datatypeval+","+nullable+","+defaultval;
			else
				coldesc += ":"+colNameVal+","+datatypeval+","+nullable+","+defaultval;
			} else {
				if ("".equals(partcoldesc) == true) 
					partcoldesc = colNameVal+","+datatypeval+","+nullable+","+defaultval;
				else
					partcoldesc += ":"+colNameVal+","+datatypeval+","+nullable+","+defaultval;
			}
			
			boolean isprimary = primarykey_v.get(i).isSelected();
			if (isprimary == true ) {
				if ("".equals(primaryCol) == true) 
					primaryCol = colNameVal;
				else {
					primaryCol += ";"+colNameVal; // :, are used for splitting so using a new meta value
				}
				
			}
			
		}
		if (primaryCol.length() > 0)
			if(Rdbms_conn.getHValue("Database_Type").compareToIgnoreCase("Informix") == 0 )
				consdesc = "PRIMARY KEY ("+primaryCol+")" + " CONSTRAINT " + "PK_"+primaryCol;
			else
				consdesc = " p_key,PRIMARY KEY,("+primaryCol+")";
		
		return coldesc;
		
	}

}
