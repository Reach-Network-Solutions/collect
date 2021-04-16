package app.nexusforms.android.formentry;

import androidx.annotation.NonNull;

import app.nexusforms.android.javarosawrapper.FormController;

public interface RequiresFormController {
    void formLoaded(@NonNull FormController formController);
}
