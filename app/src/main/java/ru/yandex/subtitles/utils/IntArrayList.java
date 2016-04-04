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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * IntArrayList holds integers to Objects. It is intended to be more memory efficient
 * than using an List&lt;Integer> to store Integers objects, because it avoids
 * auto-boxing.
 */
public class IntArrayList implements Parcelable {

    private static final int DEFAULT_CAPACITY = 16;
    private static final int MIN_CAPACITY_INCREMENT = 12;

    private int mSize;
    private int[] mItems;

    public IntArrayList() {
        mSize = 0;
        mItems = new int[DEFAULT_CAPACITY];
    }

    /* package */ IntArrayList(final Parcel in) {
        mSize = in.readInt();
        final int length = in.readInt();
        mItems = new int[length];
        in.readIntArray(mItems);
    }

    public boolean add(final int item) {
        int[] a = mItems;
        final int s = mSize;
        if (s == a.length) {
            int[] newArray = new int[newCapacity(s)];
            System.arraycopy(a, 0, newArray, 0, s);
            mItems = a = newArray;
        }
        a[s] = item;
        mSize = s + 1;
        return true;
    }

    @SuppressWarnings("all")
    public int get(final int index) {
        final int size = mSize;
        if (index >= size) {
            throwIndexOutOfBoundsException(index, size);
        }
        return mItems[index];
    }

    @SuppressWarnings("all")
    public int remove(final int index) {
        final int[] a = mItems;
        int s = mSize;
        if (index >= s) {
            throwIndexOutOfBoundsException(index, s);
        }

        final int result = a[index];
        System.arraycopy(a, index + 1, a, index, --s - index);
        a[s] = 0;  // Prevent memory leak
        mSize = s;
        return result;
    }

    public boolean contains(final int item) {
        final int s = mSize;
        int[] a = mItems;

        boolean contains = false;
        for (int i = 0; i < s; i++) {
            if (a[i] == item) {
                contains = true;
                break;
            }
        }

        return contains;
    }

    public void clear() {
        if (mSize != 0) {
            mSize = 0;
        }
    }

    public int size() {
        return mSize;
    }

    public boolean isEmpty() {
        return (mSize == 0);
    }

    public int indexOf(final int item) {
        final int[] a = mItems;
        final int s = mSize;
        for (int i = 0; i < s; i++) {
            if (item == a[i]) {
                return i;
            }
        }
        return -1;
    }

    public int[] toArray() {
        final int s = mSize;
        final int[] result = new int[s];
        System.arraycopy(mItems, 0, result, 0, s);
        return result;
    }

    @Override
    public int hashCode() {
        final int[] a = mItems;
        int hashCode = 1;
        for (int i = 0, s = mSize; i < s; i++) {
            final int e = a[i];
            hashCode = 31 * hashCode + e;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof IntArrayList)) {
            return false;
        }

        final IntArrayList that = (IntArrayList) o;
        final int s = mSize;
        if (that.size() != s) {
            return false;
        }

        final int[] a = mItems;
        for (int i = 0; i < s; i++) {
            int eThis = a[i];
            int eThat = that.get(i);
            if (eThis != eThat) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(mSize);
        dest.writeInt(mItems.length);
        dest.writeIntArray(mItems);
    }

    private static IndexOutOfBoundsException throwIndexOutOfBoundsException(final int index, final int size) {
        throw new IndexOutOfBoundsException("Invalid index " + index + ", size is " + size);
    }

    private static int newCapacity(final int currentCapacity) {
        final int increment = (currentCapacity < (MIN_CAPACITY_INCREMENT / 2) ? MIN_CAPACITY_INCREMENT : currentCapacity >> 1);
        return currentCapacity + increment;
    }

    public static final Creator<IntArrayList> CREATOR = new Creator<IntArrayList>() {

        @Override
        public IntArrayList createFromParcel(final Parcel in) {
            return new IntArrayList(in);
        }

        @Override
        public IntArrayList[] newArray(final int size) {
            return new IntArrayList[size];
        }

    };

}
