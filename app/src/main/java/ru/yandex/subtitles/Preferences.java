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
package ru.yandex.subtitles;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import ru.yandex.speechkit.Recognizer;
import ru.yandex.subtitles.utils.TextUtilsExt;

public class Preferences {

    private final static String PREF_HAS_MALE_VOICE = "has_male_voice";
    private final static String PREF_FIRST_LAUNCH = "first_launch";
    private final static String PREF_RECOGNITION_LANGUAGE = "recognition_language";
    private final static String PREF_VERSION_CODE = "version_code";
    private final static String PREF_CHANGE_ORIENTATION_HINT = "change_orientation_hint";

    private static Preferences sInstance;

    public static void instantiate(final Context context) {
        sInstance = new Preferences(context);
    }

    public static Preferences getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Preferences are not instantiated yet. " +
                    "Did you call instantiate() before getInstance()?");
        }
        return sInstance;
    }

    private final SharedPreferences mSharedPrefs;

    // TODO: Remove all usages when version 1.0.3 will have no active users
    @Deprecated
    private final SharedPreferences mDialogManagementServicePrefs;

    private Preferences(final Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mDialogManagementServicePrefs = context.getSharedPreferences("DialogManagementService", Context.MODE_PRIVATE);
    }

    public boolean hasMaleVoice() {
        return mSharedPrefs.getBoolean(PREF_HAS_MALE_VOICE, true);
    }

    public void setHasMaleVoice(final boolean isChecked) {
        setBoolean(PREF_HAS_MALE_VOICE, isChecked);
    }

    public boolean isFirstLaunch() {
        return mSharedPrefs.getBoolean(PREF_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(final boolean isFirstLaunch) {
        setBoolean(PREF_FIRST_LAUNCH, isFirstLaunch);
    }

    public boolean hasRussianRecognitionLanguage(final Context context) {
        return TextUtilsExt.equals(Recognizer.Language.RUSSIAN, getRecognitionLanguage(context));
    }

    public String getRecognitionLanguage(final Context context) {
        final String defaultLang = Recognizer.Language.RUSSIAN; // TODO: LocaleUtils.getLanguage(context);
        return mSharedPrefs.getString(PREF_RECOGNITION_LANGUAGE, defaultLang);
    }

    public void setRussianRecognitionLanguage(final boolean russian) {
        setString(PREF_RECOGNITION_LANGUAGE, (russian ? Recognizer.Language.RUSSIAN : Recognizer.Language.UKRAINIAN));
    }

    @NonNull
    public InstallationInfo getInstallationInfo() {
        final int versionCode = mSharedPrefs.getInt(PREF_VERSION_CODE, -1);

        // There was not good solution for handling version changes before version 1.1.0
        // Just because update 1.1.0 is refactoring and provides no new features,
        // we shouldn't show What's New dialog. Version code for previous app version is 4,
        // but actually it wasn't stored in shared preferences and we receive -1 in #versionCode.
        final boolean olderThan103 = (versionCode == -1);
        final boolean showedUpdate4 = mDialogManagementServicePrefs.getBoolean("showed_update_4", false);
        mDialogManagementServicePrefs.edit().putBoolean("showed_update_4", false).apply();

        final boolean haveBeenInstalledOrUpdated = (versionCode < BuildConfig.VERSION_CODE) &&
                !(showedUpdate4 && "1.1.0".equals(BuildConfig.VERSION_NAME));

        setInt(PREF_VERSION_CODE, BuildConfig.VERSION_CODE);
        return new InstallationInfo(olderThan103, haveBeenInstalledOrUpdated);
    }

    public boolean hasChangeOrientationHintShown() {
        return mSharedPrefs.getBoolean(PREF_CHANGE_ORIENTATION_HINT, false);
    }

    public void setChangeOrientationHintShown(final boolean shown) {
        setBoolean(PREF_CHANGE_ORIENTATION_HINT, shown);
    }

    private void setBoolean(final String key, final boolean value) {
        mSharedPrefs.edit().putBoolean(key, value).apply();
    }

    private void setString(final String key, final String value) {
        mSharedPrefs.edit().putString(key, value).apply();
    }

    private void setInt(final String key, final int value) {
        mSharedPrefs.edit().putInt(key, value).apply();
    }

    private void setLong(final String key, final long value) {
        mSharedPrefs.edit().putLong(key, value).apply();
    }

    public static class InstallationInfo implements Parcelable {

        private boolean mWasOlderThan103;
        private boolean mHaveBeenInstalledOrUpdated;

        /* package */ InstallationInfo(final boolean wasOlderThan103,
                                       final boolean haveBeenInstalledOrUpdated) {
            mWasOlderThan103 = wasOlderThan103;
            mHaveBeenInstalledOrUpdated = haveBeenInstalledOrUpdated;
        }

        /* package */ InstallationInfo(final Parcel src) {
            mWasOlderThan103 = (src.readInt() == 1);
            mHaveBeenInstalledOrUpdated = (src.readInt() == 1);
        }

        public boolean wasOlderThan103() {
            return mWasOlderThan103;
        }

        public boolean haveBeenInstalledOrUpdated() {
            return mHaveBeenInstalledOrUpdated;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeInt(mWasOlderThan103 ? 1 : 0);
            dest.writeInt(mHaveBeenInstalledOrUpdated ? 1 : 0);
        }

        public static final Creator<InstallationInfo> CREATOR = new Creator<InstallationInfo>() {

            @Override
            public InstallationInfo createFromParcel(final Parcel source) {
                return new InstallationInfo(source);
            }

            @Override
            public InstallationInfo[] newArray(final int size) {
                return new InstallationInfo[size];
            }

        };

    }

}
