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
package ru.yandex.subtitles.service.speechkit;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ru.yandex.speechkit.Error;
import ru.yandex.subtitles.R;

/**
 * Class is used as utility mapper from {@link Error} to human-readable message.
 */
public class ErrorResolver {

    @StringRes
    public static int resolveError(@NonNull final Error error) {
        int messageRes = R.string.error_ok;
        switch (error.getCode()) {
            case Error.ERROR_OK:
                messageRes = R.string.error_ok;
                break;

            case Error.ERROR_API_KEY:
                messageRes = R.string.error_api_key;
                break;

            case Error.ERROR_AUDIO:
                messageRes = R.string.error_audio;
                break;

            case Error.ERROR_AUDIO_PLAYER:
                messageRes = R.string.error_audio_player;
                break;

            case Error.ERROR_AUDIO_PERMISSIONS:
                messageRes = R.string.error_audio_permissions;
                break;

            case Error.ERROR_ENCODING:
                messageRes = R.string.error_encoding;
                break;

            case Error.ERROR_NETWORK:
                messageRes = R.string.error_network;
                break;

            case Error.ERROR_SERVER:
                messageRes = R.string.error_server;
                break;

            case Error.ERROR_CANCELED:
                messageRes = R.string.error_cancelled;
                break;

            case Error.ERROR_NO_SPEECH:
                messageRes = R.string.error_no_speech;
                break;

            case Error.ERROR_NO_TEXT_TO_SYNTHESIZE:
                messageRes = R.string.error_no_text_to_syntesize;
                break;

            case Error.ERROR_NOT_AVAILABLE:
                messageRes = R.string.error_not_available;
                break;

            case Error.ERROR_BUSY:
                messageRes = R.string.error_busy;
                break;

            case Error.ERROR_UNKNOWN:
                messageRes = R.string.error_unknown;
                break;
        }

        return messageRes;
    }

    private ErrorResolver() {
    }

}
