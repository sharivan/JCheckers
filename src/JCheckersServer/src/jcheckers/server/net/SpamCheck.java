package jcheckers.server.net;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class SpamCheck {

	private int maxMessages;
	private int interval;
	private SpamAction action;

	private long lastCheck;
	private int messages;
	private boolean blocked;
	private long blockedWhen;
	private int blockedTime;

	public SpamCheck(int maxMessages, int interval, SpamAction action) {
		this.maxMessages = maxMessages;
		this.interval = interval;
		this.action = action;

		lastCheck = -1;
		messages = 0;
		blocked = false;
	}

	public void block() {
		block(0);
	}

	public void block(int time) {
		blockedWhen = System.currentTimeMillis();
		blockedTime = time;
		blocked = true;
	}

	public boolean check() {
		if (isBlocked())
			return true;

		long time = System.currentTimeMillis();
		messages++;

		if (lastCheck == -1) {
			lastCheck = time;
			return true;
		}

		int delta = (int) (time - lastCheck);
		if (messages >= maxMessages || delta >= interval) {
			int messages1 = messages;
			messages = 0;
			lastCheck = time;
			if ((float) messages1 / (float) delta > (float) maxMessages / (float) interval) {
				if (action != null)
					action.onSpamDetected(this);

				return false;
			}
		}

		return true;
	}

	public SpamAction getAction() {
		return action;
	}

	public int getInterval() {
		return interval;
	}

	public int getMaxMessages() {
		return maxMessages;
	}

	public boolean isBlocked() {
		if (!blocked)
			return false;

		if (blockedTime == 0)
			return true;

		long now = System.currentTimeMillis();
		if (now >= blockedWhen + blockedTime) {
			blocked = false;
			return false;
		}

		return true;
	}

	public void setAction(SpamAction action) {
		this.action = action;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void setMaxMessages(int maxMessages) {
		this.maxMessages = maxMessages;
	}

	public boolean toggleBlock() {
		blocked = !blocked;
		return blocked;
	}

	@Override
	public String toString() {
		return maxMessages + "/" + interval;
	}

	public void unblock() {
		blocked = false;
	}

}
