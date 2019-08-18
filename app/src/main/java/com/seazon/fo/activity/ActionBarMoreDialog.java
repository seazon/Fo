package com.seazon.fo.activity;

import java.util.List;

import android.app.Dialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.seazon.fo.Core;
import com.seazon.fo.R;
import com.seazon.fo.menu.BaseAction;
import com.seazon.fo.view.selector.FoSelector;

public class ActionBarMoreDialog extends Dialog 
	implements OnItemClickListener, OnItemLongClickListener {

	private MyAdapter articleAdapter;

	List<BaseAction> menuList;
	int startIndex;
	FoSlideActivity activity;
	Core core;

	public ActionBarMoreDialog(FoSlideActivity activity,
			List<BaseAction> menuList, int startIndex) {
		super(activity);
		this.menuList = menuList;
		this.startIndex = startIndex;
		this.activity = activity;
		this.core = (Core) activity.getApplication();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_actionbar_more);
		setCanceledOnTouchOutside(true);
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
		lp.y = core.du.dip2px(Core.ACTIONBAR_HEIGHT);
		lp.width = core.du.dip2px(Core.ACTIONBAR_MORE_WIDTH);
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		dialogWindow.setAttributes(lp);

		this.articleAdapter = new MyAdapter();
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(articleAdapter);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
	}
	
	private class MyAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			return menuList.size()-startIndex;
		}

		@Override
		public Object getItem(int arg0) {
			return menuList.get(arg0+startIndex);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			if(arg1 == null) {
				arg1 = LayoutInflater.from(activity).inflate(R.layout.dialog_actionbar_more_item, null);
			}
			
			ImageView actionIconView = (ImageView)arg1.findViewById(R.id.actionIconView);
			TextView actionNameView = (TextView)arg1.findViewById(R.id.actionNameView);
			
			BaseAction menu = menuList.get(arg0+startIndex);
			actionIconView.setImageResource(menu.getIcon());
			actionIconView.setBackgroundDrawable(FoSelector.side(activity));
			actionNameView.setText(menu.getName());
			actionNameView.setBackgroundDrawable(FoSelector.side(activity));
			
			return arg1;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		BaseAction menu = (BaseAction) articleAdapter.getItem((int) arg3);
		menu.onActive();
		dismiss();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		BaseAction menu = (BaseAction) articleAdapter.getItem((int) arg3);
		menu.onLongClick();
		return true;
	}
}
