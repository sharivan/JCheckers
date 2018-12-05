package jcheckers.client.net;

/**
 * 
 * Listener que interfaceia todos os eventos relacionados a conex�o com uma sala de jogos.
 * @author miste
 *
 */
public interface RoomConnectionListener extends ConnectionListener {

	/**
	 * 
	 * � emitodo sempre quando o servidor responde que n�o foi poss�vel se conectar a sala desejada.
	 * @param c Conex�o que emitiu o evento.
	 */
	void onCouldNotConnectToTheRoom(Connection c);

	/**
	 * 
	 * @param c Conex�o que emitiu o evento.
	 * @param tableID
	 * @param tableNumber
	 * @param inviterStats
	 */
	void onInvited(Connection c, int tableID, int tableNumber, UserStats inviterStats);

	/**
	 * 
	 * � emitido ap�s a conex�o com o servidor ser estabelecida com sucesso mas ainda n�o foi feita a autentica��o, por�m o objeto relacionado a sala foi criado com sucesso e a tentativa de entrar na sala foi iniciada.
	 * @param c Conex�o que emitiu o evento.
	 * @param room
	 */
	void onJoinLobby(Connection c, Room room);

	/**
	 * 
	 * Ocorre quando uma nova mesa � criada dentro da sala de jogos.
	 * @param c Conex�o que emitiu o evento.
	 * @param table Mesa que foi criada.
	 */
	void onNewTable(Connection c, Table table);

	/**
	 * 
	 * Ocorre quando uma mesa existente na sala de jogos foi removida.
	 * @param c Conex�o que emitiu o evento.
	 * @param table Mesa que foi removida.
	 */
	void onRemoveTable(Connection c, Table table);

	/**
	 * 
	 * Ocorre quando o usu�rio muda de sala.
	 * @param c Conex�o que emitiu o evento.
	 * @param roomIndex �ndice da nova sala na lista de salas do servidor.
	 */
	void onRoomChanged(Connection c, int roomIndex);

	/**
	 * 
	 * Ocorre quando uma mesa existente na sala de jogos teve seu estado atualizado.
	 * @param c Conex�o que emitiu o evento.
	 * @param table Mesa que foi atualizada.
	 */
	void onUpdateTable(Connection c, Table table);

}
