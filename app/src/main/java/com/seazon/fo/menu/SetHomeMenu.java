package com.seazon.fo.menu;

import java.io.File;

import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.entity.MainPreferences;
import com.seazon.fo.listener.RefreshListener;

public class SetHomeMenu extends SingleFileAction {

	public SetHomeMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		//"Set Home"
		File file = core.getClipper().getCopys().get(0);
		
		if (file.isDirectory()) {
			MainPreferences mainPreferences = core.getMainPreferences();
			mainPreferences.setHome(file.getPath());
			core.saveMainPreferences(mainPreferences);
			
			activity.onRefreshSide();
			
			Toast.makeText(context, String.format(context.getResources().getString(R.string.hint_set_home_successful), file.getPath()), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, R.string.hint_set_home_failed_1, Toast.LENGTH_SHORT).show();
		}
		listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_set_home;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_set_home;
	}
	

}
