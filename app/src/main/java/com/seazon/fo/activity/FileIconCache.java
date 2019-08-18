package com.seazon.fo.activity;

import java.lang.ref.SoftReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

public class FileIconCache {

	private static SparseArray<Bitmap> defaultIconCache = new SparseArray<Bitmap>();
	private static SparseArray<SoftReference<Bitmap>> iconCache = new SparseArray<SoftReference<Bitmap>>();
	private static SparseBooleanArray showThumbCache = new SparseBooleanArray();

	public static Bitmap getDefaultIcon(int resId, Resources r) {
		Bitmap a = defaultIconCache.get(resId);
		if(a==null){
			Bitmap b = BitmapFactory.decodeResource(r, resId);
			defaultIconCache.put(resId, b);
			return b;
		}
		return a;
	}
	
	public static Bitmap getIcon(int hashcode) {
		SoftReference<Bitmap> a = iconCache.get(hashcode);
		if(a==null)
			return null;
		return a.get();
	}

	public static void putIcon(int hashcode, Bitmap bitmap) {
		iconCache.put(hashcode, new SoftReference<Bitmap>(bitmap));
	}
	
	public static boolean showThumb(int hashcode) {
		return showThumbCache.get(hashcode);
	}
	
	public static void setShowThumb(int hashcode, boolean showThumb) {
		showThumbCache.put(hashcode, showThumb);
	}
	
	public static void clear() {
		iconCache.clear();
		showThumbCache.clear();
	}
}
