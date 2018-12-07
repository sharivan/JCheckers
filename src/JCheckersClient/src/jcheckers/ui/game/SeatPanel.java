package jcheckers.ui.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import common.util.FormatterUtil;
import jcheckers.ui.ImageList;
import jcheckers.ui.chat.ChatPanel;
import jcheckers.ui.game.ImagePanel.DrawMode;

public class SeatPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6658451027227116122L;
	
	private static final Color[] seatColors = {Color.WHITE, Color.RED};
	private static final String[] imageNames = {"wm.png", "bm.png"};
	private static final String SEAT_AVAILABLE_TEXT = "⚫  Sentar ⚫";
	
	private ImageList imageList;
	private int seatIndex = -1;
	private String player;
	private int playerTime;
	private int timePerMove;
	private boolean canSeat = true;
	private boolean active;
	private boolean hasPlayerTime;
	private boolean hasTimePerMove;
	
	private ArrayList<SeatListener> listeners;
	private boolean mouseEntered;
	private long lastPlayerTimeTick = 0;
	private long lastTimePerMoveTick = 0;

	private ImagePanel pnlImage;
	private JLabel lblPlayer;
	private JLabel lblTimePerMove;
	private JLabel lblPlayerTime;
	private JSeparator separator_0;
	private JSeparator separator_1;
	private JSeparator separator_2;

	private Timer tmrPlayerTime;
	private Timer tmrTimePerMove;
	
	public SeatPanel() {
		this(null, -1);
	}

	/**
	 * Create the panel.
	 */
	public SeatPanel(ImageList imageList, int seatIndex) {
		this.imageList = imageList;
		this.seatIndex = seatIndex;
		
		listeners = new ArrayList<>();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{32, 0, 0, -18, 40, 0, 40, 0};
		gridBagLayout.rowHeights = new int[]{32, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		setOpaque(true);
		setBackground(ChatPanel.CHAT_BACKGROUND_COLOR);
		
		pnlImage = new ImagePanel();
		pnlImage.setDrawMode(DrawMode.STRETCH);
		pnlImage.setOpaque(true);
		pnlImage.setBackground(ChatPanel.CHAT_BACKGROUND_COLOR);
		GridBagConstraints gbc_pnlImage = new GridBagConstraints();
		gbc_pnlImage.insets = new Insets(0, 0, 0, 0);
		gbc_pnlImage.fill = GridBagConstraints.BOTH;
		gbc_pnlImage.gridx = 0;
		gbc_pnlImage.gridy = 0;
		add(pnlImage, gbc_pnlImage);
		
		separator_0 = new JSeparator();
		separator_0.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_separator_0 = new GridBagConstraints();
		gbc_separator_0.insets = new Insets(0, 0, 0, 0);
		gbc_separator_0.gridx = 1;
		gbc_separator_0.gridy = 0;
		add(separator_0, gbc_separator_0);
		
		lblPlayer = new JLabel(SEAT_AVAILABLE_TEXT);
		lblPlayer.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		lblPlayer.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {

			}
			
			@Override
			public void mousePressed(MouseEvent e) {

			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				mouseEntered = false;
				lblPlayer.setFont(getPlayerFont());
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				mouseEntered = true;
				lblPlayer.setFont(getPlayerFont());
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (canSeat())
					for (SeatListener listener: listeners)
						if (listener != null)
							listener.onSit(seatIndex);
			}
		});
		
		if (seatIndex != -1)
			lblPlayer.setForeground(seatColors[seatIndex]);
		else
			lblPlayer.setForeground(Color.black);
		
		lblPlayer.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblPlayer = new GridBagConstraints();
		gbc_lblPlayer.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPlayer.insets = new Insets(0, 0, 0, 0);
		gbc_lblPlayer.gridx = 2;
		gbc_lblPlayer.gridy = 0;
		add(lblPlayer, gbc_lblPlayer);
		
		separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.insets = new Insets(0, 0, 0, 0);
		gbc_separator_1.gridx = 3;
		gbc_separator_1.gridy = 0;
		add(separator_1, gbc_separator_1);
		
		lblTimePerMove = new JLabel("");
		lblTimePerMove.setForeground(Color.WHITE);
		lblTimePerMove.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblTimePerMove = new GridBagConstraints();
		gbc_lblTimePerMove.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTimePerMove.insets = new Insets(0, 0, 0, 0);
		gbc_lblTimePerMove.gridx = 4;
		gbc_lblTimePerMove.gridy = 0;
		add(lblTimePerMove, gbc_lblTimePerMove);
		
		separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_separator_2 = new GridBagConstraints();
		gbc_separator_2.insets = new Insets(0, 0, 0, 0);
		gbc_separator_2.gridx = 5;
		gbc_separator_2.gridy = 0;
		add(separator_2, gbc_separator_2);
		
		lblPlayerTime = new JLabel("");
		lblPlayerTime.setForeground(Color.WHITE);
		lblPlayerTime.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblPlayerTime = new GridBagConstraints();
		gbc_lblPlayerTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPlayerTime.gridx = 6;
		gbc_lblPlayerTime.gridy = 0;
		add(lblPlayerTime, gbc_lblPlayerTime);
		
		addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e){
            	Dimension size = new Dimension(getHeight(), getHeight());
            	pnlImage.setSize(size);
            	pnlImage.setPreferredSize(size);
            }
        });
		
		tmrPlayerTime = new Timer(1000, (e) -> handlePlayerTimer());
		tmrTimePerMove = new Timer(1000, (e) -> handleTimePerMoveTimer());
	}

	private void handlePlayerTimer() {
		long currentTime = System.currentTimeMillis();
		int delta = (int) (currentTime - lastPlayerTimeTick);
		playerTime -= delta;
		
		if (playerTime < 0)
			playerTime = 0;
		
		lblPlayerTime.setText(timeToString(playerTime / 1000));
		lastPlayerTimeTick = currentTime;
	}
	
	private void handleTimePerMoveTimer() {
		long currentTime = System.currentTimeMillis();
		int delta = (int) (currentTime - lastTimePerMoveTick);
		timePerMove -= delta;
		
		if (timePerMove < 0)
			timePerMove = 0;
		
		lblTimePerMove.setText(timeToString(timePerMove / 1000));
		lastTimePerMoveTick = currentTime;
	}

	private Font getPlayerFont() {
		if (canSeat() && mouseEntered || active)
			return new Font(Font.SANS_SERIF, Font.BOLD, 11);

		return new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	}
	
	public String getPlayer() {
		return player;
	}
	
	public void setPlayer(String player) {
		this.player = player;
		lblPlayer.setFont(getPlayerFont());
		lblPlayer.setText(player != null ? player : (canSeat ? SEAT_AVAILABLE_TEXT : ""));
	}
	
	public boolean canSeat() {
		return canSeat && player == null;
	}
	
	public void setCanSeat(boolean value) {
		canSeat = value;
		lblPlayer.setFont(getPlayerFont());
		lblPlayer.setText(player != null ? player : (canSeat ? SEAT_AVAILABLE_TEXT : ""));
	}

	public ImageList getImageList() {
		return imageList;
	}

	public void setImageList(ImageList imageList) {
		this.imageList = imageList;
		
		if (active && imageList != null && seatIndex != -1) {
			pnlImage.setImage(imageList.getImage(imageNames[seatIndex]));
		} else
			pnlImage.setImage(null);
	}

	public int getSeatIndex() {
		return seatIndex;
	}

	public void setSeatIndex(int seatIndex) {
		this.seatIndex = seatIndex;
		
		if (seatIndex != -1) {
			lblPlayer.setForeground(seatColors[seatIndex]);
			
			if (active && imageList != null)
				pnlImage.setImage(imageList.getImage(imageNames[seatIndex]));
			else
				pnlImage.setImage(null);
		} else {
			lblPlayer.setForeground(Color.BLACK);
			pnlImage.setImage(null);
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean value) {
		this.active = value;
		
		long currentTime = System.currentTimeMillis();
		lastPlayerTimeTick = currentTime;
		lastTimePerMoveTick = currentTime;
		
		if (active) {
			if (hasPlayerTime)
				tmrPlayerTime.restart();
			else
				tmrPlayerTime.stop();
			
			if (hasTimePerMove)
				tmrTimePerMove.restart();
			else
				tmrTimePerMove.stop();
		} else {
			tmrPlayerTime.stop();
			tmrTimePerMove.stop();
		}
		
		lblPlayer.setFont(getPlayerFont());
		
		if (seatIndex != -1) {
			if (value && imageList != null)
				pnlImage.setImage(imageList.getImage(imageNames[seatIndex]));
			else
				pnlImage.setImage(null);
		} else
			pnlImage.setImage(null);
	}
	
	public void addListener(SeatListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(SeatListener listener) {
		listeners.remove(listener);
	}
	
	public void setPlayerTime(int time) {
		this.playerTime = time;
		
		if (hasPlayerTime)
			lblPlayerTime.setText(timeToString(time / 1000));
	}

	private static String timeToString(int time) {
		int minutes = time / 60;
		int seconds = time % 60;
		return FormatterUtil.formatDigits(minutes, 2) + ":" + FormatterUtil.formatDigits(seconds, 2);
	}

	public boolean hasPlayerTime() {
		return hasPlayerTime;
	}

	public void setHasPlayerTime(boolean value) {
		this.hasPlayerTime = value;
		
		if (hasPlayerTime) {
			if (active)
				tmrPlayerTime.restart();
			else
				tmrPlayerTime.stop();
		} else {
			tmrPlayerTime.stop();
			lblPlayerTime.setText("");
		}
	}

	public boolean hasTimePerMove() {
		return hasTimePerMove;
	}

	public void setHasTimePerMove(boolean value) {
		this.hasTimePerMove = value;
		
		if (hasTimePerMove) {
			if (active)
				tmrTimePerMove.restart();
			else
				tmrTimePerMove.stop();
		} else {
			tmrTimePerMove.stop();
			lblTimePerMove.setText("");
		}
	}

	public int getTimePerMove() {
		return timePerMove;
	}

	public void setTimePerMove(int time) {
		this.timePerMove = time;
		
		if (hasTimePerMove)
			lblTimePerMove.setText(timeToString(time / 1000));
	}

	public int getPlayerTime() {
		return playerTime;
	}

}
