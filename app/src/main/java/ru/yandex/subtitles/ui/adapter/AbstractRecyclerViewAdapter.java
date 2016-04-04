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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base adapter class that simplifies working with {@link RecyclerView}.
 * Class provides ability to animate data changes, handle clicks on items.
 */
public abstract class AbstractRecyclerViewAdapter<E, VH extends AbstractViewHolder>
        extends RecyclerView.Adapter<VH> implements AbstractViewHolder.OnViewHolderClickListener {

    /**
     * Interface definition for a callback to be invoked when an item in this
     * AdapterView has been clicked.
     */
    public interface OnItemClickListener<E> {

        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         *
         * @param view     the view that was clicked (this will be a view provided by the adapter)
         * @param position the position of the data entity in the adapter.
         * @param item     the data entity that was clicked.
         */
        void onItemClick(final View view, final int position, final E item);

    }

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    private OnItemClickListener<E> mOnItemClickListener;

    private final List<E> mItems = new ArrayList<E>();

    private final Set<VH> mViewHolders = new HashSet<VH>();

    public AbstractRecyclerViewAdapter(final Context context) {
        super();
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public Context getContext() {
        return mContext;
    }

    public LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }

    /**
     * Register a callback to be invoked when an item in this adapter has
     * been clicked.
     *
     * @param onItemClickListener the callback that will be invoked.
     */
    public void setOnItemClickListener(@Nullable final OnItemClickListener<E> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    protected List<E> getItems() {
        return mItems;
    }

    /**
     * Set entities when new data is loaded.
     * Notice that you should provide unique {@link Object#hashCode()} for every entity
     * to make sure animation will be applied correctly. {@link Object#equals(Object)} should be
     * overriden too.
     *
     * @param items a non-null {@link List} of entities.
     */
    public void setItems(@NonNull final List<E> items) {
        applyAndAnimateRemovals(items);
        applyAndAnimateAdditions(items);
        applyAndAnimateMovedItems(items);
    }

    private void applyAndAnimateRemovals(@NonNull final List<E> items) {
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final E e = mItems.get(i);
            if (!items.contains(e)) {
                remove(i);
            }
        }
    }

    /**
     * Removes entity at a given position and animates removal.
     *
     * @param position an index in the list of entities.
     * @return removed entity.
     */
    public E remove(final int position) {
        final E e = mItems.remove(position);
        notifyItemRemoved(position);
        return e;
    }

    private void applyAndAnimateAdditions(@NonNull final List<E> items) {
        for (int i = 0, count = items.size(); i < count; i++) {
            final E e = items.get(i);
            if (!mItems.contains(e)) {
                add(i, e);
            }
        }
    }

    /**
     * Adds a data entity to a given position.
     *
     * @param position an index in the data set.
     * @param e        a data entity
     */
    public void add(final int position, final E e) {
        mItems.add(position, e);
        notifyItemInserted(position);
    }

    private void applyAndAnimateMovedItems(@NonNull final List<E> items) {
        for (int toPosition = items.size() - 1; toPosition >= 0; toPosition--) {
            final E e = items.get(toPosition);
            final int fromPosition = mItems.indexOf(e);
            if (fromPosition >= 0) {
                if (fromPosition != toPosition) {
                    move(fromPosition, toPosition);
                } else {
                    change(e, fromPosition);
                }
            }
        }
    }

    /**
     * Moves entity from one position to another with animation.
     *
     * @param fromPosition an initial index
     * @param toPosition   a new index
     */
    public void move(final int fromPosition, final int toPosition) {
        final E e = mItems.remove(fromPosition);
        mItems.add(toPosition, e);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void change(final E e, final int fromPosition) {
        mItems.set(fromPosition, e);
        notifyItemChanged(fromPosition);
    }

    /**
     * Removes all entities from adapter.
     */
    public void clear() {
        final int itemCount = mItems.size();
        mItems.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data entity at the specified position.
     */
    @NonNull
    public E getItem(final int position) {
        return mItems.get(position);
    }

    /**
     * Indicates whether all the items in this adapter are enabled. If the
     * value returned by this method changes over time, there is no guarantee
     * it will take effect.  If true, it means all items are selectable and
     * clickable (there is no separator.)
     *
     * @return True if all items are enabled, false otherwise.
     * @see #isEnabled(int)
     */
    public boolean areAllItemsEnabled() {
        return true;
    }

    /**
     * Returns true if the item at the specified position is clickable.
     * <p/>
     * The result is unspecified if position is invalid. An {@link ArrayIndexOutOfBoundsException}
     * should be thrown in that case for fast failure.
     *
     * @param position an index of the item
     * @return {@code true} if item is clickable, {@code false} otherwise.
     * @see #areAllItemsEnabled()
     */
    public boolean isEnabled(final int position) {
        return true;
    }

    /**
     * Returns array of clickable views ids at a given position.
     * Use {@link AbstractViewHolder#DEFAULT_CLICKABLE_VIEW_ID} to make root view clickable.
     *
     * @param position an index in the data set.
     * @return an array of view ids. Use {@link AbstractViewHolder#DEFAULT_CLICKABLE_VIEW_IDS}
     * if only root view should be clickable.
     */
    public int[] getClickableViewIds(final int position) {
        return AbstractViewHolder.DEFAULT_CLICKABLE_VIEW_IDS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        mViewHolders.add(holder);
        holder.setEnabled(areAllItemsEnabled() || isEnabled(position), getClickableViewIds(position));
        holder.setOnViewHolderClickListener(this);
    }

    /**
     * An {@link AbstractViewHolder} callback.
     *
     * @param view     a view that was clicked.
     * @param position an index in the data set.
     */
    @Override
    public void onViewHolderClick(final View view, final int position) {
        //The position check below is a possible solution for the bug which causes
        //ViewHolder.getAdapterPosition to return NO_POSITION for a visible item.
        if (position >= 0 && position < mItems.size()) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position, getItem(position));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewRecycled(final VH holder) {
        super.onViewRecycled(holder);
        mViewHolders.remove(holder);

        holder.onViewRecycled();
        holder.setEnabled(false, getClickableViewIds(holder.getAdapterPosition()));
        holder.setOnViewHolderClickListener(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onFailedToRecycleView(final VH holder) {
        mViewHolders.remove(holder);
        return super.onFailedToRecycleView(holder);
    }

    @NonNull
    public Set<VH> getViewHolders() {
        return mViewHolders;
    }

}
