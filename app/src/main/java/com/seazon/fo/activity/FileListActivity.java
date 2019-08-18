package com.seazon.fo.activity;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.FileUtils;
import com.seazon.fo.Helper;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.entity.FileComparator;
import com.seazon.fo.entity.Folder;
import com.seazon.fo.menu.AddFavorriteMenu;
import com.seazon.fo.menu.BluetoothMenu;
import com.seazon.fo.menu.CompressMenu;
import com.seazon.fo.menu.CopyMenu;
import com.seazon.fo.menu.CutMenu;
import com.seazon.fo.menu.DeleteMenu;
import com.seazon.fo.menu.ExitMenu;
import com.seazon.fo.menu.ExitPickMenu;
import com.seazon.fo.menu.ExtractMenu;
import com.seazon.fo.menu.HomeMenu;
import com.seazon.fo.menu.NewFolderMenu;
import com.seazon.fo.menu.PasteMenu;
import com.seazon.fo.menu.PropertiesMenu;
import com.seazon.fo.menu.RenameMenu;
import com.seazon.fo.menu.ReturnModePasteMenu;
import com.seazon.fo.menu.ReturnModeSelectMenu;
import com.seazon.fo.menu.SearchMenu;
import com.seazon.fo.menu.SelectAllMenu;
import com.seazon.fo.menu.SendMenu;
import com.seazon.fo.menu.SetHomeMenu;
import com.seazon.fo.menu.ShortcutMenu;
import com.seazon.fo.task.CopyTask;
import com.seazon.fo.task.DeleteTask;
import com.seazon.fo.task.MoveTask;
import com.seazon.slidelayout.SlideLayout;
import com.seazon.utils.LogUtils;

public class FileListActivity extends FoSlideActivity implements OnItemClickListener, OnItemLongClickListener,
        OnClickListener {

    private String currentpath = null;
    private String pickType;

    private Stack<Folder> queue = new Stack<Folder>();

    private ProgressDialog dialog;

    private AbsListView listview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateSetCenterContentView(R.layout.filelist);

        initMode();
        initCurrentPath();

        renderActionBar(core.mode);

        queue.push(new Folder(Core.PATH_ROOT_STD, 0, 0));
        try {
            FileUtils.pushPathFromParentToChild(queue, Core.PATH_ROOT_STD, currentpath);
        } catch (Exception e) {
            LogUtils.error(e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initMode() {
        if (Intent.ACTION_PICK.equals(getIntent().getAction())
                || Intent.ACTION_GET_CONTENT.equals(getIntent().getAction())) {
            core.mode = Core.MODE_PICK;
            pickType = getIntent().getType();
            LogUtils.debug("pick file, action:" + getIntent().getAction() + ", type:" + pickType);
            // } else {
            // core.mode = Core.MODE_NORMAL;
        }
    }

    private void initCurrentPath() {
        if (core.mode == Core.MODE_PICK) {
            currentpath = Core.PATH_SDCARD;
            return;
        }

        Uri uri = getIntent().getData();
        if (uri != null) {
            try {
                String path = uri.getPath();
                if (!Helper.isBlank(path)) {
                    File file = new File(path);
                    if (file.exists() && file.isFile()) {
                        path = file.getParent();
                    }

                    currentpath = path;
                }
            } catch (Exception e) {
                LogUtils.error(e);
                currentpath = null;
            }
        }

        if (Helper.isBlank(currentpath)) {
            currentpath = core.getCurrentPath();
        }

        if (Helper.isBlank(currentpath)) {
            currentpath = core.getMainPreferences().getHome();
        }

        currentpath = FileUtils.formatPath(currentpath);
    }

    protected void onStart() {
        super.onStart();
        render(currentpath);
    }

    public void onRefresh(boolean resetClipper, int mode, RefreshType renderLevel, boolean showActionBar) {
        if (resetClipper) {
            core.getClipper().getCopys().clear();
            core.getClipper().getPositions().clear();
            core.getClipper().setCopytype(Clipper.COPYTYPE_NA);
        }

        core.mode = mode;
        if (showActionBar && renderLevel != RefreshType.RENDER)
            renderActionBar(core.mode);

        if (renderLevel != RefreshType.NONE) {
            if (renderLevel == RefreshType.NOTIFY) {
                this.listViewAdapter.notifyDataSetChanged();
            } else if (renderLevel == RefreshType.SELECT_RESET) {
                for (Map<String, Object> map : listViewDataMapList) {
                    map.put(FileAdapter.SELECT, 0);
                }
                this.listViewAdapter.notifyDataSetChanged();
            } else if (renderLevel == RefreshType.SELECT_ALL) {
                for (Map<String, Object> map : listViewDataMapList) {
                    map.put(FileAdapter.SELECT, 1);
                }
                this.listViewAdapter.notifyDataSetChanged();
            } else if (renderLevel == RefreshType.RENDER) {
                render(currentpath);
            }
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (slideLayout.currentScreen) {
            case SlideLayout.SCREEN_CENTER:
                switch (core.mode) {
                case Core.MODE_SELECT:
                    onRefresh(true, Core.MODE_NORMAL, RefreshType.RENDER, true);
                    break;
                default:
                    if (exit(currentpath)) {
                        core.saveCurrentPath(core.getMainPreferences().getHome());
                        finish();
                    } else {
                        render(new File(currentpath).getParent());
                    }
                    break;
                }
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    protected boolean exit(String path) {
        if (core.getRootPath().equals(path)) {
            return true;
        } else {
            return false;
        }
    }

    public void onClick(View v) {
        onNavBarClick(v, currentpath);
    }

    public void selectAll() {
        if (core.getClipper().getCopys().size() == 0) {
            // select all
            File[] files = new File(currentpath).listFiles();
            // FIXME java.lang.NullPointerException
            for (int i = 0; i < files.length; ++i) {
                core.getClipper().getCopys().add(files[i]);
                core.getClipper().getPositions().add((long) i);
            }

            onRefresh(false, Core.MODE_SELECT, RefreshType.SELECT_ALL, true);
        } else {
            if (this.currentpath.equals(core.getClipper().getCopys().get(0).getParent())) {
                // select none
                core.getClipper().getCopys().clear();
                core.getClipper().getPositions().clear();

                onRefresh(true, Core.MODE_SELECT, RefreshType.SELECT_RESET, true);
            } else {
                // select all
                File[] files = new File(currentpath).listFiles();
                for (int i = 0; i < files.length; ++i) {
                    core.getClipper().getCopys().add(files[i]);
                    core.getClipper().getPositions().add((long) i);
                }

                onRefresh(false, Core.MODE_SELECT, RefreshType.SELECT_ALL, true);
            }
        }
    }

    protected void onNavBarClick(View v, String path) {
        for (Integer i : map1.values()) {
            if (v.getId() == i) {
                if (map2.get(i).equals(path)) {
                    // selectAll();
                } else {
                    if (core.mode == Core.MODE_SELECT) {
                        onRefresh(true, Core.MODE_NORMAL, RefreshType.NONE, true);
                        this.render(map2.get(i));
                    } else {
                        this.render(map2.get(i));
                    }
                }
                return;
            }
        }
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        HashMap<String, Object> map = (HashMap<String, Object>) listViewDataMapList.get((int) arg3);
        File file = new File(currentpath + Core.PATH_SPLIT + (String) map.get(FileAdapter.NAME).toString());

        if (core.mode == Core.MODE_SELECT) {
            onItemSelected(map, file, arg1, arg3);
        } else if (core.mode == Core.MODE_PICK) {
            onFilePick(file);
        } else {
            onFileOpen(file);
        }
    }

    private void onFilePick(File file) {
        if (file.isDirectory()) {
            if (!file.canRead()) {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                return;
            }

            this.render(file.getPath());
        } else {
            Intent data = new Intent();
            data.setData(Uri.fromFile(file));
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void onFileOpen(File file) {
        if (file.isDirectory()) {
            if (!file.canRead()) {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                return;
            }

            this.render(file.getPath());
        } else {
            FileUtils.openFile(file, this);
        }
    }

    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (core.mode == Core.MODE_SELECT)
            onRefresh(false, Core.MODE_SELECT, RefreshType.NOTIFY, false);
        else if (core.mode == Core.MODE_PICK) {
            onItemClick(arg0, arg1, arg2, arg3);
            return true;
        } else
            onRefresh(true, Core.MODE_SELECT, RefreshType.SELECT_RESET, false);

        HashMap<String, Object> map = (HashMap<String, Object>) listViewDataMapList.get((int) arg3);
        File file = new File(currentpath + Core.PATH_SPLIT + (String) map.get(FileAdapter.NAME));

        onItemSelected(map, file, arg1, arg3);

        return true;
    }

    private void onItemSelected(HashMap<String, Object> map, File file, View arg1, long arg3) {
        if (core.getClipper().getCopys().contains(file)) {
            core.getClipper().getCopys().remove(file);
            core.getClipper().getPositions().remove(arg3);

            map.put(FileAdapter.SELECT, 0);
            // ((ImageView)
            // arg1.findViewById(R.id.select)).setImageDrawable(null);
            listViewAdapter.updateView((int) arg3, 0);
        } else {
            if (core.getClipper().getCopys().size() == 0) {
                core.getClipper().getCopys().add(file);
                core.getClipper().getPositions().add(arg3);
            } else {
                if (core.getClipper().getCopys().get(0).getParent().equals(file.getParent())) {
                    core.getClipper().getCopys().add(file);
                    core.getClipper().getPositions().add(arg3);
                } else {
                    core.getClipper().getCopys().clear();
                    core.getClipper().getCopys().add(file);
                    core.getClipper().getPositions().add(arg3);
                }
            }

            map.put(FileAdapter.SELECT, 1);
            // ((ImageView)
            // arg1.findViewById(R.id.select)).setImageDrawable(FoSelector.select(FileListActivity.this));
            listViewAdapter.updateView((int) arg3, 1);
        }

        renderActionBar(Core.MODE_SELECT);
    }

    @Override
    public void initMenu(int mode) {
        File currentFolder = new File(this.currentpath);
        // ArrayList<Integer> list = new ArrayList<Integer>();
        switch (mode) {
        case Core.MODE_NORMAL:

            // findViewById(R.id.selectBar).setVisibility(View.GONE);
            addMenu(HomeMenu.class.getName());
            if (currentFolder.isDirectory() && currentFolder.canWrite())
                addMenu(NewFolderMenu.class.getName());
            addMenu(SearchMenu.class.getName());
            addMenu(ExitMenu.class.getName());
            break;
        case Core.MODE_SELECT:

            // findViewById(R.id.selectBar).setVisibility(View.VISIBLE);

            int size = core.getClipper().getCopys().size();
            // TextView selectDesView =
            // (TextView)findViewById(R.id.selectDesView);
            // selectDesView.setText(String.format(getResources().getString(R.string.select_description),
            // size));
            updateSelectionNumber(size);

            switch (size) {
            case 0:
                addMenu(SelectAllMenu.class.getName());
                addMenu(ReturnModeSelectMenu.class.getName());
                break;
            case 1:
                addMenu(SelectAllMenu.class.getName());
                addMenu(CopyMenu.class.getName());
                addMenu(CutMenu.class.getName());
                addMenu(DeleteMenu.class.getName());
                addMenu(RenameMenu.class.getName());
                if (!core.getClipper().getCopys().get(0).isDirectory()) {
                    addMenu(BluetoothMenu.class.getName());
                    addMenu(SendMenu.class.getName());
                }
                String type = Helper.getTypeByExtension(core.getClipper().getCopys().get(0).getName());
                addMenu(CompressMenu.class.getName());
                if ("application/zip".equals(type)) {
                    addMenu(ExtractMenu.class.getName());
                }
                if (core.getClipper().getCopys().get(0).isDirectory()) {
                    addMenu(SetHomeMenu.class.getName());
                    addMenu(AddFavorriteMenu.class.getName());
                }
                addMenu(ShortcutMenu.class.getName());
                addMenu(PropertiesMenu.class.getName());
                addMenu(ReturnModeSelectMenu.class.getName());
                break;
            default:

                // findViewById(R.id.selectBar).setVisibility(View.VISIBLE);

                addMenu(SelectAllMenu.class.getName());
                addMenu(CopyMenu.class.getName());
                addMenu(CutMenu.class.getName());
                addMenu(DeleteMenu.class.getName());
                addMenu(BluetoothMenu.class.getName());
                addMenu(SendMenu.class.getName());
                addMenu(CompressMenu.class.getName());
                addMenu(ReturnModeSelectMenu.class.getName());
                break;
            }
            break;
        case Core.MODE_PASTE:

            size = core.getClipper().getCopys().size();
            // selectDesView = (TextView)findViewById(R.id.selectDesView);
            updateSelectionNumber(size);
            // switch (core.getClipper().getCopytype()) {
            // case Clipper.COPYTYPE_COPY:
            // selectDesView.setText(String.format(getResources().getString(R.string.select_description_copy),
            // size));
            // break;
            // case Clipper.COPYTYPE_CUT:
            // selectDesView.setText(String.format(getResources().getString(R.string.select_description_cut),
            // size));
            // break;
            // }

            addMenu(PasteMenu.class.getName());
            addMenu(HomeMenu.class.getName());
            if (currentFolder.isDirectory() && currentFolder.canWrite())
                addMenu(NewFolderMenu.class.getName());
            addMenu(ReturnModePasteMenu.class.getName());
            break;
        case Core.MODE_PICK:

            // findViewById(R.id.selectBar).setVisibility(View.GONE);
            addMenu(HomeMenu.class.getName());
            addMenu(ExitPickMenu.class.getName());
            break;
        }

    }

    private void calcFolderQueue(String path) throws Exception {
        if (currentpath.equals(path)) {
        } else if (path.startsWith(currentpath)) {
            // to child
            FileUtils.pushPathFromParentToChild(queue, currentpath, path);
        } else if (currentpath.startsWith(path)) {
            // to parent
            Folder folder = queue.pop();
            while (!folder.path.equals(path)) {
                // FIXME java.util.EmptyStackException
                folder = queue.pop();
            }
            folder = queue.push(folder);
        } else {
            String samePath = Helper.getSamePath(currentpath, path);

            // to parent
            Folder folder = queue.pop();
            while (!folder.path.equals(samePath)) {
                folder = queue.pop();
            }
            queue.push(folder);

            // to child
            FileUtils.pushPathFromParentToChild(queue, samePath, path);
        }
    }

    public void render(Object... args) {
        try {
            String path = (String) args[0];
            calcFolderQueue(path);

            // if(thumbTask!=null && !thumbTask.isCancelled())
            // {
            // thumbTask.cancel(false);
            // }
            // if(appTask!=null && !appTask.isCancelled())
            // {
            // appTask.cancel(false);
            // }

            listViewDataMapList.clear();
            // thumbMap.clear();

            File directory = new File(path);
            // CmdTask task = new CmdTask();
            // task.execute(directory);
            //
            // if(directory.canRead()==false){
            // return;
            // }
            File[] files = null;
            files = directory.listFiles();

            if (files != null && files.length > 0) {
                FileComparator comparatorFile = new FileComparator(core.getMainPreferences().getOrder(), core
                        .getMainPreferences().getOrder2());
                try {
                    Arrays.sort(files, comparatorFile);
                } catch (Exception e) {
                    LogUtils.error(e);
                }

                HashMap<String, Object> map = null;
                String desc = null;
//                String descAdd = null;
                // Bitmap img = null;
                int resId;
                for (File file : files) {
                    if (file.getName().startsWith(".") && core.getMainPreferences().isShowHidden() == false)
                        continue;

                    map = new HashMap<String, Object>();
                    map.put(FileAdapter.NAME, file.getName());
                    map.put(FileAdapter.FILE_PATH, file.getPath());

                    if (core.getClipper().getCopys().contains(file)) {
                        map.put(FileAdapter.SELECT, 1);
                    } else {
                        map.put(FileAdapter.SELECT, 0);
                    }

                    if (file.isDirectory()) {
                        if (file.canRead() && file.canWrite()) {
                            if (isLight()) {

                                // img =
                                // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder)).getBitmap();
                                resId = R.drawable.format_folder;
                            } else {
                                resId = R.drawable.format_folder_dark;
                                // img =
                                // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder_dark)).getBitmap();
                            }
                        } else {
                            if (isLight()) {
                                resId = R.drawable.format_folder_lock;
                                // img =
                                // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder_lock)).getBitmap();
                            } else {
                                resId = R.drawable.format_folder_lock_dark;
                                // img =
                                // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder_lock_dark)).getBitmap();
                            }
                        }
                        desc = Helper.getDes(file.lastModified());
                    } else {
                        desc = Helper.getDes(file.length(), file.lastModified());
//                        descAdd = Helper.getDescAdd(file, this.getApplicationContext());
//                        desc = desc + descAdd;
                        int D = Helper.setFileIcon(file.getName(), isLight());
                        resId = D;
                        // img =
                        // ((BitmapDrawable)getResources().getDrawable(D)).getBitmap();
                    }
                    map.put("img", file.hashCode());
                    map.put(FileAdapter.RES_ID, resId);
                    // thumbMap.put(file.hashCode(), img);
                    map.put(FileAdapter.DESC, desc);

                    listViewDataMapList.add(map);
                }
            }

            currentpath = directory.getPath();
            setTitle(currentpath);
            core.saveCurrentPath(currentpath);

            // listViewAdapter.notifyDataSetChanged();

            listview = getListView();
            listview.setOnItemClickListener(this);
            listview.setOnItemLongClickListener(this);
            listview.setOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        // FIXME java.util.EmptyStackException
                        Folder f = queue.peek();
                        f.pos = listview.getFirstVisiblePosition();
                        View v = listview.getChildAt(0);
                        f.offset = (v == null ? 0 : v.getTop());
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                }
            });

            // move to pos
            Folder folder = queue.peek();
            if (folder.pos != 0) {
                if (listview instanceof ListView)
                    ((ListView) listview).setSelectionFromTop(folder.pos, folder.offset);
                else
                    listview.setSelection(folder.pos);
            }

            // appTask = new AppTask(thumbMap, listViewAdapter, getResources());
            // appTask.execute(files);

            // if (core.getMainPreferences().isShowThumb())
            // {
            // int size =
            // core.getMainPreferences().getView().equals(Core.VIEW_THUMB)?2:1;
            // thumbTask = new ThumbTask(thumbMap, listViewAdapter, this, size);
            // thumbTask.execute(files);
            // }

            setNavigationBar(core.getRootName(), currentpath.split(Core.PATH_SPLIT), core.getRootPath());
            renderActionBar(core.mode);
        } catch (Exception e) {
            LogUtils.error(e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onOperationStart(int operationType, File f, Clipper clipper) {
        switch (operationType) {
        case Core.OPERATION_TYPE_COPY:

            final CopyTask copyTask = new CopyTask(core, f.getPath(), clipper, this, this);
            copyTask.execute();
            this.dialog = ProgressDialog.show(this, null, getResources().getString(R.string.copy_files), false, true,
                    new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            copyTask.cancel(false);
                        }
                    });

            break;

        case Core.OPERATION_TYPE_MOVE:
            final MoveTask moveTask = new MoveTask(core, f.getPath(), clipper, this, this);
            moveTask.execute();
            this.dialog = ProgressDialog.show(this, null, getResources().getString(R.string.move_files), false, true,
                    new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            moveTask.cancel(false);
                        }
                    });
            break;
        case Core.OPERATION_TYPE_DELETE:
            final DeleteTask deleteTask = new DeleteTask(core, this, clipper, this, this);
            deleteTask.execute();
            this.dialog = ProgressDialog.show(this, null, getResources().getString(R.string.delete_files), false, true,
                    new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            deleteTask.cancel(false);
                        }
                    });

            break;
        }

    }

    public void onOperationCancel() {
        if (dialog != null) {
            // FIXME View not attached to window manager
            dialog.dismiss();
            dialog = null;
        }
    }

    public void onOperationUpdate(int operationType, String message) {
        if (dialog != null) {
            dialog.setMessage(message);
        }
    }

    public static int return_code_nothing = 123;
    public static int return_code_local_path = 456;
    public static int return_code_favourities = 888;
    public static int return_code_exit = 883;
    public static int return_paste = 884;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == return_code_nothing) {

        } else if (resultCode == return_code_local_path) {
            render(data.getStringExtra("Path"));
        } else if (resultCode == return_code_favourities) {
            render(data.getStringExtra("Path"));
        } else if (resultCode == return_code_exit) {
            finish();
        } else if (resultCode == return_paste) {
            onRefresh(false, Core.MODE_PASTE, RefreshType.SELECT_RESET, true);
        }
    }

}
