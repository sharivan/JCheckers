package jcheckers.client.net.boards;

public class RatingChange {

	private int sitIndex;
	private String name;
	private int gain;
	private int rating;

	protected RatingChange(int sitIndex, String name, int gain, int rating) {
		this.sitIndex = sitIndex;
		this.name = name;
		this.gain = gain;
		this.rating = rating;
	}

	public int getGain() {
		return gain;
	}

	public String getName() {
		return name;
	}

	public int getRating() {
		return rating;
	}

	public int getSitIndex() {
		return sitIndex;
	}

}
