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
package ru.yandex.subtitles.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.R;

public final class ShareUtils {

    private static final String LOG_TAG = "ShareUtils";

    public static final String MIMETYPE_TEXT_PLAIN = "text/plain";

    private static final String MAIL_TO_FORMAT = "mailto:%1$s?subject=%2$s&body=%3$s";

    @NonNull
    public static String[] prepareAddresses(final String... addresses) {
        return addresses;
    }

    public static Intent prepareMailToIntent(@NonNull final String[] addresses,
                                             final String subject, final String body) {
        // It's better to use Uri.Builder instead of building strings
        // but Mail.ru app does not work correctly with default 'mailto:' scheme.
        final String mailToScheme = String.format(MAIL_TO_FORMAT,
                TextUtilsExt.join(",", (String[]) addresses),
                Uri.encode(subject), Uri.encode(body));
        final Uri uri = Uri.parse(mailToScheme);

        final Intent mailToIntent = new Intent(Intent.ACTION_SENDTO, uri);
        mailToIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mailToIntent.putExtra(Intent.EXTRA_EMAIL, addresses);
        mailToIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        mailToIntent.putExtra(Intent.EXTRA_TEXT, body);

        return mailToIntent;
    }

    public static void share(final Context context, @Nullable final String title, @NonNull final String data) {
        final CharSequence chooserTitle = context.getResources().getText(R.string.share);
        final String appPackageName = context.getPackageName();

        final Intent intent = buildShareIntent(title, data);

        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

        final List<Intent> targetIntents = new ArrayList<Intent>();
        for (final ResolveInfo currentInfo : activities) {
            final String packageName = currentInfo.activityInfo.packageName;
            final String activityName = currentInfo.activityInfo.name;
            if (!TextUtilsExt.equals(appPackageName, packageName) && !TextUtilsExt.isEmpty(activityName)) {
                final Intent targetIntent = buildShareIntent(title, data);
                targetIntent.setComponent(new ComponentName(packageName, activityName));
                targetIntents.add(targetIntent);
            }
        }

        if (!targetIntents.isEmpty()) {
            try {
                final Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), chooserTitle);
                final Intent[] initialIntents = targetIntents.toArray(new Intent[targetIntents.size()]);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, initialIntents);

                context.startActivity(chooserIntent);
            } catch (final ActivityNotFoundException e) {
                Log.e(LOG_TAG, "No app found to share text data.", e);
            }

        } else {
            Log.e(LOG_TAG, "No app found to share text data.");
        }
    }

    @NonNull
    private static Intent buildShareIntent(final String title, final String data) {
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(MIMETYPE_TEXT_PLAIN);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (!TextUtilsExt.isEmpty(title)) {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, data);
        return shareIntent;
    }

    private ShareUtils() {
    }

}