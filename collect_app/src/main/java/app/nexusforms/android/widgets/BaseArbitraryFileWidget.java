package app.nexusforms.android.widgets;

import android.content.Context;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;

import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.utilities.MediaUtils;
import app.nexusforms.android.utilities.QuestionMediaManager;
import app.nexusforms.android.widgets.interfaces.FileWidget;
import app.nexusforms.android.widgets.interfaces.WidgetDataReceiver;
import app.nexusforms.android.widgets.utilities.WaitingForDataRegistry;

import java.io.File;

import timber.log.Timber;

public abstract class BaseArbitraryFileWidget extends QuestionWidget implements FileWidget, WidgetDataReceiver {
    @NonNull
    protected final MediaUtils mediaUtils;

    private final QuestionMediaManager questionMediaManager;
    protected final WaitingForDataRegistry waitingForDataRegistry;

    protected File answerFile;

    public BaseArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull MediaUtils mediaUtils,
                                   QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.mediaUtils = mediaUtils;
        this.questionMediaManager = questionMediaManager;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    public IAnswerData getAnswer() {
        return answerFile != null ? new StringData(answerFile.getName()) : null;
    }

    @Override
    public void deleteFile() {
        questionMediaManager.deleteAnswerFile(getFormEntryPrompt().getIndex().toString(), answerFile.getAbsolutePath());
        answerFile = null;
        hideAnswerText();
    }

    @Override
    public void setData(Object object) {
        if (answerFile != null) {
            deleteFile();
        }

        if (object instanceof File) {
            answerFile = (File) object;
            if (answerFile.exists()) {
                questionMediaManager.replaceAnswerFile(getFormEntryPrompt().getIndex().toString(), answerFile.getAbsolutePath());
                showAnswerText();
                widgetValueChanged();
            } else {
                answerFile = null;
                Timber.e("Inserting Arbitrary file FAILED");
            }
        } else if (object != null) {
            Timber.e("FileWidget's setBinaryData must receive a File but received: %s", object.getClass());
        }
    }

    protected void setupAnswerFile(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            answerFile = new File(getInstanceFolder() + File.separator + fileName);
        }
    }

    protected abstract void showAnswerText();

    protected abstract void hideAnswerText();
}