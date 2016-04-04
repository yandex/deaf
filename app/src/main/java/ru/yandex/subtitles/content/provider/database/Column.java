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

public class Column {

    private static final String COLUMN_TYPE_REAL = "REAL";
    private static final String COLUMN_TYPE_INTEGER = "INTEGER";
    private static final String COLUMN_TYPE_BLOB = "BLOB";
    private static final String COLUMN_TYPE_TEXT = "TEXT";

    public static class Builder {

        private String mName;
        private String mType;
        private boolean mIsPrimary = false;
        private boolean mNotNull = false;
        private Object mDefault = null;

        public Builder() {
        }

        public Builder text(final String name) {
            mName = name;
            mType = COLUMN_TYPE_TEXT;
            return this;
        }

        public Builder integer(final String name) {
            mName = name;
            mType = COLUMN_TYPE_INTEGER;
            return this;
        }

        public Builder real(final String name) {
            mName = name;
            mType = COLUMN_TYPE_REAL;
            return this;
        }

        public Builder blob(final String name) {
            mName = name;
            mType = COLUMN_TYPE_BLOB;
            return this;
        }

        public Builder primaryKey() {
            mIsPrimary = true;
            mNotNull = false;
            return this;
        }

        public Builder notNull() {
            if (!mIsPrimary) {
                mNotNull = true;
            }
            return this;
        }

        public Builder defaultValue(final Object def) {
            mDefault = def;
            return this;
        }

        /* package */ String build() {
            final StringBuilder sb = new StringBuilder();
            sb.append(mName).append(" ");
            sb.append(mType);
            if (mIsPrimary) {
                sb.append(" PRIMARY KEY");
            }
            if (mNotNull) {
                sb.append(" NOT NULL");
            }
            if (mDefault != null) {
                sb.append(" DEFAULT ");
                if (COLUMN_TYPE_TEXT.equals(mType)) {
                    sb.append("\"").append(mDefault).append("\"");
                } else {
                    sb.append(mDefault);
                }
            }
            return sb.toString();
        }

    }

    private Column() {
    }

}
