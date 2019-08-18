package com.seazon.fo.menu;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;

public class NewFolderMenu extends BaseAction {

	public NewFolderMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		String currentPath = core.getCurrentPath();
		if(currentPath == null) {
			Toast.makeText(context, R.string.new_folder_failed, Toast.LENGTH_SHORT).show();
			return;
		}
		newFolder(new File(currentPath));
	}
	
	private void newFolder(final File file)
	{
		int index = 0;
		File f = null;
		do
		{
			if(index==0)
				f = new File(file.getPath()+Core.PATH_SPLIT+context.getResources().getString(R.string.new_folder_default_name));
			else
				f = new File(file.getPath()+Core.PATH_SPLIT+context.getResources().getString(R.string.new_folder_default_name)+"("+index+")");
			index++;
		}
		while(f.exists());
		
		AlertDialog.Builder abc = new AlertDialog.Builder(context);
		abc.setTitle(R.string.new_folder_title);
		final EditText input2 = new EditText(context);
		input2.setSingleLine();
		input2.setText(f.getName());
		input2.selectAll();
		abc.setView(input2);
		abc.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton){
				if(Helper.newFile(file, input2.getText().toString(), true) == false){
					Toast.makeText(context, R.string.new_folder_failed, Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(context, R.string.new_folder_successful, Toast.LENGTH_SHORT).show();
					listener.onRefresh(false, core.mode, RefreshType.RENDER, true);
				}
			}
		})
		.setNegativeButton(android.R.string.cancel, null);
		AlertDialog ad = abc.create();
		ad.setCanceledOnTouchOutside(true);
		ad.show();
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_folder_new;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_new_folder;
	}

}
