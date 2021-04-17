package app.nexusforms.android.application.initialization.migration;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.source.Settings;

@RunWith(AndroidJUnit4.class)
public class KeyExtractorTest {

    private final Settings prefs = TestSettingsProvider.getTestSettings("test");

    @Test
    public void createsNewKeyBasedOnExistingKeysValue() {
        SharedPreferenceUtils.initPrefs(prefs,
                "oldKey", "blah"
        );

        MigrationUtils.extractNewKey("newKey").fromKey("oldKey")
                .fromValue("blah").toValue("newBlah")
                .apply(prefs);

        SharedPreferenceUtils.assertPrefs(prefs,
                "oldKey", "blah",
                "newKey", "newBlah"
        );
    }

    @Test
    public void whenNewKeyExists_doesNothing() {
        SharedPreferenceUtils.initPrefs(prefs,
                "oldKey", "oldBlah",
                "newKey", "existing"
        );

        MigrationUtils.extractNewKey("newKey").fromKey("oldKey")
                .fromValue("oldBlah").toValue("newBlah")
                .apply(prefs);

        SharedPreferenceUtils.assertPrefs(prefs,
                "oldKey", "oldBlah",
                "newKey", "existing"
        );
    }

    @Test
    public void whenOldKeyMissing_doesNothing() {
        SharedPreferenceUtils.initPrefs(prefs);

        MigrationUtils.extractNewKey("newKey").fromKey("oldKey")
                .fromValue("oldBlah").toValue("newBlah")
                .apply(prefs);

        SharedPreferenceUtils.assertPrefsEmpty(prefs);
    }
}