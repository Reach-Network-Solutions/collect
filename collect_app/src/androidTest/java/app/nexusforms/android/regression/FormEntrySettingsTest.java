package app.nexusforms.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;

import app.nexusforms.android.support.CollectTestRule;
import app.nexusforms.android.support.CopyFormRule;
import app.nexusforms.android.support.ResetStateRule;
import app.nexusforms.android.support.pages.MainMenuPage;
import app.nexusforms.android.support.pages.AdminSettingsPage;
import app.nexusforms.android.support.pages.ExitFormDialog;
import app.nexusforms.android.support.pages.GeneralSettingsPage;

//Issue NODK-243
public class FormEntrySettingsTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"))
            .around(rule);

    @SuppressWarnings("PMD.AvoidCallingFinalize")
    @Test
    public void movingBackwards_shouldBeTurnedOn() {
        new MainMenuPage(rule)
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .openFormManagement()
                .openConstraintProcessing()
                .clickOnString(R.string.constraint_behavior_on_finalize)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickFormEntrySettings()
                .clickMovingBackwards()
                .assertText(R.string.moving_backwards_disabled_title)
                .assertText(R.string.yes)
                .assertText(R.string.no)
                .clickOnString(R.string.yes)
                .checkIfSaveFormOptionIsDisabled()
                .pressBack(new AdminSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .openFormManagement()
                .scrollToConstraintProcessing()
                .checkIfConstraintProcessingIsDisabled()
                .assertTextDoesNotExist(R.string.constraint_behavior_on_finalize)
                .assertText(R.string.constraint_behavior_on_swipe)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .checkIfElementIsGone(R.id.review_data)
                .startBlankForm("All widgets")
                .swipeToNextQuestion()
                .closeSoftKeyboard()
                .swipeToPreviousQuestion("String widget")
                .pressBack(new ExitFormDialog("All widgets", rule))
                .assertText(R.string.do_not_save)
                .assertTextDoesNotExist(R.string.keep_changes)
                .clickOnString(R.string.do_not_save);
    }
}
