package com.seazon.fo.menu;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.EditText;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.fo.task.ZipCompressTask;
import com.seazon.fo.task.ZipCompressTaskCallback;

public class CompressMenu extends MultiFileAction implements ZipCompressTaskCallback{

	private ProgressDialog progressDialog;
	
	public CompressMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		//"zip"
		final List<File> files = core.getClipper().getCopys();
		final ZipCompressTask task = new ZipCompressTask(this);
		
		String zipFileName = "new_zip_file";
		if(files.size() == 1) {
			zipFileName = files.get(0).getName();
		}
		final String zipFilePath = files.get(0).getParent();
		
		AlertDialog.Builder abc1 = new AlertDialog.Builder(context);
		abc1.setTitle(R.string.zip_zip);
		final EditText input2 = new EditText(context);
		input2.setSingleLine();
		input2.setText(zipFileName+".zip");
		input2.setSelection(0, zipFileName.length());
		abc1.setView(input2);
		abc1.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				progressDialog = ProgressDialog.show(context, null, context.getResources().getString(R.string.zip_ziping), true);
				task.execute(zipFilePath+Core.PATH_SPLIT+input2.getText().toString(), files);
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
				task.cancel(false);
			}
		});
		AlertDialog dd2 = abc1.create();
		dd2.setCanceledOnTouchOutside(true);
		dd2.show();
	}
	
	public void onZipCompressTaskCallback(boolean result) {
		if(progressDialog !=null)
		{
			progressDialog.dismiss();
			progressDialog = null;
		}
		
		listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.RENDER, true);
		if(result)
			Toast.makeText(context, R.string.zip_zip_successful, Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(context, R.string.zip_zip_failed, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_zip;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_zip;
	}

}
