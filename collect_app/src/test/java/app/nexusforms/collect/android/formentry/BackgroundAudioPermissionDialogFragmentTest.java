package app.nexusforms.collect.android.formentry;

import android.content.DialogInterface;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.ViewModel;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;

import app.nexusforms.android.formentry.BackgroundAudioPermissionDialogFragment;
import app.nexusforms.android.formentry.BackgroundAudioViewModel;
import app.nexusforms.collect.android.fakes.FakePermissionsProvider;
import app.nexusforms.android.injection.config.AppDependencyModule;
import app.nexusforms.android.permissions.PermissionsChecker;
import app.nexusforms.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.source.SettingsProvider;
import app.nexusforms.collect.android.support.RobolectricHelpers;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import app.nexusforms.utilities.Clock;

import app.nexusforms.analytics.Analytics;
import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.audiorecorder.recording.AudioRecorder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class BackgroundAudioPermissionDialogFragmentTest {

    private BackgroundAudioViewModel backgroundAudioViewModel;
    private FakePermissionsProvider fakePermissionsProvider;

    @Before
    public void setup() {
        backgroundAudioViewModel = mock(BackgroundAudioViewModel.class);
        fakePermissionsProvider = new FakePermissionsProvider();

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public BackgroundAudioViewModel.Factory providesBackgroundAudioViewModelFactory(AudioRecorder audioRecorder, SettingsProvider settingsProvider, PermissionsChecker permissionsChecker, Clock clock, Analytics analytics) {
                return new BackgroundAudioViewModel.Factory(audioRecorder, settingsProvider.getGeneralSettings(), permissionsChecker, clock, analytics) {
                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) backgroundAudioViewModel;
                    }
                };
            }

            @Override
            public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker) {
                return fakePermissionsProvider;
            }
        });
    }

    @Test
    public void isNotCancellable() {
        FragmentScenario<BackgroundAudioPermissionDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(BackgroundAudioPermissionDialogFragment.class);
        scenario.onFragment(f -> {
            assertThat(f.isCancelable(), is(false));
        });
    }

    @Test
    public void clickingOk_andGrantingPermissions_callsGrantPermission() {
        FragmentScenario<BackgroundAudioPermissionDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(BackgroundAudioPermissionDialogFragment.class);
        scenario.onFragment(f -> {
            AlertDialog dialog = (AlertDialog) f.getDialog();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            assertThat(button.getText(), is(f.getString(R.string.ok)));

            fakePermissionsProvider.setPermissionGranted(true);

            button.performClick();
            verify(backgroundAudioViewModel).grantAudioPermission();
        });
    }

    @Test
    public void clickingOk_andGrantingPermissions_whenGrantPermissionsThrowsIllegalStateException_finishesActivity() {
        doThrow(IllegalStateException.class).when(backgroundAudioViewModel).grantAudioPermission();

        FragmentScenario<BackgroundAudioPermissionDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(BackgroundAudioPermissionDialogFragment.class);
        scenario.onFragment(f -> {
            FragmentActivity activity = f.getActivity(); // Need to grab this here as `getActivity()` will return null later

            AlertDialog dialog = (AlertDialog) f.getDialog();
            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            fakePermissionsProvider.setPermissionGranted(true);

            button.performClick();
            assertThat(activity.isFinishing(), is(true));
        });
    }
}
