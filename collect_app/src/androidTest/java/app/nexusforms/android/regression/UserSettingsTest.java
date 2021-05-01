package app.nexusforms.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import app.nexusforms.android.support.CollectTestRule;
import app.nexusforms.android.support.ResetStateRule;
import app.nexusforms.android.support.pages.MainMenuPage;

//Issue NODK-241
@RunWith(AndroidJUnit4.class)
public class UserSettingsTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain ruleChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(rule);

    @Test
    public void typeOption_ShouldNotBeVisible() {
        //TestCase1
        new MainMenuPage(rule)
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .openUserSettings()
                .assertTextDoesNotExist("Type")
                .assertTextDoesNotExist("Submission transport")
                .assertText(R.string.server_settings_title);
    }
}
