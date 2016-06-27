package org.arrah.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

public class BusinesRuleListener implements ActionListener {
	
	public BusinesRuleListener() {
		
	}
	
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() instanceof JMenuItem) {
			String source = ((JMenuItem) (e.getSource())).getText();
			if (source.equals("Add DB")) {
                new TestConnectionDialog(1).createGUI();
                return;
            }
            if (source.equals("Build Rule")) {
               BusinessRules br = new BusinessRules();
                br.setVisible(true);
                br.loadDatabaseConnections();
                br.loadBusinessRules();
                return;
            }
            if (source.equals("Execute Rule")) {
                ExecuteBusiRule ebr = new ExecuteBusiRule();
                ebr.setVisible(true);
                ebr.loadRules();
                return;
            }
            if (source.equals("Schedule Rule")) {
                new JobScheduler().setVisible(true);
                return;
            }
       }
	}

} // End of class
