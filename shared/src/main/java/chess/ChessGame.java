package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn = TeamColor.WHITE;
    private ChessBoard board = ChessBoard.newGameBoard();

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
        ChessPosition start = move.getStartPosition();
        ChessPiece piece = board.getPiece(start);
        if(piece != null && (piece.getPieceType() == ChessPiece.PieceType.KING )){
            return kingMovedIntoCheck(move);
        }
        if (piece != null && isInCheck(piece.getTeamColor())) {
            return moveDoesNotPreventCheck(move);
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
                return !end.equals(pinningPiece)
                        && !Arrays.equals(start.direction(pinningPiece), end.direction(pinningPiece));
            default: return true; // pinned twice; always causes check
        }

    }

    private boolean moveDoesNotPreventCheck(ChessMove move){
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);
        if(piece == null){
            throw new IllegalArgumentException();
        }
        ChessPosition kingPosition = board.getKingPosition(piece.getTeamColor());
        if (kingPosition == null) {
            throw new IllegalArgumentException();
        }
        HashSet<ChessPosition> kingAttackers = getAttackerPositions(attacksOnKing(piece.getTeamColor()));

        boolean worthChecking = false;
        int[] kingEndDirection = kingPosition.direction(end);
        for(var attacker : kingAttackers){
            if(Arrays.equals(kingEndDirection, kingPosition.direction(attacker))){
                worthChecking = true;
            }
        }
        if(!worthChecking){
            return true;
        }
        ChessPiece captured = board.getPiece(end);
        hardMove(move);
        boolean out = isInCheck(piece.getTeamColor());
        board.addPiece(start, piece);
        board.addPiece(end, captured);
        return out;
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
        var moveOptions = validMoves(move.getStartPosition());
        if (moveOptions == null || !moveOptions.contains(move) || currentTurn != piece.getTeamColor()) {
            throw new InvalidMoveException("Invalid move: " + move);
        }

        hardMove(move);
        currentTurn = currentTurn.other();
        piece.setHasMoved(true);
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

    private static HashSet<ChessPosition> getAttackerPositions(Collection<ChessMove> attacks){
        HashSet<ChessPosition> positions = new HashSet<>();
        for(ChessMove attack : attacks){
            positions.add(attack.getStartPosition());
        }
        return positions;
    }

    private Collection<ChessPosition> pinningPiecePositions(ChessPosition position){
        ChessPiece piece = board.getPiece(position);
        if(piece == null || piece.getPieceType() == ChessPiece.PieceType.KING){
            return new HashSet<>();
        }
        TeamColor color = piece.getTeamColor();
        var attacksOnPosition = rawAttacks(position);
        if(attacksOnPosition.isEmpty()) return new HashSet<>();

        HashSet<ChessPosition> attackers = getAttackerPositions(attacksOnPosition);

        board.removePiece(position);
        ArrayList<ChessMove> potentialKingAttacks = new ArrayList<>();
        for(var attacker : attackers){
            potentialKingAttacks.addAll(board.getPiece(attacker).pieceMoves(board, attacker));
        }
        potentialKingAttacks.removeIf(move -> !board.getKingPosition(color).equals(move.getEndPosition())
        || isPinned(move.getStartPosition()));

        HashSet<ChessPosition> pinningPieces = getAttackerPositions(potentialKingAttacks);

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
     * @param position the position to find the attackers of
     * @return the moves of the opposite team of the one occupying the passed position that end on that position, not
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
        System.out.println(teamColor + ": in checkmate? " + out);
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
        public String abbreviation(){
            return this.toString().substring(0, 1);
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
