package com.seazon.fo.activity

import android.app.Activity
import android.app.AlertDialog
import java.io.File
import java.util.Arrays
import java.util.HashMap
import java.util.Stack

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import android.widget.Toast

import com.seazon.fo.Core
import com.seazon.fo.FileUtils
import com.seazon.fo.Helper
import com.seazon.fo.R
import com.seazon.fo.RefreshType
import com.seazon.fo.entity.Clipper
import com.seazon.fo.entity.FileComparator
import com.seazon.fo.entity.Folder
import com.seazon.fo.menu.AddFavorriteMenu
import com.seazon.fo.menu.BluetoothMenu
import com.seazon.fo.menu.CompressMenu
import com.seazon.fo.menu.CopyMenu
import com.seazon.fo.menu.CutMenu
import com.seazon.fo.menu.DeleteMenu
import com.seazon.fo.menu.ExitMenu
import com.seazon.fo.menu.ExitPickMenu
import com.seazon.fo.menu.ExtractMenu
import com.seazon.fo.menu.HomeMenu
import com.seazon.fo.menu.NewFolderMenu
import com.seazon.fo.menu.PasteMenu
import com.seazon.fo.menu.PropertiesMenu
import com.seazon.fo.menu.RenameMenu
import com.seazon.fo.menu.ReturnModePasteMenu
import com.seazon.fo.menu.ReturnModeSelectMenu
import com.seazon.fo.menu.SearchMenu
import com.seazon.fo.menu.SelectAllMenu
import com.seazon.fo.menu.SendMenu
import com.seazon.fo.menu.SetHomeMenu
import com.seazon.fo.menu.ShortcutMenu
import com.seazon.fo.task.CopyTask
import com.seazon.fo.task.DeleteTask
import com.seazon.fo.task.MoveTask
import com.seazon.slidelayout.SlideLayout
import com.seazon.utils.LogUtils
import com.seazon.utils.permission.AppPermissions
import com.seazon.utils.permission.StoragePermsProvider

class FileListActivity : FoSlideActivity(), OnItemClickListener, OnItemLongClickListener, OnClickListener, StoragePermsProvider {

    companion object {
        const val return_code_nothing = 123
        const val return_code_local_path = 456
        const val return_code_favourities = 888
        const val return_code_exit = 883
        const val return_paste = 884
    }

    private var currentpath: String? = null
    private var pickType: String? = null

    private val queue = Stack<Folder>()

    private var dialog: ProgressDialog? = null

    private var listview: AbsListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateSetCenterContentView(R.layout.filelist)

        initMode()
        initCurrentPath()

        renderActionBar(core.mode)

        queue.push(Folder(Core.PATH_ROOT_STD, 0, 0))
        try {
            FileUtils.pushPathFromParentToChild(queue, Core.PATH_ROOT_STD, currentpath)
        } catch (e: Exception) {
            LogUtils.error(e)
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }

        ensureStoragePermissions(this)
    }

    private fun initMode() {
        if (Intent.ACTION_PICK == intent.action || Intent.ACTION_GET_CONTENT == intent.action) {
            core.mode = Core.MODE_PICK
            pickType = intent.type
            LogUtils.debug("pick file, action:" + intent.action + ", type:" + pickType)
            // } else {
            // core.mode = Core.MODE_NORMAL;
        }
    }

    private fun initCurrentPath() {
        if (core.mode == Core.MODE_PICK) {
            currentpath = Core.PATH_SDCARD
            return
        }

        val uri = intent.data
        if (uri != null) {
            try {
                var path = uri.path
                if (!Helper.isBlank(path)) {
                    val file = File(path!!)
                    if (file.exists() && file.isFile) {
                        path = file.parent
                    }

                    currentpath = path
                }
            } catch (e: Exception) {
                LogUtils.error(e)
                currentpath = null
            }

        }

        if (Helper.isBlank(currentpath)) {
            currentpath = core.currentPath
        }

        if (Helper.isBlank(currentpath)) {
            currentpath = core.mainPreferences.home
        }

        currentpath = FileUtils.formatPath(currentpath)
    }

    override fun onStart() {
        super.onStart()
        render(currentpath)
    }

    override fun onRefresh(resetClipper: Boolean, mode: Int, renderLevel: RefreshType, showActionBar: Boolean) {
        if (resetClipper) {
            core.clipper.copys.clear()
            core.clipper.positions.clear()
            core.clipper.copytype = Clipper.COPYTYPE_NA
        }

        core.mode = mode
        if (showActionBar && renderLevel != RefreshType.RENDER)
            renderActionBar(core.mode)

        if (renderLevel != RefreshType.NONE) {
            if (renderLevel == RefreshType.NOTIFY) {
                this.listViewAdapter.notifyDataSetChanged()
            } else if (renderLevel == RefreshType.SELECT_RESET) {
                for (map in listViewDataMapList) {
                    map[FileAdapter.SELECT] = 0
                }
                this.listViewAdapter.notifyDataSetChanged()
            } else if (renderLevel == RefreshType.SELECT_ALL) {
                for (map in listViewDataMapList) {
                    map[FileAdapter.SELECT] = 1
                }
                this.listViewAdapter.notifyDataSetChanged()
            } else if (renderLevel == RefreshType.RENDER) {
                render(currentpath)
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when (slideLayout.currentScreen) {
                SlideLayout.SCREEN_CENTER -> {
                    when (core.mode) {
                        Core.MODE_SELECT -> onRefresh(true, Core.MODE_NORMAL, RefreshType.RENDER, true)
                        else -> if (exit(currentpath)) {
                            core.saveCurrentPath(core.mainPreferences.home)
                            finish()
                        } else {
                            render(File(currentpath!!).parent)
                        }
                    }
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun exit(path: String?): Boolean {
        return if (core.rootPath == path) {
            true
        } else {
            false
        }
    }

    override fun onClick(v: View) {
        onNavBarClick(v, currentpath)
    }

    override fun selectAll() {
        if (core.clipper.copys.size == 0) {
            // select all
            val files = File(currentpath!!).listFiles()
            // FIXME java.lang.NullPointerException
            for (i in files!!.indices) {
                core.clipper.copys.add(files[i])
                core.clipper.positions.add(i.toLong())
            }

            onRefresh(false, Core.MODE_SELECT, RefreshType.SELECT_ALL, true)
        } else {
            if (this.currentpath == core.clipper.copys[0].parent) {
                // select none
                core.clipper.copys.clear()
                core.clipper.positions.clear()

                onRefresh(true, Core.MODE_SELECT, RefreshType.SELECT_RESET, true)
            } else {
                // select all
                val files = File(currentpath!!).listFiles()
                for (i in files!!.indices) {
                    core.clipper.copys.add(files[i])
                    core.clipper.positions.add(i.toLong())
                }

                onRefresh(false, Core.MODE_SELECT, RefreshType.SELECT_ALL, true)
            }
        }
    }

    override fun onNavBarClick(v: View, path: String?) {
        for (i in map1.values) {
            if (v.id == i) {
                if (map2[i] == path) {
                    // selectAll();
                } else {
                    if (core.mode == Core.MODE_SELECT) {
                        onRefresh(true, Core.MODE_NORMAL, RefreshType.NONE, true)
                        this.render(map2[i])
                    } else {
                        this.render(map2[i])
                    }
                }
                return
            }
        }
    }

    override fun onItemClick(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {
        val map = listViewDataMapList[arg3.toInt()] as HashMap<String, Any>
        val file = File(currentpath + Core.PATH_SPLIT + map[FileAdapter.NAME]!!.toString())

        if (core.mode == Core.MODE_SELECT) {
            onItemSelected(map, file, arg1, arg3)
        } else if (core.mode == Core.MODE_PICK) {
            onFilePick(file)
        } else {
            onFileOpen(file)
        }
    }

    private fun onFilePick(file: File) {
        if (file.isDirectory) {
            if (!file.canRead()) {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                return
            }

            this.render(file.path)
        } else {
            val data = Intent()
            data.data = Uri.fromFile(file)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    private fun onFileOpen(file: File) {
        if (file.isDirectory) {
            if (!file.canRead()) {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                return
            }

            this.render(file.path)
        } else {
            FileUtils.openFile(file, this)
        }
    }

    override fun onItemLongClick(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long): Boolean {
        if (core.mode == Core.MODE_SELECT)
            onRefresh(false, Core.MODE_SELECT, RefreshType.NOTIFY, false)
        else if (core.mode == Core.MODE_PICK) {
            onItemClick(arg0, arg1, arg2, arg3)
            return true
        } else
            onRefresh(true, Core.MODE_SELECT, RefreshType.SELECT_RESET, false)

        val map = listViewDataMapList[arg3.toInt()] as HashMap<String, Any>
        val file = File(currentpath + Core.PATH_SPLIT + map[FileAdapter.NAME] as String?)

        onItemSelected(map, file, arg1, arg3)

        return true
    }

    private fun onItemSelected(map: HashMap<String, Any>, file: File, arg1: View, arg3: Long) {
        if (core.clipper.copys.contains(file)) {
            core.clipper.copys.remove(file)
            core.clipper.positions.remove(arg3)

            map[FileAdapter.SELECT] = 0
            // ((ImageView)
            // arg1.findViewById(R.id.select)).setImageDrawable(null);
            listViewAdapter.updateView(arg3.toInt(), 0)
        } else {
            if (core.clipper.copys.size == 0) {
                core.clipper.copys.add(file)
                core.clipper.positions.add(arg3)
            } else {
                if (core.clipper.copys[0].parent == file.parent) {
                    core.clipper.copys.add(file)
                    core.clipper.positions.add(arg3)
                } else {
                    core.clipper.copys.clear()
                    core.clipper.copys.add(file)
                    core.clipper.positions.add(arg3)
                }
            }

            map[FileAdapter.SELECT] = 1
            // ((ImageView)
            // arg1.findViewById(R.id.select)).setImageDrawable(FoSelector.select(FileListActivity.this));
            listViewAdapter.updateView(arg3.toInt(), 1)
        }

        renderActionBar(Core.MODE_SELECT)
    }

    override fun initMenu(mode: Int) {
        val currentFolder = File(this.currentpath!!)
        // ArrayList<Integer> list = new ArrayList<Integer>();
        when (mode) {
            Core.MODE_NORMAL -> {

                // findViewById(R.id.selectBar).setVisibility(View.GONE);
                addMenu(HomeMenu::class.java.name)
                if (currentFolder.isDirectory && currentFolder.canWrite())
                    addMenu(NewFolderMenu::class.java.name)
                addMenu(SearchMenu::class.java.name)
                addMenu(ExitMenu::class.java.name)
            }
            Core.MODE_SELECT -> {

                // findViewById(R.id.selectBar).setVisibility(View.VISIBLE);

                val size = core.clipper.copys.size
                // TextView selectDesView =
                // (TextView)findViewById(R.id.selectDesView);
                // selectDesView.setText(String.format(getResources().getString(R.string.select_description),
                // size));
                updateSelectionNumber(size)

                when (size) {
                    0 -> {
                        addMenu(SelectAllMenu::class.java.name)
                        addMenu(ReturnModeSelectMenu::class.java.name)
                    }
                    1 -> {
                        addMenu(SelectAllMenu::class.java.name)
                        addMenu(CopyMenu::class.java.name)
                        addMenu(CutMenu::class.java.name)
                        addMenu(DeleteMenu::class.java.name)
                        addMenu(RenameMenu::class.java.name)
                        if (!core.clipper.copys[0].isDirectory) {
                            addMenu(BluetoothMenu::class.java.name)
                            addMenu(SendMenu::class.java.name)
                        }
                        val type = Helper.getTypeByExtension(core.clipper.copys[0].name)
                        addMenu(CompressMenu::class.java.name)
                        if ("application/zip" == type) {
                            addMenu(ExtractMenu::class.java.name)
                        }
                        if (core.clipper.copys[0].isDirectory) {
                            addMenu(SetHomeMenu::class.java.name)
                            addMenu(AddFavorriteMenu::class.java.name)
                        }
                        addMenu(ShortcutMenu::class.java.name)
                        addMenu(PropertiesMenu::class.java.name)
                        addMenu(ReturnModeSelectMenu::class.java.name)
                    }
                    else -> {

                        // findViewById(R.id.selectBar).setVisibility(View.VISIBLE);

                        addMenu(SelectAllMenu::class.java.name)
                        addMenu(CopyMenu::class.java.name)
                        addMenu(CutMenu::class.java.name)
                        addMenu(DeleteMenu::class.java.name)
                        addMenu(BluetoothMenu::class.java.name)
                        addMenu(SendMenu::class.java.name)
                        addMenu(CompressMenu::class.java.name)
                        addMenu(ReturnModeSelectMenu::class.java.name)
                    }
                }
            }
            Core.MODE_PASTE -> {

                val size = core.clipper.copys.size
                // selectDesView = (TextView)findViewById(R.id.selectDesView);
                updateSelectionNumber(size)
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

                addMenu(PasteMenu::class.java.name)
                addMenu(HomeMenu::class.java.name)
                if (currentFolder.isDirectory && currentFolder.canWrite())
                    addMenu(NewFolderMenu::class.java.name)
                addMenu(ReturnModePasteMenu::class.java.name)
            }
            Core.MODE_PICK -> {

                // findViewById(R.id.selectBar).setVisibility(View.GONE);
                addMenu(HomeMenu::class.java.name)
                addMenu(ExitPickMenu::class.java.name)
            }
        }

    }

    @Throws(Exception::class)
    private fun calcFolderQueue(path: String) {
        if (currentpath == path) {
        } else if (path.startsWith(currentpath!!)) {
            // to child
            FileUtils.pushPathFromParentToChild(queue, currentpath, path)
        } else if (currentpath!!.startsWith(path)) {
            // to parent
            var folder = queue.pop()
            while (folder.path != path) {
                // FIXME java.util.EmptyStackException
                folder = queue.pop()
            }
            folder = queue.push(folder)
        } else {
            val samePath = Helper.getSamePath(currentpath!!, path)

            // to parent
            var folder = queue.pop()
            while (folder.path != samePath) {
                folder = queue.pop()
            }
            queue.push(folder)

            // to child
            FileUtils.pushPathFromParentToChild(queue, samePath, path)
        }
    }

    override fun render(vararg args: Any?) {
        try {
            val path = args[0] as String
            calcFolderQueue(path)

            // if(thumbTask!=null && !thumbTask.isCancelled())
            // {
            // thumbTask.cancel(false);
            // }
            // if(appTask!=null && !appTask.isCancelled())
            // {
            // appTask.cancel(false);
            // }

            listViewDataMapList.clear()
            // thumbMap.clear();

            val directory = File(path)
            // CmdTask task = new CmdTask();
            // task.execute(directory);
            //
            // if(directory.canRead()==false){
            // return;
            // }
            var files: Array<File>? = null
            files = directory.listFiles()

            if (files != null && files.size > 0) {
                val comparatorFile = FileComparator(core.mainPreferences.order, core
                        .mainPreferences.order2)
                try {
                    Arrays.sort(files, comparatorFile)
                } catch (e: Exception) {
                    LogUtils.error(e)
                }

                var map: HashMap<String, Any>? = null
                var desc: String? = null
                //                String descAdd = null;
                // Bitmap img = null;
                var resId: Int
                for (file in files) {
                    if (file.name.startsWith(".") && core.mainPreferences.isShowHidden == false)
                        continue

                    map = HashMap()
                    map[FileAdapter.NAME] = file.name
                    map[FileAdapter.FILE_PATH] = file.path

                    if (core.clipper.copys.contains(file)) {
                        map[FileAdapter.SELECT] = 1
                    } else {
                        map[FileAdapter.SELECT] = 0
                    }

                    if (file.isDirectory) {
                        if (file.canRead() && file.canWrite()) {
                            if (isLight) {

                                // img =
                                // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder)).getBitmap();
                                resId = R.drawable.format_folder
                            } else {
                                resId = R.drawable.format_folder_dark
                                // img =
                                // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder_dark)).getBitmap();
                            }
                        } else {
                            if (isLight) {
                                resId = R.drawable.format_folder_lock
                                // img =
                                // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder_lock)).getBitmap();
                            } else {
                                resId = R.drawable.format_folder_lock_dark
                                // img =
                                // ((BitmapDrawable)getResources().getDrawable(R.drawable.format_folder_lock_dark)).getBitmap();
                            }
                        }
                        desc = Helper.getDes(file.lastModified())
                    } else {
                        desc = Helper.getDes(file.length(), file.lastModified())
                        //                        descAdd = Helper.getDescAdd(file, this.getApplicationContext());
                        //                        desc = desc + descAdd;
                        val D = Helper.setFileIcon(file.name, isLight)
                        resId = D
                        // img =
                        // ((BitmapDrawable)getResources().getDrawable(D)).getBitmap();
                    }
                    map["img"] = file.hashCode()
                    map[FileAdapter.RES_ID] = resId
                    // thumbMap.put(file.hashCode(), img);
                    map[FileAdapter.DESC] = desc

                    listViewDataMapList.add(map)
                }
            }

            currentpath = directory.path
            title = currentpath
            core.saveCurrentPath(currentpath)

            // listViewAdapter.notifyDataSetChanged();

            listview = listView
            listview!!.onItemClickListener = this
            listview!!.onItemLongClickListener = this
            listview!!.setOnScrollListener(object : OnScrollListener {

                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        // FIXME java.util.EmptyStackException
                        val f = queue.peek()
                        f.pos = listview!!.firstVisiblePosition
                        val v = listview!!.getChildAt(0)
                        f.offset = v?.top ?: 0
                    }
                }

                override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
            })

            // move to pos
            val folder = queue.peek()
            if (folder.pos != 0) {
                if (listview is ListView)
                    (listview as ListView).setSelectionFromTop(folder.pos, folder.offset)
                else
                    listview!!.setSelection(folder.pos)
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

            setNavigationBar(core.rootName, currentpath!!.split(Core.PATH_SPLIT.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(), core.rootPath)
            renderActionBar(core.mode)
        } catch (e: Exception) {
            LogUtils.error(e)
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    override fun onOperationStart(operationType: Int, f: File?, clipper: Clipper) {
        when (operationType) {
            Core.OPERATION_TYPE_COPY -> {

                val copyTask = CopyTask(core, f?.path, clipper, this, this)
                copyTask.execute()
                this.dialog = ProgressDialog.show(this, null, resources.getString(R.string.copy_files), false, true
                ) { copyTask.cancel(false) }
            }

            Core.OPERATION_TYPE_MOVE -> {
                val moveTask = MoveTask(core, f?.path, clipper, this, this)
                moveTask.execute()
                this.dialog = ProgressDialog.show(this, null, resources.getString(R.string.move_files), false, true
                ) { moveTask.cancel(false) }
            }
            Core.OPERATION_TYPE_DELETE -> {
                val deleteTask = DeleteTask(core, this, clipper, this, this)
                deleteTask.execute()
                this.dialog = ProgressDialog.show(this, null, resources.getString(R.string.delete_files), false, true
                ) { deleteTask.cancel(false) }
            }
        }

    }

    override fun onOperationCancel() {
        if (dialog != null) {
            // FIXME View not attached to window manager
            dialog!!.dismiss()
            dialog = null
        }
    }

    override fun onOperationUpdate(operationType: Int, message: String) {
        if (dialog != null) {
            dialog!!.setMessage(message)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == return_code_nothing) {

        } else if (resultCode == return_code_local_path) {
            render(data.getStringExtra("Path"))
        } else if (resultCode == return_code_favourities) {
            render(data.getStringExtra("Path"))
        } else if (resultCode == return_code_exit) {
            finish()
        } else if (resultCode == return_paste) {
            onRefresh(false, Core.MODE_PASTE, RefreshType.SELECT_RESET, true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == AppPermissions.DANGEROUS_PERMISSIONS_REQUEST_CODE) {
            onStoragePermissionsResult(requestCode, permissions, grantResults)
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun infoStoragePerms() {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.permission_before_grant)
            setPositiveButton(android.R.string.ok) { _, _ ->
                requestStoragePermissions(this@FileListActivity)
            }
        }.show()
    }

    override fun onStoragePermsGranted(requestCode: Int) {
        render(currentpath)
    }

    override fun onStoragePermsRejected() {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.permission_after_deny)
            setPositiveButton(R.string.permission_grant) { _, _ ->
                openAppDetail(this@FileListActivity, packageName)
            }
        }.show()
    }

    private fun openAppDetail(activity: Activity, packageName: String) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

}
