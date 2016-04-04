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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class provides read-only access to any data in content provider.
 * Implementation of object-relation mapping should be provided by child.
 */
public abstract class AbstractReadOnlyDAO<E> {

    public static boolean isCursorValid(final Cursor cursor) {
        return (cursor != null);
    }

    /**
     * Utility method for mapping binding arguments to string array.
     *
     * @param args binding arguments.
     * @return a non-null String array with prepared binding arguments.
     */
    @NonNull
    public static String[] prepareArguments(@Nullable final Object... args) {
        String[] params = { };
        if (args != null) {
            params = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                params[i] = String.valueOf(args[i]);
            }
        }
        return params;
    }

    /**
     * Returns the non-null value of the requested column as a String.
     * Notice that cursor should be moved to an absolute position.
     *
     * @param c      cursor.
     * @param column the name of the target column.
     * @return the non-null value of that column as a String.
     * @throws IllegalStateException when value is null.
     */
    @NonNull
    public static String getString(final Cursor c, final String column) {
        final String nullableString = getNullableString(c, column);
        if (nullableString == null) {
            throw new IllegalStateException("Column " + column + " should not be null");
        }
        return nullableString;
    }

    /**
     * Returns the nullable value of the requested column as a String.
     * Notice that cursor should be moved to an absolute position.
     *
     * @param c      cursor.
     * @param column the name of the target column.
     * @return the nullable value of that column as a String.
     */
    @Nullable
    public static String getNullableString(final Cursor c, final String column) {
        final int columnIndex = c.getColumnIndex(column);
        return c.getString(columnIndex);
    }

    /**
     * Returns the value of the requested column as an int.
     * Notice that cursor should be moved to an absolute position.
     *
     * @param c      cursor.
     * @param column the name of the target column.
     * @return the value of that column as an int.
     */
    public static int getInteger(final Cursor c, final String column) {
        final int columnIndex = c.getColumnIndex(column);
        return c.getInt(columnIndex);
    }

    /**
     * Returns the nullable value of the requested column as an Integer.
     * Notice that cursor should be moved to an absolute position.
     *
     * @param c      cursor.
     * @param column the name of the target column.
     * @return the nullable value of that column as an Integer.
     */
    @Nullable
    public static Integer getNullableInteger(final Cursor c, final String column) {
        final int columnIndex = c.getColumnIndex(column);
        Integer integer = null;
        if (!c.isNull(columnIndex)) {
            integer = c.getInt(columnIndex);
        }
        return integer;
    }

    /**
     * Returns the value of the requested column as a long.
     * Notice that cursor should be moved to an absolute position.
     *
     * @param c      cursor.
     * @param column the name of the target column.
     * @return the value of that column as a long.
     */
    public static long getLong(final Cursor c, final String column) {
        final int columnIndex = c.getColumnIndex(column);
        return c.getLong(columnIndex);
    }

    /**
     * Returns the nullable value of the requested column as a Long.
     * Notice that cursor should be moved to an absolute position.
     *
     * @param c      cursor.
     * @param column the name of the target column.
     * @return the nullable value of that column as a Long.
     */
    @Nullable
    public static Long getNullableLong(final Cursor c, final String column) {
        final int columnIndex = c.getColumnIndex(column);
        Long l = null;
        if (!c.isNull(columnIndex)) {
            l = c.getLong(columnIndex);
        }
        return l;
    }

    /**
     * Returns the value of the requested column as a double.
     * Notice that cursor should be moved to an absolute position.
     *
     * @param c      cursor.
     * @param column the name of the target column.
     * @return the value of that column as a double.
     */
    public static double getDouble(final Cursor c, final String column) {
        final int columnIndex = c.getColumnIndex(column);
        return c.getDouble(columnIndex);
    }

    /**
     * Returns the nullable value of the requested column as a Double.
     * Notice that cursor should be moved to an absolute position.
     *
     * @param c      cursor.
     * @param column the name of the target column.
     * @return the nullable value of that column as a Double.
     */
    @Nullable
    public static Double getNullableDouble(final Cursor c, final String column) {
        final int columnIndex = c.getColumnIndex(column);
        Double d = null;
        if (!c.isNull(columnIndex)) {
            d = c.getDouble(columnIndex);
        }
        return d;
    }

    private final Context mContext;
    private final ContentResolver mContentResolver;

    protected AbstractReadOnlyDAO(final Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public Context getContext() {
        return mContext;
    }

    public ContentResolver getContentResolver() {
        return mContentResolver;
    }

    /**
     * Query the given content uri, returning a non-null {@link List} of entities.
     *
     * @return a non-null {@link List} of data entities.
     */
    @NonNull
    public List<E> getAll() {
        return get(null, null, null);
    }

    /**
     * Query the given content uri, returning a non-null {@link List} of entities.
     *
     * @param selection     A filter declaring which rows to return, formatted as an
     *                      SQL WHERE clause (excluding the WHERE itself). Passing null
     *                      will return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be
     *                      replaced by the values from selectionArgs, in order that they
     *                      appear in the selection. The values will be bound as Strings.
     * @param sortOrder     How to order the rows, formatted as an SQL ORDER BY clause
     *                      (excluding the ORDER BY itself). Passing null will use the
     *                      default sort order, which may be unordered.
     * @return a non-null {@link List} of data entities.
     */
    @NonNull
    protected List<E> get(@Nullable final String selection, @Nullable final String[] selectionArgs,
                          @Nullable final String sortOrder) {
        final Cursor cursor = mContentResolver.query(getTableUri(), getProjection(),
                selection, selectionArgs, sortOrder);
        return getItemsFromCursor(cursor);
    }

    /**
     * Implement this method to provide content uri.
     *
     * @return a non-null content uri
     */
    @NonNull
    protected abstract Uri getTableUri();

    /**
     * Implement this method to provide a list of which columns to return during query.
     *
     * @return a nullable array of column names.
     */
    @Nullable
    protected abstract String[] getProjection();

    @NonNull
    private List<E> getItemsFromCursor(final Cursor cursor) {
        final List<E> items = new ArrayList<E>();
        if (isCursorValid(cursor)) {
            if (cursor.moveToFirst()) {
                do {
                    items.add(getItemFromCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return items;
    }

    /**
     * <p>
     * Implement this method to provide cursor-to-entity mapping.
     * </p>
     * <p>Here is the standard idiom for entity mapping:
     * <p/>
     * <pre>
     *   public class MyDataDAO extends AbstractReadOnlyDAO<MyData> {
     *
     *     ...
     *
     *     protected MyData getItemFromCursor(final Cursor cursor) {
     *         MyData myData = new MyData();
     *         myData.setInteger(getInteger(cursor, Columns.INTEGER_COLUMN));
     *         myData.setString(getString(cursor, Columns.STRING_COLUMN));
     *         return myData;
     *     }
     *
     *   }
     * </pre>
     *
     * @param cursor cursor
     * @return non-null data entity.
     */
    @NonNull
    protected abstract E getItemFromCursor(final Cursor cursor);

}
