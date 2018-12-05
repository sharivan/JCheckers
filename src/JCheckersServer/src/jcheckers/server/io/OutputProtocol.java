package jcheckers.server.io;

import java.util.HashMap;
import java.util.List;

import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.server.net.Base;
import jcheckers.server.net.Room;
import jcheckers.server.net.Table;
import jcheckers.server.net.Table.Seat;
import jcheckers.server.net.User;

public class OutputProtocol {

	public static final int MY_ID = 0x08;
	public static final int JOIN_USER = 0x0a;
	public static final int LEAVE_USER = 0x0b;
	public static final int USER_LIST = 0x0c;
	public static final int ROOM_LIST = 0x0d;
	public static final int ROOM_CHANGED = 0x0e;
	public static final int INVALID_ROOM = 0x0f;
	public static final int RESPONSE_CHAT = 0x11;
	public static final int RESPONSE_PRIVATE_CHAT = 0x12;
	public static final int RESPONSE_ADMIN_CHAT = 0x13;
	public static final int CONNECT_TO_THE_TABLE_FAILED_DO_U_WANT_TO_TRY_AGAIN = 0x14;
	public static final int PING_CONNECTION = 0x15;
	public static final int PING = 0x17;
	public static final int PONG = 0x18;
	public static final int TABLE_FOCUS = 0x19;
	public static final int CHANGE_CRYPT_MODE = 0x1f;
	public static final int NO_CRYPT_NEXT_BLOCK = 0x22;
	public static final int NOTIFY_BANNED_AND_DISCONNECT = 0x24;
	public static final int NOTIFY_BANNED_ONLY = 0x25;
	public static final int RESPONSE_SERVER_VERSION = 0x2a;
	public static final int SERVER_DEBUG_MESSAGE = 0x2c;
	public static final int ACCESS_EXPIRED_ON = 0x2f;
	public static final int ACCOUNT_BLOCKED = 0x30;
	public static final int ACCESS_EXPIRED = 0x31;
	public static final int BONUS_EXPIRED = 0x32;
	public static final int GUESTS_NOT_ALLOWED_TO_PLAY = 0x34;
	public static final int GUESTS_NOT_ALLOWED_TO_PLAY_2 = 0x35;
	public static final int GUESTS_NOT_ALLOWED_TO_PLAY_3 = 0x36;
	public static final int BONUS_RECEIVED = 0x37;
	public static final int BONUS_EXPIRED_2 = 0x38;
	public static final int SERVER_SHUTTING_DOWN = 0x3a;
	public static final int INVALID_PASSWORD = 0x3b;
	public static final int COULD_NOT_CONNECT_TO_THE_ROOM = 0x3c;
	public static final int TABLE_NOT_EXIST = 0x3d;
	public static final int YOU_HAVE_MAXIMUM_TABLES_OPEN = 0x3e;
	public static final int CONNECT_TO_THE_TABLE_REFUSED = 0x3f;
	public static final int ALREADY_CONNECTED_ON_THE_TABLE = 0x40;
	public static final int PRIVATE_TABLE = 0x41;
	public static final int NOT_ENOUGH_RATING = 0x42;
	public static final int WATCHERS_NOT_ALLOWED = 0x43;
	public static final int NO_VACANCY_IN_THE_TABLE = 0x44;
	public static final int COULD_NOT_CONNECT_TO_THE_TABLE_TRY_AGAIN_LATTER = 0x45;
	public static final int COULD_NOT_CONNECT_TO_THE_TABLE = 0x46;
	public static final int COULD_NOT_CONNECT_TO_THE_TABLE_DUE_TOURNAMENT = 0x47;
	public static final int WELLCOME = 0x48;
	public static final int RESPONSE_INFO = 0x4a;
	public static final int PLAYER_NOT_FOUND_IN_SERVER = 0x4b;
	public static final int UPDATE_TABLE = 0x4c;
	public static final int AVATARS = 0x4e;
	public static final int CLOSE_TABLE = 0x4f;
	public static final int YOURE_NOT_THE_HOST_TO_CHANGE_PARAMETERS = 0x51;
	public static final int CONFIG_CHANGED = 0x52;
	public static final int YOU_ARE_NOT_THE_HOST_TO_BOOT_PLAYERS = 0x54;
	public static final int YOU_CANT_BOOT_YOURSELF = 0x55;
	public static final int YOU_CANT_BOOT_A_PLAYER_NOT_IN_SERVER = 0x56;
	public static final int YOU_WAS_KICKED_BY_HOST = 0x57;
	public static final int OTHER_WAS_KICKED_BY_HOST = 0x58;
	public static final int YOU_CANT_KICK_A_RECONNECTING_PLAYER = 0x59;
	public static final int YOURE_NOT_THE_HOST_TO_INVITE_PLAYERS = 0x5b;
	public static final int YOU_CANT_INVITE_YOURSELF = 0x5c;
	public static final int YOU_CANT_INVITE_A_PLAYER_NOT_IN_SERVER = 0x5d;
	public static final int INVITED = 0x5e;
	public static final int INVITATION_REJECTED = 0x62;
	public static final int INVITATION_AUTO_REJECTED = 0x64;
	public static final int YOU_CANT_INVITE_A_PLAYER_ALREADY_IN_THE_TABLE = 0x65;
	public static final int YOU_CANT_INVITE_A_WATCHER_DUE_NO_VACANCY = 0x66;
	public static final int INVALID_INVIATION = 0x67;
	public static final int INVITATION_SENT = 0x68;
	public static final int INVITATIONS_INACTIVE = 0x69;
	public static final int YOURE_NOT_THE_HOST_TO_TRANSFER_HOST = 0x6b;
	public static final int YOU_CANT_TRANSFER_HOST_TO_YOURSELF = 0x6c;
	public static final int YOU_CANT_TRANSFER_HOST_TO_A_PLAYER_NOT_IN_SERVER = 0x6d;
	public static final int YOU_RECEIVED_THE_HOST = 0x6e;
	public static final int OTHER_RECEIVED_THE_HOST = 0x6f;
	public static final int YOU_CANT_TRANSFER_THE_HOST_TO_A_RECONNECTING_PLAYER = 0x70;
	public static final int COULD_RECONNECT_TO_THE_TABLE_TRY_AGAIN_LATTER = 0x71;
	public static final int YOU_RECEIVED_THE_HOST_FROM_THE_SERVER = 0x72;
	public static final int PLAYER_CONNECT = 0x73;
	public static final int PLAYER_DISCONNECT = 0x74;
	public static final int PLAYER_RECONNECTING = 0x75;
	public static final int PLAYER_RECONNECTING_FAILED = 0x76;
	public static final int YOU_CANT_WATCH_THIS_GAME = 0x78;
	public static final int UPDATE_USERS = 0x80;
	public static final int YOU_WAS_KICKED_BY_ADMIN = 0x81;
	public static final int OTHER_WAS_KICKED_BY_ADMIN = 0x82;
	public static final int NO_ENOUGH_PLAYERS_TO_START_THE_GAME = 0x84;
	public static final int START_GAME_SUGGESTED = 0x86;
	public static final int START_GAME_SUGGEST_ACCEPTED = 0x88;
	public static final int START_GAME_SUGGEST_REJECTED = 0x89;
	public static final int STOP_GAME_SUGGESTED = 0x8c;
	public static final int STOP_GAME_SUGGEST_REJECTED = 0x8f;
	public static final int QUESTION_BITS = 0x92;
	public static final int QUESTION_CANCELED = 0x93;
	public static final int START_GAME = 0x94;
	public static final int STOP_GAME = 0x95;
	public static final int PLAYER_STAND_UP = 0x9c;
	public static final int CONTINUE_GAME = 0xa2;
	public static final int SAME_LOCAL_NETWORK = 0xa6;
	public static final int YOU_CANT_CHANGE_CONFIG_NOW = 0xa8;

	protected JCheckersDataOutputStream output;
	protected int[] opcodes;

	public OutputProtocol(JCheckersDataOutputStream output) {
		this.output = output;
	}
	
	public void check() throws DuplicateOpcodeException {
		HashMap<Integer, Integer> map = new HashMap<>();
		for (int i = 0; i < opcodes.length; i++) {
			Integer opcode = map.get(opcodes[i]);
			if (opcode != null)
				throw new DuplicateOpcodeException(opcode);
			
			map.put(opcode, opcode);
		}
	}

	public void adminChat(String name, String message) {
		output.writeUChar(opcodes[RESPONSE_ADMIN_CHAT]);
		output.writeEasyString(name);
		output.writeEasyString(message);
		output.flush();
	}

	public void alreadyConnectedOnTheTable() {
		output.writeUChar(opcodes[ALREADY_CONNECTED_ON_THE_TABLE]);
		output.flush();
	}

	public void avatars(Seat[] sits) {
		output.writeUChar(opcodes[AVATARS]);
		output.writeChar(sits.length);
		for (int i = 0; i < sits.length; i++)
			if (sits[i] == null)
				output.writeInt(0);
			else {
				User user = sits[i].getUser();
				output.writeInt(!user.isReconnecting() ? user.getAvatar() : 0);
			}
		output.flush();
	}

	public void chat(String name, String message) {
		output.writeUChar(opcodes[RESPONSE_CHAT]);
		output.writeEasyString(name);
		output.writeEasyString(message);
		output.flush();
	}

	public void closeTable(int id) {
		output.writeUChar(opcodes[CLOSE_TABLE]);
		output.writeInt(id);
		output.flush();
	}

	public void configChanged(String name, int id) {
		output.writeUChar(opcodes[CONFIG_CHANGED]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void connectToTheTableRefused() {
		output.writeUChar(opcodes[CONNECT_TO_THE_TABLE_REFUSED]);
		output.flush();
	}

	public void continueGame() {
		output.writeUChar(opcodes[CONTINUE_GAME]);
		output.flush();
	}

	public void couldNotConnectToTheRoom() {
		output.writeUChar(opcodes[COULD_NOT_CONNECT_TO_THE_ROOM]);
		output.flush();
	}

	public void couldReconnectToTheTableTryAgainLatter() {
		output.writeUChar(opcodes[COULD_RECONNECT_TO_THE_TABLE_TRY_AGAIN_LATTER]);
		output.flush();

	}

	public int getOpcode(int id) {
		return opcodes[id];
	}

	public void invalidPassword() {
		output.writeUChar(opcodes[INVALID_PASSWORD]);
		output.flush();
	}

	public void invalidRoom() {
		output.writeUChar(opcodes[INVALID_ROOM]);
		output.flush();
	}

	public void inviationAutoRejected(String name, int id) {
		output.writeUChar(opcodes[INVITATION_AUTO_REJECTED]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void inviationRejected(String name, int id) {
		output.writeUChar(opcodes[INVITATION_REJECTED]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void inviationSent(String name, int id) {
		output.writeUChar(opcodes[INVITATION_SENT]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void inviationsInactive() {
		output.writeUChar(opcodes[INVITATIONS_INACTIVE]);
		output.flush();
	}

	public void invited(String invitedName, int invitedID, int tableID, int i0, User inviter, int tableNumber) {
		output.writeUChar(opcodes[INVITED]);
		output.writeString(invitedName);
		output.writeInt(invitedID);
		output.writeInt(tableID);
		output.writeChar(i0);
		inviter.writeProfileData(output);
		output.writeInt(tableNumber);
		output.flush();
	}

	public void joinUser(Base base, User user) {
		output.writeUChar(opcodes[JOIN_USER]);
		base.writeUser(user, output);
		user.writeExtraInfo(output);
		output.flush();
	}

	public void leaveUser(int id) {
		output.writeUChar(opcodes[LEAVE_USER]);
		output.writeInt(id);
		output.flush();
	}

	public void myID(int id, String room) {
		output.writeUChar(opcodes[MY_ID]);
		output.writeInt(id);
		output.writeString(room);
		output.flush();
	}

	public void noEnoughPlayersToStartTheGame() {
		output.writeUChar(opcodes[NO_ENOUGH_PLAYERS_TO_START_THE_GAME]);
		output.flush();
	}

	public void notifyBannedAndDisconnect(String bannedUntil) {
		output.writeUChar(opcodes[NOTIFY_BANNED_AND_DISCONNECT]);
		output.writeString(bannedUntil);
		output.flush();
	}

	public void otherReceivedTheHost(String newHostName, int newHostID, String oldHostName, int oldHostID) {
		output.writeUChar(opcodes[OTHER_RECEIVED_THE_HOST]);
		output.writeString(newHostName);
		output.writeInt(newHostID);
		output.writeString(oldHostName);
		output.writeInt(oldHostID);
		output.flush();
	}

	public void otherWasKickedByTheHost(String kickedName, int kickedID, String kickerName, int kickerID) {
		output.writeUChar(opcodes[OTHER_WAS_KICKED_BY_HOST]);
		output.writeString(kickedName);
		output.writeInt(kickedID);
		output.writeString(kickerName);
		output.writeInt(kickerID);
		output.flush();
	}

	public void ping(int src, int dst) {
		output.writeUChar(opcodes[PING]);
		output.writeInt(src);
		output.writeInt(dst);
		output.flush();
	}

	public void pingConnection() {
		output.writeUChar(opcodes[PING_CONNECTION]);
		output.flush();
	}

	public void playerConnect(String name, int id) {
		output.writeUChar(opcodes[PLAYER_CONNECT]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void playerDisconnect(String name, int id) {
		output.writeUChar(opcodes[PLAYER_DISCONNECT]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void playerNotFound(String name) {
		output.writeUChar(opcodes[PLAYER_NOT_FOUND_IN_SERVER]);
		output.writeString(name);
		output.flush();
	}

	public void playerReconnecting(String name, int id) {
		output.writeUChar(opcodes[PLAYER_RECONNECTING]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void playerReconnectingFailed(String name, int id) {
		output.writeUChar(opcodes[PLAYER_RECONNECTING_FAILED]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void playersInTheSameLocalNetwork(List<String> players) {
		output.writeUChar(opcodes[SAME_LOCAL_NETWORK]);
		output.writeInt(players.size());
		for (String player : players)
			output.writeString(player);
		output.flush();
	}

	public void playerStandUp(String name, int id) {
		output.writeUChar(opcodes[PLAYER_STAND_UP]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void pong(int src, int dst) {
		output.writeUChar(opcodes[PONG]);
		output.writeInt(src);
		output.writeInt(dst);
		output.flush();
	}

	public void privateChat(String name, String message) {
		output.writeUChar(opcodes[RESPONSE_PRIVATE_CHAT]);
		output.writeEasyString(name);
		output.writeEasyString(message);
		output.flush();
	}

	public void privateTable() {
		output.writeUChar(opcodes[PRIVATE_TABLE]);
		output.flush();
	}

	public void questionBits(int acceptedBits, int rejectedBits) {
		output.writeUChar(opcodes[QUESTION_BITS]);
		output.writeChar(acceptedBits);
		output.writeChar(rejectedBits);
		output.flush();
	}

	public void questionCanceled() {
		output.writeUChar(opcodes[QUESTION_CANCELED]);
		output.flush();
	}

	public void responseInfo(User user, int i0) {
		output.writeUChar(opcodes[RESPONSE_INFO]);
		output.writeString(user.getName());
		output.writeInt(user.getID());
		output.writeInt(i0);
		user.writeFullProfileData(output);
		List<Table> tables = user.tables();
		output.writeChar(0);
		output.writeChar(tables.size());
		for (Table table : tables)
			output.writeChar(table.getNumber());
		output.writeInt((int) (user.getInactiveTime() / 1000));
		output.writeInt(i0);
		output.flush();
	}

	public void responseServerVersion(String version, String lastRelease) {
		output.writeUChar(opcodes[RESPONSE_SERVER_VERSION]);
		output.writeString(version);
		output.writeString(lastRelease);
		output.flush();
	}

	public void roomChanged(int index) {
		output.writeUChar(opcodes[ROOM_CHANGED]);
		output.writeInt(index);
		output.flush();
	}

	public void roomList(List<Room> rooms, Base base, boolean onlyUpdateds) {
		output.writeUChar(opcodes[ROOM_LIST]);
		output.writeShort(rooms.size());
		for (Room room : rooms) {
			if (onlyUpdateds) {
				output.writeChar(3);
				output.writeInt(room.getIndex());
				output.writeShort(0);
			} else {
				output.writeChar(1);
				output.writeInt(room.getIndex());
				output.writeString(room.getName());
			}
			output.writeShort(room.getID());
			output.writeShort(room.userCount());
			output.writeShort(room.tableCount());
			int flags = 0;
			if (room.isWithAdmin())
				flags |= Room.WITH_ADMIN;
			if (room.equals(base))
				flags |= Room.CURRENT_ROOM;
			output.writeChar(flags);
		}
		output.flush();
	}

	public void serverShuttingDown() {
		output.writeUChar(opcodes[SERVER_SHUTTING_DOWN]);
		output.flush();
	}

	public void startGame() {
		output.writeUChar(opcodes[START_GAME]);
		output.flush();
	}

	public void startGameSuggested(String name, int id, boolean dontAsk) {
		output.writeUChar(opcodes[START_GAME_SUGGESTED]);
		output.writeString(name);
		output.writeInt(id);
		output.writeBoolean(dontAsk);
		output.flush();
	}

	public void startGameSuggestRejected(String name, int id) {
		output.writeUChar(opcodes[START_GAME_SUGGEST_REJECTED]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void stopGame() {
		output.writeUChar(opcodes[STOP_GAME]);
		output.flush();
	}

	public void stopGameSuggested(String name, int id) {
		output.writeUChar(opcodes[STOP_GAME_SUGGESTED]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void stopGameSuggestRejected(String name, int id) {
		output.writeUChar(opcodes[STOP_GAME_SUGGEST_REJECTED]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void tableFocus(int flags) {
		output.writeUChar(opcodes[TABLE_FOCUS]);
		output.writeChar(flags);
		output.flush();
	}

	public void tableNotExist() {
		output.writeUChar(opcodes[TABLE_NOT_EXIST]);
		output.flush();
	}

	public void updateTable(Table table) {
		output.writeUChar(opcodes[UPDATE_TABLE]);
		table.write(output);
		output.flush();
	}

	public void updateUsers(List<User> updateds) {
		output.writeUChar(opcodes[UPDATE_USERS]);
		output.writeInt(updateds.size());
		for (User user : updateds) {
			output.writeInt(user.getID());
			user.writeProfileData(output);
			output.writeChar(user.tableCount());
			output.writeChar(user.getBomb());
		}

		output.flush();
	}

	public void userList(Base base, List<User> users) {
		output.writeUChar(opcodes[USER_LIST]);
		output.writeInt(users.size());
		for (User other : users) {
			base.writeUser(other, output);
			output.writeShort(other.tableCount());
		}
		output.flush();
	}

	public void watchersNotAllowed() {
		output.writeUChar(opcodes[WATCHERS_NOT_ALLOWED]);
		output.flush();
	}

	public void wellcome(String room) {
		output.writeUChar(opcodes[WELLCOME]);
		output.writeString(room);
		output.flush();
	}

	public void youAreNotTheHostToChangeParameters() {
		output.writeUChar(opcodes[YOURE_NOT_THE_HOST_TO_CHANGE_PARAMETERS]);
		output.flush();
	}

	public void youAreNotTheHostToInvitePlayers() {
		output.writeUChar(opcodes[YOURE_NOT_THE_HOST_TO_INVITE_PLAYERS]);
		output.flush();
	}

	public void youAreNotTheHostToKickPlayers() {
		output.writeUChar(opcodes[YOU_ARE_NOT_THE_HOST_TO_BOOT_PLAYERS]);
		output.flush();
	}

	public void youAreNotTheHostToTransferHost() {
		output.writeUChar(opcodes[YOURE_NOT_THE_HOST_TO_TRANSFER_HOST]);
		output.flush();
	}

	public void youCantChangeParametersNow() {
		output.writeUChar(opcodes[YOU_CANT_CHANGE_CONFIG_NOW]);
		output.flush();
	}

	public void youCantInviteAPlayerAlreadyInTheTable() {
		output.writeUChar(opcodes[YOU_CANT_INVITE_A_PLAYER_ALREADY_IN_THE_TABLE]);
		output.flush();
	}

	public void youCantInviteAPlayerNotInServer() {
		output.writeUChar(opcodes[YOU_CANT_INVITE_A_PLAYER_NOT_IN_SERVER]);
		output.flush();
	}

	public void youCantInviteYourself() {
		output.writeUChar(opcodes[YOU_CANT_INVITE_YOURSELF]);
		output.flush();
	}

	public void youCantKickAPlayerNoInServer() {
		output.writeUChar(opcodes[YOU_CANT_BOOT_A_PLAYER_NOT_IN_SERVER]);
		output.flush();
	}

	public void youCantTransferHostToAPlayerNotInServer() {
		output.writeUChar(opcodes[YOU_CANT_TRANSFER_HOST_TO_A_PLAYER_NOT_IN_SERVER]);
		output.flush();
	}

	public void youCantTransferHostToYourself() {
		output.writeUChar(opcodes[YOU_CANT_TRANSFER_HOST_TO_YOURSELF]);
		output.flush();
	}

	public void youReceivedTheHost(String name, int id) {
		output.writeUChar(opcodes[YOU_RECEIVED_THE_HOST]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void youReceivedTheHostFromTheServer() {
		output.writeUChar(opcodes[YOU_RECEIVED_THE_HOST_FROM_THE_SERVER]);
		output.flush();
	}

	public void youWasKickedByTheHost(String name, int id) {
		output.writeUChar(opcodes[YOU_WAS_KICKED_BY_HOST]);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

}
