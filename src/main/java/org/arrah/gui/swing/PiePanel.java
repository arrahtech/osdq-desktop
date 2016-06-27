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
 * This class is used for creating PieChart
 *
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class PiePanel extends JPanel implements ActionListener, Serializable {
	private static final long serialVersionUID = 1L;
	private String xLabel[];
	private double xValue[];
	private String title;

	public PiePanel() {
		title = "Distribution Chart";
		addMouseListener(new PopupListener());
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawPieChart(g);
	}

	public void setLabel(String as[]) {
		xLabel = as;
	}

	public void setLabel(Object aobj[]) {
		xLabel = new String[aobj.length];
		for (int i = 0; i < aobj.length; i++)
			xLabel[i] = aobj[i].toString();

	}

	public void setValue(double ad[]) {
		xValue = ad;
	}

	public void setValue(Object aobj[]) {
		xValue = new double[aobj.length];
		for (int i = 0; i < aobj.length; i++)
			xValue[i] = ((Number) aobj[i]).doubleValue();

	}

	public void setTitle(String s) {
		title = s;
	}

	private void drawPieChart(Graphics g) {
		if (g == null || xValue == null)
			return;
		double d = 0.0D;
		int i = getHeight();
		int j = getWidth();
		for (int k = 0; k < xValue.length; k++)
			d += xValue[k];

		int l = 0;
		Color acolor[] = new Color[6];
		acolor[0] = new Color(255, 255, 123);
		acolor[1] = new Color(231, 123, 255);
		acolor[2] = new Color(255, 139, 75);
		acolor[3] = new Color(0, 176, 255);
		acolor[4] = new Color(146, 236, 84);
		acolor[5] = new Color(255, 43, 97);
		double d1 = 0.0D;
		g.setColor(Color.RED);
		Font font = new Font("Helvetika", 0, 14);
		g.setFont(font);
		g.drawString(title, 5, 15);
		byte byte0 = 18;
		byte byte1 = 20;
		for (int j1 = 0; j1 < xValue.length; j1++) {
			int i1 = (int) ((d1 * 360D) / d);
			int k1 = (int) ((xValue[j1] * 360D) / d);
			if (j1 == xValue.length - 1)
				k1 = 360 - i1;
			g.setColor(acolor[l++]);
			g.fillArc(10, 30, j - 100, i / 2, i1, k1);
			g.fillRect(10, i / 2 + 35 + j1 * byte0, byte1, byte0);
			g.setColor(Color.BLACK);
			g.drawArc(10, 30, j - 100, i / 2, i1, k1);
			g.drawRect(10, i / 2 + 35 + j1 * byte0, byte1, byte0);
			g.drawString(" (" + Math.round(xValue[j1]) + ") <= " + xLabel[j1],
					30, i / 2 + 35 + 14 + j1 * byte0);
			d1 += xValue[j1];
			if (l == 6)
				l = 0;
		}

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
				menuItem.addActionListener(PiePanel.this);
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

	public void drawPieChart() {
		Graphics g = getGraphics();
		drawPieChart(g);
	}

}
