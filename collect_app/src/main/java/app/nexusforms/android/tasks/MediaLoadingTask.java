package app.nexusforms.android.tasks;

import android.net.Uri;
import android.os.AsyncTask;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import app.nexusforms.android.activities.FormEntryActivity;
import app.nexusforms.android.application.Collect;
import app.nexusforms.android.dao.helpers.ContentResolverHelper;
import app.nexusforms.android.fragments.dialogs.ProgressDialogFragment;
import app.nexusforms.android.injection.DaggerUtils;
import app.nexusforms.android.javarosawrapper.FormController;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.android.utilities.FileUtils;
import app.nexusforms.android.utilities.ImageConverter;
import app.nexusforms.android.widgets.BaseImageWidget;
import app.nexusforms.android.widgets.QuestionWidget;

public class MediaLoadingTask extends AsyncTask<Uri, Void, File> {

    @Inject
    SettingsProvider settingsProvider;

    private WeakReference<FormEntryActivity> formEntryActivity;

    public MediaLoadingTask(FormEntryActivity formEntryActivity) {
        onAttach(formEntryActivity);
    }

    public void onAttach(FormEntryActivity formEntryActivity) {
        this.formEntryActivity = new WeakReference<>(formEntryActivity);
        DaggerUtils.getComponent(this.formEntryActivity.get()).inject(this);
    }

    @Override
    protected File doInBackground(Uri... uris) {
        FormController formController = Collect.getInstance().getFormController();

        if (formController != null) {
            File instanceFile = formController.getInstanceFile();
            if (instanceFile != null) {
                String extension = ContentResolverHelper.getFileExtensionFromUri(uris[0]);

                File newFile = FileUtils.createDestinationMediaFile(instanceFile.getParent(), extension);
                FileUtils.saveAnswerFileFromUri(uris[0], newFile, Collect.getInstance());
                QuestionWidget questionWidget = formEntryActivity.get().getWidgetWaitingForBinaryData();

                // apply image conversion if the widget is an image widget
                if (questionWidget instanceof BaseImageWidget) {
                    String imageSizeMode = settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_IMAGE_SIZE);
                    ImageConverter.execute(newFile.getPath(), questionWidget, formEntryActivity.get(), imageSizeMode);
                }
                return newFile;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(File result) {
        Fragment prev = formEntryActivity.get().getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.COLLECT_PROGRESS_DIALOG_TAG);
        if (prev != null && !formEntryActivity.get().isInstanceStateSaved()) {
            ((DialogFragment) prev).dismiss();
        }
        formEntryActivity.get().setWidgetData(result);
    }
}
