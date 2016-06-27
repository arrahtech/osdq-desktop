package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2012      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for showing relationship
 * tables. Tables with no primary key is shown
 * in one tab, other tab has table with no foreign
 * key and third tab has tables with relations.
 * Only MS Access has exceptions. In which tables
 * with primary key are shown in third tab.
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;

import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.rdbms.TableRelationInfo;

public class RelationPanel extends JPanel implements MouseMotionListener,
		MouseListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Hashtable <String, TableRelationInfo> noPk_noR;
	private Hashtable <String, TableRelationInfo>pk_noR;
	private Hashtable <String, TableRelationInfo>pk_Rel;
	private Vector<Rectangle> __v1 = null, __v2 = null, __v3 = null;
	private int __index = -1;
	private JTabbedPane tabbedPane;
	private int prevX = -1;
	int prevY = -1;
	private JComponent panel1, panel2, panel3;
	private Vector<Rel_panel> rel_vc = new Vector<Rel_panel>();
	private boolean isDragging = false;
	private boolean _isLeveled = false;
	private int _maxLevel = 0;

	public RelationPanel(Hashtable<String, TableRelationInfo> _t, Hashtable<String, TableRelationInfo> __t, 
				Hashtable <String, TableRelationInfo>___t) {
		super(new GridLayout(1, 1));
		noPk_noR = _t;
		pk_noR = __t;
		pk_Rel = ___t;

		makeTab();
		createAndShowGUI();

	}

	public RelationPanel(Hashtable<String, TableRelationInfo> ___t, int maxLevel) // Only for Related Panel
	{
		super(new GridLayout(1, 1));
		if (maxLevel > 0) {
			_isLeveled = true;
			_maxLevel = maxLevel;
		}
		pk_Rel = ___t;
		noPk_noR = new Hashtable<String, TableRelationInfo>();
		pk_noR = new Hashtable<String, TableRelationInfo>();

		makeTab();
		createAndShowGUI();

	}

	private void makeTab() {

		tabbedPane = new JTabbedPane();

		panel1 = makePanel(1, noPk_noR);
		tabbedPane.addTab("Tables with NO PK", null, panel1,
				"Tables without Primary Key");

		panel2 = makePanel(2, pk_noR);
		tabbedPane.addTab("Tables with NO FK", null, panel2,
				"Tables without Foreign Key");

		panel3 = makePanel(3, pk_Rel);
		tabbedPane.addTab("Table Model", null, panel3, "Tables Relationship");

		if (!(pk_Rel == null || pk_Rel.isEmpty() == true))
			tabbedPane.setSelectedIndex(2);
		else if (!(pk_noR == null || pk_noR.isEmpty() == true))
			tabbedPane.setSelectedIndex(1);
		else
			tabbedPane.setSelectedIndex(0);

		// Add the tabbed pane to this panel.
		add(tabbedPane);

	}

	private class Rel_panel extends JPanel implements Printable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int index;
		private Hashtable <String, TableRelationInfo> _h;
		private int rowh = 10;

		Rel_panel(final int i, final Hashtable <String, TableRelationInfo> _hash) {
			super(false);
			index = i;
			_h = _hash;
			repaint();
		}

		public void paint(Graphics g) {

			Object[] sorted = _h.keySet().toArray();
			Arrays.sort(sorted);
			int colh = 10 + 14;
			int count = 0;
			int maxw = 75; // default width
			int rowh = 10;
			int currentL = 0;

			Font font = new Font("Helvetika", Font.BOLD, 14);
			g.setFont(font);
			FontMetrics fm = getFontMetrics(font);

			switch (index) {
			case 1:
				if (__v1 == null)
					__v1 = new Vector<Rectangle>();
				break;
			case 2:
				if (__v2 == null)
					__v2 = new Vector<Rectangle>();
				break;
			case 3:
				if (__v3 == null)
					__v3 = new Vector<Rectangle>();
				break;
			}

			while (_maxLevel >= currentL) {
				for (int array_i = 0; array_i < sorted.length; array_i++) {
					int w = 0;
					int h = fm.getHeight();
					String t_n = (String) sorted[array_i];

					TableRelationInfo t_i = (TableRelationInfo) _h.get(t_n);
					if (t_i.level != currentL)
						continue;

					if (t_i.isShown == true) {
						Rectangle __r = null;
						if (index == 1)
							__r = (Rectangle) __v1.elementAt(t_i.r_i);
						if (index == 2)
							__r = (Rectangle) __v2.elementAt(t_i.r_i);
						if (index == 3)
							__r = (Rectangle) __v3.elementAt(t_i.r_i);

						int x = (int) __r.getLocation().getX();
						int y = (int) __r.getLocation().getY();
						rowh = x;
						colh = y + 14;
					}
					g.setColor(Color.BLACK); // Table color Black
					g.drawString(t_n, rowh, colh);
					w = fm.stringWidth(t_n);

					if (index != 1) {
						int pri__ = t_i.pk_c;
						int for__ = t_i.fk_c;
						g.setColor(Color.RED); // Primary Key RED
						for (int i = 0; i < pri__;) {
							w = fm.stringWidth(t_i.pk[i]) > w ? fm
									.stringWidth(t_i.pk[i]) : w;
							g.drawString(t_i.pk[i], rowh, colh + (++i * 14));
							h += 14;

						}
						if (index == 2) {
							g.setColor(new Color(100, 100, 100, 100));
							g.fillRect(rowh - 3, colh - 14, w + 3, 14 + 3); // table
																			// color
							g.setColor(new Color(0, 176, 255, 25));
							g.fillRect(rowh - 3, colh + 3, w + 3, h - 14); // some
																			// breathing
																			// space
							g.setColor(Color.BLACK);
							g.drawRect(rowh - 3, colh - 14, w + 3, h + 3); // some
																			// breathing
																			// space
							if (t_i.isShown == false) {
								Rectangle r__ = new Rectangle(rowh - 3,
										colh - 14, w += 10, h += 10);
								__v2.add(count, r__);
								t_i.isShown = true;
								t_i.r_i = count;
							}

						} else {
							g.setColor(Color.BLUE); // Foreign Key Blue
							for (int i = 0; i < for__; i++) {
								w = fm.stringWidth(t_i.fk[i]) > w ? fm
										.stringWidth(t_i.fk[i]) : w;
								g.drawString(t_i.fk[i], rowh, colh + h);
								h += 14;
							}
							g.setColor(new Color(100, 100, 100, 100));
							g.fillRect(rowh - 3, colh - 14, w + 3, 14 + 3); // table
																			// color
							g.setColor(new Color(0, 176, 255, 25));
							g.fillRect(rowh - 3, colh + 3, w + 3, h - 14); // some
																			// breathing
																			// space
							g.setColor(Color.BLACK);
							g.drawRect(rowh - 3, colh - 14, w + 3, h + 3); // some
																			// breathing
																			// space
							if (t_i.isShown == false) {
								Rectangle r__ = new Rectangle(rowh - 3,
										colh - 14, w += 10, h += 10);
								__v3.add(count, r__);
								t_i.isShown = true;
								t_i.r_i = count;
							}
						}
					} else {
						g.setColor(new Color(100, 100, 100, 100));
						g.fillRect(rowh - 3, colh - 14, w + 3, 14 + 3); // table
																		// color
						g.setColor(new Color(0, 176, 255, 25));
						g.fillRect(rowh - 3, colh + 3, w + 3, h - 14); // some
																		// breathing
																		// space
						g.setColor(Color.BLACK);
						g.drawRect(rowh - 3, colh - 14, w + 3, h + 3); // some
																		// breathing
																		// space
						if (t_i.isShown == false) {
							Rectangle r__ = new Rectangle(rowh - 3, colh - 14,
									w += 10, h += 10);
							__v1.add(count, r__);
							t_i.isShown = true;
							t_i.r_i = count;
						}
					}
					colh += h + 10;
					maxw = maxw > w ? maxw : w;
					if (_isLeveled == false) {
						if (++count % 4 == 0) {
							rowh += maxw + 10;
							colh = 10 + 14;
							maxw = 75;
						}
					} else
						count++;
				} // end of For loop
				currentL++;
				if (_isLeveled == true) {
					rowh += maxw + 10;
					colh = 10 + 14;
					maxw = 75;
				}
			} // end of while loop

			if (index == 3)
				makeLine(g);
		}

		public int getPaneWidth() {
			int colC = (_h.size() / 4 + 1) * 500 + 1000;
			return colC; // 500 pixel for all table and 1000 buffer

		}

		public int getPaneHeight() {
			return 4000; // Little more than ScrollView

		}

		/*
		 * Printing Horizontal pages. All tables should be with height of one
		 * page
		 */

		public int print(Graphics g, PageFormat pf, int pi)
				throws PrinterException {

			int height = getHeight();
			int width = getWidth();
			int totalNumPages = (int) Math.ceil(width / pf.getImageableWidth());

			if (pi >= totalNumPages) {
				return Printable.NO_SUCH_PAGE;
			}
			Graphics2D g2 = (Graphics2D) g;
			g2.translate(-(pi * pf.getImageableWidth()) + pf.getImageableX(),
					pf.getImageableY());
			g2.setColor(Color.black);
			int in = tabbedPane.getSelectedIndex();
			((Rel_panel) (rel_vc.get(in))).paint(g2);
			return Printable.PAGE_EXISTS;
		}
	};

	protected JComponent makePanel(final int index, final Hashtable<String, TableRelationInfo> _h) {
		Rel_panel panel = new Rel_panel(index, _h);
		panel.setPreferredSize(new Dimension(panel.getPaneWidth(), panel
				.getPaneHeight()));
		panel.addMouseMotionListener(this);
		panel.addMouseListener(this);

		JScrollPane scroll_pane = new JScrollPane(panel);
		scroll_pane.setPreferredSize(new Dimension(700, 600));

		scroll_pane.getHorizontalScrollBar().addAdjustmentListener(
				new MyAdjustmentListener());
		scroll_pane.getVerticalScrollBar().addAdjustmentListener(
				new MyAdjustmentListener());
		rel_vc.add(index - 1, panel);

		return scroll_pane;
	}

	/** Here goes the logic for making Connections **/
	private void makeLine(Graphics g) {
		g.setColor(Color.BLACK); // Line color Black

		Enumeration<String> enum1 = pk_Rel.keys();
		while (enum1.hasMoreElements()) {
			String t_n = (String) enum1.nextElement();
			TableRelationInfo t_i = (TableRelationInfo) pk_Rel.get(t_n);
			int for__ = t_i.fk_c;
			int pri__ = t_i.pk_c;

			Rectangle __rp = (Rectangle) __v3.elementAt(t_i.r_i);
			int xp = (int) __rp.getLocation().getX();
			int yp = (int) __rp.getLocation().getY();
			int wp = (int) __rp.getWidth();

			for (int i = 0; i < for__; i++) {
				TableRelationInfo t_f = (TableRelationInfo) pk_Rel
						.get(t_i.fk_pTable[i]);
				if (t_f == null)
					continue;

				int j = 0;
				Rectangle __rf = (Rectangle) __v3.elementAt(t_f.r_i);
				int xf = (int) __rf.getLocation().getX();
				int yf = (int) __rf.getLocation().getY();
				int wf = (int) __rf.getWidth();

				if (Rdbms_conn.getDBType().compareToIgnoreCase("ms_access") == 0) {
					yf += 7; // 14/2 to come in between
				} else {
					while (t_f.pk[j] != null
							&& t_i.fk_pKey[i].compareTo(t_f.pk[j]) != 0) {
						yf += 14; // 14/2 to come in between
						j++;
					}
				} // end of ms_access

				int y_f_p = yp + pri__ * 14 + 14 + i * 14 + 14; // Primary key,
																// table and fk
				yf += 14 + 7; // Primary key, table and fk
				if (xp + wp < xf)
					g.drawLine(xp + wp - 10, y_f_p, xf, yf); // Table value
				else if (xf + wf < xp)
					g.drawLine(xp, y_f_p, xf + wf - 10, yf); // Table value
				else
					g.drawLine(xp, y_f_p, xf, yf); // Table value
			}
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private void createAndShowGUI() {
		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		JFrame frame = new JFrame("Relationship Pane");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create and set up the content pane.
		JComponent newContentPane = this;
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.getContentPane().add(newContentPane, BorderLayout.CENTER);

		// Add Menubar
		JMenuBar menubar = new JMenuBar();
		frame.setJMenuBar(menubar);
		JMenu file_m = new JMenu("File");
		file_m.setMnemonic('F');
		menubar.add(file_m);
		JMenuItem count_m = new JMenuItem("Table Count");
		count_m.setActionCommand("tcount");
		count_m.addActionListener(this);
		file_m.add(count_m);
		file_m.addSeparator();
		JMenuItem search_m = new JMenuItem("Table Search");
		search_m.setActionCommand("tsearch");
		search_m.addActionListener(this);
		file_m.add(search_m);
		file_m.addSeparator();
		JMenuItem reset_m = new JMenuItem("Reset View");
		reset_m.setActionCommand("treset");
		reset_m.addActionListener(this);
		file_m.add(reset_m);
		file_m.addSeparator();
		JMenuItem image_m = new JMenuItem("Save as Image");
		image_m.setActionCommand("timage");
		image_m.addActionListener(this);
		file_m.add(image_m);
		file_m.addSeparator();
		JMenuItem print_m = new JMenuItem("Print");
		print_m.setActionCommand("print");
		print_m.addActionListener(this);
		file_m.add(print_m);

		// Display the window.
		frame.setLocation(100, 25);
		frame.pack();
		frame.setVisible(true);
	}

	public void mouseDragged(MouseEvent e) {
		if (__index == -1)
			return; // Not on table
		this.getTopLevelAncestor().setCursor(
				java.awt.Cursor
						.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
		isDragging = true;
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void mouseClicked(MouseEvent e) {
		// Do Nothing
	}

	public void mousePressed(MouseEvent e) {
		int in__ = tabbedPane.getSelectedIndex();
		Vector<Rectangle> __v = null;
		switch (in__) {
		case 0:
			__v = __v1;
			break;
		case 1:
			__v = __v2;
			break;
		case 2:
			__v = __v3;
			break;
		}
		int size = __v.size();
		int x = prevX = e.getX();
		int y = prevY = e.getY();
		__index = -1;
		for (int i = 0; i < size; i++) {
			if (((Rectangle) __v.elementAt(i)).contains(x, y) == true) {
				__index = i;
				break;

			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		this.getTopLevelAncestor().setCursor(
				java.awt.Cursor
						.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
		if (__index == -1)
			return; // Do nothing
		if (isDragging == false)
			return;
		isDragging = false;

		int in__ = tabbedPane.getSelectedIndex();
		Vector<Rectangle> __v = null;
		switch (in__) {
		case 0:
			__v = __v1;
			break;
		case 1:
			__v = __v2;
			break;
		case 2:
			__v = __v3;
			break;
		}

		Rectangle __r = (Rectangle) __v.elementAt(__index);
		int x = (int) __r.getLocation().getX();
		int y = (int) __r.getLocation().getY();
		int w = (int) __r.getWidth();
		int h = (int) __r.getHeight();

		int dx = e.getX() - prevX;
		int dy = e.getY() - prevY;
		Rectangle __r_n = new Rectangle(x + dx, y + dy, w, h);
		for (int i = 0; i < __v.size(); i++) {
			if (((Rectangle) __v.elementAt(i)).intersects(__r_n) == true
					&& i != __index) {
				JOptionPane.showMessageDialog(null,
						"Can not override other table", "Information",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}

		if ((x + dx) < 2 || (y + dy) < 2) {
			JOptionPane.showMessageDialog(null, "Can not move outside panel",
					"Information", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if ((x + dx) > ((Rel_panel) (rel_vc.get(in__))).getPaneWidth()
				|| (y + dy) > ((Rel_panel) (rel_vc.get(in__))).getPaneHeight()) {
			JOptionPane.showMessageDialog(null, "Can not move outside panel",
					"Information", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		__v.removeElementAt(__index);
		__v.add(__index, __r_n);

		repaint();
	}

	public void mouseEntered(MouseEvent e) {
		// Do Nothing
	}

	public void mouseExited(MouseEvent e) {
		// Do Nothing
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("tcount")) {
			int in = tabbedPane.getSelectedIndex();
			int t_cn = 0;
			switch (in) {
			case 0:
				t_cn = noPk_noR.size();
				break;
			case 1:
				t_cn = pk_noR.size();
				break;
			case 2:
				t_cn = pk_Rel.size();
				break;
			}
			JOptionPane.showMessageDialog(null, "Table Count in this Panel: "
					+ t_cn, "Table Information",
					JOptionPane.INFORMATION_MESSAGE);
		} else

		if (command.equals("tsearch")) {
			final String s31 = JOptionPane.showInputDialog(null,
					"Enter Table Name:", "Table Input Dialog", -1);
			if (s31 == null || s31.compareTo("") == 0)
				return;

			int in = tabbedPane.getSelectedIndex();
			TableRelationInfo t_i = null;
			Rectangle v_i = null;
			JViewport vport = null;
			switch (in) {
			case 0:
				t_i = (TableRelationInfo) noPk_noR.get(s31);
				if (t_i != null)
					v_i = (Rectangle) __v1.elementAt(t_i.r_i);
				if (v_i != null) {
					((JScrollPane) panel1).getVerticalScrollBar().setValue(0);
					((JScrollPane) panel1).getHorizontalScrollBar().setValue(0);
					vport = ((JScrollPane) panel1).getViewport();
				}
				break;
			case 1:
				t_i = (TableRelationInfo) pk_noR.get(s31);
				if (t_i != null)
					v_i = (Rectangle) __v2.elementAt(t_i.r_i);
				if (v_i != null) {
					((JScrollPane) panel2).getVerticalScrollBar().setValue(0);
					((JScrollPane) panel2).getHorizontalScrollBar().setValue(0);
					vport = ((JScrollPane) panel2).getViewport();
				}
				break;
			case 2:
				t_i = (TableRelationInfo) pk_Rel.get(s31);
				if (t_i != null)
					v_i = (Rectangle) __v3.elementAt(t_i.r_i);
				if (v_i != null) {
					((JScrollPane) panel3).getVerticalScrollBar().setValue(0);
					((JScrollPane) panel3).getHorizontalScrollBar().setValue(0);
					vport = ((JScrollPane) panel3).getViewport();
				}
				break;
			}
			if (t_i == null || v_i == null) {
				JOptionPane.showMessageDialog(null,
						"Table could not be found in this Panel ",
						"Table Search Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			vport.scrollRectToVisible(v_i);

			JPopupMenu popup = new JPopupMenu();
			JMenuItem sel_a;
			ImageIcon imageicon;
        imageicon = new ImageIcon(RelationPanel.class.getClassLoader().getResource("image/Found.jpg"), "Here");
        int imageLS = imageicon.getImageLoadStatus();
        if (imageLS == MediaTracker.ABORTED
            || imageLS == MediaTracker.ERRORED)
          sel_a = new JMenuItem(s31);
        else
          sel_a = new JMenuItem(s31, imageicon);

        popup.add(sel_a);
        popup.show(vport.getView(), (int) v_i.getX(), (int) v_i.getY());

		} else if (command.equals("print")) {
			PrinterJob printJob = PrinterJob.getPrinterJob();
			int in = tabbedPane.getSelectedIndex();
			printJob.setPrintable((Rel_panel) (rel_vc.get(in)));
			printJob.printDialog();
			try {
				printJob.print();
			} catch (Exception PrintException) {
				ConsoleFrame.addText("\n Printing Error:"
						+ PrintException.getMessage());
			}
		} else if (command.equals("timage")) {
			int in = tabbedPane.getSelectedIndex();
			Rel_panel vport = ((Rel_panel) (rel_vc.get(in)));
			new ImageUtil(vport, "png");
		} else if (command.equals("treset")) {
			int in = tabbedPane.getSelectedIndex();
			Iterator<TableRelationInfo> t_key = null;
			switch (in) {
			case 0:
				t_key = noPk_noR.values().iterator();
				((JScrollPane) panel1).getVerticalScrollBar().setValue(0);
				((JScrollPane) panel1).getHorizontalScrollBar().setValue(0);
				break;
			case 1:
				t_key = pk_noR.values().iterator();
				((JScrollPane) panel2).getVerticalScrollBar().setValue(0);
				((JScrollPane) panel2).getHorizontalScrollBar().setValue(0);
				break;
			case 2:
				t_key = pk_Rel.values().iterator();
				((JScrollPane) panel3).getVerticalScrollBar().setValue(0);
				((JScrollPane) panel3).getHorizontalScrollBar().setValue(0);
				break;
			}
			while (t_key.hasNext()) {
				TableRelationInfo t_info = t_key.next();
				t_info.isShown = false;
			}
			repaint();

		} // end of treset
	}

	private class MyAdjustmentListener implements AdjustmentListener {
		// This method is called whenever the value of a scrollbar is changed,
		// either by the user or programmatically.
		public void adjustmentValueChanged(AdjustmentEvent evt) {
			// Bug : there is some flickering happening
			repaint();
		}
	};

}
