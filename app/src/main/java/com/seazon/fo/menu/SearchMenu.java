package com.seazon.fo.menu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.EditText;

import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.activity.SearchActivity;
import com.seazon.fo.listener.RefreshListener;

public class SearchMenu extends BaseAction {

	public SearchMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		final String currentPath = core.getCurrentPath();
		if(currentPath == null) {
			return;
		}
		
		AlertDialog.Builder abc = new AlertDialog.Builder(context);
		abc.setTitle(R.string.common_search);
		final EditText input = new EditText(context);
		input.setSingleLine();
		abc.setView(input);
		abc.setPositiveButton(R.string.common_search, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				String query = input.getText().toString();
				if(Helper.isBlank(query))
					return;
				
				Intent intent = new Intent();
				intent.putExtra("Query", query);
				intent.putExtra("Path", currentPath);
				intent.setClass(context, SearchActivity.class);
				activity.startActivityForResult(intent, 0);
			}
		}).setNegativeButton(android.R.string.cancel, null);
		AlertDialog dd = abc.create();
		dd.setCanceledOnTouchOutside(true);
		dd.show();
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_search;
	}

	@Override
	protected int getNameForInit() {
		return R.string.common_search;
	}

}
