package com.seazon.fo.menu;

public class BaseActionException extends Exception {

	private static final long serialVersionUID = 1L;

	public BaseActionException(String s, Throwable t) {
		super(s, t);
	}

	public BaseActionException(Throwable t) {
		super(t);
	}

	public BaseActionException(String s) {
		super(s);
	}

}
