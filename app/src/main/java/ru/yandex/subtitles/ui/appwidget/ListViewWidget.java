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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import ru.yandex.subtitles.BuildConfig;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.utils.BitmapUtils;
import ru.yandex.subtitles.utils.IntentUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class ListViewWidget extends AbstractAppWidget {

    public static final String ACTION_START_CONVERSATION = BuildConfig.APPLICATION_ID +
            "ListViewWidget.ACTION_START_CONVERSATION";

    @NonNull
    @Override
    protected String getWidgetType() {
        return Analytics.APPWIDGET_TYPE_LARGE;
    }

    @NonNull
    @Override
    protected String getStartConversationAction() {
        return ACTION_START_CONVERSATION;
    }

    @Override
    public void onUpdateWidget(final Context context, final AppWidgetManager appWidgetManager,
                               final int appWidgetId) {
        final RemoteViews widgetView = new RemoteViews(context.getPackageName(),
                R.layout.appwidget_phrases);

        final Intent adapter = createAdapterIntent(context, appWidgetId);
        widgetView.setRemoteAdapter(R.id.list, adapter);
        final PendingIntent listClickIntent = createPhraseIntent(context, appWidgetId);
        widgetView.setPendingIntentTemplate(R.id.list, listClickIntent);
        widgetView.setEmptyView(R.id.list, R.id.empty);

        final PendingIntent launchIntent = createStartConversationIntent(context, appWidgetId, null);
        widgetView.setOnClickPendingIntent(R.id.start_conversation, launchIntent);

        final Resources resources = context.getResources();
        final Bitmap textBitmap = BitmapUtils.createTypefaceBitmap(context,
                TextUtilsExt.toUpperCase(resources.getString(R.string.start_conversation)),
                resources.getColor(R.color.text_primary_color),
                resources.getDimensionPixelSize(R.dimen.text_size_primary));
        widgetView.setImageViewBitmap(R.id.start_conversation, textBitmap);

        appWidgetManager.updateAppWidget(appWidgetId, widgetView);
    }

    @NonNull
    private Intent createAdapterIntent(final Context context, final int appWidgetId) {
        final Intent adapter = new Intent(context, PhrasesAppWidgetRemoteService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        adapter.setData(Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME)));
        return adapter;
    }

    @NonNull
    private PendingIntent createPhraseIntent(final Context context, final int appWidgetId) {
        final Intent listClickIntent = IntentUtils.createActionIntent(context,
                ListViewWidget.class, ACTION_START_CONVERSATION);
        listClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        listClickIntent.setData(Uri.parse(listClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, appWidgetId, listClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
