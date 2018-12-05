package jcheckers.ui;

import java.awt.Image;

/**
 * 
 * Interface para uma lista de imagens.
 * @author miste
 *
 */
public interface ImageList {

	public void destroy();

	public Image getImage(String name);

	public void init();

}
