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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.content.loader.StartingPhrasesLoader;
import ru.yandex.subtitles.service.PhrasesService;
import ru.yandex.subtitles.ui.adapter.AbstractRecyclerViewAdapter;
import ru.yandex.subtitles.ui.adapter.StartingPhrasesAdapter;
import ru.yandex.subtitles.ui.fragment.AbstractFragment;
import ru.yandex.subtitles.ui.widget.PopupMenuItemClickAdapter;
import ru.yandex.subtitles.utils.BitmapUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class PhrasesFragment extends AbstractFragment<PhrasesFragment.OnPhraseClickListener>
        implements LoaderManager.LoaderCallbacks<List<Phrase>>,
        AbstractRecyclerViewAdapter.OnItemClickListener<PhraseItem>,
        StartingPhrasesAdapter.OnItemMoveListener, View.OnClickListener,
        PopupMenuItemClickAdapter.OnPopupMenuItemClickListener<Phrase> {

    public interface OnPhraseClickListener {

        void onPhraseClick(@NonNull final Phrase phrase);

        void onStartConversationClick();

    }

    public static final String TAG = "PhrasesFragment";

    public static PhrasesFragment newInstance() {
        return new PhrasesFragment();
    }

    private RecyclerView mRecyclerView;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private StartingPhrasesAdapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phrases, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = getContext();

        mRecyclerView = findView(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        final GeneralItemAnimator itemAnimator = new RefactoredDefaultItemAnimator();
        itemAnimator.setChangeDuration(0L);
        mRecyclerView.setItemAnimator(itemAnimator);

        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        final NinePatchDrawable drawable = (NinePatchDrawable) ContextCompat
                .getDrawable(getContext(), R.drawable.material_shadow_z3);
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(drawable);
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);
        mRecyclerViewDragDropManager.setInitiateOnMove(false);

        mAdapter = new StartingPhrasesAdapter(context);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemMoveListener(this);

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);
        mRecyclerView.setAdapter(mWrappedAdapter);

        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        final Resources resources = getResources();
        final Bitmap textBitmap = BitmapUtils.createTypefaceBitmap(context,
                TextUtilsExt.toUpperCase(getString(R.string.start_conversation)),
                resources.getColor(R.color.text_primary_color),
                resources.getDimensionPixelSize(R.dimen.text_size_primary));
        final ImageView startConversationButton = findView(R.id.start_conversation);
        startConversationButton.setImageBitmap(textBitmap);
        startConversationButton.setOnClickListener(this);

        getLoaderManager().initLoader(R.id.starting_phrases_loader, null, this);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_quick_start, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_phrase:
                onAddPhraseClick();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.start_conversation:
                onStartConversationClick();
                break;
        }
    }

    private void onStartConversationClick() {
        Analytics.onStartConversationClick();
        mController.onStartConversationClick();
    }

    @Override
    public void onItemClick(final View view, final int position, final PhraseItem item) {
        final Phrase phrase = item.getPhrase();
        switch (view.getId()) {
            case R.id.more:
                onShowMoreClick(view, phrase);
                break;

            default:
                onPhraseClick(position, phrase);
                break;
        }
    }

    private void onShowMoreClick(final View view, @NonNull final Phrase phrase) {
        final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.inflate(R.menu.context_menu_starting_phrases);
        popupMenu.setOnMenuItemClickListener(new PopupMenuItemClickAdapter<Phrase>(phrase, this));
        popupMenu.show();
    }

    @Override
    public boolean onPopupMenuItemClick(final MenuItem item, @NonNull final Phrase phrase) {
        switch (item.getItemId()) {
            case R.id.action_edit_phrase:
                onEditPhraseClick(phrase);
                return true;

            case R.id.action_delete_phrase:
                onDeletePhraseClick(phrase);
                return true;

            default:
                return false;
        }
    }

    private void onAddPhraseClick() {
        final EditPhraseDialogFragment fragment = EditPhraseDialogFragment.newInstance();
        fragment.show(getChildFragmentManager(), EditPhraseDialogFragment.TAG);
    }

    private void onEditPhraseClick(@NonNull final Phrase phrase) {
        final EditPhraseDialogFragment fragment = EditPhraseDialogFragment.newInstance(phrase.getId(), phrase.getText());
        fragment.show(getChildFragmentManager(), EditPhraseDialogFragment.TAG);
    }

    private void onDeletePhraseClick(@NonNull final Phrase phrase) {
        PhrasesService.deletePhrase(getActivity(), phrase.getId());
    }

    @Override
    public void onItemMove(final int fromPosition, final Phrase fromPhrase,
                           final int toPosition, final Phrase toPhrase) {
        final boolean moveToTop = (toPosition < fromPosition);
        PhrasesService.movePhrase(getContext(), fromPhrase.getId(), toPhrase.getId(), moveToTop);

        Analytics.onPhraseMoved(fromPhrase.getText(), fromPosition, toPosition);
    }

    private void onPhraseClick(final int position, @NonNull final Phrase phrase) {
        Analytics.onPhraseClick(position, phrase);
        mController.onPhraseClick(phrase);
    }

    @Override
    public Loader<List<Phrase>> onCreateLoader(final int id, final Bundle args) {
        return new StartingPhrasesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<List<Phrase>> loader, final List<Phrase> data) {
        final List<PhraseItem> items = new ArrayList<PhraseItem>();
        if (!data.isEmpty()) {
            items.add(new PhraseItem(PhraseItem.TYPE_HEADER, getString(R.string.starting_phrases), null));
            for (final Phrase phrase : data) {
                items.add(new PhraseItem(PhraseItem.TYPE_PHRASE, null, phrase));
            }
            items.add(new PhraseItem(PhraseItem.TYPE_FOOTER, null, null));
        }
        mAdapter.setItems(items);
    }

    @Override
    public void onLoaderReset(final Loader<List<Phrase>> loader) {
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        releaseDragAndDropResources();
        super.onDestroyView();
    }

    private void releaseDragAndDropResources() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
    }

}