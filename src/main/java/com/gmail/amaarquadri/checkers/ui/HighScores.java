package com.gmail.amaarquadri.checkers.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.gmail.amaarquadri.checkers.R;
import com.gmail.amaarquadri.checkers.utility.DataAccessor;

/**
 * Created by Amaar on 2016-06-01.
 * This class shows the user several statistics about their history using this application.
 */
public class HighScores extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.high_scores);

        //append the values from DataAccessor
        ((TextView) findViewById(R.id.single_player_games_played_text_view))
                .append(" " + String.valueOf(DataAccessor.getSinglePlayerGamesPlayed()));
        ((TextView) findViewById(R.id.single_player_games_won_easy_text_view))
                .append(" " + String.valueOf(DataAccessor.getGamesWonEasy()));
        ((TextView) findViewById(R.id.single_player_games_won_medium_text_view))
                .append(" " + String.valueOf(DataAccessor.getGamesWonMedium()));
        ((TextView) findViewById(R.id.single_player_games_won_hard_text_view))
                .append(" " + String.valueOf(DataAccessor.getGamesWonHard()));
        ((TextView) findViewById(R.id.two_player_games_played_text_view))
                .append(" " + String.valueOf(DataAccessor.getTwoPlayerGamesPlayed()));
    }
}
