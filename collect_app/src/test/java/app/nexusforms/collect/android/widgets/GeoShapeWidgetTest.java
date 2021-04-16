package app.nexusforms.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.listeners.WidgetValueChangedListener;
import app.nexusforms.android.widgets.GeoShapeWidget;
import app.nexusforms.android.widgets.interfaces.GeoDataRequester;
import app.nexusforms.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.collect.android.widgets.support.GeoWidgetHelpers;
import app.nexusforms.collect.android.widgets.support.QuestionWidgetHelpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class GeoShapeWidgetTest {
    private final String answer = GeoWidgetHelpers.stringFromDoubleList();

    private GeoDataRequester geoDataRequester;
    private WaitingForDataRegistry waitingForDataRegistry;

    @Before
    public void setup() {
        geoDataRequester = mock(GeoDataRequester.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData(answer)));
        assertEquals(widget.getAnswer().getDisplayText(), answer);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), "");
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData(answer)));
        assertEquals(widget.binding.geoAnswerText.getText().toString(), answer);
    }

    @Test
    public void whenPromptIsReadOnlyAndDoesNotHaveAnswer_geoButtonIsNotDisplayed() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void whenPromptIsReadOnlyAndHasAnswer_viewGeoShapeButtonIsShown() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithReadOnlyAndAnswer(new StringData(answer)));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geoshape_view_read_only));
    }

    @Test
    public void whenPromptIsNotReadOnlyAndDoesNotHaveAnswer_startGeoShapeButtonIsShown() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_shape));
    }

    @Test
    public void whenPromptIsNotReadOnlyAndHasAnswer_viewOrChangeGeoShapeButtonIsShown() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData(answer)));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geoshape_view_change_location));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData(answer)));
        widget.clearAnswer();

        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_shape));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListeners() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));

        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void setData_updatesWidgetAnswer() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData(answer);
        assertEquals(widget.getAnswer().getDisplayText(), answer);
    }

    @Test
    public void setData_updatesWidgetDisplayedAnswer() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData(answer);
        assertEquals(widget.binding.geoAnswerText.getText().toString(), answer);
    }

    @Test
    public void setData_whenDataIsNull_updatesButtonLabel() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData(answer)));
        widget.setData("");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_shape));
    }

    @Test
    public void setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData(answer);
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.geoshape_view_change_location));
    }

    @Test
    public void setData_callsValueChangeListener() {
        GeoShapeWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);
        widget.setData(answer);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void buttonClick_requestsGeoShape() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(new StringData(answer));
        GeoShapeWidget widget = createWidget(prompt);
        widget.binding.simpleButton.performClick();
        verify(geoDataRequester).requestGeoShape(widget.getContext(), prompt, answer, waitingForDataRegistry);
    }

    @Test
    public void buttonClick_requestsGeoShape_whenAnswerIsCleared() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(new StringData(answer));
        GeoShapeWidget widget = createWidget(prompt);
        widget.clearAnswer();
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoShape(widget.getContext(), prompt, "", waitingForDataRegistry);
    }

    @Test
    public void buttonClick_requestsGeoShape_whenAnswerIsUpdated() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(null);
        GeoShapeWidget widget = createWidget(prompt);
        widget.setData(answer);
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoShape(widget.getContext(), prompt, answer, waitingForDataRegistry);
    }

    private GeoShapeWidget createWidget(FormEntryPrompt prompt) {
        return new GeoShapeWidget(QuestionWidgetHelpers.widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, geoDataRequester);
    }
}
