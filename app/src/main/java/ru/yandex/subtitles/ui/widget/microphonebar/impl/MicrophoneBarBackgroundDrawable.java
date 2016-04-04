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
package ru.yandex.subtitles.ui.widget.microphonebar.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.IntEvaluator;
import com.nineoldandroids.animation.ValueAnimator;

import ru.yandex.subtitles.ui.widget.microphonebar.ArgbEvaluator;
import ru.yandex.subtitles.ui.widget.microphonebar.ColorUtils;

public class MicrophoneBarBackgroundDrawable extends Drawable implements Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {

    private static final String LOG_TAG = "MicrophoneBarBackgroundDrawable";

    private static final int CIRCLE_ALPHA = 40;
    private static final int VOLUME_CIRCLES = 10;
    private static final float VOLUME_RELATIVE_RADIUS = 0.7f;

    public static final int DIRECTION_NONE = -1;
    public static final int DIRECTION_FROM_CENTER = 0;
    public static final int DIRECTION_TO_CENTER = 1;

    @IntDef({ DIRECTION_FROM_CENTER, DIRECTION_TO_CENTER, DIRECTION_NONE })
    public @interface Direction {
    }

    private Context mContext;

    private Point mCenter;
    private ValueAnimator mButtonSizeAnimator;
    private int mButtonRadius;
    private int mFinalButtonRadius;
    private int mCurrentButtonRadius;

    private Paint mPaint;
    private ColorSet mColorSet;
    private ValueAnimator mButtonColorAnimator;
    private int mCurrentButtonColor;

    private AbstractBackgroundAnimation mAnimation;

    private ValueAnimator mBackgroundAnimator;
    private int mCurrentBackgroundColor = Color.TRANSPARENT;
    private int mBackgroundColor = Color.TRANSPARENT;
    private int mStableBackgroundColor = Color.TRANSPARENT;
    @Direction
    private int mBackgroundAnimationDirection = DIRECTION_FROM_CENTER;
    private float mBackgroundAnimationFraction = 0.f;

    private boolean mIsVolumeShowing = false;
    private float mVolumePower = 0.f;

    public MicrophoneBarBackgroundDrawable(final Context context) {
        mContext = context;
        mCenter = new Point();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setButtonPosition(final int x, final int y) {
        mCenter.set(x, y);
        invalidateSelf();
    }

    public Point getButtonPosition() {
        return mCenter;
    }

    public void setButtonRadius(final int buttonRadius) {
        if (mButtonRadius != buttonRadius) {
            mCurrentButtonRadius = buttonRadius;
        }
        mButtonRadius = buttonRadius;
        invalidateSelf();
    }

    public int getButtonRadius() {
        return mButtonRadius;
    }

    public void expandMicrophoneButton() {
        animateMicrophoneButtonSize(mButtonRadius);
    }

    public void collapseMicrophoneButton() {
        animateMicrophoneButtonSize(0);
    }

    private void animateMicrophoneButtonSize(final int size) {
        if (mButtonSizeAnimator != null && mButtonSizeAnimator.isRunning()) {
            mButtonSizeAnimator.cancel();
        }

        mFinalButtonRadius = size;

        mButtonSizeAnimator = ValueAnimator.ofInt(mCurrentButtonRadius, size);
        mButtonSizeAnimator.setEvaluator(new IntEvaluator());
        mButtonSizeAnimator.setTarget(mCurrentButtonRadius);
        mButtonSizeAnimator.setDuration(mContext.getResources()
                .getInteger(android.R.integer.config_shortAnimTime));
        mButtonSizeAnimator.addUpdateListener(this);
        mButtonSizeAnimator.addListener(this);
        mButtonSizeAnimator.start();
    }

    public void setColorSet(final ColorSet colorSet, final boolean isAnimateButtonColor) {
        if (mButtonColorAnimator != null && mButtonColorAnimator.isRunning()) {
            mButtonColorAnimator.cancel();
        }

        mColorSet = colorSet;
        if (isAnimateButtonColor) {
            mButtonColorAnimator = ValueAnimator.ofInt(mCurrentButtonColor, colorSet.colorNormal);
            mButtonColorAnimator.setEvaluator(new ArgbEvaluator());
            mButtonColorAnimator.setTarget(mCurrentButtonColor);
            mButtonColorAnimator.setDuration(mContext.getResources()
                    .getInteger(android.R.integer.config_shortAnimTime));
            mButtonColorAnimator.addUpdateListener(this);
            mButtonColorAnimator.addListener(this);
            mButtonColorAnimator.start();
        } else {
            mCurrentButtonColor = colorSet.colorNormal;
        }
    }

    public void setBackgroundColor(final int color, final @Direction int direction) {
        setBackgroundColor(color, direction, mContext.getResources()
                .getInteger(android.R.integer.config_longAnimTime));
    }

    public void setBackgroundColor(final int color, final @Direction int direction, final int delay) {
        if (mBackgroundAnimator != null && mBackgroundAnimator.isRunning()) {
            mBackgroundAnimator.cancel();
            mBackgroundAnimationFraction = 0.f;
        }

        mBackgroundColor = color;
        mBackgroundAnimationDirection = direction;

        mBackgroundAnimator = ValueAnimator.ofInt(mCurrentBackgroundColor, color);
        mBackgroundAnimator.setEvaluator(new ArgbEvaluator());
        mBackgroundAnimator.setTarget(mCurrentBackgroundColor);
        mBackgroundAnimator.setDuration(delay);
        mBackgroundAnimator.addUpdateListener(this);
        mBackgroundAnimator.addListener(this);
        mBackgroundAnimator.start();
    }

    @Override
    public void onAnimationUpdate(final ValueAnimator animation) {
        if (animation == mBackgroundAnimator) {
            mBackgroundAnimationFraction = animation.getAnimatedFraction();
            mCurrentBackgroundColor = (Integer) animation.getAnimatedValue();

        } else if (animation == mButtonSizeAnimator) {
            mCurrentButtonRadius = (Integer) animation.getAnimatedValue();

        } else if (animation == mButtonColorAnimator) {
            mCurrentButtonColor = (Integer) animation.getAnimatedValue();
        }
        invalidateSelf();
    }

    @Override
    public void onAnimationStart(final Animator animation) {
    }

    @Override
    public void onAnimationEnd(final Animator animation) {
        if (animation == mBackgroundAnimator) {
            mBackgroundAnimationFraction = 1.f;
            mCurrentBackgroundColor = mBackgroundColor;
            mStableBackgroundColor = mBackgroundColor;

        } else if (animation == mButtonSizeAnimator) {
            mCurrentButtonRadius = mFinalButtonRadius;

        } else if (animation == mButtonColorAnimator) {
            mCurrentButtonColor = mColorSet.colorNormal;
        }
        invalidateSelf();
    }

    @Override
    public void onAnimationCancel(final Animator animation) {
    }

    @Override
    public void onAnimationRepeat(final Animator animation) {
    }

    @Override
    public void draw(final Canvas canvas) {
        // Background
        if (mCurrentBackgroundColor == mStableBackgroundColor) {
            // Stable background color
            mPaint.setColor(mStableBackgroundColor);
            canvas.drawPaint(mPaint);

        } else {
            mPaint.setColor(mCurrentBackgroundColor);
            if (mBackgroundAnimationDirection == DIRECTION_NONE) {
                canvas.drawPaint(mPaint);
            } else {
                /*final int radius = (int) Math.round(Math.sqrt(Math.pow(mCenter.x, 2)
                       + Math.pow(mCenter.y, 2)));*/

                final float backgroundRadius;
                final int width = getBounds().width();
                if (mBackgroundAnimationDirection == DIRECTION_TO_CENTER) {
                    backgroundRadius = (1.f - mBackgroundAnimationFraction) * width;
                } else {
                    backgroundRadius = mBackgroundAnimationFraction * width;
                }
                canvas.drawCircle(mCenter.x, mCenter.y, backgroundRadius, mPaint);
            }
        }

        if (mColorSet != null) {
            if (mAnimation != null && !mIsVolumeShowing && mAnimation.isRunning()) {
                mAnimation.draw(canvas, mPaint, this);
            } else {
                drawVolumeCircles(canvas);

                // Button circle
                mPaint.setColor(mCurrentButtonColor);
                canvas.drawCircle(mCenter.x, mCenter.y, mCurrentButtonRadius, mPaint);
            }
        }
    }

    private void drawVolumeCircles(final Canvas canvas) {
        final int color = ColorUtils.setAlpha(mColorSet.colorNormal, CIRCLE_ALPHA);
        mPaint.setColor(color);

        final int circles = (int) (mVolumePower * VOLUME_CIRCLES);
        for (int i = 0; i < circles; i++) {
            final int radius = (int) (mButtonRadius + VOLUME_RELATIVE_RADIUS * (i + 1) * mButtonRadius);
            canvas.drawCircle(mCenter.x, mCenter.y, radius, mPaint);
        }
    }

    public void play(final AbstractBackgroundAnimation animation) {
        if (mAnimation != null && mAnimation.isRunning()) {
            mAnimation.stop();
        }

        mAnimation = animation;
        if (mAnimation != null) {
            mAnimation.start();
        } else {
            invalidateSelf();
        }
    }

    public void setVolume(final float volume, final boolean showVolume) {
        mIsVolumeShowing = showVolume;
        mVolumePower = volume;

        if (mAnimation != null && mAnimation.isInfinite()) {
            if (mIsVolumeShowing && mAnimation.isRunning()) {
                mAnimation.stop();
                mAnimation = null;
            }
        }

        invalidateSelf();
    }

    @Override
    public void setColorFilter(final ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(final int alpha) {
    }

}
