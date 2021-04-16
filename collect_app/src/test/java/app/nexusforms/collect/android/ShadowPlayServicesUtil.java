package app.nexusforms.collect.android;

import android.content.Context;

import app.nexusforms.android.utilities.PlayServicesChecker;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(PlayServicesChecker.class)
public abstract class ShadowPlayServicesUtil {

    @Implementation
    public static boolean isGooglePlayServicesAvailable(Context context) {
        return true;
    }
}

