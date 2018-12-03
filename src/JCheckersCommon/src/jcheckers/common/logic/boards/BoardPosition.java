package jcheckers.common.logic.boards;

public class BoardPosition {

	private int row;
	private int col;

	public BoardPosition(int row, int col) {
		this.row = row;
		this.col = col;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BoardPosition))
			return false;
		BoardPosition other = (BoardPosition) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	public String toBoard2DNotation() {
		return row + "," + col;
	}

	public String toEnglishNotation(int rowCount, int colCount) {
		// TODO implementar
		return "";
	}

	@Override
	public String toString() {
		return "(" + row + "," + col + ")";
	}

}
