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
package ru.yandex.subtitles.service.cache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import retrofit.RestAdapter;
import retrofit.client.Response;
import ru.yandex.subtitles.content.dao.PhrasesDAO;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.service.speechkit.speaker.Voice;
import ru.yandex.subtitles.utils.AsyncAutoCompleteHandler;
import ru.yandex.subtitles.utils.FileUtils;
import ru.yandex.subtitles.utils.NetworkUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;

/**
 * Class that invalidates phrase's audio samples.
 */
public class SamplesCacheManager implements AsyncAutoCompleteHandler.OnHandleEventListener<Void> {

    private static final String NAME = "SamplesCacheManager";

    public static void prepareSamples(final Context context) {
        final String samplesPath = FileUtils.getCacheFilesPath(context, PhrasesUtils.SAMPLES_DIR);
        final File samplesDir = new File(samplesPath);

        final String[] samples = samplesDir.list();
        final boolean hasSamples = (samples != null && samples.length > 0);
        if (!hasSamples) {
            FileUtils.copyAssetsFolder(context.getAssets(), PhrasesUtils.ASSETS_SAMPLES_DIR, samplesPath);
        }
    }

    private final Context mContext;
    private final PhrasesDAO mPhrasesDao;
    private final AsyncAutoCompleteHandler<Void> mQueue;

    private final SpeechKitTtsCloudApi mSpeechKitTtsCloudApi;

    public SamplesCacheManager(@NonNull final Context context) {
        mContext = context;
        mPhrasesDao = new PhrasesDAO(context);
        mQueue = new AsyncAutoCompleteHandler<Void>(NAME);
        mQueue.setOnHandleEventListener(this);

        final RestAdapter retrofit = new RestAdapter.Builder()
                .setEndpoint(SpeechKitTtsCloudApi.TTS_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build();
        mSpeechKitTtsCloudApi = retrofit.create(SpeechKitTtsCloudApi.class);
    }

    public void invalidateSamples() {
        mQueue.post(null, true);
    }

    @Override
    public void onHandleEvent(final Void event) {
        if (NetworkUtils.isNetworkConnected(mContext)) {
            final List<Phrase> phrases = mPhrasesDao.getNonCachedStartingPhrases();
            for (final Phrase phrase : phrases) {
                final String sample = UUID.randomUUID().toString();
                if (ttsMale(sample, phrase) && ttsFemale(sample, phrase)) {
                    if (mPhrasesDao.setSample(phrase.getId(), sample)) {
                        Log.i(NAME, "Set sample=" + sample + " for phrase=" + phrase.getId());
                    }
                } else {
                    deleteSample(sample, phrase.getLocale());
                }
            }

            PhrasesDAO.notifyPhrasesChanged(mContext);
        }
    }

    private boolean ttsMale(@NonNull final String sample, @NonNull final Phrase phrase) {
        return ttsSpeaker(sample, phrase, Voice.ERMIL);
    }

    private boolean ttsFemale(@NonNull final String sample, @NonNull final Phrase phrase) {
        return ttsSpeaker(sample, phrase, Voice.OMAZH);
    }

    private boolean ttsSpeaker(@NonNull final String sample, @NonNull final Phrase phrase,
                               @NonNull final Voice voice) {
        final String text = String.format("\"%s\"", phrase.getText());
        final InputStream stream = tts(text, SpeechKitTtsCloudApi.FORMAT_MP3, phrase.getLocale(), voice.value());
        final String filename = PhrasesUtils.formatFilename(sample, phrase.getLocale(),
                voice.value(), SpeechKitTtsCloudApi.FORMAT_MP3);
        return (stream != null && saveFile(stream, filename));
    }

    @Nullable
    private InputStream tts(@NonNull final String text, @NonNull final String format,
                            @NonNull final String lang, @NonNull final String speaker) {
        InputStream stream = null;
        try {
            Response response = mSpeechKitTtsCloudApi.generate(text, format, lang,
                    speaker, SpeechKitTtsCloudApi.API_KEY);

            stream = response.getBody().in();
        } catch (final Exception e) {
            Log.e(NAME, "Failed to get TTS input stream.", e);
        }
        return stream;
    }

    private boolean saveFile(@NonNull final InputStream stream, @NonNull final String filename) {
        final String samplesPath = FileUtils.getCacheFilesPath(mContext, PhrasesUtils.SAMPLES_DIR);

        OutputStream output = null;
        try {
            output = new FileOutputStream(samplesPath + File.separator + filename);

            int read;
            final byte[] bytes = new byte[FileUtils.BUFFER_SIZE_1K];
            while ((read = stream.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return true;

        } catch (final IOException ioe) {
            Log.e(NAME, "Failed to save audio file.", ioe);
        } finally {
            FileUtils.safeClose(output);
            FileUtils.safeClose(stream);
        }

        return false;
    }

    public void deleteSample(@Nullable final String sample, @NonNull final String locale) {
        if (!TextUtilsExt.isEmpty(sample)) {
            final String samplesPath = FileUtils.getCacheFilesPath(mContext, PhrasesUtils.SAMPLES_DIR);
            deleteSample(samplesPath, sample, locale, Voice.ERMIL.value());
            deleteSample(samplesPath, sample, locale, Voice.OMAZH.value());
        }
    }

    private void deleteSample(@NonNull final String samplesPath, @NonNull final String sample,
                              @NonNull final String locale, @NonNull final String voice) {
        final String filename = PhrasesUtils.formatFilename(sample, locale, voice, SpeechKitTtsCloudApi.FORMAT_MP3);
        final File file = new File(samplesPath, filename);
        if (file.exists() && file.delete()) {
            Log.i(NAME, "Cached file " + file.getAbsolutePath() + " has been deleted.");
        }
    }

}
