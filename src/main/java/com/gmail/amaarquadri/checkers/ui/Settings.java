package com.gmail.amaarquadri.checkers.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.gmail.amaarquadri.checkers.R;
import com.gmail.amaarquadri.checkers.logic.CheckerBoard;
import com.gmail.amaarquadri.checkers.utility.DataAccessor;
import com.gmail.amaarquadri.checkers.utility.Utils;

/**
 * Created by Amaar on 2016-05-31.
 * This Activity allows the user to edit their settings.
 */
public class Settings extends Activity {
    /**
     * A boolean to keep track of whether or not the user has actually changed any of the settings.
     */
    private static boolean changesMade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //add the fragment
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        changesMade = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (changesMade) {
            Utils.showToast(this, R.string.changes_saved);
            changesMade = false;
            //in case any of the variables associated with the game's rules were changed
            DataAccessor.updateGameRules();
        }
    }

    /**
     * A fragment to wrap the XML for this Activity.
     */
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            //editing any of these three settings will delete the currently saved games
            //also sets it up so that changesMade will be set to true when applicable
            final Context context = getActivity();
            setListener(context, (CheckBoxPreference) findPreference("flyingKingsEnabled"));
            setListener(context, (CheckBoxPreference) findPreference("butterflyKillingEnabled"));
            setListener(context, (CheckBoxPreference) findPreference("killAfterKingingEnabled"));

            //editing this setting will not affect the currently saved games
            //sets it up so that changesMade will be set to true when applicable
            findPreference("rotateEveryTurnEnabled").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return changesMade = true;
                }
            });
        }

        /**
         * Attaches an OnPreferenceChangeListener to a CheckBoxPreference.
         * If there are saved games, the user will be asked for their confirmation to save the changes.
         * If they confirm, the changes will be made and all the saved games will be deleted.
         *
         * @param context The parent Context.
         * @param checkBoxPreference The CheckBoxPreference to attach the OnPreferenceChangeListener to.
         */
        private static void setListener(final Context context, final CheckBoxPreference checkBoxPreference) {
            checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //if there are no saved games
                    if (DataAccessor.getLastSinglePlayerGameData().equals(CheckerBoard.INITIAL_SERIALIZED_BOARD) &&
                            DataAccessor.getLastTwoPlayerGameData().equals(CheckerBoard.INITIAL_SERIALIZED_BOARD)) {
                        changesMade = true;
                        //return true to apply the change to the CheckBoxPreference
                        return true;
                    }
                    //ask the user to confirm with an AlertDialog
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.confirm_settings_change_dialog)
                            .setMessage(R.string.confirm_settings_change_dialog_warning)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //apply changes to check box and delete saved games
                                    checkBoxPreference.setChecked(!checkBoxPreference.isChecked());
                                    DataAccessor.clearLastSinglePlayerGameData();
                                    DataAccessor.clearLastTwoPlayerGameData();
                                    DataAccessor.apply();
                                    changesMade = true;
                                }
                            })
                            .show();
                    //return false to prevent changes from being applied to the CheckBoxPreference
                    return false;
                }
            });
        }
    }
}
