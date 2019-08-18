package com.seazon.fo.entity;

import java.io.File;

public class FoFile extends File implements Pathable{

	private static final long serialVersionUID = 1L;
	
	private String path;

	public FoFile(String path) {
		super(path);
		this.path = path;
	}
	
	public String getPath()
	{
		return this.path;
	}

}
