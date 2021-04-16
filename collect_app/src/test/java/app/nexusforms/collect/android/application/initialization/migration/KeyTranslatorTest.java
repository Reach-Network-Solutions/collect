package app.nexusforms.collect.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.preferences.source.Settings;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.android.application.initialization.migration.KeyTranslator;
import app.nexusforms.android.application.initialization.migration.MigrationUtils;
import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.source.Settings;

import static app.nexusforms.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class KeyTranslatorTest {

    private final Settings prefs = TestSettingsProvider.getTestSettings("test");

    @Test
    public void renamesKeyAndTranslatesValues() {
        SharedPreferenceUtils.initPrefs(prefs,
                "colour", "red"
        );

        MigrationUtils.translateKey("colour")
                .toKey("couleur")
                .fromValue("red")
                .toValue("rouge")
                .apply(prefs);

        SharedPreferenceUtils.assertPrefs(prefs,
                "couleur", "rouge"
        );
    }

    @Test
    public void canTranslateMultipleValues() {
        KeyTranslator translator = MigrationUtils.translateKey("colour")
                .toKey("couleur")
                .fromValue("red")
                .toValue("rouge")
                .fromValue("green")
                .toValue("vert");

        SharedPreferenceUtils.initPrefs(prefs,
                "colour", "red"
        );

        translator.apply(prefs);

        SharedPreferenceUtils.assertPrefs(prefs,
                "couleur", "rouge"
        );

        SharedPreferenceUtils.initPrefs(prefs,
                "colour", "green"
        );

        translator.apply(prefs);

        SharedPreferenceUtils.assertPrefs(prefs,
                "couleur", "vert"
        );
    }

    @Test
    public void whenKeyHasUnknownValue_doesNotDoAnything() {
        SharedPreferenceUtils.initPrefs(prefs,
                "colour", "blue"
        );

        MigrationUtils.translateKey("color")
                .toKey("coleur")
                .fromValue("red")
                .toValue("rouge")
                .apply(prefs);

        SharedPreferenceUtils.assertPrefs(prefs,
                "colour", "blue"
        );
    }

    @Test
    public void whenNewKeyExists_doesNotDoAnything() {
        SharedPreferenceUtils.initPrefs(prefs,
                "colour", "red",
                "couleur", "bleu"
        );

        MigrationUtils.translateKey("color")
                .toKey("coleur")
                .fromValue("red")
                .toValue("rouge")
                .apply(prefs);

        SharedPreferenceUtils.assertPrefs(prefs,
                "colour", "red",
                "couleur", "bleu"
        );
    }
}
