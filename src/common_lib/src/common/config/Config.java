package common.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import common.util.Tree;

public class Config extends Tree<ConfigEntry> {

	private static void loadTree(NodeList childs, Tree.Node<ConfigEntry> node) {
		for (int i = 0; i < childs.getLength(); i++) {
			org.w3c.dom.Node child = childs.item(i);
			NamedNodeMap attrs = child.getAttributes();
			Tree.Node<ConfigEntry> node1;
			if (attrs != null) {
				Map<String, String> map = new HashMap<>();
				for (int j = 0; j < attrs.getLength(); j++) {
					org.w3c.dom.Node attr = attrs.item(j);
					map.put(attr.getNodeName(), attr.getNodeValue());
				}
				String value = child.getNodeValue();
				if (value != null)
					map.put("value", value);
				node1 = new Tree.Node<ConfigEntry>(new MapConfigEntry(child.getNodeName(), map));
			} else
				node1 = new Tree.Node<ConfigEntry>(new SimpleConfigEntry(child.getNodeName(), child.getNodeValue()));
			node.addChild(node1);
			NodeList childs1 = child.getChildNodes();
			loadTree(childs1, node1);
		}
	}

	public static Config parseConfig(File configFile) throws ParserConfigurationException, SAXException, IOException {
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

	public Config() {
		super();
	}

	public Config(ConfigEntry rootValue) {
		super(rootValue);
	}

	public Config(List<ConfigEntry> entries) {
		super();

		Tree.Node<ConfigEntry> root = getRoot();

		for (ConfigEntry entry : entries)
			root.addChild(new Node<>(entry));
	}

	public Config(Tree.Node<ConfigEntry> root) {
		super(root);
	}

}
