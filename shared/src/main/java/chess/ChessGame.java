package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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

    public ChessGame() {}
    public ChessGame(TeamColor startColor, ChessBoard startPosition){
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
        if(piece == null) return null;
        return validateMoves(piece.pieceMoves(board, startPosition));
    }

    private ArrayList<ChessMove> allValidMoves(TeamColor teamColor){
        return validateMoves(board.getTeamMoves(teamColor));
    }

    private ArrayList<ChessMove> validateMoves(Collection<ChessMove> moves){
        ArrayList<ChessMove> validMoves = new ArrayList<>(moves);
        validMoves.removeIf(this::moveCausesCheck);
        return validMoves;
    }

    private boolean moveCausesCheck(ChessMove move){
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece startPiece = board.getPiece(start);
        if(startPiece == null) return true;
        startPiece = startPiece.clone();
        TeamColor team = startPiece.getTeamColor();

        ChessPiece endPiece = board.getPiece(end);
        if(endPiece != null) endPiece = endPiece.clone();
        boolean out;
        if(endPiece != null && endPiece.getPieceType() == ChessPiece.PieceType.KING){
            board.removePiece(start);
        }else{
            hardMove(move);
        }
        out = isInCheck(team);
        board.addPiece(start, startPiece);
        board.addPiece(end, endPiece);
        return out;
    }

    private void hardMove(ChessMove move){
//        int hash = board.hashCode();
        if(move.getPromotionPiece() != null){
            TeamColor color = board.getPiece(move.getStartPosition()).getTeamColor();
            board.addPiece(move.getEndPosition(), new ChessPiece(color, move.getPromotionPiece(), true));
        }else{
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
        var valids = validMoves(move.getStartPosition());
        if(valids==null || !valids.contains(move)) throw new InvalidMoveException("Invalid move: " + move);
        hardMove(move);
        currentTurn = switch (currentTurn){
            case BLACK -> TeamColor.WHITE;
            case WHITE -> TeamColor.BLACK;
        };
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = board.getKingPosition(teamColor);
        Collection<ChessMove> toConsider = switch(teamColor){
            case WHITE -> board.getTeamMoves(TeamColor.BLACK);
            case BLACK -> board.getTeamMoves(TeamColor.WHITE);
        };
        for(var move : toConsider){
            if(kingPosition.equals(move.getEndPosition()) && !moveCausesCheck(move)) return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if(!isInCheck(teamColor)) return false;
        return allValidMoves(teamColor).isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor)) return false;
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
        WHITE,
        BLACK
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
