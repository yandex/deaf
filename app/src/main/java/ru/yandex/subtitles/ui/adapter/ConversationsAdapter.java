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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.content.data.Conversation;
import ru.yandex.subtitles.content.data.Message;
import ru.yandex.subtitles.content.data.Thread;
import ru.yandex.subtitles.utils.DateTimeUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class ConversationsAdapter extends AbstractRecyclerViewAdapter<Conversation, ConversationsAdapter.ViewHolder> {

    private static final int[] CLICKABLE_VIEW_IDS = new int[] { AbstractViewHolder.DEFAULT_CLICKABLE_VIEW_ID, R.id.more };

    public ConversationsAdapter(final Context context) {
        super(context);
    }

    @Override
    public int[] getClickableViewIds(final int position) {
        return CLICKABLE_VIEW_IDS;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View itemView = getLayoutInflater().inflate(R.layout.list_item_conversation, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        holder.bind(getItem(position));
    }

    /* package */ static class ViewHolder extends AbstractViewHolder {

        private static final int CHARS_PER_LINE = 30;
        private static final int CHARS_LIMIT = 45;

        private static final String ELLIPSIS = "…";

        private View mPinnedView;
        private TextView mTimeView;
        private TextView mTextView;

        public ViewHolder(final View itemView) {
            super(itemView);

            mPinnedView = findView(R.id.pinned);
            mTimeView = findView(R.id.time);
            mTextView = findView(R.id.text);
        }

        public void bind(@NonNull final Conversation conversation) {
            final Thread thread = conversation.getThread();
            final Message message = conversation.getLastMessage();

            mPinnedView.setBackgroundResource(thread.isPinned() ?
                    R.color.conversation_pinned_color :
                    android.R.color.transparent);

            final CharSequence formattedTime = formatRelativeTime(getContext(), message.getTime());
            mTimeView.setText(formattedTime);

            String messageText = message.getText();
            if (messageText.length() >= CHARS_LIMIT) {
                messageText = TextUtilsExt.safeSubString(messageText, 0, CHARS_LIMIT).trim() + ELLIPSIS;
            }

            if (thread.isPinned()) {
                final Spannable spannable = new SpannableString(messageText + " !");
                final ImageSpan imagespan = new ImageSpan(getContext(),
                        R.drawable.ic_pin, DynamicDrawableSpan.ALIGN_BASELINE);
                spannable.setSpan(imagespan, spannable.length() - 1,
                        spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTextView.setText(spannable);
            } else {
                mTextView.setText(messageText);
            }
        }

        @Nullable
        public static CharSequence formatRelativeTime(final Context context, final long timestamp) {
            if (DateTimeUtils.isToday(timestamp)) {
                final int flags = DateUtils.FORMAT_SHOW_TIME;
                final CharSequence cs = DateUtils.formatDateTime(context, timestamp, flags);
                return context.getString(R.string.today_time_format, cs);

            } else if (DateTimeUtils.isYesterday(timestamp)) {
                final int flags = DateUtils.FORMAT_SHOW_TIME;
                final CharSequence cs = DateUtils.formatDateTime(context, timestamp, flags);
                return context.getString(R.string.yesterday_time_format, cs);

            } else {
                final int flags = (DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
                return context.getString(R.string.date_time_format,
                        DateUtils.formatDateTime(context, timestamp, flags),
                        DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_TIME));
            }
        }

    }

}
