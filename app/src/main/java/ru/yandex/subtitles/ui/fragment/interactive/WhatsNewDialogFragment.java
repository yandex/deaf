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
package ru.yandex.subtitles.ui.fragment.interactive;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.yandex.subtitles.Preferences;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.ui.fragment.AbstractAlertDialogFragment;

public class WhatsNewDialogFragment extends AbstractAlertDialogFragment {

    public static final String TAG = "WhatsNewDialogFragment";

    private static final String EXTRA_INFO = "installation_info";

    public static WhatsNewDialogFragment newInstance(@NonNull final Preferences.InstallationInfo installationInfo) {
        final WhatsNewDialogFragment dialog = new WhatsNewDialogFragment();

        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_INFO, installationInfo);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.whats_new_dialog_title);
        setPositive(R.string.dialog_button_neutral);
    }

    @NonNull
    @Override
    public View onCreateDialogView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_whats_new, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final StringBuilder sb = new StringBuilder();

        final Preferences.InstallationInfo info = getArguments().getParcelable(EXTRA_INFO);
        if (info != null && info.wasOlderThan103()) {
            sb.append(getString(R.string.whats_new_dialog_list_to_110));
            sb.append("\n");
        }
        sb.append(getString(R.string.whats_new_dialog_list_to_111));

        final TextView textView = findView(R.id.text);
        textView.setText(sb);
    }

    @Override
    public void onPositiveButtonClick() {
        super.onPositiveButtonClick();
        dismissAllowingStateLoss();
    }

}
