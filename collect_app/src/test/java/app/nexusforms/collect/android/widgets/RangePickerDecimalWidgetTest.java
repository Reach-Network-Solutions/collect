package app.nexusforms.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.listeners.WidgetValueChangedListener;
import org.robolectric.RobolectricTestRunner;
import java.math.BigDecimal;

import app.nexusforms.android.widgets.RangePickerDecimalWidget;
import app.nexusforms.collect.android.widgets.support.QuestionWidgetHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class RangePickerDecimalWidgetTest {

    private RangeQuestion rangeQuestion;

    @Before
    public void setup() {
        rangeQuestion = mock(RangeQuestion.class);

        when(rangeQuestion.getRangeStart()).thenReturn(new BigDecimal("1.5"));
        when(rangeQuestion.getRangeEnd()).thenReturn(new BigDecimal("5.5"));
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal("0.5"));
    }

    @Test
    public void whenRangeEndIsGreaterThanRangeStart_widgetsSetsCorrectValuesForNumberPicker() {
        RangePickerDecimalWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null));
        int index = 0;

        for (double i = 5.5F; i >= 1.5F; i -= 0.5F, index++) {
            assertThat(widget.displayedValuesForNumberPicker[index], equalTo(String.valueOf(i)));
        }
    }

    @Test
    public void whenRangeStartIsGreaterThanRangeEnd_widgetsSetsCorrectValuesForNumberPicker() {
        when(rangeQuestion.getRangeStart()).thenReturn(new BigDecimal("5.5"));
        when(rangeQuestion.getRangeEnd()).thenReturn(new BigDecimal("1.5"));
        RangePickerDecimalWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null));
        int index = 0;

        for (double i = 1.5F; i <= 5.5F; i += 0.5F, index++) {
            assertThat(widget.displayedValuesForNumberPicker[index], equalTo(String.valueOf(i)));
        }
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef(rangeQuestion)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangePickerDecimalWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.getAnswer().getValue(), equalTo(2.5));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangePickerDecimalWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.binding.widgetAnswerText.getText(), equalTo(widget.getContext().getString(R.string.no_value_selected)));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangePickerDecimalWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setNumberPickerValue_updatesAnswer() {
        RangePickerDecimalWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setNumberPickerValue(4);

        assertThat(widget.getAnswer().getDisplayText(), equalTo("3.5"));
    }

    @Test
    public void setNumberPickerValue_whenRangeStartIsGreaterThenRangeEnd_updatesAnswer() {
        when(rangeQuestion.getRangeStart()).thenReturn(new BigDecimal("5.5"));
        when(rangeQuestion.getRangeEnd()).thenReturn(new BigDecimal("1.5"));

        RangePickerDecimalWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setNumberPickerValue(4);

        assertThat(widget.getAnswer().getDisplayText(), equalTo("3.5"));
    }

    @Test
    public void clickingWidgetForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);

        RangePickerDecimalWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.binding.widgetButton.performLongClick();
        widget.binding.widgetAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.widgetButton);
        verify(listener).onLongClick(widget.binding.widgetAnswerText);
    }

    private RangePickerDecimalWidget createWidget(FormEntryPrompt prompt) {
        return new RangePickerDecimalWidget(QuestionWidgetHelpers.widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
