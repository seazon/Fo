package com.seazon.fo.menu;

import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;

public abstract class SingleFileAction extends BaseAction {

	public SingleFileAction(int id, int type, RefreshListener listener,
			FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

}
