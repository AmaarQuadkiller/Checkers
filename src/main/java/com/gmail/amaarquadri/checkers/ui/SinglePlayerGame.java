package com.gmail.amaarquadri.checkers.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;

import com.gmail.amaarquadri.checkers.R;
import com.gmail.amaarquadri.checkers.logic.CheckerBoard;
import com.gmail.amaarquadri.checkers.logic.SinglePlayerCheckerBoard;
import com.gmail.amaarquadri.checkers.utility.DataAccessor;
import com.gmail.amaarquadri.checkers.utility.Utils;

/**
 * Created by Amaar on 2016-06-01.
 * This allows the user to play a single player game with an AI.
 */
public class SinglePlayerGame extends Activity {
    /**
     * Request code for starting the GameOver Activity.
     */
    private static final int GAME_OVER_ACTIVITY_REQUEST_CODE = 2;


    /**
     * Constant indicating the EASY DifficultyLevel.
     */
    public static final int EASY = 1;

    /**
     * Constant indicating the MEDIUM DifficultyLevel.
     */
    public static final int MEDIUM = 2;

    /**
     * Constant indicating the HARD DifficultyLevel.
     */
    public static final int HARD = 3;

    /**
     * Group the EASY, MEDIUM, and HARD constants into a single @interface declaration.
     */
    @IntDef({EASY, MEDIUM, HARD})
    public @interface DifficultyLevel {}

    /**
     * This interface allows the SinglePlayerCheckerBoard to control the visibility of the loading icon.
     */
    public interface LoadingController {
        /**
         * Sets the visibility of the loading icon based on whether or not the game is loading.
         * The implementation of this interface will edit an ImageView on the UI, so it must be called from the UIThread.
         *
         * @param isLoading Whether or not the game is loading.
         */
        @UiThread
        void setLoading(boolean isLoading);
    }


    /**
     * The CheckerBoard in which this TwoPlayerGame is played.
     */
    private SinglePlayerCheckerBoard checkerBoard;

    /**
     * Whether or not the user is playing as red in this SinglePlayerGame.
     */
    private boolean isUserRed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_player_game);
        checkerBoard = (SinglePlayerCheckerBoard) findViewById(R.id.checker_board);
        isUserRed = DataAccessor.isUserRed();

        //set the CheckerBoard's GameFinishedListener
        final Context this_ = this;
        checkerBoard.setGameFinishedListener(new CheckerBoard.GameFinishedListener() {
            @Override
            public void onGameFinished(final boolean hasRedWon) {
                checkerBoard.setLocked(true);
                final boolean hasUserWon = hasRedWon == isUserRed;

                //update the SharedPreferences
                DataAccessor.incrementSinglePlayerGamesPlayed();
                DataAccessor.clearLastSinglePlayerGameData();
                if (hasUserWon) switch (DataAccessor.getDifficultyLevel()) {
                    case EASY: DataAccessor.incrementGamesWonEasy(); break;
                    case MEDIUM: DataAccessor.incrementGamesWonMedium(); break;
                    case HARD: DataAccessor.incrementGamesWonHard(); break;
                }
                DataAccessor.apply();

                //start the GameOver Activity after a 2 seconds delay
                //the checkerBoard View is used to post the Runnable to the MessageQueue
                checkerBoard.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent gameOverIntent = new Intent(this_, GameOver.class);
                        gameOverIntent.putExtra("winnerMessage", hasUserWon ?
                                R.string.user_winner_message : R.string.ai_winner_message);
                        startActivityForResult(gameOverIntent, GAME_OVER_ACTIVITY_REQUEST_CODE);
                    }
                }, 2000);
            }
        });

        //set the SinglePlayerCheckerBoard's LoadingController
        final ImageView loadingIcon = (ImageView) findViewById(R.id.loading_icon); //more efficient than calling findViewById every time
        checkerBoard.setLoadingController(new LoadingController() {
            @Override
            public void setLoading(boolean isLoading) {
                loadingIcon.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
            }
        });

        try {
            //initialize the CheckerBoard with whatever the last game's data is (could be data for a new game)
            checkerBoard.setState(DataAccessor.getLastSinglePlayerGameData());
        }
        //if the data is malformed
        catch (IllegalArgumentException e) {
            checkerBoard.reset();
            //let the user know with an AlertDialog
            new AlertDialog.Builder(this)
                    .setTitle(R.string.loading_error_alert_dialog_title)
                    .setMessage(R.string.loading_error_alert_dialog_message)
                    .setPositiveButton(R.string.main_menu, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.new_game, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!isUserRed) checkerBoard.makeAIMove();
                        }
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        //if the user is white, then the AI makes the first move after a 1 second delay
        if (!isUserRed) checkerBoard.makeAIMove();
    }

    /**
     * If the user selected "Play Again", unlock and reinitialize the CheckerBoard.
     * Otherwise, finish() this Activity so the user can return to the MainMenu.
     *
     * @param requestCode The request code supplied to the GameOver Activity.
     * @param resultCode The result code returned by the GameOver Activity.
     * @param data An Intent containing data from the GameOver Activity, namely whether or not to play again.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if the user selected play again
        if (requestCode == GAME_OVER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK &&
                data.getBooleanExtra("playAgain", false)) {
            checkerBoard.reset();
            checkerBoard.setLocked(false);

            //if the user is white, then the AI makes the first move
            if (!isUserRed) checkerBoard.makeAIMove();
        }
        //otherwise return to the main menu
        else finish();
    }

    /**
     * Undoes the last user move and the last AI move.
     */
    public void undo(View view) {
        //if the checkerBoard is locked, then show a Toast instead
        if (checkerBoard.isLocked()) {
            Utils.showToast(this, R.string.cannot_undo_now_message);
            return;
        }

        //if there aren't two moves that can be undone
        if (!checkerBoard.canUndoTwice()) {
            Utils.showToast(this, R.string.cannot_undo_message);
            return;
        }

        //undo twice: once for AI move and once for user move
        checkerBoard.undo();
        //delay between undoing moves
        checkerBoard.setLocked(true);
        checkerBoard.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkerBoard.undo();
                checkerBoard.setLocked(false);
            }
        }, 250);
    }

    /**
     * Redoes the last undone AI move and the last undone user move.
     */
    public void redo(View view) {
        //if the checkerBoard is locked, then show a Toast instead
        if (checkerBoard.isLocked()) {
            Utils.showToast(this, R.string.cannot_redo_now_message);
            return;
        }

        //if there aren't two moves that can be redone
        if (!checkerBoard.canRedoTwice()) {
            Utils.showToast(this, R.string.cannot_redo_message);
            return;
        }

        //redo twice: once for AI move and once for user move
        checkerBoard.redo();
        //delay between redoing moves
        checkerBoard.setLocked(true);
        checkerBoard.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkerBoard.redo();
                checkerBoard.setLocked(false);
            }
        }, 250);
    }

    /**
     * When the Activity is paused (goes out of view), save the CheckerBoard's serialization if the game is not over.
     * Also clear any Toasts.
     */
    @Override
    protected void onPause() {
        super.onPause();
        //if the game is not over, save the CheckerBoard's serialization to SharedPreferences in case the Activity is destroyed
        if (!checkerBoard.isGameFinished()) {
            DataAccessor.setLastSinglePlayerGameData(checkerBoard.getExistingStateSerialization());
            DataAccessor.apply();
        }
        Utils.clearToasts();
    }
}
