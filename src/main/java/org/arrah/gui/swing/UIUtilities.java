package org.arrah.gui.swing;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/***********************************************
 *     Copyright to Arrah Technology 2020      *
 *     http://www.arrahtech.com                *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for utilities functions
 * which will be used for Swing UI and 
 * generic enough to be used for many classes 
 *
 */


public class UIUtilities {
	
	// Description can be passed as argument
	public static String getMaskedString(String description) {
		
		if (description == null)
			description="";
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
		//panel.setPreferredSize(new Dimension(description.length()*8+20,50));
		
		JLabel label = new JLabel(description);
		
		JPasswordField pass = new JPasswordField(40);
		

		
		panel.add(label);
		panel.add(pass);
		
		String[] options = new String[]{"OK", "Cancel"};
		
		int option = JOptionPane.showOptionDialog(null, panel, "Masked Value",
		                         JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
		                         null, options, options[1]);
		if(option == 0) // pressing OK button
		{
		    char[] password = pass.getPassword();
		    return new String(password);
		    
		} else {
			return null;
		}
	}

}
