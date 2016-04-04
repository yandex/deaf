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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;

import ru.yandex.subtitles.BuildConfig;
import ru.yandex.subtitles.R;

public final class ApplicationUtils {

    private static final String LOG_TAG = "ApplicationUtils";

    private static final int YEAR_OF_FIRST_PUBLICATION = 2015;

    @SuppressWarnings("unchecked")
    public static <S> S getSystemService(final Context context, final String serviceName) {
        return (S) context.getSystemService(serviceName);
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ||
                Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static void brandGlowColor(final Context context) {
        final Resources resources = context.getResources();
        final int color = resources.getColor(R.color.overscroll_color);
        if (!hasLollipop()) {
            brandGlowDrawableColor(resources, "overscroll_glow", color);
            brandGlowDrawableColor(resources, "overscroll_edge", color);
        }
    }

    // It is the only one method to change built-in colors and yes - it's a hack
    private static void brandGlowDrawableColor(final Resources resources, final String drawable, final int color) {
        final int drawableRes = resources.getIdentifier(drawable, "drawable",
                "android");
        try {
            final Drawable d = resources.getDrawable(drawableRes);
            if (d != null) {
                d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        } catch (final Resources.NotFoundException rnfe) {
            Log.e(LOG_TAG, "Failed to find drawable for resource " + drawable);
        }
    }

    public static void openGooglePlayDeveloper(final Context context, final String developerName,
                                               final CharSequence dialogName) {
        final String googlePlayDeveloperUrl = "https://play.google.com/store/apps/developer?id=" + developerName;

        final Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://search?q=pub:" + developerName));
        if (IntentUtils.canStartActivity(context, intent)) {
            context.startActivity(Intent.createChooser(intent, dialogName));
        } else {
            openWebBrowser(context, googlePlayDeveloperUrl);
        }
    }

    public static void openGooglePlay(final Context context, final String packageName) {
        final Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + packageName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (IntentUtils.canStartActivity(context, intent)) {
            context.startActivity(intent);
        } else {
            openWebBrowser(context, "http://play.google.com/store/apps/details?id=" + packageName);
        }
    }

    public static void openWebBrowser(final Context context, @Nullable final String url) {
        if (TextUtilsExt.isEmpty(url)) {
            return;
        }

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (IntentUtils.canStartActivity(context, intent)) {
            context.startActivity(intent);
        } else {
            Log.e(LOG_TAG, "Can't open web link in browser. No app found to handle intent.");
        }
    }

    public static String getCopyright(final Context context) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(BuildConfig.BUILD_TIME);

        final int buildYear = calendar.get(Calendar.YEAR);
        return context.getString(R.string.copyright_format,
                buildYear > YEAR_OF_FIRST_PUBLICATION ?
                        (YEAR_OF_FIRST_PUBLICATION) : buildYear);
    }

    @NonNull
    public static String getDeviceInfo(final Context context) {
        final StringBuilder result = new StringBuilder();
        result.append(context.getString(R.string.device_model_format, getDeviceModel()));
        result.append("\n");

        result.append(context.getString(R.string.android_version_format, getAndroidVersion()));

        final String appVersion = getAppVersion(context);
        if (!TextUtilsExt.isEmpty(appVersion)) {
            result.append("\n");
            result.append(context.getString(R.string.build_version_format, appVersion));
        }

        return result.toString();
    }

    @NonNull
    public static String getDeviceModel() {
        final String manufacturer = Build.MANUFACTURER;
        final String model = Build.MODEL;
        if (TextUtilsExt.equalsIgnoreCase(manufacturer, (model.length() > manufacturer.length())
                ? model.substring(0, manufacturer.length()) : "")) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    @NonNull
    public static String getAndroidVersion() {
        return String.valueOf(Build.VERSION.RELEASE);
    }

    @NonNull
    public static String getAppVersion(final Context context) {
        String versionName;
        try {
            versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Failed to get app version.", e);
            versionName = "";
        }
        return versionName;
    }

    private ApplicationUtils() {
    }

}
