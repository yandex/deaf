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
package ru.yandex.subtitles.ui.fragment.phrases;

import ru.yandex.subtitles.content.data.Phrase;

public class PhraseItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_PHRASE = 1;
    public static final int TYPE_FOOTER = 2;

    private final int mType;
    private final String mTitle;
    private final Phrase mPhrase;

    public PhraseItem(final int type, final String title, final Phrase phrase) {
        mType = type;
        mTitle = title;
        mPhrase = phrase;
    }

    public int getType() {
        return mType;
    }

    public String getTitle() {
        return mTitle;
    }

    public Phrase getPhrase() {
        return mPhrase;
    }

    @Override
    public int hashCode() {
        switch (mType) {
            case TYPE_HEADER:
                return mTitle.hashCode();

            case TYPE_PHRASE:
                return mPhrase.hashCode();

            default:
                return super.hashCode();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;

        } else if (o == null || getClass() != o.getClass()) {
            return false;

        } else {
            final PhraseItem that = (PhraseItem) o;

            switch (mType) {
                case TYPE_HEADER:
                    return mTitle.equals(that.mTitle);

                case TYPE_PHRASE:
                    return mPhrase.equals(that.mPhrase);

                default:
                    return (mType == that.mType);
            }
        }
    }

}
