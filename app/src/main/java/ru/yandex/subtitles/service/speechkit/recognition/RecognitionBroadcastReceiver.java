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
package ru.yandex.subtitles.service.speechkit.recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import ru.yandex.speechkit.Error;
import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.utils.IntentUtils;

public class RecognitionBroadcastReceiver extends BroadcastReceiver implements Subscribeable {

    public interface RecognitionLifecycleCallbacks {

        void onRecognitionStarted(final boolean resumedAfterPlaying);

        void onPowerUpdate(final float power);

        void onError(final Error error);

        void onRecognitionDone(final boolean willStartPlaying);

    }

    private static final String ACTION_RECOGNITION_STARTED = "RecognitionBroadcastReceiver.ACTION_RECOGNITION_STARTED";
    private static final String ACTION_POWER_UPDATE = "RecognitionBroadcastReceiver.ACTION_POWER_UPDATE";
    private static final String ACTION_ERROR = "RecognitionBroadcastReceiver.ACTION_ERROR";
    private static final String ACTION_RECOGNITION_DONE = "RecognitionBroadcastReceiver.ACTION_RECOGNITION_DONE";

    private static final IntentFilter INTENT_FILTER = new IntentFilter();

    static {
        INTENT_FILTER.addAction(ACTION_RECOGNITION_STARTED);
        INTENT_FILTER.addAction(ACTION_POWER_UPDATE);
        INTENT_FILTER.addAction(ACTION_ERROR);
        INTENT_FILTER.addAction(ACTION_RECOGNITION_DONE);
    }

    private static final String EXTRA_THREAD_ID = "thread_id";
    private static final String EXTRA_POWER = "power";
    private static final String EXTRA_ERROR = "error";
    private static final String EXTRA_WILL_START_PLAYING = "will_start_playing";
    private static final String EXTRA_RESUMED_AFTER_PLAYING = "resumed_after_playing";

    public static void onRecognitionStarted(final Context context, final long threadId,
                                            final boolean resumedAfterPlaying) {
        final Intent intent = IntentUtils.createActionIntent(context,
                RecognitionBroadcastReceiver.class, ACTION_RECOGNITION_STARTED);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_RESUMED_AFTER_PLAYING, resumedAfterPlaying);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void onPowerUpdate(final Context context, final long threadId, final float power) {
        final Intent intent = IntentUtils.createActionIntent(context,
                RecognitionBroadcastReceiver.class, ACTION_POWER_UPDATE);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_POWER, power);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void onError(final Context context, final long threadId, final Error error) {
        final Intent intent = IntentUtils.createActionIntent(context,
                RecognitionBroadcastReceiver.class, ACTION_ERROR);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_ERROR, error.getCode());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void onRecognitionDone(final Context context, final long threadId,
                                         final boolean willStartPlaying) {
        final Intent intent = IntentUtils.createActionIntent(context,
                RecognitionBroadcastReceiver.class, ACTION_RECOGNITION_DONE);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_WILL_START_PLAYING, willStartPlaying);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private final long mThreadId;
    private final RecognitionLifecycleCallbacks mCallbacks;

    public RecognitionBroadcastReceiver(final long threadId,
                                        @NonNull final RecognitionLifecycleCallbacks callbacks) {
        super();
        mThreadId = threadId;
        mCallbacks = callbacks;
    }

    @Override
    public void subscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, INTENT_FILTER);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = (intent != null ? intent.getAction() : null);
        final long threadId = (intent != null ? intent.getLongExtra(EXTRA_THREAD_ID, -1) : -1);
        if (mThreadId == threadId) {
            if (ACTION_RECOGNITION_STARTED.equals(action)) {
                final boolean resumedAfterPlaying = intent.getBooleanExtra(EXTRA_RESUMED_AFTER_PLAYING, false);
                mCallbacks.onRecognitionStarted(resumedAfterPlaying);

            } else if (ACTION_POWER_UPDATE.equals(action)) {
                mCallbacks.onPowerUpdate(intent.getFloatExtra(EXTRA_POWER, 0.f));

            } else if (ACTION_ERROR.equals(action)) {
                mCallbacks.onError(Error.fromCode(intent.getIntExtra(EXTRA_ERROR, Error.ERROR_UNKNOWN)));

            } else if (ACTION_RECOGNITION_DONE.equals(action)) {
                final boolean willStartPlaying = intent.getBooleanExtra(EXTRA_WILL_START_PLAYING, false);
                mCallbacks.onRecognitionDone(willStartPlaying);

            }
        }
    }

    @Override
    public void unsubscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

}
