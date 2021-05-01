package app.nexusforms.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.source.Settings;

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
