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

import java.text.DateFormat;
import java.util.Calendar;

import ru.yandex.subtitles.BuildConfig;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.utils.ApplicationUtils;

public class AboutAppFragment extends AbstractFragment<Void> implements View.OnClickListener {

    public static final String TAG = "AboutAppFragment";

    public static AboutAppFragment newInstance() {
        return new AboutAppFragment();
    }

    private static String getVersion(final Context context) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(BuildConfig.BUILD_TIME);

        return context.getString(R.string.app_version_format,
                BuildConfig.VERSION_NAME,
                DateFormat.getDateInstance(DateFormat.LONG).format(calendar.getTimeInMillis()));
    }

    private static String getBuildNumber(final Context context) {
        return context.getString(R.string.app_build_format, BuildConfig.BUILD_NUMBER);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_app, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.about_app);

        final Context context = getActivity();

        final TextView nameView = findView(R.id.name);
        nameView.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/TextBook-New-55.otf"));

        final TextView versionView = findView(R.id.version);
        versionView.setText(getVersion(context));

        final TextView buildView = findView(R.id.build);
        buildView.setText(getBuildNumber(context));

        findView(R.id.license_agreement).setOnClickListener(this);
        findView(R.id.other_apps).setOnClickListener(this);

        final TextView copyrightView = findView(R.id.copyright);
        copyrightView.setText(ApplicationUtils.getCopyright(context));
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.license_agreement:
                openLicenseAgreement();
                break;

            case R.id.other_apps:
                openOtherApps();
                break;
        }
    }

    private void openLicenseAgreement() {
        ApplicationUtils.openWebBrowser(getActivity(), getString(R.string.license_agreement_url));
    }

    private void openOtherApps() {
        ApplicationUtils.openGooglePlayDeveloper(getActivity(),
                getString(R.string.developer_yandex),
                getString(R.string.other_yandex_apps));
    }

}