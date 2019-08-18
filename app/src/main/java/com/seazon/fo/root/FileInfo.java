package com.seazon.fo.root;

public class FileInfo {
		
	FileInfo(String path, boolean isdir) {
		mPath = path;
		mIsDir = isdir;
		mFileName = Utils.getNameFromFilepath(path);
	}

	String mPath;
	public final String getPath() {
	    return mPath;
	}
	    
	boolean mIsHidden;
	public final boolean isHidden() {
	    return mIsHidden;
	}
	public void setHidden(boolean hidden) {
	    mIsHidden = hidden;
	}

	boolean mCanRead = true;
	boolean mCanWrite = true;
	public boolean canRead() {
	    return mCanRead;
	}
	public boolean canWrite() {
	    return mCanWrite;
	}
	public void setReadWrite(boolean read, boolean write) {
	    mCanRead = read;
	    mCanWrite = write;
	}
	    
	String mFlags = "";
	public final String getFlags() {
	    return mFlags;
	}
	public void setFlags(String flags) {
	    mFlags = flags;
	}
	    
	String mLinkto = "";
	public final String getSymLink() {
	    return mLinkto;
	}
	public void setSymLink(String path) {
	    mLinkto = path;
	}
	    
	String mOwner = "";
	String mGroup = "";
	public final String getOwner() {
	    return mOwner;
	}
	public final String getGroup() {
	    return mGroup;
	}
	public void setOwnerGroup(String owner, String group) {
	    mOwner = owner;
	    mGroup = group;
	}
		
	long mFileSize;
	public final long getSize() {
	    return mFileSize;
	}
	public void setSize(long size) {
	    mFileSize = size;
	}
	    
	String mFileName;
	public final String getName() {
	    return mFileName;
	}
	    
	public final String getParent() {
	    return Utils.getParentFromFilepath(mPath);
	}

	boolean mIsDir;
	public final boolean isDir() {
	    return mIsDir;
	}
	    
	long mModified;
	public void setModified(long millis) {
	    mModified = millis;
	}
	public final long getModified() {
	    return mModified;
	}

	public static FileInfo getFileInfo(String path, boolean isdir) {
	    return new FileInfo(path, isdir);
	}
}