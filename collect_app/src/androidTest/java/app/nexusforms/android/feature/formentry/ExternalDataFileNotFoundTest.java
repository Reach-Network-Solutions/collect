package app.nexusforms.android.feature.formentry;

import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import app.nexusforms.android.activities.FormEntryActivity;
import app.nexusforms.android.storage.StoragePathProvider;
import app.nexusforms.android.storage.StorageSubdirectory;
import app.nexusforms.android.support.CopyFormRule;
import app.nexusforms.android.support.FormLoadingUtils;
import app.nexusforms.android.support.ResetStateRule;
import app.nexusforms.android.support.pages.FormEntryPage;

public class ExternalDataFileNotFoundTest {
    private static final String EXTERNAL_DATA_QUESTIONS = "external_data_questions.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(EXTERNAL_DATA_QUESTIONS);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(EXTERNAL_DATA_QUESTIONS, true));

    @Test
    public void questionsThatUseExternalFiles_ShouldDisplayFriendlyMessageWhenFilesAreMissing() {
        String formsDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS);

        new FormEntryPage("externalDataQuestions", activityTestRule)
                .assertText(activityTestRule.getActivity().getString(R.string.file_missing, formsDirPath + "/external_data_questions-media/fruits.csv"))
                .swipeToNextQuestion()
                .assertText(activityTestRule.getActivity().getString(R.string.file_missing, formsDirPath + "/external_data_questions-media/itemsets.csv"));
    }
}
