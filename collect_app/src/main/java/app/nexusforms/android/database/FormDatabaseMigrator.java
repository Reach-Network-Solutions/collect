package app.nexusforms.android.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import app.nexusforms.android.provider.FormsProviderAPI;
import app.nexusforms.android.utilities.SQLiteUtils;

import static android.provider.BaseColumns._ID;
import static app.nexusforms.android.database.DatabaseConstants.FORMS_TABLE_NAME;

public class FormDatabaseMigrator implements DatabaseMigrator {

    private static final String[] COLUMN_NAMES_V7 = {_ID, FormsProviderAPI.FormsColumns.DISPLAY_NAME, FormsProviderAPI.FormsColumns.DESCRIPTION,
            FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.JR_VERSION, FormsProviderAPI.FormsColumns.MD5_HASH, FormsProviderAPI.FormsColumns.DATE, FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FormsProviderAPI.FormsColumns.LANGUAGE,
            FormsProviderAPI.FormsColumns.SUBMISSION_URI, FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY, FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH, FormsProviderAPI.FormsColumns.AUTO_SEND, FormsProviderAPI.FormsColumns.AUTO_DELETE,
            "lastDetectedFormVersionHash"};

    // These exist in database versions 2 and 3, but not in 4...
    private static final String TEMP_FORMS_TABLE_NAME = "forms_v4";
    private static final String MODEL_VERSION = "modelVersion";

    public void onCreate(SQLiteDatabase db) {
        createFormsTableV10(db);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    public void onUpgrade(SQLiteDatabase db, int oldVersion) throws SQLException {
        switch (oldVersion) {
            case 1:
                upgradeToVersion2(db);
            case 2:
            case 3:
                upgradeToVersion4(db, oldVersion);
            case 4:
                upgradeToVersion5(db);
            case 5:
                upgradeToVersion6(db);
            case 6:
                upgradeToVersion7(db);
            case 7:
                upgradeToVersion8(db);
            case 8:
                upgradeToVersion9(db);
            case 9:
                upgradeToVersion10(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db) throws SQLException {
        SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
        createFormsTableV10(db);
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
        onCreate(db);
    }

    private void upgradeToVersion4(SQLiteDatabase db, int oldVersion) {
        // adding BASE64_RSA_PUBLIC_KEY and changing type and name of
        // integer MODEL_VERSION to text VERSION
        SQLiteUtils.dropTable(db, TEMP_FORMS_TABLE_NAME);
        createFormsTableV4(db, TEMP_FORMS_TABLE_NAME);
        db.execSQL("INSERT INTO "
                + TEMP_FORMS_TABLE_NAME
                + " ("
                + _ID
                + ", "
                + FormsProviderAPI.FormsColumns.DISPLAY_NAME
                + ", "
                + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                + ", "
                + FormsProviderAPI.FormsColumns.DESCRIPTION
                + ", "
                + FormsProviderAPI.FormsColumns.JR_FORM_ID
                + ", "
                + FormsProviderAPI.FormsColumns.MD5_HASH
                + ", "
                + FormsProviderAPI.FormsColumns.DATE
                + ", " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH
                + ", "
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH
                + ", "
                + FormsProviderAPI.FormsColumns.LANGUAGE
                + ", "
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI
                + ", "
                + FormsProviderAPI.FormsColumns.JR_VERSION
                + ", "
                + ((oldVersion != 3) ? ""
                : (FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "))
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH
                + ") SELECT "
                + _ID
                + ", "
                + FormsProviderAPI.FormsColumns.DISPLAY_NAME
                + ", "
                + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                + ", "
                + FormsProviderAPI.FormsColumns.DESCRIPTION
                + ", "
                + FormsProviderAPI.FormsColumns.JR_FORM_ID
                + ", "
                + FormsProviderAPI.FormsColumns.MD5_HASH
                + ", "
                + FormsProviderAPI.FormsColumns.DATE
                + ", " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH
                + ", "
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH
                + ", "
                + FormsProviderAPI.FormsColumns.LANGUAGE
                + ", "
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI
                + ", "
                + "CASE WHEN "
                + MODEL_VERSION
                + " IS NOT NULL THEN "
                + "CAST("
                + MODEL_VERSION
                + " AS TEXT) ELSE NULL END, "
                + ((oldVersion != 3) ? ""
                : (FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "))
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " FROM "
                + FORMS_TABLE_NAME);

        // risky failures here...
        SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
        createFormsTableV4(db, FORMS_TABLE_NAME);
        db.execSQL("INSERT INTO "
                + FORMS_TABLE_NAME
                + " ("
                + _ID
                + ", "
                + FormsProviderAPI.FormsColumns.DISPLAY_NAME
                + ", "
                + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                + ", "
                + FormsProviderAPI.FormsColumns.DESCRIPTION
                + ", "
                + FormsProviderAPI.FormsColumns.JR_FORM_ID
                + ", "
                + FormsProviderAPI.FormsColumns.MD5_HASH
                + ", "
                + FormsProviderAPI.FormsColumns.DATE
                + ", " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + ", "
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + ", "
                + FormsProviderAPI.FormsColumns.LANGUAGE + ", "
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI + ", "
                + FormsProviderAPI.FormsColumns.JR_VERSION + ", "
                + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + ") SELECT "
                + _ID + ", "
                + FormsProviderAPI.FormsColumns.DISPLAY_NAME
                + ", "
                + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT
                + ", "
                + FormsProviderAPI.FormsColumns.DESCRIPTION
                + ", "
                + FormsProviderAPI.FormsColumns.JR_FORM_ID
                + ", "
                + FormsProviderAPI.FormsColumns.MD5_HASH
                + ", "
                + FormsProviderAPI.FormsColumns.DATE
                + ", " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + ", "
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + ", "
                + FormsProviderAPI.FormsColumns.LANGUAGE + ", "
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI + ", "
                + FormsProviderAPI.FormsColumns.JR_VERSION + ", "
                + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + ", "
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " FROM "
                + TEMP_FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, TEMP_FORMS_TABLE_NAME);
    }

    private void upgradeToVersion5(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.AUTO_SEND, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.AUTO_DELETE, "text");
    }

    private void upgradeToVersion6(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, "lastDetectedFormVersionHash", "text");
    }

    private void upgradeToVersion7(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, FORMS_TABLE_NAME, temporaryTable);
        createFormsTableV7(db);
        SQLiteUtils.copyRows(db, temporaryTable, COLUMN_NAMES_V7, FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void upgradeToVersion8(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, FormsProviderAPI.FormsColumns.GEOMETRY_XPATH, "text");
    }

    private void upgradeToVersion9(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, FORMS_TABLE_NAME, temporaryTable);
        createFormsTableV9(db);
        SQLiteUtils.copyRows(db, temporaryTable, new String[]{_ID, FormsProviderAPI.FormsColumns.DISPLAY_NAME, FormsProviderAPI.FormsColumns.DESCRIPTION,
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.JR_VERSION, FormsProviderAPI.FormsColumns.MD5_HASH, FormsProviderAPI.FormsColumns.DATE, FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FormsProviderAPI.FormsColumns.LANGUAGE,
                FormsProviderAPI.FormsColumns.SUBMISSION_URI, FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY, FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH, FormsProviderAPI.FormsColumns.AUTO_SEND, FormsProviderAPI.FormsColumns.AUTO_DELETE,
                FormsProviderAPI.FormsColumns.GEOMETRY_XPATH}, FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void upgradeToVersion10(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, FORMS_TABLE_NAME, temporaryTable);
        createFormsTableV10(db);
        SQLiteUtils.copyRows(db, temporaryTable, new String[]{_ID, FormsProviderAPI.FormsColumns.DISPLAY_NAME, FormsProviderAPI.FormsColumns.DESCRIPTION,
                FormsProviderAPI.FormsColumns.JR_FORM_ID, FormsProviderAPI.FormsColumns.JR_VERSION, FormsProviderAPI.FormsColumns.MD5_HASH, FormsProviderAPI.FormsColumns.DATE, FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, FormsProviderAPI.FormsColumns.FORM_FILE_PATH, FormsProviderAPI.FormsColumns.LANGUAGE,
                FormsProviderAPI.FormsColumns.SUBMISSION_URI, FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY, FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH, FormsProviderAPI.FormsColumns.AUTO_SEND, FormsProviderAPI.FormsColumns.AUTO_DELETE,
                FormsProviderAPI.FormsColumns.GEOMETRY_XPATH}, FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void createFormsTableV4(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + _ID + " integer primary key, "
                + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " text not null, "
                + FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT + " text not null, "
                + FormsProviderAPI.FormsColumns.DESCRIPTION + " text, "
                + FormsProviderAPI.FormsColumns.JR_FORM_ID + " text not null, "
                + FormsProviderAPI.FormsColumns.JR_VERSION + " text, "
                + FormsProviderAPI.FormsColumns.MD5_HASH + " text not null, "
                + FormsProviderAPI.FormsColumns.DATE + " integer not null, " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.LANGUAGE + " text, "
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI + " text, "
                + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + " text, "
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.AUTO_SEND + " text, "
                + FormsProviderAPI.FormsColumns.AUTO_DELETE + " text, "
                + "lastDetectedFormVersionHash" + " text);");
    }

    private void createFormsTableV7(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " text not null, "
                + FormsProviderAPI.FormsColumns.DESCRIPTION + " text, "
                + FormsProviderAPI.FormsColumns.JR_FORM_ID + " text not null, "
                + FormsProviderAPI.FormsColumns.JR_VERSION + " text, "
                + FormsProviderAPI.FormsColumns.MD5_HASH + " text not null, "
                + FormsProviderAPI.FormsColumns.DATE + " integer not null, " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.LANGUAGE + " text, "
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI + " text, "
                + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + " text, "
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.AUTO_SEND + " text, "
                + FormsProviderAPI.FormsColumns.AUTO_DELETE + " text, "
                + "lastDetectedFormVersionHash" + " text);");
    }

    private void createFormsTableV9(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " text not null, "
                + FormsProviderAPI.FormsColumns.DESCRIPTION + " text, "
                + FormsProviderAPI.FormsColumns.JR_FORM_ID + " text not null, "
                + FormsProviderAPI.FormsColumns.JR_VERSION + " text, "
                + FormsProviderAPI.FormsColumns.MD5_HASH + " text not null, "
                + FormsProviderAPI.FormsColumns.DATE + " integer not null, " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.LANGUAGE + " text, "
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI + " text, "
                + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + " text, "
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.AUTO_SEND + " text, "
                + FormsProviderAPI.FormsColumns.AUTO_DELETE + " text, "
                + FormsProviderAPI.FormsColumns.GEOMETRY_XPATH + " text, "
                + "deleted" + " boolean default(0));");
    }

    private void createFormsTableV10(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + FormsProviderAPI.FormsColumns.DISPLAY_NAME + " text not null, "
                + FormsProviderAPI.FormsColumns.DESCRIPTION + " text, "
                + FormsProviderAPI.FormsColumns.JR_FORM_ID + " text not null, "
                + FormsProviderAPI.FormsColumns.JR_VERSION + " text, "
                + FormsProviderAPI.FormsColumns.MD5_HASH + " text not null, "
                + FormsProviderAPI.FormsColumns.DATE + " integer not null, " // milliseconds
                + FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.FORM_FILE_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.LANGUAGE + " text, "
                + FormsProviderAPI.FormsColumns.SUBMISSION_URI + " text, "
                + FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY + " text, "
                + FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH + " text not null, "
                + FormsProviderAPI.FormsColumns.AUTO_SEND + " text, "
                + FormsProviderAPI.FormsColumns.AUTO_DELETE + " text, "
                + FormsProviderAPI.FormsColumns.GEOMETRY_XPATH + " text, "
                + FormsProviderAPI.FormsColumns.DELETED_DATE + " integer);");
    }
}
