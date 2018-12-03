package common.process;

public interface Interruptable extends Closeable {

	void interrupt();

}
