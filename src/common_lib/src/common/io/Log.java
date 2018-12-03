package common.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import common.util.DateTimeUtil;
import common.util.DebugUtil;

public class Log {

	private static String completeMessage(String prefix, String message) {
		message = prefix != null ? "[" + prefix + "] " + message : message;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		message = DateTimeUtil.dateToStr(calendar) + ": " + message;

		return message;
	}

	public static void logToErr(PrintStream err, String prefix, String message, Throwable e) {
		synchronized (err) {
			String errorMsg = e != null ? e.getMessage() : null;
			err.println(completeMessage(prefix, message != null ? message : errorMsg != null ? errorMsg : ""));
			if (e != null)
				e.printStackTrace(err);
		}
	}

	public static void logToErr(PrintStream err, String prefix, String[] messages, Throwable e) {
		synchronized (err) {
			for (String message : messages)
				logToErr(err, prefix, message, null);

			if (e != null)
				logToErr(err, prefix, (String) null, e);
		}
	}

	public static void logToOut(PrintStream out, String prefix, String message) {
		out.println(completeMessage(prefix, message));
	}

	public static void logToOut(PrintStream out, String prefix, String[] messages) {
		synchronized (out) {
			for (String message : messages)
				logToOut(out, prefix, message);
		}
	}

	private PrintStream out;
	private PrintStream err;

	public Log(File outFile) throws FileNotFoundException, UnsupportedEncodingException {
		this(outFile, "UTF-8");
	}

	public Log(File outFile, File errorFile) throws FileNotFoundException, UnsupportedEncodingException {
		this(outFile, errorFile, "UTF-8");
	}

	public Log(File outFile, File errorFile, String cs) throws FileNotFoundException, UnsupportedEncodingException {
		out = new PrintStream(outFile, cs);
		err = new PrintStream(errorFile, cs);
	}

	public Log(File outFile, String cs) throws FileNotFoundException, UnsupportedEncodingException {
		out = new PrintStream(outFile, cs);
		err = out;
	}

	public Log(String outFileName) throws FileNotFoundException, UnsupportedEncodingException {
		this(new File(outFileName));
	}

	public Log(String outFileName, String errorFileName) throws FileNotFoundException, UnsupportedEncodingException {
		this(new File(outFileName), new File(errorFileName));
	}

	public Log(String outFileName, String errorFileName, String cs) throws FileNotFoundException, UnsupportedEncodingException {
		this(new File(outFileName), new File(errorFileName), cs);
	}

	public synchronized void close() {
		PrintStream oldOut = out;
		if (out != null) {
			out.close();
			out = null;
		}

		if (err != null) {
			if (err != oldOut)
				err.close();

			err = null;
		}
	}

	public synchronized boolean isClosed() {
		return out == null && err == null;
	}

	public void logToErr(String message) {
		logToErr(null, message, null);
	}

	public void logToErr(String prefix, String message) {
		logToErr(prefix, message, null);
	}

	public synchronized void logToErr(String prefix, String message, Throwable e) {
		logToErr(err != null ? err : System.err, prefix, message, e);
		if (DebugUtil.DEBUG_MODE && err != null)
			logToErr(System.err, prefix, message, e);
	}

	public void logToErr(String prefix, String[] messages) {
		logToErr(prefix, messages, null);
	}

	public void logToErr(String prefix, String[] messages, Throwable e) {
		logToErr(err != null ? err : System.err, prefix, messages, e);
		if (err != null && DebugUtil.DEBUG_MODE)
			logToErr(System.err, prefix, messages, e);
	}

	public void logToErr(String message, Throwable e) {
		logToErr(null, message, e);
	}

	public void logToErr(Throwable e) {
		logToErr(null, (String) null, e);
	}

	public void logToOut(String message) {
		logToOut(null, message);
	}

	public synchronized void logToOut(String prefix, String message) {
		logToOut(out != null ? out : System.out, prefix, message);
		if (out != null && DebugUtil.DEBUG_MODE)
			logToOut(System.out, prefix, message);
	}

	public synchronized void logToOut(String prefix, String[] messages) {
		logToOut(out != null ? out : System.out, prefix, messages);
		if (out != null && DebugUtil.DEBUG_MODE)
			logToOut(System.out, prefix, messages);
	}

	public void logToOut(String[] messages) {
		logToOut(null, messages);
	}

}
