/**
 * 
 */
package jcheckers.core;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

/**
 * @author Saddam Hussein
 * 
 */
public class Status {

	public static final void dumpThreadStack(JspWriter out) throws IOException {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		while (true) {
			ThreadGroup parent = group.getParent();
			if (parent == null)
				break;
			group = parent;
		}
		out.println("<ul>");
		dumpThreadStack(group, out);
		out.println("</ul>");
	}

	private static final void dumpThreadStack(ThreadGroup group, JspWriter out) throws IOException {
		out.println("<li><strong>" + group.getName() + "</strong></li>");
		Thread[] threads = new Thread[group.activeCount()];
		int count = group.enumerate(threads, false);
		if (count > 0) {
			out.println("<ul>");
			for (int i = 0; i < count; i++) {
				Thread thread = threads[i];
				out.println("<li>Thread[" + thread.getName() + "][" + thread.getState() + "]</li>");
				StackTraceElement[] stackTrace = thread.getStackTrace();
				out.println("<ul>");
				for (StackTraceElement element : stackTrace)
					out.println("<li>" + element + "</li>");
				out.println("</ul>");
			}
			out.println("</ul");
		}
		ThreadGroup[] groups = new ThreadGroup[group.activeGroupCount()];
		count = group.enumerate(groups, false);
		if (count > 0) {
			out.println("<ul>");
			for (int i = 0; i < count; i++)
				dumpThreadStack(groups[i], out);
			out.println("</ul>");
		}
	}

}
