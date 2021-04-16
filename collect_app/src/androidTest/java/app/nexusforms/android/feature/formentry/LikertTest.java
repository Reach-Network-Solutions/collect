package app.nexusforms.android.feature.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import app.nexusforms.android.activities.FormEntryActivity;
import app.nexusforms.android.support.CopyFormRule;
import app.nexusforms.android.support.CustomMatchers;
import app.nexusforms.android.support.FormLoadingUtils;
import app.nexusforms.android.support.ResetStateRule;

import java.util.Collections;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;

public class LikertTest {
    private static final String LIKERT_TEST_FORM = "likert_test.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(LIKERT_TEST_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.CAMERA))
            .around(new ResetStateRule())
            .around(new CopyFormRule(LIKERT_TEST_FORM, Collections.singletonList("famous.jpg"), true));

    @Test
    public void allText_canClick() {
        openWidgetList();
        onView(withText("Likert Widget")).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void allImages_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void insufficientText_canClick() {
        openWidgetList();
        onView(withText("Likert Widget Error")).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void insufficientImages_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget Error")).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void missingImage_canClick() {
        openWidgetList();
        onView(withText("Likert Image Widget Error2")).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void missingText_canClick() {
        openWidgetList();
        onView(withText("Likert Missing text Error")).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
    }

    @Test
    public void onlyOneRemainsClicked() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isChecked()));
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 2)).perform(click());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 2)).check(matches(isChecked()));
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isNotChecked()));
    }

    @Test
    public void testImagesLoad() {
        openWidgetList();
        onView(withText("Likert Image Widget")).perform(click());

        for (int i = 0; i < 5; i++) {
            onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), i)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void updateTest_SelectionChangeAtOneCascadeLevelWithLikert_ShouldUpdateNextLevels() {
        openWidgetList();
        onView(withText("Cascading likert")).perform(click());

        // No choices should be shown for levels 2 and 3 when no selection is made for level 1
        onView(withText(startsWith("Level1"))).perform(click());
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
        onView(withText("A1A")).check(doesNotExist());

        // Selecting C for level 1 should only reveal options for C at level 2
        // and selecting C3 for level 2 shouldn't reveal options in level 3
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 2)).perform(click());
        onView(withText("C1")).check(matches(isDisplayed()));
        onView(withText("C4")).check(matches(isDisplayed()));
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 5)).perform(click());
        onView(withText("A1A")).check(doesNotExist());

        // Selecting A for level 1 should reveal options for A at level 2
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 0)).perform(click());
        onView(withText("A1")).check(matches(isDisplayed()));
        onView(withText("A1A")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());

        // Selecting A1 for level 2 should reveal options for A1 at level 3
        onView(CustomMatchers.withIndex(withClassName(endsWith("RadioButton")), 3)).perform(click());
        onView(withText("A1A")).check(matches(isDisplayed()));
        onView(withText("B1A")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
    }

    private void openWidgetList() {
        onView(withId(R.id.menu_goto)).perform(click());
    }
}
