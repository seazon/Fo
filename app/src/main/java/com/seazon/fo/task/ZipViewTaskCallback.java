package com.seazon.fo.task;

import java.util.Map;

import com.seazon.fo.zip.MyZipFile;

public interface ZipViewTaskCallback {

	public void onZipViewTaskCallback(MyZipFile root, Map<String, MyZipFile> map);
	
}
