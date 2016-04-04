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

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private static final String LOG_TAG = "FileUtils";

    public static final int BUFFER_SIZE_1K = 1024;

    @NonNull
    public static String getCacheFilesPath(final Context context, @NonNull final String pathToFiles) {
        File filesDir = null;
        if (isExternalStorageMounted()) {
            filesDir = context.getExternalCacheDir();
        }
        if (filesDir == null) {
            filesDir = context.getCacheDir();
        }

        final String path = (filesDir == null ? "" : filesDir.getPath() + File.separator + pathToFiles);
        final File dir = new File(path);
        return (dir.exists() || dir.mkdirs() ? dir.getAbsolutePath() : "");
    }

    public static boolean isExternalStorageMounted() {
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState());
    }

    public static boolean copyAssetsFolder(final AssetManager assetManager,
                                           final String fromAssetsPath, final String toPath) {
        Log.d(LOG_TAG, "Copy folder from assets: " + fromAssetsPath + " -> " + toPath);

        boolean res = true;
        try {
            final String[] files = assetManager.list(fromAssetsPath);

            final File toFile = new File(toPath);
            if (toFile.exists() || toFile.mkdirs()) {
                for (final String file : files) {
                    final String from = fromAssetsPath + File.separator + file;
                    final String to = toPath + File.separator + file;

                    if (!TextUtilsExt.isEmpty(getExtension(file))) {
                        res &= copyAsset(assetManager, from, to);
                    } else {
                        res &= copyAssetsFolder(assetManager, from, to);
                    }
                }
            } else {
                res = false;
            }
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Failed to copy folder from assets. " + e.getMessage());
            res = false;
        }
        return res;
    }

    private static boolean copyAsset(final AssetManager assetManager,
                                     final String fromAssetsPath, final String toPath) {
        Log.d(LOG_TAG, "Copy file from assets: " + fromAssetsPath + " -> " + toPath);

        InputStream input = null;
        OutputStream output = null;

        boolean res = false;
        try {
            final File toFile = new File(toPath);
            if (!toFile.exists() && toFile.createNewFile()) {
                input = assetManager.open(fromAssetsPath);
                output = new FileOutputStream(toPath);

                // Transfer bytes from the input to the output
                final byte[] buffer = new byte[BUFFER_SIZE_1K];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                res = true;
            }
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Failed to copy folder from assets. " + e.getMessage());
            res = false;
        } finally {
            if (!safeClose(output) || !safeClose(input)) {
                Log.e(LOG_TAG, "Failed to close streams");
                res = false;
            }
        }
        return res;
    }

    public static boolean safeClose(@Nullable final Closeable closeable) {
        if (closeable != null) {
            try {
                if (Flushable.class.isInstance(closeable)) {
                    ((Flushable) closeable).flush();
                }
                closeable.close();
            } catch (final IOException ioe) {
                // Do nothing
                return false;
            }
        }
        return true;
    }

    private static String getExtension(final String filename) {
        String extension = "";
        final int index = indexOfExtension(filename);
        if (index > 0) {
            extension = filename.substring(index + 1);
        }
        return extension;
    }

    private static int indexOfExtension(final String filename) {
        int index = -1;
        if (filename != null) {
            final int extensionPos = filename.lastIndexOf(".");
            final int lastSeparator = indexOfLastSeparator(filename);
            index = (lastSeparator > extensionPos ? -1 : extensionPos);
        }
        return index;
    }

    private static int indexOfLastSeparator(final String filename) {
        int index = -1;
        if (filename != null) {
            index = filename.lastIndexOf(File.separator);
        }
        return index;
    }

    private FileUtils() {
    }

}
