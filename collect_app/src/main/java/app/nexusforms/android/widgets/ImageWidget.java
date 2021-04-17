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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;

import app.nexusforms.android.BuildConfig;
import app.nexusforms.android.R;

import app.nexusforms.android.activities.CaptureSelfieActivity;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.formentry.questions.WidgetViewUtils;
import app.nexusforms.android.listeners.PermissionListener;
import app.nexusforms.android.utilities.Appearances;
import app.nexusforms.android.utilities.ApplicationConstants;
import app.nexusforms.android.utilities.CameraUtils;
import app.nexusforms.android.utilities.ContentUriProvider;
import app.nexusforms.android.utilities.FileUtils;
import app.nexusforms.android.utilities.MediaUtils;
import app.nexusforms.android.utilities.QuestionMediaManager;
import app.nexusforms.android.widgets.interfaces.ButtonClickListener;
import app.nexusforms.android.widgets.utilities.WaitingForDataRegistry;
import app.nexusforms.android.storage.StoragePathProvider;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

import static app.nexusforms.android.formentry.questions.WidgetViewUtils.createSimpleButton;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class ImageWidget extends BaseImageWidget implements ButtonClickListener {

    Button captureButton;
    Button chooseButton;

    private boolean selfie;

    public ImageWidget(Context context, final QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, prompt, questionMediaManager, waitingForDataRegistry, new MediaUtils());
        imageClickHandler = new ViewImageClickHandler();
        imageCaptureHandler = new ImageCaptureHandler();
        setUpLayout();
        addCurrentImageToLayout();
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));
    }

    @Override
    protected void setUpLayout() {
        super.setUpLayout();

        String appearance = getFormEntryPrompt().getAppearanceHint();
        selfie = Appearances.isFrontCameraAppearance(getFormEntryPrompt());

        captureButton = WidgetViewUtils.createSimpleButton(getContext(), R.id.capture_image, questionDetails.isReadOnly(), getContext().getString(R.string.capture_image), getAnswerFontSize(), this);

        chooseButton = WidgetViewUtils.createSimpleButton(getContext(), R.id.choose_image, questionDetails.isReadOnly(), getContext().getString(R.string.choose_image), getAnswerFontSize(), this);

        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(errorTextView);

        hideButtonsIfNeeded(appearance);
        errorTextView.setVisibility(GONE);

        if (selfie) {
            if (!new CameraUtils().isFrontCameraAvailable()) {
                captureButton.setEnabled(false);
                errorTextView.setText(R.string.error_front_camera_unavailable);
                errorTextView.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public Intent addExtrasToIntent(Intent intent) {
        return intent;
    }

    @Override
    protected boolean doesSupportDefaultValues() {
        return false;
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        // reset buttons
        captureButton.setText(getContext().getString(R.string.capture_image));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        switch (buttonId) {
            case R.id.capture_image:
                getPermissionsProvider().requestCameraPermission((Activity) getContext(), new PermissionListener() {
                    @Override
                    public void granted() {
                        captureImage();
                    }

                    @Override
                    public void denied() {
                    }
                });
                break;
            case R.id.choose_image:
                imageCaptureHandler.chooseImage(R.string.choose_image);
                break;
        }
    }

    private void hideButtonsIfNeeded(String appearance) {
        if (selfie || ((appearance != null
                && appearance.toLowerCase(Locale.ENGLISH).contains(Appearances.NEW)))) {
            chooseButton.setVisibility(GONE);
        }
    }

    private void captureImage() {
        errorTextView.setVisibility(GONE);
        Intent intent;
        if (selfie) {
            intent = new Intent(getContext(), CaptureSelfieActivity.class);
        } else {
            intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            // We give the camera an absolute filename/path where to put the
            // picture because of bug:
            // http://code.google.com/p/android/issues/detail?id=1480
            // The bug appears to be fixed in Android 2.0+, but as of feb 2,
            // 2010, G1 phones only run 1.6. Without specifying the path the
            // images returned by the camera in 1.6 (and earlier) are ~1/4
            // the size. boo.

            try {
                Uri uri = ContentUriProvider.getUriForFile(getContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        new File(new StoragePathProvider().getTmpImageFilePath()));
                // if this gets modified, the onActivityResult in
                // FormEntyActivity will also need to be updated.
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                FileUtils.grantFilePermissions(intent, uri, getContext());
            } catch (IllegalArgumentException e) {
                Timber.e(e);
            }
        }

        imageCaptureHandler.captureImage(intent, ApplicationConstants.RequestCodes.IMAGE_CAPTURE, R.string.capture_image);
    }

}
