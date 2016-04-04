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

import android.support.annotation.NonNull;

public class QueryUtils {

    @NonNull
    public static String in(final String column, final int argsNumber) {
        return in(column, argsNumber, true);
    }

    @NonNull
    public static String notIn(final String column, final int argsNumber) {
        return in(column, argsNumber, false);
    }

    @NonNull
    public static String in(final String column, final int argsNumber, final boolean in) {
        final StringBuilder inClause = new StringBuilder();

        if (argsNumber > 0) {
            inClause.append(column)
                    .append(in ? " " : " not ")
                    .append("in (");
            for (int i = 0; i < argsNumber; i++) {
                if (i > 0) {
                    inClause.append(", ");
                }
                inClause.append("?");
            }
            inClause.append(")");
        }

        return inClause.toString();
    }

    private QueryUtils() {
    }

}
