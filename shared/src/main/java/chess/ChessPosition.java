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

    /**
     * Returns the difference between two ChessPosition objects in the form [rowDifference, columnDifference]. As a
     * result, positionB.equals(positionA.offset(positionA.difference(positionB))) should always return true.
     *
     * @return the difference between this and other in [rowDifference, columnDifference] format,
     * or null if other is null
     */
    public int[] difference(ChessPosition other) {
        if (other == null) {
            return null;
        }
        return new int[]{this.row - other.row, this.col - other.col};
    }

    public int[] direction(ChessPosition other){
        int[] offset = difference(other);
        int divisor = gcd(Math.abs(offset[0]), Math.abs(offset[1]));
        offset[0] /= divisor;
        offset[1] /= divisor;
        return offset;
    }

    private static int gcd(int a, int b){
        if (b==0) return a;
        return gcd(b, a%b);
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
