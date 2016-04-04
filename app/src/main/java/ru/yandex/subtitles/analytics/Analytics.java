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
package ru.yandex.subtitles.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ru.yandex.subtitles.content.data.Member;
import ru.yandex.subtitles.content.data.Message;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.content.data.Thread;
import ru.yandex.subtitles.utils.DateTimeUtils;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class Analytics {

    private static final String MAIN_START_CONVERSATION_BUTTON_PRESSED = "Start conversation button clicked.";
    private static final String MAIN_STARTING_PHRASE_PRESSED = "Opening phrase clicked.";
    private static final String CHAT_CONVERSATION_TERMINATED = "Conversation was terminated.";
    private static final String QUESTIONNAIRE_COMPLETED = "Questionnaire was completed.";
    private static final String SETTINGS_RECOGNITION_LANGUAGE_SWITCHED = "Recognition language switched";
    private static final String SETTINGS_VOCALIZATION_GENDER_SWITCHED = "Vocalization gender switched";
    private static final String SETTINGS_APP_SHARED = "App shared";
    private static final String SETTINGS_ABOUT_APP = "About app";
    private static final String SETTINGS_ABOUT_SPEECHKIT = "About speechkit";
    private static final String SETTINGS_FEEDBACK = "Feedback";
    private static final String CHAT_MESSAGE_COPIED = "Message copied";
    private static final String CHAT_MESSAGE_PINNED = "Message pinned";
    private static final String CHAT_MESSAGE_DELETED = "Message deleted";
    private static final String CHAT_MESSAGE_VOCALIZED = "Message vocalized";
    private static final String CHAT_MESSAGE_ZOOMED = "Message zoomed";
    private static final String CHAT_TEXT_PASTED = "Text was pasted";
    private static final String ZOOMED_MESSAGE_VOCALIZED = "Zoomed message vocalized";
    private static final String ZOOMED_MESSAGE_QUIT = "Zoomed message quit";
    private static final String ZOOMED_MESSAGE_HINT_CLICK = "Zoomed message hint shown";
    private static final String MAIN_OPENING_PHRASE_INSERTED = "A new opening phrase inserted";
    private static final String MAIN_OPENING_PHRASE_UPDATED = "An opening phrase was updated";
    private static final String MAIN_OPENING_PHRASE_REMOVED = "An opening phrase was deleted";
    private static final String MAIN_OPENING_PHRASE_MOVED = "An opening phrase was moved";
    private static final String HISTORY_CONVERSATION_OPENED = "Saved conversation was opened";
    private static final String HISTORY_CONVERSATION_DELETED = "Saved conversation was deleted";
    private static final String HISTORY_CONVERSATION_PINNED = "Saved conversation was pinned";
    private static final String NAVIGATION_DRAWER_SHOW_SETTINGS_CLICKED = "Settings opened from navigation drawer";
    private static final String NAVIGATION_DRAWER_START_CONVERSATION_CLICKED = "Conversation started from navigation drawer";
    private static final String NAVIGATION_DRAWER_SHOW_HISTORY_CLICKED = "History opened from navigation drawer";
    private static final String QUALITY_FEEDBACK_REPORT = "feedback code.";
    private static final String APPWIDGET_START_CONVERSATION_BUTTON_PRESSED = "Widget. Start conversation button clicked.";
    private static final String APPWIDGET_STARTING_PHRASE_PRESSED = "Widget. Opening phrase clicked.";
    private static final String APPWIDGET_INSTALLED = "Widget installed";
    private static final String APPWIDGET_DELETED = "Widget deleted";
    private static final String REVERSE_ORIENTATION_CLICKED = "Reverse orientation button clicked.";
    private static final String QUICK_RESPONSE_SELECTED = "Quick response selected.";
    private static final String QUICK_RESPONSES_CLICKED = "Quick responses button clicked.";
    private static final String SHOW_MICROPHONE_BAR_MESSAGE = "Show microphone bar message.";
    private static final String ZOOMED_MESSAGES_NAVIGATION = "Zoomed messages navigation.";

    public static final String APPWIDGET_TYPE_LARGE = "Large";
    public static final String APPWIDGET_TYPE_SMALL = "Small";

    public static final String MICROPHONE_BAR_MESSAGE_SPEAK_LOUDER = "Speak louder";
    public static final String MICROPHONE_BAR_MESSAGE_PHRASE_RECOGNIZED = "Phrase recognized";

    public static final String NAVIGATION_PREV_CLICKED = "Previous button click.";
    public static final String NAVIGATION_NEXT_CLICKED = "Next button click.";
    public static final String NAVIGATION_SWIPE_TO_PREV = "Swipe to previous.";
    public static final String NAVIGATION_SWIPE_TO_NEXT = "Swipe to next.";

    private static boolean sInitialized = false;
    private static final List<EventTracker> sEventTrackers = new ArrayList<EventTracker>();

    public static void addEventTracker(@NonNull final EventTracker eventTracker) {
        if (sInitialized) {
            throw new IllegalStateException("You must provide all EventTrackers before initialization");
        }
        sEventTrackers.add(eventTracker);
    }

    public static void initialize(final Context context) {
        sInitialized = true;
        for (final EventTracker tracker : sEventTrackers) {
            tracker.activate(context);
        }
    }

    public static void onResumeActivity(final Activity activity) {
        for (final EventTracker tracker : sEventTrackers) {
            tracker.onResumeActivity(activity);
        }
    }

    public static void onPauseActivity(final Activity activity) {
        for (final EventTracker tracker : sEventTrackers) {
            tracker.onPauseActivity(activity);
        }
    }

    private static void reportEvent(@NonNull final String event) {
        for (final EventTracker tracker : sEventTrackers) {
            tracker.reportEvent(event);
        }
    }

    private static void reportEvent(@NonNull final String event, @NonNull final Map<String, Object> attrs) {
        for (final EventTracker tracker : sEventTrackers) {
            tracker.reportEvent(event, attrs);
        }
    }

    public static void onNavigationMenuSettingsClick() {
        reportEvent(NAVIGATION_DRAWER_SHOW_SETTINGS_CLICKED);
    }

    public static void onNavigationMenuStartConversationClick() {
        reportEvent(NAVIGATION_DRAWER_START_CONVERSATION_CLICKED);
    }

    public static void onNavigationMenuConversationsClick() {
        reportEvent(NAVIGATION_DRAWER_SHOW_HISTORY_CLICKED);
    }

    public static void onRecognitionLanguageChanged() {
        reportEvent(SETTINGS_RECOGNITION_LANGUAGE_SWITCHED);
    }

    public static void onVocalizationVoiceChanged() {
        reportEvent(SETTINGS_VOCALIZATION_GENDER_SWITCHED);
    }

    public static void onAboutAppClick() {
        reportEvent(SETTINGS_ABOUT_APP);
    }

    public static void onAboutSpeechKitClick() {
        reportEvent(SETTINGS_ABOUT_SPEECHKIT);
    }

    public static void onFeedbackClick() {
        reportEvent(SETTINGS_FEEDBACK);
    }

    public static void onShareAppClick() {
        reportEvent(SETTINGS_APP_SHARED);
    }

    public static void sendQualityFeedback(final Context context, @NonNull final Quality quality) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("title", context.getString(quality.getTitle()));
        eventAttributes.put("desk", context.getString(quality.getSubtitle()));
        reportEvent(QUALITY_FEEDBACK_REPORT, eventAttributes);
    }

    public static void onStartConversationClick() {
        reportEvent(MAIN_START_CONVERSATION_BUTTON_PRESSED);
    }

    public static void onPhraseClick(final int position, final Phrase phrase) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Text", phrase.getText());
        eventAttributes.put("Position", position);
        reportEvent(MAIN_STARTING_PHRASE_PRESSED, eventAttributes);
    }

    public static void onPhraseAdded(final String text) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Text", text);
        reportEvent(MAIN_OPENING_PHRASE_INSERTED, eventAttributes);
    }

    public static void onPhraseUpdated(final String text) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Text", text);
        reportEvent(MAIN_OPENING_PHRASE_UPDATED, eventAttributes);
    }

    public static void onPhraseDeleted(final String text) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Text", text);
        reportEvent(MAIN_OPENING_PHRASE_REMOVED, eventAttributes);
    }

    public static void onPhraseMoved(final String text, final long fromPosition, final long toPosition) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Text", text);
        eventAttributes.put("From position", fromPosition);
        eventAttributes.put("To position", toPosition);
        reportEvent(MAIN_OPENING_PHRASE_MOVED, eventAttributes);
    }

    public static void onHistoryConversationOpened(@NonNull final Thread thread) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Last opening (days ago)", DateTimeUtils.getAgeDays(thread.getLastOpeningTime()));
        eventAttributes.put("Times opened before", thread.getOpeningCount());
        eventAttributes.put("Pinned messages count", thread.getPinnedMessageCount());
        reportEvent(HISTORY_CONVERSATION_OPENED, eventAttributes);
    }

    public static void onConversationPinned(@NonNull final Thread thread) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Last opening (days ago)", DateTimeUtils.getAgeDays(thread.getLastOpeningTime()));
        eventAttributes.put("Times opened before", thread.getOpeningCount());
        eventAttributes.put("Pinned messages count", thread.getPinnedMessageCount());
        reportEvent(HISTORY_CONVERSATION_PINNED, eventAttributes);
    }

    public static void onConversationDeleted(@NonNull final Thread thread) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Last opening (days ago)", DateTimeUtils.getAgeDays(thread.getLastOpeningTime()));
        eventAttributes.put("Times opened before", thread.getOpeningCount());
        eventAttributes.put("Pinned messages count", thread.getPinnedMessageCount());
        reportEvent(HISTORY_CONVERSATION_DELETED, eventAttributes);
    }

    public static void onShowZoomedMessageDescription() {
        reportEvent(ZOOMED_MESSAGE_HINT_CLICK);
    }

    public static void onExitFullscreen(final String method, final MessageMetadata messageMetadata) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Method", method);
        eventAttributes.put("Is opening phrase", messageMetadata.isOpeningPhrase() ? "Yes" : "No");
        eventAttributes.put("Was vocalized", messageMetadata.wasVocalized() ? "Yes" : "No");
        eventAttributes.put("Time displayed", TimeUnit.SECONDS.toMinutes(messageMetadata.getDurationInSeconds()));
        eventAttributes.put("Text", messageMetadata.getText());
        reportEvent(ZOOMED_MESSAGE_QUIT, eventAttributes);
    }

    public static void onVocalizeZoomedMessage() {
        reportEvent(ZOOMED_MESSAGE_VOCALIZED);
    }

    public static void onConversationTextPaste() {
        reportEvent(CHAT_TEXT_PASTED);
    }

    public static void onFullscreenMessage() {
        reportEvent(Analytics.CHAT_MESSAGE_ZOOMED);
    }

    public static void onCopyMessage(final Message message) {
        final String sender = (message.getUserId() == Member.VISAVIS ? "Collocutor" : "Device owner");

        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Sender", sender);
        reportEvent(Analytics.CHAT_MESSAGE_COPIED, eventAttributes);
    }

    public static void onPinMessage(final Message message) {
        Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Message", message.getText());
        reportEvent(Analytics.CHAT_MESSAGE_PINNED, eventAttributes);
    }

    public static void onDeleteMessage(final Message message) {
        final String sender = (message.getUserId() == Member.VISAVIS ? "Collocutor" : "Device owner");

        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Sender", sender);
        reportEvent(Analytics.CHAT_MESSAGE_DELETED, eventAttributes);
    }

    public static void onVocalizeMessage() {
        reportEvent(CHAT_MESSAGE_VOCALIZED);
    }

    public static void reportConversationMetadata(final ConversationMetadata metadata) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Duration", TimeUnit.SECONDS.toMinutes(metadata.getDurationInSeconds()));
        eventAttributes.put("Owner's statement count", metadata.getOwnerStatementCount());
        eventAttributes.put("Collocutor's statement count", metadata.getVisavisStatementCount());
        eventAttributes.put("Zoomed messages count", metadata.getZoomedMessageCount());
        eventAttributes.put("Vocalized messages count", metadata.getVocalizedMessageCount());
        eventAttributes.put("Conversations count", metadata.getConversationsCount());
        eventAttributes.put("The first phrase", TextUtilsExt.isEmpty(metadata.getFirstPhrase()) ?
                "No message" : metadata.getFirstPhrase());
        eventAttributes.put("Started from widget", metadata.isFromWidget() ? "Yes" : "No");
        eventAttributes.put("Reverse portrait", Boolean.toString(metadata.isReversePortrait()));

        reportEvent(CHAT_CONVERSATION_TERMINATED, eventAttributes);
    }

    public static void onQuestionnaireCompleted(final Context context, final List<Question> answers) {
        final Resources resources = context.getResources();

        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        for (final Question question : answers) {
            final int answerIndex = question.getAnswer();
            if (answerIndex >= 0) {
                final String title = resources.getString(question.getTitle());
                final String answer = resources.getStringArray(question.getOptions())[answerIndex];
                eventAttributes.put(title, answer);
            }
        }
        reportEvent(QUESTIONNAIRE_COMPLETED, eventAttributes);
    }

    public static void onAppWidgetStartConversationClick() {
        reportEvent(APPWIDGET_START_CONVERSATION_BUTTON_PRESSED);
    }

    public static void onAppWidgetPhraseClick(@NonNull final String phrase) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Text", phrase);
        reportEvent(APPWIDGET_STARTING_PHRASE_PRESSED, eventAttributes);
    }

    public static void onAppWidgetInstalled(final String type) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Type", type);
        reportEvent(Analytics.APPWIDGET_INSTALLED, eventAttributes);
    }

    public static void onAppWidgetDeleted(final String type) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Type", type);
        reportEvent(Analytics.APPWIDGET_DELETED, eventAttributes);
    }

    public static void onReverseOrientationClick() {
        reportEvent(REVERSE_ORIENTATION_CLICKED);
    }

    public static void onQuickResponseSelected(@NonNull final String text) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Text", text);
        reportEvent(QUICK_RESPONSE_SELECTED, eventAttributes);
    }

    public static void onQuickResponsesClick() {
        reportEvent(QUICK_RESPONSES_CLICKED);
    }

    public static void onMicrophoneBarMessage(@NonNull final String message) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Message", message);
        reportEvent(SHOW_MICROPHONE_BAR_MESSAGE, eventAttributes);
    }

    public static void onZoomedMessagesNavigation(@NonNull final String method,
                                                  final long userId) {
        final Map<String, Object> eventAttributes = new HashMap<String, Object>();
        eventAttributes.put("Method", method);
        eventAttributes.put("Messages owner", userId == Member.VISAVIS ? "Collocutor" : "Device owner");
        reportEvent(ZOOMED_MESSAGES_NAVIGATION, eventAttributes);
    }

}
