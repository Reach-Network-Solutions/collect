package app.nexusforms.android.application;

import app.nexusforms.analytics.Analytics;

import app.nexusforms.android.backgroundwork.FormUpdateManager;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.Settings;
import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.android.analytics.AnalyticsEvents;
import app.nexusforms.android.configure.ServerRepository;
import app.nexusforms.android.configure.SettingsChangeHandler;
import app.nexusforms.android.logic.PropertyManager;
import app.nexusforms.android.preferences.source.Settings;
import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.android.utilities.FileUtils;

import java.io.ByteArrayInputStream;

public class CollectSettingsChangeHandler implements SettingsChangeHandler {

    private final PropertyManager propertyManager;
    private final FormUpdateManager formUpdateManager;
    private final ServerRepository serverRepository;
    private final Analytics analytics;
    private final SettingsProvider settingsProvider;

    public CollectSettingsChangeHandler(PropertyManager propertyManager, FormUpdateManager formUpdateManager, ServerRepository serverRepository, Analytics analytics, SettingsProvider settingsProvider) {
        this.propertyManager = propertyManager;
        this.formUpdateManager = formUpdateManager;
        this.serverRepository = serverRepository;
        this.analytics = analytics;
        this.settingsProvider = settingsProvider;
    }

    @Override
    public void onSettingChanged(String changedKey, Object newValue) {
        propertyManager.reload();

        if (changedKey.equals(GeneralKeys.KEY_FORM_UPDATE_MODE) || changedKey.equals(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK) || changedKey.equals(GeneralKeys.KEY_PROTOCOL)) {
            formUpdateManager.scheduleUpdates();
        }

        if (changedKey.equals(GeneralKeys.KEY_SERVER_URL)) {
            serverRepository.save((String) newValue);
        }

        if (changedKey.equals(GeneralKeys.KEY_EXTERNAL_APP_RECORDING) && !((Boolean) newValue)) {
            Settings generalSettings = settingsProvider.getGeneralSettings();
            String currentServerUrl = generalSettings.getString(GeneralKeys.KEY_SERVER_URL);
            String serverHash = FileUtils.getMd5Hash(new ByteArrayInputStream(currentServerUrl.getBytes()));

            analytics.logServerEvent(AnalyticsEvents.INTERNAL_RECORDING_OPT_IN, serverHash);
        }
    }
}
