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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.content.data.Member;
import ru.yandex.subtitles.content.data.Message;
import ru.yandex.subtitles.service.speechkit.speaker.SpeakerBroadcastReceiver;
import ru.yandex.subtitles.ui.widget.SpeakerView;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class ConversationAdapter extends AbstractRecyclerViewAdapter<Message, ConversationAdapter.ViewHolder>
        implements SpeakerBroadcastReceiver.SpeakerEventListener {

    private static final int VIEW_TYPE_INCOMING = 1;
    private static final int VIEW_TYPE_OUTGOING = 2;
    private static final int VIEW_TYPE_PARTIAL = 3;

    private static final int[] CLICKABLE_VIEW_IDS = new int[] { R.id.speaker, R.id.bubble, R.id.more };

    private String mPartialResult;
    private long mPlayingMessageId = -1;

    public ConversationAdapter(final Context context) {
        super(context);
    }

    @Override
    public void onStartPlaying(final long messageId) {
        mPlayingMessageId = messageId;
        notifyDataSetChanged();
    }

    @Override
    public void onStopPlaying(final long messageId) {
        mPlayingMessageId = -1;
        notifyDataSetChanged();
    }

    public void setPartialResult(@Nullable final String partialResult) {
        final boolean hasPreviousPartialResult = hasPartialResult();
        mPartialResult = partialResult;

        final int pos = getItemCount() - 1;
        if (hasPartialResult()) {
            if (hasPreviousPartialResult) {
                notifyItemChanged(pos);
            } else {
                notifyItemInserted(pos);
            }

        } else if (hasPreviousPartialResult) {
            notifyItemRemoved(pos);

        }
    }

    private boolean hasPartialResult() {
        return !TextUtilsExt.isEmpty(mPartialResult);
    }

    private int getPartialResultPosition() {
        return (hasPartialResult() ? getItemCount() - 1 : -1);
    }

    public int getMessagesCount() {
        return super.getItemCount();
    }

    @Override
    public int getItemCount() {
        return getMessagesCount() + (hasPartialResult() ? 1 : 0);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(final int position) {
        return position != getPartialResultPosition();
    }

    @Override
    public int[] getClickableViewIds(final int position) {
        return CLICKABLE_VIEW_IDS;
    }

    @Override
    public int getItemViewType(final int position) {
        if (position == getPartialResultPosition()) {
            return VIEW_TYPE_PARTIAL;

        } else {
            final long member = getItem(position).getUserId();
            return (member == Member.DEVICE_OWNER ? VIEW_TYPE_OUTGOING : VIEW_TYPE_INCOMING);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int itemType) {
        final int layoutId;
        switch (itemType) {
            case VIEW_TYPE_OUTGOING:
                layoutId = R.layout.list_item_outgoing_message;
                break;

            case VIEW_TYPE_INCOMING:
                layoutId = R.layout.list_item_incoming_message;
                break;

            case VIEW_TYPE_PARTIAL:
                layoutId = R.layout.list_item_partial_result;
                break;

            default:
                throw new IllegalStateException("View type=" + itemType + " does not supported.");
        }

        final View view = getLayoutInflater().inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);

        final int itemType = getItemViewType(position);
        switch (itemType) {
            case VIEW_TYPE_INCOMING:
                holder.bindIncomingMessage(getItem(position));
                break;

            case VIEW_TYPE_OUTGOING:
                holder.bindOutgoingMessage(getItem(position), mPlayingMessageId);
                break;

            case VIEW_TYPE_PARTIAL:
                holder.bindPartialResult(mPartialResult);
                break;
        }
    }

    /* package */ static class ViewHolder extends AbstractViewHolder {

        private final SpeakerView mSpeakerView;
        private final View mBubbleView;
        private final TextView mTextView;

        @SuppressWarnings("deprecated")
        public ViewHolder(final View itemView) {
            super(itemView);

            mSpeakerView = findView(R.id.speaker);
            mBubbleView = findView(R.id.bubble);
            mTextView = findView(R.id.text);
        }

        public void bindIncomingMessage(@NonNull final Message message) {
            final int bubbleRes = (message.isPinned() ?
                    R.drawable.bubble_incoming_pinned : R.drawable.bubble_incoming);
            bindMessage(message, bubbleRes);
        }

        public void bindOutgoingMessage(@NonNull final Message message, final long playingMessageId) {
            if (mSpeakerView != null) {
                if (playingMessageId == message.getId()) {
                    mSpeakerView.onStartPlaying();

                } else {
                    mSpeakerView.onStopPlaying();
                }
            }

            final int bubbleRes = (message.isPinned() ?
                    R.drawable.bubble_outgoing_pinned : R.drawable.bubble_outgoing);
            bindMessage(message, bubbleRes);
        }

        private void bindMessage(@NonNull final Message message, @DrawableRes final int bubbleRes) {
            mBubbleView.setBackgroundResource(bubbleRes);

            final String text = message.getText();
            if (message.isPinned()) {
                final Spannable spannable = new SpannableString(text + " !");
                final ImageSpan imagespan = new ImageSpan(getContext(),
                        R.drawable.ic_pin, DynamicDrawableSpan.ALIGN_BASELINE);
                spannable.setSpan(imagespan, spannable.length() - 1,
                        spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTextView.setText(spannable);
            } else {
                mTextView.setText(message.getText());
            }
        }

        public void bindPartialResult(final String partialResult) {
            mTextView.setText(partialResult);
        }

        @Override
        public void onViewRecycled() {
            super.onViewRecycled();
            if (mSpeakerView != null) {
                mSpeakerView.onStopPlaying();
            }
        }
    }

}
