package com.seazon.fo.menu;

import java.io.File;

import android.widget.Toast;

import com.seazon.fo.R;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;

public class HomeMenu extends BaseAction {

	public HomeMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		File home = new File(core.getMainPreferences().getHome());
		if(!home.exists()) {
			Toast.makeText(context, R.string.operator_file_no_exist, Toast.LENGTH_SHORT).show();
			return;
		}
		activity.render(home.getPath());
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_home;
	}

	@Override
	protected int getNameForInit() {
		return R.string.side_path_local_home;
	}

}
