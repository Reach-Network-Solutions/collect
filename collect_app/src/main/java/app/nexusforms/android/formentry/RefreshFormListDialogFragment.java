package app.nexusforms.android.formentry;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import app.nexusforms.android.R;

import app.nexusforms.android.fragments.dialogs.ProgressDialogFragment;
import timber.log.Timber;

public class RefreshFormListDialogFragment extends ProgressDialogFragment {

    protected RefreshFormListDialogFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RefreshFormListDialogFragmentListener) {
            listener = (RefreshFormListDialogFragmentListener) context;
        }else {
            Timber.d("ATTACHING INSTANCE OF -> %s", context.toString());
        }
        setTitle(getString(R.string.downloading_data));
        setMessage(getString(R.string.please_wait));
        setCancelable(false);
    }

    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        ((AlertDialog) dialog).setIcon(android.R.drawable.ic_dialog_info);
    }

    @Override
    protected String getCancelButtonText() {
        return getString(R.string.cancel_loading_form);
    }

    @Override
    protected Cancellable getCancellable() {
        return () -> {
            if (listener != null) {
                listener.onCancelFormLoading();
            }else{
                Timber.d("LISTENER IS NULL");
            }
            dismiss();
            return true;
        };
    }

    public interface RefreshFormListDialogFragmentListener {
        void onCancelFormLoading();
    }
}
