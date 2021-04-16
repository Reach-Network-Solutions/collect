package app.nexusforms.android.metadata;

import app.nexusforms.android.preferences.source.Settings;
import app.nexusforms.utilities.RandomString;

public class SharedPreferencesInstallIDProvider implements InstallIDProvider {

    private final Settings metaPreferences;
    private final String preferencesKey;

    public SharedPreferencesInstallIDProvider(Settings metaPreferences, String preferencesKey) {
        this.metaPreferences = metaPreferences;
        this.preferencesKey = preferencesKey;
    }

    @Override
    public String getInstallID() {
        if (metaPreferences.contains(preferencesKey)) {
            return metaPreferences.getString(preferencesKey);
        } else {
            return generateAndStoreInstallID();
        }
    }

    private String generateAndStoreInstallID() {
        String installID = "collect:" + RandomString.randomString(16);
        metaPreferences.save(preferencesKey, installID);

        return installID;
    }
}
