package jcheckers.client.net;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CoreConnection {

	private URL url;

	public CoreConnection(URL url) {
		this.url = url;
	}

	public URL getURL() {
		return url;
	}

	public int login(String game, String username, String password, String[] sid, String[] host, int[] port, List<RoomInfo> rooms) throws IOException {
		HttpURLConnection connection = null;
		PrintStream ps = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

			ps = new PrintStream(connection.getOutputStream());
			ps.print("action=login&game=" + game + "&username=" + username + "&password=" + password);

			JSONObject json = new JSONObject(new JSONTokener(connection.getInputStream()));

			String response = json.getString("response");
			if (response == null)
				throw new IOException("Missing response.");

			if (response.equals("fail")) {
				String message = json.getString("message");
				throw new IOException(message);
			}

			if (!response.equals("ok"))
				throw new IOException("Unknow response '" + response + "'.");

			int result = json.getInt("result");
			if (result == -1)
				throw new IOException("Unexpected response from server.");

			if (result == 0) {
				sid[0] = json.getString("sid");
				host[0] = json.getString("host");
				port[0] = json.getInt("port");

				JSONArray jsonRooms = json.getJSONArray("rooms");
				for (Object roomObj : jsonRooms) {
					JSONObject roomInfo = (JSONObject) roomObj;

					int id = roomInfo.getInt("id");
					String name = roomInfo.getString("name");
					int users = roomInfo.getInt("users");
					int maxUsers = roomInfo.getInt("maxUsers");
					int tables = roomInfo.getInt("tables");
					boolean withAdmin = roomInfo.getBoolean("withAdmin");

					rooms.add(new RoomInfo(id, name, users, maxUsers, tables, withAdmin));
				}
			}

			return result;
		} finally {
			if (ps != null)
				ps.close();

			if (connection != null)
				connection.disconnect();
		}
	}

	public int register(String game, String username, String password, String email) throws IOException {
		HttpURLConnection connection = null;
		PrintStream ps = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

			ps = new PrintStream(connection.getOutputStream());
			ps.print("action=register&game=" + game + "&username=" + username + "&password=" + password + "&email=" + email);

			JSONObject json = new JSONObject(new JSONTokener(connection.getInputStream()));

			String response = json.getString("response");
			if (response == null)
				throw new IOException("Missing response.");

			if (response.equals("fail")) {
				String message = json.getString("message");
				throw new IOException(message);
			}

			if (!response.equals("ok"))
				throw new IOException("Unknow response '" + response + "'.");

			int result = json.getInt("result");
			if (result == -1)
				throw new IOException("Unexpected response from server.");

			return result;
		} finally {
			if (ps != null)
				ps.close();

			if (connection != null)
				connection.disconnect();
		}
	}

	public void setURL(URL url) {
		this.url = url;
	}

}
