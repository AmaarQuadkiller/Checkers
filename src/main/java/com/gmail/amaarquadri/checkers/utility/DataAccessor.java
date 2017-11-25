package com.gmail.amaarquadri.checkers.utility;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gmail.amaarquadri.checkers.logic.CheckerBoard;
import com.gmail.amaarquadri.checkers.ui.SinglePlayerGame;

/**
 * Created by Amaar on 2016-12-19.
 * Utility class with static methods for accessing and editing the SharedPreferences.
 * This extends the Application class, allowing the SharedPreferences to be initialized in the Application's onCreate() method.
 */
public class DataAccessor extends Application {
    /**
     * A static SharedPreferences that is used throughout the application.
     * It is instantiated when the Application is created and deleted when the process is terminated.
     */
    private static SharedPreferences sharedPreferences;

    /**
     * A static SharedPreferences.Editor that is used throughout the application to modify the SharedPreferences.
     * It is instantiated when the Application is created and deleted when the process is terminated.
     */
    private static SharedPreferences.Editor editor;

    /**
     * Whether or not "Flying Kings" are enabled.
     * This is kept locally to speed up AI moves, where this is queried thousands of times recursively.
     */
    private static boolean areFlyingKingsEnabled;

    /**
     * Whether or not "Allow Butterfly Capturing" is enabled.
     * This is kept locally to speed up AI moves, where this is queried thousands of times recursively.
     */
    private static boolean isButterflyKillingEnabled;

    /**
     * Whether or not "Allow Capturing After Kinging" is enabled.
     * This is kept locally to speed up AI moves, where this is queried thousands of times recursively.
     */
    private static boolean isKillAfterKingingEnabled;


    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        updateGameRules();
    }
    

    /**
     * @return The DifficultyLevel for SinglePlayerGame.
     */
    public static @SinglePlayerGame.DifficultyLevel int getDifficultyLevel() {
        switch (sharedPreferences.getInt("difficultyLevel", SinglePlayerGame.MEDIUM)) {
            case SinglePlayerGame.EASY: return SinglePlayerGame.EASY;
            case SinglePlayerGame.MEDIUM: return SinglePlayerGame.MEDIUM;
            case SinglePlayerGame.HARD: return SinglePlayerGame.HARD;
            //should never happen
            default: return SinglePlayerGame.MEDIUM;
        }
    }

    /**
     * Sets the DifficultyLevel for SinglePlayerGame.
     *
     * @param difficultyLevel The DifficultyLevel for SinglePlayerGame.
     */
    public static void setDifficultyLevel(@SinglePlayerGame.DifficultyLevel int difficultyLevel) {
        editor.putInt("difficultyLevel", difficultyLevel);
    }

    /**
     * @return Whether or not the user is playing as red in the SinglePlayerGame.
     */
    public static boolean isUserRed() {
        return sharedPreferences.getBoolean("isUserRed", true);
    }

    /**
     * Sets whether or not the user will be playing as red in the SinglePlayerGame.
     *
     * @param isUserRed Whether or not the user will be playing as red in the SinglePlayerGame.
     */
    public static void setUserRed(boolean isUserRed) {
        editor.putBoolean("isUserRed", isUserRed);
    }

    /**
     * @return The number of single player games played.
     */
    public static int getSinglePlayerGamesPlayed() {
        return sharedPreferences.getInt("singlePlayerGamesPlayed", 0);
    }

    /**
     * Increments the number of single player games played.
     */
    public static void incrementSinglePlayerGamesPlayed() {
        editor.putInt("singlePlayerGamesPlayed", getSinglePlayerGamesPlayed() + 1);
    }

    /**
     * @return The number of single player games won on easy.
     */
    public static int getGamesWonEasy() {
        return sharedPreferences.getInt("gamesWonEasy", 0);
    }

    /**
     * Increments the number of single player games won on easy.
     */
    public static void incrementGamesWonEasy() {
        editor.putInt("gamesWonEasy", getGamesWonEasy() + 1);
    }


    /**
     * @return The number of single player games won on medium.
     */
    public static int getGamesWonMedium() {
        return sharedPreferences.getInt("gamesWonMedium", 0);
    }

    /**
     * Increments the number of single player games won on medium.
     */
    public static void incrementGamesWonMedium() {
        editor.putInt("gamesWonMedium", getGamesWonMedium() + 1);
    }


    /**
     * @return The number of single player games won on hard.
     */
    public static int getGamesWonHard() {
        return sharedPreferences.getInt("gamesWonHard", 0);
    }

    /**
     * Increments the number of single player games won on hard.
     */
    public static void incrementGamesWonHard() {
        editor.putInt("gamesWonHard", getGamesWonHard() + 1);
    }


    /**
     * @return The serialization of the last saved single player game's CheckerBoard.
     */
    public static String getLastSinglePlayerGameData() {
        return sharedPreferences.getString("singlePlayerGameData", CheckerBoard.INITIAL_SERIALIZED_BOARD);
    }

    /**
     * Sets the serialization of the last saved single player game's CheckerBoard.
     *
     * @param checkerBoardData The serialization of the last saved single player game's CheckerBoard.
     */
    public static void setLastSinglePlayerGameData(String checkerBoardData) {
        editor.putString("singlePlayerGameData", checkerBoardData);
    }

    /**
     * Deletes the currently saved single player game's CheckerBoard serialization.
     */
    public static void clearLastSinglePlayerGameData() {
        setLastSinglePlayerGameData(CheckerBoard.INITIAL_SERIALIZED_BOARD);
    }


    /**
     * @return The number of two player games played.
     */
    public static int getTwoPlayerGamesPlayed() {
        return sharedPreferences.getInt("twoPlayerGamesPlayed", 0);
    }

    /**
     * Increments the number of two player games played.
     */
    public static void incrementTwoPlayerGamesPlayed() {
        editor.putInt("twoPlayerGamesPlayed", getTwoPlayerGamesPlayed() + 1);
    }


    /**
     * @return The serialization of the last saved two player game's CheckerBoard.
     */
    public static String getLastTwoPlayerGameData() {
        return sharedPreferences.getString("twoPlayerGameData", CheckerBoard.INITIAL_SERIALIZED_BOARD);
    }

    /**
     * Sets the serialization of the last saved two player game's CheckerBoard.
     *
     * @param checkerBoardData The serialization of the last saved two player game's CheckerBoard.
     */
    public static void setLastTwoPlayerGameData(String checkerBoardData) {
        editor.putString("twoPlayerGameData", checkerBoardData);
    }

    /**
     * Deletes the currently saved two player game's CheckerBoard serialization.
     */
    public static void clearLastTwoPlayerGameData() {
        setLastTwoPlayerGameData(CheckerBoard.INITIAL_SERIALIZED_BOARD);
    }


    /**
     * @return Whether or not "Rotate Screen Every Turn" is enabled.
     */
    public static boolean rotateEveryTurnEnabled() {
        return sharedPreferences.getBoolean("rotateEveryTurnEnabled", false);
    }

    /**
     * Updates the values of the SharedPreferences variables associated with the rules of the game.
     * By keeping these variables stored locally, the speed of the AI moves can be optimized.
     */
    public static void updateGameRules() {
        areFlyingKingsEnabled = sharedPreferences.getBoolean("flyingKingsEnabled", false);
        isButterflyKillingEnabled = sharedPreferences.getBoolean("butterflyKillingEnabled", false);
        isKillAfterKingingEnabled = sharedPreferences.getBoolean("killAfterKingingEnabled", false);
    }

    /**
     * @return Whether or not "Flying Kings" are enabled.
     */
    public static boolean areFlyingKingsEnabled() {
        return areFlyingKingsEnabled;
    }

    /**
     * @return Whether or not "Allow Butterfly Capturing" is enabled.
     */
    public static boolean isButterflyKillingEnabled() {
        return isButterflyKillingEnabled;
    }

    /**
     * @return Whether or not "Allow Capturing After Kinging" is enabled.
     */
    public static boolean isKillAfterKingingEnabled() {
        return isKillAfterKingingEnabled;
    }

    /**
     * Applies the changes that were pushed to the SharedPreferences.Editor to the actual SharedPreferences asynchronously.
     */
    public static void apply() {
        editor.apply();
    }
}
