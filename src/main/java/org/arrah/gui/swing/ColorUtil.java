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

/* This file is used for creating Color effects 
 *
 */
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

public class ColorUtil {

	public static void renderByLineGraphics(JComponent c, Graphics g) {

		int colorI = 0;
		for (int i = 0; i < c.getHeight(); i++) {
			GradientPaint gp = new GradientPaint(0, 0, new Color(200, 200,
					colorI), c.getWidth(), i, new Color(200, 200, colorI + 1),
					true);

			((Graphics2D) g).setPaint(gp);
			g.fillRect(0, i, c.getWidth(), i);
			if (colorI < 254)
				colorI++;
		}
	}

	public static void renderGraphics(JComponent c, Graphics g) {

		GradientPaint gp = new GradientPaint(0, 0, new Color(240, 240, 240), 0,
				c.getHeight() / 2, new Color(230, 230, 255), true);

		((Graphics2D) g).setPaint(gp);
		g.fillRect(0, 0, c.getWidth(), c.getHeight());
	}

}
