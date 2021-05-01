package app.nexusforms.android.feature.formentry;

import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import app.nexusforms.android.activities.FormEntryActivity;
import app.nexusforms.android.support.CopyFormRule;
import app.nexusforms.android.support.FormLoadingUtils;
import app.nexusforms.android.support.ResetStateRule;
import app.nexusforms.android.support.pages.FormEntryPage;

import java.util.Collections;

public class RankingWidgetWithCSVTest {

    private static final String TEST_FORM = "ranking_widget.xml";

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(TEST_FORM, Collections.singletonList("fruits.csv")));

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(TEST_FORM);

    @Test
    public void rankingWidget_shouldDisplayItemsFromSearchFunc() {
        new FormEntryPage("ranking_widget", activityTestRule)
                .clickRankingButton()
                .assertText("Mango", "Oranges", "Strawberries");
    }
}
