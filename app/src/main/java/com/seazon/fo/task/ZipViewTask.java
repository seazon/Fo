package com.seazon.fo.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.seazon.fo.Core;
import com.seazon.fo.zip.MyZipFile;
import com.seazon.utils.LogUtils;

public class ZipViewTask extends AsyncTask<Object, Object, MyZipFile> {

    private Uri path;
    private String path1;
    private ZipViewTaskCallback callback;
    private Core core;

    private Map<String, MyZipFile> map;

    public ZipViewTask(Uri path, Core core, ZipViewTaskCallback callback) {
        this.path = path;
        this.core = core;
        this.callback = callback;

        map = new HashMap<String, MyZipFile>();
    }

    public static Uri getFilePath(Context context, Uri uri) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            Cursor idCursor = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                idCursor = resolver.query(uri, new String[] { "_data" }, null, null, null);
                while (idCursor.moveToNext()) {
                    String path = idCursor.getString(0);
                    if (path == null) {
                        return null;
                    }
                    return Uri.fromFile(new File(path));
                }
                return null;
            } finally {
                if (idCursor != null)
                    idCursor.close();
            }
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            return uri;
        } else {
            return null;
        }
    }

    protected MyZipFile doInBackground(Object... params) {
        Uri u = getFilePath(core, path);
        if (u == null) {
            return null;
        }
        path1 = u.getPath();

        File rootFile = new File(path1);

        MyZipFile root = new MyZipFile();
        root.name = rootFile.getName();
        root.path = Core.PATH_ROOT_STD;
        root.size = 0;
        root.compressedSize = 0;
        root.time = rootFile.lastModified();
        root.parent = null;
        root.isDirectory = true;
        root.children = new ArrayList<MyZipFile>();

        map.put(root.path, root);

        try {
            ZipFile zipFile = new ZipFile(rootFile.getPath());
            Enumeration emu = zipFile.entries();
            while (emu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) emu.nextElement();

                MyZipFile file = create(entry);
                place(entry, file, root);

            }
            zipFile.close();

            return root;
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    // 放到对应的目录结构下去
    private void place(ZipEntry entry, MyZipFile file, MyZipFile dir) {
        // 根据path判断，如果是当前目录下的文件，直接加
        // FIXME Caused by: java.lang.StackOverflowError
        // at java.lang.String.contains(String.java:1927)
        // at com.seazon.fo.task.ZipViewTask.place(ZipViewTask.java:74)
        if (!file.path.substring(dir.path.length() + 1).contains(Core.PATH_SPLIT)) {
            if (file.isDirectory) {
                MyZipFile file2 = exist(path1, dir);
                if (file2 == null) {
                    // 关联file(也可能是一个dir)和dir的关系
                    file.parent = dir;
                    dir.children.add(file);

                    map.put(file.path, file);
                } else {
                    file2.size = file.size;
                    file2.compressedSize = file.compressedSize;
                    file2.time = file.time;
                }
            } else {

                // 关联file(也可能是一个dir)和dir的关系
                file.parent = dir;
                dir.children.add(file);

                map.put(file.path, file);
            }

        }
        // 如果不是，创建一层子目录，迭代判断
        else {
            // 创建一层子目录
            String path = file.path.substring(dir.path.length());
            if (Core.PATH_ROOT_STD.equals(dir.path)) {

            } else {
                path = path.substring(1);
            }
            String name = path.substring(0, path.indexOf(Core.PATH_SPLIT));
            if (Core.PATH_ROOT_STD.equals(dir.path)) {
                path = dir.path + name;
            } else {
                path = dir.path + Core.PATH_SPLIT + name;
            }

            // 判断是否这层目录已存在，已存在就不要创建了
            MyZipFile dir2 = exist(path, dir);
            if (dir2 == null) {
                dir2 = new MyZipFile();
                dir2.path = path;
                dir2.name = name;
                dir2.size = 0;
                dir2.compressedSize = 0;
                dir2.time = 0;
                dir2.isDirectory = true;
                dir2.children = new ArrayList<MyZipFile>();

                // 关联子目录和dir的关系
                dir2.parent = dir;
                dir.children.add(dir2);

                map.put(dir2.path, dir2);

                map.put(file.path, file);
            }
            // 迭代

            // FIXME java.lang.StackOverflowError
            place(entry, file, dir2);
        }
    }

    private MyZipFile exist(String path, MyZipFile dir) {
        for (MyZipFile file : dir.children) {
            if (!file.isDirectory)
                continue;

            if (file.path.equals(path))
                return file;
        }
        return null;
    }

    private MyZipFile create(ZipEntry entry) {
        String path = null;
        MyZipFile file = new MyZipFile();
        if (entry.isDirectory()) {
            path = entry.getName().substring(0, entry.getName().length() - 1);
        } else {
            path = entry.getName();
        }
        file.path = Core.PATH_SPLIT + path;
        file.name = path.substring(path.lastIndexOf(Core.PATH_SPLIT) + 1, path.length());
        file.size = entry.getSize();
        file.compressedSize = entry.getCompressedSize();
        file.time = entry.getTime();
        file.isDirectory = entry.isDirectory();
        file.children = new ArrayList<MyZipFile>();

        return file;
    }

    protected void onPostExecute(MyZipFile result) {
        callback.onZipViewTaskCallback(result, map);
    }

}
