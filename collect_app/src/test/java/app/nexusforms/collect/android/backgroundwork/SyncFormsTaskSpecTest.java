package app.nexusforms.collect.android.backgroundwork;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.odk.collect.analytics.Analytics;
import app.nexusforms.android.analytics.AnalyticsEvents;
import app.nexusforms.android.backgroundwork.ChangeLock;
import app.nexusforms.android.backgroundwork.SyncFormsTaskSpec;
import app.nexusforms.android.formmanagement.FormDownloader;
import app.nexusforms.android.formmanagement.ServerFormsDetailsFetcher;
import app.nexusforms.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import app.nexusforms.android.formmanagement.matchexactly.SyncStatusRepository;
import app.nexusforms.android.forms.FormSourceException;
import app.nexusforms.android.forms.FormsRepository;
import app.nexusforms.android.injection.config.AppDependencyModule;
import app.nexusforms.android.instances.InstancesRepository;
import app.nexusforms.android.notifications.Notifier;
import org.odk.collect.android.preferences.source.SettingsProvider;
import app.nexusforms.collect.android.support.BooleanChangeLock;
import app.nexusforms.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import java.util.function.Supplier;

import app.nexusforms.analytics.Analytics;
import app.nexusforms.android.preferences.source.SettingsProvider;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(RobolectricTestRunner.class)
public class SyncFormsTaskSpecTest {

    private final ServerFormsSynchronizer serverFormsSynchronizer = mock(ServerFormsSynchronizer.class);
    private final SyncStatusRepository syncStatusRepository = mock(SyncStatusRepository.class);
    private final Notifier notifier = mock(Notifier.class);
    private final Analytics analytics = mock(Analytics.class);
    private final BooleanChangeLock changeLock = new BooleanChangeLock();

    @Before
    public void setup() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ChangeLock providesFormsChangeLock() {
                return changeLock;
            }

            @Override
            public ServerFormsSynchronizer providesServerFormSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormsRepository formsRepository, FormDownloader formDownloader, InstancesRepository instancesRepository) {
                return serverFormsSynchronizer;
            }

            @Override
            public SyncStatusRepository providesServerFormSyncRepository() {
                return syncStatusRepository;
            }

            @Override
            public Notifier providesNotifier(Application application, SettingsProvider settingsProvider) {
                return notifier;
            }

            @Override
            public Analytics providesAnalytics(Application application) {
                return analytics;
            }
        });
    }

    @Test
    public void setsRepositoryToSyncing_runsSync_thenSetsRepositoryToNotSyncingAndNotifies() throws Exception {
        InOrder inOrder = inOrder(syncStatusRepository, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        inOrder.verify(syncStatusRepository).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(syncStatusRepository).finishSync(null);

        verify(notifier).onSync(null);
    }

    @Test
    public void logsAnalytics() {
        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verify(analytics).logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, "Success");
    }

    @Test
    public void whenSynchronizingFails_setsRepositoryToNotSyncingAndNotifiesWithError() throws Exception {
        FormSourceException exception = new FormSourceException.FetchError();
        doThrow(exception).when(serverFormsSynchronizer).synchronize();
        InOrder inOrder = inOrder(syncStatusRepository, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        inOrder.verify(syncStatusRepository).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(syncStatusRepository).finishSync(exception);

        verify(notifier).onSync(exception);
    }

    @Test
    public void whenSynchronizingFails_logsAnalytics() throws Exception {
        FormSourceException exception = new FormSourceException.FetchError();
        doThrow(exception).when(serverFormsSynchronizer).synchronize();

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verify(analytics).logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, "FETCH_ERROR");
    }

    @Test
    public void whenChangeLockLocked_doesNothing() {
        changeLock.lock();

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verifyNoInteractions(serverFormsSynchronizer);
        verifyNoInteractions(syncStatusRepository);
        verifyNoInteractions(notifier);
    }
}
