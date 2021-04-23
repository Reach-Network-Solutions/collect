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

package app.nexusforms.android.audio;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import app.nexusforms.android.R;
import app.nexusforms.android.databinding.NexusAudioControllerLayoutBinding;
import timber.log.Timber;

import static app.nexusforms.strings.format.LengthFormatterKt.formatLength;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class NexusAudioControllerView extends FrameLayout {

    public final NexusAudioControllerLayoutBinding nexusAudioControllerLayoutBinding;

    private final TextView currentDurationLabel;
    private final TextView totalDurationLabel;
    private final ImageButton playPauseButton;
    private final ImageButton recordOrDeleteButton;
   // private final SeekBar seekBar;
    private final SwipeListener swipeListener;

    private boolean playing;
    private int position;
    private int duration;

    private Listener listener;

    public NexusAudioControllerView(Context context) {
        this(context, null);
    }

    public NexusAudioControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        nexusAudioControllerLayoutBinding = NexusAudioControllerLayoutBinding.inflate(LayoutInflater.from(context), this, true);
        playPauseButton = nexusAudioControllerLayoutBinding.playOrPauseImageButton;
        recordOrDeleteButton = nexusAudioControllerLayoutBinding.recordOrDeleteImageButton;
        currentDurationLabel = nexusAudioControllerLayoutBinding.playProgress;
        totalDurationLabel = nexusAudioControllerLayoutBinding.audioLength;
        //seekBar = binding.seekBar;

        swipeListener = new SwipeListener();

        nexusAudioControllerLayoutBinding.nexusAudioWaveform.setOnSeekBarChangeListener(swipeListener);

        playPauseButton.setOnClickListener(view -> playClicked());

        recordOrDeleteButton.setOnClickListener(view ->
        {
            if(listener != null){
                listener.onRecordOrRemoveClicked();
            }else{
                Timber.d("LISTENER is null");
                //there's no file - request recording

            }
        });
    }

    private void playClicked() {
        if (listener == null) {
            return;
        }

        if (playing) {
            listener.onPauseClicked();
        } else {
            listener.onPlayClicked();
        }
    }

    private void onPositionChanged(Integer newPosition) {
        Integer correctedPosition = max(0, min(duration, newPosition));

        setPosition(correctedPosition);
        if (listener != null) {
            listener.onPositionChanged(correctedPosition);
        }

        double progress = (double)newPosition/(double)duration;

        nexusAudioControllerLayoutBinding.nexusAudioWaveform.updatePlayerPercent((float)progress);
    }

    public void setPlaying(Boolean playing) {
        this.playing = playing;

        if (playing) {
            playPauseButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_pause_24_nexus));
        } else {
            playPauseButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_play_arrow_40_nexus));
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setPosition(Integer position) {
        if (!swipeListener.isSwiping()) {
           renderPosition(position);
        }
    }

    public void setDuration(Integer duration) {
        this.duration = duration;

        totalDurationLabel.setText(formatLength((long) duration));
        nexusAudioControllerLayoutBinding.nexusAudioWaveform.setMax(duration);
        setPosition(0);
    }

    private void renderPosition(Integer position) {
        this.position = position;

        currentDurationLabel.setText(formatLength((long) position));

        double progress = (double)position/(double)duration;
        //listener.onPositionChanged(position);
        nexusAudioControllerLayoutBinding.nexusAudioWaveform.updatePlayerPercent((float)progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //seekBar.setProgress(position, true);

        } else {
            //seekBar.setProgress(position);
        }
    }


    public interface Listener {

        void onPlayClicked();

        void onPauseClicked();

        void onPositionChanged(Integer newPosition);

        void onRecordOrRemoveClicked();
    }

    private class SwipeListener implements SeekBar.OnSeekBarChangeListener {

        private Boolean wasPlaying = false;
        private Boolean swiping = false;

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            swiping = true;

            if (playing) {
                listener.onPauseClicked();
                wasPlaying = true;
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int newProgress, boolean fromUser) {
            Timber.d("PROGRESS CHANGED TO %s", newProgress);
            if (fromUser) {
                renderPosition(newProgress);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            swiping = false;
            onPositionChanged(position);

            if (wasPlaying) {
                listener.onPlayClicked();
                wasPlaying = false;
            }
        }

        Boolean isSwiping() {
            return swiping;
        }
    }
}
