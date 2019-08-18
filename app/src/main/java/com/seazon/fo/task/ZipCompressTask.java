package com.seazon.fo.task;

import java.io.File;
import java.util.List;

import android.os.AsyncTask;

import com.seazon.fo.Helper;
import com.seazon.fo.zip.ZipUtils;
import com.seazon.utils.LogUtils;

public class ZipCompressTask extends AsyncTask<Object, Object, Boolean> {

	private String zipFileName;
	private List<File> inputFiles;
	private ZipCompressTaskCallback callback;

	public ZipCompressTask(ZipCompressTaskCallback callback) {
		this.callback = callback;
	}

	protected Boolean doInBackground(Object... params) {

		this.zipFileName = (String) params[0];
		this.inputFiles = (List<File>) params[1];

		try {
			ZipUtils.compress(inputFiles, zipFileName);
			return true;
		} catch (Exception e) {
            LogUtils.error("compress failed", e);
			return false;
		}

	}

	protected void onPostExecute(Boolean result) {
		callback.onZipCompressTaskCallback(result);
	}

}
