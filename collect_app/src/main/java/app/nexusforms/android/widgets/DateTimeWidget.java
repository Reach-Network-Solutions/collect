/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package app.nexusforms.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import app.nexusforms.android.R;
import app.nexusforms.android.databinding.DateTimeWidgetAnswerBinding;

import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.utilities.DateTimeUtils;
import app.nexusforms.android.widgets.interfaces.WidgetDataReceiver;
import app.nexusforms.android.widgets.utilities.DateTimeWidgetUtils;
import app.nexusforms.android.logic.DatePickerDetails;

/**
 * Displays a DatePicker widget. DateWidget handles leap years and does not allow dates that do not
 * exist.
 */
@SuppressLint("ViewConstructor")
public class DateTimeWidget extends QuestionWidget implements WidgetDataReceiver {
    DateTimeWidgetAnswerBinding binding;

    private final DateTimeWidgetUtils widgetUtils;

    private LocalDateTime selectedDateTime;
    private DatePickerDetails datePickerDetails;

    public DateTimeWidget(Context context, QuestionDetails prompt, DateTimeWidgetUtils widgetUtils) {
        super(context, prompt);
        this.widgetUtils = widgetUtils;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = DateTimeWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        datePickerDetails = DateTimeWidgetUtils.getDatePickerDetails(prompt.getQuestion().getAppearanceAttr());

        if (prompt.isReadOnly()) {
            binding.dateWidget.layoutDateWidget.setEnabled(false);
            binding.timeWidget.layoutTimeWidget.setEnabled(false);
        } else {
            binding.dateWidget.layoutDateWidget.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.timeWidget.layoutTimeWidget.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            binding.dateWidget.layoutDateWidget.getEditText().setOnClickListener(v -> {
                DateTimeWidgetUtils.setWidgetWaitingForData(prompt.getIndex());
                widgetUtils.showDatePickerDialog(context, datePickerDetails, selectedDateTime);
            });

            binding.dateWidget.layoutDateWidget.setEndIconOnClickListener(v -> {
                DateTimeWidgetUtils.setWidgetWaitingForData(prompt.getIndex());
                widgetUtils.showDatePickerDialog(context, datePickerDetails, selectedDateTime);
            });

            binding.timeWidget.layoutTimeWidget.getEditText().setOnClickListener(v -> {
                DateTimeWidgetUtils.setWidgetWaitingForData(prompt.getIndex());
                widgetUtils.showTimePickerDialog(context, selectedDateTime);
            });

            binding.timeWidget.layoutTimeWidget.setEndIconOnClickListener(v -> {
                DateTimeWidgetUtils.setWidgetWaitingForData(prompt.getIndex());
                widgetUtils.showTimePickerDialog(context, selectedDateTime);
            });
        }
        binding.dateWidget.textAnswerDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.timeWidget.textAnswerTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        selectedDateTime = DateTimeUtils.getCurrentDateTime();

        if (getFormEntryPrompt().getAnswerValue() != null) {
            LocalDateTime selectedDate = new LocalDateTime(getFormEntryPrompt().getAnswerValue().getValue());
            selectedDateTime = DateTimeUtils.getSelectedDate(selectedDate, selectedDateTime);
            binding.dateWidget.textAnswerDate.setText(DateTimeWidgetUtils.getDateTimeLabel(
                    selectedDate.toDate(), datePickerDetails, false, context));

            DateTime selectedTime = new DateTime(getFormEntryPrompt().getAnswerValue().getValue());
            selectedDateTime = DateTimeUtils.getSelectedTime(selectedTime.toLocalDateTime(), selectedDateTime);
            binding.timeWidget.textAnswerTime.setText(DateTimeUtils.getTimeData(selectedTime).getDisplayText());
        }

        return binding.getRoot();
    }

    @Override
    public IAnswerData getAnswer() {
        if (isNullValue()) {
            return null;
        } else {
            if (isTimeNull()) {
                selectedDateTime = DateTimeUtils.getSelectedDate(selectedDateTime, LocalDateTime.now());
            } else if (isDateNull()) {
                selectedDateTime = DateTimeUtils.getSelectedTime(selectedDateTime, LocalDateTime.now());
            }
            return new DateTimeData(selectedDateTime.toDate());
        }
    }

    @Override
    public void clearAnswer() {
        resetAnswerFields();
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.dateWidget.layoutDateWidget.setEndIconOnLongClickListener(l);
        binding.dateWidget.layoutDateWidget.getEditText().setOnLongClickListener(l);
        binding.dateWidget.textAnswerDate.setOnLongClickListener(l);


        binding.timeWidget.layoutTimeWidget.setEndIconOnLongClickListener(l);
        binding.timeWidget.layoutTimeWidget.getEditText().setOnLongClickListener(l);
        binding.timeWidget.textAnswerTime.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.dateWidget.layoutDateWidget.cancelLongPress();
        binding.dateWidget.layoutDateWidget.getEditText().cancelLongPress();
        binding.dateWidget.textAnswerDate.cancelLongPress();

        binding.timeWidget.layoutTimeWidget.cancelLongPress();
        binding.timeWidget.layoutTimeWidget.getEditText().cancelLongPress();
        binding.timeWidget.layoutTimeWidget.cancelLongPress();
    }

    @Override
    public void setData(Object answer) {
        if (answer instanceof LocalDateTime) {
            selectedDateTime = DateTimeUtils.getSelectedDate((LocalDateTime) answer, selectedDateTime);
            binding.dateWidget.textAnswerDate.setText(DateTimeWidgetUtils.getDateTimeLabel(
                    selectedDateTime.toDate(), datePickerDetails, false, getContext()));
        }
        if (answer instanceof DateTime) {
            selectedDateTime = DateTimeUtils.getSelectedTime(((DateTime) answer).toLocalDateTime(), selectedDateTime);
            binding.timeWidget.layoutTimeWidget.getEditText().setText(new TimeData(selectedDateTime.toDate()).getDisplayText());
        }
    }

    private void resetAnswerFields() {
        selectedDateTime = DateTimeUtils.getCurrentDateTime();
        binding.dateWidget.textAnswerDate.setText(R.string.no_date_selected);
        binding.timeWidget.layoutTimeWidget.getEditText().setText(R.string.no_time_selected);
    }

    private boolean isNullValue() {
        return getFormEntryPrompt().isRequired()
                ? isDateNull() || isTimeNull()
                : isDateNull() && isTimeNull();
    }

    private boolean isDateNull() {
        return binding.dateWidget.textAnswerDate.getText().equals(getContext().getString(R.string.no_date_selected));
    }

    private boolean isTimeNull() {
        return binding.timeWidget.textAnswerTime.getText().equals(getContext().getString(R.string.no_time_selected));
    }
}
