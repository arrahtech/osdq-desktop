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
 * This class defines the framework for vertical
 * barChart and cluster chart 
 *
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import org.arrah.framework.rdbms.Rdbms_conn;

public class PlotterPanel extends JPanel implements MouseListener,
		ActionListener, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double xValues[];
	public int scaledXValues[];
	public String XLabel[];
	public double values[];
	public double zoomFactor;
	public double scale;
	public boolean init;
	public boolean bubble_init;
	public boolean slide_init;
	public Vector<java.awt.geom.Ellipse2D.Float> v_shape;
	public int color_index;
	public int gc;
	public double s_min;
	public double s_max;
	public int lo_i;
	public int hi_i;
	public String title;
	private transient java.awt.geom.Ellipse2D.Float s;
	public Vector<Double> vc;
	private boolean isDateType = false;
	private class MyEllipse extends java.awt.geom.Ellipse2D.Float implements
			Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MyEllipse(float f, float f1, float f2, float f3) {
			super(f, f1, f2, f3);
		}
	}

	public void setInit() {
		init = true;
	}

	public boolean getInit() {
		return init;
	}

	public void setBInit(Vector<Double> vector) {
		vc = vector;
		bubble_init = true;
	}

	public boolean getBInit() {
		return bubble_init;
	}

	public boolean getSInit() {
		return slide_init;
	}

	public void setSInit(double d, double d1) {
		s_min = d;
		s_max = d1;
		slide_init = true;
	}

	public void setGC(int i) {
		gc = i;
	}

	public int getGC() {
		return gc;
	}

	public double getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(double d) {
		zoomFactor = d;
	}

	public double[] getxValues() {
		return xValues;
	}

	public void setxValues(double ad[]) {
		xValues = new double[ad.length];
		scaledXValues = new int[ad.length];
		for (int i = 0; i < ad.length; i++)
			xValues[i] = ad[i];

	}

	public void setxValues(Object aobj[]) {
		xValues = new double[aobj.length];
		scaledXValues = new int[aobj.length];
		for (int i = 0; i < aobj.length; i++)
			xValues[i] = ((Number) aobj[i]).doubleValue();

	}

	public void setBoundry(double ad[]) {
		values = ad;
	}

	public double[] getBoundry() {
		return values;
	}

	public PlotterPanel() {
		zoomFactor = 1.0D;
		scale = 1.0D;
		init = false;
		bubble_init = false;
		slide_init = false;
		color_index = 0;
		gc = 0;
		s_min = 0.0D;
		s_max = 0.0D;
		lo_i = 0;
		hi_i = 0;
		title = "Bin Chart";
		ToolTipManager.sharedInstance().registerComponent(this);
		addMouseListener(this);
		addMouseListener(new PopupListener());
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawBarChart(g);
		showBubbleChart(g);
	}

	public void setXLabel(String as[]) {
		XLabel = as;
	}

	public void setXLabel(Object aobj[]) {
		XLabel = new String[aobj.length];
		for (int i = 0; i < aobj.length; i++)
			XLabel[i] = aobj[i].toString();

	}

	public void setTitle(String s1) {
		title = s1;
	}

	public String[] getXLabel() {
		return XLabel;
	}

	private Color getColor(int i) {
		switch (i) {
		case 0: // '\0'
			return Color.red;

		case 1: // '\001'
			return Color.green;

		case 2: // '\002'
			return Color.yellow;

		case 3: // '\003'
			return Color.blue;
		}
		return Color.black;
	}

	public Paint getPaint(int i) {
		switch (i) {
		case 0: // '\0'
			return new GradientPaint(0.0F, 0.0F, new Color(255, 0, 0), 0.0F,
					getHeight() / 2, new Color(255, 200, 200), true);

		case 1: // '\001'
			return new GradientPaint(0.0F, 0.0F, new Color(0, 255, 0), 0.0F,
					getHeight() / 2, new Color(200, 255, 200), true);

		case 2: // '\002'
			return new GradientPaint(0.0F, 0.0F, new Color(255, 255, 0), 0.0F,
					getHeight() / 2, new Color(255, 255, 200), true);

		case 3: // '\003'
			return new GradientPaint(0.0F, 0.0F, new Color(0, 0, 255), 0.0F,
					getHeight() / 2, new Color(200, 200, 255), true);
		}
		return Color.black;
	}

	public void setColorIndex(int i) {
		color_index = i;
	}

	private void drawgridForBubble(Graphics g) {
		int i = getWidth();
		int j = getHeight();
		byte byte0 = 25;
		g.setColor(getBackground());
		g.fillRect(0, 0, i, j);
		g.setColor(Color.black);
		g.drawRect(byte0, 0, i - 2 * byte0, j - byte0);
	}

	protected void drawgridForHistogram(Graphics g) {
		int i = getWidth();
		int j = getHeight();
		byte byte0 = 25;
		g.setColor(getBackground());
		g.fillRect(0, 0, i, j);
		g.setColor(Color.black);
		g.drawRect(byte0, 0, i - 2 * byte0, j - byte0);
		if (XLabel == null)
			return;
		int k = XLabel.length;
		if (k == 0)
			return;
		byte byte1 = 10;
		int l = (i - 2 * byte0) / k;
		int i1 = (j - 2 * byte0) / byte1;
		g.setColor(Color.gray);
		for (int j1 = 1; j1 <= k; j1++) {
			int l1 = byte0 + j1 * l;
			g.drawLine(l1, 0, l1, j - byte0);
		}

		for (int k1 = 1; k1 <= byte1; k1++) {
			int i2 = k1 * i1;
			g.drawLine(byte0, i2, i - byte0, i2);
		}

	}

	protected void showDataLabels(Graphics g) {
		int i = getWidth();
		byte byte0 = 25;
		int j = getHeight();
		int k = scaledXValues.length;
		if (k == 0)
			return;
		int l = (i - 2 * byte0) / k;
		g.setColor(Color.blue);
		for (int i1 = 0; i1 < k; i1++) {
			int j1 = byte0 + i1 * l + l / 4;
			int k1 = j - scaledXValues[i1] - 2;
			if (xValues[i1] < 0.0D)
				g.drawString(" N/A", j1, j - 25);
			else
				g.drawString(String.valueOf(Math.round(xValues[i1])),
						(j1 + l / 4)
								- (String.valueOf(Math.round(xValues[i1]))
										.length() * 4), k1);
		}

	}

	protected void showXLabels(Graphics g) {
		int i = getWidth();
		int j = getHeight();
		byte byte0 = 25;
		int i1 = j - 5;
		int j1 = XLabel.length;
		if (j1 == 0)
			return;
		int k1 = (i - 2 * byte0) / j1;
		g.setColor(Color.black);
		Font font = new Font("Helvetika", 0, 14);
		FontMetrics fm = getFontMetrics(font);
		g.setFont(font);
		for (int l1 = 0; l1 < j1; l1++) {
			int l = byte0 + l1 * k1;
			if (fm.stringWidth(XLabel[l1]) < (k1 - 10))
				g.drawString(XLabel[l1], l + 10, i1);
			else // String width is more
			{
				int len = XLabel[l1].length();
				String minS = "..";
				String newV = minS;
				int min = fm.stringWidth(minS);
				int strI = 0;
				while (min + fm.charWidth(XLabel[l1].charAt(strI)) < (k1 - 10)) {
					newV = XLabel[l1].substring(0, ++strI) + minS;
					min = fm.stringWidth(newV);
					if (strI >= len)
						break;
				}
				g.drawString(newV, l + 10, i1);
			}
		}

	}

	private void showYLabels(Graphics g) {
		Font font = new Font("Helvetika", 0, 14);
		g.setFont(font);
		g.setColor(Color.RED);
		g.drawString(title, 52, 12);
	}

	public void scaleValuesUniformly() {
		double d = max(xValues);
		double d1 = min(xValues);
		double d2 = 0.0D;
		int i = getHeight();
		d2 = d / (double) (getHeight() - 50);
		scale = d2 * zoomFactor;
		for (int j = 0; j < scaledXValues.length; j++)
			scaledXValues[j] = scale == 0.0D ? (int) (25D + xValues[j])
					: (int) (25D + xValues[j] / scale);
	}

	public void scaleSlideBubble(double d, double d1) {
		double d2 = 0.0D;
		d2 = d1 - d == 0.0D ? 1.0D : (d1 - d) / (double) (getWidth() - 75);
		scale = d2;
	}

	public void scaleBubble(Vector<Double> vector) {
		double d = ((Double) vector.elementAt(0)).doubleValue();
		double d1 = ((Double) vector.elementAt(vector.size() - 1))
				.doubleValue();
		double d2 = 0.0D;
		d2 = d1 - d == 0.0D ? 1.0D : (d1 - d) / (double) (getWidth() - 75);
		scale = d2;
	}

	private void drawSlideBubble(double d, double d1, Graphics g) {
		int i = getWidth();
		int j = getHeight();
		byte byte0 = 25;
		int k = vc.size();
		int l = 1;
		float f = 0.0F;
		double d2 = 0.0D;
		float f2 = (float) ((double) ((j - byte0) / 25) / zoomFactor);
		g.setColor(getColor(color_index));
		lo_i = 0;
		hi_i = k - 1;
		int i1 = 0;
		do {
			if (i1 >= k)
				break;
			if (d <= ((Double) vc.elementAt(i1)).doubleValue()) {
				lo_i = i1;
				break;
			}
			i1++;
		} while (true);
		i1 = 0;
		do {
			if (i1 >= k)
				break;
			if (d1 < ((Double) vc.elementAt(i1)).doubleValue()) {
				hi_i = i1;
				break;
			}
			i1++;
		} while (true);
		for (int j1 = lo_i; j1 <= hi_i; j1++) {
			double d3 = (double) (byte0 + 10)
					+ (((Double) vc.elementAt(j1)).doubleValue() - d) / scale;
			float f1;
			if (d3 > d2 + 6D) {
				f1 = j - (byte0 + 6);
				l = 1;
				d2 = d3;
			} else {
				f1 = (float) j - ((float) (byte0 + 6) + (float) (l++) * f2);
			}
			int k1 = (new Double(d3)).intValue();
			int l1 = (new Float(f1)).intValue();
			g.fillOval(k1, l1, 6, 6);
			s = new MyEllipse(k1, l1, 6F, 6F);
			v_shape.removeElementAt(j1);
			v_shape.add(j1, s);
		}

	}

	private void drawBubble(Vector<Double> vector, Graphics g) {
		double d = ((Double) vector.elementAt(0)).doubleValue();
		int i = getWidth();
		int j = getHeight();
		byte byte0 = 25;
		boolean flag = false;
		boolean flag1 = false;
		int k = 1;
		int l = vector.size();
		float f = 0.0F;
		double d1 = 0.0D;
		double d2 = 0.0D;
		float f2 = (float) ((double) ((j - byte0) / 25) / zoomFactor);
		g.setColor(getColor(color_index));
		v_shape = new Vector<java.awt.geom.Ellipse2D.Float>(20, 5);
		for (int i1 = 0; i1 < l; i1++) {
			double d3 = (double) (byte0 + 10)
					+ (((Double) vector.elementAt(i1)).doubleValue() - d)
					/ scale;
			float f1;
			if (d3 > d1 + 6D) {
				f1 = j - (byte0 + 6);
				k = 1;
				d1 = d3;
			} else {
				f1 = (float) j - ((float) (byte0 + 6) + (float) (k++) * f2);
			}
			int j1 = (new Double(d3)).intValue();
			int k1 = (new Float(f1)).intValue();
			g.fillOval(j1, k1, 6, 6);
			s = new MyEllipse(j1, k1, 6F, 6F);
			v_shape.add(i1, s);
		}

	}

	private void drawXBars(Graphics g) {
		int i = getWidth();
		int j = getHeight();
		byte byte0 = 25;
		int k = scaledXValues.length;
		if (k == 0)
			return;
		int l = (i - 2 * byte0) / k;
		int i1 = j - byte0;
		((Graphics2D) g).setPaint(getPaint(color_index));
		for (int j1 = 0; j1 < k; j1++) {
			int k1 = byte0 + j1 * l + l / 4;
			int l1 = j - scaledXValues[j1];
			int i2 = i1;
			g.fillRect(k1, l1, l / 2, i2 - l1);
		}

	}

	public static int max(int ai[]) {
		if (ai == null || ai.length == 0)
			return 0;
		int i = ai[0];
		for (int j = 0; j < ai.length; j++)
			if (ai[j] > i)
				i = ai[j];

		return i;
	}

	public static double max(double ad[]) {
		if (ad == null || ad.length == 0)
			return 0.0D;
		double d = ad[0];
		for (int i = 0; i < ad.length; i++)
			if (ad[i] > d)
				d = ad[i];

		return d;
	}

	public static int min(int ai[]) {
		if (ai == null || ai.length == 0)
			return 0;
		int i = ai[0];
		for (int j = 0; j < ai.length; j++)
			if (ai[j] < i)
				i = ai[j];

		return i;
	}

	public static double min(double ad[]) {
		if (ad == null || ad.length == 0)
			return 0.0D;
		double d = ad[0];
		for (int i = 0; i < ad.length; i++)
			if (ad[i] < d)
				d = ad[i];

		return d;
	}

	public void drawBarChart() {
		if (!init)
			return;
		Graphics g = getGraphics();
		if (g == null) {
			ConsoleFrame.addText("\n Information: Graphics not in view");
			return;
		} else {
			drawBarChart(g);
			return;
		}
	}

	public void drawBarChart(Graphics g) {
		if (!init)
			return;
		if (g == null) {
			ConsoleFrame.addText("\n Information: Graphics not in view");
			return;
		} else {
			drawgridForHistogram(g);
			scaleValuesUniformly();
			showXLabels(g);
			showYLabels(g);
			drawXBars(g);
			showDataLabels(g);
			return;
		}
	}

	public void showBubbleChart() {
		Graphics g = getGraphics();
		showBubbleChart(g);
	}

	
	// This is first chart that uses same slider information
	public void showBubbleChart(Graphics g) {
		if (!bubble_init) {
			return;
		} else {
			slide_init = false;
			drawgridForBubble(g);
			scaleBubble(vc);
			drawBubble(vc, g);
			return;
		}
	}

	// Once the slider state is changed this function is used
	public void showSlideBubbleChart() {
		if (!bubble_init)
			return;
		if (!slide_init) {
			return;
		} else {
			Graphics g = getGraphics();
			drawgridForBubble(g);
			scaleSlideBubble(s_min, s_max);
			drawSlideBubble(s_min, s_max, g);
			return;
		}
	}

	public String getToolTipText(MouseEvent mouseevent) {
		int i = mouseevent.getX();
		int j = mouseevent.getY();
		int k = getWidth();
		int l = getHeight();
		byte byte0 = 25;
		int i1 = -1;
		double d = 0.0D;
		double d1 = 0.0D;
		if (XLabel == null)
			return null;
		if (XLabel.length == 0)
			return null;
		for (int j1 = 0; j1 < XLabel.length; j1++)
			d += xValues[j1];

		if (d == 0.0D)
			return null;
		int k1 = (k - 2 * byte0) / XLabel.length;
		int l1 = 0;
		do {
			if (l1 >= xValues.length)
				break;
			d1 += xValues[l1];
			if (i >= byte0 + l1 * k1 && i < byte0 + (l1 + 1) * k1) {
				i1 = l1;
				break;
			}
			l1++;
		} while (true);
		if (i1 == -1) {
			return null;
		} else {
			String s1 = "<html><B><I>";
			String s2 = Long.toString(Math.round(xValues[i1]));
			s1 = s1 + "Bin Count=  " + s2 + "  Total Count= " + d + "<BR>";
			String s3 = Double.toString((xValues[i1] * 100D) / d);
			s1 = s1 + "Bin %age=  " + s3 + "%  ";
			String s4 = Double.toString((d1 * 100D) / d);
			s1 = s1 + "Cumulative %age=  " + s4 + "% </I></B><html>";
			return s1;
		}
	}

	public void mouseClicked(MouseEvent mouseevent) {
		if (!bubble_init)
			return;
		int i = v_shape.size();
		int j = mouseevent.getX();
		int k = mouseevent.getY();
		int l = -1;
		int i1 = 0;
		Graphics g = getGraphics();
		if (slide_init) {
			showSlideBubbleChart();
			int j1 = lo_i;
			do {
				if (j1 > hi_i)
					break;
				if (((Shape) v_shape.elementAt(j1)).contains(j, k)) {
					l = j1;
					break;
				}
				j1++;
			} while (true);
		} else {
			showBubbleChart();
			int k1 = 0;
			do {
				if (k1 >= i)
					break;
				if (((Shape) v_shape.elementAt(k1)).contains(j, k)) {
					l = k1;
					break;
				}
				k1++;
			} while (true);
		}
		if (l == -1)
			return;
		int l1 = getWidth();
		int i2 = getHeight();
		if (j <= l1 / 2)
			i1 = l1 / 2 + 50;
		else
			i1 = 100;
		Font font = new Font("Helvetika", 0, 12);
		g.setFont(font);
		FontMetrics fontmetrics = getFontMetrics(font);
		String s1 = "Group ID: " + Integer.toString(l + 1);
		String s2 = "Group Avg: ";
		if (isDateType == true) {

		     String format = Rdbms_conn.getHValue("DateFormat");
		     if (format == null || "".equals(format))
	        		format =  "dd-MMM-YYYY";
		     SimpleDateFormat df = new SimpleDateFormat(format);
			 java.util.Date value = new java.util.Date(vc.elementAt(l).longValue());
			  s2 = s2 + df.format(value);
		}
		else
			s2 = s2	+ Double.toString(((Double) vc.elementAt(l)).doubleValue());
		
		String s3 = "Groups Abv: " + Integer.toString(i - (l + 1));
		int j2 = fontmetrics.stringWidth(s1);
		int k2 = fontmetrics.stringWidth(s2);
		int l2 = fontmetrics.stringWidth(s3);
		int i3 = 0;
		if (l2 > j2)
			j2 = l2;
		if (j2 > k2)
			i3 = j2 + 8;
		else
			i3 = k2 + 8;
		g.drawLine(j, k, i1, i2 / 2);
		g.clearRect(i1 - 4, i2 / 2 - 15, i3, 50);
		g.drawRect(i1 - 4, i2 / 2 - 15, i3, 50);
		g.drawString(s1, i1, i2 / 2);
		g.drawString(s2, i1, i2 / 2 + 14);
		g.drawString(s3, i1, i2 / 2 + 28);
	}

	public void mousePressed(MouseEvent mouseevent) {
	}

	public void mouseReleased(MouseEvent mouseevent) {
	}

	public void mouseEntered(MouseEvent mouseevent) {
	}

	public void mouseExited(MouseEvent mouseevent) {
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
				menuItem.addActionListener(PlotterPanel.this);
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

	public boolean isDateType() {
		return isDateType;
	}

	public void setDateType(boolean isDateType) {
		this.isDateType = isDateType;
	}
}
