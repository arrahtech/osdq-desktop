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

/* This file is used for getting info about 
 * comparing schemas across DB
 * 
 *
 */

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;


import org.arrah.framework.analytics.RTMDiffUtil;
import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.profile.DBMetaInfo;
import org.arrah.framework.profile.NewConnTableMetaInfo;
import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.Rdbms_NewConn;
import org.arrah.framework.rdbms.Rdbms_conn;

public class CompareSchemaDialog implements TreeSelectionListener, ActionListener {

	private JPanel topTablePane = new JPanel(),botTablePane = new JPanel();
	private DefaultMutableTreeNode priTop = new DefaultMutableTreeNode("Primary DB");
	private DefaultMutableTreeNode secBot = new DefaultMutableTreeNode("Secondary DB");
	private Rdbms_NewConn newConn = null;
	
	public CompareSchemaDialog() { // Default
		
	}
	
	public void createGUI () {
		
		if ( inputTable() == false) return;
		
		//Provide preferred  sizes for the two components in the split pane
		Dimension minSize = new Dimension(300,300);
		topTablePane.setMinimumSize(minSize);
		botTablePane.setMinimumSize(minSize);

		JTree toptree = new JTree(priTop);
		toptree.getSelectionModel().setSelectionMode(1);
		
		JTree bottree = new JTree(secBot);
		toptree.getSelectionModel().setSelectionMode(1);
		toptree.addTreeSelectionListener(this);
		bottree.addTreeSelectionListener(this);
		
		JScrollPane jscrollpane1 = new JScrollPane(toptree);
		jscrollpane1.setPreferredSize(new Dimension(150,300));
		
		JScrollPane jscrollpane2 = new JScrollPane(bottree);
		jscrollpane2.setPreferredSize(new Dimension(150,300));
		
		JPanel leftPanel = new JPanel();
		BoxLayout boxl = new BoxLayout(leftPanel,BoxLayout.Y_AXIS);
		leftPanel.setLayout(boxl);
		leftPanel.add(jscrollpane1); leftPanel.add(jscrollpane2);
		
		JScrollPane jscrollpane3 = new JScrollPane(topTablePane);
		jscrollpane3.setPreferredSize(new Dimension(600,300));
		
		JScrollPane jscrollpane4 = new JScrollPane(botTablePane);
		jscrollpane4.setPreferredSize(new Dimension(600,300));
		
		//Create a split pane with the two scroll panes in it.
		JSplitPane splitPaneR = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
						jscrollpane3, jscrollpane4);
		splitPaneR.setOneTouchExpandable(true);
		splitPaneR.setDividerLocation(300);
		
		// Final Window
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, splitPaneR);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(200);
		
		
		
		// Add Menu bar
		JMenuBar menubar = new JMenuBar();
		JMenu diff = new JMenu("Diff Summary");
		menubar.add(diff);
		
		JMenuItem param = new JMenuItem("Parameter");
		param.setActionCommand("parameter");
		param.addActionListener(this);
		diff.add(param);
		
		JMenuItem procedure = new JMenuItem("Procedure");
		procedure.setActionCommand("procedure");
		procedure.addActionListener(this);
		diff.add(procedure);
		
		JMenuItem tabName = new JMenuItem("Table Name");
		tabName.setActionCommand("tablename");
		tabName.addActionListener(this);
		diff.add(tabName);
		
		JMenuItem tabMeta = new JMenuItem("Table MetaData");
		tabMeta.setActionCommand("tablemeta");
		tabMeta.addActionListener(this);
		diff.add(tabMeta);
		
		JMenuItem tabSumm = new JMenuItem("Table Summary Data");
		tabSumm.setActionCommand("tablesummary");
		tabSumm.addActionListener(this);
		diff.add(tabSumm);
		
		JMenuItem tabInd = new JMenuItem("Table Index");
		tabInd.setActionCommand("tableindex");
		tabInd.addActionListener(this);
		diff.add(tabInd);
		
		JMenuItem tabKey = new JMenuItem("Table Key");
		tabKey.setActionCommand("tablekey");
		tabKey.addActionListener(this);
		diff.add(tabKey);
		
		JDialog jd = new JDialog();
		jd.setTitle("Schema Comparison Dialog");
		jd.setLocation(75, 75);
		jd.setJMenuBar(menubar); // Add Menu bar here
		jd.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				try {
					newConn.exitConn();
				} catch (SQLException e) {
					// Do nothing
				}
				}
			});
		jd.getContentPane().add(splitPane);
		jd.pack();
		jd.setVisible(true);

	}
	
	private boolean inputTable() {
		
	TestConnectionDialog tcd = new TestConnectionDialog(1); // new Connection
	JOptionPane.showMessageDialog(null, "Choose another Data Source to compare Schema",
			"Table Comparison Dialog",
			JOptionPane.INFORMATION_MESSAGE);
	tcd.createGUI();

	Hashtable <String,String> _fileParse = tcd.getDBParam();
	if (_fileParse == null ) { 
			JOptionPane.showMessageDialog(null, "Parameters not selected for new Connection",
					"Schema Comparison Dialog",JOptionPane.ERROR_MESSAGE);
			return false; 
	} // do not show dialog

	
	try {
		newConn = new Rdbms_NewConn(_fileParse);
		if ( newConn.openConn() == false) {
			JOptionPane.showMessageDialog(null, "Can Not Create new Connection",
					"Schema Comparison Dialog",JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		createNode(priTop,false);
		createNode(secBot,true);
		return true;
		
	} catch (Exception e1) {
		System.out.println(e1.getMessage());
		e1.printStackTrace();
		return false;
	} finally {
		
	}
	
	}
	
	private void getPanel(TreePath absPath) throws Exception {
		
		if (absPath == null) return ;
		int pathC = absPath.getPathCount();
		boolean isnewConn = false;
		int funcNo = 0;
		
		if ("Primary DB".equalsIgnoreCase(absPath.getPathComponent(0).toString()) == false)
			isnewConn = true;
				
		if (pathC == 2)  { // it may be parameter or Procedure
			String selStr = absPath.getPathComponent(1).toString();
			if ("Parameters".equalsIgnoreCase(selStr) == true ) funcNo = 1;
			if ("Procedures".equalsIgnoreCase(selStr) == true ) funcNo = 2;
			if ( funcNo == 0) return ;
		}
		if (pathC == 4)  { // it may be meta data profile data, index or key Info
			String selStr = absPath.getPathComponent(3).toString();
			if ("MetaData".equalsIgnoreCase(selStr) == true ) funcNo = 4;
			if ("Profile Data".equalsIgnoreCase(selStr) == true ) funcNo = 5;
			if ("Index".equalsIgnoreCase(selStr) == true ) funcNo = 3;
			if ("Key Info".equalsIgnoreCase(selStr) == true ) funcNo = 6;
			if ( funcNo == 0) return ;
		}
		if (isnewConn == true) {
			ReportTableModel rtm = null, rtm1 = null , rtm2 = null, rtm3 = null,  rtm4 = null, rtm5 = null;
			ReportTable rt = null;
			int i=0;
			
			Vector<String> table_v = newConn.getTable();
			if (funcNo >= 3 ) 
				i = table_v.indexOf(absPath.getPathComponent(2).toString());
			switch(funcNo) {
				case 1:
					DBMetaInfo dbmeta = new DBMetaInfo(newConn);
					rtm4 = dbmeta.getParameterInfo();
					rt = new ReportTable(rtm4);
					createPanel(rt, false);
					break;
				case 2 :
					DBMetaInfo dbmeta1 = new DBMetaInfo(newConn);
					rtm5 = dbmeta1.getProcedureInfo();
					rt = new ReportTable(rtm5);
					createPanel(rt, false);
					break;
				case 3 : // index
					NewConnTableMetaInfo newTableInfo = new NewConnTableMetaInfo(newConn);
					rtm = newTableInfo.populateTable(1, i, i+1, rtm);
					rt = new ReportTable(rtm);
					createPanel(rt, false);
					break;
				case 4 : // Metadata
					NewConnTableMetaInfo newTableInfo1 = new NewConnTableMetaInfo(newConn);
					rtm1 = newTableInfo1.populateTable(2, i, i+1, rtm1);
					rt = new ReportTable(rtm1);
					createPanel(rt, false);
					break;
				case 5 : // Profiler data
					NewConnTableMetaInfo newTableInfo2 = new NewConnTableMetaInfo(newConn);
					rtm2 = newTableInfo2.populateTable(4, i, i+1, rtm2);
					rt = new ReportTable(rtm2);
					createPanel(rt, false);
					break;
				case 6 : // Key data
					NewConnTableMetaInfo newTableInfo3 = new NewConnTableMetaInfo(newConn);
					rtm3 = newTableInfo3.tableKeyInfo(table_v.get(i));
					rt = new ReportTable(rtm3);
					createPanel(rt, false);
					break;
				default :
				
		} } else {
			ReportTableModel rtm = null, rtm1 = null , rtm2 = null, rtm3 = null,  rtm4 = null, rtm5 = null;
			ReportTable rt = null;
			int i=0;
			
			Vector<String> table_v = Rdbms_conn.getTable();
			if (funcNo >= 3 ) 
				i = table_v.indexOf(absPath.getPathComponent(2).toString());
			switch(funcNo) {
				case 1:
					DBMetaInfo dbmeta = new DBMetaInfo();
					rtm4 = dbmeta.getParameterInfo();
					rt = new ReportTable(rtm4);
					createPanel(rt, true);
					break;
				case 2 :
					DBMetaInfo dbmeta1 = new DBMetaInfo();
					rtm5 = dbmeta1.getProcedureInfo();
					rt = new ReportTable(rtm5);
					createPanel(rt, true);
					break;
				case 3 :
					rtm = TableMetaInfo.populateTable(1, i, i+1, rtm);
					rt = new ReportTable(rtm);
					createPanel(rt, true);
					break;
				case 4 :
					rtm1 = TableMetaInfo.populateTable(2, i, i+1, rtm1);
					rt = new ReportTable(rtm1);
					createPanel(rt, true);
					break;
				case 5 :
					rtm2 = TableMetaInfo.populateTable(4, i, i+1, rtm2);
					rt = new ReportTable(rtm2);
					createPanel(rt, true);
					break;
				case 6 :
					rtm3 = TableMetaInfo.tableKeyInfo(table_v.get(i));
					rt = new ReportTable(rtm3);
					createPanel(rt, true);
					break;
				default :
			
		} }
	}
	
	private void createPanel(JComponent jp, boolean top) {
		if (top== true) {
			topTablePane.removeAll();
			topTablePane.add(jp);
			topTablePane.revalidate();
			topTablePane.repaint();
		} else {
			botTablePane.removeAll();
			botTablePane.add(jp);
			botTablePane.revalidate();
			botTablePane.repaint();
		}
		
	}

	private void createNode (DefaultMutableTreeNode node, boolean newConnection) throws SQLException {
		Vector<String> table_v = new Vector<String>();
		if (newConnection == false ) {
			table_v = Rdbms_conn.getTable();
			
		} else { // new connection
			newConn.populateTable();
			table_v = newConn.getTable();
		}
		int tabC = table_v.size();
		
		DefaultMutableTreeNode tablen = new DefaultMutableTreeNode("Tables("+tabC+")");
		node.add(tablen);
		DefaultMutableTreeNode proce = new DefaultMutableTreeNode("Procedures");
		node.add(proce);
		DefaultMutableTreeNode param = new DefaultMutableTreeNode("Parameters");
		node.add(param);
		
		for (int i=0; i < tabC; i++) {
			DefaultMutableTreeNode tabName =  new DefaultMutableTreeNode(table_v.get(i));
			tablen.add(tabName);
			
			DefaultMutableTreeNode tabMData =  new DefaultMutableTreeNode("MetaData");
			tabName.add(tabMData);
			DefaultMutableTreeNode tabData =  new DefaultMutableTreeNode("Profile Data");
			tabName.add(tabData);
			DefaultMutableTreeNode tabindex =  new DefaultMutableTreeNode("Index");
			tabName.add(tabindex);
			DefaultMutableTreeNode tabPK =  new DefaultMutableTreeNode("Key Info");
			tabName.add(tabPK);
		}
	}
	
	public void valueChanged(TreeSelectionEvent treeselectionevent) {
		JTree tree = (JTree)treeselectionevent.getSource();
		if ( tree == null) return;
		try {
			tree.getTopLevelAncestor().setCursor(
					java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

			TreePath treepath = tree.getSelectionPath();
			int pathC = treepath.getPathCount();
			
			if (pathC == 2)  { // it may be parameter or Procedure
				String selStr = treepath.getPathComponent(1).toString();
				if ("Parameters".equalsIgnoreCase(selStr) == true ||
						"Procedures".equalsIgnoreCase(selStr) == true ) {
					getPanel(treepath);
				}
			}
			if (pathC == 4)  { 
				getPanel(treepath);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tree.getTopLevelAncestor()
					.setCursor(
							java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}

	}
	
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
		RTMDiffUtil rtmdiff = null;
		
		if (command.equals("parameter") == true ) {
			try {
				DBMetaInfo dbmetaleft = new DBMetaInfo();
				ReportTableModel rtmL = dbmetaleft.getParameterInfo();
				
				DBMetaInfo dbmetaright = new DBMetaInfo(newConn);
				ReportTableModel rtmR = dbmetaright.getParameterInfo();
				
				rtmdiff = new RTMDiffUtil(rtmL, rtmR);
				boolean com = rtmdiff.compare(true,false);
				if ( com == false) {
					ConsoleFrame.addText("\n Parameter Comparison Failed");
					return;
				}
				
			} catch( Exception eaction) {
				ConsoleFrame.addText("\n Parameter Exception:" +eaction.getLocalizedMessage());
			}
		} // Parameter
		
		else if (command.equals("procedure") == true ) {
			try {
				DBMetaInfo dbmetaL= new DBMetaInfo();
				ReportTableModel rtmL = dbmetaL.getProcedureInfo();
				
				DBMetaInfo dbmetaR = new DBMetaInfo(newConn);
				ReportTableModel rtmR = dbmetaR.getProcedureInfo();
				
				rtmdiff = new RTMDiffUtil(rtmL, rtmR);
				boolean com = rtmdiff.compare(true,false);
				if ( com == false) {
					ConsoleFrame.addText("\n Procedure Comparison Failed");
					return;
				}
				
			} catch( Exception eaction) {
				ConsoleFrame.addText("\n Procedure Exception:" +eaction.getLocalizedMessage());
			}
		} // Procedure
		
		else if (command.equals("tablename") == true ) {
				ReportTableModel rtmL = new ReportTableModel(new String[] {"Table Name"});
				ReportTableModel rtmR = new ReportTableModel(new String[] {"Table Name"});
				
				Vector<String> priTable = Rdbms_conn.getTable();
				Vector<String> secTable = newConn.getTable();
				for (String tab: priTable )
					rtmL.addFillRow(new String[] {tab});
				for (String tab: secTable )
					rtmR.addFillRow(new String[] {tab});
				
				rtmdiff = new RTMDiffUtil(rtmL, rtmR);
				boolean com = rtmdiff.compare(true,false);
				if ( com == false) {
					ConsoleFrame.addText("\n Table Name Comparison Failed");
					return;
				}
		} // Table Name
		else {
		
		// Following options will take table name as input
		String tabName = JOptionPane.showInputDialog("Please enter Table Name");
		if (tabName == null || tabName.compareTo("") == 0) 
			return;
		
		Vector<String> priTable = Rdbms_conn.getTable();
		Vector<String> secTable = newConn.getTable();
		int lefti = priTable.indexOf(tabName);
		int righti = secTable.indexOf(tabName);
		
		if (lefti < 0 || righti < 0 ) {
			 JOptionPane.showMessageDialog(null,"Table:"+ tabName + " does not exist in both schemas");
			 return;
		}
		NewConnTableMetaInfo newTableInfo = new NewConnTableMetaInfo(newConn); // new Connection
		int tableType = -1; ReportTableModel rtmL = null, rtmR = null ;
		
		if (command.equals("tableindex") == true ) 
			tableType = 1;
		if (command.equals("tablemeta") == true )
			tableType = 2;
		if (command.equals("tablesummary") == true )
			tableType = 4;
		if (tableType == 1 ||  tableType == 2 || tableType == 4) {
			try {
				
				rtmL = TableMetaInfo.populateTable(tableType, lefti, lefti+1, rtmL);
				rtmR = newTableInfo.populateTable(tableType, righti, righti+1, rtmR);
				rtmdiff = new RTMDiffUtil(rtmL, rtmR);
				boolean com = rtmdiff.compare(true,false);
				if ( com == false) {
					ConsoleFrame.addText("\n Table Info Comparison Failed");
					return;
				}
			} catch( Exception eaction) {
				ConsoleFrame.addText("\n Table Info Exception:" +eaction.getLocalizedMessage());
			}
		}
		
		if (command.equals("tablekey") == true ) {
			try {
				rtmL = TableMetaInfo.tableKeyInfo(tabName);
				rtmR = newTableInfo.tableKeyInfo(tabName);
				rtmdiff = new RTMDiffUtil(rtmL, rtmR);
				boolean com = rtmdiff.compare(true,false);
				if ( com == false) {
					ConsoleFrame.addText("\n Index Key Failed");
					return;
				}
			} catch( Exception eaction) {
				ConsoleFrame.addText("\n Table Key Exception:" +eaction.getLocalizedMessage());	
			}
		}
		} // Table Info
		
		tabPane.add("Matched Record",new ReportTable(rtmdiff.getMatchedRTM()));
		tabPane.add("Primary No Match",new ReportTable(rtmdiff.leftNoMatchRTM()));
		tabPane.add("Secondary No Match",new ReportTable(rtmdiff.rightNoMatchRTM()));
		
		JDialog db_d = new JDialog();
		db_d.setTitle("Difference Summary Dialog");
		db_d.getContentPane().add(tabPane);
		db_d.setModal(true);
		db_d.setLocation(200, 100);
		db_d.pack();
		db_d.setVisible(true);
		}
				

} // end of class
