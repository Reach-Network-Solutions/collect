package app.nexusforms.android.injection.config;

import android.app.Application;

import org.javarosa.core.reference.ReferenceManager;
import app.nexusforms.analytics.Analytics;

import app.nexusforms.android.activities.AndroidShortcutsActivity;
import app.nexusforms.android.activities.CollectAbstractActivity;
import app.nexusforms.android.activities.DeleteSavedFormActivity;
import app.nexusforms.android.activities.FillBlankFormActivity;
import app.nexusforms.android.activities.FormDownloadListActivity;
import app.nexusforms.android.activities.FormEntryActivity;
import app.nexusforms.android.activities.FormHierarchyActivity;
import app.nexusforms.android.activities.FormMapActivity;
import app.nexusforms.android.activities.GeoPointMapActivity;
import app.nexusforms.android.activities.GeoPolyActivity;
import app.nexusforms.android.activities.InstanceUploaderActivity;
import app.nexusforms.android.activities.InstanceUploaderListActivity;
import app.nexusforms.android.activities.MainActivity;
import app.nexusforms.android.activities.MainMenuActivity;
import app.nexusforms.android.activities.SplashScreenActivity;
import app.nexusforms.android.application.Collect;
import app.nexusforms.android.application.initialization.ApplicationInitializer;
import app.nexusforms.android.backgroundwork.AutoSendTaskSpec;
import app.nexusforms.android.backgroundwork.AutoUpdateTaskSpec;
import app.nexusforms.android.backgroundwork.SyncFormsTaskSpec;
import app.nexusforms.android.fragments.nexus.FormsLibraryFragment;
import app.nexusforms.android.fragments.nexus.MyFormsFragment;
import app.nexusforms.android.preferences.CaptionedListPreference;
import app.nexusforms.android.preferences.dialogs.AdminPasswordDialogFragment;
import app.nexusforms.android.preferences.dialogs.ChangeAdminPasswordDialog;
import app.nexusforms.android.preferences.dialogs.ServerAuthDialogFragment;
import app.nexusforms.android.preferences.nexus.DataStoreManager;
import app.nexusforms.android.preferences.screens.AdminPreferencesFragment;
import app.nexusforms.android.preferences.screens.BaseAdminPreferencesFragment;
import app.nexusforms.android.preferences.screens.BaseGeneralPreferencesFragment;
import app.nexusforms.android.preferences.screens.BasePreferencesFragment;
import app.nexusforms.android.preferences.screens.ExperimentalPreferencesFragment;
import app.nexusforms.android.preferences.screens.FormManagementPreferencesFragment;
import app.nexusforms.android.preferences.screens.FormMetadataPreferencesFragment;
import app.nexusforms.android.preferences.screens.GeneralPreferencesActivity;
import app.nexusforms.android.preferences.screens.GeneralPreferencesFragment;
import app.nexusforms.android.preferences.screens.IdentityPreferencesFragment;
import app.nexusforms.android.preferences.screens.ServerPreferencesFragment;
import app.nexusforms.android.preferences.screens.UserInterfacePreferencesFragment;
import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.android.utilities.ApplicationResetter;
import app.nexusforms.android.utilities.AuthDialogUtility;
import app.nexusforms.android.utilities.ThemeUtils;
import app.nexusforms.android.adapters.InstanceUploaderAdapter;
import app.nexusforms.android.audio.AudioRecordingControllerFragment;
import app.nexusforms.android.audio.AudioRecordingErrorDialogFragment;
import app.nexusforms.android.configure.SettingsImporter;
import app.nexusforms.android.configure.qr.QRCodeScannerFragment;
import app.nexusforms.android.configure.qr.QRCodeTabsActivity;
import app.nexusforms.android.configure.qr.ShowQRCodeFragment;
import app.nexusforms.android.formentry.BackgroundAudioPermissionDialogFragment;
//mport org.odk.collect.android.formentry.ODKView;
import app.nexusforms.android.formentry.QuitFormDialogFragment;
import app.nexusforms.android.formentry.saving.SaveAnswerFileErrorDialogFragment;
import app.nexusforms.android.formentry.saving.SaveFormProgressDialogFragment;
import app.nexusforms.android.fragments.AppListFragment;
import app.nexusforms.android.fragments.BarCodeScannerFragment;
import app.nexusforms.android.fragments.BlankFormListFragment;
import app.nexusforms.android.fragments.MapBoxInitializationFragment;
import app.nexusforms.android.fragments.SavedFormListFragment;
import app.nexusforms.android.fragments.dialogs.SelectMinimalDialog;
import app.nexusforms.android.gdrive.GoogleDriveActivity;
import app.nexusforms.android.gdrive.GoogleSheetsUploaderActivity;
import app.nexusforms.android.geo.GoogleMapFragment;
import app.nexusforms.android.geo.MapboxMapFragment;
import app.nexusforms.android.geo.OsmDroidMapFragment;
import app.nexusforms.android.logic.PropertyManager;
import app.nexusforms.android.openrosa.OpenRosaHttpInterface;

import app.nexusforms.android.preferences.source.SettingsProvider;
import app.nexusforms.android.project.ProjectSettingsDialog;
import app.nexusforms.android.provider.FormsProvider;
import app.nexusforms.android.provider.InstanceProvider;
import app.nexusforms.android.storage.StorageInitializer;
import app.nexusforms.android.tasks.InstanceServerUploaderTask;
import app.nexusforms.android.tasks.MediaLoadingTask;
import app.nexusforms.android.widgets.ExStringWidget;
import app.nexusforms.android.widgets.QuestionWidget;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

/**
 * Dagger component for the application. Should include
 * application level Dagger Modules and be built with Application
 * object.
 * <p>
 * Add an `inject(MyClass myClass)` method here for objects you want
 * to inject into so Dagger knows to wire it up.
 * <p>
 * Annotated with @Singleton so modules can include @Singletons that will
 * be retained at an application level (as this an instance of this components
 * is owned by the Application object).
 * <p>
 * If you need to call a provider directly from the component (in a test
 * for example) you can add a method with the type you are looking to fetch
 * (`MyType myType()`) to this interface.
 * <p>
 * To read more about Dagger visit: https://google.github.io/dagger/users-guide
 **/

@Singleton
@Component(modules = {
        AppDependencyModule.class
})
public interface AppDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        Builder appDependencyModule(AppDependencyModule testDependencyModule);

        AppDependencyComponent build();
    }

    void inject(Collect collect);

    void inject(InstanceUploaderAdapter instanceUploaderAdapter);

    void inject(SavedFormListFragment savedFormListFragment);

    void inject(PropertyManager propertyManager);

    void inject(FormEntryActivity formEntryActivity);

    void inject(InstanceServerUploaderTask uploader);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(AuthDialogUtility authDialogUtility);

    void inject(FormDownloadListActivity formDownloadListActivity);

    void inject(InstanceUploaderListActivity activity);

    void inject(GoogleDriveActivity googleDriveActivity);

    void inject(GoogleSheetsUploaderActivity googleSheetsUploaderActivity);

    void inject(QuestionWidget questionWidget);

    void inject(ExStringWidget exStringWidget);

    //void inject(ODKView odkView);

    void inject(FormMetadataPreferencesFragment formMetadataPreferencesFragment);

    void inject(GeoPointMapActivity geoMapActivity);

    void inject(GeoPolyActivity geoPolyActivity);

    void inject(FormMapActivity formMapActivity);

    void inject(OsmDroidMapFragment mapFragment);

    void inject(GoogleMapFragment mapFragment);

    void inject(MapboxMapFragment mapFragment);

    void inject(MainMenuActivity mainMenuActivity);

    void inject(QRCodeTabsActivity qrCodeTabsActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(StorageInitializer storageInitializer);

    void inject(AutoSendTaskSpec autoSendTaskSpec);

    void inject(AdminPasswordDialogFragment adminPasswordDialogFragment);

    void inject(SplashScreenActivity splashScreenActivity);

    void inject(FormHierarchyActivity formHierarchyActivity);

    void inject(FormManagementPreferencesFragment formManagementPreferencesFragment);

    void inject(IdentityPreferencesFragment identityPreferencesFragment);

    void inject(UserInterfacePreferencesFragment userInterfacePreferencesFragment);

    void inject(SaveFormProgressDialogFragment saveFormProgressDialogFragment);

    void inject(QuitFormDialogFragment quitFormDialogFragment);

    void inject(BarCodeScannerFragment barCodeScannerFragment);

    void inject(QRCodeScannerFragment qrCodeScannerFragment);

    void inject(GeneralPreferencesActivity generalPreferencesActivity);

    void inject(ApplicationResetter applicationResetter);

    void inject(FillBlankFormActivity fillBlankFormActivity);

    void inject(MapBoxInitializationFragment mapBoxInitializationFragment);

    void inject(SyncFormsTaskSpec syncWork);

    void inject(ExperimentalPreferencesFragment experimentalPreferencesFragment);

    void inject(AutoUpdateTaskSpec autoUpdateTaskSpec);

    void inject(ServerAuthDialogFragment serverAuthDialogFragment);

    void inject(BasePreferencesFragment basePreferencesFragment);

    void inject(BlankFormListFragment blankFormListFragment);

    void inject(InstanceUploaderActivity instanceUploaderActivity);

    void inject(GeneralPreferencesFragment generalPreferencesFragment);

    void inject(DeleteSavedFormActivity deleteSavedFormActivity);

    void inject(AdminPreferencesFragment.MainMenuAccessPreferences mainMenuAccessPreferences);

    void inject(SelectMinimalDialog selectMinimalDialog);

    void inject(AudioRecordingControllerFragment audioRecordingControllerFragment);

    void inject(SaveAnswerFileErrorDialogFragment saveAnswerFileErrorDialogFragment);

    void inject(AudioRecordingErrorDialogFragment audioRecordingErrorDialogFragment);

    void inject(CollectAbstractActivity collectAbstractActivity);

    void inject(FormsProvider formsProvider);

    void inject(InstanceProvider instanceProvider);

    void inject(BackgroundAudioPermissionDialogFragment backgroundAudioPermissionDialogFragment);

    void inject(AppListFragment appListFragment);

    void inject(ChangeAdminPasswordDialog changeAdminPasswordDialog);

    void inject(MediaLoadingTask mediaLoadingTask);

    void inject(ThemeUtils themeUtils);

    void inject(BaseGeneralPreferencesFragment baseGeneralPreferencesFragment);

    void inject(BaseAdminPreferencesFragment baseAdminPreferencesFragment);

    void inject(CaptionedListPreference captionedListPreference);

    void inject(AndroidShortcutsActivity androidShortcutsActivity);

    void inject(ProjectSettingsDialog projectSettingsDialog);

    void Inject(MainActivity mainActivity);

    void inject(FormsLibraryFragment formsLibraryFragment);

    void inject(MyFormsFragment myFormsFragment);

    void inject(DataStoreManager dataStoreManager);

    OpenRosaHttpInterface openRosaHttpInterface();

    ReferenceManager referenceManager();

    Analytics analytics();

    SettingsProvider preferencesRepository();

    ApplicationInitializer applicationInitializer();

    SettingsImporter settingsImporter();
}
