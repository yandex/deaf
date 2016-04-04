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
package ru.yandex.subtitles.ui.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;

import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.ui.fragment.phrases.PhraseItem;

public class StartingPhrasesAdapter extends AbstractRecyclerViewAdapter<PhraseItem, StartingPhrasesAdapter.ViewHolder>
        implements DraggableItemAdapter<StartingPhrasesAdapter.ViewHolder> {

    public interface OnItemMoveListener {

        void onItemMove(final int fromPosition, final Phrase fromPhrase,
                        final int toPosition, final Phrase toPhrase);

    }

    private static final int[] CLICKABLE_VIEW_IDS = new int[] { AbstractViewHolder.DEFAULT_CLICKABLE_VIEW_ID, R.id.more };

    private OnItemMoveListener mOnItemMoveListener;

    public StartingPhrasesAdapter(final Context context) {
        super(context);
        setHasStableIds(true);
    }

    public void setOnItemMoveListener(@Nullable final OnItemMoveListener onItemMoveListener) {
        mOnItemMoveListener = onItemMoveListener;
    }

    @Override
    public long getItemId(final int position) {
        return (position != RecyclerView.NO_ID ? getItem(position).hashCode() : Long.MAX_VALUE);
    }

    @Override
    public int getItemViewType(final int position) {
        return getItem(position).getType();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(final int position) {
        return getItemViewType(position) == PhraseItem.TYPE_PHRASE;
    }

    @Override
    public int[] getClickableViewIds(final int position) {
        return CLICKABLE_VIEW_IDS;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final int layoutId;
        switch (viewType) {
            case PhraseItem.TYPE_HEADER:
                layoutId = R.layout.list_item_starting_phrases_header;
                break;

            case PhraseItem.TYPE_PHRASE:
                layoutId = R.layout.list_item_starting_phrases_phrase;
                break;

            case PhraseItem.TYPE_FOOTER:
                layoutId = R.layout.list_item_starting_phrases_footer;
                break;

            default:
                throw new IllegalArgumentException("View type=" + viewType + " does not supported");
        }

        final View itemView = getLayoutInflater().inflate(layoutId, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);

        final PhraseItem item = getItem(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case PhraseItem.TYPE_HEADER:
                holder.bindHeader(item.getTitle());
                break;

            case PhraseItem.TYPE_PHRASE:
                holder.bindPhrase(item.getPhrase());
                break;
        }
    }

    @Override
    public boolean onCheckCanStartDrag(final ViewHolder holder, final int position, final int x, final int y) {
        return holder.getItemId() != RecyclerView.NO_ID && getItemViewType(position) == PhraseItem.TYPE_PHRASE;
    }

    @Nullable
    @Override
    public ItemDraggableRange onGetItemDraggableRange(final ViewHolder holder, final int position) {
        ItemDraggableRange itemDraggableRange = null;

        final int itemCount = getItemCount();
        final int firstPhrasePosition = 1;
        final int lastPhrasePosition = itemCount - 2;
        if (firstPhrasePosition <= lastPhrasePosition) {
            itemDraggableRange = new ItemDraggableRange(firstPhrasePosition, lastPhrasePosition);
        }

        return itemDraggableRange;
    }

    @Override
    public void onMoveItem(final int fromPosition, final int toPosition) {
        if (fromPosition != toPosition) {
            final List<PhraseItem> items = getItems();

            final int realFrom = fromPosition - 1;
            final int realTo = toPosition - 1;
            final Phrase fromPhrase = items.get(fromPosition).getPhrase();
            final Phrase toPhrase = items.get(toPosition).getPhrase();

            items.add(toPosition, items.remove(fromPosition));
            notifyItemChanged(toPosition);
            notifyItemChanged(fromPosition);

            if (mOnItemMoveListener != null) {
                mOnItemMoveListener.onItemMove(realFrom, fromPhrase, realTo, toPhrase);
            }
        }
    }

    /* package */ static class ViewHolder extends AbstractViewHolder
            implements DraggableItemViewHolder {

        @DraggableItemStateFlags
        private int mDragStateFlags;

        private final DragDropTouchEventWorkaround mDragDropTouchEventWorkaround;

        private final View mContainerView;
        private final TextView mTitleView;
        private final TextView mPhraseView;
        private final View mMoreView;

        public ViewHolder(final View itemView) {
            super(itemView);

            mDragDropTouchEventWorkaround = new DragDropTouchEventWorkaround();

            mContainerView = findView(R.id.container);
            mTitleView = findView(R.id.title);
            mPhraseView = findView(R.id.phrase);
            mMoreView = findView(R.id.more);

            if (mContainerView != null && mMoreView != null) {
                mContainerView.setOnTouchListener(mDragDropTouchEventWorkaround);
                mMoreView.setOnTouchListener(mDragDropTouchEventWorkaround);
            }
        }

        @Override
        public void setDragStateFlags(@DraggableItemStateFlags final int flags) {
            mDragStateFlags = flags;
        }

        @Override
        @DraggableItemStateFlags
        public int getDragStateFlags() {
            return mDragStateFlags;
        }

        public void bindHeader(final String title) {
            mTitleView.setText(title);
        }

        public void bindPhrase(final Phrase phrase) {
            mPhraseView.setText(phrase.getText());

            final int dragState = getDragStateFlags();
            if (((dragState & DraggableItemConstants.STATE_FLAG_IS_UPDATED) != 0)) {
                if ((dragState & DraggableItemConstants.STATE_FLAG_IS_ACTIVE) != 0) {
                    mContainerView.setBackgroundResource(R.color.draggable_background_color);
                    mMoreView.setBackgroundResource(android.R.color.transparent);

                    mDragDropTouchEventWorkaround.cancelClick();
                } else {
                    mContainerView.setBackgroundResource(R.drawable.pre_lollipop_list_item_background_selector);
                    mMoreView.setBackgroundResource(R.drawable.borderless_background_selector);
                }
            }
        }

    }

    /**
     * DragDropManager doesn't handle native long press event,
     * so click event is not cancelled and we should cancel it manually
     */
    private static class DragDropTouchEventWorkaround implements View.OnTouchListener {

        private boolean mCancelClick = false;

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            final int action = event.getAction();
            final boolean handled = (mCancelClick &&
                    (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL));
            if (handled) {
                v.setPressed(false);
                mCancelClick = false;
            }
            return handled;
        }

        public void cancelClick() {
            mCancelClick = true;
        }

    }

}