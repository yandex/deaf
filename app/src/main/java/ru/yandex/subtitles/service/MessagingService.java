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
package ru.yandex.subtitles.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Initializer;
import ru.yandex.speechkit.SpeechKit;
import ru.yandex.subtitles.Preferences;
import ru.yandex.subtitles.content.dao.PhrasesDAO;
import ru.yandex.subtitles.content.data.Member;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.service.messaging.Messenger;
import ru.yandex.subtitles.service.speechkit.initializer.InitializerCallbacks;
import ru.yandex.subtitles.service.speechkit.initializer.SpeechKitInitializedBroadcastReceiver;
import ru.yandex.subtitles.service.speechkit.initializer.SpeechKitInitializer;
import ru.yandex.subtitles.service.speechkit.recognition.PhraseBroadcastReceiver;
import ru.yandex.subtitles.service.speechkit.recognition.RecognitionClient;
import ru.yandex.subtitles.service.speechkit.recognition.RecognitionListener;
import ru.yandex.subtitles.service.speechkit.speaker.Speaker;
import ru.yandex.subtitles.service.speechkit.speaker.SpeakerBroadcastReceiver;
import ru.yandex.subtitles.service.speechkit.speaker.SpeakerBuilder;
import ru.yandex.subtitles.service.speechkit.speaker.SpeakerListener;
import ru.yandex.subtitles.service.speechkit.speaker.Voice;
import ru.yandex.subtitles.utils.IntentUtils;

/**
 * Class that handles all message-related (recognition, vocalization, message sending, etc.) events.
 */
public class MessagingService extends Service implements InitializerCallbacks,
        RecognitionListener, SpeakerListener {

    private static final String LOG_TAG = "MessagingService";

    /**
     * Initializes SpeechKit. Should be called when application is created
     */
    public static void start(final Context context) {
        SpeechKit.getInstance(); // Obtain instance of SpeechKit and load *.so libraries
        context.startService(new Intent(context, MessagingService.class));
    }

    /**
     * Simulate sticky broadcast action
     */
    private static final String ACTION_INITIALIZE_SPEECH_KIT = "MessagingService.ACTION_INITIALIZE_SPEECH_KIT";

    public static void initializeSpeechKit(final Context context) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_INITIALIZE_SPEECH_KIT);
        context.startService(intent);
    }

    /**
     * Messaging actions
     */
    private static final String ACTION_CREATE_CONVERSATION = "MessagingService.ACTION_CREATE_CONVERSATION";
    private static final String ACTION_CLEAR_EMPTY_CONVERSATIONS = "MessagingService.ACTION_CLEAR_EMPTY_CONVERSATIONS";
    private static final String ACTION_PIN_CONVERSATION = "MessagingService.ACTION_PIN_CONVERSATION";
    private static final String ACTION_UNPIN_CONVERSATION = "MessagingService.ACTION_UNPIN_CONVERSATION";
    private static final String ACTION_DELETE_CONVERSATION = "MessagingService.ACTION_DELETE_CONVERSATION";
    private static final String ACTION_CONVERSATION_OPENED = "MessagingService.ACTION_CONVERSATION_OPENED";
    private static final String ACTION_SEND_MESSAGE = "MessagingService.ACTION_SEND_MESSAGE";
    private static final String ACTION_PIN_MESSAGE = "MessagingService.ACTION_PIN_MESSAGE";
    private static final String ACTION_UNPIN_MESSAGE = "MessagingService.ACTION_UNPIN_MESSAGE";
    private static final String ACTION_DELETE_MESSAGE = "MessagingService.ACTION_DELETE_MESSAGE";

    private static final String EXTRA_THREAD_ID = "thread_id";
    private static final String EXTRA_MESSAGE_ID = "message_id";

    public static void createConversation(final Context context, @Nullable final String phrase) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_CREATE_CONVERSATION);
        intent.putExtra(Intent.EXTRA_TEXT, phrase);
        context.startService(intent);
    }

    public static void clearEmptyConversations(final Context context) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_CLEAR_EMPTY_CONVERSATIONS);
        context.startService(intent);
    }

    public static void pinConversation(final Context context, final long threadId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_PIN_CONVERSATION);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        context.startService(intent);
    }

    public static void unpinConversation(final Context context, final long threadId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_UNPIN_CONVERSATION);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        context.startService(intent);
    }

    public static void deleteConversation(final Context context, final long threadId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_DELETE_CONVERSATION);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        context.startService(intent);
    }

    public static void onConversationOpened(final Context context, final long threadId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_CONVERSATION_OPENED);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        context.startService(intent);
    }

    public static void sendMessage(final Context context, final long threadId, final String text) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_SEND_MESSAGE);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startService(intent);
    }

    public static void pinMessage(final Context context, final long messageId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_PIN_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE_ID, messageId);
        context.startService(intent);
    }

    public static void unpinMessage(final Context context, final long messageId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_UNPIN_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE_ID, messageId);
        context.startService(intent);
    }

    public static void deleteMessage(final Context context, final long messageId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_DELETE_MESSAGE);
        intent.putExtra(EXTRA_MESSAGE_ID, messageId);
        context.startService(intent);
    }

    /**
     * Recognition actions
     */
    private static final String ACTION_START_RECOGNITION = "MessagingService.ACTION_START_RECOGNITION";
    private static final String ACTION_STOP_RECOGNITION = "MessagingService.ACTION_STOP_RECOGNITION";

    public static void startRecognition(final Context context, final long threadId) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_START_RECOGNITION);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        context.startService(intent);
    }

    public static void stopRecognition(final Context context) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_STOP_RECOGNITION);
        context.startService(intent);
    }

    /**
     * Vocalization actions
     */
    private static final String ACTION_TOGGLE_VOCALIZATION = "MessagingService.ACTION_TOGGLE_VOCALIZATION";
    private static final String ACTION_STOP_VOCALIZATION = "MessagingService.ACTION_STOP_VOCALIZATION";

    public static void toggleVocalization(final Context context, final long messageId, final String text) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_TOGGLE_VOCALIZATION);
        intent.putExtra(EXTRA_MESSAGE_ID, messageId);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startService(intent);
    }

    public static void stopVocalization(final Context context) {
        final Intent intent = IntentUtils.createActionIntent(context,
                MessagingService.class, ACTION_STOP_VOCALIZATION);
        context.startService(intent);
    }

    private static final int STATE_NOT_READY = 1;
    private static final int STATE_SPEECH_KIT_INITIALIZATION = 1 << 1;
    private static final int STATE_SPEECH_KIT_INITIALIZED = 1 << 2;
    private static final int STATE_FREE = 1 << 3;
    private static final int STATE_RECOGNITION = 1 << 4;
    private static final int STATE_RECOGNITION_PAUSED = 1 << 5;

    private int mState = STATE_NOT_READY;

    private RecognitionClient mRecognitionClient;
    private Messenger mMessenger;
    private Speaker mSpeaker;

    private PhrasesDAO mPhrasesDao;

    @Override
    public void onCreate() {
        super.onCreate();
        mMessenger = new Messenger(this);
        mPhrasesDao = new PhrasesDAO(this);
        initializeSpeechKit();
    }

    private boolean hasState(final int state) {
        return ((mState & state) == state);
    }

    private void initializeSpeechKit() {
        final SpeechKitInitializer speechKitInitializer = new SpeechKitInitializer(this, this);
        speechKitInitializer.start();
    }

    @Override
    public void onSpeechKitInitializerBegin(final Initializer initializer) {
        mState |= STATE_SPEECH_KIT_INITIALIZATION;
    }

    @Override
    public void onSpeechKitInitialized(final Initializer initializer) {
        mState |= STATE_SPEECH_KIT_INITIALIZED | STATE_FREE;
        mState &= ~STATE_NOT_READY;
        mState &= ~STATE_SPEECH_KIT_INITIALIZATION;
        SpeechKitInitializedBroadcastReceiver.onSpeechKitInitialized(this);
    }

    @Override
    public void onSpeechKitInitializerError(final Initializer initializer, final Error error) {
        mState |= STATE_NOT_READY;
        mState &= ~STATE_SPEECH_KIT_INITIALIZATION;
        Log.e(LOG_TAG, "SpeechKit initialization error: " + error.getString());
        // Freeze all dictation activity until speech kit won't be initialized
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            onHandleIntent(intent);
        }
        return START_STICKY;
    }

    private void onHandleIntent(@NonNull final Intent intent) {
        final String action = intent.getAction();

        if (ACTION_INITIALIZE_SPEECH_KIT.equals(action)) {
            onHandleInitializeSpeechKitAction();

        } else if (ACTION_CREATE_CONVERSATION.equals(action)) {
            final String phrase = intent.getStringExtra(Intent.EXTRA_TEXT);
            mMessenger.createConversation(phrase);

        } else if (ACTION_CLEAR_EMPTY_CONVERSATIONS.equals(action)) {
            mMessenger.clearEmptyConversations();

        } else if (ACTION_PIN_CONVERSATION.equals(action)) {
            final long threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1);
            mMessenger.pinConversation(threadId);

        } else if (ACTION_UNPIN_CONVERSATION.equals(action)) {
            final long threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1);
            mMessenger.unpinConversation(threadId);

        } else if (ACTION_DELETE_CONVERSATION.equals(action)) {
            final long threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1);
            mMessenger.deleteConversation(threadId);

        } else if (ACTION_CONVERSATION_OPENED.equals(action)) {
            final long threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1);
            mMessenger.onConversationOpened(threadId);

        } else if (ACTION_SEND_MESSAGE.equals(action)) {
            final long threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1);
            final String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            onHandleSendMessageAction(threadId, text);

        } else if (ACTION_PIN_MESSAGE.equals(action)) {
            final long messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1);
            mMessenger.pinMessage(messageId);

        } else if (ACTION_UNPIN_MESSAGE.equals(action)) {
            final long messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1);
            mMessenger.unpinMessage(messageId);

        } else if (ACTION_DELETE_MESSAGE.equals(action)) {
            final long messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1);
            mMessenger.deleteMessage(messageId);

        } else if (ACTION_START_RECOGNITION.equals(action)) {
            final long threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1);
            onHandleStartRecognition(threadId);

        } else if (ACTION_STOP_RECOGNITION.equals(action)) {
            onHandleStopRecognition();

        } else if (ACTION_TOGGLE_VOCALIZATION.equals(action)) {
            final long messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1);
            final String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            onHandleToggleVocalizationAction(messageId, text);

        } else if (ACTION_STOP_VOCALIZATION.equals(action)) {
            onHandleStopVocalizationAction();

        }
    }

    private void onHandleInitializeSpeechKitAction() {
        // This approach is used because sticky intents have been deprecated in Lollipop
        if (hasState(STATE_SPEECH_KIT_INITIALIZED)) {
            SpeechKitInitializedBroadcastReceiver.onSpeechKitInitialized(this);
        }
    }

    private void onHandleSendMessageAction(final long threadId, final String text) {
        if (hasState(STATE_RECOGNITION)) {
            mRecognitionClient.reject(true);
        }
        mMessenger.sendMessage(threadId, Member.DEVICE_OWNER, text);
    }

    private void onHandleStartRecognition(final long threadId) {
        stopVocalization();
        if (!hasState(STATE_RECOGNITION)) {
            mState |= STATE_RECOGNITION;
            mState &= ~STATE_FREE;

            final String locale = Preferences.getInstance().getRecognitionLanguage(this);
            mRecognitionClient = new RecognitionClient(this, locale, threadId);
            mRecognitionClient.setRecognitionListener(this);
            mRecognitionClient.start(false);
        }
    }

    @Override
    public void onPhraseRecognized(final long threadId, @NonNull final String phrase) {
        mMessenger.sendMessage(threadId, Member.VISAVIS, phrase);
        PhraseBroadcastReceiver.onPhraseRecognized(this, threadId, phrase);
    }

    @Override
    public void onRecognitionError() {
        onHandleStopRecognition();
    }

    private void onHandleStopRecognition() {
        if (hasState(STATE_RECOGNITION) || hasState(STATE_RECOGNITION_PAUSED)) {
            mState |= STATE_FREE;
            mState &= ~STATE_RECOGNITION;
            mState &= ~STATE_RECOGNITION_PAUSED;

            mRecognitionClient.stop(false);
            mRecognitionClient = null;
        }
    }

    private void onHandleToggleVocalizationAction(final long messageId, @NonNull final String text) {
        final long prevMessageId = (mSpeaker != null ? mSpeaker.getMessageId() : -1);
        final boolean startNext = (prevMessageId != messageId);

        stopVocalization();

        if (startNext && hasState(STATE_RECOGNITION)) {
            mRecognitionClient.reject(false);

            mState |= STATE_RECOGNITION_PAUSED;
            mState &= ~STATE_RECOGNITION;
            mRecognitionClient.stop(true);
            Log.d(LOG_TAG, "Pause recognition");
        }

        if (startNext) {
            final Voice voice = (Preferences.getInstance().hasMaleVoice() ? Voice.ERMIL : Voice.OMAZH);

            final Phrase phrase = mPhrasesDao.findPhraseByText(text);
            final String sample = (phrase != null ? phrase.getSample() : null);
            final String locale = (phrase != null ? phrase.getLocale() :
                    Preferences.getInstance().getRecognitionLanguage(this));

            mSpeaker = new SpeakerBuilder(this)
                    .message(messageId)
                    .text(text).sample(sample)
                    .locale(locale)
                    .voice(voice).listener(this).build();
            mSpeaker.speak();
            Log.d(LOG_TAG, "Speak");
        } else {
            Log.d(LOG_TAG, "Toggle vocalization: stop");
        }
    }

    @Override
    public void onSpeakerStarted(final long messageId) {
        Log.d(LOG_TAG, "Speaker started: " + messageId);
        SpeakerBroadcastReceiver.onStartPlaying(this, messageId);
    }

    @Override
    public void onSpeakerFinished(final long messageId) {
        Log.d(LOG_TAG, "Speaker finished: " + messageId);
        SpeakerBroadcastReceiver.onStopPlaying(this, messageId);

        mSpeaker = null;
        resumeRecognition();
    }

    private void onHandleStopVocalizationAction() {
        Log.d(LOG_TAG, "Stop vocalization");
        stopVocalization();
        resumeRecognition();
    }

    private void resumeRecognition() {
        if (hasState(STATE_RECOGNITION_PAUSED)) {
            mState |= STATE_RECOGNITION;
            mState &= ~STATE_RECOGNITION_PAUSED;

            mRecognitionClient.start(true);
            Log.d(LOG_TAG, "Resume recognition");
        }
    }

    private void stopVocalization() {
        if (mSpeaker != null) {
            mSpeaker.cancel();
            mSpeaker = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        onHandleStopRecognition();
        onHandleStopVocalizationAction();
        super.onDestroy();
    }

}