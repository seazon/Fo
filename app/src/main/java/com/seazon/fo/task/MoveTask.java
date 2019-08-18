package com.seazon.fo.task;

import java.io.File;

import android.os.AsyncTask;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.FileUtils;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.utils.LogUtils;

public class MoveTask extends AsyncTask<Object, String, Exception>
{
	private Core core;
	private String filepath;
	private Clipper clipper;
	private RefreshListener listener;
	private OperationUpdateCallback callback;
	private boolean stop;

	public MoveTask(Core core, String filepath, Clipper clipper, 
			RefreshListener listener, OperationUpdateCallback callback)
	{
		this.core = core;
		this.filepath = filepath;
		this.clipper = clipper;
		this.listener = listener;
		this.callback = callback;
	}

	protected void onCancelled () 
	{
		this.stop = true;
	}
	
	protected Exception doInBackground(Object... params) {
		try {
			for (File t_File : clipper.getCopys()) {
				if (stop) {
					return null;
				}
				File newFile = new File(filepath + Core.PATH_SPLIT
						+ t_File.getName());

				// if (newFile.exists())
				// {
				// // TODO rename
				// Helper.w("file exist:" + newFile.getPath());
				// continue;
				// }

				publishProgress(String.format(
						core.getResources().getString(
								R.string.move_files_detail), newFile.getPath()));

				if(move(t_File, filepath, Helper.whenExist(newFile.getPath()).getName()) == false)
				{
					if(tryCopy(t_File, Helper.whenExist(newFile.getPath()).getPath())==false)
						return new Exception("Move failed");
				}
			}
		} catch (Exception e) {
            LogUtils.error(e);
			return e;
		}
		return null;
	}
	
	protected void onProgressUpdate (String... values) 
	{
		callback.onOperationUpdate(Core.OPERATION_TYPE_MOVE, values[0]);
	}

	private boolean tryCopy(File srcFile, String destPath){
		try {
			FileUtils.copy(srcFile, destPath);
			return FileUtils.deleteFile(srcFile);
		} catch (Exception e) {
            LogUtils.error(e);
			return false;
		}
	}
	protected void onPostExecute(Exception resule)
	{
		callback.onOperationCancel();
		listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.RENDER, true);
		
		if(resule != null)
			Toast.makeText(core, R.string.move_failed, Toast.LENGTH_LONG).show();
		else
			Toast.makeText(core, R.string.move_complete, Toast.LENGTH_SHORT).show();
	}
	
	private boolean move(File srcFile, String destPath, String destFileName) throws Exception  {
		
		if (stop) {
			return true;
		}
		
		//如果destPath为srcFile的子目录或者就是srcFile的路径，则跳过
		if(destPath.equals(srcFile.getPath()) || destPath.startsWith(srcFile.getPath())) {
			throw new Exception();
		}

		// Destination directory
		File dir = new File(destPath);
		// Move file to new directory
		return srcFile.renameTo(new File(dir, destFileName));
	}
}
