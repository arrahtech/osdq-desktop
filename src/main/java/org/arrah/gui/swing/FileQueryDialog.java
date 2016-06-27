package org.arrah.gui.swing;

/***********************************************
 *     Copyright to Arrah Technology 2007      *
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
 * This class defines popup Query Dialog
 * from the imported file menu
 *
 */

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.arrah.framework.ndtable.RTMUtil;
import org.arrah.framework.util.DiscreetRange;

public class FileQueryDialog extends QueryDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Vector<String> col_v;
	Vector<Integer> operator_v;
	Vector<String> cond_v;
	Vector<String> ao_v;
	Vector<Integer> result_v;
	boolean isValidQ = false;
	ReportTable _rt;
	Vector<Integer> delete_v;
	Vector<Object[]> delrow_v;

	public FileQueryDialog(int i, String s, Vector<?> avector[]) {
		super(i, s, avector);
	}

	public void setReportTable(ReportTable rt) {
		_rt = rt;
	}

	public void actionPerformed(ActionEvent actionevent) {
		String s = actionevent.getActionCommand();
		if (s.equals("move")) {
			super.actionPerformed(actionevent);
		} else if (s.equals("validate")) {
			runCondition();
			if (isValidQ == true) {
				Thread[] tid = new Thread[col_v.size()];
				final Vector<Integer>[] vc_r = new Vector[col_v.size()];
				for (int i = 0; i < col_v.size(); i++) {
					final int index = i;
					tid[index] = new Thread(new Runnable() {
						public void run() {
							vc_r[index] = matchCondition(_rt, col_v.get(index),
									operator_v.get(index), cond_v.get(index));
						}
					});
					tid[i].start();
				}

				for (int i = 0; i < col_v.size(); i++) {
					try {
						tid[i].join();
					} catch (Exception exp) {

					}
				}
				result_v = vc_r[0]; // For Single Condition
				for (int i = 0; i < (ao_v.size() - 1); i++) {
					vc_r[i + 1] = DiscreetRange.mergeSet(vc_r[i], vc_r[i + 1],
							ao_v.get(i));
					result_v = vc_r[i + 1];
				}
				if (result_v == null) {
					JOptionPane.showMessageDialog(null,
							" Query Failure\n No Rows Found", "Information", 1);
					return;
				}
				JOptionPane.showMessageDialog(null, " Query Success\n"
						+ result_v.size() + " Rows Found", "Information", 1);
				apply_b.setEnabled(true);
			}
		} else if (s.equals("cancel")) {
			super.actionPerformed(actionevent);
		} else if (s.equals("apply")) {
			Vector<Integer> univ_v = new Vector<Integer>();
			Vector<Integer> del_v = new Vector<Integer>();
			delete_v = new Vector<Integer>();
			delrow_v = new Vector<Object[]>();

			for (int i = 0; i < _rt.table.getRowCount(); i++)
				univ_v.add(i);
			del_v = DiscreetRange.mergeSet(univ_v, result_v, "xor");

			Integer[] a = new Integer[del_v.size()];
			a = del_v.toArray(a);
			Arrays.sort(a);
			for (int i = (a.length - 1); i >= 0; i--) {
				delete_v.add(a[i]);
				delrow_v.add(_rt.getRow(a[i]));
				_rt.removeRows(a[i], 1);
			}
			response = 1;
			dispose();

		}
	}

	private void runCondition() {
		isValidQ = false;
		col_v = new Vector<String>();
		operator_v = new Vector<Integer>();
		cond_v = new Vector<String>();
		ao_v = new Vector<String>();

		for (int j = 0; j < index; j++) {

			JComboBox<String> jcombobox = (JComboBox<String>) cb_v.get(j);
			int k = jcombobox.getSelectedIndex();
			if (k <= 1)
				continue;
			isValidQ = true;
			String s5 = ((JFormattedTextField) wt_v.get(j)).getText().trim();
			if ((k > 3) && (s5 == null || s5.equals(""))) {
				JOptionPane
						.showMessageDialog(
								null,
								"Varibale can not be null or Empty \n  Enter %Str% or %str or Str% ",
								"Variable Format Error",
								JOptionPane.ERROR_MESSAGE);
				return;
			}
			operator_v.add(k);
			String s4 = ((JTextField) tt_v.get(j)).getText();
			col_v.add(s4);

			s5 = s5.replace('"', '\'');
			cond_v.add(s5);
			String s3 = ((JComboBox<String>) aoc_v.get(j)).getSelectedItem().toString();
			ao_v.add(s3);
		}
	}

	public static Vector<Integer> matchCondition(ReportTable _rt, String colN,
			int cond, String condV) {

		if (_rt == null) {
			ConsoleFrame.addText("\n ERROR:Table not Set for Filtering");
			return null;
		}
		_rt.cancelSorting();
		int colI = ExpressionBuilderPanel.getColumnIndex(_rt, colN);
		Vector<Integer> result_v = new Vector<Integer>();
		result_v = RTMUtil.matchCondition(_rt.getRTMModel(), colI, cond, condV);

		return result_v;
	}

}
