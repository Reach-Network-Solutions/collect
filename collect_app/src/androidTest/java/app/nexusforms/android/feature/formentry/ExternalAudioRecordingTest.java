package app.nexusforms.android.feature.formentry;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import app.nexusforms.android.activities.MainMenuActivity;
import app.nexusforms.android.support.FileUtils;
import app.nexusforms.android.support.RunnableRule;
import app.nexusforms.android.support.TestDependencies;
import app.nexusforms.android.support.TestRuleChain;
import app.nexusforms.android.support.pages.MainMenuPage;
import app.nexusforms.android.utilities.ActivityAvailability;

import java.io.File;
import java.io.IOException;

import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;

@RunWith(AndroidJUnit4.class)
public class ExternalAudioRecordingTest {

    public final TestDependencies testDependencies = new TestDependencies() {
        @Override
        public ActivityAvailability providesActivityAvailability(Context context) {
            return new ActivityAvailability(context) {
                @Override
                public boolean isActivityAvailable(Intent intent) {
                    return true;
                }
            };
        }
    };

    public final IntentsTestRule<MainMenuActivity> rule = new IntentsTestRule<>(MainMenuActivity.class);

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO))
            .around(rule)
            .around(new RunnableRule(() -> {
                // Return audio file when RECORD_SOUND_ACTION intent is sent

                try {
                    File stubRecording = File.createTempFile("test", ".m4a");
                    stubRecording.deleteOnExit();
                    FileUtils.copyFileFromAssets("media/test.m4a", stubRecording.getAbsolutePath());

                    Intent intent = new Intent();
                    intent.setData(Uri.fromFile(stubRecording));
                    Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);
                    intending(hasAction(MediaStore.Audio.Media.RECORD_SOUND_ACTION)).respondWith(result);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

    @Test
    public void onAudioQuestion_whenAudioQualityIsExternal_usesExternalRecorder() throws Exception {
        new MainMenuPage(rule)
                .copyForm("external-audio-question.xml")
                .startBlankForm("External Audio Question")
                .clickOnString(R.string.capture_audio)
                .assertContentDescriptionNotDisplayed(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.capture_audio)
                .assertContentDescriptionDisplayed(R.string.play_audio);
    }

    @Test
    public void onAudioQuestion_withoutAudioQuality_usesExternalRecorder() {
        new MainMenuPage(rule)
                .copyForm("audio-question.xml")
                .startBlankForm("Audio Question")
                .clickOnString(R.string.capture_audio)
                .assertContentDescriptionNotDisplayed(R.string.stop_recording)
                .assertTextNotDisplayed(R.string.capture_audio)
                .assertContentDescriptionDisplayed(R.string.play_audio);
    }
}
