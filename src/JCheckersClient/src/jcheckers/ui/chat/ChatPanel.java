package jcheckers.ui.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6387012737489117525L;

	public static final Color CHAT_BACKGROUND_COLOR = new Color(0x00437FB3);
	public static final Color CHAT_FOREGROUND_COLOR = Color.WHITE;
	public static final Color SYSTEM_MESSAGE_COLOR = new Color(0x00FFCB00);
	public static final Color ADMIN_MESSAGE_COLOR = SYSTEM_MESSAGE_COLOR;
	public static final Color PRIVATE_MESSAGE_COLOR = new Color(0x00B5F889);
	public static final Color ALERT_MESSAGE_COLOR = Color.RED;

	private boolean clearAfterSend = true;

	private StyledDocument doc;
	private ArrayList<ChatPanelListener> listeners;

	private JTextField txtMessage;
	private JTextPane txtChat;

	/**
	 * Create the panel.
	 */
	public ChatPanel() {
		listeners = new ArrayList<>();

		setLayout(new BorderLayout(0, 0));

		txtMessage = new JTextField();
		txtMessage.setPreferredSize(new Dimension(300, 30));
		txtMessage.setForeground(CHAT_FOREGROUND_COLOR);
		txtMessage.setBackground(CHAT_BACKGROUND_COLOR);
		txtMessage.addActionListener((e) -> onChatMessage());
		add(txtMessage, BorderLayout.NORTH);

		txtChat = new JTextPane();
		txtChat.setEditable(false);
		txtChat.setPreferredSize(new Dimension(300, 100));
		txtChat.setBackground(CHAT_BACKGROUND_COLOR);
		add(txtChat, BorderLayout.CENTER);

		doc = txtChat.getStyledDocument();
	}

	public void addListener(ChatPanelListener listener) {
		listeners.add(listener);
	}

	public void appendAdminMessage(String sender, String message) {
		SimpleAttributeSet keyWord = new SimpleAttributeSet();
		StyleConstants.setForeground(keyWord, ADMIN_MESSAGE_COLOR);
		StyleConstants.setBold(keyWord, true);

		try {
			doc.insertString(doc.getLength(), sender + ": " + message + '\n', keyWord);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void appendAlertMessage(String message) {
		SimpleAttributeSet keyWord = new SimpleAttributeSet();
		StyleConstants.setForeground(keyWord, ALERT_MESSAGE_COLOR);
		StyleConstants.setBold(keyWord, true);

		try {
			doc.insertString(doc.getLength(), "ALERTA: " + message + '\n', keyWord);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void appendMessage(String sender, String message) {
		SimpleAttributeSet keyWord = new SimpleAttributeSet();
		StyleConstants.setForeground(keyWord, CHAT_FOREGROUND_COLOR);
		StyleConstants.setBold(keyWord, true);

		try {
			doc.insertString(doc.getLength(), sender + ": " + message + '\n', keyWord);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void appendPrivateMessage(String sender, String message) {
		SimpleAttributeSet keyWord = new SimpleAttributeSet();
		StyleConstants.setForeground(keyWord, PRIVATE_MESSAGE_COLOR);
		StyleConstants.setBold(keyWord, true);

		try {
			doc.insertString(doc.getLength(), sender + ": " + message + '\n', keyWord);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void appendSystemMessage(String message) {
		SimpleAttributeSet keyWord = new SimpleAttributeSet();
		StyleConstants.setForeground(keyWord, SYSTEM_MESSAGE_COLOR);
		StyleConstants.setBold(keyWord, true);

		try {
			doc.insertString(doc.getLength(), "SYSTEM: " + message + '\n', keyWord);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void clearChat() {
		txtChat.setText("");
	}

	public void clearMessage() {
		txtMessage.setText("");
	}

	public boolean isClearAfterSend() {
		return clearAfterSend;
	}

	private void onChatMessage() {
		String message = txtMessage.getText();
		if (clearAfterSend)
			txtMessage.setText("");

		for (ChatPanelListener listener : listeners)
			if (listener != null)
				try {
					listener.onMessage(message);
				} catch (Throwable e) {
					e.printStackTrace();
				}
	}

	public void removeListener(ChatPanelListener listener) {
		listeners.remove(listener);
	}

	public void setClearAfterSend(boolean clearAfterSend) {
		this.clearAfterSend = clearAfterSend;
	}

}
