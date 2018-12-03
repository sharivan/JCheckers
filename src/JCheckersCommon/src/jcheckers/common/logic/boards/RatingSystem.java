package jcheckers.common.logic.boards;

import java.util.ArrayList;

public class RatingSystem<T> {

	protected class RatingEntry {

		private T player;
		private float rating;
		private int team;
		private boolean winner;

		protected RatingEntry(T player, float rating, int team) {
			this.player = player;
			this.rating = rating;
			this.team = team;

			winner = false;
		}

		protected T getPlayer() {
			return player;
		}

		protected float getRating() {
			return rating;
		}

		protected int getTeam() {
			return team;
		}

		protected boolean isWinner() {
			return winner;
		}

		protected void setPlayer(T player) {
			this.player = player;
		}

		protected void setRating(float rating) {
			this.rating = rating;
		}

		protected void setTeam(int team) {
			this.team = team;
		}

		protected void setWinner(boolean winner) {
			this.winner = winner;
		}

		@Override
		public String toString() {
			return (player != null ? player + " " : "") + rating;
		}

	}

	public static final float DEFAULT_MIN_RATING = 500;
	public static final float DEFAULT_MAX_RATING = 3000;
	public static final float DEFAULT_MIN_GAIN = 1;
	public static final float DEFAULT_MAX_GAIN = 50;
	public static final float DEFAULT_MIN_LOSS = 1;
	public static final float DEFAULT_MAX_LOSS = 50;

	private static final float WIN0 = 15;
	private static final float LOSS0 = 15;
	private static final float RATING_DIVISOR = 500F;
	private static final float DRAW_DIVISOR = 5;

	private static float cap(float value, float min, float max) {
		if (value < min)
			return min;
		if (value > max)
			return max;

		return value;
	}

	public static void main(String... args) {
		RatingSystem<?> system = new RatingSystem<>();
		system.cleanup();
		system.addRating(2000);
		system.addRating(1000);
		system.setWinner(1);
		system.compute();
		for (int i = 0; i < system.count(); i++)
			System.out.println(system.getOldRating(i) + " --> " + system.getNewRating(i));
	}

	private float minRating = DEFAULT_MIN_RATING;
	private float maxRating = DEFAULT_MAX_RATING;
	private float minGain = DEFAULT_MIN_GAIN;
	private float maxGain = DEFAULT_MAX_GAIN;

	private float minLoss = DEFAULT_MIN_LOSS;
	private float maxLoss = DEFAULT_MAX_LOSS;

	private ArrayList<RatingEntry> oldRatings;

	private ArrayList<Float> newRatings;

	private int winnerTeam = -1;
	private boolean hasWinner = false;

	public RatingSystem() {
		oldRatings = new ArrayList<>();
		newRatings = new ArrayList<>();
	}

	public void addRating(float rating) {
		addRating(null, rating, -1);
	}

	public void addRating(float rating, int team) {
		addRating(null, rating, -1);
	}

	public void addRating(T player, float rating) {
		addRating(player, rating, -1);
	}

	public void addRating(T player, float rating, int team) {
		oldRatings.add(new RatingEntry(player, rating, team));
		newRatings.add(-1F);
	}

	public void cleanup() {
		oldRatings.clear();
		newRatings.clear();
	}

	public void compute() {
		float[] gains = new float[oldRatings.size()];
		if (hasWinner)
			for (int i = 0; i < oldRatings.size(); i++) {
				RatingEntry entry1 = oldRatings.get(i);
				if (!entry1.winner)
					continue;

				for (int j = 0; j < oldRatings.size(); j++) {
					RatingEntry entry2 = oldRatings.get(j);
					if (entry1.equals(entry2))
						continue;
					if (entry2.winner)
						continue;

					float gain = computeWinDiff(entry1.rating, entry2.rating);
					float loss = computeLossDiff(entry1.rating, entry2.rating);

					gains[i] += gain;
					gains[j] -= loss;
					newRatings.set(j, cap(entry2.rating + gains[j], minRating, maxRating));
				}

				newRatings.set(i, cap(entry1.rating + gains[i], minRating, maxRating));
			}
		else
			for (int i = 0; i < oldRatings.size(); i++) {
				RatingEntry entry1 = oldRatings.get(i);
				for (int j = i + 1; j < oldRatings.size(); j++) {
					RatingEntry entry2 = oldRatings.get(j);

					float gain;
					float loss;
					if (entry1.rating >= entry2.rating) {
						gain = computeWinDiff(entry1.rating, entry2.rating) / DRAW_DIVISOR;
						loss = computeLossDiff(entry1.rating, entry2.rating) / DRAW_DIVISOR;
					} else {
						gain = computeWinDiff(entry2.rating, entry1.rating) / DRAW_DIVISOR;
						loss = computeLossDiff(entry2.rating, entry1.rating) / DRAW_DIVISOR;
					}

					gains[i] += cap(gain, minGain, maxGain);
					gains[j] -= cap(loss, minLoss, maxLoss);
					newRatings.set(j, cap(entry2.rating + gains[j], minRating, maxRating));
				}

				newRatings.set(i, cap(entry1.rating + gains[i], minRating, maxRating));
			}
	}

	private float computeLossDiff(float winner, float loser) {
		float delta = Math.abs(winner - loser) / RATING_DIVISOR;

		float B1 = (float) (2F * (maxLoss - LOSS0) / Math.PI);
		float B2 = (float) (2F * (minLoss - LOSS0) / Math.PI);

		float result = winner >= loser ? (float) (LOSS0 + B2 * Math.atan(delta)) : (float) (LOSS0 + B1 * Math.atan(delta));

		return result;
	}

	private float computeWinDiff(float winner, float loser) {
		float delta = Math.abs(winner - loser) / RATING_DIVISOR;

		float B1 = (float) (2F * (maxGain - WIN0) / Math.PI);
		float B2 = (float) (2F * (minGain - WIN0) / Math.PI);

		float result = winner >= loser ? (float) (WIN0 + B2 * Math.atan(delta)) : (float) (WIN0 + B1 * Math.atan(delta));

		return result;
	}

	public int count() {
		return oldRatings.size();
	}

	public float getMaxGain() {
		return maxGain;
	}

	public float getMaxLoss() {
		return maxLoss;
	}

	public float getMaxRating() {
		return maxRating;
	}

	public float getMinGain() {
		return minGain;
	}

	public float getMinLoss() {
		return minLoss;
	}

	public float getMinRating() {
		return minRating;
	}

	public float getNewRating(int index) {
		return newRatings.get(index);
	}

	private float getOldRating(int index) {
		return oldRatings.get(index).rating;
	}

	public T getPlayer(int index) {
		return oldRatings.get(index).player;
	}

	public int getWinnerTeam() {
		return winnerTeam;
	}

	public boolean hasWinner() {
		return hasWinner;
	}

	public void setMaxGain(float maxGain) {
		this.maxGain = maxGain;
	}

	public void setMaxLoss(float maxLoss) {
		this.maxLoss = maxLoss;
	}

	public void setMaxRating(float maxRating) {
		this.maxRating = maxRating;
	}

	public void setMinGain(float minGain) {
		this.minGain = minGain;
	}

	public void setMinLoss(float minLoss) {
		this.minLoss = minLoss;
	}

	public void setMinRating(float minRating) {
		this.minRating = minRating;
	}

	public void setWinner(int index) {
		hasWinner = true;
		oldRatings.get(index).winner = true;
	}

	public void setWinnerTeam(int team) {
		winnerTeam = team;
	}

	@Override
	public String toString() {
		if (oldRatings.size() == 0)
			return "";

		String result = oldRatings.get(0) + " --> " + newRatings.get(0);
		for (int i = 1; i < oldRatings.size(); i++)
			result += ", " + oldRatings.get(i) + " --> " + newRatings.get(i);

		return result;
	}

}
