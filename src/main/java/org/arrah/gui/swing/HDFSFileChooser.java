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

/* This file is used for displaying HDFS and local
 * files in directory structure where user can pick
 * the files to be transferred.
 *
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;



public class HDFSFileChooser  {

	private JDialog f = null;
	private String hdfsrootpath = null;
	
	private JTree tree = null;
	private Configuration conf = null; 
	private FileSystem fs = null;
	private FileStatus statusglob[];
	
	private DefaultMutableTreeNode top = null,a = null;
	private  String toPath = ""; // null will append the value
	private boolean copyToHadoop ;
	private JButton select = null; // same effect as double click on tree
	
	public HDFSFileChooser(String hdfspathinput , boolean toHadoop){
		hdfsrootpath = hdfspathinput;
		top = new DefaultMutableTreeNode(hdfsrootpath);
		copyToHadoop = toHadoop;
	}
	
	public void openHDFSExplorer() throws IOException
	{
		
		f = new JDialog();

        // Set  the dialog
        f.setLocation(50, 100);
        f.setModal(true);
	    f.setTitle("HDFS explorer");
	    f.add(createTree());
	    f.setSize(500, 500);
	    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    f.pack();
	    f.setVisible(true); 
	   
	}

	public void recDir(FileStatus status[], FileSystem fs) throws IOException
	{  		
	    for(int i=0; i<status.length; i++){
      	  
          	if(status[i].isDir()){ 		
          		a = new DefaultMutableTreeNode(status[i].getPath().toString().replace(hdfsrootpath, ""));          		
           		recDir(fs.listStatus(status[i].getPath()), fs);
          	}
          	else{
          		a.add(new DefaultMutableTreeNode(status[i].getPath().toString().replace(hdfsrootpath, "")));
          	}
          	top.add(a);
          } 
  }

 private JPanel createTree() {
	
	JPanel treePanel = new JPanel();
	
	JPanel botPanel = new JPanel();
	
	select = new JButton("Select");
	botPanel.add(select);
	JButton close = new JButton("Close");
	botPanel.add(close);
	
	conf = new Configuration();
	conf.set("fs.default.name", hdfsrootpath);
	
	try {
	fs = FileSystem.get(conf);
	statusglob = fs.listStatus(new Path("/"));  // you need to pass in your hdfs path
    recDir(statusglob, fs);
	} catch (IOException e) {
		e.printStackTrace();
		return treePanel;
	}

    treePanel.setLayout(new BorderLayout());
    
    tree = new JTree(top);
  
    int v = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
    int h = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
    JScrollPane jsp = new JScrollPane(tree, v, h);

    treePanel.add(jsp, BorderLayout.CENTER);

    treePanel.add(botPanel, BorderLayout.PAGE_END);
 
    close.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {

			f.dispose();
		}
	});
    
    select.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {

			  toPath=""; // clear the path
				 TreePath treePath = tree.getSelectionPath();
				 for (int i=0; i < treePath.getPathCount() ; i ++){
					 toPath = treePath.getPathComponent(i).toString(); // take last full name
				 }
				
				Path tp = new Path(hdfsrootpath,toPath);
				try {
					
					FileStatus ft = fs.getFileStatus(tp);
					
					if(copyToHadoop == false){ // Copying from Hadoop
						
						if(ft.isDir()){
							JOptionPane.showMessageDialog(null, "Selection is a directory, please select a file as source");
							return;
						}
							
						else{
							f.dispose();
						}
					}
					else { // copying to Hadoop
						
						if(ft.isDir()){
							toPath = toPath+"/"; // it is a directory
							f.dispose();
						}
						else{
							int response = JOptionPane.showConfirmDialog(null,
									"Overwrite existing file?", "Confirm Overwrite",
									JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
							
							if (response == JOptionPane.CANCEL_OPTION) 
								return ;
								
							f.dispose();
							}
					}
				} catch (IOException e1) {
					ConsoleFrame.addText("\n IOEception :"+e1.getLocalizedMessage());
					JOptionPane.showMessageDialog(null, "IO Exception"+e1.getLocalizedMessage());
				}
		  }
	});
    
    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        doMouseClicked(me);
      }
    });
    
    return treePanel;
  }

  void doMouseClicked(MouseEvent me) {
	  if (me.getClickCount() == 2) { // double clicked
		  select.doClick();
	  }	  

 }
  
  public String getPath() {
	  return hdfsrootpath + toPath;
  }
 
} // end of class
