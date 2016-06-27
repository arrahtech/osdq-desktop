package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2015      *
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
 * This file is used for showing Number Profiling
 * Analytics in Graphic interface 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class ReportViewer extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PlotterDataPanel plotterDataPanel;

	public ScatterPlotterPanel spp; // For reporting
	public PlotterPanel plotterPanel; // For reporting
	public Hashtable<String, String> _map; // For reporting
	public JTextArea r_c; // For Reporting
	public JLabel time_l; // For Reporting
	public ProfilePane pp; // For Reporting

	public ReportViewer(Hashtable<String, String> map) {
		_map = map;
		r_c = new JTextArea("No Comments. ");
		createAndShowGUI();

	};

	public void createAndShowGUI() {

		SpringLayout layout = new SpringLayout();
		this.setLayout(layout);
		this.setPreferredSize(new Dimension(800, 1250));

		time_l = new JLabel("Profile Time: "
				+ new Date(System.currentTimeMillis()).toString());
		this.add(time_l);

		JButton ac = new JButton("Comment");
		ac.setActionCommand("comment");
		ac.addKeyListener(new KeyBoardListener());
		ac.addActionListener(this);
		this.add(ac);

		JButton sl = new JButton("Save Report");
		sl.setActionCommand("Save");
		sl.addKeyListener(new KeyBoardListener());
		sl.addActionListener(new FileActionListener(this, 1));
		this.add(sl);

		JButton sb = new JButton("Statistics");
		sb.setActionCommand("Stat");
		sb.addKeyListener(new KeyBoardListener());
		sb.addActionListener(new StatisticalAnalysisListener(_map));
		this.add(sb);

		Border line_b = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
				Color.YELLOW, Color.RED);
		// Add the output table here on the top
		JPanel tp = getTablePane();
		tp.setPreferredSize(new Dimension(700, 480));
		tp.setBorder(BorderFactory
				.createTitledBorder(line_b, "Number Profiler"));
		this.add(tp);

		// Add the Binning parameter Value here
		JPanel bp = getBinParamPane();
		bp.setPreferredSize(new Dimension(775, 350));
		bp.setBorder(BorderFactory.createTitledBorder(line_b, "Bin Analysis"));
		this.add(bp);

		// Add the Data grouping Pane here
		JPanel dg = getDGPane();
		dg.setBorder(BorderFactory.createTitledBorder(line_b,
				"Cluster Analysis"));
		dg.setPreferredSize(new Dimension(775, 350));
		this.add(dg);

		this.setOpaque(true);
		JScrollPane s_topPane = new JScrollPane(this);
		s_topPane.setPreferredSize(new Dimension(810, 650));

		layout.putConstraint(SpringLayout.WEST, time_l, 5, SpringLayout.WEST,
				this);
		layout.putConstraint(SpringLayout.NORTH, time_l, 5, SpringLayout.NORTH,
				this);
		layout.putConstraint(SpringLayout.WEST, ac, 5, SpringLayout.EAST,
				time_l);
		layout.putConstraint(SpringLayout.NORTH, ac, 5, SpringLayout.NORTH,
				this);
		layout.putConstraint(SpringLayout.WEST, sl, 5, SpringLayout.EAST, ac);
		layout.putConstraint(SpringLayout.NORTH, sl, 5, SpringLayout.NORTH,
				this);
		layout.putConstraint(SpringLayout.WEST, sb, 5, SpringLayout.EAST, sl);
		layout.putConstraint(SpringLayout.NORTH, sb, 5, SpringLayout.NORTH,
				this);

		layout.putConstraint(SpringLayout.WEST, bp, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, bp, 5, SpringLayout.SOUTH, sl);
		layout.putConstraint(SpringLayout.WEST, dg, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, dg, 5, SpringLayout.SOUTH, bp);
		layout.putConstraint(SpringLayout.WEST, tp, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, tp, 5, SpringLayout.SOUTH, dg);

	}

	private JPanel getBinParamPane() {
		JPanel binPane = new JPanel();
		binPane.setLayout(new BorderLayout());
		plotterDataPanel = new PlotterDataPanel();
		plotterDataPanel.init(_map);
		binPane.add(plotterDataPanel, BorderLayout.LINE_START);
		plotterPanel = new PlotterPanel();
		binPane.add(plotterPanel, BorderLayout.CENTER);
		ButtonPanel buttonPanel = new ButtonPanel();
		buttonPanel.setPlotterPanel(plotterPanel);
		buttonPanel.setPlotterDataPanel(plotterDataPanel);

		binPane.add(buttonPanel.getPanel(), BorderLayout.SOUTH);

		return binPane;
	}

	private JPanel getDGPane() {
		spp = new ScatterPlotterPanel(false);
		spp.init(_map);
		return spp;
	}

	private JPanel getTablePane() {
		pp = new ProfilePane(false, _map);
		return pp;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("comment".equals(command)) {
			r_c = new InputDialog(1).createTextPanel();
			return;
		}
	}

}
