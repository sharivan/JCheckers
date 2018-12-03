package jcheckers.ui.board;

public enum SquareColor {
	DARK, LIGHT;

	public static SquareColor negate(SquareColor color) {
		switch (color) {
			case DARK:
				return SquareColor.LIGHT;

			case LIGHT:
				return SquareColor.DARK;
		}

		return null;
	}
}
