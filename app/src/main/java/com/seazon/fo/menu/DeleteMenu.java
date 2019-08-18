package com.seazon.fo.menu;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.seazon.fo.Core;
import com.seazon.fo.R;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.listener.RefreshListener;

public class DeleteMenu extends MultiFileAction {

	public DeleteMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		AlertDialog.Builder ab = null;
		final Clipper clipper = core.getClipper();
		if(clipper.getCopys().size()==0)
		{
			
		}
		else
		{
			String message = null;
			if(clipper.getCopys().size()==1)
			{
				final File file = clipper.getCopys().get(0);
				message = String.format(context.getResources().getString(R.string.delete_files_single_confirm), "'"+file.getName()+"'");
			}
			else
			{
				message = String.format(context.getResources().getString(R.string.delete_files_multi_confirm), clipper.getCopys().size());
			}
			
			ab = new AlertDialog.Builder(context);
			ab.setTitle(R.string.common_confirm);
			ab.setMessage(message);
			ab.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					activity.onOperationStart(Core.OPERATION_TYPE_DELETE, null, clipper);
				}
			}).setNegativeButton(android.R.string.cancel, null);
			ab.show();
		}
		
	}

	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_delete;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_delete;
	}

}
