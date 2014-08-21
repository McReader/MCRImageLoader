package by.mcreader.imageloader.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class IOUtils {

    public static final String LOG_TAG = IOUtils.class.getSimpleName();

    private IOUtils() {
    }

    public static void closeStream(Closeable stream) {

        if (stream != null) {

            try {

                stream.close();

            } catch (IOException e) {

                Log.e(LOG_TAG, "Could not close stream");

            }
        }
    }

    public static File getFileFromDir(File dir, String name) {

        return new File(dir, Converter.stringToMD5(name));

    }

    public static void clearDir(File dir) {

        if (dir == null) {

            return;

        }

        for (File file : dir.listFiles()) {

            if (!file.delete()) {

                Log.d(LOG_TAG, "failed to delete");

            }
        }
    }
}
