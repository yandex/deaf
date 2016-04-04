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
package ru.yandex.subtitles.service.speechkit.initializer;

import android.content.Context;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Initializer;
import ru.yandex.speechkit.InitializerListener;
import ru.yandex.speechkit.SpeechKit;
import ru.yandex.subtitles.BuildConfig;
import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.utils.NetworkBroadcastReceiver;
import ru.yandex.subtitles.utils.NetworkUtils;

public class SpeechKitInitializer implements InitializerListener,
        NetworkBroadcastReceiver.OnNetworkStateChangeListener {

    private static final String LOG_TAG = "SpeechKitInitializer";

    /**
     * Replace API_KEY with your unique API key. Please, read official documentation how to obtain one:
     * https://tech.yandex.com/speechkit/mobilesdk/
     */
    private static final String API_KEY = "API_KEY";
    private static final boolean DEBUG_ENABLED = BuildConfig.DEBUG;

    private WeakReference<InitializerCallbacks> mWeakInitializerCallbacks;

    private final Context mContext;
    private Subscribeable mNetworkBroadcastReceiver = null;

    public SpeechKitInitializer(final Context context, @NonNull final InitializerCallbacks callbacks) {
        mContext = context;
        setInitializerCallbacks(callbacks);
    }

    public void start() {
        final SpeechKit speechKit = SpeechKit.getInstance();
        speechKit.configure(mContext, API_KEY);

        if (DEBUG_ENABLED) {
            speechKit.setParameter(SpeechKit.Parameters.disableAntimat, "false");
        }

        initializeSpeechKit();
    }

    private void initializeSpeechKit() {
        final Initializer initializer = Initializer.create(this);
        initializer.start();
    }

    @Override
    public void onInitializerBegin(final Initializer initializer) {
        if (DEBUG_ENABLED) {
            Log.d(LOG_TAG, "SpeechKit initialization begin.");
        }
        notifyInitializerBegin(initializer);
    }

    @Override
    public void onInitializerDone(final Initializer initializer) {
        if (DEBUG_ENABLED) {
            Log.d(LOG_TAG, "SpeechKit initialization done.");
        }
        notifyInitializerDone(initializer);
    }

    @Override
    public void onError(final Initializer initializer, final Error error) {
        Log.e(LOG_TAG, "SpeechKit initialization error: " + error.getString());
        handleInitializationError(error);
        notifyInitializerError(initializer, error);
    }

    private void handleInitializationError(final Error error) {
        final int errorCode = error.getCode();
        if (errorCode == Error.ERROR_NETWORK) {
            // Listen to network changes to initialize speech kit when network will be connected
            mNetworkBroadcastReceiver = new NetworkBroadcastReceiver(this, true);
            mNetworkBroadcastReceiver.subscribe(mContext);
        }
    }

    @Override
    public void onNetworkStateChanged(@Nullable final NetworkInfo networkInfo) {
        if (NetworkUtils.isNetworkConnected(networkInfo)) {
            // Stop listen to network changes and try to initialize speechkit
            mNetworkBroadcastReceiver.unsubscribe(mContext);
            mNetworkBroadcastReceiver = null;

            // Once initialized it will handle connection errors by himself
            initializeSpeechKit();
        }
    }

    public void setInitializerCallbacks(@NonNull final InitializerCallbacks callbacks) {
        mWeakInitializerCallbacks = new WeakReference<InitializerCallbacks>(callbacks);
    }

    @Nullable
    private InitializerCallbacks getInitializerCallbacks() {
        return (mWeakInitializerCallbacks == null ? null : mWeakInitializerCallbacks.get());
    }

    private void notifyInitializerBegin(final Initializer initializer) {
        final InitializerCallbacks initializerCallbacks = getInitializerCallbacks();
        if (initializerCallbacks != null) {
            initializerCallbacks.onSpeechKitInitializerBegin(initializer);
        }
    }

    private void notifyInitializerDone(final Initializer initializer) {
        final InitializerCallbacks initializerCallbacks = getInitializerCallbacks();
        if (initializerCallbacks != null) {
            initializerCallbacks.onSpeechKitInitialized(initializer);
        }
    }

    private void notifyInitializerError(final Initializer initializer, final Error error) {
        final InitializerCallbacks initializerCallbacks = getInitializerCallbacks();
        if (initializerCallbacks != null) {
            initializerCallbacks.onSpeechKitInitializerError(initializer, error);
        }
    }

}