package app.nexusforms.android.support.pages;

import androidx.test.espresso.matcher.CursorMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import app.nexusforms.android.provider.FormsProviderAPI;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;

public class ViewSentFormPage extends Page<ViewSentFormPage> {

    ViewSentFormPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public ViewSentFormPage assertOnPage() {
        assertToolbarTitle(R.string.view_sent_forms);
        return this;
    }

    public FormHierarchyPage clickOnForm(String formName) {
        onData(CursorMatchers.withRowString(FormsProviderAPI.FormsColumns.DISPLAY_NAME, formName)).perform(click());
        return new FormHierarchyPage(formName, rule);
    }
}
