package app.nexusforms.android.network;

import android.net.NetworkInfo;

public interface NetworkStateProvider {

    boolean isDeviceOnline();

    NetworkInfo getNetworkInfo();
}
