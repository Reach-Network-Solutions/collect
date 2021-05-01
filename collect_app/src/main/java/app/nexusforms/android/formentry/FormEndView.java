package app.nexusforms.android.formentry;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import app.nexusforms.android.R;

import app.nexusforms.android.utilities.FormNameUtils;

import timber.log.Timber;

public class FormEndView extends Dialog {

    private final Listener listener;
    private final String formTitle;
    private final String defaultInstanceName;
    public EditText saveAs;

    public FormEndView(Context context, String formTitle, String defaultInstanceName, boolean instanceComplete, Listener listener) {
        super(context);
        this.formTitle = formTitle;
        this.defaultInstanceName = defaultInstanceName;
        this.listener = listener;
        init(context, instanceComplete);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }


    private void init(Context context, boolean instanceComplete) {
        this.setContentView(R.layout.form_entry_end);

        if (instanceComplete) {
            ((TextView) findViewById(R.id.description)).setText(context.getString(R.string.save_enter_data_description, formTitle));
        } else {
            ((TextView) findViewById(R.id.description)).setText(context.getString(R.string.save_progress, formTitle));
        }

        saveAs = findViewById(R.id.save_name);

        // disallow carriage returns in the name
        InputFilter returnFilter = (source, start, end, dest, dstart, dend) -> FormNameUtils.normalizeFormName(source.toString().substring(start, end), true);
        saveAs.setFilters(new InputFilter[]{returnFilter});

        saveAs.setText(defaultInstanceName);
        saveAs.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                listener.onSaveAsChanged(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        final CheckBox markAsFinalized = findViewById(R.id.mark_finished);

        markAsFinalized.setChecked(instanceComplete);


        findViewById(R.id.save_exit_button).setOnClickListener(v -> {
            listener.onSaveClicked(markAsFinalized.isChecked());
            Timber.d("MARKED AS FINAL %s", markAsFinalized.isChecked());

        });
    }

    public interface Listener {
        void onSaveAsChanged(String string);

        void onSaveClicked(boolean markAsFinalized);
    }
}
