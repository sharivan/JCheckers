package common.process;

public interface NonReturnableProcessWithArg<T> extends NonReturnableProcess {

	void exec(T arg);

}
