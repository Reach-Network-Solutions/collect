package app.nexusforms.android.application.initialization.migration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.source.Settings;

import static app.nexusforms.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static app.nexusforms.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class ValueTranslatorTest {

    private final Settings prefs = TestSettingsProvider.getTestSettings("test");

    @Test
    public void translatesValueForKey() {
        initPrefs(prefs,
                "key", "value"
        );

        MigrationUtils.translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertPrefs(prefs,
                "key", "newValue"
        );
    }

    @Test
    public void doesNotTranslateOtherValues() {
        initPrefs(prefs,
                "key", "otherValue"
        );

        MigrationUtils.translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertPrefs(prefs,
                "key", "otherValue"
        );
    }

    @Test
    public void whenKeyNotInPrefs_doesNothing() {
        initPrefs(prefs,
                "otherKey", "value"
        );

        MigrationUtils.translateValue("value").toValue("newValue").forKey("key").apply(prefs);

        assertPrefs(prefs,
                "otherKey", "value"
        );
    }
}