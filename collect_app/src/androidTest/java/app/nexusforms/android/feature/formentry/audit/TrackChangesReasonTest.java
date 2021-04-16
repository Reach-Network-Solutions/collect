package app.nexusforms.android.feature.formentry.audit;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import app.nexusforms.android.support.CollectTestRule;
import app.nexusforms.android.support.CopyFormRule;
import app.nexusforms.android.support.ResetStateRule;
import app.nexusforms.android.support.ScreenshotOnFailureTestRule;
import app.nexusforms.android.support.pages.ChangesReasonPromptPage;
import app.nexusforms.android.support.pages.FormEntryPage;
import app.nexusforms.android.support.pages.MainMenuPage;
import app.nexusforms.android.support.pages.SaveOrIgnoreDialog;

@RunWith(AndroidJUnit4.class)
public class TrackChangesReasonTest {

    private static final String TRACK_CHANGES_REASON_ON_EDIT_FORM = "track-changes-reason-on-edit.xml";
    private static final String NO_TRACK_CHANGES_REASON_FORM = "no-track-changes-reason.xml";

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(TRACK_CHANGES_REASON_ON_EDIT_FORM))
            .around(new CopyFormRule(NO_TRACK_CHANGES_REASON_FORM))
            .around(rule);

    @Rule
    public TestRule screenshotFailRule = new ScreenshotOnFailureTestRule();

    @Test
    public void openingAFormToEdit_andChangingAValue_andClickingSaveAndExit_andEnteringReason_andClickingSave_returnsToMainMenu() {
        new MainMenuPage(rule)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .swipeToEndScreen()
                .clickSaveAndExitWithChangesReasonPrompt()
                .enterReason("Needed to be more exciting and less mysterious")
                .clickSave();
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andClickingSaveAndExit_andPressingBack_returnsToForm() {
        new MainMenuPage(rule)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .swipeToEndScreen()
                .clickSaveAndExitWithChangesReasonPrompt()
                .closeSoftKeyboard()
                .pressBack(new FormEntryPage("Track Changes Reason", rule))
                .assertText(R.string.save_form_as);
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andClickingSaveAndExit_andClickingCross_returnsToForm() {
        new MainMenuPage(rule)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .swipeToEndScreen()
                .clickSaveAndExitWithChangesReasonPrompt()
                .closeSoftKeyboard()
                .pressClose(new FormEntryPage("Track Changes Reason", rule))
                .assertText(R.string.save_form_as);
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andClickingSaveAndExit_andRotating_remainsOnPrompt() {
        new MainMenuPage(rule)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .swipeToEndScreen()
                .clickSaveAndExitWithChangesReasonPrompt()
                .enterReason("Something")
                .rotateToLandscape(new ChangesReasonPromptPage("Track Changes Reason", rule))
                .assertText("Something")
                .closeSoftKeyboard()
                .clickSave();
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andPressingBack_andClickingSaveChanges_promptsForReason() {
        new MainMenuPage(rule)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("Track Changes Reason", new ChangesReasonPromptPage("Track Changes Reason", rule), rule))
                .clickSaveChanges();
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andPressingBack_andIgnoringChanges_returnsToMainMenu() {
        new MainMenuPage(rule)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("Track Changes Reason", new MainMenuPage(rule), rule))
                .clickIgnoreChanges();
    }

    @Test
    public void openingAFormToEdit_andNotChangingAValue_andClickingSaveAndExit_returnsToMainMenu() {
        new MainMenuPage(rule)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void openingFormToEdit_andChangingValue_andClickingSave_promptsForReason() {
        new MainMenuPage(rule)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .clickSaveWithChangesReasonPrompt()
                .enterReason("Bah")
                .clickSave(new FormEntryPage("Track Changes Reason", rule))
                .assertQuestion("What up?");
    }
}
