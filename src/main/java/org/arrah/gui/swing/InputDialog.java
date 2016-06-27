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

/* This file is used for getting input from Dialog Box 
 *
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class InputDialog extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea r_c;

	public InputDialog(int type) {
		this.setLocation(200, 200);
		this.setModal(true);
	}

	public JTextArea createTextPanel() {
		JPanel dp = new JPanel();
		dp.setLayout(new BorderLayout());
		r_c = new JTextArea("Add your comments..", 8, 80);
		r_c.setLineWrap(true);
		r_c.setWrapStyleWord(true);

		JScrollPane areaScrollPane = new JScrollPane(r_c);
		areaScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(350, 200));
		dp.add(areaScrollPane, BorderLayout.CENTER);

		JPanel bp = new JPanel();
		JButton ok_b = new JButton("OK");
		ok_b.setActionCommand("ok");
		ok_b.addKeyListener(new KeyBoardListener());
		ok_b.addActionListener(this);
		bp.add(ok_b);

		JButton cn_b = new JButton("Cancel");
		cn_b.setActionCommand("cancel");
		cn_b.addKeyListener(new KeyBoardListener());
		cn_b.addActionListener(this);
		bp.add(cn_b);
		dp.add(bp, BorderLayout.PAGE_END);

		this.setTitle("Comment Dialog");
		this.getContentPane().add(dp);
		this.pack();
		this.setVisible(true);

		return r_c;

	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if ("cancel".equals(command)) {
			r_c.setText("No Comments. ");
		}
		this.dispose();

	}

}
