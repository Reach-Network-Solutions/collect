package app.nexusforms.collect.android.utilities;

import org.junit.Test;
import app.nexusforms.android.logic.PropertyManager;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.Settings;

import app.nexusforms.android.utilities.WebCredentialsUtils;
import app.nexusforms.android.preferences.source.Settings;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WebCredentialsUtilsTest {

    @Test
    public void saveCredentialsPreferencesMethod_shouldSaveNewCredentialsAndReloadPropertyManager() {
        Settings generalSettings = mock(Settings.class);
        WebCredentialsUtils webCredentialsUtils = new WebCredentialsUtils(generalSettings);
        PropertyManager propertyManager = mock(PropertyManager.class);

        webCredentialsUtils.saveCredentialsPreferences("username", "password", propertyManager);

        verify(generalSettings, times(1)).save(GeneralKeys.KEY_USERNAME, "username");
        verify(generalSettings, times(1)).save(GeneralKeys.KEY_PASSWORD, "password");
        verify(propertyManager, times(1)).reload();
    }
}
