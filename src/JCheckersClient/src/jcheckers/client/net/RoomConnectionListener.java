package jcheckers.client.net;

/**
 * 
 * Listener que interfaceia todos os eventos relacionados a conexão com uma sala de jogos.
 * @author miste
 *
 */
public interface RoomConnectionListener extends ConnectionListener {

	/**
	 * 
	 * É emitodo sempre quando o servidor responde que não foi possível se conectar a sala desejada.
	 * @param c Conexão que emitiu o evento.
	 */
	void onCouldNotConnectToTheRoom(Connection c);

	/**
	 * 
	 * @param c Conexão que emitiu o evento.
	 * @param tableID
	 * @param tableNumber
	 * @param inviterStats
	 */
	void onInvited(Connection c, int tableID, int tableNumber, UserStats inviterStats);

	/**
	 * 
	 * É emitido após a conexão com o servidor ser estabelecida com sucesso mas ainda não foi feita a autenticação, porém o objeto relacionado a sala foi criado com sucesso e a tentativa de entrar na sala foi iniciada.
	 * @param c Conexão que emitiu o evento.
	 * @param room
	 */
	void onJoinLobby(Connection c, Room room);

	/**
	 * 
	 * Ocorre quando uma nova mesa é criada dentro da sala de jogos.
	 * @param c Conexão que emitiu o evento.
	 * @param table Mesa que foi criada.
	 */
	void onNewTable(Connection c, Table table);

	/**
	 * 
	 * Ocorre quando uma mesa existente na sala de jogos foi removida.
	 * @param c Conexão que emitiu o evento.
	 * @param table Mesa que foi removida.
	 */
	void onRemoveTable(Connection c, Table table);

	/**
	 * 
	 * Ocorre quando o usuário muda de sala.
	 * @param c Conexão que emitiu o evento.
	 * @param roomIndex Índice da nova sala na lista de salas do servidor.
	 */
	void onRoomChanged(Connection c, int roomIndex);

	/**
	 * 
	 * Ocorre quando uma mesa existente na sala de jogos teve seu estado atualizado.
	 * @param c Conexão que emitiu o evento.
	 * @param table Mesa que foi atualizada.
	 */
	void onUpdateTable(Connection c, Table table);

}
