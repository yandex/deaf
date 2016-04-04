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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Class enqueues an action to be performed on a different thread than your own.
 * Messages queue will be automatically stopped after specified timeout.
 */
public class AsyncAutoCompleteHandler<E> {

    public interface OnHandleEventListener<E> {

        void onHandleEvent(final E event);

    }

    private static final String LOG_TAG = "AsyncHandler";

    private static final int EVENT_TOKEN = 0xD0D0F00D;
    private static final int SHUTDOWN_TOKEN = 0xDEADBEEF;

    private long mShutdownTimeout = TimeUnit.SECONDS.toMillis(3);
    private OnHandleEventListener<E> mOnHandleEventListener;

    private final String mName;
    private AsyncHandler mHandler;

    private final Object mLock = new Object();

    public AsyncAutoCompleteHandler(final String name) {
        mName = name;
    }

    public void setShutdownTimeout(final long shutdownTimeout) {
        synchronized (mLock) {
            mShutdownTimeout = shutdownTimeout;
        }
    }

    public void setOnHandleEventListener(final OnHandleEventListener<E> onHandleEventListener) {
        mOnHandleEventListener = onHandleEventListener;
    }

    public void post(final E event) {
        post(event, false);
    }

    public void post(final E event, final boolean cancelPrevious) {
        synchronized (mLock) {
            if (mHandler == null) {
                final HandlerThread thread = new HandlerThread(mName, Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
                mHandler = new AsyncHandler(thread.getLooper());
            }

            mHandler.removeMessages(SHUTDOWN_TOKEN);
            if (cancelPrevious) {
                mHandler.removeMessages(EVENT_TOKEN);
            }
            mHandler.obtainMessage(EVENT_TOKEN, event).sendToTarget();
        }
    }

    /* package */ void handleEvent(final E event) {
        if (mOnHandleEventListener != null) {
            mOnHandleEventListener.onHandleEvent(event);
        }
    }

    /* package */ void scheduleShutdown() {
        synchronized (mLock) {
            if (mHandler != null) {
                final Message shutdownMsg = mHandler.obtainMessage(SHUTDOWN_TOKEN);
                mHandler.sendMessageDelayed(shutdownMsg, mShutdownTimeout);
            }
        }
    }

    /* package */ void shutdown() {
        synchronized (mLock) {
            if (mHandler != null) {
                mHandler.getLooper().quit();
                mHandler = null;
            }
        }
    }

    private class AsyncHandler extends Handler {

        public AsyncHandler(final Looper looper) {
            super(looper);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(final Message msg) {
            final int what = msg.what;
            switch (what) {
                case EVENT_TOKEN:
                    final E event = (E) msg.obj;
                    try {
                        handleEvent(event);
                    } catch (final Exception e) {
                        Log.w(LOG_TAG, "An exception occurred during handleEvent()", e);
                    }
                    scheduleShutdown();
                    break;

                case SHUTDOWN_TOKEN:
                    shutdown();
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    }

}
