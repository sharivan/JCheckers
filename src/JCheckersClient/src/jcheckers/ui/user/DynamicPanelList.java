package jcheckers.ui.user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

public class DynamicPanelList {

	public class TestPane extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6535970657103357116L;

		private JPanel mainList;

		private int counter = 1;

		public TestPane() {
			setLayout(new BorderLayout());

			mainList = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.weightx = 1;
			gbc.weighty = 1;
			mainList.add(new JPanel(), gbc);

			add(new JScrollPane(mainList));

			JButton add = new JButton("Add");
			add.addActionListener(e -> {
				JPanel panel = new JPanel();
				panel.add(new JLabel("Hello " + counter++));
				panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
				GridBagConstraints gbc1 = new GridBagConstraints();
				gbc1.gridwidth = GridBagConstraints.REMAINDER;
				gbc1.weightx = 1;
				gbc1.fill = GridBagConstraints.HORIZONTAL;
				mainList.add(panel, gbc1, counter - 2);

				validate();
				repaint();
			});

			add(add, BorderLayout.SOUTH);

		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(200, 200);
		}
	}

	public static void main(String[] args) {
		new DynamicPanelList();
	}

	public DynamicPanelList() {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
			}

			JFrame frame = new JFrame("Test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(new TestPane());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

}
