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
 * Author$ : Dheeraj Chugh                     *
 *                                             *
 ***********************************************/

/* This file is used for displaying UI where
 * user can choose local and HDFS files to be
 * transferred
 *
 */


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.arrah.framework.hadooputil.HDFSTransfer;
import org.arrah.framework.hadooputil.HDFSTransferProgressListener;
import org.arrah.framework.util.AsciiParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class HDFSFileTransferFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
    private String srcPath = null;
    private String destPath = null;
    
    private String filesystem = null;
    
    private  JTextArea area = new JTextArea();
    private  JTextField hdfsfiletext = new JTextField(35);
    private  JTextField localfiletext = new JTextField(35);
    
    private  JFormattedTextField nbytes = new JFormattedTextField(new Integer(1));
    private  JFormattedTextField nlines = new JFormattedTextField(new Integer(1));
    
    private JRadioButton bybyte,byline,bydelim;
    private JComboBox<String> delimetersBox;
    
    private boolean toHdfs;
    
    private HDFSTransfer hdt = null;
    private HDFSFileChooser mc = null;
    
    public static JProgressBar progressBar = new JProgressBar();
    
    private HDFSTransferProgressListener hdfsTransferProgressListener = new HDFSTransferProgressListener() {
      
      @Override
      public void progressUpdate(final int progressCounter) {
        progressBar.setStringPainted(true);
        progressBar.setValue(progressCounter);
      }
    };
   
    
    public HDFSFileTransferFrame( boolean copyToHdfs) {
    	toHdfs = copyToHdfs;
        initUI();
    }

    private  void initUI() {

        JPanel panel = new JPanel(); // parent panel
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        
        
        JLabel hdfsfile  = new JLabel("Hdfs file:        ");
        JLabel localfile = new JLabel("Local file: ");
       
        hdfsfiletext.setText("hdfs://hostAddress:port/");
        hdfsfile.setToolTipText("Base URI for HDFS");
        hdfsfiletext.setToolTipText("Base URI for HDFS");
        
        JButton openlocal = new JButton("Browse Local");
        openlocal.setActionCommand("browselocal");
        openlocal.addActionListener(this);
        JButton openhdfs = new JButton("Browse HDFS");
        openhdfs.setActionCommand("browsehdfs");
        openhdfs.addActionListener(this);
        
        
        JLabel filecontents = new JLabel("File Contents");
        area.setEditable(false);
        JScrollPane pane = new JScrollPane();
        pane.getViewport().add(area);
        pane.setPreferredSize(new Dimension(500,220));
        
        JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,2,4));
        previewPanel.setPreferredSize(new Dimension(400,90));
         bybyte = new JRadioButton("Number of bytes");
         byline = new JRadioButton("Number of lines");
         bydelim = new JRadioButton("Choose delimeter");
        
        ButtonGroup radiogroup1 = new ButtonGroup();
        radiogroup1.add(bybyte);
        radiogroup1.add(byline);
        radiogroup1.add(bydelim);
        byline.setSelected(true);
        
        delimetersBox = new JComboBox<String>();
        AsciiParser ascobj = new AsciiParser();
        try {
			ascobj.init("resource/ascii.txt");
			for(int i=0; i<AsciiParser.delims.size(); i++){
				delimetersBox.addItem(AsciiParser.delims.get(i).toString());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        nbytes.setPreferredSize(new Dimension (70,20));
        nlines.setPreferredSize(new Dimension (70,20));
        
        JButton preview = new JButton("File Preview");
        preview.setActionCommand("preview");
        preview.addActionListener(this);
        
        previewPanel.add(bybyte);previewPanel.add(nbytes);previewPanel.add(byline);previewPanel.add(nlines);
        previewPanel.add(bydelim);previewPanel.add(delimetersBox);previewPanel.add(preview);
        
        JPanel buttonPanel = new JPanel();
        JButton help = new JButton("Help");
        help.setActionCommand("help");
        help.addActionListener(this);
        JButton copy = new JButton("Copy");
        copy.setActionCommand("copy");
        copy.addActionListener(this);
        JButton cancel = new JButton("Cancel");
        cancel.setActionCommand("cancel");
        cancel.addActionListener(this);
        buttonPanel.add(help);buttonPanel.add(copy);buttonPanel.add(cancel);
        
        progressBar.setStringPainted(true);
        
        panel.add(hdfsfile); panel.add(hdfsfiletext);panel.add(openhdfs);
        panel.add(localfile); panel.add(localfiletext);panel.add(openlocal);

        panel.add(filecontents);panel.add(pane);
        panel.add(previewPanel);
        
        panel.add(buttonPanel);panel.add(progressBar);
        
        // Set Layout for the Panel
        layout.putConstraint(SpringLayout.WEST, hdfsfile, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, hdfsfile, 8, SpringLayout.NORTH,panel);
		layout.putConstraint(SpringLayout.WEST, hdfsfiletext, 5, SpringLayout.EAST, hdfsfile);
		layout.putConstraint(SpringLayout.NORTH, hdfsfiletext, 8, SpringLayout.NORTH,panel);
		layout.putConstraint(SpringLayout.WEST, openhdfs, 5, SpringLayout.EAST, hdfsfiletext);
		layout.putConstraint(SpringLayout.NORTH, openhdfs, 8, SpringLayout.NORTH,panel);
		
		layout.putConstraint(SpringLayout.WEST, localfile, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, localfile, 15, SpringLayout.SOUTH,hdfsfile);
		layout.putConstraint(SpringLayout.WEST, localfiletext, 5, SpringLayout.EAST, localfile);
		layout.putConstraint(SpringLayout.NORTH, localfiletext, 15, SpringLayout.SOUTH,hdfsfile);
		layout.putConstraint(SpringLayout.WEST, openlocal, 5, SpringLayout.EAST, localfiletext);
		layout.putConstraint(SpringLayout.NORTH, openlocal, 15, SpringLayout.SOUTH,hdfsfile);
		
		layout.putConstraint(SpringLayout.WEST, filecontents, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, filecontents, 10, SpringLayout.SOUTH,localfile);
		layout.putConstraint(SpringLayout.WEST, pane, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, pane, 8, SpringLayout.SOUTH,filecontents);
		layout.putConstraint(SpringLayout.WEST, previewPanel, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, previewPanel, 8, SpringLayout.SOUTH,pane);
		
		layout.putConstraint(SpringLayout.WEST, buttonPanel, 200, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, buttonPanel, 5, SpringLayout.SOUTH,previewPanel);
		layout.putConstraint(SpringLayout.WEST, progressBar, 220, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.NORTH, progressBar, 5, SpringLayout.SOUTH,buttonPanel);
        
		if (toHdfs == true)
			this.setTitle("HDFS Copy Utility - Copying to HDFS..");
		else
			this.setTitle("HDFS Copy Utility - Copying From HDFS..");
        this.setPreferredSize(new Dimension(650, 530));
        this.setLocation(150,100);
        this.add(panel);
        this.pack();
        this.setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
       
        //Initiate copy button
        copy.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				progressBar.setValue(0);
				String hdfspathstring = hdfsfiletext.getText();
				if(toHdfs == true){
					
					destPath=hdfsfiletext.getText();
					srcPath = localfiletext.getText();
					
					if(destPath == null || srcPath == null || "".equals(destPath) || "".equals(srcPath)) {
						JOptionPane.showMessageDialog(null, "Select both Source and Destination paths");
						return;
					}
					try {
						if(destPath != null && srcPath != null)
						{
							setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
							hdt = new HDFSTransfer(hdfsTransferProgressListener, hdfspathstring);
							hdt.moveToHDFS(srcPath, destPath);	
						}
					} catch (IOException e) {
						ConsoleFrame.addText("\n Copy failed. Ensure to enter a correct HDFS Path");
						JOptionPane.showMessageDialog(null, "Copy failed."+e.getLocalizedMessage());
						return;
					} finally {
						setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
					}
				}
				else {
					destPath=localfiletext.getText();
					srcPath = hdfsfiletext.getText();

					if(destPath == null || srcPath == null)
						JOptionPane.showMessageDialog(null, "Ensure to select both Source and Destination paths");
					try {
						if(destPath != null && srcPath != null)
						{
							setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
							hdt = new HDFSTransfer(hdfsTransferProgressListener, hdfspathstring);
							hdt.moveFromHDFS(srcPath, destPath);
						}
					} catch (IOException e) {
						ConsoleFrame.addText("\n Copy failed. Ensure to enter a correct HDFS Path");
						JOptionPane.showMessageDialog(null, "Copy failed."+e.getLocalizedMessage());
						return;
						
					} finally {
						setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
					}
				}
				JOptionPane.showMessageDialog(null," Copy Successful");
			}
		});
        
    } // end of Function initUI
    
    /* This function will set path for HDFS scr and destfile */
    private void setPath() {
    	
			if(toHdfs == false) {
				hdfsfiletext.setText(mc.getPath());
				srcPath=mc.getPath();
				ConsoleFrame.addText("\n Selected Source path is:"+ srcPath);
			}
			else  {
				hdfsfiletext.setText(mc.getPath()); 
				destPath=mc.getPath();
				ConsoleFrame.addText("\n Selected Destination path is:"+ destPath);
			}
			
    }

    //All Action Listeners - start
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		 //help button
		if (command.equals("help")) {
			JOptionPane.showMessageDialog(null,"Utility to copy a file from User's local machine to Hadoop FS and vice verca \n" +
								"\n-> HDFS Path: Path to Hadoop FS of the format - hdfs://<hostAddress>:port/ \n" +
								"                           e.g. hdfs://xx.xx.xx.xx:9000/                     \n" +
								"\n-> Click on Browse HDFS button to set or select the HDFS path, unless you would not be  \n" +
								"    able to view the HDFS explorer. Further every time you update the HDFS   \n" +
								"    path click Browse button to update the explorer with new path            ");
			return;
		} // end of help
		if (command.equals("cancel")) {
			this.dispose();
		}
		
		 //openLocal directory system
		if (command.equals("browselocal")) {
			progressBar.setValue(0);
            File file = null;
                if(toHdfs == true){
                	try { 
                	file = FileSelectionUtil.chooseFile("File to move to HDFS");
                	if (file == null ) return;
                	srcPath = file.getPath();
                	localfiletext.setText(srcPath);
                	} catch (FileNotFoundException fileException) {
                		JOptionPane.showMessageDialog(null, "File Exception:"+ fileException.getLocalizedMessage());
                	}
                }
                else {
                	file = FileSelectionUtil.promptForFilename("File to Save");
                	if (file == null ) return;
                	destPath = file.getPath();
                	localfiletext.setText(destPath);
                	
                }
           return;
	    } // end of browse local
		
		//openHDFS directory system
		if (command.equals("browsehdfs")) {
			progressBar.setValue(0);
			 //Validate
			String hdfsPath = hdfsfiletext.getText();
			
			if(hdfsPath == null || hdfsPath.isEmpty()){
				JOptionPane.showMessageDialog(null, "HDFS URI can not be empty.");
				return;
			}

			if(HDFSTransfer.testhdpath(hdfsPath)){
				JOptionPane.showMessageDialog(null, "URI accessed successfully. Will show Files ....");
			}
			else {
				JOptionPane.showMessageDialog(null, "Invalid HDFS URI.\nEnter a valid HDFS path" +
						"\n or Click Help button");
				return;
			}

			mc = new HDFSFileChooser(hdfsPath,toHdfs);
			try {
					mc.openHDFSExplorer();
					setPath();
			} catch (IOException IOException){
				JOptionPane.showMessageDialog(null, "File IO Exception:"+ IOException.getLocalizedMessage());
				return;
			}
			
			return;
		} // end of browse hdfs
		
		//Preview Source File directory system
	if (command.equals("preview")) {
		progressBar.setValue(0);
		int limit = 0;
		if( srcPath == null ||srcPath.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Select a source file to display");
			return;
		}
		else
		{
			String hdfspathstring = hdfsfiletext.getText();
			if(!bybyte.isSelected() && !byline.isSelected() && !bydelim.isSelected()){
				JOptionPane.showMessageDialog(null, "Select a display option type \n (Number of bytes or\n Number of lines or\n Choose delimeter)");
				return;
			}
			else{
				if(bybyte.isSelected()){
					try{
						limit = Integer.parseInt(nbytes.getText().toString());
						
						if(toHdfs == false){
							filesystem = "hdfs";
							hdt = new HDFSTransfer(hdfsTransferProgressListener, hdfspathstring);
						}
						else {
							filesystem = "local";
							hdt = new HDFSTransfer(hdfsTransferProgressListener);
						}
						try {
							setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
							area.setText(hdt.readFile(srcPath, filesystem, "bybytes", limit));
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(null, "IO Exception"+ioe.getLocalizedMessage() );
							return;
							
						} finally {
							setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
						}
					}catch (NumberFormatException nume) {
						JOptionPane.showMessageDialog(null, "Please enter a  numbers only\n (within range 0 to 2147483647)");
						return;
					}
				}
				else if(byline.isSelected()){
					try{
						limit = Integer.parseInt(nlines.getText().toString());
						
						if(toHdfs == false){
							filesystem = "hdfs";
							hdt = new HDFSTransfer(hdfsTransferProgressListener, hdfspathstring);
						}
						else {
							filesystem = "local";
							hdt = new HDFSTransfer(hdfsTransferProgressListener);
						}
						try {
							setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
							area.setText(hdt.readFile(srcPath, filesystem, "byline", limit));
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(null, "IO Exception"+ioe.getLocalizedMessage() );
							return;
						} finally {
							setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
						}
					}catch (NumberFormatException nume) {
						JOptionPane.showMessageDialog(null, "Please enter a  number only\n (within range 0 to 2147483647)");
						return;
					}
				}
				else if(bydelim.isSelected()){
						
					limit = delimetersBox.getSelectedIndex();
					limit = limit +1;
					
					if(toHdfs == false){
						filesystem = "hdfs";
						hdt = new HDFSTransfer(hdfsTransferProgressListener, hdfspathstring);
					}
					else {
						filesystem = "local";
						hdt = new HDFSTransfer(hdfsTransferProgressListener);
					}
					try {
						setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
						area.setText(hdt.readFile(srcPath, filesystem, "bydelim", limit));
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(null, "IO Exception"+ioe.getLocalizedMessage() );
						return;
					} finally {
						setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
					}
				}
			}
		}		
	} // end of preview
	
	
	} // end of Action listener 
		
} // end of class
