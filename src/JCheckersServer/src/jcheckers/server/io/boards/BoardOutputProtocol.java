package jcheckers.server.io.boards;

import jcheckers.common.io.JCheckersDataOutputStream;
import jcheckers.server.io.OutputProtocol;

public class BoardOutputProtocol extends OutputProtocol {

	public static final int RATING_TOO_HIGH = 0x50;
	public static final int PACHISI_GAME_STATE = 0x8a;
	public static final int DRAUGHTS_AND_CHESS_GAME_STATE = 0x8b;
	public static final int PLAYER_SUGGESTED_PAUSE_GAME = 0xa3; // string name,
																// int id, int
																// time
	public static final int PLAYER_REJECTED_PAUSE_GAME = 0xa7; // string name,
																// int id
	public static final int PLAYER_OFFERED_DRAW = 0xb4; // string name, int id
	public static final int PLAYER_REJECTED_DRAW = 0xb7; // string name, int id
	public static final int PLAYER_ACCEPTED_DRAW = 0xb8; // string name, int id
	public static final int PLAYER_SUGGESTED_UNDO_MOVE = 0xba; // string name,
																// int id
	public static final int PLAYER_REJECTED_UNDO_MOVE = 0xbd; // string name,
																// int id
	public static final int PLAYER_ACCEPTED_UNDO_MOVE = 0xbe; // string name,
																// int id
	public static final int HIDE_CONTROLS = 0xc3;
	public static final int GAME_PAUSED = 0xc5;
	public static final int WINNERS = 0xc8;
	public static final int RATING_CHANGE = 0xcd;

	private static final int[] RESPONSE_OPCODES = new int[256];

	static {
		for (int i = 0; i < RESPONSE_OPCODES.length; i++)
			RESPONSE_OPCODES[i] = i;

		RESPONSE_OPCODES[MY_ID] = 0x08;
		RESPONSE_OPCODES[JOIN_USER] = 0x0a;
		RESPONSE_OPCODES[LEAVE_USER] = 0x0c;
		RESPONSE_OPCODES[USER_LIST] = 0x0d;
		RESPONSE_OPCODES[ROOM_LIST] = 0x0f;
		RESPONSE_OPCODES[ROOM_CHANGED] = 0x10;
		RESPONSE_OPCODES[INVALID_ROOM] = 0x11;
		RESPONSE_OPCODES[RESPONSE_CHAT] = 0x13;
		RESPONSE_OPCODES[RESPONSE_PRIVATE_CHAT] = 0x14;
		RESPONSE_OPCODES[RESPONSE_ADMIN_CHAT] = 0x15;
		RESPONSE_OPCODES[PING_CONNECTION] = 0x16;
		RESPONSE_OPCODES[PING] = 0x18;
		RESPONSE_OPCODES[PONG] = 0x19;
		RESPONSE_OPCODES[TABLE_FOCUS] = 0x1b;
		RESPONSE_OPCODES[CHANGE_CRYPT_MODE] = 0x21;
		RESPONSE_OPCODES[NO_CRYPT_NEXT_BLOCK] = 0x24;
		RESPONSE_OPCODES[NOTIFY_BANNED_AND_DISCONNECT] = 0x26;
		RESPONSE_OPCODES[NOTIFY_BANNED_ONLY] = 0x27;
		RESPONSE_OPCODES[RESPONSE_SERVER_VERSION] = 0x2c;
		RESPONSE_OPCODES[SERVER_DEBUG_MESSAGE] = 0x2e;
		RESPONSE_OPCODES[ACCESS_EXPIRED_ON] = 0x32;
		RESPONSE_OPCODES[ACCOUNT_BLOCKED] = 0x3a;
		RESPONSE_OPCODES[ACCESS_EXPIRED] = 0x3d;
		RESPONSE_OPCODES[BONUS_EXPIRED] = 0x3e;
		RESPONSE_OPCODES[GUESTS_NOT_ALLOWED_TO_PLAY] = 0x43;
		RESPONSE_OPCODES[GUESTS_NOT_ALLOWED_TO_PLAY_2] = 0x44;
		RESPONSE_OPCODES[GUESTS_NOT_ALLOWED_TO_PLAY_3] = 0x45;
		RESPONSE_OPCODES[BONUS_RECEIVED] = 0x46;
		RESPONSE_OPCODES[BONUS_EXPIRED_2] = 0x47;
		RESPONSE_OPCODES[SERVER_SHUTTING_DOWN] = 0x49;
		RESPONSE_OPCODES[INVALID_PASSWORD] = 0x4a;
		RESPONSE_OPCODES[COULD_NOT_CONNECT_TO_THE_ROOM] = 0x4b;
		RESPONSE_OPCODES[TABLE_NOT_EXIST] = 0x4c;
		RESPONSE_OPCODES[YOU_HAVE_MAXIMUM_TABLES_OPEN] = 0x4d;
		RESPONSE_OPCODES[CONNECT_TO_THE_TABLE_REFUSED] = 0x4e;
		RESPONSE_OPCODES[ALREADY_CONNECTED_ON_THE_TABLE] = 0x4f;
		RESPONSE_OPCODES[PRIVATE_TABLE] = 0x50;
		RESPONSE_OPCODES[NOT_ENOUGH_RATING] = 0x51;
		RESPONSE_OPCODES[RATING_TOO_HIGH] = 0x52;
		RESPONSE_OPCODES[WATCHERS_NOT_ALLOWED] = 0x53;
		RESPONSE_OPCODES[NO_VACANCY_IN_THE_TABLE] = 0x54;
		RESPONSE_OPCODES[COULD_NOT_CONNECT_TO_THE_TABLE_TRY_AGAIN_LATTER] = 0x55;
		RESPONSE_OPCODES[COULD_NOT_CONNECT_TO_THE_TABLE] = 0x56;
		RESPONSE_OPCODES[WELLCOME] = 0x57;
		RESPONSE_OPCODES[RESPONSE_INFO] = 0x59;
		RESPONSE_OPCODES[PLAYER_NOT_FOUND_IN_SERVER] = 0x5a;
		RESPONSE_OPCODES[UPDATE_TABLE] = 0x5b;
		RESPONSE_OPCODES[AVATARS] = 0x5d;
		RESPONSE_OPCODES[CLOSE_TABLE] = 0x5e;
		RESPONSE_OPCODES[YOURE_NOT_THE_HOST_TO_CHANGE_PARAMETERS] = 0x60;
		RESPONSE_OPCODES[CONFIG_CHANGED] = 0x61;
		RESPONSE_OPCODES[YOU_ARE_NOT_THE_HOST_TO_BOOT_PLAYERS] = 0x63;
		RESPONSE_OPCODES[YOU_CANT_BOOT_YOURSELF] = 0x64;
		RESPONSE_OPCODES[YOU_CANT_BOOT_A_PLAYER_NOT_IN_SERVER] = 0x65;
		RESPONSE_OPCODES[YOU_WAS_KICKED_BY_HOST] = 0x66;
		RESPONSE_OPCODES[OTHER_WAS_KICKED_BY_HOST] = 0x67;
		RESPONSE_OPCODES[YOU_CANT_KICK_A_RECONNECTING_PLAYER] = 0x68;
		RESPONSE_OPCODES[YOURE_NOT_THE_HOST_TO_INVITE_PLAYERS] = 0x6a;
		RESPONSE_OPCODES[YOU_CANT_INVITE_YOURSELF] = 0x6b;
		RESPONSE_OPCODES[YOU_CANT_INVITE_A_PLAYER_NOT_IN_SERVER] = 0x6c;
		RESPONSE_OPCODES[INVITED] = 0x6d;
		RESPONSE_OPCODES[INVITATION_REJECTED] = 0x71;
		RESPONSE_OPCODES[INVITATION_AUTO_REJECTED] = 0x73;
		RESPONSE_OPCODES[YOU_CANT_INVITE_A_PLAYER_ALREADY_IN_THE_TABLE] = 0x74;
		RESPONSE_OPCODES[YOU_CANT_INVITE_A_WATCHER_DUE_NO_VACANCY] = 0x75;
		RESPONSE_OPCODES[INVALID_INVIATION] = 0x76;
		RESPONSE_OPCODES[INVITATION_SENT] = 0x77;
		RESPONSE_OPCODES[INVITATIONS_INACTIVE] = 0x78;
		RESPONSE_OPCODES[YOURE_NOT_THE_HOST_TO_TRANSFER_HOST] = 0x7a;
		RESPONSE_OPCODES[YOU_CANT_TRANSFER_HOST_TO_YOURSELF] = 0x7b;
		RESPONSE_OPCODES[YOU_CANT_TRANSFER_HOST_TO_A_PLAYER_NOT_IN_SERVER] = 0x7c;
		RESPONSE_OPCODES[YOU_RECEIVED_THE_HOST] = 0x7d;
		RESPONSE_OPCODES[OTHER_RECEIVED_THE_HOST] = 0x7e;
		RESPONSE_OPCODES[YOU_CANT_TRANSFER_THE_HOST_TO_A_RECONNECTING_PLAYER] = 0x7f;
		RESPONSE_OPCODES[COULD_RECONNECT_TO_THE_TABLE_TRY_AGAIN_LATTER] = 0x80;
		RESPONSE_OPCODES[YOU_RECEIVED_THE_HOST_FROM_THE_SERVER] = 0x81;
		RESPONSE_OPCODES[PLAYER_CONNECT] = 0x82;
		RESPONSE_OPCODES[PLAYER_DISCONNECT] = 0x83;
		RESPONSE_OPCODES[PLAYER_RECONNECTING] = 0x84;
		RESPONSE_OPCODES[PLAYER_RECONNECTING_FAILED] = 0x85;
		RESPONSE_OPCODES[YOU_CANT_WATCH_THIS_GAME] = 0x87;
		RESPONSE_OPCODES[DRAUGHTS_AND_CHESS_GAME_STATE] = 0x89;
		RESPONSE_OPCODES[UPDATE_USERS] = 0x8d;
		RESPONSE_OPCODES[YOU_WAS_KICKED_BY_ADMIN] = 0x8f;
		RESPONSE_OPCODES[OTHER_WAS_KICKED_BY_ADMIN] = 0x90;
		RESPONSE_OPCODES[NO_ENOUGH_PLAYERS_TO_START_THE_GAME] = 0x92;
		RESPONSE_OPCODES[START_GAME_SUGGESTED] = 0x93;
		RESPONSE_OPCODES[START_GAME_SUGGEST_ACCEPTED] = 0x95;
		RESPONSE_OPCODES[START_GAME_SUGGEST_REJECTED] = 0x96;
		RESPONSE_OPCODES[STOP_GAME_SUGGESTED] = 0x99;
		RESPONSE_OPCODES[STOP_GAME_SUGGEST_REJECTED] = 0x9c;
		RESPONSE_OPCODES[PLAYER_REJECTED_PAUSE_GAME] = 0xa6;
		RESPONSE_OPCODES[QUESTION_BITS] = 0xc1;
		RESPONSE_OPCODES[QUESTION_CANCELED] = 0xc2;
		RESPONSE_OPCODES[STOP_GAME] = 0xc4;
		RESPONSE_OPCODES[START_GAME] = 0xc6;
		RESPONSE_OPCODES[PLAYER_STAND_UP] = 0xca;
		RESPONSE_OPCODES[CONTINUE_GAME] = 0xcb;
		RESPONSE_OPCODES[SAME_LOCAL_NETWORK] = 0xcc;
		RESPONSE_OPCODES[YOU_CANT_CHANGE_CONFIG_NOW] = 0xce;
	}

	public BoardOutputProtocol(JCheckersDataOutputStream output) {
		super(output);

		opcodes = RESPONSE_OPCODES;
	}

	public void gamePaused() {
		output.writeUChar(GAME_PAUSED);
		output.flush();
	}

	public void hideControls() {
		output.writeUChar(HIDE_CONTROLS);
		output.flush();
	}

	public void notifyPlayerAcceptedDraw(String name, int id) {
		output.writeUChar(PLAYER_ACCEPTED_DRAW);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void notifyPlayerAcceptedUndoMove(String name, int id) {
		output.writeUChar(PLAYER_ACCEPTED_UNDO_MOVE);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void notifyPlayerOfferedDraw(String name, int id) {
		output.writeUChar(PLAYER_OFFERED_DRAW);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void notifyPlayerRejectedDraw(String name, int id) {
		output.writeUChar(PLAYER_REJECTED_DRAW);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void notifyPlayerRejectedPauseGame(String name, int id) {
		output.writeUChar(PLAYER_REJECTED_PAUSE_GAME);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void notifyPlayerRejectedUndoMove(String name, int id) {
		output.writeUChar(PLAYER_REJECTED_UNDO_MOVE);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void notifyPlayerSuggestedPauseGame(String name, int id, int time) {
		output.writeUChar(PLAYER_SUGGESTED_PAUSE_GAME);
		output.writeString(name);
		output.writeInt(id);
		output.writeInt(time);
		output.flush();
	}

	public void notifyPlayerSuggestedUndoLastMove(String name, int id) {
		output.writeUChar(PLAYER_SUGGESTED_UNDO_MOVE);
		output.writeString(name);
		output.writeInt(id);
		output.flush();
	}

	public void notifyRatingChange(int[] sitIndexes, String[] names, int[] gains, int[] ratings) {
		output.writeUChar(RATING_CHANGE);
		output.writeChar(sitIndexes.length);
		for (int i = 0; i < sitIndexes.length; i++) {
			output.writeChar(sitIndexes[i]);
			output.writeString(names[i]);
			output.writeInt(gains[i]);
			output.writeInt(ratings[i]);
		}
		output.flush();
	}

	public void notifyRatingTooHigh() {
		output.writeUChar(RATING_TOO_HIGH);
		output.flush();
	}

	public void notifyWinners(int byte1, int byte2, int byte3, int byte4, int byte5, int byte6, String[] names) {
		output.writeUChar(WINNERS);
		output.writeChar(byte1);
		output.writeChar(byte2);
		output.writeChar(byte3);
		output.writeChar(byte4);
		output.writeChar(byte5);
		output.writeChar(byte6);
		output.writeChar(names.length);
		for (String name : names)
			output.writeString(name);
		output.flush();
	}

}
