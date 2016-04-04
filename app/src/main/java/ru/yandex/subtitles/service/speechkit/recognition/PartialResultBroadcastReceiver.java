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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.utils.IntentUtils;

public class PartialResultBroadcastReceiver extends BroadcastReceiver implements Subscribeable {

    public interface OnPartialResultListener {

        void onPartialResult(@Nullable final String partialResult);

    }

    private static final String ACTION_PARTIAL_RESULT = "PartialResultBroadcastReceiver.ACTION_PARTIAL_RESULT";
    private static final IntentFilter INTENT_FILTER = new IntentFilter(ACTION_PARTIAL_RESULT);

    private static final String EXTRA_THREAD_ID = "thread_id";

    public static void onPartialResult(final Context context, final long threadId, @Nullable final String partialResult) {
        final Intent intent = IntentUtils.createActionIntent(context,
                PartialResultBroadcastReceiver.class, ACTION_PARTIAL_RESULT);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(Intent.EXTRA_TEXT, partialResult);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private final long mThreadId;
    private final OnPartialResultListener mPartialResultListener;

    public PartialResultBroadcastReceiver(final long threadId,
                                          @NonNull final OnPartialResultListener partialResultListener) {
        super();
        mThreadId = threadId;
        mPartialResultListener = partialResultListener;
    }

    @Override
    public void subscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, INTENT_FILTER);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = (intent != null ? intent.getAction() : null);
        final long threadId = (intent != null ? intent.getLongExtra(EXTRA_THREAD_ID, -1) : -1);
        if (mThreadId == threadId && ACTION_PARTIAL_RESULT.equals(action)) {
            mPartialResultListener.onPartialResult(intent.getStringExtra(Intent.EXTRA_TEXT));
        }
    }

    @Override
    public void unsubscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

}
