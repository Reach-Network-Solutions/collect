package app.nexusforms.android.widgets.items;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.javarosa.form.api.FormEntryPrompt;

import app.nexusforms.android.R;
import app.nexusforms.android.databinding.SelectMinimalWidgetAnswerBinding;

import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.utilities.QuestionFontSizeUtils;
import app.nexusforms.android.widgets.interfaces.MultiChoiceWidget;
import app.nexusforms.android.widgets.interfaces.WidgetDataReceiver;
import app.nexusforms.android.widgets.utilities.WaitingForDataRegistry;

public abstract class SelectMinimalWidget extends ItemsWidget implements WidgetDataReceiver, MultiChoiceWidget {
    SelectMinimalWidgetAnswerBinding binding;
    private final WaitingForDataRegistry waitingForDataRegistry;

    public SelectMinimalWidget(Context context, QuestionDetails prompt, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, prompt);
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = SelectMinimalWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        //binding.textAnswerMinimal.setTextSize(QuestionFontSizeUtils.getQuestionFontSize());
        if (prompt.isReadOnly()) {
            binding.textAnswerMinimal.setEnabled(false);
        } else {
            binding.textAnswerMinimal.setOnClickListener(v -> {
                waitingForDataRegistry.waitForData(prompt.getIndex());
                showDialog();
            });
            binding.layoutMinimal.setEndIconOnClickListener(v -> {
                waitingForDataRegistry.waitForData(prompt.getIndex());
                showDialog();
            });
        }
        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        // binding.textAnswerMinimal.setText(R.string.select_answer);
        binding.textAnswerMinimal.setText("");
        widgetValueChanged();
    }

    @Override
    public int getChoiceCount() {
        return items.size();
    }

    protected abstract void showDialog();
}
