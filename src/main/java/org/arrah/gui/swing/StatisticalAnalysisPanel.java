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
 * This file is used to analyze data from File
 */

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.util.Vector;


import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.arrah.framework.profile.StatisticalAnalysis;



public class StatisticalAnalysisPanel {

	private ReportTable freq_t,range_t,perc_t ;
	private StatisticalAnalysis sa;
	private Object[] _colObj;
	private boolean isNumber = false;

	public StatisticalAnalysisPanel(Object[] colValue) {
		 sa = new StatisticalAnalysis(colValue);
	}

	
	private class FileScatter extends ScatterPlotterPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FileScatter(boolean isDate) {
			super(isDate);
		};

		public Vector<Double> fillValues() {

			final int gc = getGC();
			if (gc == 0)
				return null;

			int counter = 0;
			double d = 0;
			double sum = 0;
			Vector<Double> vc = new Vector<Double>(20, 5);
			int i = 0;
			_colObj = sa.getColObject();
			
			int colC = _colObj.length;
			try {
				for (int c = 0; c < colC; c++) {

					String colV_s = _colObj[c].toString();
					if (colV_s.equals(""))
						continue;
					d = Double.valueOf(colV_s).doubleValue();
					counter++;
					if (counter <= gc) {
						sum += d;
						if (counter != gc)
							continue;
					}
					double avg = sum / counter;
					sum = 0;
					counter = 0;
					vc.add(i++, new Double(avg));
				}
			} catch (NumberFormatException e) {
				counter = 0;
				ConsoleFrame
						.addText("\n ERROR: Could not fill data into Cluster Chart");
				JOptionPane.showMessageDialog(null, e.getMessage(),
						"Error Message", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			// After loop breaks
			// Rounding off the values
			if (counter != 0 && Math.round((float) counter / gc) > 0) {
				double avg = sum / counter;
				vc.add(i, new Double(avg));
			}

			return vc;
		}
	}

	public ReportTable getFrequencyTable() {
		return freq_t;
	}

	public ReportTable getRangeTable() {
		return range_t;
	}

	public ReportTable getPercTable() {
		return perc_t;
	}

	
	public void createAndShowGUI() {

		final FileScatter spp = new FileScatter(false);
		final JTabbedPane _ta_p = new JTabbedPane() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				super.paint(g);
				if (this.getSelectedIndex() != 3)
					return;
				spp.setSlideBar();
			}
		};
		freq_t = new ReportTable(sa.getFrequencyTable());
		range_t = new ReportTable(sa.getRangeTable());
		
		_ta_p.addTab("Frequency Analysis", null, freq_t, "Frequency Analyis");
		_ta_p.addTab("Variation Analysis", null, range_t, "Variation Analysis");

		isNumber = sa.isObjNumber();
		if (isNumber == true) {
			perc_t =  new ReportTable(sa.getPercTable());
			_ta_p.addTab("Percentile Analysis", null, perc_t,"Percentile Analysis");
			_ta_p.addTab("Cluster Analysis", null, spp, "Cluster Analysis");
		}

		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		final JFrame frame = new JFrame("Advance Number Analyis") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				super.paint(g);
				if (_ta_p.getSelectedIndex() != 3)
					return;
				spp.g_p.showBubbleChart();
				spp.setSlideBar();
			}
		};
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create and set up the content pane.
		frame.getContentPane().add(_ta_p, BorderLayout.CENTER);

		// Display the window.
		frame.setLocation(125, 75);
		frame.pack();
		frame.setVisible(true);
	}
}
