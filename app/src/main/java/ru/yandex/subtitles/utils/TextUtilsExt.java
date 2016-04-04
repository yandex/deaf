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
package ru.yandex.subtitles.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Locale;

public final class TextUtilsExt {

    public static boolean isEmpty(@Nullable final CharSequence cs) {
        return TextUtils.isEmpty(cs);
    }

    public static boolean equals(@Nullable final CharSequence a, @Nullable final CharSequence b) {
        return TextUtils.equals(a, b);
    }

    public static boolean equalsIgnoreCase(final CharSequence cs1, final CharSequence cs2) {
        final String str1 = (cs1 == null ? null : cs1.toString());
        final String str2 = (cs2 == null ? null : cs2.toString());
        return equalsIgnoreCase(str1, str2);
    }

    public static boolean equalsIgnoreCase(final String str1, final String str2) {
        return (str1 == null ? str2 == null : str1.equalsIgnoreCase(str2));
    }

    @Nullable
    public static String toLowerCase(@Nullable final String s) {
        return (s == null ? null : s.toLowerCase(Locale.getDefault()));
    }

    @Nullable
    public static String toUpperCase(@Nullable final String s) {
        return (s == null ? null : s.toUpperCase(Locale.getDefault()));
    }

    @NonNull
    public static String join(final CharSequence delimiter, @NonNull final Object... tokens) {
        return TextUtils.join(delimiter, tokens);
    }

    @NonNull
    public static String join(final CharSequence delimiter, @NonNull final Iterable tokens) {
        return TextUtils.join(delimiter, tokens);
    }

    @NonNull
    public static String safeSubString(final String source, final int start, final int end) {
        final String subString;
        if (source == null || start > end || source.length() < start) {
            subString = "";
        } else if (source.length() < end) {
            subString = source.substring(start, source.length());
        } else {
            subString = source.substring(start, end);
        }
        return subString;
    }

    public static void putTextToClipboard(final Context context, final String text) {
        final ClipboardManager clipboard = ApplicationUtils.getSystemService(context, Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(null, text);
        clipboard.setPrimaryClip(clip);
    }

    public static boolean contains(@Nullable final String str, @Nullable final String part) {
        return !(isEmpty(str) || isEmpty(part)) && str.contains(part);
    }

    private TextUtilsExt() {
    }

}
