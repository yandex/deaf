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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public class ClickExplodeBackgroundAnimation extends AbstractBackgroundAnimation {

    private final static int OUTER_ALPHA = 92;

    private final ColorSet mColorSet;

    private final Point mCenter;
    private int mOuterCircleColor = Color.TRANSPARENT;

    // Animators
    private ValueAnimator mInnerCircleRadiusAnimator;
    private ValueAnimator mOuterCircleRadiusAnimator;

    public ClickExplodeBackgroundAnimation(final MicrophoneBarBackgroundDrawable drawable,
                                           final ColorSet colorSet) {
        super(drawable);
        mColorSet = colorSet;
        mCenter = drawable.getButtonPosition();

        onPrepareAnimation();
    }

    public void onPrepareAnimation() {
        mOuterCircleColor = Color.argb(OUTER_ALPHA,
                Color.red(mColorSet.colorNormal),
                Color.green(mColorSet.colorNormal),
                Color.blue(mColorSet.colorNormal));

        final Rect bounds = mDrawable.getBounds();
        final int buttonRadius = mDrawable.getButtonRadius();

        mInnerCircleRadiusAnimator = new ValueAnimator();
        mInnerCircleRadiusAnimator.setIntValues(buttonRadius,
                (bounds.height() - buttonRadius) / 8 + buttonRadius,
                bounds.height() / 2,
                (bounds.height() - buttonRadius) / 8 + buttonRadius,
                (int) (buttonRadius * 0.85f),
                buttonRadius);
        mInnerCircleRadiusAnimator.setInterpolator(ACCELERATE_DECELERATE_INTEPROLATOR);
        mInnerCircleRadiusAnimator.setEvaluator(new IntEvaluator());
        mInnerCircleRadiusAnimator.setTarget(mInnerCircleRadius);
        mInnerCircleRadiusAnimator.setDuration(500L);
        mInnerCircleRadiusAnimator.addUpdateListener(this);

        mOuterCircleRadiusAnimator = new ValueAnimator();
        mOuterCircleRadiusAnimator.setIntValues(buttonRadius,
                bounds.height() / 2 + buttonRadius / 2,
                (int) (bounds.height() / 2.f),
                (int) (bounds.height() / 2.05f),
                (int) (bounds.height() / 2.f),
                (int) (bounds.height() / 2.05f),
                buttonRadius);
        mOuterCircleRadiusAnimator.setInterpolator(ACCELERATE_DECELERATE_INTEPROLATOR);
        mOuterCircleRadiusAnimator.setEvaluator(new IntEvaluator());
        mOuterCircleRadiusAnimator.setTarget(mInnerCircleRadius);
        mOuterCircleRadiusAnimator.setDuration(1200L);
        mOuterCircleRadiusAnimator.addUpdateListener(this);

        mAnimatorSet.playTogether(mInnerCircleRadiusAnimator,
                mOuterCircleRadiusAnimator);
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    private int mOuterCircleRadius = 0;
    private int mInnerCircleRadius = 0;

    @Override
    public void draw(final Canvas canvas, final Paint paint, final MicrophoneBarBackgroundDrawable drawable) {
        // Outer circle
        paint.setColor(mOuterCircleColor);
        canvas.drawCircle(mCenter.x, mCenter.y, mOuterCircleRadius, paint);

        // Inner circle
        paint.setColor(mColorSet.colorNormal);
        canvas.drawCircle(mCenter.x, mCenter.y, mInnerCircleRadius, paint);
    }

    @Override
    public void onAnimationUpdate(final ValueAnimator animation) {
        if (animation.equals(mInnerCircleRadiusAnimator)) {
            mInnerCircleRadius = (Integer) animation.getAnimatedValue();
        } else if (animation.equals(mOuterCircleRadiusAnimator)) {
            mOuterCircleRadius = (Integer) animation.getAnimatedValue();
        }
        super.onAnimationUpdate(animation);
    }

}