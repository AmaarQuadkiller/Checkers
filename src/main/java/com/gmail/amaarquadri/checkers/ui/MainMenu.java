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
import com.gmail.amaarquadri.checkers.utility.DataAccessor;

/**
 * This class is the main menu where the user can navigate to other parts of the application.
 */
public class MainMenu extends Activity {
    /**
     * An AlertDialog for when the user has a choice of whether or not to continue with a single player game.
     */
    private AlertDialog continueSinglePlayerGameDialog;

    /**
     * An AlertDialog for when the user has a choice of whether or not to continue with a two player game.
     */
    private AlertDialog continueTwoPlayerGameDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //manifest file sets theme to AppTheme_BrandedLaunch which allows the launch screen to show a background image
        //before inflating MainMenu's layout, revert to AppTheme_NoActionBar
        setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.main_menu);
    }


    /**
     * Starts the SinglePlayerGame Activity.
     */
    public void startSinglePlayerGameSetup(View view) {
        //if there is no game saved launch SinglePlayerGame
        if (DataAccessor.getLastSinglePlayerGameData().equals(CheckerBoard.INITIAL_SERIALIZED_BOARD))
            startActivity(new Intent(this, SinglePlayerGameSetup.class));
        //otherwise ask if they want to continue the game
        else {
            //lazy initialization
            if (continueSinglePlayerGameDialog == null) {
                final Context this_ = this;
                continueSinglePlayerGameDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.continue_game_prompt)
                        .setMessage(R.string.game_deleted_warning_prompt)
                        .setNegativeButton(R.string.new_game, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataAccessor.clearLastSinglePlayerGameData();
                                DataAccessor.apply();
                                startActivity(new Intent(this_, SinglePlayerGameSetup.class));
                            }
                        })
                        .setPositiveButton(R.string.continue_game, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(this_, SinglePlayerGame.class));
                            }
                        })
                        .create();
            }
            continueSinglePlayerGameDialog.show();
        }
    }

    /**
     * Starts the TwoPlayerGame Activity.
     */
    public void startTwoPlayerGame(View view) {
        //if there is no game saved launch TwoPlayerGame
        if (DataAccessor.getLastTwoPlayerGameData().equals(CheckerBoard.INITIAL_SERIALIZED_BOARD))
            startActivity(new Intent(this, TwoPlayerGame.class));
        //otherwise ask if they want to continue the game
        else {
            //lazy initialization
            if (continueTwoPlayerGameDialog == null) {
                final Context this_ = this;
                continueTwoPlayerGameDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.continue_game_prompt)
                        .setMessage(R.string.game_deleted_warning_prompt)
                        .setNegativeButton(R.string.new_game, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataAccessor.clearLastTwoPlayerGameData();
                                DataAccessor.apply();
                                startActivity(new Intent(this_, TwoPlayerGame.class));
                            }
                        })
                        .setPositiveButton(R.string.continue_game, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(this_, TwoPlayerGame.class));
                            }
                        })
                        .create();
            }
            continueTwoPlayerGameDialog.show();
        }
    }

    /**
     * Starts the HighScores Activity.
     */
    public void viewHighScores(View view) {
        startActivity(new Intent(this, HighScores.class));
    }

    /**
     * Starts the Settings Activity.
     */
    public void editSettings(View view) {
        startActivity(new Intent(this, Settings.class));
    }
}
