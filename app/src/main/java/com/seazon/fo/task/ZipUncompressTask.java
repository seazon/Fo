package com.seazon.fo.task;

import java.io.File;

import android.os.AsyncTask;

import com.seazon.fo.Core;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.zip.UnZipUtils;
import com.seazon.utils.LogUtils;

public class ZipUncompressTask extends AsyncTask<Object, Object, Boolean> {

	private String zipFileName;
	private String outputDirectory;
	private ZipUncompressTaskCallback callback;

	public ZipUncompressTask(String zipFileName, String outputDirectory,
			ZipUncompressTaskCallback callback) {
		this.zipFileName = zipFileName;
		this.outputDirectory = outputDirectory;
		this.callback = callback;
	}

	protected Boolean doInBackground(Object... params) {
		try {

			File d2 = new File(zipFileName);
			int i = d2.getName().lastIndexOf(".");
			String path = null;
			if (i == -1) {
				path = outputDirectory + Core.PATH_SPLIT + d2.getName();
			} else {

				path = outputDirectory + Core.PATH_SPLIT + d2.getName().substring(0, i);
			}

			UnZipUtils.decompress(zipFileName, Helper.whenExist(path).getPath());
			return true;
		} catch (Exception e) {
            LogUtils.error(e);
			return false;
		}
	}
	
	protected void onPostExecute(Boolean result) {
		callback.onZipUncompressTaskCallback(result);
	}

}
