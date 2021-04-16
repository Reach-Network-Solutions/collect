package app.nexusforms.android.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

import app.nexusforms.android.utilities.MultiClickGuard;

public class MultiClickSafeTextInputEditText extends TextInputEditText {

    public MultiClickSafeTextInputEditText(@NonNull Context context) {
        super(context);
    }

    public MultiClickSafeTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        return MultiClickGuard.allowClick(getClass().getName()) && super.performClick();
    }
}
