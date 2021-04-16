package app.nexusforms.android.support;

import android.content.Context;
import android.webkit.MimeTypeMap;

import androidx.test.espresso.IdlingResource;
import androidx.work.WorkManager;

import app.nexusforms.android.gdrive.GoogleAccountPicker;
import app.nexusforms.android.gdrive.GoogleApiProvider;
import app.nexusforms.android.gdrive.sheets.DriveApi;
import app.nexusforms.android.gdrive.sheets.SheetsApi;
import app.nexusforms.android.injection.config.AppDependencyModule;
import app.nexusforms.android.openrosa.OpenRosaHttpInterface;
import app.nexusforms.android.storage.StoragePathProvider;
import org.odk.collect.async.Scheduler;
import app.nexusforms.utilities.UserAgentProvider;

import java.util.List;

import app.nexusforms.async.Scheduler;

import static java.util.Arrays.asList;

public class TestDependencies extends AppDependencyModule {

    private final CallbackCountingTaskExecutorRule countingTaskExecutorRule = new CallbackCountingTaskExecutorRule();

    public final StubOpenRosaServer server = new StubOpenRosaServer();
    public final TestScheduler scheduler = new TestScheduler();
    public final FakeGoogleApi googleApi = new FakeGoogleApi();
    public final FakeGoogleAccountPicker googleAccountPicker = new FakeGoogleAccountPicker();
    public final StoragePathProvider storagePathProvider = new StoragePathProvider();

    public final List<IdlingResource> idlingResources = asList(
            new SchedulerIdlingResource(scheduler),
            new CountingTaskExecutorIdlingResource(countingTaskExecutorRule)
    );

    @Override
    public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
        return server;
    }

    @Override
    public Scheduler providesScheduler(WorkManager workManager) {
        return scheduler;
    }

    @Override
    public GoogleApiProvider providesGoogleApiProvider(Context context) {
        return new GoogleApiProvider(context) {

            @Override
            public SheetsApi getSheetsApi(String account) {
                googleApi.setAttemptAccount(account);
                return googleApi;
            }

            @Override
            public DriveApi getDriveApi(String account) {
                googleApi.setAttemptAccount(account);
                return googleApi;
            }
        };
    }

    @Override
    public GoogleAccountPicker providesGoogleAccountPicker(Context context) {
        return googleAccountPicker;
    }
}
