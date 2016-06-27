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

/* This file is used for creating generic 
 * KeyBoard behavior for all component
 *
 */

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;

public class KeyBoardListener implements KeyListener {
	public KeyBoardListener() {
	}; // /Constructor

	public void keyPressed(KeyEvent e) {

		int key = e.getKeyCode();
		if (key != KeyEvent.VK_ENTER)
			return;
		String event_class_name = e.getSource().getClass().getName();
		if (event_class_name.compareToIgnoreCase("javax.swing.JButton") == 0) {
			((JButton) e.getSource()).doClick();
		}

	}// End of KeyPressed

	public void keyReleased(KeyEvent e) {

	}// End of KeyReleased

	public void keyTyped(KeyEvent e) {

	}// End of KeyTyped

}
