package app.nexusforms.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.source.Settings;

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
