package jcheckers.client.net.boards;

import jcheckers.client.net.Connection;
import jcheckers.client.net.TableConnectionListener;

public interface BoardsTableConnectionListener extends TableConnectionListener {

	void onGamePaused(Connection c);

	void onHideControls(Connection c);

	void onPlayerAcceptedDraw(Connection c, String name, int id);

	void onPlayerAcceptedUndoMove(Connection c, String name, int id);

	void onPlayerOfferedDraw(Connection c, String name, int id);

	void onPlayerRejectedDraw(Connection c, String name, int id);

	void onPlayerRejectedPauseGame(Connection c, String name, int id);

	void onPlayerRejectedUndoMove(Connection c, String name, int id);

	void onPlayerSuggestedPauseGame(Connection c, String name, int id);

	void onPlayerSuggestedUndoMove(Connection c, String name, int id);

	void onRatingChanges(Connection c, RatingChange[] ratingChanges);

	void onRatingTooHigh(Connection c);

}
