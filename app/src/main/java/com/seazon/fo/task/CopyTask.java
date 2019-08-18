package com.seazon.fo.task;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.os.AsyncTask;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.utils.LogUtils;

public class CopyTask extends AsyncTask<Object, String, Exception> {
	private Core core;
	private String filepath;
	private Clipper clipper;
	private RefreshListener listener;
	private OperationUpdateCallback callback;
	private boolean stop;

	public CopyTask(Core core, String filepath, Clipper clipper,
			RefreshListener listener, OperationUpdateCallback callback) {
		this.core = core;
		this.filepath = filepath;
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
				File newFile = new File(filepath + Core.PATH_SPLIT + t_File.getName());
//				if (newFile.exists()) {
//					// TODO rename
//					Helper.w("file exist:" + newFile.getPath());
//					continue;
//				}
//				copy(t_File, newFile.getPath());
				
				copy(t_File, Helper.whenExist(newFile.getPath()).getPath());
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
		callback.onOperationUpdate(Core.OPERATION_TYPE_COPY, values[0]);
	}

	protected void onPostExecute(Exception resule) {
		callback.onOperationCancel();
		listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.RENDER, true);
		if (resule != null)
			Toast.makeText(core, R.string.copy_failed, Toast.LENGTH_LONG).show();
		else
			Toast.makeText(core, R.string.copy_complete, Toast.LENGTH_SHORT).show();
	}

	private void copy(File oldfile, String newPath) throws Exception {
		
		if (stop) {
			return;
		}

		publishProgress(String.format(
				core.getResources().getString(R.string.copy_files_detail),
				oldfile.getPath()));

		int byteread = 0;
		if (oldfile.exists()) {
			if (oldfile.isDirectory()) {
				// 如果newPath为oldfile的子目录，则跳过
				if ((newPath+"/").startsWith(oldfile.getPath()+"/")) {
					throw new Exception();
				}
				File newDirectory = new File(newPath);

				if (newDirectory != null) {
					newDirectory.mkdirs();
					for (File t_File : oldfile.listFiles()) {
						copy(t_File,
								newDirectory.getPath() + Core.PATH_SPLIT + t_File.getName());
					}
				}
			} else {
				FileInputStream fis = null;
				BufferedInputStream inBuff = null;
				FileOutputStream fos = null;
				BufferedOutputStream outBuff = null;
				try {
					fis = new FileInputStream(oldfile);
					inBuff = new BufferedInputStream(fis);
					fos = new FileOutputStream(newPath);
					outBuff = new BufferedOutputStream(fos);
					byte[] buffer = new byte[262144];
					while ((byteread = inBuff.read(buffer)) != -1) {
						outBuff.write(buffer, 0, byteread);
					}
					outBuff.flush();
				} finally {
					if (inBuff != null)
						inBuff.close();
					if (outBuff != null)
						outBuff.close();
					if (fis != null)
						fis.close();
					if (fos != null)
						fos.close();
				}
			}
		}
	}
}
