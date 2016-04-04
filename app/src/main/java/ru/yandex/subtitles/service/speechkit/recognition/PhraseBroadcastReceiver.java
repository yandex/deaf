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

import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.utils.IntentUtils;

public class PhraseBroadcastReceiver extends BroadcastReceiver implements Subscribeable {

    public interface OnPhraseRecognizedListener {

        void onPhraseRecognized(@NonNull final String phrase);

    }

    private static final String ACTION_PHRASE_RECOGNIZED = "PhraseBroadcastReceiver.ACTION_PHRASE_RECOGNIZED";

    private static final IntentFilter INTENT_FILTER = new IntentFilter();
    static {
        INTENT_FILTER.addAction(ACTION_PHRASE_RECOGNIZED);
    }

    private static final String EXTRA_THREAD = "thread_id";

    public static void onPhraseRecognized(final Context context, final long threadId,
                                          @NonNull final String phrase) {
        final Intent intent = IntentUtils.createActionIntent(context,
                PhraseBroadcastReceiver.class, ACTION_PHRASE_RECOGNIZED);
        intent.putExtra(EXTRA_THREAD, threadId);
        intent.putExtra(Intent.EXTRA_TEXT, phrase);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private final long mThreadId;
    private final OnPhraseRecognizedListener mOnPhraseRecognizedListener;

    public PhraseBroadcastReceiver(final long threadId, @NonNull final OnPhraseRecognizedListener listener) {
        super();
        mThreadId = threadId;
        mOnPhraseRecognizedListener = listener;
    }

    @Override
    public void subscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, INTENT_FILTER);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = (intent != null ? intent.getAction() : null);
        if (ACTION_PHRASE_RECOGNIZED.equals(action)) {
            final long threadId = intent.getLongExtra(EXTRA_THREAD, -1);
            final String phrase = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (mThreadId == threadId) {
                mOnPhraseRecognizedListener.onPhraseRecognized(phrase);
            }
        }
    }

    @Override
    public void unsubscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

}
