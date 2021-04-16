package app.nexusforms.android.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;

import app.nexusforms.android.utilities.MultiClickGuard;

public class MultiClickSafeImageButton extends AppCompatImageButton {
    public MultiClickSafeImageButton(@NonNull Context context) {
        super(context);
    }

    public MultiClickSafeImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        return MultiClickGuard.allowClick(getClass().getName()) && super.performClick();
    }
}
