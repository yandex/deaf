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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

public class OrientationUtils {

    private static final String LOG_TAG = "OrientationUtils";

    public static void toggleScreenOrientation(@NonNull final Activity activity) {
        if (isReversePortrait(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }
    }

    public static boolean isReversePortrait(@NonNull final Activity activity) {
        final int orientation = activity.getRequestedOrientation();
        return orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
    }

    public static boolean isAccelerometerOrientationLocked(@NonNull final Context context) {
        final ContentResolver cr = context.getContentResolver();
        try {
            return Settings.System.getInt(cr, Settings.System.ACCELEROMETER_ROTATION, 1) == 0;
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Failed to get value for Settings.System.ACCELEROMETER_ROTATION. ", e);
            return false;
        }
    }

    private OrientationUtils() {
    }

}
