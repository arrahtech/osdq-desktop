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

/* 
 * This is Regex Panel which shows which is used
 * for creating and displaying records.
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.util.KeyValueParser;


public class RegexPanel extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JTextField f_t,e_t,n_t;
	private Hashtable <String,String> __h;
	private ReportTable rt;

	
	public RegexPanel () {
		// Constructor
		JPanel cen =createPanel();
		
		this.setLocation(250,50);
		this.getContentPane().add(cen);
		this.setPreferredSize(new Dimension(720, 620));
		this.setTitle("Regex Creation Dialog");
		this.setModal(true);
		this.pack();
		this.setVisible(true);
	}
	private JPanel createPanel() {
		JPanel fd_panel = new JPanel(new BorderLayout());
		
		JEditorPane editorPane = createEditorPane();
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(500, 350));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));

		JLabel n_l = new JLabel("Name:", JLabel.TRAILING);
		JLabel f_l = new JLabel("Regex:", JLabel.TRAILING);
		JLabel e_l = new JLabel("Example:", JLabel.TRAILING);
		n_t = new JTextField("Name of Regex", 15);
		f_t = new JTextField("Format String", 15);
		e_t = new JTextField("Example", 15);
		
		JButton validate = new JButton("Validate");
		validate.setActionCommand("validate");
		validate.addActionListener(this);
		validate.addKeyListener(new KeyBoardListener());
		
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		
		rt = populateTable();
		JScrollPane regexScroll = new JScrollPane(rt);
		regexScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		regexScroll.setPreferredSize(new Dimension(700, 250));
		regexScroll.setMinimumSize(new Dimension(10, 10));

		JPanel labP = new JPanel(new BorderLayout());
		JLabel tit = new JLabel ("  Existing Regex Expression  ",JLabel.CENTER); 
		labP.add(tit,BorderLayout.PAGE_START);
		labP.add(regexScroll,BorderLayout.CENTER);
		
		fd_panel.add(labP, BorderLayout.PAGE_START);
		fd_panel.add(editorScrollPane, BorderLayout.CENTER);
		
		JPanel wp = new JPanel ( new BorderLayout() );
		JLabel hl = new JLabel(); //dummy
		wp.add(hl,BorderLayout.PAGE_START); 
		
		JPanel cp = new JPanel(new SpringLayout());
		cp.add(new JLabel());cp.add(new JLabel()); //dummy
		cp.add(new JLabel("Create",JLabel.TRAILING));cp.add(new JLabel("Regex Here"));
		cp.add(n_l);
		cp.add(n_t);
		cp.add(f_l);
		cp.add(f_t);
		cp.add(e_l);
		cp.add(e_t);
		SpringUtilities.makeCompactGrid(cp, 5, 2, 3, 3, 3, 3);
		wp.add(cp,BorderLayout.CENTER); 
		
		JPanel bp = new JPanel ( );
		bp.add(validate);
		bp.add(cancel);
		wp.add(bp,BorderLayout.PAGE_END);
		
		fd_panel.add(wp,BorderLayout.WEST);

		return fd_panel;
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if ("validate".equals(command)) {
			if ( n_t.getText() == null || f_t.getText() == null || e_t.getText() == null ||
					"".equals(n_t.getText()) || "".equals(f_t.getText())||  "".equals(e_t.getText()) ) {
				JOptionPane.showMessageDialog(null, "Name, Regex or Example can not be Empty",
						"Regex Information Dialog",JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			String name = n_t.getText();
			String format = f_t.getText();
			String exam = e_t.getText();
			boolean isValid = false;
			
			try {
				isValid = Pattern.matches(format, exam);
				
			} catch(PatternSyntaxException pe) {
				JOptionPane.showMessageDialog(null, "Not a valid Regex",
						"Regex Information Dialog",JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (isValid == false) {
				JOptionPane.showMessageDialog(null, "Example did not match regex",
						"Regex Information Dialog",JOptionPane.INFORMATION_MESSAGE);
				return;
			} else {
				if (__h.containsKey(name) == true) {
					JOptionPane.showMessageDialog(null, "Name Already exist. \n Please choose another name for Regex.",
							"Regex Information Dialog",JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				int n = JOptionPane.showConfirmDialog(null, "Valid Regex:"
						+ format + "\n Do you want to save this format ?",
						"Regex Save Dialog", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					__h.put(name, format);
					boolean saved = KeyValueParser.saveTextFile("resource/popupmenu.txt", __h);
					if (saved == true) {
						rt.getRTMModel().addFillRow(new String[] {name,format});
						JOptionPane.showMessageDialog(null, "Regex Saved.",
							"Regex Save Dialog",JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, "Regex Can't be Saved:",
							"Regex Save Dialog",JOptionPane.INFORMATION_MESSAGE);
					}
						
				}
			}
		}
		if ("cancel".equals(command)) {
			this.dispose();
		}
		
	}
	private ReportTable populateTable() {
		ReportTableModel rtm = new ReportTableModel(new String[]{"Name","Regex"});
		// Create the popup menu From regexString.txt
		__h = KeyValueParser.parseFile("resource/popupmenu.txt");
		if (__h == null) return null;
			Enumeration<String> enum1 = __h.keys();
			while (enum1.hasMoreElements()) {
				String key_n = (String) enum1.nextElement();
				String val_n = __h.get(key_n);
				rtm.addFillRow(new String[]{key_n,val_n});
			}
		rt = new ReportTable(rtm);
		return rt;
		
	}
	private JEditorPane createEditorPane() {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		String fileN = "htmls/PatternHelp.html";
		java.net.URL helpURL = RegexPanel.class.getClassLoader().getResource(fileN);
		if (helpURL != null) {
			try {
				editorPane.setPage(helpURL);
			} catch (IOException e) {
				ConsoleFrame.addText("\n Attempted to read a bad URL: "
						+ helpURL);
			}
		} else {
			ConsoleFrame.addText("\n Couldn't find file:" + fileN);
		}
		return editorPane;
	}

}
