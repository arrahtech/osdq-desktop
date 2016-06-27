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

/* This file is used for displaying table records
 * for sql search or similarity search
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.arrah.framework.profile.TableMetaInfo;
import org.arrah.framework.rdbms.Rdbms_conn;
import org.arrah.framework.rdbms.SqlType;


public class TableSearchDialog implements ActionListener {
	
	private Vector<String> tableList;
	private Vector<?> colvector[] = null;
	private JComboBox<String> _rTableC;
	private JCheckBox [] _uqiC;
	private JDialog d_m;
	private JScrollPane jscrollpane1;
	private JPanel jp_p;
	private String _searchStr="";
	private boolean isFuzzy = false, isMultifacet=false, isDispose=true;;
	
	public TableSearchDialog (String searchStr) {
		_searchStr = searchStr;
	}
	public TableSearchDialog (String searchStr, boolean fuzzy) {
		_searchStr = searchStr;
		isFuzzy = fuzzy;
	}
	public TableSearchDialog (boolean multifacet) {
		isMultifacet = multifacet;
	}

	public JDialog createMapDialog() {

		TableItemListener tl = new TableItemListener();
		tableList = Rdbms_conn.getTable();
		_rTableC = new JComboBox<String>();
		
		for (int i=0; i < tableList.size(); i++ ) {
			_rTableC.addItem(tableList.get(i));
		_rTableC.addItemListener(tl);
		}
		
		// Header Making
		JPanel jp = new JPanel();
		JLabel partition = new JLabel("Select Table:    ",JLabel.TRAILING);
		partition.setForeground(Color.BLUE);
		jp.add(partition);
		jp.add(_rTableC);
		
		JPanel colP = columnPanel(0);
		jscrollpane1 = new JScrollPane(colP);
		jscrollpane1.setPreferredSize(new Dimension(575, 400));

		JPanel bp = new JPanel();

		JButton ok = new JButton("Search");;
		ok.setActionCommand("search");
		ok.addActionListener(this);
		ok.addKeyListener(new KeyBoardListener());
		bp.add(ok);
		
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		cancel.addKeyListener(new KeyBoardListener());
		bp.add(cancel);

		jp_p = new JPanel(new BorderLayout());
		jp_p.add(jp,BorderLayout.PAGE_START);
		jp_p.add(jscrollpane1, BorderLayout.CENTER);
		jp_p.add(bp, BorderLayout.PAGE_END);

		d_m = new JDialog();
		d_m.setModal(true);
		if (_searchStr == null || "".equals(_searchStr))
			d_m.setTitle("Multi Facet Table Search ");
		else
			d_m.setTitle("\""+ _searchStr + "\""+" String Table Search ");
		d_m.setLocation(250, 100);
		d_m.getContentPane().add(jp_p);
		d_m.pack();
		d_m.setVisible(true);

		return d_m;
	}
	
	private class TableItemListener implements ItemListener {
		private int index = 0;
		

		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED ) {
				index = _rTableC.getSelectedIndex();
				JPanel jp = columnPanel(index);
				jp_p.remove(jscrollpane1);
				jscrollpane1 = new JScrollPane(jp);
				jscrollpane1.setPreferredSize(new Dimension(575, 400));
				jp_p.add(jscrollpane1, BorderLayout.CENTER);
				
				d_m.revalidate();
				d_m.repaint();
			}
		}
	} // End of TableListener
	
	private JPanel columnPanel(int tableIndex) {
		
		JPanel jp = new JPanel(new SpringLayout());
		JLabel colNH = new JLabel("Column Name",JLabel.LEADING);
		colNH.setForeground(Color.BLUE);
		jp.add(colNH);
		JLabel colTH = new JLabel("Column Type",JLabel.LEADING);
		colTH.setForeground(Color.BLUE);
		jp.add(colTH);
		JLabel uniq = new JLabel("Search Column(s)",JLabel.CENTER);
		uniq.setForeground(Color.BLUE);
		jp.add(uniq);
		
		colvector = TableMetaInfo.populateTable(5, tableIndex, tableIndex+1, colvector); // get column info
		int colsize = colvector[0].size();
		_uqiC = new JCheckBox[colsize];
		
		for (int i =0; i < colsize; i++ ){
			_uqiC[i] = new JCheckBox();
		}
		
		for (int i=0; i < colvector[0].size(); i++) {
            JLabel colN = new JLabel((String)colvector[0].get(i),JLabel.LEADING);
            JLabel colT = new JLabel(SqlType.getTypeName((Integer) colvector[1].get(i)),JLabel.LEADING);
            jp.add(colN); jp.add(colT);
			_uqiC[i].setHorizontalAlignment(JCheckBox.CENTER);
			jp.add(_uqiC[i]);

		}
		SpringUtilities.makeCompactGrid(jp, colsize+1, 3, 10, 5, 25, 5);
		return jp;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("cancel".equals(e.getActionCommand())) 
			d_m.dispose();
		if ("search".equals(e.getActionCommand())) {
			int index = _rTableC.getSelectedIndex();
			Vector<String> colName_v = new Vector<String>();

			for (int i=0; i < colvector[0].size(); i++) {
				if (_uqiC[i].isSelected() == true)
				colName_v.add((String)colvector[0].get(i)); // Fill column name
			}
			if (colName_v.size() ==0 ) {
				JOptionPane.showMessageDialog(null,
						"Choose the column(s) to search", "No Column Error",
						JOptionPane.ERROR_MESSAGE);
				return ;
			}			
			// Send Information to like search comparison
			
			try {
			d_m.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
				if (isMultifacet == true) {
					MultifacetPanel mf = new MultifacetPanel(tableList.get(index), colName_v);
					if (mf.isCancel() == true)  {
						isDispose = false;
						return;
					}
				}
				else if (isFuzzy == false) {
					new SearchDBPanel(_searchStr, tableList.get(index), colName_v);
				}
				else {
					 new SimilarityCheckPanel(_searchStr, tableList.get(index), colName_v);
				}
			} catch (Exception ee) {
				System.out.println("Exeption in Table Search:"+ee.getMessage());
			} finally {
				d_m.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
				if (isDispose == true)
					d_m.dispose();
			}
			d_m.dispose();
		}
	}
}
