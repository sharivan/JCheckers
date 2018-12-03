package jcheckers.client.net;

import java.io.IOException;

import jcheckers.client.io.InputProtocol;
import jcheckers.client.io.OutputProtocol;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.io.JCheckersIOException;

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
