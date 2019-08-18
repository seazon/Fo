package com.seazon.fo.task;

public interface SearchCallback {

	public void onSearchUpdate(String path);
	
	public void onSearchBack(String name, String path);
	
	public void onSearchFinish(boolean isFinished);
	
}
