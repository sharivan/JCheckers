package jcheckers.client.net;

import java.io.IOException;

import jcheckers.client.io.InputProtocol;
import jcheckers.client.io.OutputProtocol;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.io.JCheckersIOException;

/**
 * 
 * Implementa as funções da conexão com uma mesa de jogos aberta.
 * @author miste
 *
 */
public abstract class OpenTable extends Base {

	@SuppressWarnings("unused")
	private static final boolean DEBUG = false;

	protected static final int NO_REASON = 0;
	protected static final int NO_ENOUGH_PLAYERS = 1;

	protected Table table;

	protected OpenTable(Connection connection) {
		super(connection);
	}

	public void acceptStartGame() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.START_GAME_SUGGEST_ACCEPTED));
		output.flush();
	}

	public void changeAvatar(int avatarIndex) {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.CHANGE_AVATAR));
		output.writeInt(avatarIndex);
		output.flush();
	}

	public void changeTableParameters(TableParams params) {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.CHANGE_TABLE_PARAMETERS));

		try {
			params.writeTo(output);
		} catch (JCheckersIOException e) {
			e.printStackTrace();
		}

		output.flush();
	}

	public int getFocusedBits() {
		// TODO Implementar
		return 0;
	}

	public final int getGameID() {
		return table.getGameID();
	}

	public synchronized User getHost() {
		return table.getHost();
	}

	public int getID() {
		return table.getID();
	}

	public int getNumber() {
		return table.getNumber();
	}

	public int getReconnectBits() {
		return table.getReconnectingBits();
	}

	public int getSeatBits() {
		return table.getSeatBits();
	}

	public int getSeatCount() {
		return table.getSeatCount();
	}

	public int getTimePerTurn() {
		return table.getParams().getTimePerTurn();
	}

	public void gotFocus() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.GOT_FOCUS));
		output.flush();
	}

	public void kick(int id) {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.TRANSFER_HOST));
		output.writeInt(id);
		output.flush();
	}

	public void lostFocus() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.LOST_FOCUS));
		output.flush();
	}

	@Override
	protected void parseData(int opcode, JCheckersDataInputStream in) throws IOException {
		switch (opcode) {
			case InputProtocol.ALREADY_CONNECTED_ON_THE_TABLE: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onAlreadyConnected(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.TABLE_NOT_EXIST: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onTableNotExist(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.WATCHERS_NOT_ALLOWED: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onWatchersNotAllowed(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.PRIVATE_TABLE: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onPrivateTable(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.UPDATE_TABLE: {
				int id = in.readInt();

				Table table = connection.createTable(getRoomID(), id, in);
				this.table = table;

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onUpdate(connection, table);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.AVATARS: {
				int count = in.readUChar();
				int[] avatars = new int[count];
				for (int i = 0; i < count; i++)
					avatars[i] = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onAvatars(connection, avatars);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.CLOSE_TABLE: {
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onTableClosedBy(connection, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.CONFIG_CHANGED: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onParametersChangedBy(connection, id, name);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.CONNECT_TO_THE_TABLE_REFUSED: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onConnectionRefused(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.COULD_RECONNECT_TO_THE_TABLE_TRY_AGAIN_LATTER: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onCouldNotReconnectToTheTable(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.START_GAME_SUGGESTED: {
				String name = in.readString();
				int id = in.readInt();
				boolean dontAsk = in.readBoolean();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onStartGameSuggested(connection, name, id, dontAsk);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.START_GAME_SUGGEST_REJECTED: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onStartGameSuggestRejected(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.STOP_GAME_SUGGESTED: {
				String name = in.readString();
				int id = in.readInt();
				boolean dontAsk = in.readBoolean();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onStopGameSuggested(connection, name, id, dontAsk);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.STOP_GAME_SUGGEST_REJECTED: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onStopGameSuggestRejected(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.START_GAME: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onStartGame(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.CONTINUE_GAME: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onContinueGame(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.STOP_GAME: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onStopGame(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.TABLE_FOCUS: {
				int flags = in.readUChar();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onTableFocus(connection, flags);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.INVITATION_SENT: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onInviationSent(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.INVITATION_AUTO_REJECTED: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onInviationAutoRejected(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.INVITATION_REJECTED: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onInviationRejected(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.INVITATIONS_INACTIVE: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onInviationsInactive(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.NO_ENOUGH_PLAYERS_TO_START_THE_GAME: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onNoEnoughPlayersToStartTheGame(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.OTHER_RECEIVED_THE_HOST: {
				String newHostName = in.readString();
				int newHostID = in.readInt();
				String oldHostName = in.readString();
				int oldHostID = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onTransferHost(connection, newHostName, newHostID, oldHostName, oldHostID);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.OTHER_WAS_KICKED_BY_HOST: {
				String kickedName = in.readString();
				int kickedID = in.readInt();
				String kickerName = in.readString();
				int kickerID = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onKickedByHost(connection, kickedName, kickedID, kickerName, kickerID);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.PLAYER_RECONNECTING: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onPlayerTryingToReconnect(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.PLAYER_RECONNECTING_FAILED: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onPlayerReconnectingFailed(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.SAME_LOCAL_NETWORK: {
				int count = in.readInt();
				String[] names = new String[count];
				for (int i = 0; i < count; i++)
					names[i] = in.readString();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onPlayersInTheSameLocalNetwork(connection, names);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.PLAYER_STAND_UP: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onPlayerStandUp(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.QUESTION_BITS: {
				int acceptedBits = in.readUChar();
				int rejectedBits = in.readUChar();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onQuestionBits(connection, acceptedBits, rejectedBits);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.QUESTION_CANCELED: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onQuestionCanceled(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOURE_NOT_THE_HOST_TO_CHANGE_PARAMETERS: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYoureNotTheHostToChangeParameters(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOURE_NOT_THE_HOST_TO_INVITE_PLAYERS: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYoureNotTheHostToInvitePlayers(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOURE_NOT_THE_HOST_TO_TRANSFER_HOST: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYoureNotTheHostToTransferHost(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_CANT_CHANGE_CONFIG_NOW: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYouCantChangeParametersNow(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_CANT_INVITE_A_PLAYER_ALREADY_IN_THE_TABLE: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYouCantInviteAPlayerAlreadyInTheTable(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_CANT_INVITE_A_PLAYER_NOT_IN_SERVER: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYouCantInviteAPlayerNotInServer(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_CANT_INVITE_YOURSELF: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYouCantInviteYourself(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_CANT_BOOT_A_PLAYER_NOT_IN_SERVER: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYouCantBootAPlayerNotInServer(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_CANT_TRANSFER_HOST_TO_A_PLAYER_NOT_IN_SERVER: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYouCantTransferHostToAPlayerNotInServer(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_CANT_TRANSFER_HOST_TO_YOURSELF: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onYouCantTransferHostToYourself(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_RECEIVED_THE_HOST: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onTransferHost(connection, null, 0, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_RECEIVED_THE_HOST_FROM_THE_SERVER: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onTransferHost(connection, null, 0, null, 0);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case InputProtocol.YOU_WAS_KICKED_BY_HOST: {
				String kickerName = in.readString();
				int kickerID = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof TableConnectionListener)
						try {
							((TableConnectionListener) listener).onKickedByHost(connection, null, 0, kickerName, kickerID);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				connection.close(true);

				break;
			}

			default:
				super.parseData(opcode, in);
		}
	}

	public void rejectStartGame() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.START_GAME_SUGGEST_REJECTED));
		output.flush();
	}

	public void sit(int sitIndex) {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.SIT));
		output.writeInt(sitIndex);
		output.writeInt(connection.getMyID());
		output.flush();
	}

	public void standUp() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.STAND_UP));
		output.writeInt(connection.getMyID());
		output.flush();
	}

	public void suggestStartGame() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.SUGGEST_START_GAME));
		output.flush();
	}

	@Override
	public String toString() {
		return "table " + getNumber() + " in room " + getRoomName();
	}

	public void transferHost(int id) {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(OutputProtocol.BOOT));
		output.writeInt(id);
		output.flush();
	}

}
