package common.process;

public interface ReturnableProcess<T> extends Process {

	T exec();

}
