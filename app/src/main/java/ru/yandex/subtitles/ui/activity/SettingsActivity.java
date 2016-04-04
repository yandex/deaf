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
package ru.yandex.subtitles.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.ui.fragment.settings.AboutAppFragment;
import ru.yandex.subtitles.ui.fragment.settings.AboutSpeechKitFragment;
import ru.yandex.subtitles.ui.fragment.settings.FeedbackFragment;
import ru.yandex.subtitles.ui.fragment.settings.SettingsFragment;

public class SettingsActivity extends AbstractActivity implements SettingsFragment.OnSettingsClickListener,
        FeedbackFragment.OnFeedbackSentListener {

    public static void start(final Context context) {
        final Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            showFragmentNoAnimate(SettingsFragment.newInstance(), SettingsFragment.TAG);
        }
    }

    @Override
    public void onAboutAppClick() {
        showFragment(AboutAppFragment.newInstance(), AboutAppFragment.TAG);
    }

    @Override
    public void onAboutSpeechKitClick() {
        showFragment(AboutSpeechKitFragment.newInstance(), AboutSpeechKitFragment.TAG);
    }

    @Override
    public void onFeedbackClick() {
        showFragment(FeedbackFragment.newInstance(), FeedbackFragment.TAG);
    }

    @Override
    public void onFeedbackSent() {
        switchToFragment(SettingsFragment.TAG);
    }

}
