package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {


    private ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow() -1;
        int column = position.getColumn() -1;
        board[row][column] = piece;
    }

    public void removePiece(ChessPosition position) {
        int row = position.getRow() -1;
        int column = position.getColumn() -1;
        board[row][column] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow() -1;
        int column = position.getColumn() -1;
        if (row < 0 || row >= 8 || column < 0 || column >= 8) {
            return null;
        }
        return board[row][column];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessPiece.PieceType[] pieceTypes = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };
        ChessGame.TeamColor color;
        for(int column = 0; column < 8; column++) {
            color = ChessGame.TeamColor.WHITE;
            board[0][column] = new ChessPiece(color, pieceTypes[column]);
            board[1][column] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
            color = ChessGame.TeamColor.BLACK;
            board[6][column] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
            board[7][column] = new ChessPiece(color, pieceTypes[column]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "board=" + Arrays.toString(board) +
                '}';
    }
}
