// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) deadcode fieldsfirst

package common.util;

public class Square {

	public static int distance(int x1, int y1, int x2, int y2) {
		return Math.max(x2 - x1, y2 - y1);
	}

	public static int distance(Square sq1, Square sq2) {
		return distance(sq1.getX(), sq1.getY(), sq2.getX(), sq2.getY());
	}

	private int x;
	private int y;

	public Square(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Object clone() {
		return new Square(x, y);
	}

	public boolean equals(int x, int y) {
		return x == this.x && y == this.y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Square other = (Square) obj;
		return equals(other);
	}

	public boolean equals(Square sq) {
		return sq.x == x && sq.y == y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public int hashCode() {
		return x * 0x10000 + y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

}
