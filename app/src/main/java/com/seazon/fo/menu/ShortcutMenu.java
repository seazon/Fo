package com.seazon.fo.menu;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.CompressedFileListActivity;
import com.seazon.fo.activity.FileListActivity;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.utils.LogUtils;

public class ShortcutMenu extends MultiFileAction {

	public ShortcutMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		File file = core.getClipper().getCopys().get(0);

		int resourceId = 0;
		if(file.isDirectory()) {
			resourceId = R.drawable.ic_icon_shortcut_folder;
		} else {
			resourceId = R.drawable.ic_icon_shortcut_file;
		}
		
		Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, file.getName());
		shortcutIntent.putExtra("duplicate", false);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getFileIntent(file));
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(context, resourceId));
		context.sendBroadcast(shortcutIntent);
		
		listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
		Toast.makeText(context, String.format(context.getResources().getString(R.string.operator_add_shortcut_successful, file.getName())), Toast.LENGTH_SHORT).show();
	}
	
	private Intent getFileIntent(File file) {
		if(file.isDirectory()) {
			Intent intent = new Intent();
			intent.setClass(context, FileListActivity.class);
			intent.setData(Uri.fromFile(file));
			return intent;
		}
		
		String type = Helper.getTypeByExtension(file.getName());
		if ("application/zip".equals(type)) {
			Intent intent = new Intent();
			intent.setClass(context, CompressedFileListActivity.class);
			intent.putExtra("inner", true);
			intent.setData(Uri.fromFile(file));
			return intent;
		} else if ("*/*".equals(type)) {
			return getUnknowFileIntent(file);
		} else {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), type);
				return intent;
			} catch (Exception e) {
                LogUtils.error(e);
				return getUnknowFileIntent(file);
			}
		}
	}
	
	private Intent getUnknowFileIntent(File file) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "*/*");
		return intent;
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_shortcut;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_add_shortcut;
	}

}
