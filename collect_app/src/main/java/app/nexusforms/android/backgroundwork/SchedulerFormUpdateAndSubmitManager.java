package app.nexusforms.android.backgroundwork;

import android.app.Application;

import app.nexusforms.android.preferences.source.Settings;
import app.nexusforms.android.preferences.Protocol;
import app.nexusforms.async.Scheduler;

import static app.nexusforms.android.configure.SettingsUtils.getFormUpdateMode;
import static app.nexusforms.android.preferences.keys.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static app.nexusforms.android.preferences.keys.GeneralKeys.KEY_PROTOCOL;

public class SchedulerFormUpdateAndSubmitManager implements FormUpdateManager, FormSubmitManager {

    private static final String MATCH_EXACTLY_SYNC_TAG = "match_exactly";
    private static final String AUTO_UPDATE_TAG = "serverPollingJob";
    public static final String AUTO_SEND_TAG = "AutoSendWorker";

    private final Scheduler scheduler;
    private final Settings generalSettings;
    private final Application application;

    public SchedulerFormUpdateAndSubmitManager(Scheduler scheduler, Settings generalSettings, Application application) {
        this.scheduler = scheduler;
        this.generalSettings = generalSettings;
        this.application = application;
    }

    @Override
    public void scheduleUpdates() {
        String protocol = generalSettings.getString(KEY_PROTOCOL);
        if (Protocol.parse(application, protocol) == Protocol.GOOGLE) {
            scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
            scheduler.cancelDeferred(AUTO_UPDATE_TAG);
            return;
        }

        String period = generalSettings.getString(KEY_PERIODIC_FORM_UPDATES_CHECK);
        long periodInMilliseconds = BackgroundWorkUtils.getPeriodInMilliseconds(period, application);

        switch (getFormUpdateMode(application, generalSettings)) {
            case MANUAL:
                scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
                scheduler.cancelDeferred(AUTO_UPDATE_TAG);
                break;
            case PREVIOUSLY_DOWNLOADED_ONLY:
                scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
                scheduler.networkDeferred(AUTO_UPDATE_TAG, new AutoUpdateTaskSpec(), periodInMilliseconds);
                break;
            case MATCH_EXACTLY:
                scheduler.cancelDeferred(AUTO_UPDATE_TAG);
                scheduler.networkDeferred(MATCH_EXACTLY_SYNC_TAG, new SyncFormsTaskSpec(), periodInMilliseconds);
                break;
        }
    }

    @Override
    public void scheduleSubmit() {
        scheduler.networkDeferred(AUTO_SEND_TAG, new AutoSendTaskSpec());
    }
}
