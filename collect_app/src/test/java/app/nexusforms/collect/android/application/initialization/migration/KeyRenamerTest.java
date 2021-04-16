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
public class KeyRenamerTest {

    private final Settings prefs = TestSettingsProvider.getTestSettings("test");

    @Test
    public void renamesKeys() {
        SharedPreferenceUtils.initPrefs(prefs,
                "colour", "red"
        );

        MigrationUtils.renameKey("colour")
                .toKey("couleur")
                .apply(prefs);

        SharedPreferenceUtils.assertPrefs(prefs,
                "couleur", "red"
        );
    }

    @Test
    public void whenNewKeyExists_doesNotDoAnything() {
        SharedPreferenceUtils.initPrefs(prefs,
                "colour", "red",
                "couleur", "blue"
        );

        MigrationUtils.renameKey("colour")
                .toKey("couleur")
                .apply(prefs);

        SharedPreferenceUtils.assertPrefs(prefs,
                "colour", "red",
                "couleur", "blue"
        );
    }
}