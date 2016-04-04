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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

public final class NetworkUtils {

    public static boolean isNetworkConnected(final Context context) {
        final NetworkInfo activeNetwork = getActiveNetworkInfo(context);
        return isNetworkConnected(activeNetwork);
    }

    public static boolean isNetworkConnected(@Nullable final NetworkInfo activeNetwork) {
        return (activeNetwork != null && activeNetwork.isConnected());
    }

    public static boolean isNetworkConnectedOrConnecting(final Context context) {
        final NetworkInfo activeNetwork = getActiveNetworkInfo(context);
        return isNetworkConnectedOrConnecting(activeNetwork);
    }

    public static boolean isNetworkConnectedOrConnecting(@Nullable final NetworkInfo activeNetwork) {
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    @Nullable
    public static NetworkInfo getActiveNetworkInfo(final Context context) {
        final ConnectivityManager connectivityManager = getConnectivityManager(context);
        return connectivityManager.getActiveNetworkInfo();
    }

    public static ConnectivityManager getConnectivityManager(final Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private NetworkUtils() {
    }

}
