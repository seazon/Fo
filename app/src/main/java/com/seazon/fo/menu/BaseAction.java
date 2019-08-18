package com.seazon.fo.menu;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;

public abstract class BaseAction {

	protected int id;
	protected int type;
	protected int name;
	protected int icon;
	protected RefreshListener listener;
	protected FoSlideActivity activity;
	protected Context context;
	protected Core core;
	protected ImageView imageView;
//	protected TextView textView;
	
	public BaseAction(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		this.id = id;
		this.type = type;
		this.listener = listener;
		this.activity = activity;
		this.context = activity;
		this.core = (Core) activity.getApplication();
		this.name = getNameForInit();
		this.icon = getIconForInit();
	}
	
	abstract protected int getIconForInit();

	abstract protected int getNameForInit();

	abstract public void onActive();
	
	public void onLongClick() {
		Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
	}

	public int getType() {
		return type;
	}
	
	public int getId() {
		return id;
	}

	public int getName() {
		return name;
	}

	public int getIcon() {
		return icon;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}
//	public void setTextView(TextView textView) {
//		this.textView = textView;
//	}
//	public TextView getTextView() {
//		return this.textView;
//	}
	
	public void update() {
		imageView.setImageResource(getIconForInit());
	}
	
}
