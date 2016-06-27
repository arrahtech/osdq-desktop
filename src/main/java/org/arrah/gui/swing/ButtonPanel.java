package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2006      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for creating button in Bin chart
 *
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ButtonPanel extends JPanel implements ActionListener

{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel buttonPanel;
	private PlotterPanel plotterPanel;
	private PlotterDataPanel plotterDataPanel;
	private boolean hist = false, plot = false;

	public ButtonPanel() {
		JButton histButton = new JButton("Bar Chart");
		JButton zoomInButton = new JButton("Zoom In");
		JButton zoomOutButton = new JButton("Zoom Out");
		JButton resetButton = new JButton("Reset");
		JButton exitButton = new JButton("Exit");

		buttonPanel = new JPanel();

		histButton.addActionListener(this);
		histButton.addKeyListener(new KeyBoardListener());
		zoomInButton.addActionListener(this);
		zoomInButton.addKeyListener(new KeyBoardListener());
		zoomOutButton.addActionListener(this);
		zoomOutButton.addKeyListener(new KeyBoardListener());
		exitButton.addActionListener(this);
		exitButton.addKeyListener(new KeyBoardListener());
		resetButton.addActionListener(this);
		resetButton.addKeyListener(new KeyBoardListener());

		buttonPanel.add(histButton);
		buttonPanel.add(zoomInButton);
		buttonPanel.add(zoomOutButton);
		buttonPanel.add(resetButton);
		// buttonPanel.add(exitButton); No Exit for time being
	}

	public JPanel getPanel() {
		return buttonPanel;
	}

	public void setPlotterPanel(PlotterPanel p) {
		plotterPanel = p;
	}

	public void setPlotterDataPanel(PlotterDataPanel p) {
		plotterDataPanel = p;
	}

	public void actionPerformed(ActionEvent event) {
		String click_val = ((JButton) event.getSource()).getText();
		if (click_val.equalsIgnoreCase("Exit")) {
			System.exit(0);
		} else if (click_val.equalsIgnoreCase("Bar Chart")) {
			plotterPanel.setZoomFactor(1);
			try { // Setting the cursor

				buttonPanel
						.getTopLevelAncestor()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				plotterDataPanel.fillXValues();
				plotterPanel.setXLabel(plotterDataPanel.getNValues());
				plotterPanel.setxValues(plotterDataPanel.getXValues());
				plotterPanel.setColorIndex(plotterDataPanel.getColorIndex());
				plotterPanel.setBoundry(plotterDataPanel.getBboundary());
				plotterPanel.setInit();
				plotterPanel.drawBarChart();
			} catch (SQLException e) {
				plotterPanel.drawBarChart();
			} finally {

				buttonPanel
						.getTopLevelAncestor()
						.setCursor(
								java.awt.Cursor
										.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			}
			hist = true;
			plot = false;
		} else if (click_val.equalsIgnoreCase("Zoom In")) {
			plotterPanel.setxValues(plotterDataPanel.getXValues());
			plotterPanel.setZoomFactor(plotterPanel.getZoomFactor() / 1.5);
			plotterPanel.drawBarChart();
		} else if (click_val.equalsIgnoreCase("Zoom Out")) {
			plotterPanel.setxValues(plotterDataPanel.getXValues());
			plotterPanel.setZoomFactor(plotterPanel.getZoomFactor() * 1.5);
			plotterPanel.drawBarChart();
		} else if (click_val.equalsIgnoreCase("Reset")) {
			plotterPanel.setxValues(plotterDataPanel.getXValues());
			plotterPanel.setZoomFactor(1);
			plotterPanel.drawBarChart();
		}

	}

}
