package app.nexusforms.android.formentry.saving;

import android.content.Context;

import androidx.annotation.NonNull;

import app.nexusforms.android.R;

import app.nexusforms.android.fragments.dialogs.ProgressDialogFragment;

public class SaveAnswerFileProgressDialogFragment extends ProgressDialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setMessage(getString(R.string.saving_file));
    }
}
