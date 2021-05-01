package app.nexusforms.android.configure.qr;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import app.nexusforms.android.R;

import app.nexusforms.android.activities.CollectAbstractActivity;
import app.nexusforms.android.injection.DaggerUtils;
import app.nexusforms.android.listeners.PermissionListener;
import app.nexusforms.android.utilities.ActivityAvailability;
import app.nexusforms.android.utilities.FileProvider;
import app.nexusforms.android.utilities.MultiClickGuard;

import app.nexusforms.analytics.Analytics;
import app.nexusforms.android.configure.SettingsImporter;

import app.nexusforms.async.Scheduler;

import javax.inject.Inject;

public class QRCodeTabsActivity extends CollectAbstractActivity {

    private static String[] fragmentTitleList;

    @Inject
    QRCodeGenerator qrCodeGenerator;

    @Inject
    ActivityAvailability activityAvailability;

    @Inject
    FileProvider fileProvider;

    @Inject
    Scheduler scheduler;

    @Inject
    QRCodeDecoder qrCodeDecoder;

    @Inject
    SettingsImporter settingsImporter;

    @Inject
    Analytics analytics;

    @Inject
    JsonPreferencesGenerator jsonPreferencesGenerator;

    private QRCodeMenuDelegate menuDelegate;
    private QRCodeActivityResultDelegate activityResultDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);

        menuDelegate = new QRCodeMenuDelegate(this, activityAvailability, qrCodeGenerator, jsonPreferencesGenerator, fileProvider, settingsProvider, scheduler);
        activityResultDelegate = new QRCodeActivityResultDelegate(this, settingsImporter, qrCodeDecoder, analytics);
        setContentView(R.layout.tabs_layout);

        initToolbar(getString(R.string.configure_via_qr_code));
        menuDelegate = new QRCodeMenuDelegate(this, activityAvailability, qrCodeGenerator, jsonPreferencesGenerator, fileProvider, settingsProvider, scheduler);

        permissionsProvider.requestCameraPermission(this, new PermissionListener() {
            @Override
            public void granted() {
                setupViewPager();
            }

            @Override
            public void denied() {
                finish();
            }
        });
    }

    private void setupViewPager() {
        fragmentTitleList = new String[]{getString(R.string.scan_qr_code_fragment_title),
                getString(R.string.view_qr_code_fragment_title)};

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        QRCodeTabsAdapter adapter = new QRCodeTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(fragmentTitleList[position])).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuDelegate.onCreateOptionsMenu(getMenuInflater(), menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        if (menuDelegate.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        activityResultDelegate.onActivityResult(requestCode, resultCode, data);
    }
}
