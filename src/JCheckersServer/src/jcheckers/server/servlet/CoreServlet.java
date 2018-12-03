package jcheckers.server.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import jcheckers.core.Core;
import jcheckers.server.net.Room;
import jcheckers.server.net.Server;

@WebServlet(name = "CoreServlet", urlPatterns = { "/CoreServlet" })
public class CoreServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6607755816343685009L;

	@Override
	public void destroy() {

	}

	@Override
	public void init() {

	}

	@Override
	protected final void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json");

		JSONWriter out = new JSONWriter(response.getWriter()).object();

		Core core = (Core) getServletContext().getAttribute("core");
		if (core == null) {
			out.key("response").value("fail");
			out.key("message").value("Core is offline.");
			return;
		}

		String action = request.getParameter("action");
		if (action == null) {
			out.key("response").value("fail");
			out.key("message").value("Missing action.");
			return;
		}

		try {
			switch (action) {
				case "register": {
					String game = request.getParameter("game");
					String username = request.getParameter("username");
					String password = request.getParameter("password");
					String email = request.getParameter("email");

					Server server = core.getServer(game);
					if (server == null) {
						out.key("response").value("fail");
						out.key("message").value("Server doesn't exist or is offline.");
						return;
					}

					String[] errorMessage = new String[1];
					int result = server.register(username, password, email, errorMessage);
					if (result == -1)
						throw new IOException(errorMessage[0]);

					out.key("response").value("ok");
					out.key("result").value(result);
					break;
				}

				case "login": {
					String game = request.getParameter("game");
					String username = request.getParameter("username");
					String password = request.getParameter("password");
					String ip = request.getRemoteAddr();

					Server server = core.getServer(game);
					if (server == null) {
						out.key("response").value("fail");
						out.key("message").value("Server doesn't exist or is offline.");
						return;
					}

					String[] sid = new String[1];
					int result = server.login(game, username, password, ip, sid);
					if (result == -1)
						throw new IOException(sid[0]);

					out.key("response").value("ok");
					out.key("result").value(result);
					if (result == 0) {
						out.key("sid").value(sid[0]);
						out.key("host").value(server.getHost());
						out.key("port").value(server.getPort());

						out.key("rooms").array();
						List<Room> rooms = server.rooms();
						for (Room room : rooms) {
							out.object();
							out.key("id").value(room.getID());
							out.key("name").value(room.getName());
							out.key("users").value(room.userCount());
							out.key("maxUsers").value(room.getMaxPlayers());
							out.key("tables").value(room.tableCount());
							out.key("withAdmin").value(room.isWithAdmin());
							out.endObject();
						}

						out.endArray();
					}

					break;
				}

				case "logout": {
					String game = request.getParameter("game");
					String username = request.getParameter("username");
					String sid = request.getParameter("sid");

					Server server = core.getServer(game);
					if (server == null) {
						out.key("response").value("fail");
						out.key("message").value("Server doesn't exist or is offline.");
						return;
					}

					server.logout(game, username, sid);
					out.key("response").value("ok");
					break;
				}

				case "update": {
					break;
				}

				default:
					out.key("response").value("fail");
					out.key("message").value("Invalid action: " + action);
			}
		} catch (Throwable e) {
			out.key("response").value("fail");
			out.key("message").value("Internal error: " + e.getMessage());
		}

		out.endObject();
	}

}
