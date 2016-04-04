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
package ru.yandex.subtitles.ui.widget.microphonebar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.ui.widget.microphonebar.impl.AbstractBackgroundAnimation;
import ru.yandex.subtitles.ui.widget.microphonebar.impl.ClickExplodeBackgroundAnimation;
import ru.yandex.subtitles.ui.widget.microphonebar.impl.ColorSet;
import ru.yandex.subtitles.ui.widget.microphonebar.impl.MicrophoneBarBackgroundDrawable;
import ru.yandex.subtitles.ui.widget.microphonebar.impl.WaitingBackgroundAnimation;
import ru.yandex.subtitles.utils.ApplicationUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;
import ru.yandex.subtitles.utils.ViewUtils;

public class MicrophoneBarView extends LinearLayout implements MicrophoneBarController,
        View.OnClickListener {

    private static final int DEFAULT_BUTTON_HEIGHT = 56; // dp

    private static final float VOLUME_THRESHOLD = 0.25f;
    private static final long VOLUME_OBSERVER_TIMEOUT = 1500L;
    private static final long VOLUME_OBSERVER_ERROR_TIMEOUT = 8000L;

    private static final int OPACITY_100 = 255;
    private static final int OPACITY_90 = 230;
    private static final int OPACITY_0 = 0;

    private boolean mAttachedToWindow = false;

    private CircularProgressBar mProgressBar;
    private ImageView mButtonView;
    private MicrophoneBarBackgroundDrawable mBackgroundDrawable;

    private int mState;
    private MicrophoneBarListener mMicrophoneBarListener;

    // Local properties
    private ColorSet mNormalColorSet;
    private ColorSet mInProgressColorSet;
    private ColorSet mRecordingColorSet;
    private ColorSet mErrorColorSet;

    // Background colors
    private int mBackgroundColorWithMessage = Color.WHITE;
    private int mBackgroundColorActive = Color.YELLOW;
    private int mBackgroundColorError = Color.RED;

    // Message text properties
    private CharSequence mMessage = "";
    private int mMessageType = -1;

    private long mMessageAppearanceDuration;
    private long mMessageDuration = DEFAULT_MESSAGE_DURATION;
    private int mMessageColor = Color.BLACK;

    private int mButtonSize;
    private Drawable mButtonImageDrawable;

    private boolean mIsPostDelayVolume = true;
    private boolean mHasVolumeCallbacks = false;

    private final Rect mButtonRect = new Rect();
    private final int[] mSelfLocation = new int[2];

    private PopupWindow mPopupWindow;

    public MicrophoneBarView(final Context context) {
        this(context, null);
    }

    public MicrophoneBarView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.microphoneBarStyle);
    }

    public MicrophoneBarView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        inflate();

        mBackgroundDrawable = new MicrophoneBarBackgroundDrawable(getContext());
        ViewUtils.setBackgroundCompat(this, mBackgroundDrawable);

        readAttrs(attrs, defStyle);
        updateMicrophoneViewProperties();
        updateBackgroundDrawable(getBackgroundAnimationDirection(), true);

        mBackgroundDrawable.invalidateSelf();
    }

    private void inflate() {
        inflate(getContext(), R.layout.view_microphone_bar, this);

        mProgressBar = (CircularProgressBar) findViewById(R.id.microphone_bar_progress);
        mButtonView = (ImageView) findViewById(R.id.microphone_bar_button);
        mButtonView.setOnClickListener(this);
    }

    @SuppressWarnings("deprecated")
    private void readAttrs(final AttributeSet attrs, final int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MicrophoneBarStyle, defStyle, 0);
        if (a != null) {
            try {
                // Color scheme related attributes
                mNormalColorSet = new ColorSet();
                mNormalColorSet.colorNormal = a.getColor(R.styleable.MicrophoneBarStyle_microphone_normal,
                        Color.parseColor("#ffe071"));
                mNormalColorSet.colorPressed = a.getColor(R.styleable.MicrophoneBarStyle_microphone_pressed,
                        Color.parseColor("#ffcc00"));

                mInProgressColorSet = new ColorSet();
                mInProgressColorSet.colorPressed = Color.TRANSPARENT;
                mInProgressColorSet.colorNormal = mNormalColorSet.colorNormal;

                mRecordingColorSet = new ColorSet();
                mRecordingColorSet.colorNormal = mNormalColorSet.colorPressed;
                mRecordingColorSet.colorPressed = mNormalColorSet.colorPressed;

                mErrorColorSet = new ColorSet();
                mErrorColorSet.colorNormal = a.getColor(R.styleable.MicrophoneBarStyle_microphone_error_normal,
                        Color.parseColor("#f7b7b5"));
                mErrorColorSet.colorPressed = a.getColor(R.styleable.MicrophoneBarStyle_microphone_error_pressed,
                        Color.parseColor("#ea4a44"));

                // Background colors
                mBackgroundColorWithMessage = a.getColor(R.styleable.MicrophoneBarStyle_background_normal_with_message,
                        Color.parseColor("#ffffff"));
                mBackgroundColorActive = a.getColor(R.styleable.MicrophoneBarStyle_background_active,
                        Color.parseColor("#fffae5"));
                mBackgroundColorError = a.getColor(R.styleable.MicrophoneBarStyle_background_error,
                        Color.parseColor("#fdeceb"));

                // Message related attributes
                final Resources resources = getContext().getResources();
                final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
                mMessageAppearanceDuration = a.getInteger(R.styleable.MicrophoneBarStyle_message_appearance_duration,
                        resources.getInteger(android.R.integer.config_shortAnimTime));
                mMessageDuration = a.getInteger(R.styleable.MicrophoneBarStyle_message_duration,
                        DEFAULT_MESSAGE_DURATION);

                mMessageColor = a.getColor(R.styleable.MicrophoneBarStyle_message_color, Color.BLACK);

                // Button related attributes
                final int defaultSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        DEFAULT_BUTTON_HEIGHT, displayMetrics);
                mButtonSize = a.getDimensionPixelSize(R.styleable.MicrophoneBarStyle_button_size, defaultSize);
                mButtonImageDrawable = a.getDrawable(R.styleable.MicrophoneBarStyle_src);
                if (mButtonImageDrawable == null) {
                    mButtonImageDrawable = getResources().getDrawable(R.drawable.ic_microphone);
                }
                setState(STATE_DISABLED);
            } finally {
                a.recycle();
            }
        }
    }

    private void updateMicrophoneViewProperties() {
        mButtonView.setImageDrawable(mButtonImageDrawable);

        // Update view size
        final ViewGroup.LayoutParams lp = mButtonView.getLayoutParams();
        lp.height = mButtonSize;
        lp.width = mButtonSize;
        mButtonView.setLayoutParams(lp);

        updateMicrophoneViewBackground();
    }

    private void updateMicrophoneViewBackground() {
        final ColorSet cs = getColorSetForState(mState);

        final StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_pressed },
                createOvalDrawable(cs.colorPressed));
        drawable.addState(new int[] { android.R.attr.state_enabled }, createOvalDrawable(Color.TRANSPARENT));
        drawable.addState(new int[] { }, createOvalDrawable(Color.LTGRAY));

        ViewUtils.setBackgroundCompat(mButtonView, drawable);
        mButtonView.setEnabled(mState != STATE_DISABLED);
    }

    private Drawable createOvalDrawable(final int color) {
        final OvalShape ovalShape = new OvalShape();
        final ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    private void updateBackgroundDrawable(final @MicrophoneBarBackgroundDrawable.Direction int direction,
                                          final boolean isAnimateButtonColor) {
        if (TextUtilsExt.isEmpty(mMessage)) {
            mBackgroundDrawable.setBackgroundColor(getBackgroundColor(mState), direction);
        }
        mBackgroundDrawable.setColorSet(getColorSetForState(mState), isAnimateButtonColor);
        mBackgroundDrawable.setButtonRadius(mButtonSize / 2);
    }

    private int getBackgroundColor(final int state) {
        final int color;
        switch (state) {
            case STATE_IN_PROGRESS:
            case STATE_STOPPING:
                color = ColorUtils.setAlpha(mBackgroundColorWithMessage, OPACITY_90);
                break;

            case STATE_RECORDING:
                // It is because on Lollipop devices ripple effect draws under container background
                final int opacity = (ApplicationUtils.isLollipop() ? OPACITY_0 : OPACITY_100);
                color = ColorUtils.setAlpha(mBackgroundColorActive, opacity);
                break;

            default:
                color = ColorUtils.setAlpha(mBackgroundColorWithMessage, OPACITY_0);
                break;
        }
        return color;
    }

    @MicrophoneBarBackgroundDrawable.Direction
    private int getBackgroundAnimationDirection() {
        final @MicrophoneBarBackgroundDrawable.Direction int direction;
        switch (mState) {
            case STATE_IDLE:
            case STATE_IN_PROGRESS:
                direction = MicrophoneBarBackgroundDrawable.DIRECTION_NONE;
                break;

            case STATE_RECORDING:
                direction = MicrophoneBarBackgroundDrawable.DIRECTION_FROM_CENTER;
                break;

            default:
                direction = MicrophoneBarBackgroundDrawable.DIRECTION_TO_CENTER;
                break;
        }
        return direction;
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        super.onLayout(changed, l, t, r, b);
        final int halfSize = mButtonSize / 2;

        final ViewGroup parent = (ViewGroup) mButtonView.getParent();
        final int x = mButtonView.getLeft() + halfSize + (parent != null ? parent.getLeft() : 0);
        final int y = mButtonView.getTop() + halfSize + (parent != null ? parent.getTop() : 0);
        mBackgroundDrawable.setButtonPosition(x, y);
    }

    @Override
    public void setMicrophoneBarListener(final MicrophoneBarListener listener) {
        mMicrophoneBarListener = listener;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void setState(@State final int newState) {
        if (mState != newState) {
            mProgressBar.setVisibility(INVISIBLE);
            mBackgroundDrawable.expandMicrophoneButton();

            removeCallbacks(mVolumeThresholdObserver);
            mHasVolumeCallbacks = false;
            mVolumeThresholdObserver.run();

            removeCallbacks(mVolumeErrorObserver);

            mState = newState;
            mIsPostDelayVolume = true;

            updateMicrophoneViewBackground();
            updateBackgroundDrawable(getBackgroundAnimationDirection(), true);

            mBackgroundDrawable.play(null);
            mBackgroundDrawable.setVolume(0.f, true);

            if (mState == STATE_IN_PROGRESS) {
                mProgressBar.setVisibility(VISIBLE);
                mBackgroundDrawable.collapseMicrophoneButton();

            } else if (mState == STATE_RECORDING) {
                showMessage(getResources().getText(R.string.recording), NOTIFICATION_MESSAGE);
                animateStartRecording();

            } else if (mState == STATE_STOPPING) {
                showMessage(getResources().getText(R.string.microphone_turn_off), NOTIFICATION_MESSAGE);
            }
        }
    }

    @Override
    public void onStartPlaying() {
        removeCallbacks(mVolumeErrorObserver);
        removeCallbacks(mVolumeThresholdObserver);
        mHasVolumeCallbacks = false;
        mIsPostDelayVolume = true;
        mBackgroundDrawable.setVolume(0.f, false);
    }

    @Override
    public void onStopPlaying() {
        postDelayed(mVolumeThresholdObserver, VOLUME_OBSERVER_TIMEOUT);
        mHasVolumeCallbacks = true;

        mIsPostDelayVolume = false;
    }

    private void animateStartRecording() {
        final AbstractBackgroundAnimation animation = new ClickExplodeBackgroundAnimation(
                mBackgroundDrawable, mRecordingColorSet);
        mBackgroundDrawable.play(animation);
    }

    private ColorSet getColorSetForState(@State final int state) {
        final ColorSet colorSet;
        if (!TextUtilsExt.isEmpty(mMessage) && mMessageType == ERROR_MESSAGE) {
            colorSet = mErrorColorSet;

        } else if (state == STATE_IN_PROGRESS) {
            colorSet = mInProgressColorSet;

        } else if (state == STATE_RECORDING) {
            colorSet = mRecordingColorSet;

        } else {
            colorSet = mNormalColorSet;
        }
        return colorSet;
    }

    @Override
    public void setMessageDuration(final long timeInMillis) {
        mMessageDuration = timeInMillis;
    }

    @Override
    public void showMessage(@NonNull final CharSequence message) {
        showMessage(message, NOTIFICATION_MESSAGE);
    }

    @Override
    public void showErrorMessage(@NonNull final CharSequence message) {
        showMessage(message, ERROR_MESSAGE);
    }

    private void showMessage(@NonNull final CharSequence message, final int messageType) {
        removeCallbacks(mShowMessageRunnable);
        removeCallbacks(mHideMessageRunnable);
        removeCallbacks(mPlayWaitingAnimationRunnable);

        mMessage = message;
        mMessageType = messageType;

        final boolean hasError = (messageType == ERROR_MESSAGE);
        mIsPostDelayVolume = hasError;
        if (hasError) {
            mBackgroundDrawable.play(null);
            mBackgroundDrawable.setVolume(0.f, false);
        }

        updateMicrophoneViewBackground();
        updateBackgroundDrawable(MicrophoneBarBackgroundDrawable.DIRECTION_NONE, false);

        final int delay = getResources().getInteger(android.R.integer.config_longAnimTime);
        mBackgroundDrawable.setBackgroundColor(getBackgroundColorForMessage(),
                MicrophoneBarBackgroundDrawable.DIRECTION_NONE, delay);
        if (hasError) {
            mBackgroundDrawable.play(new ClickExplodeBackgroundAnimation(mBackgroundDrawable, mErrorColorSet));
        }

        postDelayed(mShowMessageRunnable, delay);
    }

    private int getBackgroundColorForMessage() {
        final int opacity = (ApplicationUtils.isLollipop() ? OPACITY_0 : OPACITY_90);
        final int color;
        if (mMessageType == ERROR_MESSAGE) {
            color = ColorUtils.setAlpha(mBackgroundColorError, OPACITY_100);

        } else if (mState == STATE_RECORDING) {
            color = ColorUtils.setAlpha(mBackgroundColorActive, opacity);

        } else {
            color = ColorUtils.setAlpha(mBackgroundColorWithMessage, opacity);
        }
        return color;
    }

    private final Runnable mShowMessageRunnable = new Runnable() {
        @Override
        public void run() {
            dismissMessagePopup();

            if (mAttachedToWindow && !TextUtilsExt.isEmpty(mMessage)) {
                final LayoutInflater inflater = LayoutInflater.from(getContext());
                final View popupView = inflater.inflate(R.layout.view_microphone_bar_message, MicrophoneBarView.this, false);
                final TextView messageView = (TextView) popupView.findViewById(R.id.microphone_bar_message);
                messageView.setTextColor(mMessageColor);
                messageView.setText(mMessage);

                popupView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                final int popupWidth = popupView.getMeasuredWidth();
                final int popupHeight = popupView.getMeasuredHeight();
                final int yOffset = getResources().getDimensionPixelOffset(R.dimen.dimen_4dp);
                final int xOffset = (mButtonView.getLeft() + mButtonView.getMeasuredWidth() / 2) - (popupWidth / 2);

                mPopupWindow = new PopupWindow(popupView, popupWidth, popupHeight);
                mPopupWindow.setBackgroundDrawable(null);
                mPopupWindow.setFocusable(false);
                mPopupWindow.setOutsideTouchable(false);
                mPopupWindow.showAsDropDown(mButtonView, xOffset, yOffset);
            }

            postDelayed(mHideMessageRunnable, mMessageDuration);
        }
    };

    private final Runnable mHideMessageRunnable = new Runnable() {
        @Override
        public void run() {
            postDelayed(mClearMessageRunnable, mMessageAppearanceDuration);
        }
    };

    private final Runnable mClearMessageRunnable = new Runnable() {
        @Override
        public void run() {
            mMessage = "";
            mMessageType = -1;
            dismissMessagePopup();

            updateMicrophoneViewBackground();
            if (mState == STATE_STOPPING) {
                setState(STATE_IDLE);
            } else {
                updateBackgroundDrawable(MicrophoneBarBackgroundDrawable.DIRECTION_NONE, true);
                if (mState == STATE_RECORDING) {
                    postDelayed(mPlayWaitingAnimationRunnable, VOLUME_OBSERVER_TIMEOUT);
                }
            }

            mIsPostDelayVolume = false;
        }
    };

    /* package */ void dismissMessagePopup() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }

    @Override
    public void setVolume(final float volume) {
        if (mState == STATE_RECORDING) {
            if (volume >= VOLUME_THRESHOLD && volume <= 1.f) {
                removeCallbacks(mVolumeErrorObserver);
                if (!mIsPostDelayVolume) {
                    removeCallbacks(mVolumeThresholdObserver);
                    mHasVolumeCallbacks = false;
                    mBackgroundDrawable.setVolume(volume, true);
                }

            } else if (volume < VOLUME_THRESHOLD || volume > 1.f) {
                mBackgroundDrawable.setVolume(0.f, false);
                if (!mHasVolumeCallbacks) {
                    postDelayed(mVolumeThresholdObserver, VOLUME_OBSERVER_TIMEOUT);
                    mHasVolumeCallbacks = true;
                }
            }
        }
    }

    private final Runnable mVolumeThresholdObserver = new Runnable() {
        @Override
        public void run() {
            if (mState == STATE_RECORDING) {
                removeCallbacks(mVolumeThresholdObserver);
                mBackgroundDrawable.setVolume(0.f, false);
                if (mMessageType != ERROR_MESSAGE) {
                    post(mPlayWaitingAnimationRunnable);
                }
                postDelayed(mVolumeErrorObserver, VOLUME_OBSERVER_ERROR_TIMEOUT);
            }
        }
    };

    private final Runnable mVolumeErrorObserver = new Runnable() {
        @Override
        public void run() {
            mBackgroundDrawable.setVolume(0.f, false);
            Analytics.onMicrophoneBarMessage(Analytics.MICROPHONE_BAR_MESSAGE_SPEAK_LOUDER);
            showErrorMessage(getResources().getText(R.string.speak_louder));
        }
    };

    private final Runnable mPlayWaitingAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            mBackgroundDrawable.play(
                    new WaitingBackgroundAnimation(mBackgroundDrawable, mRecordingColorSet));
        }
    };

    @Override
    public void onClick(final View v) {
        if (mMicrophoneBarListener != null) {
            mMicrophoneBarListener.onMicrophoneClicked(this, mState);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        mButtonView.getLocationOnScreen(mSelfLocation);
        final int x = (int) ev.getRawX() - mSelfLocation[0];
        final int y = (int) ev.getRawY() - mSelfLocation[1];
        mButtonView.getHitRect(mButtonRect);
        return !mButtonRect.contains(x, y);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        dismissMessagePopup();
        mAttachedToWindow = false;
        super.onDetachedFromWindow();
    }

}