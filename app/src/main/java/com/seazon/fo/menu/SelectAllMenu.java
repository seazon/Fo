package com.seazon.fo.menu;

import com.seazon.fo.R;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;

public class SelectAllMenu extends BaseAction {

	public SelectAllMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		activity.selectAll();
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_select_all;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_select_all;
	}

}
