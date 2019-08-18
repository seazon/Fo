package com.seazon.fo.menu;

import com.seazon.fo.R;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;

public class ExitMenu extends BaseAction {

	public ExitMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		core.saveCurrentPath(core.getMainPreferences().getHome());
		activity.finish();
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_exit;
	}

	@Override
	protected int getNameForInit() {
		return R.string.side_others_exit;
	}

}
