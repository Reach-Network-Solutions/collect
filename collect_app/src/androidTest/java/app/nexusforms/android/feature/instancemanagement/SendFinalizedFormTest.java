package app.nexusforms.android.feature.instancemanagement;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import app.nexusforms.android.support.CollectTestRule;
import app.nexusforms.android.support.TestDependencies;
import app.nexusforms.android.support.TestRuleChain;
import app.nexusforms.android.support.pages.MainMenuPage;
import app.nexusforms.android.support.pages.GeneralSettingsPage;
import app.nexusforms.android.support.pages.SendFinalizedFormPage;

@RunWith(AndroidJUnit4.class)
public class SendFinalizedFormTest {

    private final TestDependencies testDependencies = new TestDependencies();
    public final CollectTestRule.StubbedIntents rule = new CollectTestRule.StubbedIntents();

    @Rule
    public RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.GET_ACCOUNTS))
            .around(rule);

    @Test
    public void canViewSentForms() {
        rule.mainMenu()
                .setServer(testDependencies.server.getURL())
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "123")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm("One Question")
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage(rule))
                .pressBack(new MainMenuPage(rule))

                .clickViewSentForm(1)
                .clickOnForm("One Question")
                .assertText("123");
    }

    @Test
    public void whenDeleteAfterSendIsEnabled_deletesFilledForm() {
        rule.mainMenu()
                .setServer(testDependencies.server.getURL())

                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickFormManagement()
                .scrollToRecyclerViewItemAndClickText(R.string.delete_after_send)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "123")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm("One Question")
                .clickSendSelected()
                .clickOK(new SendFinalizedFormPage(rule))
                .pressBack(new MainMenuPage(rule))

                .clickViewSentForm(1)
                .clickOnText("One Question")
                .assertOnPage();
    }

    @Test
    public void whenGoogleUsedAsServer_sendsSubmissionToSheet() {
        testDependencies.googleAccountPicker.setDeviceAccount("dani@davey.com");
        testDependencies.googleApi.setAccount("dani@davey.com");

        rule.mainMenu()
                .setGoogleAccount("dani@davey.com")
                .copyForm("one-question-google.xml")
                .startBlankForm("One Question Google")
                .answerQuestion("what is your age", "47")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm("One Question Google")
                .clickSendSelected()
                .assertText("One Question Google - Success");
    }
}
