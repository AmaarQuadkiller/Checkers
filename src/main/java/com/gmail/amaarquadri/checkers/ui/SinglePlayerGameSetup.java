package com.gmail.amaarquadri.checkers.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.gmail.amaarquadri.checkers.R;
import com.gmail.amaarquadri.checkers.utility.DataAccessor;

/**
 * Created by Amaar on 2016-09-11.
 * Activity where the user can set up a SinglePlayerGame.
 */
public class SinglePlayerGameSetup extends Activity {
    /**
     * Request code for starting the SinglePlayerGame.
     */
    private static final int SINGLE_PLAYER_GAME_REQUEST_CODE = 1;


    /**
     * A Spinner that the user can use to select the DifficultyLevel for the SinglePlayerGame.
     */
    private Spinner difficultyLevelSpinner;

    /**
     * A RadioButton to let the user choose to play as red.
     */
    private RadioButton redRadioButton;

    /**
     * A RadioButton to let the user choose to play as white.
     */
    private RadioButton whiteRadioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_player_game_setup);

        difficultyLevelSpinner = (Spinner) findViewById(R.id.difficulty_level_spinner);
        //initialize the difficultyLevelSpinner with the string-array resource, and view resource
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, R.layout.spinner);
        //initialize the difficultyLevelSpinner with a built in dropdown item view
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultyLevelSpinner.setAdapter(adapter);

        redRadioButton = (RadioButton) findViewById(R.id.red_radio_button);
        whiteRadioButton = (RadioButton) findViewById(R.id.white_radio_button);
        //touching the TextView that says "Red" or "White" will select the associated RadioButton
        findViewById(R.id.red_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redRadioButton.setChecked(true);
            }
        });
        findViewById(R.id.white_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteRadioButton.setChecked(true);
            }
        });

        //initialize the difficultyLevelSpinner with the last used DifficultyLevel
        switch (DataAccessor.getDifficultyLevel()) {
            case SinglePlayerGame.EASY: difficultyLevelSpinner.setSelection(0); break;
            case SinglePlayerGame.MEDIUM: difficultyLevelSpinner.setSelection(1); break;
            case SinglePlayerGame.HARD: difficultyLevelSpinner.setSelection(2);
        }
        //initialize the RadioButtons with the last used value of isUserRed
        (DataAccessor.isUserRed() ? redRadioButton : whiteRadioButton).setChecked(true);
    }


    /**
     * Updates the SharedPreferences, and starts the SinglePlayerGame Activity.
     */
    public void startSinglePlayerGame(View view) {
        updateSharedPreferences();
        startActivityForResult(new Intent(this, SinglePlayerGame.class), SINGLE_PLAYER_GAME_REQUEST_CODE);
    }

    /**
     * When the SinglePlayerGame finishes, finish() this Activity so the user can return to the MainMenu.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }

    /**
     * Updates the SharedPreferences with the current selections for the difficultyLevelSpinner and the RadioButtons.
     */
    private void updateSharedPreferences() {
        switch (difficultyLevelSpinner.getSelectedItemPosition()) {
            case 0: DataAccessor.setDifficultyLevel(SinglePlayerGame.EASY); break;
            case 1: DataAccessor.setDifficultyLevel(SinglePlayerGame.MEDIUM); break;
            case 2: DataAccessor.setDifficultyLevel(SinglePlayerGame.HARD); break;
            //should never happen
            default: DataAccessor.setDifficultyLevel(SinglePlayerGame.MEDIUM);
        }
        DataAccessor.setUserRed(redRadioButton.isChecked());
        DataAccessor.apply();
    }

    /**
     * When the Application is paused (goes out of view), update the SharedPreferences.
     * This ensures a consistent user experience if this Activity is destroyed.
     */
    @Override
    protected void onPause() {
        updateSharedPreferences();
        super.onPause();
    }
}
