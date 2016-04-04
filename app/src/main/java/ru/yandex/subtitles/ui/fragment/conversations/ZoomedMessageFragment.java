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
package ru.yandex.subtitles.ui.fragment.conversations;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.MessageAnalytics;
import ru.yandex.subtitles.analytics.MessageMetadata;
import ru.yandex.subtitles.analytics.ZoomedMessage;
import ru.yandex.subtitles.content.data.Message;
import ru.yandex.subtitles.content.loader.MessageLoader;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;

/**
 * A nested fragment used in {@link ru.yandex.subtitles.ui.adapter.ZoomedMessagesAdapter}.
 */
public class ZoomedMessageFragment extends AbstractFragment<Void> implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<MessageAnalytics> {

    public static final String TAG = "ZoomedMessageFragment";

    private static final String EXTRA_MESSAGE = "message";

    public static ZoomedMessageFragment newInstance(@NonNull final ZoomedMessage message) {
        final ZoomedMessageFragment fragment = new ZoomedMessageFragment();

        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    private long mDisplayDuration;

    private boolean mFragmentVisible;
    private boolean mFragmentResumed;

    private Message mMessage;
    private MessageMetadata mMessageMetadata;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        final Bundle args = getArguments();
        final ZoomedMessage zoomedMessage = args.getParcelable(EXTRA_MESSAGE);
        if (zoomedMessage != null) {
            mMessage = zoomedMessage.getMessage();
            mMessageMetadata = zoomedMessage.getMetadata();
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_zoomed_message, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView messageView = findView(R.id.message);
        messageView.setMovementMethod(new ScrollingMovementMethod());
        messageView.setOnClickListener(this);

        if (mMessage != null) {
            messageView.setText(mMessage.getText());

            final Bundle loaderArgs = MessageLoader.forMessageId(mMessage.getId());
            getLoaderManager().initLoader(R.id.message_loader, loaderArgs, this);
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.message:
                onExitFullscreenClick();
                break;
        }
    }

    private void onExitFullscreenClick() {
        final Fragment target = getTargetFragment();
        if (target != null) {
            target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
        }
    }

    @Override
    public Loader<MessageAnalytics> onCreateLoader(final int id, final Bundle args) {
        return new MessageLoader(getActivity(), args);
    }

    @Override
    public void onLoadFinished(final Loader<MessageAnalytics> loader, final MessageAnalytics data) {
        if (mMessageMetadata != null && mMessage != null) {
            mMessageMetadata.setOpeningPhrase(data.isOpeningPhrase());
            // mMessageMetadata.setText(data.getMessage().getText());
        }
    }

    @Override
    public void onLoaderReset(final Loader<MessageAnalytics> loader) {
    }

    @Override
    public void setMenuVisibility(final boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        onHandleResumedState(mFragmentResumed, menuVisible);
        mFragmentVisible = menuVisible;
    }

    @Override
    public void onResume() {
        super.onResume();
        onHandleResumedState(true, mFragmentVisible);
        mFragmentResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        onHandleResumedState(false, mFragmentVisible);
        mFragmentResumed = false;
    }

    private void onHandleResumedState(final boolean isFragmentResumed, final boolean isFragmentVisible) {
        // This fragment is used as child fragment and
        // we should handle visible state internally
        if (isFragmentResumed && isFragmentVisible) {
            if (!mFragmentResumed || !mFragmentVisible) {
                onResumeInternal();
            }

        } else if (mFragmentResumed && mFragmentVisible) {
            onPauseInternal();
        }
    }

    private void onResumeInternal() {
        Log.i(TAG, "onResumeInternal() " + this);
        mDisplayDuration = System.currentTimeMillis();
    }

    private void onPauseInternal() {
        Log.i(TAG, "onPauseInternal() " + this);
        stopTrackingDisplayDuration();
    }

    private void stopTrackingDisplayDuration() {
        if (mMessageMetadata != null) {
            final long duration = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - mDisplayDuration);
            mMessageMetadata.setDurationInSeconds(mMessageMetadata.getDurationInSeconds() + duration);
        }
    }

}
