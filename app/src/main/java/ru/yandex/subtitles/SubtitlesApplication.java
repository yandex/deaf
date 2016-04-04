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
package ru.yandex.subtitles;

import android.app.Application;

import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.service.MessagingService;
import ru.yandex.subtitles.service.PhrasesService;

public class SubtitlesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /*
        Uncomment the line below to report analytics events. Please also
        modify YandexMetricaEventTracker.java to provide your unique Yandex.Metrica API key.
        */
        // Analytics.addEventTracker(new YandexMetricaEventTracker());
        Analytics.initialize(this);

        Preferences.instantiate(this);

        MessagingService.start(this);

        PhrasesService.prepareSamples(this);
        PhrasesService.invalidateSamples(this);
    }

}
