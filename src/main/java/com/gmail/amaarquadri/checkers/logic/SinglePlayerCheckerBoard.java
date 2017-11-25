package com.gmail.amaarquadri.checkers.logic;

import android.content.Context;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.AttributeSet;
import android.util.Log;

import com.gmail.amaarquadri.checkers.ui.SinglePlayerGame;
import com.gmail.amaarquadri.checkers.utility.DataAccessor;
import com.gmail.amaarquadri.checkers.utility.Utils;

import java.util.ArrayList;

import static com.gmail.amaarquadri.checkers.ui.SinglePlayerGame.EASY;
import static com.gmail.amaarquadri.checkers.ui.SinglePlayerGame.HARD;
import static com.gmail.amaarquadri.checkers.ui.SinglePlayerGame.MEDIUM;

/**
 * Created by Amaar on 2016-09-11.
 * This CheckerBoard adds on functionality specific to SinglePlayerGame, namely AI.
 */
public class SinglePlayerCheckerBoard extends CheckerBoard {
    public static final String TAG = "SinglePlayerCheckerBoar";
    /**
     * Whether or not the user is playing as red.
     */
    private final boolean isUserRed;

    /**
     * The DifficultyLevel at which this AI will make moves.
     */
    private int depth;

    /**
     * A LoadingController used to control the visibility of the loading icon.
     */
    private SinglePlayerGame.LoadingController loadingController;


    /**
     * Constructor used by XML.
     *
     * @param context The parent context.
     * @param attrs   A collection of attributes.
     */
    public SinglePlayerCheckerBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        isUserRed = DataAccessor.isUserRed();
        //determine the depth of recursive tree search based on difficulty level
        //depth is the number of moves into the future that will be taken into consideration
        switch (DataAccessor.getDifficultyLevel()) {
            //TODO: perhaps adjust values
            case EASY: depth = 3; break;
            case MEDIUM: depth = 5; break;
            case HARD: depth = 6; break; //averaged 2.6 seconds
            //should never happen
            default: depth = 6;
        }
        loadingController = null;
    }

    /**
     * @param loadingController A LoadingController used to control the visibility of the loading icon.
     */
    public void setLoadingController(SinglePlayerGame.LoadingController loadingController) {
        this.loadingController = loadingController;
    }

    /**
     * Does cleanup duty at the end of a turn.
     * Should also be called manually when the user has decided to end their turn when they had an opportunity to double kill.
     */
    @Override
    public void endTurn() {
        super.endTurn();
        //make the next move via AI, if it is the AI's turn
        if (!isGameFinished() && isUserRed != isRedTurn) makeAIMove();
    }


    /**
     * Makes the next move on the CheckerBoard using artificial intelligence.
     */
    @UiThread
    public void makeAIMove() {
        //lock up the ui
        setLocked(true);

        //show the loading icon
        loadingController.setLoading(true);

        //must be done in UI Thread because it calls CheckerBoardState.getState
        final char[][] board = Utils.toCharArray(this.board);

        //run the bulk of the computations in a worker Thread to prevent UI unresponsiveness
        new Thread(new Runnable() {
            @WorkerThread
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                final DetailedMove move = pickMove(board);
                long timeElapsed = System.currentTimeMillis() - startTime;
                Log.d("SinglePlayerCheckerBoar", "timeElapsed = " + timeElapsed);

                //ensure that at least 1 second passes before the Move is executed
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        executeMove(move);
                    }
                }, 1000 - timeElapsed);
            }
        }).start();
    }

    /**
     * Determines the best possible move for the AI to make.
     *
     * @param board The board for which a move needs to be determined.
     * @return The best possible Move.
     */
    @WorkerThread
    private DetailedMove pickMove(char[][] board) {
        //an ArrayList to hold the best Move(s) (multiple Moves will be present if they are all tied for best Move)
        ArrayList<DetailedMove> bestMoves = new ArrayList<>();

        //the value of the best Move(s) checked so far
        //starts at the worst possible value (based on whether we are maximizing or minimizing)
        int optimalValue = isRedTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        //for every possible move
        for (DetailedMove move : Utils.getAllPossibleDetailedMoves(isRedTurn, board)) {
            int mValue = move.getValue(!isRedTurn, optimalValue, depth);
            //if this Move is better than all the others, it replaces them all
            if (isRedTurn ? mValue > optimalValue : mValue < optimalValue) {
                optimalValue = mValue;
                bestMoves.clear();
                bestMoves.add(move);
            }
            //if this Move is just as good it gets added to the list
            else if (mValue == optimalValue) bestMoves.add(move);
        }

        //pick one of the bestMoves randomly
        return bestMoves.get((int) (Math.random() * bestMoves.size()));
    }

    /**
     * Executes a given Move by editing the CheckerBoardSquares on the UI Thread.
     *
     * @param finalMove The Move which is to be executed.
     */
    @WorkerThread
    private void executeMove(final DetailedMove finalMove) {
        //create and post all of the tasks to be done (showing intermediate and final boards) to the UI Thread
        //give each task incrementally longer delays (using count) so that they show in sequence
        int count = -1; //starts at -1 so that first task gets no delay

        for (final char[][] intermediateBoard : finalMove.getBoards()) postDelayed(new Runnable() {
            @UiThread
            @Override
            public void run() {
                //update CheckerBoardSquares directly to avoid using setState() and adding to gameHistory
                //only need to loop over odd squares
                for (int i = 0; i < 8; i++) for (int j = (i + 1) % 2; j < 8; j += 2)
                    board[i][j].setState(intermediateBoard[i][j]);
            }
        }, 1000 * ++count);

        //hide the loading icon, unlock the CheckerBoard, and end the turn
        postDelayed(new Runnable() {
            @UiThread
            @Override
            public void run() {
                loadingController.setLoading(false);
                setLocked(false);
                endTurn();
            }
        }, 1000 * count); //same delay as the last board being shown
    }
}
