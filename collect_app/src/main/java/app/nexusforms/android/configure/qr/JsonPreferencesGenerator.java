package app.nexusforms.android.configure.qr;

import org.json.JSONException;
import org.json.JSONObject;

import app.nexusforms.android.preferences.keys.AdminKeys;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.SettingsProvider;

import app.nexusforms.android.preferences.source.SettingsProvider;

import java.util.Collection;

import timber.log.Timber;

public class JsonPreferencesGenerator {
    private final SettingsProvider settingsProvider;
    public JsonPreferencesGenerator(SettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    public String getJSONFromPreferences(Collection<String> includedPasswordKeys) throws JSONException {
        JSONObject sharedPrefJson = getPrefsAsJson(includedPasswordKeys);
        Timber.i(sharedPrefJson.toString());
        return sharedPrefJson.toString();
    }

    private JSONObject getPrefsAsJson(Collection<String> includedPasswordKeys) throws JSONException {
        JSONObject prefs = new JSONObject();
        prefs.put("general", getGeneralPrefsAsJson(includedPasswordKeys));
        prefs.put("admin", getAdminPrefsAsJson(includedPasswordKeys));
        return prefs;
    }

    private JSONObject getGeneralPrefsAsJson(Collection<String> includedPasswordKeys) throws JSONException {
        JSONObject generalPrefs = new JSONObject();

        for (String key : GeneralKeys.DEFAULTS.keySet()) {
            if (key.equals(GeneralKeys.KEY_PASSWORD) && !includedPasswordKeys.contains(GeneralKeys.KEY_PASSWORD)) {
                continue;
            }

            Object defaultValue = GeneralKeys.DEFAULTS.get(key);
            Object value = settingsProvider.getGeneralSettings().getAll().get(key);

            if (value == null) {
                value = "";
            }
            if (defaultValue == null) {
                defaultValue = "";
            }

            if (!defaultValue.equals(value)) {
                generalPrefs.put(key, value);
            }
        }
        return generalPrefs;
    }

    private JSONObject getAdminPrefsAsJson(Collection<String> includedPasswordKeys) throws JSONException {
        JSONObject adminPrefs = new JSONObject();

        for (String key : AdminKeys.ALL_KEYS) {
            if (key.equals(AdminKeys.KEY_ADMIN_PW) && !includedPasswordKeys.contains(AdminKeys.KEY_ADMIN_PW)) {
                continue;
            }

            Object defaultValue = AdminKeys.getDefaults().get(key);
            Object value = settingsProvider.getAdminSettings().getAll().get(key);

            if (defaultValue != value) {
                adminPrefs.put(key, value);
            }
        }
        return adminPrefs;
    }
}
