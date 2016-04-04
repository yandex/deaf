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
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;

import ru.yandex.subtitles.BuildConfig;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.ui.activity.ConversationActivity;
import ru.yandex.subtitles.ui.activity.QuickStartActivity;
import ru.yandex.subtitles.utils.IntentUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;

/**
 * Base class that handles common Android app widget events.
 */
public abstract class AbstractAppWidget extends AppWidgetProvider {

    private static final String ACTION_APPWIDGET_DATASET_CHANGED = BuildConfig.APPLICATION_ID +
            "AbstractAppWidget.ACTION_APPWIDGET_DATASET_CHANGED";

    public static void notifyDataSetChanged(final Context context) {
        context.sendBroadcast(new Intent(ACTION_APPWIDGET_DATASET_CHANGED));
    }

    @Override
    public void onEnabled(final Context context) {
        super.onEnabled(context);
        Analytics.onAppWidgetInstalled(getWidgetType());
    }

    @Override
    public void onDeleted(final Context context, final int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Analytics.onAppWidgetDeleted(getWidgetType());
    }

    @NonNull
    protected abstract String getWidgetType();

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        super.onReceive(context, intent);

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final ComponentName componentName = new ComponentName(context, getClass());
        final int[] widgetIds = appWidgetManager.getAppWidgetIds(componentName);

        final String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action) ||
                ACTION_APPWIDGET_DATASET_CHANGED.equals(action)) {
            notifyAppWidgetsViewDataChanged(appWidgetManager, widgetIds);

        } else if (getStartConversationAction().equals(action)) {
            startConversation(context, intent);

        }
    }

    @NonNull
    protected abstract String getStartConversationAction();

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    private void updateWidgets(final Context context, final AppWidgetManager appWidgetManager,
                               final int[] appWidgetIds) {
        for (final int appWidgetId : appWidgetIds) {
            onUpdateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public abstract void onUpdateWidget(final Context context, final AppWidgetManager appWidgetManager,
                                        final int appWidgetId);

    @NonNull
    protected PendingIntent createStartConversationIntent(final Context context, final int appWidgetId,
                                                          @Nullable final String phrase) {
        final Intent launchIntent = IntentUtils.createActionIntent(context, getClass(),
                getStartConversationAction());
        launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        launchIntent.putExtra(Intent.EXTRA_TEXT, phrase);
        return PendingIntent.getBroadcast(context, appWidgetId, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void notifyAppWidgetsViewDataChanged(final AppWidgetManager appWidgetManager,
                                                 final int[] appWidgetIds) {
        for (final int appWidgetId : appWidgetIds) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list);
        }
    }

    private void startConversation(final Context context, final Intent intent) {
        final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        final String phrase = intent.getStringExtra(Intent.EXTRA_TEXT);

        if (TextUtilsExt.isEmpty(phrase)) {
            Analytics.onAppWidgetStartConversationClick();
        } else {
            Analytics.onAppWidgetPhraseClick(phrase);
        }

        final Intent mainIntent = QuickStartActivity.createStartIntent(context);
        final Intent conversationIntent = ConversationActivity.createStartConversationIntent(context, appWidgetId, phrase);

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(mainIntent);
        stackBuilder.addNextIntent(conversationIntent);
        stackBuilder.startActivities();
    }

}
