package app.nexusforms.android.formentry.saving;

import android.net.Uri;

import app.nexusforms.analytics.Analytics;

import app.nexusforms.android.javarosawrapper.FormController;
import app.nexusforms.android.utilities.MediaUtils;
import app.nexusforms.android.tasks.SaveFormToDisk;
import app.nexusforms.android.tasks.SaveToDiskResult;

import java.util.ArrayList;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(Uri instanceContentURI, FormController formController, MediaUtils mediaUtils, boolean shouldFinalize, boolean exitAfter,
                                 String updatedSaveName, ProgressListener progressListener, Analytics analytics, ArrayList<String> tempFiles) {
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(formController, mediaUtils, exitAfter, shouldFinalize,
                updatedSaveName, instanceContentURI, analytics, tempFiles);
        return saveFormToDisk.saveForm(progressListener);
    }
}
