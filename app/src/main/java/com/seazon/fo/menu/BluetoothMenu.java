package com.seazon.fo.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.utils.LogUtils;

public class BluetoothMenu extends MultiFileAction {

	public BluetoothMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		try {
			Iterator<File> i = core.getClipper().getCopys().iterator();
			while (i.hasNext()) {
				File file = i.next();
				if (file.isDirectory()) {
					Toast.makeText(context, R.string.send_failed_can_not_send_folder, Toast.LENGTH_SHORT).show();
					return;
				}
			}

			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("*/*");
			intent.setClassName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
			ArrayList<Uri> uris = new ArrayList<Uri>();
			for (File file : core.getClipper().getCopys()) {
				uris.add(Uri.fromFile(file));
			}
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			activity.startActivity(intent);
		} catch (Exception e) {
            LogUtils.error(e);
			Toast.makeText(context, R.string.hint_send_failed_2, Toast.LENGTH_SHORT).show();
		} finally {
			listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
		}
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_bluetooth;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_send_by_bluetooth;
	}
}
