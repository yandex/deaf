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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import ru.yandex.speechkit.Error;
import ru.yandex.subtitles.R;
import ru.yandex.subtitles.analytics.ConversationMetadata;
import ru.yandex.subtitles.content.Subscribeable;
import ru.yandex.subtitles.service.MessagingService;
import ru.yandex.subtitles.service.messaging.CreateConversationBroadcastReceiver;
import ru.yandex.subtitles.service.speechkit.ErrorResolver;
import ru.yandex.subtitles.service.speechkit.initializer.SpeechKitInitializedBroadcastReceiver;
import ru.yandex.subtitles.service.speechkit.recognition.RecognitionBroadcastReceiver;
import ru.yandex.subtitles.ui.fragment.conversations.ConversationFragment;
import ru.yandex.subtitles.ui.fragment.conversations.ZoomedMessagesFragment;
import ru.yandex.subtitles.ui.fragment.interactive.NoConnectionDialogFragment;
import ru.yandex.subtitles.ui.widget.InterceptableToolbar;
import ru.yandex.subtitles.ui.widget.microphonebar.MicrophoneBarController;
import ru.yandex.subtitles.ui.widget.microphonebar.MicrophoneBarListener;
import ru.yandex.subtitles.ui.widget.microphonebar.MicrophoneBarView;
import ru.yandex.subtitles.utils.ApplicationUtils;
import ru.yandex.subtitles.utils.IntentUtils;
import ru.yandex.subtitles.utils.NetworkBroadcastReceiver;
import ru.yandex.subtitles.utils.NetworkUtils;

public class ConversationActivity extends AbstractActivity
        implements CreateConversationBroadcastReceiver.OnCreateConversationListener,
        ConversationFragment.OnConversationListener,
        ZoomedMessagesFragment.OnZoomedMessagesListener,
        MicrophoneBarListener,
        SpeechKitInitializedBroadcastReceiver.OnSpeechKitInitializedListener,
        RecognitionBroadcastReceiver.RecognitionLifecycleCallbacks,
        NetworkBroadcastReceiver.OnNetworkStateChangeListener {

    private static final String KEY_HAS_CREATE_CONVERSATION_RECEIVER = "has_create_conversation_receiver";
    private static final String KEY_START_RECOGNITION = "start_recognition";
    private static final String KEY_THREAD = "thread_id";
    private static final String KEY_METADATA = "metadata";
    private static final String KEY_HAS_NETWORK_ERROR = "has_network_error";

    private static final String EXTRA_THREAD = "thread_id";
    private static final String EXTRA_FROM_WIDGET = "from_widget";

    public static void startConversation(final Context context) {
        startConversation(context, null);
    }

    @NonNull
    public static Intent createStartConversationIntent(final Context context,
                                                       @Nullable final Integer appWidgetId,
                                                       @Nullable final String phrase) {
        final Intent intent = IntentUtils.createActionIntent(context,
                ConversationActivity.class, Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, phrase);
        intent.putExtra(EXTRA_FROM_WIDGET, appWidgetId != null);
        return intent;
    }

    public static void startConversation(final Context context, @Nullable final String phrase) {
        final Intent intent = createStartConversationIntent(context, null, phrase);
        context.startActivity(intent);
    }

    public static void openConversation(final Context context, final long threadId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                ConversationActivity.class, Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_THREAD, threadId);
        context.startActivity(intent);
    }

    private Subscribeable mSpeechKitReceiver;
    private Subscribeable mCreateConversationReceiver;
    private Subscribeable mRecognitionReceiver;

    private boolean mHasNetworkError = false;
    private Subscribeable mNetworkChangesReceiver;

    private MicrophoneBarView mMicrophoneBarView;

    private boolean mRecognitionStarted = false;
    private boolean mSpeechKitInitialized = false;
    private long mThreadId = -1;
    private ConversationMetadata mMetadata;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        mMicrophoneBarView = findView(R.id.microphone_bar);
        mMicrophoneBarView.setMicrophoneBarListener(this);

        final InterceptableToolbar toolbar = findView(R.id.toolbar);
        if (ApplicationUtils.hasLollipop()) {
            toolbar.setElevation(0.f);
        }
        toolbar.setNavigationIcon(R.drawable.ic_ab_back_mtrl_black);
        toolbar.setIntersectionTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                return mMicrophoneBarView.onTouchEvent(event);
            }
        });
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        if (savedInstanceState == null) {
            mMetadata = new ConversationMetadata();
            parseIntent(getIntent());

        } else {
            mThreadId = savedInstanceState.getLong(KEY_THREAD);
            mMetadata = savedInstanceState.getParcelable(KEY_METADATA);
            mHasNetworkError = savedInstanceState.getBoolean(KEY_HAS_NETWORK_ERROR);
            mRecognitionStarted = savedInstanceState.getBoolean(KEY_START_RECOGNITION);

            if (savedInstanceState.getBoolean(KEY_HAS_CREATE_CONVERSATION_RECEIVER)) {
                registerCreateConversationReceiver();
                
            } else {
                registerRecognitionReceiver();
            }
        }

        mSpeechKitReceiver = new SpeechKitInitializedBroadcastReceiver(this);
        mSpeechKitReceiver.subscribe(this);
        MessagingService.initializeSpeechKit(this);

        mNetworkChangesReceiver = new NetworkBroadcastReceiver(this, true);
    }

    private void parseIntent(final Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            final boolean fromWidget = intent.getBooleanExtra(EXTRA_FROM_WIDGET, false);
            mMetadata.setFromWidget(fromWidget);

            final String phrase = intent.getStringExtra(Intent.EXTRA_TEXT);
            createConversationWithPhrase(phrase);

        } else if (Intent.ACTION_VIEW.equals(action)) {
            mThreadId = intent.getLongExtra(EXTRA_THREAD, ConversationFragment.STUB_CONVERSATION);
            showFragmentNoAnimate(ConversationFragment.newInstance(mThreadId, false, mMetadata), ConversationFragment.TAG);
            checkNetworkConnectivityState();
            registerRecognitionReceiver();

        } else {
            // Fallback for no action case. Typically should never happen
            throw new IllegalStateException("Please provide correct intent action to launch conversation activity.");
        }
    }

    private void createConversationWithPhrase(@Nullable final String phrase) {
        registerCreateConversationReceiver();
        MessagingService.createConversation(this, phrase);
    }

    private void registerCreateConversationReceiver() {
        mCreateConversationReceiver = new CreateConversationBroadcastReceiver(this);
        mCreateConversationReceiver.subscribe(this);
    }

    @Override
    public void onConversationCreated(final long threadId, @Nullable final Long messageId,
                                      @Nullable final Long memberId) {
        unregisterCreateConversationReceiver();

        mThreadId = threadId;
        registerRecognitionReceiver();

        mRecognitionStarted = true;
        autoStartRecognition();

        if (messageId != null && memberId != null) {
            showFragmentNoAnimateAllowingStateLoss(ZoomedMessagesFragment
                    .newInstance(threadId, messageId, memberId, mMetadata), ZoomedMessagesFragment.TAG);
        } else {
            showFragmentNoAnimateAllowingStateLoss(ConversationFragment
                    .newInstance(threadId, true, mMetadata), ConversationFragment.TAG);
            checkNetworkConnectivityState();
        }
    }

    private void registerRecognitionReceiver() {
        mRecognitionReceiver = new RecognitionBroadcastReceiver(mThreadId, this);
        mRecognitionReceiver.subscribe(this);
    }

    @Override
    public void onConversationCreateError(final int kind) {
        unregisterCreateConversationReceiver();
        showFragmentNoAnimateAllowingStateLoss(ConversationFragment
                .newInstance(ConversationFragment.STUB_CONVERSATION, false, mMetadata), ConversationFragment.TAG);
        checkNetworkConnectivityState();
        // TODO: onConversationCreateError
    }

    private void unregisterCreateConversationReceiver() {
        if (mCreateConversationReceiver != null) {
            mCreateConversationReceiver.unsubscribe(this);
            mCreateConversationReceiver = null;
        }
    }

    @Override
    public void onSpeechKitInitialized() {
        mSpeechKitInitialized = true;
        mHasNetworkError = false;
        mMicrophoneBarView.setState(MicrophoneBarController.STATE_IDLE);
        autoStartRecognition();
    }

    @Override
    public void onMessageClick(final long threadId, final long messageId, final long memberId) {
        showFragment(ZoomedMessagesFragment.newInstance(threadId, messageId, memberId, mMetadata),
                ZoomedMessagesFragment.TAG);
    }

    @Override
    public void onPhraseRecognized(@NonNull final String phrase) {
        mMicrophoneBarView.showMessage(getString(R.string.phrase_recognized));
    }

    @Override
    public void onExitFullscreenClick(final long threadId) {
        if (switchToFragment(ConversationFragment.TAG) == null) {
            showFragment(ConversationFragment.newInstance(threadId, false, mMetadata), ConversationFragment.TAG,
                    /* addToBackStack */ true, /* clearBackStack */ true,
                    /* animate */ true, /* allow state loss */ false);
            checkNetworkConnectivityState();
        }
    }

    private void checkNetworkConnectivityState() {
        if (!NetworkUtils.isNetworkConnectedOrConnecting(this) && !mSpeechKitInitialized) {
            mHasNetworkError = true;
            onSpeechKitNetworkError();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void autoStartRecognition() {
        if (mSpeechKitInitialized && mRecognitionStarted && isActivityResumed()) {
            mRecognitionStarted = true;
            mMicrophoneBarView.setState(MicrophoneBarController.STATE_IN_PROGRESS);
            MessagingService.startRecognition(this, mThreadId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNetworkChangesReceiver.subscribe(this);
        autoStartRecognition();
    }

    @Override
    public void onMicrophoneClicked(final MicrophoneBarController controller,
                                    @MicrophoneBarController.State final int state) {
        switch (state) {
            case MicrophoneBarController.STATE_IDLE:
            case MicrophoneBarController.STATE_STOPPING:
                mRecognitionStarted = true;
                mMicrophoneBarView.setState(MicrophoneBarController.STATE_IN_PROGRESS);
                MessagingService.startRecognition(this, mThreadId);
                break;

            case MicrophoneBarController.STATE_IN_PROGRESS:
            case MicrophoneBarController.STATE_RECORDING:
                mRecognitionStarted = false;
                mMicrophoneBarView.setState(MicrophoneBarController.STATE_STOPPING);
                MessagingService.stopRecognition(this);
                break;
        }
    }

    @Override
    public void onRecognitionStarted(final boolean resumedAfterPlaying) {
        mHasNetworkError = false;

        mMicrophoneBarView.setKeepScreenOn(true);
        mMicrophoneBarView.setState(MicrophoneBarController.STATE_RECORDING);
        if (resumedAfterPlaying) {
            mMicrophoneBarView.showMessage("");
        }
    }

    @Override
    public void onPowerUpdate(final float power) {
        mMicrophoneBarView.setVolume(power);
    }

    @Override
    public void onError(final Error error) {
        mMicrophoneBarView.setState(MicrophoneBarController.STATE_IDLE);
        if (error.getCode() == Error.ERROR_NETWORK) {
            mHasNetworkError = true;
            mMicrophoneBarView.showMessage("");
            onSpeechKitNetworkError();
        } else {
            mMicrophoneBarView.showErrorMessage(getString(ErrorResolver.resolveError(error)));
        }
        mMicrophoneBarView.setKeepScreenOn(false);
    }

    @Override
    public void onNetworkStateChanged(@Nullable final NetworkInfo networkInfo) {
        if (NetworkUtils.isNetworkConnected(networkInfo)) {
            if (mRecognitionStarted && mHasNetworkError) {
                mMicrophoneBarView.setState(MicrophoneBarController.STATE_RECORDING);
                MessagingService.startRecognition(this, mThreadId);
            }
            mHasNetworkError = false;
        }
    }

    @Override
    public void onRecognitionDone(final boolean willStartPlaying) {
        mHasNetworkError = false;
        mMicrophoneBarView.setKeepScreenOn(false);
        if (willStartPlaying) {
            mMicrophoneBarView.setState(MicrophoneBarController.STATE_IDLE);
        } else {
            mMicrophoneBarView.setState(MicrophoneBarController.STATE_STOPPING);
        }
    }

    private void onSpeechKitNetworkError() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(NoConnectionDialogFragment.TAG) == null) {
            final NoConnectionDialogFragment dialog = NoConnectionDialogFragment.newInstance();
            dialog.show(fragmentManager, NoConnectionDialogFragment.TAG);
        }
    }

    @Override
    public void onPause() {
        mNetworkChangesReceiver.unsubscribe(this);
        MessagingService.stopRecognition(this);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_HAS_CREATE_CONVERSATION_RECEIVER, mCreateConversationReceiver != null);
        outState.putBoolean(KEY_START_RECOGNITION, mRecognitionStarted);
        outState.putParcelable(KEY_METADATA, mMetadata);
        outState.putLong(KEY_THREAD, mThreadId);
        outState.putBoolean(KEY_HAS_NETWORK_ERROR, mHasNetworkError);
    }

    @Override
    protected void onDestroy() {
        mSpeechKitReceiver.unsubscribe(this);
        unregisterRecognitionReceiver();
        unregisterCreateConversationReceiver();
        super.onDestroy();
    }

    private void unregisterRecognitionReceiver() {
        if (mRecognitionReceiver != null) {
            mRecognitionReceiver.unsubscribe(this);
        }
    }

    @Override
    public void finish() {
        MessagingService.clearEmptyConversations(this);
        super.finish();
    }

}