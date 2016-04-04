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
package ru.yandex.subtitles.content.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.subtitles.R;
import ru.yandex.subtitles.content.data.Phrase;
import ru.yandex.subtitles.content.provider.BaseColumns;
import ru.yandex.subtitles.content.provider.MessengerContentProvider;
import ru.yandex.subtitles.content.provider.database.Column;
import ru.yandex.subtitles.content.provider.database.Table;
import ru.yandex.subtitles.ui.appwidget.ListViewWidget;

public class PhrasesDAO extends AbstractIdentifyDAO<Long, Phrase> {

    private static final String LOG_TAG = "PhrasesDAO";

    public static final String TABLE = "canned_phrases";

    public interface Columns extends BaseColumns {

        String TEXT = "text";
        String CATEGORY_ID = "category_id";
        String TYPE = "type";
        String PRESET = "preset";
        String LOCALE = "locale";
        String SAMPLE = "sample";
        String PREV_PHRASE = "prev_phrase";
        String NEXT_PHRASE = "next_phrase";

    }

    public static final Uri CONTENT_URI = MessengerContentProvider.createContentUri(TABLE);

    /**
     * Notice that it is not real content uri. It should not be used to load data,
     * only notification on data changes are available.
     */
    public static final Uri STARTING_PHRASES_CONTENT_URI = MessengerContentProvider.createContentUri("starting_phrases");

    public static final String[] PROJECTION = {
            Columns._ID,
            Columns.TEXT,
            Columns.CATEGORY_ID,
            Columns.TYPE,
            Columns.PRESET,
            Columns.LOCALE,
            Columns.SAMPLE,
            Columns.PREV_PHRASE,
            Columns.NEXT_PHRASE
    };

    public static void onCreate(final Context context, final SQLiteDatabase db) {
        final Table.Builder tableBuilder = new Table.Builder(TABLE);
        tableBuilder.addColumn(new Column.Builder().integer(Columns._ID).primaryKey());
        tableBuilder.addColumn(new Column.Builder().text(Columns.TEXT));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.CATEGORY_ID));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.TYPE));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.PRESET).defaultValue(Phrase.PRESET_USER_DEFINED));
        tableBuilder.addColumn(new Column.Builder().text(Columns.LOCALE));
        tableBuilder.addColumn(new Column.Builder().text(Columns.SAMPLE));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.PREV_PHRASE));
        tableBuilder.addColumn(new Column.Builder().integer(Columns.NEXT_PHRASE));
        tableBuilder.create(db);

        Table.createIndex(db, TABLE, Columns._ID, new String[] { Columns._ID });

        insertGeneralStartingPhrases(context, db);
        updateSamples(context, db);
        insertQuickResponses(context, db);
        makeStartingPhrasesLinked(db);
    }

    private static void insertGeneralStartingPhrases(final Context context, final SQLiteDatabase db) {
        final String langRu = context.getString(R.string.locale_ru);
        final String[] itemsRu = context.getResources().getStringArray(R.array.start_phrases_locale_ru);
        insertPhrases(db, Phrase.CATEGORY_STARTING_PHRASE, Phrase.TYPE_STARTING_PHRASE, itemsRu, langRu);
    }

    private static void insertQuickResponses(final Context context, final SQLiteDatabase db) {
        final String langRu = context.getString(R.string.locale_ru);
        final String[] itemsRu = context.getResources().getStringArray(R.array.quick_responses_locale_ru);
        insertPhrases(db, Phrase.CATEGORY_QUICK_RESPONSE, Phrase.TYPE_QUICK_RESPONSE, itemsRu, langRu);
    }

    private static void insertPhrases(final SQLiteDatabase db, final long categoryId,
                                      final int type, @NonNull final String[] items,
                                      @NonNull final String lang) {
        final ContentValues cv = new ContentValues();
        cv.put(Columns.CATEGORY_ID, categoryId);
        cv.put(Columns.PRESET, Phrase.PRESET_PREDEFINED);
        cv.put(Columns.TYPE, type);
        cv.put(Columns.LOCALE, lang);

        for (final String text : items) {
            cv.put(Columns.TEXT, text);
            db.insert(TABLE, null, cv);
        }
    }

    private static void updateSamples(final Context context, final SQLiteDatabase db) {
        final String[] phrasesRu = context.getResources().getStringArray(R.array.start_phrases_locale_ru);
        final String[] samplesRu = context.getResources().getStringArray(R.array.start_phrases_samples_locale_ru);

        final ContentValues cv = new ContentValues();
        for (int i = 0; i < phrasesRu.length; i++) {
            cv.put(Columns.SAMPLE, samplesRu[i]);
            db.update(TABLE, cv, WHERE_TYPE_AND_TEXT,
                    prepareArguments(Phrase.TYPE_STARTING_PHRASE, phrasesRu[i]));
        }
    }

    public static void onUpgrade(final Context context, final SQLiteDatabase db, final int toVersion) {
        ContentValues cv;
        switch (toVersion) {
            case 2:
                new Table.Alter(TABLE)
                        .addColumn(new Column.Builder()
                                .integer(Columns.TYPE)
                                .defaultValue(Phrase.TYPE_STARTING_PHRASE))
                        .execute(db);
                insertQuickResponses(context, db);
                break;

            case 3:
                cv = new ContentValues();
                cv.put(Columns.CATEGORY_ID, Phrase.CATEGORY_QUICK_RESPONSE);
                db.update(TABLE, cv, null, null);
                insertGeneralStartingPhrases(context, db);
                break;

            case 4:
            case 5:
            case 8:
            case 9:
                db.delete(TABLE, null, null);
                insertQuickResponses(context, db);
                insertGeneralStartingPhrases(context, db);
                break;

            case 10: // Release
                break;

            case 11: // Update 1
                new Table.Alter(TABLE)
                        .addColumn(new Column.Builder()
                                .integer(Columns.PRESET)
                                .defaultValue(Phrase.PRESET_USER_DEFINED))
                        .execute(db);

                cv = new ContentValues();
                cv.put(Columns.PRESET, Phrase.PRESET_PREDEFINED);
                db.update(TABLE, cv, null, null);
                break;

            case 12: // Refactoring
                // For some reason it was set up wrong in release build
                // and test data have been pushed to users
                db.delete(TABLE, WHERE_CATEGORY_AND_TYPE,
                        prepareArguments(Phrase.CATEGORY_QUICK_RESPONSE, Phrase.TYPE_STARTING_PHRASE));

                final String langRu = context.getString(R.string.locale_ru);
                new Table.Alter(TABLE)
                        .addColumn(new Column.Builder()
                                .text(Columns.LOCALE))
                        .execute(db);

                cv = new ContentValues();
                cv.put(Columns.LOCALE, langRu);
                db.update(TABLE, cv, null, null);
                break;

            case 13: // 1.1.1 internal
                new Table.Alter(TABLE)
                        .addColumn(new Column.Builder().text(Columns.SAMPLE))
                        .execute(db);
                updateSamples(context, db);
                break;

            case 14: // 1.1.1 release
                new Table.Alter(TABLE)
                        .addColumn(new Column.Builder().integer(Columns.PREV_PHRASE))
                        .addColumn(new Column.Builder().integer(Columns.NEXT_PHRASE))
                        .execute(db);
                makeStartingPhrasesLinked(db);

                // Because user is not able to edit quick responses, we can just delete previously
                // inserted phrases and insert them again with new order
                db.delete(TABLE, WHERE_CATEGORY_AND_TYPE,
                        prepareArguments(Phrase.CATEGORY_QUICK_RESPONSE, Phrase.TYPE_QUICK_RESPONSE));
                insertQuickResponses(context, db);
                break;

        }
    }

    @SuppressWarnings("all")
    private static void makeStartingPhrasesLinked(final SQLiteDatabase db) {
        final Cursor predefined = db.query(TABLE, PROJECTION, WHERE_TYPE_AND_PRESET,
                prepareArguments(Phrase.TYPE_STARTING_PHRASE, Phrase.PRESET_PREDEFINED),
                null, null, SORT_PREDEFINED);
        final Cursor userDefined = db.query(TABLE, PROJECTION, WHERE_TYPE_AND_PRESET,
                prepareArguments(Phrase.TYPE_STARTING_PHRASE, Phrase.PRESET_USER_DEFINED),
                null, null, SORT_USER_DEFINED);

        final Cursor[] cursors;
        if (predefined != null && userDefined != null) {
            cursors = new Cursor[] { userDefined, predefined };
        } else if (predefined != null) {
            cursors = new Cursor[] { predefined };
        } else if (userDefined != null) {
            cursors = new Cursor[] { userDefined };
        } else {
            cursors = new Cursor[] { };
        }

        makePhrasesLinked(db, cursors);
    }

    private static void makePhrasesLinked(final SQLiteDatabase db, final Cursor[] cursors) {
        final List<ContentValues> contentValues = new ArrayList<ContentValues>();

        final MergeCursor mergeCursor = new MergeCursor(cursors);
        final int count = mergeCursor.getCount();
        for (int i = 0; i < count; i++) {
            final Long id = getPhraseId(mergeCursor, i);
            final Long prevPhrase = (i - 1 >= 0 ? getPhraseId(mergeCursor, i - 1) : null);
            final Long nextPhrase = (i + 1 < count ? getPhraseId(mergeCursor, i + 1) : null);

            final ContentValues values = new ContentValues();
            values.put(Columns._ID, id);
            values.put(Columns.PREV_PHRASE, prevPhrase);
            values.put(Columns.NEXT_PHRASE, nextPhrase);
            contentValues.add(values);
        }
        mergeCursor.close();

        for (final ContentValues values : contentValues) {
            if (db.update(TABLE, values, Columns._ID + "=?", prepareArguments(values.get(Columns._ID))) != 1) {
                throw new RuntimeException("Failed to update phrases linked order.");
            }
        }
    }

    @Nullable
    private static Long getPhraseId(@NonNull final Cursor cursor, final int pos) {
        if (cursor.moveToPosition(pos)) {
            return getNullableLong(cursor, Columns._ID);
        } else {
            throw new RuntimeException("Failed to move cursor to position=" + pos +
                    ". Probably it is device's or developer's issue.");
        }
    }

    public static void notifyPhrasesChanged(@NonNull final Context context) {
        // We need it because data really updated only when linked phrases order is calculated.
        // It means that CONTENT_URI can be notified many times.
        context.getContentResolver().notifyChange(STARTING_PHRASES_CONTENT_URI, null);

        // It's a hack, but Google use it in their apps
        ListViewWidget.notifyDataSetChanged(context);
    }

    private static final String WHERE_ID = Columns._ID + "=?";
    private static final String WHERE_CATEGORY_AND_TYPE = Columns.CATEGORY_ID + "=? AND " + Columns.TYPE + "=?";
    private static final String WHERE_CATEGORY_AND_TYPE_AND_PREV_IS_NULL = Columns.CATEGORY_ID + "=? AND " + Columns.TYPE + "=? AND " + Columns.PREV_PHRASE + " IS NULL";
    private static final String WHERE_TYPE_AND_SAMPLE_IS_NULL = Columns.TYPE + "=? AND " + Columns.SAMPLE + " IS NULL";
    private static final String WHERE_TYPE_AND_PRESET = Columns.TYPE + "=? AND " + Columns.PRESET + "=?";
    private static final String WHERE_TYPE_AND_TEXT = Columns.TYPE + "=? AND " + Columns.TEXT + "=?";
    private static final String SORT_PREDEFINED = Columns._ID + " asc";
    private static final String SORT_USER_DEFINED = Columns._ID + " desc";

    public PhrasesDAO(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected Uri getTableUri() {
        return CONTENT_URI;
    }

    @Nullable
    @Override
    protected String[] getProjection() {
        return PROJECTION;
    }

    @NonNull
    @Override
    protected Phrase getItemFromCursor(final Cursor cursor) {
        final Phrase phrase = new Phrase();
        phrase.setId(getLong(cursor, Columns._ID));
        phrase.setText(getString(cursor, Columns.TEXT));
        phrase.setCategoryId(getLong(cursor, Columns.CATEGORY_ID));
        phrase.setType(getInteger(cursor, Columns.TYPE));
        phrase.setPreset(getInteger(cursor, Columns.PRESET));
        phrase.setLocale(getString(cursor, Columns.LOCALE));
        phrase.setSample(getNullableString(cursor, Columns.SAMPLE));
        phrase.setPrevPhrase(getNullableLong(cursor, Columns.PREV_PHRASE));
        phrase.setNextPhrase(getNullableLong(cursor, Columns.NEXT_PHRASE));
        return phrase;
    }

    @NonNull
    @Override
    protected ContentValues toContentValues(@NonNull final Phrase entity) {
        final ContentValues values = new ContentValues();
        values.put(Columns._ID, entity.getId());
        values.put(Columns.TEXT, entity.getText());
        values.put(Columns.CATEGORY_ID, entity.getCategoryId());
        values.put(Columns.TYPE, entity.getType());
        values.put(Columns.PRESET, entity.getPreset());
        values.put(Columns.LOCALE, entity.getLocale());
        values.put(Columns.SAMPLE, entity.getSample());
        values.put(Columns.PREV_PHRASE, entity.getPrevPhrase());
        values.put(Columns.NEXT_PHRASE, entity.getNextPhrase());
        return values;
    }

    @Override
    protected Long parseKey(@NonNull final String key) {
        return Long.parseLong(key);
    }

    @NonNull
    public List<Phrase> getStartingPhrases() {
        final List<Phrase> rawPhrases = get(WHERE_CATEGORY_AND_TYPE,
                prepareArguments(Phrase.CATEGORY_STARTING_PHRASE, Phrase.TYPE_STARTING_PHRASE),
                null);
        return linkPhrases(rawPhrases);
    }

    @NonNull
    private List<Phrase> linkPhrases(final List<Phrase> rawPhrases) {
        Phrase firstPhrase = null;
        final LongSparseArray<Phrase> phrasesMap = new LongSparseArray<Phrase>();
        for (final Phrase phrase : rawPhrases) {
            if (phrase.getPrevPhrase() == null) {
                firstPhrase = phrase;
            }
            phrasesMap.put(phrase.getId(), phrase);
        }

        final List<Phrase> phrases = new ArrayList<Phrase>();
        Long nextPhrase = null;
        if (firstPhrase != null) {
            nextPhrase = firstPhrase.getNextPhrase();
            phrases.add(firstPhrase);
        }

        final int rawPhrasesCount = rawPhrases.size();
        int safeGuard = 0;
        while (nextPhrase != null) {
            final Phrase phrase = phrasesMap.get(nextPhrase);
            nextPhrase = phrase.getNextPhrase();
            phrases.add(phrase);

            if (safeGuard >= rawPhrasesCount) {
                throw new IllegalStateException("Linked order has been corrupted or " +
                        "linked nodes has not been linked yet.");
            }
            safeGuard++;
        }

        return phrases;
    }

    public boolean setPrevPhrase(final long phraseId, @Nullable final Long prevPhraseId) {
        final ContentValues contentValues = new ContentValues(1);
        contentValues.put(Columns.PREV_PHRASE, prevPhraseId);
        return update(getTableUri(), contentValues, WHERE_ID, prepareArguments(phraseId)) == 1;
    }

    public boolean setNextPhrase(final long phraseId, @Nullable final Long nextPhraseId) {
        final ContentValues contentValues = new ContentValues(1);
        contentValues.put(Columns.NEXT_PHRASE, nextPhraseId);
        return update(getTableUri(), contentValues, WHERE_ID, prepareArguments(phraseId)) == 1;
    }

    public boolean setSample(final long phraseId, @Nullable final String sample) {
        final ContentValues contentValues = new ContentValues(1);
        contentValues.put(Columns.SAMPLE, sample);
        return update(getTableUri(), contentValues, WHERE_ID, prepareArguments(phraseId)) == 1;
    }

    @Nullable
    public Phrase getFirstStartingPhrase() {
        final List<Phrase> phrases = get(WHERE_CATEGORY_AND_TYPE_AND_PREV_IS_NULL,
                prepareArguments(Phrase.CATEGORY_STARTING_PHRASE, Phrase.TYPE_STARTING_PHRASE),
                null);
        return (phrases.isEmpty() ? null : phrases.get(0));
    }

    @NonNull
    public List<Phrase> getNonCachedStartingPhrases() {
        return get(WHERE_TYPE_AND_SAMPLE_IS_NULL, prepareArguments(Phrase.TYPE_STARTING_PHRASE), null);
    }

    public boolean isStartingPhrase(@NonNull final String phrase) {
        return getCount(WHERE_TYPE_AND_TEXT, prepareArguments(Phrase.TYPE_STARTING_PHRASE, phrase)) > 0;
    }

    @Nullable
    public Phrase findPhraseByText(@NonNull final String text) {
        final List<Phrase> phrases = get(WHERE_TYPE_AND_TEXT,
                prepareArguments(Phrase.TYPE_STARTING_PHRASE, text), null);
        return (!phrases.isEmpty() ? phrases.get(0) : null);
    }

    @NonNull
    public List<Phrase> getQuickResponses() {
        return get(WHERE_CATEGORY_AND_TYPE,
                prepareArguments(Phrase.CATEGORY_QUICK_RESPONSE, Phrase.TYPE_QUICK_RESPONSE),
                SORT_PREDEFINED);
    }

}
