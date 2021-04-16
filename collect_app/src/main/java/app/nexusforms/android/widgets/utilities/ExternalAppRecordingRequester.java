package app.nexusforms.android.widgets.utilities;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Toast;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;

import app.nexusforms.android.analytics.AnalyticsEvents;
import app.nexusforms.android.formentry.FormEntryViewModel;
import app.nexusforms.android.listeners.PermissionListener;
import app.nexusforms.android.permissions.PermissionsProvider;
import app.nexusforms.android.utilities.ActivityAvailability;
import app.nexusforms.android.utilities.ApplicationConstants;

public class ExternalAppRecordingRequester implements RecordingRequester {

    private final Activity activity;
    private final PermissionsProvider permissionsProvider;
    private final ActivityAvailability activityAvailability;
    private final WaitingForDataRegistry waitingForDataRegistry;
    private final FormEntryViewModel formEntryViewModel;

    public ExternalAppRecordingRequester(Activity activity, ActivityAvailability activityAvailability, WaitingForDataRegistry waitingForDataRegistry, PermissionsProvider permissionsProvider, FormEntryViewModel formEntryViewModel) {
        this.activity = activity;
        this.permissionsProvider = permissionsProvider;
        this.activityAvailability = activityAvailability;
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.formEntryViewModel = formEntryViewModel;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionsProvider.requestRecordAudioPermission(activity, new PermissionListener() {
            @Override
            public void granted() {
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString());

                if (activityAvailability.isActivityAvailable(intent)) {
                    waitingForDataRegistry.waitForData(prompt.getIndex());
                    activity.startActivityForResult(intent, ApplicationConstants.RequestCodes.AUDIO_CAPTURE);
                } else {
                    Toast.makeText(activity, activity.getString(R.string.activity_not_found,
                            activity.getString(R.string.capture_audio)), Toast.LENGTH_SHORT).show();
                    waitingForDataRegistry.cancelWaitingForData();
                }
            }

            @Override
            public void denied() {
            }
        });

        formEntryViewModel.logFormEvent(AnalyticsEvents.AUDIO_RECORDING_EXTERNAL);
    }
}
