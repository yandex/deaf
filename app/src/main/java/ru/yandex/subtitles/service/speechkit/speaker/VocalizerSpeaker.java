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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Synthesis;
import ru.yandex.speechkit.Vocalizer;
import ru.yandex.speechkit.VocalizerListener;

/**
 * Class that handles text-to-speech action by using {@link Vocalizer}.
 */
public class VocalizerSpeaker extends AbstractSpeaker implements VocalizerListener {

    private static final String LOG_TAG = "VocalizerSpeaker";

    private final Vocalizer mVocalizer;

    public VocalizerSpeaker(final Context context, final long messageId, @NonNull final String text,
                            @NonNull final String locale, @NonNull final Voice voice,
                            @NonNull final SpeakerListener speakerListener) {
        super(context, messageId, speakerListener);

        Log.i(LOG_TAG, "Text to vocalize: " + text);
        mVocalizer = Vocalizer.createVocalizer(locale, text, true, voice.value());
        mVocalizer.setListener(this);
        prepareSoundPool(context);
    }

    @Override
    protected void onStart() {
        mVocalizer.start();
    }

    public void onSynthesisBegin(final Vocalizer vocalizer) {
        Log.i(LOG_TAG, "Syntesis begin");
        playNotification();
    }

    public void onSynthesisDone(final Vocalizer vocalizer, final Synthesis synthesis) {
        Log.i(LOG_TAG, "Syntesis done");
    }

    public void onPlayingBegin(final Vocalizer vocalizer) {
        Log.i(LOG_TAG, "Playing begin");
    }

    public void onVocalizerError(final Vocalizer vocalizer, final Error error) {
        Log.i(LOG_TAG, "Vocalizer error: " + error.getString());
        release();
    }

    public void onPlayingDone(final Vocalizer vocalizer) {
        Log.i(LOG_TAG, "Playing done");
        release();
    }


    @Override
    public void onCancel() {
        if (mVocalizer != null && inProgress()) {
            mVocalizer.cancel();
        }
    }

}
