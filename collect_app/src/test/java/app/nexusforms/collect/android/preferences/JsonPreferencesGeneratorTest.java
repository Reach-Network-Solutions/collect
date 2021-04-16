package app.nexusforms.collect.android.preferences;

import junit.framework.TestCase;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.TestSettingsProvider;
import app.nexusforms.android.configure.qr.JsonPreferencesGenerator;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;

import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.keys.AdminKeys;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.SettingsProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@RunWith(RobolectricTestRunner.class)
public class JsonPreferencesGeneratorTest extends TestCase {
    private JsonPreferencesGenerator jsonPreferencesGenerator;
    private final SettingsProvider settingsProvider = TestSettingsProvider.getSettingsProvider();

    @Before
    public void setup() {
        jsonPreferencesGenerator = new JsonPreferencesGenerator(settingsProvider);
    }

    @Test
    public void whenAdminPasswordIncluded_shouldBePresentInJson() throws JSONException {
        settingsProvider.getAdminSettings().save(AdminKeys.KEY_ADMIN_PW, "123456");
        String jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(Collections.singletonList(AdminKeys.KEY_ADMIN_PW));
        assertThat(jsonPrefs, containsString("admin_pw"));
        assertThat(jsonPrefs, containsString("123456"));
    }

    @Test
    public void whenAdminPasswordExcluded_shouldNotBePresentInJson() throws JSONException {
        settingsProvider.getAdminSettings().save(AdminKeys.KEY_ADMIN_PW, "123456");
        String jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(new ArrayList<>());
        assertThat(jsonPrefs, not(containsString("admin_pw")));
        assertThat(jsonPrefs, not(containsString("123456")));
    }

    @Test
    public void whenUserPasswordIncluded_shouldBePresentInJson() throws JSONException {
        settingsProvider.getGeneralSettings().save(GeneralKeys.KEY_PASSWORD, "123456");
        String jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(Collections.singletonList(GeneralKeys.KEY_PASSWORD));
        assertThat(jsonPrefs, containsString("password"));
        assertThat(jsonPrefs, containsString("123456"));
    }

    @Test
    public void whenUserPasswordExcluded_shouldNotBePresentInJson() throws JSONException {
        settingsProvider.getGeneralSettings().save(GeneralKeys.KEY_PASSWORD, "123456");
        String jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(new ArrayList<>());
        assertThat(jsonPrefs, not(containsString("password")));
        assertThat(jsonPrefs, not(containsString("123456")));
    }
}