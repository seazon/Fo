package com.seazon.fo.root;

import java.io.File;
import java.io.FilenameFilter;

import android.os.Environment;
import android.text.TextUtils;

public class Utils {
	public static String getNewPathName(String path, int i) {
	    String ext = getExtFromFilename(path);
	    return !TextUtils.isEmpty(ext) ?
				path.substring(0, path.lastIndexOf('.')) + "_" + i + "." + ext
				: path + "_" + i;
	}

	public static String getExtFromFilename(String filename) {
	    int dotPosition = filename.lastIndexOf('.');
	    if (dotPosition != -1) {
	        return filename.substring(dotPosition + 1, filename.length())
	            	.toLowerCase();
	    }
	    return "";
	}
	    
	public static boolean canShowFile(FilenameFilter filter, boolean showHidden, String absolutePath, boolean isHidden) {
	    String name = getNameFromFilepath(absolutePath);
	    	
	    return (filter == null || (filter != null && filter.accept(new File(getParentFromFilepath(absolutePath)), name)))
	    		&& ((!isHidden && name.charAt(0) != '.') || showHidden)
	    		&& !absolutePath.equals("/mnt/sdcard/.android_secure");
	}
	
	public static String makePath(String path, String name) {
	    if (path.endsWith(File.separator))
	        return path + name;
	
	    return path + File.separator + name;
	}
	
	public static String getNameFromFilepath(String path) {
	    int pos = path.lastIndexOf('/');
	    if (pos != -1)
	        return path.substring(pos + 1);
	    return "";
	}
	
	public static String getParentFromFilepath(String path) {
	    int pos = path.lastIndexOf('/');
	    if (pos > 0)
	        return path.substring(0, pos);
	    return "/";
	}
	
	public static String getSdDirectory() {
	    return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
}