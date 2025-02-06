package chess;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;



/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public final class ChessPiece implements Comparable<ChessPiece>, Cloneable{

    private final ChessGame.TeamColor color;
    private final PieceType type;
    private boolean hasMoved;
    private int enPassant;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type, boolean hasMoved) {
        this.color = pieceColor;
        this.type = type;
        this.hasMoved = hasMoved;
        this.enPassant = 0;
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
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

    private static final Map<PieceType, Boolean> CONTINUOUS = Map.of(
            PieceType.KING, false,
            PieceType.KNIGHT, false,
            PieceType.PAWN, false,
            PieceType.QUEEN, true,
            PieceType.ROOK, true,
            PieceType.BISHOP, true
    );

    private static final Map<PieceType, int[][]> OFFSETS = Map.of(
            PieceType.KING, new int[][]{{-1, 1}, {0, 1}, {1, 1}, {-1, 0}, {1, 0}, {-1, -1}, {0, -1}, {1, -1}},
            PieceType.KNIGHT, new int[][]{{-1, 2}, {1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}},
            PieceType.PAWN, new int[0][0],
            PieceType.QUEEN, new int[][]{{-1, 1}, {0, 1}, {1, 1}, {-1, 0}, {1, 0}, {-1, -1}, {0, -1}, {1, -1}},
            PieceType.ROOK, new int[][]{{0, 1}, {-1, 0}, {1, 0}, {0, -1}},
            PieceType.BISHOP, new int[][]{{-1, 1}, {1, 1}, {-1, -1}, {1, -1}}
    );


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
        if(type == PieceType.PAWN) return pawnMoves(board, myPosition);
        ArrayList<ChessMove> moves = normalMoves(board, myPosition);
        if(type == PieceType.KING){
            ChessMove queenSideCastle = canCastle(board, myPosition, true);
            if(queenSideCastle != null) moves.add(queenSideCastle);
            ChessMove kingSideCastle = canCastle(board, myPosition, false);
            if(kingSideCastle != null) moves.add(kingSideCastle);
        }
        return moves;
    }

    private ArrayList<ChessMove> normalMoves(ChessBoard board, ChessPosition myPosition){
        ArrayList<ChessMove> moves = new ArrayList<>();
        boolean continuous = CONTINUOUS.get(type);
        for(var direction : OFFSETS.get(type)){
            ChessPosition endPosition = myPosition.offset(direction);
            boolean previousWasOpen;
            do{
                if(validMove(board, endPosition)){
                    moves.add(new ChessMove(myPosition, endPosition));
                }
                previousWasOpen = openSquare(board, endPosition);
                endPosition = endPosition.offset(direction);
            }while(continuous && previousWasOpen);
        }
        return moves;
    }

    private ChessMove canCastle(ChessBoard board, ChessPosition myPosition, boolean queenSide) {
        if(hasMoved) return null;

        if(myPosition.getRow() % 7 != 1) return null;

        int[] rookOffset = queenSide ? new int[]{0, -4} : new int[]{0, 3};
        ChessPiece rook = board.getPiece(myPosition.offset(rookOffset));
        if(rook == null) return null;
        if(rook.hasMoved) return null;
        if(rook.color != color) return null;
        if(rook.type != PieceType.ROOK) return null;

        int[][]emptyOffsets = queenSide ? new int[][]{{0, -3}, {0, -2}, {0, -1}} : new int[][]{{0, 2}, {0, 1}};
        boolean middleEmpty = true;
        for(var space : emptyOffsets){
            middleEmpty = middleEmpty && openSquare(board, myPosition.offset(space));
        }
        if(!middleEmpty) return null;

        int columnOffset = queenSide? -2 : 2;

        return new ChessMove(myPosition, myPosition.offset(0, columnOffset));
    }

    private ArrayList<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){
        int direction = color == ChessGame.TeamColor.WHITE? 1 : -1;

        ArrayList<ChessMove> protoMoves = checkPawnCaptures(board, myPosition, direction);

        protoMoves.addAll(checkPawnAdvances(board, myPosition, direction));

        ChessMove enPassantMove = enPassant(board, myPosition, direction);
        if(enPassantMove != null && ! protoMoves.contains(enPassantMove)) protoMoves.add(enPassantMove);

        return permutePawnPromotions(myPosition, direction, protoMoves);
    }

    private ArrayList<ChessMove> checkPawnCaptures(ChessBoard board, ChessPosition myPosition, int direction) {
        ArrayList<ChessMove> moves = new ArrayList<>();

        final int[][] CAPTURE_OFFSETS = new int[][] {{direction, 1}, {direction, -1}};
        for(var capture : CAPTURE_OFFSETS){
            ChessPosition endPosition = myPosition.offset(capture);
            if(validCapture(board, endPosition)){
                moves.add(new ChessMove(myPosition, endPosition));
            }
        }
        return moves;
    }

    private ArrayList<ChessMove> checkPawnAdvances(ChessBoard board, ChessPosition myPosition, int direction) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int startRow = direction == 1? 2 : 7;
        final int[][] OPEN_OFFSETS = myPosition.getRow() == startRow?
                new int[][] {{direction, 0}, {2 * direction, 0}} : new int[][] {{direction, 0}};
        boolean firstSquareOpen = true;
        for(var open : OPEN_OFFSETS){
            ChessPosition endPosition = myPosition.offset(open);
            if(firstSquareOpen && openSquare(board, endPosition)){
                moves.add(new ChessMove(myPosition, endPosition));
            }else{
                firstSquareOpen = false;
            }
        }
        return moves;
    }

    private ChessMove enPassant(ChessBoard board, ChessPosition myPosition, int direction){
        if(enPassant == 0) return null;

        int startRow = direction == 1? 2 : 7;
        int enPassantRow = startRow + 3 * direction;
        if(myPosition.getRow() != enPassantRow) return null;

        ChessPiece foe = board.getPiece(myPosition.offset(enPassant, 0));
        if(foe == null || foe.color == color || foe.type != PieceType.PAWN) return null;

        return new ChessMove(myPosition, myPosition.offset(enPassant, direction));

    }

    private static ArrayList<ChessMove> permutePawnPromotions(
            ChessPosition myPosition, int direction, ArrayList<ChessMove> protoMoves) {

        int promoRow = direction == 1? 8 : 1;
        ArrayList<ChessMove> moves = new ArrayList<>();
        for(var move : protoMoves){
            if(move.getEndPosition().getRow() == promoRow){
                for(var promoType : PieceType.values()){
                    if(promoType != PieceType.KING && promoType != PieceType.PAWN){
                        moves.add(new ChessMove(myPosition, move.getEndPosition(), promoType));
                    }
                }
            } else {
                moves.add(move);
            }
        }
        return moves;
    }

    private boolean validCapture(ChessBoard board, ChessPosition endPosition){
        if(board.notOnBoard(endPosition)){
            return false;
        }
        ChessPiece other = board.getPiece(endPosition);
        return other != null && other.color != color;
    }

    private boolean openSquare(ChessBoard board, ChessPosition endPosition){
        if(board.notOnBoard(endPosition)){
            return false;
        }
        return board.getPiece(endPosition) == null;
    }

    private boolean validMove(ChessBoard board, ChessPosition endPosition){
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
    public String toString(){
        String typeString = String.valueOf(type);
        return "" + String.valueOf(color).charAt(0) + typeString.charAt(0) + typeString.toLowerCase().charAt(1);
    }

    @Override
    public ChessPiece clone(){
        try{
            return (ChessPiece) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
