package app.nexusforms.android.formentry;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;
import app.nexusforms.android.R;

import app.nexusforms.android.formentry.backgroundlocation.BackgroundLocationViewModel;
import app.nexusforms.android.formentry.questions.AnswersProvider;
import app.nexusforms.android.formentry.saving.FormSaveViewModel;
import app.nexusforms.android.javarosawrapper.FormController;
import app.nexusforms.android.preferences.keys.AdminKeys;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.screens.GeneralPreferencesActivity;
import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.android.utilities.ApplicationConstants;
import app.nexusforms.android.utilities.DialogUtils;
import app.nexusforms.android.utilities.MenuDelegate;
import app.nexusforms.android.utilities.PlayServicesChecker;

import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.audiorecorder.recording.AudioRecorder;

public class FormEntryMenuDelegate implements MenuDelegate, RequiresFormController {

    private final AppCompatActivity activity;
    private final AnswersProvider answersProvider;
    private final FormIndexAnimationHandler formIndexAnimationHandler;
    private final FormEntryViewModel formEntryViewModel;
    private final FormSaveViewModel formSaveViewModel;
    private final BackgroundLocationViewModel backgroundLocationViewModel;
    private final BackgroundAudioViewModel backgroundAudioViewModel;

    @Nullable
    private FormController formController;
    private final AudioRecorder audioRecorder;
    private final SettingsProvider settingsProvider;

    public FormEntryMenuDelegate(AppCompatActivity activity, AnswersProvider answersProvider,
                                 FormIndexAnimationHandler formIndexAnimationHandler, FormSaveViewModel formSaveViewModel,
                                 FormEntryViewModel formEntryViewModel, AudioRecorder audioRecorder,
                                 BackgroundLocationViewModel backgroundLocationViewModel,
                                 BackgroundAudioViewModel backgroundAudioViewModel, SettingsProvider settingsProvider) {
        this.activity = activity;
        this.answersProvider = answersProvider;
        this.formIndexAnimationHandler = formIndexAnimationHandler;

        this.audioRecorder = audioRecorder;
        this.formEntryViewModel = formEntryViewModel;
        this.formSaveViewModel = formSaveViewModel;
        this.backgroundLocationViewModel = backgroundLocationViewModel;
        this.backgroundAudioViewModel = backgroundAudioViewModel;
        this.settingsProvider = settingsProvider;
    }

    @Override
    public void formLoaded(@NotNull FormController formController) {
        this.formController = formController;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.form_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean useability;

        useability = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_SAVE_MID);

        menu.findItem(R.id.menu_save).setVisible(useability).setEnabled(useability);

        useability = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_JUMP_TO);

        menu.findItem(R.id.menu_goto).setVisible(useability)
                .setEnabled(useability);

        useability = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_CHANGE_LANGUAGE)
                && (formController != null)
                && formController.getLanguages() != null
                && formController.getLanguages().length > 1;

        menu.findItem(R.id.menu_languages).setVisible(useability)
                .setEnabled(useability);

        useability = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_ACCESS_SETTINGS);

        menu.findItem(R.id.menu_preferences).setVisible(useability)
                .setEnabled(useability);

        if (formController != null && formController.currentFormCollectsBackgroundLocation()
                && new PlayServicesChecker().isGooglePlayServicesAvailable(activity)) {
            MenuItem backgroundLocation = menu.findItem(R.id.track_location);
            backgroundLocation.setVisible(true);
            backgroundLocation.setChecked(settingsProvider.getGeneralSettings().getBoolean(GeneralKeys.KEY_BACKGROUND_LOCATION));
        }

        menu.findItem(R.id.menu_add_repeat).setVisible(formEntryViewModel.canAddRepeat());
        menu.findItem(R.id.menu_record_audio).setVisible(formEntryViewModel.hasBackgroundRecording().getValue());
        menu.findItem(R.id.menu_record_audio).setChecked(backgroundAudioViewModel.isBackgroundRecordingEnabled().getValue());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_repeat) {
            if (audioRecorder.isRecording() && !backgroundAudioViewModel.isBackgroundRecording()) {
                DialogUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
            } else {
                formSaveViewModel.saveAnswersForScreen(answersProvider.getAnswers());
                formEntryViewModel.promptForNewRepeat();
                formIndexAnimationHandler.handle(formEntryViewModel.getCurrentIndex());
            }

            return true;
        } else if (item.getItemId() == R.id.menu_preferences) {
            if (audioRecorder.isRecording()) {
                DialogUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
            } else {
                Intent pref = new Intent(activity, GeneralPreferencesActivity.class);
                activity.startActivityForResult(pref, ApplicationConstants.RequestCodes.CHANGE_SETTINGS);
            }

            return true;
        } else if (item.getItemId() == R.id.track_location) {
            backgroundLocationViewModel.backgroundLocationPreferenceToggled(settingsProvider.getGeneralSettings());
            return true;
        } else if (item.getItemId() == R.id.menu_goto) {
            if (audioRecorder.isRecording() && !backgroundAudioViewModel.isBackgroundRecording()) {
                DialogUtils.showIfNotShowing(RecordingWarningDialogFragment.class, activity.getSupportFragmentManager());
            } else {
                formSaveViewModel.saveAnswersForScreen(answersProvider.getAnswers());

                //formEntryViewModel.openHierarchy();
                //Intent i = new Intent(activity, FormHierarchyActivity.class);
               // activity.startActivityForResult(i, ApplicationConstants.RequestCodes.HIERARCHY_ACTIVITY);
            }

            return true;
        } else if (item.getItemId() == R.id.menu_record_audio) {
            boolean enabled = !item.isChecked();

            if (!enabled) {
                new MaterialAlertDialogBuilder(activity)
                        .setMessage(R.string.stop_recording_confirmation)
                        .setPositiveButton(R.string.disable_recording, (dialog, which) -> backgroundAudioViewModel.setBackgroundRecordingEnabled(false))
                        .setNegativeButton(R.string.cancel, null)
                        .create()
                        .show();
            } else {
                new MaterialAlertDialogBuilder(activity)
                        .setMessage(R.string.background_audio_recording_enabled_explanation)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();

                backgroundAudioViewModel.setBackgroundRecordingEnabled(true);
            }

            return true;
        } else {
            return false;
        }
    }
}
