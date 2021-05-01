package app.nexusforms.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import app.nexusforms.android.application.Collect;
import app.nexusforms.android.instances.Instance;
import app.nexusforms.android.instances.InstancesRepository;
import app.nexusforms.android.provider.InstanceProvider;
import app.nexusforms.android.provider.InstanceProviderAPI;
import app.nexusforms.android.storage.StoragePathProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Mediates between {@link Instance} objects and the underlying SQLite database that stores them.
 * <p>
 * Uses {@link InstancesDao} to perform database queries. {@link InstancesDao} provides a thin
 * convenience layer over {@link InstanceProvider} which exposes
 * {@link Cursor} and {@link androidx.loader.content.CursorLoader} objects that need to be managed.
 * This can be advantageous when providing data to Android components (e.g. lists through adapters)
 * but is cumbersome in domain code and makes writing test implementations harder.
 * <p>
 * Over time, we should consider redefining the responsibility split between
 * {@link InstanceProvider}, {@link InstancesRepository} and
 * {@link InstancesDao}.
 */
public final class DatabaseInstancesRepository implements InstancesRepository {

    @Override
    public Instance get(Long databaseId) {
        String selection = InstanceProviderAPI.InstanceColumns._ID + "=?";
        String[] selectionArgs = {Long.toString(databaseId)};

        Cursor c = getInstancesCursor(selection, selectionArgs);
        List<Instance> result = getInstancesFromCursor(c);
        return !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public Instance getOneByPath(String instancePath) {
        Cursor c = getInstancesCursor(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?", new String[]{new StoragePathProvider().getRelativeInstancePath(instancePath)});
        List<Instance> instances = getInstancesFromCursor(c);
        if (instances.size() == 1) {
            return instances.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Instance> getAll() {
        return getInstancesFromCursor(getInstancesCursor(null, null));
    }

    @Override
    public List<Instance> getAllNotDeleted() {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL ";
        return getInstancesFromCursor(getInstancesCursor(selection, null));
    }

    @Override
    public List<Instance> getAllByStatus(String... status) {
        Cursor instancesCursor = getCursorForAllByStatus(status);
        return getInstancesFromCursor(instancesCursor);
    }

    @Override
    public int getCountByStatus(String... status) {
        return getCursorForAllByStatus(status).getCount();
    }


    @Override
    public List<Instance> getAllByFormId(String formId) {
        Cursor c = getInstancesCursor(InstanceProviderAPI.InstanceColumns.JR_FORM_ID + " = ?", new String[]{formId});
        return getInstancesFromCursor(c);
    }

    @Override
    public List<Instance> getAllNotDeletedByFormIdAndVersion(String jrFormId, String jrVersion) {
        if (jrVersion != null) {
            return getInstancesFromCursor(getInstancesCursor(InstanceProviderAPI.InstanceColumns.JR_FORM_ID + " = ? AND " + InstanceProviderAPI.InstanceColumns.JR_VERSION + " = ? AND " + InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL", new String[]{jrFormId, jrVersion}));
        } else {
            return getInstancesFromCursor(getInstancesCursor(InstanceProviderAPI.InstanceColumns.JR_FORM_ID + " = ? AND " + InstanceProviderAPI.InstanceColumns.JR_VERSION + " IS NULL AND " + InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL", new String[]{jrFormId}));
        }
    }

    @Override
    public void delete(Long id) {
        Uri uri = Uri.withAppendedPath(InstanceProviderAPI.InstanceColumns.CONTENT_URI, id.toString());
        Collect.getInstance().getContentResolver().delete(uri, null, null);
    }

    @Override
    public void deleteAll() {
        Collect.getInstance().getContentResolver().delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null);
    }

    @Override
    public Instance save(Instance instance) {
        if (instance.getStatus() == null) {
            instance = new Instance.Builder(instance)
                    .status(Instance.STATUS_INCOMPLETE)
                    .build();
        }

        if (instance.getLastStatusChangeDate() == null) {
            instance = new Instance.Builder(instance)
                    .lastStatusChangeDate(System.currentTimeMillis())
                    .build();
        }

        Long instanceId = instance.getId();
        ContentValues values = getValuesFromInstanceObject(instance);

        if (instanceId == null) {
            Uri uri = Collect.getInstance().getContentResolver().insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
            Cursor cursor = Collect.getInstance().getContentResolver().query(uri, null, null, null, null);
            return getInstancesFromCursor(cursor).get(0);
        } else {
            Collect.getInstance().getContentResolver().update(
                    InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                    values,
                    InstanceProviderAPI.InstanceColumns._ID + "=?",
                    new String[]{instanceId.toString()}
            );

            return get(instanceId);
        }
    }

    @Override
    public void softDelete(Long id) {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.DELETED_DATE, System.currentTimeMillis());

        Collect.getInstance().getContentResolver().update(
                InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                values,
                InstanceProviderAPI.InstanceColumns._ID + "=?",
                new String[]{id.toString()}
        );
    }

    private Cursor getCursorForAllByStatus(String[] status) {
        StringBuilder selection = new StringBuilder(InstanceProviderAPI.InstanceColumns.STATUS + "=?");
        for (int i = 1; i < status.length; i++) {
            selection.append(" or ").append(InstanceProviderAPI.InstanceColumns.STATUS).append("=?");
        }

        return getInstancesCursor(selection.toString(), status);
    }

    private Cursor getInstancesCursor(String selection, String[] selectionArgs) {
        return Collect.getInstance().getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, selection, selectionArgs, null);
    }

    private static ContentValues getValuesFromInstanceObject(Instance instance) {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, instance.getDisplayName());
        values.put(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, instance.getSubmissionUri());
        values.put(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE, Boolean.toString(instance.canEditWhenComplete()));
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, instance.getInstanceFilePath());
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, instance.getJrFormId());
        values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, instance.getJrVersion());
        values.put(InstanceProviderAPI.InstanceColumns.STATUS, instance.getStatus());
        values.put(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE, instance.getLastStatusChangeDate());
        values.put(InstanceProviderAPI.InstanceColumns.DELETED_DATE, instance.getDeletedDate());
        values.put(InstanceProviderAPI.InstanceColumns.GEOMETRY, instance.getGeometry());
        values.put(InstanceProviderAPI.InstanceColumns.GEOMETRY_TYPE, instance.getGeometryType());
        return values;
    }

    public static List<Instance> getInstancesFromCursor(Cursor cursor) {
        List<Instance> instances = new ArrayList<>();
        if (cursor != null) {
            try {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int displayNameColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME);
                    int submissionUriColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI);
                    int canEditWhenCompleteIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE);
                    int instanceFilePathIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
                    int jrFormIdColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID);
                    int jrVersionColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION);
                    int statusColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS);
                    int lastStatusChangeDateColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE);
                    int deletedDateColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DELETED_DATE);
                    int geometryTypeColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.GEOMETRY_TYPE);
                    int geometryColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.GEOMETRY);

                    int databaseIdIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID);

                    Instance instance = new Instance.Builder()
                            .displayName(cursor.getString(displayNameColumnIndex))
                            .submissionUri(cursor.getString(submissionUriColumnIndex))
                            .canEditWhenComplete(Boolean.valueOf(cursor.getString(canEditWhenCompleteIndex)))
                            .instanceFilePath(cursor.getString(instanceFilePathIndex))
                            .jrFormId(cursor.getString(jrFormIdColumnIndex))
                            .jrVersion(cursor.getString(jrVersionColumnIndex))
                            .status(cursor.getString(statusColumnIndex))
                            .lastStatusChangeDate(cursor.getLong(lastStatusChangeDateColumnIndex))
                            .deletedDate(cursor.isNull(deletedDateColumnIndex) ? null : cursor.getLong(deletedDateColumnIndex))
                            .geometryType(cursor.getString(geometryTypeColumnIndex))
                            .geometry(cursor.getString(geometryColumnIndex))
                            .id(cursor.getLong(databaseIdIndex))
                            .build();

                    instances.add(instance);
                }
            } finally {
                cursor.close();
            }
        }
        return instances;
    }
}
