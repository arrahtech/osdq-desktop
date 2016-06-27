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

/* 
 * This file is used to create  Horizontal Graph as
 * Methods are provided get and set data.
 *
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;

public class HorizontalPlotter extends PlotterPanel {
	private static final long serialVersionUID = 1L;
	private int pixelW = 20; // Draw width
	private int lmargin = 50; // Default Margin

	public HorizontalPlotter() {
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawBarChart(g);
	}

	protected void drawgridForHistogram(Graphics graphics) {
		int width = getWidth();
		int height = getHeight();
		int margin = 50;

		graphics.setColor(Color.black);
		graphics.drawRect(lmargin, margin / 2 + 1, width - lmargin - 5, height
				- margin);
	}

	protected void showDataLabels(Graphics graphics) {
		int height = getHeight();
		int xpoints = scaledXValues.length;
		int margin = 25;

		if (xpoints == 0)
			return;
		int dy = pixelW;
		graphics.setColor(Color.blue);

		for (int i = 0; i < xpoints; i++) {
			int y = height - (margin + i * dy) - 3;
			if (xValues[i] < 0)
				graphics.drawString(" N/A", 5, y);
			else
				graphics.drawString(String.valueOf(Math.round(xValues[i])),
						lmargin
								- 3
								- String.valueOf(Math.round(xValues[i]))
										.length() * 8, y);
		}
	}

	protected void showXLabels(Graphics graphics) {
		int height = getHeight();
		int margin = 50;

		int yPos = height - margin / 2 - 3;

		int xpoints = XLabel.length;
		if (xpoints == 0)
			return;
		int dy = pixelW;

		graphics.setColor(Color.black);
		Font font = new Font("Helvetica", Font.PLAIN, 14);
		graphics.setFont(font);
		graphics.drawString("Patterns", lmargin + 75, height - 10);

		for (int x = 0; x < xpoints; x++) {
			graphics.drawString(XLabel[x], lmargin + 5, yPos);
			yPos = yPos - dy;

		}
	}

	private void showYLabels(Graphics graphics) {
		Font font = new Font("Helvetica", Font.PLAIN, 14);
		graphics.setFont(font);
		graphics.setColor(Color.RED);
		graphics.drawString(title, lmargin + 25, 20);
	}

	public void scaleValuesUniformly() {
		double maxX = max(xValues);
		double xScale = 0;

		xScale = (double) (maxX / (getWidth() - lmargin - 5));
		scale = xScale * zoomFactor;

		for (int i = 0; i < scaledXValues.length; i++) {
			scaledXValues[i] = scale != 0 ? (int) (xValues[i] / scale)
					: (int) (xValues[i]);
		}
	}

	private void drawXBars(Graphics graphics) {

		int height = getHeight();
		int margin = 50;

		int xpoints = scaledXValues.length;
		if (xpoints == 0)
			return;
		int dy = pixelW;
		int ci = 0;
		Color[] ca = new Color[4];
		ca[0] = new Color(230, 166, 75);
		ca[1] = new Color(95, 188, 154);
		ca[2] = new Color(184, 217, 103);
		ca[3] = new Color(71, 200, 200);

		for (int i = 0; i < xpoints; i++) {
			int sx = scaledXValues[i]; // Line location is 2
			int sy = (height - margin + 5) - (i * dy);
			graphics.setColor(ca[ci++]);
			graphics.fillRect(lmargin, sy, sx, pixelW);
			graphics.setColor(Color.BLACK);
			graphics.drawRect(lmargin, sy, sx, pixelW);
			if (ci == 4)
				ci = 0;
		}
	}

	public void drawBarChart() {
		if (init == false)
			return;
		Graphics graphics = getGraphics();
		if (graphics == null) {
			ConsoleFrame.addText("\n Information: Graphics not in view");
			return;
		}
		drawBarChart(graphics);
	}

	public void drawBarChart(Graphics graphics) {
		if (init == false)
			return;
		if (graphics == null) {
			ConsoleFrame.addText("\n Information: Graphics not in view");
			return;

		}
		if (XLabel.length == 0)
			return;
		int width = getWidth();
		lmargin = (int) (width * 0.20);
		lmargin = (lmargin > 50) ? lmargin : 50;

		scaleValuesUniformly();
		showYLabels(graphics);
		drawXBars(graphics);
		drawgridForHistogram(graphics);
		showDataLabels(graphics);
		showXLabels(graphics);
	}

	public String getToolTipText(MouseEvent e) {
		return null;

	}

}
