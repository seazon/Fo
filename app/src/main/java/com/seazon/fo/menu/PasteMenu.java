package com.seazon.fo.menu;

import java.io.File;

import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.R;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.listener.RefreshListener;

public class PasteMenu extends BaseAction {

	public PasteMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		final Clipper clipper = core.getClipper();
		String currentPath = core.getCurrentPath();
		if(currentPath == null) {
			if (clipper.getCopytype() == Clipper.COPYTYPE_COPY) {
				Toast.makeText(context, R.string.copy_failed, Toast.LENGTH_SHORT).show();
			} else if (clipper.getCopytype() == Clipper.COPYTYPE_CUT) {
				Toast.makeText(context, R.string.move_failed, Toast.LENGTH_SHORT).show();
			}
			return;
		}
		File file = new File(currentPath);
		if (clipper.getCopytype() == Clipper.COPYTYPE_COPY) {
			activity.onOperationStart(Core.OPERATION_TYPE_COPY, file, clipper);
		} else if (clipper.getCopytype() == Clipper.COPYTYPE_CUT) {
			activity.onOperationStart(Core.OPERATION_TYPE_MOVE, file, clipper);
		}
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_paste;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_paste;
	}

}
