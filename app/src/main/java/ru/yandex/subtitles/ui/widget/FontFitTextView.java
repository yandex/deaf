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
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class FontFitTextView extends AppCompatTextView {

    private static final float MAX_TEXT_SIZE = 120f;

    private final Paint mPaint = new Paint();

    public FontFitTextView(final Context context) {
        this(context, null);
    }

    public FontFitTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontFitTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private void refitText(final String text, final int textWidth, final int textHeight) {
        if (textWidth <= 0) {
            return;
        }

        final int targetWidth = (textWidth - getPaddingLeft() - getPaddingRight());
        float textSize = MAX_TEXT_SIZE;

        final TypedValue outValue = new TypedValue();
        getResources().getValue(R.integer.scale_font_ratio, outValue, true);
        float value = outValue.getFloat();

        mPaint.set(getPaint());

        final String longestWord = getLongestWord(text);
        while (textSize > 0f) {
            float size = textSize * value;
            mPaint.setTextSize(size);
            if (canFit(text, longestWord, size, targetWidth, textHeight)) {
                textSize = size;
                break;

            } else {
                textSize = textSize - 4f;
            }

        }

        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    private boolean canFit(final String text, final String longestWord, final float size,
                           final int w, final int h) {
        if (TextUtilsExt.isEmpty(text)) {
            return false;
        }

        final float letterWidth = mPaint.measureText(text) / text.length();
        final float letterHeight = size * 2.3f;
        final int lettersPerRow = Math.round(w / letterWidth);
        final int rowsNeeded = Math.round(text.length() / lettersPerRow);
        final int minimalHeight = Math.round(rowsNeeded * letterHeight);

        final int longestWordLength = longestWord.length();

        return ((Math.round(longestWordLength * letterWidth) < w) && (minimalHeight < h));
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        refitText(getText().toString(), width, height);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(getText().toString(), getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        if (w != oldw) {
            refitText(getText().toString(), w, h);
        }
    }

    public static String getLongestWord(final String sentence) {
        final int i = sentence.indexOf(' ');
        if (i == -1) {
            return sentence;
        }

        final String first = sentence.substring(0, i).trim();
        final String rest = sentence.substring(i).trim();
        return compare(first, getLongestWord(rest));
    }

    public static String compare(final String st1, final String st2) {
        return (st1.length() > st2.length() ? st1 : st2);
    }

}
