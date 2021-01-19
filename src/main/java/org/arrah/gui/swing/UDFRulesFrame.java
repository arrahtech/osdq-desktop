package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2020      *
 *     http://www.arrahtech.com                *
 *                                             *
 * Any part of code or file can be changed,    *
 * redistributed, modified with copyright      *
 * information intact                          *
 *                                             *
 * Author$ : Vivek Singh                       *
 *                                             *
 ***********************************************/

/*
 * This file is used for implementing business 
 * rules from UDF. This file is changed from
 * BusinessRulesFrame.java
 * 
 */

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.arrah.framework.ndtable.DisplayFileAsTableCore;
import org.arrah.framework.ndtable.ReportTableModel;
import org.arrah.framework.udf.UDFEvaluator;
import org.arrah.framework.udf.UDFInterfaceToRTM;
import org.arrah.framework.xml.FilePaths;
import org.arrah.framework.xml.XmlReader;
import org.arrah.framework.xml.XmlWriter;


public class UDFRulesFrame extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
    private Hashtable<String, String> hashRule;
    private XmlReader xmlReader = null;
    private XmlWriter xmlWriter = null;
    
    
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnModify;
    private javax.swing.JButton btnValidate; // Validate the query
   
    private javax.swing.JComboBox<String> jcbRule;
    private javax.swing.JComboBox<String> udfSelectionCombo;


    private javax.swing.JScrollPane jspDescription;
    private javax.swing.JScrollPane jspQuery;

    private javax.swing.JTextField txtRule, txtFileLocation;
    
    private javax.swing.JTextArea txtQuery;
    private javax.swing.JTextArea  txtDescription;
    
    private javax.swing.JCheckBox  nRuleCheck;
    private javax.swing.JLabel ruleDescL,columnSelectionL,fileLocationL;
    
    
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form BusinessRules
     */
    public UDFRulesFrame() {
        initComponents();

        loadGui();
        addListeners();
    }

    private void loadGui() {

        Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) screenSize.getWidth() - 200, (int) screenSize.getHeight() - scnMax.bottom - 100);

        setLayout(null);
        getContentPane().add(jspQuery);

        setLocationRelativeTo(null);

    }

    public void loadBusinessRules() {
        jcbRule.removeAllItems();
        jcbRule.addItem("Select Existing Rule");
        xmlReader = new XmlReader();

        String temp[] = xmlReader.getRulesName(new File(FilePaths.getFilePathUDFRules()), "rule", "rule_Name");
        if (temp != null) {
            for (int i = 0; i < temp.length; i++) {
                jcbRule.addItem(temp[i].trim());
            }
        }
    }

    private void addListeners() {

        jcbRule.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbRule.getSelectedIndex() != 0) {
                        btnCreate.setEnabled(false);
                        btnModify.setEnabled(true);
                        btnDelete.setEnabled(true);

                        txtRule.setText(jcbRule.getSelectedItem().toString());
                        txtRule.setEditable(false);
                        
                        xmlReader = new XmlReader();
                        hashRule = new Hashtable<>();
                        hashRule = xmlReader.getRuleDetails(new File(FilePaths.getFilePathUDFRules()), "rule", jcbRule.getSelectedItem().toString());

                        txtQuery.setText(hashRule.get("column_Names"));
                        txtDescription.setText(hashRule.get("rule_Description"));
                        udfSelectionCombo.setSelectedItem(hashRule.get("rule_Type"));
                        txtFileLocation.setText(hashRule.get("table_Names"));
                        
                        nRuleCheck.setSelected(false);

                    } else {
                        txtRule.setText("New Rule Name");
                        txtRule.setEditable(true);
                        clearData();
                        nRuleCheck.setSelected(true);
                    }
                }
            }
        });
        
        nRuleCheck.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                	jcbRule.setSelectedIndex(0);
                	jcbRule.setEnabled(false);
                	txtRule.setText("New Rule Name");
                } else {
                	btnCreate.setEnabled(false);
                	jcbRule.setEnabled(true);
                }
            }
        });


    }

    private void clearData() {
    	
        txtRule.setText("");
        txtFileLocation.setText("");
        txtDescription.setText("");
        txtQuery.setText("");

        btnCreate.setEnabled(true);
        btnModify.setEnabled(false);
        btnDelete.setEnabled(false);
        btnClear.setEnabled(true);
        btnClose.setEnabled(true);
        txtDescription.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jspQuery = new javax.swing.JScrollPane();
        txtQuery = new javax.swing.JTextArea();

        jcbRule = new javax.swing.JComboBox<String>();
        udfSelectionCombo = new javax.swing.JComboBox<String>();
        
        txtRule = new javax.swing.JTextField();
        jspDescription = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        
        txtFileLocation = new javax.swing.JTextField();


        btnClose = new javax.swing.JButton();
        btnCreate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();

        btnModify = new javax.swing.JButton();
        btnValidate = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("UDF Rule Builder Dialog");

        jspQuery.setPreferredSize(new java.awt.Dimension(226, 100));
 
        txtQuery.setEditable(true); // can write query and edit
        txtQuery.setColumns(20);
        txtQuery.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtQuery.setLineWrap(true);
        txtQuery.setRows(5);
        txtQuery.setWrapStyleWord(true);
        jspQuery.setViewportView(txtQuery);



        jcbRule.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbRule.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Existing Rule" }));
        jcbRule.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbRule.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbRule.setPreferredSize(new java.awt.Dimension(240, 25));
        
        udfSelectionCombo.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        udfSelectionCombo.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Existing UDF Metic" }));
        udfSelectionCombo.setMaximumSize(new java.awt.Dimension(240, 25));
        udfSelectionCombo.setMinimumSize(new java.awt.Dimension(240, 25));
        udfSelectionCombo.setPreferredSize(new java.awt.Dimension(240, 25));

        txtRule.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtRule.setMaximumSize(new java.awt.Dimension(240, 25));
        txtRule.setMinimumSize(new java.awt.Dimension(240, 25));
        txtRule.setPreferredSize(new java.awt.Dimension(240, 25));
        
        txtFileLocation.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtFileLocation.setMaximumSize(new java.awt.Dimension(240, 25));
        txtFileLocation.setMinimumSize(new java.awt.Dimension(240, 25));
        txtFileLocation.setPreferredSize(new java.awt.Dimension(240, 25));

        jspDescription.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jspDescription.setPreferredSize(new java.awt.Dimension(243, 70));

        txtDescription.setColumns(20);
        txtDescription.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtDescription.setLineWrap(true);
        txtDescription.setRows(5);
        txtDescription.setWrapStyleWord(true);
        jspDescription.setViewportView(txtDescription);


        btnClose.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        btnClose.setText("CLOSE");
        btnClose.setMaximumSize(new java.awt.Dimension(110, 30));
        btnClose.setMinimumSize(new java.awt.Dimension(110, 30));
        btnClose.setPreferredSize(new java.awt.Dimension(110, 30));
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnCreate.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        btnCreate.setText("CREATE");
        btnCreate.setMaximumSize(new java.awt.Dimension(110, 30));
        btnCreate.setMinimumSize(new java.awt.Dimension(110, 30));
        btnCreate.setPreferredSize(new java.awt.Dimension(110, 30));
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });

        btnDelete.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        btnDelete.setText("DELETE");
        btnDelete.setMaximumSize(new java.awt.Dimension(110, 30));
        btnDelete.setMinimumSize(new java.awt.Dimension(110, 30));
        btnDelete.setPreferredSize(new java.awt.Dimension(110, 30));
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnClear.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        btnClear.setText("CLEAR");
        btnClear.setMaximumSize(new java.awt.Dimension(110, 30));
        btnClear.setMinimumSize(new java.awt.Dimension(110, 30));
        btnClear.setPreferredSize(new java.awt.Dimension(110, 30));
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });


        btnModify.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        btnModify.setText("MODIFY");
        btnModify.setMaximumSize(new java.awt.Dimension(110, 30));
        btnModify.setMinimumSize(new java.awt.Dimension(110, 30));
        btnModify.setPreferredSize(new java.awt.Dimension(110, 30));
        btnModify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModifyActionPerformed(evt);
            }
        });
        
        btnValidate.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        btnValidate.setText("Validate");
        btnValidate.setMaximumSize(new java.awt.Dimension(110, 30));
        btnValidate.setMinimumSize(new java.awt.Dimension(110, 30));
        btnValidate.setPreferredSize(new java.awt.Dimension(110, 30));
        btnValidate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValidateActionPerformed(evt);
            }
        });
        
        nRuleCheck = new javax.swing.JCheckBox("New Rule");
        nRuleCheck.setPreferredSize(new java.awt.Dimension(100, 50));
        
        ruleDescL = new javax.swing.JLabel("Rule Description:");
        columnSelectionL = new javax.swing.JLabel("Comma Separated Columns for UDF");
        fileLocationL = new javax.swing.JLabel("File Location:");
        
        // Now load the udf
        ConcurrentHashMap<String, Method> funcName = UDFEvaluator.getMetricUdf();

		Enumeration<String> funckey =  funcName.keys();
		
		while (funckey.hasMoreElements()) {
			udfSelectionCombo.addItem(funckey.nextElement());
		}

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                		
                     .addGroup(layout.createSequentialGroup()
                    	.addComponent(nRuleCheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    	.addComponent(txtRule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    	
                    .addGroup(layout.createSequentialGroup()
                    		.addComponent(jcbRule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    		.addComponent(fileLocationL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    		.addComponent(txtFileLocation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    		
                      .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    		.addComponent(columnSelectionL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    		.addComponent(jspQuery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ruleDescL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jspDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnValidate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(udfSelectionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            
            	))
            	
              .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                   .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                   .addComponent(btnModify, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                   .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                   .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                   .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
   
           );
        
        layout.setVerticalGroup(
        	layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                .addComponent(nRuleCheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addComponent(txtRule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                
	            .addGap(5, 5, 5)
	            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jcbRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(fileLocationL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(txtFileLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE) )
                
                .addGap(10, 10, 10)
	            .addComponent(ruleDescL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jspDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(udfSelectionCombo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 
	            .addGap(25, 25, 25)
                .addComponent(columnSelectionL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jspQuery, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap()
                .addGap(5, 5, 5)
                .addComponent(btnValidate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap()
	            .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnModify, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    )
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearData();
        jcbRule.setSelectedIndex(0);
        txtQuery.setText("");
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
    	
        if (txtRule.getText().toString().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter the rule name to be created!!!");
            
            txtRule.requestFocus();
        } else {
            hashRule = new Hashtable<>();
            hashRule.put("rule_Name", txtRule.getText().trim());
            hashRule.put("database_ConnectionName", "");
            hashRule.put("rule_Type", udfSelectionCombo.getSelectedItem().toString()); // Default
            hashRule.put("table_Names", txtFileLocation.getText().trim());
            hashRule.put("column_Names", txtQuery.getText().trim());
            hashRule.put("condition_Names", "");
            hashRule.put("query_Text","" );
            if( txtDescription.getText().trim().equals("") ) {
                hashRule.put("rule_Description", "");
            } else {
                hashRule.put("rule_Description", txtDescription.getText().trim());
            }

            txtQuery.setText(txtQuery.getText().trim());
            
            xmlWriter = new XmlWriter();
            xmlWriter.writeXmlFile(hashRule,"udfrule");
            JOptionPane.showMessageDialog(null, "Business Rule with name \"" + txtRule.getText() + "\" successfully created!!!");
            jcbRule.setSelectedIndex(0);
            clearData();
            loadBusinessRules();
        }
    }//GEN-LAST:event_btnCreateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (jcbRule.getSelectedIndex() == 0 ) {
            JOptionPane.showMessageDialog(null, "Please select a rule name to delete!!!");
        } else {
            xmlWriter = new XmlWriter();
            xmlWriter.deleteNode(txtRule.getText().trim(),"udfrule");
            JOptionPane.showMessageDialog(null, "Business Rule \"" + txtRule.getText() + "\" successfully deleted!!!");
            clearData();
            loadBusinessRules();
        }
    }//GEN-LAST:event_btnDeleteActionPerformed
    
    private void btnValidateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
    	try {
    		
    	if (txtQuery.getText() == null || txtQuery.getText().trim().equalsIgnoreCase("")) {
    		JOptionPane.showMessageDialog(null, "Please Enter Columns ( comma separated) to Validate");
    		return;
    	}
    	
    	String exp = txtQuery.getText().trim();
    	String fileLocation = txtFileLocation.getText().trim();
    	
		UDFInterfaceToRTM.evalUDF(udfSelectionCombo.getSelectedItem().toString(), new DisplayFileAsTableCore().loadFileIntoRTM(fileLocation), Arrays.asList(exp.split(",")) );
		ReportTableModel rtmmodel = UDFInterfaceToRTM.metricrtm;
		
		JDialog jd = new JDialog();
		jd.setTitle("UDF Metric Display Dialog");
		jd.setLocation(75, 75);
		jd.getContentPane().add(new ReportTable(rtmmodel));
		jd.pack();
		jd.setVisible(true);
		
		return;

	    } catch (Exception ex) {
	    	JOptionPane.showMessageDialog(null, "Exception:"+ex.getLocalizedMessage());
	    }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnModifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModifyActionPerformed

        hashRule = new Hashtable<>();
        hashRule.put("rule_Name", txtRule.getText().trim());
        hashRule.put("database_ConnectionName", "");
        hashRule.put("rule_Type", udfSelectionCombo.getSelectedItem().toString()); // Default
        hashRule.put("table_Names", txtFileLocation.getText().trim());
        hashRule.put("column_Names", txtQuery.getText().trim());
        hashRule.put("condition_Names", "");
        if (txtQuery.getText() != null)
        	txtQuery.setText(txtQuery.getText().trim());
        
        hashRule.put("query_Text", "");
        if( txtDescription.getText().trim().equals("") ) {
            hashRule.put("rule_Description", "");
        } else {
            hashRule.put("rule_Description", txtDescription.getText().trim());
        }
        
        xmlWriter = new XmlWriter();
        xmlWriter.modifyRule(hashRule,"udfrule");
        JOptionPane.showMessageDialog(null, "Business Rule \"" + txtRule.getText() + "\" successfully modified!!!");
        clearData();
        loadBusinessRules();

    }//GEN-LAST:event_btnModifyActionPerformed

} // End of UDFRulesFrame class