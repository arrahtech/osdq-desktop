package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2013      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used selection of File both
 * for opening and saving 
 *
 */

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileSelectionUtil {
	
	/**
	 * Display the showSaveDialog which allows the user to specify file and
	 * directory
	 * 
	 * @return file to be saved
	 */
	public static File promptForFilename(String title) {
		File file = null;
		String _title = "Arrah Technology Save File";
		if ( title != null || "".equals(title) == false)
			_title = title;
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(_title);
		chooser.setCurrentDirectory(new File("."));

		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		} else {
			return null;
		}
		if (file.exists()) {
			int response = JOptionPane.showConfirmDialog(null,
					"Overwrite existing file?", "Confirm Overwrite",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.CANCEL_OPTION) {
				return null;
			}
		}
		return file;
	}
	
	public static File chooseFile(String title) throws FileNotFoundException {
		File f = null;
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(new File("."));

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return f = chooser.getSelectedFile();
		else
			return f;
	}
	
}
