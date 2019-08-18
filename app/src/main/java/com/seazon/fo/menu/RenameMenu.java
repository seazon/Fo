package com.seazon.fo.menu;

import java.io.File;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.EditText;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FileAdapter;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;

public class RenameMenu extends SingleFileAction {

	public RenameMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		// "Rename"
		final File file = core.getClipper().getCopys().get(0);
		AlertDialog.Builder abc = new AlertDialog.Builder(context);
		abc.setTitle(R.string.rename_title);
		final EditText input = new EditText(context);
		input.setSingleLine();
		input.setText(file.getName());
		int index = file.getName().lastIndexOf(".");
		if(index==-1)
			input.selectAll();
		else
			input.setSelection(0, index);
		abc.setView(input);
		abc.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				if (Helper.rename(file, input.getText().toString()) == false)
				{
					Toast.makeText(context, R.string.rename_failed, Toast.LENGTH_SHORT).show();
					
					listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
				}
				else
				{
					long position = core.getClipper().getPositions().get(0);
					File newFile = new File(file.getParent()+Core.PATH_SPLIT+input.getText().toString());
					Map<String, Object> map = activity.listViewDataMapList.get((int)position);
					map.put(FileAdapter.NAME, newFile.getName());
					map.put(FileAdapter.FILE_PATH, newFile.getPath());
					if (!newFile.isDirectory()) {
						map.put(FileAdapter.DESC, Helper.getDes(newFile.length(), newFile.lastModified()));
					} else {
						map.put(FileAdapter.DESC, Helper.getDes(newFile.lastModified()));
					}
					Toast.makeText(context, R.string.rename_successful, Toast.LENGTH_SHORT).show();
					listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
				}
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		})
		.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
			}
		});
		AlertDialog dd = abc.create();
		dd.setCanceledOnTouchOutside(true);
		dd.show();
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_rename;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_rename;
	}
	

}
