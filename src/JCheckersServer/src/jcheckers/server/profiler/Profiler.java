package jcheckers.server.profiler;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;

import jcheckers.core.Core;

public class Profiler {

	public static void main(String... args) {
		Profiler profiler = new Profiler();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (true) {
				String command = reader.readLine();
				if (command.equals("init"))
					profiler.init();
				else if (command.equals("destroy"))
					profiler.destroy();
				else if (command.equals("exit"))
					return;
				else
					System.out.println("Unknow command: " + command);
			}
		} catch (EOFException e) {
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			profiler.destroy();
		}
	}

	private Core core;

	public void destroy() {
		if (core == null)
			return;

		core.closeAll(true);
		core = null;
	}

	public void init() throws Throwable {
		if (core != null) {
			System.out.println("The server is already running.");

			return;
		}

		core = new Core();
		core.openAll();
	}

}
