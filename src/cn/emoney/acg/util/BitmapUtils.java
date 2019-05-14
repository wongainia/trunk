package cn.emoney.acg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import cn.emoney.acg.ACGApplication;

/**
 * 图片工具类
 * 
 * @author xiechengfa
 * 
 */
public class BitmapUtils {
	// 相册里图片的高度的最大像素
	// private final static int ALBUM_MAX_HEIGHT_PX = 1900;
	public final static String JPG = ".jpg";
	public final static String JPEG = ".jpeg";
	public final static String PNG = ".png";

	// 图片的方向
	public final static int IMAGE_ORIENTATION_VERTICAL = 1;// 坚屏
	public final static int IMAGE_ORIENTATION_HORIZONTAL = 2;// 横屏
	public final static int IMAGE_ORIENTATION_SQUARE = 3;// 正方

	/**
	 * 创建Drawable
	 * 
	 * @param path
	 * @return
	 */
	public static Drawable createDrawable(String path) {
		if (path == null) {
			return null;
		}

		Drawable drawable = null;
		try {
			File file = new File(path);
			if (!file.exists() || file.isDirectory()) {
				return null;
			}

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			opts.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
			opts.inPurgeable = true;
			opts.inInputShareable = true;
			opts.inDither = false;
			FileInputStream is = null;
			Bitmap bitmap = null;
			try {
				is = new FileInputStream(path);
				bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(), null,
						opts);
				if (bitmap == null) {
					// 创建图片失败，源文件可能有误，则删除
					FileUtils.deleteFile(path);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (is != null) {
						is.close();
						is = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (bitmap == null) {
				return null;
			}

			drawable = new BitmapDrawable(ACGApplication.getInstance()
					.getResources(), bitmap);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

		return drawable;
	}

	/**
	 * 创建Drawable
	 * 
	 * @param resId
	 * @return
	 */
	public static Drawable createDrawable(int resId) {
		Drawable drawable = null;
		try {
			drawable = ACGApplication.getInstance().getResources()
					.getDrawable(resId);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return drawable;
	}

	/**
	 * 创建图片
	 * 
	 * @param path
	 * @return
	 */
	public static Bitmap createBitmap(String path) {
		if (path == null) {
			return null;
		}

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 1;
		opts.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		opts.inDither = false;
		FileInputStream is = null;
		Bitmap bitmap = null;
		try {
			is = new FileInputStream(path);
			bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return bitmap;
	}

	/**
	 * 创建图片
	 * 
	 * @param resId
	 *            图片ID
	 * @return 图片
	 * @throws OutOfMeomeryException
	 */
	public static Bitmap createBitmap(int resId) {
		return BitmapFactory.decodeResource(ACGApplication.getInstance()
				.getResources(), resId);
	}

	/**
	 * 创建缩放图片
	 * 
	 * @param path
	 * @param pixOfWidth
	 * @param pixOfHeight
	 * @return
	 */
	public static Bitmap createBitmapOfSampleSize(String path, int pixOfWidth,
			int pixOfHeight) {
		if (path == null) {
			return null;
		}

		if (pixOfWidth * pixOfHeight <= 0) {
			return null;
		}

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;// 设置成了true,不占用内存，只获取bitmap宽高
		BitmapFactory.decodeFile(path, opts);

		if (opts.outWidth * opts.outHeight <= 0) {
			return null;
		}

		opts.inSampleSize = computeSampleSize(opts, -1, pixOfWidth
				* pixOfHeight);
		opts.inJustDecodeBounds = false;// 这里一定要将其设置回false，因为之前我们将其设置成了true
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		opts.inDither = false;
		FileInputStream is = null;
		Bitmap bitmap = null;
		try {
			is = new FileInputStream(path);
			bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return bitmap;
	}

	/**
	 * 读取图像的旋转度
	 * 
	 * @param path
	 * @return
	 */
	public static int readBitmapDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 旋转图片
	 * 
	 * @param angle
	 * @param bitmap
	 * @return
	 */
	public static Bitmap rotaingBitmap(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		return resizedBitmap;
	}

	/**
	 * 删除和保存图片
	 * 
	 * @param path
	 * @param mBitmap
	 */
	public static void deleteAndSaveBitmap(String path, Bitmap mBitmap) {
		if (path == null) {
			return;
		}
		// del
		FileUtils.deleteFile(path);
		// save
		saveBitmapOfPNG(path, mBitmap);
	}

	/**
	 * 保存图片
	 * 
	 * @param bitName
	 *            图片名称
	 * @param mBitmap
	 *            图片
	 */
	public static void saveBitmapOfPNG(String path, Bitmap mBitmap) {
		if (mBitmap == null || mBitmap.isRecycled() || path == null) {
			return;
		}

		FileUtils.createFileDir(path);
		File f = new File(path);
		if (f.exists()) {
			return;
		}

		FileOutputStream fOut = null;
		try {
			f.createNewFile();
			fOut = new FileOutputStream(f);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
			FileUtils.deleteFile(path);
		} finally {
			try {
				if (fOut != null)
					fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @Desp:获取图片宽高
	 * @param @param path
	 * @param @return
	 * @return Point
	 */
	public static Point getImageSize(String path) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;// 设置成了true,不占用内存，只获取bitmap宽高
		BitmapFactory.decodeFile(path, opts);
		return new Point(opts.outWidth, opts.outHeight);
	}

	/**
	 * @Desp:获取图片宽
	 * @param @param path
	 * @param @return
	 * @return Point
	 */
	public static int getImageWidth(String path) {
		return getImageSize(path).x;
	}

	/**
	 * @Desp:获取图片高
	 * @param @param path
	 * @param @return
	 * @return Point
	 */
	public static int getImageHeight(String path) {
		return getImageSize(path).y;
	}

	/**
	 * @Desp:获取图片宽高
	 * @param @param path
	 * @param @return
	 * @return Point
	 */
	public static Point getImageSize(int resId) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;// 设置成了true,不占用内存，只获取bitmap宽高
		BitmapFactory.decodeResource(ACGApplication.getInstance()
				.getResources(), resId, opts);
		return new Point(opts.outWidth, opts.outHeight);
	}

	/**
	 * 获取图片的宽
	 * 
	 * @param resId
	 * @return
	 */
	public static int getImageWidth(int resId) {
		return getImageSize(resId).x;
	}

	/**
	 * 获取图片的高
	 * 
	 * @param resId
	 * @return
	 */
	public static int getImageHeight(int resId) {
		return getImageSize(resId).y;
	}

	// 计算缩放值
	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
}
