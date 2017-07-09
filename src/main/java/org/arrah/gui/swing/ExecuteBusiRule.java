package org.arrah.gui.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_NewConn;
import org.arrah.framework.xml.FilePaths;
import org.arrah.framework.xml.XmlReader;


public class ExecuteBusiRule extends javax.swing.JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String query = null, dbConnName;
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    ResultSetMetaData rsmd;
    DefaultTableModel model;
    TableCellRenderer rendererFromHeader;
    int count = 0;
    XmlReader xmlReader = null;
    Hashtable<String, String> hashTable, hashRule;
    int i;

    /**
     * Creates new form ExecuteBRule
     */
    public ExecuteBusiRule() {
        initComponents();

        loadGui();
        loadRules();
        addListeners();

    }

    private void loadGui() {

        Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) screenSize.getWidth(), (int) screenSize.getHeight() - scnMax.bottom);

        pnlExeRule.setLocation(new Point(10, 10));
        pnlExeRule.setSize(new Dimension(getWidth() - 40, getHeight() - 110));
        getContentPane().add(pnlExeRule);

        btnClose.setLocation(new Point(getWidth() - 140, getHeight() - 85));
        getContentPane().add(btnClose);

        setLocationRelativeTo(null);
    }

    public void loadRules() {
        jcbBrules.removeAllItems();
        jcbBrules.addItem("Select Rule Name");
        xmlReader = new XmlReader();

        String temp[] = xmlReader.getRulesName(new File(FilePaths.getFilePathRules()), "rule", "rule_Name");
        if (temp != null) {
            for (int j = 0; j < temp.length; j++) {
                jcbBrules.addItem(temp[j].trim());
            }
        }
    }
    
    private void addListeners() {
        
        jcbBrules.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbBrules.getSelectedIndex() != 0) {
                        xmlReader = new XmlReader();
                        hashRule = new Hashtable<>();
                        hashRule = xmlReader.getRuleDetails(new File(FilePaths.getFilePathRules()), "rule", jcbBrules.getSelectedItem().toString());
                        
                        System.out.println(hashRule.get("rule_Name") + ":" + hashRule.get("database_ConnectionName") + ":" + hashRule.get("rule_Type") + ":" + hashRule.get("table_Names") + ":" + hashRule.get("column_Names") + ":" + hashRule.get("condition_Names"));
                        dbConnName = hashRule.get("database_ConnectionName");
                        
                        hashTable = new Hashtable<>();
                        hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", hashRule.get("database_ConnectionName"));
                        
                        hashRule.put("Database_Type", hashTable.get("Database_Type"));
                        switch (hashRule.get("rule_Type")) {
                            case "JOIN":
                                txtBusirule.setText(new QueryBuilder().getJoinQuery(hashRule));
                                break;
                            case "NON-JOIN":
                                txtBusirule.setText(new QueryBuilder().getNonJoinQuery(hashRule));
                                break;
                        }
                    } else {
                        txtBusirule.setText("");
                    }
                }
            }
        });
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlExeRule = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jcbBrules = new javax.swing.JComboBox<>();
        txtBusirule = new javax.swing.JTextField();
        btnExecute = new javax.swing.JButton();
        jspRuleResult = new javax.swing.JScrollPane();
        jtbRuleResult = new javax.swing.JTable();
        btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Execute Business Rule Dialog");

        pnlExeRule.setBackground(new java.awt.Color(255, 255, 255));
        pnlExeRule.setBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(0, 0, 0), null));

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel1.setText("Business Rule Name");
        jLabel1.setMaximumSize(new java.awt.Dimension(150, 25));
        jLabel1.setMinimumSize(new java.awt.Dimension(150, 25));
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 25));

        jcbBrules.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbBrules.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Rule Name" }));
        jcbBrules.setMaximumSize(new java.awt.Dimension(250, 25));
        jcbBrules.setMinimumSize(new java.awt.Dimension(250, 25));
        jcbBrules.setPreferredSize(new java.awt.Dimension(250, 25));

        txtBusirule.setEditable(false);
        txtBusirule.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtBusirule.setMaximumSize(new java.awt.Dimension(2147483647, 25));
        txtBusirule.setMinimumSize(new java.awt.Dimension(6, 25));
        txtBusirule.setOpaque(false);
        txtBusirule.setPreferredSize(new java.awt.Dimension(6, 25));

        btnExecute.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        btnExecute.setText("EXECUTE");
        btnExecute.setMaximumSize(new java.awt.Dimension(100, 35));
        btnExecute.setMinimumSize(new java.awt.Dimension(100, 35));
        btnExecute.setPreferredSize(new java.awt.Dimension(100, 35));
        btnExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExecuteActionPerformed(evt);
            }
        });

        jspRuleResult.setBackground(new java.awt.Color(255, 255, 255));
        jspRuleResult.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jspRuleResult.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jspRuleResult.setFocusCycleRoot(true);
        jspRuleResult.setOpaque(false);

        jtbRuleResult.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jtbRuleResult.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jtbRuleResult.setOpaque(false);
        jspRuleResult.setViewportView(jtbRuleResult);

        javax.swing.GroupLayout pnlExeRuleLayout = new javax.swing.GroupLayout(pnlExeRule);
        pnlExeRule.setLayout(pnlExeRuleLayout);
        pnlExeRuleLayout.setHorizontalGroup(
            pnlExeRuleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExeRuleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExeRuleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jspRuleResult)
                    .addGroup(pnlExeRuleLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcbBrules, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(txtBusirule, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlExeRuleLayout.setVerticalGroup(
            pnlExeRuleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExeRuleLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(pnlExeRuleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbBrules, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBusirule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExecute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jspRuleResult, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnClose.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnClose.setText("CLOSE");
        btnClose.setMaximumSize(new java.awt.Dimension(110, 40));
        btnClose.setMinimumSize(new java.awt.Dimension(110, 40));
        btnClose.setPreferredSize(new java.awt.Dimension(110, 40));
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlExeRule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClose, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlExeRule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExecuteActionPerformed
        if( jcbBrules.getSelectedIndex() == 0 ) {
            JOptionPane.showMessageDialog(null, "Please select a business rule to execute!!!");
        } else {
            try {
                query = txtBusirule.getText().trim();

                xmlReader = new XmlReader();
                hashTable = new Hashtable<String, String>();
                hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", dbConnName);
                
                Rdbms_NewConn.init(hashTable);

                Rdbms_NewConn.get().openConn();
                
                rs = Rdbms_NewConn.get().execute(query);
                
                loadTableData(rs);
                
                Rdbms_NewConn.get().closeConn();
                
            } catch (  Exception  ex) {
                Logger.getLogger(ExecuteBusiRule.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Rule Execution Failed");
            }
        }
    }//GEN-LAST:event_btnExecuteActionPerformed
    
    private void loadTableData( ResultSet rs ) {
        try {
            count = 1;
            model = (DefaultTableModel) jtbRuleResult.getModel();
            for (i = model.getRowCount() - 1; i >= 0; i--) {
                model.removeRow(i);
            }
            model.setColumnCount(0);
            model.addColumn("Sr. No.");
            
            rsmd = rs.getMetaData();
            for (i = 1; i <= rsmd.getColumnCount(); i++) {
                model.addColumn(rsmd.getColumnName(i));
                jtbRuleResult.getColumnModel().getColumn(i - 1).setPreferredWidth(250);
                jtbRuleResult.getColumnModel().getColumn(i - 1).setMaxWidth(250);
                jtbRuleResult.getColumnModel().getColumn(i - 1).setMinWidth(250);
            }
            
            rendererFromHeader = jtbRuleResult.getTableHeader().getDefaultRenderer();
            JLabel headerLabel = (JLabel) rendererFromHeader;
            headerLabel.setHorizontalAlignment(JLabel.CENTER);

            count = 1;
            
            while (rs.next()) {
                Object[] resultSet = new Object[rsmd.getColumnCount()+1];
                resultSet[0] = count++;
                for( int j = 1;j<=rsmd.getColumnCount();j++ ) {
                    resultSet[j] = rs.getString(j);
                }
                model.addRow(resultSet);
            }
            
            jspRuleResult.setPreferredSize(new Dimension((250 * i), 40 * count));
            jspRuleResult.setMaximumSize(new Dimension((250 * i), 40 * count));
            jspRuleResult.setMinimumSize(new Dimension((250 * i), 40 * count));
            jtbRuleResult.setPreferredSize(new Dimension((250 * i), 20 * count));
            jtbRuleResult.setMinimumSize(new Dimension((250 * i), 20 * count));
            jtbRuleResult.setMaximumSize(new Dimension((250 * i), 20 * count));
            jtbRuleResult.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 14));
            
        } catch (SQLException ex) {
            Logger.getLogger(ExecuteBusiRule.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ExecuteBusiRule.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ExecuteBusiRule.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ExecuteBusiRule.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ExecuteBusiRule.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ExecuteBusiRule().setVisible(true);
            }
        });
    }
    
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExecute;
    private javax.swing.JLabel jLabel1;
    public static javax.swing.JComboBox<String>jcbBrules;
    public static javax.swing.JScrollPane jspRuleResult;
    public static javax.swing.JTable jtbRuleResult;
    private javax.swing.JPanel pnlExeRule;
    private javax.swing.JTextField txtBusirule;
    // End of variables declaration//GEN-END:variables
}
