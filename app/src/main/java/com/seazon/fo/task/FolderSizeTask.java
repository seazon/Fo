package com.seazon.fo.task;

import java.io.File;

import android.os.AsyncTask;

public class FolderSizeTask extends AsyncTask<String, Long, Object> {

	private long size = 0;
	private FolderSizeCallback callback;

	private boolean stop;

	public FolderSizeTask(FolderSizeCallback callback) {
		this.callback = callback;
	}

	protected void onCancelled() {
		this.stop = true;
	}

	protected Object doInBackground(String... params) {
		String path = params[0];
		calc(path);
		return null;
	}

	protected void onPostExecute(Object result) {
		callback.onSearchFinish(this.size);
	}

	protected void onProgressUpdate(Long... values) {
		callback.onSearchUpdate(values[0]);
	}

	private void calc(String path) {
		if (this.stop) {
			return;
		}

        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory() && file.canRead()) {
                File[] fileArray = file.listFiles();
                if (fileArray != null) {
                    for (File f : fileArray) {
                        if (f.isDirectory()) {
                            calc(f.getPath());
                        } else {
                            this.size += f.length();
                        }
                    }
                }
                this.publishProgress(this.size);
            }
        }
	}
}
