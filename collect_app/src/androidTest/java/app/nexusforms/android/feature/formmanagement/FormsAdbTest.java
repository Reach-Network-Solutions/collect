package app.nexusforms.android.feature.formmanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import app.nexusforms.android.storage.StorageSubdirectory;
import app.nexusforms.android.support.CollectTestRule;
import app.nexusforms.android.support.FormLoadingUtils;
import app.nexusforms.android.support.TestDependencies;
import app.nexusforms.android.support.TestRuleChain;
import app.nexusforms.android.support.pages.MainMenuPage;

import java.io.File;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FormsAdbTest {

    public final TestDependencies testDependencies = new TestDependencies();
    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(rule);

    @Test
    public void canUpdateFormOnDisk() throws Exception {
        MainMenuPage mainMenuPage = rule.mainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm()
                .assertFormExists("One Question")
                .pressBack(new MainMenuPage(rule));

        FormLoadingUtils.copyFormToStorage("one-question-updated.xml", "one-question.xml");

        mainMenuPage
                .clickFillBlankForm()
                .assertFormExists("One Question Updated")
                .assertFormDoesNotExist("One Question");
    }

    @Test
    public void canDeleteFormFromDisk() {
        MainMenuPage mainMenuPage = rule.mainMenu()
                .copyForm("one-question.xml")
                .clickFillBlankForm()
                .assertFormExists("One Question")
                .pressBack(new MainMenuPage(rule));

        String formsDir = testDependencies.storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS);
        boolean formDeleted = new File(formsDir, "one-question.xml").delete();
        assertTrue(formDeleted);

        mainMenuPage
                .clickFillBlankForm()
                .assertNoForms();
    }
}
