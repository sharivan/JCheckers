package common.io;

import java.util.ArrayList;

public class CommandLineParser {

	private String input;
	private int pos;

	private ArrayList<String> parameters;

	public CommandLineParser(String input) {
		this.input = input;

		pos = 0;

		parseParameters();
	}

	public String getInput() {
		return input;
	}

	public String getParameter(int index) {
		return parameters.get(index);
	}

	public int getParameterCount() {
		return parameters.size();
	}

	private void parseParameters() {
		skipBlanks();

		char c = input.charAt(pos++);
		if (c == '"')
			while (input.charAt(pos++) != '"') {
			}
		else
			while (input.charAt(pos++) != ' ') {
			}
	}

	private void skipBlanks() {
		while (input.charAt(pos++) == ' ') {
		}
	}

}
