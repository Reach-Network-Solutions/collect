package app.nexusforms.android.configure.qr;

import android.content.Context;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeResult;

import app.nexusforms.android.R;

import app.nexusforms.android.activities.ActivityUtils;
import app.nexusforms.android.activities.MainMenuActivity;
import app.nexusforms.android.fragments.BarCodeScannerFragment;
import app.nexusforms.android.injection.DaggerUtils;
import app.nexusforms.android.utilities.CompressionUtils;
import app.nexusforms.android.utilities.FileUtils;
import app.nexusforms.android.utilities.ToastUtils;

import app.nexusforms.analytics.Analytics;
import app.nexusforms.android.analytics.AnalyticsEvents;
import app.nexusforms.android.configure.SettingsImporter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

public class QRCodeScannerFragment extends BarCodeScannerFragment {

    @Inject
    SettingsImporter settingsImporter;

    @Inject
    Analytics analytics;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    protected void handleScanningResult(BarcodeResult result) throws IOException, DataFormatException {
        boolean importSuccess = settingsImporter.fromJSON(CompressionUtils.decompress(result.getText()));
        String settingsHash = FileUtils.getMd5Hash(new ByteArrayInputStream(result.getText().getBytes()));

        if (importSuccess) {
            ToastUtils.showLongToast(getString(R.string.successfully_imported_settings));
            analytics.logEvent(AnalyticsEvents.SETTINGS_IMPORT_QR, "Success", settingsHash);
            ActivityUtils.startActivityAndCloseAllOthers(requireActivity(), MainMenuActivity.class);
        } else {
            ToastUtils.showLongToast(getString(R.string.invalid_qrcode));
            analytics.logEvent(AnalyticsEvents.SETTINGS_IMPORT_QR, "No valid settings", settingsHash);
        }
    }

    @Override
    protected Collection<String> getSupportedCodeFormats() {
        return Collections.singletonList(IntentIntegrator.QR_CODE);
    }
}
