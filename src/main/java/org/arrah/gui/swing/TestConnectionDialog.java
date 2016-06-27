package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for Testing RDBMS connection
 *
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.arrah.framework.rdbms.Rdbms_NewConn;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.xml.XmlWriter;

public class TestConnectionDialog extends JDialog implements ActionListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Hashtable<String,String> _dbparam;
	private JComboBox<String> jc,restype,resconcur,jccon;
	private JPasswordField passfield ;
	private JTextField dsn,user,driver,protocol,jdbc_cs,coname;
	private JTextField catalog,schemaPattern,tablePattern,colPattern,type;
	private JTextArea info;
	private JButton ok_b,add_b;
	private JCheckBox quoteC, demoDB, fileLoad;
	
	private String infoStatus ="INFORMATION: \n$ORACLE_HOME/lib should be in LIBPATH (for AIX), \n" +
			"LD_LIBRARY_PATH (for Solaris) or \nSHLIBPATH (for HP) for UNIX user. \n\n"+
			"For Windows user PATH variable should be set." +
			"\n\n $ORACLE_HOME/jdbc/lib/ojdbc*.jar should be in CLASSPATH";
				
	private int dbIndex;
	private int connectionType = 0 ; // default existing connection
	private boolean isFileLoad=false;

	public TestConnectionDialog(int connectionType) {
		_dbparam = new Hashtable <String,String>();
		_dbparam.put("Database_Type", "ORACLE_NATIVE");
		_dbparam.put("Database_Driver", "oracle.jdbc.OracleDriver");
		_dbparam.put("Database_Protocol", "jdbc:oracle:thin");
		_dbparam.put("Database_DSN", "");
		_dbparam.put("Database_User", "");
		_dbparam.put("Database_Passwd", "");
		_dbparam.put("Database_Catalog", "");
		_dbparam.put("Database_SchemaPattern", "");
		_dbparam.put("Database_TablePattern", "");
		_dbparam.put("Database_ColumnPattern", "");
		_dbparam.put("Database_TableType", "TABLE"); // Default show tables
		_dbparam.put("Database_ResultsetType", "");
		_dbparam.put("Database_ResultsetConcur", "");
		_dbparam.put("Database_SupportQuote", "");
		
		this.setConnectionType(connectionType);
		this.setLocation(400, 200);
		this.setModal(true);
	}

	public void createGUI() {
		JPanel dp = new JPanel();
		dp.setLayout(new BorderLayout());
		
		
		String[] labels = {" *DB Connections: ", " *DB Connection Name", " *DB Type: ", " *DB Name: ", " *User: ", " *Password: "," DB Driver"," DB Protocol",
				"JDBC URL","DB Catalog" ,"DB Schema Pattern", "DB Table Pattern","DB Column Pattern","DB Show Type"}; 
		int numPairs = labels.length;  
		
		String[] dbtype = {"Oracle JDBC Client","Oracle Windows Bridge","Mysql JDBC Client","Mysql Windows Bridge",
							"SQLServer JDBC Client","SQLServer Windows Bridge","Access JDBC Client",
							"Access Windows Bridge","Postgres JDBC Client","DB2 JDBC Client",
							"Hive JDBC Client","Hive2 JDBC Client","Informix JDBC Client","Splice Derby Client","Others (JDBC Bridge)","Others (Windows Bridge)"}; 
		String[] cname = {"New Connection"};
		jc = new JComboBox<String>(dbtype);
		jc.addActionListener(this);
                
        jccon = new JComboBox<String>(cname);
		
		//Create and populate the panel which is enhanced by Advance button        
		JPanel p = new JPanel(new SpringLayout());   
		
		JLabel l = new JLabel(labels[0], JLabel.TRAILING);             
		p.add(l);                        
		l.setLabelFor(jccon);             
		p.add(jccon);
                
                l = new JLabel(labels[1], JLabel.TRAILING);             
		p.add(l);             
		coname = new JTextField(10);
		l.setLabelFor(coname);             
		p.add(coname); 
		coname.setText("");
		coname.addFocusListener(new ConnFocusEvent("cname"));
                
		l = new JLabel(labels[2], JLabel.TRAILING);             
		p.add(l);                        
		l.setLabelFor(jc);             
		p.add(jc); 
		
		l = new JLabel(labels[3], JLabel.TRAILING);             
		p.add(l);             
		dsn = new JTextField(10);
		l.setLabelFor(dsn);             
		p.add(dsn); 
		dsn.setText("//hostname/SID");
		dsn.addFocusListener(new ConnFocusEvent("dsn"));
		
		l = new JLabel(labels[4], JLabel.TRAILING);             
		p.add(l);             
		user = new JTextField(10);             
		l.setLabelFor(user);             
		p.add(user); 
		user.addFocusListener(new ConnFocusEvent("user"));
		
		JLabel pass = new JLabel(labels[5], JLabel.TRAILING);             
		p.add(pass);                        
		passfield = new JPasswordField();
		pass.setLabelFor(passfield); 
		p.add(passfield); 
		passfield.addFocusListener(new ConnFocusEvent("passfield"));
		
		JLabel driver_l = new JLabel(labels[6], JLabel.TRAILING);             
		p.add(driver_l);                        
		driver = new JTextField("oracle.jdbc.OracleDriver");
		pass.setLabelFor(driver); 
		p.add(driver);
		driver.setEditable(false);
		driver.addFocusListener(new ConnFocusEvent("driver"));
		
		JLabel protocol_l = new JLabel(labels[7], JLabel.TRAILING);             
		p.add(protocol_l); 
		// protocol = new JTextField("jdbc:oracle:oci8");
		protocol = new JTextField("jdbc:oracle:thin");
		protocol_l.setLabelFor(protocol); 
		p.add(protocol);
		protocol.setEditable(false);
		protocol.addFocusListener(new ConnFocusEvent("protocol"));
		
		JLabel jdbc_cs_l = new JLabel(labels[8], JLabel.TRAILING);             
		p.add(jdbc_cs_l); 
		jdbc_cs = new JTextField();
		jdbc_cs_l.setLabelFor(jdbc_cs); 
		p.add(jdbc_cs);
		jdbc_cs.setEditable(false);
		jdbc_cs.addFocusListener(new ConnFocusEvent("jdbc_cs"));
		
		l = new JLabel(labels[9], JLabel.TRAILING);             
		p.add(l);             
		catalog = new JTextField(10);             
		l.setLabelFor(catalog);             
		p.add(catalog); 
		catalog.addFocusListener(new ConnFocusEvent("catalog"));
		
		l = new JLabel(labels[10], JLabel.TRAILING);             
		p.add(l);             
		schemaPattern = new JTextField(10);             
		l.setLabelFor(schemaPattern);             
		p.add(schemaPattern); 
		schemaPattern.addFocusListener(new ConnFocusEvent("schemaPattern"));
		
		l = new JLabel(labels[11], JLabel.TRAILING);             
		p.add(l);             
		tablePattern = new JTextField(10);             
		l.setLabelFor(tablePattern);             
		p.add(tablePattern);
		tablePattern.addFocusListener(new ConnFocusEvent("tablePattern"));
		
		l = new JLabel(labels[12], JLabel.TRAILING);             
		p.add(l);             
		colPattern = new JTextField(10);             
		l.setLabelFor(colPattern);             
		p.add(colPattern);
		colPattern.addFocusListener(new ConnFocusEvent("colPattern"));
		
		l = new JLabel(labels[13], JLabel.TRAILING);             
		p.add(l);             
		type = new JTextField(10);             
		l.setLabelFor(type);
		type.setText("TABLE");
		p.add(type);
		type.addFocusListener(new ConnFocusEvent("type"));
		
		//Lay out the panel.        
		SpringUtilities.makeCompactGrid(p,                                        
				numPairs, 2, //rows, cols                                        
				6, 6,        //initX, initY                                        
				6, 6);       //xPad, yPad          
		

		// Bottom Panel 
		JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 25));
		bp.setPreferredSize(new Dimension(500,70));
		
		fileLoad = new JCheckBox("File Load");
		fileLoad.addItemListener(this);
		bp.add(fileLoad);
		
		demoDB = new JCheckBox("Demo DB");
		demoDB.addItemListener(this);
		bp.add(demoDB);
		
		JButton tstc = new JButton("Test Connection");
		tstc.setActionCommand("testconn");
		tstc.addKeyListener(new KeyBoardListener());
		tstc.addActionListener(this);
		bp.add(tstc);
		
		add_b = new JButton("Add");
		add_b.setActionCommand("addconn");
		add_b.addKeyListener(new KeyBoardListener());
		add_b.addActionListener(this);
		bp.add(add_b);
		add_b.setEnabled(false);
        
		/*** Not used for now
		JButton modc = new JButton("Modify");
		modc.setActionCommand("modconn");
		modc.addKeyListener(new KeyBoardListener());
		modc.addActionListener(this);
		bp.add(modc);
        modc.setEnabled(false);
		
		JButton delc = new JButton("Delete");
		delc.setActionCommand("delconn");
		delc.addKeyListener(new KeyBoardListener());
		delc.addActionListener(this);
		bp.add(delc);
        delc.setEnabled(false);
        ***/
		
		ok_b = new JButton("Continue");
		ok_b.setActionCommand("continue");
		ok_b.addKeyListener(new KeyBoardListener());
		ok_b.addActionListener(this);
		bp.add(ok_b);
		ok_b.setEnabled(false);

		JButton cn_b = new JButton("Exit");
		cn_b.setActionCommand("cancel");
		cn_b.addKeyListener(new KeyBoardListener());
		cn_b.addActionListener(this);
		bp.add(cn_b);
		
		// Create Information Panel
		JPanel infoPanel = new JPanel(new BorderLayout());
		
		
		info = new JTextArea(20,20);
		info.setEditable(false);
		info.setWrapStyleWord(true);
                info.setLineWrap(true);
		info.setBackground(this.getBackground());
		JScrollPane scrollPane = new JScrollPane(info);
		scrollPane.setPreferredSize(new Dimension(300, 300));

		infoPanel.add(scrollPane,BorderLayout.CENTER);
		info.setText(infoStatus);
		// Add resultset Input
		JPanel respanel = createResultsetInputPanel();
		infoPanel.add(respanel,BorderLayout.PAGE_END);
		
		dp.add(p, BorderLayout.CENTER);
		dp.add(infoPanel,BorderLayout.EAST);
		dp.add(bp, BorderLayout.SOUTH);
		

		this.setTitle("Arrah Technology Connection Dialog");
		this.getContentPane().add(dp,BorderLayout.CENTER);
		this.pack();
                this.setLocationRelativeTo(null);
		this.setVisible(true);

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox ) {
			cleanText();
			infoStatus="";
			info.setText(infoStatus);
			@SuppressWarnings("unchecked")
			JComboBox<String> cb = (JComboBox<String>)e.getSource();
	        dbIndex = cb.getSelectedIndex();
	        switch (dbIndex) {
	        	case 0 :
	        		_dbparam.put("Database_Type", "ORACLE_NATIVE");
	        		driver.setText("oracle.jdbc.OracleDriver");
	        		protocol.setText("jdbc:oracle:thin");
	        		_dbparam.put("Database_Driver", "oracle.jdbc.OracleDriver");
	        		_dbparam.put("Database_Protocol", "jdbc:oracle:thin");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//hostname/SID");
	        		 infoStatus ="INFORMATION: \n$ORACLE_HOME/lib should be in LIBPATH (for AIX), \n" +
	        					"LD_LIBRARY_PATH (for Solaris) or \nSHLIBPATH (for HP) for UNIX user. \n\n"+
	        					"For Windows user PATH variable should be set." +
	        					"\n\n $ORACLE_HOME/jdbc/lib/ojdbc*.jar should be in CLASSPATH";
	        		 info.setText(infoStatus);
	        		 disableResInput();
	        		break;
	        	case 1 :
	        		_dbparam.put("Database_Type", "ORACLE_ODBC");
	        		driver.setText("sun.jdbc.odbc.JdbcOdbcDriver");
	        		protocol.setText("jdbc:odbc");
	        		_dbparam.put("Database_Driver", "sun.jdbc.odbc.JdbcOdbcDriver");
	        		_dbparam.put("Database_Protocol", "jdbc:odbc");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		disableResInput();
	        		break;
	        	case 2 :
	        		_dbparam.put("Database_Type", "MYSQL");
	        		driver.setText("com.mysql.jdbc.Driver");
	        		protocol.setText("jdbc:mysql");
	        		_dbparam.put("Database_Driver", "com.mysql.jdbc.Driver");
	        		_dbparam.put("Database_Protocol", "jdbc:mysql");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//hostname/db");
	        		disableResInput();
	        		break;
	        	case 3 :
	        		_dbparam.put("Database_Type", "MYSQL");
	        		driver.setText("sun.jdbc.odbc.JdbcOdbcDriver");
	        		protocol.setText("jdbc:odbc");
	        		_dbparam.put("Database_Driver", "sun.jdbc.odbc.JdbcOdbcDriver");
	        		_dbparam.put("Database_Protocol", "jdbc:odbc");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		disableResInput();
	        		break;
	        	case 4 :
	        		_dbparam.put("Database_Type", "SQL_SERVER");
	        		driver.setText("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	        		protocol.setText("jdbc:sqlserver");
	        		_dbparam.put("Database_Driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
	        		_dbparam.put("Database_Protocol", "jdbc:sqlserver");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//hostname;databaseName=db");
	        		disableResInput();
	        		break;
	        	case 5 :
	        		_dbparam.put("Database_Type", "SQL_SERVER");
	        		driver.setText("sun.jdbc.odbc.JdbcOdbcDriver");
	        		protocol.setText("jdbc:odbc");
	        		_dbparam.put("Database_Driver", "sun.jdbc.odbc.JdbcOdbcDriver");
	        		_dbparam.put("Database_Protocol", "jdbc:odbc");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		disableResInput();
	        		break;
	        		
	        	case 6 : // Enable Input as it is not tested fully
	        		_dbparam.put("Database_Type", "MS_ACCESS_JDBC");
	        		driver.setText("net.ucanaccess.jdbc.UcanaccessDriver");
	        		protocol.setText("jdbc:ucanaccess");
	        		_dbparam.put("Database_Driver", "net.ucanaccess.jdbc.UcanaccessDriver");
	        		_dbparam.put("Database_Protocol", "jdbc:ucanaccess");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//<mdb or accdb file path>");
	        		disableResInput();
	        		break;
	        		
	        	case 7 :
	        		_dbparam.put("Database_Type", "MS_ACCESS");
	        		driver.setText("sun.jdbc.odbc.JdbcOdbcDriver");
	        		protocol.setText("jdbc:odbc");
	        		_dbparam.put("Database_Driver", "sun.jdbc.odbc.JdbcOdbcDriver");
	        		_dbparam.put("Database_Protocol", "jdbc:odbc");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		disableResInput();
	        		break;
	        	case 8 :
	        		_dbparam.put("Database_Type", "POSTGRES");
	        		driver.setText("org.postgresql.Driver");
	        		protocol.setText("jdbc:postgresql");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//hostname/db");
	        		disableResInput();
	        		break;
	        	case 9 :
	        		_dbparam.put("Database_Type", "DB2");
	        		driver.setText("com.ibm.db2.jcc.DB2Driver");
	        		protocol.setText("jdbc:db2");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//hostname/db");
	        		disableResInput();
	        		break;
	        		
	        	case 10 :
	        		_dbparam.put("Database_Type", "HIVE");
	        		driver.setText("org.apache.hadoop.hive.jdbc.HiveDriver");
	        		protocol.setText("jdbc:hive");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//hostname/db");
	        		disableResInput();
	        		break;
	        	case 11:
	        		_dbparam.put("Database_Type", "HIVE");
	        		driver.setText("org.apache.hive.jdbc.HiveDriver");
	        		protocol.setText("jdbc:hive2");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//hostname/db");
	        		disableResInput();
	        		break;
	        	case 12 :
	        		_dbparam.put("Database_Type", "INFORMIX");
	        		driver.setText("com.informix.jdbc.IfxDriver");
	        		protocol.setText("jdbc:informix-sqli");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//hostname/db");
	        		disableResInput();
	        		break;
	        		
	        	case 13 :
	        		_dbparam.put("Database_Type", "SPLICE");
	        		driver.setText("org.apache.derby.jdbc.ClientDriver");
	        		protocol.setText("jdbc:derby");
	        		driver.setEditable(false);
	        		protocol.setEditable(false);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("//hostname/db");
	        		disableResInput();
	        		break;

	        	case 14 :
	        		_dbparam.put("Database_Type", "Others");
	        		driver.setText("jdbc.DbNameDriver");
	        		protocol.setText("jdbc:dbname");
	        		driver.setEditable(true);
	        		protocol.setEditable(true);
	        		jdbc_cs.setEditable(true);
	        		dsn.setText("Enter DB Name");
	        		jdbc_cs.setText("Enter JDBC Connect String");
	        		info.setText("Enter JDBC connect string if using JDBC driver.\n" +
	        				"Make sure JDBC driver is in CLASSPATH");
	        		enableResInput();
	        		
	        		break;
	        	default:
	        		_dbparam.put("Database_Type", "Others");
	        		driver.setText("sun.jdbc.odbc.JdbcOdbcDriver");
	        		protocol.setText("jdbc:odbc");
	        		driver.setEditable(true);
	        		protocol.setEditable(true);
	        		jdbc_cs.setEditable(false);
	        		dsn.setText("Enter DSN ");
	        		info.setText("Enter DSN if using Window odbc Driver \n" +
	        				"Start --> Control Panel-->Administrative Tools-->Data Sources-->System DSN");
	        		enableResInput();
	        }
	        	
		}
		String command = e.getActionCommand();
		if ("cancel".equals(command)) {
			this.dispose();
			if ( connectionType == 0 ) // default connection
			System.exit(0);
			else 
			 _dbparam = null;
			return;
		}
		if ("continue".equals(command)) {
			this.dispose();
			return;
		}
		
        if( "addconn".equals(command) ) {
                    
            String dsn_s = dsn.getText();
            String user_s = user.getText();
            char[] passwd = passfield.getPassword();
            String driver_s = driver.getText();
            String protocol_s = protocol.getText();
            String catalog_s = catalog.getText();
            String schemaPattern_s = schemaPattern.getText();
            String tablePattern_s = tablePattern.getText();
            String colPattern_s = colPattern.getText();
            String type_s = type.getText();
            String jdbc_cs_s = jdbc_cs.getText();
            String db_name = coname.getText().trim();
            String db_type = jc.getSelectedItem().toString();
            
            if(db_name != null) _dbparam.put("Database_ConnectionName", db_name);
            if(dsn_s != null) _dbparam.put("Database_DSN", dsn_s);
            if(user_s != null)_dbparam.put("Database_User", user_s);
            if(passwd != null)_dbparam.put("Database_Passwd", new String(passwd) );
            if(driver_s != null)_dbparam.put("Database_Driver", driver_s);
            if(protocol_s != null)_dbparam.put("Database_Protocol", protocol_s);
            if(catalog_s != null)_dbparam.put("Database_Catalog", catalog_s);
            if(schemaPattern_s != null)_dbparam.put("Database_SchemaPattern", schemaPattern_s);
            if(tablePattern_s != null)_dbparam.put("Database_TablePattern", tablePattern_s);
            if(colPattern_s != null)_dbparam.put("Database_ColumnPattern", colPattern_s);
            if(type_s != null)_dbparam.put("Database_TableType", type_s);
            if(jdbc_cs_s != null)_dbparam.put("Database_JDBC", jdbc_cs_s);
            
            // Make sure Test connection is done before Add
            if(db_type != null)_dbparam.put("Database_Type", _dbparam.get("Database_Type"));
            
            new XmlWriter().writeConnection(_dbparam);
            
            cleanText();
            coname.setText("");
            
            infoStatus = "Database Connection Successfully Added";
            info.setText(infoStatus);
                
        }
                
		if ("testconn".equals(command)) {
			// Prompt here for null fields
			
			String status="";
			String dsn_s = dsn.getText();
			String user_s = user.getText();
			char[] passwd = passfield.getPassword();
			String driver_s = driver.getText();
			String protocol_s = protocol.getText();
			String catalog_s = catalog.getText();
			String schemaPattern_s = schemaPattern.getText();
			String tablePattern_s = tablePattern.getText();
			String colPattern_s = colPattern.getText();
			String type_s = type.getText();
			String jdbc_cs_s = jdbc_cs.getText();
			String restype_s = restype.getSelectedItem().toString();
			String resconcur_s = resconcur.getSelectedItem().toString();
			String quote_s = quoteC.isSelected()==true?"YES":"NO";
			
			// Validate resconcur_s resconcur_s
			if (restype.isEnabled() == true || resconcur.isEnabled() == true) {
				if (restype_s.compareToIgnoreCase("ResultSet Type") == 0 || 
						resconcur_s.compareToIgnoreCase("ResultSet Concurrency")==0 ) {
					JOptionPane.showMessageDialog(null, "Please choose Resultset Type and Concurrency");
					return;
				}
				resconcur_s = resconcur_s.compareToIgnoreCase("CONCUR_READ_ONLY") ==0 ?"1007" : "1008";
				if (restype_s.compareToIgnoreCase("TYPE_FORWARD_ONLY") == 0)
					restype_s="1003";
				if (restype_s.compareToIgnoreCase("TYPE_SCROLL_INSENSITIVE") == 0)
					restype_s="1004";
				if (restype_s.compareToIgnoreCase("TYPE_SCROLL_SENSITIVE") == 0)
					restype_s="1005";
					
			}
			
			if(dsn_s != null) _dbparam.put("Database_DSN", dsn_s);
			if(user_s != null)_dbparam.put("Database_User", user_s);
			if(passwd != null)_dbparam.put("Database_Passwd", new String(passwd) );
			if(driver_s != null)_dbparam.put("Database_Driver", driver_s);
			if(protocol_s != null)_dbparam.put("Database_Protocol", protocol_s);
			if(catalog_s != null)_dbparam.put("Database_Catalog", catalog_s);
			if(schemaPattern_s != null)_dbparam.put("Database_SchemaPattern", schemaPattern_s);
			if(tablePattern_s != null)_dbparam.put("Database_TablePattern", tablePattern_s);
			if(colPattern_s != null)_dbparam.put("Database_ColumnPattern", colPattern_s);
			if(type_s != null)_dbparam.put("Database_TableType", type_s);
			if(jdbc_cs_s != null)_dbparam.put("Database_JDBC", jdbc_cs_s);
			if(restype_s != null)_dbparam.put("Database_ResultsetType", restype_s);
			if(resconcur_s != null)_dbparam.put("Database_ResultsetConcur", resconcur_s);
			if(quote_s != null)_dbparam.put("Database_SupportQuote", quote_s);
			
			try {
				
				if (connectionType == 0 ) { // Default connection
				Rdbms_conn.init(_dbparam);
				status = Rdbms_conn.testConn();
				info.setText(status);
				Rdbms_conn.closeConn();
				} else { // New Connection
					Rdbms_NewConn newConn = new Rdbms_NewConn(_dbparam);
					status = newConn.testConn();
					info.setText(status);
					newConn.closeConn();	
				}
			} catch (Exception e1) {
				System.out.println("Connection Failed");
				e1.printStackTrace();
			}
			if("Connection Successful".equals(status)) {
				ok_b.setEnabled(true);
                add_b.setEnabled(true);
			}
			
			return;
		}

	}
	
	public Hashtable<String,String> getDBParam() {
		return _dbparam;
		
	}

	public class ConnFocusEvent implements FocusListener {
	String sel_id="";
	ConnFocusEvent(String selected_f) {
		sel_id=selected_f;
	}
		
	@Override
	public void focusGained(FocusEvent e) {
                if( "cname".equals(sel_id) ) {
                    infoStatus = "Enter Database Connection Name";
                    info.setText(infoStatus);
                }
		if ("dsn".equals(sel_id)) {
			switch (dbIndex) {
			case 0:
				infoStatus = "Enter Oracle SID value. \nFormat should be //hostname[:port]/SID \nCheck tnsnames.ora file for Details.";
				break;
			case 4:
				infoStatus = "Enter Hostname and DataBase Name. \nFormat should be //hostname[[\\instanceName][:port]][;databaseName=value][;property=value]" ;
				break;
			case 6:
				infoStatus = "Enter Hostname and DataBase Name. \nFormat should be //c:\\Database.[mdb][accdb]" ;
				break;
			case 2:case 8:case 9:case 10:case 11:case 13:
				infoStatus = "Enter Hostname and DataBase Name. \nFormat should be //hostname[:port]/dbname.";
				break;
			case 12:
				infoStatus = "Enter Hostname and DataBase Name. \nFormat should be //hostname[:port]/dbname::INFORMIXSERVER=name[;property=value]" ;
				break;
			case 1: case 3: case 5: case 7:
				infoStatus = "Enter System DSN of your window. \n" +
						"Start --> Control Panel-->Administrative Tools-->Data Sources-->System DSN.";
				break;
			default:
			}
			info.setText(infoStatus);
			
		}
		if ("user".equals(sel_id)) {
			infoStatus = "Enter Database User Name.";
			info.setText(infoStatus);
		}
		if ("passfield".equals(sel_id)) {
			infoStatus = "Enter Database Password.";
			info.setText(infoStatus);
		}
		if ("driver".equals(sel_id)) {
			switch (dbIndex) {
			case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:case 8:case 9: case 10: case 11:case 12:case 13:
				infoStatus = "Default Database Driver is shown.";
				 break;
			default:
				infoStatus = "Please enter the Driver Name. \nMake sure the Driver is in CLASSPATH";
			}	
			info.setText(infoStatus);
		}
		if ("protocol".equals(sel_id)) {
			switch (dbIndex) {
			case 0: case 1: case 2: case 3: case 4: case 5:case 6: case 7:case 8:case 9: case 10: case 11:case 12:case 13:
				infoStatus = "Default Connection Protocol is shown.";
				 break;
			default:
				infoStatus = "Please enter the Protocol.";
			}	
			info.setText(infoStatus);
		}
		if ("catalog".equals(sel_id)) {
			infoStatus = "Enter Catalog to connect. It is NOT a must field.\n" +
					"SQL will be build like Catalog.Table \n" +
					"Most Databases do not need that";
			info.setText(infoStatus);
			
		}
		if ("schemaPattern".equals(sel_id)) {
			infoStatus = "Enter Schema Name. It is NOT a must field." +
					"\nOracle default schema name is USER Name in capital letters.";
			info.setText(infoStatus);
		}
		if ("tablePattern".equals(sel_id)) {
			infoStatus = "Enter Table Pattern to match. It is NOT a must field. \n" +
					"Tb_% will show all the tables starting with Tb_";
			info.setText(infoStatus);
		}
		if ("colPattern".equals(sel_id)) {
			infoStatus = "Enter Column Pattern to match. It is NOT a must field. \n" +
					"Cl_% will show all the columns starting with Cl_";
			info.setText(infoStatus);
		}
		if ("type".equals(sel_id)) {
			infoStatus = "Enter Table type. \"TABLE\" will match user tables. \n" +
					"Other options are : VIEW, SYSTEM TABLE,GLOBAL TEMPORARY,\nLOCAL TEMPORARY, ALIAS, SYNONYM";
			info.setText(infoStatus);
		}
		if ("jdbc_cs".equals(sel_id)) {
			infoStatus = "Enter JDBC Connect String. \n" +
					"Make sure Driver Manager is in CLASSPATH";
			info.setText(infoStatus);
		}
		
	} 

	@Override
	public void focusLost(FocusEvent e) {
		if ("user".equals(sel_id)) {
			// Oracle Schema Name is default USER name in capital
			if (dbIndex ==0 || dbIndex ==1 ) {
			String userName = user.getText();
			if (userName != null || "".equals(userName))
				schemaPattern.setText(userName.toUpperCase());
		} }
		
	}

	}
	
	// It is a utility function which clean all JTextfield except type and default values
	private void cleanText() {
        jccon.setSelectedIndex(0);
		dsn.setText("");
		user.setText("");
		passfield.setText("");
		catalog.setText("");
		schemaPattern.setText("");
		tablePattern.setText("");
		colPattern.setText("");
		jdbc_cs.setText("");
		
	}

	public int getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(int connectionType) {
		this.connectionType = connectionType;
	}

	// This UI function will have input to take take parameters for 
	// double quote, resultSetType, resultSetConcurrency
	
	private JPanel createResultsetInputPanel() {
		JPanel rpanel = new JPanel();
		quoteC = new JCheckBox("Double Quote");
		restype = new JComboBox<String>(new String[] {"ResultSet Type","TYPE_FORWARD_ONLY", "TYPE_SCROLL_INSENSITIVE", "TYPE_SCROLL_SENSITIVE"});
		resconcur = new JComboBox<String>(new String[] {"ResultSet Concurrency","CONCUR_READ_ONLY","CONCUR_UPDATABLE"});
		rpanel.add(quoteC);rpanel.add(restype);rpanel.add(resconcur);
		disableResInput();
		
		return rpanel;
	}
	private void enableResInput() {
		quoteC.setEnabled(true);restype.setEnabled(true);resconcur.setEnabled(true);
		
	}
	private void disableResInput() {
		quoteC.setEnabled(false);restype.setEnabled(false);resconcur.setEnabled(false);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && e.getSource() == demoDB) {
		SwingUtilities.invokeLater(new Runnable() {
	        public void run()
	         {
				int selop = JOptionPane.showConfirmDialog(null,"Do you want to load Demo database ?","Choose Demo DB", JOptionPane.YES_NO_OPTION);
				if (selop == JOptionPane.NO_OPTION) {
					demoDB.setSelected(false);
					return;
				} else {
					fillDemoVal();
				}
	         }
	    });
			
		}
		if (e.getStateChange() == ItemEvent.SELECTED && e.getSource() == fileLoad) {
		SwingUtilities.invokeLater(new Runnable() {
	        public void run()
	         {
				int selop = JOptionPane.showConfirmDialog(null,"Do you want to load File ?","Choose FILE", JOptionPane.YES_NO_OPTION);
				if (selop == JOptionPane.NO_OPTION) {
					fileLoad.setSelected(false);
					return;
				} else {
					dispose();
					setFileLoad(true);
				}
	         }
	    });
			
		}
	}
		
	final private void fillDemoVal() {
			 // Now load the database	    
			_dbparam = new Hashtable <String,String>();
			_dbparam.put("Database_Type", "MS_ACCESS_JDBC");
			_dbparam.put("Database_Driver", "net.ucanaccess.jdbc.UcanaccessDriver");
			_dbparam.put("Database_Protocol", "jdbc:ucanaccess");
			_dbparam.put("Database_DSN", "//configuration/ArrahDemo.accdb");
			_dbparam.put("Database_User", "");
			_dbparam.put("Database_Passwd", "");
			_dbparam.put("Database_Catalog", "");
			_dbparam.put("Database_SchemaPattern", "");
			_dbparam.put("Database_TablePattern", "");
			_dbparam.put("Database_ColumnPattern", "");
			_dbparam.put("Database_TableType", "TABLE"); // Default show tables
			_dbparam.put("Database_ResultsetType", "");
			_dbparam.put("Database_ResultsetConcur", "");
			_dbparam.put("Database_SupportQuote", "");
			
			String status ="";
			try {
			   
	      //loads ArrahDemo.accdb as inputstream from classpath
			  //and copies the same as a file under configuration.
			  //this removes filesystem dependency
			  Path path = FileSystems.getDefault().getPath("configuration", "ArrahDemo.accdb");
			  File demoDbFile = path.toFile();
			  demoDbFile.getParentFile().mkdirs();//creates configuration, if not found
			  if (!demoDbFile.exists()) {
			    Files
			    .copy(
			        TestConnectionDialog.class.getClassLoader()
			        .getResourceAsStream("resource/ArrahDemo.accdb"),
			        path);
			  }
				if (connectionType == 0 ) { // Default connection
					Rdbms_conn.init(_dbparam);
					status = Rdbms_conn.testConn();
					info.setText(status);
					Rdbms_conn.closeConn();
				} else { // New Connection
					Rdbms_NewConn newConn = new Rdbms_NewConn(_dbparam);
					status = newConn.testConn();
					info.setText(status);
					newConn.closeConn();	
				}
			} catch (Exception e1) {
				System.out.println("Connection Failed");
				e1.printStackTrace();
			}
			if("Connection Successful".equals(status)) {
				this.dispose();
				return;
			}		
		}

	public boolean isFileLoad() {
		return isFileLoad;
	}

	public void setFileLoad(boolean isFileLoad) {
		this.isFileLoad = isFileLoad;
	}
}
