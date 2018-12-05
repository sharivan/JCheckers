package jcheckers.ui.common;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * 
 * Painel Swing contendo os botões de OK e Cancelar.
 * @author miste
 *
 */
public class OkCancelPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5794964085755663270L;

	private JButton btnOK;
	private JButton btnCancel;

	/**
	 * Create the panel.
	 */
	public OkCancelPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		Component horizontalGlue = Box.createHorizontalGlue();
		add(horizontalGlue);

		btnOK = new JButton("OK");
		add(btnOK);

		btnCancel = new JButton("Cancelar");
		add(btnCancel);
	}

	public void addCancelActionListener(ActionListener l) {
		btnCancel.addActionListener(l);
	}

	public void addOKActionListener(ActionListener l) {
		btnOK.addActionListener(l);
	}

	public String getCancelText() {
		return btnCancel.getText();
	}

	public String getOKText() {
		return btnOK.getText();
	}

	public void removeCancelActionListener(ActionListener l) {
		btnCancel.removeActionListener(l);
	}

	public void removeOKActionListener(ActionListener l) {
		btnOK.removeActionListener(l);
	}

	public void setCancelText(String text) {
		btnCancel.setText(text);
	}

	public void setOKText(String text) {
		btnOK.setText(text);
	}

}
