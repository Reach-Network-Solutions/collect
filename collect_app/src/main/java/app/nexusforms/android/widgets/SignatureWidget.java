/*
 * Copyright (C) 2012 University of Washington
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
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import app.nexusforms.android.R;

import app.nexusforms.android.activities.DrawActivity;
import app.nexusforms.android.formentry.questions.QuestionDetails;
import app.nexusforms.android.formentry.questions.WidgetViewUtils;
import app.nexusforms.android.utilities.ApplicationConstants;
import app.nexusforms.android.utilities.MediaUtils;
import app.nexusforms.android.utilities.QuestionMediaManager;
import app.nexusforms.android.widgets.interfaces.ButtonClickListener;
import app.nexusforms.android.widgets.utilities.WaitingForDataRegistry;

import static app.nexusforms.android.formentry.questions.WidgetViewUtils.createSimpleButton;

/**
 * Signature widget.
 *
 * @author BehrAtherton@gmail.com
 */
@SuppressLint("ViewConstructor")
public class SignatureWidget extends BaseImageWidget implements ButtonClickListener {

    //Button signButton;

    public SignatureWidget(Context context, QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, prompt, questionMediaManager, waitingForDataRegistry, new MediaUtils());
        imageClickHandler = new DrawImageClickHandler(DrawActivity.OPTION_SIGNATURE, ApplicationConstants.RequestCodes.SIGNATURE_CAPTURE, R.string.signature_capture);
        setUpLayout();
        addCurrentImageToLayout();
        addAnswerView(answerLayout, WidgetViewUtils.getStandardMargin(context));
    }

    @Override
    protected void setUpLayout() {
        super.setUpLayout();
        //signButton = createSimpleButton(getContext(), questionDetails.isReadOnly(), getContext().getString(R.string.sign_button), getAnswerFontSize(), this);

        //answerLayout.addView(signButton);
        answerLayout.addView(errorTextView);

        errorTextView.setVisibility(GONE);
    }

    @Override
    public Intent addExtrasToIntent(Intent intent) {
        return intent;
    }

    @Override
    protected boolean doesSupportDefaultValues() {
        return true;
    }

    @Override
    public void clearAnswer() {
        super.clearAnswer();
        // reset buttons
        //signButton.setText(getContext().getString(R.string.sign_button));
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        //signButton.setOnLongClickListener(l);
        super.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        //signButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        imageClickHandler.clickImage("signButton");
    }
}
