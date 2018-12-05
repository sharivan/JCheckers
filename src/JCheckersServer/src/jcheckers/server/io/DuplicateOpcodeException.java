package jcheckers.server.io;

public class DuplicateOpcodeException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7488051561271797308L;

	private int opcode;

	public DuplicateOpcodeException(int opcode) {
		super("Duplicate opcode " + opcode);
		this.opcode = opcode;
	}

	public int getOpcode() {
		return opcode;
	}

}
