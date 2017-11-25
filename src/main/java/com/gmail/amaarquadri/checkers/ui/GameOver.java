package com.gmail.amaarquadri.checkers.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gmail.amaarquadri.checkers.R;

/**
 * Created by Amaar on 2016-06-20.
 * Activity for when a game finishes.
 * Displays a message to the user and gives them the choice to play again or return to the main menu.
 */
public class GameOver extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over);
        //initialize the winner_message_text_view with the winnerMessage
        ((TextView) findViewById(R.id.winner_message_text_view))
                //the winnerMessage actually contains the resource id, but that works out fine (setText() accepts resIds)
                .setText(getIntent().getIntExtra("winnerMessage", R.string.user_winner_message));
    }

    /**
     * When the user clicks the Play Again Button, finish() this Activity and send the instruction back to the previous Activity.
     */
    public void playAgain(View view) {
        Intent data = new Intent();
        data.putExtra("playAgain", true);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * When the user clicks the Main Menu Button, finish() this Activity and send the instruction back to the previous Activity.
     */
    public void goToMainMenu(View view) {
        Intent data = new Intent();
        data.putExtra("playAgain", false);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * When the user presses the back button, finish() this Activity.
     * The calling Activity should return the user to the Main Menu.
     */
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
