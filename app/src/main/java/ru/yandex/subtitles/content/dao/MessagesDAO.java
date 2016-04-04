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
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.yandex.subtitles.content.data.Message;
import ru.yandex.subtitles.content.provider.MessengerContentProvider;
import ru.yandex.subtitles.content.provider.database.Column;
import ru.yandex.subtitles.content.provider.database.Table;

public class MessagesDAO extends AbstractIdentifyDAO<Long, Message> {

    public static final String TABLE = "messages";

    public interface Columns extends BaseColumns {

        String USER_ID = "user_id";
        String THREAD_ID = "chat_id";
        String TEXT = "text";
        String TIME = "time";
        String TIMEZONE = "timezone";
        String PINNED = "pinned";

    }

    public static final Uri CONTENT_URI = MessengerContentProvider.createContentUri(TABLE);

    public static final String[] PROJECTION = {
            Columns._ID,
            Columns.USER_ID,
            Columns.THREAD_ID,
            Columns.TEXT,
            Columns.TIME,
            Columns.TIMEZONE,
            Columns.PINNED,
    };

    public static void onCreate(final Context context, final SQLiteDatabase db) {
        Table.drop(db, TABLE);

        final Table.Builder tableBuilder = new Table.Builder(TABLE);
        tableBuilder.addColumn(new Column.Builder().integer(Columns._ID).primaryKey());
        tableBuilder.addColumn(new Column.Builder().integer(Columns.USER_ID));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.THREAD_ID));
        tableBuilder.addColumn(new Column.Builder().text(Columns.TEXT));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.TIME));
        tableBuilder.addColumn(new Column.Builder().text(Columns.TIMEZONE));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.PINNED).defaultValue(0));
        tableBuilder.create(db);

        Table.createIndex(db, TABLE, Columns._ID, new String[] { Columns._ID });
        Table.createIndex(db, TABLE, Columns.THREAD_ID, new String[] { Columns.THREAD_ID });
    }

    public static void onUpgrade(final Context context, final SQLiteDatabase db, final int toVersion) {
        switch (toVersion) {
            case 6:
                new Table.Alter(TABLE)
                        .addColumn(new Column.Builder()
                                .integer(Columns.PINNED)
                                .defaultValue(0))
                        .execute(db);
                break;
        }
    }

    private static final String WHERE_MESSAGE_ID = Columns._ID + "=?";
    private static final String WHERE_THREAD_ID = Columns.THREAD_ID + "=?";
    private static final String WHERE_THREAD_ID_AND_USER_ID = Columns.THREAD_ID + "=? AND " + Columns.USER_ID + "=?";
    private static final String SORT_BY_TIME_ASC = Columns.TIME + " asc";
    private static final String SORT_BY_TIME_DESC_LIMIT_1 = Columns.TIME + " desc limit 1";

    public MessagesDAO(final Context context) {
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
    protected Message getItemFromCursor(final Cursor cursor) {
        final Message message = new Message();
        message.setId(getLong(cursor, Columns._ID));
        message.setUserId(getLong(cursor, Columns.USER_ID));
        message.setThreadId(getLong(cursor, Columns.THREAD_ID));
        message.setText(getString(cursor, Columns.TEXT));
        message.setTime(getLong(cursor, Columns.TIME));
        message.setTimezone(getString(cursor, Columns.TIMEZONE));
        message.setPinned(getInteger(cursor, Columns.PINNED) == 1);
        return message;
    }

    @NonNull
    @Override
    protected ContentValues toContentValues(@NonNull final Message entity) {
        final ContentValues values = new ContentValues();
        values.put(Columns._ID, entity.getId());
        values.put(Columns.USER_ID, entity.getUserId());
        values.put(Columns.THREAD_ID, entity.getThreadId());
        values.put(Columns.TEXT, entity.getText());
        values.put(Columns.TIME, entity.getTime());
        values.put(Columns.TIMEZONE, entity.getTimezone());
        values.put(Columns.PINNED, entity.isPinned() ? 1 : 0);
        return values;
    }

    @Override
    protected Long parseKey(@NonNull final String key) {
        return Long.parseLong(key);
    }

    public int setPinned(final long messageId, final boolean pinned) {
        final ContentValues values = new ContentValues();
        values.put(Columns.PINNED, pinned ? 1 : 0);
        return update(CONTENT_URI, values, WHERE_MESSAGE_ID, prepareArguments(messageId));
    }

    @NonNull
    public List<Message> getAllByThreadId(final long threadId) {
        return get(WHERE_THREAD_ID, prepareArguments(threadId), SORT_BY_TIME_ASC);
    }

    @NonNull
    public List<Message> getAllByThreadIdAndUser(final long threadId, final long userId) {
        return get(WHERE_THREAD_ID_AND_USER_ID, prepareArguments(threadId, userId), SORT_BY_TIME_ASC);
    }

    public int getCountByThreadId(final long threadId) {
        return getCount(WHERE_THREAD_ID, prepareArguments(threadId));
    }

    @Nullable
    public Message getLastMessageByThreadId(final long threadId) {
        final List<Message> messages = get(WHERE_THREAD_ID,
                prepareArguments(threadId), SORT_BY_TIME_DESC_LIMIT_1);
        return (messages.isEmpty() ? null : messages.get(0));
    }

    public int getCountByThreadIdAndUserId(final long threadId, final long member) {
        return getCount(WHERE_THREAD_ID_AND_USER_ID, prepareArguments(threadId, member));
    }

}
