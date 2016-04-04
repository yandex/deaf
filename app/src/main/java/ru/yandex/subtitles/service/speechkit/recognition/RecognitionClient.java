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

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;
import ru.yandex.subtitles.utils.TextUtilsExt;

/**
 * Class that handles recognition events.
 */
public class RecognitionClient extends RecognizerListenerAdapter {

    private final Context mContext;

    /**
     * Recognition language. Only ru-Ru is supported now.
     */
    private final String mLocale;

    /**
     * An thread that starts recognition.
     */
    private final long mThreadId;

    private RecognitionListener mRecognitionListener;

    private List<Recognizer> mRecognizers;

    private String mPartialResult;

    public RecognitionClient(final Context context, @NonNull final String locale, final long threadId) {
        mContext = context;
        mLocale = locale;
        mThreadId = threadId;

        mRecognizers = new ArrayList<Recognizer>();
    }

    @MainThread
    public void setRecognitionListener(final RecognitionListener recognitionListener) {
        mRecognitionListener = recognitionListener;
    }

    /**
     * Starts recognition.
     *
     * @param resumedAfterPlaying {@code true} if recognition was previously
     *                            stopped to vocalize message, {@code false} otherwise. Used to notify UI.
     */
    @MainThread
    public void start(final boolean resumedAfterPlaying) {
        startRecognizer();
        RecognitionBroadcastReceiver.onRecognitionStarted(mContext, mThreadId, resumedAfterPlaying);
    }

    private void startRecognizer() {
        final Recognizer recognizer = createRecognizer(mLocale, this);
        mRecognizers.add(recognizer);
        recognizer.start();
    }

    @NonNull
    private Recognizer createRecognizer(@NonNull final String locale,
                                        @NonNull final RecognizerListener listener) {
        final Recognizer recognizer = Recognizer.create(locale,
                Recognizer.Model.NOTES, listener, true);
        recognizer.setVADEnabled(false);
        return recognizer;
    }
    /**
     * Stops recognition.
     *
     * @param willStartPlaying {@code true} if message vocalization will be started,
     *                         {@code false} otherwise. Used to notify UI.
     */
    @MainThread
    public void stop(final boolean willStartPlaying) {
        stopRecognizers();
        RecognitionBroadcastReceiver.onRecognitionDone(mContext, mThreadId, willStartPlaying);
    }

    private void stopRecognizers() {
        if (!TextUtilsExt.isEmpty(mPartialResult)) {
            onPhraseRecognized(mPartialResult);
        }
        onPartialResult(null);

        final Iterator<Recognizer> iterator = mRecognizers.iterator();
        while (iterator.hasNext()) {
            final Recognizer recognizer = iterator.next();
            recognizer.finishRecording();
            iterator.remove();
        }
    }

    /**
     * Rejects active {@link Recognizer}s and calls {@link RecognitionListener#onPhraseRecognized(long, String)}
     * if there is partial recognition result.
     *
     * @param shouldContinueRecognition {@code true} if recognition should be continued and
     *                                  method was called only to obtain current partial result, not vocalization.
     *                                  {@code false} otherwise.
     */
    @MainThread
    public void reject(final boolean shouldContinueRecognition) {
        stopRecognizers();
        if (shouldContinueRecognition) {
            startRecognizer();
        }
    }

    @MainThread
    @Override
    public void onPowerUpdated(final Recognizer recognizer, final float volume) {
        super.onPowerUpdated(recognizer, volume);
        RecognitionBroadcastReceiver.onPowerUpdate(mContext, mThreadId, volume);
    }

    @MainThread
    @Override
    public void onPartialResults(final Recognizer recognizer, final Recognition recognition,
                                 final boolean endOfUtterance) {
        super.onPartialResults(recognizer, recognition, endOfUtterance);

        if (mRecognizers.contains(recognizer)) {
            final String partialResult = recognition.getBestResultText();
            if (!TextUtilsExt.isEmpty(partialResult)) {
                if (endOfUtterance) {
                    onPhraseRecognized(partialResult);
                    onPartialResult(null);
                } else {
                    onPartialResult(partialResult);
                }
            }
        }
    }

    private void onPartialResult(@Nullable final String partialResult) {
        mPartialResult = partialResult;
        PartialResultBroadcastReceiver.onPartialResult(mContext, mThreadId, partialResult);
    }

    private void onPhraseRecognized(@NonNull final String phrase) {
        if (mRecognitionListener != null) {
            mRecognitionListener.onPhraseRecognized(mThreadId, phrase);
        }
    }

    @MainThread
    @Override
    public void onError(final Recognizer recognizer, final Error error) {
        super.onError(recognizer, error);

        if (mRecognizers.contains(recognizer)) {
            if (mRecognitionListener != null) {
                mRecognitionListener.onRecognitionError();
            }
            RecognitionBroadcastReceiver.onError(mContext, mThreadId, error);
            recognizer.cancel();
            mRecognizers.remove(recognizer);
        }
    }

    @MainThread
    @Override
    public void onRecognitionDone(final Recognizer recognizer, final Recognition recognition) {
        super.onRecognitionDone(recognizer, recognition);
        mRecognizers.remove(recognizer);
    }

}