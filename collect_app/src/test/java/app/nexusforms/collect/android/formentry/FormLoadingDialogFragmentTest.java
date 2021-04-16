package app.nexusforms.collect.android.formentry;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import app.nexusforms.android.formentry.FormLoadingDialogFragment;
import app.nexusforms.collect.android.support.RobolectricHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class FormLoadingDialogFragmentTest {

    @Test
    public void dialogIsNotCancellable() {
        FragmentScenario<FormLoadingDialogFragment> fragmentScenario = RobolectricHelpers.launchDialogFragment(FormLoadingDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }
}
