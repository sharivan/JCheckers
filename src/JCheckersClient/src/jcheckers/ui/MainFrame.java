package jcheckers.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import common.config.Config;
import common.config.ConfigEntry;
import common.config.MapConfigEntry;
import common.process.ProcessQueue;
import common.util.Tree;
import jcheckers.client.net.Connection;
import jcheckers.client.net.CoreConnection;
import jcheckers.client.net.Room;
import jcheckers.client.net.RoomConnectionListener;
import jcheckers.client.net.RoomInfo;
import jcheckers.client.net.Table;
import jcheckers.client.net.User;
import jcheckers.client.net.boards.draughts.DraughtsConnection;
import jcheckers.client.net.boards.draughts.DraughtsRoom;
import jcheckers.client.net.boards.draughts.DraughtsTableParams;
import jcheckers.client.net.boards.draughts.DraughtsUser;
import jcheckers.ui.chat.ChatPanel;
import jcheckers.ui.game.TableFrame;
import jcheckers.ui.lobby.RoomListPanel;
import jcheckers.ui.lobby.TableEntryListener;
import jcheckers.ui.lobby.TableListPanel;
import jcheckers.ui.login.LoginFrame;
import jcheckers.ui.login.LoginListener;
import jcheckers.ui.options.OptionListener;
import jcheckers.ui.options.OptionsFrame;
import jcheckers.ui.options.TableParamsPanel;
import jcheckers.ui.register.RegisterFrame;
import jcheckers.ui.register.RegisterListener;
import jcheckers.ui.user.UserListPanel;

public class MainFrame extends JFrame {

	private class DraughtsConnectionHandler implements RoomConnectionListener {

		@Override
		public void onAdminChat(Connection c, String sender, String message) {
			EventQueue.invokeLater(() -> handleAdminChat(sender, message));
		}

		@Override
		public void onChat(Connection c, String sender, String message) {
			EventQueue.invokeLater(() -> handleChat(sender, message));
		}

		@Override
		public void onClose(Connection c) {

		}

		@Override
		public void onError(Connection c, Throwable e) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Erro: " + e.getMessage()));
		}

		@Override
		public void onJoinLobby(Connection c, Room room) {
			MainFrame.this.room = (DraughtsRoom) room;
		}

		@Override
		public void onJoinUser(Connection c, User user) {
			EventQueue.invokeLater(() -> userList.addEntry((DraughtsUser) user));
		}

		@Override
		public void onLeaveUser(Connection c, User user) {
			EventQueue.invokeLater(() -> userList.removeEntry((DraughtsUser) user));
		}

		@Override
		public void onNewTable(Connection c, Table table) {
			EventQueue.invokeLater(() -> tableList.addEntry(table));
		}

		@Override
		public void onOpen(Connection c) {

		}

		@Override
		public void onPrivateChat(Connection c, String sender, String message) {
			EventQueue.invokeLater(() -> handlePrivateChat(sender, message));
		}

		@Override
		public void onRemoveTable(Connection c, Table table) {
			EventQueue.invokeLater(() -> tableList.removeEntry(table));
		}

		@Override
		public void onUpdateTable(Connection c, Table table) {
			EventQueue.invokeLater(() -> tableList.updateEntry(table));
		}

		@Override
		public void onUserList(Connection connection, User[] users) {
			EventQueue.invokeLater(() -> userList.addEntries(users));
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -670140197532946365L;
	
	private static final String DEFAULT_SERVER_URL = "http://localhost:8080/JCheckersServer/CoreServlet";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				MainFrame frame = new MainFrame(args.length > 0 ? new URL(args[0]) : null);
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private ImageList imgList;
	private ProcessQueue queue;

	private URL url;
	private CoreConnection core;

	private String host;
	private int port;
	private String username;
	private String sid;
	private DraughtsConnection connection;
	private DraughtsRoom room;
	private DraughtsTableParams params;

	private JPanel contentPane;
	private RoomListPanel roomList;
	private JPanel pnlLobbyContent;
	private TableListPanel tableList;
	private UserListPanel userList;
	private ChatPanel pnlChat;
	private JPanel pnlCenter;
	private JButton btnLogin;
	private JButton btnRegister;
	private JPanel pnlUsersContent;
	private JTabbedPane pnlRight;
	private JPanel pnlStart;
	private JPanel pnlRoom;
	private JButton btnOptions;
	private JButton btnExit;
	private JLabel lblStatusBar;
	private JPanel pnlCreateTable;
	private TableParamsPanel pnlTableParams;
	private JButton btnCreateTable;
	
	public MainFrame() throws ParserConfigurationException, SAXException, IOException {
		this(null);
	}
	
	private void parseClientConfigs(Config config) throws MalformedURLException {
		Tree.Node<ConfigEntry> root = config.getChild(0);
		ConfigEntry entry = root.getValue();
		if (!entry.getName().equalsIgnoreCase("config"))
			return;

		for (int i = 0; i < root.getChildCount(); i++) {
			Tree.Node<ConfigEntry> child = root.getChild(i);
			entry = child.getValue();
			if (!(entry instanceof MapConfigEntry))
				continue;

			String name = entry.getName();
			Map<String, String> attrs = ((MapConfigEntry) entry).map();
			if (name.equals("client")) {
				String urlStr = attrs.getOrDefault("url", DEFAULT_SERVER_URL);
				url = new URL(urlStr);
			}
		}
	}

	/**
	 * Create the frame.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public MainFrame(URL url) throws ParserConfigurationException, SAXException, IOException {
		if (url == null) {
			Config config = Config.parseConfig(new File("config.xml"));
			parseClientConfigs(config);
		} else
			this.url = url;
		
		setTitle("JCheckers");
		queue = new ProcessQueue();

		imgList = new ImageListImpl("images");
		imgList.init();

		core = new CoreConnection(this.url);

		params = new DraughtsTableParams();
		params.setGameType(DraughtsTableParams.AMERICAN);

		host = "localhost";
		port = 5085;

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onExit();
			}
		});

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 900, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		lblStatusBar = new JLabel("");
		contentPane.add(lblStatusBar, BorderLayout.SOUTH);

		pnlRight = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(pnlRight, BorderLayout.EAST);

		pnlStart = new JPanel();
		GridBagLayout gbl_pnlStart = new GridBagLayout();
		gbl_pnlStart.columnWidths = new int[] { 247, 0 };
		gbl_pnlStart.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_pnlStart.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_pnlStart.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		pnlStart.setLayout(gbl_pnlStart);
		pnlRight.addTab("Início", null, pnlStart, null);

		pnlRight.setSelectedIndex(0);

		pnlRoom = new JPanel();
		pnlRoom.setLayout(new BorderLayout());

		pnlCreateTable = new JPanel();
		pnlCreateTable.setLayout(new BorderLayout());

		pnlTableParams = new TableParamsPanel();
		pnlCreateTable.add(pnlTableParams, BorderLayout.CENTER);

		btnCreateTable = new JButton();
		btnCreateTable.setText("Criar Mesa");
		btnCreateTable.addActionListener((e) -> createTable());
		pnlCreateTable.add(btnCreateTable, BorderLayout.SOUTH);

		btnLogin = new JButton("Login");
		btnLogin.addActionListener((e) -> showLoginDialog(null, false));
		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.fill = GridBagConstraints.BOTH;
		gbc_btnLogin.insets = new Insets(0, 0, 5, 0);
		gbc_btnLogin.gridx = 0;
		gbc_btnLogin.gridy = 0;
		pnlStart.add(btnLogin, gbc_btnLogin);

		btnRegister = new JButton("Registrar");
		btnRegister.addActionListener((e) -> showRegisterDialog(null, null, 0));
		GridBagConstraints gbc_btnRegister = new GridBagConstraints();
		gbc_btnRegister.fill = GridBagConstraints.BOTH;
		gbc_btnRegister.insets = new Insets(0, 0, 5, 0);
		gbc_btnRegister.gridx = 0;
		gbc_btnRegister.gridy = 1;
		pnlStart.add(btnRegister, gbc_btnRegister);

		btnOptions = new JButton("Op\u00E7\u00F5es");
		btnOptions.setVisible(false);
		btnOptions.addActionListener((e) -> showOptionDialog(url));
		GridBagConstraints gbc_btnOptions = new GridBagConstraints();
		gbc_btnOptions.fill = GridBagConstraints.BOTH;
		gbc_btnOptions.insets = new Insets(0, 0, 5, 0);
		gbc_btnOptions.gridx = 0;
		gbc_btnOptions.gridy = 2;
		pnlStart.add(btnOptions, gbc_btnOptions);

		btnExit = new JButton("Sair");
		btnExit.addActionListener((e) -> onExit());
		GridBagConstraints gbc_btnExit = new GridBagConstraints();
		gbc_btnExit.insets = new Insets(0, 0, 5, 0);
		gbc_btnExit.fill = GridBagConstraints.BOTH;
		gbc_btnExit.gridx = 0;
		gbc_btnExit.gridy = 3;
		pnlStart.add(btnExit, gbc_btnExit);

		pnlUsersContent = new JPanel();
		pnlUsersContent.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		pnlUsersContent.setBounds(10, 141, 247, 387);
		pnlUsersContent.setVisible(false);
		pnlRoom.add(pnlUsersContent);
		pnlUsersContent.setLayout(new BorderLayout(0, 0));

		pnlCenter = new JPanel();
		pnlCenter.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		contentPane.add(pnlCenter, BorderLayout.CENTER);
		pnlCenter.setLayout(new BorderLayout(0, 0));

		roomList = new RoomListPanel();
		roomList.addListener((id) -> joinLobby(id));

		pnlLobbyContent = new JPanel();
		pnlLobbyContent.setLayout(new BorderLayout(0, 0));

		tableList = new TableListPanel();
		tableList.addListener(new TableEntryListener() {

			@Override
			public void onSit(int id, int sitIndex) {
				joinTable(id, sitIndex);
			}

			@Override
			public void onWatch(int id) {
				joinTable(id, -1);
			}
		});
		pnlLobbyContent.add(tableList, BorderLayout.CENTER);

		pnlChat = new ChatPanel();
		pnlChat.addListener((message) -> sendMessageToChat(message));
		pnlLobbyContent.add(pnlChat, BorderLayout.SOUTH);

		userList = new UserListPanel();
	}

	private void createTable() {
		fetchTableParams();

		TableFrame table = new TableFrame(host, port, username, sid);
		table.setVisible(true);
		table.connectCreating(connection.getRoomID(), params);
	}

	private void fetchTableParams() {
		params.setGameType(pnlTableParams.getVariant());
		params.setHasIncrementTime(pnlTableParams.hasIncrementTime());
		params.setHasTime(pnlTableParams.hasTime());
		params.setHasTimePerTurn(pnlTableParams.hasTimePerMove());
		params.setIncrementTime(pnlTableParams.getIncrementTime());
		params.setNoWatches(!pnlTableParams.isAllowWatchers());
		params.setPrivacy(pnlTableParams.getPrivacy());
		params.setRated(pnlTableParams.isRated());
		params.setSwapSides(pnlTableParams.isSwapSides());
		params.setTime(pnlTableParams.getTime());
		params.setTimePerTurn(pnlTableParams.getTimePerMove());
	}

	private void handleAdminChat(String sender, String message) {
		pnlChat.appendAdminMessage(sender, message);
	}

	private void handleChat(String sender, String message) {
		pnlChat.appendMessage(sender, message);
	}

	private void handlePrivateChat(String sender, String message) {
		pnlChat.appendPrivateMessage(sender, message);
	}

	private void joinLobby(int id) {
		pnlCenter.remove(roomList);
		pnlCenter.add(pnlLobbyContent, BorderLayout.CENTER);
		pnlCenter.revalidate();
		pnlCenter.repaint();

		pnlUsersContent.setVisible(true);
		pnlUsersContent.add(userList, BorderLayout.CENTER);
		pnlUsersContent.revalidate();
		pnlUsersContent.repaint();

		connection = new DraughtsConnection(host, port, username, sid);
		connection.addListener(new DraughtsConnectionHandler());
		connection.joinLobby(id);

		pnlRight.addTab("Sala", null, pnlRoom, null);
		pnlRight.addTab("Criar Mesa", null, pnlCreateTable, null);

		pnlRight.setSelectedIndex(1);
	}

	private void joinTable(int id, int seatIndex) {
		TableFrame table = new TableFrame(host, port, username, sid);
		table.setVisible(true);

		if (seatIndex != -1)
			table.connectSitting(connection.getRoomID(), id, seatIndex);
		else
			table.connectWatching(connection.getRoomID(), id);
	}

	private void login(String username, String password) {
		queue.post(() -> {
			String[] sid = new String[1];
			ArrayList<RoomInfo> rooms = new ArrayList<>();
			try {
				String[] host = new String[1];
				int[] port = new int[1];
				int result = core.login("draughts", username, password, sid, host, port, rooms);
				if (result == 1)
					showLoginDialog(username, true);
				else {
					this.username = username;
					this.sid = sid[0];
					this.host = host[0];
					this.port = port[0];

					EventQueue.invokeLater(() -> {
						btnLogin.setEnabled(false);
						btnRegister.setEnabled(false);
						pnlCenter.add(roomList, BorderLayout.CENTER);
						roomList.addEntries(rooms);
						pnlCenter.revalidate();
						pnlCenter.repaint();
					});
				}
			} catch (ConnectException e) {
				JOptionPane.showMessageDialog(new JFrame(), "Erro ao se conectar ao servidor principal.");
			} catch (Throwable e) {
				JOptionPane.showMessageDialog(new JFrame(), "Erro interno no servidor principal: " + e.toString());
				e.printStackTrace();
			}
		});
	}

	private void onExit() {
		imgList.destroy();
		queue.close();

		dispose();
		System.exit(0);
	}

	private void register(String username, String password, String email) {
		queue.post(() -> {
			try {
				int result = core.register("draughts", username, password, email);
				if (result == -1)
					JOptionPane.showMessageDialog(new JFrame(), "Erro interno no servidor principal: ");
				else if (result == 0)
					JOptionPane.showMessageDialog(new JFrame(), "Registro completado com sucesso.");
				else
					showRegisterDialog(username, email, result);
			} catch (ConnectException e) {
				JOptionPane.showMessageDialog(new JFrame(), "Erro ao se conectar ao servidor principal.");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	private void sendMessageToChat(String message) {
		room.sendMessageToChat(message);
	}

	private void showLoginDialog(String username, boolean warn) {
		LoginFrame frame = new LoginFrame();
		frame.addListener(new LoginListener() {

			@Override
			public void onCancel() {

			}

			@Override
			public void onLogin(String username, char[] password) {
				queue.post(() -> login(username, new String(password)));
			}
		});

		if (username != null)
			frame.setUsername(username);

		frame.warn(warn);
		frame.setVisible(true);
	}

	private void showOptionDialog(URL url) {
		OptionsFrame frame = new OptionsFrame();
		frame.addListener(new OptionListener() {

			@Override
			public void onCancel() {

			}

			@Override
			public void onOK() {
				try {
					MainFrame.this.url = frame.getURL();
					core.setURL(MainFrame.this.url);
				} catch (MalformedURLException e) {
					JOptionPane.showMessageDialog(new JFrame(), "URL inválida: " + frame.getURLStr());
				}
			}
		});

		frame.setURL(url);
		frame.setVisible(true);
	}

	private void showRegisterDialog(String username, String email, int warn) {
		RegisterFrame frame = new RegisterFrame();
		frame.addListener(new RegisterListener() {

			@Override
			public void onCancel() {

			}

			@Override
			public void onRegister(String username, char[] password, String email) {
				queue.post(() -> register(username, new String(password), email));
			}
		});

		if (username != null)
			frame.setUsername(username);

		if (email != null)
			frame.setEMail(email);

		frame.warn(warn);
		frame.setVisible(true);
	}
}
