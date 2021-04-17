package app.nexusforms.android.widgets;

import android.view.View;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import app.nexusforms.android.R;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.listeners.WidgetValueChangedListener;
import app.nexusforms.android.widgets.interfaces.GeoDataRequester;
import app.nexusforms.android.widgets.support.GeoWidgetHelpers;
import app.nexusforms.android.widgets.support.QuestionWidgetHelpers;
import app.nexusforms.android.widgets.utilities.GeoWidgetUtils;
import app.nexusforms.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class GeoPointWidgetTest {
    private final GeoPointData answer = new GeoPointData(GeoWidgetHelpers.getRandomDoubleArray());

    private GeoDataRequester geoDataRequester;
    private WaitingForDataRegistry waitingForDataRegistry;

    @Before
    public void setup() {
        geoDataRequester = mock(GeoDataRequester.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowTheGeoButton() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        assertEquals(widget.getAnswer().getDisplayText(), answer.getDisplayText());
    }

    @Test
    public void answerTextViewShouldShowCorrectAnswer() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
    }

    @Test
    public void whenPromptDoesNotHaveHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void whenPromptHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.change_location));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        widget.clearAnswer();

        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListeners() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));

        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void setData_updatesWidgetAnswer() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.getAnswer().getDisplayText(), answer.getDisplayText());
    }

    @Test
    public void setData_updatesWidgetDisplayedAnswer() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
    }

    @Test
    public void setData_whenDataIsNull_updatesButtonLabel() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        widget.setData("");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.change_location));
    }

    @Test
    public void setData_callsValueChangeListener() {
        GeoPointWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);
        widget.setData(answer.getDisplayText());
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void buttonClick_requestsGeoPoint() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(answer);
        GeoPointWidget widget = createWidget(prompt);
        widget.binding.simpleButton.performClick();
        verify(geoDataRequester).requestGeoPoint(widget.getContext(), prompt, answer.getDisplayText(), waitingForDataRegistry);
    }

    @Test
    public void buttonClick_requestsGeoPoint_whenAnswerIsCleared() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(answer);
        GeoPointWidget widget = createWidget(prompt);
        widget.clearAnswer();
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoPoint(widget.getContext(), prompt, null, waitingForDataRegistry);
    }

    @Test
    public void buttonClick_requestsGeoPoint_whenAnswerIsUpdated() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(null);
        GeoPointWidget widget = createWidget(prompt);
        widget.setData(answer);
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoPoint(widget.getContext(), prompt, answer.getDisplayText(), waitingForDataRegistry);
    }

    private GeoPointWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointWidget(QuestionWidgetHelpers.widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, geoDataRequester);
    }
}
