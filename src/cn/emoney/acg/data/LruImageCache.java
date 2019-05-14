package cn.emoney.acg.data;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class LruImageCache implements ImageCache {
    
    private static LruCache<String, Bitmap> memoryCache;
    private static LruImageCache instance;
    
    private LruImageCache() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }
    
    public static LruImageCache getInstance() {
        if (instance == null) {
            synchronized (LruImageCache.class) {
                if (instance == null) {
                    instance = new LruImageCache();
                }
            }
        }
        
        return instance;
    }
    
    @Override
    public Bitmap getBitmap(String key) {
        return memoryCache.get(key);
    }

    @Override
    public void putBitmap(String key, Bitmap value) {
        if (getBitmap(key) == null) {
            memoryCache.put(key, value);
        }
    }

}
