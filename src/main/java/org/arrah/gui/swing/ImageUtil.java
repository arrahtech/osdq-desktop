package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2007      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used saving
 * png image from a JComponent Object
 *
 */

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class ImageUtil {

	public ImageUtil(JComponent comp, String itype) {
		saveImage(comp, itype);
	}

	private class AtdFilter extends FileFilter {
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith(".png")
					|| f.isDirectory();
		}

		public String getDescription() {
			return "PNG image  (*.png) ";
		}
	}

	private void saveImage(JComponent comp, String itype) {
		try {
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			BufferedImage bImage = gc.createCompatibleImage(comp.getWidth(),
					comp.getHeight(), Transparency.BITMASK);

			Graphics2D g = bImage.createGraphics();
			comp.paint(g);

			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("PNG Image Save File");
			chooser.setCurrentDirectory(new File("."));
			chooser.setFileFilter(new AtdFilter());

			int returnVal = chooser.showSaveDialog(null);
			File f = null;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
				if (f.getName().toLowerCase().endsWith(".png") == false) {
					File renameF = new File(f.getAbsolutePath() + ".png");
					f = renameF;
				}
			} else
				return;
			if (f.exists()) {
				int response = JOptionPane.showConfirmDialog(null,
						"Overwrite existing file?", "Confirm Overwrite",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.CANCEL_OPTION)
					return;
			}

			ImageIO.write(bImage, "png", f);
		} catch (Exception exp) {
			ConsoleFrame.addText("\n Save Exception:" + exp.getMessage());
			JOptionPane.showMessageDialog(null, exp.getMessage(),
					"Image Save Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void removeWaring() {

	}
}
