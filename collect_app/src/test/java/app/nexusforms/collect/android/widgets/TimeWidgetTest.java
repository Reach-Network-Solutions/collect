package app.nexusforms.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.listeners.WidgetValueChangedListener;
import app.nexusforms.android.widgets.TimeWidget;
import app.nexusforms.collect.android.support.TestScreenContextActivity;
import app.nexusforms.android.utilities.DateTimeUtils;
import app.nexusforms.android.widgets.utilities.DateTimeWidgetUtils;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.collect.android.widgets.support.QuestionWidgetHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class TimeWidgetTest {
    private TestScreenContextActivity widgetActivity;
    private DateTimeWidgetUtils widgetUtils;
    private View.OnLongClickListener onLongClickListener;

    private QuestionDef questionDef;
    private LocalDateTime timeAnswer;

    @Before
    public void setUp() {
        widgetActivity = QuestionWidgetHelpers.widgetTestActivity();

        questionDef = mock(QuestionDef.class);
        onLongClickListener = mock(View.OnLongClickListener.class);
        widgetUtils = mock(DateTimeWidgetUtils.class);

        timeAnswer = DateTimeUtils.getSelectedTime(new LocalDateTime().withTime(12, 10, 0, 0), LocalDateTime.now());
    }

    @Test
    public void usingReadOnlyOption_doesNotShowButton() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef(questionDef));
        assertEquals(widget.binding.timeButton.getVisibility(), View.GONE);
    }

    @Test
    public void whenPromptIsNotReadOnly_buttonShowsCorrectText() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, null));
        assertEquals(widget.binding.timeButton.getText(), widget.getContext().getString(R.string.select_time));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsTime() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate())));
        assertEquals(widget.getAnswer().getDisplayText(), new TimeData(timeAnswer.toDateTime().toDate()).getDisplayText());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_answerTextViewShowsNoTimeSelected() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, null));
        assertEquals(widget.binding.timeAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectTime() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate())));
        assertEquals(widget.binding.timeAnswerText.getText(), DateTimeUtils.getTimeData(timeAnswer.toDateTime()).getDisplayText());
    }

    @Test
    public void clickingButton_callsDisplayTimePickerDialogWithCurrentTime_whenPromptDoesNotHaveAnswer() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, null);
        TimeWidget widget = createWidget(prompt);
        widget.binding.timeButton.performClick();

        verify(widgetUtils).showTimePickerDialog(widgetActivity, DateTimeUtils.getCurrentDateTime());
    }

    @Test
    public void clickingButton_callsDisplayTimePickerDialogWithSelectedTime_whenPromptHasAnswer() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate()));
        TimeWidget widget = createWidget(prompt);
        widget.binding.timeButton.performClick();

        verify(widgetUtils).showTimePickerDialog(widgetActivity, timeAnswer);
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate())));
        widget.clearAnswer();
        assertEquals(widget.binding.timeAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate())));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListener() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(onLongClickListener);
        widget.binding.timeButton.performLongClick();
        widget.binding.timeAnswerText.performLongClick();

        verify(onLongClickListener).onLongClick(widget.binding.timeButton);
        verify(onLongClickListener).onLongClick(widget.binding.timeAnswerText);
    }

    @Test
    public void setData_updatesWidgetAnswer() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setData(timeAnswer.toDateTime());
        assertEquals(widget.getAnswer().getDisplayText(), new TimeData(timeAnswer.toDateTime().toDate()).getDisplayText());
    }

    @Test
    public void setData_updatesValueDisplayedInAnswerTextView() {
        TimeWidget widget = createWidget(QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setData(timeAnswer.toDateTime());
        assertEquals(widget.binding.timeAnswerText.getText(), DateTimeUtils.getTimeData(timeAnswer.toDateTime()).getDisplayText());
    }

    private TimeWidget createWidget(FormEntryPrompt prompt) {
        return new TimeWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"), widgetUtils);
    }
}
