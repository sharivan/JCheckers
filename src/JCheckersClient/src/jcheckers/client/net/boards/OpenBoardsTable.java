package jcheckers.client.net.boards;

import java.io.IOException;

import jcheckers.client.io.OutputProtocol;
import jcheckers.client.io.boards.BoardsInputProtocol;
import jcheckers.client.io.boards.BoardsOutputProtocol;
import jcheckers.client.net.Connection;
import jcheckers.client.net.ConnectionListener;
import jcheckers.client.net.OpenTable;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;

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
	protected void parseData(int opcode, JCheckersDataInputStream in) throws IOException {
		switch (opcode) {
			case BoardsInputProtocol.GAME_PAUSED: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onGamePaused(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.HIDE_CONTROLS: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onHideControls(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.PLAYER_ACCEPTED_DRAW: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onPlayerAcceptedDraw(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.PLAYER_ACCEPTED_UNDO_MOVE: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onPlayerAcceptedUndoMove(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.PLAYER_OFFERED_DRAW: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onPlayerOfferedDraw(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.PLAYER_REJECTED_DRAW: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onPlayerRejectedDraw(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.PLAYER_REJECTED_PAUSE_GAME: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onPlayerRejectedPauseGame(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.PLAYER_REJECTED_UNDO_MOVE: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onPlayerRejectedUndoMove(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.PLAYER_SUGGESTED_PAUSE_GAME: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onPlayerSuggestedPauseGame(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.PLAYER_SUGGESTED_UNDO_MOVE: {
				String name = in.readString();
				int id = in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onPlayerSuggestedUndoMove(connection, name, id);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.RATING_CHANGE: {
				int count = in.readUChar();
				RatingChange[] ratingChanges = new RatingChange[count];
				for (int i = 0; i < count; i++) {
					int sitIndex = in.readUChar();
					String name = in.readString();
					int gain = in.readInt();
					int rating = in.readInt();
					ratingChanges[i] = new RatingChange(sitIndex, name, gain, rating);
				}

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onRatingChanges(connection, ratingChanges);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			case BoardsInputProtocol.RATING_TOO_HIGH: {
				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof BoardsTableConnectionListener)
						try {
							((BoardsTableConnectionListener) listener).onRatingTooHigh(connection);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			default:
				super.parseData(opcode, in);
		}
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
