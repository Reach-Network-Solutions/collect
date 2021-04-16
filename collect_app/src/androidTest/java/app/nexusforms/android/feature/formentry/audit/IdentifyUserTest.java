package app.nexusforms.android.feature.formentry.audit;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import app.nexusforms.android.activities.MainMenuActivity;
import app.nexusforms.android.support.CopyFormRule;
import app.nexusforms.android.support.ResetStateRule;
import app.nexusforms.android.support.pages.MainMenuPage;
import app.nexusforms.android.support.pages.IdentifyUserPromptPage;

@RunWith(AndroidJUnit4.class)
public class IdentifyUserTest {

    private static final String IDENTIFY_USER_AUDIT_FORM = "identify-user-audit.xml";
    private static final String IDENTIFY_USER_AUDIT_FALSE_FORM = "identify-user-audit-false.xml";

    @Rule
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(IDENTIFY_USER_AUDIT_FORM))
            .around(new CopyFormRule(IDENTIFY_USER_AUDIT_FALSE_FORM));

    @Test
    public void openingForm_andThenEnteringIdentity_proceedsToForm() {
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Lucius")
                .clickKeyboardEnter()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void openingSavedForm_promptsForIdentity() {
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Lucius")
                .clickKeyboardEnter()
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnFormWithIdentityPrompt("Identify User");
    }

    @Test
    public void openingForm_andEnteringBlankIdentity_remainsOnIdentityPrompt() {
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("  ")
                .clickKeyboardEnterWithValidationError();
    }

    @Test
    public void openingForm_andPressingBack_returnsToMainMenu() {
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .closeSoftKeyboard()
                .pressBack(new MainMenuPage(rule));
    }

    @Test
    public void openingForm_andRotating_remainsOnIdentityPrompt() {
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Blah")
                .rotateToLandscape(new IdentifyUserPromptPage("Identify User", rule))
                .assertText("Blah");
    }

    @Test
    public void openingForm_andPressingCloseCross_returnsToMainMenu() {
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .pressClose();
    }

    @Test
    public void openFormWithIdentifyUserFalse_proceedsToForm() {
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnForm("Identify User False")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }
}
