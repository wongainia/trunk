package cn.emoney.acg.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleyHelper {

	private static VolleyHelper mInstance;
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private static Context mCtx;
	private LruCache<String, Bitmap> mCache = null;

	private VolleyHelper(Context context) {
		mCtx = context;
		mRequestQueue = getRequestQueue();
		mCache = new LruCache<String, Bitmap>(1024 * 1024 * 30);
		mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {

			@Override
			public Bitmap getBitmap(String url) {
				Bitmap tBitmap = mCache.get(url);
				return tBitmap;
			}

			@Override
			public void putBitmap(String url, Bitmap bitmap) {

				mCache.put(url, bitmap);
			}
		});

	}

	public static synchronized VolleyHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new VolleyHelper(context);
		}
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
		}
		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req) {
		getRequestQueue().add(req);
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

}
