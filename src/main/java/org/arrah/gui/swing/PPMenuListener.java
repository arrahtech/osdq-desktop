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

/* This file is used for  creating listener for
 * Profiler popup menu items and showing in 
 * ReportTable
 *
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.profile.TableFirstInformation;
import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.rdbms.TableRelationInfo;

public class PPMenuListener implements ActionListener {
	private String _table;
	private Vector<TableRelationInfo> levelTable;
	ReportTable _rt;
	boolean _isRec = false;
	boolean _isLevel = false;
	JComponent _parent;
	int maxLevel = 0;

	public PPMenuListener(String table, JComponent parent) {
		_table = table;
		_parent = parent;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			String command = e.getActionCommand();

			String cat = Rdbms_conn.getHValue("Database_Catalog");
			cat = "";
			String sch = Rdbms_conn.getHValue("Database_SchemaPattern");
			cat = cat.compareTo("") != 0 ? cat : null;
			sch = sch.compareTo("") != 0 ? sch : null;

			_parent.getTopLevelAncestor().setCursor(
					java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

			if ("super".equals(command)) {
				_isRec = true;
				_rt = new ReportTable(TableMetaInfo.getSuperTableInfo(cat, sch,
						_table));
			}
			if ("default".equals(command)) {
				_isRec = true;
				_rt = new ReportTable(TableMetaInfo.getColumnDefaultValue(cat,
						sch, _table));
			}
			if ("duplicate".equals(command)) {
				_isRec = true;
				TableFirstInformation tabInfo = new TableFirstInformation();
				
				HashMap<String,Double> profileMap = tabInfo.getTableProfile(_table);
				ReportTableModel rtm = new ReportTableModel(new String[]{"Information","Count"}, false, false);
				
				
				 if (profileMap.get("Total Count") != null)
					 rtm.addFillRow(new String[] {"Total Count",profileMap.get("Total Count").toString()});
				 
				 if (profileMap.get("Duplicate Pattern") != null)
					 rtm.addFillRow(new String[] {"Duplicate Pattern",profileMap.get("Duplicate Pattern").toString()});
				 
				 if (profileMap.get("Duplicate Count") != null)
					 rtm.addFillRow(new String[] {"Duplicate Count",profileMap.get("Duplicate Count").toString()});
				 
				 if(profileMap.get("Duplicate %") != null)
					 rtm.addFillRow(new String[] {"Duplicate %",profileMap.get("Duplicate %").toString()});
				 
				 if(profileMap.get("Unique Pattern") != null)
					 rtm.addFillRow(new String[] {"Unique Pattern",profileMap.get("Unique Pattern").toString()});
				 
				 if(profileMap.get("Unique %") != null)
					 rtm.addFillRow(new String[] {"Unique %",profileMap.get("Unique %").toString()});
				 
				 _rt = new ReportTable(rtm);
			}
			if ("pattern".equals(command)) {
				_isRec = true;
				TableFirstInformation tabInfo = new TableFirstInformation();
				double tabCount = tabInfo.getTableCount(_table);
				double patCount = tabInfo.getPatternCount(_table);
				Vector<Double> pv = tabInfo.getPatternValue();
				
				_rt = new ReportTable(new String[]{"Pattern","Count","Percentage"}, false, false);
				if ( (tabCount > 0) && (patCount > 0) && (pv != null))
				for (int i=0; i <patCount; i++ ) {
					_rt.addFillRow(new String[] {"Pattern_"+(i+1),
							pv.get(i).toString(),((Double)((pv.get(i)/tabCount)*100)).toString() });
				}
			}
			if ("fill".equals(command)) {
				_isRec = true;
				TableFirstInformation tabInfo = new TableFirstInformation();
				double tabCount = tabInfo.getTableCount(_table);
				int[] fillCount = tabInfo.getTableFill(_table);
				
				_rt = new ReportTable(new String[]{"Empty Column #","Count","Percentage"}, false, false);
				
				if ( (tabCount > 0) && (fillCount.length > 0) )
				for (int i=0; i <fillCount.length; i++ ) {
					_rt.addFillRow(new String[] {""+i+" Empty Column",
							((Integer)(fillCount[i])).toString(),((Double)((fillCount[i]/tabCount)*100)).toString() });
				}
				
			}
			
			if ("relation".equals(command)) {
				_isRec = true;
				ReportTableModel rtm = TableMetaInfo.tableKeyInfo(_table);
				_rt = new ReportTable(rtm);
			}
			if ("relationimage".equals(command)) {
				TableRelationInfo TableRelationInfo = TableMetaInfo
						.getTableRelationInfo(cat, sch, _table);
				int level = 0; // Start from Root
				TableRelationInfo.level = level;
				levelTable = new Vector<TableRelationInfo>();
				levelTable.add(TableRelationInfo);

				Vector<TableRelationInfo> lastLevel = levelTable;
				Vector<TableRelationInfo> nextLevel = new Vector<TableRelationInfo>();

				while (lastLevel.size() > 0) {
					level++;
					int lastSize = lastLevel.size();
					for (int i = 0; i < lastSize; i++) {
						TableRelationInfo = lastLevel.get(i);
						for (int j = 0; j < TableRelationInfo.fk_c; j++) {
							String table = TableRelationInfo.fk_pTable[j];
							TableRelationInfo TableRelationInfo_temp = TableMetaInfo
									.getTableRelationInfo(cat, sch, table);
							TableRelationInfo_temp.level = level;
							nextLevel.add(TableRelationInfo_temp);
							levelTable.add(TableRelationInfo_temp);
						}
					}

					lastLevel = new Vector<TableRelationInfo>(nextLevel);
					nextLevel.clear();
				}
				_isLevel = true;
				maxLevel = level;
			}

			// Set up the dialog
			if (_isRec == true) {
				JDialog jd = new JDialog();
				jd.setModal(true);
				jd.setTitle(_table + " Information");
				jd.setLocation(200, 150);
				jd.getContentPane().add(_rt);
				jd.pack();
				jd.setVisible(true);
			} else if (_isLevel == true) {
				if (levelTable.size() == 1) {
					String table = levelTable.get(0).tableName;
					JOptionPane.showMessageDialog(null, table
							+ " is Top Level Table", "Information Message",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					Hashtable<String, TableRelationInfo> rt = new Hashtable<String, TableRelationInfo>();
					for (int i = 0; i < levelTable.size(); i++) {
						TableRelationInfo t = levelTable.get(i);
						rt.put(t.tableName, t);
					}
					RelationPanel rp = new RelationPanel(rt, maxLevel - 1);
				}
			} else {
				JOptionPane.showMessageDialog(null, "No Relevant Data Found",
						"Error Message", 0);
			}

		} catch (SQLException sqlexception) {
			ConsoleFrame
					.addText("\n WARNING: Exception in Profiler PopMenu call ");
			JOptionPane.showMessageDialog(null, sqlexception.getMessage(),
					"Error Message", 0);
		} catch (NullPointerException nullpointerexception) {
			ConsoleFrame
					.addText("\n WARNING: Null Pointer Exception in Profiler Popup Menu call \n"
								+nullpointerexception.getMessage());
		} catch (UnsupportedOperationException unsupportedoperationexception) {
			ConsoleFrame
					.addText("\n WARNING: This operation is not supported on this database");
			JOptionPane.showMessageDialog(null,
					"This operation is not supported on this database",
					"Error Message", 0);
		} catch (Exception exception) {
			ConsoleFrame.addText("\n WARNING: Exception Happened -"+exception.getMessage());
		} finally {
			_parent.getTopLevelAncestor()
					.setCursor(
							java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}

	} // End of Action Performed

}
