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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.yandex.subtitles.Preferences;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.analytics.ConversationMetadata;
import ru.yandex.subtitles.analytics.ThreadAnalytics;
import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.content.data.Message;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.content.loader.MessagesLoader;
import ru.yandex.subtitles.content.loader.QuickResponsesLoader;
import ru.yandex.subtitles.content.loader.ThreadAnalyticsLoader;
import ru.yandex.subtitles.service.MessagingService;
import ru.yandex.subtitles.service.speechkit.recognition.PartialResultBroadcastReceiver;
import ru.yandex.subtitles.service.speechkit.speaker.SpeakerBroadcastReceiver;
import ru.yandex.subtitles.ui.adapter.AbstractRecyclerViewAdapter;
import ru.yandex.subtitles.ui.adapter.ConversationAdapter;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.ui.fragment.interactive.ChangeOrientationHintDialogFragment;
import ru.yandex.subtitles.ui.widget.ContextableRecyclerView;
import ru.yandex.subtitles.ui.widget.NoFadeItemAnimator;
import ru.yandex.subtitles.ui.widget.TypeMessageView;
import ru.yandex.subtitles.utils.OrientationUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;
import ru.yandex.subtitles.utils.ViewUtils;

public class ConversationFragment extends AbstractFragment<ConversationFragment.OnConversationListener>
        implements AbstractRecyclerViewAdapter.OnItemClickListener<Message>,
        TypeMessageView.TypeMessageViewCallbacks,
        PartialResultBroadcastReceiver.OnPartialResultListener {

    public interface OnConversationListener {

        void onMessageClick(final long threadId, final long messageId, final long memberId);

    }

    public static final String TAG = "ConversationFragment";

    private static final int REQUEST_CODE_ORIENTATION_HINT = 2933;

    public static final int STUB_CONVERSATION = -1;

    private static final String EXTRA_THREAD_ID = "thread_id";
    private static final String EXTRA_SHOW_KEYBOARD = "show_keyboard";
    private static final String EXTRA_CONVERSATION_METADATA = "conversation_metadata";

    public static ConversationFragment newInstance(final long threadId, final boolean showKeyboard,
                                                   @NonNull final ConversationMetadata conversationMetadata) {
        final ConversationFragment fragment = new ConversationFragment();

        final Bundle args = new Bundle();
        args.putLong(EXTRA_THREAD_ID, threadId);
        args.putBoolean(EXTRA_SHOW_KEYBOARD, showKeyboard);
        args.putParcelable(EXTRA_CONVERSATION_METADATA, conversationMetadata);
        fragment.setArguments(args);

        return fragment;
    }

    private long mDisplayDuration;
    private ConversationMetadata mConversationMetadata;

    private ContextableRecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private TypeMessageView mTypeMessageView;

    private Subscribeable mPartialResultReceiver;
    private Subscribeable mSpeakerReceiver;

    private ConversationAdapter mAdapter;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final Bundle args = getArguments();
        mConversationMetadata = args.getParcelable(EXTRA_CONVERSATION_METADATA);

        final long threadId = args.getLong(EXTRA_THREAD_ID);
        mPartialResultReceiver = new PartialResultBroadcastReceiver(threadId, this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();
        mRecyclerView = findView(R.id.recycler);
        mLayoutManager = new LinearLayoutManager(activity);
        mLayoutManager.setReverseLayout(false);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new NoFadeItemAnimator());
        registerForContextMenu(mRecyclerView);

        mAdapter = new ConversationAdapter(activity);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mTypeMessageView = findView(R.id.type_message_view);
        mTypeMessageView.setTypeMessageViewCallbacks(this);
        mTypeMessageView.setRootView(findView(R.id.root_view));

        final long threadId = getArguments().getLong(EXTRA_THREAD_ID);

        final LoaderManager loaderManager = getLoaderManager();
        final Bundle messagesArgs = MessagesLoader.forThreadId(threadId);
        loaderManager.initLoader(R.id.messages_loader, messagesArgs, mMessagesLoaderCallbacks);

        final Bundle analyticsArgs = ThreadAnalyticsLoader.forThreadId(threadId);
        loaderManager.initLoader(R.id.thread_analytics_loader, analyticsArgs, mThreadAnalyticsLoaderCallbacks);

        loaderManager.initLoader(R.id.quick_responses_loader, null, mQuickResponsesLoaderCallbacks);

        mPartialResultReceiver.subscribe(activity);

        mSpeakerReceiver = new SpeakerBroadcastReceiver(mAdapter);
        mSpeakerReceiver.subscribe(activity);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_conversation, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_orientation:
                onChangeOrientationClick();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onChangeOrientationClick() {
        final Preferences preferences = Preferences.getInstance();

        final Activity activity = getActivity();
        final boolean accelerometerOrientationLocked = OrientationUtils
                .isAccelerometerOrientationLocked(activity);

        if (!preferences.hasChangeOrientationHintShown() || accelerometerOrientationLocked) {
            preferences.setChangeOrientationHintShown(true);

            final DialogFragment dialogFragment = ChangeOrientationHintDialogFragment
                    .newInstance(accelerometerOrientationLocked);
            dialogFragment.setTargetFragment(this, REQUEST_CODE_ORIENTATION_HINT);
            dialogFragment.show(getChildFragmentManager(), ChangeOrientationHintDialogFragment.TAG);

        } else {
            Analytics.onReverseOrientationClick();
            OrientationUtils.toggleScreenOrientation(activity);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_ORIENTATION_HINT && resultCode == Activity.RESULT_OK) {
            final Activity activity = getActivity();
            if (activity != null && !OrientationUtils.isAccelerometerOrientationLocked(activity)) {
                Analytics.onReverseOrientationClick();
                OrientationUtils.toggleScreenOrientation(activity);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onItemClick(final View view, final int position, final Message message) {
        switch (view.getId()) {
            case R.id.more:
                onShowMoreClick(message);
                break;

            case R.id.speaker:
                onSpeakerClick(message);
                break;

            default:
                onMessageClick(message);
                break;
        }
    }

    private void onShowMoreClick(@NonNull final Message message) {
        ViewUtils.hideSoftwareKeyboard(mTypeMessageView);
        mRecyclerView.createRecyclerContextMenuInfo(message);
        openContextMenu(mRecyclerView);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        final ContextableRecyclerView.RecyclerContextMenuInfo recyclerInfo =
                (ContextableRecyclerView.RecyclerContextMenuInfo) menuInfo;
        if (recyclerInfo != null) {
            final Message message = (Message) recyclerInfo.getData();

            getMenuInflater().inflate(R.menu.context_menu_message, menu);
            if (message.isPinned()) {
                menu.findItem(R.id.action_pin_message).setVisible(false);
            } else {
                menu.findItem(R.id.action_unpin_message).setVisible(false);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final ContextableRecyclerView.RecyclerContextMenuInfo recyclerInfo =
                (ContextableRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        final Message message = (Message) recyclerInfo.getData();

        switch (item.getItemId()) {
            case R.id.action_fullscreen_message:
                onMessageClick(message);
                return true;

            case R.id.action_pin_message:
                MessagingService.pinMessage(getActivity(), message.getId());
                return true;

            case R.id.action_unpin_message:
                MessagingService.unpinMessage(getActivity(), message.getId());
                return true;

            case R.id.action_copy_message:
                onCopyMessageClick(message);
                return true;

            case R.id.action_delete_message:
                MessagingService.deleteMessage(getActivity(), message.getId());
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void onCopyMessageClick(@NonNull final Message message) {
        final Context context = getActivity();
        TextUtilsExt.putTextToClipboard(context, message.getText());
        Analytics.onCopyMessage(message);

        Toast.makeText(context, R.string.message_has_been_copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private void onSpeakerClick(@NonNull final Message message) {
        mConversationMetadata.increaseVocalizedMessageCount();
        Analytics.onVocalizeMessage();

        MessagingService.toggleVocalization(getActivity(), message.getId(), message.getText());
    }

    private void onMessageClick(@NonNull final Message message) {
        mConversationMetadata.increaseZoomedMessageCount();
        Analytics.onFullscreenMessage();

        mController.onMessageClick(message.getThreadId(), message.getId(), message.getUserId());
    }

    @Override
    public void onSendMessageClick(@NonNull final String message) {
        final long threadId = getArguments().getLong(EXTRA_THREAD_ID);
        MessagingService.sendMessage(getActivity(), threadId, message);
    }

    @Override
    public void onPartialResult(@Nullable final String partialResult) {
        if (TextUtilsExt.isEmpty(partialResult)) {
            return;
        }
        mAdapter.setPartialResult(partialResult);
        final int lastCompletelyVisibleItem = mLayoutManager.findLastCompletelyVisibleItemPosition();
        if (lastCompletelyVisibleItem == mAdapter.getMessagesCount() - 1) {
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    private final LoaderManager.LoaderCallbacks<List<Message>> mMessagesLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<Message>>() {

                @Override
                public Loader<List<Message>> onCreateLoader(final int id, final Bundle args) {
                    return new MessagesLoader(getActivity(), args);
                }

                @Override
                public void onLoadFinished(final Loader<List<Message>> loader, final List<Message> data) {
                    onMessagesLoaded(data);
                }

                @Override
                public void onLoaderReset(final Loader<List<Message>> loader) {
                }

            };

    /* package */ void onMessagesLoaded(@NonNull final List<Message> messages) {
        if (TextUtilsExt.isEmpty(mConversationMetadata.getFirstPhrase()) && !messages.isEmpty()) {
            mConversationMetadata.setFirstPhrase(messages.get(0).getText());
        }

        final int previousCount = mAdapter.getMessagesCount();
        mAdapter.setItems(messages);

        if (previousCount < messages.size()) {
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    private final LoaderManager.LoaderCallbacks<List<Phrase>> mQuickResponsesLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<Phrase>>() {

                @Override
                public Loader<List<Phrase>> onCreateLoader(final int id, final Bundle args) {
                    return new QuickResponsesLoader(getActivity());
                }

                @Override
                public void onLoadFinished(final Loader<List<Phrase>> loader, final List<Phrase> data) {
                    onQuickResponsesLoaded(data);
                }

                @Override
                public void onLoaderReset(final Loader<List<Phrase>> loader) {
                }

            };

    /* package */ void onQuickResponsesLoaded(@NonNull final List<Phrase> phrases) {
        mTypeMessageView.setQuickResponses(phrases);
    }

    @Override
    public void onResume() {
        mDisplayDuration = System.currentTimeMillis();
        super.onResume();

        final Bundle args = getArguments();
        final boolean showKeyboard = args.getBoolean(EXTRA_SHOW_KEYBOARD, false);
        if (showKeyboard) {
            mTypeMessageView.showKeyboard();
        }
        args.putBoolean(EXTRA_SHOW_KEYBOARD, false);
    }

    @Override
    public void onPause() {
        MessagingService.stopVocalization(getActivity());
        stopTrackingDisplayDuration();
        mTypeMessageView.onPause();
        super.onPause();
    }

    private final LoaderManager.LoaderCallbacks<ThreadAnalytics> mThreadAnalyticsLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<ThreadAnalytics>() {

                @Override
                public Loader<ThreadAnalytics> onCreateLoader(final int id, final Bundle args) {
                    return new ThreadAnalyticsLoader(getActivity(), args);
                }

                @Override
                public void onLoadFinished(final Loader<ThreadAnalytics> loader, final ThreadAnalytics data) {
                    mConversationMetadata.setConversationsCount(data.getConversationsCount());
                    mConversationMetadata.setOwnerStatementCount(data.getOwnerStatementCount());
                    mConversationMetadata.setVisavisStatementCount(data.getVisavisStatementCount());
                }

                @Override
                public void onLoaderReset(final Loader<ThreadAnalytics> loader) {
                }

            };

    private void stopTrackingDisplayDuration() {
        final long duration = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - mDisplayDuration);
        mConversationMetadata.setDurationInSeconds(mConversationMetadata.getDurationInSeconds() + duration);
    }

    @Override
    public boolean onBackPressed() {
        if (!mTypeMessageView.onBackPressed()) {
            stopTrackingDisplayDuration();

            mConversationMetadata.setReversePortrait(OrientationUtils.isReversePortrait(getActivity()));
            Analytics.reportConversationMetadata(mConversationMetadata);
            return super.onBackPressed();

        } else {
            return true;
        }
    }

    @Override
    public void onDestroyView() {
        final Context context = getActivity();
        mPartialResultReceiver.unsubscribe(context);
        mSpeakerReceiver.unsubscribe(context);
        unregisterForContextMenu(mRecyclerView);
        super.onDestroyView();
    }

}