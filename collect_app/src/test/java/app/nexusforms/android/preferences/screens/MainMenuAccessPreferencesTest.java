package app.nexusforms.android.preferences.screens;

import android.content.Context;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.preference.CheckBoxPreference;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.FormUpdateMode;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.Settings;
import app.nexusforms.android.preferences.Protocol;

import app.nexusforms.android.preferences.screens.AdminPreferencesFragment;
import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.keys.AdminKeys;
import app.nexusforms.android.preferences.source.Settings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class MainMenuAccessPreferencesTest {

    private Context context;
    private final Settings generalSettings = TestSettingsProvider.getGeneralSettings();
    private final Settings adminSettings = TestSettingsProvider.getAdminSettings();

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        generalSettings.clear();
        generalSettings.setDefaultForAllSettingsWithoutValues();
        adminSettings.clear();
        adminSettings.setDefaultForAllSettingsWithoutValues();
    }

    @Test
    public void whenMatchExactlyEnabled_showsGetBlankFormAsUncheckedAndDisabled() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context));

        FragmentScenario<AdminPreferencesFragment.MainMenuAccessPreferences> scenario = FragmentScenario.launch(AdminPreferencesFragment.MainMenuAccessPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference getBlankForm = f.findPreference(AdminKeys.KEY_GET_BLANK);
            assertThat(getBlankForm.isEnabled(), is(false));
            assertThat(getBlankForm.isChecked(), is(false));
            assertThat(adminSettings.getBoolean(AdminKeys.KEY_GET_BLANK), is(true));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_andGoogleUsedAsProtocol_getBlankFormIsEnabled() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context));
        generalSettings.save(GeneralKeys.KEY_PROTOCOL, Protocol.GOOGLE.getValue(context));

        FragmentScenario<AdminPreferencesFragment.MainMenuAccessPreferences> scenario = FragmentScenario.launch(AdminPreferencesFragment.MainMenuAccessPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference getBlankForm = f.findPreference(AdminKeys.KEY_GET_BLANK);
            assertThat(getBlankForm.isEnabled(), is(true));
        });
    }
}