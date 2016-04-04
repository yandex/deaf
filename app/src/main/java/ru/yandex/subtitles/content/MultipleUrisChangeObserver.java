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
package ru.yandex.subtitles.content;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

public class MultipleUrisChangeObserver extends ContentObserver implements Subscribeable {

    private final Loader<?> mLoader;
    private final List<Uri> mUris;

    public MultipleUrisChangeObserver(final Loader<?> loader) {
        super(new Handler());
        mLoader = loader;
        mUris = new ArrayList<Uri>();
    }

    public void add(@NonNull final Uri contentUri) {
        mUris.add(contentUri);
    }

    @Override
    public void subscribe(final Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        for (final Uri uri : mUris) {
            contentResolver.registerContentObserver(uri, true, this);
        }
    }

    @Override
    public void onChange(final boolean selfChange) {
        super.onChange(selfChange);
        mLoader.onContentChanged();
    }

    @Override
    public void unsubscribe(final Context context) {
        context.getContentResolver().unregisterContentObserver(this);
    }

}
