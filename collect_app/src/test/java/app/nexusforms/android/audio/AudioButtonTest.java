package app.nexusforms.android.audio;

import android.app.Activity;

import androidx.fragment.app.FragmentActivity;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.android.R;
import app.nexusforms.android.support.RobolectricHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(RobolectricTestRunner.class)
public class AudioButtonTest {

    private AudioButton button;

    @Before
    public void setup() {
        Activity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class, com.google.android.material.R.style.Theme_MaterialComponents);
        button = new AudioButton(activity);
    }

    @Test
    public void isPlayingReturnsFalse_andShowsPlayIcon() {
        assertThat(button.isPlaying(), equalTo(false));
        MatcherAssert.assertThat(RobolectricHelpers.getCreatedFromResId(button.getIcon()), equalTo(R.drawable.ic_volume_up_black_24dp));
    }

    @Test
    public void whenPlayingIsTrue_showsPlayingIcon() {
        button.setPlaying(true);
        MatcherAssert.assertThat(RobolectricHelpers.getCreatedFromResId(button.getIcon()), equalTo(R.drawable.ic_stop_black_24dp));
    }

    @Test
    public void whenPlayingIsFalse_showsPlayIcon() {
        button.setPlaying(false);
        MatcherAssert.assertThat(RobolectricHelpers.getCreatedFromResId(button.getIcon()), equalTo(R.drawable.ic_volume_up_black_24dp));
    }
}