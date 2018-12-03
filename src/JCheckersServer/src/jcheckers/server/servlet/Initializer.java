package jcheckers.server.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import jcheckers.core.Core;

@WebServlet(name = "Initializer", urlPatterns = { "/Initializer" }, loadOnStartup = 0)
public class Initializer extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5414740381509535178L;

	private Core core;

	@Override
	public void destroy() {
		if (core == null)
			return;

		try {
			core.destroy();
			getServletContext().removeAttribute("core");
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			core = null;
		}
	}

	@Override
	public void init() {
		try {
			core = new Core();
			if (core.openAll())
				getServletContext().setAttribute("core", core);
			else
				try {
					core.destroy();
				} finally {
					core = null;
				}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
