package com.gmail.amaarquadri.checkers.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gmail.amaarquadri.checkers.R;
import com.gmail.amaarquadri.checkers.logic.CheckerBoard;
import com.gmail.amaarquadri.checkers.logic.TwoPlayerCheckerBoard;
import com.gmail.amaarquadri.checkers.utility.DataAccessor;
import com.gmail.amaarquadri.checkers.utility.Utils;

/**
 * Created by Amaar on 2016-05-30.
 * This Activity allows the user to play a two player game.
 * //TODO: make use of the nearby connections API to implement 2 player games
 */
public class TwoPlayerGame extends Activity {
    /**
     * Request code for starting the GameOver Activity.
     */
    private static final int GAME_OVER_ACTIVITY_REQUEST_CODE = 3;


    /**
     * The CheckerBoard in which this TwoPlayerGame is played.
     */
    private TwoPlayerCheckerBoard checkerBoard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.two_player_game);
        checkerBoard = (TwoPlayerCheckerBoard) findViewById(R.id.checker_board);

        //initialize the CheckerBoard with a GameFinishedListener
        final Context this_ = this;
        checkerBoard.setGameFinishedListener(new CheckerBoard.GameFinishedListener() {
            @Override
            public void onGameFinished(final boolean hasRedWon) {
                checkerBoard.setLocked(true);

                //update the SharedPreferences
                DataAccessor.incrementTwoPlayerGamesPlayed();
                DataAccessor.clearLastTwoPlayerGameData();
                DataAccessor.apply();

                //start the GameOver Activity after a 2 seconds delay
                checkerBoard.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent gameOverIntent = new Intent(this_, GameOver.class);
                        gameOverIntent.putExtra("winnerMessage", hasRedWon ?
                                R.string.red_winner_message : R.string.white_winner_message);
                        startActivityForResult(gameOverIntent, GAME_OVER_ACTIVITY_REQUEST_CODE);
                    }
                }, 2000);
            }
        });

        try {
            //initialize the CheckerBoard with whatever the last game's data is (could be data for a new game)
            checkerBoard.setState(DataAccessor.getLastTwoPlayerGameData());
        }
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
                    .setNegativeButton(R.string.new_game, null)
                    .setCancelable(false)
                    .show();
        }
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
        }
        //otherwise return to the main menu
        else finish();
    }

    /**
     * Undoes the last move.
     */
    public void undo(View view) {
        checkerBoard.undo();
    }

    /**
     * Redoes the last undone move.
     */
    public void redo(View view) {
        checkerBoard.redo();
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
            DataAccessor.setLastTwoPlayerGameData(checkerBoard.getExistingStateSerialization());
            DataAccessor.apply();
        }
        Utils.clearToasts();
    }
}
