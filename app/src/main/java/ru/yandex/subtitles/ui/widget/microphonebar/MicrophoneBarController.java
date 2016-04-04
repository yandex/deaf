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
package ru.yandex.subtitles.ui.widget.microphonebar;

import android.support.annotation.IntDef;

/**
 * Interface describes panel widget the provides interactive animations of
 */
public interface MicrophoneBarController {

    int DEFAULT_MESSAGE_DURATION = 3000;

    int STATE_DISABLED = -1;
    int STATE_IDLE = 0;
    int STATE_IN_PROGRESS = 1;
    int STATE_RECORDING = 2;
    int STATE_STOPPING = 3;

    int NOTIFICATION_MESSAGE = 0;
    int ERROR_MESSAGE = 1;

    @IntDef({ STATE_DISABLED, STATE_IDLE, STATE_IN_PROGRESS,
            STATE_RECORDING, STATE_STOPPING })
    @interface State {
    }

    /**
     * Animate volume changes on microphone view.
     *
     * @param volume relative amplitude of voice. Available values from 0.f to 1.f
     */
    void setVolume(final float volume);

    /**
     * Shows message on microphone bar.
     *
     * @param message text message to be shown.
     */
    void showMessage(final CharSequence message);

    void showErrorMessage(final CharSequence message);

    /**
     * Sets the default message duration.
     *
     * @param timeInMillis
     */
    void setMessageDuration(final long timeInMillis);

    /**
     * Sets microphone bar state according to SpeechKit callbacks
     *
     * @param newState
     */
    void setState(@State final int newState);

    /**
     * Returns current state of microphone bar
     */
    @State
    int getState();

    /**
     * Sets listener to handle microphone bar controller changes.
     */
    void setMicrophoneBarListener(final MicrophoneBarListener listener);

    void onStartPlaying();

    void onStopPlaying();

}