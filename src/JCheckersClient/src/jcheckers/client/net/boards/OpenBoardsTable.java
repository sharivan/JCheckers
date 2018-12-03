package jcheckers.client.net.boards;

import jcheckers.client.io.OutputProtocol;
import jcheckers.client.io.boards.BoardsOutputProtocol;
import jcheckers.client.net.Connection;
import jcheckers.client.net.OpenTable;
import jcheckers.client.net.User;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.io.JCheckersIOException;

public abstract class OpenBoardsTable extends OpenTable {

	protected OpenBoardsTable(Connection connection) {
		super(connection);
	}

	public void acceptDraw() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(BoardsOutputProtocol.ACCEPT_DRAW));
		output.flush();
	}

	public boolean isNoWatches() {
		return ((BoardsTable) table).getParams().isNoWatches();
	}

	public void offerDraw() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(BoardsOutputProtocol.OFFER_DRAW));
		output.flush();
	}

	@Override
	protected void readUser(User user, JCheckersDataInputStream input) throws JCheckersIOException {
		super.readUser(user, input);

		((BoardsUser) user).rating = input.readInt();
	}

	public void rejectDraw() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(BoardsOutputProtocol.REJECT_DRAW));
		output.flush();
	}

	public void resign() {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(BoardsOutputProtocol.RESIGN));
		output.writeInt(connection.getMyID());
		output.flush();
	}

}
