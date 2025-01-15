package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;
    private final int[][] moveOffsets;
    private boolean hasMoved;
    private int enPassant;
    private static final Map<PieceType, int[][]> potentialMoves = Map.of(
            PieceType.PAWN, new int[][] {},
            PieceType.KING, new int[][] {
                    {1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1} },
            PieceType.KNIGHT, new int[][] {
                    {2, 1}, {1, 2}, {-2, 1}, {-1, 2}, {-2, -1}, {-1, -2}, {2, -1}, {1, -2}, },
            PieceType.QUEEN, new int[][] {
                    {1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1} },
            PieceType.BISHOP, new int[][] {
                    {1, 1}, {-1, 1}, {-1, -1}, {1, -1} },
            PieceType.ROOK, new int[][] {
                    {1, 0}, {0, 1}, {-1, 0}, {0, -1} }
    );
    private static final Map<PieceType, Boolean> continuousMoves = Map.of(
            PieceType.PAWN, false,
            PieceType.KING, false,
            PieceType.KNIGHT, false,
            PieceType.QUEEN, true,
            PieceType.BISHOP, true,
            PieceType.ROOK, true
    );

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type, boolean hasMoved) {
        this.pieceColor = pieceColor;
        this.type = type;
        this.hasMoved = hasMoved;
        this.moveOffsets = potentialMoves.get(type);
        this.enPassant = 0;
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type){
        this(pieceColor, type, false);
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
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    public void setEnPassant(int enPassant) throws IllegalArgumentException{
        if (Math.abs(enPassant) > 1)
            throw new IllegalArgumentException();
        this.enPassant = enPassant;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if(type == ChessPiece.PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        }
        List<ChessMove> moves = new ArrayList<>();
        ChessPosition endPosition;
        ChessPiece endPiece;
        boolean valid, empty, capture;
        boolean continuous = continuousMoves.get(type);
        for(var offset : moveOffsets) {
            endPosition = myPosition.offset(offset);
            endPiece = board.getPiece(endPosition);
            valid = endPosition.onBoard();
            do {
                empty =  endPiece == null;
                capture = !empty && endPiece.getTeamColor() == this.pieceColor;
                if(valid && (empty || capture))
                    moves.add(new ChessMove(myPosition, endPosition));
                endPosition = endPosition.offset(offset);
                endPiece = board.getPiece(endPosition);
                valid = endPosition.onBoard();
            } while (continuous && empty && valid);
        }
        if(type == ChessPiece.PieceType.KING && !hasMoved) {
            if (canCastle(board, myPosition, true))
                moves.add(new ChessMove(myPosition, myPosition.offset(0,-2)));
            if (canCastle(board, myPosition, false))
                moves.add(new ChessMove(myPosition, myPosition.offset(0,2)));
        }
        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        int direction = switch (pieceColor) {
            case WHITE -> 1;
            case BLACK -> -1;
        };
        List<ChessMove> moves = new ArrayList<>();
        ChessPosition endPosition;
        ChessPiece endPiece;
        int[][] captures = {{direction, 1}, {direction, -1}};
        for (var offset : moveOffsets) {
            endPosition = myPosition.offset(offset);
            endPiece = board.getPiece(endPosition);
            if(endPiece != null && endPiece.getTeamColor() != this.pieceColor)
                moves.add(new ChessMove(myPosition, endPosition));
        }
        if (enPassant != 0)
            moves.add(new ChessMove(myPosition, myPosition.offset(enPassant, direction)));
        do {
            endPosition = myPosition.offset(0, direction);
            endPiece = board.getPiece(endPosition);
            if (endPosition.onBoard() && endPiece == null)
                moves.add(new ChessMove(myPosition, endPosition));
            direction *= 2;
        } while (myPosition.getRow() == 2 && Math.abs(direction) == 2);

        return moves;
    }

    private boolean canCastle(ChessBoard board, ChessPosition myPosition, boolean left) {
        int[] rookOffset = left? new int[]{0, -4} : new int[]{0, 3};
        int[] emptyOffset1 = left? new int[]{0, -3} : new int[]{0, 2};
        int[] emptyOffset2 = left? new int[]{0, -2} : new int[]{0, 1};
        int[] emptyOffset3 = left? new int[]{0, -1} : new int[]{0, 0};
        boolean rookNotMoved = board.getPiece(myPosition.offset(rookOffset)).hasMoved;
        boolean empty1 = board.getPiece(myPosition.offset(emptyOffset1))==null;
        boolean empty2 = board.getPiece(myPosition.offset(emptyOffset2))==null;
        boolean empty3 = board.getPiece(myPosition.offset(emptyOffset3))==null;

        return rookNotMoved && empty1 && empty2 && (!left || empty3);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "type=" + type +
                ", pieceColor=" + pieceColor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }


}

