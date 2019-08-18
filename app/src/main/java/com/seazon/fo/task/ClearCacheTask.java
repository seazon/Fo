package com.seazon.fo.task;

import java.io.File;

import android.os.AsyncTask;

import com.seazon.fo.Core;
import com.seazon.fo.Helper;
import com.seazon.fo.activity.FileIconCache;

public class ClearCacheTask extends AsyncTask<Object, Object, Object> {
	private ClearCacheTaskCallback listener;

	public ClearCacheTask(ClearCacheTaskCallback listener) {
		this.listener = listener;
	}

	protected Object doInBackground(Object... params) {
		Helper.deleteFile(new File(Core.PATH_FO_THUMB));
		Helper.deleteFile(new File(Core.PATH_FO_THUMB_2X));
		FileIconCache.clear();
		return null;
	}

	protected void onPostExecute(Object resule) {
		listener.callback();
	}
}
