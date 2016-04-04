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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import ru.yandex.subtitles.content.Subscribeable;

public class NetworkBroadcastReceiver extends BroadcastReceiver implements Subscribeable {

    public interface OnNetworkStateChangeListener {

        void onNetworkStateChanged(@Nullable final NetworkInfo networkInfo);

    }

    private static final String EXTRA_AIRPLANE_STATE = "state";

    private final OnNetworkStateChangeListener mOnNetworkStateChangeListener;
    private final boolean mScansNetworkChanges;

    private boolean mRegistered;

    public NetworkBroadcastReceiver(final OnNetworkStateChangeListener networkStateChangeListener,
                                    final boolean scansNetworkChanges) {
        mOnNetworkStateChangeListener = networkStateChangeListener;
        mScansNetworkChanges = scansNetworkChanges;
    }

    @Override
    public void subscribe(final Context context) {
        if (!mRegistered) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            if (mScansNetworkChanges) {
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            }
            context.registerReceiver(this, filter);

            mRegistered = true;
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // On some versions of Android this may be called with a null Intent,
        // also without extras (getExtras() == null), in such case we use defaults.
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            //if (!intent.hasExtra(EXTRA_AIRPLANE_STATE)) {
            //	return; // No airplane state, ignore it. Should we query Utils.isAirplaneModeOn?
            //}
            // TODO: dispatcher.dispatchAirplaneModeChange(intent.getBooleanExtra(EXTRA_AIRPLANE_STATE, false));

        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            final ConnectivityManager connectivityManager = ApplicationUtils
                    .getSystemService(context, Context.CONNECTIVITY_SERVICE);
            mOnNetworkStateChangeListener.onNetworkStateChanged(connectivityManager.getActiveNetworkInfo());

        }
    }

    @Override
    public void unsubscribe(final Context context) {
        if (mRegistered) {
            context.unregisterReceiver(this);
            mRegistered = false;
        }
    }

}
