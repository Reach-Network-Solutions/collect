package app.nexusforms.android.instrumented.database.helpers;

import android.database.sqlite.SQLiteDatabase;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import app.nexusforms.android.database.DatabaseInstancesRepository;
import app.nexusforms.android.database.InstanceDatabaseMigrator;
import app.nexusforms.android.database.InstancesDatabaseHelper;
import app.nexusforms.android.instances.Instance;
import app.nexusforms.android.storage.StoragePathProvider;
import app.nexusforms.android.storage.StorageSubdirectory;
import app.nexusforms.android.utilities.FileUtils;
import app.nexusforms.android.utilities.SQLiteUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import app.nexusforms.android.database.DatabaseConstants;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
@Ignore("`Parameterized` causes problems for Firebase sharding. Probably need to replace this at JUnit level")
public class InstancesDatabaseHelperTest extends SqlLiteHelperTest {
    private String databasePath;

    @Parameterized.Parameter
    public String description;

    /**
     * SQLite file that should contain exactly two instances:
     * - one complete instance with date field set to 1564413556249
     * - one incomplete instance with date field set to 1564413579406
     */
    @Parameterized.Parameter(1)
    public String dbFilename;

    @Before
    public void saveRealDb() {
        databasePath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.METADATA) + File.separator + "instances.db";
        FileUtils.copyFile(new File(databasePath), new File(databasePath + TEMPORARY_EXTENSION));
    }

    @After
    public void restoreRealDb() {
        FileUtils.copyFile(new File(databasePath + TEMPORARY_EXTENSION), new File(databasePath));
        FileUtils.deleteAndReport(new File(databasePath + TEMPORARY_EXTENSION));
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Downgrading from version with extra column drops that column", "instances_v7000_added_fakeColumn.db"},
                {"Downgrading from version with missing column adds that column", "instances_v7000_removed_jrVersion.db"},

                {"Upgrading from version with extra column drops that column", "instances_v3.db"},
                {"Upgrading from version with missing column adds that column", "instances_v4_removed_jrVersion.db"},

                {"Upgrading from v5 results in current version columns", "instances_v5.db"}
        });
    }

    @Test
    public void testMigration() throws IOException {
        app.nexusforms.android.support.FileUtils.copyFileFromAssets("database" + File.separator + dbFilename, databasePath);
        InstancesDatabaseHelper databaseHelper = new InstancesDatabaseHelper(new InstanceDatabaseMigrator(), new StoragePathProvider());
        ensureMigrationAppliesFully(databaseHelper);

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        MatcherAssert.assertThat(db.getVersion(), Matchers.is(DatabaseConstants.INSTANCES_DATABASE_VERSION));

        List<String> newColumnNames = SQLiteUtils.getColumnNames(db, DatabaseConstants.INSTANCES_TABLE_NAME);
        MatcherAssert.assertThat(newColumnNames, Matchers.contains(InstanceDatabaseMigrator.CURRENT_VERSION_COLUMN_NAMES));
        assertThatInstancesAreKeptAfterMigrating();
    }

    private void assertThatInstancesAreKeptAfterMigrating() {
        List<Instance> instances = new DatabaseInstancesRepository().getAll();
        assertEquals(2, instances.size());
        assertEquals("complete", instances.get(0).getStatus());
        assertEquals(Long.valueOf(1564413556249L), instances.get(0).getLastStatusChangeDate());
        assertEquals("incomplete", instances.get(1).getStatus());
        assertEquals(Long.valueOf(1564413579406L), instances.get(1).getLastStatusChangeDate());
    }
}
