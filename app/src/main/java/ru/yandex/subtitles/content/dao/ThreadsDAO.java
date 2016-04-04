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
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.yandex.subtitles.content.data.Thread;
import ru.yandex.subtitles.content.provider.BaseColumns;
import ru.yandex.subtitles.content.provider.MessengerContentProvider;
import ru.yandex.subtitles.content.provider.database.Column;
import ru.yandex.subtitles.content.provider.database.Table;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class ThreadsDAO extends AbstractIdentifyDAO<Long, Thread> {

    public static final String TABLE = "threads";

    public interface Columns extends BaseColumns {

        String DELETED = "deleted";
        String PINNED = "pinned";
        String LAST_OPENING_TIME = "last_opening_time";
        String OPENING_COUNT = "opening_count";
        String PINNED_MESSAGE_COUNT = "pinned_message_count";

    }

    public static final Uri CONTENT_URI = MessengerContentProvider.createContentUri(TABLE);

    public static final String[] PROJECTION = {
            Columns._ID,
            Columns.DELETED,
            Columns.PINNED,
            Columns.LAST_OPENING_TIME,
            Columns.OPENING_COUNT,
            Columns.PINNED_MESSAGE_COUNT
    };

    public static void onCreate(final Context context, final SQLiteDatabase db) {
        final Table.Builder tableBuilder = new Table.Builder(TABLE);
        tableBuilder.addColumn(new Column.Builder().integer(Columns._ID).primaryKey());
        tableBuilder.addColumn(new Column.Builder().integer(Columns.DELETED).defaultValue(0));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.PINNED).defaultValue(0));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.LAST_OPENING_TIME).defaultValue(0));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.OPENING_COUNT).defaultValue(0));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.PINNED_MESSAGE_COUNT).defaultValue(0));
        tableBuilder.create(db);

        Table.createIndex(db, TABLE, Columns._ID, new String[] { Columns._ID });
    }

    public static void onUpgrade(final Context context, final SQLiteDatabase db, final int toVersion) {
        switch (toVersion) {
            case 7:
                new Table.Alter("chats")
                        .addColumn(new Column.Builder().integer(Columns.DELETED).defaultValue(0))
                        .addColumn(new Column.Builder().integer(Columns.PINNED).defaultValue(0))
                        .execute(db);
                break;

            case 10:
                new Table.Alter("chats")
                        .addColumn(new Column.Builder().integer(Columns.LAST_OPENING_TIME).defaultValue(0))
                        .addColumn(new Column.Builder().integer(Columns.OPENING_COUNT).defaultValue(0))
                        .addColumn(new Column.Builder().integer(Columns.PINNED_MESSAGE_COUNT).defaultValue(0))
                        .execute(db);
                break;

            case 12:
                onCreate(context, db);

                final String columns = TextUtilsExt.join(", ", Columns._ID, Columns.DELETED,
                        Columns.PINNED, Columns.LAST_OPENING_TIME,
                        Columns.OPENING_COUNT, Columns.PINNED_MESSAGE_COUNT);
                db.execSQL("insert into " + TABLE + " (" + columns + ") select " + columns + " from chats;");

                Table.drop(db, "chats");
                break;
        }
    }

    private static final String WHERE_THREAD_ID = Columns._ID + "=?";
    private static final String WHERE_DELETED = Columns.DELETED + "=?";

    public ThreadsDAO(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected Uri getTableUri() {
        return CONTENT_URI;
    }

    @Nullable
    @Override
    protected String[] getProjection() {
        return PROJECTION;
    }

    @NonNull
    @Override
    protected Thread getItemFromCursor(final Cursor cursor) {
        final Thread thread = new Thread();
        thread.setId(getLong(cursor, Columns._ID));
        thread.setDeleted(getInteger(cursor, Columns.DELETED) == 1);
        thread.setPinned(getInteger(cursor, Columns.PINNED) == 1);
        thread.setLastOpeningTime(getLong(cursor, Columns.LAST_OPENING_TIME));
        thread.setOpeningCount(getInteger(cursor, Columns.OPENING_COUNT));
        thread.setPinnedMessageCount(getInteger(cursor, Columns.PINNED_MESSAGE_COUNT));
        return thread;
    }

    @NonNull
    @Override
    protected ContentValues toContentValues(@NonNull final Thread entity) {
        final ContentValues values = new ContentValues();
        values.put(Columns._ID, entity.getId());
        values.put(Columns.DELETED, entity.isDeleted() ? 1 : 0);
        values.put(Columns.PINNED, entity.isPinned() ? 1 : 0);
        values.put(Columns.LAST_OPENING_TIME, entity.getLastOpeningTime());
        values.put(Columns.OPENING_COUNT, entity.getOpeningCount());
        values.put(Columns.PINNED_MESSAGE_COUNT, entity.getPinnedMessageCount());
        return values;
    }

    @Override
    protected Long parseKey(@NonNull final String key) {
        return Long.parseLong(key);
    }

    @NonNull
    public List<Thread> getAllExcludeDeleted() {
        return get(WHERE_DELETED, prepareArguments(0), null);
    }

    public int setPinned(final long threadId, final boolean pinned) {
        final ContentValues values = new ContentValues();
        values.put(Columns.PINNED, pinned ? 1 : 0);
        return update(CONTENT_URI, values, WHERE_THREAD_ID, prepareArguments(threadId));
    }

    public int setDeleted(final long threadId, final boolean deleted) {
        final ContentValues values = new ContentValues();
        values.put(Columns.DELETED, deleted ? 1 : 0);
        return update(CONTENT_URI, values, WHERE_THREAD_ID, prepareArguments(threadId));
    }

    public int getCountExcludeDeleted() {
        return getCount(WHERE_DELETED, prepareArguments(0));
    }

    public boolean setPinnedMessageCount(final long threadId, final int count) {
        final ContentValues values = new ContentValues(2);
        values.put(Columns._ID, threadId);
        values.put(Columns.PINNED_MESSAGE_COUNT, count);
        return insertOrUpdate(threadId, values);
    }

    public boolean setOpeningTimeAndCount(final long threadId, final long lastOpeningTime,
                                          final int openingCount) {
        final ContentValues values = new ContentValues(3);
        values.put(Columns._ID, threadId);
        values.put(Columns.LAST_OPENING_TIME, lastOpeningTime);
        values.put(Columns.OPENING_COUNT, openingCount);
        return insertOrUpdate(threadId, values);
    }

}
