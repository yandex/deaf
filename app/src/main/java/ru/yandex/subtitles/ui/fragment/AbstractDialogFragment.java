package ru.yandex.subtitles.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;

import ru.yandex.subtitles.ui.activity.ActivityResultListener;
import ru.yandex.subtitles.utils.ViewUtils;

/**
 * AbstractDialogFragment.java
 * <p/>
 * Base dialog fragment class that provides ability to send dialog result to the calling class.
 * <p/>
 * This file is a part of the Yandex.Talk app.
 * <p/>
 * Version for Android © 2015 YANDEX
 * <p/>
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at https://legal.yandex.ru/talk_mobile_agreement
 */
public abstract class AbstractDialogFragment extends DialogFragment {

    private int mRequestCode;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setStyle(STYLE_NO_TITLE, 0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setSoftInputMode(getSoftInputModeFlags());
        return dialog;
    }

    public int getSoftInputModeFlags() {
        return WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
    }

    public <V extends View> V findView(final int id) {
        return ViewUtils.findView(getView(), id);
    }

    @Override
    public void onPause() {
        final View view = getView();
        if (view != null) {
            final View focusedView = view.findFocus();
            ViewUtils.hideSoftwareKeyboard(focusedView);
        }
        super.onPause();
    }

    public void setRequestCode(final int requestCode) {
        mRequestCode = requestCode;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    protected void sendResultToTarget(final int resultCode, @Nullable final Intent data) {
        final Fragment target = getTargetFragment();
        final Fragment parent = getParentFragment();
        final Activity activity = getActivity();
        if (target != null) {
            target.onActivityResult(getTargetRequestCode(), resultCode, data);

        } else if (parent != null) {
            parent.onActivityResult(getRequestCode(), resultCode, data);

        } else if (activity != null && ActivityResultListener.class.isInstance(activity)) {
            final ActivityResultListener listener = (ActivityResultListener) activity;
            listener.onActivityResult(getRequestCode(), resultCode, data);
        }
    }

}
