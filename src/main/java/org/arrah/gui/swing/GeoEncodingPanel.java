package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2018      *
 *     http://www.arrahtec.org                 *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with the copyright  *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/* This file is used for taking inputs for
 * geo encoding and address completion options
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;



public class GeoEncodingPanel  implements ActionListener{

	private int _rowC = 0;

	private JComboBox<String>[] firstColCom, secondColCom;
	private JCheckBox[] selOption;

	private JDialog jd;
	private String[] _tag,_firstFile,_secondFile;
	private boolean _showCheckBox = false, cancel_clicked= false ;
	private int[] _tagActiveCode = null; //0 mean active for both, 1 mean active for 1st, 2 means active for second
	public int[] firstSelIndex = null, secondSelIndex = null;



	public GeoEncodingPanel(String[] tag, String[] firstFileCol, String[] secondFileCol) {
		_tag = tag;
		_firstFile = firstFileCol;
		_secondFile = secondFileCol;
		_rowC = _tag.length;
	}; // Constructor

	
	 public void createInputDialog(boolean isCheckBox) {

		 _showCheckBox = isCheckBox;
		JPanel jp = new JPanel(new BorderLayout());
	
		JPanel cenP = new JPanel(new SpringLayout());
		firstColCom = new JComboBox[_rowC];
		secondColCom = new JComboBox[_rowC];
		selOption = new JCheckBox[_rowC];
		
		JLabel hd1 =  new JLabel("Select",JLabel.CENTER);
		JLabel hd2 =  new JLabel("Attribute",JLabel.CENTER);
		JLabel hd3 =  new JLabel("Exising Field",JLabel.CENTER);
		JLabel hd4 =  new JLabel("MappedTo Field",JLabel.CENTER);
		
		
		if (_showCheckBox == true)
			cenP.add(hd1);
		cenP.add(hd2);cenP.add(hd3);cenP.add(hd4);
		
		for (int i=0; i<_rowC; i++) {
			if (_showCheckBox == true) {
				selOption[i] = new JCheckBox();
				selOption[i].setSelected(true);
			}
			JLabel jl = new JLabel (_tag[i]);
			firstColCom[i] = new JComboBox<String>(_firstFile);
			secondColCom[i] = new JComboBox<String>(_secondFile);
			
			if (_tagActiveCode != null) {
				if (_tagActiveCode[i] == 1)
					secondColCom[i].setEnabled(false);
				else if (_tagActiveCode[i] == 2)
					firstColCom[i].setEnabled(false);
			}
			
			if (_showCheckBox == true)
				cenP.add(selOption[i]);
			cenP.add(jl);
			cenP.add(firstColCom[i]);
			cenP.add(secondColCom[i]);
		}
		
		if (_showCheckBox == true)
			SpringUtilities.makeCompactGrid(cenP, _rowC+1, 4, 3, 3, 3, 3); // checkBox
		else
			SpringUtilities.makeCompactGrid(cenP, _rowC+1, 3, 3, 3, 3,3); // hearder for  +1
		
		 int size = 50;
		 size += (400 > _rowC*35 )?_rowC*35 : 150;

		cenP.setPreferredSize(new Dimension(600, size)); 
		JScrollPane pane = new JScrollPane(cenP);
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		jp.add(pane,BorderLayout.CENTER);
		
		JPanel botP = new JPanel();
		JButton ok = new JButton("OK");
		ok.addKeyListener(new KeyBoardListener());
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		botP.add(ok);

		JButton cancel = new JButton("Cancel");
		cancel.addKeyListener(new KeyBoardListener());
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		botP.add(cancel);

		jp.add(botP,BorderLayout.PAGE_END);
		jp.setPreferredSize(new Dimension(550, size+150)); // 1 for tips
				
		jd = new JDialog();
		jd.setTitle("Address Parameters Input");
		jd.setLocation(150, 150);
		jd.getContentPane().add(jp);
		jd.setModal(true);
		jd.pack();
		jd.setVisible(true);

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String action = e.getActionCommand();
		if (action.equals("cancel")) {
			jd.dispose();
			setCancel_clicked(true);
			return;
		}
		if (action.equals("ok")) {
			firstSelIndex = new int[_rowC];
			secondSelIndex = new int[_rowC];
			
			if (_showCheckBox == true) {
				for (int i=0 ; i < _rowC; i++) {
					if (selOption[i].isSelected() == true) {
						firstSelIndex[i] = firstColCom[i].getSelectedIndex();
						secondSelIndex[i] = secondColCom[i].getSelectedIndex();
					} else {
						firstSelIndex[i] = -1;
						secondSelIndex[i] = -1;
					}
				}
			} else {
			
				for (int i=0 ; i < _rowC; i++) {
					if (firstColCom[i].isEnabled() == false)
						firstSelIndex[i] = -1;
					else 
						firstSelIndex[i] = firstColCom[i].getSelectedIndex();
					
					if (secondColCom[i].isEnabled() == false)
						secondSelIndex[i] = -1;
					else 
						secondSelIndex[i] = secondColCom[i].getSelectedIndex();
				}
			}
				
			jd.dispose();
			return;
		}
	}

	public boolean isCancel_clicked() {
		return cancel_clicked;
	}


	public void setCancel_clicked(boolean cancel_clicked) {
		this.cancel_clicked = cancel_clicked;
	}


	public int[] get_tagActiveCode() {
		return _tagActiveCode;
	}


	public void set_tagActiveCode(int[] _tagActiveCode) {
		this._tagActiveCode = _tagActiveCode;
	}
	
	public List<Integer> getLeftActiveIndex() {
		List<Integer> active = new ArrayList<Integer>();
		for (int i=0; i < firstSelIndex.length; i++) {
			if (firstSelIndex[i] !=-1)
				active.add(firstSelIndex[i]);
		}
		return active;
	}
	
	public List<Integer> getRightActiveIndex() {
		List<Integer> active = new ArrayList<Integer>();
		for (int i=0; i < secondSelIndex.length; i++) {
			if (secondSelIndex[i] !=-1)
				active.add(secondSelIndex[i]);
		}
		return active;
	}
	
	public boolean[] getSelCheckboxIndex() {
		boolean[] active = new boolean[_rowC];
		for (int i=0 ; i < _rowC; i++) {
			if (selOption[i].isSelected() == true) 
				active[i] = true;
			else
				active[i] = false;
					
			}
		return active;
	}


}
