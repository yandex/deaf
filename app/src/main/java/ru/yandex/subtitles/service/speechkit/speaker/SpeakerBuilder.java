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
import android.support.annotation.Nullable;

import ru.yandex.subtitles.utils.TextUtilsExt;

public class SpeakerBuilder {

    private Context mContext;
    private long mMessageId = -1;
    private String mText;
    private String mSample;
    private Voice mVoice;
    private String mLocale;
    private SpeakerListener mSpeakerListener;

    public SpeakerBuilder(final Context context) {
        mContext = context;
    }

    public SpeakerBuilder message(final long messageId) {
        mMessageId = messageId;
        return this;
    }
    
    public SpeakerBuilder text(@NonNull final String text) {
        mText = text;
        return this;
    }

    public SpeakerBuilder sample(@Nullable final String sample) {
        mSample = sample;
        return this;
    }

    public SpeakerBuilder locale(@NonNull final String lang) {
        mLocale = lang;
        return this;
    }

    public SpeakerBuilder voice(@NonNull final Voice voice) {
        mVoice = voice;
        return this;
    }

    public SpeakerBuilder listener(@NonNull final SpeakerListener listener) {
        mSpeakerListener = listener;
        return this;
    }

    @NonNull
    public Speaker build() {
        if (mMessageId < 0) {
            throw new IllegalArgumentException("Please provide message identificator");
        }
        if (TextUtilsExt.isEmpty(mText)) {
            throw new IllegalArgumentException("Text to vocalize should not be empty");
        }
        if (TextUtilsExt.isEmpty(mLocale)) {
            throw new IllegalArgumentException("Vocalization language should not be empty");
        }
        if (mVoice == null) {
            throw new IllegalArgumentException("Please provide voice");
        }
        if (mSpeakerListener == null) {
            throw new IllegalArgumentException("Speaker listener should not be null");
        }

        final Speaker speaker;
        if (TextUtilsExt.isEmpty(mSample)) {
            speaker = new VocalizerSpeaker(mContext, mMessageId, mText, mLocale, mVoice, mSpeakerListener);
        } else {
            speaker = new SampleSpeaker(mContext, mMessageId, mSample, mLocale, mVoice, mSpeakerListener);
        }
        return speaker;
    }

}
