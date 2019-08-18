package com.seazon.fo.task;

import java.io.File;

import android.os.AsyncTask;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.utils.LogUtils;

public class DeleteTask extends AsyncTask<Object, String, Exception> {
	private Core core;
	private FoSlideActivity activity;
	private Clipper clipper;
	private RefreshListener listener;
	private OperationUpdateCallback callback;
	private boolean stop;

	public DeleteTask(Core core, FoSlideActivity activity, Clipper clipper, RefreshListener listener,
			OperationUpdateCallback callback) {
		this.core = core;
		this.activity = activity;
		this.clipper = clipper;
		this.listener = listener;
		this.callback = callback;
	}

	protected void onCancelled() {
		this.stop = true;
	}

	protected Exception doInBackground(Object... params) {
		try {
			for (File t_File : clipper.getCopys()) {

				if(deleteFile(t_File) == false) {
					return new Exception("Delete failed");
				}
				
				if (stop) {
					return null;
				}
			}
		} catch (Exception e) {
            LogUtils.error(e);
			return e;
		}
		return null;
	}

	protected void onProgressUpdate(String... values) {
		callback.onOperationUpdate(Core.OPERATION_TYPE_DELETE, values[0]);
	}

	protected void onPostExecute(Exception resule) {
		callback.onOperationCancel();
		listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.RENDER, true);
		
		if (resule != null)
			Toast.makeText(core, R.string.delete_failed+":"+resule.getMessage(), Toast.LENGTH_LONG).show();
		else
			Toast.makeText(core, R.string.delete_complete, Toast.LENGTH_SHORT).show();
	}

	private boolean deleteFile(File file) throws Exception {
		if (stop)
			return true;

		if (file == null)
			return true;

		publishProgress(String.format(
				core.getResources().getString(R.string.delete_files_detail),
				file.getPath()));

		boolean ret = true;
		if (file.isDirectory() && file.canRead() && file.canWrite()) {
			for (File son : file.listFiles()) {
				ret = deleteFile(son);
				if (ret == false) {
					return false;
				}
			}
		}

		return file.delete();
	}
}
