package chess;

import java.util.*;
import java.util.function.Predicate;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn = TeamColor.WHITE;
    private ChessBoard board = ChessBoard.newGameBoard();
//    private boolean blackInCheck = false;
//    private boolean whiteInCheck = false;

    public ChessGame() {
    }

    public ChessGame(TeamColor startColor, ChessBoard startPosition) {
        currentTurn = startColor;
        board = startPosition;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Collection of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public ArrayList<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        return validateMoves(piece.pieceMoves(board, startPosition));
    }

    private ArrayList<ChessMove> allValidMoves(TeamColor teamColor) {
        return validateMoves(board.getTeamMoves(teamColor));
    }

    private ArrayList<ChessMove> validateMoves(Collection<ChessMove> moves) {
        ArrayList<ChessMove> validMoves = new ArrayList<>(moves);
        validMoves.removeIf(this::moveCausesCheck);
        return validMoves;
    }

    private boolean moveCausesCheck(ChessMove move) {
//Bug is with killing kings. sequence of calls and status of kings?
        ChessPosition start = move.getStartPosition();
        ChessPiece piece = board.getPiece(start);
        if(piece != null && piece.getPieceType() == ChessPiece.PieceType.KING){
            return kingMovedIntoCheck(move);
        }
        Collection<ChessPosition> pinningPieces = pinningPiecePositions(start);
        switch(pinningPieces.size()){
            case 0: return false;
            case 1:
                //when pinned: kill attacker, or stay in line
                // end in  pinningPieces
                // end -> attacker direction same
                ChessPosition end = move.getEndPosition();
                ChessPosition pinningPiece = pinningPieces.iterator().next();
                return !end.equals(pinningPiece) && !Arrays.equals(start.direction(pinningPiece), end.direction(pinningPiece));
            default: return true; // pinned twice; always causes check
        }

    }

    private boolean kingMovedIntoCheck(ChessMove move){
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece king = board.getPiece(start);
        if(king == null || king.getPieceType() != ChessPiece.PieceType.KING){
            throw new IllegalArgumentException();
        }
        ChessPiece captured = board.getPiece(end);
        hardMove(move);
        boolean out = isInCheck(king.getTeamColor());
        board.addPiece(start, king);
        board.addPiece(end, captured);
        return out;
    }

    private void hardMove(ChessMove move) {
//        int hash = board.hashCode();
        if (move.getPromotionPiece() != null) {
            TeamColor color = board.getPiece(move.getStartPosition()).getTeamColor();
            board.addPiece(move.getEndPosition(), new ChessPiece(color, move.getPromotionPiece(), true));
        } else {
            board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
        }
        board.removePiece(move.getStartPosition());
//        return hash == board.hashCode();
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        var valids = validMoves(move.getStartPosition());
        if (valids == null || !valids.contains(move) || currentTurn != piece.getTeamColor()) {
            throw new InvalidMoveException("Invalid move: " + move);
        }

        hardMove(move);
        currentTurn = currentTurn.other();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        System.out.println("Is " + teamColor + " in check? \n" + board);
        boolean out = !attacksOnKing(teamColor).isEmpty();
        System.out.println(teamColor + ": in check? " + out);
        return out;
    }

    private boolean isPinned(ChessPosition position){
        return !pinningPiecePositions(position).isEmpty();
    }

    private Collection<ChessPosition> pinningPiecePositions(ChessPosition position){
        HashSet<ChessPosition> pinningPieces = new HashSet<>();
        ChessPiece piece = board.getPiece(position);
        if(piece == null || piece.getPieceType() == ChessPiece.PieceType.KING){
            return pinningPieces;
        }
        TeamColor color = piece.getTeamColor();
        var attacksOnPosition = rawAttacks(position);
        if(attacksOnPosition.isEmpty()) return pinningPieces;

        HashSet<ChessPosition> attackers = new HashSet<>();
        for(var attack : attacksOnPosition){
            attackers.add(attack.getStartPosition());
        }

        board.removePiece(position);
        ArrayList<ChessMove> potentialKingAttacks = new ArrayList<>();
        for(var attacker : attackers){
            potentialKingAttacks.addAll(board.getPiece(attacker).pieceMoves(board, attacker));
        }
        potentialKingAttacks.removeIf(move -> !board.getKingPosition(color).equals(move.getEndPosition())
        || isPinned(move.getStartPosition()));

        for(var attack : potentialKingAttacks){
            pinningPieces.add(attack.getStartPosition());
        }
        board.addPiece(position, piece);
        return pinningPieces;
    }

    private boolean isPinned(ChessMove move){
        return isPinned(move.getStartPosition());
    }

    private Collection<ChessMove> attacksOnKing(TeamColor teamColor){
        Collection<ChessMove> toConsider = rawAttacks(board.getKingPosition(teamColor));
        toConsider.removeIf(this::isPinned);
        return toConsider;
    }

    /**
     * Calculates all moves the opponent could make that end on the given position.
     * @returns the moves of the opposite team of the one occupying the passed position that end on that position, not
     * filtered for validity.
     * */
    private Collection<ChessMove> rawAttacks(ChessPosition position){
        ArrayList<ChessMove> attacks = new ArrayList<>();
        if(position == null){
            return attacks;
        }
        ChessPiece piece = board.getPiece(position);
        if(piece == null){
            return attacks;
        }
        attacks = board.getTeamMoves(piece.getTeamColor().other());
        attacks.removeIf( move -> !position.equals(move.getEndPosition()));
        return attacks;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {

        if (!isInCheck(teamColor)) {
            return false;
        }
        System.out.println("Is " + teamColor + " in checkmate? \n" + board);
        boolean out = allValidMoves(teamColor).isEmpty();
        System.out.println(teamColor + ": in check? " + out);
        return out;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return allValidMoves(teamColor).isEmpty();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE, BLACK;
        public TeamColor other(){
            return switch(this){
                case WHITE -> BLACK;
                case BLACK -> WHITE;
            };
        }
        public int pawnDirection(){
            return switch(this){
                case WHITE -> 1;
                case BLACK -> -1;
            };
        }
        public int pawnStartRow(){
            return switch(this){
                case WHITE -> 2;
                case BLACK -> 7;
            };
        }
        public int pawnPromoRow(){
            return switch(this){
                case WHITE -> 8;
                case BLACK -> 1;
            };
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, board);
    }
}
