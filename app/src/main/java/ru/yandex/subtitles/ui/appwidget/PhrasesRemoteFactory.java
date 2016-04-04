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
package ru.yandex.subtitles.ui.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.content.dao.PhrasesDAO;
import ru.yandex.subtitles.content.data.Phrase;

public class PhrasesRemoteFactory implements RemoteViewsService.RemoteViewsFactory {

    /**
     * Lock to avoid race condition between widgets.
     */
    private static final Object sWidgetLock = new Object();

    private final Context mContext;
    private final PhrasesDAO mPhrasesDao;

    private final int mAppWidgetId;
    private List<Phrase> mPhrases = new ArrayList<Phrase>();

    public PhrasesRemoteFactory(final Context context, final Intent intent) {
        mContext = context;
        mPhrasesDao = new PhrasesDAO(context);

        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int getCount() {
        synchronized (sWidgetLock) {
            return mPhrases.size();
        }
    }

    @Override
    public long getItemId(final int position) {
        synchronized (sWidgetLock) {
            return mPhrases.get(position).getId();
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public void onDataSetChanged() {
        synchronized (sWidgetLock) {
            mPhrases.clear();
            final long identityToken = Binder.clearCallingIdentity();
            try {
                mPhrases.addAll(mPhrasesDao.getStartingPhrases());
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        }
    }

    @Override
    public RemoteViews getViewAt(final int position) {
        synchronized (sWidgetLock) {
            final Phrase phrase = mPhrases.get(position);

            final RemoteViews itemView = new RemoteViews(mContext.getPackageName(), R.layout.list_item_appwidget_phrase);
            itemView.setTextViewText(R.id.text, phrase.getText());

            final Bundle extras = new Bundle();
            extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            extras.putString(Intent.EXTRA_TEXT, phrase.getText());

            final Intent clickIntent = new Intent();
            clickIntent.putExtras(extras);

            itemView.setOnClickFillInIntent(R.id.phrase, clickIntent);

            return itemView;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public void onDestroy() {
        synchronized (sWidgetLock) {
            mPhrases.clear();
        }
    }

}
