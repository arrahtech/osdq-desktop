package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This file is the main class which show 
 * the main frame menus.
 *
 *
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.util.KeyValueParser;

public class Profiler extends JPanel implements TreeSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTree tree;
	private RightView rightView;
	private static Hashtable<String, String> _fileParse;
	private Hashtable<String, String> _tooltip;
	private Hashtable<String, String> select_info;
	private FirPanel _firPanel;
	private JTabbedPane tp;
	private static JLabel statusBar = new JLabel();
	private static boolean playWithLineStyle = false;
	private static String lineStyle = "Horizontal";

	private class CellRenderer extends DefaultTreeCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTreeCellRendererComponent(JTree jtree, Object obj,
				boolean flag, boolean flag1, boolean flag2, int i, boolean flag3) {
			super.getTreeCellRendererComponent(jtree, obj, flag, flag1, flag2,
					i, flag3);
			if (flag2) {
				if (((DefaultMutableTreeNode) obj).getParent() == null) {
					JOptionPane.showMessageDialog(null,
							"Data Source has No Table or Column",
							"Data Source Error", 0);
					return this;
				}
				String s = ((DefaultMutableTreeNode) obj).getParent()
						.toString() + "." + obj.toString();
				setToolTipText((String) _tooltip.get(s));
			} else {
				setToolTipText((String) _tooltip.get(obj.toString()));
			}
			return this;
		}

		public CellRenderer() {
		}
	}

	private class myChangeListener implements ChangeListener {

		public void stateChanged(ChangeEvent changeevent) {
			if (select_info == null)
				return;
			if (tp.getSelectedIndex() != 0)
				return;
			int i = select_info.size();
			if (select_info.get("Column") != null)
				_firPanel.setColText(select_info.get("Column"));
			else
				_firPanel.setColText("");
			if (select_info.get("Table") != null)
				_firPanel.setTableText( select_info.get("Table"));
			else
				_firPanel.setTableText("");
			if (select_info.get("Schema") != null)
				_firPanel.setDSNText(select_info.get("Schema"));
			else
				_firPanel.setDSNText("");
			
			if (i >= 3)
				_firPanel.setDummyText( select_info.get("Column"));
			else if (i == 2)
				_firPanel.setDummyText( select_info.get("Table"));
			else if (i == 1)
				_firPanel.setDummyText( select_info.get("Schema"));
		}

		private myChangeListener() {
		}

	}

	public Profiler() {
		super(new GridLayout(1, 0));
		select_info = null;
		// Take from connection dialog
		// _fileParse = KeyValueParser.parseFile("./configFile.txt");
		_tooltip = new Hashtable<String, String>();
		DefaultMutableTreeNode defaultmutabletreenode = new DefaultMutableTreeNode(
				_fileParse.get("Database_DSN"));
		try {
			createNodes(defaultmutabletreenode);
		} catch (SQLException sqlexception) {
			ConsoleFrame.addText("\n ERROR:CreateNode SQL exception ");
			JOptionPane.showMessageDialog(null, sqlexception.getMessage(),
					"Error Message", 0);
		}
		tree = new JTree(defaultmutabletreenode);
		tree.getSelectionModel().setSelectionMode(1);
		tree.setDragEnabled(true);
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(new PopupListener());
		if (playWithLineStyle) {
			System.out.println("line style = " + lineStyle);
			tree.putClientProperty("JTree.lineStyle", lineStyle);
		}
		ToolTipManager.sharedInstance().registerComponent(tree);
		if (defaultmutabletreenode.getChildCount() > 0)
			tree.setCellRenderer(new CellRenderer());
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new BorderLayout());
		JScrollPane jscrollpane = new JScrollPane(tree);
		jpanel.add(jscrollpane, "Center");
		jpanel.add(statusBar, "Last");
		rightView = new RightView();
		_firPanel = new FirPanel();
		JScrollPane jscrollpane1 = new JScrollPane(_firPanel);
		tp = new JTabbedPane();
		tp.addChangeListener(new myChangeListener());
		tp.addTab("Information", null, jscrollpane1, "Information Panel");
		JSplitPane jsplitpane = new JSplitPane(1);
		jsplitpane.setTopComponent(jpanel);
		jsplitpane.setBottomComponent(tp);
		jsplitpane.setDividerLocation(225);
		jsplitpane.setPreferredSize(new Dimension(900, 670));
		add(jsplitpane);
	}

	public void valueChanged(TreeSelectionEvent treeselectionevent) {
		try {

			tree.getTopLevelAncestor().setCursor(
					java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode) tree
					.getLastSelectedPathComponent();
			if (defaultmutabletreenode == null)
				return;
			_firPanel.setDSNText("");
			_firPanel.setTableText("");
			_firPanel.setColText("");
			TreePath treepath = tree.getSelectionPath();
			int i = treepath.getPathCount();
			select_info = new Hashtable<String, String>();
			String s = "";
			switch (i) {
			case 3: // '\003'
				int j = treepath.getPathComponent(2).toString()
						.lastIndexOf(':');
				s = treepath.getPathComponent(2).toString().substring(0, j);
				select_info.put("Column", s);
				select_info.put("Type", treepath.getPathComponent(2).toString()
						.substring(j + 1));
				_firPanel.setColText( select_info.get("Column"));
				// fall through

			case 2: // '\002'
				select_info.put("Table", treepath.getPathComponent(1).toString() );
				_firPanel.setTableText( select_info.get("Table"));
				// fall through

			case 1: // '\001'
				select_info.put("Schema", treepath.getPathComponent(0).toString());
				_firPanel.setDSNText( select_info.get("Schema"));
				
				// append schema+table+column to make is unique
				if (i == 1)
					_firPanel.setDummyText( select_info.get("Schema"));
				else if (i == 2)
					_firPanel.setDummyText( select_info.get("Table") +
							select_info.get("Schema"));
				else if (i == 3)
					_firPanel.setDummyText( select_info.get("Column") + 
							select_info.get("Table") + 
							select_info.get("Schema"));
				break;
			}
			if (i == 3) {
				if (tp.getTabCount() == 1) {
					JScrollPane jscrollpane = rightView.getRScrollPane();
					tp.addTab("Analysis", null, jscrollpane, "Analysis Panel");
				}
				rightView.setLabel(select_info);
				QueryBuilder.unsetCond();

			} else if (tp.getTabCount() == 2)
				tp.removeTabAt(1);
		} finally {
			tree.getTopLevelAncestor()
					.setCursor(
							java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}

	}

	private void createNodes(DefaultMutableTreeNode defaultmutabletreenode) throws SQLException {
		// Rdbms_conn.init(_fileParse); Test Connection has set it.
		String s = (String) _fileParse.get("Database_SchemaPattern");
		String s1 = (String) _fileParse.get("Database_TablePattern");
		String s2 = (String) _fileParse.get("Database_TableType");
		String s3 = (String) _fileParse.get("Database_Catalog");
		s3 = "";

		Rdbms_conn.populateTable(s3.compareTo("") != 0 ? s3 : (s3 = null), s
				.compareTo("") != 0 ? s : (s = null),
				s1.compareTo("") != 0 ? s1 : (s1 = null), s2.split(","));
		Vector<String> table_v = Rdbms_conn.getTable();
		Vector<String> tableDesc_v = Rdbms_conn.getTableDesc();
		int tableCount = table_v.size();

		for (int i = 0; i < tableCount;) {
			String s5 = table_v.get(i);
			String s7 = s5 + " <BR>Desc:" + tableDesc_v.get(i);
			ConsoleFrame.addText(".");
			DefaultMutableTreeNode defaultmutabletreenode1 = new DefaultMutableTreeNode(
					s5);
			defaultmutabletreenode.add(defaultmutabletreenode1);
			_tooltip.put(s5, "<html>" + (++i) + "." + s7 + "</html>");
		}

		statusBar.setText(" Table: " + tableCount + "      User: "
				+ _fileParse.get("Database_User"));
		ConsoleFrame.addText("\n Loading Tables ");

		Rdbms_conn.openConn();
		DatabaseMetaData databasemetadata = Rdbms_conn.getMetaData();

		ResultSet resultset1 = null;
		
	for (Enumeration<?> enumeration = defaultmutabletreenode.children(); enumeration
				.hasMoreElements(); ) { // moving out resultset1.close
		try {
			DefaultMutableTreeNode defaultmutabletreenode3 = (DefaultMutableTreeNode) enumeration
					.nextElement();
			String s6 = defaultmutabletreenode3.toString();
			ConsoleFrame.addText("\n Getting information for Table:" + s6);
			int j = 0;
			DefaultMutableTreeNode defaultmutabletreenode2 = null;
			
			try {
				resultset1 = databasemetadata.getColumns(s3, s, s6, null);
				while ( resultset1.next() ) { // moving out node.add
					try {
					j++;
					String s8 = resultset1.getString(4);
					String s9 = s8 + ":";
					s9 = s9 + resultset1.getString(6);
					String s10 = s8 + " <BR>Desc:" + resultset1.getString(12);
					_tooltip.put(s6 + "." + s9, "<html>" + j + "." + s10
						+ "</html>");
					defaultmutabletreenode2 = new DefaultMutableTreeNode(s9);
					defaultmutabletreenode3.add(defaultmutabletreenode2); 
					} catch (Exception exp) {
						// ignore if any error in column type
					}
				}
			} catch (Exception exp) {
				ConsoleFrame.addText("\n Table:"+s6+":"+exp.getLocalizedMessage() );
				System.out.println("Table:"+s6+":"+exp.getLocalizedMessage());
			}
			
		 if (resultset1 != null) resultset1.close();
		} catch (Exception exp) {
			ConsoleFrame.addText("\n Exception in Creating Table Node");
			System.out.println("Error:" + exp.getMessage());
			JOptionPane.showMessageDialog(null, exp.getMessage(),
					"Error Message", 0);
			// System.exit(0); why should we exit
		} finally {
			
		}
	} // End of for loop for tables

		try {
			Rdbms_conn.closeConn();
		} catch (SQLException sqlexception) {
			ConsoleFrame.addText("\n SQL exception in Creating Node");
			JOptionPane.showMessageDialog(null, sqlexception.getMessage(),
					"Error Message", 0);
		}
	}

	private static void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		Profiler profiler = new Profiler();
		final JFrame jframe = new JFrame(
				"Aggregate Profiler : Provided by Arrah Technology");
		jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		profiler.setOpaque(true);
		jframe.setContentPane(profiler);
		
		JMenuBar jmenubar = new JMenuBar();
		jframe.setJMenuBar(jmenubar);
		JMenu jmenu = new JMenu("File");
		jmenu.setMnemonic('F');
		jmenubar.add(jmenu);
		JMenuItem jmenuitem = new JMenuItem("Open");
		jmenuitem.setAccelerator(KeyStroke.getKeyStroke(79, 2));
		jmenuitem.addActionListener(new FileActionListener());
		jmenu.add(jmenuitem);
		jmenu.addSeparator();
		JMenuItem jmenuitem1 = new JMenuItem("Show Console");
		jmenuitem1.addActionListener(new FileActionListener());
		jmenu.add(jmenuitem1);
		JMenu jmenu1 = new JMenu("Metadata Info");
		jmenu1.setMnemonic('D');
		jmenubar.add(jmenu1);
		JMenuItem jmenuitem2 = new JMenuItem("General Info");
		jmenuitem2.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu1.add(jmenuitem2);
		JMenuItem jmenuitem3 = new JMenuItem("Support Info");
		jmenuitem3.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu1.add(jmenuitem3);
		JMenuItem jmenuitem4 = new JMenuItem("Limitation Info");
		jmenuitem4.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu1.add(jmenuitem4);
		jmenu1.addSeparator();
		JMenuItem jmenuitem5 = new JMenuItem("Functions Info");
		jmenuitem5.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu1.add(jmenuitem5);
		JMenu jmenu2 = new JMenu("Type Info");
		JMenuItem jmenuitem6 = new JMenuItem("Standard SQL Type Info");
		jmenuitem6.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu2.add(jmenuitem6);
		JMenuItem jmenuitem7 = new JMenuItem("User Defined Type Info");
		jmenuitem7.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu2.add(jmenuitem7);
		jmenu1.add(jmenu2);
		JMenu jmenu3 = new JMenu("Object Info");
		JMenuItem jmenuitem8 = new JMenuItem("Catalog Info");
		jmenuitem8.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu3.add(jmenuitem8);
		JMenuItem jmenuitem9 = new JMenuItem("Schema Info");
		jmenuitem9.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu3.add(jmenuitem9);
		jmenu3.addSeparator();
		JMenuItem jmenuitem10 = new JMenuItem("Procedure Info");
		jmenuitem10.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu3.add(jmenuitem10);
		JMenuItem jmenuitem11 = new JMenuItem("Parameter Info");
		jmenuitem11.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu3.add(jmenuitem11);
		jmenu3.addSeparator();
		JMenuItem jmenuitem12 = new JMenuItem("Index Info");
		jmenuitem12.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu3.add(jmenuitem12);
		jmenu1.add(jmenu3);
		jmenu1.addSeparator();
		JMenuItem jmenuitem13 = new JMenuItem("Table Model Info");
		jmenuitem13.setAccelerator(KeyStroke.getKeyStroke(84, 2));
		jmenuitem13.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu1.add(jmenuitem13);
		jmenu1.addSeparator();
		JMenu jmenu4 = new JMenu("Summary Info");
		JMenuItem jmenuitem14 = new JMenuItem("DB MetaData Info");
		jmenuitem14.setAccelerator(KeyStroke.getKeyStroke(77, 2));
		jmenuitem14.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu4.add(jmenuitem14);
		JMenuItem jmenuitem15 = new JMenuItem("Table MetaData Info");
		jmenuitem15.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu4.add(jmenuitem15);
		JMenuItem jmenuitem16 = new JMenuItem("Data Info");
		jmenuitem16.setAccelerator(KeyStroke.getKeyStroke(73, 2));
		jmenuitem16.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu4.add(jmenuitem16);
		jmenu1.add(jmenu4);
		JMenu jmenu5 = new JMenu("Privilege Info");
		JMenuItem jmenuitem17 = new JMenuItem("All Tables Info");
		jmenuitem17.setAccelerator(KeyStroke.getKeyStroke(80, 2));
		jmenuitem17.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu5.add(jmenuitem17);
		JMenuItem jmenuitem18 = new JMenuItem("Table Info");
		jmenuitem18.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu5.add(jmenuitem18);
		JMenuItem jmenuitem19 = new JMenuItem("Column Info");
		jmenuitem19.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu5.add(jmenuitem19);
		jmenu1.add(jmenu5);
		jmenu1.addSeparator();
		JMenuItem jmenuitem38 = new JMenuItem("Data Dictionary");
		jmenuitem38.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu1.add(jmenuitem38);
		jmenu1.addSeparator();
		JMenu jmenu10 = new JMenu("Search");
		JMenuItem jmenuitem40 = new JMenuItem("Table Name");
		jmenuitem40.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu10.add(jmenuitem40);
		JMenuItem jmenuitem41 = new JMenuItem("Column Name");
		jmenuitem41.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu10.add(jmenuitem41);
		JMenuItem jmenuitem42 = new JMenuItem("Native Datatype");
		jmenuitem42.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu10.add(jmenuitem42);
		JMenuItem jmenuitem43 = new JMenuItem("SQL Datatype");
		jmenuitem43.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu10.add(jmenuitem43);
		jmenu1.add(jmenu10);
		jmenu1.addSeparator();
		JMenuItem piimenu = new JMenuItem("Personally Identifiable Info");
		piimenu.addActionListener(new DBMetaInfoPanel(jmenubar));
		jmenu1.add(piimenu);
		

		JMenu jmenu6 = new JMenu("Tools");
		jmenu6.setMnemonic('T');
		jmenubar.add(jmenu6);
		JMenuItem jmenuitem20 = new JMenuItem("SQL Interface");
		jmenuitem20.addActionListener(new ToolListener(jmenubar));
		jmenuitem20.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 10));
		jmenu6.add(jmenuitem20);
		jmenu6.addSeparator();
		JMenuItem create_t = new JMenuItem("Create Table");
		create_t.addActionListener(new ToolListener(jmenubar));
		create_t.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 10));
		jmenu6.add(create_t);
		jmenu6.addSeparator();
		
		// Import file will become Menu and load csv, xml and XLS
		// will become menu item
		JMenu impFile = new JMenu("Open File");
		
		JMenuItem jmenuitem21 = new JMenuItem("Text Format");
		impFile.add(jmenuitem21);
		jmenuitem21.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, 10));
		jmenuitem21.addActionListener(new ToolListener(jmenubar));
		
		JMenuItem impCsv = new JMenuItem("OpenCSV Format");
		impFile.add(impCsv);
		impCsv.addActionListener(new ToolListener(jmenubar));
		
		JMenuItem impXML = new JMenuItem("XML Format");
		impFile.add(impXML);
		impXML.addActionListener(new ToolListener(jmenubar));
		
		JMenuItem impXLS = new JMenuItem("XLS Format");
		impFile.add(impXLS);
		impXLS.addActionListener(new ToolListener(jmenubar));
		
		JMenuItem impMultiLine = new JMenuItem("Multi-Line Format");
		impFile.add(impMultiLine);
		impMultiLine.addActionListener(new ToolListener(jmenubar));
		
		jmenu6.add(impFile);
		jmenu6.addSeparator(); // end of import file
		
		// This menu will give options to copy file
		// to/from HDFS file system
		
		JMenu copyFile = new JMenu("Copy File");
		
		JMenuItem toHdfs = new JMenuItem("To HDFS");
		copyFile.add(toHdfs);
		toHdfs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, 10));
		toHdfs.addActionListener(new ToolListener(jmenubar));
		
		JMenuItem fromHdfs = new JMenuItem("From HDFS");
		copyFile.add(fromHdfs);
		fromHdfs.addActionListener(new ToolListener(jmenubar));
		
		jmenu6.add(copyFile);
		jmenu6.addSeparator(); // end of import file
		
		JMenuItem diffFile = new JMenuItem("Diff File");
		diffFile.addActionListener(new ToolListener(jmenubar));
		jmenu6.add(diffFile);
		jmenu6.addSeparator(); // end of diff file
		
		
		JMenuItem jmenuitem22 = new JMenuItem("Create Format");
		jmenuitem22.addActionListener(new ToolListener(jmenubar));
		jmenuitem22.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, 10));
		jmenu6.add(jmenuitem22);
		jmenu6.addSeparator();
		JMenuItem jmenuitem34 = new JMenuItem("Create Regex");
		jmenuitem34.addActionListener(new ToolListener(jmenubar));
		jmenuitem34.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 10));
		jmenu6.add(jmenuitem34);
		jmenu6.addSeparator();
		
		JMenuItem standardV = new JMenuItem("Create Standardization Value");
		standardV.addActionListener(new ToolListener(jmenubar));
		jmenu6.add(standardV);
		jmenu6.addSeparator();
		
		JMenu likeSearch = new JMenu("DB LIKE Search");
		
		JMenuItem jmenuitem23 = new JMenuItem("Search DB");
		jmenuitem23.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, 10));
		jmenuitem23.addActionListener(new ToolListener(jmenubar));
		likeSearch.add(jmenuitem23);
		likeSearch.addSeparator();
		JMenuItem jmenuitem35 = new JMenuItem("Search Table");
		jmenuitem35.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 10));
		jmenuitem35.addActionListener(new ToolListener(jmenubar));
		likeSearch.add(jmenuitem35);
		jmenu6.add(likeSearch);
		jmenu6.addSeparator();
		
		JMenuItem jmenuitem36 = new JMenuItem("Fuzzy Search");
		jmenuitem36.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 10));
		jmenuitem36.addActionListener(new ToolListener(jmenubar));
		jmenu6.add(jmenuitem36);
		jmenu6.addSeparator();
		
		JMenuItem mfsearch = new JMenuItem("Multi Facet Search");
		mfsearch.addActionListener(new ToolListener(jmenubar));
		jmenu6.add(mfsearch);
		
		// Add record related item here
		jmenu6.addSeparator();
		JMenu recordM = new JMenu("Record Match");
		JMenuItem recordMSingle = new JMenuItem("Single File Match");
		recordMSingle.addActionListener(new ToolListener(jmenubar));
		recordM.add(recordMSingle);
		JMenuItem recordMMulti = new JMenuItem("Multiple File Match");
		recordMMulti.addActionListener(new ToolListener(jmenubar));
		recordM.add(recordMMulti);
		jmenu6.add(recordM);
		jmenu6.addSeparator();
		JMenuItem recordLink1n = new JMenuItem("1:N Record Linkage");
		recordLink1n.addActionListener(new ToolListener(jmenubar));
		jmenu6.add(recordLink1n);
		jmenu6.addSeparator();
		JMenuItem recordLink11 = new JMenuItem("1:1 Record Linkage");
		recordLink11.addActionListener(new ToolListener(jmenubar));
		jmenu6.add(recordLink11);
		jmenu6.addSeparator();
		JMenu recordMerge = new JMenu("Record Merge");
		JMenuItem recordMergeSingle = new JMenuItem("Single File Merge");
		recordMergeSingle.addActionListener(new ToolListener(jmenubar));
		recordMerge.add(recordMergeSingle);
		JMenuItem recordMergeMulti = new JMenuItem("Multiple File Merge");
		recordMergeMulti.addActionListener(new ToolListener(jmenubar));
		recordMerge.add(recordMergeMulti);
		jmenu6.add(recordMerge);
		
		
		JMenu jmenu7 = new JMenu("Data Quality");
		jmenu7.setMnemonic('Q');
		jmenubar.add(jmenu7);
		JMenuItem jmenuitem24 = new JMenuItem("Duplicate");
		jmenuitem24.addActionListener(new QualityListener());
		jmenu7.add(jmenuitem24);
		jmenu7.addSeparator();
		
		JMenu fdedup = new JMenu("Fuzzy DeDup");
		JMenuItem jmenuitem31 = new JMenuItem("DeDup-Delete");
		jmenuitem31.addActionListener(new QualityListener());
		fdedup.add(jmenuitem31);
		fdedup.addSeparator();
		
		JMenuItem freplace = new JMenuItem("DeDup-Replace");
		freplace.addActionListener(new QualityListener());
		fdedup.add(freplace);
		jmenu7.add(fdedup);
		jmenu7.addSeparator();
		
		JMenuItem jmenuitem25 = new JMenuItem("Standardisation Regex");
		jmenuitem25.addActionListener(new QualityListener());
		jmenu7.add(jmenuitem25);
		jmenu7.addSeparator();
		JMenuItem stdfuzzy = new JMenuItem("Standardisation Fuzzy");
		stdfuzzy.addActionListener(new QualityListener());
		jmenu7.add(stdfuzzy);
		jmenu7.addSeparator();
		JMenuItem replaceNull = new JMenuItem("Replace Null");
		replaceNull.addActionListener(new QualityListener());
		jmenu7.add(replaceNull);
		jmenu7.addSeparator();

		JMenu jmenu8 = new JMenu("InComplete");
		jmenu7.add(jmenu8);
		JMenuItem jmenuitem26 = new JMenuItem("AND (Inclusive)");
		jmenuitem26.addActionListener(new QualityListener());
		jmenu8.add(jmenuitem26);
		JMenuItem jmenuitem27 = new JMenuItem("OR (Exclusive)");
		jmenuitem27.addActionListener(new QualityListener());
		jmenu8.add(jmenuitem27);
		jmenu7.addSeparator();
		JMenu jmenu9 = new JMenu("Formatted");
		jmenu7.add(jmenu9);
		JMenuItem jmenuitem28 = new JMenuItem("Match");
		jmenuitem28.addActionListener(new QualityListener());
		jmenu9.add(jmenuitem28);
		JMenuItem jmenuitem29 = new JMenuItem("No Match");
		jmenuitem29.addActionListener(new QualityListener());
		jmenu9.add(jmenuitem29);
		jmenu7.addSeparator();
		JMenu caseFormatC_m = new JMenu("Case Format");
		jmenu7.add(caseFormatC_m);
		JMenuItem upperC_m = new JMenuItem("UPPER CASE");
		upperC_m.addActionListener(new QualityListener());
		upperC_m.setActionCommand("uppercase");
		caseFormatC_m.add(upperC_m);
		JMenuItem lowerC_m = new JMenuItem("lower case");
		lowerC_m.addActionListener(new QualityListener());
		lowerC_m.setActionCommand("lowercase");
		caseFormatC_m.add(lowerC_m);
		JMenuItem titleC_m = new JMenuItem("Title Case");
		titleC_m.addActionListener(new QualityListener());
		titleC_m.setActionCommand("titlecase");
		caseFormatC_m.add(titleC_m);
		JMenuItem sentenceC_m = new JMenuItem("Sentence case");
		sentenceC_m.addActionListener(new QualityListener());
		sentenceC_m.setActionCommand("sentencecase");
		caseFormatC_m.add(sentenceC_m);
		jmenu7.addSeparator();
		JMenu discreetRange = new JMenu("Discreet Range");
		jmenu7.add(discreetRange);
		JMenuItem matchdiscreetRange = new JMenuItem("Discreet Match");
		matchdiscreetRange.addActionListener(new QualityListener());
		discreetRange.add(matchdiscreetRange);
		JMenuItem nomatchdiscreetRange = new JMenuItem("Discreet No Match");
		nomatchdiscreetRange.addActionListener(new QualityListener());
		discreetRange.add(nomatchdiscreetRange);
		jmenu7.addSeparator();
		JMenuItem jmenuitem30 = new JMenuItem("Cardinality");
		jmenuitem30.addActionListener(new QualityListener());
		jmenu7.add(jmenuitem30);
		jmenu7.addSeparator();
		JMenuItem jmenuitem32 = new JMenuItem("Cardinality Editable");
		jmenuitem32.addActionListener(new QualityListener());
		jmenu7.add(jmenuitem32);
		jmenu7.addSeparator();
		JMenuItem crossColval = new JMenuItem("Cross Column Search");
		crossColval.addActionListener(new QualityListener());
		jmenu7.add(crossColval);
		jmenu7.addSeparator();
		JMenu dscomp = new JMenu("DataSource Comparison");
		JMenuItem jmenuitem33 = new JMenuItem("Table Comparison");
		jmenuitem33.addActionListener(new QualityListener());
		dscomp.add(jmenuitem33);
		JMenuItem jmenuitem37 = new JMenuItem("DB Comparison");
		jmenuitem37.addActionListener(new QualityListener());
		dscomp.add(jmenuitem37);
		JMenuItem jmenuitem39 = new JMenuItem("Schema Comparison");
		jmenuitem39.addActionListener(new QualityListener());
		dscomp.add(jmenuitem39);
		jmenu7.add(dscomp);
		jmenu7.addSeparator();
		JMenuItem boxPlot = new JMenuItem("Box Plot");
		boxPlot.addActionListener(new QualityListener());
		jmenu7.add(boxPlot);
		jmenu7.addSeparator();
		JMenuItem kMean = new JMenuItem("K Mean Cluster");
		kMean.addActionListener(new QualityListener());
		jmenu7.add(kMean);
		
		// Business Rule Menu
		// Testing needs to be done for business rules
		
		JMenu businessrule = new JMenu("Business Rules");
		businessrule.setMnemonic('B');
		jmenubar.add(businessrule);
		JMenuItem addnewdb = new JMenuItem("Add DB");
		addnewdb.addActionListener(new BusinesRuleListener());
		businessrule.add(addnewdb);
		businessrule.addSeparator();
		JMenuItem buildrule = new JMenuItem("Build Rule");
		buildrule.addActionListener(new BusinesRuleListener());
		businessrule.add(buildrule);
		businessrule.addSeparator();
		JMenuItem executerule = new JMenuItem("Execute Rule");
		executerule.addActionListener(new BusinesRuleListener());
		businessrule.add(executerule);
		businessrule.addSeparator();
		JMenuItem schedulerule = new JMenuItem("Schedule Rule");
		schedulerule.addActionListener(new BusinesRuleListener());
		businessrule.add(schedulerule);
		

		jframe.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				int n = JOptionPane.showConfirmDialog(null,
						"Do you want to Exit ?", "Exit  Dialog",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

				if (n == JOptionPane.YES_OPTION) {
					try {
						Rdbms_conn.exitConn();
					} catch (Exception exp) {
					}
					; // do nothing
					jframe.dispose();
					System.exit(0);
				}
			}
		});
		jframe.setLocation(25, 5);
		jframe.pack();
		jframe.setVisible(true);
	}

	private class PopupListener extends MouseAdapter {
		PopupListener() {
		}

		public void mousePressed(final MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(final MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				int rowid = tree.getRowForLocation(e.getX(), e.getY());
				if (!tree.isRowSelected(rowid))
					tree.setSelectionRow(rowid);
				TreePath path = tree.getSelectionPath();
				if (path == null || path.getPathCount() < 2)
					return; // Root Clicked
				JPopupMenu popup = new JPopupMenu();
				ActionListener al = new PPMenuListener(path.getPathComponent(1)
						.toString(), tree);

				JMenuItem menuItem = new JMenuItem("Super Table Info");
				menuItem.setActionCommand("super");
				popup.add(menuItem);
				menuItem.addActionListener(al);
				popup.addSeparator();

				JMenuItem menuItem0 = new JMenuItem("Relationship Info");
				menuItem0.setActionCommand("relation");
				popup.add(menuItem0);
				menuItem0.addActionListener(al);
				popup.addSeparator();

				JMenuItem menuItem2 = new JMenuItem("Relationship Image");
				menuItem2.setActionCommand("relationimage");
				popup.add(menuItem2);
				menuItem2.addActionListener(al);
				popup.addSeparator();

				JMenuItem menuItem1 = new JMenuItem("Default Info");
				menuItem1.setActionCommand("default");
				menuItem1.addActionListener(al);
				popup.add(menuItem1);
				popup.addSeparator();
				
				JMenuItem menuItem3 = new JMenuItem("Duplicate Info");
				menuItem3.setActionCommand("duplicate");
				menuItem3.addActionListener(al);
				popup.add(menuItem3);
				popup.addSeparator();

				JMenuItem menuItem4 = new JMenuItem("Pattern Info");
				menuItem4.setActionCommand("pattern");
				menuItem4.addActionListener(al);
				popup.add(menuItem4);
				popup.addSeparator();
				
				JMenuItem menuItem5 = new JMenuItem("Table Completeness Info");
				menuItem5.setActionCommand("fill");
				menuItem5.addActionListener(al);
				popup.add(menuItem5);
				
				
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public static void main(String args[]) {
		boolean success = false;
		boolean fileLoad = false;
		
		if ((args.length > 0) && ( args[0] != null && !"".equals(args[0]) 
		    && !"\r".equals(args[0]) && !"\n".equals(args[0]))) { // open the confileFile.txt
			
			// mac os append ctrl-M to file name
			char last = args[0].charAt(args[0].length() -1);
			if (Character.isISOControl(last))
				args[0] = args[0].substring(0, args[0].length() - 1);
			
			
			_fileParse = KeyValueParser.parseFile(args[0]);
			try {
				Rdbms_conn.init(_fileParse);
				String status = Rdbms_conn.testConn();
				if ("Connection Successful".equals(status)) {
					Rdbms_conn.openConn();
					success = true;
				} else {
					System.out.println(" Can not open connection. Check configuration File:"+args[0]);
					System.out.println(" Status:"+status);
				}
			} catch (Exception e) {
				System.out.println(" Exception:"+e.getMessage());
			}
		} 
		// If configFile fails
		if ( success == false){
			TestConnectionDialog tcd = new TestConnectionDialog(0); // Default main connection
			tcd.createGUI();
			_fileParse = tcd.getDBParam();
			fileLoad = tcd.isFileLoad();
		}
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				ConsoleFrame.createGUI();
			}
		});
		t.start();
		if (fileLoad == false)
			createAndShowGUI();
		else { // Show FileMenu
			new FileLoaderFrame();
		}
	}
}
