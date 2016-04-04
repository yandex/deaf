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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.content.data.Conversation;
import ru.yandex.subtitles.content.data.Thread;
import ru.yandex.subtitles.content.loader.ConversationsLoader;
import ru.yandex.subtitles.service.MessagingService;
import ru.yandex.subtitles.ui.adapter.AbstractRecyclerViewAdapter;
import ru.yandex.subtitles.ui.adapter.ConversationsAdapter;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.ui.widget.NoFadeItemAnimator;
import ru.yandex.subtitles.ui.widget.PopupMenuItemClickAdapter;

public class ConversationsFragment extends AbstractFragment<ConversationsFragment.OnConversationClickListener>
        implements LoaderManager.LoaderCallbacks<List<Conversation>>,
        AbstractRecyclerViewAdapter.OnItemClickListener<Conversation>,
        PopupMenuItemClickAdapter.OnPopupMenuItemClickListener<Conversation> {

    public interface OnConversationClickListener {

        void onConversationClick(final long conversationId);

    }

    public static final String TAG = "ConversationsFragment";

    public static ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mEmptyView;

    private ConversationsAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversations, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.conversations);

        final Context context = getActivity();
        mRecyclerView = findView(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setItemAnimator(new NoFadeItemAnimator());

        mAdapter = new ConversationsAdapter(context);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mProgressBar = findView(R.id.progress_bar);
        mEmptyView = findView(R.id.empty);

        onProgressChanged(true, false);
        getLoaderManager().initLoader(R.id.conversations_loader, null, this);
    }

    private void onProgressChanged(final boolean showProgress, final boolean showEmpty) {
        mEmptyView.setVisibility(!showProgress && showEmpty ? View.VISIBLE : View.GONE);
        mProgressBar.setVisibility(showProgress && !showEmpty ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(!showProgress && !showEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(final View view, final int position, final Conversation conversation) {
        switch (view.getId()) {
            case R.id.more:
                onShowMoreClick(view, conversation);
                break;

            default:
                onConversationClick(conversation.getThread());
                break;
        }
    }

    private void onShowMoreClick(final View view, @NonNull final Conversation conversation) {
        final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.inflate(R.menu.context_menu_conversation);

        final Menu menu = popupMenu.getMenu();
        if (conversation.getThread().isPinned()) {
            menu.findItem(R.id.action_pin_conversation).setVisible(false);
        } else {
            menu.findItem(R.id.action_unpin_conversation).setVisible(false);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenuItemClickAdapter<Conversation>(conversation, this));
        popupMenu.show();
    }

    @Override
    public boolean onPopupMenuItemClick(final MenuItem item, final Conversation entity) {
        final Context context = getActivity();
        final long threadId = entity.getThread().getId();
        switch (item.getItemId()) {
            case R.id.action_pin_conversation:
                MessagingService.pinConversation(context, threadId);
                return true;

            case R.id.action_unpin_conversation:
                MessagingService.unpinConversation(context, threadId);
                return true;

            case R.id.action_delete_conversation:
                MessagingService.deleteConversation(context, threadId);
                return true;

            default:
                return false;
        }
    }

    private void onConversationClick(@NonNull final Thread thread) {
        MessagingService.onConversationOpened(getActivity(), thread.getId());
        mController.onConversationClick(thread.getId());
    }

    @Override
    public Loader<List<Conversation>> onCreateLoader(final int id, final Bundle args) {
        return new ConversationsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<List<Conversation>> loader, final List<Conversation> data) {
        onProgressChanged(false, data.isEmpty());
        mAdapter.setItems(data);
    }

    @Override
    public void onLoaderReset(final Loader<List<Conversation>> loader) {
        mAdapter.clear();
    }

}
