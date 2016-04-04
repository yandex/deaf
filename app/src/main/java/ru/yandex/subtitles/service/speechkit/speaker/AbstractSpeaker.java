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

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.NonNull;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.utils.ApplicationUtils;

/**
 * Class provides an ability to playing audio with audio focus handling.
 * Child should implement {@link AbstractSpeaker#onStart()} and {@link AbstractSpeaker#onCancel()}
 * to start and stop playing action.
 */
public abstract class AbstractSpeaker implements Speaker,
        AudioManager.OnAudioFocusChangeListener {

    private static final int STATE_NONE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 1 << 1;
    private static final int STATE_SPEAK_REQUESTED = 1 << 2;
    private static final int STATE_PLAYING = 1 << 3;

    private final Context mContext;
    private final long mMessageId;

    private final AudioManager mAudioManager;
    private SoundPool mSoundPool;
    private int mNotification;

    private final SpeakerListener mSpeakerListener;

    private boolean mNotificationReady = false;
    private float mNotificationVolume = 0.8f;

    private int mState = STATE_NONE;

    public AbstractSpeaker(final Context context, final long messageId,
                           @NonNull final SpeakerListener speakerListener) {
        mContext = context;
        mMessageId = messageId;
        mSpeakerListener = speakerListener;
        mAudioManager = ApplicationUtils.getSystemService(context, Context.AUDIO_SERVICE);

        mSoundPool = createSoundPool();
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    private boolean hasState(final int state) {
        return ((mState & state) == state);
    }

    @SuppressWarnings("deprecated")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    private SoundPool createSoundPool() {
        if (ApplicationUtils.hasLollipop()) {
            final AudioAttributes audioAttrs = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            return new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttrs).build();
        } else {
            return new SoundPool(/* max streams */ 1, AudioManager.STREAM_MUSIC, /* not used */ 0);
        }
    }

    protected void prepareSoundPool(final Context context) {
        final int notification = mSoundPool.load(context, R.raw.multimedia_pop_up_alert_tone_2, /* priority */ 1);
        mState = STATE_PREPARING;

        final int status = (notification > 0 ? 0 : -1);
        onLoadComplete(mSoundPool, notification, status);
    }

    private void onLoadComplete(final SoundPool soundPool, final int sampleId, final int status) {
        mNotification = sampleId;
        mNotificationReady = (status == 0);

        mState |= STATE_PREPARED;
        mState &= ~STATE_PREPARING;
        if (hasState(STATE_SPEAK_REQUESTED)) {
            speak();
        }
    }

    @Override
    public final void speak() {
        mState |= STATE_SPEAK_REQUESTED;
        if (hasState(STATE_PREPARED) && !hasState(STATE_PLAYING)) {
            final int audioFocus = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mState |= STATE_PLAYING;
                mState &= ~STATE_SPEAK_REQUESTED;

                // Check if user changed (accidentally) volume to zero
                final int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                if (streamVolume < (maxVolume / 8)) {
                    final int safeVolume = (maxVolume - (maxVolume / 5)); // All are integers
                    mNotificationVolume = (safeVolume / (float) maxVolume);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, safeVolume, 0);
                }

                onStart();
                mSpeakerListener.onSpeakerStarted(mMessageId);

            } else {
                release();
            }
        }
    }

    /**
     * Start audio playing here.
     */
    protected abstract void onStart();

    protected void playNotification() {
        if (mNotificationReady) {
            mSoundPool.play(mNotification, mNotificationVolume, mNotificationVolume,
                /* stream priority */ 1,
                /* loop mode - no loop */ 0,
                /* playback rate - normal */ 1);
        }
    }

    @Override
    public final long getMessageId() {
        return mMessageId;
    }

    @Override
    public boolean inProgress() {
        return hasState(STATE_PLAYING);
    }

    @Override
    public void onAudioFocusChange(final int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                cancel();
                break;
        }
    }

    protected final void release() {
        if (mState != STATE_NONE) {
            mState = STATE_NONE;
            onRelease();
            if (mNotificationReady) {
                mSoundPool.unload(mNotification);
            }
            mSoundPool.release();
            mAudioManager.abandonAudioFocus(this);
        }
        mSpeakerListener.onSpeakerFinished(mMessageId);
    }

    /**
     * Override this method to release resources when speaker is finished.
     */
    protected void onRelease() {
    }

    @Override
    public final void cancel() {
        onCancel();
        release();
    }

    /**
     * Stop audio playing here.
     */
    protected abstract void onCancel();

}
