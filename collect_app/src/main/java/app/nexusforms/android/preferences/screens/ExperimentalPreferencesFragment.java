package app.nexusforms.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import app.nexusforms.android.R;
import app.nexusforms.android.activities.MainMenuActivity;
import app.nexusforms.android.injection.DaggerUtils;
import app.nexusforms.android.preferences.keys.GeneralKeys;

import static app.nexusforms.android.activities.ActivityUtils.startActivityAndCloseAllOthers;

public class ExperimentalPreferencesFragment extends BaseGeneralPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.experimental_preferences, rootKey);

        findPreference(GeneralKeys.KEY_MAGENTA_THEME).setOnPreferenceChangeListener((preference, newValue) -> {
            startActivityAndCloseAllOthers(requireActivity(), MainMenuActivity.class);
            return true;
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }
}
