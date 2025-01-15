package chess;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;
    private int[][] moveOffsets;
    private static Map<PieceType, int[][]> potentialMoves = null;

    private void generatePotentialMoves() {
        potentialMoves = new HashMap<PieceType, int[][]>();
        potentialMoves.put(PieceType.KING, new int[][] { {0}, {1, 1}, {1, 0} });
        potentialMoves.put(PieceType.QUEEN, new int[][] { {1}, {1, 1}, {1, 0} });
        potentialMoves.put(PieceType.BISHOP, new int[][] { {1}, {1, 1} });
        potentialMoves.put(PieceType.KNIGHT, new int[][] { {0}, {2, 1}, {1, 2} });
        potentialMoves.put(PieceType.ROOK, new int[][] { {1}, {1, 0} });
        potentialMoves.put(PieceType.PAWN, null);
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        if (potentialMoves == null)
            generatePotentialMoves();
        this.pieceColor = pieceColor;
        this.type = type;
        this.moveOffsets = potentialMoves.get(type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        throw new RuntimeException("Not implemented");
    }
}

