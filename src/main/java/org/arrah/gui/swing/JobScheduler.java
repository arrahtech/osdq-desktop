package org.arrah.gui.swing;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.arrah.framework.rdbms.QueryBuilder;
import org.arrah.framework.rdbms.Rdbms_NewConn;
import org.arrah.framework.scheduler.QuartzScheduler;
import org.arrah.framework.xml.FilePaths;
import org.arrah.framework.xml.XmlReader;
import org.quartz.SchedulerException;


public class JobScheduler extends javax.swing.JFrame {
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSchedule;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    public static javax.swing.JComboBox<String> jcbFrequency;
    public static javax.swing.JComboBox<String> jcbRules;
    public static javax.swing.JComboBox<String> jcbSfrequency;
    public static com.toedter.calendar.JDateChooser jdcEdate;
    public static com.toedter.calendar.JDateChooser jdcSdate;
    private javax.swing.JLabel labHdec;
    private javax.swing.JLabel labHinc;
    private javax.swing.JLabel labMdec;
    private javax.swing.JLabel labMinc;
    private javax.swing.JLabel labSdec;
    private javax.swing.JLabel labSinc;
    private javax.swing.JTextField txtHours;
    private javax.swing.JTextField txtMinutes;
    private javax.swing.JTextField txtSeconds;
    // End of variables declaration//GEN-END:variables
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String freq[] = {"One Time","Daily", "Weekly", "Monthly"}, days[] = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    private DateFormat ddf;
    private int temp = 0;
    private XmlReader xmlReader;
    public static Hashtable<String, String> hashValues, hashRule, hashTable;
    String query = "", dbName = "", time = "", ctime[], startDate, endDate;
    int tempT = 0;
    String[] splitDate;
    public static int startdayofMonth, enddayofMonth;
    SimpleDateFormat formattedDate;
    /**
     * Creates new form JobScheduler
     */
    public JobScheduler() {
        initComponents();

        loadGui();
        loadData();
        loadListeners();
    }

    private void loadGui() {
        ddf = new SimpleDateFormat("HH:mm:ss");
        setLocationRelativeTo(null);
        jcbSfrequency.setEnabled(false);
        //One-time should be enables as soon as the UI is loaded. jdcSdate.setEnabled(false);
        jdcEdate.setEnabled(false);
    }

    private void loadData() {
        jcbRules.removeAllItems();
        jcbRules.addItem("Select Rule Name");
        xmlReader = new XmlReader();

        jcbSfrequency.setEnabled(false);

        String temp[] = xmlReader.getRulesName(new File(FilePaths.getFilePathRules()), "rule", "rule_Name");
        if (temp != null) {
            for (int i = 0; i < temp.length; i++) {
                jcbRules.addItem(temp[i].trim());
            }
        }

        for (int i = 0; i < freq.length; i++) {
            jcbFrequency.addItem(freq[i]);
        }
        for (int i = 0; i < days.length; i++) {
            jcbSfrequency.addItem(days[i]);
        }
        
        time = ddf.format(new Date().getTime());
        ctime = time.split(":");
        txtHours.setText(ctime[0].trim());
        txtMinutes.setText(ctime[1].trim());
        txtSeconds.setText(ctime[2].trim());
        
    }

    private void loadListeners() {

        jcbRules.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbRules.getSelectedIndex() != 0) {
                        xmlReader = new XmlReader();
                        hashRule = new Hashtable<String, String>();
                        hashRule = xmlReader.getRuleDetails(new File(FilePaths.getFilePathRules()), "rule", jcbRules.getSelectedItem().toString());
                        dbName = hashRule.get("database_ConnectionName");

                        hashTable = new Hashtable<String, String>();
                        hashTable = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", hashRule.get("database_ConnectionName"));
                        hashRule.put("Database_Type", hashTable.get("Database_Type"));

                        if (hashRule.get("rule_Type").equals("JOIN")) {
                            query = new QueryBuilder().getJoinQuery(hashRule);
                        } else if (hashRule.get("rule_Type").equals("NON-JOIN")) {
                            query = new QueryBuilder().getNonJoinQuery(hashRule);
                        }

                        
                    }
                }
            }
        });

        jcbFrequency.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jcbFrequency.getSelectedIndex() == 0) {
                        jcbSfrequency.setEnabled(false);
                        jdcSdate.setEnabled(true);
                        jdcEdate.setEnabled(false);
                        jdcSdate.setDate(null);
                        jdcEdate.setDate(null);
                    } else if (jcbFrequency.getSelectedIndex() == 1) {
                        jcbSfrequency.setEnabled(false);
                        jdcSdate.setEnabled(false);
                        jdcEdate.setEnabled(false);
                        jdcSdate.setDate(null);
                        jdcEdate.setDate(null);
                    } else if (jcbFrequency.getSelectedIndex() == 2) {
                        jcbSfrequency.setEnabled(true);
                        jdcSdate.setEnabled(false);
                        jdcEdate.setEnabled(false);
                        jdcSdate.setDate(null);
                        jdcEdate.setDate(null);
                        jcbSfrequency.setSelectedIndex(0);
                    } else if (jcbFrequency.getSelectedIndex() == 3) {
                        jdcSdate.setEnabled(true);
                        jdcEdate.setEnabled(true);
                        jdcSdate.setDate(null);
                        jdcEdate.setDate(null);
                        jcbSfrequency.setEnabled(false);
                    }
                }
            }
        });

        
        labHinc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                temp = Integer.parseInt(txtHours.getText());
                tempT = setHoursInc(temp);
                txtHours.setText(tempT>9?String.valueOf(tempT):"0"+String.valueOf(tempT));
            }
        });
        
        labHdec.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                temp = Integer.parseInt(txtHours.getText());
                tempT = setHoursDec(temp);
                txtHours.setText(tempT>9?String.valueOf(tempT):"0"+String.valueOf(tempT));
            }
        });
        
        labMinc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                temp = Integer.parseInt(txtMinutes.getText());
                tempT = setMinutesInc(temp);
                txtMinutes.setText(tempT>9?String.valueOf(tempT):"0"+String.valueOf(tempT));
            }
        });
        
        labMdec.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                temp = Integer.parseInt(txtMinutes.getText());
                tempT = setMinutesDec(temp);
                txtMinutes.setText(tempT>9?String.valueOf(tempT):"0"+String.valueOf(tempT));
            }
        });
        
        labSinc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                temp = Integer.parseInt(txtSeconds.getText());
                tempT = setSecondsInc(temp);
                txtSeconds.setText(tempT>9?String.valueOf(tempT):"0"+String.valueOf(tempT));
            }
        });
        
        labSdec.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                temp = Integer.parseInt(txtSeconds.getText());
                tempT = setSecondsDec(temp);
                txtSeconds.setText(tempT>9?String.valueOf(tempT):"0"+String.valueOf(tempT));
            }
        });

    }

    private int setHoursInc(int hours) {
        hours++;
        if( hours == 24 ) {
            tempT = 0;
        } else {
            tempT = hours;
        }
        return tempT;
    }
    
    private int setHoursDec(int hours) {
        hours--;
        if( hours < 0 ) {
            tempT = 23;
        } else {
            tempT = hours;
        }
        return tempT;
    }
    
    private int setMinutesInc(int minutes) {
        minutes++;
        if( minutes == 60 ) {
            tempT = 0;
        } else {
            tempT = minutes;
        }
        return tempT;
    }
    
    private int setMinutesDec(int minutes) {
        minutes--;
        if( minutes < 0 ) {
            tempT = 59;
        } else {
            tempT = minutes;
        }
        return tempT;
    }
    
    private int setSecondsInc(int seconds) {
        seconds++;
        if( seconds == 60 ) {
            tempT = 0;
        } else {
            tempT = seconds;
        }
        return tempT;
    }
    
    private int setSecondsDec(int seconds) {
        seconds--;
        if( seconds < 0 ) {
            tempT = 59;
        } else {
            tempT = seconds;
        }
        return tempT;
    }
    
    private void clearForm() {
        jcbRules.setSelectedIndex(0);
        jcbFrequency.setSelectedIndex(0);
        jcbSfrequency.setSelectedIndex(0);
        jdcSdate.setDate(null);
        jdcEdate.setDate(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jcbRules = new javax.swing.JComboBox <String>();
        jcbFrequency = new javax.swing.JComboBox <String>();
        jcbSfrequency = new javax.swing.JComboBox <String>();
        jdcSdate = new com.toedter.calendar.JDateChooser();
        jdcEdate = new com.toedter.calendar.JDateChooser();
        btnSchedule = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        txtHours = new javax.swing.JTextField();
        labHinc = new javax.swing.JLabel();
        labHdec = new javax.swing.JLabel();
        labMdec = new javax.swing.JLabel();
        labMinc = new javax.swing.JLabel();
        txtMinutes = new javax.swing.JTextField();
        labSdec = new javax.swing.JLabel();
        labSinc = new javax.swing.JLabel();
        txtSeconds = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Job Scheduler");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel1.setText("Job Scheduler");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel2.setText("Rules");
        jLabel2.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel3.setText("Frequency");
        jLabel3.setMaximumSize(new java.awt.Dimension(120, 25));
        jLabel3.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel3.setPreferredSize(new java.awt.Dimension(120, 25));

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel4.setText("Time");
        jLabel4.setMaximumSize(new java.awt.Dimension(120, 26));
        jLabel4.setMinimumSize(new java.awt.Dimension(120, 26));
        jLabel4.setPreferredSize(new java.awt.Dimension(120, 26));

        jcbRules.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbRules.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Select Rule Name" }));
        jcbRules.setMaximumSize(new java.awt.Dimension(250, 25));
        jcbRules.setMinimumSize(new java.awt.Dimension(250, 25));
        jcbRules.setPreferredSize(new java.awt.Dimension(250, 25));

        jcbFrequency.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbFrequency.setMaximumSize(new java.awt.Dimension(90, 25));
        jcbFrequency.setMinimumSize(new java.awt.Dimension(90, 25));
        jcbFrequency.setPreferredSize(new java.awt.Dimension(90, 25));

        jcbSfrequency.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jcbSfrequency.setMaximumSize(new java.awt.Dimension(90, 25));
        jcbSfrequency.setMinimumSize(new java.awt.Dimension(90, 25));
        jcbSfrequency.setPreferredSize(new java.awt.Dimension(90, 25));

        jdcSdate.setDateFormatString("yyyy-MM-dd");
        jdcSdate.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jdcSdate.setMaximumSize(new java.awt.Dimension(100, 25));
        jdcSdate.setMinimumSize(new java.awt.Dimension(100, 25));
        jdcSdate.setPreferredSize(new java.awt.Dimension(100, 25));

        jdcEdate.setDateFormatString("yyyy-MM-dd");
        jdcEdate.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jdcEdate.setMaximumSize(new java.awt.Dimension(100, 25));
        jdcEdate.setMinimumSize(new java.awt.Dimension(100, 25));
        jdcEdate.setPreferredSize(new java.awt.Dimension(100, 25));

        btnSchedule.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnSchedule.setText("SCHEDULE");
        btnSchedule.setMaximumSize(new java.awt.Dimension(120, 35));
        btnSchedule.setMinimumSize(new java.awt.Dimension(120, 35));
        btnSchedule.setPreferredSize(new java.awt.Dimension(120, 35));
        btnSchedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScheduleActionPerformed(evt);
            }
        });

        btnClear.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnClear.setText("CLEAR");
        btnClear.setMaximumSize(new java.awt.Dimension(120, 35));
        btnClear.setMinimumSize(new java.awt.Dimension(120, 35));
        btnClear.setPreferredSize(new java.awt.Dimension(120, 35));
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnClose.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnClose.setText("CLOSE");
        btnClose.setMaximumSize(new java.awt.Dimension(120, 35));
        btnClose.setMinimumSize(new java.awt.Dimension(120, 35));
        btnClose.setPreferredSize(new java.awt.Dimension(120, 35));
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        txtHours.setEditable(false);
        txtHours.setBackground(new java.awt.Color(255, 255, 255));
        txtHours.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtHours.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtHours.setMaximumSize(new java.awt.Dimension(35, 26));
        txtHours.setMinimumSize(new java.awt.Dimension(35, 26));
        txtHours.setOpaque(false);
        txtHours.setPreferredSize(new java.awt.Dimension(35, 26));

        labHinc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        //labHinc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/arrah/gui/swing/up.png"))); // NOI18N
        labHinc.setToolTipText("Click here to increase the hours");
        labHinc.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));
        labHinc.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labHinc.setMaximumSize(new java.awt.Dimension(25, 13));
        labHinc.setMinimumSize(new java.awt.Dimension(25, 13));
        labHinc.setPreferredSize(new java.awt.Dimension(25, 13));

        labHdec.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        //labHdec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/arrah/gui/swing/down.png"))); // NOI18N
        labHdec.setToolTipText("Click here to decrease the hours");
        labHdec.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));
        labHdec.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labHdec.setMaximumSize(new java.awt.Dimension(25, 13));
        labHdec.setMinimumSize(new java.awt.Dimension(25, 13));
        labHdec.setPreferredSize(new java.awt.Dimension(25, 13));

        labMdec.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       // labMdec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/arrah/gui/swing/down.png"))); // NOI18N
        labMdec.setToolTipText("Click here to decrease the minutes");
        labMdec.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));
        labMdec.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labMdec.setMaximumSize(new java.awt.Dimension(25, 13));
        labMdec.setMinimumSize(new java.awt.Dimension(25, 13));
        labMdec.setPreferredSize(new java.awt.Dimension(25, 13));

        labMinc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       // labMinc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/arrah/gui/swing/up.png"))); // NOI18N
        labMinc.setToolTipText("Click here to increase the minutes");
        labMinc.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));
        labMinc.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labMinc.setMaximumSize(new java.awt.Dimension(25, 13));
        labMinc.setMinimumSize(new java.awt.Dimension(25, 13));
        labMinc.setPreferredSize(new java.awt.Dimension(25, 13));

        txtMinutes.setEditable(false);
        txtMinutes.setBackground(new java.awt.Color(255, 255, 255));
        txtMinutes.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtMinutes.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtMinutes.setMaximumSize(new java.awt.Dimension(35, 26));
        txtMinutes.setMinimumSize(new java.awt.Dimension(35, 26));
        txtMinutes.setOpaque(false);
        txtMinutes.setPreferredSize(new java.awt.Dimension(35, 26));

        labSdec.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        //labSdec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/arrah/gui/swing/down.png"))); // NOI18N
        labSdec.setToolTipText("Click here to increase the seconds");
        labSdec.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));
        labSdec.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labSdec.setMaximumSize(new java.awt.Dimension(25, 13));
        labSdec.setMinimumSize(new java.awt.Dimension(25, 13));
        labSdec.setPreferredSize(new java.awt.Dimension(25, 13));

        labSinc.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       // labSinc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/arrah/gui/swing/up.png"))); // NOI18N
        labSinc.setToolTipText("Click here to increase the seconds");
        labSinc.setBorder(javax.swing.BorderFactory.createCompoundBorder(null, javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));
        labSinc.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labSinc.setMaximumSize(new java.awt.Dimension(25, 13));
        labSinc.setMinimumSize(new java.awt.Dimension(25, 13));
        labSinc.setPreferredSize(new java.awt.Dimension(25, 13));

        txtSeconds.setEditable(false);
        txtSeconds.setBackground(new java.awt.Color(255, 255, 255));
        txtSeconds.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        txtSeconds.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSeconds.setMaximumSize(new java.awt.Dimension(35, 26));
        txtSeconds.setMinimumSize(new java.awt.Dimension(35, 26));
        txtSeconds.setOpaque(false);
        txtSeconds.setPreferredSize(new java.awt.Dimension(35, 26));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labHinc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labHdec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, 0)
                                .addComponent(txtMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labMinc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labMdec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, 0)
                                .addComponent(txtSeconds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labSinc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labSdec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(86, 86, 86)
                                .addComponent(btnSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jcbRules, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jcbFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(jcbSfrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jdcSdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(jdcEdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(10, 10, 10))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbRules, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jcbFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jcbSfrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jdcSdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jdcEdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labHinc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labMinc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(labMdec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtMinutes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(labSinc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(labSdec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtSeconds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(labHdec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtHours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnScheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScheduleActionPerformed

        try {
            int hour = 0, minute = 0, seconds = 0;

            if (jcbRules.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please select the rule name to be scheduled!!!");
                jcbRules.requestFocus();
            }
                       
        else {
            	ddf = new SimpleDateFormat("dd");
            	time = txtHours.getText() + ":" + txtMinutes.getText() + ":" + txtSeconds.getText();
                JOptionPane.showMessageDialog(null, "<html><center>Rule: " + jcbRules.getSelectedItem().toString() + "<br/>Period: " + jcbFrequency.getSelectedItem().toString() + ", " + jcbFrequency.getSelectedItem().toString() + "<br/>Date : " + jdcSdate.getDate() +"<br/> Time : "+ time + "</center></html>");
                formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); //2014-11-24
           	    if(jdcSdate.getDate() != null){
           	    startDate  = formattedDate .format(jdcSdate.getDate());
           	    splitDate=startDate.split("-");
           	    //Get only the startdayofmonth to schedule on a monthly basis because Quartz takes the day as the input.
           	       	 startdayofMonth=Integer.parseInt(startDate.substring(Math.max(startDate.length() - 2, 0)));
           	       }
           	   
           	    if(jdcEdate.getDate() != null){
           	    endDate  = formattedDate .format(jdcEdate.getDate());
           	    //Get only the enddayofmonth to schedule on a monthly basis because Quartz takes the day as the input.
        	    enddayofMonth=Integer.parseInt(endDate.substring(Math.max(endDate.length() - 2, 0)));
           	    }
                hashValues = new Hashtable<String, String>();
                hashValues = xmlReader.getDatabaseDetails(new File(FilePaths.getFilePathDB()), "entry", dbName);
                Rdbms_NewConn dbmsConn = new Rdbms_NewConn(hashValues);

                dbmsConn.openConn();
                String[] splitTime = time.split(":");

                // split the time into hours, minutes, seconds so that these are passed as arguments to the scheduler
                for (int i = 0; i < splitTime.length; i++) {
                    hour = Integer.parseInt(splitTime[0]);
                    minute = Integer.parseInt(splitTime[1]);
                    seconds = Integer.parseInt(splitTime[2]);
                }

                try {
                    new QuartzScheduler(query, hour, minute, seconds, 
                        jcbSfrequency.getSelectedItem().toString(), 
                        jcbFrequency.getSelectedItem().toString(), 
                        jdcEdate.getDate(), 
                        startdayofMonth,
                        hashValues,
                        jcbRules.getSelectedItem().toString());
                } catch (SchedulerException   e) {
                    e.printStackTrace();
                } catch (InterruptedException  e) {
                    e.printStackTrace();
                } 
            }

        } catch (SQLException ex) {
            Logger.getLogger(JobScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnScheduleActionPerformed

    
}
