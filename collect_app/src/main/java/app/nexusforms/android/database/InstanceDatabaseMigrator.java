package app.nexusforms.android.database;

import android.database.sqlite.SQLiteDatabase;

import app.nexusforms.android.instances.Instance;
import app.nexusforms.android.provider.InstanceProviderAPI;
import app.nexusforms.android.utilities.SQLiteUtils;

import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;

public class InstanceDatabaseMigrator implements DatabaseMigrator {
    private static final String[] COLUMN_NAMES_V5 = {_ID, InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE,
            InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, InstanceProviderAPI.InstanceColumns.JR_FORM_ID, InstanceProviderAPI.InstanceColumns.JR_VERSION, InstanceProviderAPI.InstanceColumns.STATUS, InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE, InstanceProviderAPI.InstanceColumns.DELETED_DATE};

    private static final String[] COLUMN_NAMES_V6 = {_ID, InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, InstanceProviderAPI.InstanceColumns.SUBMISSION_URI,
            InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE, InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, InstanceProviderAPI.InstanceColumns.JR_FORM_ID, InstanceProviderAPI.InstanceColumns.JR_VERSION, InstanceProviderAPI.InstanceColumns.STATUS,
            InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE, InstanceProviderAPI.InstanceColumns.DELETED_DATE, InstanceProviderAPI.InstanceColumns.GEOMETRY, InstanceProviderAPI.InstanceColumns.GEOMETRY_TYPE};

    public static final String[] CURRENT_VERSION_COLUMN_NAMES = COLUMN_NAMES_V6;

    public void onCreate(SQLiteDatabase db) {
        createInstancesTableV5(db, DatabaseConstants.INSTANCES_TABLE_NAME);
        upgradeToVersion6(db, DatabaseConstants.INSTANCES_TABLE_NAME);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    public void onUpgrade(SQLiteDatabase db, int oldVersion) {
        switch (oldVersion) {
            case 1:
                upgradeToVersion2(db);
            case 2:
                upgradeToVersion3(db);
            case 3:
                upgradeToVersion4(db);
            case 4:
                upgradeToVersion5(db);
            case 5:
                upgradeToVersion6(db, DatabaseConstants.INSTANCES_TABLE_NAME);
                break;
            default:
                Timber.i("Unknown version %d", oldVersion);
        }
    }

    public void onDowngrade(SQLiteDatabase db) {
        String temporaryTableName = DatabaseConstants.INSTANCES_TABLE_NAME + "_tmp";
        createInstancesTableV5(db, temporaryTableName);
        upgradeToVersion6(db, temporaryTableName);

        dropObsoleteColumns(db, CURRENT_VERSION_COLUMN_NAMES, temporaryTableName);
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        if (!SQLiteUtils.doesColumnExist(db, DatabaseConstants.INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE)) {
            SQLiteUtils.addColumn(db, DatabaseConstants.INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE, "text");

            db.execSQL("UPDATE " + DatabaseConstants.INSTANCES_TABLE_NAME + " SET "
                    + InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " = '" + true
                    + "' WHERE " + InstanceProviderAPI.InstanceColumns.STATUS + " IS NOT NULL AND "
                    + InstanceProviderAPI.InstanceColumns.STATUS + " != '" + Instance.STATUS_INCOMPLETE
                    + "'");
        }
    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, DatabaseConstants.INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.JR_VERSION, "text");
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, DatabaseConstants.INSTANCES_TABLE_NAME, InstanceProviderAPI.InstanceColumns.DELETED_DATE, "date");
    }

    /**
     * Upgrade to version 5. Prior versions of the instances table included a {@code displaySubtext}
     * column which was redundant with the {@link InstanceProviderAPI.InstanceColumns#STATUS} and
     * {@link InstanceProviderAPI.InstanceColumns#LAST_STATUS_CHANGE_DATE} columns and included
     * unlocalized text. Version 5 removes this column.
     */
    private void upgradeToVersion5(SQLiteDatabase db) {
        String temporaryTableName = DatabaseConstants.INSTANCES_TABLE_NAME + "_tmp";

        // onDowngrade in Collect v1.22 always failed to clean up the temporary table so remove it now.
        // Going from v1.23 to v1.22 and back to v1.23 will result in instance status information
        // being lost.
        SQLiteUtils.dropTable(db, temporaryTableName);

        createInstancesTableV5(db, temporaryTableName);
        dropObsoleteColumns(db, COLUMN_NAMES_V5, temporaryTableName);
    }

    /**
     * Use the existing temporary table with the provided name to only keep the given relevant
     * columns, dropping all others.
     *
     * NOTE: the temporary table with the name provided is dropped.
     *
     * The move and copy strategy is used to overcome the fact that SQLITE does not directly support
     * removing a column. See https://sqlite.org/lang_altertable.html
     *
     * @param db                    the database to operate on
     * @param relevantColumns       the columns relevant to the current version
     * @param temporaryTableName    the name of the temporary table to use and then drop
     */
    private void dropObsoleteColumns(SQLiteDatabase db, String[] relevantColumns, String temporaryTableName) {
        List<String> columns = SQLiteUtils.getColumnNames(db, DatabaseConstants.INSTANCES_TABLE_NAME);
        columns.retainAll(Arrays.asList(relevantColumns));
        String[] columnsToKeep = columns.toArray(new String[0]);

        SQLiteUtils.copyRows(db, DatabaseConstants.INSTANCES_TABLE_NAME, columnsToKeep, temporaryTableName);
        SQLiteUtils.dropTable(db, DatabaseConstants.INSTANCES_TABLE_NAME);
        SQLiteUtils.renameTable(db, temporaryTableName, DatabaseConstants.INSTANCES_TABLE_NAME);
    }

    private void upgradeToVersion6(SQLiteDatabase db, String name) {
        SQLiteUtils.addColumn(db, name, InstanceProviderAPI.InstanceColumns.GEOMETRY, "text");
        SQLiteUtils.addColumn(db, name, InstanceProviderAPI.InstanceColumns.GEOMETRY_TYPE, "text");
    }

    private void createInstancesTableV5(SQLiteDatabase db, String name) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + name + " ("
                + _ID + " integer primary key, "
                + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " text not null, "
                + InstanceProviderAPI.InstanceColumns.SUBMISSION_URI + " text, "
                + InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE + " text, "
                + InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " text not null, "
                + InstanceProviderAPI.InstanceColumns.JR_FORM_ID + " text not null, "
                + InstanceProviderAPI.InstanceColumns.JR_VERSION + " text, "
                + InstanceProviderAPI.InstanceColumns.STATUS + " text not null, "
                + InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " date not null, "
                + InstanceProviderAPI.InstanceColumns.DELETED_DATE + " date );");
    }
}
