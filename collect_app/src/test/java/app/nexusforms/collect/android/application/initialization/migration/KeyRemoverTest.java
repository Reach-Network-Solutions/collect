package app.nexusforms.collect.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.preferences.source.Settings;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.android.application.initialization.migration.MigrationUtils;
import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.source.Settings;

import static app.nexusforms.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class KeyRemoverTest {

    private final Settings prefs = TestSettingsProvider.getTestSettings("test");

    @Test
    public void whenKeyDoesNotExist_doesNothing() {
        SharedPreferenceUtils.initPrefs(prefs);

        MigrationUtils.removeKey("blah").apply(prefs);

        SharedPreferenceUtils.assertPrefsEmpty(prefs);
    }
}
