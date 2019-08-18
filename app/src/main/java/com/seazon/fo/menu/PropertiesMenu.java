package com.seazon.fo.menu;

import java.io.File;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

import com.seazon.fo.Core;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.fo.task.FolderSizeCallback;
import com.seazon.fo.task.FolderSizeTask;

public class PropertiesMenu extends SingleFileAction implements FolderSizeCallback{

	private AlertDialog alert;
	private String pp;
	
	public PropertiesMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		
		File file = core.getClipper().getCopys().get(0);
		final FolderSizeTask folderSizeTask = new FolderSizeTask(this);
		folderSizeTask.execute(file.getPath());

		this.pp = getMeee(file);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(file.getName())
				.setMessage(String.format(pp, "> 0 byte"))
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								dialog.cancel();
							}
						}).setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						folderSizeTask.cancel(false);
					}
				});
		alert = builder.create();
		alert.setCanceledOnTouchOutside(true);
		alert.show();
		
		listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
	}
	
	public void onSearchFinish(Long size) {
		alert.setMessage(String.format(pp, Helper.setFileSize(size)));
	}

	public void onSearchUpdate(Long size) {
		alert.setMessage(String.format(pp, "> "+Helper.setFileSize(size)));
	}
	
	private String getMeee(File file){
		
		Resources resources = context.getResources();
		
		String message = "- "+resources.getString(R.string.properties_general_path)+": "+file.getParent()+"\n";
		if(file.isDirectory() && file.canRead())
		{
			message+="- "+resources.getString(R.string.properties_general_include)+": "+String.format(resources.getString(R.string.properties_general_include_item), file.listFiles().length)+"\n";
			message+="- "+resources.getString(R.string.properties_general_size)+": %s\n";
		}
		else
		{
			message+="- "+resources.getString(R.string.properties_general_size)+": "+Helper.setFileSize(file.length())+"\n";
		}
		message+="- "+resources.getString(R.string.properties_general_permission)+": "+Helper.rwString(file)+"\n"
		+"- "+resources.getString(R.string.properties_general_last_modified)+": "+Helper.getFormatDateTime(new Date(file.lastModified()), "yyyy-MM-dd HH:mm:ss")+"\n";
		
		String type = Helper.getTypeByExtension(file.getName());
		if (type != null && type.equals("application/vnd.android.package-archive"))
		{
			PackageInfo pi = context.getPackageManager().getPackageArchiveInfo(file.getPath(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null)
			{
				message += "\n- "+resources.getString(R.string.properties_app_package_name)+": " + pi.packageName 
				+ "\n- "+resources.getString(R.string.properties_app_minimum_version_required)+": "+Helper.sdkLevelToString(pi.applicationInfo.targetSdkVersion)+" ("+pi.applicationInfo.targetSdkVersion+")"
				+"\n- "+resources.getString(R.string.properties_app_version_code)+": "  + pi.versionCode 
				+ "\n- "+resources.getString(R.string.properties_app_version_name)+": "  + pi.versionName;
			}
		}
		else if (type != null && type.startsWith("image/"))
		{
			// get ori width and height
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file.getPath(),  opt);
			message += "\n- "+resources.getString(R.string.properties_image_resolution)+": " + opt.outWidth+" x "+opt.outHeight; 
		}
		return message;
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_properties;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_properties;
	}

}
