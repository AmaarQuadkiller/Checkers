package com.gmail.amaarquadri.checkers.logic;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.gmail.amaarquadri.checkers.R;
import com.gmail.amaarquadri.checkers.utility.DataAccessor;
import com.gmail.amaarquadri.checkers.utility.Utils;

import java.util.Stack;

import static com.gmail.amaarquadri.checkers.logic.CheckerBoardSquare.BlackCircleData;

/**
 * Created by Amaar on 2016-05-30.
 * This class represents an interactive fully-functional CheckerBoard.
 * TODO: consider saving gameHistory/gameFuture in case the activity is destroyed by the system
 */
public class CheckerBoard extends LinearLayout {
    //STATIC MEMBERS


    /**
     * The String used to initialize a CheckerBoard to the starting state.
     */
    public static final String INITIAL_SERIALIZED_BOARD = "TwErrwwErwErrwwErwErrwwErwErrwwEr";

    /**
     * This interface provides a listener for when the game finishes.
     */
    public interface GameFinishedListener {
        /**
         * Method that is called when the game finishes.
         *
         * @param hasRedWon Whether or not the red player has won.
         */
        @UiThread
        void onGameFinished(boolean hasRedWon);
    }


    //INSTANCE VARIABLES


    /**
     * The parent Context.
     */
    protected final Context context;

    /**
     * A 8 by 8 CheckerBoardSquare[][] containing all the CheckerBoardSquares in this CheckerBoard.
     */
    protected CheckerBoardSquare[][] board;

    /**
     * A history of all the past moves that were made on this CheckerBoard.
     * The last item is the current state of the CheckerBoard.
     */
    private Stack<String> gameHistory;

    /**
     * A history of all future moves that were undone on this CheckerBoard.
     * The first item is the first future state of the CheckerBoard.
     */
    private Stack<String> gameFuture;

    /**
     * Whether or not this CheckerBoard is locked. If it is, all user input will be ignored.
     */
    private boolean isLocked;

    /**
     * Whether or not it is currently red's turn.
     */
    protected boolean isRedTurn;

    /**
     * A SnackBar to offer the user the opportunity to end their turn as opposed to performing a double kill.
     * Since SnackBars cannot be reused, a new instance will be created every time it is needed.
     * After the endTurnSnackBar is dismissed, endTurnSnackBar will be set to null.
     * Thus checking whether or not endTurnSnackBar == null can be used to determine if the game is awaiting double kill confirmation.
     */
    private Snackbar endTurnSnackBar;

    /**
     * A listener for when the game finishes.
     */
    private GameFinishedListener gameFinishedListener;


    //INITIALIZATION


    /**
     * Constructor used by XML.
     *
     * @param context The parent context.
     * @param attrs A collection of attributes.
     */
    public CheckerBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        board = new CheckerBoardSquare[8][8];
        gameHistory = new Stack<>();
        gameFuture = new Stack<>();
        isLocked = false;
        isRedTurn = true;
        endTurnSnackBar = null;
        gameFinishedListener = null;

        //construct the view hierarchy within this CheckerBoard (which is a LinearLayout)
        setOrientation(VERTICAL);
        //j goes first because change in rows represents the change in the vertical direction
        for (int j = 0; j < 8; j++) {
            //LinearLayout containing one row of CheckerBoardSquares
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(HORIZONTAL);
            for (int i = 0; i < 8; i++) {
                final int i_ = i, j_ = j; //bruh wheres the java 8
                CheckerBoardSquare square = new CheckerBoardSquare(context, (i + j) % 2 == 0);
                square.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleInput(i_, j_);
                    }
                });
                //the squares divide up the width evenly due to the weights
                //the square's height is irrelevant and will be based on its width during the measure phase.
                row.addView(square, new LayoutParams(0, -1, 1));
                board[i][j] = square;
            }
            //each row takes up as much width as it can, and only as much height as it needs
            addView(row, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    /**
     * Sets the gameFinishedListener that is necessary for proper functioning at the end of the game.
     * This method must be called before the first call to setState().
     *
     * @param gameFinishedListener A listener for when the game finishes.
     */
    public final void setGameFinishedListener(GameFinishedListener gameFinishedListener) {
        this.gameFinishedListener = gameFinishedListener;
    }

    /**
     * Initialize this CheckerBoard to a specified state.
     * The serializedBoard must be in the format constructed by getSerialization().
     * The serialized board cannot be inverted.
     *
     * @param serializedBoard A String representing the state to initialize this CheckerBoard to.
     */
    @CallSuper
    public void setState(String serializedBoard) {
        if (serializedBoard.length() != 33) throw new IllegalArgumentException();
        switch (serializedBoard.charAt(0)) {
            case 'T': isRedTurn = true; break;
            case 'F': isRedTurn = false; break;
            default: throw new IllegalArgumentException();
        }
        int count = 1;
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2) {
            char c = serializedBoard.charAt(count);
            count++;
            if (c == 'E' || c == 'r' || c == 'w' || c == 'R' || c == 'W') board[i][j].setState(c);
            else throw new IllegalArgumentException();
        }
        gameHistory.push(serializedBoard);
    }

    /**
     * Resets this CheckerBoard so that it can be used for a new game.
     */
    public void reset() {
        isRedTurn = true;
        int count = 1;
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2)
            board[i][j].setState(INITIAL_SERIALIZED_BOARD.charAt(count++));
        Utils.clearStack(gameHistory);
        Utils.clearStack(gameFuture);
        gameHistory.push(INITIAL_SERIALIZED_BOARD);
    }


    //CHECKERS LOGIC


    /**
     * Returns whether a red piece can move up.
     * This is identical to asking whether a white piece can move down.
     * This is the opposite of asking whether a red piece can move down.
     * This is the opposite of asking whether a white piece can move up.
     * This does not apply to kings because they can move in all directions.
     * This will need to be overwritten to return false if the board is inverted.
     *
     * @return Whether a red piece can move up.
     */
    protected boolean canRedPieceMoveUp() {
        return true;
    }

    /**
     * @param i The horizontal position of the square to be checked.
     * @param j The vertical position of the square to be checked.
     * @return True if the coordinates are valid and the square is empty. False otherwise.
     */
    private boolean isValidAndEmpty(int i, int j) {
        return i >= 0 && i < 8 && j >= 0 && j < 8 && board[i][j].isEmpty();
    }

    /**
     * Returns whether or not two pieces are enemies.
     * More specifically, it returns true if the second piece is not empty, and one of the two pieces is red and the other piece is white.
     * It is assumed that the first piece is not empty, and that the second piece has valid coordinates.
     *
     * @param selectedSquare The first CheckerBoardSquare that is being compared.
     * @param i The horizontal position of the second piece being compared.
     * @param j The vertical position of the second piece being compared.
     * @return Whether or not the two pieces are enemies.
     */
    private boolean areEnemies(CheckerBoardSquare selectedSquare, int i, int j) {
        return !board[i][j].isEmpty() && selectedSquare.isRed() != board[i][j].isRed();
    }

    /**
     * Reacts to the user selecting a specified square on the board.
     *
     * @param i The index going right from the left of the board.
     * @param j The index going down from the top of the board.
     */
    private void handleInput(int i, int j) {
        //don't do anything if the CheckerBoard is locked
        if (isLocked) return;

        //stored to avoid querying the CheckerBoardSquare[][] repeatedly
        CheckerBoardSquare selectedSquare = board[i][j];

        //if the game is waiting for the user to confirm a double kill (or cancel it)
        if (endTurnSnackBar != null) {
            //handle the input if it is to a black circle, otherwise ignore it
            if (selectedSquare.hasBlackCircle()) handleInputToBlackCircle(i, j);
            return;
        }

        //if the user picked a square that doesn't contain any piece
        if (selectedSquare.isEmpty()) {
            //if it holds a black circle, handle the input, otherwise deselect everything
            if (selectedSquare.hasBlackCircle()) handleInputToBlackCircle(i, j);
            else deselectEverything();
            return;
        }

        //if the user picked the piece that is already highlighted deselect it
        if (selectedSquare.isHighlighted()) {
            deselectEverything();
            return;
        }

        //if the user picked a piece whose turn it isn't
        if (isRedTurn != selectedSquare.isRed()) {
            //deselect everything and notify the user
            deselectEverything();
            Utils.showToast(context, isRedTurn ? R.string.reds_turn : R.string.whites_turn);
            return;
        }

        //if the user picked a piece whose turn it is

        //deselect everything, and highlight the piece
        deselectEverything();
        selectedSquare.setHighlighted(true);

        //if the piece can move upwards, show any moves it can make upwards
        if (selectedSquare.isRed() == canRedPieceMoveUp() || selectedSquare.isKing()) {
            //check move to top left
            if (isValidAndEmpty(i - 1, j - 1))
                board[i - 1][j - 1].setBlackCircleData(new BlackCircleData(i, j, false));

            //check move to top right
            if (isValidAndEmpty(i + 1, j - 1))
                board[i + 1][j - 1].setBlackCircleData(new BlackCircleData(i, j, false));

            checkKillsAbove(i, j);
        }

        //if the piece can move downwards, show any moves it can make downwards
        if (selectedSquare.isRed() != canRedPieceMoveUp() || selectedSquare.isKing()) {
            //check move to bottom left
            if (isValidAndEmpty(i - 1, j + 1))
                board[i - 1][j + 1].setBlackCircleData(new BlackCircleData(i, j, false));

            //check move to bottom right
            if (isValidAndEmpty(i + 1, j + 1))
                board[i + 1][j + 1].setBlackCircleData(new BlackCircleData(i, j, false));

            checkKillsBelow(i, j);
        }

        //if the piece is a king and kings are allowed to fly
        if (selectedSquare.isKing() && DataAccessor.areFlyingKingsEnabled()) {
            //check flying moves to the top left (only if the CheckerBoardSquare immediately to the top left is empty)
            //n represents the number of squares to the top left
            //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
            if (isValidAndEmpty(i - 1, j - 1)) for (int n = 2; n < 8; n++) {
                int destI = i - n, destJ = j - n;
                if (isValidAndEmpty(destI, destJ))
                    board[destI][destJ].setBlackCircleData(new BlackCircleData(i, j, false));
                //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                else break;
            }

            //check flying moves to the top right (only if the CheckerBoardSquare immediately to the top right is empty)
            //n represents the number of squares to the top right
            //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
            if (isValidAndEmpty(i + 1, j - 1)) for (int n = 2; n < 8; n++) {
                int destI = i + n, destJ = j - n;
                if (isValidAndEmpty(destI, destJ))
                    board[destI][destJ].setBlackCircleData(new BlackCircleData(i, j, false));
                //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                else break;
            }

            //check flying moves to the bottom left (only if the CheckerBoardSquare immediately to the bottom left is empty)
            //n represents the number of squares to the bottom left
            //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
            if (isValidAndEmpty(i - 1, j + 1)) for (int n = 2; n < 8; n++) {
                int destI = i - n, destJ = j + n;
                if (isValidAndEmpty(destI, destJ))
                    board[destI][destJ].setBlackCircleData(new BlackCircleData(i, j, false));
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                else break;
            }

            //check flying moves to the bottom right (only if the CheckerBoardSquare immediately to the bottom right is empty)
            //n represents the number of squares to the bottom right
            //n ranges from 2 to 7 because 1 was already accounted for above and 7 is the maximum possible (corner to corner)
            if (isValidAndEmpty(i + 1, j + 1)) for (int n = 2; n < 8; n++) {
                int destI = i + n, destJ = j + n;
                if (isValidAndEmpty(destI, destJ))
                    board[destI][destJ].setBlackCircleData(new BlackCircleData(i, j, false));
                    //if the king cannot fly to this CheckerBoardSquare, then it cannot fly further in this direction
                else break;
            }
        }
    }

    /**
     * Reacts to the user confirming their move by selecting a square with a black circle.
     * This is a sub-method of handleInput(int, int).
     *
     * @param i The index going right from the left of the board.
     * @param j The index going down from the top of the board.
     */
    private void handleInputToBlackCircle(int i , int j) {
        //stored to avoid querying the CheckerBoardSquare[][] repeatedly
        CheckerBoardSquare selectedSquare = board[i][j];

        //implement the changes from the black circle data
        BlackCircleData data = selectedSquare.getBlackCircleData();
        //start square is where the piece started before making this move
        CheckerBoardSquare startSquare = board[data.getStartI()][data.getStartJ()];
        selectedSquare.setPiece(startSquare.isRed(), startSquare.isKing() || j == 0 || j == 7); //make king if necessary
        startSquare.setEmpty();
        if (data.isKill()) board[data.getKillI()][data.getKillJ()].setEmpty();

        //get rid of any other black circles
        deselectEverything();

        //if the previous move allows for the possibility that the user can double kill
        if (data.canContinueMove()) {
            //show any of the kills that the piece can make
            if (selectedSquare.isRed() == canRedPieceMoveUp() || selectedSquare.isKing()) checkKillsAbove(i, j);
            if (selectedSquare.isRed() != canRedPieceMoveUp() || selectedSquare.isKing()) checkKillsBelow(i, j);

            //if there is a black circle (i.e. there is a possible double kill)
            for (int x = 0; x < 8; x++) for (int y = 0; y < 8; y++) if (board[x][y].hasBlackCircle()) {
                //highlight the selected piece
                selectedSquare.setHighlighted(true);
                //if endTurnSnackBar is not already showing, then show it
                if (endTurnSnackBar == null) {
                    endTurnSnackBar = Snackbar
                            .make(this, R.string.end_turn_snack_bar_message, Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(ContextCompat.getColor(context, R.color.snack_bar_action_text_color))
                            .setAction(R.string.end_turn, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    endTurn();
                                }
                            });
                    endTurnSnackBar.show();
                }
                return;
            }
        }
        //if the following code is reached there are no possible legal double kills, hence end the turn
        endTurn();
    }

    /**
     * Shows any moves that the piece at (i, j) can make that involve killing another piece in the upwards direction.
     *
     * @param i The index going right from the left of the board.
     * @param j The index going down from the top of the board.
     */
    private void checkKillsAbove(int i, int j) {
        //stored to avoid querying the CheckerBoardSquare[][] repeatedly
        CheckerBoardSquare selectedPiece = board[i][j];

        //prevents killing in the same move as getting a king, unless killAfterKingingEnabled is set to true
        boolean canContinueMove = j != 2 || selectedPiece.isKing() || DataAccessor.isKillAfterKingingEnabled();

        //check kill to top left
        if (isValidAndEmpty(i - 2, j - 2) && areEnemies(selectedPiece, i - 1, j - 1))
            board[i - 2][j - 2].setBlackCircleData(new BlackCircleData(i, j, i - 1, j - 1, canContinueMove));

        //check kill to top right
        if (isValidAndEmpty(i + 2, j - 2) && areEnemies(selectedPiece, i + 1, j - 1))
            board[i + 2][j - 2].setBlackCircleData(new BlackCircleData(i, j, i + 1, j - 1, canContinueMove));

        //check for butterfly kills if they are enabled
        if (DataAccessor.isButterflyKillingEnabled()) {
            //check upwards kill from left side of the board
            if (i == 1 && j > 1 && board[1][j - 2].isEmpty() && areEnemies(selectedPiece, 0, j - 1))
                board[1][j - 2].setBlackCircleData(new BlackCircleData(i, j, 0, j - 1, canContinueMove));

            //check upwards kill from right side of the board
            if (i == 6 && j > 1 && board[6][j - 2].isEmpty() && areEnemies(selectedPiece, 7, j - 1))
                board[6][j - 2].setBlackCircleData(new BlackCircleData(i, j, 7, j - 1, canContinueMove));
        }
    }

    /**
     * Shows any moves that the piece at (i, j) can make that involve killing another piece in the downwards direction.
     *
     * @param i The index going right from the left of the board.
     * @param j The index going down from the top of the board.
     */
    private void checkKillsBelow(int i, int j) {
        //stored to avoid querying the CheckerBoardSquare[][] repeatedly
        CheckerBoardSquare selectedPiece = board[i][j];

        //prevents killing in the same move as getting a king, unless killAfterKingingEnabled is set to true
        boolean canContinueMove = j != 5 || selectedPiece.isKing() || DataAccessor.isKillAfterKingingEnabled();

        //check kill to bottom left
        if (isValidAndEmpty(i - 2, j + 2) && areEnemies(selectedPiece, i - 1, j + 1))
            board[i - 2][j + 2].setBlackCircleData(new BlackCircleData(i, j, i - 1, j + 1, canContinueMove));

        //check kill to bottom right
        if (isValidAndEmpty(i + 2, j + 2) && areEnemies(selectedPiece, i + 1, j + 1))
            board[i + 2][j + 2].setBlackCircleData(new BlackCircleData(i, j, i + 1, j + 1, canContinueMove));

        //check for butterfly kills if they are enabled
        if (DataAccessor.isButterflyKillingEnabled()) {
            //check downwards kill from left side of the board
            if (i == 1 && j < 6 && board[1][j + 2].isEmpty() && areEnemies(selectedPiece, 0, j + 1))
                board[1][j + 2].setBlackCircleData(new BlackCircleData(i, j, 0, j + 1, canContinueMove));

            //check downwards kill from right side of the board
            if (i == 6 && j < 6 && board[6][j + 2].isEmpty() && areEnemies(selectedPiece, 7, j + 1))
                board[6][j + 2].setBlackCircleData(new BlackCircleData(i, j, 7, j + 1, canContinueMove));
        }
    }




    //CHECKERS GAME LIFECYCLE METHODS


    /**
     * Does cleanup duty at the end of a turn.
     * Should also be called manually when the user has decided to end their turn when they had an opportunity to double kill.
     */
    @CallSuper
    public void endTurn() {
        isRedTurn = !isRedTurn;
        deselectEverything();
        gameHistory.push(getSerialization());
        Utils.clearStack(gameFuture);
        if (endTurnSnackBar != null) {
            endTurnSnackBar.dismiss();
            endTurnSnackBar = null;
        }
        //whoever's turn just finished has won
        if (isGameFinished()) gameFinishedListener.onGameFinished(!isRedTurn);
        //clear any Toasts (not your turn, cannot undo/redo)
        Utils.clearToasts();
    }

    /**
     * Returns whether or not the game has finished.
     * If the game has finished, the player whose turn it is has lost.
     * @return Whether or not the game has finished.
     */
    public final boolean isGameFinished() {
        //all logical operations will be done at the level of chars (as opposed to manipulating the ui)
        return !Utils.canMove(isRedTurn, Utils.toCharArray(board));
    }

    /**
     * Undoes the last move.
     */
    public final void undo() {
        //if there are no more moves to undo (gameHistory must always have at least 1 item)
        if (gameHistory.size() == 1) {
            Utils.showToast(context, R.string.cannot_undo_message);
            return;
        }

        //undo a move
        gameFuture.push(gameHistory.pop());
        setState(gameHistory.pop()); //the item popped from gameHistory will be pushed back to it in setState
    }

    /**
     * @return Whether or not there are at least 2 moves that can be undone.
     */
    public final boolean canUndoTwice() {
        return gameHistory.size() > 2;
    }

    /**
     * Redoes the last undone move.
     */
    public final void redo() {
        //if there are no more moves to redo
        if (gameFuture.empty()) {
            Utils.showToast(context, R.string.cannot_redo_message);
            return;
        }

        setState(gameFuture.pop()); //the item popped from gameFuture will be pushed to gameHistory in setState
    }

    /**
     * @return Whether or not there are at least 2 moves that can be redone.
     */
    public final boolean canRedoTwice() {
        return gameFuture.size() >= 2;
    }

    /**
     * Sets the locked state of this CheckerBoard, either preventing it or permitting it to process user input.
     *
     * @param isLocked Whether or not to lock this CheckerBoard (prevent it from processing user input).
     */
    public final void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    /**
     * @return Whether or not this CheckerBoard is locked.
     */
    public final boolean isLocked() {
        return isLocked;
    }

    /**
     * Removes all black circles and highlighting, reverting the board to its natural state.
     */
    private void deselectEverything() {
        //only need to loop over odd squares
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2) {
            CheckerBoardSquare square = board[i][j];
            if (square.hasBlackCircle()) square.setEmpty();
            else if (square.isHighlighted()) square.setHighlighted(false); //else for efficiency
        }
    }

    /**
     * Returns a String serializing the current state of this CheckerBoard.
     * Can later be used to initialize a CheckerBoard to this CheckerBoard's current state.
     * No piece can be highlighted when this method is called (for example by using deselectEverything()).
     *
     * @return A String serializing the current state of this CheckerBoard.
     */
    protected String getSerialization() {
        char[] result = new char[33]; //char[] is more efficient than concatenating Strings
        result[0] = isRedTurn ? 'T' : 'F';
        int count = 1; //using counter is more efficient that calculating 8*i+j+1 in loop
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2) {
            result[count] = board[i][j].getState();
            count++;
        }
        return new String(result);
    }

    /**
     * Returns a String serializing the current state of the CheckerBoard (identical result to getSerialization).
     * This makes use of the already present copy of the current state in gameHistory as opposed to recreating it.
     * This is the fastest and most efficient way to get the current state's serialization.
     * This is important because it is used in the onPause method which needs to return as quickly as possible.
     *
     * @return A String containing the serialization of the current state of the CheckerBoard.
     */
    public final String getExistingStateSerialization() {
        return gameHistory.peek();
    }
}
