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
 * This class is used for creating K Mean plot
 * using 3rd party open source jdmf
 *
 */


import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import net.sf.jdmf.algorithms.clustering.KMeansAlgorithm;
import net.sf.jdmf.data.input.attribute.Attribute;
import net.sf.jdmf.data.input.clustering.ClusteringInputData;
import net.sf.jdmf.data.output.clustering.Cluster;
import net.sf.jdmf.data.output.clustering.ClusteringDataMiningModel;
import net.sf.jdmf.visualization.clustering.ChartGenerator;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.gui.swing.ConsoleFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;


public class KMeanPanel extends JPanel implements  Serializable {
	private static final long serialVersionUID = 1L;

	private ClusteringInputData inputData;

	public KMeanPanel(String titleName, String xName, String yName) {

	}

	public ClusteringInputData addRTMDataSet(ReportTableModel rtm, Vector<?> colname) throws Exception {
		inputData = new ClusteringInputData();
		
		int rowC= rtm.getModel().getRowCount();
		
		for (int j=0; j <colname.size(); j++ )  {
			
			int index = rtm.getColumnIndex((String)colname.get(j));
			Attribute firstAttribute = new Attribute();
	        firstAttribute.setName((String)colname.get(j));

			for (int i=0; i < rowC; i++) {
				try {
					Object cell = rtm.getModel().getValueAt(i, index);
					if (cell instanceof Number) {
						firstAttribute.addValue(Double.parseDouble(cell.toString()));
					}
					else if (cell instanceof java.util.Date)
						firstAttribute.addValue((java.util.Date)cell);
					else
						firstAttribute.addValue((String)cell);
				} catch (Exception e) {
					ConsoleFrame.addText("\n Exception for row :" +i + "  Execption:"+e.getLocalizedMessage());
				}
			}
			inputData.addAttribute(firstAttribute);
		}
		return inputData;

	}
	// Create the K Mean Plot
	public void drawKMeanPlot( int noOfCluster, Vector<?>colname )  throws Exception {
		
	KMeansAlgorithm algorithm = new KMeansAlgorithm();
    // predicted number of clusters
    inputData.setNumberOfClusters( noOfCluster );

	// analyze input data and produce a model        
    ClusteringDataMiningModel dataMiningModel 
         = (ClusteringDataMiningModel) algorithm.analyze( inputData );

    ChartGenerator chartGenerator = new ChartGenerator();
        
    // visualize the clusters formed (2D only)
    List<Cluster> clusters = dataMiningModel.getClusters();
    JFreeChart xyChart;
    if (colname.size() > 1) // two columns selected
    xyChart = chartGenerator.generateXYChart( 
        clusters , 0, colname.get(0).toString(), 1, colname.get(1).toString() );
    else // only one column
    	xyChart = chartGenerator.generateXYChart( 
    	        clusters , 0, colname.get(0).toString(), 0, colname.get(0).toString() );
 // show the percentage of points falling into each cluster
    JFreeChart pieChart = chartGenerator.generatePieChart( clusters );
    
     final ChartPanel chartPanel = new ChartPanel(xyChart);
     chartPanel.setPreferredSize(new java.awt.Dimension(400, 450));
     
     final ChartPanel piechartPanel = new ChartPanel(pieChart);
     piechartPanel.setPreferredSize(new java.awt.Dimension(400, 300));
   
     this.add(chartPanel);
     this.add(piechartPanel);
	}


}
