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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import ru.yandex.subtitles.service.cache.PhrasesUtils;
import ru.yandex.subtitles.service.cache.SpeechKitTtsCloudApi;
import ru.yandex.subtitles.utils.FileUtils;

/**
 * Class that handles audio sample playing by using {@link MediaPlayer}.
 */
public class SampleSpeaker extends AbstractSpeaker implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    private static final String LOG_TAG = "SampleSpeaker";

    private final MediaPlayer mMediaPlayer;

    public SampleSpeaker(final Context context, final long messageId, @NonNull final String sample,
                         @NonNull final String locale, @NonNull final Voice voice,
                         @NonNull final SpeakerListener speakerListener) {
        super(context, messageId, speakerListener);

        Log.i(LOG_TAG, "Sample to vocalize: " + sample);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);

        final String samplesPath = FileUtils.getCacheFilesPath(context, PhrasesUtils.SAMPLES_DIR);
        final String filename = PhrasesUtils.formatFilename(sample, locale, voice.value(),
                SpeechKitTtsCloudApi.FORMAT_MP3);
        try {
            mMediaPlayer.setDataSource(samplesPath + File.separator + filename);
            mMediaPlayer.prepare();
            prepareSoundPool(context);
        } catch (final IOException ioe) {
            Log.e(LOG_TAG, "SampleSpeaker failed with error: ", ioe);
            release();
        }
    }

    @Override
    protected void onStart() {
        playNotification();
        mMediaPlayer.start();
    }

    @Override
    public boolean onError(final MediaPlayer mediaPlayer, final int what, final int extra) {
        Log.e(LOG_TAG, "MediaPlayerSpeaker error. What=" + what + "; extra=" + extra);
        release();
        return true;
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        release();
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        mMediaPlayer.reset();
        mMediaPlayer.release();
    }

    @Override
    protected void onCancel() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

}
