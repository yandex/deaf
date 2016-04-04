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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.analytics.ConversationMetadata;
import ru.yandex.subtitles.analytics.MessageMetadata;
import ru.yandex.subtitles.analytics.ZoomedMessage;
import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.content.data.Message;
import ru.yandex.subtitles.content.loader.ZoomedMessagesLoader;
import ru.yandex.subtitles.service.MessagingService;
import ru.yandex.subtitles.service.speechkit.recognition.PhraseBroadcastReceiver;
import ru.yandex.subtitles.service.speechkit.speaker.SpeakerBroadcastReceiver;
import ru.yandex.subtitles.ui.adapter.ZoomedMessagesAdapter;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.ui.widget.SpeakerView;
import ru.yandex.subtitles.ui.widget.ViewPagerSwipeDetector;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class ZoomedMessagesFragment extends AbstractFragment<ZoomedMessagesFragment.OnZoomedMessagesListener>
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<List<ZoomedMessage>>,
        ViewPagerSwipeDetector.OnViewPagerSwipeListener, PhraseBroadcastReceiver.OnPhraseRecognizedListener,
        SpeakerBroadcastReceiver.SpeakerEventListener {

    public interface OnZoomedMessagesListener {

        void onPhraseRecognized(@NonNull final String phrase);

        void onExitFullscreenClick(final long threadId);

    }

    public static final String TAG = "ZoomedMessagesFragment";

    public static final int EXIT_FULLSCREEN_REQUEST_CODE = 2345;

    private static final String EXTRA_MESSAGE = "message_id";
    private static final String EXTRA_ZOOMED_MESSAGE = "zoomed_message";
    private static final String EXTRA_MEMBER = "member_id";
    private static final String EXTRA_THREAD = "thread_id";
    private static final String EXTRA_CONVERSATION_METADATA = "conversation_metadata";

    public static ZoomedMessagesFragment newInstance(final long threadId, final long messageId,
                                                     final long memberId,
                                                     @NonNull final ConversationMetadata conversationMetadata) {
        final ZoomedMessagesFragment fragment = new ZoomedMessagesFragment();

        final Bundle args = new Bundle();
        args.putLong(EXTRA_THREAD, threadId);
        args.putLong(EXTRA_MESSAGE, messageId);
        args.putLong(EXTRA_MEMBER, memberId);
        args.putParcelable(EXTRA_CONVERSATION_METADATA, conversationMetadata);
        fragment.setArguments(args);

        return fragment;
    }

    private long mDisplayDuration;
    private ConversationMetadata mConversationMetadata;

    private TextView mDescriptionView;
    private View mSlidePrevView;
    private View mSlideNextView;
    private ViewPager mViewPager;
    private SpeakerView mSpeakerView;

    private boolean mDescriptionShowing;
    private ZoomedMessagesAdapter mAdapter;

    private Subscribeable mPhraseReceiver;
    private Subscribeable mSpeakerReceiver;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final Bundle args = getArguments();
        mConversationMetadata = args.getParcelable(EXTRA_CONVERSATION_METADATA);

        final long threadId = args.getLong(EXTRA_THREAD);
        mPhraseReceiver = new PhraseBroadcastReceiver(threadId, this);
        mSpeakerReceiver = new SpeakerBroadcastReceiver(this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_zoomed_messages, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDescriptionView = findView(R.id.description);
        mDescriptionView.setOnClickListener(this);

        mSlidePrevView = findView(R.id.slide_prev);
        mSlidePrevView.setOnClickListener(this);

        mSlideNextView = findView(R.id.slide_next);
        mSlideNextView.setOnClickListener(this);

        mAdapter = new ZoomedMessagesAdapter(this, getChildFragmentManager());
        mViewPager = findView(R.id.view_pager);
        mViewPager.addOnPageChangeListener(new ViewPagerSwipeDetector(this));
        mViewPager.setAdapter(mAdapter);

        mSpeakerView = findView(R.id.speaker);
        mSpeakerView.setOnClickListener(this);

        findView(R.id.exit_fullscreen).setOnClickListener(this);

        final Bundle args = getArguments();
        final long threadId = args.getLong(EXTRA_THREAD);
        final long memberId = args.getLong(EXTRA_MEMBER);
        final Bundle loaderArgs = ZoomedMessagesLoader.forThreadIdAndUserId(threadId, memberId);
        getLoaderManager().initLoader(R.id.typed_messages_loader, loaderArgs, this);

        final Context context = getActivity();
        mPhraseReceiver.subscribe(context);
        mSpeakerReceiver.subscribe(context);
    }

    @Override
    public void onPhraseRecognized(@NonNull final String phrase) {
        if (isAttached()) {
            Analytics.onMicrophoneBarMessage(Analytics.MICROPHONE_BAR_MESSAGE_PHRASE_RECOGNIZED);
            mController.onPhraseRecognized(phrase);
        }
    }

    @Override
    public void onDestroyView() {
        final Context context = getActivity();
        mPhraseReceiver.unsubscribe(context);
        mSpeakerReceiver.unsubscribe(context);
        super.onDestroyView();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.description:
            case R.id.exit_fullscreen:
                onExitFullscreenClick("Exit click");
                break;

            case R.id.slide_prev:
                onSlidePrevClick();
                break;

            case R.id.slide_next:
                onSlideNextClick();
                break;

            case R.id.speaker:
                onVocalizeClick();
                break;
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == EXIT_FULLSCREEN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            onExitFullscreenClick("Message click");

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onExitFullscreenClick(final String method) {
        stopTrackingDisplayDuration();
        reportExitFullscreen(method);

        final long threadId = getArguments().getLong(EXTRA_THREAD);
        mController.onExitFullscreenClick(threadId);
    }

    private void reportExitFullscreen(final String method) {
        final ZoomedMessage zoomedMessage = getArguments().getParcelable(EXTRA_ZOOMED_MESSAGE);
        if (zoomedMessage != null) {
            final MessageMetadata messageMetadata = zoomedMessage.getMetadata();
            Analytics.onExitFullscreen(method, messageMetadata);
        }
    }

    private void onSlidePrevClick() {
        final int currentPos = mViewPager.getCurrentItem();
        final int previousPos = Math.max(currentPos - 1, 0);
        mViewPager.setCurrentItem(previousPos, true);
        reportNavigationEvent(Analytics.NAVIGATION_PREV_CLICKED);
    }

    private void onSlideNextClick() {
        final int currentPos = mViewPager.getCurrentItem();
        final int nextPos = Math.min(currentPos + 1, mAdapter.getCount());
        mViewPager.setCurrentItem(nextPos, true);
        reportNavigationEvent(Analytics.NAVIGATION_NEXT_CLICKED);
    }

    private void reportNavigationEvent(@NonNull final String method) {
        final ZoomedMessage zoomedMessage = getArguments().getParcelable(EXTRA_ZOOMED_MESSAGE);
        final Message message = (zoomedMessage != null ? zoomedMessage.getMessage() : null);
        if (message != null) {
            Analytics.onZoomedMessagesNavigation(method, message.getUserId());
        }
    }

    private void onVocalizeClick() {
        final ZoomedMessage zoomedMessage = getArguments().getParcelable(EXTRA_ZOOMED_MESSAGE);
        final Message message = (zoomedMessage != null ? zoomedMessage.getMessage() : null);
        final String text = (message != null ? message.getText() : null);
        if (!TextUtilsExt.isEmpty(text)) {
            final boolean isDescriptionVisible = (mDescriptionView.getVisibility() == View.VISIBLE);
            final String textToVocalize = (isDescriptionVisible ?
                    getString(R.string.zoomed_message_description) : "") + text;

            zoomedMessage.getMetadata().setWasVocalized(true);
            mConversationMetadata.increaseVocalizedMessageCount();
            Analytics.onVocalizeZoomedMessage();

            MessagingService.toggleVocalization(getActivity(), message.getId(), textToVocalize);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_zoomed_message, menu);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final String description = getString(R.string.zoomed_message_description);

        final ZoomedMessage zoomedMessage = getArguments().getParcelable(EXTRA_ZOOMED_MESSAGE);
        final Message message = (zoomedMessage != null ? zoomedMessage.getMessage() : null);

        final boolean menuItemVisible = (message != null && !TextUtilsExt.contains(message.getText(), description));
        final MenuItem item = menu.findItem(R.id.action_info);
        item.setVisible(menuItemVisible);

        mDescriptionView.setVisibility(menuItemVisible && mDescriptionShowing ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                toggleDescriptionVisibility();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleDescriptionVisibility() {
        final boolean visible = (mDescriptionView.getVisibility() == View.VISIBLE);
        if (!visible) {
            Analytics.onShowZoomedMessageDescription();
        }
        mDescriptionShowing = !visible;
        mDescriptionView.setVisibility(mDescriptionShowing ? View.VISIBLE : View.GONE);
    }

    @Override
    public Loader<List<ZoomedMessage>> onCreateLoader(final int id, final Bundle args) {
        return new ZoomedMessagesLoader(getActivity(), args);
    }

    @Override
    public void onLoadFinished(final Loader<List<ZoomedMessage>> loader, final List<ZoomedMessage> data) {
        mAdapter.setData(data);

        final Bundle args = getArguments();
        final long messageId = args.getLong(EXTRA_MESSAGE);
        final int position = findMessagePositionById(data, messageId);

        mViewPager.setCurrentItem(position, false);
        getArguments().putParcelable(EXTRA_ZOOMED_MESSAGE, data.get(position));

        updateSlidePanelsVisibility(position);
        invalidateOptionsMenu();
    }

    private int findMessagePositionById(@NonNull final List<ZoomedMessage> messages, final long messageId) {
        final int size = messages.size();
        for (int i = 0; i < size; i++) {
            final Message message = messages.get(i).getMessage();
            if (message != null && message.getId() == messageId) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onLoaderReset(final Loader<List<ZoomedMessage>> loader) {
    }

    @Override
    public void onPageSelected(final int position) {
        MessagingService.stopVocalization(getActivity());
        reportExitFullscreen("Exit on slide");

        final Bundle args = getArguments();

        final ZoomedMessage zoomedMessage = mAdapter.getData(position);
        final Message message = zoomedMessage.getMessage();
        if (message != null) {
            args.putLong(EXTRA_MESSAGE, message.getId());
        }
        args.putParcelable(EXTRA_ZOOMED_MESSAGE, zoomedMessage);

        updateSlidePanelsVisibility(position);
        invalidateOptionsMenu();
    }

    @Override
    public void onPageSwipedLeft() {
        reportNavigationEvent(Analytics.NAVIGATION_SWIPE_TO_PREV);
    }

    @Override
    public void onPageSwipedRight() {
        reportNavigationEvent(Analytics.NAVIGATION_SWIPE_TO_NEXT);
    }

    private void updateSlidePanelsVisibility(final int position) {
        mSlidePrevView.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
        mSlideNextView.setVisibility(position < mAdapter.getCount() - 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        mDisplayDuration = System.currentTimeMillis();
        super.onResume();
    }

    @Override
    public void onPause() {
        MessagingService.stopVocalization(getActivity());
        stopTrackingDisplayDuration();
        super.onPause();
    }

    private void stopTrackingDisplayDuration() {
        final long duration = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - mDisplayDuration);
        mConversationMetadata.setDurationInSeconds(mConversationMetadata.getDurationInSeconds() + duration);
    }

    @Override
    public boolean onBackPressed() {
        stopTrackingDisplayDuration();
        reportExitFullscreen("Hardware button click");
        return super.onBackPressed();
    }

    @Override
    public void onStartPlaying(final long messageId) {
        mSpeakerView.onStartPlaying();
    }

    @Override
    public void onStopPlaying(final long messageId) {
        mSpeakerView.onStopPlaying();
    }

}
