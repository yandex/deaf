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
package ru.yandex.subtitles.ui.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import ru.yandex.subtitles.Preferences;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.utils.ShareUtils;

public class SettingsFragment extends AbstractFragment<SettingsFragment.OnSettingsClickListener>
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public interface OnSettingsClickListener {

        void onAboutAppClick();

        void onAboutSpeechKitClick();

        void onFeedbackClick();

    }

    public static final String TAG = "SettingsFragment";

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.settings);

        final Preferences preferences = Preferences.getInstance();

        // TODO: uncomment it to support Ukrainian. Note: uncomment it also in res/layout/fragment_settings.xml
        // TODO: currently Ukrainian is not supported by SpeechKit
        /* final ToggleButton recognitionLanguageSwitch = findView(R.id.recognition_language_switch);
        recognitionLanguageSwitch.setChecked(preferences.hasRussianRecognitionLanguage(getActivity()));
        recognitionLanguageSwitch.setOnCheckedChangeListener(this); */

        final ToggleButton voiceSwitch = findView(R.id.vocalization_voice_switch);
        voiceSwitch.setChecked(preferences.hasMaleVoice());
        voiceSwitch.setOnCheckedChangeListener(this);

        findView(R.id.about_app).setOnClickListener(this);
        findView(R.id.about_speech_kit).setOnClickListener(this);
        findView(R.id.feedback).setOnClickListener(this);
        findView(R.id.share_app).setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(final CompoundButton v, final boolean isChecked) {
        switch (v.getId()) {
            case R.id.vocalization_voice_switch:
                Analytics.onVocalizationVoiceChanged();
                Preferences.getInstance().setHasMaleVoice(isChecked);
                break;

            // TODO: uncomment it to support Ukrainian
            /* case R.id.recognition_language_switch:
                Analytics.onRecognitionLanguageChanged();
                Preferences.getInstance().setRussianRecognitionLanguage(isChecked);
                break; */
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.about_app:
                Analytics.onAboutAppClick();
                mController.onAboutAppClick();
                break;

            case R.id.about_speech_kit:
                Analytics.onAboutSpeechKitClick();
                mController.onAboutSpeechKitClick();
                break;

            case R.id.feedback:
                Analytics.onFeedbackClick();
                mController.onFeedbackClick();
                break;

            case R.id.share_app:
                Analytics.onShareAppClick();
                onShareAppClick();
                break;
        }
    }

    private void onShareAppClick() {
        ShareUtils.share(getActivity(),
                getString(R.string.yandex_conversation),
                getString(R.string.content_for_sharing));
    }

}
