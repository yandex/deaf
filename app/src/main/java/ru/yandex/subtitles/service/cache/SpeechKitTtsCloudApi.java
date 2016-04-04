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

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.Streaming;

public interface SpeechKitTtsCloudApi {

    String TTS_ENDPOINT = "https://tts.voicetech.yandex.net";

    String FORMAT_MP3 = "mp3";
    String FORMAT_WAV = "wav";

    /**
     * Replace API_KEY with your unique API key. Please, read official documentation how to obtain one:
     * https://tech.yandex.ru/speechkit/cloud/
     */
    String API_KEY = "API_KEY";

    @GET("/generate")
    @Streaming
    Response generate(@Query("text") final String text,
                      @Query("format") final String format,
                      @Query("lang") final String lang,
                      @Query("speaker") final String speaker,
                      @Query("key") final String apiKey);

}
