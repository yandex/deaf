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

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.TextView;

public class QueryTextListener implements TextWatcher, TextView.OnEditorActionListener {

    public interface OnQueryTextListener {

        boolean onQueryTextChange(final String newText);

        boolean onQueryTextSubmit(final String query);

    }

    private int mActionMask = 0;
    private final OnQueryTextListener mListener;

    public QueryTextListener(final int mask, final OnQueryTextListener listener) {
        setActionMask(mask);
        mListener = listener;
    }

    public void setActionMask(final int mask) {
        mActionMask = mask;
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        mListener.onQueryTextChange(s == null ? "" : String.valueOf(s));
    }

    @Override
    public void afterTextChanged(final Editable s) {
    }

    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        final boolean submitted = (mActionMask & actionId) == actionId;
        return (submitted && mListener.onQueryTextSubmit(v.getEditableText().toString()));
    }

}