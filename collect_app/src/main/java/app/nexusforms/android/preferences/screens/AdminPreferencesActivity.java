/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package app.nexusforms.android.preferences.screens;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import app.nexusforms.android.R;
import app.nexusforms.android.activities.CollectAbstractActivity;
import app.nexusforms.android.activities.MainActivity;
import app.nexusforms.android.activities.MainMenuActivity;
import app.nexusforms.android.fragments.dialogs.MovingBackwardsDialog;
import app.nexusforms.android.fragments.dialogs.ResetSettingsResultDialog;
import app.nexusforms.android.utilities.ThemeUtils;

import static app.nexusforms.android.activities.ActivityUtils.startActivityAndCloseAllOthers;

/**
 * Handles admin preferences, which are password-protectable and govern which app features and
 * general preferences the end user of the app will be able to see.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com; constraint behavior
 *         option)
 */
public class AdminPreferencesActivity extends CollectAbstractActivity implements MovingBackwardsDialog.MovingBackwardsDialogListener,
        ResetSettingsResultDialog.ResetSettingsResultDialogListener {
    public static final String ADMIN_PREFERENCES = "admin_prefs";
    public static final String TAG = "AdminPreferencesFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_layout);
        setTheme(new ThemeUtils(this).getSettingsTheme());

        setTitle(R.string.admin_preferences);

        Fragment fragment = new AdminPreferencesFragment();
        Bundle args = new Bundle();
        args.putBoolean(GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE, true);
        fragment.setArguments(args);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.preferences_fragment_container, fragment, TAG)
                    .commit();
        }
    }

    @Override
    public void preventOtherWaysOfEditingForm() {
        AdminPreferencesFragment fragment = (AdminPreferencesFragment) getSupportFragmentManager().findFragmentByTag(TAG);
        fragment.preventOtherWaysOfEditingForm();
    }

    @Override
    public void onDialogClosed() {
        startActivityAndCloseAllOthers(this, MainActivity.class);
    }
}
