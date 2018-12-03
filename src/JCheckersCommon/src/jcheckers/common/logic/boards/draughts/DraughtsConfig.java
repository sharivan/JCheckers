package jcheckers.common.logic.boards.draughts;

public class DraughtsConfig {

	public static final int WHITE = 0;
	public static final int BLACK = 1;

	// s = size
	// c = initial color
	// m = mirrored
	// ck = can man capture kings
	// bk = has back captures
	// nsbk = man dont start back captures
	// sclw = stop capturing at last row
	// fk = has flying kings
	// mc = has maximum capture priority
	// mcwk = has maximum capture priority with kings
	// mck = has maximum capture priority capturing maximum amount of kings
	// mcksk = has maximum capture priority capturing maximum amount of kings
	// starting capturing a king
	// mbkc = man can become king during capture
	// md = number of moves without capturing or promoting to draw
	//
	// (s, c, m, ck, bk, nsbk, sclw, fk, mc, mcwk, mck, mcksk, mbkc, md)
	public static final DraughtsConfig BRAZILIAN = new DraughtsConfig(8, WHITE, false, true, true, false, false, true, true, false, false, false, false, 40); // brazilian
	public static final DraughtsConfig SPANISH = new DraughtsConfig(8, WHITE, false, true, false, false, true, true, true, false, true, false, false, 40); // spanish
	public static final DraughtsConfig ITALIAN = new DraughtsConfig(8, WHITE, false, false, false, false, true, false, true, true, true, true, false, 50); // italian
	public static final DraughtsConfig RUSSIAN = new DraughtsConfig(8, WHITE, false, true, true, false, false, true, false, false, false, false, true, 40); // russian
	public static final DraughtsConfig INTERNATIONAL = new DraughtsConfig(10, WHITE, false, true, true, false, false, true, true, false, false, false, false, 50); // international
	public static final DraughtsConfig AMERICAN = new DraughtsConfig(8, BLACK, false, true, false, false, true, false, false, false, false, false, false, 50); // american
	public static final DraughtsConfig AMERICAN10x10 = new DraughtsConfig(10, BLACK, false, true, false, false, true, false, false, false, false, false, false, 50); // american
																																										// 10x10

	private int size;
	private int initialColor;
	private boolean isMirrored;
	private boolean canManCaptureKings;
	private boolean canManMakeBackCaptures;
	private boolean manDontStartBackCaptures;
	private boolean hasFlyingKings;
	private boolean hasMaximumCapture;
	private boolean hasMaximumCaptureAmountOfKings;
	private boolean hasMaximumCaptureAmountOfKingsStartingCapturingAKing;
	private boolean hasMaximumCaptureWithKings;
	private boolean canManCanBecomeKingDuringCapture;
	private boolean stopCapturingAtLastRow;
	private int minimalNumberNumberMovesToDraw;

	public DraughtsConfig(int size, int initialColor, boolean isMirrored, boolean canManCaptureKings, boolean canManMakeBackCaptures, boolean manDontStartBackCaptures, boolean stopCapturingAtLastRow,
			boolean hasFlyingKings, boolean hasMaximumCapture, boolean hasMaximumCaptureWithKings, boolean hasMaximumCaptureAmountOfKings, boolean hasMaximumCaptureAmountOfKingsStartingCapturingAKing,
			boolean manCanBecomeKingDuringCapture, int minimalNumberNumberMovesToDraw) {
		this.size = size;
		this.initialColor = initialColor;
		this.isMirrored = isMirrored;
		this.canManCaptureKings = canManCaptureKings;
		this.canManMakeBackCaptures = canManMakeBackCaptures;
		this.manDontStartBackCaptures = manDontStartBackCaptures;
		this.stopCapturingAtLastRow = stopCapturingAtLastRow;
		this.hasFlyingKings = hasFlyingKings;
		this.hasMaximumCapture = hasMaximumCapture;
		this.hasMaximumCaptureWithKings = hasMaximumCaptureWithKings;
		this.hasMaximumCaptureAmountOfKings = hasMaximumCaptureAmountOfKings;
		this.hasMaximumCaptureAmountOfKingsStartingCapturingAKing = hasMaximumCaptureAmountOfKingsStartingCapturingAKing;
		canManCanBecomeKingDuringCapture = manCanBecomeKingDuringCapture;
		this.minimalNumberNumberMovesToDraw = minimalNumberNumberMovesToDraw;
	}

	public boolean canManCanBecomeKingDuringCapture() {
		return canManCanBecomeKingDuringCapture;
	}

	public boolean canManCaptureKings() {
		return canManCaptureKings;
	}

	public boolean canManMakeBackCaptures() {
		return canManMakeBackCaptures;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DraughtsConfig))
			return false;
		DraughtsConfig other = (DraughtsConfig) obj;
		if (canManCanBecomeKingDuringCapture != other.canManCanBecomeKingDuringCapture)
			return false;
		if (canManCaptureKings != other.canManCaptureKings)
			return false;
		if (canManMakeBackCaptures != other.canManMakeBackCaptures)
			return false;
		if (hasFlyingKings != other.hasFlyingKings)
			return false;
		if (hasMaximumCapture != other.hasMaximumCapture)
			return false;
		if (hasMaximumCaptureAmountOfKings != other.hasMaximumCaptureAmountOfKings)
			return false;
		if (initialColor != other.initialColor)
			return false;
		if (isMirrored != other.isMirrored)
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	public int getInitialColor() {
		return initialColor;
	}

	public int getMinimalNumberMovesToDraw() {
		return minimalNumberNumberMovesToDraw;
	}

	public int getSize() {
		return size;
	}

	public boolean hasFlyingKings() {
		return hasFlyingKings;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (canManCanBecomeKingDuringCapture ? 1231 : 1237);
		result = prime * result + (canManCaptureKings ? 1231 : 1237);
		result = prime * result + (canManMakeBackCaptures ? 1231 : 1237);
		result = prime * result + (hasFlyingKings ? 1231 : 1237);
		result = prime * result + (hasMaximumCapture ? 1231 : 1237);
		result = prime * result + (hasMaximumCaptureAmountOfKings ? 1231 : 1237);
		result = prime * result + initialColor;
		result = prime * result + (isMirrored ? 1231 : 1237);
		result = prime * result + size;
		return result;
	}

	public boolean hasMaximumCapture() {
		return hasMaximumCapture;
	}

	public boolean hasMaximumCaptureAmountOfKings() {
		return hasMaximumCaptureAmountOfKings;
	}

	public boolean hasMaximumCaptureAmountOfKingsStartingCapturingAKing() {
		return hasMaximumCaptureAmountOfKingsStartingCapturingAKing;
	}

	public boolean hasMaximumCaptureWithKings() {
		return hasMaximumCaptureWithKings;
	}

	public boolean isMirrored() {
		return isMirrored;
	}

	public boolean manDontStartBackCaptures() {
		return manDontStartBackCaptures;
	}

	public void setCanManCanBecomeKingDuringCapture(boolean canManCanBecomeKingDuringCapture) {
		this.canManCanBecomeKingDuringCapture = canManCanBecomeKingDuringCapture;
	}

	public void setCanManCaptureKings(boolean canManCaptureKings) {
		this.canManCaptureKings = canManCaptureKings;
	}

	public void setCanManMakeBackCaptures(boolean canManMakeBackCaptures) {
		this.canManMakeBackCaptures = canManMakeBackCaptures;
	}

	public void setHasFlyingKings(boolean hasFlyingKings) {
		this.hasFlyingKings = hasFlyingKings;
	}

	public void setHasMaximumCapture(boolean hasMaximumCapture) {
		this.hasMaximumCapture = hasMaximumCapture;
	}

	public void setHasQualityCapture(boolean hasQualityCapture) {
		hasMaximumCaptureAmountOfKings = hasQualityCapture;
	}

	public void setInitialTurn(int initialTurn) {
		initialColor = initialTurn;
	}

	public void setMirrored(boolean isMirrored) {
		this.isMirrored = isMirrored;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setStopCapturingAtLastRow(boolean stopCapturingAtLastRow) {
		this.stopCapturingAtLastRow = stopCapturingAtLastRow;
	}

	public boolean stopCapturingAtLastRow() {
		return stopCapturingAtLastRow;
	}

}
