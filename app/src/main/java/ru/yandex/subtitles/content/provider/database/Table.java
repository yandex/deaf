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
package ru.yandex.subtitles.content.provider.database;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.utils.TextUtilsExt;

public class Table {

    private static final String INDEX_SFX = "_idx";

    public static class Builder {

        private final String mName;
        private List<Column.Builder> mColumns;

        public Builder(final String name) {
            mName = name;
            mColumns = new ArrayList<Column.Builder>();
        }

        public Builder addColumn(final Column.Builder columnBuilder) {
            mColumns.add(columnBuilder);
            return this;
        }

        public void create(final SQLiteDatabase db) {
            final List<String> columnDefinitions = new ArrayList<String>();
            for (final Column.Builder column : mColumns) {
                columnDefinitions.add(column.build());
            }

            final String sql = "CREATE TABLE " + mName + "(" +
                    TextUtilsExt.join(", ", columnDefinitions) + ");";
            db.execSQL(sql);
        }

    }

    public static class Alter {

        private final String mName;
        private final List<Column.Builder> mAddedColumns = new ArrayList<Column.Builder>();

        public Alter(@NonNull final String name) {
            mName = name;
        }

        public Alter addColumn(@NonNull final Column.Builder columnBuilder) {
            mAddedColumns.add(columnBuilder);
            return this;
        }

        public void execute(final SQLiteDatabase db) {
            db.beginTransaction();
            try {
                for (final Column.Builder column : mAddedColumns) {
                    db.execSQL("ALTER TABLE " + mName + " ADD COLUMN " + column.build() + ";");
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

    }

    public static void createIndex(final SQLiteDatabase db, final String table,
                                   final String name, final String[] onColumns) {
        final String sql = "CREATE INDEX IF NOT EXISTS " + name + INDEX_SFX + " ON " + table +
                "(" + TextUtilsExt.join(", ", onColumns) + ");";
        db.execSQL(sql);
    }

    public static void drop(final SQLiteDatabase db, final String table) {
        final String sql = "DROP TABLE IF EXISTS " + table + ";";
        db.execSQL(sql);
    }

    private Table() {
    }

}
