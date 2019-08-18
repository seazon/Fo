package com.seazon.fo.task;

import java.io.File;

import com.seazon.fo.entity.Clipper;

public interface OperationUpdateCallback {

	public void onOperationStart(int operationType, File f, Clipper clipper);
	public void onOperationCancel();
	public void onOperationUpdate(int operationType, String message);
	
}
