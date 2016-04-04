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
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.TextView;

import ru.yandex.subtitles.utils.TextUtilsExt;
import ru.yandex.subtitles.utils.ViewUtils;

public class YandexEditText extends AppCompatEditText {

    public interface OnKeyboardHideListener {

        void onKeyboardHide(final TextView view);

    }

    public interface OnSelectionChangeListener {

        void onSelectionChanged(final TextView view, final int start, final int end);

    }

    public interface OnTextContextMenuListener {

        void onTextCopy(final TextView view, final String text);

        void onTextCut(final TextView view, final String text);

        void onTextPaste(final TextView view, final String text);

    }

    public interface OnTextChangeListener {

        void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter);

    }

    private OnKeyboardHideListener mKeyboardHideListener;
    private OnSelectionChangeListener mSelectionChangeListener;
    private OnTextContextMenuListener mTextContextMenuListener;
    private OnTextChangeListener mTextChangeListener;

    public YandexEditText(final Context context) {
        this(context, null);
    }

    public YandexEditText(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YandexEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnKeyboardHideListener(final OnKeyboardHideListener keyboardHideListener) {
        mKeyboardHideListener = keyboardHideListener;
    }

    public void setOnSelectionChangeListener(final OnSelectionChangeListener selectionChangeListener) {
        mSelectionChangeListener = selectionChangeListener;
    }

    public void setTextContextMenuListener(final OnTextContextMenuListener textContextMenuListener) {
        mTextContextMenuListener = textContextMenuListener;
    }

    public void setOnTextChangeListener(final OnTextChangeListener textChangeListener) {
        mTextChangeListener = textChangeListener;
    }

    @Override
    public boolean onKeyPreIme(final int keyCode, @NonNull final KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mKeyboardHideListener != null && ViewUtils.hideSoftwareKeyboard(this)) {
                mKeyboardHideListener.onKeyboardHide(this);
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    protected void onSelectionChanged(final int selStart, final int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (mSelectionChangeListener != null) {
            mSelectionChangeListener.onSelectionChanged(this, selStart, selEnd);
        }
    }

    @Override
    public boolean onTextContextMenuItem(final int id) {
        int min = 0;
        int max = 0;
        String text = getText().toString();
        if (isFocused()) {
            final int selStart = getSelectionStart();
            final int selEnd = getSelectionEnd();

            min = Math.max(0, Math.min(selStart, selEnd));
            max = Math.max(0, Math.max(selStart, selEnd));
            text = TextUtilsExt.safeSubString(text, min, max);
        }

        final boolean handled = super.onTextContextMenuItem(id);
        if (mTextContextMenuListener != null) {
            switch (id) {
                case android.R.id.cut:
                    mTextContextMenuListener.onTextCut(this, text);
                    break;

                case android.R.id.paste:
                    mTextContextMenuListener.onTextPaste(this, getText().toString());
                    break;

                case android.R.id.copy:
                    mTextContextMenuListener.onTextCopy(this, text);
                    break;
            }
        }

        return handled;
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChanged(text, start, lengthBefore, lengthAfter);
        }
    }

}
