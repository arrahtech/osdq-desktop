package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2017      *
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
 * This file is used to create entity
 * resolution input and processing
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.arrah.framework.dataquality.EntityResolutionLucene;
import org.arrah.framework.dataquality.SimilarityCheckLucene;
import org.arrah.framework.dataquality.SimilarityCheckLucene.Hits;


public class ERPanel implements ActionListener, ItemListener {
    private ReportTable _rt, _rtMap, outputRT;
    private String[] colName;
    private String[] colType;
    private JDialog d_m;
    private JFrame dg;
    private JComboBox<String>[] sType, mcolName;
    private JTextField[] sLower,sHigher;
    private SimilarityCheckLucene _simcheck;
    private JRadioButton jr1;
    private JTextField jtx1;
    private Vector<EntityResolutionLucene.MappingClass>  mappingV;
    private boolean _onemapping = false;


    // For RTM  table and single Column Search
    public ERPanel(ReportTable reportTableIndex, ReportTable reportTableSearch) {

        _rt = reportTableIndex;
        _rtMap = reportTableSearch;
        _simcheck = new SimilarityCheckLucene(_rt.getRTMModel());
        colName = getColName();
        mapDialog();

    }
    
    // For RTM  table and single Column Search
    public ERPanel(ReportTable reportTableIndex, ReportTable reportTableSearch, boolean mappingType) {
    	_onemapping = mappingType;
        _rt = reportTableIndex;
        _rtMap = reportTableSearch;
        _simcheck = new SimilarityCheckLucene(_rt.getRTMModel());
        colName = getColName();
        mapDialog();

    }

    // Private supporting functions
    private String[] getColName() {
        int colC = _rt.table.getColumnCount();
        colName = new String[colC];
        colType = new String[colC];

        for (int i = 0; i < colC; i++) {
            colName[i] = _rt.table.getColumnName(i);
            colType[i] = _rt.table.getColumnClass(i).getName();
        }
        return colName;
    }

    // UI for multi-facet search
    private JDialog mapDialog() {
    	//Header
    	JPanel headerp = new JPanel();
    	jr1 = new JRadioButton("Memory Index");
    	jr1.setSelected(true);
    	JRadioButton jr2 = new JRadioButton("File Index");
    	ButtonGroup bgrp = new ButtonGroup();
    	bgrp.add(jr1);bgrp.add(jr2);
    	jtx1 = new JTextField("Index_name",25);
    	headerp.add(jr1);headerp.add(jr2);headerp.add(jtx1);

        int colC = colName.length;
        sType = new JComboBox[colC];
        mcolName = new JComboBox[colC];
        sLower = new JTextField[colC];
        sHigher = new JTextField[colC];
        JTextField[] sColName = new JTextField[colC];


        JPanel jp = new JPanel();
        SpringLayout layout = new SpringLayout();
        jp.setLayout(layout);

        JLabel l1 = new JLabel("Field Name");
        l1.setForeground(Color.BLUE);
        JLabel l2 = new JLabel("Map To");
        l2.setForeground(Color.BLUE);
        JLabel l3 = new JLabel("Map Type");
        l3.setForeground(Color.BLUE);
        JLabel l4 = new JLabel("Input 1");
        l4.setForeground(Color.BLUE);
        JLabel l5 = new JLabel("Input 2");
        l5.setForeground(Color.BLUE);

        jp.add(l2);
        jp.add(l1);
        jp.add(l3);
        jp.add(l4);
        jp.add(l5);


        for (int i = 0; i < colC; i++) {
            mcolName[i] = new JComboBox<String>(_rtMap.getAllColNameAsString());
            jp.add(mcolName[i]);
            
            sColName[i] = new JTextField(10);
            sColName[i].setText(colName[i]);
            sColName[i].setEditable(false);
            sColName[i].setToolTipText(colType[i]);
            jp.add(sColName[i]);
            


            sType[i] = new JComboBox<String>(new String[] { "Not Applicable", "Exact Match",
                    "Similar-Any Word", "Similar-All Words", "Range Bound - Number","Starts With","Ends With","Range Bound - Date" });
            sType[i].addItemListener(this);
            sType[i].setActionCommand(Integer.toString(i));
            jp.add(sType[i]);

            sLower[i] = new JTextField(10);
            sLower[i].setText(new String("Input"));
            sLower[i].setEnabled(false);
            jp.add(sLower[i]);

            sHigher[i] = new JTextField(10);
            sHigher[i].setText(new String("Input"));
            sHigher[i].setEnabled(false);
            jp.add(sHigher[i]);

            if (colType[i].toUpperCase().contains("DATE") || colType[i].toUpperCase().contains("TIME") ) {
            	sLower[i].setToolTipText("Lower bound in seconds");
            	sHigher[i].setToolTipText("Upper bound in seconds");
            }
        }
        SpringUtilities.makeCompactGrid(jp, colC + 1, 5, 3, 3, 3, 3); // +1 for
        // header

        JScrollPane jscrollpane1 = new JScrollPane(jp);
        if (colC * 35 + 50 > 400)
            jscrollpane1.setPreferredSize(new Dimension(725, 400));
        else
            jscrollpane1.setPreferredSize(new Dimension(725, colC * 35 + 50));

        JPanel bp = new JPanel();

        JButton ok = new JButton("Search");
        ok.setActionCommand("simcheck");
        ok.addActionListener(this);
        ok.addKeyListener(new KeyBoardListener());
        bp.add(ok);
        JButton cancel = new JButton("Cancel");
        cancel.setActionCommand("mcancel");
        cancel.addActionListener(this);
        cancel.addKeyListener(new KeyBoardListener());
        bp.add(cancel);

        JPanel jp_p = new JPanel(new BorderLayout());
        jp_p.add(headerp, BorderLayout.PAGE_START);
        jp_p.add(jscrollpane1, BorderLayout.CENTER);
        jp_p.add(bp, BorderLayout.PAGE_END);

        d_m = new JDialog();
        d_m.setTitle("Entity Resolution Dialog");
        d_m.setLocation(150, 150);
        d_m.getContentPane().add(jp_p);
        d_m.setModal(true);
        d_m.pack();
        d_m.setVisible(true);

        return d_m;
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("simcheck")) {
            try {
            	String indexName = jtx1.getText();
            	if (jr1.isSelected() == false)  { // file index is selected
            		if (indexName == null || "".equals(indexName)) {
            			JOptionPane.showMessageDialog(null, "Index Name can not be empty");
            			return;
            		}
            	}
                d_m.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (jr1.isSelected() == true) // RAM Selected
                	_simcheck.makeIndex();
                else
                _simcheck.makeIndex(indexName);
                if (validateInput() == false ) return;
                searchTableIndex(_onemapping);
            } finally {
                d_m.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                d_m.dispose();
            }
        }
        if (command.equals("mcancel")) {
            d_m.dispose();
        }
    }

 
    private void searchTableIndex(boolean oneMapping) { // Search the table
    	String [] oldCol = _rtMap.getRTMModel().getAllColNameStr();
    	String[] newCol = new String[colName.length + oldCol.length];
    	for (int i=0; i <oldCol.length; i++ )
    		newCol[i] = oldCol[i];
    	for (int i=0; i <colName.length; i++ )
    		newCol[oldCol.length+i] = colName[i];

    	
        outputRT = new ReportTable(newCol, false, true);

        if (_simcheck.openIndex() == false)
            return;

        EntityResolutionLucene erl = new EntityResolutionLucene(_rtMap.getRTMModel());
        int rowC = _rtMap.getModel().getRowCount();
        
        for (int i=0; i < rowC; i++ ) {
	        String queryString = erl.prepareLQuery(getMappingValue(), i);
	        if (queryString == null || queryString.equals("") == true) {
	            ConsoleFrame.addText("\nEmpty Query for Row:"+i);
	            continue;
	        }
	        
	        Query qry = _simcheck.parseQuery(queryString);
	        Hits hit = _simcheck.searchIndex(qry);
	        if (hit == null || hit.length() <= 0) {
	        	ConsoleFrame.addText("\nNo Record Found for Row:" +i);
	            continue;
	        }
	        
	        // Iterate over the Documents in the Hits object
	        for (int j = 0; j < hit.length(); j++) {
	            try {
	            	if (oneMapping == true && j ==1) break; // break inner loop
	                Document doc = hit.doc(j);
	                String rowid = doc.get("at__rowid__");
	                Object[] row = null, rowMap = null;
	                row = _rt.getRow(Integer.parseInt(rowid));
	                rowMap = _rtMap.getRow(i);
	                outputRT.addFillRow(rowMap,row);
	            } catch (Exception e) {
	                ConsoleFrame.addText("\n " + e.getMessage());
	                ConsoleFrame.addText("\n Error: Can not open Document");
	            }
	        }
        }

        _simcheck.closeSeachIndex();

        JPanel jp_p = new JPanel(new BorderLayout());
        jp_p.add(outputRT, BorderLayout.CENTER);
        // Show the table now
        dg = new JFrame("Entity Resolution Frame");
        dg.setLocation(250, 100);
        dg.getContentPane().add(jp_p);
        dg.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dg.pack();
        QualityListener.bringToFront(dg);
    }



    public void itemStateChanged(ItemEvent event) {

        if (event.getStateChange() == ItemEvent.SELECTED ) {
            JComboBox<String> selCombo = (JComboBox<String>) event.getSource();
            String s = event.getItem().toString();
            int index = Integer.parseInt(selCombo.getActionCommand());

            if("Not Applicable".equalsIgnoreCase(s) == true) {
                sLower[index].setEnabled(false);
                sHigher[index].setEnabled(false);

            } else {

                if("Starts With".equalsIgnoreCase(s) == true ||
                		"Ends With".equalsIgnoreCase(s) == true) {
                	sLower[index].setToolTipText("Number of characters to match");
                    sLower[index].setEnabled(true);
                    sHigher[index].setEnabled(false);
                }
                if("Exact Match".equalsIgnoreCase(s) == true||
                		"Similar-Any Word".equalsIgnoreCase(s)  == true ||
                        "Similar-All Words".equalsIgnoreCase(s) == true	) {
                    sLower[index].setEnabled(false);
                    sHigher[index].setEnabled(false);

                }
                if(s.startsWith("Range Bound - Number")  == true ) {
                	sLower[index].setToolTipText("Lower Bound in Number");
                	sHigher[index].setToolTipText("Higher Bound in Number");
                    sLower[index].setEnabled(true);
                    sHigher[index].setEnabled(true);

                }
                if(s.startsWith("Range Bound - Date")  == true ) {
                	sLower[index].setToolTipText("Lower Bound in Seconds");
                	sHigher[index].setToolTipText("Higher Bound in Seconds");
                    sLower[index].setEnabled(true);
                    sHigher[index].setEnabled(true);

                }
            } // end of else
        }
    } // end of item state

    private boolean validateInput() {
        for (int j=0; j < colName.length; j++) {

            if (sLower[j].isEnabled() == true || sHigher[j].isEnabled() == true) {
                if (sLower[j].getText() == null  || sHigher[j].getText() == null) {

                    JOptionPane.showMessageDialog(null,
                            "Range Value can not be empty");
                    return false;
                }
            }
        }
        
        // Now fill EntityResoution
        mappingV = new Vector<EntityResolutionLucene.MappingClass>();
        for (int i=0; i<colName.length; i++ ) {
        	JComboBox<String> t = sType[i];
        	int index = t.getSelectedIndex();
        	if (index == 0) continue;
        	
        	EntityResolutionLucene.MappingClass mc = new EntityResolutionLucene.MappingClass(colName[i],mcolName[i].getSelectedItem().toString());
        	mc.setMappingType(index);
        	
        	if (index == 4 || index == 7) { // range Bound
        		mc.setLowerrange(sLower[i].getText());
        		mc.setUpperrange(sHigher[i].getText());
        	}
        	if (index == 5)  { // starts with
        		mc.setStartswith(sLower[i].getText());
        	}
        	if (index == 6) { // Ends with
        		mc.setEndsswith(sLower[i].getText());
        	}
        	mappingV.add(mc);
        }
        return true;
    }
    
    public EntityResolutionLucene.MappingClass[] getMappingValue() {
    	EntityResolutionLucene.MappingClass[] a = new EntityResolutionLucene.MappingClass[mappingV.size()];
    	return a = mappingV.toArray(a);
    }
} // End of Multi Facet panel