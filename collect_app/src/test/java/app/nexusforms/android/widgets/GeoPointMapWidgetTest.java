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
public class GeoPointMapWidgetTest {
    private final GeoPointData answer = new GeoPointData(GeoWidgetHelpers.getRandomDoubleArray());

    private GeoDataRequester geoDataRequester;
    private WaitingForDataRegistry waitingForDataRegistry;

    @Before
    public void setup() {
        geoDataRequester = mock(GeoDataRequester.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsPromptAnswer() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        assertEquals(widget.getAnswer().getDisplayText(),
                new GeoPointData(GeoWidgetUtils.getLocationParamsFromStringAnswer(answer.getDisplayText())).getDisplayText());
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectAnswer() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
    }

    @Test
    public void whenPromptIsReadOnlyAndDoesNotHaveAnswer_geoButtonIsNotDisplayed() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void whenPromptIsReadOnlyAndHasAnswer_viewGeoPointButtonIsShown() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithReadOnlyAndAnswer(answer));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geopoint_view_read_only));
    }

    @Test
    public void whenPromptIsNotReadOnlyAndDoesNotHaveAnswer_startGeoPointButtonIsShown() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void whenPromptIsNotReadOnlyAndHasAnswer_viewOrChangeLocationButtonIsShown() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.view_change_location));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        widget.clearAnswer();

        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_updatesWidgetAnswer() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.getAnswer().getDisplayText(), answer.getDisplayText());
    }


    @Test
    public void setData_updatesWidgetDisplayedAnswer() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
    }

    @Test
    public void setData_whenDataIsNull_updatesButtonLabel() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(answer));
        widget.setData("");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData(answer.getDisplayText());
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.view_change_location));
    }

    @Test
    public void setData_callsValueChangeListener() {
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);
        widget.setData(answer.getDisplayText());

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoPointMapWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));

        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void buttonClick_requestsGeoPoint() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(answer);
        GeoPointMapWidget widget = createWidget(prompt);
        widget.binding.simpleButton.performClick();
        verify(geoDataRequester).requestGeoPoint(widget.getContext(), prompt, answer.getDisplayText(), waitingForDataRegistry);
    }

    @Test
    public void buttonClick_requestsGeoPoint_whenAnswerIsCleared() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(answer);
        GeoPointMapWidget widget = createWidget(prompt);
        widget.clearAnswer();
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoPoint(widget.getContext(), prompt, null, waitingForDataRegistry);
    }

    @Test
    public void buttonClick_requestsGeoPoint_whenAnswerIsUpdated() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(null);
        GeoPointMapWidget widget = createWidget(prompt);
        widget.setData(answer);
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoPoint(widget.getContext(), prompt, answer.getDisplayText(), waitingForDataRegistry);
    }

    private GeoPointMapWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointMapWidget(QuestionWidgetHelpers.widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, geoDataRequester);
    }
}