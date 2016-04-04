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
import android.support.annotation.Nullable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.ui.adapter.AbstractRecyclerViewAdapter;
import ru.yandex.subtitles.ui.widget.keyboard.OverKeyboardPopupWindow;
import ru.yandex.subtitles.ui.widget.keyboard.QuickResponsesPopupWindow;
import ru.yandex.subtitles.utils.TextUtilsExt;
import ru.yandex.subtitles.utils.ViewUtils;

public class TypeMessageView extends FrameLayout implements View.OnClickListener,
        TextView.OnEditorActionListener, YandexEditText.OnTextContextMenuListener,
        YandexEditText.OnTextChangeListener, YandexEditText.OnKeyboardHideListener,
        OverKeyboardPopupWindow.OnKeyboardHideListener,
        AbstractRecyclerViewAdapter.OnItemClickListener<Phrase> {

    public interface TypeMessageViewCallbacks {

        void onSendMessageClick(@NonNull final String message);

    }

    private ImageView mQuickResponsesButton;
    private YandexEditText mMessageView;
    private View mSendButton;

    private List<Phrase> mQuickResponses = new ArrayList<Phrase>();
    private QuickResponsesPopupWindow mQuickResponsesPopupWindow;

    private TypeMessageViewCallbacks mTypeMessageViewCallbacks;

    public TypeMessageView(final Context context) {
        this(context, null);
    }

    public TypeMessageView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TypeMessageView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final Context context = getContext();

        final View viewLayout = LayoutInflater.from(context)
                .inflate(R.layout.view_type_message, this, false);
        addView(viewLayout);

        mQuickResponsesButton = ViewUtils.findView(this, R.id.action_quick_responses);
        mQuickResponsesButton.setOnClickListener(this);
        mQuickResponsesButton.setVisibility(GONE);

        mSendButton = ViewUtils.findView(this, R.id.action_send);
        mSendButton.setEnabled(false);
        mSendButton.setOnClickListener(this);

        mMessageView = ViewUtils.findView(this, R.id.text);
        mMessageView.setOnEditorActionListener(this);
        mMessageView.setTextContextMenuListener(this);
        mMessageView.setOnTextChangeListener(this);
        mMessageView.setOnKeyboardHideListener(this);
    }

    public void setTypeMessageViewCallbacks(final TypeMessageViewCallbacks typeMessageViewCallbacks) {
        mTypeMessageViewCallbacks = typeMessageViewCallbacks;
    }

    public void setRootView(@NonNull final View rootView) {
        mQuickResponsesPopupWindow = new QuickResponsesPopupWindow(getContext(), rootView);
        mQuickResponsesPopupWindow.setQuickResponses(mQuickResponses);
        mQuickResponsesPopupWindow.setKeyboardHideListener(this);
        mQuickResponsesPopupWindow.setOnItemClickListener(this);
        mQuickResponsesButton.setVisibility(VISIBLE);
    }

    public void setQuickResponses(@NonNull final List<Phrase> phrases) {
        mQuickResponses.clear();
        mQuickResponses.addAll(phrases);
        if (mQuickResponsesPopupWindow != null) {
            mQuickResponsesPopupWindow.setQuickResponses(mQuickResponses);
        }
    }

    @Override
    public void onTextChanged(final CharSequence text, final int start,
                              final int lengthBefore, final int lengthAfter) {
        mSendButton.setEnabled(!TextUtilsExt.isEmpty(text));
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.action_send:
                onSendClick();
                break;

            case R.id.action_quick_responses:
                onQuickResponsesClick();
                break;
        }
    }

    private void onSendClick() {
        final String text = mMessageView.getText().toString().trim();
        if (TextUtilsExt.isEmpty(text)) {
            ViewUtils.showSoftwareKeyboard(mMessageView);

        } else if (mTypeMessageViewCallbacks != null) {
            mTypeMessageViewCallbacks.onSendMessageClick(text);
            mMessageView.setText(null);
        }
    }

    private void onQuickResponsesClick() {
        // If popup is not showing => quick responses is not visible, we need to show it
        if (!mQuickResponsesPopupWindow.isShowing()) {
            // If keyboard is visible, simply show the  popup
            if (mQuickResponsesPopupWindow.isKeyboardOpen()) {
                mQuickResponsesPopupWindow.showAtBottom();
                mQuickResponsesButton.setImageResource(R.drawable.ic_switch_to_keyboard);

            } else {
                // Open the text keyboard first and immediately after that show the emoji popup
                mMessageView.setFocusableInTouchMode(true);
                mMessageView.requestFocus();
                mQuickResponsesPopupWindow.showAtBottomPending();
                ViewUtils.showSoftwareKeyboard(mMessageView);
                mQuickResponsesButton.setImageResource(R.drawable.ic_switch_to_keyboard);
            }
            Analytics.onQuickResponsesClick();

        } else {
            // If popup is showing, simply dismiss it to show the undelying text keyboard
            hideQuickResponses();
        }
    }

    @Override
    public void onItemClick(final View view, final int position, final Phrase phrase) {
        final String text = phrase.getText();
        if (TextUtilsExt.isEmpty(text)) {
            return;
        }
        Analytics.onQuickResponseSelected(text);

        final Editable editable = mMessageView.getText();

        final int start = Math.min(mMessageView.getSelectionStart(), mMessageView.getSelectionEnd());
        final int end = Math.max(mMessageView.getSelectionStart(), mMessageView.getSelectionEnd());
        if (start < 0) {
            editable.append(text);

        } else if (start != end) {
            editable.replace(Math.min(start, end), Math.max(start, end),
                    text, 0, text.length());

        } else {
            final boolean spaceBefore = (start > 0 && start <= editable.length() && editable.charAt(start - 1) != ' ');

            final StringBuilder sb = new StringBuilder();
            sb.append(spaceBefore ? " " : "").append(text).append(" ");
            editable.replace(Math.min(start, end), Math.max(start, end),
                    sb, 0, sb.length());
        }
    }

    @Override
    public void onKeyboardHide(final TextView view) {
        hideQuickResponses();
    }

    @Override
    public void onKeyboardHide() {
        hideQuickResponses();
    }

    private boolean hideQuickResponses() {
        if (isQuickResponsesShowing()) {
            mQuickResponsesPopupWindow.dismiss();
            mQuickResponsesButton.setImageResource(R.drawable.ic_switch_to_quick_responses);
            return true;
        } else {
            return false;
        }
    }

    private boolean isQuickResponsesShowing() {
        return mQuickResponsesPopupWindow != null && mQuickResponsesPopupWindow.isShowing();
    }

    public void onPause() {
        hideKeyboard();
    }

    public boolean onBackPressed() {
        return hideKeyboard();
    }

    private boolean hideKeyboard() {
        final boolean keyboardHided = ViewUtils.hideSoftwareKeyboard(mMessageView);
        if (keyboardHided || isQuickResponsesShowing()) {
            hideQuickResponses();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onEditorAction(final TextView v, final int actionId, @Nullable final KeyEvent keyEvent) {
        final int keyCode = (keyEvent != null ? keyEvent.getKeyCode() : KeyEvent.KEYCODE_UNKNOWN);
        final int action = (keyEvent != null ? keyEvent.getAction() : -1);
        final boolean isShiftPressed = (keyEvent != null && keyEvent.isShiftPressed());
        final boolean isAltPressed = (keyEvent != null && keyEvent.isAltPressed());
        final boolean isCtrlPressed = (keyEvent != null && keyEvent.isCtrlPressed());

        switch (v.getId()) {
            case R.id.text:
                // Ctrl + Enter to send message, Enter to start new line
                if (keyCode == KeyEvent.KEYCODE_ENTER && action == KeyEvent.ACTION_DOWN &&
                        !isShiftPressed && !isAltPressed && isCtrlPressed) {
                    onSendClick();
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onTextCopy(final TextView view, final String text) {
    }

    @Override
    public void onTextCut(final TextView view, final String text) {
    }

    @Override
    public void onTextPaste(final TextView view, final String text) {
        Analytics.onConversationTextPaste();
    }

    public void showKeyboard() {
        mMessageView.requestFocus();
        ViewUtils.showSoftwareKeyboard(mMessageView);
    }

}
