package com.seazon.fo.activity;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.FileUtils;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.entity.Folder;
import com.seazon.fo.menu.ExtractMenu;
import com.seazon.fo.menu.ReturnCompressMenu;
import com.seazon.fo.task.ZipViewTask;
import com.seazon.fo.task.ZipViewTaskCallback;
import com.seazon.fo.zip.MyZipFile;
import com.seazon.fo.zip.ZipFileComparator;
import com.seazon.slidelayout.SlideLayout;
import com.seazon.utils.LogUtils;

public class CompressedFileListActivity extends FoSlideActivity implements OnItemClickListener, OnClickListener,
        ZipViewTaskCallback {
    private Stack<Folder> queue = new Stack<Folder>();

    private AbsListView listview;

    private ProgressDialog progressDialog;

    private Map<String, MyZipFile> map;
    private MyZipFile current;
    private String zipFileName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateSetCenterContentView(R.layout.filelist);

        renderActionBar(core.mode);

        Uri uri = getIntent().getData();

        inner = getIntent().getBooleanExtra("inner", false);

        // FIXME Caused by: java.lang.NullPointerException
        zipFileName = new File(uri.getPath()).getName();

        this.progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.zip_open), true);

        ZipViewTask task = new ZipViewTask(uri, core, this);
        task.execute();
    }

    @Override
    public void initMenu(int mode) {
        // findViewById(R.id.selectBar).setVisibility(View.GONE);
        addMenu(ExtractMenu.class.getName());
        addMenu(ReturnCompressMenu.class.getName());
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (slideLayout.currentScreen) {
            case SlideLayout.SCREEN_CENTER:
                if (exit(current.path)) {
                    finish();
                } else {
                    render(current.parent);
                }
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public void onClick(View v) {
        onNavBarClick(v, current.path);
    }

    protected void onNavBarClick(View v, String path) {
        for (Integer i : map1.values()) {
            if (v.getId() == i) {
                if (map2.get(i).equals(path)) {
                } else {
                    this.render(map.get(map2.get(i)));
                }
                return;
            }
        }
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        HashMap<String, Object> map = (HashMap<String, Object>) listViewDataMapList.get((int) arg3);
        MyZipFile file = null;
        if (Core.PATH_ROOT_STD.equals(current.path)) {
            file = this.map.get(Core.PATH_SPLIT + (String) map.get(FileAdapter.NAME).toString());
        } else {
            file = this.map.get(current.path + Core.PATH_SPLIT + (String) map.get(FileAdapter.NAME).toString());
        }

        if (file != null && file.isDirectory) {
            this.render(file);
        }
    }

    public void render(Object... args) {
        try {
            MyZipFile zipFile = (MyZipFile) args[0];
            if (current.path.equals(zipFile.path)) {
            } else if (zipFile.path.startsWith(current.path)) {
                // to child
                FileUtils.pushPathFromParentToChild(queue, current.path, zipFile.path);
            } else if (current.path.startsWith(zipFile.path)) {
                // to parent
                Folder folder = queue.pop();
                while (!folder.path.equals(zipFile.path)) {
                    folder = queue.pop();
                }
                folder = queue.push(folder);
            } else {
                String samePath = Helper.getSamePath(current.path, zipFile.path);

                // to parent
                Folder folder = queue.pop();
                while (!folder.path.equals(samePath)) {
                    folder = queue.pop();
                }
                queue.push(folder);

                // to child
                FileUtils.pushPathFromParentToChild(queue, samePath, zipFile.path);
            }

            listViewDataMapList.clear();
            // thumbMap.clear();

            MyZipFile directory = this.map.get(zipFile.path);
            List<MyZipFile> files = directory.children;

            if (files != null && files.size() > 0) {
                ZipFileComparator comparatorFile = new ZipFileComparator(core.getMainPreferences().getOrder(), core
                        .getMainPreferences().getOrder2());
                try {
                    Collections.sort(files, comparatorFile);
                } catch (Exception e) {
                    LogUtils.error(e);
                }

                HashMap<String, Object> map = null;
                String desc = null;
                // Bitmap img = null;
                int resId;
                for (MyZipFile file : files) {
                    map = new HashMap<String, Object>();
                    map.put(FileAdapter.NAME, file.name);
                    map.put(FileAdapter.FILE_PATH, file.path);

                    if (core.getClipper().getCopys().contains(file)) {
                        map.put(FileAdapter.SELECT, 1);
                    } else {
                        map.put(FileAdapter.SELECT, 0);
                    }

                    if (file.isDirectory) {
                        if (isLight()) {
                            resId = R.drawable.format_folder;
                            // img =
                            // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder)).getBitmap();
                        } else {
                            resId = R.drawable.format_folder_dark;
                            // img =
                            // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder_dark)).getBitmap();
                        }
                        desc = Helper.getDes(file.time);
                        ;
                    } else {
                        desc = Helper.getDes(file.compressedSize, file.time);
                        int D = Helper.setFileIcon(file.name, isLight());
                        resId = D;
                        // img =
                        // ((BitmapDrawable)getResources().getDrawable(D)).getBitmap();
                    }
                    // map.put("img", file.hashCode());
                    map.put(FileAdapter.RES_ID, resId);
                    // thumbMap.put(file.hashCode(), img);
                    map.put(FileAdapter.DESC, desc);

                    listViewDataMapList.add(map);

                }
            }

            listview = getListView();
            listview.setOnItemClickListener(this);
            listview.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        Folder f = queue.peek();
                        f.pos = listview.getFirstVisiblePosition();
                        View v = listview.getChildAt(0);
                        f.offset = (v == null ? 0 : v.getTop());
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    // TODO Auto-generated method stub
                }
            });

            current = directory;

            // listViewAdapter.notifyDataSetChanged();

            // move to pos
            Folder folder = queue.peek();
            if (folder.pos != 0) {
                if (listview instanceof ListView)
                    ((ListView) listview).setSelectionFromTop(folder.pos, folder.offset);
                else
                    listview.setSelection(folder.pos);
            }

            setNavigationBar(zipFileName, current.path.split(Core.PATH_SPLIT), Core.PATH_ROOT_STD);
        } catch (Exception e) {
            LogUtils.error(e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onZipViewTaskCallback(MyZipFile root, Map<String, MyZipFile> map) {

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        this.current = root;
        this.map = map;

        zipFileName = root.name; // TODO java.lang.NullPointerException

        queue.push(new Folder(Core.PATH_ROOT_STD, 0, 0)); // TODO 没有优化位置处理
        try {
            FileUtils.pushPathFromParentToChild(queue, Core.PATH_ROOT_STD, root.path);
        } catch (Exception e) {
            LogUtils.error(e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        render(root);
    }

    public void onRefresh(boolean resetClipper, int mode, RefreshType renderLevel, boolean showActionBar) {
        render(this.current);
    }

}
