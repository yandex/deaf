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

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class DateTimeUtils {

    public static String getTimezoneCode() {
        return TimeZone.getDefault().getDisplayName(Locale.US);
    }

    /**
     * <p>Checks if a date is today.</p>
     *
     * @param time the time, not altered, not null.
     * @return true if the date is today.
     */
    public static boolean isToday(final long time) {
        return isSameDay(time, System.currentTimeMillis());
    }

    /**
     * <p>Checks if a date is today.</p>
     *
     * @param time the time, not altered, not null.
     * @return true if the date is today.
     */
    public static boolean isYesterday(final long time) {
        return isSameDay(time, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
    }

    /**
     * <p>Checks if two dates are on the same day ignoring time.</p>
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if they represent the same day
     */
    public static boolean isSameDay(final long date1, final long date2) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(date1);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date2);
        return isSameDay(cal1, cal2);
    }

    /**
     * <p>Checks if two calendars represent the same day ignoring time.</p>
     *
     * @param cal1 the first calendar, not altered, not null
     * @param cal2 the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public static boolean isSameDay(final Calendar cal1, final Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    /*
     * Takes timestamp in milliseconds and returns time that has passed since then
     * in days.
     */
    public static long getAgeDays(final long timestamp) {
        final long ageMillis = System.currentTimeMillis() - timestamp;
        return (timestamp != 0 ? TimeUnit.MILLISECONDS.toDays(ageMillis) : 0);
    }

    private DateTimeUtils() {
    }

}
