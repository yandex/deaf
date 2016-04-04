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
package ru.yandex.subtitles.ui.widget.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.PopupWindow;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.utils.ApplicationUtils;

/**
 * Base class to create popup window that appears over software keyboard.
 */
public abstract class OverKeyboardPopupWindow extends PopupWindow
        implements ViewTreeObserver.OnGlobalLayoutListener {

    public interface OnKeyboardHideListener {

        void onKeyboardHide();

    }

    private int mKeyboardHeight = 0;
    private boolean mPendingOpen = false;
    private boolean mKeyboardOpen = false;

    private Context mContext;
    private View mRootView;

    private OnKeyboardHideListener mKeyboardHideListener;

    public OverKeyboardPopupWindow(final Context context, @NonNull final View rootView) {
        super(context);
        mContext = context;
        mRootView = rootView;

        setBackgroundDrawable(null);
        if (ApplicationUtils.hasLollipop()) {
            setElevation(0f);
        }

        final View view = onCreateView(LayoutInflater.from(context));
        onViewCreated(view);
        setContentView(view);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // Default size
        setSize(mContext.getResources().getDimensionPixelSize(R.dimen.supposed_keyboard_height),
                WindowManager.LayoutParams.MATCH_PARENT);
        setSizeForSoftKeyboard();
    }

    public void setKeyboardHideListener(final OnKeyboardHideListener keyboardHideListener) {
        mKeyboardHideListener = keyboardHideListener;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    /**
     * Manually set the popup window size
     *
     * @param width  Width of the popup
     * @param height Height of the popup
     */
    public void setSize(final int width, final int height) {
        setWidth(width);
        setHeight(height);
    }

    /**
     * Call this function to resize the emoji popup according to your soft keyboard size
     */
    public void setSizeForSoftKeyboard() {
        final ViewTreeObserver viewTreeObserver = mRootView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        final Rect r = new Rect();
        mRootView.getWindowVisibleDisplayFrame(r);

        final int screenHeight = calculateScreenHeight();
        int heightDifference = screenHeight - (r.bottom - r.top);

        final Resources resources = mContext.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            heightDifference -= resources.getDimensionPixelSize(resourceId);
        }

        if (heightDifference > 100) {
            mKeyboardHeight = heightDifference;
            setSize(WindowManager.LayoutParams.MATCH_PARENT, mKeyboardHeight);

            mKeyboardOpen = true;
            if (mPendingOpen) {
                showAtBottom();
                mPendingOpen = false;
            }
        } else {
            if (mKeyboardOpen && mKeyboardHideListener != null) {
                mKeyboardHideListener.onKeyboardHide();
            }
            mKeyboardOpen = false;
        }
    }

    private int calculateScreenHeight() {
        final WindowManager wm = ApplicationUtils.getSystemService(mContext, Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    /**
     * Use this function to show the popup.
     * NOTE: Since, the soft keyboard sizes are variable on different android devices, the
     * library needs you to open the soft keyboard at least once before calling this function.
     * If that is not possible see showAtBottomPending() function.
     */
    public void showAtBottom() {
        showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
    }

    /**
     * Use this function when the soft keyboard has not been opened yet. This
     * will show the popup after the keyboard is up next time.
     * Generally, you will be calling InputMethodManager.showSoftInput function after
     * calling this function.
     */
    public void showAtBottomPending() {
        if (isKeyboardOpen()) {
            showAtBottom();
        } else {
            mPendingOpen = true;
        }
    }

    /**
     * @return Returns true if the soft keyboard is open, false otherwise.
     */
    public boolean isKeyboardOpen() {
        return mKeyboardOpen;
    }

    /**
     * @return keyboard height in pixels
     */
    public int getKeyboardHeight() {
        return mKeyboardHeight;
    }

    @NonNull
    public abstract View onCreateView(final LayoutInflater inflater);

    public abstract void onViewCreated(final View view);


}
