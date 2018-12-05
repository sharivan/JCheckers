package jcheckers.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;

/**
 * 
 * Implementação da lista de imagens com a funcionalidade de carrega-las por meio de um resource path.
 * @author miste
 *
 */
public class ResourceImageList implements ImageList {

	private static final boolean DEBUG = false;
	private static final String DEBUG_PATH = System.getProperty("user.home") + File.separator + System.getProperty("user.home") + "list.txt";

	private String path;

	private Hashtable<String, Image> images;

	public ResourceImageList() {
		this("images");
	}

	public ResourceImageList(String path) {
		this.path = path;

		images = new Hashtable<>();
	}

	@Override
	public void destroy() {
		Collection<Image> values = images.values();
		for (Image image : values)
			image.flush();

		values.clear();
	}

	@Override
	public Image getImage(String name) {
		return images.get(name);
	}

	/**
	 * List directory contents for a resource folder. Not recursive. This is
	 * basically a brute-force implementation. Works for regular files and also
	 * JARs.
	 * 
	 * @author Greg Briggs
	 * @param clazz
	 *            Any java class that lives in the same place as the resources
	 *            you want.
	 * @param path
	 *            Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	HashMap<String, Image> getImageList(Class<ResourceImageList> clazz, String path) throws URISyntaxException, IOException {
		HashMap<String, Image> map = new HashMap<>();

		path = path.replace('.', '/');
		if (!path.endsWith("/"))
			path += "/";

		URL dirURL = clazz.getClassLoader().getResource(path);
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			/* A file path: easy enough */
			File[] list = new File(dirURL.toURI()).listFiles();
			for (File file : list) {
				Image image = Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());
				map.put(file.getName(), image);
			}
			return map;
		}

		if (dirURL == null) {
			/*
			 * In case of a jar file, we can't actually find a directory. Have
			 * to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}

		if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // strip
																							// out
																							// only
																							// the
																							// JAR
																							// file
			@SuppressWarnings("resource")
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries
															// in jar
			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();
				String name = jarEntry.getName();
				if (name.startsWith(path)) { // filter according to the path
					String entry = name.substring(path.length());
					int checkSubdir = entry.indexOf("/");
					if (checkSubdir >= 0)
						// if it is a subdirectory, we just return the directory
						// name
						entry = entry.substring(0, checkSubdir);
					BufferedImage image = ImageIO.read(jar.getInputStream(jarEntry));
					map.put(entry, image);
				}
			}

			return map;
		}

		throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
	}

	@Override
	public void init() {
		PrintStream ps = null;
		try {
			if (DEBUG)
				try {
					ps = new PrintStream(DEBUG_PATH);
				} catch (FileNotFoundException e) {
					ps = System.out;
				}

			HashMap<String, Image> map = getImageList(ResourceImageList.class, path);
			Set<String> list = map.keySet();

			for (String name : list) {
				if (DEBUG)
					ps.println(name);
				Image image = map.get(name);
				if (image != null)
					images.put(name, image);
			}
		} catch (Throwable e) {
			if (DEBUG)
				e.printStackTrace(ps);
			else
				e.printStackTrace();
		} finally {
			if (ps != null && ps != System.out)
				ps.close();
		}
	}

}
