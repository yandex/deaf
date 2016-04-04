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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.yandex.subtitles.BuildConfig;
import ru.yandex.subtitles.content.dao.MessagesDAO;
import ru.yandex.subtitles.content.dao.PhrasesDAO;
import ru.yandex.subtitles.content.dao.ThreadsDAO;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yandex.subtitles.db";
    private static final int DATABASE_VERSION = BuildConfig.DATABASE_VERSION;

    private final Context mContext;

    public DatabaseHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        PhrasesDAO.onCreate(mContext, db);
        ThreadsDAO.onCreate(mContext, db);
        MessagesDAO.onCreate(mContext, db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        int toVersion = oldVersion + 1;
        while (toVersion <= newVersion) {
            PhrasesDAO.onUpgrade(mContext, db, toVersion);
            ThreadsDAO.onUpgrade(mContext, db, toVersion);
            MessagesDAO.onUpgrade(mContext, db, toVersion);

            toVersion++;
        }
    }

}