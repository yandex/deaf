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

import android.util.Log;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;

/**
 * An adapter class to a Yandex.SpeechKit {@link RecognizerListener}.
 */
public class RecognizerListenerAdapter implements RecognizerListener {

    private static final String LOG_TAG = "RecognizerListener";

    @Override
    public void onRecordingBegin(final Recognizer recognizer) {
        Log.d(LOG_TAG, "Recording begin");
    }

    @Override
    public void onSoundDataRecorded(final Recognizer recognizer, final byte[] bytes) {
    }

    @Override
    public void onSpeechDetected(final Recognizer recognizer) {
    }

    @Override
    public void onSpeechEnds(final Recognizer recognizer) {
    }

    @Override
    public void onPartialResults(final Recognizer recognizer,
                                 final Recognition recognition,
                                 final boolean endOfUtterance) {
        Log.d(LOG_TAG, "Partial results: " + recognition + "; end of utterance: " + endOfUtterance);
    }

    @Override
    public void onPowerUpdated(final Recognizer recognizer, final float volume) {
        Log.d(LOG_TAG, "Power updated: " + volume);
    }

    @Override
    public void onError(final Recognizer recognizer, final Error error) {
        Log.d(LOG_TAG, "Error: " + error.getString());
    }

    @Override
    public void onRecordingDone(final Recognizer recognizer) {
        Log.d(LOG_TAG, "Recording done");
    }

    @Override
    public void onRecognitionDone(final Recognizer recognizer, final Recognition recognition) {
        Log.d(LOG_TAG, "Recognition done");
    }

}
