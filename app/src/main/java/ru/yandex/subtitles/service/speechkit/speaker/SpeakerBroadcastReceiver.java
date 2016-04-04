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
package ru.yandex.subtitles.service.speechkit.speaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.utils.IntentUtils;

public class SpeakerBroadcastReceiver extends BroadcastReceiver implements Subscribeable {

    public interface SpeakerEventListener {

        void onStartPlaying(final long messageId);

        void onStopPlaying(final long messageId);

    }

    private static final String ACTION_START_PLAYING = "SpeakerBroadcastReceiver.ACTION_START_PLAYING";
    private static final String ACTION_STOP_PLAYING = "SpeakerBroadcastReceiver.ACTION_STOP_PLAYING";

    private static final IntentFilter INTENT_FILTER = new IntentFilter();

    static {
        INTENT_FILTER.addAction(ACTION_START_PLAYING);
        INTENT_FILTER.addAction(ACTION_STOP_PLAYING);
    }

    private static final String EXTRA_MESSAGE_ID = "message_id";

    public static void onStartPlaying(final Context context, final long messageId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                SpeakerBroadcastReceiver.class, ACTION_START_PLAYING);
        intent.putExtra(EXTRA_MESSAGE_ID, messageId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void onStopPlaying(final Context context, final long messageId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                SpeakerBroadcastReceiver.class, ACTION_STOP_PLAYING);
        intent.putExtra(EXTRA_MESSAGE_ID, messageId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private final SpeakerEventListener mSpeakerEventListener;

    public SpeakerBroadcastReceiver(@NonNull final SpeakerEventListener listener) {
        mSpeakerEventListener = listener;
    }

    @Override
    public void subscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, INTENT_FILTER);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = (intent != null ? intent.getAction() : null);
        if (ACTION_START_PLAYING.equals(action)) {
            final long messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1);
            mSpeakerEventListener.onStartPlaying(messageId);

        } else if (ACTION_STOP_PLAYING.equals(action)) {
            final long messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1);
            mSpeakerEventListener.onStopPlaying(messageId);

        }
    }

    @Override
    public void unsubscribe(final Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

}
