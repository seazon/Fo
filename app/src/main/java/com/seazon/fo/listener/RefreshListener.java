package com.seazon.fo.listener;

import com.seazon.fo.RefreshType;

public interface RefreshListener
{
	public void onRefresh(boolean resetClipper, int mode, RefreshType renderLevel, boolean showActionBar);
}
