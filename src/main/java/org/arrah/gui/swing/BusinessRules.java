package org.arrah.gui.swing;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.xml.FilePaths;
import org.arrah.framework.xml.XmlReader;
import org.arrah.framework.xml.XmlWriter;


public class BusinessRules extends javax.swing.JFrame implements ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DefaultMutableTreeNode rootNode, dataBase, dataTable, dataRow;
    String dbDetails[] = null, condName = null, deLimiter = null, joinCompCol = null, query = null;
    Connection conn;
    ResultSet rs, rsCol;
    DatabaseMetaData md;
    int i;
    Hashtable<String, String> hashTable, hashRule;
    XmlReader xmlReader = null;
    XmlWriter xmlWriter = null;
    DefaultComboBoxModel<String> comboModel;
    QueryBuilder q_b;
    List<String> db_tables, db_cols;

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
        setSize((int) screenSize.getWidth(), (int) screenSize.getHeight() - scnMax.bottom);

        setLayout(null);

        lblBusinessRule.setLocation(new Point(10, 0));
        getContentPane().add(lblBusinessRule);

        jspQuery.setLocation(new Point(10, 25));
        jspQuery.setSize(new Dimension( (int) getWidth() - 40, 35));
        getContentPane().add(jspQuery);

        pnlRules.setLocation(new Point(10, 60));
        pnlRules.setSize(new Dimension( (int) getWidth() - 40 , getHeight() - 100));
        getContentPane().add(pnlRules);

        setLocationRelativeTo(null);

        bgRuletype.add(jrbJoin);
        bgRuletype.add(jrbNonjoin);

        jrbNonjoin.setActionCommand("nonjoin");
        jrbJoin.setActionCommand("join");

        jrbJoin.addActionListener(this);
        jrbNonjoin.addActionListener(this);

        jcbJcolumn.setEnabled(false);
        jcbJtable.setEnabled(false);

        jrbNonjoin.setSelected(true);
    }

    public void loadBusinessRules() {
        jcbRule.removeAllItems();
        jcbRule.addItem("Select Rule Name");
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
                        hashTable = new Hashtable<String, String>();
                        hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", hashRule.get("database_ConnectionName"));
                        hashRule.put("Database_Type", hashTable.get("Database_Type"));
                        
                        if( hashRule.get("rule_Type").equals("JOIN") ) {
                            txtQuery.setText(new QueryBuilder().getJoinQuery(hashRule));
                        } else if( hashRule.get("rule_Type").equals("NON-JOIN") ) {
                            txtQuery.setText(new QueryBuilder().getNonJoinQuery(hashRule));
                        }

                        comboModel = (DefaultComboBoxModel<String>) jcbConnection.getModel();

                        jcbConnection.setSelectedIndex(comboModel.getIndexOf(hashRule.get("database_ConnectionName")));

                        if (hashRule.get("rule_Type").equals("JOIN")) {
                            jrbJoin.setSelected(true);
                            jrbNonjoin.setSelected(false);
                        } else if (hashRule.get("rule_Type").equals("NON-JOIN")) {
                            jrbJoin.setSelected(false);
                            jrbNonjoin.setSelected(true);

                            comboModel = (DefaultComboBoxModel<String>) jcbTable.getModel();

                            jcbTable.setSelectedIndex(comboModel.getIndexOf(hashRule.get("table_Names")));

                        }
                        txtDescription.setText(hashRule.get("rule_Description"));
                        txtTables.setText(hashRule.get("table_Names"));
                        txtColumns.setText(hashRule.get("column_Names"));
                        txtCondition.setText(hashRule.get("condition_Names"));

                    } else {
                        txtRule.setText("");
                        txtRule.setEditable(true);
                        clearData();
                    }
                }
            }
        });

        jcbConnection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbConnection.getSelectedIndex() != 0) {
                        if (txtRule.getText().toString().trim().equals("")) {
                            JOptionPane.showMessageDialog(null, "Please enter the rule name to be created!!!");
                            txtRule.requestFocus();
                            jcbJoin.setSelectedIndex(0);
                        } else {
                            jcbTable.removeAllItems();
                            jcbTable.addItem("Select Table Name");
                            jcbCtable.removeAllItems();
                            jcbCtable.addItem("Select Table Name");
                            jcbJtable.removeAllItems();
                            jcbJtable.addItem("Select Table Name");
                            xmlReader = new XmlReader();
                            hashTable = new Hashtable<String, String>();
                            hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", jcbConnection.getSelectedItem().toString());
                            db_tables = new ArrayList<String>();
                            q_b = new QueryBuilder();

                            db_tables = q_b.get_all_tables(hashTable);

                            for (int i = 0; i < db_tables.size(); i++) {
                                jcbTable.addItem(db_tables.get(i));
                                jcbCtable.addItem(db_tables.get(i));
                                jcbJtable.addItem(db_tables.get(i));
                            }
                        }
                    } else {
                        jcbTable.removeAllItems();
                        jcbTable.addItem("Select Table Name");
                        jcbCtable.removeAllItems();
                        jcbCtable.addItem("Select Table Name");
                        jcbJtable.removeAllItems();
                        jcbJtable.addItem("Select Table Name");
                    }
                }
            }
        });

        jcbJoin.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbJoin.getSelectedIndex() != 0) {
                        if (txtRule.getText().toString().trim().equals("")) {
                            JOptionPane.showMessageDialog(null, "Please enter the rule name to be created!!!");
                            txtRule.requestFocus();
                        } else if (jcbConnection.getSelectedIndex() == 0) {
                            JOptionPane.showMessageDialog(null, "Please select DB Connection name!!!");
                            jcbConnection.requestFocus();
                        } else {
                            if (jcbJoin.getSelectedIndex() != 0) {
                                if (!jrbJoin.isSelected()) {
                                    jrbNonjoin.setSelected(false);
                                    jrbJoin.setSelected(true);
                                }
                            } else if (jcbJoin.getSelectedIndex() == 0) {
                                jrbNonjoin.setSelected(true);
                                jrbJoin.setSelected(false);
                            }
                        }
                    } else {
                        jrbNonjoin.setSelected(true);
                        jrbJoin.setSelected(false);
                    }
                }
            }
        });

        jcbTable.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbTable.getSelectedIndex() != 0) {
                        if (jrbJoin.isSelected() && jcbJoin.getSelectedIndex() == 0) {
                            JOptionPane.showMessageDialog(null, "Please select the JOIN operation to be performed!!!");
                            jcbTable.setSelectedIndex(0);
                            jcbJoin.requestFocus();
                        } else if (jrbJoin.isSelected() && jcbJoin.getSelectedIndex() != 0) {
                            if (txtTables.getText().trim().equals("")) {
                                txtTables.setText(jcbTable.getSelectedItem().toString());
                            } else {
                                txtTables.setText(txtTables.getText() + " " + jcbJoin.getSelectedItem().toString() + " " + jcbTable.getSelectedItem().toString());
                            }
                        } else if (jrbNonjoin.isSelected()) {
                            txtTables.setText(jcbTable.getSelectedItem().toString());
                        }
                        jcbColumn.removeAllItems();
                        jcbColumn.addItem("Select Column Name");
                        jcbColumn.addItem("All Fields");
                        xmlReader = new XmlReader();
                        hashTable = new Hashtable<String, String>();
                        hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", jcbConnection.getSelectedItem().toString());
                        db_cols = new ArrayList<String>();
                        db_cols = q_b.get_all_cols(hashTable, jcbTable.getSelectedItem().toString());

                        for (int j = 0; j < db_cols.size(); j++) {
                            jcbColumn.addItem(db_cols.get(j));
                        }

                    } else {
                        jcbColumn.removeAllItems();
                        jcbColumn.addItem("Select Column Name");
                        jcbColumn.addItem("All Fields");
                    }
                }
            }
        });

        jcbColumn.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbColumn.getSelectedIndex() != 0 && jrbJoin.isSelected() && jcbTable.getSelectedIndex() != 0) {
                        if (jcbColumn.getSelectedIndex() == 1) {
                            if (txtColumns.getText().trim().equals("")) {
                                txtColumns.setText(jcbTable.getSelectedItem().toString() + ".*");
                            } else {
                                txtColumns.setText(txtColumns.getText() + ", " + jcbTable.getSelectedItem().toString() + ".*");
                            }
                        } else {
                            if (txtColumns.getText().trim().equals("")) {
                                txtColumns.setText(jcbTable.getSelectedItem().toString() + "." + jcbColumn.getSelectedItem().toString());
                            } else {
                                txtColumns.setText(txtColumns.getText() + ", " + jcbTable.getSelectedItem().toString() + "." + jcbColumn.getSelectedItem().toString());
                            }
                        }
                    } else if (jrbNonjoin.isSelected()) {
                        if (jcbColumn.getSelectedIndex() == 1 && txtColumns.getText().trim().equals("")) {
                            txtColumns.setText("*");
                        } else if (jcbColumn.getSelectedIndex() > 1) {
                            if (txtColumns.getText().trim().equals("")) {
                                txtColumns.setText(jcbColumn.getSelectedItem().toString());
                            } else if (!txtColumns.getText().trim().contains("*")) {
                                txtColumns.setText(txtColumns.getText() + ", " + jcbColumn.getSelectedItem().toString());
                            }
                        }
                    }
                }
            }
        });

        jcbCtable.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbCtable.getSelectedIndex() != 0) {
                        jcbCcolumn.removeAllItems();
                        jcbCcolumn.addItem("Select Comp. Col. Name");

                        xmlReader = new XmlReader();
                        hashTable = new Hashtable<String, String>();
                        hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", jcbConnection.getSelectedItem().toString());
                        db_cols = new ArrayList<String>();
                        db_cols = q_b.get_all_cols(hashTable, jcbCtable.getSelectedItem().toString());
                        for (int j = 0; j < db_cols.size(); j++) {
                            jcbCcolumn.addItem(db_cols.get(j));
                        }
                    } else {
                        jcbCcolumn.removeAllItems();
                        jcbCcolumn.addItem("Select Comp. Col. Name");
                    }
                }
            }
        });

        jcbJtable.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbJtable.getSelectedIndex() != 0) {

                        jcbJcolumn.removeAllItems();
                        jcbJcolumn.addItem("Select Comp. Col. Name");

                        xmlReader = new XmlReader();
                        hashTable = new Hashtable<>();
                        hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", jcbConnection.getSelectedItem().toString());
                        db_cols = new ArrayList<>();
                        db_cols = q_b.get_all_cols(hashTable, jcbJtable.getSelectedItem().toString());
                        for (int j = 0; j < db_cols.size(); j++) {
                            jcbJcolumn.addItem(db_cols.get(j));
                        }
                    } else {
                        jcbJcolumn.removeAllItems();
                        jcbJcolumn.addItem("Select Comp. Col. Name");
                    }
                }
            }
        });

        jcbCondition.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbCondition.getSelectedIndex() != 0) {
                        if (jrbJoin.isSelected()) {
                            if (jcbCondition.getSelectedIndex() != 0) {
                                if (jcbCtable.getSelectedIndex() == 0) {
                                    JOptionPane.showMessageDialog(null, "Please select the first table for condition!!!");
                                    jcbCtable.requestFocus();
                                    jcbCondition.setSelectedIndex(0);
                                } else if (jcbCcolumn.getSelectedIndex() == 0) {
                                    JOptionPane.showMessageDialog(null, "Please select the first column for condition!!!");
                                    jcbCcolumn.requestFocus();
                                    jcbCondition.setSelectedIndex(0);
                                } else if (jcbJtable.getSelectedIndex() == 0) {
                                    JOptionPane.showMessageDialog(null, "Please select the second table for condition!!!");
                                    jcbJtable.requestFocus();
                                    jcbCondition.setSelectedIndex(0);
                                } else if (jcbJcolumn.getSelectedIndex() == 0) {
                                    JOptionPane.showMessageDialog(null, "Please select the second column for condition!!!");
                                    jcbJcolumn.requestFocus();
                                    jcbCondition.setSelectedIndex(0);
                                } else {
                                    condName = getCondition(jcbCondition.getSelectedItem().toString());
                                    if (txtCondition.getText().trim().equals("")) {
                                        if (condName.equals("like")) {
                                            txtCondition.setText(jcbCtable.getSelectedItem().toString() + "." + jcbCcolumn.getSelectedItem().toString() + condName + " '%" + jcbJtable.getSelectedItem().toString() + "." + jcbJcolumn.getSelectedItem().toString() + "'%");
                                        } else {
                                            txtCondition.setText(jcbCtable.getSelectedItem().toString() + "." + jcbCcolumn.getSelectedItem().toString() + condName + jcbJtable.getSelectedItem().toString() + "." + jcbJcolumn.getSelectedItem().toString());
                                        }
                                    } else {
                                        if (condName.equals("like")) {
                                            txtCondition.setText(txtCondition.getText() + jcbCtable.getSelectedItem().toString() + "." + jcbCcolumn.getSelectedItem().toString() + " " + condName + " '%" + jcbJtable.getSelectedItem().toString() + "." + jcbJcolumn.getSelectedItem().toString() + "'%");
                                        } else {
                                            txtCondition.setText(txtCondition.getText() + jcbCtable.getSelectedItem().toString() + "." + jcbCcolumn.getSelectedItem().toString() + condName + jcbJtable.getSelectedItem().toString() + "." + jcbJcolumn.getSelectedItem().toString());
                                        }
                                    }
                                    jcbCtable.setSelectedIndex(0);
                                    jcbCcolumn.setSelectedIndex(0);
                                    jcbCondition.setSelectedIndex(0);
                                    jcbJtable.setSelectedIndex(0);
                                    jcbJcolumn.setSelectedIndex(0);
                                }
                            }
                        }
                    } else {
                        jcbCtable.setSelectedIndex(0);
                        jcbCcolumn.setSelectedIndex(0);
                        jcbCondition.setSelectedIndex(0);
                        jcbJtable.setSelectedIndex(0);
                        jcbJcolumn.setSelectedIndex(0);
                    }
                }
            }
        });

        txtValue.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (jrbNonjoin.isSelected()) {
                    if (jcbCcolumn.getSelectedIndex() == 0) {
                        JOptionPane.showMessageDialog(null, "Please select a column for condition!!!");
                        jcbCtable.requestFocus();
                    } else if (jcbCondition.getSelectedIndex() == 0) {
                        JOptionPane.showMessageDialog(null, "Please select a condition!!!");
                        jcbCondition.requestFocus();
                    } else {
                        condName = getCondition(jcbCondition.getSelectedItem().toString());
                        if (txtCondition.getText().trim().equals("")) {
                            if (condName.equals("like")) {
                                txtCondition.setText(jcbCcolumn.getSelectedItem().toString() + " like " + "'%" + txtValue.getText() + "%'");
                            } else {
                                txtCondition.setText(jcbCcolumn.getSelectedItem().toString() + condName + "'" + txtValue.getText() + "'");
                            }
                        } else {
                            if (condName.equals("like")) {
                                txtCondition.setText(txtCondition.getText() + jcbCcolumn.getSelectedItem().toString() + " like " + "'%" + txtValue.getText() + "%'");
                            } else {
                                txtCondition.setText(txtCondition.getText() + jcbCcolumn.getSelectedItem().toString() + condName + "'" + txtValue.getText() + "'");
                            }
                        }
                        jcbCtable.setSelectedIndex(0);
                        jcbCcolumn.setSelectedIndex(0);
                        jcbCondition.setSelectedIndex(0);
                        txtValue.setText("");
                    }
                }
            }
        });

        jcbDelimiter.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbDelimiter.getSelectedIndex() != 0) {
                        if (jrbNonjoin.isSelected()) {
                            if (!txtCondition.getText().trim().equals("")) {
                                txtCondition.setText(txtCondition.getText() + " " + jcbDelimiter.getSelectedItem().toString() + " ");
                            }
                        } else if (jrbJoin.isSelected()) {
                            if (!txtCondition.getText().trim().equals("")) {
                                txtCondition.setText(txtCondition.getText() + " " + jcbDelimiter.getSelectedItem().toString() + " ");
                            }
                        }
                    }
                }
            }
        });

        labTdelete.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent arg0) {
                if (txtTables.getText().trim().equals("")) {
                    JOptionPane.showMessageDialog(null, "No tables selected yet to delete!!!");
                } else {
                    txtTables.setText("");
                    jcbTable.setSelectedIndex(0);
                }
            }
        });

        labCdelete.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent arg0) {
                if (txtColumns.getText().trim().equals("")) {
                    JOptionPane.showMessageDialog(null, "No columns selected yet to delete!!!");
                } else {
                    txtColumns.setText("");
                    jcbColumn.setSelectedIndex(0);
                }
            }
        });

        labVdelete.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent arg0) {
                if (txtCondition.getText().trim().equals("")) {
                    JOptionPane.showMessageDialog(null, "No columns selected yet to delete!!!");
                } else {
                    txtCondition.setText("");
                    jcbCtable.setSelectedIndex(0);
                    jcbCcolumn.setSelectedIndex(0);
                    jcbCondition.setSelectedIndex(0);
                    txtValue.setText("");
                }
            }
        });

    }

    private String getCondition(String conName) {
        String temp = null;
        if (!conName.equals("Select Condition")) {
            switch (conName) {
                case "Equal To":
                    temp = "=";
                    break;
                case "Not Equal To":
                    temp = "<>";
                    break;
                case "Greater Than":
                    temp = ">";
                    break;
                case "Greater Than Equal To":
                    temp = ">=";
                    break;
                case "Less Than":
                    temp = "<";
                    break;
                case "Less Than Equal To":
                    temp = "<=";
                    break;
                case "Like":
                    temp = "like";
                    break;
            }
        }
        return temp;
    }

    private void clearData() {
        txtRule.setText("");
        jcbConnection.setSelectedIndex(0);
        jcbTable.setSelectedIndex(0);
        txtTables.setText("");
        jcbColumn.setSelectedIndex(0);
        txtColumns.setText("");
        jcbCcolumn.setSelectedIndex(0);
        jcbCondition.setSelectedIndex(0);
        txtValue.setText("");
        txtCondition.setText("");
        jcbDelimiter.setSelectedIndex(0);
        jcbJoin.setSelectedIndex(0);
        jrbJoin.setSelected(false);
        jrbNonjoin.setSelected(true);
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

        bgRuletype = new javax.swing.ButtonGroup();
        jspQuery = new javax.swing.JScrollPane();
        txtQuery = new javax.swing.JTextArea();
        pnlRules = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jcbRule = new javax.swing.JComboBox<String>();
        txtRule = new javax.swing.JTextField();
        jspDescription = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        jrbNonjoin = new javax.swing.JRadioButton();
        jrbJoin = new javax.swing.JRadioButton();
        jcbJoin = new javax.swing.JComboBox<String>();
        jcbConnection = new javax.swing.JComboBox<String>();
        jcbTable = new javax.swing.JComboBox<String>();
        jspTables = new javax.swing.JScrollPane();
        txtTables = new javax.swing.JTextArea();
        jcbColumn = new javax.swing.JComboBox<String>();
        jspColumns = new javax.swing.JScrollPane();
        txtColumns = new javax.swing.JTextArea();
        jcbCcolumn = new javax.swing.JComboBox<String>();
        jcbCondition = new javax.swing.JComboBox<String>();
        txtValue = new javax.swing.JTextField();
        jcbDelimiter = new javax.swing.JComboBox<String>();
        jspCondition = new javax.swing.JScrollPane();
        txtCondition = new javax.swing.JTextArea();
        btnClose = new javax.swing.JButton();
        btnCreate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        labTdelete = new javax.swing.JLabel();
        labVdelete = new javax.swing.JLabel();
        labCdelete = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jcbCtable = new javax.swing.JComboBox<String>();
        btnModify = new javax.swing.JButton();
        jcbJtable = new javax.swing.JComboBox<String>();
        jcbJcolumn = new javax.swing.JComboBox<String>();
        jLabel13 = new javax.swing.JLabel();
        lblBusinessRule = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Business Rule Builder Dialog");

        jspQuery.setPreferredSize(new java.awt.Dimension(226, 35));

        txtQuery.setEditable(false);
        txtQuery.setColumns(20);
        txtQuery.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtQuery.setLineWrap(true);
        txtQuery.setRows(5);
        txtQuery.setWrapStyleWord(true);
        jspQuery.setViewportView(txtQuery);

        pnlRules.setBackground(new java.awt.Color(255, 255, 255));
        pnlRules.setOpaque(false);

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel1.setText("Existing Rules");
        jLabel1.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel1.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel1.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel2.setText("Rule Name");
        jLabel2.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel3.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel3.setText("DB Connection");
        jLabel3.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel3.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel3.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel4.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel4.setText("Table Name");
        jLabel4.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel4.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel4.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel5.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel5.setText("Column Name");
        jLabel5.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel5.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel5.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel6.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel6.setText("Comp. Col. Name");
        jLabel6.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel6.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel6.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel8.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel8.setText("Comparison");
        jLabel8.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel8.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel8.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel9.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel9.setText("Value");
        jLabel9.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel9.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel9.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel11.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel11.setText("Condition");
        jLabel11.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel11.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel11.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel10.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel10.setText("Join");
        jLabel10.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel10.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel10.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel12.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel12.setText("Tables");
        jLabel12.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel12.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel12.setPreferredSize(new java.awt.Dimension(120, 25));

        jcbRule.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbRule.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Rule Name" }));
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

        jrbNonjoin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jrbNonjoin.setText("Single");
        jrbNonjoin.setMaximumSize(new java.awt.Dimension(120, 25));
        jrbNonjoin.setMinimumSize(new java.awt.Dimension(120, 25));
        jrbNonjoin.setPreferredSize(new java.awt.Dimension(120, 25));

        jrbJoin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jrbJoin.setText("Multiple");
        jrbJoin.setMaximumSize(new java.awt.Dimension(120, 25));
        jrbJoin.setMinimumSize(new java.awt.Dimension(120, 25));
        jrbJoin.setPreferredSize(new java.awt.Dimension(120, 25));

        jcbJoin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbJoin.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Join Operation", "JOIN", "LEFT-JOIN", "RIGHT-JOIN", "OUTER-JOIN", "INNER-JOIN" }));
        jcbJoin.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbJoin.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbJoin.setPreferredSize(new java.awt.Dimension(240, 25));

        jcbConnection.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbConnection.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Database Connection" }));
        jcbConnection.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbConnection.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbConnection.setPreferredSize(new java.awt.Dimension(240, 25));

        jcbTable.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbTable.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Table Name" }));
        jcbTable.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbTable.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbTable.setPreferredSize(new java.awt.Dimension(240, 25));

        jspTables.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jspTables.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        txtTables.setEditable(false);
        txtTables.setColumns(20);
        txtTables.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtTables.setLineWrap(true);
        txtTables.setRows(5);
        txtTables.setWrapStyleWord(true);
        jspTables.setViewportView(txtTables);

        jcbColumn.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbColumn.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Column Name" }));
        jcbColumn.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbColumn.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbColumn.setPreferredSize(new java.awt.Dimension(240, 25));

        jspColumns.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jspColumns.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        txtColumns.setEditable(false);
        txtColumns.setColumns(20);
        txtColumns.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtColumns.setLineWrap(true);
        txtColumns.setRows(5);
        txtColumns.setWrapStyleWord(true);
        jspColumns.setViewportView(txtColumns);

        jcbCcolumn.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbCcolumn.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Comp. Col. Name" }));
        jcbCcolumn.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbCcolumn.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbCcolumn.setPreferredSize(new java.awt.Dimension(240, 25));

        jcbCondition.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbCondition.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Condition", "Equal To", "Not Equal To", "Less Than", "Less Than Equal To", "Greater Than", "Greater Than Equal To", "Like" }));
        jcbCondition.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbCondition.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbCondition.setPreferredSize(new java.awt.Dimension(240, 25));

        txtValue.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtValue.setMaximumSize(new java.awt.Dimension(240, 25));
        txtValue.setMinimumSize(new java.awt.Dimension(240, 25));
        txtValue.setPreferredSize(new java.awt.Dimension(240, 25));

        jcbDelimiter.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbDelimiter.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Delimiter", "AND", "OR" }));
        jcbDelimiter.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbDelimiter.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbDelimiter.setPreferredSize(new java.awt.Dimension(240, 25));

        jspCondition.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jspCondition.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jspCondition.setPreferredSize(new java.awt.Dimension(243, 70));

        txtCondition.setEditable(true);
        txtCondition.setColumns(20);
        txtCondition.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtCondition.setLineWrap(true);
        txtCondition.setRows(5);
        txtCondition.setWrapStyleWord(true);
        jspCondition.setViewportView(txtCondition);

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

        labTdelete.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        labTdelete.setIcon(new javax.swing.ImageIcon(getClass().getClassLoader().getResource("image/delete.png"))); // NOI18N
        labTdelete.setToolTipText("Click here to delete table details");
        labTdelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        labVdelete.setIcon(new javax.swing.ImageIcon(getClass().getClassLoader().getResource("image/delete.png"))); // NOI18N
        labVdelete.setToolTipText("Click here to delete value details");
        labVdelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        labCdelete.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        labCdelete.setIcon(new javax.swing.ImageIcon(getClass().getClassLoader().getResource("image/delete.png"))); // NOI18N
        labCdelete.setToolTipText("Click here to delete column details");
        labCdelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel7.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel7.setText("Table Name");
        jLabel7.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel7.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel7.setPreferredSize(new java.awt.Dimension(120, 25));

        jcbCtable.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbCtable.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Table Name" }));
        jcbCtable.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbCtable.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbCtable.setPreferredSize(new java.awt.Dimension(240, 25));

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

        jcbJtable.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbJtable.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Table Name" }));
        jcbJtable.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbJtable.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbJtable.setPreferredSize(new java.awt.Dimension(240, 25));

        jcbJcolumn.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbJcolumn.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Comp. Col. Name" }));
        jcbJcolumn.setMaximumSize(new java.awt.Dimension(240, 25));
        jcbJcolumn.setMinimumSize(new java.awt.Dimension(240, 25));
        jcbJcolumn.setPreferredSize(new java.awt.Dimension(240, 25));

        jLabel13.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel13.setText("Rule Description");
        jLabel13.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel13.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel13.setPreferredSize(new java.awt.Dimension(120, 25));

        javax.swing.GroupLayout pnlRulesLayout = new javax.swing.GroupLayout(pnlRules);
        pnlRules.setLayout(pnlRulesLayout);
        pnlRulesLayout.setHorizontalGroup(
            pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRulesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRulesLayout.createSequentialGroup()
                        .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlRulesLayout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jcbTable, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jspTables, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(pnlRulesLayout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jcbConnection, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlRulesLayout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jcbJoin, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlRulesLayout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(jrbNonjoin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jrbJoin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlRulesLayout.createSequentialGroup()
                                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtRule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jspDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRulesLayout.createSequentialGroup()
                                .addGap(240, 240, 240)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jcbRule, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10))
                            .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(labCdelete)
                                .addComponent(labTdelete))))
                    .addGroup(pnlRulesLayout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcbCtable, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcbJtable, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlRulesLayout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcbCcolumn, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jcbJcolumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlRulesLayout.createSequentialGroup()
                        .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jcbDelimiter, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcbCondition, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jspCondition, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(labVdelete))
                    .addGroup(pnlRulesLayout.createSequentialGroup()
                        .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnModify, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlRulesLayout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jspColumns, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jcbColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );
        pnlRulesLayout.setVerticalGroup(
            pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(pnlRulesLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jspDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jrbNonjoin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jrbJoin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbJoin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jspTables, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labTdelete))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbColumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jspColumns, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labCdelete))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jcbCtable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jcbJtable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jcbCcolumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jcbJcolumn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRulesLayout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlRulesLayout.createSequentialGroup()
                        .addComponent(jcbCondition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(txtValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jcbDelimiter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jspCondition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labVdelete))
                .addGap(10, 10, 10)
                .addGroup(pnlRulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCreate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnModify, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        lblBusinessRule.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        lblBusinessRule.setText("Business Rule");
        lblBusinessRule.setMaximumSize(new java.awt.Dimension(107, 25));
        lblBusinessRule.setMinimumSize(new java.awt.Dimension(107, 25));
        lblBusinessRule.setPreferredSize(new java.awt.Dimension(107, 25));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jspQuery, javax.swing.GroupLayout.DEFAULT_SIZE, 992, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblBusinessRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pnlRules, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblBusinessRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jspQuery, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlRules, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
        if (jrbJoin.isSelected()) {
            if (txtRule.getText().toString().trim().equals("")) {
                JOptionPane.showMessageDialog(null, "Please enter the rule name to be created!!!");
                txtRule.requestFocus();
            } else if (jcbConnection.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please select DB Connection name!!!");
                jcbConnection.requestFocus();
            } else if (jcbJoin.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please select a JOIN operation to be performed!!!");
                jcbJoin.requestFocus();
            } else if (!txtTables.getText().contains("JOIN")) {
                JOptionPane.showMessageDialog(null, "Please select one more table name on which JOIN operation to be performed!!!");
                jcbTable.requestFocus();
            } else {
                hashRule = new Hashtable<>();
                hashRule.put("rule_Name", txtRule.getText().trim());
                hashRule.put("database_ConnectionName", jcbConnection.getSelectedItem().toString());
                hashRule.put("rule_Type", "JOIN");
                hashRule.put("table_Names", txtTables.getText().trim());
                hashRule.put("column_Names", txtColumns.getText().trim());
                hashRule.put("condition_Names", txtCondition.getText().trim());
                hashRule.put("join_Name", jcbJoin.getSelectedItem().toString());
                if( txtDescription.getText().trim().equals("") ) {
                    hashRule.put("rule_Description", "");
                } else {
                    hashRule.put("rule_Description", txtDescription.getText().trim());
                }

                hashTable = new Hashtable<>();
                hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", hashRule.get("database_ConnectionName"));
                hashRule.put("Database_Type", hashTable.get("Database_Type"));
                
                txtQuery.setText(new QueryBuilder().getJoinQuery(hashRule));
                
                xmlWriter = new XmlWriter();
                xmlWriter.writeXmlFile(hashRule);
                JOptionPane.showMessageDialog(null, "Business Rule with name \"" + txtRule.getText() + "\" successfully created!!!");
                jcbRule.setSelectedIndex(0);
                clearData();
                loadBusinessRules();
            }
        } else if (jrbNonjoin.isSelected()) {
            if (txtRule.getText().toString().trim().equals("")) {
                JOptionPane.showMessageDialog(null, "Please enter the rule name to be created!!!");
                txtRule.requestFocus();
            } else if (jcbConnection.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please select DB Connection name!!!");
                jcbConnection.requestFocus();
            } else if (txtTables.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "Please select select a database table name!!!");
                jcbTable.requestFocus();
            } else {
                if (txtCondition.getText().trim().equals("")) {
                    txtQuery.setText("SELECT " + txtColumns.getText().trim() + " FROM " + txtTables.getText().trim());
                } else {
                    txtQuery.setText("SELECT " + txtColumns.getText().trim() + " FROM " + txtTables.getText().trim() + " WHERE " + txtCondition.getText());
                }
                hashRule = new Hashtable<>();
                hashRule.put("rule_Name", txtRule.getText().trim());
                hashRule.put("database_ConnectionName", jcbConnection.getSelectedItem().toString());
                hashRule.put("rule_Type", "NON-JOIN");
                hashRule.put("table_Names", txtTables.getText().trim());
                hashRule.put("column_Names", txtColumns.getText().trim());
                hashRule.put("condition_Names", txtCondition.getText().trim());
                if (jcbJoin.getSelectedIndex() == 0) {
                    hashRule.put("join_Name", "");
                } else {
                    hashRule.put("join_Name", jcbJoin.getSelectedItem().toString());
                }
                if( txtDescription.getText().trim().equals("") ) {
                    hashRule.put("rule_Description", "");
                } else {
                    hashRule.put("rule_Description", txtDescription.getText().trim());
                }
                
                xmlWriter = new XmlWriter();
                xmlWriter.writeXmlFile(hashRule);
                JOptionPane.showMessageDialog(null, "Business Rule with name \"" + txtRule.getText() + "\" successfully created!!!");
                clearData();
                loadBusinessRules();
            }
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

    private void btnModifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModifyActionPerformed
        if (jcbConnection.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "Please select the database connection name!!!");
            jcbConnection.requestFocus();
        } else if (txtTables.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Please select the tables for business rule!!!");
            jcbTable.requestFocus();
        } else if (txtColumns.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Please select the columns for business rule!!!");
            jcbColumn.requestFocus();
        } else {
            if (jrbJoin.isSelected()) {
                hashRule = new Hashtable<>();
                hashRule.put("rule_Name", txtRule.getText().trim());
                hashRule.put("database_ConnectionName", jcbConnection.getSelectedItem().toString());
                hashRule.put("rule_Type", "JOIN");
                hashRule.put("table_Names", txtTables.getText().trim());
                hashRule.put("column_Names", txtColumns.getText().trim());
                hashRule.put("condition_Names", txtCondition.getText().trim());
                hashRule.put("join_Name", jcbJoin.getSelectedItem().toString());
                hashTable = new Hashtable<>();
                hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", hashRule.get("database_ConnectionName"));
                hashRule.put("Database_Type", hashTable.get("Database_Type"));
                if( txtDescription.getText().trim().equals("") ) {
                    hashRule.put("rule_Description", "");
                } else {
                    hashRule.put("rule_Description", txtDescription.getText().trim());
                }
                
                txtQuery.setText(new QueryBuilder().getJoinQuery(hashRule));
                
                xmlWriter = new XmlWriter();
                xmlWriter.modifyRule(hashRule);
                JOptionPane.showMessageDialog(null, "Business Rule \"" + txtRule.getText() + "\" successfully modified!!!");
                clearData();
                loadBusinessRules();
            } else if (jrbNonjoin.isSelected()) {
                hashRule = new Hashtable<>();
                hashRule.put("rule_Name", txtRule.getText().trim());
                hashRule.put("database_ConnectionName", jcbConnection.getSelectedItem().toString());
                hashRule.put("rule_Type", "NON-JOIN");
                hashRule.put("table_Names", txtTables.getText().trim());
                hashRule.put("column_Names", txtColumns.getText().trim());
                hashRule.put("condition_Names", txtCondition.getText().trim());
                hashRule.put("join_Name", "");
                /* Added on 12/09 to get the Database type for modify */
                hashTable = new Hashtable<>();
                hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", hashRule.get("database_ConnectionName"));
                hashRule.put("Database_Type", hashTable.get("Database_Type"));

                if( txtDescription.getText().trim().equals("") ) {
                    hashRule.put("rule_Description", "");
                } else {
                    hashRule.put("rule_Description", txtDescription.getText().trim());
                }
                
                txtQuery.setText(new QueryBuilder().getNonJoinQuery(hashRule));
                
                xmlWriter = new XmlWriter();
                xmlWriter.modifyRule(hashRule);
                JOptionPane.showMessageDialog(null, "Business Rule \"" + txtRule.getText() + "\" successfully modified!!!");
                clearData();
                loadBusinessRules();
            }
        }
    }//GEN-LAST:event_btnModifyActionPerformed

    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgRuletype;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnModify;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JComboBox<String> jcbCcolumn;
    private javax.swing.JComboBox<String> jcbColumn;
    private javax.swing.JComboBox<String> jcbCondition;
    private javax.swing.JComboBox<String> jcbConnection;
    private javax.swing.JComboBox<String> jcbCtable;
    private javax.swing.JComboBox<String> jcbDelimiter;
    private javax.swing.JComboBox<String> jcbJcolumn;
    private javax.swing.JComboBox<String> jcbJoin;
    private javax.swing.JComboBox<String> jcbJtable;
    private javax.swing.JComboBox<String> jcbRule;
    private javax.swing.JComboBox<String> jcbTable;
    private javax.swing.JRadioButton jrbJoin;
    private javax.swing.JRadioButton jrbNonjoin;
    private javax.swing.JScrollPane jspColumns;
    private javax.swing.JScrollPane jspCondition;
    private javax.swing.JScrollPane jspDescription;
    private javax.swing.JScrollPane jspQuery;
    private javax.swing.JScrollPane jspTables;
    private javax.swing.JLabel labCdelete;
    private javax.swing.JLabel labTdelete;
    private javax.swing.JLabel labVdelete;
    private javax.swing.JLabel lblBusinessRule;
    private javax.swing.JPanel pnlRules;
    private javax.swing.JTextArea txtColumns;
    private javax.swing.JTextArea txtCondition;
    private javax.swing.JTextArea txtDescription;
    private javax.swing.JTextArea txtQuery;
    private javax.swing.JTextField txtRule;
    private javax.swing.JTextArea txtTables;
    private javax.swing.JTextField txtValue;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("nonjoin")) {
            jcbJoin.setSelectedIndex(0);
            jcbJoin.setEnabled(false);
            jcbJcolumn.setEnabled(false);
            jcbJtable.setEnabled(false);
        } else if (e.getActionCommand().equals("join")) {
            jcbJcolumn.setEnabled(true);
            jcbJoin.setEnabled(true);
            jcbJtable.setEnabled(true);
            jcbJcolumn.setSelectedIndex(0);
            jcbJtable.setSelectedIndex(0);
        }
    }
}
