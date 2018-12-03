package jcheckers.client.net.boards.draughts;

import jcheckers.client.net.boards.BoardsTableConnectionListener;
import jcheckers.client.net.boards.draughts.OpenDraughtsTable.DraughtsMove;

public interface DraughtsTableConnectionListener extends BoardsTableConnectionListener {

	void onGameState(DraughtsConnection c, int gameID, int gameType, boolean running, int gameState, int currentTurn, int timePerTurn, int[] times, DraughtsMove[] moves);

}
