package jcheckers.client.io;

import jcheckers.common.io.JCheckersDataOutputStream;

public class OutputProtocol {

	public static final int HELLO = 0x01;
	public static final int JOIN_ROOM = 0x02;
	public static final int CREATE_TABLE = 0x03;
	public static final int JOIN_TABLE_SITING = 0x04;
	public static final int JOIN_TABLE_WATCHING = 0x05;
	public static final int JOIN_TABLE_RECONNECTED_SITING = 0x06;
	public static final int JOIN_TABLE_RECONNECTED_WATCHING = 0x07;
	public static final int CHANGE_ROOM = 0x0e;
	public static final int SEND_CHAT_MESSAGE = 0x10;
	public static final int PING_CONNECTION = 0x15;
	public static final int PING = 0x17;
	public static final int PONG = 0x18;
	public static final int GOT_FOCUS = 0x1a;
	public static final int LOST_FOCUS = 0x1b;
	public static final int CHANGE_KEY = 0x21;
	public static final int NO_CRYPT_NEXT_BLOCK = 0x22;
	public static final int EXIT = 0x23;
	public static final int REQUEST_SERVER_VERSION = 0x29;
	public static final int REQUEST_SERVER_IP = 0x2b;
	public static final int REQUEST_INFO = 0x49;
	public static final int CHANGE_AVATAR = 0x4d;
	public static final int CHANGE_TABLE_PARAMETERS = 0x50;
	public static final int BOOT = 0x53;
	public static final int INVITE = 0x5a;
	public static final int INVITATION_REJECTED = 0x62;
	public static final int INVITATION_AUTO_REJECTED = 0x64;
	public static final int TRANSFER_HOST = 0x6a;
	public static final int SIT = 0x77;
	public static final int STAND_UP = 0x79;
	public static final int SUGGEST_START_GAME = 0x83;
	public static final int START_GAME_SUGGEST_ACCEPTED = 0x87;
	public static final int START_GAME_SUGGEST_REJECTED = 0x88;
	public static final int SUGGEST_STOP_GAME = 0x8b;
	public static final int STOP_GAME_SUGGEST_ACCEPTED = 0x8d;
	public static final int STOP_GAME_SUGGEST_REJECTED = 0x8e;
	public static final int DONT_CONTINUE_GAME = 0x90;
	public static final int CONTINUE_GAME = 0x91;

	protected JCheckersDataOutputStream output;
	protected int[] opcodes;

	public OutputProtocol(JCheckersDataOutputStream output) {
		this.output = output;
	}

	public int getOpcode(int index) {
		return opcodes[index];
	}

	public void pingConnection() {
		output.writeUChar(opcodes[PING_CONNECTION]);
		output.flush();
	}

	public void pong(int src, int dst) {
		output.writeUChar(opcodes[PONG]);
		output.writeInt(dst);
		output.writeInt(src);
		output.flush();
	}

	public void sendMessageToChat(String message) {
		output.writeUChar(opcodes[SEND_CHAT_MESSAGE]);
		output.writeEasyString(message);
		output.flush();
	}

}
