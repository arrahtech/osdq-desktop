package org.arrah.gui.swing;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_NewConn;
import org.arrah.framework.xml.FilePaths;
import org.arrah.framework.xml.XmlReader;
import org.arrah.framework.xml.XmlWriter;


public class BusinessRules extends javax.swing.JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private Hashtable<String, String> hashTable, hashRule;
    private XmlReader xmlReader = null;
    private XmlWriter xmlWriter = null;
    private DefaultComboBoxModel<String> comboModel;
    private QueryBuilder q_b;
    private List<String> db_tables, db_cols;

    /**
     * Creates new form BusinessRules
     */
    public BusinessRules() {
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

        String temp[] = xmlReader.getRulesName(new File(FilePaths.getFilePathRules()), "rule", "rule_Name");
        if (temp != null) {
            for (int i = 0; i < temp.length; i++) {
                jcbRule.addItem(temp[i].trim());
            }
        }
    }

    public void loadDatabaseConnections() {
        jcbConnection.removeAllItems();
        jcbConnection.addItem("Select Database Connection");
        xmlReader = new XmlReader();

        String temp[] = xmlReader.getRulesName(new File(FilePaths.getFilePathDB()), "entry", "database_ConnectionName");
        if (temp != null) {
            for (int i = 0; i < temp.length; i++) {
                jcbConnection.addItem(temp[i].trim());
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
                        hashRule = xmlReader.getRuleDetails(new File(FilePaths.getFilePathRules()), "rule", jcbRule.getSelectedItem().toString());
                        
//                        hashTable = new Hashtable<String, String>();
//                        hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", hashRule.get("database_ConnectionName"));
//                        hashRule.put("Database_Type", hashTable.get("Database_Type"));

                        txtQuery.setText(hashRule.get("query_Text"));
                        comboModel = (DefaultComboBoxModel<String>) jcbConnection.getModel();

                        jcbConnection.setSelectedIndex(comboModel.getIndexOf(hashRule.get("database_ConnectionName")));

                        txtDescription.setText(hashRule.get("rule_Description"));
                        txtTables.setText(hashRule.get("table_Names"));
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

        jcbConnection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbConnection.getSelectedIndex() != 0) {
                    	
                            jcbTable.removeAllItems();
                            jcbTable.addItem("Show Tables");

                            xmlReader = new XmlReader();
                            
                            hashTable = new Hashtable<String, String>();
                            hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", jcbConnection.getSelectedItem().toString());
                            
                         // This file may not have passwd stored so if it is not stored ask for it
            				String passwd = (String) hashTable.get("Database_Passwd");
            				String dsn = (String) hashTable.get("Database_DSN");
            				
            				if ((dsn != null && ("".equals(dsn) == false)) && (passwd == null || "".equals(passwd) || passwd.matches("\\*.*") == true)) {
            					//passwd = JOptionPane.showInputDialog("Enter Password to Connect DB:"+ dsn);
            					passwd = UIUtilities.getMaskedString("Enter Password to Connect DB:"+ dsn);
            					hashTable.put("Database_Passwd",passwd);
            				}
            				
                            // System.out.println(hashTable.toString());
                            db_tables = new ArrayList<String>();
                            q_b = new QueryBuilder();
                            db_tables = q_b.get_all_tables(hashTable);

                            for (int i = 0; i < db_tables.size(); i++) {
                                jcbTable.addItem(db_tables.get(i));
                            }
                    } else {
                        jcbTable.removeAllItems();
                        jcbTable.addItem("Show Tables");
                    }
                }
            }
        });

        jcbTable.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbTable.getSelectedIndex() != 0) {

                        jcbColumn.removeAllItems();
                        jcbColumn.addItem("Show Columns");
                        xmlReader = new XmlReader();
//                        hashTable = new Hashtable<String, String>();
//                        hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", jcbConnection.getSelectedItem().toString());
                        db_cols = new ArrayList<String>();
                        db_cols = q_b.get_all_cols(hashTable, jcbTable.getSelectedItem().toString());

                        for (int j = 0; j < db_cols.size(); j++) {
                            jcbColumn.addItem(db_cols.get(j));
                        }

                    } else {
                        jcbColumn.removeAllItems();
                        jcbColumn.addItem("Show Columns");
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
        jcbConnection.setSelectedIndex(0);
        jcbTable.setSelectedIndex(0);
        txtTables.setText("");
        jcbColumn.setSelectedIndex(0);

        txtValue.setText("");

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
        txtRule = new javax.swing.JTextField();
        jspDescription = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();

        jcbConnection = new javax.swing.JComboBox<String>();
        jcbTable = new javax.swing.JComboBox<String>();

        txtTables = new javax.swing.JTextArea();
        jcbColumn = new javax.swing.JComboBox<String>();

        txtValue = new javax.swing.JTextField();


        btnClose = new javax.swing.JButton();
        btnCreate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();

        btnModify = new javax.swing.JButton();
        btnValidate = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Business Rule Builder Dialog");

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

        txtRule.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtRule.setMaximumSize(new java.awt.Dimension(240, 25));
        txtRule.setMinimumSize(new java.awt.Dimension(240, 25));
        txtRule.setPreferredSize(new java.awt.Dimension(240, 25));

        jspDescription.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jspDescription.setPreferredSize(new java.awt.Dimension(243, 70));

        txtDescription.setColumns(20);
        txtDescription.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtDescription.setLineWrap(true);
        txtDescription.setRows(5);
        txtDescription.setWrapStyleWord(true);
        jspDescription.setViewportView(txtDescription);

        jcbConnection.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbConnection.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Database Connection" }));
        jcbConnection.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbConnection.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbConnection.setPreferredSize(new java.awt.Dimension(240, 25));

        jcbTable.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbTable.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Show Tables" }));
        jcbTable.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbTable.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbTable.setPreferredSize(new java.awt.Dimension(240, 25));


        txtTables.setEditable(false);
        txtTables.setColumns(20);
        txtTables.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtTables.setLineWrap(true);
        txtTables.setRows(5);
        txtTables.setWrapStyleWord(true);


        jcbColumn.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbColumn.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Show Columns" }));
        jcbColumn.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbColumn.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbColumn.setPreferredSize(new java.awt.Dimension(240, 25));

        txtValue.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtValue.setMaximumSize(new java.awt.Dimension(240, 25));
        txtValue.setMinimumSize(new java.awt.Dimension(240, 25));
        txtValue.setPreferredSize(new java.awt.Dimension(240, 25));


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
        queryL = new javax.swing.JLabel("Query for Business Rule:");

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
                    			
                      .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    		.addComponent(queryL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    		.addComponent(jspQuery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    		.addComponent(jcbRule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ruleDescL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jspDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnValidate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                      
            	))
               .addGroup(layout.createSequentialGroup()
            		    .addGap(5, 5, 5)
            		    .addComponent(jcbConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE) //added
            			.addGap(5, 5, 5)
            		    .addComponent(jcbTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jcbColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE) 
                 )
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
	                
                .addComponent(jcbRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(jcbConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE) //added
	                .addComponent(jcbTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addComponent(jcbColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	             )
                .addGap(10, 10, 10)
	            .addComponent(ruleDescL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jspDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 
	            .addGap(25, 25, 25)
                .addComponent(queryL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        } else if (jcbConnection.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "Please select DB Connection name!!!");
            jcbConnection.requestFocus();
        } else {
            hashRule = new Hashtable<>();
            hashRule.put("rule_Name", txtRule.getText().trim());
            hashRule.put("database_ConnectionName", jcbConnection.getSelectedItem().toString());
            hashRule.put("rule_Type", "JOIN");
            hashRule.put("table_Names", txtTables.getText().trim());
            hashRule.put("column_Names", "");
            hashRule.put("condition_Names", "");
            hashRule.put("query_Text", txtQuery.getText().trim());
            if( txtDescription.getText().trim().equals("") ) {
                hashRule.put("rule_Description", "");
            } else {
                hashRule.put("rule_Description", txtDescription.getText().trim());
            }

//            hashTable = new Hashtable<>();
//            hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", hashRule.get("database_ConnectionName"));
//            hashRule.put("Database_Type", hashTable.get("Database_Type"));
            
           // txtQuery.setText(new QueryBuilder().getJoinQuery(hashRule));
            txtQuery.setText(txtQuery.getText().trim());
            
            xmlWriter = new XmlWriter();
            xmlWriter.writeXmlFile(hashRule);
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
            xmlWriter.deleteNode(txtRule.getText().trim());
            JOptionPane.showMessageDialog(null, "Business Rule \"" + txtRule.getText() + "\" successfully deleted!!!");
            clearData();
            loadBusinessRules();
        }
    }//GEN-LAST:event_btnDeleteActionPerformed
    
    private void btnValidateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
    	try {
    		
    	if (jcbConnection.getSelectedIndex() == 0 ) {
    		JOptionPane.showMessageDialog(null, "Please select a connection");
    		return;
    	}
    	if (txtQuery.getText() == null || txtQuery.getText().trim().equalsIgnoreCase("")) {
    		JOptionPane.showMessageDialog(null, "Please Enter Query to Validate");
    		return;
    	}
//    	hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", jcbConnection.getSelectedItem().toString());
    	Rdbms_NewConn dbmsConn = new Rdbms_NewConn(hashTable);
        dbmsConn.openConn();
        System.out.println("Connected to " + hashTable.get("Database_ConnName"));
        
        String native_s = dbmsConn.checkAndReturnSql(txtQuery.getText());
        JOptionPane.showMessageDialog(null, "Query OK \n Native Query:"+native_s);
       
        dbmsConn.closeConn();

	    } catch (SQLException ex) {
	    	JOptionPane.showMessageDialog(null, "Exception:"+ex.getLocalizedMessage());
	    }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnModifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModifyActionPerformed
        if (jcbConnection.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "Please select the database connection name!!!");
            jcbConnection.requestFocus();
        }  else {
                hashRule = new Hashtable<>();
                hashRule.put("rule_Name", txtRule.getText().trim());
                hashRule.put("database_ConnectionName", jcbConnection.getSelectedItem().toString());
                hashRule.put("rule_Type", "JOIN");
                hashRule.put("table_Names", txtTables.getText().trim());
                hashRule.put("column_Names", "");
                hashRule.put("condition_Names", "");
                if (txtQuery.getText() != null)
                	txtQuery.setText(txtQuery.getText().trim());
                hashRule.put("query_Text", txtQuery.getText());
//                hashTable = new Hashtable<>();
//                hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", hashRule.get("database_ConnectionName"));
//                hashRule.put("Database_Type", hashTable.get("Database_Type"));
                if( txtDescription.getText().trim().equals("") ) {
                    hashRule.put("rule_Description", "");
                } else {
                    hashRule.put("rule_Description", txtDescription.getText().trim());
                }
                
                xmlWriter = new XmlWriter();
                xmlWriter.modifyRule(hashRule);
                JOptionPane.showMessageDialog(null, "Business Rule \"" + txtRule.getText() + "\" successfully modified!!!");
                clearData();
                loadBusinessRules();
        }
    }//GEN-LAST:event_btnModifyActionPerformed

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
   
    private javax.swing.JComboBox<String> jcbColumn;
    private javax.swing.JComboBox<String> jcbConnection;
    private javax.swing.JComboBox<String> jcbRule;
    private javax.swing.JComboBox<String> jcbTable;


    private javax.swing.JScrollPane jspDescription;
    private javax.swing.JScrollPane jspQuery;

    private javax.swing.JTextArea txtQuery;
    private javax.swing.JTextField txtRule;
    private javax.swing.JTextArea txtTables;
    private javax.swing.JTextField txtValue;
    private javax.swing.JTextArea  txtDescription;
    
    private javax.swing.JCheckBox  nRuleCheck;
    private javax.swing.JLabel ruleDescL,queryL;
    
    
    // End of variables declaration//GEN-END:variables


}