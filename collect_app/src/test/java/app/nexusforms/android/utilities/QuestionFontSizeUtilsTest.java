package app.nexusforms.android.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;

import app.nexusforms.android.TestSettingsProvider;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.android.utilities.QuestionFontSizeUtils;
import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.keys.GeneralKeys;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(RobolectricTestRunner.class)
public class QuestionFontSizeUtilsTest {

    @Test
    public void whenFontSizeNotSpecified_shouldReturnDefaultValue() {
        assertThat(QuestionFontSizeUtils.getQuestionFontSize(), is(QuestionFontSizeUtils.DEFAULT_FONT_SIZE));
    }

    @Test
    public void whenFontSizeSpecified_shouldReturnSelectedValue() {
        TestSettingsProvider.getGeneralSettings().save(GeneralKeys.KEY_FONT_SIZE, "30");
        assertThat(QuestionFontSizeUtils.getQuestionFontSize(), is(30));
    }
}
