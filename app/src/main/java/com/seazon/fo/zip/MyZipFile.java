package com.seazon.fo.zip;

import java.util.List;


public class MyZipFile {

	public String name;
	public String path;
	public long size;
	public long compressedSize;
	public long time;
	public boolean isDirectory;
	public MyZipFile parent;
	public List<MyZipFile> children;
	
	
}
