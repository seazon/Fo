package com.seazon.fo;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;

import com.seazon.utils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;

public class ImageUtil {

	public static Bitmap getThumb2(final long position, final File file,
			final int size, final Core core) {
		try {
			
			String path = size == 1 ? Core.PATH_FO_THUMB
					: Core.PATH_FO_THUMB_2X;
			String hash = String.valueOf(file.hashCode());
			File thumb = new File(path + Core.PATH_SPLIT + hash
					+ ".tmp");
			
			if (thumb.exists()) {
				return BitmapFactory.decodeFile(thumb.getPath());
			}
			
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					return null;
				}
			}
			thumb.createNewFile();
			
			int length;
			if (size == 2) {
				length = core.du.dip2px(96);
			} else {
				length = core.du.dip2px(48);
			}
			Bitmap bitmap = ImageUtil.zoom(file.getPath(), length, length);
			ImageUtil.saveImage(bitmap, thumb.getPath());
			return bitmap;
			
		} catch (Exception e) {
            LogUtils.error(e);
			return null;
			
		}
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {

		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}

	private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	private static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {

		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
				width, height / 2, matrix, false);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);

		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);
		return bitmapWithReflection;

	}

	private static Bitmap drawShadow(Bitmap original, int radius) {

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShadowLayer((float) (radius * 0.5), (float) (radius * 0.5),
				(float) (radius * 0.5), 0x88000000);

		int PicWidth = original.getWidth();
		int PicHegiht = original.getHeight();

		Bitmap output = Bitmap.createBitmap(original.getWidth(),
				original.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		Rect rect = new Rect(0, 0, PicWidth - radius, PicHegiht - radius);
		Rect rect2 = new Rect(0, 0, PicWidth, PicHegiht);

		RectF rectF = new RectF(rect);
		canvas.drawRoundRect(rectF, 10f, 10f, paint);

		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));

		canvas.drawBitmap(original, rect2, rect, null);
		return output;

	}

	private static Bitmap zoom(Bitmap bitmap, int subwidth, int subheight,
			boolean cut) {

		int srcwidth = bitmap.getWidth();
		int srcheight = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) subwidth / srcwidth);
		float scaleHeight = ((float) subheight / srcheight);
		int cuttop = 0;
		int cutleft = 0;
		if (cut || scaleWidht == scaleHeight) {
			matrix.postScale(scaleWidht, scaleHeight);
		} else if (scaleWidht < scaleHeight) {
			cutleft = (int) ((srcwidth - (subwidth * srcheight / subheight)) / 2);
			matrix.postScale(scaleHeight, scaleHeight);
		} else {
			cuttop = (int) ((srcheight - (subheight * srcwidth / subwidth)) / 2);
			matrix.postScale(scaleWidht, scaleWidht);
		}
		return Bitmap.createBitmap(bitmap, cutleft, cuttop, srcwidth - 2
				* cutleft, srcheight - 2 * cuttop, matrix, true);

	}

	public static Bitmap zoom(String file, int witdh, int height) {

		BitmapFactory.Options opts = new BitmapFactory.Options();

		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, opts);
		if (opts.outWidth < witdh & opts.outHeight < height) {
			opts.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(file, opts);
		}

		opts.inJustDecodeBounds = false;
		opts.inSampleSize = Math.min(opts.outWidth / witdh, opts.outHeight
				/ height);
		Bitmap bitmap = BitmapFactory.decodeFile(file, opts);
		return ImageUtil.zoom(bitmap, witdh, height, false);

	}

	public static void saveImage(Bitmap newbitmap, String file)
			throws Exception {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			newbitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
			out.flush();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
                    LogUtils.error(e);
				}
			}
		}
	}

//	public static void saveImage(String url, String file) throws Exception {
//		FileOutputStream t_FOS = null;
//		BufferedInputStream t_BIS = null;
//
//		try {
//			File dir = new File(file);
//
//			if (dir == null || dir.getParentFile() == null) {
//				throw new Exception("Invalid path:" + file);
//			}
//
//			if (!dir.getParentFile().exists()) {
//				if (!dir.mkdirs()) {
//					throw new Exception("Make dir failed.");
//				}
//			}
//
//			HttpClient client = getHttpClient();
//			HttpGet get = new HttpGet(url);
//			HttpResponse response = client.execute(get);
//			if (response.getStatusLine().getStatusCode() != 200) {
//				throw new Exception("HTTP "
//						+ response.getStatusLine().getStatusCode());
//			}
//
//			byte[] t_Buff = new byte[40960];
//			int t_sReadSize = 0;
//			t_BIS = new BufferedInputStream(response.getEntity().getContent());
//			t_FOS = new FileOutputStream(dir);
//			while ((t_sReadSize = t_BIS.read(t_Buff)) != -1) {
//				t_FOS.write(t_Buff, 0, t_sReadSize);
//			}
//			t_FOS.flush();
//		} finally {
//			if (t_FOS != null) {
//				try {
//					t_FOS.close();
//				} catch (Exception e) {
//                    LogUtils.error(e);
//				}
//			}
//			if (t_BIS != null) {
//				try {
//					t_BIS.close();
//				} catch (Exception e) {
//                    LogUtils.error(e);
//				}
//			}
//		}
//	}
//
//	private static final int SET_CONNECTION_TIMEOUT = 5 * 1000;
//	private static final int SET_SOCKET_TIMEOUT = 20 * 1000;
//
//	public static HttpClient getHttpClient() {
//		HttpClient client = null;
//		try {
//			KeyStore trustStore = KeyStore.getInstance(KeyStore
//					.getDefaultType());
//			trustStore.load(null, null);
//
//			HttpParams params = new BasicHttpParams();
//
//			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//			SchemeRegistry registry = new SchemeRegistry();
//			registry.register(new Scheme("http", PlainSocketFactory
//					.getSocketFactory(), 80));
//			registry.register(new Scheme("https", SSLSocketFactory
//					.getSocketFactory(), 443));
//
//			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
//					params, registry);
//
//			HttpConnectionParams.setConnectionTimeout(params,
//					SET_CONNECTION_TIMEOUT);
//			HttpConnectionParams.setSoTimeout(params, SET_SOCKET_TIMEOUT);
//			client = new DefaultHttpClient(ccm, params);
//		} catch (Exception e) {
//            LogUtils.error(e);
//			client = new DefaultHttpClient();
//		}
//
//		HttpHost proxy = new HttpHost("10.66.1.108", 3128);// TODO
//		client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
//
//		return client;
//	}
}
