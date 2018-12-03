package common.util;

public class Line {

	public static boolean squarInLine(int x, int y, int x1, int y1, int x2, int y2) {
		int xMax;
		int xMin;
		if (x1 >= x2) {
			xMax = x1;
			xMin = x2;
		} else {
			xMax = x2;
			xMin = x1;
		}

		int yMax;
		int yMin;
		if (y1 >= y2) {
			yMax = y1;
			yMin = y2;
		} else {
			yMax = y2;
			yMin = y1;
		}

		return xMin <= x && x <= xMax && yMin <= y && y <= yMax && (x2 - x1) * y - (y2 - y1) * x == x2 * y1 - x1 * y2;
	}

	private Square sq1;
	private Square sq2;

	public Line(int x1, int y1, int x2, int y2) {
		sq1 = new Square(x1, y1);
		sq2 = new Square(x2, y2);
	}

	public Line(Square sq1, Square sq2) {
		if (sq1 == null)
			throw new NullPointerException("The first square is null.");
		if (sq2 == null)
			throw new NullPointerException("The first square is null.");
		this.sq1 = sq1;
		this.sq2 = sq2;
	}

	@Override
	public Object clone() {
		return new Line(sq1, sq2);
	}

	public boolean contains(int x, int y) {
		int x1 = sq1.getX();
		int y1 = sq1.getY();
		int x2 = sq2.getX();
		int y2 = sq2.getY();

		return squarInLine(x, y, x1, y1, x2, y2);
	}

	public boolean contains(Square sq) {
		return contains(sq.getX(), sq.getY());
	}

	public boolean equals(int x1, int y1, int x2, int y2) {
		return sq1.equals(x1, y1) && sq2.equals(x2, y2);
	}

	public boolean equals(Line other) {
		return other.sq1.equals(sq1) && other.sq2.equals(sq2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Line other = (Line) obj;
		return equals(other);
	}

	public boolean equals(Square sq1, Square sq2) {
		return this.sq1.equals(sq1) && this.sq2.equals(sq2);
	}

	public Square getSq1() {
		return sq1;
	}

	public Square getSq2() {
		return sq2;
	}

	public int getX1() {
		return sq1.getX();
	}

	public int getX2() {
		return sq2.getX();
	}

	public int getY1() {
		return sq1.getY();
	}

	public int getY2() {
		return sq2.getY();
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (sq1 == null ? 0 : sq1.hashCode());
		result = prime * result + (sq2 == null ? 0 : sq2.hashCode());
		return result;
	}

	public int size() {
		return Square.distance(sq1, sq2);
	}

	@Override
	public String toString() {
		return sq1 + "-" + sq2;
	}

}
