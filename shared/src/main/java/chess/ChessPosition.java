package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition implements Comparable<ChessPosition> {

    private static final char[] COLUMNS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    private final int col, row;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    public ChessPosition offset(int rowOffset, int colOffset) {
        return new ChessPosition(row + rowOffset, col + colOffset);
    }

    public ChessPosition offset(int... offsets) {
        return offset(offsets[0], offsets[1]);
    }

    public boolean onBoard() {
        return !(row > 8 || col > 8 || row < 1 || col < 1);
    }


    @Override
    public int compareTo(ChessPosition other) {
        return (this.row - other.row) * 2 + this.col - other.col;
    }

    @Override
    public String toString() {
        return "" + COLUMNS[col - 1] + row;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return col == that.col && row == that.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row);
    }
}
