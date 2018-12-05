package jcheckers.client.net;

public interface TableConnectionListener extends ConnectionListener {

	void onAlreadyConnected(Connection c);

	void onAvatars(Connection c, int[] avatars);

	void onConnectionRefused(Connection c);

	void onContinueGame(Connection c);

	void onCouldNotReconnectToTheTable(Connection c);

	void onInviationAutoRejected(Connection c, String name, int id);

	void onInviationRejected(Connection c, String name, int id);

	void onInviationSent(Connection c, String name, int id);

	void onInviationsInactive(Connection c);

	void onJoinTable(Connection c, OpenTable table);

	void onKickedByHost(Connection c, String kickedName, int kickedID, String kickerName, int kickerID);

	void onNoEnoughPlayersToStartTheGame(Connection c);

	void onParametersChangedBy(Connection c, int id, String name);

	void onPlayerReconnectingFailed(Connection c, String name, int id);

	void onPlayersInTheSameLocalNetwork(Connection c, String[] names);

	void onPlayerStandUp(Connection c, String name, int id);

	void onPlayerTryingToReconnect(Connection c, String name, int id);

	void onPrivateTable(Connection c);

	void onQuestionBits(Connection c, int acceptedBits, int rejectedBits);

	void onQuestionCanceled(Connection c);

	void onStartGame(Connection c);

	void onStartGameSuggested(Connection c, String name, int id, boolean dontAsk);

	void onStartGameSuggestRejected(Connection c, String name, int id);

	void onStopGame(Connection c);

	void onStopGameSuggested(Connection c, String name, int id, boolean dontAsk);

	void onStopGameSuggestRejected(Connection c, String name, int id);

	void onTableClosedBy(Connection c, int id);

	void onTableFocus(Connection c, int flags);

	void onTableNotExist(Connection c);

	void onTransferHost(Connection c, String newHostName, int newHostID, String oldHostName, int oldHostID);

	void onUpdate(Connection c, Table table);

	void onWatchersNotAllowed(Connection c);

	void onYouCantBootAPlayerNotInServer(Connection c);

	void onYouCantChangeParametersNow(Connection c);

	void onYouCantInviteAPlayerAlreadyInTheTable(Connection c);

	void onYouCantInviteAPlayerNotInServer(Connection c);

	void onYouCantInviteYourself(Connection c);

	void onYouCantTransferHostToAPlayerNotInServer(Connection c);

	void onYouCantTransferHostToYourself(Connection c);

	void onYoureNotTheHostToChangeParameters(Connection c);

	void onYoureNotTheHostToInvitePlayers(Connection c);

	void onYoureNotTheHostToTransferHost(Connection c);

}
