package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2006      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with copyright      *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This file is used for creating console utility
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

public class ConsoleFrame {
	private static JTextArea textArea = new JTextArea();;
	private static JFrame frame = null;
	private static JToolBar tbar = new JToolBar();;

	public static void hideFrame() {
		frame.setVisible(false);
	}
	public static void showFrame() {
		if (frame.isShowing() == false)
			frame.setVisible(true);
		frame.toFront();
	}

	public static void createGUI() {
		if (frame != null)
			return;
		
		textArea.setEditable(false);
		Font ft = new Font("Helvetika", Font.PLAIN, 16);
		textArea.setFont(ft);
		JScrollPane sp = new JScrollPane(textArea);
		sp.setPreferredSize(new Dimension(390, 390));

		
		JButton clear = new JButton("Clear");
		clear.setActionCommand("clear");
		tbar.add(clear);
		clear.addKeyListener(new KeyBoardListener());
		clear.addActionListener(new ConsoleFrame.barListener());
		JButton hide = new JButton("Hide");
		tbar.add(hide);
		hide.setActionCommand("hide");
		hide.addKeyListener(new KeyBoardListener());
		hide.addActionListener(new ConsoleFrame.barListener());

		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		frame = new JFrame("Aggregate Profiler Console");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		frame.getContentPane().add(sp, BorderLayout.CENTER);
		frame.getContentPane().add(tbar, BorderLayout.PAGE_START);
		frame.setLocation(0, 0);

		frame.pack();
		frame.setVisible(true);
	}

	public static void addText(String text) {
		if (text == (String)null ) return;
		textArea.insert(text, 0);
	}

	private static class barListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("hide")) {
				hideFrame();
				return;
			}
			if (command.equals("clear")) {
				textArea.setText("");
				return;
			}

		}
	}
}
