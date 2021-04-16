package app.nexusforms.android.formentry.saving;

import android.net.Uri;

import app.nexusforms.analytics.Analytics;

import app.nexusforms.android.javarosawrapper.FormController;
import app.nexusforms.android.utilities.MediaUtils;
import app.nexusforms.android.tasks.SaveToDiskResult;

import java.util.ArrayList;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, MediaUtils mediaUtils, boolean shouldFinalize, boolean exitAfter,
                          String updatedSaveName, ProgressListener progressListener, Analytics analytics, ArrayList<String> tempFiles);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
