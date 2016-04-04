/**
 * Copyright 2015 YA LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.yandex.subtitles.content.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import ru.yandex.subtitles.content.provider.BaseColumns;
import ru.yandex.subtitles.utils.TextUtilsExt;

/**
 * Class provides read-write access and object-relation mapping to any data in content provider.
 */
public abstract class AbstractIdentifyDAO<K, E extends Identify<K>> extends AbstractReadOnlyDAO<E> {

    private static final String[] COUNT_1_PROJECTION = new String[] { "count(1) as " + BaseColumns._COUNT };
    private static final String WHERE_BASE_COLUMNS_ID = BaseColumns._ID + "=?";

    /**
     * Returns normalized id that can be used in content uris.
     *
     * @param id entity id as is.
     * @return normalized id.
     */
    protected static String normalizeId(@NonNull final String id) {
        return id.replaceAll("/", "");
    }

    public AbstractIdentifyDAO(final Context context) {
        super(context);
    }

    /**
     * Method performs mapping a given entity to {@link ContentValues} and
     * inserts data to content provider.
     *
     * @param entity entity to be inserted in database.
     * @return inserted content uri.
     */
    @Nullable
    public Uri insert(@NonNull final E entity) {
        return insert(getTableUri(), toContentValues(entity));
    }

    /**
     * Method inserts a given {@link ContentValues} to content provider.
     *
     * @param values columns-values map.
     * @return inserted content uri.
     */
    @Nullable
    public Uri insert(@NonNull final ContentValues values) {
        return insert(getTableUri(), values);
    }

    /**
     * Method inserts a given {@link ContentValues} to content provider.
     *
     * @param uri    table content uri.
     * @param values columns-values map.
     * @return inserted content uri.
     */
    @Nullable
    protected Uri insert(@NonNull final Uri uri, @NonNull final ContentValues values) {
        return getContentResolver().insert(uri, values);
    }

    /**
     * Inserts a {@link List} of entities in one transaction.
     *
     * @param entities a non-null {@link List} of entities.
     * @return the number of values that were inserted.
     */
    public int bulkInsert(@NonNull final List<E> entities) {
        return bulkInsert(getTableUri(), entities);
    }

    /**
     * Inserts a {@link List} of entities in one transaction.
     *
     * @param uri      table content uri.
     * @param entities a non-null {@link List} of entities.
     * @return the number of values that were inserted.
     */
    protected int bulkInsert(@NonNull final Uri uri, @NonNull final List<E> entities) {
        final ContentValues[] contentValues = toContentValues(entities);
        return getContentResolver().bulkInsert(uri, contentValues);
    }

    /**
     * Updates a given entity record in the database.
     * Notice that entity should have non-null id to be updated.
     *
     * @param entity a data entity.
     * @return the number of rows affected.
     */
    public int update(@NonNull final E entity) {
        return update(entity.getId(), toContentValues(entity));
    }

    /**
     * Updates a given {@link ContentValues} columns in the database.
     *
     * @param id     an id of entity.
     * @param values columns-values map.
     * @return the number of rows affected.
     */
    protected int update(@NonNull final K id, @NonNull final ContentValues values) {
        final String normalizedId = normalizeId(String.valueOf(id));
        return update(getTableUri(), values, WHERE_BASE_COLUMNS_ID, new String[] { normalizedId });
    }

    /**
     * Updates a set of column/value pair by using given content uri and selection.
     *
     * @param uri           the URI to query. This can potentially have a record ID if this
     *                      is an update request for a specific record.
     * @param values        a set of column_name/value pairs to update in the database.
     *                      This must not be {@code null}.
     * @param selection     an optional filter to match rows to update.
     * @param selectionArgs a list of selection binding arguments.
     * @return the number of rows affected.
     */
    protected int update(@NonNull final Uri uri, @NonNull final ContentValues values,
                         @Nullable final String selection,
                         @Nullable final String[] selectionArgs) {
        return getContentResolver().update(uri, values, selection, selectionArgs);
    }

    /**
     * Updates a given data entity if entity exists in the database, inserts new record otherwise.
     *
     * @param entity a data entity.
     * @return {@code true} if entity was updated or inserted, {@code false} otherwise.
     */
    public boolean insertOrUpdate(@NonNull final E entity) {
        return (update(entity) > 0 || insert(entity) != null);
    }

    /**
     * Updates a given column/value pair if entity with given id exists in the database,
     * inserts new record otherwise.
     *
     * @param id     an entity id.
     * @param values an column/value pair.
     * @return {@code true} if entity was updated or inserted, {@code false} otherwise.
     */
    public boolean insertOrUpdate(@NonNull final K id, @NonNull final ContentValues values) {
        return (update(id, values) > 0 || insert(values) != null);
    }

    /**
     * <p>
     * Implement that method to map your data entity to {@link ContentValues}.
     * </p>
     * <p>Here is the standard idiom for entity mapping:
     * <p/>
     * <pre>
     *   public class MyDataDAO extends AbstractIdentifyDAO<String, MyData> {
     *
     *     ...
     *
     *     protected ContentValues toContentValues(final MyData entity) {
     *         ContentValues values = new ContentValues();
     *         values.put(Columns._ID, entity.getId());
     *         values.put(Columns.INTEGER_COLUMN, entity.getInteger());
     *         values.put(Columns.STRING_COLUMN, entity.getString());
     *         return values;
     *     }
     *
     *   }
     * </pre>
     *
     * @param entity a data entity.
     * @return an column/value pair.
     */
    @NonNull
    protected abstract ContentValues toContentValues(@NonNull final E entity);

    @NonNull
    private ContentValues[] toContentValues(@NonNull final List<E> entities) {
        final int size = entities.size();

        final ContentValues[] contentValues = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            contentValues[i] = toContentValues(entities.get(i));
        }
        return contentValues;
    }

    /**
     * Deletes rows from the database.
     *
     * @param uri           a table content uri.
     * @param selection     an optional filter to match rows to update
     * @param selectionArgs an optional binding arguments.
     * @return the number of rows affected.
     */
    protected int delete(@NonNull final Uri uri,
                         @Nullable final String selection,
                         @Nullable final String[] selectionArgs) {
        return getContentResolver().delete(uri, selection, selectionArgs);
    }

    /**
     * Deletes entity from the database.
     *
     * @param entity a data entity.
     * @return the number of rows affected.
     */
    public int delete(@NonNull final E entity) {
        return delete(entity.getId());
    }

    /**
     * Deletes entity from the database by a given id.
     *
     * @param id an entity id.
     * @return the number of rows affected.
     */
    public int delete(final K id) {
        int deletedRecords = 0;
        final String normalizedId = normalizeId(String.valueOf(id));
        if (!TextUtils.isEmpty(normalizedId)) {
            deletedRecords = delete(getTableUri(), WHERE_BASE_COLUMNS_ID,
                    prepareArguments(normalizedId));
        }
        return deletedRecords;
    }

    /**
     * Return an data entity for a given entity id.
     *
     * @param id an entity id.
     * @return an data entity or {@code null}.
     */
    @Nullable
    public E get(final K id) {
        final String normalizedId = normalizeId(String.valueOf(id));
        final List<E> candidates = get(WHERE_BASE_COLUMNS_ID, prepareArguments(normalizedId), null);
        return (candidates.isEmpty() ? null : candidates.get(0));
    }

    /**
     * Returns total number of rows in the table.
     *
     * @return number of rows in the table.
     */
    public int getCount() {
        return getCount(null);
    }

    /**
     * Returns number of rows with a given id.
     *
     * @param id an entity id.
     * @return number of rows.
     */
    public int getCount(@Nullable final K id) {
        return getCount(id, true);
    }

    /**
     * Return number of rows.
     *
     * @param id an entity id.
     * @param in a boolean flag to indicate which rows should be included to result.
     * @return a number of rows.
     */
    public int getCount(@Nullable final K id, final boolean in) {
        String selection = null;
        String[] selectionArgs = null;
        final String normalizedId = (id == null ? null : normalizeId(String.valueOf(id)));
        if (!TextUtilsExt.isEmpty(normalizedId)) {
            selection = QueryUtils.in(BaseColumns._ID, 1, in);
            selectionArgs = prepareArguments(normalizedId);
        }

        return getCount(selection, selectionArgs);
    }

    /**
     * Return number of rows that match a given selection.
     *
     * @param selection     an optional filter to match rows.
     * @param selectionArgs an optional binding arguments.
     * @return number of rows.
     */
    protected int getCount(@Nullable final String selection, @Nullable final String[] selectionArgs) {
        final Cursor cursor = getContentResolver().query(getTableUri(), COUNT_1_PROJECTION,
                selection, selectionArgs, null);

        int count = 0;
        if (isCursorValid(cursor)) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndex(BaseColumns._COUNT));
            }
            cursor.close();
        }

        return count;
    }

    /**
     * Checks if entity with a given id exists in the database.
     *
     * @param id an entity id.
     * @return {@code true} if entity exists, {@code false} otherwise.
     */
    public boolean exists(final K id) {
        return getCount(id) > 0;
    }

    /**
     * Returns an entity id from a given content uri.
     *
     * @param contentUri an inserted content uri.
     * @return a parsed entity id.
     */
    @Nullable
    public K parseId(@Nullable final Uri contentUri) {
        final String last = (contentUri != null ? contentUri.getLastPathSegment() : null);
        return last == null ? null : parseKey(last);
    }

    /**
     * Implement that method to parse entity key.
     *
     * @param key a String representation of an entity id.
     * @return a parsed entity id.
     */
    protected abstract K parseKey(@NonNull final String key);

}