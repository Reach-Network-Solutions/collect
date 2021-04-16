package app.nexusforms.collect.android.widgets;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.widgets.StringWidget;
import app.nexusforms.collect.android.widgets.base.GeneralStringWidgetTest;

/**
 * @author James Knight
 */
public class StringWidgetTest extends GeneralStringWidgetTest<StringWidget, StringData> {

    @NonNull
    @Override
    public StringWidget createWidget() {
        return new StringWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID", readOnlyOverride));
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }
}
