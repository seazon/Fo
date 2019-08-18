package com.seazon.fo.menu;

import java.io.File;

import android.app.ProgressDialog;
import android.net.Uri;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.fo.task.ZipUncompressTask;
import com.seazon.fo.task.ZipUncompressTaskCallback;
import com.seazon.fo.task.ZipViewTask;
import com.seazon.utils.LogUtils;

public class ExtractMenu extends MultiFileAction implements ZipUncompressTaskCallback{

	private ProgressDialog progressDialog;
	private boolean fromIntent;
	
	public ExtractMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		File file = null;
		Uri uri = activity.getIntent().getData();
		if(uri == null){
			file = core.getClipper().getCopys().get(0);
			fromIntent = false;
		}else{
			Uri u = ZipViewTask.getFilePath(activity, uri);
			if(u==null){
				Toast.makeText(context, R.string.zip_unzip_failed, Toast.LENGTH_SHORT).show();
			}
			file = new File(u.getPath());
			fromIntent = true;
		}
		
		progressDialog = ProgressDialog.show(context, null, context.getResources().getString(R.string.zip_unziping), true);
		ZipUncompressTask task = new ZipUncompressTask(file.getPath(), file.getParent(), this);
		task.execute();
	}
	
	@Override
	public void onZipUncompressTaskCallback(boolean result) {
		if(progressDialog != null) {
			try {
				progressDialog.dismiss();
				progressDialog = null;
			} catch (IllegalArgumentException e) {
                LogUtils.error(e);
			}
		}
		
		if(result) {
			Toast.makeText(context, R.string.zip_unzip_successful, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, R.string.zip_unzip_failed, Toast.LENGTH_SHORT).show();
		}
		
		if (fromIntent) {
			activity.finish();
		} else {
			listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.RENDER, true);
		}
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_unzip;
	}

	@Override
	protected int getNameForInit() {
		return R.string.zip_unzip;
	}

}
