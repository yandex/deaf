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

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.util.Log;

/**
 * Registerable content observer that triggers data loading when a provided {@link Uri} is notified.
 */
public class ContentProviderChangeObserver extends ContentObserver
        implements Subscribeable {

    private static final String LOG_TAG = "ContentChangeObserver";

    private final Loader<?> mLoader;
    private final Uri mUri;

    public ContentProviderChangeObserver(final Loader<?> loader, final Uri uri) {
        super(new Handler());
        mLoader = loader;
        mUri = uri;
    }

    @Override
    public void subscribe(final Context context) {
        Log.i(LOG_TAG, "Register content observer on uri=" + mUri);
        context.getContentResolver().registerContentObserver(mUri, true, this);
    }

    @Override
    public void unsubscribe(final Context context) {
        Log.i(LOG_TAG, "Unregister content observer on uri=" + mUri);
        context.getContentResolver().unregisterContentObserver(this);
    }

    @Override
    public void onChange(final boolean selfChange) {
        Log.i(LOG_TAG, "Uri=" + mUri + " has been changed.");
        super.onChange(selfChange);
        mLoader.onContentChanged();
    }

}
