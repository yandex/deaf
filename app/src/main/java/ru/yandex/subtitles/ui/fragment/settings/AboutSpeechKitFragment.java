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

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.utils.ApplicationUtils;

public class AboutSpeechKitFragment extends AbstractFragment<Void> implements View.OnClickListener {

    public static final String TAG = "AboutSpeechKitFragment";

    public static AboutSpeechKitFragment newInstance() {
        return new AboutSpeechKitFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_speechkit, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.about_speech_kit);

        final Context context = getActivity();

        final TextView nameView = findView(R.id.name);
        nameView.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/TextBook-New-55.otf"));

        findView(R.id.more).setOnClickListener(this);

        final TextView copyrightView = findView(R.id.copyright);
        copyrightView.setText(ApplicationUtils.getCopyright(context));
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.more:
                ApplicationUtils.openWebBrowser(getActivity(), getString(R.string.speechkit_api_link));
                break;
        }
    }

}