package common.util;

import java.util.Collection;
import java.util.Vector;

public class Polygonal {

	private Vector<Square> squares;

	public Polygonal() {
		squares = new Vector<>();
	}

	public Polygonal(Collection<? extends Square> squares) {
		this.squares = new Vector<>(squares);
	}

	public Polygonal(Line line) {
		squares = new Vector<>();

		addSquares(line);
	}

	public Polygonal(Polygonal p) {
		squares = new Vector<>();

		addSquares(p);
	}

	public Polygonal(Square[] squares) {
		this.squares = new Vector<>();

		addSquares(squares);
	}

	public boolean addSquare(int x, int y) {
		return addSquare(new Square(x, y));
	}

	public boolean addSquare(Square sq) {
		if (sq == null)
			throw new NullPointerException("The square is null.");
		if (squares.size() > 0 && squares.lastElement().equals(sq))
			return false;
		squares.add(sq);
		return true;
	}

	public int addSquares(Collection<? extends Square> squares) {
		int result = 0;
		for (Square sq : squares)
			if (addSquare(sq))
				result++;
		return result;
	}

	public int addSquares(Line line) {
		int result = 0;
		if (addSquare(line.getSq1()))
			result++;
		if (addSquare(line.getSq2()))
			result++;
		return result;
	}

	public int addSquares(Polygonal p) {
		int result = 0;
		for (int i = 0; i < p.size(); i++)
			if (addSquare(p.getSquare(i)))
				result++;
		return result;
	}

	public int addSquares(Square[] squares) {
		int result = 0;
		for (int i = 0; i < squares.length; i++)
			if (addSquare(squares[i]))
				result++;
		return result;
	}

	public void clear() {
		squares.clear();
	}

	@Override
	public Object clone() {
		return new Polygonal(squares);
	}

	public boolean contains(int x, int y) {
		if (squares.size() == 0)
			return false;
		if (squares.size() == 1)
			return squares.get(0).equals(x, y);
		for (int i = 1; i < squares.size(); i++) {
			Square sq1 = squares.get(i - 1);
			Square sq2 = squares.get(i);

			int x1 = sq1.getX();
			int y1 = sq1.getY();
			int x2 = sq2.getX();
			int y2 = sq2.getY();

			if (Line.squarInLine(x, y, x1, y1, x2, y2))
				return true;
		}
		return false;
	}

	public boolean contains(Square sq) {
		return contains(sq.getX(), sq.getY());
	}

	public boolean containsSquare(Square sq) {
		return squares.contains(sq);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Polygonal other = (Polygonal) obj;
		if (squares == null) {
			if (other.squares != null)
				return false;
		} else if (!squares.equals(other.squares))
			return false;
		return true;
	}

	public Square first() {
		return squares.firstElement();
	}

	public Square getSquare(int index) {
		return squares.get(index);
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (squares == null ? 0 : squares.hashCode());
		return result;
	}

	public int indexOf(Collection<? extends Square> squares) {
		int index = -1;
		loop: while (true) {
			int i = 0;
			for (Square sq : squares) {
				if (i == 0) {
					index = this.squares.indexOf(sq, index + 1);
					if (index == -1)
						return -1;
					if (index == this.squares.size() - 1)
						return -1;
				} else if (!this.squares.get(index + i).equals(sq))
					continue loop;
				i++;
			}
		}
	}

	public int indexOf(Line line) {
		int index = -1;
		while (true) {
			index = squares.indexOf(line.getSq1(), index + 1);
			if (index == -1)
				return -1;
			if (index == squares.size() - 1)
				return -1;
			if (squares.get(index + 1).equals(line.getSq2()))
				return index;
		}
	}

	public int indexOf(Polygonal p) {
		int index = -1;
		loop: while (true) {
			index = squares.indexOf(p.getSquare(0), index + 1);
			if (index == -1)
				return -1;
			if (index == squares.size() - 1)
				return -1;
			for (int i = 1; i < p.size(); i++)
				if (!squares.get(index + i).equals(p.getSquare(i)))
					continue loop;
		}
	}

	public int indexOf(Square sq) {
		return squares.indexOf(sq);
	}

	public int indexOf(Square[] squares) {
		int index = -1;
		loop: while (true) {
			index = this.squares.indexOf(squares[0], index + 1);
			if (index == -1)
				return -1;
			if (index == this.squares.size() - 1)
				return -1;
			for (int i = 1; i < squares.length; i++)
				if (!this.squares.get(index + i).equals(squares[i]))
					continue loop;
		}
	}

	public Line intercept(int x, int y) {
		if (squares.size() < 2)
			return null;
		for (int i = 1; i < squares.size(); i++) {
			Square sq1 = squares.get(i - 1);
			Square sq2 = squares.get(i);

			int x1 = sq1.getX();
			int y1 = sq1.getY();
			int x2 = sq2.getX();
			int y2 = sq2.getY();

			if (Line.squarInLine(x, y, x1, y1, x2, y2))
				return new Line(x1, y1, x2, y2);
		}
		return null;
	}

	public boolean isPolygon() {
		return squares.size() > 0 && squares.firstElement().equals(squares.lastElement());
	}

	public Square last() {
		return squares.lastElement();
	}

	public int length() {
		if (squares.size() < 2)
			return 0;

		int result = 0;
		for (int i = 1; i < squares.size(); i++) {
			Square sq1 = squares.get(i - 1);
			Square sq2 = squares.get(i);
			result += Square.distance(sq1, sq2);
		}

		return result;
	}

	public boolean removeSquare(Square sq) {
		return squares.remove(sq);
	}

	public void replace(Collection<? extends Square> oldSqs, Collection<? extends Square> newSqs) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, newSqs);
	}

	public void replace(Collection<? extends Square> oldSqs, Line line) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, line);
	}

	public void replace(Collection<? extends Square> oldSqs, Polygonal p) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, p);
	}

	public void replace(Collection<? extends Square> oldSqs, Square newSq) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, newSq);
	}

	public void replace(Collection<? extends Square> oldSqs, Square[] newSqs) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, newSqs);
	}

	public void replace(int index, Collection<? extends Square> squares) {
		if (squares.size() == 0)
			return;

		int j = 0;
		for (Square sq : squares)
			if (replace(index + j, sq))
				j++;
	}

	public void replace(int index, Line line) {
		if (replace(index, line.getSq1()))
			replace(index + 1, line.getSq2());
		else
			replace(index, line.getSq2());
	}

	public void replace(int index, Polygonal p) {
		if (p.size() == 0)
			return;

		int j = 0;
		for (int i = 0; i < p.size(); i++)
			if (replace(index + j, p.getSquare(i)))
				j++;
	}

	public boolean replace(int index, Square sq) {
		if (sq == null)
			throw new NullPointerException("The square is null.");
		if (squares.size() > index - 1 && squares.get(index - 1).equals(sq) || squares.size() > index + 1 && squares.get(index + 1).equals(sq)) {
			squares.remove(index);
			return false;
		}
		squares.set(index, sq);
		return true;
	}

	public void replace(int index, Square[] squares) {
		if (squares.length == 0)
			return;

		int j = 0;
		for (int i = 0; i < squares.length; i++)
			if (replace(index + j, squares[i]))
				j++;
	}

	public void replace(Line oldLine, Collection<? extends Square> newSqs) {
		int index = squares.indexOf(oldLine);
		if (index != -1)
			replace(index, newSqs);
	}

	public void replace(Line oldLine, Line line) {
		int index = squares.indexOf(oldLine);
		if (index != -1)
			replace(index, line);
	}

	public void replace(Line oldLine, Polygonal p) {
		int index = squares.indexOf(oldLine);
		if (index != -1)
			replace(index, p);
	}

	public void replace(Line oldLine, Square newSq) {
		int index = squares.indexOf(oldLine);
		if (index != -1)
			replace(index, newSq);
	}

	public void replace(Line oldLine, Square[] newSqs) {
		int index = squares.indexOf(oldLine);
		if (index != -1)
			replace(index, newSqs);
	}

	public void replace(Polygonal oldP, Collection<? extends Square> newSqs) {
		int index = squares.indexOf(oldP);
		if (index != -1)
			replace(index, newSqs);
	}

	public void replace(Polygonal oldP, Line line) {
		int index = squares.indexOf(oldP);
		if (index != -1)
			replace(index, line);
	}

	public void replace(Polygonal oldP, Polygonal p) {
		int index = squares.indexOf(oldP);
		if (index != -1)
			replace(index, p);
	}

	public void replace(Polygonal oldP, Square newSq) {
		int index = squares.indexOf(oldP);
		if (index != -1)
			replace(index, newSq);
	}

	public void replace(Polygonal oldP, Square[] newSqs) {
		int index = squares.indexOf(oldP);
		if (index != -1)
			replace(index, newSqs);
	}

	public void replace(Square oldSq, Collection<? extends Square> newSqs) {
		int index = squares.indexOf(oldSq);
		if (index != -1)
			replace(index, newSqs);
	}

	public void replace(Square oldSq, Line line) {
		int index = squares.indexOf(oldSq);
		if (index != -1)
			replace(index, line);
	}

	public void replace(Square oldSq, Polygonal p) {
		int index = squares.indexOf(oldSq);
		if (index != -1)
			replace(index, p);
	}

	public void replace(Square oldSq, Square newSq) {
		int index = squares.indexOf(oldSq);
		if (index != -1)
			replace(index, newSq);
	}

	public void replace(Square oldSq, Square[] newSqs) {
		int index = squares.indexOf(oldSq);
		if (index != -1)
			replace(index, newSqs);
	}

	public void replace(Square[] oldSqs, Collection<? extends Square> newSqs) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, newSqs);
	}

	public void replace(Square[] oldSqs, Line line) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, line);
	}

	public void replace(Square[] oldSqs, Polygonal p) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, p);
	}

	public void replace(Square[] oldSqs, Square newSq) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, newSq);
	}

	public void replace(Square[] oldSqs, Square[] newSqs) {
		int index = squares.indexOf(oldSqs);
		if (index != -1)
			replace(index, newSqs);
	}

	public int size() {
		return squares.size();
	}

	public Square[] toArray() {
		return (Square[]) squares.toArray();
	}

	@Override
	public String toString() {
		if (squares.size() == 0)
			return "";
		String result = squares.firstElement().toString();
		for (int i = 1; i < squares.size(); i++)
			result += "-" + squares.get(i);
		return result;
	}

}
