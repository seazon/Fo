package com.seazon.fo.task;

import java.io.File;

import android.os.AsyncTask;

import com.seazon.fo.Helper;
import com.seazon.utils.LogUtils;

public class SearchTask extends AsyncTask<String, String, Object> {

	private static final int level_max = 20;
	public static final int result_max = 100;
	
	private SearchCallback callback;
	private boolean stop = false;
	private int cnt;

	public SearchTask(SearchCallback callback) {
		this.callback = callback;
		cnt = 0;
	}

	protected Object doInBackground(String... params) {
		String query = params[0];
		String path = params[1];
		String type = params[2];
		boolean caseSensitive = Boolean.parseBoolean(params[3]);

		doSearch(query, path, type, caseSensitive, 1);

		return null;
	}

	protected void onProgressUpdate(String... values) {
		if(values.length == 1)
			this.callback.onSearchUpdate(values[0]);
		else if(values.length == 2)
		{
			cnt++;
			if(cnt > result_max)
			{
				if(this.isCancelled())
				{
					if(!stop)
						this.callback.onSearchFinish(true);
					
					stop = true;
				}
			}
			else
			{
				this.callback.onSearchBack(values[0], values[1]);
			}
		}
	}
	
	protected void onPostExecute (Object result) 
	{
		callback.onSearchFinish(true);
	}

	private void doSearch(String filter, String path, String type, boolean caseSensitive, int level) {
		if (this.isCancelled()) {
			return;
		}

		this.publishProgress(path);
		
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory() && file.canRead()) {
				
				if(file.getPath().equals("/proc"))
					return;
				
				if(file.getPath().equals("/sys"))
					return;
				
				File[] fileArray = file.listFiles();
				if(fileArray == null)
					return;
				
				for (File f : fileArray) {
					if (this.isCancelled()) {
						return;
					}
					if (f.isDirectory()) {
						if(level < level_max)
							doSearch(filter, f.getPath(), type, caseSensitive, level+1);
					} else {
						if(caseSensitive)
						{
//							if(f.getName().matches(filter)) {
							if (f.getName().indexOf(filter) >= 0) {
								this.publishProgress(f.getName(), f.getPath());
							}
						}
						else
						{
//							if(f.getName().toLowerCase().matches(filter.toLowerCase())) {
							if (f.getName().toLowerCase().indexOf(filter.toLowerCase()) >= 0) {
								this.publishProgress(f.getName(), f.getPath());
							}
						}
						
					}
				}
			} else {
			}
		} else {
            LogUtils.warn("The path had been apointed was not Exist!");
		}
	}
}
