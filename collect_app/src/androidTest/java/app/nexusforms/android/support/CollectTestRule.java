package app.nexusforms.android.support;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;

import app.nexusforms.android.activities.MainMenuActivity;
import app.nexusforms.android.support.pages.MainMenuPage;

public class CollectTestRule extends ActivityTestRule<MainMenuActivity> {

    public CollectTestRule() {
        super(MainMenuActivity.class);
    }

    public MainMenuPage mainMenu() {
        return new MainMenuPage(this).assertOnPage();
    }

    public static class StubbedIntents extends IntentsTestRule<MainMenuActivity> {

        public StubbedIntents() {
            super(MainMenuActivity.class);
        }

        public MainMenuPage mainMenu() {
            return new MainMenuPage(this).assertOnPage();
        }
    }
}
