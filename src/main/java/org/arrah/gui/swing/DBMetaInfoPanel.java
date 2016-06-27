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

/* This file is used for creating Metadata info 
 *
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.arrah.framework.analytics.MetadataMatcher;
import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.profile.DBMetaInfo;
import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.DataDictionaryPDF;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.rdbms.TableRelationInfo;
import org.arrah.framework.util.KeyValueParser;


public class DBMetaInfoPanel implements ActionListener {
	private boolean isFrame;
	private boolean summary_info;
	private boolean variableQ;
	private TableMetaInfoPanel vp;
	private ReportTableModel rtm__;
	private ReportTable rt__;
	private String f_title;
	private JComponent src_;

	public DBMetaInfoPanel() {
		isFrame = false;
		summary_info = false;
		variableQ = false;
		vp = null;
		rt__ = null;
		f_title = "DB Meta Information";
	}

	public DBMetaInfoPanel(JComponent src) {
		src_ = src;
		isFrame = false;
		summary_info = false;
		variableQ = false;
		vp = null;
		rt__ = null;
		f_title = "DB Meta Information";
	}

	public void actionPerformed(ActionEvent actionevent) {
		Hashtable<String, TableRelationInfo> hashtable = null, hashtable1 = null, hashtable2 = null;
		String s;
		String s31;

		try {

			if (src_ != null)
				src_.getTopLevelAncestor()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

			s = ((JMenuItem) actionevent.getSource()).getText();

			if (s.equals("Data Dictionary")) {
				File pdfFile = FileSelectionUtil.promptForFilename("Data Dictionary PDF File");
				if (pdfFile == null) {
					return;
				}
				if (pdfFile.getName().toLowerCase().endsWith(".pdf") == false) {
					File renameF = new File(pdfFile.getAbsolutePath() + ".pdf");
					pdfFile = renameF;
				}
				DataDictionaryPDF datad = new DataDictionaryPDF();
				datad.createDDPDF(pdfFile);
				ConsoleFrame.addText("\nData Dictionary File Saved at:"+pdfFile.getAbsolutePath());
				return;
			}
			if (s.equals("Personally Identifiable Info")) {
				// Create the Column name into hashtable
				Hashtable<String,String>__h = KeyValueParser.parseFile("resource/piiSearch.txt");
				Vector<String> tableName = Rdbms_conn.getTable();
				
				if (tableName == null || __h == null) {
					JOptionPane.showMessageDialog(null, "Resource file not Found or \n"
							+ " No Table found");
					return;
				}
				// Create MetaData Matcher
				MetadataMatcher mm = new MetadataMatcher(__h);
				String[] colName = new String[] {"Table","Column","PIIGroup","Confidence",
						"field1","field2","field3","field4","field5","field6","field7","field8","field9","field10"};
				rtm__ = new ReportTableModel(colName,true,true);
				
				for (int i=0; i<tableName.size(); i++) {
					String table = tableName.get(i);
					
					Vector<?>[] colInfo = Rdbms_conn.populateColumn(table, null);
					if (colInfo == null ) {
						JOptionPane.showMessageDialog(null,  "No Column found for Table:"+table);
						continue;
					}
					String[] colNames = new String[colInfo[0].size()];
					colNames = colInfo[0].toArray(colNames);
					Hashtable<String, Vector<String>> ht = mm.matchedKeys(colNames,0.8f);
					
					for (Enumeration<String> e = ht.keys(); e.hasMoreElements();) {
						String key = e.nextElement();
						Vector<String>piiv= ht.get(key);
						int size = piiv.size();
						// System.out.println("--- Table:" + table+" Column:" + key + " Value:" + ht.get(key) + "Size:" + size);
						for (int j = 0; j < size; j++) {
							Object[] row = new Object[10+4];
							Object[] colD = new Object[10];
							colD = mm.getPIIColData(table, key, piiv.get(j), colD);
							String confidence = mm.getConfidenceLevel();
							row[0]= table;row[1]= key;row[2]= piiv.get(j);row[3]= confidence;
							for (int z=0; z < colD.length; z++) 
								row[z+4] = colD[z];
							rtm__.addFillRow(row);
						}
						
					} // end of column 
				} // End of table loop
				
				rt__ = new ReportTable(rtm__);
				isFrame = true;
				summary_info = true;
			}
			
			else if (s.equals("General Info")) {
				f_title = "General Information";
				rtm__ = new DBMetaInfo().getGeneralInfo();
				rt__ = new ReportTable(rtm__);

				isFrame = true;
				summary_info = true;
			} else if (s.equals("Support Info")) {
				f_title = "Support Information";
				rtm__ = new DBMetaInfo().getSupportInfo();
				rt__ = new ReportTable(rtm__);

				isFrame = true;
				summary_info = true;
			} else if (s.equals("Limitation Info")) {
				f_title = "Limitation Information";
				rtm__ = new DBMetaInfo().getLimitationInfo();
				rt__ = new ReportTable(rtm__);

				isFrame = true;
				summary_info = true;
			} else if (s.equals("Functions Info")) {
				f_title = "Functions Information";
				rtm__ = new DBMetaInfo().getFunctionInfo();
				rt__ = new ReportTable(rtm__);

				isFrame = true;
				summary_info = true;
			} else if (s.equals("Catalog Info")) {
				f_title = "Catalog Information";
				rtm__ = new DBMetaInfo().getCatalogInfo();
				rt__ = new ReportTable(rtm__);

				isFrame = true;
				summary_info = true;
			} else if (s.equals("Standard SQL Type Info")) {
				f_title = "Standard SQL Type Information";
				rtm__ = new DBMetaInfo().getStandardSQLInfo();
				rt__ = new ReportTable(rtm__);

				isFrame = true;
				summary_info = true;
			} else if (s.equals("User Defined Type Info")) {
				f_title = "User Defined Type Information";
				rtm__ = new DBMetaInfo().getUserSQLInfo();
				rt__ = new ReportTable(rtm__);

				isFrame = true;
				summary_info = true;
			} else if (s.equals("Schema Info")) {
				f_title = "Schema Information";
				rtm__ = new DBMetaInfo().getSchemaInfo();
				rt__ = new ReportTable(rtm__);

				isFrame = true;
				summary_info = true;
			} else if (s.equals("Procedure Info")) {
				f_title = "Procedure Information";
				rtm__ = new DBMetaInfo().getProcedureInfo();
				rt__ = new ReportTable(rtm__);

				summary_info = true;
				isFrame = true;
			} else if (s.equals("Index Info")) {
				f_title = "Index Information";
				vp = new TableMetaInfoPanel(1);
				variableQ = true;
				isFrame = true;
			} else if (s.equals("Parameter Info")) {
				f_title = "Parameter Information";
				rtm__ = new DBMetaInfo().getParameterInfo();
				rt__ = new ReportTable(rtm__);

				isFrame = true;
				summary_info = true;

			} else if (s.equals("Table Model Info")) {
				f_title = "Table Model Information";

				hashtable = new Hashtable<String, TableRelationInfo>();
				hashtable1 = new Hashtable<String, TableRelationInfo>();
				hashtable2 = new Hashtable<String, TableRelationInfo>();
				DBMetaInfo dbMetaInfo = new DBMetaInfo();
				rtm__ = dbMetaInfo.getTableModelInfo();

				// write code to populate hashtable
				hashtable = dbMetaInfo.getOnlyPKTable();
				hashtable1 = dbMetaInfo.getNoPKTable();
				hashtable2 = dbMetaInfo.getRelatedTable();

			} else if (s.equals("DB MetaData Info")) {
				f_title = "DB MetaData Information";
				vp = new TableMetaInfoPanel(2);
				variableQ = true;
				isFrame = true;
			} else if (s.equals("Table MetaData Info")) {
				f_title = "Table MetaData Information";
				s31 = JOptionPane.showInputDialog(null,
						"Enter MetaData Table Pattern:", "Table Input Dialog",
						-1);
				if (s31 == null || s31.compareTo("") == 0)
					return;

				rtm__ = new DBMetaInfo().getTableMetaData(s31);
				rt__ = new ReportTable(rtm__);

				summary_info = true;
				isFrame = true;

			} else if (s.equals("All Tables Info")) {
				f_title = "All Table Privilege Information";
				vp = new TableMetaInfoPanel(3);
				variableQ = true;
				isFrame = true;
			} else if (s.equals("Table Info")) {
				f_title = " Table Privilege Information";
				s31 = JOptionPane.showInputDialog(null, "Enter Table Pattern:",
						"Table Input Dialog", -1);
				if (s31 == null || s31.compareTo("") == 0)
					return;

				rtm__ = new DBMetaInfo().getTablePrivilege(s31);
				rt__ = new ReportTable(rtm__);

				summary_info = true;
				isFrame = true;

			} else if (s.equals("Column Info")) {
				f_title = "Column Privilege Information";
				s31 = JOptionPane.showInputDialog(null, "Enter Table Pattern:",
						"Table Input Dialog", -1);
				if (s31 == null || s31.compareTo("") == 0)
					return;
				rtm__ = new DBMetaInfo().getColumnPrivilege(s31);
				rt__ = new ReportTable(rtm__);

				summary_info = true;
				isFrame = true;
			} else if (s.equals("Data Info")) {
				f_title = "Data Summary Information";
				vp = new TableMetaInfoPanel(4);
				variableQ = true;
				isFrame = true;
			} else if (s.equals("Table Name")) {
				f_title = "Table Name Information";
				s31 = JOptionPane.showInputDialog(null, "Enter Table Name:",
						"Table Input Dialog", -1);
				if (s31 == null || s31.compareTo("") == 0)
					return;
				
				rtm__ = TableMetaInfo.populateTable(2, 0,Rdbms_conn.getTableCount(), rtm__);
				rt__ = new ReportTable(rtm__);
				new SimilarityCheckPanel(s31, rt__, 0);
				
				return;

			} else if (s.equals("Column Name")) {
				f_title = "Table Name Information";
				s31 = JOptionPane.showInputDialog(null, "Enter Column Name:",
						"Column Input Dialog", -1);
				if (s31 == null || s31.compareTo("") == 0)
					return;
				
				rtm__ = TableMetaInfo.populateTable(2, 0,Rdbms_conn.getTableCount(), rtm__);
				rt__ = new ReportTable(rtm__);
				new SimilarityCheckPanel(s31, rt__, 1);
				
				return;

			} else if (s.equals("Native Datatype")) {
				f_title = "Table Name Information";
				s31 = JOptionPane.showInputDialog(null, "Enter Native (DataBase) Datatype:",
						"Native Datatype Input Dialog", -1);
				if (s31 == null || s31.compareTo("") == 0)
					return;
				
				rtm__ = TableMetaInfo.populateTable(2, 0,Rdbms_conn.getTableCount(), rtm__);
				rt__ = new ReportTable(rtm__);
				new SimilarityCheckPanel(s31, rt__, 2);
				
				return;

			} else if (s.equals("SQL Datatype")) {
				f_title = "Table Name Information";
				s31 = JOptionPane.showInputDialog(null, "Enter SQL (java.sql) Datatype:",
						"Java SQL Datatype Input Dialog", -1);
				if (s31 == null || s31.compareTo("") == 0)
					return;
				
				rtm__ = TableMetaInfo.populateTable(2, 0,Rdbms_conn.getTableCount(), rtm__);
				rt__ = new ReportTable(rtm__);
				new SimilarityCheckPanel(s31, rt__, 3);
				
				return;

			} else {
				return;
			}
			Rdbms_conn.closeConn();
			if (isFrame) {
				JFrame.setDefaultLookAndFeelDecorated(true);
				JFrame jframe = new JFrame(f_title);
				jframe.setDefaultCloseOperation(2);
				if (summary_info)
					jframe.setContentPane(rt__);
				else if (variableQ)
					jframe.setContentPane(vp);
				jframe.setLocation(100, 100);
				jframe.pack();
				jframe.setVisible(true);
			} else {
				 new RelationPanel(hashtable1, hashtable,
						hashtable2);
			}
		} catch (SQLException sqlexception) {
			ConsoleFrame
					.addText("\n WARNING: SQL Exception in DBInfo Menu call ");
			JOptionPane.showMessageDialog(null, sqlexception.getMessage(),
					"Error Message", 0);
		} catch (NullPointerException nullpointerexception) {
			ConsoleFrame
					.addText("\n WARNING: Null Pointer Exception in DBInfo Menu call ");
			ConsoleFrame.addText("\n Message: "
					+ nullpointerexception.getMessage());
		} catch (UnsupportedOperationException unsupportedoperationexception) {
			ConsoleFrame
					.addText("\n WARNING: This operation is not supported on this database");
		} catch (Exception exception) {
			ConsoleFrame.addText("\n WARNING: Unknown Exception Happened");
			ConsoleFrame.addText("\n Message: " + exception.getMessage());
			exception.printStackTrace();
			JOptionPane.showMessageDialog(null, exception.getMessage(),
					"Error Message", 0);
		} finally {
			if (src_ != null)
				src_.getTopLevelAncestor()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}
		return;
	}

}
