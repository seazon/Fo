package com.seazon.fo.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.FileUtils;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.entity.FileComparator;
import com.seazon.fo.menu.AddFavorriteMenu;
import com.seazon.fo.menu.BluetoothMenu;
import com.seazon.fo.menu.CompressMenu;
import com.seazon.fo.menu.CopySearchMenu;
import com.seazon.fo.menu.CutSearchMenu;
import com.seazon.fo.menu.DeleteMenu;
import com.seazon.fo.menu.ExtractMenu;
import com.seazon.fo.menu.PropertiesMenu;
import com.seazon.fo.menu.RenameMenu;
import com.seazon.fo.menu.ReturnModeSelectSearchMenu;
import com.seazon.fo.menu.ReturnSearchMenu;
import com.seazon.fo.menu.SelectAllMenu;
import com.seazon.fo.menu.SendMenu;
import com.seazon.fo.menu.SetHomeMenu;
import com.seazon.fo.menu.ShortcutMenu;
import com.seazon.fo.task.CopyTask;
import com.seazon.fo.task.DeleteTask;
import com.seazon.fo.task.MoveTask;
import com.seazon.fo.task.SearchCallback;
import com.seazon.fo.task.SearchTask;
import com.seazon.fo.view.selector.FoSelector;
import com.seazon.slidelayout.SlideLayout;

public class SearchActivity extends FoSlideActivity implements
		OnItemClickListener, OnItemLongClickListener, OnClickListener,
		SearchCallback {

	private List<File> files = new ArrayList<File>();
	
	private ProgressDialog dialog;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onCreateSetCenterContentView(R.layout.filelist);

		renderActionBar(core.mode);
		
		setNavigationBar(getResources().getString(R.string.search_result_title), 
				new String[]{getResources().getString(R.string.search_result_title)}, 
				Core.PATH_ROOT_STD);
		
		handleIntent(getIntent()) ;
	}
	
	private void handleIntent(Intent intent) {
		files.clear();

		String path = intent.getStringExtra("Path");
		if (Helper.isBlank(path))
			path = Core.PATH_SDCARD;

		String query = intent.getStringExtra("Query");

		final SearchTask searchTask = new SearchTask(this);
		searchTask.execute(query, path, "*", "false");
		this.dialog = ProgressDialog.show(this, null,
				getResources().getString(R.string.search_searching), false,
				true, new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						searchTask.cancel(false);
					}
				});

		render();
	}

	public void render(Object... args)
	{
		TextView c = (TextView)findViewById(android.R.id.empty);
		c.setText(R.string.search_result_no_results);
		
		
		
		listViewDataMapList.clear();
//		thumbMap.clear();
		
		File[] a = (File[])files.toArray(new File[files.size()]);
		
		FileComparator comparatorFile = new FileComparator(core.getMainPreferences().getOrder(), core.getMainPreferences().getOrder2());
		Arrays.sort(a, comparatorFile);
		
		for (File file : a)
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FileAdapter.NAME, file.getName());
			map.put(FileAdapter.FILE_PATH, file.getPath());
			if (core.getClipper().getCopys().contains(file))
			{
				map.put(FileAdapter.SELECT, 1);
			}
			else
			{
				map.put(FileAdapter.SELECT, 0);
			}
//			Bitmap img = null;
			int D = Helper.setFileIcon(file.getName(), isLight());
//			img = ((BitmapDrawable)getResources().getDrawable(D)).getBitmap();
//			map.put("img", file.hashCode());
			map.put(FileAdapter.RES_ID, D);
//			thumbMap.put(file.hashCode(), img);
			map.put(FileAdapter.DESC, Helper.getDes(file.length(), file.lastModified()));
			
			listViewDataMapList.add(map);
		}
		
		AbsListView listView = getListView();
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		
//		listViewAdapter.notifyDataSetChanged();
		
//		appTask = new AppTask(thumbMap, listViewAdapter, getResources());
//		appTask.execute(a);
		
//		if (core.getMainPreferences().isShowThumb())
//		{
//			int size = core.getMainPreferences().getView().equals(Core.VIEW_THUMB)?2:1;
//			thumbTask = new ThumbTask(thumbMap, listViewAdapter, this, size);
//			thumbTask.execute(a);
//		}
		
		renderActionBar(core.mode);
	}
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		HashMap<String, Object> map = (HashMap<String, Object>) listViewDataMapList.get((int) arg3);
		File file = new File(map.get(FileAdapter.FILE_PATH).toString());

		if (core.mode != Core.MODE_SELECT)
		{
			FileUtils.openFile(file, this);
		}
		else
		{
			if (core.getClipper().getCopys().contains(file))
			{
				core.getClipper().getCopys().remove(file);
				core.getClipper().getPositions().remove(arg3);

				map.put(FileAdapter.SELECT, 0);
				((ImageView) arg1.findViewById(R.id.select)).setImageDrawable(null);
			}
			else
			{
				core.getClipper().getCopys().add(file);
				core.getClipper().getPositions().add(arg3);
				
				map.put(FileAdapter.SELECT, 1);
				((ImageView) arg1.findViewById(R.id.select)).setImageDrawable(FoSelector.select(core));
			}
			
			renderActionBar(Core.MODE_SELECT);
		}
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if(core.mode == Core.MODE_SELECT)
			onRefresh(false, Core.MODE_SELECT, RefreshType.NOTIFY, false);
		else
			onRefresh(true, Core.MODE_SELECT, RefreshType.SELECT_RESET, false);
		
		HashMap<String, Object> map = (HashMap<String, Object>) listViewDataMapList.get((int) arg3);
		File file = new File(map.get(FileAdapter.FILE_PATH).toString());
		
		if (core.getClipper().getCopys().contains(file))
		{
			core.getClipper().getCopys().remove(file);
			core.getClipper().getPositions().remove(arg3);

			map.put(FileAdapter.SELECT, 0);
			((ImageView) arg1.findViewById(R.id.select)).setImageDrawable(null);
		}
		else
		{
			core.getClipper().getCopys().add(file);
			core.getClipper().getPositions().add(arg3);

			map.put(FileAdapter.SELECT, 1);
			((ImageView) arg1.findViewById(R.id.select)).setImageDrawable(FoSelector.select(core));
		}
		
		renderActionBar(Core.MODE_SELECT);
		
		return true;
	}
	
	protected void onActivityResult (int requestCode, int resultCode, Intent data) 
	{
    	if(resultCode == FileListActivity.return_code_nothing)
    	{
    		
    	}
    	else if(resultCode == FileListActivity.return_code_local_path)
    	{
    		setResult(resultCode, data);
    		finish();
    	}
    	else if(resultCode == FileListActivity.return_code_favourities)
    	{
    		setResult(resultCode, data);
    		finish();
    	}
    	else if(resultCode == FileListActivity.return_code_exit)
    	{
    		setResult(resultCode, data);
    		finish();
    	}
	}
	
	@Override
	public void initMenu(int mode) {
		switch (mode) {
		case Core.MODE_NORMAL:
//			findViewById(R.id.selectBar).setVisibility(View.GONE);
			
			addMenu(ReturnSearchMenu.class.getName());
			break;
		case Core.MODE_SELECT:
//			findViewById(R.id.selectBar).setVisibility(View.VISIBLE);
			
			int size = core.getClipper().getCopys().size();
//			TextView selectDesView = (TextView)findViewById(R.id.selectDesView);
//			selectDesView.setText(String.format(getResources().getString(R.string.select_description), size));
			updateSelectionNumber(size);
			switch (size) {
			case 0:
				addMenu(SelectAllMenu.class.getName());
				addMenu(ReturnModeSelectSearchMenu.class.getName());
				break;
			case 1:
				addMenu(SelectAllMenu.class.getName());
				addMenu(CopySearchMenu.class.getName());
				addMenu(CutSearchMenu.class.getName());
				addMenu(DeleteMenu.class.getName());
				addMenu(RenameMenu.class.getName());
				if(!core.getClipper().getCopys().get(0).isDirectory()) {
					addMenu(BluetoothMenu.class.getName());
					addMenu(SendMenu.class.getName());
				}
				String type = Helper.getTypeByExtension(core.getClipper().getCopys().get(0).getName());
				addMenu(CompressMenu.class.getName());
				if ("application/zip".equals(type)) {
					addMenu(ExtractMenu.class.getName());
				}
				if(core.getClipper().getCopys().get(0).isDirectory()) {
					addMenu(SetHomeMenu.class.getName());
					addMenu(AddFavorriteMenu.class.getName());
				}
				addMenu(ShortcutMenu.class.getName());
				addMenu(PropertiesMenu.class.getName());
				addMenu(ReturnModeSelectSearchMenu.class.getName());
				break;
			default:
				addMenu(SelectAllMenu.class.getName());
				addMenu(CopySearchMenu.class.getName());
				addMenu(CutSearchMenu.class.getName());
				addMenu(DeleteMenu.class.getName());
				addMenu(BluetoothMenu.class.getName());
				addMenu(SendMenu.class.getName());
				addMenu(CompressMenu.class.getName());
				addMenu(ReturnModeSelectSearchMenu.class.getName());
				break;
			}
			break;
		}
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			switch (slideLayout.currentScreen) {
			case SlideLayout.SCREEN_CENTER:
				switch (core.mode) {
				case Core.MODE_SELECT:
					onRefresh(true, Core.MODE_NORMAL, RefreshType.RENDER, true);
					break;
				default:
					finish();
					break;
				}
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	public void onRefresh(boolean resetClipper, int mode, RefreshType renderLevel, boolean showActionBar) {
		
		if (resetClipper)
		{
			core.getClipper().getCopys().clear();
			core.getClipper().getPositions().clear();
			core.getClipper().setCopytype(Clipper.COPYTYPE_NA);
		}
		
		core.mode = mode;
		if(showActionBar && renderLevel != RefreshType.RENDER)
			renderActionBar(core.mode);
		
		if (renderLevel != RefreshType.NONE)
		{
			if(renderLevel == RefreshType.NOTIFY)
			{
				this.listViewAdapter.notifyDataSetChanged();
			}
			else if(renderLevel == RefreshType.SELECT_RESET)
			{
				for(Map<String, Object> map : listViewDataMapList)
				{
					map.put(FileAdapter.SELECT, 0);
				}
				this.listViewAdapter.notifyDataSetChanged();
			}
			else if(renderLevel == RefreshType.SELECT_ALL)
			{
				for(Map<String, Object> map : listViewDataMapList)
				{
					map.put(FileAdapter.SELECT, 1);
				}
				this.listViewAdapter.notifyDataSetChanged();
			}
			else if(renderLevel == RefreshType.RENDER)
			{
				render();
			}
		}
	}

	public void onOperationCancel() {
		if(dialog!= null)
		{
			dialog.dismiss();
			dialog = null;
		}
	}

	public void onOperationStart(int operationType, File f, Clipper clipper) {
		switch (operationType) {
		case Core.OPERATION_TYPE_COPY:
			
			final CopyTask copyTask = new CopyTask(core, f.getPath(),clipper,this,this);
			copyTask.execute();
			this.dialog = ProgressDialog.show(this, null, getResources().getString(R.string.copy_files), false, true, new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					copyTask.cancel(false);
				}
			});
			
			break;

		case Core.OPERATION_TYPE_MOVE:
			final MoveTask moveTask = new MoveTask(core, f.getPath(),clipper,this,this);
			moveTask.execute();
			this.dialog = ProgressDialog.show(this, null, getResources().getString(R.string.move_files), false, true, new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					moveTask.cancel(false);
				}
			});
			break;
		case Core.OPERATION_TYPE_DELETE:
			final DeleteTask deleteTask = new DeleteTask(core, this, clipper, this,this);
			deleteTask.execute();
			this.dialog = ProgressDialog.show(this, null, getResources().getString(R.string.delete_files), false, true, new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					deleteTask.cancel(false);
				}
			});
			
			break;
		}
	}

	public void selectAll()
	{
		if(core.getClipper().getCopys().size() == 0)
		{
			//select all
			core.getClipper().getCopys().addAll(files);
			for(int i=0;i<files.size();++i)
				core.getClipper().getPositions().add((long)i);
			
			onRefresh(false, Core.MODE_SELECT, RefreshType.SELECT_ALL, true);
		}
		else
		{
			//select none
			core.getClipper().getCopys().clear();
			core.getClipper().getPositions().clear();
			
			onRefresh(true, Core.MODE_SELECT, RefreshType.SELECT_RESET, true);
		}
	}
	
	protected void onNavBarClick(View v, String path) {
		selectAll();
	}
	
	public void onOperationUpdate(int operationType, String message) {
		if(dialog!= null)
		{
			dialog.setMessage(message);
		}
	}

	public void onSearchBack(String name, String path) {
		files.add(new File(path));
	}

	public void onSearchFinish(boolean isFinished) {
		if(dialog!= null)
		{
			dialog.dismiss();
			dialog = null;
		}
		
		render();
		
		if(!isFinished)
		{
			Toast.makeText(this,  String.format(getResources().getString(R.string.search_result_too_many_results), SearchTask.result_max), Toast.LENGTH_LONG).show();
		}
	}

	public void onSearchUpdate(String path) {
		if(dialog!= null)
		{
			dialog.setMessage(getResources().getString(R.string.search_searching)+"\n"+ path);
		}
	}

	public void onClick(View v) {
		onNavBarClick(v, null);
	}

	

}
