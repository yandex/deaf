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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public abstract class AbstractAnimator {

    protected static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    protected static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    private Runnable mOnStartAnimationRunnable;
    private Runnable mOnEndAnimationRunnable;

    private AnimatorSet mAnimatorSet;

    public AbstractAnimator() {
        mAnimatorSet = new AnimatorSet();
    }

    public void setDuration(final long duration) {
        mAnimatorSet.setDuration(duration);
    }

    public void setTarget(final Object target) {
        mAnimatorSet.setTarget(target);
    }

    public void setInterpolator(final Interpolator interpolator) {
        mAnimatorSet.setInterpolator(interpolator);
    }

    public void runOnStartAnimation(final Runnable onStartAnimationRunnable) {
        mOnStartAnimationRunnable = onStartAnimationRunnable;
    }

    public void runOnEndAnimation(final Runnable onEndAnimationRunnable) {
        mOnEndAnimationRunnable = onEndAnimationRunnable;
    }

    public void playTogether(Animator... items) {
        mAnimatorSet.playTogether(items);
    }

    public void animate() {
        mAnimatorSet.addListener(makeAnimatorListener());
        mAnimatorSet.start();
    }

    private Animator.AnimatorListener makeAnimatorListener() {
        return new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(final Animator animation) {
                runSafe(mOnStartAnimationRunnable);
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                runSafe(mOnEndAnimationRunnable);
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

        };
    }

    private void runSafe(final Runnable r) {
        if (r != null) {
            r.run();
        }
    }

}