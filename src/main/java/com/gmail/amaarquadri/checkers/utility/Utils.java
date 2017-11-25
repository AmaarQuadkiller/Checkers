package com.gmail.amaarquadri.checkers.utility;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.gmail.amaarquadri.checkers.logic.CheckerBoardSquare;
import com.gmail.amaarquadri.checkers.logic.DetailedMove;
import com.gmail.amaarquadri.checkers.logic.Move;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Amaar on 2016-06-15.
 * This class holds various utility methods.
 */
public class Utils {
    /**
     * Prevents Utils initialization.
     */
    private Utils() {
        throw new AssertionError();
    }


    /**
     * Holds the last shown Toast.
     */
    private static Toast lastCreatedToast;

    /**
     * Creates a Toast using Toast.makeText();
     * Shows the Toast after canceling any currently showing Toasts (that were shown using this method).
     * This prevents Toasts from being queued and being shown long after the intended time.
     *
     * @param context The parent Context.
     * @param resId The resource id of the string resource to use.
     */
    public static void showToast(Context context, @StringRes int resId) {
        clearToasts();
        lastCreatedToast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        lastCreatedToast.show();
    }

    /**
     * Cancels any Toasts that were shown using the showToast() method.
     */
    public static void clearToasts() {
        if (lastCreatedToast != null) lastCreatedToast.cancel();
    }

    /**
     * Clears all the elements in a Stack.
     *
     * @param stack The Stack to be cleared.
     */
    public static void clearStack(Stack stack) {
        while (!stack.empty()) stack.pop();
    }

    /**
     * Creates an 8x8 char[][] where each char is the state of the corresponding CheckerBoardSquare.
     *
     * @param checkerBoardSquares The CheckerBoardSquare[][] that is being used to create the char[][].
     * @return The resulting 8x8 char[][].
     */
    public static char[][] toCharArray(CheckerBoardSquare[][] checkerBoardSquares) {
        char[][] board = new char[8][8];
        for (int i = 0; i < 8; i++) for (int j = 0; j < 8; j++)
            board[i][j] = checkerBoardSquares[i][j].getState();
        return board;
    }

    /**
     * Creates an 8x8 char[][] where each char is the state of the corresponding CheckerBoardSquare.
     *
     * @param checkerBoardSquares The CheckerBoardSquare[][] that is being used to create the char[][].
     * @return The resulting 8x8 char[][].
     */
    public static char[][] toCharArray(com.gmail.amaarquadri.checkers.experimental.CheckerBoardSquare[][] checkerBoardSquares) {
        char[][] board = new char[8][8];
        for (int i = 0; i < 8; i++) for (int j = 0; j < 8; j++)
            board[i][j] = checkerBoardSquares[i][j].getState();
        return board;
    }

    /**
     * Clones an 8 by 8 char[][].
     *
     * @param board The 8 by 8 char[][] to be cloned.
     * @return The cloned char[][].
     */
    private static char[][] clone(char[][] board) {
        char[][] clone = new char[8][8];
        for (int i = 0; i < 8; i++) System.arraycopy(board[i], 0, clone[i], 0, 8);
        return clone;
    }


    //BASIC CHECKERS LOGICAL OPERATORS


    /**
     *
     * @param c The char to test.
     * @return Whether or not the given char represents an empty square.
     */
    private static boolean isEmpty(char c) {
        return c == 'E';
    }

    /**
     * @param board The board to be tested.
     * @param i The horizontal position to be tested.
     * @param j The vertical position to be tested.
     * @return True if the position specified is valid and the corresponding square is not empty.
     */
    private static boolean isValidAndEmpty(char[][] board, int i, int j) {
        return i >= 0 && i < 8 && j >= 0 && j < 8 && board[i][j] == 'E';
    }

    /**
     * @param c The char to test.
     * @return Whether or not the given char represents a red piece.
     */
    private static boolean isRed(char c) {
        return c == 'r' || c == 'R';
    }

    /**
     * @param c The char to test.
     * @return Whether or not the given char represents a king.
     */
    private static boolean isKing(char c) {
        return c == 'R' || c == 'W';
    }

    /**
     * Returns the given char after accounting for whether or not it needs to be kinged.
     *
     * @param c The char to test.
     * @param j The vertical position of the piece.
     * @return The kinged version of the char if it needs to be kinged. Otherwise, the char itself is returned.
     */
    private static char checkKing(char c, int j) {
        if (j == 0 && c == 'r') return 'R';
        if (j == 7 && c == 'w') return 'W';
        return c;
    }

    /**
     * @param c1 The first char.
     * @param c2 The second char.
     * @return Whether or not the two chars represent pieces that are enemies of each other.
     */
    private static boolean isEnemy(char c1, char c2) {
        return c1 != 'E' && c2 != 'E' && (c1 == 'r' || c1 == 'R') != (c2 == 'r' || c2 == 'R');
    }


    //HARDCORE CHECKERS LOGIC
    //ALL OF THESE METHODS REQUIRED A CHECKERBOARD CONSISTING ONLY OF 'r' 'w' 'R' 'W' and 'E' (i.e. no highlighting or black circles)


    /**
     * Returns whether or not the player whose turn it is can make a Move.
     * If this returns false, then the game is over and the player whose turn it is has lost.
     *
     * @param isRedTurn Whether or not it is red's turn.
     * @param board The board on which to perform the operations.
     * @return Whether or not the player whose turn it is can make a Move.
     */
    public static boolean canMove(boolean isRedTurn, char[][] board) {
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2) {
            char c = board[i][j];

            //if it is not the piece's turn do nothing
            if (isEmpty(c) || isRed(c) != isRedTurn) continue;

            //if the piece can move up
            if (c != 'w') {
                //check move to top left
                if (isValidAndEmpty(board, i - 1, j - 1)) return true;

                //check move to top right
                if (isValidAndEmpty(board, i + 1, j - 1)) return true;

                //check kill to top left
                if (isValidAndEmpty(board, i - 2, j - 2) && isEnemy(c, board[i - 1][j - 1])) return true;

                //check kill to top right
                if (isValidAndEmpty(board, i + 2, j - 2) && isEnemy(c, board[i + 1][j - 1])) return true;

                //check butterfly killing if it is enabled
                if (DataAccessor.isButterflyKillingEnabled()) {
                    //check upwards kill from left side of the board
                    if (i == 1 && isValidAndEmpty(board, 1, j - 2) && isEnemy(c, board[0][j - 1])) return true;

                    //check upwards kill from right side of the board
                    if (i == 6 && isValidAndEmpty(board, 6, j - 2) && isEnemy(c, board[7][j - 1])) return true;
                }
            }

            //if the piece can move down
            if (c != 'r') {
                //check moves to the bottom left
                if (isValidAndEmpty(board, i - 1, j + 1)) return true;

                //check moves to the top left
                if (isValidAndEmpty(board, i + 1, j + 1)) return true;

                //check kill to bottom left
                if (isValidAndEmpty(board, i - 2, j + 2) && isEnemy(c, board[i - 1][j + 1])) return true;

                //check kill to bottom right
                if (isValidAndEmpty(board, i + 2, j + 2) && isEnemy(c, board[i + 1][j + 1])) return true;

                //check butterfly killing if it is enabled
                if (DataAccessor.isButterflyKillingEnabled()) {
                    //check downwards kill from left side of the board
                    if (i == 1 && isValidAndEmpty(board, 1, j + 2) && isEnemy(c, board[0][j + 1])) return true;

                    //check downwards kill from right side of the board
                    if (i == 6 && isValidAndEmpty(board, 6, j + 2) && isEnemy(c, board[7][j + 1])) return true;
                }
            }
        }
        //if none of the above if statements evaluated to true for any of the squares on the board
        return false;
    }

    /**
     * Creates and returns an ArrayList<Move> containing all the possible Moves that can be made on the given board, given whose turn it is.
     * Used exclusively in Move.getValue().
     * This is because it does not create DetailedMoves, which make reference to all the intermediateBoards.
     * This removes overhead, and is acceptable because the intermediateBoards have no effect on the value of a Move.
     *
     * @param isRedTurn Whether or not it is red's turn.
     * @param board The board on which to perform the operations.
     * @return An ArrayList<Move> containing all the possible Moves that can be made on the given board, given whose turn it is.
     */
    public static ArrayList<Move> getAllPossibleMoves(boolean isRedTurn, char[][] board) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2) {
            char c = board[i][j];

            //if it is not the piece's turn do nothing
            if (isEmpty(c) || isRed(c) != isRedTurn) continue;

            //if the piece can move up
            if (c != 'w') {
                //check move to top left
                if (isValidAndEmpty(board, i - 1, j - 1)) {
                    char[][] clone = clone(board);
                    clone[i - 1][j - 1] = checkKing(c, j - 1);
                    clone[i][j] = 'E';
                    moves.add(new Move(clone));
                }

                //check move to top right
                if (isValidAndEmpty(board, i + 1, j - 1)) {
                    char[][] clone = clone(board);
                    clone[i + 1][j - 1] = checkKing(c, j - 1);
                    clone[i][j] = 'E';
                    moves.add(new Move(clone));
                }

                checkKillsAbove(board, i, j, moves);
            }

            //if the piece can move down
            if (c != 'r') {
                //check moves to the bottom left
                if (isValidAndEmpty(board, i - 1, j + 1)) {
                    char[][] clone = clone(board);
                    clone[i - 1][j + 1] = checkKing(c, j + 1);
                    clone[i][j] = 'E';
                    moves.add(new Move(clone));
                }

                //check moves to the top left
                if (isValidAndEmpty(board, i + 1, j + 1)) {
                    char[][] clone = clone(board);
                    clone[i + 1][j + 1] = checkKing(c, j + 1);
                    clone[i][j] = 'E';
                    moves.add(new Move(clone));
                }

                checkKillsBelow(board, i, j, moves);
            }

            //if the piece is a king and kings are allowed to fly
            if (isKing(c) && DataAccessor.areFlyingKingsEnabled()) {
                //check flying moves to the top left (only if the CheckerBoardSquare immediately to the top left is empty)
                //n represents the number of squares to the top left
                //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
                if (isValidAndEmpty(board, i - 1, j - 1)) for (int n = 2; n < 8; n++) {
                    int destI = i - n, destJ = j - n;
                    if (isValidAndEmpty(board, destI, destJ)) {
                        char[][] clone = clone(board);
                        clone[destI][destJ] = c;
                        clone[i][j] = 'E';
                        moves.add(new Move(clone));
                    }
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                    else break;
                }

                //check flying moves to the top right (only if the CheckerBoardSquare immediately to the top right is empty)
                //n represents the number of squares to the top right
                //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
                if (isValidAndEmpty(board, i + 1, j - 1)) for (int n = 2; n < 8; n++) {
                    int destI = i + n, destJ = j - n;
                    if (isValidAndEmpty(board, destI, destJ)) {
                        char[][] clone = clone(board);
                        clone[destI][destJ] = c;
                        clone[i][j] = 'E';
                        moves.add(new Move(clone));
                    }
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                    else break;
                }

                //check flying moves to the bottom left (only if the CheckerBoardSquare immediately to the bottom left is empty)
                //n represents the number of squares to the bottom left
                //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
                if (isValidAndEmpty(board, i - 1, j + 1)) for (int n = 2; n < 8; n++) {
                    int destI = i - n, destJ = j + n;
                    if (isValidAndEmpty(board, destI, destJ)) {
                        char[][] clone = clone(board);
                        clone[destI][destJ] = c;
                        clone[i][j] = 'E';
                        moves.add(new Move(clone));
                    }
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                    else break;
                }

                //check flying moves to the bottom right (only if the CheckerBoardSquare immediately to the bottom right is empty)
                //n represents the number of squares to the bottom right
                //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
                if (isValidAndEmpty(board, i + 1, j + 1)) for (int n = 2; n < 8; n++) {
                    int destI = i + n, destJ = j + n;
                    if (isValidAndEmpty(board, destI, destJ)) {
                        char[][] clone = clone(board);
                        clone[destI][destJ] = c;
                        clone[i][j] = 'E';
                        moves.add(new Move(clone));
                    }
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                    else break;
                }
            }
        }
        return moves;
    }

    /**
     * Checks for any kills that the selected piece can make in the upward direction.
     * Only used non-recursively in getAllPossibleMoves().
     *
     * @param board The board on which to perform the operations.
     * @param i The horizontal position of the selected piece.
     * @param j The vertical position of the selected piece.
     * @param moves An ArrayList<Move> to add any new Moves to.
     */
    private static void checkKillsAbove(char[][] board, int i, int j, ArrayList<Move> moves) {
        char c = board[i][j];
        //prevents killing in the same move as getting a king, unless killAfterKingingEnabled is set to true
        boolean canContinueMove = j != 2 || isKing(c) || DataAccessor.isKillAfterKingingEnabled();

        //check kill to top left
        if (isValidAndEmpty(board, i - 2, j - 2) && isEnemy(c, board[i - 1][j - 1])) {
            char[][] clone = clone(board);
            clone[i - 2][j - 2] = checkKing(c, j - 2);
            clone[i - 1][j - 1] = clone[i][j] = 'E';
            moves.add(new Move(clone));

            if (canContinueMove) {
                checkKillsAbove(clone, i - 2, j - 2, moves);
                if (isKing(clone[i - 2][j - 2])) checkKillsBelow(clone, i - 2, j - 2, moves);
            }
        }

        //check kill to top right
        if (isValidAndEmpty(board, i + 2, j - 2) && isEnemy(c, board[i + 1][j - 1])) {
            char[][] clone = clone(board);
            clone[i + 2][j - 2] = checkKing(c, j - 2);
            clone[i + 1][j - 1] = clone[i][j] = 'E';
            moves.add(new Move(clone));

            if (canContinueMove) {
                checkKillsAbove(clone, i + 2, j - 2, moves);
                if (isKing(clone[i + 2][j - 2])) checkKillsBelow(clone, i + 2, j - 2, moves);
            }
        }

        //check butterfly killing if it is enabled
        if (DataAccessor.isButterflyKillingEnabled()) {
            //check upwards kill from left side of the board
            if (i == 1 && isValidAndEmpty(board, 1, j - 2) && isEnemy(c, board[0][j - 1])) {
                char[][] clone = clone(board);
                clone[1][j - 2] = checkKing(c, j - 2);
                clone[0][j - 1] = clone[i][j] = 'E';
                moves.add(new Move(clone));

                if (canContinueMove) {
                    checkKillsAbove(clone, 1, j - 2, moves);
                    if (isKing(clone[1][j - 2])) checkKillsBelow(clone, 1, j - 2, moves);
                }
            }

            //check upwards kill from right side of the board
            if (i == 6 && isValidAndEmpty(board, 6, j - 2) && isEnemy(c, board[7][j - 1])) {
                char[][] clone = clone(board);
                clone[6][j - 2] = checkKing(c, j - 2);
                clone[7][j - 1] = clone[i][j] = 'E';
                moves.add(new Move(clone));

                if (canContinueMove) {
                    checkKillsAbove(clone, 6, j - 2, moves);
                    if (isKing(clone[6][j - 2])) checkKillsBelow(clone, 6, j - 2, moves);
                }
            }
        }
    }

    /**
     * Checks for any kills that the selected piece can make in the downward direction.
     * Only used non-recursively in getAllPossibleMoves().
     *
     * @param board The board on which to perform the operations.
     * @param i The horizontal position of the selected piece.
     * @param j The vertical position of the selected piece.
     * @param moves An ArrayList<Move> to add any new Moves to.
     */
    private static void checkKillsBelow(char[][] board, int i, int j, ArrayList<Move> moves) {
        char c = board[i][j];
        //prevents killing in the same move as getting a king, unless killAfterKingingEnabled is set to true
        boolean canContinueMove = j != 5 || isKing(c) || DataAccessor.isKillAfterKingingEnabled();

        //check kill to bottom left
        if (isValidAndEmpty(board, i - 2, j + 2) && isEnemy(c, board[i - 1][j + 1])) {
            char[][] clone = clone(board);
            clone[i - 2][j + 2] = checkKing(c, j + 2);
            clone[i - 1][j + 1] = clone[i][j] = 'E';
            moves.add(new Move(clone));

            if (canContinueMove) {
                checkKillsBelow(clone, i - 2, j + 2, moves);
                if (isKing(clone[i - 2][j + 2])) checkKillsAbove(clone, i - 2, j + 2, moves);
            }
        }

        //check kill to bottom right
        if (isValidAndEmpty(board, i + 2, j + 2) && isEnemy(c, board[i + 1][j + 1])) {
            char[][] clone = clone(board);
            clone[i + 2][j + 2] = checkKing(c, j + 2);
            clone[i + 1][j + 1] = clone[i][j] = 'E';
            moves.add(new Move(clone));

            if (canContinueMove) {
                checkKillsBelow(clone, i + 2, j + 2, moves);
                if (isKing(clone[i + 2][j + 2])) checkKillsAbove(clone, i + 2, j + 2, moves);
            }
        }

        //check butterfly killing if it is enabled
        if (DataAccessor.isButterflyKillingEnabled()) {
            //check downwards kill from left side of the board
            if (i == 1 && isValidAndEmpty(board, 1, j + 2) && isEnemy(c, board[0][j + 1])) {
                char[][] clone = clone(board);
                clone[1][j + 2] = checkKing(c, j + 2);
                clone[0][j + 1] = clone[i][j] = 'E';
                moves.add(new Move(clone));

                if (canContinueMove) {
                    checkKillsBelow(clone, 1, j + 2, moves);
                    if (isKing(clone[1][j + 2])) checkKillsAbove(clone, 1, j + 2, moves);
                }
            }

            //check downwards kill from right side of the board
            if (i == 6 && isValidAndEmpty(board, 6, j + 2) && isEnemy(c, board[7][j + 1])) {
                char[][] clone = clone(board);
                clone[6][j + 2] = checkKing(c, j + 2);
                clone[7][j + 1] = clone[i][j] = 'E';
                moves.add(new Move(clone));

                if (canContinueMove) {
                    checkKillsBelow(clone, 6, j + 2, moves);
                    if (isKing(clone[6][j + 2])) checkKillsAbove(clone, 6, j + 2, moves);
                }
            }
        }
    }

    /**
     * Creates and returns an ArrayList<DetailedMove> containing all the possible Moves that can be made on the given board, given whose turn it is.
     * Used exclusively in SinglePlayerCheckerBoard.makeAIMove().
     * This is because it creates DetailedMoves, which make reference to all the intermediateBoards.
     * This adds overhead, and is only necessary when the Move has a possibility of being executed.
     *
     * @param isRedTurn Whether or not it is red's turn.
     * @param board The board on which to perform the operations.
     * @return An ArrayList<DetailedMove> containing all the possible Moves that can be made on the given board, given whose turn it is.
     */
    public static ArrayList<DetailedMove> getAllPossibleDetailedMoves(boolean isRedTurn, char[][] board) {
        ArrayList<DetailedMove> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2) {
            char c = board[i][j];

            //if it is not the piece's turn do nothing
            if (isEmpty(c) || isRed(c) != isRedTurn) continue;

            //if the piece can move up
            if (c != 'w') {
                //check move to top left
                if (isValidAndEmpty(board, i - 1, j - 1)) {
                    char[][] clone = clone(board);
                    clone[i - 1][j - 1] = checkKing(c, j - 1);
                    clone[i][j] = 'E';
                    moves.add(new DetailedMove(clone));
                }

                //check move to top right
                if (isValidAndEmpty(board, i + 1, j - 1)) {
                    char[][] clone = clone(board);
                    clone[i + 1][j - 1] = checkKing(c, j - 1);
                    clone[i][j] = 'E';
                    moves.add(new DetailedMove(clone));
                }

                checkDetailedKillsAbove(board, i, j, moves, new ArrayList<char[][]>());
            }

            //if the piece can move down
            if (c != 'r') {
                //check moves to the bottom left
                if (isValidAndEmpty(board, i - 1, j + 1)) {
                    char[][] clone = clone(board);
                    clone[i - 1][j + 1] = checkKing(c, j + 1);
                    clone[i][j] = 'E';
                    moves.add(new DetailedMove(clone));
                }

                //check moves to the top left
                if (isValidAndEmpty(board, i + 1, j + 1)) {
                    char[][] clone = clone(board);
                    clone[i + 1][j + 1] = checkKing(c, j + 1);
                    clone[i][j] = 'E';
                    moves.add(new DetailedMove(clone));
                }

                checkDetailedKillsBelow(board, i, j, moves, new ArrayList<char[][]>());
            }

            //if the piece is a king and kings are allowed to fly
            if (isKing(c) && DataAccessor.areFlyingKingsEnabled()) {
                //check flying moves to the top left (only if the CheckerBoardSquare immediately to the top left is empty)
                //n represents the number of squares to the top left
                //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
                if (isValidAndEmpty(board, i - 1, j - 1)) for (int n = 2; n < 8; n++) {
                    int destI = i - n, destJ = j - n;
                    if (isValidAndEmpty(board, destI, destJ)) {
                        char[][] clone = clone(board);
                        clone[destI][destJ] = c;
                        clone[i][j] = 'E';
                        moves.add(new DetailedMove(clone));
                    }
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                    else break;
                }

                //check flying moves to the top right (only if the CheckerBoardSquare immediately to the top right is empty)
                //n represents the number of squares to the top right
                //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
                if (isValidAndEmpty(board, i + 1, j - 1)) for (int n = 2; n < 8; n++) {
                    int destI = i + n, destJ = j - n;
                    if (isValidAndEmpty(board, destI, destJ)) {
                        char[][] clone = clone(board);
                        clone[destI][destJ] = c;
                        clone[i][j] = 'E';
                        moves.add(new DetailedMove(clone));
                    }
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                    else break;
                }

                //check flying moves to the bottom left (only if the CheckerBoardSquare immediately to the bottom left is empty)
                //n represents the number of squares to the bottom left
                //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
                if (isValidAndEmpty(board, i - 1, j + 1)) for (int n = 2; n < 8; n++) {
                    int destI = i - n, destJ = j + n;
                    if (isValidAndEmpty(board, destI, destJ)) {
                        char[][] clone = clone(board);
                        clone[destI][destJ] = c;
                        clone[i][j] = 'E';
                        moves.add(new DetailedMove(clone));
                    }
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                    else break;
                }

                //check flying moves to the bottom right (only if the CheckerBoardSquare immediately to the bottom right is empty)
                //n represents the number of squares to the bottom right
                //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
                if (isValidAndEmpty(board, i + 1, j + 1)) for (int n = 2; n < 8; n++) {
                    int destI = i + n, destJ = j + n;
                    if (isValidAndEmpty(board, destI, destJ)) {
                        char[][] clone = clone(board);
                        clone[destI][destJ] = c;
                        clone[i][j] = 'E';
                        moves.add(new DetailedMove(clone));
                    }
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                    else break;
                }
            }
        }
        return moves;
    }

    /**
     * Checks for any kills that the selected piece can make in the upward direction.
     * Only used non-recursively in getAllPossibleDetailedMoves().
     *
     * @param board The board on which to perform the operations.
     * @param i The horizontal position of the selected piece.
     * @param j The vertical position of the selected piece.
     * @param moves An ArrayList<DetailedMove> to add any new Moves to.
     * @param prefix A set of predetermined, intermediate states for any Moves that are generated.
     */
    private static void checkDetailedKillsAbove(char[][] board, int i, int j, ArrayList<DetailedMove> moves,
                                        ArrayList<char[][]> prefix) {
        char c = board[i][j];
        //prevents killing in the same move as getting a king, unless killAfterKingingEnabled is set to true
        boolean canContinueMove = j != 2 || isKing(c) || DataAccessor.isKillAfterKingingEnabled();

        //check kill to top left
        if (isValidAndEmpty(board, i - 2, j - 2) && isEnemy(c, board[i - 1][j - 1])) {
            char[][] clone = clone(board);
            clone[i - 2][j - 2] = checkKing(c, j - 2);
            clone[i - 1][j - 1] = clone[i][j] = 'E';
            moves.add(new DetailedMove(prefix, clone));

            if (canContinueMove) {
                ArrayList<char[][]> newPrefix = new ArrayList<>(prefix);
                newPrefix.add(clone);
                checkDetailedKillsAbove(clone, i - 2, j - 2, moves, newPrefix);
                if (isKing(clone[i - 2][j - 2])) checkDetailedKillsBelow(clone, i - 2, j - 2, moves, newPrefix);
            }
        }

        //check kill to top right
        if (isValidAndEmpty(board, i + 2, j - 2) && isEnemy(c, board[i + 1][j - 1])) {
            char[][] clone = clone(board);
            clone[i + 2][j - 2] = checkKing(c, j - 2);
            clone[i + 1][j - 1] = clone[i][j] = 'E';
            moves.add(new DetailedMove(prefix, clone));

            if (canContinueMove) {
                ArrayList<char[][]> newPrefix = new ArrayList<>(prefix);
                newPrefix.add(clone);
                checkDetailedKillsAbove(clone, i + 2, j - 2, moves, newPrefix);
                if (isKing(clone[i + 2][j - 2])) checkDetailedKillsBelow(clone, i + 2, j - 2, moves, newPrefix);
            }
        }

        //check butterfly killing if it is enabled
        if (DataAccessor.isButterflyKillingEnabled()) {
            //check upwards kill from left side of the board
            if (i == 1 && isValidAndEmpty(board, 1, j - 2) && isEnemy(c, board[0][j - 1])) {
                char[][] clone = clone(board);
                clone[1][j - 2] = checkKing(c, j - 2);
                clone[0][j - 1] = clone[i][j] = 'E';
                moves.add(new DetailedMove(prefix, clone));

                if (canContinueMove) {
                    ArrayList<char[][]> newPrefix = new ArrayList<>(prefix);
                    newPrefix.add(clone);
                    checkDetailedKillsAbove(clone, 1, j - 2, moves, newPrefix);
                    if (isKing(clone[1][j - 2])) checkDetailedKillsBelow(clone, 1, j - 2, moves, newPrefix);
                }
            }

            //check upwards kill from right side of the board
            if (i == 6 && isValidAndEmpty(board, 6, j - 2) && isEnemy(c, board[7][j - 1])) {
                char[][] clone = clone(board);
                clone[6][j - 2] = checkKing(c, j - 2);
                clone[7][j - 1] = clone[i][j] = 'E';
                moves.add(new DetailedMove(prefix, clone));

                if (canContinueMove) {
                    ArrayList<char[][]> newPrefix = new ArrayList<>(prefix);
                    newPrefix.add(clone);
                    checkDetailedKillsAbove(clone, 6, j - 2, moves, newPrefix);
                    if (isKing(clone[6][j - 2])) checkDetailedKillsBelow(clone, 6, j - 2, moves, newPrefix);
                }
            }
        }
    }

    /**
     * Checks for any kills that the selected piece can make in the downward direction.
     * Only used non-recursively in getAllPossibleDetailedMoves().
     *
     * @param board The board on which to perform the operations.
     * @param i The horizontal position of the selected piece.
     * @param j The vertical position of the selected piece.
     * @param moves An ArrayList<DetailedMove> to add any new Moves to.
     * @param prefix A set of predetermined, intermediate states for any Moves that are generated.
     */
    private static void checkDetailedKillsBelow(char[][] board, int i, int j, ArrayList<DetailedMove> moves,
                                        ArrayList<char[][]> prefix) {
        char c = board[i][j];
        //prevents killing in the same move as getting a king, unless killAfterKingingEnabled is set to true
        boolean canContinueMove = j != 5 || isKing(c) || DataAccessor.isKillAfterKingingEnabled();

        //check kill to bottom left
        if (isValidAndEmpty(board, i - 2, j + 2) && isEnemy(c, board[i - 1][j + 1])) {
            char[][] clone = clone(board);
            clone[i - 2][j + 2] = checkKing(c, j + 2);
            clone[i - 1][j + 1] = clone[i][j] = 'E';
            moves.add(new DetailedMove(prefix, clone));

            if (canContinueMove) {
                ArrayList<char[][]> newPrefix = new ArrayList<>(prefix);
                newPrefix.add(clone);
                checkDetailedKillsBelow(clone, i - 2, j + 2, moves, newPrefix);
                if (isKing(clone[i - 2][j + 2])) checkDetailedKillsAbove(clone, i - 2, j + 2, moves, newPrefix);
            }
        }

        //check kill to bottom right
        if (isValidAndEmpty(board, i + 2, j + 2) && isEnemy(c, board[i + 1][j + 1])) {
            char[][] clone = clone(board);
            clone[i + 2][j + 2] = checkKing(c, j + 2);
            clone[i + 1][j + 1] = clone[i][j] = 'E';
            moves.add(new DetailedMove(prefix, clone));

            if (canContinueMove) {
                ArrayList<char[][]> newPrefix = new ArrayList<>(prefix);
                newPrefix.add(clone);
                checkDetailedKillsBelow(clone, i + 2, j + 2, moves, newPrefix);
                if (isKing(clone[i + 2][j + 2])) checkDetailedKillsAbove(clone, i + 2, j + 2, moves, newPrefix);
            }
        }

        //check butterfly killing if it is enabled
        if (DataAccessor.isButterflyKillingEnabled()) {
            //check downwards kill from left side of the board
            if (i == 1 && isValidAndEmpty(board, 1, j + 2) && isEnemy(c, board[0][j + 1])) {
                char[][] clone = clone(board);
                clone[1][j + 2] = checkKing(c, j + 2);
                clone[0][j + 1] = clone[i][j] = 'E';
                moves.add(new DetailedMove(prefix, clone));

                if (canContinueMove) {
                    ArrayList<char[][]> newPrefix = new ArrayList<>(prefix);
                    newPrefix.add(clone);
                    checkDetailedKillsBelow(clone, 1, j + 2, moves, newPrefix);
                    if (isKing(clone[1][j + 2])) checkDetailedKillsAbove(clone, 1, j + 2, moves, newPrefix);
                }
            }

            //check downwards kill from right side of the board
            if (i == 6 && isValidAndEmpty(board, 6, j + 2) && isEnemy(c, board[7][j + 1])) {
                char[][] clone = clone(board);
                clone[6][j + 2] = checkKing(c, j + 2);
                clone[7][j + 1] = clone[i][j] = 'E';
                moves.add(new DetailedMove(prefix, clone));

                if (canContinueMove) {
                    ArrayList<char[][]> newPrefix = new ArrayList<>(prefix);
                    newPrefix.add(clone);
                    checkDetailedKillsBelow(clone, 6, j + 2, moves, newPrefix);
                    if (isKing(clone[6][j + 2])) checkDetailedKillsAbove(clone, 6, j + 2, moves, newPrefix);
                }
            }
        }
    }
}
