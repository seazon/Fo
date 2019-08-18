package com.seazon.fo.menu;

import com.seazon.fo.R;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;

public class ReturnSearchMenu extends BaseAction {

	public ReturnSearchMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		activity.finish();
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_back;
	}

	@Override
	protected int getNameForInit() {
		return R.string.common_back;
	}

}
