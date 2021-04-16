package app.nexusforms.collect.android.support;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import app.nexusforms.android.utilities.ScreenContext;
import org.odk.collect.testshared.FakeLifecycleOwner;

public class TestScreenContext implements ScreenContext {

    private final FragmentActivity activity;
    private final FakeLifecycleOwner lifecycleOwner = new FakeLifecycleOwner();

    public TestScreenContext(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public FragmentActivity getActivity() {
        return activity;
    }

    @Override
    public LifecycleOwner getViewLifecycle() {
        return lifecycleOwner;
    }

    public void destroyLifecycle() {
        lifecycleOwner.destroy();
    }
}
