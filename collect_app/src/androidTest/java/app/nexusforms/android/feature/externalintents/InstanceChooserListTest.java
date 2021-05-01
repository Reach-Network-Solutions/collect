package app.nexusforms.android.feature.externalintents;

import androidx.test.filters.Suppress;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import app.nexusforms.android.activities.InstanceChooserList;

import java.io.IOException;

import static app.nexusforms.android.feature.externalintents.ExportedActivitiesUtils.testDirectories;

@Suppress
// Frequent failures: https://github.com/getodk/collect/issues/796
@RunWith(AndroidJUnit4.class)
public class InstanceChooserListTest {

    @Rule
    public ActivityTestRule<InstanceChooserList> instanceChooserListRule =
            new ExportedActivityTestRule<>(InstanceChooserList.class);

    @Test
    public void instanceChooserListMakesDirsTest() throws IOException {
        testDirectories();
    }

}
