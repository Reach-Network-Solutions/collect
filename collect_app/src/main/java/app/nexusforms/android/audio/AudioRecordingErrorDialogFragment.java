package app.nexusforms.android.audio;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import app.nexusforms.android.R;

import app.nexusforms.android.injection.DaggerUtils;

import app.nexusforms.audiorecorder.recording.AudioRecorder;
import app.nexusforms.audiorecorder.recording.MicInUseException;
import app.nexusforms.shared.data.Consumable;

import javax.inject.Inject;

public class AudioRecordingErrorDialogFragment extends DialogFragment {

    @Inject
    AudioRecorder audioRecorder;

    @Nullable
    Consumable<Exception> exception;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
        exception = audioRecorder.failedToStart().getValue();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setPositiveButton(R.string.ok, null);

        if (exception != null && exception.getValue() instanceof MicInUseException) {
            dialogBuilder.setMessage(R.string.mic_in_use);
        } else {
            dialogBuilder.setMessage(R.string.start_recording_failed);
        }

        return dialogBuilder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (exception != null) {
            exception.consume();
        }
    }
}
