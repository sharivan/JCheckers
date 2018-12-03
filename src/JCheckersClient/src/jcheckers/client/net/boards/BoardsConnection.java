package jcheckers.client.net.boards;

import jcheckers.client.io.InputProtocol;
import jcheckers.client.io.OutputProtocol;
import jcheckers.client.io.boards.BoardsInputProtocol;
import jcheckers.client.io.boards.BoardsOutputProtocol;
import jcheckers.client.net.Connection;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;

public abstract class BoardsConnection extends Connection {

	public BoardsConnection(String host, int port, String username, String sid) {
		super(host, port, username, sid);
	}

	@Override
	protected InputProtocol createInputProtocol(JCheckersDataInputStream input) {
		return new BoardsInputProtocol(input);
	}

	@Override
	protected OutputProtocol createOutputProtocol(JCheckersDataOutputStream output) {
		return new BoardsOutputProtocol(output);
	}

	@Override
	public int getGameID() {
		return 0xcc;
	}

}
