package jcheckers.client.net;

/**
 * 
 * Listener que interfaceia todos os eventos genéricos relacionados a uma conexão com o servidor.
 * @author miste
 *
 */
public interface ConnectionListener {

	/**
	 * 
	 * É chamado sempre que um administrador manda uma mensagem para o chat.
	 * @param c Conexão que emitiu o evento.
	 * @param sender Nome do administrador que enviou a mensagem.
	 * @param message Mensagem enviada pelo administrador.
	 */
	void onAdminChat(Connection c, String sender, String message);

	/**
	 * 
	 * É chamado sempre que o usuário que se conectou ao servidor encontra-se banido.
	 * @param c Conexão que emitiu o evento.
	 * @param bannedUntil String contendo a data no qual o banimento do usuário irá expirar.
	 */
	void onBanned(Connection c, String bannedUntil);

	/**
	 * 
	 * É chamado sempre que um usuário envia uma mensagem (pública) no chat
	 * @param c Conexão que emitiu o evento.
	 * @param sender Nome do usuário que enviou a mensagem.
	 * @param message Mensagem enviada pelo usuário.
	 */
	void onChat(Connection c, String sender, String message);

	/**
	 * 
	 * É chamado sempre que a conexão com o servidor é fechada.
	 * @param c Conexão que emitiu o evento.
	 */
	void onClose(Connection c);

	/**
	 * 
	 * É chamado sempre que ocorre uma exceção (erro) na conexão com o servidor.
	 * @param c Conexão que emitiu o evento.
	 * @param e Exceção lançada.
	 */
	void onError(Connection c, Throwable e);

	/**
	 * 
	 * Ocorre sempre que o servidor acusa a senha do usuário como incorreta no ato da conexão, fechando-a imediatamente após o evento ser emitido.
	 * @param c Conexão que emitiu o evento.
	 */
	void onInvalidPassword(Connection c);

	/**
	 * 
	 * Ocorre sempre que o usuário tenta se conectar em uma sala que não existe, fechando a conexão imediatamente após o evento ser emitido.
	 * @param c Conexão que emitiu o evento.
	 */
	void onInvalidRoom(Connection c);

	/**
	 * 
	 * Ocorre sempre que um usuário se conecta na sala.
	 * @param c Conexão que emitiu o evento.
	 * @param user Usuário que se conectou na sala.
	 */
	void onJoinUser(Connection c, User user);

	/**
	 * 
	 * Ocorre sempre que um usuário se desconecta da sala.
	 * @param c Conexão que emitiu o evento.
	 * @param user Usuário que se desconectou da sala.
	 */
	void onLeaveUser(Connection c, User user);

	/**
	 * 
	 * Ocorre sempre que a conexão com o servidor é estabelecida e a autenticação foi enviada, mas ainda não recebeu resposta do servidor.
	 * @param c Conexão que emitiu o evento.
	 */
	void onOpen(Connection c);

	/**
	 * 
	 * @param c Conexão que emitiu o evento.
	 * @param name
	 */
	void onPlayerNotFoundInServer(Connection c, String name);

	/**
	 * 
	 * @param c Conexão que emitiu o evento.
	 * @param src
	 * @param dst
	 */
	void onPong(Connection c, int src, int dst);

	/**
	 * 
	 * É chamado sempre que um usuário envia uma mensagem privada no chat
	 * @param c Conexão que emitiu o evento.
	 * @param sender Nome do usuário que enviou a mensagem.
	 * @param message Mensagem enviada pelo usuário.
	 */
	void onPrivateChat(Connection c, String sender, String message);

	/**
	 * 
	 * @param c Conexão que emitiu o evento.
	 * @param stats
	 * @param tables
	 * @param inactiveTime
	 */
	void onResponseInfo(Connection c, UserStats stats, int[] tables, int inactiveTime);

	/**
	 * 
	 * Ocorre sempre que o servidor está sendo desligado (usando soft shut-down).
	 * @param c Conexão que emitiu o evento.
	 */
	void onServerShuttingDown(Connection c);

	/**
	 * 
	 * @param c Conexão que emitiu o evento.
	 * @param version Versão do servidor.
	 * @param lastRelease Data da última modificação no código do servidor.
	 */
	void onServerVersion(Connection c, String version, String lastRelease);

	/**
	 * 
	 * @param c Conexão que emitiu o evento.
	 * @param users
	 */
	void onUpdateUsers(Connection c, User[] users);

	/**
	 * 
	 * @param c Conexão que emitiu o evento.
	 * @param users
	 */
	void onUserList(Connection c, User[] users);

	/**
	 * 
	 * Ocorre após a entrada com sucesso em uma sala de jogos.
	 * @param c Conexão que emitiu o evento.
	 * @param roomName Nome da sala.
	 */
	void onWelcome(Connection c, String roomName);

}
