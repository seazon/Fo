package com.seazon.fo.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Clipper {

	public final static int COPYTYPE_NA = 0;
	public final static int COPYTYPE_COPY = 1;
	public final static int COPYTYPE_CUT = 2;
	public final static int COPYTYPE_DELETE = 3;
	public final static int COPYTYPE_NEW = 4;
	public final static int COPYTYPE_RENAME = 5;

	private int copytype;
	private List<File> copys;
	private List<Long> positions;

	public Clipper() {
		this.copytype = Clipper.COPYTYPE_NA;
		this.copys = new ArrayList<File>();
		this.positions = new ArrayList<Long>();
	}

	public Integer getCopytype() {
		return copytype;
	}

	public void setCopytype(Integer copytype) {
		this.copytype = copytype;
	}
	
	public List<File> getCopys() {
		return copys;
	}

	public void setCopys(List<File> copys) {
		this.copys = copys;
	}

	public List<Long> getPositions() {
		return positions;
	}

	public void setPositions(List<Long> positions) {
		this.positions = positions;
	}

}
