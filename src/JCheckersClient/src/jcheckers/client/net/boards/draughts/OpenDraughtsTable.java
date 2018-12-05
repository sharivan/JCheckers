package jcheckers.client.net.boards.draughts;

import java.io.IOException;

import jcheckers.client.io.OutputProtocol;
import jcheckers.client.io.boards.BoardsInputProtocol;
import jcheckers.client.io.boards.BoardsOutputProtocol;
import jcheckers.client.net.Connection;
import jcheckers.client.net.ConnectionListener;
import jcheckers.client.net.boards.OpenBoardsTable;
import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.common.logic.boards.BoardMove;
import jcheckers.common.logic.boards.BoardPosition;

public final class OpenDraughtsTable extends OpenBoardsTable {

	public class DraughtsMove {

		private BoardMove[] moves;
		private int turn;

		private DraughtsMove() {

		}

		public int count() {
			return moves.length;
		}

		public BoardMove getMove(int index) {
			return moves[index];
		}

		public int getTurn() {
			return turn;
		}

	}

	protected OpenDraughtsTable(Connection connection) {
		super(connection);
	}

	public void doMove(int turnNum, int turn, BoardMove move) {
		JCheckersDataOutputStream output = connection.prepareOutput();
		OutputProtocol protocol = createOutputProtocol(output);
		output.writeUChar(protocol.getOpcode(BoardsOutputProtocol.CHECKERS_AND_CHESS_MOVE));
		output.writeInt(turnNum);
		output.writeShort(turn);
		output.writeShort(0);

		int positions = move.count();
		output.writeChar(3 * positions - 4);
		if (positions > 1) {
			BoardPosition src = move.source();
			for (int posIndex = 1; posIndex < positions; posIndex++) {
				if (posIndex > 1) {
					output.writeInt(-1);
					output.writeInt(-1);
				}

				BoardPosition dst = move.get(posIndex);
				output.writeInt(src.getCol());
				output.writeInt(src.getRow());
				output.writeInt(dst.getCol());
				output.writeInt(dst.getRow());
				src = dst;
			}
		}

		output.flush();
	}

	@Override
	protected void parseData(int opcode, JCheckersDataInputStream in) throws IOException {
		switch (opcode) {
			case BoardsInputProtocol.DRAUGHTS_AND_CHESS_GAME_STATE: {
				int gameID = in.readUChar();
				int gameType = in.readUChar();
				boolean running = in.readBoolean();
				int gameState = in.readUChar();
				int currentTurn = in.readUChar();
				in.readUChar();
				in.readUChar();
				in.readUChar();
				in.readInt();
				int timePerTurn = in.readInt();
				in.readBoolean();
				in.readInt();
				in.readBoolean();
				in.readInt();
				in.readInt();
				in.readUChar();
				in.readInt();

				int playerCount = in.readUChar();
				int[] times = new int[playerCount];
				;
				if (gameState != 0)
					for (int i = 0; i < playerCount; i++)
						times[i] = in.readInt();

				in.readUChar();
				in.readInt();

				int count = in.readInt();
				for (int i = 0; i < count; i++)
					in.readShort();

				in.readChar();

				int moveCount = in.readInt();
				DraughtsMove[] moves = new DraughtsMove[moveCount];
				for (int i = 0; i < moveCount; i++) {
					DraughtsMove move = new DraughtsMove();
					moves[i] = move;

					move.turn = in.readUChar();
					in.readUChar();
					in.readShort();
					in.readInt();
					int positions = in.readUChar();
					positions = (positions + 4) / 3;
					move.moves = new BoardMove[positions - 1];
					if (positions > 1)
						for (int posIndex = 1; posIndex < positions; posIndex++) {
							if (posIndex > 1) {
								in.readInt();
								in.readInt();
							}

							int srcCol = in.readInt();
							int srcRol = in.readInt();
							int dstCol = in.readInt();
							int dstRow = in.readInt();
							move.moves[posIndex - 1] = new BoardMove(srcRol, srcCol, dstRow, dstCol);
						}
				}

				in.readUChar();
				in.readUChar();
				in.readUChar();
				in.readInt();

				for (ConnectionListener listener : listeners)
					if (listener != null && listener instanceof DraughtsTableConnectionListener)
						try {
							((DraughtsTableConnectionListener) listener).onGameState((DraughtsConnection) connection, gameID, gameType, running, gameState, currentTurn, timePerTurn, times, moves);
						} catch (Throwable e) {
							e.printStackTrace();
						}

				break;
			}

			default:
				super.parseData(opcode, in);
		}
	}

}
