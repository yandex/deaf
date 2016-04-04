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
package ru.yandex.subtitles.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.utils.TextUtilsExt;
import ru.yandex.subtitles.utils.ViewUtils;

/**
 * Base dialog fragment class that constructs dialog with the app-defined system UI.
 * Notice that you should manually dismiss dialog on button click or on any event you want.
 */
public abstract class AbstractAlertDialogFragment extends AbstractDialogFragment
        implements View.OnClickListener {

    private CharSequence mTitle;
    private CharSequence mPositive;
    private CharSequence mNegative;

    public void setTitle(@StringRes final int title) {
        mTitle = getString(title);
    }

    public void setTitle(final CharSequence title) {
        mTitle = title;
    }

    public void setPositive(@StringRes final int positive) {
        mPositive = getString(positive);
    }

    public void setPositive(final CharSequence positive) {
        mPositive = positive;
    }

    public void setNegative(@StringRes final int negative) {
        mNegative = getString(negative);
    }

    public void setNegative(final CharSequence negative) {
        mNegative = negative;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Subtitles_Dialog_Alert);
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_fragment_alert, container, false);
        final ViewGroup content = ViewUtils.findView(view, R.id.content);
        content.addView(onCreateDialogView(inflater, content, savedInstanceState));
        return view;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This will be called between {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     *
     * @param inflater           the LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          if non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState if non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return return the View for the fragment's UI, or null.
     */
    @NonNull
    public abstract View onCreateDialogView(final LayoutInflater inflater, final ViewGroup container,
                                            final Bundle savedInstanceState);

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView titleView = findView(R.id.title);
        titleView.setText(mTitle);
        titleView.setVisibility(TextUtilsExt.isEmpty(mTitle) ? View.GONE : View.VISIBLE);

        final TextView negativeView = findView(R.id.negative);
        negativeView.setText(mNegative);
        negativeView.setVisibility(TextUtilsExt.isEmpty(mNegative) ? View.GONE : View.VISIBLE);
        negativeView.setOnClickListener(this);

        final TextView positiveView = findView(R.id.positive);
        positiveView.setText(mPositive);
        positiveView.setVisibility(TextUtilsExt.isEmpty(mPositive) ? View.GONE : View.VISIBLE);
        positiveView.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.positive:
                onPositiveButtonClick();
                break;

            case R.id.negative:
                onNegativeButtonClick();
                break;
        }
    }

    public void onPositiveButtonClick() {
    }

    public void onNegativeButtonClick() {
    }

}
