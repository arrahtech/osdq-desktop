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
 * This class is used for creating  Compare
 * Table graphics
 *
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

public class UniversePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	private boolean init;
	private String[] st;
	private long[] lg;
	private boolean isLeft; // Left and right are done differently

	public UniversePanel() {
		title = "Universe";
		init = false;
		isLeft = false;
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	protected void paintComponent(Graphics g) {
		drawUniverseChart(g);
	}

	public void setTitle(String s) {
		title = s;
	}

	public void setValues(String[] s) {
		st = s;
	}

	private void drawUniverseChart(Graphics g) {
		if (g == null || init == false)
			return;

		int i = getHeight();
		int j = getWidth();
		g.setColor(getBackground());
		g.fillRect(0, 0, j, i);

		byte by0 = 18;
		byte by1 = 20;
		byte fs = 14;
		byte gp = 10;

		g.setColor(Color.BLACK);
		Font font = new Font("Helvetika", 0, fs);
		g.setFont(font);
		g.drawString(title, 0, fs + 1);

		long ct = lg[1] + lg[2];
		if (ct > 0L) {
			int len1 = (int) (lg[0] * (i - 1) / ct);
			int len2 = 0;
			int len3 = 0;
			if (isLeft)
				len2 = (int) ((lg[2] - lg[0]) * (i - 1) / ct);
			else
				len2 = (int) ((ct - lg[0]) * (i - 1) / ct);

			// Scaling to show Values
			if (lg[1] > 0L && isLeft) {
				g.setColor(new Color(231, 139, 75));
				len3 = (int) (lg[1] * (i - 1) / ct);
				g.fillRect(0, i - len3, j, len3 + 1);
			}
			if (lg[2] > 0L) {
				g.setColor(new Color(255, 0, 0));
				if (isLeft)
					g.fillRect(0, i - len3 - len2 - 1, j, len2 + 1);
				else
					g.fillRect(0, i - len2 - 1, j, len2 + 1);
			}
			if (lg[0] > 0L) {
				g.setColor(new Color(0, 255, 0));
				if (isLeft)
					g.fillRect(0, i - len3 - len2 - len1 - 1, j, len1 + 1);
				else
					g.fillRect(0, i - len2 - len1 - 1, j, len1 + 1);
			}
		}
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, j - 1, i - 1);
	}

	public static long[] convertLong(String[] st) {
		long lg[] = new long[st.length];
		for (int i = 0; i < st.length; i++) {
			if (st[i] == null) {
				lg[i] = 0;
				continue;
			}
			try {
				lg[i] = Math.round(Double.parseDouble(st[i]));
			} catch (NumberFormatException e) {
				lg[i] = 0;
				ConsoleFrame.addText("\n Parsing Exception:" + st[i]);
			}
		}
		return lg;

	}

	public void setLeft(boolean left) {
		isLeft = left;
	}

	public void drawUniverseChart() {
		init = true;
		Graphics g = getGraphics();
		lg = convertLong(st);
		drawUniverseChart(g);
	}

	public String getToolTipText(MouseEvent mouseevent) {
		if (!init)
			return null;
		String tip = "<html>";
		if (isLeft) {
			tip += " Unique Record Match(" + String.valueOf(lg[0]) + ")<BR>";
			tip += " Unique Record NO Match(" + String.valueOf(lg[2] - lg[0]) + ")<BR>";
			tip += "Null(" + String.valueOf(lg[1]) + ")<BR>";
		}
		else {
			tip += "Match(" + String.valueOf(lg[0]) + ")<BR>";
			tip += "NO Match(" + String.valueOf(lg[2] - lg[0]) + ")<BR>";
			tip += "Null(" + String.valueOf(lg[1]) + ")<BR>";
		}
			
		return tip += "</html>";
	}
}
