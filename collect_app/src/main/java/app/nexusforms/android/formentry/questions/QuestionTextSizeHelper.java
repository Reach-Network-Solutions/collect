package app.nexusforms.android.formentry.questions;

import app.nexusforms.android.preferences.source.Settings;

import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.Settings;

public class QuestionTextSizeHelper {

    private final Settings generalSettings;

    public QuestionTextSizeHelper(Settings generalSettings) {
        this.generalSettings = generalSettings;
    }

    public float getHeadline6() {
        return getBaseFontSize() - 1; // 20sp by default
    }

    public float getSubtitle1() {
        return getBaseFontSize() - 5; // 16sp by default
    }

    private int getBaseFontSize() {
        return Integer.parseInt(String.valueOf(generalSettings.getString(GeneralKeys.KEY_FONT_SIZE)));
    }
}
