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
	public static File lastreadFile=null;
	public static File lastsaveFile=null;
	
	public static File promptForFilename(String title) {
		File file = null;
		String _title = "Arrah Technology Save File";
		if ( title != null || "".equals(title) == false)
			_title = title;
		
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(_title);
		if (lastsaveFile == null)
			chooser.setCurrentDirectory(new File("."));
		else
			chooser.setCurrentDirectory(lastsaveFile);

		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
			lastsaveFile = file;
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
		if (lastreadFile == null)
			chooser.setCurrentDirectory(new File("."));
		else
			chooser.setCurrentDirectory(lastreadFile);

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
			lastreadFile = f;
			return  f;
		}
		else
			return f;
	}
	
	public static File[] chooseFiles(String title) throws FileNotFoundException {
		File[] f = null;
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (lastreadFile == null)
			chooser.setCurrentDirectory(new File("."));
		else
			chooser.setCurrentDirectory(lastreadFile);
		
		chooser.setMultiSelectionEnabled(true); // Multi Select

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFiles();
			lastreadFile = f[0]; // first file select location
			return  f;
		}
		else
			return f;
	}
	
}
