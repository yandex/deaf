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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.utils.IntentUtils;

public class SpeechKitInitializedBroadcastReceiver extends BroadcastReceiver
        implements Subscribeable {

    public interface OnSpeechKitInitializedListener {

        void onSpeechKitInitialized();

    }

    private static final String ACTION_SK_INITIALIZED = "SpeechKitInitializedBroadcastReceiver.ACTION_SK_INITIALIZED";

    private static final IntentFilter INTENT_FILTER = new IntentFilter(ACTION_SK_INITIALIZED);

    public static void onSpeechKitInitialized(final Context context) {
        final Intent intent = IntentUtils.createActionIntent(context,
                SpeechKitInitializedBroadcastReceiver.class, ACTION_SK_INITIALIZED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private final OnSpeechKitInitializedListener mInitializedListener;

    public SpeechKitInitializedBroadcastReceiver(@NonNull final OnSpeechKitInitializedListener initializedListener) {
        super();
        mInitializedListener = initializedListener;
    }

    @Override
    public void subscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, INTENT_FILTER);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = (intent != null ? intent.getAction() : null);
        if (ACTION_SK_INITIALIZED.equals(action)) {
            mInitializedListener.onSpeechKitInitialized();
        }
    }

    @Override
    public void unsubscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

}
