package com.seazon.fo.listener;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.seazon.fo.menu.BaseAction;

public class OnMoreClickListener implements OnClickListener {
	private ArrayList<BaseAction> moreActions;

	public OnMoreClickListener(ArrayList<BaseAction> moreActions) {
		this.moreActions = moreActions;
	}

	public void onClick(DialogInterface dialog, int which) {
		BaseAction baseAction = moreActions.get(which);
		baseAction.onActive();
	}

}
