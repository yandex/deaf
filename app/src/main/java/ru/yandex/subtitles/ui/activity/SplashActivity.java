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
package ru.yandex.subtitles.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import ru.yandex.subtitles.Preferences;
import ru.yandex.subtitles.R;

public class SplashActivity extends AbstractActivity implements Runnable {

    private static final long SPLASH_SCREEN_TIME_MILLIS = 2000L;

    private static final String KEY_START_TIME = "start_time";

    private Handler mHandler;
    private long mStartTime;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());

        // This is hack for one instance application.
        // The problem is described here:
        // http://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ
        // http://code.google.com/p/android/issues/detail?id=2373
        // http://code.google.com/p/android/issues/detail?id=5277
        if (!isTaskRoot()) {
            // close activity immediately when it is not a root task.
            closeActivity();

        } else {
            setContentView(R.layout.activity_splash);

            mStartTime = System.currentTimeMillis();
            if (savedInstanceState != null) {
                mStartTime = savedInstanceState.getLong(KEY_START_TIME, System.currentTimeMillis());
            }
            emulateLoading(mStartTime);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_START_TIME, mStartTime);
    }

    @Override
    public void onBackPressedAction() {
    }

    private void emulateLoading(final long startTime) {
        long ms = 0;
        final long now = System.currentTimeMillis();
        if (now - startTime < SPLASH_SCREEN_TIME_MILLIS) {
            ms = SPLASH_SCREEN_TIME_MILLIS - (now - startTime);
        }
        mHandler.postDelayed(this, ms);
    }

    @Override
    public void run() {
        if (Preferences.getInstance().isFirstLaunch()) {
            QuestionnaireActivity.start(this);
        } else {
            QuickStartActivity.start(this);
        }
        closeActivity();
    }

    private void closeActivity() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

}
