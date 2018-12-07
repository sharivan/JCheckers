package jcheckers.ui.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
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
import jcheckers.common.logic.boards.BoardPiece;
import jcheckers.common.logic.boards.BoardPosition;
import jcheckers.common.logic.boards.draughts.DraughtsConfig;
import jcheckers.common.logic.boards.draughts.DraughtsGame;
import jcheckers.common.logic.boards.draughts.DraughtsGameListener;
import jcheckers.common.logic.boards.draughts.DraughtsMan;
import jcheckers.common.logic.boards.draughts.DraughtsPiece;
import jcheckers.ui.ResourceImageList;
import jcheckers.ui.board.GameBoard;
import jcheckers.ui.chat.ChatPanel;
import jcheckers.ui.options.TableParamsPanel;
import jcheckers.ui.user.UserListPanel;

/**
 * 
 * Frame Swing que representa uma mesa aberta.
 * 
 * Uma mesa aberta est� associada a uma nova conex�o com o servidor onde o usu�rio pode interagir com os usu�rios presentes nela.
 * @author miste
 *
 */
public class TableFrame extends JFrame {

	private class DraughtsConnectionHandler implements DraughtsTableConnectionListener {

		@Override
		public void onAdminChat(Connection c, String sender, String message) {
			EventQueue.invokeLater(() -> handleAdminChat(sender, message));
		}

		@Override
		public void onAlreadyConnected(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� j� est� conectado a esta mesa."));
		}

		@Override
		public void onAvatars(Connection c, int[] avatars) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onBanned(Connection c, String bannedUntil) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� foi banido desta sala at� " + bannedUntil));
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
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Conex�o recusada."));
		}

		@Override
		public void onContinueGame(Connection c) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCouldNotReconnectToTheTable(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "N�o foi poss�vel reconectar � mesa, tente novamente depois."));
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
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Sala inv�lida."));
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
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage((kickedName == null ? "Voc�" : kickedName) + " foi expulso por " + kickerName));
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
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage("Os par�metros da mesa foram alterados por " + name));
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
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Usu�rio " + name + " n�o foi encontrado no servidor."));
		}

		@Override
		public void onPlayerOfferedDraw(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " ofereceu empate."));
		}

		@Override
		public void onPlayerReconnectingFailed(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " n�o conseguiu se reconectar � mesa."));
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
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " rejeitou desfazer a �ltima jogada."));
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
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " sugeriu desfazer a �ltima jogada."));
		}

		@Override
		public void onPlayerTryingToReconnect(Connection c, String name, int id) {
			EventQueue.invokeLater(() -> pnlChat.appendSystemMessage(name + " foi desconectado da mesa e est� tentando reconectar."));
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
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Sua pontua��o � muito alta."));
		}

		@Override
		public void onResponseInfo(Connection c, UserStats stats, int[] tables, int inactiveTime) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServerShuttingDown(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Servidor est� sendo desligado."));
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
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Mesa n�o existe."));
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
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Espectadores n�o permitidos."));
		}

		@Override
		public void onWelcome(Connection c, String roomName) {

		}

		@Override
		public void onYouCantBootAPlayerNotInServer(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode expulsar um usu�rio que n�o est� no servidor."));
		}

		@Override
		public void onYouCantChangeParametersNow(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode alterar os par�metros da mesa agora."));
		}

		@Override
		public void onYouCantInviteAPlayerAlreadyInTheTable(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode convidar um usu�rio que j� est� na mesa."));
		}

		@Override
		public void onYouCantInviteAPlayerNotInServer(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode convidar um usu�rio que n�o est� no servidor."));
		}

		@Override
		public void onYouCantInviteYourself(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode convidar a voc� mesmo."));
		}

		@Override
		public void onYouCantTransferHostToAPlayerNotInServer(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode transferir o host para um usu�rio que n�o est� no servidor."));
		}

		@Override
		public void onYouCantTransferHostToYourself(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode transferir o host para voc� mesmo."));
		}

		@Override
		public void onYoureNotTheHostToChangeParameters(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode alterar os par�metros da mesa, voc� n�o � o host."));
		}

		@Override
		public void onYoureNotTheHostToInvitePlayers(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode convidar pessoas para a mesa, voc� n�o � o host."));
		}

		@Override
		public void onYoureNotTheHostToTransferHost(Connection c) {
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(new JFrame(), "Voc� n�o pode transferir o host, voc� n�o � o host."));
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2466747814427990717L;
	
	private static final DraughtsConfig[] configs = {DraughtsConfig.BRAZILIAN, DraughtsConfig.INTERNATIONAL, DraughtsConfig.AMERICAN, DraughtsConfig.AMERICAN10x10};

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

	private ResourceImageList imgList;

	private DraughtsConnection connection;
	private OpenDraughtsTable openTable;
	private DraughtsTable table;
	private DraughtsGame game;

	/**
	 * Fila de processos utilizada para processamento em segundo plano.
	 */
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

	private JPanel pnlPlayers;

	private SeatPanel[] players;
	private SeatPanel pnlPlayer1;
	private SeatPanel pnlPlayer2;
	
	private JPanel pnlGameControls;

	private JButton btnStartGame;

	private JButton btnStandUp;

	private JButton btnOfferDraw;

	private JButton btnResign;

	/**
	 * Create the frame.
	 */
	public TableFrame(String host, int port, String username, String sid) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.sid = sid;

		imgList = new ResourceImageList("images");
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

			@Override
			public void onSetBoard(int row, int col, BoardPiece piece) {

			}

			@Override
			public void onChangeConfig(DraughtsConfig config) {

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

		players = new SeatPanel[2];

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
		GridBagConstraints gbc_pnlPlayers = new GridBagConstraints();
		gbc_pnlPlayers.insets = new Insets(0, 0, 5, 0);
		gbc_pnlPlayers.gridx = 0;
		gbc_pnlPlayers.gridy = 0;
		gbc_pnlPlayers.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlPlayers.anchor = GridBagConstraints.NORTH;
		pnlControls.add(pnlPlayers, gbc_pnlPlayers);
		pnlPlayers.setLayout(new GridLayout(2, 0, 0, 0));

		pnlPlayer1 = new SeatPanel(imgList, 0);
		pnlPlayer1.addListener((sitIndex) -> sit(sitIndex));
		pnlPlayers.add(pnlPlayer1);
		players[0] = pnlPlayer1;

		pnlPlayer2 = new SeatPanel(imgList, 1);
		pnlPlayer2.addListener((sitIndex) -> sit(sitIndex));
		pnlPlayers.add(pnlPlayer2);
		players[1] = pnlPlayer2;

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
		pnlRight.addTab("Usu�rios", null, tableUserList, null);

		pnlRefreshTableParameters = new JPanel();
		pnlRefreshTableParameters.setLayout(new BorderLayout());

		pnlTableParams = new TableParamsPanel();
		pnlRefreshTableParameters.add(pnlTableParams, BorderLayout.CENTER);

		btnRefreshTableParameters = new JButton();
		btnRefreshTableParameters.setEnabled(false);
		btnRefreshTableParameters.setText("Alterar par�metros da mesa");
		btnRefreshTableParameters.addActionListener((e) -> changeTableParameters());
		pnlRefreshTableParameters.add(btnRefreshTableParameters, BorderLayout.SOUTH);

		pnlRight.addTab("Par�metros", null, pnlRefreshTableParameters, null);

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
		
		game.setConfig(configs[gameType]);

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
		
		if (table.getParams().hasTime())
			for (int i = 0; i < times.length; i++) {
				players[i].setHasPlayerTime(true);
				players[i].setPlayerTime(times[i] * 1000);
			}
		else {
			players[0].setHasPlayerTime(false);
			players[1].setHasPlayerTime(false);
		}

		this.currentTurn = currentTurn;
		if (currentTurn != 4) {
			players[currentTurn].setHasTimePerMove(table.getParams().hasTimePerTurn());
			players[currentTurn].setTimePerMove(timePerTurn * 1000);
			players[currentTurn].setActive(true);
			players[1 - currentTurn].setActive(false);
		} else {
			players[0].setActive(false);
			players[1].setActive(false);
		}

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

		message += " e " + names[names.length - 1] + " est�o na mesma rede local.";

		pnlChat.appendAlertMessage(message);
	}

	private void handlePrivateChat(String sender, String message) {
		pnlChat.appendPrivateMessage(sender, message);
	}

	private void handleRatingChanges(RatingChange[] ratingChanges) {
		for (RatingChange change : ratingChanges)
			pnlChat.appendSystemMessage("A pontua��o do jogador " + change.getName() + " foi alterada de " + (change.getRating() - change.getGain()) + " para " + change.getRating() + " ("
					+ (change.getGain() >= 0 ? "+" : "") + change.getGain() + ")");
	}

	private void handleStartGame() {
		turnNum = 0;

		DraughtsTableParams params = table.getParams();
		game.setConfig(configs[params.getGameType()]);
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
				players[i].setPlayer(user.getName());
				players[i].setCanSeat(false);
			} else {
				players[i].setPlayer(null);
				players[i].setCanSeat(iCanSit);
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
			pnlChat.appendSystemMessage("Voc� recebeu o status de host do servidor.");
		else
			pnlChat.appendSystemMessage((newHostName == null ? "Voc� recebeu o status de host de " + oldHostName : oldHostName + " transferiu o status de host para " + newHostName) + ".");
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
		game.setConfig(configs[params.getGameType()]);
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
