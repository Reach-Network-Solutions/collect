package app.nexusforms.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.source.Settings;

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