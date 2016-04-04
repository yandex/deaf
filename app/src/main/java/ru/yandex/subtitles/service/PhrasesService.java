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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import ru.yandex.subtitles.analytics.Analytics;
import ru.yandex.subtitles.content.dao.PhrasesDAO;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.service.cache.SamplesCacheManager;
import ru.yandex.subtitles.utils.IntentUtils;
import ru.yandex.subtitles.utils.LocaleUtils;

/**
 * Class that handles all phrase-related (adding, editing, removing phrase, changing phrase order) events.
 */
public class PhrasesService extends LiveLongAndProsperIntentService {

    private static final String NAME = "PhrasesService";

    private static final String ACTION_ADD_OR_UPDATE_PHRASE = "PhrasesService.ACTION_ADD_OR_UPDATE_PHRASE";
    private static final String ACTION_DELETE_PHRASE = "PhrasesService.ACTION_DELETE_PHRASE";
    private static final String ACTION_MOVE_PHRASE = "PhrasesService.ACTION_MOVE_PHRASE";
    private static final String ACTION_INVALIDATE_SAMPLES = "PhrasesService.ACTION_INVALIDATE_SAMPLES";

    private static final String EXTRA_PHRASE_ID = "phrase_id";
    private static final String EXTRA_FROM_PHRASE_ID = "from_phrase_id";
    private static final String EXTRA_TO_PHRASE_ID = "to_phrase_id";
    private static final String EXTRA_MOVE_TO_TOP = "move_to_top";
    private static final String EXTRA_PHRASE_TEXT = "phrase_text";

    public static void addOrUpdatePhrase(final Context context, @Nullable final Long id,
                                         final String text) {
        final Intent intent = IntentUtils.createActionIntent(context,
                PhrasesService.class, ACTION_ADD_OR_UPDATE_PHRASE);
        intent.putExtra(EXTRA_PHRASE_ID, id);
        intent.putExtra(EXTRA_PHRASE_TEXT, text);
        context.startService(intent);
    }

    public static void deletePhrase(final Context context, @NonNull final Long id) {
        final Intent intent = IntentUtils.createActionIntent(context,
                PhrasesService.class, ACTION_DELETE_PHRASE);
        intent.putExtra(EXTRA_PHRASE_ID, id);
        context.startService(intent);
    }

    public static void movePhrase(final Context context, final long fromPhraseId,
                                  final long toPhraseId, final boolean moveToTop) {
        final Intent intent = IntentUtils.createActionIntent(context,
                PhrasesService.class, ACTION_MOVE_PHRASE);
        intent.putExtra(EXTRA_FROM_PHRASE_ID, fromPhraseId);
        intent.putExtra(EXTRA_TO_PHRASE_ID, toPhraseId);
        intent.putExtra(EXTRA_MOVE_TO_TOP, moveToTop);
        context.startService(intent);
    }

    public static void invalidateSamples(final Context context) {
        final Intent intent = IntentUtils.createActionIntent(context,
                PhrasesService.class, ACTION_INVALIDATE_SAMPLES);
        context.startService(intent);
    }

    public static void prepareSamples(final Context context) {
        SamplesCacheManager.prepareSamples(context);
    }

    private PhrasesDAO mPhrasesDao;
    private SamplesCacheManager mSamplesCacheManager;

    public PhrasesService() {
        super(NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPhrasesDao = new PhrasesDAO(this);
        mSamplesCacheManager = new SamplesCacheManager(this);
    }

    @Override
    protected void onHandleIntent(@NonNull final Intent intent) {
        final String action = intent.getAction();
        if (ACTION_ADD_OR_UPDATE_PHRASE.equals(action)) {
            final Long id = (Long) intent.getSerializableExtra(EXTRA_PHRASE_ID);
            final String text = intent.getStringExtra(EXTRA_PHRASE_TEXT);
            if (id == null) {
                onHandleAddPhraseAction(text);
            } else {
                onHandleUpdatePhraseAction(id, text);
            }

        } else if (ACTION_DELETE_PHRASE.equals(action)) {
            final Long id = (Long) intent.getSerializableExtra(EXTRA_PHRASE_ID);
            onHandleDeletePhraseAction(id);

        } else if (ACTION_MOVE_PHRASE.equals(action)) {
            final long fromPhraseId = intent.getLongExtra(EXTRA_FROM_PHRASE_ID, -1);
            final long toPhraseId = intent.getLongExtra(EXTRA_TO_PHRASE_ID, -1);
            final boolean moveToTop = intent.getBooleanExtra(EXTRA_MOVE_TO_TOP, false);
            onHandleMovePhraseAction(fromPhraseId, toPhraseId, moveToTop);

        } else if (ACTION_INVALIDATE_SAMPLES.equals(action)) {
            mSamplesCacheManager.invalidateSamples();

        }
    }

    private void onHandleAddPhraseAction(@NonNull final String text) {
        final Phrase firstPhrase = mPhrasesDao.getFirstStartingPhrase();

        final Phrase phrase = createEmptyPhrase();
        phrase.setText(text);

        final Uri insertedUri = mPhrasesDao.insert(phrase);
        final Long insertedId = mPhrasesDao.parseId(insertedUri);
        if (firstPhrase != null && insertedId != null) {
            if (closeRemovedPhrasesGap(insertedId, firstPhrase.getId())) {
                Log.i(NAME, "Phrase has been inserted successfully.");

            } else {
                Log.e(NAME, "Linked order has been corrupted. Trying to revert it back.");
                firstPhrase.setPrevPhrase(null);
                mPhrasesDao.update(firstPhrase);
                mPhrasesDao.delete(phrase);
            }
        }

        PhrasesDAO.notifyPhrasesChanged(this);
        Analytics.onPhraseAdded(text);

        mSamplesCacheManager.invalidateSamples();
    }

    @NonNull
    private Phrase createEmptyPhrase() {
        final String locale = LocaleUtils.getLanguage(this);

        final Phrase phrase = new Phrase();
        phrase.setCategoryId(Phrase.CATEGORY_STARTING_PHRASE);
        phrase.setType(Phrase.TYPE_STARTING_PHRASE);
        phrase.setPreset(Phrase.PRESET_USER_DEFINED);
        phrase.setLocale(locale);
        phrase.setSample(null);
        return phrase;
    }

    private void onHandleUpdatePhraseAction(final long id, @NonNull final String text) {
        final Phrase phrase = mPhrasesDao.get(id);
        if (phrase != null) {
            phrase.setText(text);

            final String sample = phrase.getSample();
            phrase.setSample(null);

            if (mPhrasesDao.update(phrase) > 0) {
                PhrasesDAO.notifyPhrasesChanged(this);
                Analytics.onPhraseUpdated(text);

                mSamplesCacheManager.deleteSample(sample, phrase.getLocale());
            }
            mSamplesCacheManager.invalidateSamples();
        }
    }

    private void onHandleDeletePhraseAction(@NonNull final Long id) {
        final Phrase phrase = mPhrasesDao.get(id);
        if (phrase != null && mPhrasesDao.delete(id) > 0) {
            final Long prevPhraseId = phrase.getPrevPhrase();
            final Long nextPhraseId = phrase.getNextPhrase();

            if (!closeRemovedPhrasesGap(prevPhraseId, nextPhraseId)) {
                Log.e(NAME, "Failed to close moved phrases gap on prev=" +
                        prevPhraseId + " and next=" + nextPhraseId);
            }

            Log.i(NAME, "Phrase with id=" + id + " has been deleted.");
            mSamplesCacheManager.deleteSample(phrase.getSample(), phrase.getLocale());

            PhrasesDAO.notifyPhrasesChanged(this);
            Analytics.onPhraseDeleted(phrase.getText());
        }
    }

    private boolean closeRemovedPhrasesGap(@Nullable final Long prevPhraseId, @Nullable final Long nextPhraseId) {
        return setPhraseNext(prevPhraseId, nextPhraseId) && setPhrasePrev(nextPhraseId, prevPhraseId);
    }

    private boolean setPhrasePrev(@Nullable final Long phraseId, @Nullable final Long prevPhraseId) {
        return (phraseId == null || mPhrasesDao.setPrevPhrase(phraseId, prevPhraseId));
    }

    private boolean setPhraseNext(@Nullable final Long phraseId, @Nullable final Long nextPhraseId) {
        return (phraseId == null || mPhrasesDao.setNextPhrase(phraseId, nextPhraseId));
    }

    private void onHandleMovePhraseAction(final long fromPhraseId, final long toPhraseId,
                                          final boolean moveToTop) {
        final Phrase from = mPhrasesDao.get(fromPhraseId);
        final Phrase to = mPhrasesDao.get(toPhraseId);
        if (from != null && to != null) {
            // Remove phrase from
            if (!closeRemovedPhrasesGap(from.getPrevPhrase(), from.getNextPhrase())) {
                throw new IllegalStateException("Failed to remove node 'From' ");
            }

            // Insert phrase to
            if (moveToTop) {
                setPhraseNext(to.getPrevPhrase(), from.getId());
                setPhrasePrev(from.getId(), to.getPrevPhrase());
                setPhraseNext(from.getId(), to.getId());
                setPhrasePrev(to.getId(), from.getId());
            } else {
                setPhraseNext(to.getId(), from.getId());
                setPhrasePrev(from.getId(), to.getId());
                setPhraseNext(from.getId(), to.getNextPhrase());
                setPhrasePrev(to.getNextPhrase(), from.getId());
            }
        }
        PhrasesDAO.notifyPhrasesChanged(this);
    }

}