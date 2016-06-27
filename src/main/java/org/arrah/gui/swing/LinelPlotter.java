package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2007      *
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
 * This file is used to create  Line Graph as
 * Methods are provided get and set data.
 *
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;

public class LinelPlotter extends PlotterPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LinelPlotter() {
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawLineChart(g);
	}

	private void drawgridForLine(Graphics graphics) {
		drawgridForHistogram(graphics);
	}

	private void showYLabels(Graphics graphics) {
		Font font = new Font("Helvetica", Font.PLAIN, 14);
		graphics.setFont(font);
		graphics.setColor(Color.RED);
		graphics.drawString(title, 52, 12);
	}

	private void drawXLine(Graphics graphics) {
		int width = getWidth();
		int height = getHeight();

		int margin = 25;
		int prevX = margin;

		Graphics2D g2 = (Graphics2D) graphics;
		g2.setColor(Color.red);
		g2.setStroke(new BasicStroke(2));

		int xpoints = scaledXValues.length;
		if (xpoints == 0)
			return;
		int prevY = height - scaledXValues[0];

		for (int i = 0; i < xpoints; i++) {
			int sy = height - scaledXValues[i];
			int sx = ((width - 25) / xpoints) * i + ((width - 25) / xpoints)
					/ 2 + margin;
			g2.drawLine(prevX, prevY, sx, sy);
			prevY = sy;
			prevX = sx;
		}
		g2.drawLine(prevX, prevY, width - margin, prevY);
	}

	public void drawLineChart() {
		if (init == false)
			return;
		Graphics graphics = getGraphics();
		if (graphics == null) {
			ConsoleFrame.addText("\n Information: Graphics not in view");
			return;
		}
		drawBarChart(graphics);
	}

	public void drawLineChart(Graphics graphics) {
		if (init == false)
			return;
		if (graphics == null) {
			ConsoleFrame.addText("\n Information: Graphics not in view");
			return;

		}
		if (XLabel.length == 0)
			return;
		drawgridForLine(graphics);
		scaleValuesUniformly();
		showXLabels(graphics);
		showYLabels(graphics);
		drawXLine(graphics);
		showDataLabels(graphics);

	}
}
