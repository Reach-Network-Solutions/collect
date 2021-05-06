package app.nexusforms.android.preferences.screens;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import app.nexusforms.android.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;

import org.jetbrains.annotations.NotNull;
import app.nexusforms.android.activities.CollectAbstractActivity;
import app.nexusforms.android.configure.SettingsChangeHandler;
import app.nexusforms.android.injection.DaggerUtils;
import app.nexusforms.android.preferences.keys.AdminKeys;
import app.nexusforms.android.preferences.DisabledPreferencesRemover;
import app.nexusforms.android.preferences.nexus.DataStoreManager;
import app.nexusforms.android.preferences.source.Settings;
import app.nexusforms.android.preferences.source.SettingsProvider;

import javax.inject.Inject;

public abstract class BasePreferencesFragment extends PreferenceFragmentCompat implements Settings.OnSettingChangeListener {

    @Inject
    SettingsChangeHandler settingsChangeHandler;

    @Inject
    SettingsProvider settingsProvider;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        super.onDisplayPreferenceDialog(preference);

        // If we don't do this there is extra padding on "Cancel" and "OK" on
        // the preference dialogs. This appears to have something to with the `updateLocale`
        // calls in `CollectAbstractActivity` and weirdly only happens for English.
        DialogPreference dialogPreference = (DialogPreference) preference;
        dialogPreference.setNegativeButtonText(R.string.cancel);
        dialogPreference.setPositiveButtonText(R.string.ok);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        if (activity instanceof CollectAbstractActivity) {
            ((CollectAbstractActivity) activity).initToolbar(getPreferenceScreen().getTitle());
        }
        removeDisabledPrefs();

        super.onViewCreated(view, savedInstanceState);
    }

    private void removeDisabledPrefs() {
        if (!isInAdminMode()) {
            DisabledPreferencesRemover preferencesRemover = new DisabledPreferencesRemover(this, settingsProvider.getAdminSettings());
            preferencesRemover.remove(AdminKeys.adminToGeneral);
            preferencesRemover.removeEmptyCategories();
        }
    }

    protected boolean isInAdminMode() {
        return getArguments() != null && getArguments().getBoolean(GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE, false);
    }
}
