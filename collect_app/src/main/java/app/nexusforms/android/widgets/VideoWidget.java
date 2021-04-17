/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package app.nexusforms.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import app.nexusforms.android.R;

import app.nexusforms.android.activities.CaptureSelfieVideoActivity;
import app.nexusforms.android.analytics.AnalyticsEvents;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.formentry.questions.WidgetViewUtils;
import app.nexusforms.android.listeners.PermissionListener;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.utilities.Appearances;
import app.nexusforms.android.utilities.ApplicationConstants;
import app.nexusforms.android.utilities.CameraUtils;
import app.nexusforms.android.utilities.MediaUtils;
import app.nexusforms.android.utilities.QuestionMediaManager;
import app.nexusforms.android.utilities.ToastUtils;
import app.nexusforms.android.widgets.interfaces.ButtonClickListener;
import app.nexusforms.android.widgets.interfaces.FileWidget;
import app.nexusforms.android.widgets.interfaces.WidgetDataReceiver;
import app.nexusforms.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

import static app.nexusforms.android.formentry.questions.WidgetViewUtils.createSimpleButton;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class VideoWidget extends QuestionWidget implements FileWidget, ButtonClickListener, WidgetDataReceiver {
    private final WaitingForDataRegistry waitingForDataRegistry;
    private final QuestionMediaManager questionMediaManager;
    private final MediaUtils mediaUtils;

    Button captureButton;
    Button playButton;
    Button chooseButton;
    private String binaryName;

    private final boolean selfie;

    public VideoWidget(Context context, QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        this(context, prompt, waitingForDataRegistry, questionMediaManager, new CameraUtils(), new MediaUtils());
    }

    public VideoWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry, QuestionMediaManager questionMediaManager, CameraUtils cameraUtils, MediaUtils mediaUtils) {
        super(context, questionDetails);

        this.waitingForDataRegistry = waitingForDataRegistry;
        this.questionMediaManager = questionMediaManager;
        this.mediaUtils = mediaUtils;

        selfie = Appearances.isFrontCameraAppearance(getFormEntryPrompt());

        captureButton = WidgetViewUtils.createSimpleButton(getContext(), R.id.capture_video, questionDetails.isReadOnly(), getContext().getString(R.string.capture_video), getAnswerFontSize(), this);

        chooseButton = WidgetViewUtils.createSimpleButton(getContext(), R.id.choose_video, questionDetails.isReadOnly(), getContext().getString(R.string.choose_video), getAnswerFontSize(), this);

        playButton = WidgetViewUtils.createSimpleButton(getContext(), R.id.play_video, false, getContext().getString(R.string.play_video), getAnswerFontSize(), this);
        playButton.setVisibility(VISIBLE);

        // retrieve answer from data model and update ui
        binaryName = questionDetails.getPrompt().getAnswerText();
        playButton.setEnabled(binaryName != null);

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(playButton);
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));

        hideButtonsIfNeeded();

        if (selfie) {
            if (!cameraUtils.isFrontCameraAvailable()) {
                captureButton.setEnabled(false);
                ToastUtils.showLongToast(R.string.error_front_camera_unavailable);
            }
        }
    }

    @Override
    public void deleteFile() {
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(),
                        getInstanceFolder() + File.separator + binaryName);
        binaryName = null;
    }

    @Override
    public void clearAnswer() {
        // remove the file
        deleteFile();

        // reset buttons
        playButton.setEnabled(false);

        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        if (binaryName != null) {
            return new StringData(binaryName);
        } else {
            return null;
        }
    }

    @Override
    public void setData(Object object) {
        if (binaryName != null) {
            deleteFile();
        }

        if (object instanceof File) {
            File newVideo = (File) object;
            if (newVideo.exists()) {
                questionMediaManager.replaceAnswerFile(getFormEntryPrompt().getIndex().toString(), newVideo.getAbsolutePath());
                binaryName = newVideo.getName();
                widgetValueChanged();
                playButton.setEnabled(binaryName != null);
            } else {
                Timber.e("Inserting Video file FAILED");
            }
        } else {
            Timber.e("VideoWidget's setBinaryData must receive a File or Uri object.");
        }
    }

    private void hideButtonsIfNeeded() {
        if (selfie || (getFormEntryPrompt().getAppearanceHint() != null
                && getFormEntryPrompt().getAppearanceHint().toLowerCase(Locale.ENGLISH).contains(Appearances.NEW))) {
            chooseButton.setVisibility(GONE);
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
        playButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
        playButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int id) {
        switch (id) {
            case R.id.capture_video:
                if (selfie) {
                    getPermissionsProvider().requestCameraAndRecordAudioPermissions((Activity) getContext(), new PermissionListener() {
                        @Override
                        public void granted() {
                            captureVideo();
                        }

                        @Override
                        public void denied() {
                        }
                    });
                } else {
                    getPermissionsProvider().requestCameraPermission((Activity) getContext(), new PermissionListener() {
                        @Override
                        public void granted() {
                            captureVideo();
                        }

                        @Override
                        public void denied() {
                        }
                    });
                }
                break;
            case R.id.choose_video:
                chooseVideo();
                break;
            case R.id.play_video:
                playVideoFile();
                break;
        }
    }

    private void captureVideo() {
        Intent i;
        if (selfie) {
            i = new Intent(getContext(), CaptureSelfieVideoActivity.class);
        } else {
            i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }

        // request high resolution if configured for that...
        boolean highResolution = settingsProvider.getGeneralSettings().getBoolean(GeneralKeys.KEY_HIGH_RESOLUTION);
        if (highResolution) {
            i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            analytics.logEvent(AnalyticsEvents.REQUEST_HIGH_RES_VIDEO, getQuestionDetails().getFormAnalyticsID(), "");
        } else {
            analytics.logEvent(AnalyticsEvents.REQUEST_VIDEO_NOT_HIGH_RES, getQuestionDetails().getFormAnalyticsID(), "");
        }
        try {
            waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
            ((Activity) getContext()).startActivityForResult(i,
                    ApplicationConstants.RequestCodes.VIDEO_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            getContext().getString(R.string.capture_video)), Toast.LENGTH_SHORT)
                    .show();
            waitingForDataRegistry.cancelWaitingForData();
        }
    }

    private void chooseVideo() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        try {
            waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
            ((Activity) getContext()).startActivityForResult(i,
                    ApplicationConstants.RequestCodes.VIDEO_CHOOSER);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            getContext().getString(R.string.choose_video)), Toast.LENGTH_SHORT)
                    .show();

            waitingForDataRegistry.cancelWaitingForData();
        }
    }

    private void playVideoFile() {
        File file = new File(getInstanceFolder() + File.separator + binaryName);
        mediaUtils.openFile(getContext(), file, "video/*");
    }
}
