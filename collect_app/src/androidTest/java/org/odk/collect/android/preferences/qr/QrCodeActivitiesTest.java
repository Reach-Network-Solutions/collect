package org.odk.collect.android.preferences.qr;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class QrCodeActivitiesTest {
    @Test
    public void test() {
        FragmentScenario<QRScannerFragment> fs =
                FragmentScenario.launchInContainer(QRScannerFragment.class);

        fs.onFragment(new QrCodeFragmentAction());

    }


}
