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

import com.nineoldandroids.animation.TypeEvaluator;

/**
 * This evaluator can be used to perform type interpolation between integer
 * values that represent ARGB colors.
 * <p/>
 * Fixed issue:
 * https://code.google.com/p/android/issues/detail?id=36158
 */
public class ArgbEvaluator implements TypeEvaluator {

    /**
     * This function returns the calculated in-between value for a color
     * given integers that represent the start and end values in the four
     * bytes of the 32-bit int. Each channel is separately linearly interpolated
     * and the resulting calculated values are recombined into the return value.
     *
     * @param fraction   The fraction from the starting to the ending values
     * @param startValue A 32-bit int value representing colors in the
     *                   separate bytes of the parameter
     * @param endValue   A 32-bit int value representing colors in the
     *                   separate bytes of the parameter
     * @return A value that is calculated to be the linearly interpolated
     * result, derived by separating the start and end values into separate
     * color channels and interpolating each one separately, recombining the
     * resulting values in the same way.
     */
    public Object evaluate(final float fraction, final Object startValue, final Object endValue) {
        final int startInt = (Integer) startValue;
        final int startA = (startInt >> 24) & 0xff;
        final int startR = (startInt >> 16) & 0xff;
        final int startG = (startInt >> 8) & 0xff;
        final int startB = startInt & 0xff;

        final int endInt = (Integer) endValue;
        final int endA = (endInt >> 24) & 0xff;
        final int endR = (endInt >> 16) & 0xff;
        final int endG = (endInt >> 8) & 0xff;
        final int endB = endInt & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));
    }
}