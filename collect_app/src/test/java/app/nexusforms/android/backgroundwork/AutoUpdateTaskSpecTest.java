package app.nexusforms.android.backgroundwork;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import app.nexusforms.android.R;
import app.nexusforms.analytics.Analytics;
import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.application.Collect;
import app.nexusforms.android.formmanagement.DiskFormsSynchronizer;
import app.nexusforms.android.formmanagement.FormDownloader;
import app.nexusforms.android.formmanagement.ServerFormDetails;
import app.nexusforms.android.formmanagement.ServerFormsDetailsFetcher;
import app.nexusforms.android.forms.FormSource;
import app.nexusforms.android.forms.FormsRepository;
import app.nexusforms.android.forms.ManifestFile;
import app.nexusforms.android.injection.config.AppDependencyModule;
import app.nexusforms.android.notifications.Notifier;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.Settings;
import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.android.storage.StoragePathProvider;
import app.nexusforms.android.support.BooleanChangeLock;
import app.nexusforms.android.support.RobolectricHelpers;

import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.function.Supplier;

import app.nexusforms.analytics.Analytics;
import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.preferences.source.Settings;
import app.nexusforms.android.preferences.source.SettingsProvider;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("PMD.DoubleBraceInitialization")
public class AutoUpdateTaskSpecTest {

    private final BooleanChangeLock changeLock = new BooleanChangeLock();
    private final FormDownloader formDownloader = mock(FormDownloader.class);
    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher = mock(ServerFormsDetailsFetcher.class);
    private final Notifier notifier = mock(Notifier.class);
    private final Settings generalSettings = TestSettingsProvider.getGeneralSettings();

    @Before
    public void setup() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ChangeLock providesFormsChangeLock() {
                return changeLock;
            }

            @Override
            public FormDownloader providesFormDownloader(FormSource formSource, FormsRepository formsRepository, StoragePathProvider storagePathProvider, Analytics analytics) {
                return formDownloader;
            }

            @Override
            public ServerFormsDetailsFetcher providesServerFormDetailsFetcher(FormsRepository formsRepository, FormSource formSource, DiskFormsSynchronizer diskFormsSynchronizer) {
                return serverFormsDetailsFetcher;
            }

            @Override
            public Notifier providesNotifier(Application application, SettingsProvider settingsProvider) {
                return notifier;
            }
        });
        generalSettings.clear();
        generalSettings.setDefaultForAllSettingsWithoutValues();
    }

    @Test
    public void whenThereAreUpdatedFormsOnServer_sendsUpdatesToNotifier() throws Exception {
        ServerFormDetails updatedForm = new ServerFormDetails("", "", "", "", "", false, true, new ManifestFile("", emptyList()));
        ServerFormDetails oldForm = new ServerFormDetails("", "", "", "", "", false, false, new ManifestFile("", emptyList()));
        when(serverFormsDetailsFetcher.fetchFormDetails()).thenReturn(asList(
                updatedForm,
                oldForm
        ));

        AutoUpdateTaskSpec taskSpec = new AutoUpdateTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verify(notifier).onUpdatesAvailable(asList(updatedForm));
    }

    @Test
    public void whenAutoDownloadEnabled_andChangeLockLocked_doesNotDownload() throws Exception {
        when(serverFormsDetailsFetcher.fetchFormDetails()).thenReturn(asList(new ServerFormDetails("", "", "", "", "", false, true, new ManifestFile("", emptyList()))));
        generalSettings.save(GeneralKeys.KEY_AUTOMATIC_UPDATE, true);
        changeLock.lock();

        AutoUpdateTaskSpec taskSpec = new AutoUpdateTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verifyNoInteractions(formDownloader);
    }

    @Test
    public void whenAutoDownloadEnabled_andDownloadIsCancelled_sendsCompletedDownloadsToNotifier() throws Exception {
        generalSettings.save(GeneralKeys.KEY_AUTOMATIC_UPDATE, true);

        ServerFormDetails form1 = new ServerFormDetails("", "", "form1", "", "", false, true, new ManifestFile("", emptyList()));
        ServerFormDetails form2 = new ServerFormDetails("", "", "form2", "", "", false, true, new ManifestFile("", emptyList()));
        when(serverFormsDetailsFetcher.fetchFormDetails()).thenReturn(asList(form1, form2));

        // Cancel form download after downloading one form
        doAnswer(new Answer<Void>() {

            private boolean calledBefore;

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (!calledBefore) {
                    calledBefore = true;
                } else {
                    throw new InterruptedException();
                }

                return null;
            }
        }).when(formDownloader).downloadForm(any(), any(), any());

        AutoUpdateTaskSpec taskSpec = new AutoUpdateTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verify(notifier).onUpdatesDownloaded(new HashMap<ServerFormDetails, String>() {
            {
                put(form1, Collect.getInstance().getString(R.string.success));
            }
        });
    }
}
