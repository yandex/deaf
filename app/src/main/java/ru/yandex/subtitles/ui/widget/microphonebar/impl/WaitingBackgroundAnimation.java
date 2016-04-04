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

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class WaitingBackgroundAnimation extends AbstractBackgroundAnimation {

    private final ColorSet mColorSet;

    private final Point mCenter;

    private ValueAnimator mAlphaAnimator;
    private ValueAnimator mRadiusAnimator;
    private ValueAnimator mButtonAnimator;

    public WaitingBackgroundAnimation(final MicrophoneBarBackgroundDrawable drawable,
                                      final ColorSet colorSet) {
        super(drawable);
        mColorSet = colorSet;
        mCenter = drawable.getButtonPosition();

        onPrepareAnimation();
    }

    public void onPrepareAnimation() {
        mAlphaAnimator = ValueAnimator.ofInt(0, 96);
        mAlphaAnimator.setInterpolator(ACCELERATE_DECELERATE_INTEPROLATOR);
        mAlphaAnimator.setEvaluator(new IntEvaluator());
        mAlphaAnimator.setTarget(mBackgroundCircleAlpha);
        mAlphaAnimator.setDuration(1800L);
        mAlphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAlphaAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAlphaAnimator.addListener(this);
        mAlphaAnimator.addUpdateListener(this);

        final int startRadius = (int) Math.round(Math.sqrt(Math.pow(mCenter.x, 2)
                + Math.pow(mCenter.y, 2)));

        mRadiusAnimator = ValueAnimator.ofInt(startRadius, 0);
        mRadiusAnimator.setInterpolator(ACCELERATE_DECELERATE_INTEPROLATOR);
        mRadiusAnimator.setEvaluator(new IntEvaluator());
        mRadiusAnimator.setTarget(mBackgroundCircleRadius);
        mRadiusAnimator.setDuration(1800L);
        mRadiusAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRadiusAnimator.setRepeatMode(ValueAnimator.RESTART);
        mRadiusAnimator.addUpdateListener(this);

        mAnimatorSet.playTogether(mAlphaAnimator, mRadiusAnimator);

        final int buttonRadius = mDrawable.getButtonRadius();
        mButtonAnimator = ValueAnimator.ofInt(buttonRadius, (int) (buttonRadius * 0.9f), buttonRadius);
        mButtonAnimator.setInterpolator(ACCELERATE_DECELERATE_INTEPROLATOR);
        mButtonAnimator.setEvaluator(new IntEvaluator());
        mButtonAnimator.setTarget(mButtonRadius);
        mButtonAnimator.setDuration(200L);
        mButtonAnimator.addUpdateListener(this);
    }

    @Override
    public boolean isInfinite() {
        return true;
    }

    private int mBackgroundCircleRadius;
    private int mBackgroundCircleAlpha;
    private int mButtonRadius;

    @Override
    public void draw(final Canvas canvas, final Paint paint, final MicrophoneBarBackgroundDrawable drawable) {
        // Background circle
        final int color = (mBackgroundCircleAlpha << 24) | (mColorSet.colorNormal & 0x00ffffff);
        paint.setColor(color);
        canvas.drawCircle(mCenter.x, mCenter.y, mBackgroundCircleRadius, paint);

        // Button
        paint.setColor(mColorSet.colorNormal);
        canvas.drawCircle(mCenter.x, mCenter.y, mButtonRadius, paint);
    }

    @Override
    public void onAnimationUpdate(final ValueAnimator animation) {
        if (animation.equals(mAlphaAnimator)) {
            mBackgroundCircleAlpha = (Integer) animation.getAnimatedValue();
        } else if (animation.equals(mRadiusAnimator)) {
            mBackgroundCircleRadius = (Integer) animation.getAnimatedValue();

            if (!mButtonAnimator.isRunning()) {
                final int buttonRadius = mDrawable.getButtonRadius();
                mButtonRadius = buttonRadius;
                if (mBackgroundCircleRadius <= buttonRadius) {
                    mButtonAnimator.start();
                }
            }
        } else if (animation.equals(mButtonAnimator)) {
            mButtonRadius = (Integer) animation.getAnimatedValue();
        }
        super.onAnimationUpdate(animation);
    }

}
