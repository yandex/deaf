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

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.ui.fragment.AbstractAlertDialogFragment;
import ru.yandex.subtitles.utils.ApplicationUtils;
import ru.yandex.subtitles.utils.IntentUtils;

public class ChangeOrientationHintDialogFragment extends AbstractAlertDialogFragment {

    public static final String TAG = "ChangeOrientationHintDialogFragment";

    private static final int REQUEST_CODE_SETTINGS = 1034;

    private static final String EXTRA_ACCELEROMETER_ORIENTATION_LOCKED = "accelerometer_orientation_locked";

    public static ChangeOrientationHintDialogFragment newInstance(final boolean isAccelerometerOrientationLocked) {
        final ChangeOrientationHintDialogFragment fragment = new ChangeOrientationHintDialogFragment();

        final Bundle args = new Bundle();
        args.putBoolean(EXTRA_ACCELEROMETER_ORIENTATION_LOCKED, isAccelerometerOrientationLocked);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.change_orientation_hint_dialog_title);
        setNegative(R.string.dialog_button_neutral);

        final Bundle args = getArguments();
        final boolean isAccelerometerOrientationLocked = args
                .getBoolean(EXTRA_ACCELEROMETER_ORIENTATION_LOCKED, false);
        final boolean settingsExists = IntentUtils.canStartActivity(getActivity(), Settings.ACTION_DISPLAY_SETTINGS);
        if (!ApplicationUtils.hasLollipop() && isAccelerometerOrientationLocked && settingsExists) {
            setPositive(R.string.open_settings);
        }
    }

    @NonNull
    @Override
    public View onCreateDialogView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_change_orientation_hint, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bundle args = getArguments();
        final boolean isAccelerometerOrientationLocked = args
                .getBoolean(EXTRA_ACCELEROMETER_ORIENTATION_LOCKED, false);

        final StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.change_orientation_hint_dialog_text));
        if (isAccelerometerOrientationLocked) {
            sb.append(" ").append(getString(R.string.change_orientation_hint_dialog_orientation_locked));
        }

        final TextView textView = findView(R.id.text);
        textView.setText(sb);
    }

    @Override
    public void onPositiveButtonClick() {
        super.onPositiveButtonClick();

        final Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            dismissAllowingStateLoss();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNegativeButtonClick() {
        super.onNegativeButtonClick();
        dismissAllowingStateLoss();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        sendResultToTarget(Activity.RESULT_OK, null);
    }

}
