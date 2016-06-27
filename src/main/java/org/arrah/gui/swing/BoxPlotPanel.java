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

/*
 * This class is used for creating BoxPlot
 *
 */

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.arrah.framework.ndtable.ReportTableModel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;


public class BoxPlotPanel extends JPanel implements ActionListener, Serializable {
	private static final long serialVersionUID = 1L;
	private String xTitle, yTitle;
	private String title;
	private DefaultBoxAndWhiskerCategoryDataset dataset;

	public BoxPlotPanel(String titleName, String xName, String yName) {
		title = titleName;
		xTitle = xName;
		yTitle = yName;
		dataset = new DefaultBoxAndWhiskerCategoryDataset();
		addMouseListener(new PopupListener());
	}
	
	public void addDataSet(ArrayList<Object> list, String ser, String type) {
		dataset.add(list, ser, type);
	}

	public void addNumDataSet(ArrayList<Number> list, String ser, String type) {
		dataset.add(list, ser, type);
	}

	public void addDateDataSet(ArrayList<java.util.Date> list, String ser, String type) {
		dataset.add(list, ser, type);
	}
	
	public void addRTMDataSet(ReportTableModel rtm, String col1, String compCol) throws Exception {
		int rowC= rtm.getModel().getRowCount();
		ArrayList<Object> colVal = new ArrayList<Object> ();
		ArrayList<Object> comcolVal = new ArrayList<Object> ();
		int comIndex = -1; // nothing to compare
		int index = rtm.getColumnIndex(col1);
		if ("".equals(compCol) == false) // another col to compare
		comIndex = rtm.getColumnIndex(compCol);
		
		for (int i=0; i < rowC; i++) {
			try {
				Object cell = rtm.getModel().getValueAt(i, index);
				colVal.add(cell);
				if (comIndex != -1)  {// something to compare
					Object compcell = rtm.getModel().getValueAt(i, comIndex);
					comcolVal.add(compcell);
				}
				
			} catch (Exception e) {
				ConsoleFrame.addText("\n Exception for row :" +i + "  Execption:"+e.getLocalizedMessage());
			}
		}
		
		addDataSet(colVal, "", col1);
		if (comIndex != -1) // add compare field
		addDataSet(comcolVal,  "", compCol);
	}
	// Create the Box Plot
	public void drawBoxPlot() throws Exception {

    final CategoryAxis xAxis = new CategoryAxis(xTitle);
    final NumberAxis yAxis = new NumberAxis(yTitle);
    yAxis.setAutoRangeIncludesZero(false);
    final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
    renderer.setFillBox(false);
    renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
    final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

    final JFreeChart chart = new JFreeChart(title,
        new Font("SansSerif", Font.BOLD, 14),
        plot,
        true
    );
    final ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(250, 450));
    this.setLayout(new BorderLayout());
    this.add(chartPanel,BorderLayout.CENTER);
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
				JPopupMenu popup = new JPopupMenu();

				JMenuItem menuItem = new JMenuItem("Save as Image");
				menuItem.setActionCommand("saveimage");
				menuItem.addActionListener(BoxPlotPanel.this);
				popup.add(menuItem);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		// Save Image
		ImageUtil imgutil = new ImageUtil(this, "png");
		imgutil.removeWaring();
		return;
	}

}
