package com.seazon.fo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Stack;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.seazon.fo.activity.CompressedFileListActivity;
import com.seazon.fo.entity.Folder;
import com.seazon.utils.LogUtils;

public class FileUtils {

	
	/**
	 * remove last "/" if exists
	 * @param path
	 * @return
	 */
	public static String formatPath(String path) {
		if (!Core.PATH_ROOT_STD.equals(path) && path.endsWith(Core.PATH_SPLIT))
			return path.substring(0, path.length() - 1);

		return path;
	}
	
	public static void pushPathFromParentToChild(Stack<Folder> queue, String fromParentPath, String toChildPath) throws Exception
	{
		fromParentPath = FileUtils.formatPath(fromParentPath);
		toChildPath = FileUtils.formatPath(toChildPath);
		
		// 如果 toChildPath 与 fromParentPath 相同
		if(fromParentPath.equals(toChildPath))
			return;
		
		// 如果 toChildPath 不是 fromParentPath 的子目录，则抛出异常
		if(fromParentPath.startsWith(toChildPath))
			throw new Exception("Path is not a valid subdirectory");
	
		String[] newPath = null;
		if (fromParentPath.equals("/")) {
			newPath = toChildPath.split("/");
		} else {
			newPath = toChildPath.substring(fromParentPath.length()).split("/");
		}
		
		int index = 1;
		while(!fromParentPath.equals(toChildPath))
		{
			if(fromParentPath.equals("/"))
				fromParentPath = fromParentPath+newPath[index];
			else
				fromParentPath = fromParentPath+"/"+newPath[index];
			queue.push(new Folder(fromParentPath, 0, 0));
			index++;
		}
	}
	
	public static void openFile(File file, Activity context) {
		String type = Helper.getTypeByExtension(file.getName());
		if ("application/zip".equals(type)) {
			Intent intent = new Intent();
			intent.setClass(context, CompressedFileListActivity.class);
			intent.putExtra("inner", true);
			intent.setData(Uri.fromFile(file));
			context.startActivityForResult(intent, 0);
		} else if ("*/*".equals(type)) {
			openFileUnknow(file, context);
		} else {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), type);
				context.startActivity(intent);
			} catch (Exception e) {
                LogUtils.error(e);
				openFileUnknow(file, context);
			}
		}
	}

	public static void openFileUnknow(File file, Activity context) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "*/*");
		context.startActivity(Intent.createChooser(intent, context
				.getResources().getString(R.string.operator_open_with)));
	}
	
	public static void copy(File oldfile, String newPath) throws Exception {

		int byteread = 0;
		if (oldfile.exists()) {
			if (oldfile.isDirectory()) {
				// 如果newPath为oldfile的子目录，则跳过
				if ((newPath+"/").startsWith(oldfile.getPath()+"/")) {
					throw new Exception();
				}
				File newDirectory = new File(newPath);

				if (newDirectory != null) {
					newDirectory.mkdirs();
					for (File t_File : oldfile.listFiles()) {
						copy(t_File,
								newDirectory.getPath() + Core.PATH_SPLIT + t_File.getName());
					}
				}
			} else {
				FileInputStream fis = null;
				BufferedInputStream inBuff = null;
				FileOutputStream fos = null;
				BufferedOutputStream outBuff = null;
				try {
					fis = new FileInputStream(oldfile);
					inBuff = new BufferedInputStream(fis);
					fos = new FileOutputStream(newPath);
					outBuff = new BufferedOutputStream(fos);
					byte[] buffer = new byte[262144];
					while ((byteread = inBuff.read(buffer)) != -1) {
						outBuff.write(buffer, 0, byteread);
					}
					outBuff.flush();
				} finally {
					if (inBuff != null)
						inBuff.close();
					if (outBuff != null)
						outBuff.close();
					if (fis != null)
						fis.close();
					if (fos != null)
						fos.close();
				}
			}
		}
	}
	
	public static boolean deleteFile(File file) throws Exception {

		if (file == null)
			return true;

		boolean ret = true;
		if (file.isDirectory() && file.canRead() && file.canWrite()) {
			for (File son : file.listFiles()) {
				ret = deleteFile(son);
				if (ret == false) {
					return false;
				}
			}
		}

		return file.delete();
	}
}
