package com.seazon.fo.menu;

import com.seazon.fo.R;
import com.seazon.fo.activity.FileListActivity;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.listener.RefreshListener;

public class CutSearchMenu extends MultiFileAction {

	public CutSearchMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		
		core.getClipper().setCopytype(Clipper.COPYTYPE_CUT);
		activity.setResult(FileListActivity.return_paste);
		activity.finish();
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
