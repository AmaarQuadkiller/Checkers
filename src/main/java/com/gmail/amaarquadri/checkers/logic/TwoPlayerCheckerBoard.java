package com.gmail.amaarquadri.checkers.logic;

import android.content.Context;
import android.util.AttributeSet;

import com.gmail.amaarquadri.checkers.utility.DataAccessor;

/**
 * Created by Amaar on 2016-09-11.
 * This CheckerBoard adds on functionality specific to TwoPlayerGame, namely the flipIfNecessaryMethod() and its usages.
 */
public class TwoPlayerCheckerBoard extends CheckerBoard {
    /**
     * Whether or not this CheckerBoard is currently upside-down.
     * Used when "Rotate Every Turn" is enabled.
     */
    private boolean isInverted;


    /**
     * Constructor used by XML.
     *
     * @param context The parent context.
     * @param attrs   A collection of attributes.
     */
    public TwoPlayerCheckerBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        isInverted = false;
    }


    /**
     * Initialize this CheckerBoard to a specified state.
     * The serializedBoard must be in the format constructed by getSerialization().
     * The serialized board cannot be inverted.
     *
     * @param serializedBoard A String representing the state to initialize this CheckerBoard to.
     */
    @Override
    public void setState(String serializedBoard) {
        super.setState(serializedBoard);
        //The board will always be set to a state that was constructed by getSerialization() which
        //never returns an inverted board. Therefore, isInverted must be set to false because that is
        //the value that it will be. The following call to flipIfNecessary() will take care of the rest.
        isInverted = false;
        flipIfNecessary(false);
    }

    /**
     * Returns whether a red piece can move up.
     * This is identical to asking whether a white piece can move down.
     * This is the opposite of asking whether a red piece can move down.
     * This is the opposite of asking whether a white piece can move up.
     * This does not apply to kings because they can move in all directions.
     * This is overwritten to return false if the board is inverted.
     *
     * @return Whether a red piece can move up.
     */
    @Override
    protected boolean canRedPieceMoveUp() {
        return !isInverted;
    }

    /**
     * Does cleanup duty at the end of a turn.
     * Flips the board if the game is not finished and it is necessary to do so.
     */
    @Override
    public void endTurn() {
        super.endTurn();
        if (!isGameFinished()) flipIfNecessary(true);
    }

    /**
     * Flips the board-upside down from its current state if necessary and "Rotate Every Turn" is enabled.
     *
     * @param delay Whether or not to apply a 1 second delay before flipping.
     */
    private void flipIfNecessary(boolean delay) {
        if (isRedTurn == isInverted && DataAccessor.rotateEveryTurnEnabled()) {
            isInverted = !isInverted;
            if (delay) {
                setLocked(true);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flip();
                        setLocked(false);
                    }
                }, 1000);
            }
            else flip();
        }
    }

    /**
     * Flips the board upside-down immediately when called, regardless of the state of the board.
     * Thus the caller must ensure that the board needs to be flipped before calling this method.
     */
    private void flip() {
        //only need to loop over odd squares
        for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 4; j += 2) {
            char c = board[i][j].getState();
            board[i][j].setState(board[7 - i][7 - j].getState());
            board[7 - i][7 - j].setState(c);
        }
    }

    /**
     * Returns a String serializing the current state of this CheckerBoard.
     * Can later be used to initialize a CheckerBoard to this CheckerBoard's current state.
     *
     * @return A String serializing the current state of this CheckerBoard.
     */
    @Override
    public String getSerialization() {
        //if the CheckerBoard is not inverted just delegate to the super method
        if (!isInverted) return super.getSerialization();

        //otherwise manually create the serialization
        char[] result = new char[33]; //char[] is more efficient than concatenating Strings
        result[0] = isRedTurn ? 'T' : 'F';
        int count = 1; //using counter is more efficient that calculating 8*i+j+1 in loop
        //go through the CheckerBoardSquare[][] in reverse to account for the inversion
        for (int i = 7; i >= 0; i--) for (int j = 7 - (i % 2); j >= 0; j -= 2) {
            result[count] = board[i][j].getState();
            count++;
        }
        return new String(result);
    }
}
