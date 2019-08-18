package com.seazon.fo;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;

import com.seazon.utils.LogUtils;

public class VideoUtil {

	public static void getThumb(Context context, File file, long position,final int size,
			MediaThumbCallback callback) {
		new SingleMediaScanner(context, file, position, size,callback);
	}

	static class SingleMediaScanner implements MediaScannerConnectionClient {

		private MediaScannerConnection mMs;
		private File mFile;
		private MediaThumbCallback callback;
		private Context context;
		private long position;
		private int size;

		public SingleMediaScanner(Context context, File f, long position,final int size,
				MediaThumbCallback callback) {
			mFile = f;
			this.size = size;
			this.position = position;
			this.context = context;
			this.callback = callback;
			mMs = new MediaScannerConnection(context, this);
			mMs.connect();
		}

		@Override
		public void onMediaScannerConnected() {
			mMs.scanFile(mFile.getAbsolutePath(), null);
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {

			if (uri == null){
				
				callback.callback(position, null);
				return;
			}

			try {
				long origId = Long.parseLong(uri.getPath().substring(
						uri.getPath().lastIndexOf("/") + 1));

				Bitmap bitmap = null;
				if(size == 2){
					
					bitmap = MediaStore.Video.Thumbnails.getThumbnail(
							context.getContentResolver(), origId,
							Thumbnails.MINI_KIND, null);
				}else{
					bitmap = MediaStore.Video.Thumbnails.getThumbnail(
							context.getContentResolver(), origId,
							Thumbnails.MICRO_KIND, null);
				}
				callback.callback(position, bitmap);

			} catch (Exception e) {
                LogUtils.error(e);
				callback.callback(position, null);
			} finally {
				mMs.disconnect();
			}

		}
	}
}
