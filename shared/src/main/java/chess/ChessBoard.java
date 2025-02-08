package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private final ChessPiece[][] board = new ChessPiece[8][8];
    private static final ChessPiece.PieceType[] PIECE_ORDER = new ChessPiece.PieceType[]{
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.KING,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.ROOK
    };

    public ChessBoard() {

    }

    public static ChessBoard newGameBoard() {
        var board = new ChessBoard();
        board.resetBoard();
        return board;
    }

    public static boolean notOnBoard(ChessPosition pos) {
        if(pos == null){
            return true;
        }
        int row = pos.getRow();
        int col = pos.getColumn();
        return row < 1 || row > 8 || col < 1 || col > 8;
    }

    public static boolean onBoard(ChessPosition pos) {
        return !notOnBoard(pos);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        if (notOnBoard(position)) {
            return;
        }
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    public void removePiece(ChessPosition position) {
        if (notOnBoard(position)) {
            return;
        }
        board[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (notOnBoard(position)) {
            return null;
        }
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

//    public ArrayList<ChessMove> getAllMoves(){
//        return getTeamMoves(null);
//    }
//

    /**
     * Returns potential moves of all the pieces of the passed color. Returns all moves if passed null.
     */
    public ArrayList<ChessMove> getTeamMoves(ChessGame.TeamColor team) {
        ChessGame.TeamColor filter = team == null? null : team.other();
        ArrayList<ChessMove> moves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                ChessPosition pos = new ChessPosition(row + 1, column + 1);
                ChessPiece piece = board[row][column];
                if (piece != null && piece.getTeamColor() != filter) {
                    moves.addAll(piece.pieceMoves(this, pos));
                }
            }
        }
        return moves;

    }

    public ArrayList<ChessPosition> getTeamPositions(ChessGame.TeamColor team) {
        ArrayList<ChessPosition> positions = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                ChessPiece piece = board[row][column];
                if (piece != null && piece.getTeamColor() == team) {
                    positions.add(new ChessPosition(row + 1, column + 1));
                }
            }
        }
        return positions;
    }

    public ChessPosition getKingPosition(ChessGame.TeamColor color) {
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                ChessPiece piece = board[row][column];
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == color) {
                    return new ChessPosition(row + 1, column + 1);
                }
            }
        }
        throw new RuntimeException("King Missing:\n" + this);
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessGame.TeamColor color;
        for (int column = 0; column < 8; column++) {
            color = ChessGame.TeamColor.WHITE;
            board[0][column] = new ChessPiece(color, PIECE_ORDER[column]);
            board[1][column] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
            color = ChessGame.TeamColor.BLACK;
            board[6][column] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
            board[7][column] = new ChessPiece(color, PIECE_ORDER[column]);
        }
        for (int row = 2; row < 6; row++) {
            for (int column = 0; column < 8; column++) {
                board[row][column] = null;
            }
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
        StringBuilder s = new StringBuilder();
        for (int j = 7; j >= 0; j--) {
            s.append("|");
            for (int i = 0; i < 8; i++) {
                ChessPiece piece = board[j][i];
                s.append(piece == null ? "  |" : piece + "|");
            }
            if(j!= 0) {
                s.append("\n");
            }
        }
        return s.toString();
    }
}
