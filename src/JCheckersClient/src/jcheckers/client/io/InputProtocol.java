package jcheckers.client.io;

import jcheckers.common.io.JCheckersDataInputStream;

/**
 * 
 * Contém os opcodes utilizados no protocolo de entrada, vindo do servidor.
 * @author miste
 *
 */
public class InputProtocol {

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

	protected JCheckersDataInputStream input;
	protected int[] opcodes;

	public InputProtocol(JCheckersDataInputStream input) {
		this.input = input;
	}

	public int getOpcode(int index) {
		return opcodes[index];
	}

}
