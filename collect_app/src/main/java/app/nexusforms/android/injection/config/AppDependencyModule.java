package app.nexusforms.android.injection.config;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.telephony.TelephonyManager;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;

import app.nexusforms.analytics.Analytics;
import app.nexusforms.analytics.BlockableFirebaseAnalytics;
import app.nexusforms.analytics.NoopAnalytics;
import app.nexusforms.android.application.CollectSettingsChangeHandler;
import app.nexusforms.android.application.initialization.ApplicationInitializer;
import app.nexusforms.android.application.initialization.CollectSettingsPreferenceMigrator;
import app.nexusforms.android.application.initialization.SettingsPreferenceMigrator;
import app.nexusforms.android.backgroundwork.ChangeLock;
import app.nexusforms.android.backgroundwork.FormSubmitManager;
import app.nexusforms.android.backgroundwork.FormUpdateManager;
import app.nexusforms.android.backgroundwork.ReentrantLockChangeLock;
import app.nexusforms.android.backgroundwork.SchedulerFormUpdateAndSubmitManager;
import app.nexusforms.android.formmanagement.DiskFormsSynchronizer;
import app.nexusforms.android.formmanagement.FormDownloader;
import app.nexusforms.android.formmanagement.FormMetadataParser;
import app.nexusforms.android.formmanagement.ServerFormDownloader;
import app.nexusforms.android.formmanagement.ServerFormsDetailsFetcher;
import app.nexusforms.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import app.nexusforms.android.formmanagement.matchexactly.SyncStatusRepository;
import app.nexusforms.android.instances.InstancesRepository;
import app.nexusforms.android.notifications.NotificationManagerNotifier;
import app.nexusforms.android.notifications.Notifier;
import app.nexusforms.android.permissions.PermissionsChecker;
import app.nexusforms.android.permissions.PermissionsProvider;
import app.nexusforms.android.preferences.keys.AdminKeys;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.keys.MetaKeys;
import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.android.preferences.source.SettingsStore;
import app.nexusforms.android.utilities.ActivityAvailability;
import app.nexusforms.android.utilities.AdminPasswordProvider;
import app.nexusforms.android.utilities.AndroidUserAgent;
import app.nexusforms.android.utilities.DeviceDetailsProvider;
import app.nexusforms.android.utilities.ExternalAppIntentProvider;
import app.nexusforms.android.utilities.ExternalWebPageHelper;
import app.nexusforms.android.utilities.FileProvider;
import app.nexusforms.android.utilities.FormsDirDiskFormsSynchronizer;
import app.nexusforms.android.utilities.MediaUtils;
import app.nexusforms.android.utilities.ScreenUtils;
import app.nexusforms.android.utilities.SoftKeyboardController;
import app.nexusforms.android.utilities.WebCredentialsUtils;
import app.nexusforms.android.configure.ServerRepository;
import app.nexusforms.android.configure.SettingsChangeHandler;
import app.nexusforms.android.configure.SettingsImporter;
import app.nexusforms.android.configure.SharedPreferencesServerRepository;
import app.nexusforms.android.configure.StructureAndTypeSettingsValidator;
import app.nexusforms.android.configure.qr.CachingQRCodeGenerator;
import app.nexusforms.android.configure.qr.JsonPreferencesGenerator;
import app.nexusforms.android.configure.qr.QRCodeDecoder;
import app.nexusforms.android.configure.qr.QRCodeGenerator;
import app.nexusforms.android.configure.qr.QRCodeUtils;
import app.nexusforms.android.database.DatabaseFormsRepository;
import app.nexusforms.android.database.DatabaseInstancesRepository;
import app.nexusforms.android.events.RxEventBus;
import app.nexusforms.android.formentry.BackgroundAudioViewModel;
import app.nexusforms.android.formentry.FormEntryViewModel;
import app.nexusforms.android.formentry.media.AudioHelperFactory;
import app.nexusforms.android.formentry.media.ScreenContextAudioHelperFactory;
import app.nexusforms.android.formentry.saving.DiskFormSaver;
import app.nexusforms.android.formentry.saving.FormSaveViewModel;
import app.nexusforms.android.forms.FormSource;
import app.nexusforms.android.forms.FormsRepository;
import app.nexusforms.android.gdrive.GoogleAccountCredentialGoogleAccountPicker;
import app.nexusforms.android.gdrive.GoogleAccountPicker;
import app.nexusforms.android.gdrive.GoogleApiProvider;
import app.nexusforms.android.geo.MapProvider;
import app.nexusforms.android.logic.PropertyManager;
import app.nexusforms.android.metadata.InstallIDProvider;
import app.nexusforms.android.metadata.SharedPreferencesInstallIDProvider;
import app.nexusforms.android.network.ConnectivityProvider;
import app.nexusforms.android.network.NetworkStateProvider;
import app.nexusforms.android.openrosa.CollectThenSystemContentTypeMapper;
import app.nexusforms.android.openrosa.OpenRosaFormSource;
import app.nexusforms.android.openrosa.OpenRosaHttpInterface;
import app.nexusforms.android.openrosa.OpenRosaResponseParserImpl;
import app.nexusforms.android.openrosa.okhttp.OkHttpConnection;
import app.nexusforms.android.openrosa.okhttp.OkHttpOpenRosaServerClientProvider;

import app.nexusforms.android.storage.StorageInitializer;
import app.nexusforms.android.storage.StoragePathProvider;
import app.nexusforms.android.storage.StorageSubdirectory;
import app.nexusforms.android.version.VersionInformation;
import app.nexusforms.android.views.BarcodeViewDecoder;
import app.nexusforms.async.CoroutineAndWorkManagerScheduler;
import app.nexusforms.async.Scheduler;
import app.nexusforms.audiorecorder.recording.AudioRecorder;
import app.nexusforms.audiorecorder.recording.AudioRecorderFactory;
import app.nexusforms.utilities.Clock;
import app.nexusforms.utilities.UserAgentProvider;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import static androidx.core.content.FileProvider.getUriForFile;

/**
 * Add dependency providers here (annotated with @Provides)
 * for objects you need to inject
 */
@Module
@SuppressWarnings("PMD.CouplingBetweenObjects")
public class AppDependencyModule {

    @Provides
    Context context(Application application) {
        return application;
    }

    @Provides
    @Singleton
    RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }

    @Provides
    MimeTypeMap provideMimeTypeMap() {
        return MimeTypeMap.getSingleton();
    }

    @Provides
    @Singleton
    UserAgentProvider providesUserAgent() {
        return new AndroidUserAgent();
    }

    @Provides
    @Singleton
    public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider) {
        return new OkHttpConnection(
                new OkHttpOpenRosaServerClientProvider(new OkHttpClient()),
                new CollectThenSystemContentTypeMapper(mimeTypeMap),
                userAgentProvider.getUserAgent()
        );
    }

    @Provides
    WebCredentialsUtils provideWebCredentials(SettingsProvider settingsProvider) {
        return new WebCredentialsUtils(settingsProvider.getGeneralSettings());
    }

    @Provides
    public FormDownloader providesFormDownloader(FormSource formSource, FormsRepository formsRepository, StoragePathProvider storagePathProvider, Analytics analytics) {
        return new ServerFormDownloader(formSource, formsRepository, new File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE)), storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS), new FormMetadataParser(ReferenceManager.instance()), analytics);
    }

    @Provides
    @Singleton
    public Analytics providesAnalytics(Application application) {
        try {
            return new BlockableFirebaseAnalytics(application);
        } catch (IllegalStateException e) {
            // Couldn't setup Firebase so use no-op instance
            return new NoopAnalytics();
        }
    }

    @Provides
    public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker) {
        return new PermissionsProvider(permissionsChecker);
    }

    @Provides
    public ReferenceManager providesReferenceManager() {
        return ReferenceManager.instance();
    }

    @Provides
    public AudioHelperFactory providesAudioHelperFactory(Scheduler scheduler) {
        return new ScreenContextAudioHelperFactory(scheduler, MediaPlayer::new);
    }

    @Provides
    public ActivityAvailability providesActivityAvailability(Context context) {
        return new ActivityAvailability(context);
    }

    @Provides
    @Singleton
    public StorageInitializer providesStorageInitializer() {
        return new StorageInitializer();
    }

    @Provides
    @Singleton
    public SettingsProvider providesSettingsProvider(Context context) {
        return new SettingsProvider(context);
    }

    @Provides
    InstallIDProvider providesInstallIDProvider(SettingsProvider settingsProvider) {
        return new SharedPreferencesInstallIDProvider(settingsProvider.getMetaSettings(), MetaKeys.KEY_INSTALL_ID);
    }

    @Provides
    public DeviceDetailsProvider providesDeviceDetailsProvider(Context context, InstallIDProvider installIDProvider) {
        return new DeviceDetailsProvider() {

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getDeviceId() {
                return installIDProvider.getInstallID();
            }

            @Override
            @SuppressLint({"MissingPermission", "HardwareIds"})
            public String getLine1Number() {
                TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                return telMgr.getLine1Number();
            }
        };
    }

    @Provides
    @Singleton
    public MapProvider providesMapProvider() {
        return new MapProvider();
    }

    @Provides
    public StoragePathProvider providesStoragePathProvider() {
        return new StoragePathProvider();
    }

    @Provides
    public AdminPasswordProvider providesAdminPasswordProvider(SettingsProvider settingsProvider) {
        return new AdminPasswordProvider(settingsProvider.getAdminSettings());
    }

    @Provides
    public FormUpdateManager providesFormUpdateManger(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        return new SchedulerFormUpdateAndSubmitManager(scheduler, settingsProvider.getGeneralSettings(), application);
    }

    @Provides
    public FormSubmitManager providesFormSubmitManager(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        return new SchedulerFormUpdateAndSubmitManager(scheduler, settingsProvider.getGeneralSettings(), application);
    }

    @Provides
    public NetworkStateProvider providesConnectivityProvider() {
        return new ConnectivityProvider();
    }

    @Provides
    public QRCodeGenerator providesQRCodeGenerator(Context context) {
        return new CachingQRCodeGenerator();
    }

    @Provides
    public VersionInformation providesVersionInformation() {
        return new VersionInformation(() -> BuildConfig.VERSION_NAME);
    }

    @Provides
    public FileProvider providesFileProvider(Context context) {
        return filePath -> getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(filePath));
    }

    @Provides
    public WorkManager providesWorkManager(Context context) {
        return WorkManager.getInstance(context);
    }

    @Provides
    public Scheduler providesScheduler(WorkManager workManager) {
        return new CoroutineAndWorkManagerScheduler(workManager);
    }

    @Singleton
    @Provides
    public ApplicationInitializer providesApplicationInitializer(Application application, UserAgentProvider userAgentProvider,
                                                                 SettingsPreferenceMigrator preferenceMigrator, PropertyManager propertyManager,
                                                                 Analytics analytics, StorageInitializer storageInitializer, SettingsProvider settingsProvider) {
        return new ApplicationInitializer(application, userAgentProvider, preferenceMigrator, propertyManager, analytics, storageInitializer, settingsProvider);
    }

    @Provides
    public SettingsPreferenceMigrator providesPreferenceMigrator(SettingsProvider settingsProvider) {
        return new CollectSettingsPreferenceMigrator(settingsProvider.getMetaSettings());
    }

    @Provides
    @Singleton
    public PropertyManager providesPropertyManager(RxEventBus eventBus, PermissionsProvider permissionsProvider, DeviceDetailsProvider deviceDetailsProvider, SettingsProvider settingsProvider) {
        return new PropertyManager(eventBus, permissionsProvider, deviceDetailsProvider, settingsProvider);
    }

    @Provides
    public ServerRepository providesServerRepository(Context context, SettingsProvider settingsProvider) {
        return new SharedPreferencesServerRepository(context.getString(R.string.default_server_url), settingsProvider.getMetaSettings());
    }

    @Provides
    public SettingsChangeHandler providesSettingsChangeHandler(PropertyManager propertyManager, FormUpdateManager formUpdateManager, ServerRepository serverRepository, Analytics analytics, SettingsProvider settingsProvider) {
        return new CollectSettingsChangeHandler(propertyManager, formUpdateManager, serverRepository, analytics, settingsProvider);
    }

    @Provides
    public SettingsImporter providesCollectSettingsImporter(SettingsProvider settingsProvider, SettingsPreferenceMigrator preferenceMigrator, SettingsChangeHandler settingsChangeHandler) {
        HashMap<String, Object> generalDefaults = GeneralKeys.DEFAULTS;
        Map<String, Object> adminDefaults = AdminKeys.getDefaults();
        return new SettingsImporter(
                settingsProvider.getGeneralSettings(),
                settingsProvider.getAdminSettings(),
                preferenceMigrator,
                new StructureAndTypeSettingsValidator(generalDefaults, adminDefaults),
                generalDefaults,
                adminDefaults,
                settingsChangeHandler
        );
    }

    @Provides
    public BarcodeViewDecoder providesBarcodeViewDecoder() {
        return new BarcodeViewDecoder();
    }

    @Provides
    public QRCodeDecoder providesQRCodeDecoder() {
        return new QRCodeUtils();
    }

    @Provides
    public FormsRepository providesFormRepository() {
        return new DatabaseFormsRepository();
    }

    @Provides
    public FormSource providesFormSource(SettingsProvider settingsProvider, Context context, OpenRosaHttpInterface openRosaHttpInterface, WebCredentialsUtils webCredentialsUtils, Analytics analytics) {
        String serverURL = settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_SERVER_URL);
        String formListPath = settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_FORMLIST_URL);

        return new OpenRosaFormSource(serverURL, formListPath, openRosaHttpInterface, webCredentialsUtils, analytics, new OpenRosaResponseParserImpl());
    }

    @Provides
    public DiskFormsSynchronizer providesDiskFormSynchronizer() {
        return new FormsDirDiskFormsSynchronizer();
    }

    @Provides
    @Singleton
    public SyncStatusRepository providesServerFormSyncRepository() {
        return new SyncStatusRepository();
    }

    @Provides
    public ServerFormsDetailsFetcher providesServerFormDetailsFetcher(FormsRepository formsRepository, FormSource formSource, DiskFormsSynchronizer diskFormsSynchronizer) {
        return new ServerFormsDetailsFetcher(formsRepository, formSource, diskFormsSynchronizer);
    }

    @Provides
    public ServerFormsSynchronizer providesServerFormSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormsRepository formsRepository, FormDownloader formDownloader, InstancesRepository instancesRepository) {
        return new ServerFormsSynchronizer(serverFormsDetailsFetcher, formsRepository, instancesRepository, formDownloader);
    }

    @Provides
    public Notifier providesNotifier(Application application, SettingsProvider settingsProvider) {
        return new NotificationManagerNotifier(application, settingsProvider);
    }

    @Provides
    @Named("FORMS")
    @Singleton
    public ChangeLock providesFormsChangeLock() {
        return new ReentrantLockChangeLock();
    }

    @Provides
    @Named("INSTANCES")
    @Singleton
    public ChangeLock providesInstancesChangeLock() {
        return new ReentrantLockChangeLock();
    }

    @Provides
    public InstancesRepository providesInstancesRepository() {
        return new DatabaseInstancesRepository();
    }

    @Provides
    public GoogleApiProvider providesGoogleApiProvider(Context context) {
        return new GoogleApiProvider(context);
    }

    @Provides
    public GoogleAccountPicker providesGoogleAccountPicker(Context context) {
        return new GoogleAccountCredentialGoogleAccountPicker(GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff()));
    }

    @Provides
    ScreenUtils providesScreenUtils(Context context) {
        return new ScreenUtils(context);
    }

    @Provides
    public AudioRecorder providesAudioRecorder(Application application) {
        return new AudioRecorderFactory(application).create();
    }

    @Provides
    public FormSaveViewModel.FactoryFactory providesFormSaveViewModelFactoryFactory(Analytics analytics, Scheduler scheduler, AudioRecorder audioRecorder) {
        return (owner, defaultArgs) -> new AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            @NonNull
            @Override
            protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
                return (T) new FormSaveViewModel(handle, System::currentTimeMillis, new DiskFormSaver(), new MediaUtils(), analytics, scheduler, audioRecorder);
            }
        };
    }

    @Provides
    public Clock providesClock() {
        return System::currentTimeMillis;
    }

    @Provides
    public SoftKeyboardController provideSoftKeyboardController() {
        return new SoftKeyboardController();
    }

    @Provides
    public JsonPreferencesGenerator providesJsonPreferencesGenerator(SettingsProvider settingsProvider) {
        return new JsonPreferencesGenerator(settingsProvider);
    }

    @Provides
    @Singleton
    public PermissionsChecker providesPermissionsChecker(Context context) {
        return new PermissionsChecker(context);
    }

    @Provides
    @Singleton
    public ExternalAppIntentProvider providesExternalAppIntentProvider() {
        return new ExternalAppIntentProvider();
    }

    @Provides
    public FormEntryViewModel.Factory providesFormEntryViewModelFactory(Clock clock, Analytics analytics) {
        return new FormEntryViewModel.Factory(clock, analytics);
    }

    @Provides
    public BackgroundAudioViewModel.Factory providesBackgroundAudioViewModelFactory(AudioRecorder audioRecorder, SettingsProvider settingsProvider, PermissionsChecker permissionsChecker, Clock clock, Analytics analytics) {
        return new BackgroundAudioViewModel.Factory(audioRecorder, settingsProvider.getGeneralSettings(), permissionsChecker, clock, analytics);
    }

    @Provides
    @Named("GENERAL_SETTINGS_STORE")
    @Singleton
    public SettingsStore providesGeneralSettingsStore(SettingsProvider settingsProvider) {
        return new SettingsStore(settingsProvider.getGeneralSettings());
    }

    @Provides
    @Named("ADMIN_SETTINGS_STORE")
    @Singleton
    public SettingsStore providesAdminSettingsStore(SettingsProvider settingsProvider) {
        return new SettingsStore(settingsProvider.getAdminSettings());
    }

    @Provides
    public ExternalWebPageHelper providesExternalWebPageHelper() {
        return new ExternalWebPageHelper();
    }
}
