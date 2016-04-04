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
import android.support.annotation.NonNull;

public final class IntentUtils {

    @NonNull
    public static Intent createActionIntent(final Context context, final Class<?> cls, final String action) {
        final Intent intent = new Intent(context, cls);
        intent.setAction(action);
        return intent;
    }

    public static boolean canStartActivity(@NonNull final Context context, @NonNull final String action) {
        return canStartActivity(context, new Intent(action));
    }

    public static boolean canStartActivity(@NonNull final Context context, @NonNull final Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        return (packageManager != null && packageManager.resolveActivity(intent, 0) != null);
    }

    private IntentUtils() {
    }

}
