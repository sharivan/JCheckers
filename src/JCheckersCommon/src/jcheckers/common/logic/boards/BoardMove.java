package jcheckers.common.logic.boards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import common.util.Tree.Node;

public class BoardMove {

	private BoardPosition[] positions;

	public BoardMove(BoardMove move, BoardPosition newPosition) {
		positions = new BoardPosition[move.positions.length + 1];
		for (int i = 0; i < move.positions.length; i++)
			positions[i] = move.positions[i];

		positions[positions.length - 1] = newPosition;
	}
	
	public BoardMove(BoardMove firstMove, BoardMove secondMove) {
		if (!firstMove.last().equals(secondMove.first()))
			throw new RuntimeException("The first and last move are not connected: first=" + firstMove + " last=" + secondMove);
		
		int firstMoveCount = firstMove.count();
		int secondMoveCount = secondMove.count();
		positions = new BoardPosition[firstMoveCount + secondMoveCount - 1];

		for (int i = 0; i < firstMoveCount; i++)
			positions[i] = firstMove.positions[i];
		
		for (int i = firstMoveCount; i < firstMoveCount + secondMoveCount - 1; i++)
			positions[i] = secondMove.positions[i - firstMoveCount + 1];
	}

	public BoardMove(BoardMove move, int start, int count) {
		positions = new BoardPosition[count];

		for (int i = 0; i < count; i++)
			positions[i] = move.positions[i + start];
	}

	public BoardMove(BoardPosition... positions) {
		this.positions = new BoardPosition[positions.length];
		for (int i = 0; i < positions.length; i++)
			this.positions[i] = positions[i];
	}

	public BoardMove(Collection<? extends BoardPosition> c) {
		this(c.toArray(new BoardPosition[] {}));
	}

	public BoardMove(int... values) {
		int len = values.length;
		if ((len & 1) != 0)
			throw new RuntimeException("The numbers of arguments shold be a pair number.");

		len = len >>> 1;
		positions = new BoardPosition[len];

		for (int i = 0; i < len; i++)
			positions[i] = new BoardPosition(values[2 * i], values[2 * i + 1]);
	}

	public BoardMove(Node<BoardPosition> node) {
		ArrayList<BoardPosition> positionsList = new ArrayList<>();
		while (node != null) {
			positionsList.add(node.getValue());
			node = node.getParent();
		}

		positions = new BoardPosition[positionsList.size()];
		for (int i = 0; i < positions.length; i++)
			positions[i] = positionsList.get(positions.length - i - 1);
	}

	public int count() {
		return positions.length;
	}

	public BoardPosition dest() {
		if (positions.length == 0)
			return null;

		return positions[positions.length - 1];
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BoardMove))
			return false;
		BoardMove other = (BoardMove) obj;
		if (!Arrays.equals(positions, other.positions))
			return false;
		return true;
	}

	public BoardPosition get(int index) {
		return positions[index];
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(positions);
		return result;
	}

	public int length() {
		if (positions.length <= 1)
			return 0;

		int result = 0;
		for (int i = 1; i < positions.length; i++)
			result += Math.max(positions[i].getRow() - positions[i - 1].getRow(), positions[i].getCol() - positions[i - 1].getCol());

		return result;
	}

	public BoardPosition source() {
		if (positions.length == 0)
			return null;

		return positions[0];
	}

	public boolean startWith(BoardMove move) {
		if (move.positions.length > positions.length)
			return false;

		int min = Math.min(positions.length, move.positions.length);

		for (int i = 0; i < min; i++)
			if (!positions[i].equals(move.positions[i]))
				return false;

		return true;
	}

	public String toBoard2DNotation() {
		if (positions.length == 0)
			return "";

		String result = positions[0].toBoard2DNotation();
		for (int i = 1; i < positions.length; i++)
			result += " - " + positions[i].toBoard2DNotation();

		return result;
	}

	public String toEnglishNotation(int rowCount, int colCount) {
		if (positions.length == 0)
			return "";

		String result = positions[0].toEnglishNotation(rowCount, colCount);
		for (int i = 1; i < positions.length; i++)
			result += " - " + positions[i].toEnglishNotation(rowCount, colCount);

		return result;
	}

	@Override
	public String toString() {
		return "<" + toBoard2DNotation() + ">";
	}
	
	public BoardPosition first() {
		return positions[0];
	}

	public BoardPosition last() {
		return positions[positions.length - 1];
	}
	
	public BoardMove subMove(int start) {
		return subMove(start, positions.length - start);
	}
	
	public BoardMove subMove(int start, int count) {
		return new BoardMove(this, start, count);
	}

}
