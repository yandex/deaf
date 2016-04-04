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
package ru.yandex.subtitles.analytics;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Implement interface to provide event reporting to any tracking system, like Yandex.Metrica,
 * Google Analytics, Fabric, etc.
 */
public interface EventTracker {

    /**
     * Activate your event tracker implementation with the application context.
     *
     * @param context an application context
     */
    void activate(final Context context);

    void onResumeActivity(final Activity activity);

    void onPauseActivity(final Activity activity);

    void reportEvent(@NonNull final String event);

    void reportEvent(@NonNull final String event, @NonNull final Map<String, Object> attrs);

}
