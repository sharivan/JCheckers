package jcheckers.ui.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import common.process.ProcessQueue;
import common.util.DigestUtil;
import jcheckers.client.net.Connection;
import jcheckers.client.net.OpenTable;
import jcheckers.client.net.Table;
import jcheckers.client.net.Table.Seat;
import jcheckers.client.net.TableParams;
import jcheckers.client.net.User;
import jcheckers.client.net.UserStats;
import jcheckers.client.net.boards.RatingChange;
import jcheckers.client.net.boards.draughts.DraughtsConnection;
import jcheckers.client.net.boards.draughts.DraughtsTable;
import jcheckers.client.net.boards.draughts.DraughtsTableConnectionListener;
import jcheckers.client.net.boards.draughts.DraughtsTableParams;
import jcheckers.client.net.boards.draughts.DraughtsUser;
import jcheckers.client.net.boards.draughts.OpenDraughtsTable;
import jcheckers.client.net.boards.draughts.OpenDraughtsTable.DraughtsMove;
import jcheckers.common.logic.Game.StopReason;
import jcheckers.common.logic.Player;
import jcheckers.common.logic.boards.BoardMove;
import jcheckers.common.logic.boards.BoardPosition;
import jcheckers.common.logic.boards.draughts.DraughtsConfig;
import jcheckers.common.logic.boards.draughts.DraughtsGame;
import jcheckers.common.logic.boards.draughts.DraughtsGameListener;
import jcheckers.common.logic.boards.draughts.DraughtsMan;
import jcheckers.common.logic.boards.draughts.DraughtsPiece;
import jcheckers.ui.ImageListImpl;
import jcheckers.ui.board.GameBoard;
import jcheckers.ui.chat.ChatPanel;
import jcheckers.ui.options.TableParamsPanel;
import jcheckers.ui.user.UserListPanel;

public class TableFrame extends JFrame {

	private class DraughtsConnectionHandler implements DraughtsTableConnectionListener {

		@Override
		public void onAdminChat(Connection c, String sender, String message) {
			EventQueue.invokeLater(() -> handleAdminChat(sender, message));
		}

		@Override
		public void onAlreadyConnected(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você já está conectado a esta mesa."));
		}

		@Override
		public void onAvatars(Connection c, int[] avatars) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBanned(Connection c, String bannedUntil) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você foi banido desta sala até " + bannedUntil));
		}

		@Override
		public void onChat(Connection c, String sender, String message) {
			EventQueue.invokeLater(() -> handleChat(sender, message));
		}

		@Override
		public void onClose(Connection c) {

		}

		@Override
		public void onConnectionRefused(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Conexão recusada."));
		}

		@Override
		public void onContinueGame(Connection c) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCouldNotReconnectToTheTable(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Não foi possível reconectar à mesa, tente novamente depois."));
		}

		@Override
		public void onError(Connection c, Throwable e) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Erro: " + e.getMessage()));
		}

		@Override
		public void onGamePaused(Connection c) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage("Jogo pausado."));
		}

		@Override
		public void onGameState(DraughtsConnection c, int gameID, int gameType, boolean running, int gameState, int currentTurn, int timePerTurn, int[] times, DraughtsMove[] moves) {
			EventQueue.invokeLater(() -> handleGameState(gameID, gameType, running, gameState, currentTurn, timePerTurn, times, moves));
		}

		@Override
		public void onHideControls(Connection c) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onInvalidPassword(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Senha incorreta."));
		}

		@Override
		public void onInvalidRoom(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Sala inválida."));
		}

		@Override
		public void onInviationAutoRejected(Connection c, String name, int id) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onInviationRejected(Connection c, String name, int id) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onInviationSent(Connection c, String name, int id) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onInviationsInactive(Connection c) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onJoinTable(Connection c, OpenTable table) {
			openTable = (OpenDraughtsTable) table;
		}

		@Override
		public void onJoinUser(Connection c, User user) {
			EventQueue.invokeLater(() -> roomUserList.addEntry((DraughtsUser) user));
		}

		@Override
		public void onKickedByHost(Connection c, String kickedName, int kickedID, String kickerName, int kickerID) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage((kickedName == null ? "Você" : kickedName) + " foi expulso por " + kickerName));
		}

		@Override
		public void onLeaveUser(Connection c, User user) {
			EventQueue.invokeLater(() -> roomUserList.removeEntry((DraughtsUser) user));
		}

		@Override
		public void onNoEnoughPlayersToStartTheGame(Connection c) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onOpen(Connection c) {

		}

		@Override
		public void onParametersChangedBy(Connection c, int id, String name) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage("Os parâmetros da mesa foram alterados por " + name));
		}

		@Override
		public void onPlayerAcceptedDraw(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " aceitou o empate."));
		}

		@Override
		public void onPlayerAcceptedUndoMove(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " aceitou voltar a jogada."));
		}

		@Override
		public void onPlayerNotFoundInServer(Connection c, String name) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Usuário " + name + " não foi encontrado no servidor."));
		}

		@Override
		public void onPlayerOfferedDraw(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " ofereceu empate."));
		}

		@Override
		public void onPlayerReconnectingFailed(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " não conseguiu se reconectar à mesa."));
		}

		@Override
		public void onPlayerRejectedDraw(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " rejeitou o empate."));
		}

		@Override
		public void onPlayerRejectedPauseGame(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " rejeitou pausar o jogo."));
		}

		@Override
		public void onPlayerRejectedUndoMove(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " rejeitou desfazer a última jogada."));
		}

		@Override
		public void onPlayersInTheSameLocalNetwork(Connection c, String[] names) {
			EventQueue.invokeLater(() -> handlePlayersOnTheSameLocalNetwork(names));
		}

		@Override
		public void onPlayerStandUp(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " levantou-se da mesa."));
		}

		@Override
		public void onPlayerSuggestedPauseGame(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " sugeriu pausar o jogo."));
		}

		@Override
		public void onPlayerSuggestedUndoMove(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " sugeriu desfazer a última jogada."));
		}

		@Override
		public void onPlayerTryingToReconnect(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " foi desconectado da mesa e está tentando reconectar."));
		}

		@Override
		public void onPong(Connection c, int src, int dst) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPrivateChat(Connection c, String sender, String message) {
			EventQueue.invokeLater(() -> handlePrivateChat(sender, message));
		}

		@Override
		public void onPrivateTable(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Mesa privada."));
		}

		@Override
		public void onQuestionBits(Connection c, int acceptedBits, int rejectedBits) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onQuestionCanceled(Connection c) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRatingChanges(Connection c, RatingChange[] ratingChanges) {
			EventQueue.invokeLater(() -> handleRatingChanges(ratingChanges));
		}

		@Override
		public void onRatingTooHigh(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Sua pontuação é muito alta."));
		}

		@Override
		public void onResponseInfo(Connection c, UserStats stats, int[] tables, int inactiveTime) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServerShuttingDown(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Servidor está sendo desligado."));
		}

		@Override
		public void onServerVersion(Connection c, String version, String lastRelease) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage("Server - Version: " + version + " - Last Release: " + lastRelease));
		}

		@Override
		public void onStartGame(Connection c) {
			EventQueue.invokeLater(() -> handleStartGame());
		}

		@Override
		public void onStartGameSuggested(Connection c, String name, int id, boolean dontAsk) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartGameSuggestRejected(Connection c, String name, int id) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopGame(Connection c) {
			EventQueue.invokeLater(() -> handleStopGame());
		}

		@Override
		public void onStopGameSuggested(Connection c, String name, int id, boolean dontAsk) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopGameSuggestRejected(Connection c, String name, int id) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTableClosedBy(Connection c, int id) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "A mesa foi fechada por " + table.getUserByID(id).getName()));
		}

		@Override
		public void onTableFocus(Connection c, int flags) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTableNotExist(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Mesa não existe."));
		}

		@Override
		public void onTransferHost(Connection c, String newHostName, int newHostID, String oldHostName, int oldHostID) {
			EventQueue.invokeLater(() -> handleTransferHost(newHostName, oldHostName));
		}

		@Override
		public void onUpdate(Connection c, Table table) {
			EventQueue.invokeLater(() -> handleTableUpdate((DraughtsTable) table));
		}

		@Override
		public void onUpdateUsers(Connection c, User[] users) {
			EventQueue.invokeLater(() -> roomUserList.updateEntries(users));
		}

		@Override
		public void onUserList(Connection c, User[] users) {
			EventQueue.invokeLater(() -> roomUserList.addEntries(users));
		}

		@Override
		public void onWatchersNotAllowed(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Espectadores não permitidos."));
		}

		@Override
		public void onWelcome(Connection c, String roomName) {

		}

		@Override
		public void onYouCantBootAPlayerNotInServer(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode expulsar um usuário que não está no servidor."));
		}

		@Override
		public void onYouCantChangeParametersNow(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode alterar os parâmetros da mesa agora."));
		}

		@Override
		public void onYouCantInviteAPlayerAlreadyInTheTable(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode convidar um usuário que já está na mesa."));
		}

		@Override
		public void onYouCantInviteAPlayerNotInServer(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode convidar um usuário que não está no servidor."));
		}

		@Override
		public void onYouCantInviteYourself(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode convidar a você mesmo."));
		}

		@Override
		public void onYouCantTransferHostToAPlayerNotInServer(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode transferir o host para um usuário que não está no servidor."));
		}

		@Override
		public void onYouCantTransferHostToYourself(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode transferir o host para você mesmo."));
		}

		@Override
		public void onYoureNotTheHostToChangeParameters(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode alterar os parâmetros da mesa, você não é o host."));
		}

		@Override
		public void onYoureNotTheHostToInvitePlayers(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode convidar pessoas para a mesa, você não é o host."));
		}

		@Override
		public void onYoureNotTheHostToTransferHost(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Você não pode transferir o host, você não é o host."));
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2466747814427990717L;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				DraughtsTableParams params = new DraughtsTableParams();
				params.setGameType(Integer.parseInt(args[5]));

				final String host = args[0];
				final int port = Integer.parseInt(args[1]);
				final String username = args[2];
				final String password = args[3];
				final String sid = DigestUtil.md5(password);
				final int room = Integer.parseInt(args[4]);

				TableFrame frame = new TableFrame(host, port, username, sid);
				frame.setVisible(true);

				frame.connectCreating(room, params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private ImageListImpl imgList;

	private DraughtsConnection connection;
	private OpenDraughtsTable openTable;
	private DraughtsTable table;
	private DraughtsGame game;

	private ProcessQueue queue;

	private String host;
	private int port;
	private String username;
	private String sid;

	private boolean running;
	private int currentTurn = -1;
	private int myTurn = -1;
	private int turnNum = 0;

	private JPanel contentPane;

	private JLabel lblPlayer1;
	private JLabel lblPlayer2;

	private GameBoard board;

	private ChatPanel pnlChat;

	private UserListPanel roomUserList;
	private UserListPanel tableUserList;

	private JTabbedPane pnlRight;

	private JPanel pnlCenter;

	private JPanel pnlControls;

	private TableParamsPanel pnlTableParams;

	private JPanel pnlRefreshTableParameters;

	private JButton btnRefreshTableParameters;

	private JLabel[] lblPlayers;
	private JButton[] btnSeats;

	private JPanel pnlPlayers;

	private JPanel pnlGameControls;

	private JButton btnStartGame;

	private JButton btnStandUp;

	private JButton btnOfferDraw;

	private JButton btnResign;

	private JPanel pnlPlayer1;
	private JPanel pnlPlayer2;
	private JButton btnSit1;
	private JButton btnSit2;

	/**
	 * Create the frame.
	 */
	public TableFrame(String host, int port, String username, String sid) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.sid = sid;

		imgList = new ImageListImpl("images");
		imgList.init();

		queue = new ProcessQueue();

		game = new DraughtsGame(() -> queue);

		game.addListener(new DraughtsGameListener() {

			@Override
			public void onCapture(DraughtsPiece piece, int row, int col) {

			}

			@Override
			public void onChangeTurn(int turn) {
				System.out.println("Change turn: " + turn);
			}

			@Override
			public void onMove(BoardMove move) {
				if (isMyTurn())
					openTable.doMove(turnNum, myTurn, translateMove(move));
			}

			@Override
			public void onNextRound(int round) {

			}

			@Override
			public void onPause(int time) {
				System.out.println("Game paused for " + time + "ms");
			}

			@Override
			public void onPauseTimeOut() {
				System.out.println("Game pause timeout!");
			}

			@Override
			public void onPromote(DraughtsMan man, int row, int col) {

			}

			@Override
			public void onResume() {
				System.out.println("Game resumed!");
			}

			@Override
			public void onRotateLeft() {

			}

			@Override
			public void onRotateRight() {

			}

			@Override
			public void onSingleMove(BoardPosition src, BoardPosition dst) {

			}

			@Override
			public void onStart() {
				System.out.println("Game starting...");
			}

			@Override
			public void onStarted() {
				System.out.println("Game started!");
			}

			@Override
			public void onStop(StopReason reason) {
				System.out.println("Game stoped: " + reason);
			}

			@Override
			public void onSwap(int index1, int index2) {

			}

			@Override
			public void onUndoLastMove() {
				System.out.println("Undo last move!");
			}
		});

		game.setConfig(DraughtsConfig.AMERICAN);
		game.setUseTime(false);
		game.setUseIncrementTime(false);
		game.setUseTimePerTurn(false);

		Player white = game.join(DraughtsGame.WHITE, "White");
		Player black = game.join(DraughtsGame.BLACK, "Black");

		white.start();
		black.start();
		if (!game.start()) {
			System.err.println("Game not started!");
			return;
		}

		game.stop();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				connection.close();
				board.close();
				game.close();
				queue.close();
				imgList.destroy();
			}
		});

		lblPlayers = new JLabel[2];
		btnSeats = new JButton[2];

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 814, 734);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		pnlRight = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(pnlRight, BorderLayout.EAST);

		pnlControls = new JPanel();
		GridBagLayout gbl_pnlControls = new GridBagLayout();
		gbl_pnlControls.columnWidths = new int[] { 0 };
		gbl_pnlControls.rowHeights = new int[] { 0, 0 };
		gbl_pnlControls.columnWeights = new double[] { 1.0 };
		gbl_pnlControls.rowWeights = new double[] { 0.0, 1.0 };
		pnlControls.setLayout(gbl_pnlControls);

		pnlPlayers = new JPanel();
		pnlPlayers.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		pnlPlayers.setPreferredSize(new Dimension(100, 50));
		GridBagConstraints gbc_pnlPlayers = new GridBagConstraints();
		gbc_pnlPlayers.insets = new Insets(0, 0, 5, 0);
		gbc_pnlPlayers.gridx = 0;
		gbc_pnlPlayers.gridy = 0;
		gbc_pnlPlayers.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlPlayers.anchor = GridBagConstraints.NORTH;
		pnlControls.add(pnlPlayers, gbc_pnlPlayers);
		pnlPlayers.setLayout(new GridLayout(2, 0, 0, 0));

		pnlPlayer1 = new JPanel();
		pnlPlayer1.setOpaque(true);
		pnlPlayer1.setFont(new Font("Tahoma", Font.BOLD, 11));
		pnlPlayer1.setForeground(Color.WHITE);
		pnlPlayer1.setBackground(ChatPanel.CHAT_BACKGROUND_COLOR);
		pnlPlayers.add(pnlPlayer1);
		GridBagLayout gbl_pnlPlayer1 = new GridBagLayout();
		gbl_pnlPlayer1.columnWidths = new int[] { 0, 0, 0 };
		gbl_pnlPlayer1.rowHeights = new int[] { 0, 0 };
		gbl_pnlPlayer1.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_pnlPlayer1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlPlayer1.setLayout(gbl_pnlPlayer1);

		lblPlayer1 = new JLabel("(Vazio)");
		lblPlayer1.setOpaque(true);
		lblPlayer1.setForeground(Color.WHITE);
		lblPlayer1.setBackground(ChatPanel.CHAT_BACKGROUND_COLOR);
		lblPlayer1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPlayer1.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblPlayer1 = new GridBagConstraints();
		gbc_lblPlayer1.fill = GridBagConstraints.BOTH;
		gbc_lblPlayer1.insets = new Insets(0, 0, 0, 5);
		gbc_lblPlayer1.gridx = 0;
		gbc_lblPlayer1.gridy = 0;
		pnlPlayer1.add(lblPlayer1, gbc_lblPlayer1);
		lblPlayers[0] = lblPlayer1;

		btnSit1 = new JButton("Sentar");
		btnSit1.setOpaque(true);
		btnSit1.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnSit1.setForeground(Color.WHITE);
		btnSit1.setBackground(ChatPanel.CHAT_BACKGROUND_COLOR);
		btnSit1.addActionListener((e) -> sit(0));
		GridBagConstraints gbc_btnSit1 = new GridBagConstraints();
		gbc_btnSit1.fill = GridBagConstraints.BOTH;
		gbc_btnSit1.gridx = 1;
		gbc_btnSit1.gridy = 0;
		pnlPlayer1.add(btnSit1, gbc_btnSit1);
		btnSeats[0] = btnSit1;

		pnlPlayer2 = new JPanel();
		pnlPlayer2.setOpaque(true);
		pnlPlayer2.setFont(new Font("Tahoma", Font.BOLD, 11));
		pnlPlayer2.setForeground(Color.RED);
		pnlPlayer2.setBackground(ChatPanel.CHAT_BACKGROUND_COLOR);
		pnlPlayers.add(pnlPlayer2);
		GridBagLayout gbl_pnlPlayer2 = new GridBagLayout();
		gbl_pnlPlayer2.columnWidths = new int[] { 0, 0, 0 };
		gbl_pnlPlayer2.rowHeights = new int[] { 0, 0 };
		gbl_pnlPlayer2.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_pnlPlayer2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlPlayer2.setLayout(gbl_pnlPlayer2);

		lblPlayer2 = new JLabel("(Vazio)");
		lblPlayer2.setOpaque(true);
		lblPlayer2.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPlayer2.setForeground(Color.RED);
		lblPlayer2.setBackground(ChatPanel.CHAT_BACKGROUND_COLOR);
		GridBagConstraints gbc_lblPlayer2 = new GridBagConstraints();
		gbc_lblPlayer2.fill = GridBagConstraints.BOTH;
		gbc_lblPlayer2.insets = new Insets(0, 0, 0, 5);
		gbc_lblPlayer2.gridx = 0;
		gbc_lblPlayer2.gridy = 0;
		pnlPlayer2.add(lblPlayer2, gbc_lblPlayer2);
		lblPlayers[1] = lblPlayer2;
		lblPlayer2.setHorizontalAlignment(SwingConstants.CENTER);

		btnSit2 = new JButton("Sentar");
		btnSit2.setOpaque(true);
		btnSit2.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnSit2.setForeground(Color.RED);
		btnSit2.setBackground(ChatPanel.CHAT_BACKGROUND_COLOR);
		btnSit2.addActionListener((e) -> sit(1));
		GridBagConstraints gbc_btnSit2 = new GridBagConstraints();
		gbc_btnSit2.fill = GridBagConstraints.BOTH;
		gbc_btnSit2.gridx = 1;
		gbc_btnSit2.gridy = 0;
		pnlPlayer2.add(btnSit2, gbc_btnSit2);
		btnSeats[1] = btnSit2;

		pnlRight.addTab("Controles", null, pnlControls, null);

		pnlGameControls = new JPanel();
		pnlGameControls.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_pnlGameControls = new GridBagConstraints();
		gbc_pnlGameControls.fill = GridBagConstraints.BOTH;
		gbc_pnlGameControls.gridx = 0;
		gbc_pnlGameControls.gridy = 1;
		pnlControls.add(pnlGameControls, gbc_pnlGameControls);
		GridBagLayout gbl_pnlGameControls = new GridBagLayout();
		gbl_pnlGameControls.columnWidths = new int[] { 0, 0 };
		gbl_pnlGameControls.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_pnlGameControls.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_pnlGameControls.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		pnlGameControls.setLayout(gbl_pnlGameControls);

		btnStartGame = new JButton("Iniciar Jogo");
		btnStartGame.addActionListener((e) -> suggestStartGame());
		btnStartGame.setEnabled(false);
		GridBagConstraints gbc_btnStartGame = new GridBagConstraints();
		gbc_btnStartGame.insets = new Insets(0, 0, 5, 0);
		gbc_btnStartGame.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStartGame.gridx = 0;
		gbc_btnStartGame.gridy = 0;
		pnlGameControls.add(btnStartGame, gbc_btnStartGame);

		btnStandUp = new JButton("Levantar");
		btnStandUp.addActionListener((e) -> standUp());
		btnStandUp.setEnabled(false);
		GridBagConstraints gbc_btnStandUp = new GridBagConstraints();
		gbc_btnStandUp.insets = new Insets(0, 0, 5, 0);
		gbc_btnStandUp.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStandUp.gridx = 0;
		gbc_btnStandUp.gridy = 1;
		pnlGameControls.add(btnStandUp, gbc_btnStandUp);

		btnOfferDraw = new JButton("Oferecer Empate");
		btnOfferDraw.addActionListener((e) -> offerDraw());
		btnOfferDraw.setEnabled(false);
		GridBagConstraints gbc_btnOfferDraw = new GridBagConstraints();
		gbc_btnOfferDraw.insets = new Insets(0, 0, 5, 0);
		gbc_btnOfferDraw.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOfferDraw.gridx = 0;
		gbc_btnOfferDraw.gridy = 2;
		pnlGameControls.add(btnOfferDraw, gbc_btnOfferDraw);

		btnResign = new JButton("Desistir");
		btnResign.addActionListener((e) -> resign());
		btnResign.setEnabled(false);
		GridBagConstraints gbc_btnResign = new GridBagConstraints();
		gbc_btnResign.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnResign.gridx = 0;
		gbc_btnResign.gridy = 3;
		pnlGameControls.add(btnResign, gbc_btnResign);

		pnlRight.setSelectedIndex(0);

		pnlCenter = new JPanel();
		contentPane.add(pnlCenter, BorderLayout.CENTER);
		GridBagLayout gbl_pnlCenter = new GridBagLayout();
		gbl_pnlCenter.columnWidths = new int[] { 500, 0 };
		gbl_pnlCenter.rowHeights = new int[] { 497, 130, 0 };
		gbl_pnlCenter.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_pnlCenter.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		pnlCenter.setLayout(gbl_pnlCenter);

		board = new GameBoard(imgList, game);
		board.setPreferredSize(new Dimension(500, 500));
		board.setSize(new Dimension(500, 500));
		GridBagConstraints gbc_board = new GridBagConstraints();
		gbc_board.fill = GridBagConstraints.BOTH;
		gbc_board.gridx = 0;
		gbc_board.gridy = 0;
		pnlCenter.add(board, gbc_board);

		pnlChat = new ChatPanel();
		pnlChat.addListener((message) -> sendMessageToChat(message));
		GridBagConstraints gbc_pnlChat = new GridBagConstraints();
		gbc_pnlChat.fill = GridBagConstraints.BOTH;
		gbc_pnlChat.gridx = 0;
		gbc_pnlChat.gridy = 1;
		pnlCenter.add(pnlChat, gbc_pnlChat);

		roomUserList = new UserListPanel();

		tableUserList = new UserListPanel();
		pnlRight.addTab("Usuários", null, tableUserList, null);

		pnlRefreshTableParameters = new JPanel();
		pnlRefreshTableParameters.setLayout(new BorderLayout());

		pnlTableParams = new TableParamsPanel();
		pnlRefreshTableParameters.add(pnlTableParams, BorderLayout.CENTER);

		btnRefreshTableParameters = new JButton();
		btnRefreshTableParameters.setEnabled(false);
		btnRefreshTableParameters.setText("Alterar parâmetros da mesa");
		btnRefreshTableParameters.addActionListener((e) -> changeTableParameters());
		pnlRefreshTableParameters.add(btnRefreshTableParameters, BorderLayout.SOUTH);

		pnlRight.addTab("Parâmetros", null, pnlRefreshTableParameters, null);

		connection = new DraughtsConnection(host, port, username, sid);
		connection.addListener(new DraughtsConnectionHandler());
	}

	private void changeTableParameters() {
		DraughtsTableParams params = fetchTableParams();
		openTable.changeTableParameters(params);
	}

	public void connectCreating(int room, TableParams params) {
		queue.post(() -> connection.createTable(room, params));
	}

	public void connectSitting(int room, int id, int sitIndex) {
		queue.post(() -> connection.joinTable(room, id, sitIndex));
	}

	public void connectWatching(int room, int id) {
		queue.post(() -> connection.joinTable(room, id));
	}

	private DraughtsTableParams fetchTableParams() {
		DraughtsTableParams params = new DraughtsTableParams();
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
		return params;
	}

	public String getHost() {
		return host;
	}

	public int getNumber() {
		return openTable != null ? openTable.getNumber() : -1;
	}

	public OpenDraughtsTable getOpenTable() {
		return openTable;
	}

	public int getPort() {
		return port;
	}

	public int getRoomID() {
		return openTable != null ? openTable.getRoomID() : -1;
	}

	public String getRoomName() {
		return openTable != null ? openTable.getRoomName() : "";
	}

	public String getSID() {
		return sid;
	}

	public String getUsername() {
		return username;
	}

	private void handleAdminChat(String sender, String message) {
		pnlChat.appendAdminMessage(sender, message);
	}

	private void handleChat(String sender, String message) {
		pnlChat.appendMessage(sender, message);
	}

	private void handleGameState(int gameID, int gameType, boolean running, int gameState, int currentTurn, int timePerTurn, int[] times, DraughtsMove[] moves) {
		this.running = running;

		for (int i = turnNum; i < moves.length; i++) {
			DraughtsMove move = moves[i];
			for (int j = 0; j < move.count(); j++) {
				BoardMove m = move.getMove(j);
				BoardPosition src = m.get(0);
				src = translatePosition(src);
				BoardPosition dst = m.get(1);
				dst = translatePosition(dst);
				game.doMove(new BoardMove(src, dst), true, true);
			}
		}

		this.currentTurn = currentTurn;

		turnNum = moves.length;
		board.setLocked(!isMyTurn());
		btnOfferDraw.setEnabled(imPlaying());
		btnResign.setEnabled(imPlaying());
	}

	private void handlePlayersOnTheSameLocalNetwork(String[] names) {
		if (names.length < 2)
			return;

		String message = names[0];
		for (int i = 1; i < names.length - 1; i++)
			message += ", " + names[i];

		message += " e " + names[names.length - 1] + " estão na mesma rede local.";

		pnlChat.appendAlertMessage(message);
	}

	private void handlePrivateChat(String sender, String message) {
		pnlChat.appendPrivateMessage(sender, message);
	}

	private void handleRatingChanges(RatingChange[] ratingChanges) {
		for (RatingChange change : ratingChanges)
			pnlChat.appendSystemMessage("A pontuação do jogador " + change.getName() + " foi alterada de " + (change.getRating() - change.getGain()) + " para " + change.getRating() + " ("
					+ (change.getGain() >= 0 ? "+" : "") + change.getGain() + ")");
	}

	private void handleStartGame() {
		turnNum = 0;

		DraughtsTableParams params = table.getParams();
		switch (params.getGameType()) {
			case DraughtsTableParams.CLASSIC:
				game.setConfig(DraughtsConfig.BRAZILIAN);
				break;

			case DraughtsTableParams.INTERNATIONAL:
				game.setConfig(DraughtsConfig.INTERNATIONAL);
				break;

			case DraughtsTableParams.AMERICAN:
				game.setConfig(DraughtsConfig.AMERICAN);
				break;

			case DraughtsTableParams.AMERICAN10x10:
				game.setConfig(DraughtsConfig.AMERICAN10x10);
				break;
		}

		game.setUseTime(false);
		game.setUseIncrementTime(false);
		game.setUseTimePerTurn(false);

		Player white = game.join(DraughtsGame.WHITE, "White");
		Player black = game.join(DraughtsGame.BLACK, "Black");

		white.start();
		black.start();
		if (!game.start()) {
			System.err.println("Game not started!");
			return;
		}

		board.setLocked(!isMyTurn());
		btnRefreshTableParameters.setEnabled(imHost() && !running);
	}

	private void handleStopGame() {
		board.setLocked(true);
		game.stop();
	}

	private void handleTableUpdate(DraughtsTable table) {
		this.table = table;

		tableUserList.clear();

		setTitle("Sala " + openTable.getRoomName() + " - Mesa #" + table.getNumber());

		myTurn = -1;
		int maxSeats = table.getMaxSeatCount();
		for (int i = 0; i < maxSeats; i++) {
			Seat seat = table.getSeat(i);
			if (seat != null && seat.getUser().getID() == connection.getMyID()) {
				myTurn = i;
				break;
			}
		}

		boolean iCanSit = iCanSit();
		for (int i = 0; i < maxSeats; i++) {
			Seat seat = table.getSeat(i);
			if (seat != null) {
				DraughtsUser user = (DraughtsUser) seat.getUser();
				tableUserList.addEntry(user);
				lblPlayers[i].setText(user.getName());
				btnSeats[i].setVisible(false);
			} else {
				lblPlayers[i].setText("(Vazio)");
				btnSeats[i].setVisible(iCanSit);
			}
		}

		for (int i = 0; i < table.getWatcherCount(); i++) {
			User watcher = table.getWatcher(i);
			tableUserList.addEntry((DraughtsUser) watcher);
		}

		btnStartGame.setEnabled(imSiting());
		btnStandUp.setEnabled(imSiting());
		btnOfferDraw.setEnabled(game.isRunning());
		btnResign.setEnabled(game.isRunning());

		updateTableParameters(table.getParams());

		board.setRotated(imWhite());
		board.setLocked(!isMyTurn());
		btnRefreshTableParameters.setEnabled(imHost() && !running);
	}

	private void handleTransferHost(String newHostName, String oldHostName) {
		if (oldHostName == null)
			pnlChat.appendSystemMessage("Você recebeu o status de host do servidor.");
		else
			pnlChat.appendSystemMessage((newHostName == null ? "Você recebeu o status de host de " + oldHostName : oldHostName + " transferiu o status de host para " + newHostName) + ".");
	}

	private boolean iCanSit() {
		return !imSiting() && table.getSeatCount() < table.getMaxSeatCount();
	}

	private boolean imHost() {
		return table.getHostID() == connection.getMyID();
	}

	private boolean imPlaying() {
		return imSiting() && running;
	}

	private boolean imSiting() {
		return myTurn != -1;
	}

	private boolean imWhite() {
		return imSiting() && myTurn == 0;
	}

	private boolean isMyTurn() {
		return imSiting() && running && currentTurn == myTurn;
	}

	private void offerDraw() {
		openTable.offerDraw();
	}

	private void resign() {
		openTable.resign();
	}

	private void sendMessageToChat(String message) {
		openTable.sendMessageToChat(message);
	}

	private void sit(int sitIndex) {
		openTable.sit(sitIndex);
	}

	private void standUp() {
		openTable.standUp();
	}

	private void suggestStartGame() {
		openTable.suggestStartGame();
	}

	private BoardMove translateMove(BoardMove move) {
		BoardPosition[] positions = new BoardPosition[move.count()];
		for (int i = 0; i < positions.length; i++)
			positions[i] = translatePosition(move.get(i));

		return new BoardMove(positions);
	}

	private BoardPosition translatePosition(BoardPosition pos) {
		return new BoardPosition(pos.getRow(), game.getColCount() - pos.getCol() - 1);
	}

	private void updateTableParameters(DraughtsTableParams params) {
		pnlTableParams.setVariant(params.getGameType());
		pnlTableParams.setHasIncrementTime(params.hasIncrementTime());
		pnlTableParams.setHasTime(params.hasTime());
		pnlTableParams.setHasTimePerMove(params.hasTimePerTurn());
		pnlTableParams.setIncrementTime(params.getIncrementTime());
		pnlTableParams.setAllowWatchers(!params.isNoWatches());
		pnlTableParams.setPrivacy(params.getPrivacy());
		pnlTableParams.setRated(params.isRated());
		pnlTableParams.setSwapSides(params.isSwapSides());
		pnlTableParams.setTime(params.getTime());
		pnlTableParams.setTimePerMove(params.getTimePerTurn());
	}
}
