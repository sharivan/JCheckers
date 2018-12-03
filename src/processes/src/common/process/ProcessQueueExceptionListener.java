package common.process;

public interface ProcessQueueExceptionListener {

	void notifyException(Throwable e);

}
