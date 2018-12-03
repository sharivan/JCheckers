package jcheckers.ui.options;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class TableParamsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5246980892207142344L;
	private JLabel lblVariant;
	private JLabel lblTablePrivacy;
	private JComboBox<String> cmbTablePrivacy;
	private JComboBox<String> cmbVariant;
	private JCheckBox chkRated;
	private JSpinner spnTimePerMove;
	private JLabel lblTimePerMove;
	private JSpinner spnIncrementTime;
	private JCheckBox chkIncrementTime;
	private JLabel lblIncrementTime;
	private JCheckBox chkTime;
	private JSpinner spnTime;
	private JLabel lblTime;
	private JCheckBox chkAllowWatchers;
	private JCheckBox chkSwapSides;
	private JCheckBox chkTimePerMove;

	/**
	 * Create the panel.
	 */
	public TableParamsPanel() {
		GridBagLayout gbc = new GridBagLayout();
		gbc.columnWidths = new int[] { 0, 0, 0, 0 };
		gbc.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbc.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbc.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gbc);

		lblVariant = new JLabel("Variante");
		GridBagConstraints gbc_lblVariant = new GridBagConstraints();
		gbc_lblVariant.insets = new Insets(0, 0, 5, 5);
		gbc_lblVariant.anchor = GridBagConstraints.EAST;
		gbc_lblVariant.gridx = 0;
		gbc_lblVariant.gridy = 0;
		add(lblVariant, gbc_lblVariant);

		cmbVariant = new JComboBox<>();
		cmbVariant.setModel(new DefaultComboBoxModel<>(new String[] { "Cl\u00E1ssica", "Internacional", "Americana", "Americana 10x10" }));
		cmbVariant.setSelectedIndex(0);
		cmbVariant.setEditable(true);
		GridBagConstraints gbc_cmbVariant = new GridBagConstraints();
		gbc_cmbVariant.gridwidth = 2;
		gbc_cmbVariant.insets = new Insets(0, 0, 5, 0);
		gbc_cmbVariant.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbVariant.gridx = 1;
		gbc_cmbVariant.gridy = 0;
		add(cmbVariant, gbc_cmbVariant);

		lblTablePrivacy = new JLabel("Tipo de mesa");
		GridBagConstraints gbc_lblTablePrivacy = new GridBagConstraints();
		gbc_lblTablePrivacy.anchor = GridBagConstraints.EAST;
		gbc_lblTablePrivacy.insets = new Insets(0, 0, 5, 5);
		gbc_lblTablePrivacy.gridx = 0;
		gbc_lblTablePrivacy.gridy = 1;
		add(lblTablePrivacy, gbc_lblTablePrivacy);

		cmbTablePrivacy = new JComboBox<>();
		cmbTablePrivacy.setEditable(true);
		cmbTablePrivacy.setModel(new DefaultComboBoxModel<>(new String[] { "P\u00FAblica", "Privada", "Desafio" }));
		cmbTablePrivacy.setSelectedIndex(0);
		GridBagConstraints gbc_cmbTablePrivacy = new GridBagConstraints();
		gbc_cmbTablePrivacy.gridwidth = 2;
		gbc_cmbTablePrivacy.insets = new Insets(0, 0, 5, 0);
		gbc_cmbTablePrivacy.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbTablePrivacy.gridx = 1;
		gbc_cmbTablePrivacy.gridy = 1;
		add(cmbTablePrivacy, gbc_cmbTablePrivacy);

		chkRated = new JCheckBox("Pontua\u00E7\u00E3o ativa");
		GridBagConstraints gbc_chkRated = new GridBagConstraints();
		gbc_chkRated.insets = new Insets(0, 0, 5, 0);
		gbc_chkRated.anchor = GridBagConstraints.WEST;
		gbc_chkRated.gridx = 0;
		gbc_chkRated.gridy = 2;
		gbc_chkRated.gridwidth = 3;
		add(chkRated, gbc_chkRated);

		chkTimePerMove = new JCheckBox("Tempo por movimento");
		chkTimePerMove.addActionListener((e) -> onTimePerMoveChanged());
		GridBagConstraints gbc_chkHasTimePerTurn = new GridBagConstraints();
		gbc_chkHasTimePerTurn.insets = new Insets(0, 0, 5, 5);
		gbc_chkHasTimePerTurn.gridx = 0;
		gbc_chkHasTimePerTurn.gridy = 3;
		add(chkTimePerMove, gbc_chkHasTimePerTurn);

		spnTimePerMove = new JSpinner();
		spnTimePerMove.setEnabled(false);
		spnTimePerMove.setModel(new SpinnerNumberModel(new Integer(15), new Integer(5), null, new Integer(1)));
		GridBagConstraints gbc_spnTimePerMove = new GridBagConstraints();
		gbc_spnTimePerMove.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnTimePerMove.insets = new Insets(0, 0, 5, 5);
		gbc_spnTimePerMove.gridx = 1;
		gbc_spnTimePerMove.gridy = 3;
		add(spnTimePerMove, gbc_spnTimePerMove);

		lblTimePerMove = new JLabel("segundos");
		lblTimePerMove.setEnabled(false);
		GridBagConstraints gbc_lblTimePerMove = new GridBagConstraints();
		gbc_lblTimePerMove.anchor = GridBagConstraints.WEST;
		gbc_lblTimePerMove.insets = new Insets(0, 0, 5, 0);
		gbc_lblTimePerMove.gridx = 2;
		gbc_lblTimePerMove.gridy = 3;
		add(lblTimePerMove, gbc_lblTimePerMove);

		chkIncrementTime = new JCheckBox("Incremento");
		chkIncrementTime.addActionListener((e) -> onIncrementTimeChanged());
		GridBagConstraints gbc_chkIncrementTime = new GridBagConstraints();
		gbc_chkIncrementTime.anchor = GridBagConstraints.WEST;
		gbc_chkIncrementTime.insets = new Insets(0, 0, 5, 5);
		gbc_chkIncrementTime.gridx = 0;
		gbc_chkIncrementTime.gridy = 4;
		add(chkIncrementTime, gbc_chkIncrementTime);

		spnIncrementTime = new JSpinner();
		spnIncrementTime.setEnabled(false);
		spnIncrementTime.setModel(new SpinnerNumberModel(1, 1, 30, 1));
		GridBagConstraints gbc_spnIncrementTime = new GridBagConstraints();
		gbc_spnIncrementTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnIncrementTime.insets = new Insets(0, 0, 5, 5);
		gbc_spnIncrementTime.gridx = 1;
		gbc_spnIncrementTime.gridy = 4;
		add(spnIncrementTime, gbc_spnIncrementTime);

		lblIncrementTime = new JLabel("segundos");
		lblIncrementTime.setEnabled(false);
		GridBagConstraints gbc_lblIncrementTime = new GridBagConstraints();
		gbc_lblIncrementTime.anchor = GridBagConstraints.WEST;
		gbc_lblIncrementTime.insets = new Insets(0, 0, 5, 0);
		gbc_lblIncrementTime.gridx = 2;
		gbc_lblIncrementTime.gridy = 4;
		add(lblIncrementTime, gbc_lblIncrementTime);

		chkTime = new JCheckBox("Tempo de jogo");
		chkTime.addActionListener((e) -> onTimeChanged());
		chkTime.setSelected(true);
		GridBagConstraints gbc_chkTime = new GridBagConstraints();
		gbc_chkTime.anchor = GridBagConstraints.WEST;
		gbc_chkTime.insets = new Insets(0, 0, 5, 5);
		gbc_chkTime.gridx = 0;
		gbc_chkTime.gridy = 5;
		add(chkTime, gbc_chkTime);

		spnTime = new JSpinner();
		spnTime.setModel(new SpinnerNumberModel(10, 1, 20, 1));
		GridBagConstraints gbc_spnTime = new GridBagConstraints();
		gbc_spnTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnTime.insets = new Insets(0, 0, 5, 5);
		gbc_spnTime.gridx = 1;
		gbc_spnTime.gridy = 5;
		add(spnTime, gbc_spnTime);

		lblTime = new JLabel("minutos");
		GridBagConstraints gbc_lblTime = new GridBagConstraints();
		gbc_lblTime.anchor = GridBagConstraints.WEST;
		gbc_lblTime.insets = new Insets(0, 0, 5, 0);
		gbc_lblTime.gridx = 2;
		gbc_lblTime.gridy = 5;
		add(lblTime, gbc_lblTime);

		chkAllowWatchers = new JCheckBox("Permitir visitantes");
		chkAllowWatchers.setSelected(true);
		GridBagConstraints gbc_chkAllowWatchers = new GridBagConstraints();
		gbc_chkAllowWatchers.insets = new Insets(0, 0, 5, 0);
		gbc_chkAllowWatchers.anchor = GridBagConstraints.WEST;
		gbc_chkAllowWatchers.gridwidth = 3;
		gbc_chkAllowWatchers.gridx = 0;
		gbc_chkAllowWatchers.gridy = 6;
		add(chkAllowWatchers, gbc_chkAllowWatchers);

		chkSwapSides = new JCheckBox("Mudar de lado");
		chkSwapSides.setSelected(true);
		GridBagConstraints gbc_chkSwapSides = new GridBagConstraints();
		gbc_chkSwapSides.gridwidth = 3;
		gbc_chkSwapSides.anchor = GridBagConstraints.WEST;
		gbc_chkSwapSides.insets = new Insets(0, 0, 0, 5);
		gbc_chkSwapSides.gridx = 0;
		gbc_chkSwapSides.gridy = 7;
		add(chkSwapSides, gbc_chkSwapSides);
	}

	public int getIncrementTime() {
		return (Integer) spnIncrementTime.getValue();
	}

	public int getPrivacy() {
		return cmbTablePrivacy.getSelectedIndex();
	}

	public int getTime() {
		return (Integer) spnTime.getValue();
	}

	public int getTimePerMove() {
		return (Integer) spnTimePerMove.getValue();
	}

	public int getVariant() {
		return cmbVariant.getSelectedIndex();
	}

	public boolean hasIncrementTime() {
		return chkIncrementTime.isSelected();
	}

	public boolean hasTime() {
		return chkTime.isSelected();
	}

	public boolean hasTimePerMove() {
		return chkTimePerMove.isSelected();
	}

	public boolean isAllowWatchers() {
		return chkAllowWatchers.isSelected();
	}

	public boolean isRated() {
		return chkRated.isSelected();
	}

	public boolean isSwapSides() {
		return chkSwapSides.isSelected();
	}

	private void onIncrementTimeChanged() {
		boolean checked = chkIncrementTime.isSelected();
		spnIncrementTime.setEnabled(checked);
		lblIncrementTime.setEnabled(checked);
	}

	private void onTimeChanged() {
		boolean checked = chkTime.isSelected();
		spnTime.setEnabled(checked);
		lblTime.setEnabled(checked);
	}

	private void onTimePerMoveChanged() {
		boolean checked = chkTimePerMove.isSelected();
		spnTimePerMove.setEnabled(checked);
		lblTimePerMove.setEnabled(checked);
	}

	public void setAllowWatchers(boolean value) {
		chkAllowWatchers.setSelected(value);
	}

	public void setHasIncrementTime(boolean value) {
		chkIncrementTime.setSelected(value);
		onIncrementTimeChanged();
	}

	public void setHasTime(boolean value) {
		chkTime.setSelected(value);
		onTimeChanged();
	}

	public void setHasTimePerMove(boolean value) {
		chkTimePerMove.setSelected(value);
		onTimePerMoveChanged();
	}

	public void setIncrementTime(int value) {
		spnIncrementTime.setValue(value);
	}

	public void setPrivacy(int value) {
		cmbTablePrivacy.setSelectedIndex(value);
	}

	public void setRated(boolean value) {
		chkRated.setSelected(value);
	}

	public void setSwapSides(boolean value) {
		chkSwapSides.setSelected(value);
	}

	public void setTime(int value) {
		spnTime.setValue(value);
	}

	public void setTimePerMove(int value) {
		spnTimePerMove.setValue(value);
	}

	public void setVariant(int value) {
		cmbVariant.setSelectedIndex(value);
	}

}
