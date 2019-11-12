package com.seazon.fo.menu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.FileUtils;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.activity.FoSlideActivity;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.utils.LogUtils;

public class SendMenu extends MultiFileAction {

	public SendMenu(int id, int type, RefreshListener listener, FoSlideActivity activity) {
		super(id, type, listener, activity);
	}

	@Override
	public void onActive() {
		try {
			Iterator<File> i = core.getClipper().getCopys().iterator();
			while (i.hasNext()) {
				File file = i.next();
				if (file.isDirectory()) {
					Toast.makeText(context, R.string.send_failed_can_not_send_folder, Toast.LENGTH_SHORT).show();
					return;
				}
			}

			if (core.getClipper().getCopys().size() == 1) {
				File file = core.getClipper().getCopys().get(0);
				String type = Helper.getTypeByExtension(file.getName());
				if(type.startsWith("image/") || type.startsWith("audio/") || type.startsWith("video/")) {
					new SingleMediaScanner(context, file);
				} else {
					shareSingleFile(context, file);
				}
			} else {
				new MultiMediaScanner(context, core.getClipper().getCopys());
			}
		} catch (Exception e) {
            LogUtils.error(e);
			Toast.makeText(context, R.string.send_failed, Toast.LENGTH_SHORT).show();
		} finally {
			listener.onRefresh(true, Core.MODE_NORMAL, RefreshType.SELECT_RESET, true);
		}
	}

	public void shareSingleFile(Context context, File file) {
		shareSingleFile(file, FileUtils.getUriForFile(context, file));
	}

	private void shareSingleFile(File file, Uri uri){
		String type = Helper.getTypeByExtension(file.getName());
		
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType(type);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		if(type.startsWith("text/")) {
			intent.putExtra(Intent.EXTRA_TEXT, readText(file, 140));
		}
		activity.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.operator_send_by)));
	}
	
	public class SingleMediaScanner implements MediaScannerConnectionClient {

		private MediaScannerConnection mMs;
		private File mFile;

		public SingleMediaScanner(Context context, File f) {
			mFile = f;
			mMs = new MediaScannerConnection(context, this);
			mMs.connect();
		}

		@Override
		public void onMediaScannerConnected() {
			mMs.scanFile(mFile.getAbsolutePath(), null);
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			File file = new File(path);
			if (uri == null) {
				shareSingleFile(context, file);
			} else {
				shareSingleFile(file, uri);
			}

			mMs.disconnect();
		}
	}
	public class MultiMediaScanner implements MediaScannerConnectionClient {
		
		private MediaScannerConnection mMs;
		private List<File> mFile;
		private int max;
		private int cur;
		private ArrayList<Uri> uris;
		private String typeAll;
		
		public MultiMediaScanner(Context context, List<File> f) {
			mFile = new ArrayList<File>();
			mFile.addAll(f);
			max = f.size();
			cur = 0;
			uris = new ArrayList<Uri>();
			mMs = new MediaScannerConnection(context, this);
			mMs.connect();
		}
		
		@Override
		public void onMediaScannerConnected() {
			for(File f:mFile)
			{
				mMs.scanFile(f.getAbsolutePath(), null);
			}
		}
		
		private void setType(String type){
			if(typeAll == null) {
				typeAll = type;
			}
			if(!typeAll.equals(type)) {
				typeAll = typeAll.substring(0, typeAll.indexOf("/"));
				type = type.substring(0, type.indexOf("/"));
				if (typeAll.equals(type)) {
					typeAll = typeAll+"/*";
				} else {
					typeAll = "*/*";
				}
			}
		}
		
		@Override
		public void onScanCompleted(String path, Uri uri) {
            LogUtils.debug("onScanCompleted, path:" + path + ", uri:" + uri);
			File file = new File(path);
			if(uri==null)
				uri = Uri.fromFile(file);
			String type = null;
//			ArrayList<Uri> uris = new ArrayList<Uri>();
//			for (File file : core.getClipper().getCopys()) {
				type = Helper.getTypeByExtension(file.getName());
				
				if(type.startsWith("image/") || type.startsWith("audio/") || type.startsWith("video/")) {
//					Uri uri = getImageContentUri(context, file);
					uris.add(uri);
				} else {
					uris.add(Uri.fromFile(file));
				}
				
				setType(type);
//			uris.add(uri);
			cur++;
			if(cur==max)
			{
				
				Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				
				
//				}
				intent.setType(typeAll);
				
				mMs.disconnect();
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			activity.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.operator_send_by)));
			}
			
			
//			intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//			String typeAll = null;
//			String type = null;
//			ArrayList<Uri> uris = new ArrayList<Uri>();
//			for (File file : core.getClipper().getCopys()) {
//				type = Helper.getTypeByExtension(file.getName());
//				
////				if(type.startsWith("image/")) {// || type.startsWith("audio/") || type.startsWith("video/")) {
////					Uri uri = getImageContentUri(context, file);
////					uris.add(uri);
////				} else {
//					uris.add(Uri.fromFile(file));
////				}
//				
//				if(typeAll == null) {
//					typeAll = type;
//				}
//				if(!typeAll.equals(type)) {
//					typeAll = typeAll.substring(0, typeAll.indexOf("/"));
//					type = type.substring(0, type.indexOf("/"));
//					if (typeAll.equals(type)) {
//						typeAll = typeAll+"/*";
//					} else {
//						typeAll = "*/*";
//					}
//				}
//			}
//			intent.setType(typeAll);
//			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		}
		
	}

	private String readText(File file, int maxSize)
	{
		FileInputStream fis = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis));
			StringBuilder sb = new StringBuilder();
			String s = null;
			int size = 0;
			while ((s = br.readLine()) != null && (size+=s.length()) <= maxSize) {
				sb.append(s + "\n");
			}
			return sb.toString();
		} catch (Exception e) {
            LogUtils.error(e);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
                    LogUtils.error(e);
				}
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
                    LogUtils.error(e);
				}
		}
		return "";
	}
	
	public static Uri getImageContentUri(Context context, File imageFile) {
//        String filePath = imageFile.getAbsolutePath();
//        Cursor cursor = context.getContentResolver().query(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                new String[] { MediaStore.Images.Media._ID },
//                MediaStore.Images.Media.DATA + "=? ",
//                new String[] { filePath }, null);
//        cursor.moveToFirst();
//        int id = cursor.getInt(cursor
//                .getColumnIndex(MediaStore.MediaColumns._ID));
//        Uri baseUri = Uri.parse("content://media/external/images/media");
//        return Uri.withAppendedPath(baseUri, "" + id);
        
        Uri mUri = Uri.parse("content://media/external/images/media"); 
        Uri mImageUri = null;
         
                     Cursor cursor = context.getContentResolver().query(
                             MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
                             null, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
                     try{
                     cursor.moveToFirst();
         
                     while (!cursor.isAfterLast()) {
                        String data = cursor.getString(cursor
                                .getColumnIndex(MediaStore.MediaColumns.DATA));
                        if (imageFile.getPath().equals(data)) {
                            int ringtoneID = cursor.getInt(cursor
                                    .getColumnIndex(MediaStore.MediaColumns._ID));
                            mImageUri = Uri.withAppendedPath(mUri, ""
                                    + ringtoneID);
                            return mImageUri;
//                            break;
                        }
                        cursor.moveToNext();
                    }
                     }finally{
                    	 if(cursor!=null)
                    	 {
                    		 cursor.close();
                    	 }
                     }
                     return Uri.fromFile(imageFile);
    }
	
	@Override
	protected int getIconForInit() {
		return R.drawable.ic_menu_send;
	}

	@Override
	protected int getNameForInit() {
		return R.string.operator_send_by;
	}

}
