package com.seazon.fo.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.seazon.fo.Core;
import com.seazon.fo.DensityUtil;
import com.seazon.fo.Favorite;
import com.seazon.fo.FavoritesConfig;
import com.seazon.fo.R;
import com.seazon.fo.RefreshType;
import com.seazon.fo.entity.Clipper;
import com.seazon.fo.entity.MainPreferences;
import com.seazon.fo.listener.RefreshListener;
import com.seazon.fo.menu.BaseAction;
import com.seazon.fo.task.OperationUpdateCallback;
import com.seazon.fo.view.selector.FoSelector;
import com.seazon.slidelayout.SlideActivity;
import com.seazon.slidelayout.SlideLayout;

public abstract class FoSlideActivity extends SlideActivity implements RefreshListener, OnClickListener,
        OnTouchListener, FoMode, OperationUpdateCallback {

    private final static int TYPE_INVALID = -1;
    private final static int TYPE_TITLE = 0;
    private final static int TYPE_LOCAL_PATH = 1;
    private final static int TYPE_FAVORITES = 2;
    private final static int TYPE_VIEW = 4;
    private final static int TYPE_ORDER = 5;
    private final static int TYPE_SETTING = 3;
    private final static int TYPE_EXIT = 6;

    private int type = TYPE_INVALID;
    private HashMap<String, Object> map = null;
    protected boolean inner = true;

    public List<Map<String, Object>> listViewDataMapList = new LinkedList<Map<String, Object>>();
    protected FileAdapter listViewAdapter;
    // protected SparseArray<Bitmap> thumbMap = new SparseArray<Bitmap>();

    private List<Map<String, Object>> leftSideListViewDataMapList = new LinkedList<Map<String, Object>>();
    private LeftSideAdapter leftSideListViewAdapter;

    public class LeftSideAdapter extends SimpleAdapter {
        private LayoutInflater inflater;

        public LeftSideAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
                int[] to) {
            super(context, data, resource, from, to);
            this.inflater = LayoutInflater.from(context);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View myView = null;

            final HashMap<String, Object> map = (HashMap<String, Object>) leftSideListViewDataMapList
                    .get((int) position);
            final int type = (Integer) map.get("Type");
            final String name = (String) map.get("Name");

            if (convertView != null && (Integer) convertView.getTag() == type) {
                myView = convertView;
            } else {
                switch (type) {
                case TYPE_TITLE:
                    myView = inflater.inflate(R.layout.l_title_row, null);
                    break;
                case TYPE_FAVORITES:
                    myView = inflater.inflate(R.layout.l_favorite_row, null);
                    break;
                default:
                    myView = inflater.inflate(R.layout.l_row, null);
                    break;
                }
                myView.setTag(type);
            }

            TextView feedTitleView = null;

            switch (type) {
            case TYPE_TITLE:
                break;
            case TYPE_FAVORITES:
                feedTitleView = (TextView) myView.findViewById(R.id.nameView);
                feedTitleView.setBackgroundDrawable(FoSelector.side(FoSlideActivity.this));
                View deleteView = myView.findViewById(R.id.deleteView);
                deleteView.setBackgroundDrawable(FoSelector.side(FoSlideActivity.this));
                deleteView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {

                        final File file = new File((String) map.get("Path"));

                        AlertDialog.Builder ab = new AlertDialog.Builder(FoSlideActivity.this);
                        ab.setTitle(R.string.common_confirm);
                        ab.setMessage(String.format(getResources().getString(R.string.operator_del_favorites_confirm),
                                "'" + file.getName() + "'"));
                        ab.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                FavoritesConfig.delFavorite(file.getPath());
                                onRefreshSide();
                                Toast.makeText(FoSlideActivity.this, R.string.operator_del_favorites_successful,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(android.R.string.cancel, null);
                        ab.show();
                    }
                });

                break;
            default:
                feedTitleView = (TextView) myView.findViewById(R.id.nameView);
                feedTitleView.setBackgroundDrawable(FoSelector.side(FoSlideActivity.this));
                break;
            }

            feedTitleView = (TextView) myView.findViewById(R.id.nameView);
            feedTitleView.setText(name);

            return myView;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void onCreateSetCenterContentView(int resource) {
        setSlideLayoutContentView(R.layout.filelist, R.layout.l);

        setLeftSideView();
    }

    protected void onStart() {
        super.onStart();

        renderLeftSide();
    }

    private List<BaseAction> menuList;

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            int whichScreen = SlideLayout.SCREEN_CENTER;
            switch (slideLayout.currentScreen) {
            case SlideLayout.SCREEN_LEFT:
                whichScreen = SlideLayout.SCREEN_CENTER;
                break;
            case SlideLayout.SCREEN_CENTER:
                whichScreen = SlideLayout.SCREEN_LEFT;
                onSlideStart(SlideLayout.SCREEN_LEFT);
                break;
            }

            slideLayout.scrollToScreen(whichScreen);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (slideLayout.currentScreen) {
            case SlideLayout.SCREEN_LEFT:
                slideLayout.scrollToScreen(SlideLayout.SCREEN_CENTER);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    protected boolean exit(String path) {
        if (Core.PATH_ROOT_STD.equals(path)) {
            return true;
        } else {
            return false;
        }
    }

    public abstract void render(Object... args);

    public void onSlideStop() {
        Intent intent = null;
        switch (type) {
        case TYPE_LOCAL_PATH:
            File home = new File((String) map.get("Path"));
            if (!home.exists()) {
                Toast.makeText(this, R.string.operator_file_no_exist, Toast.LENGTH_SHORT).show();
                return;
            }

            if (this instanceof FileListActivity) {
                render(home.getPath());
            } else {
                if (inner) {
                    Intent intent2 = new Intent();
                    intent2.putExtra("Path", home.getPath());
                    setResult(FileListActivity.return_code_local_path, intent2);
                } else {
                    Intent intent2 = new Intent();
                    intent2.putExtra("Path", home.getPath());
                    intent2.setClass(this, FileListActivity.class);
                    startActivity(intent2);
                }
                finish();
            }
            break;
        case TYPE_FAVORITES:
            File favorite = new File((String) map.get("Path"));
            if (!favorite.exists()) {
                Toast.makeText(this, R.string.operator_file_no_exist, Toast.LENGTH_SHORT).show();
                break;
            }

            if (this instanceof FileListActivity) {
                render(favorite.getPath());
            } else {
                if (inner) {
                    Intent intent2 = new Intent();
                    intent2.putExtra("Path", favorite.getPath());
                    setResult(FileListActivity.return_code_favourities, intent2);
                } else {
                    Intent intent2 = new Intent();
                    intent2.setClass(this, FileListActivity.class);
                    intent2.putExtra("Path", favorite.getPath());
                    startActivity(intent2);
                }
                finish();
            }
            break;
        case TYPE_VIEW:
            MainPreferences mainPreferences = core.getMainPreferences();
            view = mainPreferences.getView();
            if (Core.VIEW_LIST.equals(view)) {
                view = Core.VIEW_ICONS;
            } else {
                view = Core.VIEW_LIST;
            }
            mainPreferences.setView((String) view);
            core.saveMainPreferences(mainPreferences);
            FileIconCache.clear();
            onRefresh(false, core.mode, RefreshType.RENDER, true);
            break;
        case TYPE_ORDER:
            order = core.getMainPreferences().getOrder();
            showOrderDialog();
            break;
        case TYPE_EXIT:
            core.saveCurrentPath(core.getMainPreferences().getHome());
            if (this instanceof FileListActivity) {
            } else {
                setResult(FileListActivity.return_code_exit);
            }
            finish();
            break;
        case TYPE_SETTING:
            intent = new Intent();
            intent.setClass(this, MainPreferencesActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            break;
        }

        type = TYPE_INVALID;
        map = null;
    }

    // ######## FoModel ########

    private int calcMenuCountsMax() {
        // 测量长度
        DisplayMetrics dm = new DisplayMetrics();
        dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        DensityUtil du = new DensityUtil(this);
        width = du.px2dip(width);
        // 计算可以放几个
        int menuCountsMax = width / Core.ACTIONBAR_WIDTH;
        return menuCountsMax;
    }

    private TextView selectNumberView = null;

    protected void updateSelectionNumber(int count) {
        // for (BaseAction ba : menuList) {
        if (selectNumberView != null)
            selectNumberView.setText(String.valueOf(count));
        // }
    }

    private void renderActionBarSub(int menuCountsMax) {
        // set layout
        LinearLayout actionBar = (LinearLayout) findViewById(R.id.actionBar);
        actionBar.removeAllViews();

        LayoutInflater inflater = this.getLayoutInflater();

        DensityUtil du = new DensityUtil(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(du.dip2px(Core.ACTIONBAR_WIDTH),
                du.dip2px(Core.ACTIONBAR_HEIGHT));
        // int paddingWidth = du.dip2px(8);
        // int paddingHeight = du.dip2px(4);
        // need put how many
        int menuCounts = menuList.size();
        // calculate need more button or not
        boolean needMoreMenu = menuCounts <= menuCountsMax ? false : true;
        final int showMenuCounts = menuCounts <= menuCountsMax ? menuCounts : (menuCountsMax - 1);
        // put
        View menu1;
        ImageView imenu1;
        for (int i = 0; i < showMenuCounts; ++i) {
            final BaseAction ba = menuList.get(i);
            if (ba.getType() == 2) {
                menu1 = inflater.inflate(R.layout.actionbar_barge, null);
                imenu1 = (ImageView) ((ViewGroup) menu1).getChildAt(0);
                // ba.setTextView((TextView)((ViewGroup)menu1).getChildAt(1));
                selectNumberView = (TextView) ((ViewGroup) menu1).getChildAt(1);
            } else {
                menu1 = inflater.inflate(R.layout.actionbar_normal, null);
                imenu1 = (ImageView) menu1;
            }
            // menu1.setPadding(paddingWidth, paddingHeight, paddingWidth,
            // paddingHeight);

            imenu1.setBackgroundDrawable(FoSelector.actionBar(this));
            imenu1.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    ba.onLongClick();
                    return false;
                }
            });
            imenu1.setImageResource(ba.getIcon());
            imenu1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ba.onActive();
                }
            });
            actionBar.addView(menu1, params);
            ba.setImageView(imenu1);
        }

        if (needMoreMenu) {
            menu1 = inflater.inflate(R.layout.actionbar_normal, null);
            imenu1 = (ImageView) menu1;
            // menu1 = new ImageView(this);
            // menu1.setPadding(paddingWidth, paddingHeight, paddingWidth,
            // paddingHeight);
            imenu1.setBackgroundDrawable(FoSelector.actionBar(this));
            imenu1.setImageResource(R.drawable.ic_menu_more);
            imenu1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActionBarMoreDialog dialog = new ActionBarMoreDialog(FoSlideActivity.this, menuList, showMenuCounts);
                    dialog.show();
                }
            });
            actionBar.addView(menu1, params);
        }
    }

    protected final void addMenu(String className) {
        BaseAction ba = core.am.newAction(className, this, this);
        menuList.add(ba);
    }

    protected final void updateMenu(String className) {
        for (BaseAction ba : menuList) {
            if (ba.getClass().getName().equals(className)) {
                ba.update();
                break;
            }
        }
    }

    public void renderActionBar(int mode) {
        int menuCountsMax = calcMenuCountsMax();

        menuList = new ArrayList<BaseAction>();
        initMenu(mode);

        renderActionBarSub(menuCountsMax);
        int size = core.getClipper().getCopys().size();
        updateSelectionNumber(size);
    }

    public abstract void initMenu(int mode);

    // ######## Left Side ########
    private String order;
    private String view;

    public void setLeftSideView() {
        leftSideListViewAdapter = new LeftSideAdapter(this, leftSideListViewDataMapList, R.layout.l_row,
                new String[] { "Name" }, new int[] { R.id.nameView });
        ListView linkHomeListView = (ListView) findViewById(R.id.leftSideListView);
        linkHomeListView.setAdapter(leftSideListViewAdapter);
        linkHomeListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                HashMap<String, Object> labelMap = (HashMap<String, Object>) leftSideListViewDataMapList
                        .get((int) arg3);
                type = (Integer) labelMap.get("Type");
                map = labelMap;
                slideLayout.scrollToScreen(SlideLayout.SCREEN_CENTER);
            }
        });
    }

    public void onRefreshSide() {
        renderLeftSide();
    }

    private void renderLeftSide() {
        String path = Environment.getExternalStorageDirectory().getPath();
        // FIXME Caused by: java.lang.IllegalArgumentException
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long availaBlocks = stat.getAvailableBlocks();

        String format1 = Formatter.formatFileSize(this, totalBlocks * blockSize);
        String format2 = Formatter.formatFileSize(this, availaBlocks * blockSize);
        String a = String.format(getResources().getString(R.string.side_main_storage_size), format2 + " / " + format1);
        ((TextView) findViewById(R.id.mainStorageSizeView)).setText(a);

        this.leftSideListViewDataMapList.clear();

        Map<String, Object> map = null;

        map = new HashMap<String, Object>();
        map.put("Type", TYPE_TITLE);
        map.put("Name", getResources().getString(R.string.side_path_local));
        leftSideListViewDataMapList.add(map);

        map = new HashMap<String, Object>();
        map.put("Type", TYPE_LOCAL_PATH);
        map.put("Name", getResources().getString(R.string.side_path_local_home));
        map.put("Path", core.getMainPreferences().getHome());
        leftSideListViewDataMapList.add(map);

        List<Favorite> list = FavoritesConfig.getFavorites();
        if (list == null) {
            list = new ArrayList<Favorite>();
            // new
            Favorite f = null;

            f = new Favorite();
            f.name = getResources().getString(R.string.side_path_local_dcim);
            f.path = Core.PATH_DCIM;
            FavoritesConfig.saveFavorite(f);
            list.add(f);

            f = new Favorite();
            f.name = getResources().getString(R.string.side_path_local_download);
            f.path = Core.PATH_DOWNLOAD;
            FavoritesConfig.saveFavorite(f);
            list.add(f);

            f = new Favorite();
            f.name = getResources().getString(R.string.side_path_local_movies);
            f.path = Core.PATH_MOVIES;
            FavoritesConfig.saveFavorite(f);
            list.add(f);

            f = new Favorite();
            f.name = getResources().getString(R.string.side_path_local_music);
            f.path = Core.PATH_MUSIC;
            FavoritesConfig.saveFavorite(f);
            list.add(f);

            f = new Favorite();
            f.name = getResources().getString(R.string.side_path_local_pictures);
            f.path = Core.PATH_PICTURES;
            FavoritesConfig.saveFavorite(f);
            list.add(f);
        }

        // sort
        Collections.sort(list, new Comparator<Favorite>() {
            @Override
            public int compare(Favorite lhs, Favorite rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });

        for (Favorite f : list) {
            map = new HashMap<String, Object>();
            map.put("Type", TYPE_FAVORITES);
            map.put("Name", f.name);
            map.put("Path", f.path);
            leftSideListViewDataMapList.add(map);
        }

        map = new HashMap<String, Object>();
        map.put("Type", TYPE_TITLE);
        map.put("Name", getResources().getString(R.string.side_others));
        leftSideListViewDataMapList.add(map);

        map = new HashMap<String, Object>();
        map.put("Type", TYPE_VIEW);
        map.put("Name", getResources().getString(R.string.side_others_view));
        leftSideListViewDataMapList.add(map);

        map = new HashMap<String, Object>();
        map.put("Type", TYPE_ORDER);
        map.put("Name", getResources().getString(R.string.side_others_order));
        leftSideListViewDataMapList.add(map);

        map = new HashMap<String, Object>();
        map.put("Type", TYPE_SETTING);
        map.put("Name", getResources().getString(R.string.side_others_preferences));
        leftSideListViewDataMapList.add(map);

        map = new HashMap<String, Object>();
        map.put("Type", TYPE_EXIT);
        map.put("Name", getResources().getString(R.string.side_others_exit));
        leftSideListViewDataMapList.add(map);

        this.leftSideListViewAdapter.notifyDataSetChanged();
    }

    protected AbsListView getListView() {

        AbsListView listView = null;

        String viewType = core.getMainPreferences().getView();
        listView = (AbsListView) findViewById(R.id.gridView);
        listViewAdapter = new FileAdapter(this, listViewDataMapList, viewType, listView);
        ((GridView) listView).setAdapter(listViewAdapter);
        if (viewType.equals(Core.VIEW_LIST)) {
            ((GridView) listView).setColumnWidth(core.du.dip2px(288));
        } else if (viewType.equals(Core.VIEW_ICONS)) {
            ((GridView) listView).setColumnWidth(core.du.dip2px(96));
        }

        listView.setEmptyView(findViewById(android.R.id.empty));

        return listView;
    }

    private void showOrderDialog() {
        int checkedItem = 0;
        if (Core.ORDER_NAME.equals(order)) {
            checkedItem = 0;
        } else if (Core.ORDER_TYPE.equals(order)) {
            checkedItem = 1;
        } else if (Core.ORDER_SIZE.equals(order)) {
            checkedItem = 2;
        } else if (Core.ORDER_DATEMODIFIED.equals(order)) {
            checkedItem = 3;
        }

        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(R.string.setting_view_order);
        ab.setSingleChoiceItems(R.array.entries_list_setting_order, checkedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case 0:
                    order = Core.ORDER_NAME;
                    break;
                case 1:
                    order = Core.ORDER_TYPE;
                    break;
                case 2:
                    order = Core.ORDER_SIZE;
                    break;
                case 3:
                    order = Core.ORDER_DATEMODIFIED;
                    break;
                }
            }
        });
        ab.setPositiveButton(R.string.setting_view_order2_asc, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                MainPreferences mainPreferences = core.getMainPreferences();
                mainPreferences.setOrder((String) order);
                mainPreferences.setOrder2(Core.ORDER2_ASC);

                core.saveMainPreferences(mainPreferences);

                onRefresh(false, core.mode, RefreshType.RENDER, true);
            }
        }).setNegativeButton(R.string.setting_view_order2_desc, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                MainPreferences mainPreferences = core.getMainPreferences();
                mainPreferences.setOrder((String) order);
                mainPreferences.setOrder2(Core.ORDER2_DESC);

                core.saveMainPreferences(mainPreferences);

                onRefresh(false, core.mode, RefreshType.RENDER, true);
            }
        });
        AlertDialog ad = ab.create();
        ad.setCanceledOnTouchOutside(true);
        ab.show();
    }

    // ######## Navigation Bar ########

    protected Map<String, Integer> map1 = new HashMap<String, Integer>();
    protected Map<Integer, String> map2 = new HashMap<Integer, String>();

    private final Handler mHandler = new Handler();
    private Runnable mScrollToButton = new Runnable() {
        public void run() {
            LinearLayout layout = (LinearLayout) findViewById(R.id.navBar);
            HorizontalScrollView sView = (HorizontalScrollView) findViewById(R.id.scrollView);
            int off = layout.getMeasuredWidth() - sView.getWidth();
            if (off > 0) {
                sView.scrollTo(off, 0);// 改变滚动条的位置
            }
        }
    };

    protected void setNavigationBar(String rootName, String[] a, String rootPath) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.navBar);
        // delete all button
        layout.removeAllViews();
        // delete map1 and map2
        this.map1.clear();
        this.map2.clear();

        // generate button one by one

        // 设置线性布局的属性
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);

        int index = 0;

        ImageView imageView = null;
        Button b = null;

        // 添加头
        imageView = new ImageView(this);
        imageView.setBackgroundResource(R.drawable.bar11);
        imageView.setId(index);
        layout.addView(imageView, params);
        index++;

        String aa = "";
        for (int i = 0; i < a.length; ++i) {
            // 添加中段
            b = new Button(this);
            if (a[i].equals("")) {
                b.setText(rootName);
                map1.put(rootPath, index);
                map2.put(index, rootPath);
            } else {
                aa += Core.PATH_SPLIT + a[i];
                b.setText(a[i]);
                map1.put(aa, index);
                map2.put(index, aa);
            }
            b.setBackgroundResource(R.drawable.bar11);
            b.setTextSize(16);
            if (core.isLightTheme())
                b.setTextColor(getResources().getColor(R.color.light_text_nav));
            else
                b.setTextColor(getResources().getColor(R.color.dark_text_nav));
            b.setId(index);
            b.setOnClickListener(this);
            b.setOnTouchListener(this);
            layout.addView(b, params);
            index++;

            // 添加尾
            imageView = new ImageView(this);
            if (i == a.length - 1)
                imageView.setBackgroundResource(R.drawable.bar24);
            else
                imageView.setBackgroundResource(R.drawable.bar21);
            imageView.setId(index);
            layout.addView(imageView, params);
            index++;
        }

        if (a.length == 0) {
            // 添加中段
            b = new Button(this);
            b.setText(rootName);
            map1.put(rootPath, index);
            map2.put(index, rootPath);
            b.setBackgroundResource(R.drawable.bar11);
            b.setTextSize(16);
            if (core.isLightTheme())
                b.setTextColor(getResources().getColor(R.color.light_text_nav));
            else
                b.setTextColor(getResources().getColor(R.color.dark_text_nav));
            b.setId(index);
            b.setOnClickListener(this);
            b.setOnTouchListener(this);
            layout.addView(b, params);
            index++;

            // 添加尾
            imageView = new ImageView(this);
            imageView.setBackgroundResource(R.drawable.bar24);
            imageView.setId(index);
            layout.addView(imageView, params);
            index++;
        }

        mHandler.post(mScrollToButton);// 传递一个消息进行滚动
    }

    abstract protected void onNavBarClick(View v, String path);

    public void selectAll() {

    }

    public boolean onTouch(View v, MotionEvent event) {
        int length = map1.size();
        for (Integer i : map1.values()) {
            if (v.getId() == i) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((Button) findViewById(i)).setBackgroundResource(R.drawable.bar12);

                    if (i == 1)
                        ((ImageView) findViewById(i - 1)).setBackgroundResource(R.drawable.bar12);
                    else
                        ((ImageView) findViewById(i - 1)).setBackgroundResource(R.drawable.bar23);

                    if (i + 1 == length * 2)
                        ((ImageView) findViewById(i + 1)).setBackgroundResource(R.drawable.bar25);
                    else
                        ((ImageView) findViewById(i + 1)).setBackgroundResource(R.drawable.bar22);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((Button) findViewById(i)).setBackgroundResource(R.drawable.bar11);

                    if (i == 1)
                        ((ImageView) findViewById(i - 1)).setBackgroundResource(R.drawable.bar11);
                    else
                        ((ImageView) findViewById(i - 1)).setBackgroundResource(R.drawable.bar21);

                    if (i + 1 == length * 2)
                        ((ImageView) findViewById(i + 1)).setBackgroundResource(R.drawable.bar24);
                    else
                        ((ImageView) findViewById(i + 1)).setBackgroundResource(R.drawable.bar21);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    ((Button) findViewById(i)).setBackgroundResource(R.drawable.bar11);

                    if (i == 1)
                        ((ImageView) findViewById(i - 1)).setBackgroundResource(R.drawable.bar11);
                    else
                        ((ImageView) findViewById(i - 1)).setBackgroundResource(R.drawable.bar21);

                    if (i + 1 == length * 2)
                        ((ImageView) findViewById(i + 1)).setBackgroundResource(R.drawable.bar24);
                    else
                        ((ImageView) findViewById(i + 1)).setBackgroundResource(R.drawable.bar21);
                }
                return false;
            }
        }
        return false;
    }

    public void onOperationStart(int operationType, File f, Clipper clipper) {
    }

    public void onOperationCancel() {
    }

    public void onOperationUpdate(int operationType, String message) {
    }

}
