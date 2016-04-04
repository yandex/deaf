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
package ru.yandex.subtitles.ui.fragment.phrases;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.service.PhrasesService;
import ru.yandex.subtitles.ui.fragment.AbstractAlertDialogFragment;
import ru.yandex.subtitles.utils.TextUtilsExt;
import ru.yandex.subtitles.utils.ViewUtils;

public class EditPhraseDialogFragment extends AbstractAlertDialogFragment {

    public static final String TAG = "EditPhraseDialog";

    private static final String EXTRA_PHRASE_ID = "phrase_id";
    private static final String EXTRA_PHRASE_TEXT = "phrase_text";

    public static EditPhraseDialogFragment newInstance() {
        return newInstance(null, null);
    }

    public static EditPhraseDialogFragment newInstance(@Nullable final Long id, final String text) {
        final EditPhraseDialogFragment fragment = new EditPhraseDialogFragment();

        final Bundle args = new Bundle();
        args.putSerializable(EXTRA_PHRASE_ID, id);
        args.putString(EXTRA_PHRASE_TEXT, text);
        fragment.setArguments(args);

        return fragment;
    }

    private EditText mEditText;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        final Long id = (Long) args.getSerializable(EXTRA_PHRASE_ID);
        setTitle(id == null ? R.string.add_phrase : R.string.edit_phrase);

        setPositive(R.string.save);
        setNegative(R.string.cancel);
    }

    @Override
    public int getSoftInputModeFlags() {
        return WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
    }

    @NonNull
    @Override
    public View onCreateDialogView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        final Window window = getDialog().getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return inflater.inflate(R.layout.dialog_fragment_edit_phrase, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditText = findView(R.id.text);

        final Bundle args = getArguments();
        final String text = args.getString(EXTRA_PHRASE_TEXT);
        mEditText.setText(text);
        if (!TextUtilsExt.isEmpty(text)) {
            mEditText.setSelection(text.length());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mEditText.requestFocus();
        ViewUtils.showSoftwareKeyboard(mEditText);
    }

    @Override
    public void onPositiveButtonClick() {
        final String text = mEditText.getText().toString().trim();
        if (!TextUtilsExt.isEmpty(text)) {
            final Bundle args = getArguments();
            final Long id = (Long) args.getSerializable(EXTRA_PHRASE_ID);
            PhrasesService.addOrUpdatePhrase(getActivity(), id, text);
        }
        dismissAllowingStateLoss();
    }

    @Override
    public void onNegativeButtonClick() {
        super.onNegativeButtonClick();
        dismissAllowingStateLoss();
    }
}