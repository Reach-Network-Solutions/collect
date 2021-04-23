/*
 * Copyright (C) 2018 Shobhit Agarwal
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
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;

import app.nexusforms.android.R;

import app.nexusforms.android.analytics.AnalyticsEvents;
import app.nexusforms.android.audio.NexusAudioControllerView;
import app.nexusforms.android.audio.NexusAudioWaveForm;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.utilities.Appearances;
import app.nexusforms.android.utilities.QuestionMediaManager;
import app.nexusforms.android.widgets.interfaces.FileWidget;
import app.nexusforms.android.widgets.interfaces.WidgetDataReceiver;
import app.nexusforms.android.widgets.utilities.AudioFileRequester;
import app.nexusforms.android.widgets.utilities.AudioPlayer;
import app.nexusforms.android.widgets.utilities.RecordingRequester;
import app.nexusforms.android.widgets.utilities.RecordingStatusHandler;
import app.nexusforms.android.databinding.AudioWidgetAnswerBinding;
import app.nexusforms.audioclips.Clip;

import java.io.File;
import java.util.Locale;

import javax.annotation.Nullable;

import timber.log.Timber;

import static app.nexusforms.strings.format.LengthFormatterKt.formatLength;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class AudioWidget extends QuestionWidget implements FileWidget, WidgetDataReceiver {

    AudioWidgetAnswerBinding binding;

    private final AudioPlayer audioPlayer;
    private final RecordingRequester recordingRequester;
    private final QuestionMediaManager questionMediaManager;
    private final AudioFileRequester audioFileRequester;
    private NexusAudioWaveForm nexusAudioWaveForm;
    byte[] audioBytes;
    private ImageButton recordOrDeleteButton;
    private ImageButton playOrPauseButton;

    private boolean recordingInProgress;
    private String binaryName;

    public AudioWidget(Context context, QuestionDetails questionDetails, QuestionMediaManager questionMediaManager, AudioPlayer audioPlayer, RecordingRequester recordingRequester, AudioFileRequester audioFileRequester, RecordingStatusHandler recordingStatusHandler) {
        super(context, questionDetails);
        this.audioPlayer = audioPlayer;

        this.questionMediaManager = questionMediaManager;
        this.recordingRequester = recordingRequester;
        this.audioFileRequester = audioFileRequester;

        binaryName = questionDetails.getPrompt().getAnswerText();
        updateVisibilities();
        updatePlayerMedia();
        prepareNexusAudioWaveForm(null);

        recordingStatusHandler.onBlockedStatusChange(isRecordingBlocked -> {
            binding.captureButton.setEnabled(!isRecordingBlocked);
            binding.chooseButton.setEnabled(!isRecordingBlocked);
        });

        recordingStatusHandler.onRecordingStatusChange(getFormEntryPrompt(), session -> {
            if (session != null) {
                recordingInProgress = true;
                updateVisibilities();

                binding.audioPlayer.nexusAudioControllerLayoutBinding.audioLength.setText(formatLength(session.first));
                //binding.audioPlayer.waveform.addAmplitude(session.second);
            } else {
                recordingInProgress = false;
                updateVisibilities();
            }
        });
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = AudioWidgetAnswerBinding.inflate(LayoutInflater.from(context));

        binding.captureButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.chooseButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        nexusAudioWaveForm = binding.audioPlayer.nexusAudioControllerLayoutBinding.nexusAudioWaveform;

        binding.captureButton.setOnClickListener(v -> {
            //binding.audioPlayer.waveform.clear();
            recordingRequester.requestRecording(getFormEntryPrompt());
        });

        binding.chooseButton.setOnClickListener(v -> audioFileRequester.requestFile(getFormEntryPrompt()));


        return binding.getRoot();
    }

    @Override
    public void deleteFile() {
        audioPlayer.stop();
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(), getAudioFile().getAbsolutePath());
        binaryName = null;
    }

    @Override
    public void clearAnswer() {
        deleteFile();
        widgetValueChanged();
        updateVisibilities();
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
        if (object instanceof File) {
            File newAudio = (File) object;
            if (newAudio.exists()) {
                if (binaryName != null) {
                    deleteFile();
                }

                questionMediaManager.replaceAnswerFile(getFormEntryPrompt().getIndex().toString(), newAudio.getAbsolutePath());
                binaryName = newAudio.getName();
                updateVisibilities();
                updatePlayerMedia();
                widgetValueChanged();
                prepareNexusAudioWaveForm(newAudio);
                //we have the audio file!
                Timber.d("ACTUAL FILE -> %s", newAudio.getAbsolutePath());
            } else {
                Timber.e("NO AUDIO EXISTS at: %s", newAudio.getAbsolutePath());
            }
        } else {
            Timber.e("AudioWidget's setBinaryData must receive a File object.");
        }
    }

    private void prepareNexusAudioWaveForm(@Nullable File audioFile) {
        //prepare with audio File
        if(audioFile != null) {
            audioBytes = NexusAudioWaveForm.audioFileToBytes(audioFile);
        }
        //prepare with answer data
        if(binaryName != null){
            File referredFile = getAudioFile();
            audioBytes = NexusAudioWaveForm.audioFileToBytes(referredFile);
        }
        nexusAudioWaveForm.updateVisualizer(audioBytes);

    }

    private void updateVisibilities() {
        if (recordingInProgress) {
            binding.captureButton.setVisibility(GONE);
            binding.chooseButton.setVisibility(GONE);
            binding.audioPlayer.nexusAudioControllerLayoutBinding.audioLength.setVisibility(VISIBLE);
            binding.audioPlayer.nexusAudioControllerLayoutBinding.nexusAudioWaveform.setVisibility(VISIBLE);
            //binding.audioPlayer.audioController.setVisibility(GONE);
            binding.audioPlayer.nexusAudioControllerLayoutBinding.recordOrDeleteImageButton.setVisibility(INVISIBLE);
            nexusAudioWaveForm.setVisibility(INVISIBLE);
        } else if (getAnswer() == null) {
            binding.captureButton.setVisibility(VISIBLE);
            binding.chooseButton.setVisibility(VISIBLE);
            binding.audioPlayer.nexusAudioControllerLayoutBinding.audioLength.setVisibility(INVISIBLE);

            //binding.audioPlayer.audioController.setVisibility(GONE);
            nexusAudioWaveForm.setVisibility(INVISIBLE);
            binding.audioPlayer.nexusAudioControllerLayoutBinding.playOrPauseImageButton.setVisibility(INVISIBLE);
            binding.audioPlayer.nexusAudioControllerLayoutBinding.recordOrDeleteImageButton.setVisibility(VISIBLE);

            binding.audioPlayer.nexusAudioControllerLayoutBinding.playProgress.setVisibility(INVISIBLE);
            binding.audioPlayer.nexusAudioControllerLayoutBinding.recordOrDeleteImageButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_mic_none_24 ));
        } else {
            binding.captureButton.setVisibility(GONE);
            binding.chooseButton.setVisibility(GONE);
            binding.audioPlayer.nexusAudioControllerLayoutBinding.audioLength.setVisibility(VISIBLE);
            //binding.audioPlayer.audioController.setVisibility(VISIBLE);
            nexusAudioWaveForm.setVisibility(VISIBLE);
            binding.audioPlayer.nexusAudioControllerLayoutBinding.playProgress.setVisibility(VISIBLE);

            binding.audioPlayer.nexusAudioControllerLayoutBinding.playOrPauseImageButton.setVisibility(VISIBLE);

            binding.audioPlayer.nexusAudioControllerLayoutBinding.recordOrDeleteImageButton.setVisibility(VISIBLE);

            binding.audioPlayer.nexusAudioControllerLayoutBinding.recordOrDeleteImageButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_delete_menu_24 ));
        }

        if (questionDetails.isReadOnly()) {
            binding.captureButton.setVisibility(GONE);
            binding.chooseButton.setVisibility(GONE);
        }

        if (getFormEntryPrompt().getAppearanceHint() != null && getFormEntryPrompt().getAppearanceHint().toLowerCase(Locale.ENGLISH).contains(Appearances.NEW)) {
            binding.chooseButton.setVisibility(GONE);
        }
    }

    private void updatePlayerMedia() {
        if (binaryName != null) {
            Clip clip = new Clip("audio:" + getFormEntryPrompt().getIndex().toString(), getAudioFile().getAbsolutePath());

            Integer audioDuration = getDurationOfFile(clip.getURI());

            audioPlayer.onPlayingChanged(clip.getClipID(), binding.audioPlayer::setPlaying);
            audioPlayer.onPositionChanged(clip.getClipID(), binding.audioPlayer::setPosition);
            binding.audioPlayer.setDuration(audioDuration);
            binding.audioPlayer.setListener(new NexusAudioControllerView.Listener() {
                @Override
                public void onPlayClicked() {
                    audioPlayer.play(clip);
                }

                @Override
                public void onPauseClicked() {
                    audioPlayer.pause();
                }

                @Override
                public void onPositionChanged(Integer newPosition) {
                    analytics.logFormEvent(AnalyticsEvents.AUDIO_PLAYER_SEEK, questionDetails.getFormAnalyticsID());
                    audioPlayer.setPosition(clip.getClipID(), newPosition);

                        double percentage = (double)newPosition / (double)audioDuration;

                        nexusAudioWaveForm.updatePlayerPercent((float) percentage);

                        Timber.d("POSTED POS %s from %s / %s"  , percentage, newPosition, audioDuration);

                }

                @Override
                public void onRecordOrRemoveClicked() {

                    if(recordingInProgress){
                        //just wait
                    }else if(getAnswer() == null){
                        recordingRequester.requestRecording(getFormEntryPrompt());
                    }else{
                        //is tapping delete button

                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle(R.string.delete_answer_file_question)
                            .setMessage(R.string.answer_file_delete_warning)
                            .setPositiveButton(R.string.delete_answer_file, (dialog, which) -> clearAnswer())
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                    }
                }
            });

        }else{
            binding.audioPlayer.setListener(new NexusAudioControllerView.Listener() {

                @Override
                public void onPlayClicked() {

                }

                @Override
                public void onPauseClicked() {

                }

                @Override
                public void onPositionChanged(Integer newPosition) {

                }

                @Override
                public void onRecordOrRemoveClicked() {
                    if(getAnswer() == null){
                        recordingRequester.requestRecording(getFormEntryPrompt());
                    }
                }
            });
        }
    }

    private Integer getDurationOfFile(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return durationString != null ? Integer.parseInt(durationString) : 0;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.captureButton.setOnLongClickListener(l);
        binding.chooseButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.captureButton.cancelLongPress();
        binding.chooseButton.cancelLongPress();
    }

    /**
     * Returns the audio file added to the widget for the current instance
     */
    private File getAudioFile() {
        return questionMediaManager.getAnswerFile(binaryName);
    }
}
