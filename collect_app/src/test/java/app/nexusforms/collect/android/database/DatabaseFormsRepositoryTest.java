package app.nexusforms.collect.android.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;

import app.nexusforms.android.database.DatabaseFormsRepository;
import app.nexusforms.android.forms.FormsRepository;
import app.nexusforms.collect.android.forms.FormsRepositoryTest;
import app.nexusforms.android.injection.config.AppDependencyModule;
import app.nexusforms.android.storage.StorageInitializer;
import app.nexusforms.android.storage.StoragePathProvider;
import app.nexusforms.android.storage.StorageSubdirectory;
import app.nexusforms.collect.android.support.RobolectricHelpers;
import app.nexusforms.utilities.Clock;

@RunWith(AndroidJUnit4.class)
public class DatabaseFormsRepositoryTest extends FormsRepositoryTest {

    private StoragePathProvider storagePathProvider;

    @Before
    public void setup() {
        RobolectricHelpers.mountExternalStorage();
        storagePathProvider = new StoragePathProvider();
        new StorageInitializer().createOdkDirsOnStorage();
    }

    @Override
    public FormsRepository buildSubject() {
        return new DatabaseFormsRepository();
    }

    @Override
    public FormsRepository buildSubject(Clock clock) {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public Clock providesClock() {
                return clock;
            }
        });

        return buildSubject();
    }

    @Override
    public String getFormFilesPath() {
        return storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS);
    }
}
