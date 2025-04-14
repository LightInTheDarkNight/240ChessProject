package client.repl;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;

import javax.websocket.MessageHandler;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static client.repl.EscapeSequences.*;

public class GameplayOptions extends ChessMenuOptions implements MessageHandler.Whole<String> {
    public static String prompt = SET_TEXT_COLOR_GREEN + "[Game Option] >>> " + RESET_TEXT_COLOR;
    private final PrintStream out;
    public GameplayOptions(PrintStream out){
        this.out = out;
    }
    public void onMessage(String message){
        ServerMessage received = new Gson().fromJson(message, ServerMessage.class);
        switch(received.serverMessageType()) {
            case NOTIFICATION -> out.println(SET_TEXT_COLOR_GREEN + received.message() + RESET_TEXT_COLOR);
            case LOAD_GAME -> {
                currentGame = new Gson().fromJson(received.game(), ChessGame.class);
                drawBoard(out, null);
            }
            case ERROR -> out.println(SET_TEXT_COLOR_RED + received.message() + RESET_TEXT_COLOR);
        }
        out.print(prompt);
    }
    public static void redrawBoard(Scanner in, PrintStream out){
        drawBoard(out, null);
    }

    public static void makeMove(Scanner in, PrintStream out){
        if(currentGame.getTeamTurn() != perspective){
            out.println(SET_TEXT_COLOR_BLUE + "It isn't your turn yet." + RESET_TEXT_COLOR);
            return;
        }

        ChessPosition start = getChessPosition(in, out, "Please enter the starting position of your move.");
        if(start == null){
            return;
        }

        ChessPiece piece = currentGame.getBoard().getPiece(start);
        if(piece == null){
            out.println(SET_TEXT_COLOR_BLUE + "There is no piece at that starting position." + RESET_TEXT_COLOR);
            return;
        }

        ChessPosition end = getChessPosition(in, out, "Please enter the ending position of your move.");
        if(end == null){
            return;
        }
        ChessPiece.PieceType promotion = null;
        if(piece.getPieceType() == ChessPiece.PieceType.PAWN && end.getRow() == perspective.pawnPromoRow()){
            promotion = getPromotionPiece(in, out);
            if(promotion == null){
                return;
            }
        }
        try{
            socket.makeMove(authToken, currentGameID, new ChessMove(start, end, promotion));
        } catch (IOException e) {
            out.println(SET_TEXT_COLOR_RED + "Sorry, an error occurred. Please try again." + RESET_TEXT_COLOR);
        }
    }

    public static void resignGame(Scanner in, PrintStream out){
        out.println(SET_TEXT_COLOR_BLUE + "Are you sure you want to resign the game?");
        if(confirm(in, out)){
            try{
                socket.resignGame(authToken, currentGameID);
            } catch (IOException e) {
                out.println(SET_TEXT_COLOR_RED + "Sorry, an error occurred. Please try again." + RESET_TEXT_COLOR);
            }
        }
    }

    public static void leaveGame(Scanner in, PrintStream out){
        out.println(SET_TEXT_COLOR_BLUE + "Are you sure you want to leave the game?");
        if(confirm(in, out)){
            try{
                socket.leaveGame(authToken, currentGameID);
                currentGameID = 0;
                currentGame = null;
                perspective = null;
                socket = null;
                out.println(SET_TEXT_COLOR_BLUE + "You left the game. Returning to previous menu...");
            } catch (IOException e) {
                out.println(SET_TEXT_COLOR_RED + "Sorry, an error occurred. If the game needed to be opened for a "
                        + "different player, use the JoinGame option again. Returning to previous menu..."
                        + RESET_TEXT_COLOR);
            }
        }
    }

    public static void highlightLegalMoves(Scanner in, PrintStream out){
        ChessPosition position = getChessPosition(in, out,
                "Please enter the position of the piece whose legal moves you would like to highlight.");
        if (position == null) {
            return;
        }
        drawBoard(out, position);
    }

    private static ChessPosition getChessPosition(Scanner in, PrintStream out, String initialPrompt) {
        boolean repeat;
        ChessPosition position;
        do{
            repeat = false;
            out.print(SET_TEXT_COLOR_BLUE + initialPrompt + "\n" + """
                    Please follow the format 'a1' or 'A1":
                    [piece position] >>>\s""" + RESET_TEXT_COLOR);
            String temp = in.nextLine();
            if("exit".equals(temp.toLowerCase(Locale.ROOT))){
                out.println(SET_TEXT_COLOR_BLUE + "Canceling operation." + RESET_TEXT_COLOR);
                return null;
            }
            if(temp.isBlank()){
                repeat = true;
                out.println(SET_TEXT_COLOR_BLUE + """
                        "Sorry, this position is required for this operation.
                        If you want to cancel the operation, please type 'exit'.""" + RESET_TEXT_COLOR);
            }
            position = extractPosition(temp);
            if(position == null){
                repeat = true;
                out.println(SET_TEXT_COLOR_BLUE + """
                        "Sorry, your input could not be recognized. Please try again.
                        If you want to cancel the operation, please type 'exit'.""" + RESET_TEXT_COLOR);
            }
        }while(repeat);
        return position;
    }

    private static ChessPosition extractPosition(String position){
        if(position.length() != 2){
            return null;
        }
        Map<Character, Integer> chars = Map.of(
                'a', 1, 'b', 2, 'c', 3, 'd', 4,
                'e', 5, 'f', 6, 'g', 7, 'h', 8);
        Map<Character, Integer> nums = Map.of(
                '1', 1, '2', 2, '3', 3, '4', 4,
                '5', 5, '6', 6, '7', 7, '8', 8);
        Integer row = chars.get(position.toLowerCase(Locale.ROOT).charAt(0));
        Integer col = nums.get(position.toLowerCase(Locale.ROOT).charAt(1));
        if(row == null || col == null){
            return null;
        }
        return new ChessPosition(row, col);
    }

    private static ChessPiece.PieceType getPromotionPiece(Scanner in, PrintStream out){
        boolean repeat;
        ChessPiece.PieceType promo;
        do{
            repeat = false;
            out.print(SET_TEXT_COLOR_BLUE + """
                    It looks like you're promoting a pawn! Please enter the type of piece you want it to become.
                    It can become anything besides a King or a pawn.
                    Please type the full name of the new piece type or its first initial
                    (use K for Knight - it can't be confused with a King :).
                    
                    [piece type] >>>\s""" + RESET_TEXT_COLOR);
            String temp = in.nextLine().toLowerCase(Locale.ROOT);
            if("exit".equals(temp)){
                out.println(SET_TEXT_COLOR_BLUE + "Canceling operation." + RESET_TEXT_COLOR);
                return null;
            }
            if(temp.isBlank()){
                repeat = true;
                out.println(SET_TEXT_COLOR_BLUE + """
                        "Sorry, a promotion piece type is required for this move.
                        If you want to cancel the operation, please type 'exit'.""" + RESET_TEXT_COLOR);
            }
            promo = switch(temp.charAt(0)){
                case 'q' -> ChessPiece.PieceType.QUEEN;
                case 'r' -> ChessPiece.PieceType.ROOK;
                case 'k' -> ChessPiece.PieceType.KNIGHT;
                case 'b' -> ChessPiece.PieceType.BISHOP;
                default -> null;
            };
            if(promo == null){
                repeat = true;
                out.println(SET_TEXT_COLOR_BLUE + """
                        "Sorry, your input could not be recognized. Please try again.
                        If you want to cancel the operation, please type 'exit'.""" + RESET_TEXT_COLOR);
            }
        }while(repeat);
        return promo;
    }

    protected static void drawBoard(PrintStream out, ChessPosition highlightMoves){
        if(currentGame == null){
            throw new RuntimeException("Error: current game is not set.");
        }
        if(perspective == null){
            throw new RuntimeException("Error: game perspective is not set.");
        }
        List<String> rows = new ArrayList<>(Arrays.asList(getRowStrings(highlightMoves)));
        if(perspective == ChessGame.TeamColor.BLACK){
            rows = rows.reversed();
        }
        out.println();
        for(String row:rows){
            out.print(row);
        }
    }

    private static String[] getRowStrings(ChessPosition highlightMoves){
        String[][][] squares = getSquares(highlightMoves);
        String[] rows = new String[10];
        for(int i = 0; i < 10; i++){
            rows[i] = getRowString(squares[i]);
        }
        return rows;
    }

    private static String[][][] getSquares(ChessPosition highlightMoves){
        String[][][] out = new String[10][10][];
        String[] rowLabels = new String[]{ONE_LABEL, TWO_LABEL, THREE_LABEL, FOUR_LABEL, FIVE_LABEL, SIX_LABEL,
                SEVEN_LABEL, EIGHT_LABEL};
        String[] columnLabels = new String[]{A_LABEL, B_LABEL, C_LABEL, D_LABEL, E_LABEL, F_LABEL, G_LABEL, H_LABEL};
        for(int i = 8; i > 0; i --){
            String[] row = getLabelStrings(rowLabels[i-1]);
            String[] col = getLabelStrings(columnLabels[i-1]);
            out[i][0] = out[i][9] = row;
            out[0][i] = out[9][i] = col;
        }
        out[0][0] = out[0][9] = out[9][0] = out[9][9] = getLabelStrings(EMPTY);
        Set<ChessPosition> toHighlight = new HashSet<>();
        for(ChessMove move : currentGame.validMoves(highlightMoves)){
            toHighlight.add(move.getEndPosition());
        }
        for(int i = 1; i < 9; i ++){
            for(int j = 1; j < 9; j ++){
                String squareColor = BLACK_SQUARE_COLOR; // Dark square
                if((i + j) % 2 == 0){
                    squareColor = WHITE_SQUARE_COLOR; // Light square
                }
                if(toHighlight.contains(new ChessPosition(i, j))){ //Highlighted square
                    squareColor = HIGHLIGHT_SQUARE_COLOR;
                }
                out[i][j] = getSquareStrings(squareColor, currentGame.getBoard().getPiece(new ChessPosition(i, j)));
            }
        }
        return out;
    }

    private static String[] getLabelStrings(String label){
        return new String[]{SET_BG_COLOR_BLACK + EMPTY, SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + label,
                SET_BG_COLOR_BLACK + EMPTY};
    }

    private static String[] getSquareStrings(String squareColor, ChessPiece piece){
        String pieceRep = squareColor + getPieceString(piece);
        String rowBorder = squareColor + EMPTY;
        return new String[]{rowBorder, pieceRep, rowBorder};
    }

    private static String getPieceString(ChessPiece piece){
        if(piece == null){
            return EMPTY;
        }
        String[] blackStrings = new String[]{
                BLACK_KING, BLACK_QUEEN, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK, BLACK_PAWN};
        String[] whiteStrings = new String[]{
                WHITE_KING, WHITE_QUEEN, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK, WHITE_PAWN};
        for(int i = 0; i < blackStrings.length; i++){
            blackStrings[i] = BLACK_PIECE_COLOR + blackStrings[i];
            whiteStrings[i] = WHITE_PIECE_COLOR + whiteStrings[i];
        }
        String[] toUse = switch(piece.getTeamColor()){
            case WHITE -> whiteStrings;
            case BLACK -> blackStrings;
        };
        return toUse[piece.getPieceType().ordinal()];

    }

    private static String getRowString(String[][] squares){
        StringBuilder[] builders = new StringBuilder[]{
                new StringBuilder(), new StringBuilder(), new StringBuilder()};
        for (String[] square : squares) {
            for (int j = 0; j < 3; j++) {
                builders[j].append(square[j]);
            }
        }
        for(StringBuilder builder:builders){
            builder.append("\n" + RESET_BG_COLOR);
        }
        builders[0].append(builders[1]).append(builders[2]);
        return builders[0].toString();
    }
}
