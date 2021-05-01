package app.nexusforms.android.feature.settings;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.work.WorkManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import app.nexusforms.android.configure.qr.QRCodeGenerator;
import app.nexusforms.android.injection.config.AppDependencyModule;
import app.nexusforms.android.configure.qr.JsonPreferencesGenerator;
import app.nexusforms.android.support.CallbackCountingTaskExecutorRule;
import app.nexusforms.android.support.CollectTestRule;
import app.nexusforms.android.support.CountingTaskExecutorIdlingResource;
import app.nexusforms.android.support.IdlingResourceRule;
import app.nexusforms.android.support.ResetStateRule;
import app.nexusforms.android.support.RunnableRule;
import app.nexusforms.android.support.SchedulerIdlingResource;
import app.nexusforms.android.support.TestScheduler;
import app.nexusforms.android.support.pages.MainMenuPage;
import app.nexusforms.android.support.pages.GeneralSettingsPage;
import app.nexusforms.android.support.pages.QRCodePage;
import app.nexusforms.android.utilities.CompressionUtils;
import app.nexusforms.android.views.BarcodeViewDecoder;
import org.odk.collect.async.Scheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import app.nexusforms.async.Scheduler;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@RunWith(AndroidJUnit4.class)
public class ConfigureWithQRCodeTest {

    private final CollectTestRule rule = new CollectTestRule();
    private final StubQRCodeGenerator stubQRCodeGenerator = new StubQRCodeGenerator();
    private final StubBarcodeViewDecoder stubBarcodeViewDecoder = new StubBarcodeViewDecoder();
    private final TestScheduler testScheduler = new TestScheduler();
    private final CallbackCountingTaskExecutorRule countingTaskExecutorRule = new CallbackCountingTaskExecutorRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA
            ))
            .around(new ResetStateRule(new AppDependencyModule() {

                @Override
                public BarcodeViewDecoder providesBarcodeViewDecoder() {
                    return stubBarcodeViewDecoder;
                }

                @Override
                public QRCodeGenerator providesQRCodeGenerator(Context context) {
                    return stubQRCodeGenerator;
                }

                @Override
                public Scheduler providesScheduler(WorkManager workManager) {
                    return testScheduler;
                }
            }))
            .around(countingTaskExecutorRule)
            .around(new IdlingResourceRule(new SchedulerIdlingResource(testScheduler)))
            .around(new IdlingResourceRule(new CountingTaskExecutorIdlingResource(countingTaskExecutorRule)))
            .around(new RunnableRule(stubQRCodeGenerator::setup))
            .around(rule);

    @After
    public void teardown() {
        // Clean up files created by stub generator
        stubQRCodeGenerator.teardown();
    }

    @Test
    public void clickConfigureQRCode_opensScanner_andThenScanning_importsSettings() {
        QRCodePage qrCodePage = rule.mainMenu()
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickConfigureQR();

        stubBarcodeViewDecoder.scan("{\"general\":{ \"server_url\": \"http://gallops.example\" },\"admin\":{}}");
        qrCodePage.checkIsToastWithMessageDisplayed(R.string.successfully_imported_settings);

        new MainMenuPage(rule)
                .assertOnPage()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickServerSettings()
                .assertText("http://gallops.example");
    }

    @Test
    public void clickConfigureQRCode_andClickingOnView_showsQRCode() {
        rule.mainMenu()
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickConfigureQR()
                .clickView()
                .assertImageViewShowsImage(R.id.ivQRcode, BitmapFactory.decodeResource(
                        getApplicationContext().getResources(),
                        stubQRCodeGenerator.getDrawableID()
                ));
    }

    @Test
    public void whenThereIsAnAdminPassword_canRemoveFromQRCode() {
        rule.mainMenu()
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickOnString(R.string.admin_password)
                .inputText("blah")
                .clickOKOnDialog()
                .pressBack(new MainMenuPage(rule))

                .openProjectSettingsDialog()
                .clickAdminSettingsWithPassword("blah")
                .clickConfigureQR()
                .clickView()
                .clickOnString(R.string.qrcode_with_admin_password)
                .clickOnString(R.string.admin_password)
                .clickOnString(R.string.generate)
                .assertText(R.string.qrcode_without_passwords);
    }

    @Test
    public void whenThereIsAServerPassword_canRemoveFromQRCode() {
        rule.mainMenu()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .clickServerSettings()
                .clickServerPassword()
                .inputText("blah")
                .clickOKOnDialog()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickConfigureQR()
                .clickView()
                .clickOnString(R.string.qrcode_with_server_password)
                .clickOnString(R.string.server_password)
                .clickOnString(R.string.generate)
                .assertText(R.string.qrcode_without_passwords);
    }

    private static class StubQRCodeGenerator implements QRCodeGenerator {

        private static final int CHECKER_BACKGROUND_DRAWABLE_ID = R.drawable.checker_background;

        @Override
        public String generateQRCode(Collection<String> selectedPasswordKeys, JsonPreferencesGenerator jsonPreferencesGenerator) {
            return getQRCodeFilePath();
        }

        public void setup() {
            Bitmap bitmap = BitmapFactory.decodeResource(
                    getApplicationContext().getResources(),
                    getDrawableID());
            saveBitmap(bitmap);
        }

        public void teardown() {
            File file = new File(getQRCodeFilePath());
            if (file.exists()) {
                file.delete();
            }
        }

        String getQRCodeFilePath() {
            return getApplicationContext().getExternalFilesDir(null) + File.separator + "test-collect-settings.png";
        }

        int getDrawableID() {
            return CHECKER_BACKGROUND_DRAWABLE_ID;
        }

        private void saveBitmap(Bitmap bitmap) {
            try (FileOutputStream out = new FileOutputStream(getQRCodeFilePath())) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class StubBarcodeViewDecoder extends BarcodeViewDecoder {

        MutableLiveData<BarcodeResult> liveData = new MutableLiveData<>();

        @Override
        public LiveData<BarcodeResult> waitForBarcode(DecoratedBarcodeView view) {
            return liveData;
        }

        public void scan(String settings) {
            try {
                Result result = new Result(CompressionUtils.compress(settings), new byte[]{}, new ResultPoint[]{}, BarcodeFormat.AZTEC);
                BarcodeResult barcodeResult = new BarcodeResult(result, null);
                liveData.postValue(barcodeResult);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
