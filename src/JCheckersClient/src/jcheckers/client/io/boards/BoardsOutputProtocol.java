package jcheckers.client.io.boards;

import jcheckers.client.io.OutputProtocol;
import jcheckers.common.io.JCheckersDataOutputStream;

public class BoardsOutputProtocol extends OutputProtocol {

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

		REQUEST_OPCODES[CHANGE_ROOM] = 0x10;
		REQUEST_OPCODES[SEND_CHAT_MESSAGE] = 0x12;
		REQUEST_OPCODES[PING_CONNECTION] = 0x16;
		REQUEST_OPCODES[PING] = 0x18;
		REQUEST_OPCODES[PONG] = 0x19;
		REQUEST_OPCODES[GOT_FOCUS] = 0x1c;
		REQUEST_OPCODES[LOST_FOCUS] = 0x1d;
		REQUEST_OPCODES[CHANGE_KEY] = 0x21;
		REQUEST_OPCODES[NO_CRYPT_NEXT_BLOCK] = 0x24;
		REQUEST_OPCODES[EXIT] = 0x25;
		REQUEST_OPCODES[REQUEST_SERVER_VERSION] = 0x2b;
		REQUEST_OPCODES[REQUEST_SERVER_IP] = 0x2d;
		REQUEST_OPCODES[REQUEST_INFO] = 0x58;
		REQUEST_OPCODES[CHANGE_AVATAR] = 0x5c;
		REQUEST_OPCODES[CHANGE_TABLE_PARAMETERS] = 0x5f;
		REQUEST_OPCODES[BOOT] = 0x62;
		REQUEST_OPCODES[INVITE] = 0x69;
		REQUEST_OPCODES[INVITATION_REJECTED] = 0x71;
		REQUEST_OPCODES[INVITATION_AUTO_REJECTED] = 0x73;
		REQUEST_OPCODES[TRANSFER_HOST] = 0x79;
		REQUEST_OPCODES[SIT] = 0x86;
		REQUEST_OPCODES[STAND_UP] = 0x88;
		REQUEST_OPCODES[SUGGEST_START_GAME] = 0x91;
		REQUEST_OPCODES[START_GAME_SUGGEST_ACCEPTED] = 0x94;
		REQUEST_OPCODES[START_GAME_SUGGEST_REJECTED] = 0x95;
		REQUEST_OPCODES[SUGGEST_STOP_GAME] = 0x98;
		REQUEST_OPCODES[STOP_GAME_SUGGEST_ACCEPTED] = 0x9a;
		REQUEST_OPCODES[STOP_GAME_SUGGEST_REJECTED] = 0x9b;
		REQUEST_OPCODES[DONT_CONTINUE_GAME] = 0xbf;
		REQUEST_OPCODES[CONTINUE_GAME] = 0xc0;
	}

	public BoardsOutputProtocol(JCheckersDataOutputStream output) {
		super(output);

		opcodes = REQUEST_OPCODES;
	}

}
