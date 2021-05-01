package app.nexusforms.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;

import app.nexusforms.android.application.Collect;
import app.nexusforms.android.provider.FormsProviderAPI;
import app.nexusforms.android.forms.Form;
import app.nexusforms.android.forms.FormsRepository;
import app.nexusforms.android.storage.StoragePathProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import static android.provider.BaseColumns._ID;

public class DatabaseFormsRepository implements FormsRepository {

    private final StoragePathProvider storagePathProvider;

    public DatabaseFormsRepository() {
        storagePathProvider = new StoragePathProvider();
    }

    @Nullable
    @Override
    public Form get(Long id) {
        return queryForForm(_ID + "=?", new String[]{id.toString()});
    }

    @Nullable
    @Override
    public Form getLatestByFormIdAndVersion(String jrFormId, @Nullable String jrVersion) {
        List<Form> all = getAllByFormIdAndVersion(jrFormId, jrVersion);
        if (!all.isEmpty()) {
            return all.stream().max(Comparator.comparingLong(Form::getDate)).get();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Form getOneByPath(String path) {
        String selection = FormsProviderAPI.FormsColumns.FORM_FILE_PATH + "=?";
        String[] selectionArgs = {new StoragePathProvider().getRelativeFormPath(path)};
        return queryForForm(selection, selectionArgs);
    }

    @Nullable
    @Override
    public Form getOneByMd5Hash(@NotNull String hash) {
        if (hash == null) {
            throw new IllegalArgumentException("null hash");
        }

        String selection = FormsProviderAPI.FormsColumns.MD5_HASH + "=?";
        String[] selectionArgs = {hash};
        return queryForForm(selection, selectionArgs);
    }

    @Override
    public List<Form> getAll() {
        return queryForForms(null, null);
    }

    @Override
    public List<Form> getAllByFormIdAndVersion(String jrFormId, @Nullable String jrVersion) {
        if (jrVersion != null) {
            return queryForForms(FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND " + FormsProviderAPI.FormsColumns.JR_VERSION + "=?", new String[]{jrFormId, jrVersion});
        } else {
            return queryForForms(FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND " + FormsProviderAPI.FormsColumns.JR_VERSION + " IS NULL", new String[]{jrFormId});
        }
    }

    @Override
    public List<Form> getAllByFormId(String formId) {
        return queryForForms(FormsProviderAPI.FormsColumns.JR_FORM_ID + "=?", new String[]{formId});
    }

    @Override
    public List<Form> getAllNotDeletedByFormId(String jrFormId) {
        return queryForForms(FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND " + FormsProviderAPI.FormsColumns.DELETED_DATE + " IS NULL", new String[]{jrFormId});
    }


    @Override
    public List<Form> getAllNotDeletedByFormIdAndVersion(String jrFormId, @Nullable String jrVersion) {
        if (jrVersion != null) {
            return queryForForms(FormsProviderAPI.FormsColumns.DELETED_DATE + " IS NULL AND " + FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND " + FormsProviderAPI.FormsColumns.JR_VERSION + "=?", new String[]{jrFormId, jrVersion});
        } else {
            return queryForForms(FormsProviderAPI.FormsColumns.DELETED_DATE + " IS NULL AND " + FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND " + FormsProviderAPI.FormsColumns.JR_VERSION + " IS NULL", new String[]{jrFormId});
        }
    }

    @Override
    public Form save(@NotNull Form form) {
        final ContentValues values = getValuesFromFormObject(form, storagePathProvider);

        if (form.isDeleted()) {
            values.put(FormsProviderAPI.FormsColumns.DELETED_DATE, 0L);
        } else {
            values.putNull(FormsProviderAPI.FormsColumns.DELETED_DATE);
        }

        if (form.getId() == null) {
            Uri uri = Collect.getInstance().getContentResolver().insert(FormsProviderAPI.FormsColumns.CONTENT_URI, values);

            try (Cursor cursor = Collect.getInstance().getContentResolver().query(uri, null, null, null, null)) {
                return getFormsFromCursor(cursor, storagePathProvider).get(0);
            }
        } else {
            Collect.getInstance().getContentResolver().update(FormsProviderAPI.FormsColumns.CONTENT_URI, values, _ID + "=?", new String[]{form.getId().toString()});
            return get(form.getId());
        }
    }

    @Override
    public void delete(Long id) {
        String selection = _ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        Collect.getInstance().getContentResolver().delete(FormsProviderAPI.FormsColumns.CONTENT_URI, selection, selectionArgs);
    }

    @Override
    public void softDelete(Long id) {
        ContentValues values = new ContentValues();
        values.put(FormsProviderAPI.FormsColumns.DELETED_DATE, System.currentTimeMillis());
        Collect.getInstance().getContentResolver().update(FormsProviderAPI.FormsColumns.CONTENT_URI, values, _ID + "=?", new String[]{id.toString()});
    }

    @Override
    public void deleteByMd5Hash(@NotNull String md5Hash) {
        String selection = FormsProviderAPI.FormsColumns.MD5_HASH + "=?";
        String[] selectionArgs = {md5Hash};

        Collect.getInstance().getContentResolver().delete(FormsProviderAPI.FormsColumns.CONTENT_URI, selection, selectionArgs);
    }

    @Override
    public void deleteAll() {
        Collect.getInstance().getContentResolver().delete(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null);
    }

    @Override
    public void restore(Long id) {
        ContentValues values = new ContentValues();
        values.putNull(FormsProviderAPI.FormsColumns.DELETED_DATE);
        Collect.getInstance().getContentResolver().update(FormsProviderAPI.FormsColumns.CONTENT_URI, values, _ID + "=?", new String[]{id.toString()});
    }

    @Nullable
    private Form queryForForm(String selection, String[] selectionArgs) {
        List<Form> forms = queryForForms(selection, selectionArgs);
        return !forms.isEmpty() ? forms.get(0) : null;
    }

    private List<Form> queryForForms(String selection, String[] selectionArgs) {
        try (Cursor cursor = Collect.getInstance().getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, selection, selectionArgs, null)) {
            return getFormsFromCursor(cursor, storagePathProvider);
        }
    }

    @NotNull
    public static List<Form> getFormsFromCursor(Cursor cursor, StoragePathProvider storagePathProvider) {
        List<Form> forms = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                Form form = getFormFromCurrentCursorPosition(cursor, storagePathProvider);

                forms.add(form);
            }

        }
        return forms;
    }

    private static Form getFormFromCurrentCursorPosition(Cursor cursor, StoragePathProvider storagePathProvider) {
        int idColumnIndex = cursor.getColumnIndex(_ID);
        int displayNameColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DISPLAY_NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DESCRIPTION);
        int jrFormIdColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID);
        int jrVersionColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION);
        int formFilePathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH);
        int submissionUriColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.SUBMISSION_URI);
        int base64RSAPublicKeyColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY);
        int md5HashColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.MD5_HASH);
        int dateColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DATE);
        int jrCacheFilePathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH);
        int formMediaPathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH);
        int languageColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.LANGUAGE);
        int autoSendColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.AUTO_SEND);
        int autoDeleteColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.AUTO_DELETE);
        int geometryXpathColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.GEOMETRY_XPATH);
        int deletedDateColumnIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.DELETED_DATE);

        return new Form.Builder()
                .id(cursor.getLong(idColumnIndex))
                .displayName(cursor.getString(displayNameColumnIndex))
                .description(cursor.getString(descriptionColumnIndex))
                .jrFormId(cursor.getString(jrFormIdColumnIndex))
                .jrVersion(cursor.getString(jrVersionColumnIndex))
                .formFilePath(storagePathProvider.getAbsoluteFormFilePath(cursor.getString(formFilePathColumnIndex)))
                .submissionUri(cursor.getString(submissionUriColumnIndex))
                .base64RSAPublicKey(cursor.getString(base64RSAPublicKeyColumnIndex))
                .md5Hash(cursor.getString(md5HashColumnIndex))
                .date(cursor.getLong(dateColumnIndex))
                .jrCacheFilePath(storagePathProvider.getAbsoluteCacheFilePath(cursor.getString(jrCacheFilePathColumnIndex)))
                .formMediaPath(storagePathProvider.getAbsoluteFormFilePath(cursor.getString(formMediaPathColumnIndex)))
                .language(cursor.getString(languageColumnIndex))
                .autoSend(cursor.getString(autoSendColumnIndex))
                .autoDelete(cursor.getString(autoDeleteColumnIndex))
                .geometryXpath(cursor.getString(geometryXpathColumnIndex))
                .deleted(!cursor.isNull(deletedDateColumnIndex))
                .build();
    }

    private static ContentValues getValuesFromFormObject(Form form, StoragePathProvider storagePathProvider) {
        ContentValues values = new ContentValues();
        values.put(FormsProviderAPI.FormsColumns.DISPLAY_NAME, form.getDisplayName());
        values.put(FormsProviderAPI.FormsColumns.DESCRIPTION, form.getDescription());
        values.put(FormsProviderAPI.FormsColumns.JR_FORM_ID, form.getJrFormId());
        values.put(FormsProviderAPI.FormsColumns.JR_VERSION, form.getJrVersion());
        values.put(FormsProviderAPI.FormsColumns.FORM_FILE_PATH, storagePathProvider.getRelativeFormPath(form.getFormFilePath()));
        values.put(FormsProviderAPI.FormsColumns.SUBMISSION_URI, form.getSubmissionUri());
        values.put(FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY, form.getBASE64RSAPublicKey());
        values.put(FormsProviderAPI.FormsColumns.MD5_HASH, form.getMD5Hash());
        values.put(FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH, storagePathProvider.getRelativeFormPath(form.getFormMediaPath()));
        values.put(FormsProviderAPI.FormsColumns.LANGUAGE, form.getLanguage());
        values.put(FormsProviderAPI.FormsColumns.AUTO_SEND, form.getAutoSend());
        values.put(FormsProviderAPI.FormsColumns.AUTO_DELETE, form.getAutoDelete());
        values.put(FormsProviderAPI.FormsColumns.GEOMETRY_XPATH, form.getGeometryXpath());

        return values;
    }
}
