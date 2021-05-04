package app.nexusforms.android.widgets;

import androidx.annotation.NonNull;

import org.javarosa.core.model.data.IntegerData;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import org.junit.Test;

import app.nexusforms.android.widgets.base.GeneralStringWidgetTest;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;
import static app.nexusforms.android.utilities.Appearances.THOUSANDS_SEP;

/**
 * @author James Knight
 */
public class IntegerWidgetTest extends GeneralStringWidgetTest<IntegerWidget, IntegerData> {

    @NonNull
    @Override
    public IntegerWidget createWidget() {
        return new IntegerWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID", readOnlyOverride));
    }

    @NonNull
    @Override
    public IntegerData getNextAnswer() {
        return new IntegerData(randomInteger());
    }

    private int randomInteger() {
        return Math.abs(random.nextInt()) % 1_000_000_000;
    }

    @Test
    public void digitsAboveLimitOfNineShouldBeTruncatedFromRight() {
        getWidget().answerTextInputLayout.setText("123456789123");
        assertEquals("123456789", getWidget().getAnswerText());
    }

    @Test
    public void separatorsShouldBeAddedWhenEnabled() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(THOUSANDS_SEP);
        getWidget().answerTextInputLayout.setText("123456789");
        assertEquals("123,456,789", getWidget().answerTextInputLayout.getText().toString());
    }
}
