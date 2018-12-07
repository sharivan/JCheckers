package jcheckers.ui.game;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	
	public static enum DrawMode {
		NONE,
		CENTER,
		STRETCH,
		TILE
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 593028601414193973L;
	
	private Image image;
	private DrawMode drawMode = DrawMode.NONE;
	
	public ImagePanel() {
		this(null);
	}

	/**
	 * Create the panel.
	 */
	public ImagePanel(Image image) {
		this.image = image;
		
		setLayout(null);
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
		repaint();
	}
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (image == null)
        	return;
        
        switch (drawMode) {
        	case NONE: {
        		g.drawImage(image, 0, 0, this);    
        		break;
        	}
        		
        	case CENTER: {
        		int width = getWidth();  
                int height = getHeight();
                int imgWidth = image.getWidth(this);
                int imgHeight = image.getHeight(this);
                int imgX = (width - imgWidth) / 2;
                int imgY = (height - imgHeight) / 2;
                
                g.drawImage(image, imgX, imgY, this); 
        		break;
        	}
        		
        	case STRETCH: {
        		int width = getWidth();  
                int height = getHeight(); 
                
        		g.drawImage(image, 0, 0, width, height, this);  
        		break;
        	}
        		
        	case TILE: {
        		int width = getWidth();  
                int height = getHeight();
                int imgWidth = image.getWidth(this);
                int imgHeight = image.getHeight(this);
                
        		for (int x = 0; x < width; x += imgWidth)
                    for (int y = 0; y < height; y += imgHeight)
                        g.drawImage(image, x, y, this);
        		
        		break;
        	}
        }        
    }

	public DrawMode getDrawMode() {
		return drawMode;
	}

	public void setDrawMode(DrawMode drawMode) {
		this.drawMode = drawMode;
		repaint();
	}

}
