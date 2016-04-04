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
package ru.yandex.subtitles.content.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ru.yandex.subtitles.BuildConfig;
import ru.yandex.subtitles.content.dao.MessagesDAO;
import ru.yandex.subtitles.content.dao.PhrasesDAO;
import ru.yandex.subtitles.content.dao.ThreadsDAO;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class MessengerContentProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".contentprovider";
    public static final String SCHEME = "content";

    @NonNull
    public static Uri createContentUri(final String table) {
        return Uri.parse(SCHEME + "://" + AUTHORITY + "/" + table);
    }

    private static final int MATCH_DIR = 1;
    private static final int MATCH_ID = 2;

    public static final String WHERE_ID = BaseColumns._ID + "=?";

    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private DatabaseHelper mDatabaseHelper;

    @Override
    public void attachInfo(final Context context, final ProviderInfo info) {
        super.attachInfo(context, info);

        final String authority = info.authority;
        addUri(authority, PhrasesDAO.TABLE, true);
        addUri(authority, ThreadsDAO.TABLE, true);
        addUri(authority, MessagesDAO.TABLE, true);
    }

    public void addUri(final String authority, final String table, final boolean hasChild) {
        mUriMatcher.addURI(authority, table, MATCH_DIR);
        if (hasChild) {
            mUriMatcher.addURI(authority, table + "/*", MATCH_ID);
        }
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        mDatabaseHelper = new DatabaseHelper(context);
        return true;
    }

    @Override
    public String getType(@NonNull final Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_DIR:
                return "vnd.android.cursor.dir/vnd." + uri.getAuthority() + "." + getTableFromUri(uri);

            case MATCH_ID:
                return "vnd.android.cursor.dir/item." + uri.getAuthority() + "." + getTableFromUri(uri);

            default:
                throw new SQLiteException("Unknown uri: " + uri);
        }
    }

    private String getTableFromUri(@NonNull final Uri uri) {
        return uri.getPathSegments().get(0);
    }

    @Override
    public Cursor query(@NonNull final Uri uri, final String[] projection, final String where,
                        final String[] whereArgs, final String sort) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_DIR:
                return withNotificationUri(uri, query(getTableFromUri(uri), projection, where, whereArgs, sort));

            case MATCH_ID:
                return withNotificationUri(uri, queryById(getTableFromUri(uri), projection, uri.getLastPathSegment()));

            default:
                throw new SQLiteException("Unknown uri: " + uri);
        }
    }

    private Cursor query(final String table, final String[] projection, final String where,
                         final String[] whereArgs, final String sort) {
        return mDatabaseHelper.getReadableDatabase()
                .query(table, projection, where, whereArgs, null, null, sort);
    }

    private Cursor queryById(final String table, final String[] projection, final String id) {
        return mDatabaseHelper.getReadableDatabase()
                .query(table, projection, WHERE_ID, new String[] { id },
                        null, null, null);
    }

    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_DIR:
                return notifyInsert(uri, insert(getTableFromUri(uri), values));

            case MATCH_ID:
                notifyChange(uriWithoutId(uri), updateById(getTableFromUri(uri), uri.getLastPathSegment(), values));
                return uri;

            default:
                throw new SQLiteException("Unknown uri: " + uri);
        }
    }

    @NonNull
    private String insert(final String table, final ContentValues values) {
        final String uid = values.getAsString(BaseColumns._ID);
        final long rowId = mDatabaseHelper.getWritableDatabase()
                .insert(table, null, values);
        if (rowId <= 0) {
            throw new SQLiteException("Failed to insert row into table '" + table + "'");
        }
        return TextUtilsExt.isEmpty(uid) ? Long.toString(rowId) : uid;
    }

    @Override
    public int bulkInsert(@NonNull final Uri uri, @NonNull final ContentValues[] values) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_DIR:
                return notifyChange(uri, bulkInsert(getTableFromUri(uri), values));

            case MATCH_ID:
            default:
                throw new SQLiteException("Unknown uri: " + uri);
        }
    }

    private int bulkInsert(final String table, final ContentValues[] bulkValues) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (final ContentValues values : bulkValues) {
                final String id = values.getAsString(BaseColumns._ID);
                if (TextUtils.isEmpty(id) || updateById(table, id, values) <= 0) {
                    insert(table, values);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return bulkValues.length;
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues values,
                      final String where, final String[] whereArgs) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_DIR:
                return notifyChange(uri, update(getTableFromUri(uri), values, where, whereArgs));

            case MATCH_ID:
                return notifyChange(uri, updateById(getTableFromUri(uri), uri.getLastPathSegment(), values));

            default:
                throw new SQLiteException("Unknown uri: " + uri);
        }
    }

    private int update(final String table, final ContentValues values,
                       final String where, final String[] whereArgs) {
        return mDatabaseHelper.getWritableDatabase()
                .update(table, values, where, whereArgs);
    }

    private int updateById(final String table, final String id, final ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int affectedRows = db.update(table, values, WHERE_ID, new String[] { id });
        if (affectedRows < 1) {
            if (db.insert(table, null, values) > 0) {
                ++affectedRows;
            }
        }
        return affectedRows;
    }

    @Override
    public int delete(@NonNull final Uri uri, final String where, final String[] whereArgs) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_DIR:
                return notifyChange(uri, delete(getTableFromUri(uri), where, whereArgs));

            case MATCH_ID:
                return notifyChange(uri, deleteById(getTableFromUri(uri), uri.getLastPathSegment()));

            default:
                throw new SQLiteException("Unknown uri: " + uri);
        }
    }

    private int delete(final String table, final String where, final String[] whereArgs) {
        return mDatabaseHelper.getWritableDatabase().delete(table, where, whereArgs);
    }

    private int deleteById(final String table, final String id) {
        return mDatabaseHelper.getWritableDatabase().delete(table, WHERE_ID, new String[] { id });
    }

    @SuppressWarnings("all")
    private Cursor withNotificationUri(final Uri uri, final Cursor cursor) {
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @SuppressWarnings("all")
    private Uri notifyInsert(final Uri uri, @NonNull final String uid) {
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, uid);
    }

    @SuppressWarnings("all")
    private int notifyChange(final Uri uri, final int affectedRows) {
        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return affectedRows;
    }

    private Uri uriWithoutId(@NonNull final Uri uri) {
        return Uri.parse(uri.getScheme() + "://" + uri.getAuthority() + "/" + getTableFromUri(uri));
    }

}

