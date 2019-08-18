package com.seazon.fo.menu;

import com.seazon.fo.Core;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.listener.RefreshListener;

public class CutMenu extends MultiFileAction {

	public CutMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		core.getClipper().setCopytype(Clipper.COPYTYPE_CUT);
		listener.onRefresh(false, Core.MODE_PASTE, RefreshType.SELECT_RESET, true);
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_cut;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_cut;
	}

}
