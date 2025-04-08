package chess;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;


/**
 * Represents a single chess piece. Positions are 1-indexed.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public final class ChessPiece implements Comparable<ChessPiece>, Cloneable {

    private final ChessGame.TeamColor color;
    private final PieceType type;
    private boolean hasMoved;
    private int enPassant;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type, boolean hasMoved) {
        this.color = pieceColor;
        this.type = type;
        this.hasMoved = hasMoved;
        this.enPassant = 0;
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this(pieceColor, type, false);
    }


    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN;
        private static final Map<PieceType, int[][]> OFFSETS = Map.of(
                PieceType.KING, new int[][]{{-1, 1}, {0, 1}, {1, 1}, {-1, 0}, {1, 0}, {-1, -1}, {0, -1}, {1, -1}},
                PieceType.KNIGHT, new int[][]{{-1, 2}, {1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}},
                PieceType.PAWN, new int[0][0],
                PieceType.QUEEN, new int[][]{{-1, 1}, {0, 1}, {1, 1}, {-1, 0}, {1, 0}, {-1, -1}, {0, -1}, {1, -1}},
                PieceType.ROOK, new int[][]{{0, 1}, {-1, 0}, {1, 0}, {0, -1}},
                PieceType.BISHOP, new int[][]{{-1, 1}, {1, 1}, {-1, -1}, {1, -1}}
        );

        public String abbreviation() {
            return switch (this) {
                case KING -> "K";
                case QUEEN -> "Q";
                case BISHOP -> "B";
                case KNIGHT -> "N";
                case ROOK -> "R";
                case PAWN -> "P";
            };
        }

        public boolean continuous() {
            return switch (this) {
                case KING, PAWN, KNIGHT -> false;
                case QUEEN, ROOK, BISHOP -> true;
            };
        }

        public int[][] offsets() {
            return OFFSETS.get(this);
        }

    }


    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    public void setEnPassant(int enPassant) throws IllegalArgumentException {
        if (Math.abs(enPassant) > 1) {
            throw new IllegalArgumentException();
        }
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
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if (type == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        }
        ArrayList<ChessMove> moves = normalMoves(board, myPosition);
        if (type == PieceType.KING) {
            ChessMove queenSideCastle = canCastle(board, myPosition, true);
            if (queenSideCastle != null) {
                moves.add(queenSideCastle);
            }
            ChessMove kingSideCastle = canCastle(board, myPosition, false);
            if (kingSideCastle != null) {
                moves.add(kingSideCastle);
            }
        }
        return moves;
    }

    private ArrayList<ChessMove> normalMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        boolean continuous = type.continuous();
        for (var direction : type.offsets()) {
            ChessPosition endPosition = myPosition.offset(direction);
            boolean previousWasOpen;
            do {
                if (validMove(board, endPosition)) {
                    moves.add(new ChessMove(myPosition, endPosition));
                }
                previousWasOpen = openSquare(board, endPosition);
                endPosition = endPosition.offset(direction);
            } while (continuous && previousWasOpen);
        }
        return moves;
    }

    private ChessMove canCastle(ChessBoard board, ChessPosition myPosition, boolean queenSide) {
        if (hasMoved) {
            return null;
        }

        if (myPosition.getRow() % 7 != 1) {
            return null;
        }

        int[] rookOffset = queenSide ? new int[]{0, -4} : new int[]{0, 3};
        ChessPiece rook = board.getPiece(myPosition.offset(rookOffset));
        if (rook == null) {
            return null;
        }
        if (rook.hasMoved) {
            return null;
        }
        if (rook.color != color) {
            return null;
        }
        if (rook.type != PieceType.ROOK) {
            return null;
        }

        int[][] emptyOffsets = queenSide ? new int[][]{{0, -3}, {0, -2}, {0, -1}} : new int[][]{{0, 2}, {0, 1}};
        boolean middleEmpty = true;
        for (var space : emptyOffsets) {
            middleEmpty = middleEmpty && openSquare(board, myPosition.offset(space));
        }
        if (!middleEmpty) {
            return null;
        }

        int columnOffset = queenSide ? -2 : 2;

        return new ChessMove(myPosition, myPosition.offset(0, columnOffset));
    }

    private ArrayList<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        int direction = color.pawnDirection();

        ArrayList<ChessMove> protoMoves = checkPawnCaptures(board, myPosition, direction);

        protoMoves.addAll(checkPawnAdvances(board, myPosition, direction));

        ChessMove enPassantMove = enPassant(board, myPosition, direction);
        if (enPassantMove != null) {
            protoMoves.add(enPassantMove);
        }

        return permutePawnPromotions(myPosition, protoMoves);
    }

    private ArrayList<ChessMove> checkPawnCaptures(ChessBoard board, ChessPosition myPosition, int direction) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        final int[][] captureOffsets = new int[][]{{direction, 1}, {direction, -1}};
        for (var capture : captureOffsets) {
            ChessPosition endPosition = myPosition.offset(capture);
            if (validCapture(board, endPosition)) {
                moves.add(new ChessMove(myPosition, endPosition));
            }
        }
        return moves;
    }

    private ArrayList<ChessMove> checkPawnAdvances(ChessBoard board, ChessPosition myPosition, int direction) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int startRow = color.pawnStartRow();
        final int[][] openOffsets = myPosition.getRow() == startRow ?
                new int[][]{{direction, 0}, {2 * direction, 0}} : new int[][]{{direction, 0}};
        boolean firstSquareOpen = true;
        for (var open : openOffsets) {
            ChessPosition endPosition = myPosition.offset(open);
            if (firstSquareOpen && openSquare(board, endPosition)) {
                moves.add(new ChessMove(myPosition, endPosition));
            } else {
                firstSquareOpen = false;
            }
        }
        return moves;
    }

    private ChessMove enPassant(ChessBoard board, ChessPosition myPosition, int direction) {
        if (enPassant == 0) {
            return null;
        }

        int startRow = color.pawnStartRow();
        int enPassantRow = startRow + 3 * direction;
        if (myPosition.getRow() != enPassantRow) {
            return null;
        }

        ChessPiece foe = board.getPiece(myPosition.offset(0, enPassant));
        if (foe == null || foe.color == color || foe.type != PieceType.PAWN) {
            return null;
        }

        return new ChessMove(myPosition, myPosition.offset(direction, enPassant));

    }

    private ArrayList<ChessMove> permutePawnPromotions(ChessPosition myPosition, ArrayList<ChessMove> protoMoves) {
        int promoRow = color.pawnPromoRow();
        ArrayList<ChessMove> moves = new ArrayList<>();
        for (var move : protoMoves) {
            if (move.getEndPosition().getRow() == promoRow) {
                for (var promoType : PieceType.values()) {
                    if (promoType != PieceType.KING && promoType != PieceType.PAWN) {
                        moves.add(new ChessMove(myPosition, move.getEndPosition(), promoType));
                    }
                }
            } else {
                moves.add(move);
            }
        }
        return moves;
    }

    private boolean validCapture(ChessBoard board, ChessPosition endPosition) {
        if (ChessBoard.notOnBoard(endPosition)) {
            return false;
        }
        ChessPiece other = board.getPiece(endPosition);
        return other != null && other.color != color;
    }

    private boolean openSquare(ChessBoard board, ChessPosition endPosition) {
        if (ChessBoard.notOnBoard(endPosition)) {
            return false;
        }
        return board.getPiece(endPosition) == null;
    }

    private boolean validMove(ChessBoard board, ChessPosition endPosition) {
        if (ChessBoard.notOnBoard(endPosition)) {
            return false;
        }
        return openSquare(board, endPosition) || validCapture(board, endPosition);
    }

    @Override
    public int compareTo(ChessPiece o) {
        return this.color.compareTo(o.color) * 31 + this.type.compareTo(o.type);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    @Override
    public String toString() {
        return color.abbreviation() + type.abbreviation();
    }

    @Override
    public ChessPiece clone() {
        try {
            return (ChessPiece) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
