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
package ru.yandex.subtitles.content.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import ru.yandex.subtitles.content.dao.Identify;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class Phrase implements Identify<Long>, Parcelable {

    public final static byte CATEGORY_STARTING_PHRASE = 0;
    public final static byte CATEGORY_QUICK_RESPONSE = 1;

    public final static byte TYPE_STARTING_PHRASE = 1;
    public final static byte TYPE_QUICK_RESPONSE = 2;

    public final static int PRESET_PREDEFINED = 1;
    public final static int PRESET_USER_DEFINED = 0;

    private Long mId = null;
    private String mText;
    private long mCategoryId;
    private int mType;
    private int mPreset = 0;
    private String mLocale;
    private String mSample;
    private Long mPrevPhrase;
    private Long mNextPhrase;

    public Phrase() {
    }

    /* package */ Phrase(final Parcel src) {
        mId = (Long) src.readValue(Long.class.getClassLoader());
        mText = (String) src.readValue(String.class.getClassLoader());
        mCategoryId = src.readLong();
        mType = src.readInt();
        mPreset = src.readInt();
        mLocale = (String) src.readValue(String.class.getClassLoader());
        mSample = (String) src.readValue(String.class.getClassLoader());
        mPrevPhrase = (Long) src.readValue(Long.class.getClassLoader());
        mNextPhrase = (Long) src.readValue(Long.class.getClassLoader());
    }

    @Override
    public Long getId() {
        return mId;
    }

    public void setId(final Long id) {
        mId = id;
    }

    public String getText() {
        return mText;
    }

    public void setText(final String text) {
        mText = text;
    }

    public long getCategoryId() {
        return mCategoryId;
    }

    public void setCategoryId(final long categoryId) {
        mCategoryId = categoryId;
    }

    public int getType() {
        return mType;
    }

    public void setType(final int type) {
        mType = type;
    }

    public int getPreset() {
        return mPreset;
    }

    public void setPreset(final int preset) {
        mPreset = preset;
    }

    public String getLocale() {
        return mLocale;
    }

    public void setLocale(final String locale) {
        mLocale = locale;
    }

    @Nullable
    public String getSample() {
        return mSample;
    }

    public void setSample(@Nullable final String sample) {
        mSample = sample;
    }

    @Nullable
    public Long getPrevPhrase() {
        return mPrevPhrase;
    }

    public void setPrevPhrase(@Nullable final Long prevPhrase) {
        mPrevPhrase = prevPhrase;
    }

    @Nullable
    public Long getNextPhrase() {
        return mNextPhrase;
    }

    public void setNextPhrase(@Nullable final Long nextPhrase) {
        mNextPhrase = nextPhrase;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;

        } else if (o == null || getClass() != o.getClass()) {
            return false;

        } else {
            final Phrase phrase = (Phrase) o;
            return (mType == phrase.mType && mId.equals(phrase.mId) &&
                    TextUtilsExt.equals(mLocale, phrase.mLocale));
        }
    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + mType;
        result = 31 * result + mLocale.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeValue(mId);
        dest.writeValue(mText);
        dest.writeLong(mCategoryId);
        dest.writeInt(mType);
        dest.writeInt(mPreset);
        dest.writeValue(mLocale);
        dest.writeValue(mSample);
        dest.writeValue(mPrevPhrase);
        dest.writeValue(mNextPhrase);
    }

    public static final Parcelable.Creator<Phrase> CREATOR = new Parcelable.Creator<Phrase>() {

        @Override
        public Phrase createFromParcel(final Parcel src) {
            return new Phrase(src);
        }

        @Override
        public Phrase[] newArray(final int size) {
            return new Phrase[size];
        }

    };

}
