package app.nexusforms.android.utilities;

import app.nexusforms.android.application.Collect;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.SettingsProvider;

import app.nexusforms.android.preferences.source.SettingsProvider;

public class QuestionFontSizeUtils {
    public static final int DEFAULT_FONT_SIZE = 21;

    private QuestionFontSizeUtils() {

    }

    public static int getQuestionFontSize() {
        try {
            return Integer.parseInt(new SettingsProvider(Collect.getInstance()).getGeneralSettings().getString(GeneralKeys.KEY_FONT_SIZE));
        } catch (Exception | Error e) {
            return DEFAULT_FONT_SIZE;
        }
    }
}
