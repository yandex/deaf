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
package ru.yandex.subtitles.content.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.content.AsyncTaskLoader;

import ru.yandex.subtitles.content.Subscribeable;

/**
 * A loader that returns an object entity or {@link java.util.List} of entities.
 * This class performs the data query on a background thread
 * so that it does not block the application's UI.
 */
public abstract class AbstractLoader<E> extends AsyncTaskLoader<E> {

    private E mData;

    private Subscribeable mObserver;

    public AbstractLoader(final Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (mObserver == null) {
            mObserver = createContentObserver();
            mObserver.subscribe(getContext());
        }

        if (mData == null || takeContentChanged()) {
            forceLoad();
        }
    }

    @NonNull
    private Subscribeable createContentObserver() {
        Subscribeable observer = onCreateContentObserver();
        if (observer == null) {
            observer = new DummyObserver();
        }
        return observer;
    }

    /**
     * Implement this method to provide {@link Subscribeable} that will
     * listen to the data changes.
     *
     * @return implementation of data change observer.
     */
    @Nullable
    protected abstract Subscribeable onCreateContentObserver();

    /**
     * {@inheritDoc}
     */
    @WorkerThread
    @Override
    public abstract E loadInBackground();

    @Override
    public void deliverResult(final E data) {
        if (data != null) {
            mData = data;
        }

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        if (mObserver != null) {
            mObserver.unsubscribe(getContext());
            mObserver = null;
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    private static final class DummyObserver implements Subscribeable {

        public void subscribe(final Context context) {
        }

        public void unsubscribe(final Context context) {
        }

    }

}
