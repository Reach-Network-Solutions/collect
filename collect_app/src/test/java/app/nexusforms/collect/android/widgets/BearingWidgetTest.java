package app.nexusforms.collect.android.widgets;

import android.content.ComponentName;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import app.nexusforms.android.activities.BearingActivity;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.listeners.WidgetValueChangedListener;
import app.nexusforms.android.widgets.BearingWidget;
import app.nexusforms.collect.android.support.TestScreenContextActivity;
import app.nexusforms.android.utilities.ApplicationConstants;
import app.nexusforms.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import app.nexusforms.collect.android.widgets.support.QuestionWidgetHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */
@RunWith(RobolectricTestRunner.class)
public class BearingWidgetTest {
    private final FakeWaitingForDataRegistry fakeWaitingForDataRegistry = new FakeWaitingForDataRegistry();

    private TestScreenContextActivity widgetActivity;
    private ShadowActivity shadowActivity;
    private SensorManager sensorManager;
    private View.OnLongClickListener listener;

    @Before
    public void setUp() {
        widgetActivity = QuestionWidgetHelpers.widgetTestActivity();
        shadowActivity = shadowOf(widgetActivity);

        sensorManager = mock(SensorManager.class);
        listener = mock(View.OnLongClickListener.class);

        Sensor sensor = mock(Sensor.class);
        when(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)).thenReturn(sensor);
        when(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)).thenReturn(sensor);
    }

    @Test
    public void usingReadOnlyOption_hidesBearingButton() {
        assertThat(createWidget(QuestionWidgetHelpers.promptWithReadOnly()).binding.bearingButton.getVisibility(), is(View.GONE));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_getBearingButtonIsShown() {
        assertThat(createWidget(QuestionWidgetHelpers.promptWithAnswer(null)).binding.bearingButton.getText(),
                is(widgetActivity.getString(R.string.get_bearing)));
    }

    @Test
    public void whenPromptHasAnswer_replaceBearingButtonIsShown() {
        assertThat(createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData("blah"))).binding.bearingButton.getText(),
                is(widgetActivity.getString(R.string.replace_bearing)));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectAnswer() {
        assertThat(createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData("blah"))).binding.answerText.getText().toString(), is("blah"));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(QuestionWidgetHelpers.promptWithAnswer(null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        assertThat(createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData("blah"))).getAnswer().getDisplayText(), is("blah"));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData("blah")));
        widget.clearAnswer();
        assertThat(widget.binding.answerText.getText().toString(), is(""));
    }

    @Test
    public void clearAnswer_updatesButtonLabel() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData("blah")));
        widget.clearAnswer();
        assertThat(widget.binding.bearingButton.getText(), is(widgetActivity.getString(R.string.get_bearing)));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(new StringData("blah")));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_updatesWidgetAnswer() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData("blah");
        assertThat(widget.binding.answerText.getText().toString(), is("blah"));
    }

    @Test
    public void setData_updatesButtonLabel() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setData("blah");
        assertThat(widget.binding.bearingButton.getText(), is(widgetActivity.getString(R.string.replace_bearing)));
    }

    @Test
    public void setData_callsValueChangeListeners() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget);

        widget.setData("blah");
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingAnswerTextForLong_callsOnLongClickListener() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setOnLongClickListener(listener);

        widget.binding.answerText.performLongClick();
        verify(listener).onLongClick(widget.binding.answerText);
    }

    @Test
    public void clickingBearingButtonForLong_callOnLongClickListener() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setOnLongClickListener(listener);

        widget.binding.bearingButton.performLongClick();
        verify(listener).onLongClick(widget.binding.bearingButton);
    }

    @Test
    public void clickingBearingButtonForLong_whenSensorIsAvailable_callsOnLongClickListener() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.setOnLongClickListener(listener);

        widget.binding.bearingButton.performLongClick();
        verify(listener).onLongClick(widget.binding.bearingButton);
    }

    @Test
    public void clickingBearingButton_whenAccelerometerSensorIsNotAvailable_doesNotLaunchAnyIntent() {
        assertNoIntentLaunchedWhenSensorIsUnavailable(Sensor.TYPE_ACCELEROMETER);
    }

    @Test
    public void clickingBearingButton_whenAccelerometerSensorIsNotAvailable_disablesBearingButton() {
        assertBearingButtonIsDisabledWhenSensorIsUnavailable(Sensor.TYPE_ACCELEROMETER);
    }

    @Test
    public void clickingBearingButton_whenAccelerometerSensorIsNotAvailable_makesEditTextEditable() {
        assertAnswerTextIsEditableWhenSensorIsUnavailable(Sensor.TYPE_ACCELEROMETER);
    }

    @Test
    public void clickingBearingButton_whenMagneticSensorIsNotAvailable_doesNotLaunchAnyIntent() {
        assertNoIntentLaunchedWhenSensorIsUnavailable(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Test
    public void clickingBearingButton_whenMagneticSensorIsNotAvailable_disablesBearingButton() {
        assertBearingButtonIsDisabledWhenSensorIsUnavailable(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Test
    public void clickingBearingButton_whenMagneticSensorIsNotAvailable_makesEditTextEditable() {
        assertAnswerTextIsEditableWhenSensorIsUnavailable(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Test
    public void clickingBearingButton_whenSensorIsAvailable_setsWidgetWaitingForData() {
        FormEntryPrompt prompt = QuestionWidgetHelpers.promptWithAnswer(null);
        FormIndex formIndex = mock(FormIndex.class);
        when(prompt.getIndex()).thenReturn(formIndex);

        BearingWidget widget = createWidget(prompt);
        widget.binding.bearingButton.performClick();
        assertThat(fakeWaitingForDataRegistry.waiting.contains(formIndex), is(true));
    }

    @Test
    public void clickingBearingButton_whenSensorIsAvailable_launchesCorrectIntent() {
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.binding.bearingButton.performClick();

        assertThat(shadowActivity.getNextStartedActivity().getComponent(), is(new ComponentName(widgetActivity, BearingActivity.class)));
        assertThat(shadowActivity.getNextStartedActivityForResult().requestCode, is(ApplicationConstants.RequestCodes.BEARING_CAPTURE));
    }

    private BearingWidget createWidget(FormEntryPrompt prompt) {
        return new BearingWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"), fakeWaitingForDataRegistry, sensorManager);
    }

    private void assertNoIntentLaunchedWhenSensorIsUnavailable(int sensorType) {
        when(sensorManager.getDefaultSensor(sensorType)).thenReturn(null);
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.binding.bearingButton.performClick();

        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
        assertThat(ShadowToast.getTextOfLatestToast(), is(widgetActivity.getString(R.string.bearing_lack_of_sensors)));
    }

    private void assertAnswerTextIsEditableWhenSensorIsUnavailable(int sensorType) {
        when(sensorManager.getDefaultSensor(sensorType)).thenReturn(null);
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.binding.bearingButton.performClick();

        assertThat(widget.binding.answerText.didTouchFocusSelect(), is(true));
        assertThat(widget.binding.answerText.hasFocusable(), is(true));
    }

    private void assertBearingButtonIsDisabledWhenSensorIsUnavailable(int sensorType) {
        when(sensorManager.getDefaultSensor(sensorType)).thenReturn(null);
        BearingWidget widget = createWidget(QuestionWidgetHelpers.promptWithAnswer(null));
        widget.binding.bearingButton.performClick();

        assertThat(widget.binding.bearingButton.isEnabled(), is(false));
    }
}
