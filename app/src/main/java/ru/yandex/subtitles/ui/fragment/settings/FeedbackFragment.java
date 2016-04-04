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
package ru.yandex.subtitles.ui.fragment.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.ui.widget.QueryTextListener;
import ru.yandex.subtitles.utils.ApplicationUtils;
import ru.yandex.subtitles.utils.IntentUtils;
import ru.yandex.subtitles.utils.ShareUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;
import ru.yandex.subtitles.utils.ViewUtils;

public class FeedbackFragment extends AbstractFragment<FeedbackFragment.OnFeedbackSentListener>
        implements QueryTextListener.OnQueryTextListener {

    public interface OnFeedbackSentListener {

        void onFeedbackSent();

    }

    public static final String TAG = "FeedbackFragment";

    private static final int REQUEST_CODE_EMAIL = 9283;

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    private EditText mCommentView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.feedback);

        final QueryTextListener textListener = new QueryTextListener(EditorInfo.IME_ACTION_SEND, this);
        mCommentView = findView(R.id.comment);
        mCommentView.addTextChangedListener(textListener);
        mCommentView.setOnEditorActionListener(textListener);

        final TextView deviceInfoView = findView(R.id.device_info);
        deviceInfoView.setText(ApplicationUtils.getDeviceInfo(getActivity()));
    }

    @Override
    public void onPause() {
        ViewUtils.hideSoftwareKeyboard(mCommentView);
        super.onPause();
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        if (!TextUtilsExt.isEmpty(query)) {
            sendFeedback(getActivity());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_feedback, menu);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem item = menu.findItem(R.id.action_send);
        item.setEnabled(mCommentView != null && !TextUtilsExt.isEmpty(mCommentView.getText()));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                sendFeedback(getActivity());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendFeedback(final Context context) {
        final String sendTo = getString(R.string.support_email);
        final String[] addresses = ShareUtils.prepareAddresses(sendTo);
        final String subject = getString(R.string.feedback_subject,
                ApplicationUtils.getAppVersion(context));
        final String body = mCommentView.getText() + "\n\n" + ApplicationUtils.getDeviceInfo(context);

        sendEmail(addresses, subject, body);
    }

    private void sendEmail(@NonNull final String[] addresses,
                           final String subject, final String body) {
        final Intent mailToIntent = ShareUtils.prepareMailToIntent(addresses, subject, body);
        if (IntentUtils.canStartActivity(getContext(), mailToIntent)) {
            startActivityForResult(Intent.createChooser(mailToIntent,
                    getString(R.string.send)), REQUEST_CODE_EMAIL);
        } else {
            Log.e(TAG, "Failed to share data to email app. No app found to handle intent.");
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_EMAIL) {
            mController.onFeedbackSent();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
