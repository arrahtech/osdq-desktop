package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2016      *
 *                                             *
 *                                             *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* 
 * This file is used for showing file load
 * menu items - all the features that is file
 * dependent which have options here.
 *
 */

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.arrah.framework.rdbms.Rdbms_conn;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Hashtable;

public class FileLoaderFrame {
	final private JFrame jframe;

	public FileLoaderFrame() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		jframe = new JFrame(
				"Aggregate Profiler : Provided by Arrah Technology");
		jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		jframe.setContentPane(createTopPanel());
		addMenuItem();
		
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
		
		jframe.setLocation(50,50);
		jframe.setPreferredSize(new Dimension(500,400));
		jframe.pack();
		jframe.setVisible(true);

	}
	
	private JPanel createTopPanel() {
		JPanel jpanel = new JPanel();
		// String s = null;
		
		/* Under LGPL or Apache
		LicenseManager licensemanager = new LicenseManager();
		if (licensemanager.isValid()) {
			if (licensemanager.isEval) {
				s = licensemanager.c_name;
				s = s + "<BR> Trial Days remaining - "
						+ licensemanager.days_remaining;
			} else {
				s = "Licensed to: " + licensemanager.c_name;
			}
		} else {
			s = "Does not have Enterprise  License...<BR>";
			s = "Community License (LGPL) used. ";
		}
		*/
		
		String s = " Community License (LGPL). ";
		s = "<html> <B> <I> <U> &copy; 2006-2016  Arrah Technology </U> <BR>"
				+ s + "</I></B> </html>";
		
		jpanel.setLayout(new GridLayout(12, 1));
		JLabel jlabel = new JLabel();
		JLabel jlabel1 = new JLabel(
				"Welcome to Open Source Data Quality Project", 0);
		jlabel1.setFont(new Font("Helvetica", 1, 16));
		JLabel jlabel2 = new JLabel(
				"http://sourceforge.net/projects/dataquality/", 0);
		JLabel jlabel3 = new JLabel("arrah@users.sourceforge.net", 0);
		
		JLabel jlabel4 = new JLabel(s, 0);
		jpanel.add(jlabel);
		jpanel.add(jlabel1);
		jpanel.add(jlabel2);
		jpanel.add(jlabel3);
		jpanel.add(jlabel4);
		return jpanel;
	}
	
	private void addMenuItem() {
		// Import file will become Menu and load csv, xml and XLS
		// will become menu item
		JMenuBar jmenubar = new JMenuBar();
		jframe.setJMenuBar(jmenubar);
		JMenu jmenu6 = new JMenu("File Loader");
		
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
		jmenu6.addSeparator();
		
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
		
		// Add record related item here
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
		
		jmenubar.add(jmenu6);
		
		// Initialize the singleton class RDBMS_Conn t o avoid static call null
		try {
			new Rdbms_conn();
			Rdbms_conn.init(new Hashtable<String,String>());
		} catch (SQLException e) {
			ConsoleFrame.addText("\n Exception in Initializing Rdbms_conn:"+ e.getLocalizedMessage() );
		};
		
	}

}
