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
package ru.yandex.subtitles.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;

import ru.yandex.subtitles.utils.AsyncAutoCompleteHandler;

/**
 * LiveLongAndProsperIntentService is a base class for {@link Service}s that handle
 * asynchronous * requests (expressed as {@link Intent}s) on demand.  Clients send
 * requests through {@link android.content.Context#startService(Intent)} calls; the
 * service is started as needed, handles each Intent in turn using a worker
 * thread, and <b>doesn't stop</b> itself when it runs out of work.
 * <p/>
 * <p>This "work queue processor" pattern is commonly used to offload tasks
 * from an application's main thread.  The LiveLongAndProsperIntentService class exists to
 * simplify this pattern and take care of the mechanics.  To use it, extend
 * LiveLongAndProsperIntentService and implement {@link #onHandleIntent(Intent)}.
 * LiveLongAndProsperIntentService will receive the Intents, and launch a worker thread.
 * <p/>
 * <p>All requests are handled on a single worker thread -- they may take as
 * long as necessary (and will not block the application's main loop), but
 * only one request will be processed at a time.
 * <p/>
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For a detailed discussion about how to create services, read the
 * <a href="{@docRoot}guide/topics/fundamentals/services.html">Services</a> developer guide.</p>
 * </div>
 *
 * @see android.os.AsyncTask
 */
public abstract class LiveLongAndProsperIntentService extends Service
        implements AsyncAutoCompleteHandler.OnHandleEventListener<Intent> {

    private final String mName;

    private AsyncAutoCompleteHandler<Intent> mServiceHandler;

    /**
     * Creates an LiveLongAndProsperIntentService. Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public LiveLongAndProsperIntentService(final String name) {
        super();
        mName = name;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceHandler = new AsyncAutoCompleteHandler<Intent>("LiveLongAndProsperIntentService [" + mName + "]");
        mServiceHandler.setOnHandleEventListener(this);
    }

    @SuppressWarnings("deprecated")
    @Override
    public final void onStart(final Intent intent, final int startId) {
        mServiceHandler.post(intent);
    }

    /**
     * You should not override this method for your LiveLongAndProsperIntentService. Instead,
     * override {@link #onHandleIntent}, which the system calls when the
     * LiveLongAndProsperIntentService receives a start request.
     *
     * @see Service#onStartCommand
     */
    @Override
    public final int onStartCommand(final Intent intent, final int flags, final int startId) {
        onStart(intent, startId);
        return START_STICKY;
    }

    /**
     * Unless you provide binding for your service, you don't need to implement this
     * method, because the default implementation returns null.
     *
     * @see Service#onBind
     */
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    /**
     * Callback method for async auto complete queue
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(Intent)}.
     */
    @Override
    public final void onHandleEvent(final Intent intent) {
        if (intent != null) {
            onHandleIntent(intent);
        }
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same LiveLongAndProsperIntentService, but it will not hold up anything else.
     * When all requests have been handled, the LiveLongAndProsperIntentService
     * will not stop itself, so you can use it to observer data changes.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(Intent)}.
     */
    protected abstract void onHandleIntent(@NonNull final Intent intent);

}
