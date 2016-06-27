package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2014      *
 *                                             *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for creating First
 * Information report displayed  
 *
 */

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.MediaTracker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.profile.FirstInformation;
import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_conn;

public class FirPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PlotterPanel barChart;
	private HorizontalPlotter horizontalC;
	private PiePanel pieChart;
	private JPanel topP;
	private JPanel tableP;
	private ReportTable _rt;
	private JLabel tableN;
	private JLabel colN;
	private JLabel dsnN;
	private JLabel colC;
	private JLabel maxL;
	private JLabel minL;
	private JLabel dummyN;
	private String tC;
	private QueryBuilder table_qb;
	private String qString = "";
	
	private double _ad[] ;
	private Vector _avector[],_avector1[] ;
	
	private class LinkMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent mouseevent) {
			try {
				mouseevent.getComponent().setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				Vector<String> vector = Rdbms_conn.getTable();
				String s = tableN.getText();
				int i = vector.indexOf(s);
				ReportTable reporttable = null;
				ReportTableModel rtm = null;
				String s1 = ((JLabel) mouseevent.getSource()).getText();
				if (s1 != null) {
					if (s1.equals("<html><body><a href=\"\">Show Condition</A></body></html>")) {
						String qry_msg = qString;
						if ("".equals(qry_msg))
							qry_msg = "Condition Not Set";
						JOptionPane.showMessageDialog(null, qry_msg,
								"Query Information",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					} else if (s1
							.equals("<html><body><a href=\"\">Save As Image</A></body></html>")) {
						new ImageUtil(_comp, "png");
						return;
					} else if (s1
							.equals("<html><body><a href=\"\">Table View</A></body></html>")) {
						
						/* Create Table View and put into JDialog*/
						JTabbedPane tb = createTableView();
						JDialog jd = new JDialog();
						jd.setLocation(200, 150);
						jd.setTitle(s +" Record View ");
						jd.getContentPane().add(tb);
						jd.pack();
						jd.setVisible(true);
						return;
					} else if (s1
							.equals("<html><body><a href=\"\">MetaData</A></body></html>")) {
						s = s + " MetaData Information";
						reporttable = new ReportTable(
								TableMetaInfo.populateTable(2, i, i + 1, rtm));
					} else if (s1
							.equals("<html><body><a href=\"\">Summary Data</A></body></html>")) {
						s = s + " Summary Information";
						reporttable = new ReportTable(
								TableMetaInfo.populateTable(4, i, i + 1, rtm));
					} else if (s1
							.equals("<html><body><a href=\"\">Table Privilege</A></body></html>")) {
						s = s + " Table Privilege Information";
						reporttable = new ReportTable(
								TableMetaInfo.populateTable(3, i, i + 1, rtm));
					} else if (s1
							.equals("<html><body><a href=\"\">Indexes</A></body></html>")) {
						s = s + " Table Index Information";
						reporttable = new ReportTable(
								TableMetaInfo.populateTable(1, i, i + 1, rtm));
					} else if (s1
							.equals("<html><body><a href=\"\">Min Value: </A></body></html>")
							|| s1.equals("<html><body><a href=\"\">Max Value: </A></body></html>")) {
						String sd = dsnN.getText();
						String st = tableN.getText();
						String sc = colN.getText();
						QueryBuilder querybuilder = new QueryBuilder(sd, st,
								sc, Rdbms_conn.getDBType());
						String top_sel_query = querybuilder.top_query(true,
								"top_count", "20");
						String bottom_sel_query = querybuilder.bottom_query(
								true, "bottom_count", "20");
						ReportTable rt = new ReportTable(new String[] { "Top",
								"Bottom" });
						try {
							Rdbms_conn.openConn();
							ResultSet rs = Rdbms_conn.runQuery(top_sel_query);
							int counter = 0;
							while (rs.next()) {
								String top_val = rs.getString("top_count");
								rt.addRow();
								rt.setTableValueAt(top_val, counter++, 0);
							}
							rs.close();
							rs = Rdbms_conn.runQuery(bottom_sel_query);
							counter = 0;
							while (rs.next()) {
								String bot_val = rs.getString("bottom_count");
								rt.setTableValueAt(bot_val, counter++, 1);
							}
							rs.close();
							Rdbms_conn.closeConn();
						} catch (SQLException exp) {
							ConsoleFrame.addText("Error: " + exp.getMessage());
							JOptionPane.showMessageDialog(null,
									exp.getMessage(), "Error Message",
									JOptionPane.ERROR_MESSAGE);
							return;

						}

						JDialog jd = new JDialog();
						jd.setLocation(200, 150);
						jd.setTitle(st + ":" + sc);
						jd.getContentPane().add(rt);
						jd.pack();
						jd.setVisible(true);
						return;

					} else if (s1
							.equals("<html><body><a href=\"\">Query</A></body></html>")) {
						Vector avector[] = null;
						avector = TableMetaInfo.populateTable(5, i, i + 1,
								avector);
						QueryDialog querydialog = new QueryDialog(1, s, avector);
						s = s + " Table Query Panel";
						querydialog.setLocation(175, 100);
						querydialog.setTitle(s);
						querydialog.setModal(true);
						querydialog.pack();
						querydialog.setVisible(true);
						int j = querydialog.response;
						if (j == 1) {
							_rt = querydialog._rt;
							colC.setText(querydialog.tC);
							removeAll();
							add(createTableP(tableP));
							qString = querydialog.cond;
							revalidate();
							repaint();
						}
						return;
					}
				} else {
					Vector avector[] = null;
					avector = TableMetaInfo.populateTable(5, i, i + 1, avector);
					QueryDialog querydialog = new QueryDialog(1, s, avector);
					s = s + " Table Query Panel";
					querydialog.setLocation(175, 100);
					querydialog.setTitle(s);
					querydialog.setModal(true);
					querydialog.pack();
					querydialog.setVisible(true);
					int j = querydialog.response;
					if (j == 1) {
						_rt = querydialog._rt;
						colC.setText(querydialog.tC);
						removeAll();
						add(createTableP(tableP));
						qString = querydialog.cond;
						revalidate();
						repaint();
					}
					return;
				}
				JDialog jdialog = new JDialog();
				if (reporttable == null) {
					return;
				} else {
					jdialog.getContentPane().add(reporttable);
					jdialog.setLocation(175, 100);
					jdialog.setTitle(s);
					jdialog.pack();
					jdialog.setVisible(true);
					return;
				}
			} finally {
				mouseevent
						.getComponent()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
		}

		public void mouseEntered(MouseEvent mouseevent) {
			mouseevent.getComponent().setCursor(Cursor.getPredefinedCursor(12));
		}

		private LinkMouseListener() {
		}

		private LinkMouseListener(JComponent comp) {
			_comp = comp;
		}

		private JComponent _comp;
	}

	private class MyChangeListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent propertychangeevent) {
			String s = dsnN.getText();
			String s1 = tableN.getText();
			String s2 = colN.getText();
			if (tableN.getText().equals("")) {
				removeAll();
				add(topP);
				revalidate();
				repaint();
				return;
			}
			if (colN.getText().equals("")) {
				tC = "0";
				table_qb = new QueryBuilder(s, s1, Rdbms_conn.getDBType());
				runTableQuery(table_qb, "");
			} else {
				QueryBuilder querybuilder = new QueryBuilder(s, s1, s2,
						Rdbms_conn.getDBType());
				removeAll();
				add(createColPanel());
				revalidate();
				repaint();
				_ad = FirstInformation.getProfileValues(querybuilder);
				try {
          _avector = FirstInformation
          		.getPatternValues(querybuilder);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
				_avector1 = getDistributionValues(querybuilder);
				showBar(_ad);
				showPatternChart(_avector);
				showDistributionChart(_avector1);
			}
		}

		private MyChangeListener() {
		}

	}

	public FirPanel() {
		topP = null;
		tableP = null;
		tC = "0";
		table_qb = null;
		showGUI();
	}

	private void showGUI() {
		dsnN = new JLabel("", 0);
		dsnN.setToolTipText("DSN");
		tableN = new JLabel("", 0);
		tableN.setToolTipText("Table");
		colN = new JLabel("", 0);
		colN.setToolTipText("Column");
		colC = new JLabel("", 0);
		colC.setToolTipText("Record Count");
		dummyN = new JLabel();
		dummyN.addPropertyChangeListener("text", new MyChangeListener());
		setLayout(new GridLayout(1, 1));
		topP = createTopPanel();
		add(topP);
	}

	private void showBar(double ad[]) {
		String as[];
		as = (new String[] { "Total", "Unique", "Repeat", "Pattern", "Null" });
		barChart.setZoomFactor(1.0D);
		barChart.setInit();
		try { // Setting the cursor
			barChart.getTopLevelAncestor().setCursor(
					java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			barChart.setXLabel(as);
			barChart.setxValues(ad);
			barChart.setColorIndex(3);
			barChart.setTitle("Record Count");
			barChart.drawBarChart();
		} finally {
			barChart.getTopLevelAncestor()
					.setCursor(
							java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}
	}

	public void setTableText(String s) {
		tableN.setText(s);
	}

	public void setColText(String s) {
		colN.setText(s);
	}

	public void setDSNText(String s) {
		dsnN.setText(s);
	}

	public void setDummyText(String s) {
		dummyN.setText(s);
	}

	private JPanel createColPanel() {
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new BorderLayout());
		JPanel jpanel1 = new JPanel(new GridLayout(1, 4));
		jpanel1.add(tableN);
		jpanel1.add(colN);
		JLabel sTable = new JLabel(
				"<html><body><a href=\"\">Table View</A></body></html>",
				JLabel.CENTER);
		sTable.addMouseListener(new LinkMouseListener(jpanel));
		jpanel1.add(sTable);
		JLabel sImage = new JLabel(
				"<html><body><a href=\"\">Save As Image</A></body></html>",
				JLabel.CENTER);
		sImage.addMouseListener(new LinkMouseListener(jpanel));
		jpanel1.add(sImage);
		jpanel1.setPreferredSize(new Dimension(600, 25));
		jpanel.add(jpanel1, "First");
		jpanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		JPanel jpanel2 = new JPanel(new GridLayout(2, 1));
		barChart = new PlotterPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public String getToolTipText(MouseEvent mouseevent) {
				return null;
			}
		};
		pieChart = new PiePanel();
		jpanel2.add(barChart);
		jpanel2.add(pieChart);

		JPanel jpanel3 = new JPanel(new GridLayout(2, 2));
		JLabel lminL = new JLabel(
				"<html><body><a href=\"\">Min Value: </A></body></html>",
				JLabel.CENTER);
		lminL.addMouseListener(new LinkMouseListener());
		JLabel lmaxL = new JLabel(
				"<html><body><a href=\"\">Max Value: </A></body></html>",
				JLabel.CENTER);
		lmaxL.addMouseListener(new LinkMouseListener());
		minL = new JLabel("", JLabel.LEADING);
		maxL = new JLabel("", JLabel.LEADING);
		jpanel3.add(lminL);
		jpanel3.add(minL);
		jpanel3.add(lmaxL);
		jpanel3.add(maxL);
		jpanel3.setPreferredSize(new Dimension(300, 40));
		horizontalC = new HorizontalPlotter();
		horizontalC.setPreferredSize(new Dimension(300, 571));
		JPanel jpanel4 = new JPanel(new BorderLayout());
		jpanel4.add(jpanel3, "First");
		jpanel4.add(horizontalC, "Center");

		// Create a split pane with the two scroll panes in it.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				jpanel2, jpanel4);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(337);

		// Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(200, 50);
		jpanel2.setMinimumSize(minimumSize);
		jpanel4.setMinimumSize(minimumSize);

		jpanel.add(splitPane, "Center");
		return jpanel;
	}

	private JPanel createTableP(JPanel jpanel) {
		if (jpanel == null) {
			qString = "";
			jpanel = new JPanel();
			jpanel.setLayout(new BorderLayout());

			JPanel jpanel1 = new JPanel(new GridLayout(1, 3));
			jpanel1.add(dsnN);
			jpanel1.add(tableN);
			jpanel1.add(colC);
			jpanel1.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));

			JPanel jpanel2 = new JPanel();
			LinkMouseListener linkmouselistener = new LinkMouseListener();
			ImageIcon imageicon = new ImageIcon(getClass().getClassLoader().getResource("image/Filter.gif"), "Query");
			int imageLS = imageicon.getImageLoadStatus();

			JLabel jlabel;
			if (imageLS == MediaTracker.ABORTED
					|| imageLS == MediaTracker.ERRORED)
				jlabel = new JLabel(
						"<html><body><a href=\"\">Query</A></body></html>", 0);
			else
				jlabel = new JLabel(imageicon, 2);
			jlabel.addMouseListener(linkmouselistener);
			jlabel.setToolTipText("Add Conditions");

			JLabel jlabel1 = new JLabel(
					"<html><body><a href=\"\">Show Condition</A></body></html>",
					0);
			jlabel1.addMouseListener(linkmouselistener);
			JLabel jlabel2 = new JLabel(
					"<html><body><a href=\"\">MetaData</A></body></html>", 0);
			jlabel2.addMouseListener(linkmouselistener);
			JLabel jlabel3 = new JLabel(
					"<html><body><a href=\"\">Summary Data</A></body></html>",
					0);
			jlabel3.addMouseListener(linkmouselistener);
			JLabel jlabel4 = new JLabel(
					"<html><body><a href=\"\">Table Privilege</A></body></html>",
					0);
			jlabel4.addMouseListener(linkmouselistener);
			JLabel jlabel5 = new JLabel(
					"<html><body><a href=\"\">Indexes</A></body></html>", 0);
			jlabel5.addMouseListener(linkmouselistener);

			jpanel2.add(jlabel);
			jpanel2.add(jlabel1);
			jpanel2.add(jlabel2);
			jpanel2.add(jlabel3);
			jpanel2.add(jlabel4);
			jpanel2.add(jlabel5);

			JPanel jpanel3 = new JPanel(new GridLayout(2, 1));
			jpanel3.add(jpanel1);
			jpanel3.add(jpanel2);
			jpanel.add(jpanel3, "First");
			jpanel.add(_rt, "Center");
		} else {
			jpanel.revalidate();
		}
		return jpanel;
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

	private void showPatternChart(Vector avector[]) {
		if (avector == null)
			return;
		byte byte0 = 25;
		if (avector[1].size() > byte0) {
			double d = 0.0D;
			double d1 = 0.0D;
			int i;
			for (; avector[0].size() > byte0; avector[1].removeElementAt(i)) {
				d1++;
				i = avector[0].size() - 1;
				d += ((Double) avector[1].elementAt(i)).doubleValue();
				avector[0].removeElementAt(i);
			}

			avector[0].add(byte0, "Others(" + Math.round(d1) + ")");
			avector[1].add(byte0, new Double(d));
		}
		horizontalC.setZoomFactor(1.0D);
		horizontalC.setInit();
		try { // Setting the cursor
			horizontalC.getTopLevelAncestor().setCursor(
					java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			horizontalC.setXLabel(avector[0].toArray());
			horizontalC.setxValues(avector[1].toArray());
			horizontalC.setColorIndex(3);
			horizontalC.setTitle("Pattern Information");
			horizontalC.drawBarChart();
		} finally {
			horizontalC
					.getTopLevelAncestor()
					.setCursor(
							java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}
	}

	private void showDistributionChart(Vector avector[]) {
		if (avector == null)
			return;
		try { // Setting the cursor
			pieChart.getTopLevelAncestor().setCursor(
					java.awt.Cursor
							.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
			pieChart.setLabel(avector[0].toArray());
			pieChart.setValue(avector[1].toArray());
			pieChart.drawPieChart();
		} finally {
			pieChart.getTopLevelAncestor()
					.setCursor(
							java.awt.Cursor
									.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		}
	}

	public Vector[] getDistributionValues(QueryBuilder querybuilder)

	{
		Vector avector[] = FirstInformation.getDistributionValues(querybuilder);
		minL.setText(FirstInformation.getMinVal());
		maxL.setText(FirstInformation.getMaxVal());
		return avector;
	}

	private void runTableQuery(QueryBuilder querybuilder, String s) {
		try {
			String s1 = querybuilder.get_tableAll_query();
			if (s != null && !s.equals(""))
				s1 = s1 + " WHERE " + s;
			Rdbms_conn.openConn();
			ResultSet resultset = Rdbms_conn.runQuery(s1, 100); // get 100 rows
			_rt = SqlTablePanel.getSQLValue(resultset, true);
			resultset.close();
			String s2 = querybuilder.get_tableCount_query();
			if (s != null && !s.equals(""))
				s2 = s2 + " WHERE " + s;
			for (ResultSet resultset1 = Rdbms_conn.runQuery(s2); resultset1
					.next(); colC.setText(tC))
				tC = resultset1.getString("row_count");

			removeAll();
			add(createTableP(tableP));
			revalidate();
			repaint();
			Rdbms_conn.closeConn();
		} catch (SQLException sqlexception) {
			ConsoleFrame.addText("\n Error: Report Table Not Filled");
			ConsoleFrame.addText("\n " + sqlexception.getMessage());
			JOptionPane.showMessageDialog(null, sqlexception.getMessage(),
					"Error Message", 0);
			return;
		}
	}
	
	private JTabbedPane createTableView() {
		JTabbedPane tb = new JTabbedPane();
		ReportTable rt_rec  = new ReportTable(new String[]{"Record","Count","Percentage"}) ;
		ReportTable rt_dist = new ReportTable(new String[]{"Less or Equal","Count","Percentage"}) ;
		ReportTable rt_patt = new ReportTable(new String[]{"Pattern","Count","Percentage"}) ;
		
		tb.addTab("Record",null,rt_rec,"Record");
		tb.addTab("Distribution",null,rt_dist,"Distribution");
		tb.addTab("Pattern",null,rt_patt,"Pattern");
		
		/* Fill record count */
		rt_rec.addFillRow(new String[] { "Total", ((Double)_ad[0]).toString(), "100.00" });
		if (_ad[0] <= 0.0D) return tb; // zero count no analysis
		
		rt_rec.addFillRow(new String[] { "Unique",((Double)_ad[1]).toString(), ((Double)((_ad[1]/_ad[0])*100)).toString() });
		rt_rec.addFillRow(new String[] { "Repeat", ((Double)_ad[2]).toString(), ((Double)((_ad[2]/_ad[0])*100)).toString() });
		rt_rec.addFillRow(new String[] { "Pattern",((Double)_ad[3]).toString(), ((Double)((_ad[3]/_ad[0])*100)).toString() });
		rt_rec.addFillRow(new String[] { "Null",((Double)_ad[4]).toString(), ((Double)((_ad[4]/_ad[0])*100)).toString() });
		
		
		/* Fill Distribution Count */
		for (int i =0; i < _avector1[0].size(); i++) {
			rt_dist.addFillRow(new String[] {  _avector1[0].get(i).toString(),
					_avector1[1].get(i).toString() ,((Double)(((Double)(_avector1[1].get(i))/_ad[0])*100)).toString() });
		}
		
		/* Fill Pattern count */
		for (int i =0; i < _avector[0].size(); i++) {
			rt_patt.addFillRow(new String[] {  _avector[0].get(i).toString(),
					_avector[1].get(i).toString() ,((Double)(((Double)(_avector[1].get(i))/_ad[0])*100)).toString() });
		}
		

		
		
		return tb;
	}

}
