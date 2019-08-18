package com.seazon.fo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class SupportUtils {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void executeTask(AsyncTask task, Object... params) {

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else  {
			task.execute(params);
		}
		
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void hideSystemUi(Activity activity) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void showSystemUi(Activity activity) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		}
	}
	
	public static void setBackground(View view, Drawable drawable) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackground(drawable);
		} else {
			view.setBackgroundDrawable(drawable);
		}
	}
	
}
