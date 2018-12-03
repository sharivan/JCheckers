package jcheckers.core;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import common.config.Config;
import common.config.ConfigEntry;
import common.config.MapConfigEntry;
import common.config.SimpleConfigEntry;
import common.io.Log;
import common.process.TimeOutException;
import common.util.DateTimeUtil;
import common.util.Tree;
import jcheckers.server.net.Server;

public class Core {

	private static final String DEFAULT_HOME_DIR = System.getProperty("os.name").toLowerCase().indexOf("windows") != -1 ? "C:\\jcheckers\\" : "/home/jcheckers/";

	private static Core core;

	public static void main(String... args) {
		System.out.println("Starting application...");
		boolean fail = false;
		try {
			core = new Core();
		} catch (Throwable e) {
			fail = true;
			e.printStackTrace();

			return;
		}

		if (!fail)
			fail = !core.openAll();

		if (fail) {
			System.out.println("Failed to start the application.");
			core = null;

			return;
		}

		System.out.println("Application started.");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			terminate();
		}));

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (true) {
				String command = reader.readLine();
				if (command.equals("open all"))
					core.openAll();
				else if (command.equals("close all"))
					core.closeAll(true);
				else if (command.equals("destroy") || command.equals("exit"))
					return;
				else
					System.out.println("Unknow command: " + command);
			}
		} catch (EOFException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			terminate();
		}
	}

	private static void terminate() {
		synchronized (core) {
			if (core.isDestroyed())
				return;

			core.closeAll(true);

			core.destroy();
		}
	}

	private File homeDir;

	private File logDir;
	private Config config;

	private Log log;

	private Map<String, Class<? extends Server>> serverClasses;

	private Map<String, Server> servers;

	private boolean destroyed;

	public Core() throws IOException, ParserConfigurationException, SAXException {
		this(null);
	}

	public Core(Map<String, Class<? extends Server>> serverClasses) throws IOException, ParserConfigurationException, SAXException {
		this.serverClasses = serverClasses != null ? new HashMap<>(serverClasses) : new HashMap<>();

		destroyed = false;

		String s = System.getenv("JCHECKERS_HOME");
		if (s == null)
			s = DEFAULT_HOME_DIR;

		homeDir = new File(s);

		if (!homeDir.exists() && !homeDir.mkdir())
			throw new IOException("Could not create the home directory " + homeDir.getAbsolutePath());

		logDir = new File(homeDir, "logs");
		if (!logDir.exists() && !logDir.mkdir())
			throw new IOException("Could not create the log directory " + logDir.getAbsolutePath());

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		log = new Log(new File(logDir, "core[" + DateTimeUtil.dateToStr_(calendar) + "].txt"));
		log.logToOut("Log started.");

		File configFile = new File(homeDir, "config.xml");
		log.logToOut("Loading config from '" + configFile.getAbsolutePath() + "'...");
		config = parseConfig(configFile);
		parseServerConfigs();

		log.logToOut("Config loaded.");

		servers = new HashMap<>();
	}

	public synchronized void close(String serverName, boolean wait) {
		if (destroyed)
			return;

		closeInternal(serverName, wait);

		System.gc();
	}

	public void closeAll() {
		closeAll(false);
	}

	public synchronized void closeAll(boolean wait) {
		if (destroyed)
			return;

		log.logToOut("Closing all servers...");

		ArrayList<String> serverNames = new ArrayList<>(servers.keySet());
		for (String serverName : serverNames)
			closeInternal(serverName, wait);

		servers.clear();

		System.gc();

		log.logToOut("All servers closed.");
	}

	private void closeInternal(String serverName, boolean wait) {
		Server server = servers.remove(serverName);
		try {
			server.close(wait, true);
		} catch (TimeOutException e) {
			log.logToErr("WARNING: The server " + serverName + " was forced closed.");
		}
		log.logToOut(serverName + " closed.");
	}

	public synchronized boolean destroy() {
		if (destroyed)
			return false;

		try {
			closeAll(true);

			log.logToOut("Log closed.");
			log.close();

			System.gc();

			return true;
		} finally {
			config = null;
			serverClasses = null;
			destroyed = true;
		}
	}

	public Tree<ConfigEntry> getConfig() {
		return config;
	}

	public String getHomeDir() {
		return homeDir.getAbsolutePath();
	}

	public Server getServer(String serverName) {
		return servers.get(serverName);
	}

	public synchronized boolean isDestroyed() {
		return destroyed;
	}

	private void loadTree(NodeList childs, Tree.Node<ConfigEntry> node) {
		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			NamedNodeMap attrs = child.getAttributes();
			Tree.Node<ConfigEntry> node1;
			if (attrs != null) {
				Map<String, String> map = new HashMap<>();
				for (int j = 0; j < attrs.getLength(); j++) {
					Node attr = attrs.item(j);
					map.put(attr.getNodeName(), attr.getNodeValue());
				}
				String value = child.getNodeValue();
				if (value != null)
					map.put("value", value);
				node1 = new Tree.Node<>(new MapConfigEntry(child.getNodeName(), map));
			} else
				node1 = new Tree.Node<>(new SimpleConfigEntry(child.getNodeName(), child.getNodeValue()));
			node.addChild(node1);
			NodeList childs1 = child.getChildNodes();
			loadTree(childs1, node1);
		}
	}

	public synchronized boolean open(String serverName) {
		if (destroyed)
			return false;

		openInternal(serverName);

		return true;
	}

	public synchronized boolean openAll() {
		if (destroyed)
			return false;

		log.logToOut("Opening all servers...");

		Set<String> serverNames = serverClasses.keySet();
		for (String serverName : serverNames)
			openInternal(serverName);

		log.logToOut("All servers is open.");

		return true;
	}

	private void openInternal(String serverName) {
		try {
			File file = new File(homeDir, serverName);
			String serverHome = file.getAbsolutePath();
			if (!file.exists() && !file.mkdir())
				throw new IOException("Could not create the server directory " + serverHome);

			Class<? extends Server> serverClass = serverClasses.get(serverName);
			Server server = serverClass.newInstance();
			server.open(serverHome, config);
			servers.put(serverName, server);

			log.logToOut(serverName + " is open.");
		} catch (ClassNotFoundException | IOException | SQLException | InstantiationException | IllegalAccessException e) {
			log.logToErr(e);
		} catch (InterruptedException e) {
		}
	}

	private Config parseConfig(File configFile) throws ParserConfigurationException, SAXException, IOException {
		if (configFile == null)
			return null;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		Document doc = docBuilder.parse(configFile);
		Tree.Node<ConfigEntry> root = new Tree.Node<>();

		NodeList childs = doc.getChildNodes();
		loadTree(childs, root);

		return new Config(root);
	}

	@SuppressWarnings("unchecked")
	private void parseServerConfigs() {
		ArrayList<ConfigEntry> globalConfig = new ArrayList<>();

		Tree.Node<ConfigEntry> root = config.getChild(0);
		ConfigEntry entry = root.getValue();
		if (!entry.getName().equalsIgnoreCase("config"))
			return;

		for (int j = 0; j < root.getChildCount(); j++) {
			Tree.Node<ConfigEntry> node = root.getChild(j);
			entry = node.getValue();
			if (!entry.getName().equalsIgnoreCase("servers"))
				globalConfig.add(entry);
			else
				for (int k = 0; k < node.getChildCount(); k++) {
					Tree.Node<ConfigEntry> child = node.getChild(k);
					entry = child.getValue();
					if (entry.getName().equalsIgnoreCase("server") && entry instanceof MapConfigEntry) {
						Map<String, String> attrs = ((MapConfigEntry) entry).map();
						try {
							String name = attrs.get("name");
							if (!serverClasses.containsKey(name))
								serverClasses.put(name, (Class<? extends Server>) Class.forName(attrs.get("class")));
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
		}

		config = new Config(globalConfig);
	}

}
