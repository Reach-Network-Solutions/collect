package app.nexusforms.android.preferences.screens;

import android.os.Bundle;

import app.nexusforms.android.R;

public class CustomServerPathsPreferencesFragment extends BaseGeneralPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.custom_server_paths_preferences, rootKey);
    }
}
