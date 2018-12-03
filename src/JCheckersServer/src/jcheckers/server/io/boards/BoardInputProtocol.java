package jcheckers.server.io.boards;

import jcheckers.common.io.JCheckersDataInputStream;
import jcheckers.server.io.InputProtocol;

public class BoardInputProtocol extends InputProtocol {

	public static final int SUGGEST_PAUSE_GAME = 0xa2; // int time (in minutes,
														// 0=no limit)
	public static final int ACCEPT_PAUSE_GAME = 0xa4;
	public static final int REJECT_PAUSE_GAME = 0xa5;
	public static final int OFFER_DRAW = 0xb3;
	public static final int ACCEPT_DRAW = 0xb5;
	public static final int REJECT_DRAW = 0xb6;
	public static final int SUGGEST_UNDO_LAST_MOVE = 0xb9;
	public static final int ACCEPT_UNDO_LAST_MOVE = 0xbb;
	public static final int REJECT_UNDO_LAST_MOVE = 0xbc;
	public static final int TIME_UP = 0xcf;
	public static final int CHECKERS_AND_CHESS_MOVE = 0xd0;
	public static final int RANDOM_MOVE = 0xd4;
	public static final int RESIGN = 0xd5;

	private static final int[] REQUEST_OPCODES = new int[256];

	static {
		for (int i = 0; i < REQUEST_OPCODES.length; i++)
			REQUEST_OPCODES[i] = i;

		REQUEST_OPCODES[0x10] = CHANGE_ROOM;
		REQUEST_OPCODES[0x12] = RECEIVE_CHAT;
		REQUEST_OPCODES[0x16] = PING_CONNECTION;
		REQUEST_OPCODES[0x18] = PING;
		REQUEST_OPCODES[0x19] = PONG;
		REQUEST_OPCODES[0x1c] = GOT_FOCUS;
		REQUEST_OPCODES[0x1d] = LOST_FOCUS;
		REQUEST_OPCODES[0x21] = CHANGE_KEY;
		REQUEST_OPCODES[0x24] = NO_CRYPT_NEXT_BLOCK;
		REQUEST_OPCODES[0x25] = EXIT;
		REQUEST_OPCODES[0x2b] = REQUEST_SERVER_VERSION;
		REQUEST_OPCODES[0x2d] = REQUEST_SERVER_IP;
		REQUEST_OPCODES[0x58] = REQUEST_INFO;
		REQUEST_OPCODES[0x5c] = CHANGE_AVATAR;
		REQUEST_OPCODES[0x5f] = CHANGE_TABLE_PARAMETERS;
		REQUEST_OPCODES[0x62] = BOOT;
		REQUEST_OPCODES[0x69] = INVITE;
		REQUEST_OPCODES[0x71] = INVITATION_REJECTED;
		REQUEST_OPCODES[0x73] = INVITATION_AUTO_REJECTED;
		REQUEST_OPCODES[0x79] = TRANSFER_HOST;
		REQUEST_OPCODES[0x86] = SIT;
		REQUEST_OPCODES[0x88] = STAND_UP;
		REQUEST_OPCODES[0x91] = SUGGEST_START_GAME;
		REQUEST_OPCODES[0x94] = START_GAME_SUGGEST_ACCEPTED;
		REQUEST_OPCODES[0x95] = START_GAME_SUGGEST_REJECTED;
		REQUEST_OPCODES[0x98] = SUGGEST_STOP_GAME;
		REQUEST_OPCODES[0x9a] = STOP_GAME_SUGGEST_ACCEPTED;
		REQUEST_OPCODES[0x9b] = STOP_GAME_SUGGEST_REJECTED;
		REQUEST_OPCODES[0xbf] = DONT_CONTINUE_GAME;
		REQUEST_OPCODES[0xc0] = CONTINUE_GAME;
	}

	public BoardInputProtocol(JCheckersDataInputStream input) {
		super(input);

		opcodes = REQUEST_OPCODES;
	}

}
