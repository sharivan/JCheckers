package jcheckers.client.net;

/**
 * 
 * Listener que interfaceia todos os eventos gen�ricos relacionados a uma conex�o com o servidor.
 * @author miste
 *
 */
public interface ConnectionListener {

	/**
	 * 
	 * � chamado sempre que um administrador manda uma mensagem para o chat.
	 * @param c Conex�o que emitiu o evento.
	 * @param sender Nome do administrador que enviou a mensagem.
	 * @param message Mensagem enviada pelo administrador.
	 */
	void onAdminChat(Connection c, String sender, String message);

	/**
	 * 
	 * � chamado sempre que o usu�rio que se conectou ao servidor encontra-se banido.
	 * @param c Conex�o que emitiu o evento.
	 * @param bannedUntil String contendo a data no qual o banimento do usu�rio ir� expirar.
	 */
	void onBanned(Connection c, String bannedUntil);

	/**
	 * 
	 * � chamado sempre que um usu�rio envia uma mensagem (p�blica) no chat
	 * @param c Conex�o que emitiu o evento.
	 * @param sender Nome do usu�rio que enviou a mensagem.
	 * @param message Mensagem enviada pelo usu�rio.
	 */
	void onChat(Connection c, String sender, String message);

	/**
	 * 
	 * � chamado sempre que a conex�o com o servidor � fechada.
	 * @param c Conex�o que emitiu o evento.
	 */
	void onClose(Connection c);

	/**
	 * 
	 * � chamado sempre que ocorre uma exce��o (erro) na conex�o com o servidor.
	 * @param c Conex�o que emitiu o evento.
	 * @param e Exce��o lan�ada.
	 */
	void onError(Connection c, Throwable e);

	/**
	 * 
	 * Ocorre sempre que o servidor acusa a senha do usu�rio como incorreta no ato da conex�o, fechando-a imediatamente ap�s o evento ser emitido.
	 * @param c Conex�o que emitiu o evento.
	 */
	void onInvalidPassword(Connection c);

	/**
	 * 
	 * Ocorre sempre que o usu�rio tenta se conectar em uma sala que n�o existe, fechando a conex�o imediatamente ap�s o evento ser emitido.
	 * @param c Conex�o que emitiu o evento.
	 */
	void onInvalidRoom(Connection c);

	/**
	 * 
	 * Ocorre sempre que um usu�rio se conecta na sala.
	 * @param c Conex�o que emitiu o evento.
	 * @param user Usu�rio que se conectou na sala.
	 */
	void onJoinUser(Connection c, User user);

	/**
	 * 
	 * Ocorre sempre que um usu�rio se desconecta da sala.
	 * @param c Conex�o que emitiu o evento.
	 * @param user Usu�rio que se desconectou da sala.
	 */
	void onLeaveUser(Connection c, User user);

	/**
	 * 
	 * Ocorre sempre que a conex�o com o servidor � estabelecida e a autentica��o foi enviada, mas ainda n�o recebeu resposta do servidor.
	 * @param c Conex�o que emitiu o evento.
	 */
	void onOpen(Connection c);

	/**
	 * 
	 * @param c Conex�o que emitiu o evento.
	 * @param name
	 */
	void onPlayerNotFoundInServer(Connection c, String name);

	/**
	 * 
	 * @param c Conex�o que emitiu o evento.
	 * @param src
	 * @param dst
	 */
	void onPong(Connection c, int src, int dst);

	/**
	 * 
	 * � chamado sempre que um usu�rio envia uma mensagem privada no chat
	 * @param c Conex�o que emitiu o evento.
	 * @param sender Nome do usu�rio que enviou a mensagem.
	 * @param message Mensagem enviada pelo usu�rio.
	 */
	void onPrivateChat(Connection c, String sender, String message);

	/**
	 * 
	 * @param c Conex�o que emitiu o evento.
	 * @param stats
	 * @param tables
	 * @param inactiveTime
	 */
	void onResponseInfo(Connection c, UserStats stats, int[] tables, int inactiveTime);

	/**
	 * 
	 * Ocorre sempre que o servidor est� sendo desligado (usando soft shut-down).
	 * @param c Conex�o que emitiu o evento.
	 */
	void onServerShuttingDown(Connection c);

	/**
	 * 
	 * @param c Conex�o que emitiu o evento.
	 * @param version Vers�o do servidor.
	 * @param lastRelease Data da �ltima modifica��o no c�digo do servidor.
	 */
	void onServerVersion(Connection c, String version, String lastRelease);

	/**
	 * 
	 * @param c Conex�o que emitiu o evento.
	 * @param users
	 */
	void onUpdateUsers(Connection c, User[] users);

	/**
	 * 
	 * @param c Conex�o que emitiu o evento.
	 * @param users
	 */
	void onUserList(Connection c, User[] users);

	/**
	 * 
	 * Ocorre ap�s a entrada com sucesso em uma sala de jogos.
	 * @param c Conex�o que emitiu o evento.
	 * @param roomName Nome da sala.
	 */
	void onWelcome(Connection c, String roomName);

}
