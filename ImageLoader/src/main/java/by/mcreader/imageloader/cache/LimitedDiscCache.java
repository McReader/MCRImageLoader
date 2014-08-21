package by.mcreader.imageloader.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import by.mcreader.imageloader.utils.Converter;
import by.mcreader.imageloader.utils.IOUtils;

public class LimitedDiscCache extends BaseDiscCache {

    private static final String LOG_TAG = LimitedDiscCache.class.getSimpleName();

    private static final int MIN_NORMAL_CACHE_SIZE = 2 * 1024 * 1024;

    private AtomicInteger mCacheSize;

    private int mCacheLimit;

    private Map<String, Long> LRUList = Collections.synchronizedMap(new HashMap<String, Long>());

    public LimitedDiscCache(File dir, int limit) {

        super(dir);

        mCacheLimit = limit > MIN_NORMAL_CACHE_SIZE ? limit : MIN_NORMAL_CACHE_SIZE;

        mCacheSize = new AtomicInteger();

        initCalculateCacheSize();
    }

    private void initCalculateCacheSize() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                int size = 0;

                File[] cachedFiles = mCacheDir == null ? null : mCacheDir.listFiles();

                if (cachedFiles == null) {

                    return;

                }

                for (File cachedFile : cachedFiles) {

                    if (!cachedFile.isDirectory()) {

                        size += cachedFile.length();

                        LRUList.put(cachedFile.getName(), cachedFile.lastModified());
                    }
                }

                mCacheSize.set(size);
            }
        }).start();
    }

    // update in lrulist
    @Override
    public Bitmap get(String name) {

        String key = Converter.stringToMD5(name);

        // TODO key
        File file = IOUtils.getFileFromDir(mCacheDir, key);

        if (LRUList.remove(key) != null) {

            LRUList.put(key, System.currentTimeMillis());

        } else {

            file.setLastModified(System.currentTimeMillis());

        }

        Bitmap bitmap = null;

        FileInputStream fis = null;

        try {

            if (file.exists()) {

                fis = new FileInputStream(file);

                bitmap = BitmapFactory.decodeFileDescriptor(fis.getFD());

            }
        } catch (FileNotFoundException e) {

            // Ignored, because not cached yet

        } catch (IOException e) {

            Log.e(LOG_TAG, e.getMessage());

        } finally {

            IOUtils.closeStream(fis);

        }

        return bitmap;
    }

    // add to lrulist
    @Override
    public File put(String name, Bitmap value) {

        String key = Converter.stringToMD5(name);

        File cached = super.put(key, value);

        int fileSize = Math.round(cached.length());

        long currSize = mCacheSize.get();

        while (currSize + fileSize > mCacheLimit) {

            int freed = removeLastUsed();

            currSize -= freed;

            // TODO refactor this situation
            if (freed == 0) {

                break;

            } else {

                mCacheSize.addAndGet(-freed);

            }
        }

        LRUList.put(key, cached.lastModified());

        mCacheSize.addAndGet(fileSize);

        return cached;
    }

    private int removeLastUsed() {

        if (LRUList.size() == 0) {

            return 0;

        }

        long oldest = -1, entryValue = -1;

        String oldestKey = null;

        synchronized (LRUList) {

            for (Entry<String, Long> entry : LRUList.entrySet()) {

                entryValue = entry.getValue();

                if (oldest == -1) {

                    oldest = entryValue;
                    oldestKey = entry.getKey();

                } else if (oldest > entryValue) {

                    oldest = entryValue;
                    oldestKey = entry.getKey();

                }
            }
        }

        if (oldestKey == null) {

            return 0;

        }

        File toRemove = new File(mCacheDir, oldestKey);

        if (!toRemove.exists()) {

            LRUList.remove(oldestKey);

        }

        int removedSize = (int) toRemove.length();

        toRemove.delete();

        LRUList.remove(oldestKey);

        return removedSize;

    }

    public void clear() {
        IOUtils.clearDir(mCacheDir);
    }

}
