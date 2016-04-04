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
package ru.yandex.subtitles.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.utils.ViewUtils;

public class SpeakerView extends FrameLayout implements View.OnClickListener {

    private CircularProgressBar mProgressBar;
    private OnClickListener mOnClickListener;
    private final int mLayout;

    public SpeakerView(final Context context) {
        this(context, null);
    }

    public SpeakerView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeakerView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SpeakerView, defStyleAttr, 0);

        try {
            mLayout = typedArray.getResourceId(R.styleable.SpeakerView_alternateLayout, R.layout.view_speaker);
        } finally {
            typedArray.recycle();
        }

        initialize();
    }

    private void initialize() {
        inflate(getContext(), mLayout, this);

        final ImageView speakView = ViewUtils.findView(this, R.id.speak);
        if (speakView == null) {
            throw new IllegalArgumentException("SpeakerView layout MUST contain an ImageView with id = speak.");
        } else {
            speakView.setOnClickListener(this);
        }

        mProgressBar = ViewUtils.findView(this, R.id.speaker_progress);
        if (mProgressBar == null) {
            throw new IllegalArgumentException("SpeakerView layout MUST contain a CircularProgressBar" +
                    " with id = speaker_progress.");
        } else {
            mProgressBar.setVisibility(GONE);
        }
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        mOnClickListener = l;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.speak:
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(this);
                }
                break;
        }
    }

    public void onStartPlaying() {
        mProgressBar.setVisibility(VISIBLE);
    }

    public void onStopPlaying() {
        mProgressBar.setVisibility(GONE);
    }

}
