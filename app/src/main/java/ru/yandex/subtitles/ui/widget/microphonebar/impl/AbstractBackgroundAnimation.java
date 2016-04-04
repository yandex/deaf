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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public abstract class AbstractBackgroundAnimation implements Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {

    protected final static Interpolator ACCELERATE_DECELERATE_INTEPROLATOR = new AccelerateDecelerateInterpolator();

    protected AnimatorSet mAnimatorSet = new AnimatorSet();
    protected MicrophoneBarBackgroundDrawable mDrawable;

    public AbstractBackgroundAnimation(final MicrophoneBarBackgroundDrawable drawable) {
        mDrawable = drawable;
        mAnimatorSet.addListener(this);
    }

    public boolean isRunning() {
        return mAnimatorSet.isRunning();
    }

    public void start() {
        mAnimatorSet.start();
    }

    public void stop() {
        mAnimatorSet.cancel();
        mAnimatorSet.removeListener(this);
    }

    public abstract boolean isInfinite();

    public abstract void draw(final Canvas canvas, final Paint paint, MicrophoneBarBackgroundDrawable drawable);

    @Override
    public void onAnimationStart(final Animator animation) {
        invalidateDrawable();
    }

    @Override
    public void onAnimationEnd(final Animator animation) {
    }

    @Override
    public void onAnimationCancel(final Animator animation) {
    }

    @Override
    public void onAnimationRepeat(final Animator animation) {
    }

    @Override
    public void onAnimationUpdate(final ValueAnimator animation) {
        invalidateDrawable();
    }

    private void invalidateDrawable() {
        if (mDrawable != null) {
            mDrawable.invalidateSelf();
        }
    }

}
