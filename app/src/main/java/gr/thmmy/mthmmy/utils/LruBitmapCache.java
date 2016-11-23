package gr.thmmy.mthmmy.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

class LruBitmapCache extends LruCache<String, Bitmap> implements
        ImageCache {
    private static final int CACHE_SIZE_DIVIDER = 8;

    LruBitmapCache() {
        this(getDefaultLruCacheSize());
    }

    private LruBitmapCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    private static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / CACHE_SIZE_DIVIDER;

        return cacheSize;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}